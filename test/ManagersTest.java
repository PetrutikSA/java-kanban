import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagersTest {

    @Test
    void getInMemoryTaskManagerNoNull() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "TaskManager не проинициализирован");
    }

    @Test
    void getInMemoryHistoryManagerNoNull() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "HistoryManager не проинициализирован");
    }
}