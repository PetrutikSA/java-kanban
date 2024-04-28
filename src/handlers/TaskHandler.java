package handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import managers.exeptions.NotFoundException;
import managers.exeptions.PeriodCrossingException;
import managers.tasks.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TaskHandler extends BaseHttpHandler {

    public TaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String response;
            String method = exchange.getRequestMethod();
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            //Если запрос ко всем задачам, то id будет установлен -1
            int id = (pathParts.length == 2) ? -1 : Integer.parseInt(pathParts[2]);

            switch (method) {
                case "GET":
                    response = handleGetRequest(exchange, id);
                    sendResponse(exchange, response, 200, true);
                    return;
                case "POST":
                    handlePostRequest(exchange, id);
                    sendResponse(exchange, postCompleted, 201, false);
                    return;
                case "DELETE":
                    handleDeleteRequest(exchange, id);
                    sendResponse(exchange, deleteCompleted, 200, false);
                    return;
                default:
                    sendResponse(exchange, methodSyntaxError, 400, false);
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, taskIdSyntaxError, 400, false);
        } catch (NotFoundException e) {
            sendResponse(exchange, taskNotFoundError, 404, false);
        } catch (PeriodCrossingException e) {
            sendResponse(exchange, hasInteractionsError, 406, false);
        }
    }

    private String handleGetRequest(HttpExchange exchange, int id) throws NotFoundException {
        Gson gson = new GsonBuilder().create();
        if (id == -1) { //если id не указан возвращаем все задачи
            return gson.toJson(taskManager.getTasksList());
        } else { //возвращаем задачу
            return gson.toJson(taskManager.getTask(id));
        }
    }

    private void handlePostRequest(HttpExchange exchange, int id)
            throws IOException, NotFoundException, PeriodCrossingException {
        Gson gson = new GsonBuilder().create();
        InputStream inputStream = exchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        Task task = gson.fromJson(body, Task.class);
        if (id == -1) { //если id не указан добавляем задачу
            taskManager.createTask(task);
        } else { //обновляем задачу
            taskManager.updateTask(task);
        }
    }

    private void handleDeleteRequest(HttpExchange exchange, int id) throws NotFoundException {
        if (id == -1) { //если id не указан удаляем весь пул
            taskManager.removeTaskPool();
        } else { //удаляем задачу
            taskManager.removeTask(id);
        }
    }
}
