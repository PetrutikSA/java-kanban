import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.ArrayList;

public interface TaskManager {
    ArrayList<Task> getTasksList();

    ArrayList<Epic> getEpicsList();

    ArrayList<Subtask> getSubtasksList();

    void removeTaskPool();

    void removeEpicPool();

    void removeSubtaskPool();

    Task getTask(int id);

    void createTask(Task task);

    void updateTask(Task task);

    void removeTask(int id);

    ArrayList<Subtask> getEpicSubtasksList(int id);

    ArrayList<Task> getHistory();
}
