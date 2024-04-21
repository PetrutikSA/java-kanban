import managers.tasks.FileBackedTaskManager;
import managers.tasks.ManagerSaveException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

class FileBackedTaskManagerTest {
    private static final String CURRENT_DIR = System.getProperty("user.dir");
    private Path testDB;

    @BeforeEach
    void beforeEach() {
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
        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage(), e.getCause());
        }
    }

    @Test
    void createDefaultManager() {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager();
        fileBackedTaskManager.createTask(new Task("Task1", "First task to complete", Status.NEW,
                LocalDateTime.of(2024, 5, 12, 18, 30), Duration.ofDays(2)));
        String fileName = "db.csv";
        Path file = Paths.get(CURRENT_DIR, fileName);
        assertTrue(Files.exists(file), "Файл по умолчанию не создан");

        fileBackedTaskManager.createTask(new Epic("Epic1", "Epic to complete"));
        fileBackedTaskManager.createTask(new Subtask("Subtask1", "First Subtask to first epic",
                Status.DONE, 2, LocalDateTime.of(2024, 4, 28, 9, 0),
                Duration.ofHours(8)));
        fileBackedTaskManager.createTask(new Subtask("Subtask2", "Second Subtask to first epic",
                Status.NEW, 2, LocalDateTime.of(2024, 5, 15, 10, 30),
                Duration.ofMinutes(25)));
        fileBackedTaskManager.getTask(1);
        fileBackedTaskManager.getTask(2);
        fileBackedTaskManager.getTask(1);

        FileBackedTaskManager afterLoadFileBackedTaskManager = FileBackedTaskManager.load(file.toFile());
        List<Task> history = afterLoadFileBackedTaskManager.getHistory();
        assertNotNull(history, "История не возвращается из managers.history.HistoryManager");
        System.out.println(afterLoadFileBackedTaskManager.getTask(1));
        assertEquals(2, history.size(), "Возвращается некорректный список истории просмотров задач");
        assertEquals("Task{id='1, 'name='Task1', description='First task to complete', status=NEW'}",
                afterLoadFileBackedTaskManager.getTask(1).toString(), "Созданная и полученная задача " +
                        "не совпадает");
        assertEquals("Epic{id='2, 'name='Epic1', description='Epic to complete', status=IN_PROGRESS, "
                        + "subtasksNumber='2'}", afterLoadFileBackedTaskManager.getTask(2).toString(),
                "Созданный и полученный эпик не совпадает");
        assertEquals("Subtask{id='3, 'name='Subtask1', description='First Subtask to first epic', "
                        + "status=DONE, epicID='2'}", afterLoadFileBackedTaskManager.getTask(3).toString(),
                "Созданная и полученная подзадача не совпадает");
        assertEquals("Subtask{id='4, 'name='Subtask2', description='Second Subtask to first epic', "
                        + "status=NEW, epicID='2'}", afterLoadFileBackedTaskManager.getTask(4).toString(),
                "Созданная и полученная подзадача не совпадает");
        try {
            Files.delete(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void loadManager() {
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());

        assertEquals(1, fileBackedTaskManager.getTasksList().size(),
                "Некорректный размер пулла задач");
        assertEquals(2, fileBackedTaskManager.getEpicsList().size(),
                "Некорректный размер пулла эпиков");
        assertEquals(3, fileBackedTaskManager.getSubtasksList().size(),
                "Некорректный размер пулла подзадач");

        List<Task> history = fileBackedTaskManager.getHistory();
        assertNotNull(history, "История не возвращается из managers.history.HistoryManager");
        assertEquals(2, history.size(), "Возвращается некорректный список истории просмотров задач");

        assertEquals("Task{id='1, 'name='Task1', description='First task to complete', status=NEW'}",
                fileBackedTaskManager.getTask(1).toString(), "Созданная и полученная задача не совпадает");
        assertEquals("Epic{id='2, 'name='Epic1', description='First epic to complete', status=NEW, "
                        + "subtasksNumber='2'}", fileBackedTaskManager.getTask(2).toString(),
                "Созданный и полученный эпик не совпадает");
        assertEquals("Subtask{id='6, 'name='Subtask3', description='First subtask to second epic', "
                        + "status=NEW, epicID='3'}", fileBackedTaskManager.getTask(6).toString(),
                "Созданная и полученная подзадача не совпадает");
    }

    @Test
    void removeTaskPool() {
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());
        fileBackedTaskManager.removeTaskPool();
        FileBackedTaskManager afterLoadFileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());
        assertTrue(afterLoadFileBackedTaskManager.getTasksList().isEmpty(), "Некорректный размер пулла задач");
    }

    @Test
    void removeEpicPool() {
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());
        fileBackedTaskManager.removeEpicPool();
        FileBackedTaskManager afterLoadFileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());
        assertTrue(afterLoadFileBackedTaskManager.getEpicsList().isEmpty(),
                "Некорректный размер пулла эпиков");
        assertTrue(afterLoadFileBackedTaskManager.getSubtasksList().isEmpty(),
                "Некорректный размер пулла подзадач");
    }

    @Test
    void removeSubtaskPool() {
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());
        fileBackedTaskManager.removeSubtaskPool();
        FileBackedTaskManager afterLoadFileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());
        assertTrue(afterLoadFileBackedTaskManager.getSubtasksList().isEmpty(),
                "Некорректный размер пулла подзадач");
        List<Epic> epics = afterLoadFileBackedTaskManager.getEpicsList();
        for (Epic epic : epics) {
            assertTrue(epic.getSubTasksIds().isEmpty(), String.format("У Эпика %d не очищены подзадачи", epic.getId()));
        }
    }

    @Test
    void correctHistoryOrder() {
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());
        fileBackedTaskManager.getTask(1);
        fileBackedTaskManager.getTask(3);
        FileBackedTaskManager afterLoadFileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());
        List<Task> history = afterLoadFileBackedTaskManager.getHistory();
        assertNotNull(history, "История не возвращается из managers.history.HistoryManager");
        assertEquals(3, history.size(), "Возвращается некорректный список истории просмотров задач");
        assertEquals("Subtask{id='6, 'name='Subtask3', description='First subtask to second epic', "
                        + "status=NEW, epicID='3'}", history.get(0).toString(),
                "Некорректная последовательность вывода истории");
        assertEquals("Task{id='1, 'name='Task1', description='First task to complete', status=NEW'}",
                history.get(1).toString(), "Некорректная последовательность вывода истории");
        assertEquals("Epic{id='3, 'name='Epic2', description='Second epic to complete', status=NEW, "
                        + "subtasksNumber='1'}", history.get(2).toString(),
                "Некорректная последовательность вывода истории");
    }

    @Test
    void correctMaintainingIdNumbering() {
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());
        fileBackedTaskManager.createTask(new Task("Task2", "Second task to complete", Status.NEW,
                LocalDateTime.of(2024, 7, 28, 12, 0), Duration.ofHours(2)));
        FileBackedTaskManager afterLoadFileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());
        assertEquals("Task{id='7, 'name='Task2', description='Second task to complete', status=NEW'}",
                afterLoadFileBackedTaskManager.getTask(7).toString(), "Некорректная нумерация новых задач");
    }

    @Test
    void updateTask() {
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());
        Task newTask = new Task("Task1", "First task updated Status", Status.DONE,
                LocalDateTime.of(2024, 5, 12, 18, 30), Duration.ofDays(2));
        newTask.setId(1);
        fileBackedTaskManager.updateTask(newTask);
        FileBackedTaskManager afterLoadFileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());
        assertEquals("Task{id='1, 'name='Task1', description='First task updated Status', status=DONE'}",
                afterLoadFileBackedTaskManager.getTask(1).toString(), "Некорректно обновлена задача");
    }

    @Test
    void updateSubtask() {
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());
        Subtask newSubtask = new Subtask("Subtask2", "Second subtask to first epic updated",
                Status.DONE, 2, LocalDateTime.of(2024, 6, 1, 9, 30),
                Duration.ofDays(4));
        newSubtask.setId(5);
        fileBackedTaskManager.updateTask(newSubtask);
        FileBackedTaskManager afterLoadFileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());
        assertEquals("Epic{id='2, 'name='Epic1', description='First epic to complete', status=IN_PROGRESS, "
                        + "subtasksNumber='2'}", afterLoadFileBackedTaskManager.getTask(2).toString(),
                "У соответствующего эпика не пересмотрен статус");
        assertEquals("Subtask{id='5, 'name='Subtask2', description='Second subtask to first epic updated', "
                        + "status=DONE, epicID='2'}", afterLoadFileBackedTaskManager.getTask(5).toString(),
                "Некорректно обновлена задача");
    }

    @Test
    void removeTask() {
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());
        fileBackedTaskManager.removeTask(1);
        FileBackedTaskManager afterLoadFileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());
        assertTrue(afterLoadFileBackedTaskManager.getTasksList().isEmpty(), "Некорректный размер пулла задач");
        List<Task> history = afterLoadFileBackedTaskManager.getHistory();
        assertEquals(1, history.size(), "Возвращается некорректный список истории просмотров задач");
    }

    @Test
    void removeEpic() {
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());
        fileBackedTaskManager.removeTask(2);
        FileBackedTaskManager afterLoadFileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());
        assertEquals(1, afterLoadFileBackedTaskManager.getEpicsList().size(),
                "Некорректный размер пулла эпиков");
        assertEquals(1, afterLoadFileBackedTaskManager.getSubtasksList().size(),
                "Некорректный размер пулла подзадач");
    }

    @Test
    void removeSubtask() {
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());
        fileBackedTaskManager.removeTask(6);
        FileBackedTaskManager afterLoadFileBackedTaskManager = FileBackedTaskManager.load(testDB.toFile());
        assertEquals(2, afterLoadFileBackedTaskManager.getSubtasksList().size(),
                "Некорректный размер пулла подзадач");
        List<Task> history = afterLoadFileBackedTaskManager.getHistory();
        assertEquals(1, history.size(), "Возвращается некорректный список истории просмотров задач");
        Epic connectedEpic = (Epic) afterLoadFileBackedTaskManager.getTask(3);
        assertTrue(connectedEpic.getSubTasksIds().isEmpty(), "У Эпика не удалена подзадача");
    }

    @Test
    void testLoadFileException() {
        assertThrows(ManagerSaveException.class, () -> {
            String file = "Wrong path";
            FileBackedTaskManager.load(Paths.get(CURRENT_DIR, file).toFile());
        }, "Загрузка по некорректному пути должна приводить к ошибке");
    }
}