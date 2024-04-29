package handlers;

import managers.tasks.TaskManager;
import tasks.Task;

import java.util.List;

public class HistoryHandler extends UtilHandler {

    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected List<Task> taskListFromTaskManager(TaskManager taskManager) {
        return taskManager.getHistory();
    }
}
