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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected final String taskInDBNotEqualToCreated = "Созданная и полученная задача не совпадает";
    protected final String taskPoolSizeError = "Некорректный размер пулла задач";
    protected final String epicPoolSizeError = "Некорректный размер пулла эпиков";
    protected final String subtaskPoolSizeError = "Некорректный размер пулла подзадач";
    protected final String poolReturnsNull = "Пул возвращает null";
    protected final String incorrectDeleting = "Удалена некорректная задача";
    protected final String incorrectUpdating = "Данные по задаче изменены некорректно";
    protected final String epicStatusError = "Некорректный статус Эпика";
    protected final String incorrectPrioritizedTaskOrder = "Некорректный порядок приоритета при создании задач";
    protected final String userChangeTaskWithoutTaskManager = "Пользователь изменил данные извне";
    protected final String crossingTaskPeriodError = "Не должны быть внесены в базу данные по пересекающейся задаче";
    protected final String ifDBWasNotUpdatedShouldReturnFalse = "Должно вернуться false при попытке обновления базы";
    protected final String historyReturnNull = "История не возвращается из HistoryManager";
    protected final String historySizeError = "Некорректный размер истории";
    protected final String historyOrderError = "Некорректный порядок задач в истории";

    protected abstract T createTaskManager();

    @BeforeEach
    void beforeEach() {
        taskManager = createTaskManager();
        addTasks();
    }

    void addTasks() {
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
                taskManager.getTask(2).toString(), taskInDBNotEqualToCreated);
        assertEquals("Epic{id='5, 'name='Epic2', description='Second epic to complete', status=NEW, "
                        + "subtasksNumber='1'}", taskManager.getTask(5).toString(),
                taskInDBNotEqualToCreated);
        assertEquals("Subtask{id='9, 'name='Subtask4', description='First Subtask to second epic', "
                        + "status=NEW, epicID='5'}", taskManager.getTask(9).toString(),
                taskInDBNotEqualToCreated);
    }

    @Test
    void correctFormingPoolsOfTasks() {
        assertEquals(3, taskManager.getTasksList().size(), taskPoolSizeError);
        assertEquals(2, taskManager.getEpicsList().size(), epicPoolSizeError);
        assertEquals(4, taskManager.getSubtasksList().size(), subtaskPoolSizeError);
    }

    @Test
    void correctRemoveTaskPool() {
        taskManager.removeTaskPool();
        assertNotNull(taskManager.getTasksList(), poolReturnsNull);
        assertTrue(taskManager.getTasksList().isEmpty(), taskPoolSizeError);
        assertFalse(taskManager.getEpicsList().isEmpty(), epicPoolSizeError);
        assertFalse(taskManager.getSubtasksList().isEmpty(), subtaskPoolSizeError);
    }

    @Test
    void correctRemoveEpicPool() {
        taskManager.removeEpicPool();
        assertNotNull(taskManager.getEpicsList(), poolReturnsNull);
        assertTrue(taskManager.getEpicsList().isEmpty(), epicPoolSizeError);
        assertFalse(taskManager.getTasksList().isEmpty(), taskPoolSizeError);
        assertTrue(taskManager.getSubtasksList().isEmpty(), subtaskPoolSizeError);
    }

    @Test
    void correctRemoveSubtaskPool() {
        taskManager.removeSubtaskPool();
        assertNotNull(taskManager.getSubtasksList(), poolReturnsNull);
        assertTrue(taskManager.getSubtasksList().isEmpty(), subtaskPoolSizeError);
        assertFalse(taskManager.getTasksList().isEmpty(), taskPoolSizeError);
        List<Epic> epics = taskManager.getEpicsList();
        assertFalse(epics.isEmpty(), epicPoolSizeError);
        for (Epic epic : epics) {
            assertTrue(epic.getSubTasksIds().isEmpty(), "Подзадачи в эпиках не очищены");
            assertEquals(Status.NEW, epic.getStatus(), "Статус не указан как Новый");
        }
    }

    @Test
    void correctRemoveTask() {
        taskManager.removeTask(2);
        assertEquals(2, taskManager.getTasksList().size(), taskPoolSizeError);
        assertNotNull(taskManager.getTask(1), incorrectDeleting);
        assertNotNull(taskManager.getTask(3), incorrectDeleting);
        assertEquals(2, taskManager.getEpicsList().size(), epicPoolSizeError);
        assertEquals(4, taskManager.getSubtasksList().size(), subtaskPoolSizeError);
    }

    @Test
    void correctRemoveEpic() {
        taskManager.removeTask(4);
        assertEquals(3, taskManager.getTasksList().size(), taskPoolSizeError);
        assertEquals(1, taskManager.getEpicsList().size(), epicPoolSizeError);
        assertNotNull(taskManager.getTask(5), incorrectDeleting);
        assertEquals(1, taskManager.getSubtasksList().size(), subtaskPoolSizeError);
    }

    @Test
    void correctRemoveSubtask() {
        taskManager.removeTask(7);
        assertEquals(3, taskManager.getTasksList().size(), taskPoolSizeError);
        assertEquals(2, taskManager.getEpicsList().size(), epicPoolSizeError);
        assertEquals(3, taskManager.getSubtasksList().size(), subtaskPoolSizeError);
        assertNotNull(taskManager.getTask(6), incorrectDeleting);
        assertNotNull(taskManager.getTask(8), incorrectDeleting);
        assertNotNull(taskManager.getTask(9), incorrectDeleting);
        Epic epic = (Epic) taskManager.getTask(4);
        assertEquals(2, epic.getSubTasksIds().size(), "Не отредактирован перечень подзадач у  эпика");
    }

    @Test
    void correctUpdateTask() {
        Task newTask = new Task("Task2", "Second task updated Status", Status.DONE,
                LocalDateTime.of(2024, 4, 30, 12, 0), Duration.ofHours(3));
        newTask.setId(2);
        taskManager.updateTask(newTask);
        correctFormingPoolsOfTasks();
        assertEquals("Task{id='2, 'name='Task2', description='Second task updated Status', status=DONE'}",
                taskManager.getTask(2).toString(), incorrectUpdating);
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
                "subtasksNumber='1'}", taskManager.getTask(5).toString(), incorrectUpdating);
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
                taskManager.getTask(8).toString(), incorrectUpdating);
        assertEquals("Subtask{id='9, 'name='Subtask4', description='First Subtask to second epic status " +
                        "change', status=DONE, epicID='5'}",
                taskManager.getTask(9).toString(), incorrectUpdating);
        assertEquals(Status.IN_PROGRESS, taskManager.getTask(4).getStatus(), epicStatusError);
        assertEquals(Status.DONE, taskManager.getTask(5).getStatus(), epicStatusError);
    }

    @Test
    void clearingEpicsSubtaskListWhenDeleteSubtask() {
        taskManager.removeTask(9);
        Epic epic1 = (Epic) taskManager.getTask(4);
        Epic epic2 = (Epic) taskManager.getTask(5);
        assertEquals(3, epic1.getSubTasksIds().size(), subtaskPoolSizeError);
        assertEquals(0, epic2.getSubTasksIds().size(), subtaskPoolSizeError);
    }

    @Test
    void dataInBaseCannotBeChangedWithoutUsingManager() {
        Task task = new Task("Name", "Description", Status.NEW,
                LocalDateTime.of(2025, 10, 2, 0, 0), Duration.ofDays(1));
        taskManager.createTask(task);
        task.setId(16);
        Task taskInManager = taskManager.getTask(16);
        assertNull(taskInManager, userChangeTaskWithoutTaskManager);
        taskInManager = taskManager.getTask(10);
        taskInManager.setStatus(Status.DONE);
        assertEquals("Task{id='10, 'name='Name', description='Description', status=NEW'}",
                taskManager.getTask(10).toString(), userChangeTaskWithoutTaskManager);
    }

    @Test
    void taskIntervalCrossingCheckWhenCreated() {
        boolean isTaskCreated = taskManager.createTask(new Task("NewTask", "Task to complete",
                Status.NEW, LocalDateTime.of(2024, 5, 13, 18, 30), Duration.ofDays(2)));
        assertFalse(isTaskCreated, ifDBWasNotUpdatedShouldReturnFalse);
        assertNull(taskManager.getTask(10), crossingTaskPeriodError);
    }

    @Test
    void taskIntervalCrossingCheckWhenUpdated() {
        Subtask updatedSubtask = new Subtask("Subtask2", "Second Subtask to first epic", Status.NEW,
                4, LocalDateTime.of(2024, 6, 3, 9, 30), Duration.ofDays(1));
        updatedSubtask.setId(7);
        boolean isTaskUpdated = taskManager.updateTask(updatedSubtask);
        assertFalse(isTaskUpdated, ifDBWasNotUpdatedShouldReturnFalse);
        assertEquals("Subtask{id='7, 'name='Subtask2', description='Second Subtask to first epic', "
                        + "status=NEW, epicID='4'}", taskManager.getTask(7).toString(),
                crossingTaskPeriodError);
    }

    @Test
    void epicTimeControl() {
        Epic epic = (Epic) taskManager.getTask(4);
        LocalDateTime epicStart = epic.getStartTime();
        LocalDateTime epicEnd = epic.getEndTime();
        assertEquals(epicStart, LocalDateTime.of(2024, 4, 28, 9, 0),
                "дата начала не совпадает с началом самой ранней подзадачи");
        assertEquals(epicEnd, LocalDateTime.of(2024, 6, 5, 9, 30),
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
    void correctPrioritizedTaskListFormingWhileCreating() {
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(6, prioritizedTasks.get(0).getId(), incorrectPrioritizedTaskOrder);
        assertEquals(2, prioritizedTasks.get(2).getId(), incorrectPrioritizedTaskOrder);
        assertEquals(9, prioritizedTasks.get(6).getId(), incorrectPrioritizedTaskOrder);
    }

    @Test
    void correctPrioritizedTaskListFormingWhileUpdating() {
        Task task2updated = new Task("Task2", "Second task to complete", Status.NEW
                , LocalDateTime.of(2024, 4, 20, 0, 0), Duration.ofHours(1));
        task2updated.setId(2);
        Subtask subtask4updated = new Subtask("Subtask4", "First Subtask to second epic", Status.NEW, 5);
        subtask4updated.setId(9);
        taskManager.updateTask(task2updated);
        taskManager.updateTask(subtask4updated);

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(2, prioritizedTasks.get(0).getId(), incorrectPrioritizedTaskOrder);
        assertEquals(3, prioritizedTasks.get(2).getId(), incorrectPrioritizedTaskOrder);
        assertEquals(8, prioritizedTasks.get(5).getId(), incorrectPrioritizedTaskOrder);
    }

    @Test
    void correctPrioritizedTaskListFormingWhenDeletingTask() {
        taskManager.removeTask(2);
        taskManager.removeTask(9);

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(1, prioritizedTasks.get(2).getId(), incorrectPrioritizedTaskOrder);
        assertEquals(6, prioritizedTasks.get(0).getId(), incorrectPrioritizedTaskOrder);
        assertEquals(8, prioritizedTasks.get(4).getId(), incorrectPrioritizedTaskOrder);
    }

    @Test
    void correctPrioritizedTaskListFormingWhenDeletingPool() {
        taskManager.removeEpicPool();

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(3, prioritizedTasks.get(0).getId(), incorrectPrioritizedTaskOrder);
        assertEquals(2, prioritizedTasks.get(1).getId(), incorrectPrioritizedTaskOrder);
        assertEquals(1, prioritizedTasks.get(2).getId(), incorrectPrioritizedTaskOrder);
    }

    @Test
    void getHistory() {
        taskManager.getTask(1);
        List<Task> tasks = taskManager.getHistory();
        assertNotNull(tasks, historyReturnNull);
        assertEquals(1, tasks.size(), historySizeError);
        taskManager.removeTask(1);
        tasks = taskManager.getHistory();
        assertTrue(tasks.isEmpty(), historySizeError);
    }

    @Test
    void addTaskToHistory() {
        taskManager.getTask(1);
        taskManager.createTask(new Task("Name2", "Description2", Status.NEW, LocalDateTime.of(2024, 4, 30, 0,15), Duration.ofDays(2)));
        assertEquals(1, taskManager.getHistory().size(), "Задача в историю добавляется при создании, а не просмотре");
        taskManager.getTask(2);
        assertEquals(2, taskManager.getHistory().size(), "Задача не добавлена в историю после просмотра");
    }

    @Test
    void correctSizeHistoryControl() {
        taskManager.getTask(1);
        for (int i = 1; i <= 13; i++) {
            taskManager.createTask(new Task("Name" + i, "Description" + i, Status.NEW, LocalDateTime.of(2024, 4, i, 0,15), Duration.ofDays(1)));
            taskManager.getTask(i + 9); //девять уже есть в BeforeEach
        }

        for (int i = 1; i <= 5; i++) {
            taskManager.getTask(i + 9);
        }

        List<Task> history = taskManager.getHistory();
        assertEquals(14, history.size(), historySizeError);
        assertEquals("Task{id='1, 'name='Task1', description='First task to complete', status=NEW'}",
                history.get(0).toString(), historyOrderError);
        assertEquals("Task{id='19, 'name='Name10', description='Description10', status=NEW'}",
                history.get(5).toString(), historyOrderError);
        assertEquals("Task{id='10, 'name='Name1', description='Description1', status=NEW'}",
                history.get(9).toString(), historyOrderError);
    }

    @Test
    void historyManagerSaveTaskLastConditions() {
        Task updateTask = new Task("Name", "Task done", Status.DONE, LocalDateTime.of(2024, 4, 30, 0,15), Duration.ofDays(2));
        updateTask.setId(1);
        taskManager.updateTask(updateTask);
        taskManager.getTask(1);
        List<Task> tasks = taskManager.getHistory();
        assertEquals(1, tasks.size(), historySizeError);
        assertEquals("Task{id='1, 'name='Task1', description='First task to complete', status=NEW'}",
                tasks.get(0).toString(), "Вторая версия задачи не корректно сохранена");
    }

    @Test
    void historyManagerClearWhenRemoveEpicPool() {
        for (int i = 4; i < 8; i++) {
            taskManager.getTask(i);
        }
        List<Task> history = taskManager.getHistory();
        assertEquals(4, history.size(), historySizeError);
        taskManager.removeEpicPool();
        history = taskManager.getHistory();
        assertEquals(0, history.size(), historySizeError);
    }
}
