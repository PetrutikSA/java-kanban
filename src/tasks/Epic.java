package tasks;

import tasks.enums.Status;
import tasks.enums.TaskTypes;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task{
     private List<Integer> subTasksIds;
     public Epic(String name, String description) {
        super(name, description, Status.NEW);
        subTasksIds = new ArrayList<>();
        taskType = TaskTypes.EPIC;
    }

    public void addSubTasks(int subTaskId) {
        if (!subTasksIds.contains(subTaskId) && (subTaskId != id)) {
            subTasksIds.add(subTaskId);
        }
    }

    public List<Integer> getSubTasksIds() {
        return subTasksIds;
    }

    @Override
    public String toString() {
        return "Epic{id='" + id + ", 'name='" + name + "', description='" + description + "', status=" + status
                + ", subtasksNumber='" + subTasksIds.size() + "'}";
    }
}
