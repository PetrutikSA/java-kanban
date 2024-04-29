import handlers.type_tokens.TaskListTypeToken;
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
        HttpResponse<String> response = sendRequest("GET", "");
        assertEquals(200, response.statusCode(), code200);
        List<Task> taskPool = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        assertNotNull(taskPool, poolNotReceived);
        assertEquals(objectsInDB.size(), taskPool.size(), "Некорректное количество задач");
    }

    @Test
    void getById() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("GET", "/" + objectsInDB.get(0).getId());
        assertEquals(200, response.statusCode(), code200);
        Task task = gson.fromJson(response.body(), classOfT);
        assertNotNull(task, poolNotReceived);
        assertEquals(objectsInDB.get(0).toString(), task.toString(), "Возвращается некорректная задача");
    }

    @Test
    void getTaskThatNotExist() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("GET", "/20");
        assertEquals(404, response.statusCode(), code404);
    }

    @Test
    void create() throws IOException, InterruptedException {
        String newTaskJson = gson.toJson(newObject);
        HttpResponse<String> response = sendRequest("POST", newTaskJson, "");
        assertEquals(201, response.statusCode(), code201);
        newObject.setId(10);

        response = sendRequest("GET", "/10");
        assertEquals(200, response.statusCode(), code200);
        T task = gson.fromJson(response.body(), classOfT);

        assertEquals(newObject.toString(), task.toString(), taskInDBNotEqual);
    }

    @Test
    void updateTask () throws IOException, InterruptedException {
        String updatedTaskJson = gson.toJson(updatedObject);
        HttpResponse<String> response = sendRequest("POST", updatedTaskJson,"/" + updatedObject.getId());
        assertEquals(201, response.statusCode(), code201);

        response = sendRequest("GET", "/" + updatedObject.getId());
        assertEquals(200, response.statusCode(), code200);
        T task = gson.fromJson(response.body(), classOfT);

        assertEquals(updatedObject.toString(), task.toString(), taskInDBNotEqual);
    }

    @Test
    void updateNotExistedTask () throws IOException, InterruptedException {
        notInDBObjectToUpdate.setId(10);

        String updatedTaskJson = gson.toJson(notInDBObjectToUpdate);
        HttpResponse<String> response = sendRequest("POST", updatedTaskJson,"/3");
        assertEquals(404, response.statusCode(), code404);

        response = sendRequest("GET", "/10");
        assertEquals(404, response.statusCode(), code404);
    }

    @Test
    void deleteTask () throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("DELETE","/" + objectsInDB.get(0).getId());
        assertEquals(200, response.statusCode(), code200);
        response = sendRequest("GET", "/" + objectsInDB.get(0).getId());
        assertEquals(404, response.statusCode(), code404);
    }

    @Test
    void deleteNotExistedTask () throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("DELETE","/20");
        assertEquals(404, response.statusCode(), code404);
    }

    @Test
    void deletePool () throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("DELETE","");
        assertEquals(200, response.statusCode(), code200);
        response = sendRequest("GET", "");
        List<T> taskPool = gson.fromJson(response.body(), new TaskListTypeToken().getType());
        assertEquals(0, taskPool.size(), "Пул задач не очищен");
    }
}
