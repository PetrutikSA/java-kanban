package managers;

import managers.history.HistoryManager;
import managers.tasks.TaskManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagersTest {

    @Test
    void getInMemoryTaskManagerNoNull() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "managers.tasks.TaskManager не проинициализирован");
    }

    @Test
    void getInMemoryHistoryManagerNoNull() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "managers.history.HistoryManager не проинициализирован");
    }
}