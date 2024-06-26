package tasks;

import tasks.enums.Status;
import tasks.enums.TaskTypes;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private Integer epicId;

    public Subtask(String name, String description, Status status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
        taskType = TaskTypes.SUBTASK;
    }

    public Subtask(String name, String description, Status status, int epicId, LocalDateTime startTime, Duration duration) {
        this(name, description, status, epicId);
        this.startTime = startTime;
        this.duration = duration;
    }

    public Subtask(Subtask subtask) {
        super(subtask.getName(), subtask.description, subtask.getStatus());
        this.id = subtask.getId();
        this.epicId = subtask.getEpicId();
        this.taskType = TaskTypes.SUBTASK;
        this.duration = subtask.duration;
        this.startTime = subtask.startTime;
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

    public String saveToString() {
        return String.format("%s,%s", super.saveToString(), epicId);
    }
}
