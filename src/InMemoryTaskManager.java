import tasks.Epic;
import tasks.enums.Status;
import tasks.Subtask;
import tasks.Task;
import tasks.enums.TaskTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> taskPool = new HashMap<>();
    private final Map<Integer, Subtask> subtaskPool = new HashMap<>();
    private final Map<Integer, Epic> epicPool = new HashMap<>();
    private int lastTaskId = 0;
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public List<Task> getTasksList() {
        ArrayList<Task> tasksList = new ArrayList<>();
        for (Map.Entry<Integer, Task> pair : taskPool.entrySet()) {
            tasksList.add(pair.getValue());
        }
        return tasksList;
    }

    @Override
    public List<Epic> getEpicsList() {
        ArrayList<Epic> epicsList = new ArrayList<>();
        for (Map.Entry<Integer, Epic> pair : epicPool.entrySet()) {
            epicsList.add(pair.getValue());
        }
        return epicsList;
    }

    @Override
    public List<Subtask> getSubtasksList() {
        ArrayList<Subtask> subtasksList = new ArrayList<>();
        for (Map.Entry<Integer, Subtask> pair : subtaskPool.entrySet()) {
            subtasksList.add(pair.getValue());
        }
        return subtasksList;
    }

    @Override
    public void removeTaskPool() {
        clearHistoryWhenRemovingPool(taskPool);
        taskPool.clear();
    }

    @Override
    public void removeEpicPool() {
        clearHistoryWhenRemovingPool(epicPool);
        clearHistoryWhenRemovingPool(subtaskPool);
        epicPool.clear();
        subtaskPool.clear();
    }

    @Override
    public void removeSubtaskPool() {
        clearHistoryWhenRemovingPool(subtaskPool);
        subtaskPool.clear();
        //Пересмотр статуса эпиков согласно логике программы
        for (Integer id : epicPool.keySet()) {
            Epic epic = epicPool.get(id);
            epic.getSubTasksIds().clear();
            epicStatusControl(epic);
        }
    }

    private void clearHistoryWhenRemovingPool (Map<Integer, ? extends Task> pool) {
        if (!pool.isEmpty()) {
            for (int id : pool.keySet()) {
                historyManager.remove(id);
            }
        }
    }

    @Override
    public Task getTask(int id) {
        return getTask(id, true);
    }

    //Метод вызывается из remove и update, по ТЗ должна собираться история только просмотренных пользователем задач
    private Task getTask(int id, boolean shouldBeAddToHistory) {
        Task task;
        if (taskPool.containsKey(id)) {
            task = taskPool.get(id);
        } else if (epicPool.containsKey(id)) {
            task = epicPool.get(id);
        } else {
            task = subtaskPool.getOrDefault(id, null);
        }
        if (task != null && shouldBeAddToHistory) {
            historyManager.addTaskToHistory(task);
        }
        return task;
    }

    @Override
    public void createTask(Task task) {
        if (task != null) {
            lastTaskId++;
            task.setId(lastTaskId);
            TaskTypes taskType = task.getTaskType();
            switch (taskType) {
                case TASK:
                    taskPool.put(lastTaskId, task);
                    break;
                case EPIC:
                    Epic epic = (Epic) task;
                    epicPool.put(lastTaskId, epic);
                    break;
                case SUBTASK:
                    Subtask subtask = (Subtask) task;
                    int connectedEpicId = subtask.getEpicId();
                    Epic connectedEpic = epicPool.get(connectedEpicId);
                    subtaskPool.put(lastTaskId, subtask);
                    connectedEpic.addSubTasks(lastTaskId);
                    epicStatusControl(connectedEpic);
                    break;
            }
        }
    }

    @Override
    public void updateTask(Task task) {
        if (task != null) {
            int taskId = task.getId();
            TaskTypes taskType = task.getTaskType();
            Task oldTask = getTask(taskId, false);
            // Проверка что есть то, что обновлять и не позволяет изменить тип задачи, напр. обновить эпик на задачу
            if (oldTask != null && taskType.equals(oldTask.getTaskType())) {
                switch (taskType) {
                    case TASK:
                        taskPool.put(taskId, task);
                        break;
                    case EPIC:
                        epicPool.put(taskId, (Epic) task);
                        break;
                    case SUBTASK:
                        Subtask subtask = (Subtask) task;
                        subtaskPool.put(taskId, subtask);
                        int connectedEpicId = subtask.getEpicId();
                        epicStatusControl(epicPool.get(connectedEpicId));
                        break;
                }
            }
        }
    }

    @Override
    public void removeTask(int id) {
        Task task = getTask(id, false);
        if (task != null) {
            historyManager.remove(id);
            TaskTypes taskType = task.getTaskType();
            switch (taskType) {
                case TASK:
                    taskPool.remove(id);
                    break;
                case EPIC:
                    // Удаляются эпик из пулла, удаляются все связанные подзадачи
                    Epic epic = epicPool.remove(id);
                    for (Integer subtaskId : epic.getSubTasksIds()) {
                        subtaskPool.remove(subtaskId);
                    }
                    break;
                case SUBTASK:
                    // Удаляется подзадача из пулла, из эпика, проверяется статус эпика
                    Subtask subtask = subtaskPool.remove(id);
                    Epic connectedEpic = epicPool.get(subtask.getEpicId());
                    connectedEpic.getSubTasksIds().remove((Integer) id);
                    epicStatusControl(connectedEpic);
                    break;
            }
        }
    }

    @Override
    public List<Subtask> getEpicSubtasksList(int id) {
        ArrayList<Subtask> subtasks = new ArrayList<>();
        if (epicPool.containsKey(id)) {
            Epic epic = epicPool.get(id);
            for (Integer subtaskId : epic.getSubTasksIds()) {
                Subtask subtask = subtaskPool.get(subtaskId);
                subtasks.add(subtask);
            }
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
}