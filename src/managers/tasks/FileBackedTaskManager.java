package managers.tasks;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import tasks.enums.Status;
import tasks.enums.TaskTypes;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final String fileName;
    private final String currentDir;
    private final Path file;
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public FileBackedTaskManager() { // значения по умолчанию
        fileName = "db.csv";
        currentDir = System.getProperty("user.dir");
        file = Paths.get(currentDir, fileName);
    }

    public FileBackedTaskManager(String fileName, String currentDir) {
        this.fileName = fileName;
        this.currentDir = currentDir;
        file = Paths.get(currentDir, fileName);
    }

    private void save() {
        StringBuilder sb = new StringBuilder();
        sb.append("DB/History,TaskType,Id,Name,Description,Status,StartTime,Duration,Epic/Subtasks,EpicEndTime");
        if (!taskPool.isEmpty()) {
            taskPool.values().stream()
                    .map(task -> String.format("\nDB,%s", task.saveToString()))
                    .forEach(sb::append);
        }
        if (!epicPool.isEmpty()) {
            epicPool.values().stream()
                    .map(epic -> String.format("\nDB,%s", epic.saveToString()))
                    .forEach(sb::append);
        }
        if (!subtaskPool.isEmpty()) {
            subtaskPool.values().stream()
                    .map(subtask -> String.format("\nDB,%s", subtask.saveToString()))
                    .forEach(sb::append);
        }
        List<Task> history = getHistory();
        if (!history.isEmpty()) {
            history.stream()
                    .map(task -> String.format("\nHistory,%s", task.saveToString()))
                    .forEach(sb::append);
        }

        try (Writer fileWriter = new FileWriter(file.toFile())) {
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
            fileWriter.write(sb.toString());
        } catch (IOException exception) {
            throw new ManagerSaveException(exception.getMessage(), exception.getCause());
        }
    }

    public static FileBackedTaskManager load(File file) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file.getName(), file.getParent());
        try {
            String content = Files.readString(file.toPath());
            String[] contentArray = content.split("\n");
            if (contentArray.length > 1) { //в противном случае пустой
                for (int i = 1; i < contentArray.length; i++) {
                    String[] line = contentArray[i].split(",");
                    boolean isDataBaseValue = line[0].equals("DB");
                    TaskTypes taskType = TaskTypes.valueOf(line[1]);
                    int id = Integer.parseInt(line[2]);
                    String name = line[3];
                    String description = line[4];
                    Status status = Status.valueOf(line[5]);
                    LocalDateTime startTime = (line[6].isBlank()) ? null : LocalDateTime.parse(line[6], FORMATTER);
                    Duration duration = (line[7].isBlank()) ? null : Duration.ofMinutes(Long.parseLong(line[7]));
                    if (fileBackedTaskManager.lastTaskId < id) fileBackedTaskManager.lastTaskId = id;

                    switch (taskType) {
                        case TASK:
                            Task task = new Task(name, description, status, startTime, duration);
                            task.setId(id);
                            if (isDataBaseValue) {
                                fileBackedTaskManager.taskPool.put(id, task);
                                fileBackedTaskManager.checkAndPutInPrioritizedTasks(task);
                            } else {
                                fileBackedTaskManager.historyManager.addTaskToHistory(task);
                            }
                            break;
                        case EPIC:
                            Epic epic = new Epic(name, description);
                            epic.setId(id);
                            epic.setStatus(status);
                            if (startTime != null) epic.setStartTime(startTime);
                            if (duration != null) epic.setDuration(duration);
                            if (!line[8].isBlank()) {
                                String[] subtasksId = line[8].split("_");
                                Arrays.stream(subtasksId)
                                        .mapToInt(Integer::parseInt)
                                        .forEach(epic::addSubTasks);
                            }
                            LocalDateTime endTime = (line[9].isBlank()) ? null : LocalDateTime.parse(line[9], FORMATTER);
                            epic.setEndTime(endTime);
                            if (isDataBaseValue) {
                                fileBackedTaskManager.epicPool.put(id, epic);
                            } else {
                                fileBackedTaskManager.historyManager.addTaskToHistory(epic);
                            }
                            break;
                        case SUBTASK:
                            int epicId = Integer.parseInt(line[8]);
                            Subtask subtask = new Subtask(name, description, status, epicId, startTime, duration);
                            subtask.setId(id);
                            if (isDataBaseValue) {
                                fileBackedTaskManager.subtaskPool.put(id, subtask);
                                fileBackedTaskManager.checkAndPutInPrioritizedTasks(subtask);
                            } else {
                                fileBackedTaskManager.historyManager.addTaskToHistory(subtask);
                            }
                            break;
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage(), e.getCause());
        }
        return fileBackedTaskManager;
    }

    @Override
    public void removeTaskPool() {
        super.removeTaskPool();
        save();
    }

    @Override
    public void removeEpicPool() {
        super.removeEpicPool();
        save();
    }

    @Override
    public void removeSubtaskPool() {
        super.removeSubtaskPool();
        save();
    }

    @Override
    public Task getTask(int id) {
        Task task = super.getTask(id);
        save();
        return task;
    }

    @Override
    public boolean createTask(Task task) {
        boolean isCreated = super.createTask(task);
        if (isCreated) {
            save();
            return true;
        }
        return false;
    }

    @Override
    public boolean updateTask(Task task) {
        boolean isUpdated = super.updateTask(task);
        if (isUpdated) {
            save();
            return true;
        }
        return false;
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }
}
