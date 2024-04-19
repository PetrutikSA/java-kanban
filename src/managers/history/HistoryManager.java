package managers.history;

import tasks.Task;

import java.util.List;

public interface HistoryManager {
    void addTaskToHistory(Task task);

    List<Task> getHistory();

    void remove(int id);
}
