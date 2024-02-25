package tasks;

import java.util.ArrayList;

public class Epic extends Task{
     private ArrayList<Integer> subTasksId;
     public Epic(String name, String description) {
        super(name, description, Status.NEW);
        subTasksId = new ArrayList<>();
        taskType = TaskTypes.EPIC;
    }

    public void addSubTasks(int subTaskId) {
        if (!subTasksId.contains(subTaskId)) {
            subTasksId.add(subTaskId);
        }
    }

    public ArrayList<Integer> getSubTasksId() {
        return subTasksId;
    }

    @Override
    public String toString() {
        return "Epic{id='" + id + ", 'name='" + name + "', description='" + description + "', status=" + status
                + ", subtasksNumber='" + subTasksId.size() + "'}";
    }
}
