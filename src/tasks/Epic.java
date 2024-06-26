package tasks;

import tasks.enums.Status;
import tasks.enums.TaskTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static handlers.adapters.LocalDateTimeAdapter.DATE_TIME_FORMATTER;

public class Epic extends Task {
    private List<Integer> subTasksIds;
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        subTasksIds = new ArrayList<>();
        taskType = TaskTypes.EPIC;
    }

    public Epic(Epic epic) {
        super(epic.getName(), epic.getDescription(), epic.getStatus());
        this.id = epic.getId();
        this.subTasksIds = epic.getSubTasksIds();
        this.taskType = TaskTypes.EPIC;
        this.duration = epic.duration;
        this.startTime = epic.startTime;
        this.endTime = epic.endTime;
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

    public void setSubTasksIds(List<Integer> subTasksIds) {
        this.subTasksIds = subTasksIds;
    }

    public String saveToString() {
        String subtasksToString = " "; // для метода load из managers.tasks.FileBackedTaskManager
        if (!subTasksIds.isEmpty()) {
            String[] subtasksArray = new String[subTasksIds.size()];
            for (int i = 0; i < subtasksArray.length; i++) {
                subtasksArray[i] = String.valueOf(subTasksIds.get(i));
            }
            subtasksToString = String.join("_", subtasksArray);
        }
        String endTimeToString = (endTime == null) ? " " : endTime.format(DATE_TIME_FORMATTER);
        return String.format("%s,%s,%s", super.saveToString(), subtasksToString, endTimeToString);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
