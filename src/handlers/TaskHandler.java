package handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import managers.exeptions.CommandNotFoundException;
import managers.exeptions.NotFoundException;
import managers.exeptions.PeriodCrossingException;
import managers.tasks.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TaskHandler extends BaseHttpHandler {

    public TaskHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            //Если запрос ко всем задачам, то id будет установлен -1
            int id = (pathParts.length == 2) ? -1 : Integer.parseInt(pathParts[2]);

            switch (method) {
                case "GET":
                    String response = handleGetRequest(exchange, id, gson);
                    sendResponse(exchange, response, 200, true);
                    return;
                case "POST":
                    handlePostRequest(exchange, id, gson);
                    sendResponse(exchange, postCompleted, 201, false);
                    return;
                case "DELETE":
                    handleDeleteRequest(id);
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
        } catch (CommandNotFoundException e) {
            sendResponse(exchange, commandNotFoundError, 400, false);
        }
    }

    protected String handleGetRequest(HttpExchange exchange, int id, Gson gson) throws NotFoundException {
        if (id == -1) { //если id не указан возвращаем все задачи
            return gson.toJson(taskList());
        } else { //возвращаем задачу
            return gson.toJson(taskManager.getTask(id));
        }
    }

    protected List<? extends Task> taskList () { //выделено для переопределения в наследуемых классах
        return taskManager.getTasksList();
    }

    private void handlePostRequest(HttpExchange exchange, int id, Gson gson)
            throws IOException, NotFoundException, PeriodCrossingException {

        InputStream inputStream = exchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        Task task = taskFromRequest(body, gson);
        if (id == -1) { //если id не указан добавляем задачу
            taskManager.createTask(task);
        } else { //обновляем задачу
            taskManager.updateTask(task);
        }
    }

    protected Task taskFromRequest (String body, Gson gson) { //выделено для переопределения в наследуемых классах
        return gson.fromJson(body, Task.class);
    }

    private void handleDeleteRequest(int id) throws NotFoundException {
        if (id == -1) { //если id не указан удаляем весь пул
            removePool();
        } else { //удаляем задачу
            taskManager.removeTask(id);
        }
    }

    protected void removePool() {
        taskManager.removeTaskPool();
    }
}
