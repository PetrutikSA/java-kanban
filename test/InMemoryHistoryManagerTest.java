import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.enums.Status;
import tasks.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryHistoryManagerTest {

    HistoryManager historyManager;
    Task task;

    @BeforeEach
    void beforeEach() {
        historyManager = Managers.getDefaultHistory();
        task = new Task("Name", "Description", Status.NEW);
        task.setId(1);
        historyManager.addTaskToHistory(task);
    }

    @Test
    void getHistory() {
        List<Task> tasks = historyManager.getHistory();
        assertNotNull(tasks, "История не возвращается из HistoryManager");
        assertEquals(1, tasks.size(), "Возвращается некорректный список истории просмотров задач");
    }

    @Test
    void addTaskToHistory() {
        Task task2 = new Task("Name2", "Description2", Status.NEW);
        historyManager.addTaskToHistory(task2);
        List<Task> tasks = historyManager.getHistory();
        assertEquals(2, tasks.size(), "Задача не добавлена");
    }

    @Test
    void correctSizeHistoryControl() {
        for (int i = 1; i <= 13; i++) {
            Task newTask = new Task("Name" + i, "Description" + i, Status.NEW);
            newTask.setId((i));
            historyManager.addTaskToHistory(newTask);
        }

        for (int i = 1; i < 6; i++) {
            Task newTask = new Task("Name" + i, "Description" + i, Status.NEW);
            newTask.setId((i));
            historyManager.addTaskToHistory(newTask);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(13, history.size(), "Некорректный размер истории");
        assertEquals("Task{id='6, 'name='Name6', description='Description6', status=NEW'}",
                history.get(0).toString(), "Некорректный порядок задач в истории");
        assertEquals("Task{id='11, 'name='Name11', description='Description11', status=NEW'}",
                history.get(5).toString(), "Некорректный порядок задач в истории");
        assertEquals("Task{id='1, 'name='Name1', description='Description1', status=NEW'}",
                history.get(8).toString(), "Некорректный порядок задач в истории");
    }

    @Test
    void historyManagerSaveTaskConditions() {
        TaskManager taskManager = Managers.getDefault();
        taskManager.createTask(task);
        taskManager.getTask(1);
        Task updateTask = new Task("Name", "Task done", Status.DONE);
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
        TaskManager taskManager = Managers.getDefault();
        Epic epic1 = new Epic("Epic1", "First epic to complete");
        Epic epic2 = new Epic("Epic2", "Second epic to complete");
        Subtask subtask1 = new Subtask("Subtask1", "First Subtask to first epic", Status.NEW, 1);
        Subtask subtask2 = new Subtask("Subtask2", "Second Subtask to first epic", Status.NEW, 2);
        taskManager.createTask(epic1);
        taskManager.createTask(epic2);
        taskManager.createTask(subtask1);
        taskManager.createTask(subtask2);
        taskManager.getTask(1);
        taskManager.getTask(2);
        taskManager.getTask(3);
        taskManager.getTask(4);
        List<Task> history = taskManager.getHistory();
        assertEquals(4, history.size(), "Некорректный размер истории");
        taskManager.removeEpicPool();
        history = taskManager.getHistory();
        assertEquals(0, history.size(), "Некорректный размер истории");
    }
}