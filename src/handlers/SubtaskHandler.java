package handlers;

import com.google.gson.Gson;
import managers.tasks.TaskManager;
import tasks.Subtask;
import tasks.Task;

import java.util.List;

public class SubtaskHandler extends TaskHandler {

    public SubtaskHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    protected List<? extends Task> taskList() {
        return taskManager.getSubtasksList();
    }

    @Override
    protected Task taskFromRequest(String body, Gson gson) {
        return gson.fromJson(body, Subtask.class);
    }

    @Override
    protected void removePool() {
        taskManager.removeSubtaskPool();
    }
}
