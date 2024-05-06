import org.junit.jupiter.api.Test;
import tasks.Task;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrioritizedHandlerTest extends BaseHttpHandlerTest {
    protected final String notCorrectPrioritizedOrder = "Некорректное порядок задач в списке приоритетных";
    @Override
    protected void init() {
        basePath = "/prioritized";
    }

    @Test
    void prioritizedCorrectOrder() throws IOException, InterruptedException {
        HttpResponse<String> response = sendGetRequest("");
        List<Task> prioritized = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        assertEquals(7, prioritized.size(), "Некорректный размер списка приоритетных задач");
        assertEquals(testObjects.subtask1.getId(), prioritized.getFirst().getId(), notCorrectPrioritizedOrder);
        assertEquals(testObjects.task2.getId(), prioritized.get(2).getId(), notCorrectPrioritizedOrder);
        assertEquals(testObjects.subtask4.getId(), prioritized.get(6).getId(), notCorrectPrioritizedOrder);
    }
}
