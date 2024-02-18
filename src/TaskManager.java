import tasks.*;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private final HashMap<Integer, Task> taskPool = new HashMap<>();
    private final HashMap<Integer, Subtask> subtaskPool = new HashMap<>();
    private final HashMap<Integer, Epic> epicPool = new HashMap<>();
    private int lastTaskId = 0;

    public HashMap<Integer, ? extends Task> getTaskPool(TaskTypes taskTypes) {
        switch (taskTypes) {
            case TASK:
                return taskPool;
            case SUBTASK:
                return subtaskPool;
            case EPIC:
                return epicPool;
            default:
                return null;
        }
    }

    public void removeTaskPool(TaskTypes taskTypes) {
        switch (taskTypes) {
            case TASK:
                taskPool.clear();
            case EPIC:
                epicPool.clear();
                subtaskPool.clear(); //Подзадачи не могут существовать без эпиков
            case SUBTASK:
                subtaskPool.clear();
                //Пересмотр статуса эпиков согласно логике программы
                for (Integer id : epicPool.keySet()) {
                    Epic epic = epicPool.get(id);
                    epic.getSubTasks().clear();
                    epicStatusControl(epic);
                }
        }
    }

    public Task getTask(int id) {
        if (taskPool.containsKey(id)) {
            return taskPool.get(id);
        } else if (epicPool.containsKey(id)) {
            return epicPool.get(id);
        } else if (subtaskPool.containsKey(id)) {
            return subtaskPool.get(id);
        } else {
            return null;
        }
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
                    epicStatusControl(epic);
                    break;
                case SUBTASK:
                    Subtask subtask = (Subtask) task;
                    Epic connectedEpic = subtask.getEpic();
                    // Если Эпика к которому относится подзадача нет в пуле, он добавляется в него
                    if (!epicPool.containsKey(connectedEpic.getId())) {
                        createTask(connectedEpic);
                    }
                    subtaskPool.put(lastTaskId, subtask);
                    connectedEpic.addSubTasks(subtask);
                    epicStatusControl(connectedEpic);
                    break;
            }
        }
    }

    public void updateTask(Task task) {
        if (task != null) {
            int taskId = task.getId();
            TaskTypes taskType = task.getTaskType();
            // Проверка что есть что обновлять и не позволяет изменить тип задачи, напр. обновить эпик на задачу
            if (getTask(taskId) != null && taskType.equals(getTask(taskId).getTaskType())) {
                switch (taskType) {
                    case TASK:
                        taskPool.put(taskId, task);
                        break;
                    case EPIC:
                        Epic newEpic = (Epic) task;
                        //Проверка и удаление подзадач, которые отсутствуют в новой вариации эпика;
                        Epic oldEpic = epicPool.get(taskId);
                        ArrayList<Subtask> subtasksToException = new ArrayList<>(oldEpic.getSubTasks());
                        for (Subtask subtask : oldEpic.getSubTasks()) {
                            if (newEpic.getSubTasks().contains(subtask)) {
                                subtask.setEpic(newEpic);
                                subtasksToException.remove(subtask);
                            }
                        }
                        for (Subtask subtask : subtasksToException) {
                            subtaskPool.remove(subtask.getId());
                        }
                        epicStatusControl(newEpic);
                        epicPool.put(taskId, newEpic);
                        break;
                    case SUBTASK:
                        Subtask newSubtask = (Subtask) task;
                        // Проверка эпика для подзадачи и обновление данных в эпиках
                        Epic epicConnectedNewSubtask = newSubtask.getEpic();
                        Subtask oldSubtask = subtaskPool.get(taskId);
                        Epic epicConnectedOldSubtask = oldSubtask.getEpic();
                        epicConnectedOldSubtask.getSubTasks().remove(oldSubtask);
                        epicConnectedNewSubtask.addSubTasks(newSubtask);
                        epicStatusControl(epicConnectedOldSubtask);
                        epicStatusControl(epicConnectedNewSubtask);
                        subtaskPool.put(taskId, (Subtask) task);
                        break;
                }
            }
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
                    // Вначале удаляются все связанные подзадачи, потом сам эпик
                    Epic epic = (Epic) task;
                    for (Subtask subtask : epic.getSubTasks()) {
                        subtaskPool.remove(subtask.getId());
                    }
                    epicPool.remove(id);
                    break;
                case SUBTASK:
                    // Вначале удаляется подзадача из эпика, проверяется его статус, потом  подзадача удаляется из пулла
                    Subtask subtask = (Subtask) task;
                    Epic connectedEpic = subtask.getEpic();
                    connectedEpic.getSubTasks().remove(subtask);
                    epicStatusControl(connectedEpic);
                    subtaskPool.remove(id);
                    break;
            }
        }
    }

    public ArrayList<Subtask> getEpicSubtasksList(int id) {
        if (epicPool.containsKey(id)) {
            return epicPool.get(id).getSubTasks();
        } else {
            return null;
        }
    }

    public void epicStatusControl(Epic epic) {
        if (epic != null) {
            ArrayList<Subtask> subtasks = epic.getSubTasks();
            boolean isSubtasksCompleate = true;
            boolean isAllSubtasksNew = true;
            for (Subtask subtask : subtasks) {
                if (subtask.getStatus() != Status.DONE) {
                    isSubtasksCompleate = false;
                }
                if ((subtask.getStatus() != Status.NEW)) {
                    isAllSubtasksNew = false;
                }
            }
            if (subtasks.isEmpty() || isAllSubtasksNew) {
                epic.setStatus(Status.NEW);
            } else if (isSubtasksCompleate) {
                epic.setStatus(Status.DONE);
            } else {
                epic.setStatus(Status.IN_PROGRESS);
            }
        }
    }
}