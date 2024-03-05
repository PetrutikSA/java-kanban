package tasks;

import tasks.enums.Status;
import tasks.enums.TaskTypes;

public class Subtask extends Task {
    private Integer epicId;
    public Subtask(String name, String description, Status status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
        taskType = TaskTypes.SUBTASK;
    }

    public Integer getEpicId() {
        return epicId;
    }

    public void setEpicId(Integer epicId) {
        if (id != epicId) {
            this.epicId = epicId;
        }
    }

    @Override
    public String toString() {
        return "Subtask{id='" + id + ", 'name='" + name + "', description='" + description + "', status=" + status
                + ", epicID='" + epicId + "'}";
    }
}
