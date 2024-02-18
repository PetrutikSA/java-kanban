package tasks;

import java.util.ArrayList;

public class Epic extends Task{
     private ArrayList<Subtask> subTasks;
     public Epic(String name, String description) {
        super(name, description, Status.NEW);
        subTasks = new ArrayList<>();
        taskType = TaskTypes.EPIC;
    }

    public void addSubTasks(Subtask subTask) {
        if (!subTasks.contains(subTask)) {
            subTasks.add(subTask);
        }
    }

    public ArrayList<Subtask> getSubTasks() {
        return subTasks;
    }

    @Override
    public String toString() {
        return "tasks.Epic{id='" + id + ", 'name='" + name + "', description='" + description + "', status=" + status
                + ", subtasksNumber='" + subTasks.size() + "'}";
    }
}
