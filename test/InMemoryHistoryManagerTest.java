import managers.Managers;
import managers.tasks.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.enums.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryHistoryManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void beforeEach() {
        taskManager = Managers.getDefault();
        taskManager.createTask(new Task("Name", "Description", Status.NEW, LocalDateTime.of(2024, 7, 3, 15,15), Duration.ofDays(1)));
        taskManager.getTask(1);
    }

    @Test
    void getHistory() {
        List<Task> tasks = taskManager.getHistory();
        assertNotNull(tasks, "История не возвращается из HistoryManager");
        assertEquals(1, tasks.size(), "Возвращается некорректный список истории просмотров задач");
    }

    @Test
    void addTaskToHistory() {
        taskManager.createTask(new Task("Name2", "Description2", Status.NEW, LocalDateTime.of(2024, 4, 30, 0,15), Duration.ofDays(2)));
        assertEquals(1, taskManager.getHistory().size(), "Задача в историю добавляется при создании, а не просмотре");
        taskManager.getTask(2);
        assertEquals(2, taskManager.getHistory().size(), "Задача не добавлена в историю после просмотра");
    }

    @Test
    void correctSizeHistoryControl() {
        for (int i = 1; i <= 13; i++) {
            taskManager.createTask(new Task("Name" + i, "Description" + i, Status.NEW, LocalDateTime.of(2024, 4, i, 0,15), Duration.ofDays(1)));
            taskManager.getTask(i + 1); //одна задача уже есть создана в BeforeEach
        }

        for (int i = 1; i <= 5; i++) {
            taskManager.getTask(i + 1);
        }

        List<Task> history = taskManager.getHistory();
        assertEquals(14, history.size(), "Некорректный размер истории");
        assertEquals("Task{id='1, 'name='Name', description='Description', status=NEW'}",
                history.get(0).toString(), "Некорректный порядок задач в истории"); //вызов в BeforeEach
        assertEquals("Task{id='11, 'name='Name10', description='Description10', status=NEW'}",
                history.get(5).toString(), "Некорректный порядок задач в истории");
        assertEquals("Task{id='2, 'name='Name1', description='Description1', status=NEW'}",
                history.get(9).toString(), "Некорректный порядок задач в истории");
    }

    @Test
    void historyManagerSaveTaskLastConditions() {
        Task updateTask = new Task("Name", "Task done", Status.DONE, LocalDateTime.of(2024, 4, 30, 0,15), Duration.ofDays(2));
        updateTask.setId(1);
        taskManager.updateTask(updateTask);
        taskManager.getTask(1);
        List<Task> tasks = taskManager.getHistory();
        assertEquals(1, tasks.size(), "Некорректный размер истории");
        assertEquals("Task{id='1, 'name='Name', description='Task done', status=DONE'}",
                tasks.get(0).toString(), "Вторая версия задачи не корректно сохранена");
    }

    @Test
    void historyManagerClearWhenRemoveEpicPool() {
        Epic epic1 = new Epic("Epic1", "First epic to complete");
        Epic epic2 = new Epic("Epic2", "Second epic to complete");
        Subtask subtask1 = new Subtask("Subtask1", "First Subtask to first epic", Status.NEW, 2, LocalDateTime.of(2024, 4, 30, 0,15), Duration.ofDays(2));
        Subtask subtask2 = new Subtask("Subtask2", "Second Subtask to first epic", Status.NEW, 3, LocalDateTime.of(2024, 5, 18, 9,15), Duration.ofDays(1));
        taskManager.createTask(epic1);
        taskManager.createTask(epic2);
        taskManager.createTask(subtask1);
        taskManager.createTask(subtask2);
        taskManager.getTask(2);
        taskManager.getTask(3);
        taskManager.getTask(4);
        taskManager.getTask(5);
        List<Task> history = taskManager.getHistory();
        assertEquals(5, history.size(), "Некорректный размер истории");
        taskManager.removeEpicPool();
        history = taskManager.getHistory();
        assertEquals(1, history.size(), "Некорректный размер истории");
    }
}