import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Status;
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
        for (int i = 1; i < 13; i++) {
            Task newTask = new Task("Name" + i, "Description" + i, Status.NEW);
            newTask.setId((i + 1));
            historyManager.addTaskToHistory(newTask);
        }
        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size(), "Некорректный размер истории");
        assertEquals("Task{id='4, 'name='Name3', description='Description3', status=NEW'}",
                history.get(0).toString(), "Некорректный порядок задач в истории");
        assertEquals("Task{id='9, 'name='Name8', description='Description8', status=NEW'}",
                history.get(5).toString(), "Некорректный порядок задач в истории");
        assertEquals("Task{id='13, 'name='Name12', description='Description12', status=NEW'}",
                history.get(9).toString(), "Некорректный порядок задач в истории");
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
        assertEquals("Task{id='1, 'name='Name', description='Description', status=NEW'}",
                tasks.get(0).toString(), "Первая версия задачи не корректно сохранена");
        assertEquals("Task{id='1, 'name='Name', description='Task done', status=DONE'}",
                tasks.get(1).toString(), "Вторая версия задачи не корректно сохранена");
    }
}