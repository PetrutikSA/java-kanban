package managers;

import managers.history.HistoryManager;
import managers.history.InMemoryHistoryManager;
import managers.tasks.InMemoryTaskManager;
import managers.tasks.TaskManager;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
