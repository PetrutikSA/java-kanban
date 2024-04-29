import com.google.gson.Gson;
import handlers.type_tokens.TaskListTypeToken;
import managers.Managers;
import managers.TestObjects;
import managers.tasks.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.enums.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

class HttpTaskServerTaskHandleTest {
    private TaskManager taskManager;
    private TestObjects testObjects;
    private HttpTaskServer taskServer;
    private final int port = 8080;
    private final String basePath = "/tasks";
    private Gson gson;

    protected final String taskInDBNotEqual = "Возвращаемая из сервера задача не совпадает";

    @BeforeEach
    void beforeEach() throws IOException {
        taskManager = Managers.getDefault();
        testObjects = new TestObjects();
        testObjects.fillManagerWithTestObjects(taskManager);
        taskServer = new HttpTaskServer(taskManager);
        gson = taskServer.getGson();
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
    void getTaskPool () throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("GET", "");
        assertEquals(200, response.statusCode());
        List<Task> taskPool = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        assertNotNull(taskPool, "Задачи не возвращаются");
        assertEquals(3, taskPool.size(), "Некорректное количество задач");
    }

    @Test
    void getTaskById () throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("GET", "/2");
        assertEquals(200, response.statusCode());
        Task task = gson.fromJson(response.body(), Task.class);

        assertNotNull(task, "Задача не возвращаются");
        Task taskToCheck = new Task(testObjects.task2);
        taskToCheck.setId(2);
        assertEquals(testObjects.task2.toString(), task.toString(), "Возвращается некорректная задача");
    }

    @Test
    void getTaskThatNotExist () throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("GET", "/20");
        assertEquals(404, response.statusCode(), "Должна возращатся ошибка 404");
    }

    @Test
    void createTask () throws IOException, InterruptedException {
        Task newTask = new Task("New", "NewDescription", Status.NEW,
                LocalDateTime.of(2024,7,1,18,0), Duration.ofDays(1));
        String newTaskJson = gson.toJson(newTask);
        HttpResponse<String> response = sendRequest("POST", newTaskJson,"");
        assertEquals(201, response.statusCode(), "Должен возращатся код 201");
        newTask.setId(10);

        response = sendRequest("GET", "/10");
        assertEquals(200, response.statusCode());
        Task task = gson.fromJson(response.body(), Task.class);

        assertEquals(newTask.toString(), task.toString(), taskInDBNotEqual);
    }

    @Test
    void createTaskWithCrossingPeriod () throws IOException, InterruptedException {
        Task newTask = new Task("New", "NewDescription", Status.NEW,
                testObjects.task1.getStartTime(), Duration.ofDays(1));
        String newTaskJson = gson.toJson(newTask);
        HttpResponse<String> response = sendRequest("POST", newTaskJson,"");
        assertEquals(406, response.statusCode(), "Должен возращатся код 406");
        newTask.setId(10);

        response = sendRequest("GET", "/10");
        assertEquals(404, response.statusCode(),"Должен возращатся код 406");
    }

    @Test
    void updateTask () throws IOException, InterruptedException {
        Task updatedTask = new Task(testObjects.task3);
        updatedTask.setId(3);
        updatedTask.setStatus(Status.DONE);
        updatedTask.setDuration(Duration.ofHours(2));

        String updatedTaskJson = gson.toJson(updatedTask);
        HttpResponse<String> response = sendRequest("POST", updatedTaskJson,"/3");
        assertEquals(201, response.statusCode(), "Должен возращатся код 201");

        response = sendRequest("GET", "/3");
        assertEquals(200, response.statusCode());
        Task task = gson.fromJson(response.body(), Task.class);

        assertEquals(updatedTask.toString(), task.toString(), taskInDBNotEqual);
    }

    @Test
    void updateNotExistedTask () throws IOException, InterruptedException {
        Task updatedTask = new Task("New", "NewDescription", Status.NEW,
                LocalDateTime.of(2024,7,1,18,0), Duration.ofDays(1));
        updatedTask.setId(10);

        String updatedTaskJson = gson.toJson(updatedTask);
        HttpResponse<String> response = sendRequest("POST", updatedTaskJson,"/3");
        assertEquals(404, response.statusCode(), "Должен возращатся код 404");

        response = sendRequest("GET", "/10");
        assertEquals(404, response.statusCode(), "Должен возращатся код 404");
    }

    @Test
    void updateTaskWithCrossingPeriod () throws IOException, InterruptedException {
        Task updatedTask = new Task(testObjects.task2);
        updatedTask.setId(2);
        updatedTask.setStartTime(testObjects.task3.getStartTime());

        String updatedTaskJson = gson.toJson(updatedTask);
        HttpResponse<String> response = sendRequest("POST", updatedTaskJson,"/2");
        assertEquals(406, response.statusCode(), "Должен возращатся код 406");
    }


    @Test
    void deleteTask () throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("DELETE","/2");
        assertEquals(200, response.statusCode(), "Должен возращатся код 200");
        response = sendRequest("GET", "/2");
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
        List<Task> taskPool = gson.fromJson(response.body(), new TaskListTypeToken().getType());
        assertEquals(0, taskPool.size(), "Пул задач не очищен");
    }

    @AfterEach
    void afterEach() {
        taskServer.stop();
    }
}