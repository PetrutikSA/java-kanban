import org.junit.jupiter.api.Test;
import tasks.Task;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HistoryHandlerTest extends BaseHttpHandlerTest{
    protected final String notCorrectHistorySize = "Некорректное количество задач в истории";
    protected final String notCorrectHistoryOrder = "Некорректное порядок задач в истории";
    @Override
    protected void init() {
        basePath = "/history";
    }

    @Test
    void emptyHistoryGet () throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("GET", "");
        assertEquals(200, response.statusCode());
        List<Task> history = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        assertNotNull(history, "История не возвращается");
        assertEquals(0, history.size(), notCorrectHistorySize);
    }

    @Test
    void historyCorrectOrder() throws IOException, InterruptedException {
        taskManager.getTask(2);
        taskManager.getTask(9);
        taskManager.getTask(2);

        HttpResponse<String> response = sendRequest("GET", "");
        List<Task> history = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        assertEquals(2, history.size(), notCorrectHistorySize);
        assertEquals(9, history.get(0).getId(), notCorrectHistoryOrder);
        assertEquals(2, history.get(1).getId(), notCorrectHistoryOrder);
    }
}
