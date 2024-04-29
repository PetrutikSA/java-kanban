package handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.tasks.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager taskManager;
    protected Gson gson;
    protected final String taskIdSyntaxError = "Номер задачи долже быть указан в виде числа";
    protected final String methodSyntaxError = "Некорректный метод!";
    protected final String hasInteractionsError = "Задача пересекается с существующими";
    protected final String postCompleted = "База данных успешно обновлена";
    protected final String deleteCompleted = "Данные успешно удалены";
    protected final String taskNotFoundError = "Задача не найдена";
    protected final String commandNotFoundError = "Команда не найдена";

    protected BaseHttpHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
    }

    @Override
    public abstract void handle(HttpExchange exchange) throws IOException;

    protected void sendResponse(HttpExchange h, String text, int responseCode, boolean isTextJson) throws IOException {
        String contentType = (isTextJson) ? "application/json;charset=utf-8" : "text/html;charset=utf-8";
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", contentType);
        h.sendResponseHeaders(responseCode, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }
}
