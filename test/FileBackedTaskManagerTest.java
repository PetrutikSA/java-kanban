import managers.tasks.FileBackedTaskManager;
import managers.tasks.ManagerSaveException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.enums.Status;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private static final String CURRENT_DIR = System.getProperty("user.dir");
    private Path testDB;
    private final Path defaultDB = Paths.get(CURRENT_DIR, "db.csv");

    @Override
    protected FileBackedTaskManager createTaskManager() {
        return new FileBackedTaskManager();
    }

    @Override
    void addTasks() {
        //Вызов в методе @BeforeEach абстрактоного класса
        //Для проверки дефолтной версии FileBackedTaskManager (db.csv)
        super.addTasks();
        //Для проверки версии загруженной из готового файла
        try {
            testDB = Files.createTempFile(Paths.get(CURRENT_DIR), "testDB", ".csv");
            try (Writer writer = new FileWriter(testDB.toFile())) {
                writer.append("DB/History,TaskType,Id,Name,Description,Status,StartTime,Duration,Epic/Subtasks,EpicEndTime\n");
                writer.append("DB,TASK,1,Task1,First task to complete,NEW,2024-05-12T18:30:00.000,2880\n");
                writer.append("DB,EPIC,2,Epic1,First epic to complete,NEW,2024-04-28T09:00:00.000,54750,4_5,2024-06-05T09:30:00.000\n");
                writer.append("DB,EPIC,3,Epic2,Second epic to complete,NEW, , ,6, \n");
                writer.append("DB,SUBTASK,4,Subtask1,First subtask to first epic,NEW,2024-04-28T09:00:00.000,480,2\n");
                writer.append("DB,SUBTASK,5,Subtask2,Second subtask to first epic,NEW,2024-06-01T09:30:00.000,5760,2\n");
                writer.append("DB,SUBTASK,6,Subtask3,First subtask to second epic,NEW,2024-07-03T15:15:00.000,1440,3\n");
                writer.append("History,TASK,1,Task1,First task to complete,NEW,2024-05-12T18:30:00.000,2880\n");
                writer.append("History,SUBTASK,6,Subtask3,First subtask to second epic,NEW, , ,3");
            }
        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage(), e.getCause());
        }
    }

    @AfterEach
    void afterEach() {
        try {
            Files.delete(testDB);
            Files.delete(defaultDB);
        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage(), e.getCause());
        }
    }

    @Test
    void loadManager() {
        taskManager = FileBackedTaskManager.load(testDB.toFile());

        assertEquals(1, taskManager.getTasksList().size(), taskPoolSizeError);
        assertEquals(2, taskManager.getEpicsList().size(), epicPoolSizeError);
        assertEquals(3, taskManager.getSubtasksList().size(), subtaskPoolSizeError);

        List<Task> history = taskManager.getHistory();
        assertNotNull(history, historyReturnNull);
        assertEquals(2, history.size(), historySizeError);

        assertEquals("Task{id='1, 'name='Task1', description='First task to complete', status=NEW'}",
                taskManager.getTask(1).toString(), taskInDBNotEqualToCreated);
        assertEquals("Epic{id='2, 'name='Epic1', description='First epic to complete', status=NEW, "
                        + "subtasksNumber='2'}", taskManager.getTask(2).toString(),
                taskInDBNotEqualToCreated);
        assertEquals("Subtask{id='6, 'name='Subtask3', description='First subtask to second epic', "
                        + "status=NEW, epicID='3'}", taskManager.getTask(6).toString(),
                taskInDBNotEqualToCreated);
    }

    @Test
    void removeTaskPool() {
        taskManager = FileBackedTaskManager.load(testDB.toFile());
        taskManager.removeTaskPool();
        taskManager = FileBackedTaskManager.load(testDB.toFile());
        assertTrue(taskManager.getTasksList().isEmpty(), taskPoolSizeError);
    }

    @Test
    void removeEpicPool() {
        taskManager = FileBackedTaskManager.load(testDB.toFile());
        taskManager.removeEpicPool();
        taskManager = FileBackedTaskManager.load(testDB.toFile());
        assertTrue(taskManager.getEpicsList().isEmpty(), epicPoolSizeError);
        assertTrue(taskManager.getSubtasksList().isEmpty(), subtaskPoolSizeError);
    }

    @Test
    void removeSubtaskPool() {
        taskManager = FileBackedTaskManager.load(testDB.toFile());
        taskManager.removeSubtaskPool();
        taskManager = FileBackedTaskManager.load(testDB.toFile());
        assertTrue(taskManager.getSubtasksList().isEmpty(), subtaskPoolSizeError);
        List<Epic> epics = taskManager.getEpicsList();
        for (Epic epic : epics) {
            assertTrue(epic.getSubTasksIds().isEmpty(), String.format("У Эпика %d не очищены подзадачи", epic.getId()));
        }
    }

    @Test
    void correctHistoryOrder() {
        taskManager = FileBackedTaskManager.load(testDB.toFile());
        taskManager.getTask(1);
        taskManager.getTask(3);
        taskManager = FileBackedTaskManager.load(testDB.toFile());
        List<Task> history = taskManager.getHistory();
        assertNotNull(history, historyReturnNull);
        assertEquals(3, history.size(), historySizeError);
        assertEquals("Subtask{id='6, 'name='Subtask3', description='First subtask to second epic', "
                + "status=NEW, epicID='3'}", history.get(0).toString(), historyOrderError);
        assertEquals("Task{id='1, 'name='Task1', description='First task to complete', status=NEW'}",
                history.get(1).toString(), historyOrderError);
        assertEquals("Epic{id='3, 'name='Epic2', description='Second epic to complete', status=NEW, "
                + "subtasksNumber='1'}", history.get(2).toString(), historyOrderError);
    }

    @Test
    void correctMaintainingIdNumbering() {
        taskManager = FileBackedTaskManager.load(testDB.toFile());
        taskManager.createTask(new Task("Task2", "Second task to complete", Status.NEW,
                LocalDateTime.of(2024, 7, 28, 12, 0), Duration.ofHours(2)));
        taskManager = FileBackedTaskManager.load(testDB.toFile());
        assertEquals("Task{id='7, 'name='Task2', description='Second task to complete', status=NEW'}",
                taskManager.getTask(7).toString(), "Некорректная нумерация новых задач");
    }

    @Test
    void updateTask() {
        taskManager = FileBackedTaskManager.load(testDB.toFile());
        Task newTask = new Task("Task1", "First task updated Status", Status.DONE,
                LocalDateTime.of(2024, 5, 12, 18, 30), Duration.ofDays(2));
        newTask.setId(1);
        taskManager.updateTask(newTask);
        taskManager = FileBackedTaskManager.load(testDB.toFile());
        assertEquals("Task{id='1, 'name='Task1', description='First task updated Status', status=DONE'}",
                taskManager.getTask(1).toString(), incorrectUpdating);
    }

    @Test
    void updateSubtask() {
        taskManager = FileBackedTaskManager.load(testDB.toFile());
        Subtask newSubtask = new Subtask("Subtask2", "Second subtask to first epic updated",
                Status.DONE, 2, LocalDateTime.of(2024, 6, 1, 9, 30),
                Duration.ofDays(4));
        newSubtask.setId(5);
        taskManager.updateTask(newSubtask);
        taskManager = FileBackedTaskManager.load(testDB.toFile());
        assertEquals("Epic{id='2, 'name='Epic1', description='First epic to complete', status=IN_PROGRESS, "
                + "subtasksNumber='2'}", taskManager.getTask(2).toString(), epicStatusError);
        assertEquals("Subtask{id='5, 'name='Subtask2', description='Second subtask to first epic updated', "
                + "status=DONE, epicID='2'}", taskManager.getTask(5).toString(), incorrectUpdating);
    }

    @Test
    void removeTask() {
        taskManager = FileBackedTaskManager.load(testDB.toFile());
        taskManager.removeTask(1);
        taskManager = FileBackedTaskManager.load(testDB.toFile());
        assertTrue(taskManager.getTasksList().isEmpty(), taskPoolSizeError);
        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size(), historySizeError);
    }

    @Test
    void removeEpic() {
        taskManager = FileBackedTaskManager.load(testDB.toFile());
        taskManager.removeTask(2);
        taskManager = FileBackedTaskManager.load(testDB.toFile());
        assertEquals(1, taskManager.getEpicsList().size(), epicPoolSizeError);
        assertEquals(1, taskManager.getSubtasksList().size(), subtaskPoolSizeError);
    }

    @Test
    void removeSubtask() {
        taskManager = FileBackedTaskManager.load(testDB.toFile());
        taskManager.removeTask(6);
        taskManager = FileBackedTaskManager.load(testDB.toFile());
        assertEquals(2, taskManager.getSubtasksList().size(), subtaskPoolSizeError);
        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size(), historySizeError);
        Epic connectedEpic = (Epic) taskManager.getTask(3);
        assertTrue(connectedEpic.getSubTasksIds().isEmpty(), epicPoolSizeError);
    }

    @Test
    void testLoadFileException() {
        assertThrows(ManagerSaveException.class, () -> {
            String file = "Wrong path";
            FileBackedTaskManager.load(Paths.get(CURRENT_DIR, file).toFile());
        }, "Загрузка по некорректному пути должна приводить к ошибке");
    }
}