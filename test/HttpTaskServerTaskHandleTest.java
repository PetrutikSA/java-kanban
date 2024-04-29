import managers.Managers;
import managers.TestObjects;
import managers.tasks.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class HttpTaskServerTaskHandleTest {
    private TaskManager taskManager;
    private TestObjects testObjects;
    private HttpTaskServer taskServer;
    private final int port = 8080;
    private final String basePath = "/tasks";

    @BeforeEach
    void beforeEach() throws IOException {
        taskManager = Managers.getDefault();
        testObjects = new TestObjects();
        testObjects.fillManagerWithTestObjects(taskManager);
        taskServer = new HttpTaskServer(taskManager);
        taskServer.start(port);
    }

    private HttpResponse<String> sendRequest (String method, String requestString, String additionalPath) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:%d%s%s", port, basePath, additionalPath));
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(url);
        switch (method) {
            case "GET":
                requestBuilder.GET();
                break;
            case "POST":
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(requestString));
                break;
            case "DELETE":
                requestBuilder.DELETE();
                break;
            default:
                throw new IOException();
        }
        HttpRequest request =  requestBuilder.build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendRequest (String method, String additionalPath) throws IOException, InterruptedException {
        if (method.equals("POST")) {
            throw new IOException();
        } else {
            return sendRequest(method,"", additionalPath);
        }
    }

    @Test
    void getTaskPool () {

    }

    @AfterEach
    void afterEach() {
        taskServer.stop();
    }
}