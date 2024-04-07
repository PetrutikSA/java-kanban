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
import java.util.List;

import tasks.enums.Status;
import tasks.enums.TaskTypes;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private final String fileName;
    private final String currentDir;
    private final Path file;

    FileBackedTaskManager() { // значения по умолчанию
        fileName = "db.csv";
        currentDir = System.getProperty("user.dir");
        file = Paths.get(currentDir, fileName);
    }

    FileBackedTaskManager(String fileName, String currentDir) {
        this.fileName = fileName;
        this.currentDir = currentDir;
        file = Paths.get(currentDir, fileName);
    }

    void save() {
        StringBuilder sb = new StringBuilder();
        sb.append("DB/History,TaskType,Id,Name,Description,Status,Epic/Subtasks");
        if (!taskPool.isEmpty()) {
            for (Integer key : taskPool.keySet()) {
                sb.append(String.format("\nDB,%s", taskPool.get(key).saveToString()));
            }
        }
        if (!epicPool.isEmpty()) {
            for (Integer key : epicPool.keySet()) {
                sb.append(String.format("\nDB,%s", epicPool.get(key).saveToString()));
            }
        }
        if (!subtaskPool.isEmpty()) {
            for (Integer key : subtaskPool.keySet()) {
                sb.append(String.format("\nDB,%s", subtaskPool.get(key).saveToString()));
            }
        }
        List<Task> history = getHistory();
        if (!history.isEmpty()) {
            for (Task task : history) {
                sb.append(String.format("\nHistory,%s", task.saveToString()));
            }
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
                for (String s : contentArray) {
                    String[] line = s.split(",");
                    boolean isDataBaseValue = line[0].equals("DB");
                    TaskTypes taskType = TaskTypes.valueOf(line[1]);
                    int id = Integer.parseInt(line[2]);
                    String name = line[3];
                    String description = line[4];
                    Status status = Status.valueOf(line[5]);
                    if (fileBackedTaskManager.lastTaskId < id) fileBackedTaskManager.lastTaskId = id;

                    switch (taskType) {
                        case TASK:
                            Task task = new Task(name, description, status);
                            task.setId(id);
                            if (isDataBaseValue) {
                                fileBackedTaskManager.taskPool.put(id, task);
                            } else {
                                fileBackedTaskManager.historyManager.addTaskToHistory(task);
                            }
                            break;
                        case EPIC:
                            Epic epic = new Epic(name, description);
                            epic.setId(id);
                            epic.setStatus(status);
                            if (!line[6].isBlank()) {
                                String[] subtasksId = line[6].split("_");
                                for (String currentSubtaskId : subtasksId) {
                                    epic.addSubTasks(Integer.parseInt(currentSubtaskId));
                                }
                            }
                            if (isDataBaseValue) {
                                fileBackedTaskManager.epicPool.put(id, epic);
                            } else {
                                fileBackedTaskManager.historyManager.addTaskToHistory(epic);
                            }
                            break;
                        case SUBTASK:
                            int epicId = Integer.parseInt(line[6]);
                            Subtask subtask = new Subtask(name, description, status, epicId);
                            subtask.setEpicId(id);
                            if (isDataBaseValue) {
                                fileBackedTaskManager.subtaskPool.put(id, subtask);
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
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }
}
