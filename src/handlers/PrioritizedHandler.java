package handlers;

import managers.tasks.TaskManager;
import tasks.Task;

import java.util.List;

public class PrioritizedHandler extends UtilHandler {

    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected List<Task> taskListFromTaskManager(TaskManager taskManager) {
        return taskManager.getPrioritizedTasks();
    }
}
