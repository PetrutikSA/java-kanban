package managers.tasks;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.List;

public interface TaskManager {
    List<Task> getTasksList();

    List<Epic> getEpicsList();

    List<Subtask> getSubtasksList();

    void removeTaskPool();

    void removeEpicPool();

    void removeSubtaskPool();

    Task getTask(int id);

    boolean createTask(Task task);

    boolean updateTask(Task task);

    void removeTask(int id);

    List<Subtask> getEpicSubtasksList(int id);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
