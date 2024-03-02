import tasks.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private final int historySizeLimit = 10;
    private final ArrayList<Task> tasksHistory = new ArrayList<>(historySizeLimit);

    @Override
    public void addTaskToHistory(Task task) {
        if (tasksHistory.size() == historySizeLimit) {
            tasksHistory.remove(0);
        }
        tasksHistory.add(task);
    }

    @Override
    public ArrayList<Task> getHistory() {
        return tasksHistory;
    }
}
