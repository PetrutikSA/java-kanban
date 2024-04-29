package handlers;

import com.google.gson.Gson;
import managers.tasks.TaskManager;
import tasks.Epic;
import tasks.Task;

import java.util.List;

public class EpicHandler extends TaskHandler {

    public EpicHandler(TaskManager taskManager) {
        super(taskManager);
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
