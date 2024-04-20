package tasks;

import tasks.enums.Status;
import tasks.enums.TaskTypes;

import java.time.Duration;
import java.time.LocalDateTime;

import static managers.tasks.FileBackedTaskManager.FORMATTER;

public class Task {
    protected int id;
    protected String name;
    protected String description;
    protected Status status;
    protected TaskTypes taskType;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String name, String description, Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
        taskType = TaskTypes.TASK;
    }

    public Task (String name, String description, Status status, LocalDateTime startTime, Duration duration) {
        this(name, description, status);
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(Task task) {
        this.id = task.getId();
        this.name = task.getName();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.taskType = TaskTypes.TASK;
        this.duration = task.duration;
        this.startTime = task.startTime;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return this.id == task.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public TaskTypes getTaskType() {
        return taskType;
    }

    @Override
    public String toString() {
        return "Task{id='" + id + ", 'name='" + name + "', description='" + description + "', status=" + status + "'}";
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String saveToString() {
        return String.format("%s,%d,%s,%s,%s,%s,%d", taskType, id, name, description, status
                , startTime.format(FORMATTER), duration.toMinutes());
    }

    public LocalDateTime getEndTime() {
        return startTime.plus(duration);
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
}
