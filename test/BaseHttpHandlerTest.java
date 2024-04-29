import com.google.gson.Gson;
import handlers.type_tokens.TaskListTypeToken;
import managers.Managers;
import managers.TestObjects;
import managers.tasks.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class BaseHttpHandlerTest<T extends Task> {
    protected TaskManager taskManager;
    protected TestObjects testObjects = new TestObjects();
    protected HttpTaskServer taskServer;
    protected final int port = 8080;
    protected String basePath;
    protected Gson gson;
    protected Class<T> classOfT;
    protected List<T> objectsInDB;
    protected T newObject;
    protected T updatedObject;
    protected T notInDBObjectToUpdate;

    protected final String taskInDBNotEqual = "Возвращаемая из сервера задача не совпадает";

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

    protected HttpResponse<String> sendRequest(String method, String requestString, String additionalPath) throws IOException, InterruptedException {
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
        HttpRequest request = requestBuilder.build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected HttpResponse<String> sendRequest(String method, String additionalPath) throws IOException, InterruptedException {
        if (method.equals("POST")) {
            throw new IOException();
        } else {
            return sendRequest(method, "", additionalPath);
        }
    }

    @Test
    void getPool() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("GET", "");
        assertEquals(200, response.statusCode());
        List<Task> taskPool = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        assertNotNull(taskPool, "Задачи не возвращаются");
        assertEquals(objectsInDB.size(), taskPool.size(), "Некорректное количество задач");
    }

    @Test
    void getById() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("GET", "/" + objectsInDB.get(0).getId());
        assertEquals(200, response.statusCode());
        Task task = gson.fromJson(response.body(), classOfT);
        assertNotNull(task, "Задача не возвращаются");
        assertEquals(objectsInDB.get(0).toString(), task.toString(), "Возвращается некорректная задача");
    }

    @Test
    void getTaskThatNotExist() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("GET", "/20");
        assertEquals(404, response.statusCode(), "Должна возращатся ошибка 404");
    }

    @Test
    void create() throws IOException, InterruptedException {
        String newTaskJson = gson.toJson(newObject);
        HttpResponse<String> response = sendRequest("POST", newTaskJson, "");
        assertEquals(201, response.statusCode(), "Должен возращатся код 201");
        newObject.setId(10);

        response = sendRequest("GET", "/10");
        assertEquals(200, response.statusCode());
        T task = gson.fromJson(response.body(), classOfT);

        assertEquals(newObject.toString(), task.toString(), taskInDBNotEqual);
    }

    @Test
    void updateTask () throws IOException, InterruptedException {
        String updatedTaskJson = gson.toJson(updatedObject);
        HttpResponse<String> response = sendRequest("POST", updatedTaskJson,"/" + updatedObject.getId());
        assertEquals(201, response.statusCode(), "Должен возращатся код 201");

        response = sendRequest("GET", "/" + updatedObject.getId());
        assertEquals(200, response.statusCode());
        T task = gson.fromJson(response.body(), classOfT);

        assertEquals(updatedObject.toString(), task.toString(), taskInDBNotEqual);
    }

    @Test
    void updateNotExistedTask () throws IOException, InterruptedException {
        notInDBObjectToUpdate.setId(10);

        String updatedTaskJson = gson.toJson(notInDBObjectToUpdate);
        HttpResponse<String> response = sendRequest("POST", updatedTaskJson,"/3");
        assertEquals(404, response.statusCode(), "Должен возращатся код 404");

        response = sendRequest("GET", "/10");
        assertEquals(404, response.statusCode(), "Должен возращатся код 404");
    }

    @Test
    void deleteTask () throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("DELETE","/" + objectsInDB.get(0).getId());
        assertEquals(200, response.statusCode(), "Должен возращатся код 200");
        response = sendRequest("GET", "/" + objectsInDB.get(0).getId());
        assertEquals(404, response.statusCode(), "Должен возращатся код 404");
    }

    @Test
    void deleteNotExistedTask () throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("DELETE","/20");
        assertEquals(404, response.statusCode(), "Должен возращатся код 404");
    }

    @Test
    void deletePool () throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("DELETE","");
        assertEquals(200, response.statusCode(), "Должен возращатся код 200");
        response = sendRequest("GET", "");
        List<T> taskPool = gson.fromJson(response.body(), new TaskListTypeToken().getType());
        assertEquals(0, taskPool.size(), "Пул задач не очищен");
    }
}
