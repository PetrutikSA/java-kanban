package handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import managers.exeptions.CommandNotFoundException;
import managers.exeptions.NotFoundException;
import managers.tasks.TaskManager;
import tasks.Epic;
import tasks.Task;

import java.util.List;

public class EpicHandler extends TaskHandler {

    public EpicHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    protected String handleGetRequest(HttpExchange exchange, int id, Gson gson) throws NotFoundException, CommandNotFoundException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        if (id == -1) { //если id не указан возвращаем все задачи
            return gson.toJson(taskList());
        } else {
            Epic epic = (Epic) taskManager.getTask(id);
            if (pathParts.length == 3){ //возвращаем задачу
                return gson.toJson(epic);
            } else if (pathParts[3].equals("subtasks")) { //если запрос спика подзадач
                return gson.toJson(epic.getSubTasksIds());
            } else {
                throw new CommandNotFoundException();
            }
        }
    }

    @Override
    protected List<? extends Task> taskList() {
        return taskManager.getEpicsList();
    }

    @Override
    protected Task taskFromRequest(String body, Gson gson) {
        return gson.fromJson(body, Epic.class);
    }

    @Override
    protected void removePool() {
        taskManager.removeEpicPool();
    }
}
