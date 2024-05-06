import org.junit.jupiter.api.Test;
import tasks.Task;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class CrossingPeriodHandlerTest<T extends Task> extends BaseTaskHandlerTest<T>{
    protected T crossingPeriodNewObject;
    protected T crossingPeriodObjectToUpdate;
    @Test
    void createTaskWithCrossingPeriod () throws IOException, InterruptedException {
        String newTaskJson = gson.toJson(crossingPeriodNewObject);
        HttpResponse<String> response = sendPostRequest(newTaskJson,"");
        assertEquals(HttpCodes.Error406.getCode(), response.statusCode(), code406);
        crossingPeriodNewObject.setId(10);

        response = sendGetRequest("/10");
        assertEquals(HttpCodes.Error404.getCode(), response.statusCode(),code406);
    }

    @Test
    void updateTaskWithCrossingPeriod () throws IOException, InterruptedException {
        String updatedTaskJson = gson.toJson(crossingPeriodObjectToUpdate);
        HttpResponse<String> response = sendPostRequest(updatedTaskJson,"/" +
                crossingPeriodNewObject.getId());
        assertEquals(HttpCodes.Error406.getCode(), response.statusCode(), code406);
    }
}
