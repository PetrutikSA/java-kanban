import tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int HISTORY_SIZE_LIMIT = 10;
    private final List<Task> tasksHistory = new ArrayList<>(HISTORY_SIZE_LIMIT);

    @Override
    public void addTaskToHistory(Task task) {
        if (task != null) {
            if (tasksHistory.size() == HISTORY_SIZE_LIMIT) {
                tasksHistory.remove(0);
            }
            tasksHistory.add(task);
        }
    }

    @Override
    public List<Task> getHistory() {
        return tasksHistory;
    }
}
