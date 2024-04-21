package managers.tasks;

import managers.Managers;
import managers.history.HistoryManager;
import tasks.Epic;
import tasks.enums.Status;
import tasks.Subtask;
import tasks.Task;
import tasks.enums.TaskTypes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> taskPool = new HashMap<>();
    protected final Map<Integer, Subtask> subtaskPool = new HashMap<>();
    protected final Map<Integer, Epic> epicPool = new HashMap<>();
    protected int lastTaskId = 0;
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    protected TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));

    @Override
    public List<Task> getTasksList() {
        return new ArrayList<>(taskPool.values());
    }

    @Override
    public List<Epic> getEpicsList() {
        return new ArrayList<>(epicPool.values());
    }

    @Override
    public List<Subtask> getSubtasksList() {
        return new ArrayList<>(subtaskPool.values());
    }

    @Override
    public void removeTaskPool() {
        clearHistoryWhenRemovingPool(taskPool);
        taskPool.values()
                .forEach(task -> prioritizedTasks.remove(task));
        taskPool.clear();
    }

    @Override
    public void removeEpicPool() {
        clearHistoryWhenRemovingPool(epicPool);
        epicPool.clear();
        removeSubtaskPool();
    }

    @Override
    public void removeSubtaskPool() {
        clearHistoryWhenRemovingPool(subtaskPool);
        subtaskPool.values()
                .forEach(subtask -> prioritizedTasks.remove(subtask));
        subtaskPool.clear();
        //Пересмотр статуса эпиков согласно логике программы
        epicPool.values()
                .forEach(epic -> {
                    epic.getSubTasksIds().clear();
                    epicStatusControl(epic);
                    epicTimeControl(epic);
                });
    }

    private void clearHistoryWhenRemovingPool(Map<Integer, ? extends Task> pool) {
        if (!pool.isEmpty()) {
            pool.keySet()
                    .forEach(historyManager::remove);
        }
    }

    @Override
    public Task getTask(int id) {
        return getTask(id, true);
    }

    //Метод вызывается из remove и update, по ТЗ должна собираться история только просмотренных пользователем задач
    private Task getTask(int id, boolean shouldBeAddToHistory) {
        Task task = null;
        if (taskPool.containsKey(id)) {
            task = new Task(taskPool.get(id));
        } else if (epicPool.containsKey(id)) {
            task = new Epic(epicPool.get(id));
        } else if (subtaskPool.containsKey(id)) {
            task = new Subtask(subtaskPool.get(id));
        }
        if (task != null && shouldBeAddToHistory) {
            historyManager.addTaskToHistory(task);
        }
        return task;
    }

    @Override
    public boolean createTask(Task task) {
        if (task != null) {
            lastTaskId++;
            task.setId(lastTaskId);
            TaskTypes taskType = task.getTaskType();
            switch (taskType) {
                case TASK:
                    Task taskToDB = new Task(task);
                    if (!isTaskCrossingWithPrioritized(taskToDB)) {
                        taskPool.put(lastTaskId, taskToDB);
                        checkAndPutInPrioritizedTasks(taskToDB);
                        return true;
                    }
                    return false;
                case EPIC:
                    Epic epic = (Epic) task;
                    epicPool.put(lastTaskId, new Epic(epic));
                    return true;
                case SUBTASK:
                    Subtask subtask = (Subtask) task;
                    int connectedEpicId = subtask.getEpicId();
                    Epic connectedEpic = epicPool.get(connectedEpicId);
                    Subtask subtaskToDB = new Subtask(subtask);
                    if (!isTaskCrossingWithPrioritized(subtaskToDB)) {
                        subtaskPool.put(lastTaskId, subtaskToDB);
                        checkAndPutInPrioritizedTasks(subtaskToDB);
                        connectedEpic.addSubTasks(lastTaskId);
                        epicStatusControl(connectedEpic);
                        epicTimeControl(connectedEpic);
                        return true;
                    }
                    return false;
            }
        }
        return false;
    }

    @Override
    public boolean updateTask(Task task) {
        if (task != null) {
            int taskId = task.getId();
            TaskTypes taskType = task.getTaskType();
            Task oldTask = getTask(taskId, false);
            // Проверка что есть то, что обновлять и не позволяет изменить тип задачи, напр. обновить эпик на задачу
            if (oldTask != null && taskType.equals(oldTask.getTaskType())) {
                switch (taskType) {
                    case TASK:
                        Task oldTaskInDB = taskPool.get(taskId);
                        Task newTaskToDB = new Task(task);
                        if (!isTaskCrossingWithPrioritized(newTaskToDB)) {
                            taskPool.put(taskId, newTaskToDB);
                            prioritizedTasks.remove(oldTaskInDB);
                            checkAndPutInPrioritizedTasks(newTaskToDB);
                            return true;
                        }
                        return false;
                    case EPIC:
                        Epic epic = (Epic) task;
                        epicPool.put(taskId, new Epic(epic));
                        return true;
                    case SUBTASK:
                        Subtask subtask = (Subtask) task;
                        Subtask oldSubtaskFromDB = subtaskPool.get(taskId);
                        Subtask newSubtaskToDB = new Subtask(subtask);
                        if (!isTaskCrossingWithPrioritized(newSubtaskToDB)) {
                            subtaskPool.put(taskId, newSubtaskToDB);
                            prioritizedTasks.remove(oldSubtaskFromDB);
                            checkAndPutInPrioritizedTasks(newSubtaskToDB);
                            int connectedEpicId = subtask.getEpicId();
                            epicStatusControl(epicPool.get(connectedEpicId));
                            epicTimeControl(epicPool.get(connectedEpicId));
                            return true;
                        }
                        return false;
                }
            }
        }
        return false;
    }

    @Override
    public void removeTask(int id) {
        Task task = getTask(id, false);
        if (task != null) {
            historyManager.remove(id);
            TaskTypes taskType = task.getTaskType();
            switch (taskType) {
                case TASK:
                    Task oldTaskFromDB = taskPool.remove(id);
                    prioritizedTasks.remove(oldTaskFromDB);
                    break;
                case EPIC:
                    // Удаляются эпик из пулла, удаляются все связанные подзадачи
                    Epic epic = epicPool.remove(id);
                    epic.getSubTasksIds().stream()
                            .map(subtaskPool::remove)
                            .forEach(oldSubTaskInDBConnectedToEpic -> prioritizedTasks.remove(oldSubTaskInDBConnectedToEpic));
                    break;
                case SUBTASK:
                    // Удаляется подзадача из пулла, из эпика, проверяется статус эпика
                    Subtask subtask = subtaskPool.remove(id);
                    prioritizedTasks.remove(subtask);
                    Epic connectedEpic = epicPool.get(subtask.getEpicId());
                    connectedEpic.getSubTasksIds().remove((Integer) id);
                    epicStatusControl(connectedEpic);
                    epicTimeControl(connectedEpic);
                    break;
            }
        }
    }

    @Override
    public List<Subtask> getEpicSubtasksList(int id) {
        ArrayList<Subtask> subtasks = new ArrayList<>();
        if (epicPool.containsKey(id)) {
            Epic epic = epicPool.get(id);
            subtasks = epic.getSubTasksIds().stream()
                    .map(subtaskPool::get)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return subtasks;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void epicStatusControl(Epic epic) {
        if (epic != null) {
            List<Integer> subTasksId = epic.getSubTasksIds();
            boolean isSubtasksCompleate = true;
            boolean isAllSubtasksNew = true;
            for (Integer subtaskId : subTasksId) {
                Subtask subtask = subtaskPool.get(subtaskId);
                if (subtask.getStatus() != Status.DONE) {
                    isSubtasksCompleate = false;
                }
                if ((subtask.getStatus() != Status.NEW)) {
                    isAllSubtasksNew = false;
                }
            }
            if (subTasksId.isEmpty() || isAllSubtasksNew) {
                epic.setStatus(Status.NEW);
            } else if (isSubtasksCompleate) {
                epic.setStatus(Status.DONE);
            } else {
                epic.setStatus(Status.IN_PROGRESS);
            }
        }
    }

    private void epicTimeControl(Epic epic) {
        if (epic != null) {
            List<Integer> subTasksId = epic.getSubTasksIds();
            if (!subTasksId.isEmpty()) {
                Subtask subtask = subtaskPool.get(subTasksId.get(0));
                LocalDateTime startTime = subtask.getStartTime();
                LocalDateTime endTime = subtask.getEndTime();
                for (int i = 1; i < subTasksId.size(); i++) {
                    subtask = subtaskPool.get(subTasksId.get(i));
                    startTime = (startTime.isBefore(subtask.getStartTime())) ? startTime : subtask.getStartTime();
                    endTime = (endTime.isAfter(subtask.getEndTime())) ? endTime : subtask.getEndTime();
                }
                epic.setStartTime(startTime);
                epic.setEndTime(endTime);
                epic.setDuration(Duration.between(startTime, endTime));
            } else {
                epic.setStartTime(null);
                epic.setDuration(null);
                epic.setEndTime(null);
            }
        }
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private void checkAndPutInPrioritizedTasks(Task task) {
        if (task != null) {
            if (task.getStartTime() != null && task.getDuration() != null) {
                prioritizedTasks.add(task);
            }
        }
    }

    private boolean isTaskCrossingWithPrioritized(Task task) {
        return prioritizedTasks.stream()
                .anyMatch(prioritizedTask -> prioritizedTask.isTasksPeriodCrossing(task));

    }
}