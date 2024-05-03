import com.google.gson.Gson;
import managers.Managers;
import managers.TestObjects;
import managers.tasks.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class BaseHttpHandlerTest {
    protected TaskManager taskManager;
    protected TestObjects testObjects = new TestObjects();
    protected HttpTaskServer taskServer;
    protected final int port = 8080;
    protected String basePath;
    protected Gson gson;

    @BeforeEach
    void beforeEach() throws IOException {
        init();
        taskManager = Managers.getDefault();
        testObjects.fillManagerWithTestObjects(taskManager);
        taskServer = new HttpTaskServer(taskManager);
        gson = taskServer.getGson();
        taskServer.start(port);
    }

    protected abstract void init();

    @AfterEach
    void afterEach() {
        taskServer.stop();
    }

    protected HttpResponse<String> sendGetRequest(String additionalPath) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:%d%s%s", port, basePath, additionalPath));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected HttpResponse<String> sendPostRequest(String requestString, String additionalPath) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:%d%s%s", port, basePath, additionalPath));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(requestString))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected HttpResponse<String> sendDeleteRequest(String additionalPath) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:%d%s%s", port, basePath, additionalPath));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
