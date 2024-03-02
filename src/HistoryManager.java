import tasks.Task;

import java.util.ArrayList;

public interface HistoryManager {
    void addTaskToHistory(Task task);

    ArrayList<Task> getHistory();
}
