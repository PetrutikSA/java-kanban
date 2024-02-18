package tasks;

public class Subtask extends Task {
    private Epic epic;
    public Subtask(String name, String description, Status status, Epic epic) {
        super(name, description, status);
        this.epic = epic;
        taskType = TaskTypes.SUBTASK;
        epic.addSubTasks(this);
    }

    public Epic getEpic() {
        return epic;
    }

    public void setEpic(Epic epic) {
        this.epic = epic;
    }

    @Override
    public String toString() {
        return "tasks.Subtask{id='" + id + ", 'name='" + name + "', description='" + description + "', status=" + status
                + ", epicID='" + epic.getId() + "'}";
    }
}
