import org.junit.jupiter.api.Test;
import tasks.Task;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class BaseTaskHandlerTest <T extends Task> extends BaseHttpHandlerTest {
    protected Class<T> classOfT;
    protected List<T> objectsInDB;
    protected T newObject;
    protected T updatedObject;
    protected T notInDBObjectToUpdate;

    protected final String taskInDBNotEqual = "Возвращаемая из сервера задача не совпадает";
    protected final String poolNotReceived = "Пул не возвращаются";
    protected final String code406 = "Должен возращатся код 406";
    protected final String code404 = "Должен возращатся код 404";
    protected final String code200 = "Должен возращатся код 200";
    protected final String code201 = "Должен возращатся код 201";

    @Test
    void getPool() throws IOException, InterruptedException {
        HttpResponse<String> response = sendGetRequest( "");
        assertEquals(HttpCodes.Complete200.getCode(), response.statusCode(), code200);
        List<Task> taskPool = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        assertNotNull(taskPool, poolNotReceived);
        assertEquals(objectsInDB.size(), taskPool.size(), "Некорректное количество задач");
    }

    @Test
    void getById() throws IOException, InterruptedException {
        HttpResponse<String> response = sendGetRequest("/" + objectsInDB.getFirst().getId());
        assertEquals(HttpCodes.Complete200.getCode(), response.statusCode(), code200);
        Task task = gson.fromJson(response.body(), classOfT);
        assertNotNull(task, poolNotReceived);
        assertEquals(objectsInDB.getFirst().toString(), task.toString(), "Возвращается некорректная задача");
    }

    @Test
    void getTaskThatNotExist() throws IOException, InterruptedException {
        HttpResponse<String> response = sendGetRequest("/20");
        assertEquals(HttpCodes.Error404.getCode(), response.statusCode(), code404);
    }

    @Test
    void create() throws IOException, InterruptedException {
        String newTaskJson = gson.toJson(newObject);
        HttpResponse<String> response = sendPostRequest(newTaskJson, "");
        assertEquals(HttpCodes.Complete201.getCode(), response.statusCode(), code201);
        newObject.setId(10);

        response = sendGetRequest("/10");
        assertEquals(HttpCodes.Complete200.getCode(), response.statusCode(), code200);
        T task = gson.fromJson(response.body(), classOfT);

        assertEquals(newObject.toString(), task.toString(), taskInDBNotEqual);
    }

    @Test
    void updateTask () throws IOException, InterruptedException {
        String updatedTaskJson = gson.toJson(updatedObject);
        HttpResponse<String> response = sendPostRequest(updatedTaskJson,"/" + updatedObject.getId());
        assertEquals(HttpCodes.Complete201.getCode(), response.statusCode(), code201);

        response = sendGetRequest("/" + updatedObject.getId());
        assertEquals(HttpCodes.Complete200.getCode(), response.statusCode(), code200);
        T task = gson.fromJson(response.body(), classOfT);

        assertEquals(updatedObject.toString(), task.toString(), taskInDBNotEqual);
    }

    @Test
    void updateNotExistedTask () throws IOException, InterruptedException {
        notInDBObjectToUpdate.setId(10);

        String updatedTaskJson = gson.toJson(notInDBObjectToUpdate);
        HttpResponse<String> response = sendPostRequest(updatedTaskJson,"/3");
        assertEquals(HttpCodes.Error404.getCode(), response.statusCode(), code404);

        response = sendGetRequest("/10");
        assertEquals(HttpCodes.Error404.getCode(), response.statusCode(), code404);
    }

    @Test
    void deleteTask () throws IOException, InterruptedException {
        HttpResponse<String> response = sendDeleteRequest("/" + objectsInDB.getFirst().getId());
        assertEquals(HttpCodes.Complete200.getCode(), response.statusCode(), code200);
        response = sendGetRequest("/" + objectsInDB.getFirst().getId());
        assertEquals(HttpCodes.Error404.getCode(), response.statusCode(), code404);
    }

    @Test
    void deleteNotExistedTask () throws IOException, InterruptedException {
        HttpResponse<String> response = sendDeleteRequest("/20");
        assertEquals(HttpCodes.Error404.getCode(), response.statusCode(), code404);
    }

    @Test
    void deletePool () throws IOException, InterruptedException {
        HttpResponse<String> response = sendDeleteRequest("");
        assertEquals(HttpCodes.Complete200.getCode(), response.statusCode(), code200);
        response = sendGetRequest("");
        List<T> taskPool = gson.fromJson(response.body(), new TaskListTypeToken().getType());
        assertEquals(0, taskPool.size(), "Пул задач не очищен");
    }
}
