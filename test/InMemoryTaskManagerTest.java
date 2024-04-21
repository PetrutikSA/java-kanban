import managers.Managers;
import managers.tasks.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tasks.Epic;
import tasks.enums.Status;
import tasks.Subtask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void beforeEach() {
        taskManager = Managers.getDefault();
        taskManager.createTask(new Task("Task1", "First task to complete", Status.NEW,
                LocalDateTime.of(2024, 5, 12, 18, 30), Duration.ofDays(2)));
        taskManager.createTask(new Task("Task2", "Second task to complete", Status.NEW,
                LocalDateTime.of(2024, 4, 30, 12, 0), Duration.ofHours(3)));
        taskManager.createTask(new Task("Task3", "Third task to complete", Status.NEW,
                LocalDateTime.of(2024, 4, 29, 21, 0), Duration.ofMinutes(30)));
        taskManager.createTask(new Epic("Epic1", "First epic to complete"));
        taskManager.createTask(new Epic("Epic2", "Second epic to complete"));
        taskManager.createTask(new Subtask("Subtask1", "First Subtask to first epic", Status.NEW,
                4, LocalDateTime.of(2024, 4, 28, 9, 0), Duration.ofHours(8)));
        taskManager.createTask(new Subtask("Subtask2", "Second Subtask to first epic", Status.NEW,
                4, LocalDateTime.of(2024, 5, 15, 10, 30), Duration.ofMinutes(25)));
        taskManager.createTask(new Subtask("Subtask3", "Third Subtask to first epic", Status.NEW,
                4, LocalDateTime.of(2024, 6, 1, 9, 30), Duration.ofDays(4)));
        taskManager.createTask(new Subtask("Subtask4", "First Subtask to second epic", Status.NEW,
                5, LocalDateTime.of(2024, 7, 3, 15, 15), Duration.ofDays(1)));
    }

    @Test
    void getSameTaskByIdAsWasCreated() {
        assertEquals("Task{id='2, 'name='Task2', description='Second task to complete', status=NEW'}",
                taskManager.getTask(2).toString(), "Созданная и полученная задача не совпадает)");
        assertEquals("Epic{id='5, 'name='Epic2', description='Second epic to complete', status=NEW, "
                        + "subtasksNumber='1'}", taskManager.getTask(5).toString(),
                "Созданный и полученный эпик не совпадает)");
        assertEquals("Subtask{id='9, 'name='Subtask4', description='First Subtask to second epic', "
                        + "status=NEW, epicID='5'}", taskManager.getTask(9).toString(),
                "Созданная и полученная подзадача не совпадает)");
    }

    @Test
    void correctFormingPoolsOfTasks() {
        assertEquals(3, taskManager.getTasksList().size(), "Некорректный размер пулла задач");
        assertEquals(2, taskManager.getEpicsList().size(), "Некорректный размер пулла эпиков");
        assertEquals(4, taskManager.getSubtasksList().size(), "Некорректный размер пулла подзадач");
    }

    @Test
    void correctRemoveTaskPool() {
        taskManager.removeTaskPool();
        assertNotNull(taskManager.getTasksList(), "Пулл задач возвращает null");
        assertTrue(taskManager.getTasksList().isEmpty(), "Пулл задач не очищен");
        assertFalse(taskManager.getEpicsList().isEmpty(), "Пулл эпиков так же очищен");
        assertFalse(taskManager.getSubtasksList().isEmpty(), "Пулл подзадач так же очищен");
    }

    @Test
    void correctRemoveEpicPool() {
        taskManager.removeEpicPool();
        assertNotNull(taskManager.getEpicsList(), "Пулл эпиков возвращает null");
        assertTrue(taskManager.getEpicsList().isEmpty(), "Пулл эпиков не очищен");
        assertFalse(taskManager.getTasksList().isEmpty(), "Пулл задач так же очищен");
        assertTrue(taskManager.getSubtasksList().isEmpty(), "Пулл подзадач не очищен");
    }

    @Test
    void correctRemoveSubtaskPool() {
        taskManager.removeSubtaskPool();
        assertNotNull(taskManager.getSubtasksList(), "Пулл подзадач возвращает null");
        assertTrue(taskManager.getSubtasksList().isEmpty(), "Пулл подзадач не очищен");
        assertFalse(taskManager.getTasksList().isEmpty(), "Пулл задач так же очищен");
        List<Epic> epics = taskManager.getEpicsList();
        assertFalse(epics.isEmpty(), "Пулл эпиков так же очищен");
        for (Epic epic : epics) {
            assertTrue(epic.getSubTasksIds().isEmpty(), "Подзадачи в эпиках не очищены");
            assertEquals(Status.NEW, epic.getStatus(), "Статус не указан как Новый");
        }
    }

    @Test
    void correctRemoveTask() {
        taskManager.removeTask(2);
        assertEquals(2, taskManager.getTasksList().size(), "Некорректный размер пулла задач");
        assertNotNull(taskManager.getTask(1), "Удалена некорректная задача");
        assertNotNull(taskManager.getTask(3), "Удалена некорректная задача");
        assertEquals(2, taskManager.getEpicsList().size(), "Некорректный размер пулла эпиков");
        assertEquals(4, taskManager.getSubtasksList().size(), "Некорректный размер пулла подзадач");
    }

    @Test
    void correctRemoveEpic() {
        taskManager.removeTask(4);
        assertEquals(3, taskManager.getTasksList().size(), "Некорректный размер пулла задач");
        assertEquals(1, taskManager.getEpicsList().size(), "Некорректный размер пулла эпиков");
        assertNotNull(taskManager.getTask(5), "Удалена некорректный эпик");
        assertEquals(1, taskManager.getSubtasksList().size(), "Некорректный размер пулла подзадач");
    }

    @Test
    void correctRemoveSubtask() {
        taskManager.removeTask(7);
        assertEquals(3, taskManager.getTasksList().size(), "Некорректный размер пулла задач");
        assertEquals(2, taskManager.getEpicsList().size(), "Некорректный размер пулла эпиков");
        assertEquals(3, taskManager.getSubtasksList().size(), "Некорректный размер пулла подзадач");
        assertNotNull(taskManager.getTask(6), "Удалена некорректная подзадача");
        assertNotNull(taskManager.getTask(8), "Удалена некорректная подзадача");
        assertNotNull(taskManager.getTask(9), "Удалена некорректная подзадача");
        Epic epic = (Epic) taskManager.getTask(4);
        assertEquals(2, epic.getSubTasksIds().size(), "Не отредактирован перечень подзадач у  эпика");
    }

    @Test
    void correctUpdateTask() {
        Task newTask = new Task("Task2", "Second task updatet Status", Status.DONE,
                LocalDateTime.of(2024, 4, 30, 12, 0), Duration.ofHours(3));
        newTask.setId(2);
        taskManager.updateTask(newTask);
        correctFormingPoolsOfTasks();
        assertEquals("Task{id='2, 'name='Task2', description='Second task updatet Status', status=DONE'}",
                taskManager.getTask(2).toString(), "Изменения проведены некорректно");
    }

    @Test
    void correctUpdateEpic() {
        Epic newEpic = new Epic("Epic2", "Second epic updated");
        newEpic.setId(5);
        newEpic.addSubTasks(9);
        newEpic.setStartTime(LocalDateTime.of(2024, 7, 3, 15, 15));
        newEpic.setEndTime(LocalDateTime.of(2024, 7, 4, 15, 15));
        newEpic.setDuration(Duration.ofDays(1));
        taskManager.updateTask(newEpic);
        correctFormingPoolsOfTasks();
        assertEquals("Epic{id='5, 'name='Epic2', description='Second epic updated', status=NEW, " +
                        "subtasksNumber='1'}", taskManager.getTask(5).toString(), "Изменения проведены некорректно");
    }

    @Test
    void correctUpdateSubtaskAndConnectedEpicStatus() {
        Subtask newSubtask3 = new Subtask("Subtask3", "Third Subtask status change", Status.IN_PROGRESS,
                4, LocalDateTime.of(2024, 6, 1, 9, 30), Duration.ofDays(4));
        Subtask newSubtask4 = new Subtask("Subtask4", "First Subtask to second epic status change",
                Status.DONE, 5, LocalDateTime.of(2024, 7, 3, 15, 15),
                Duration.ofDays(1));
        newSubtask3.setId(8);
        newSubtask4.setId(9);
        taskManager.updateTask(newSubtask3);
        taskManager.updateTask(newSubtask4);
        correctFormingPoolsOfTasks();
        assertEquals("Subtask{id='8, 'name='Subtask3', description='Third Subtask status change', " +
                        "status=IN_PROGRESS, epicID='4'}",
                taskManager.getTask(8).toString(), "Изменения проведены некорректно");
        assertEquals("Subtask{id='9, 'name='Subtask4', description='First Subtask to second epic status " +
                        "change', status=DONE, epicID='5'}",
                taskManager.getTask(9).toString(), "Изменения проведены некорректно");
        assertEquals(Status.IN_PROGRESS, taskManager.getTask(4).getStatus(), "Статус эпика не изменен");
        assertEquals(Status.DONE, taskManager.getTask(5).getStatus(), "Статус эпика не изменен");
    }

    @Test
    void clearingEpicsSubtaskListWhenDeleteSubtask() {
        taskManager.removeTask(9);
        Epic epic1 = (Epic) taskManager.getTask(4);
        Epic epic2 = (Epic) taskManager.getTask(5);
        assertEquals(3, epic1.getSubTasksIds().size(), "Некорректный размер списка подзадач");
        assertEquals(0, epic2.getSubTasksIds().size(), "Некорректный размер списка подзадач");
    }

    @Test
    void dataInBaseCannotBeChangedWithoutUsingManager() {
        Task task = new Task("Name", "Description", Status.NEW,
                LocalDateTime.of(2025, 10, 2, 0, 0), Duration.ofDays(1));
        taskManager.createTask(task);
        task.setId(16);
        Task taskInManager = taskManager.getTask(16);
        assertNull(taskInManager, "Пользователь изменил данные извне");
        taskInManager = taskManager.getTask(10);
        taskInManager.setStatus(Status.DONE);
        assertEquals("Task{id='10, 'name='Name', description='Description', status=NEW'}",
                taskManager.getTask(10).toString(), "Пользователь изменил данные через метод getTask");
    }

    @Test
    void taskIntervalCrossingCheckWhenCreated() {
        boolean isTaskCreated = taskManager.createTask(new Task("NewTask", "Task to complete",
                Status.NEW, LocalDateTime.of(2024, 5, 13, 18, 30), Duration.ofDays(2)));
        assertFalse(isTaskCreated, "Доблно вернуться false при попытке создания пересекающейся задачи");
        assertNull(taskManager.getTask(10), "Задача не должна добавиться");
    }

    @Test
    void taskIntervalCrossingCheckWhenUpdated() {
        Subtask updatedSubtask = new Subtask("Subtask2", "Second Subtask to first epic", Status.NEW,
                4, LocalDateTime.of(2024, 6, 3, 9, 30), Duration.ofDays(1));
        updatedSubtask.setId(7);
        boolean isTaskUpdated = taskManager.updateTask(updatedSubtask);
        assertFalse(isTaskUpdated, "Доблно вернуться false при попытке обновления пересекающейся задачи");
        assertEquals("Subtask{id='7, 'name='Subtask2', description='Second Subtask to first epic', "
                        + "status=NEW, epicID='4'}", taskManager.getTask(7).toString(),
                "Задача не должна была обновиться");
    }

    @Test
    void epicTimeControl() {
        Epic epic = (Epic) taskManager.getTask(4);
        LocalDateTime epicStart = epic.getStartTime();
        LocalDateTime epicEnd = epic.getEndTime();
        assertEquals(epicStart, LocalDateTime.of(2024, 4, 28, 9, 0),
                "дата начала не совпадает с началом самой ранней подзадачи");
        assertEquals(epicEnd,LocalDateTime.of(2024, 6, 5, 9, 30),
                "дата окончания не совпадает с окончанием самой поздней подзадачи");

        taskManager.removeTask(6);
        taskManager.removeTask(8);
        epic = (Epic) taskManager.getTask(4);
        epicStart = epic.getStartTime();
        epicEnd = epic.getEndTime();
        assertEquals(LocalDateTime.of(2024, 5, 15, 10, 30), epicStart,
                "после удаление подзадач дата начала не пересматривается");
        assertEquals(LocalDateTime.of(2024, 5, 15, 10, 55), epicEnd,
                "после удаление подзадач дата окончания не пересматривается");

        taskManager.removeSubtaskPool();
        epic = (Epic) taskManager.getTask(4);
        epicStart = epic.getStartTime();
        epicEnd = epic.getEndTime();

        assertNull(epicStart, "У эпика без подзадач должна быть обнулена дата начала");
        assertNull(epicEnd, "У эпика без подзадач должна быть обнулена дата окончания");
    }

    @Test
    void correctPrioritizedTaskListFormingWhileCreating () {
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(6, prioritizedTasks.get(0).getId(),
                "Некорректный пордок приоритета при создании задач");
        assertEquals(2, prioritizedTasks.get(2).getId(),
                "Некорректный пордок приоритета при создании задач");
        assertEquals(9, prioritizedTasks.get(6).getId(),
                "Некорректный пордок приоритета при создании задач");
    }

    @Test
    void correctPrioritizedTaskListFormingWhileUpdating () {
        Task task2updated =  new Task("Task2", "Second task to complete", Status.NEW
                , LocalDateTime.of(2024, 4, 20, 0, 0), Duration.ofHours(1));
        task2updated.setId(2);
        Subtask subtask4updated= new Subtask("Subtask4", "First Subtask to second epic", Status.NEW, 5);
        subtask4updated.setId(9);
        taskManager.updateTask(task2updated);
        taskManager.updateTask(subtask4updated);

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(2, prioritizedTasks.get(0).getId(),
                "Некорректный пордок приоритета при обновлении задач");
        assertEquals(3, prioritizedTasks.get(2).getId(),
                "Некорректный пордок приоритета при обновлении задач");
        assertEquals(8, prioritizedTasks.get(5).getId(),
                "Некорректный пордок приоритета при обновлении задач");
    }

    @Test
    void correctPrioritizedTaskListFormingWhenDeletingTask () {
        taskManager.removeTask(2);
        taskManager.removeTask(9);

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(6, prioritizedTasks.get(0).getId(),
                "Некорректный пордок приоритета при удалении задач");
        assertEquals(1, prioritizedTasks.get(2).getId(),
                "Некорректный пордок приоритета при удалении задач");
        assertEquals(8, prioritizedTasks.get(4).getId(),
                "Некорректный пордок приоритета при удалении задач");
    }

    @Test
    void correctPrioritizedTaskListFormingWhenDeletingPool () {
        taskManager.removeEpicPool();

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(3, prioritizedTasks.get(0).getId(),
                "Некорректный пордок приоритета при удалении задач");
        assertEquals(2, prioritizedTasks.get(1).getId(),
                "Некорректный пордок приоритета при удалении задач");
        assertEquals(1, prioritizedTasks.get(2).getId(),
                "Некорректный пордок приоритета при удалении задач");
    }
}