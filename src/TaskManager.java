import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TaskManager {
    private final HashMap<Integer, Task> taskPool = new HashMap<>();
    private final HashMap<Integer, Subtask> subtaskPool = new HashMap<>();
    private final HashMap<Integer, Epic> epicPool = new HashMap<>();
    private int lastTaskId = 0;

    public ArrayList<Task> getTasksList() {
        ArrayList<Task> tasksList = new ArrayList<>();
        for (Map.Entry<Integer, Task> pair : taskPool.entrySet()) {
            tasksList.add(pair.getValue());
        }
        return tasksList;
    }

    public ArrayList<Epic> getEpicsList() {
        ArrayList<Epic> epicsList = new ArrayList<>();
        for (Map.Entry<Integer, Epic> pair : epicPool.entrySet()) {
            epicsList.add(pair.getValue());
        }
        return epicsList;
    }

    public ArrayList<Subtask> getSubtasksList() {
        ArrayList<Subtask> subtasksList = new ArrayList<>();
        for (Map.Entry<Integer, Subtask> pair : subtaskPool.entrySet()) {
            subtasksList.add(pair.getValue());
        }
        return subtasksList;
    }

    public void removeTaskPool() {
        taskPool.clear();
    }

    public void removeEpicPool() {
        epicPool.clear();
        subtaskPool.clear();
    }

    public void removeSubtaskPool() {
        subtaskPool.clear();
        //Пересмотр статуса эпиков согласно логике программы
        for (Integer id : epicPool.keySet()) {
            Epic epic = epicPool.get(id);
            epic.getSubTasksId().clear();
            epicStatusControl(epic);
        }
    }

    public Task getTask(int id) {
        if (taskPool.containsKey(id)) {
            return taskPool.get(id);
        } else if (epicPool.containsKey(id)) {
            return epicPool.get(id);
        } else return subtaskPool.getOrDefault(id, null);
    }

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

    public void updateTask(Task task) {
        if (task != null) {
            int taskId = task.getId();
            TaskTypes taskType = task.getTaskType();
            Task oldTask = getTask(taskId);
            // Проверка что есть то, что обновлять и не позволяет изменить тип задачи, напр. обновить эпик на задачу
            if (oldTask != null && taskType.equals(oldTask.getTaskType())) {
                switch (taskType) {
                    case TASK:
                        taskPool.put(taskId, task);
                        break;
                    case EPIC:
                        updateEpic(taskId, (Epic) task, (Epic) oldTask);
                        break;
                    case SUBTASK:
                        updateSubtask(taskId, (Subtask) task, (Subtask) oldTask);
                        break;
                }
            }
        }
    }

    public void updateEpic(int taskId, Epic newEpic, Epic oldEpic) {
        //Проверка и отвязывание подзадач, которые отсутствуют в новой вариации эпика;
        ArrayList<Integer> subtasksToDisconnection = new ArrayList<>(oldEpic.getSubTasksId());
        for (Integer subtaskId : oldEpic.getSubTasksId()) {
            if (newEpic.getSubTasksId().contains(subtaskId)) {
                subtasksToDisconnection.remove(subtaskId);
            }
        }
        for (Integer subtaskId : subtasksToDisconnection) {
            subtaskPool.get(subtaskId).setEpicId(null);
        }
        epicStatusControl(newEpic);
        epicPool.put(taskId, newEpic);
    }

    public void updateSubtask(int taskId, Subtask newSubtask, Subtask oldSubtask) {
        subtaskPool.put(taskId, newSubtask);
        Integer epicNewId = newSubtask.getEpicId();
        Integer epicOldId = oldSubtask.getEpicId();
        // проверка подвязывает/отвязывает ли пользователь подзадачу к эпику/-ам
        if (!Objects.equals(epicOldId, epicNewId)) {
            if (epicOldId != null) {
                Epic epicOld = epicPool.get(epicOldId);
                epicOld.getSubTasksId().remove((Integer)taskId);
                epicStatusControl(epicOld);
            }
            //
            if (epicOldId != null) {
                Epic epicNew = epicPool.get(epicNewId);
                epicNew.addSubTasks(taskId);
                epicStatusControl(epicNew);
            }
        } else {
            Epic epic = epicPool.get(epicNewId);
            epicStatusControl(epic);
        }
    }


    public void removeTask(int id) {
        Task task = getTask(id);
        if (task != null) {
            TaskTypes taskType = task.getTaskType();
            switch (taskType) {
                case TASK:
                    taskPool.remove(id);
                    break;
                case EPIC:
                    // Удаляются эпик из пулла, удаляются все связанные подзадачи
                    Epic epic = epicPool.remove(id);
                    for (Integer subtaskId : epic.getSubTasksId()) {
                        subtaskPool.remove(subtaskId);
                    }
                    break;
                case SUBTASK:
                    // Удаляется подзадача из пулла, из эпика, проверяется статус эпика
                    Subtask subtask = subtaskPool.remove(id);
                    Epic connectedEpic = epicPool.get(subtask.getEpicId());
                    connectedEpic.getSubTasksId().remove(id);
                    epicStatusControl(connectedEpic);
                    break;
            }
        }
    }

    public ArrayList<Subtask> getEpicSubtasksList(int id) {
        ArrayList<Subtask> subtasks = new ArrayList<>();
        if (epicPool.containsKey(id)) {
            Epic epic = epicPool.get(id);
            for (Integer subtaskId : epic.getSubTasksId()) {
                Subtask subtask = subtaskPool.get(subtaskId);
                subtasks.add(subtask);
            }
        }
        return subtasks;
    }

    private void epicStatusControl(Epic epic) {
        if (epic != null) {
            ArrayList<Integer> subTasksId = epic.getSubTasksId();
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