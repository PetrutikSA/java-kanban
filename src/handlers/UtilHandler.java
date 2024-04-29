package handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import handlers.adapters.DurationAdapter;
import handlers.adapters.LocalDateTimeAdapter;
import managers.tasks.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public abstract class UtilHandler extends BaseHttpHandler{

    protected UtilHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();

        if (method.equals("GET")) {
            String response = gson.toJson(taskListFromTaskManager(taskManager));
            sendResponse(exchange, response, 200, true);
        } else {
            sendResponse(exchange, methodSyntaxError, 400, false);
        }
    }

    protected abstract List<Task> taskListFromTaskManager(TaskManager taskManager);
}
