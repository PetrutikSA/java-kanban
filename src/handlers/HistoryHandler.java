package handlers;

import com.google.gson.Gson;
import managers.tasks.TaskManager;
import tasks.Task;

import java.util.List;

public class HistoryHandler extends UtilHandler {

    public HistoryHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    protected List<Task> taskListFromTaskManager(TaskManager taskManager) {
        return taskManager.getHistory();
    }
}
