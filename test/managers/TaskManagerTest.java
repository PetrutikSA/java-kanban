package managers;

import managers.exeptions.NotFoundException;
import managers.exeptions.PeriodCrossingException;
import managers.tasks.TaskManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.enums.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected TestObjects testObjects;

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
        testObjects = new TestObjects();
        testObjects.fillManagerWithTestObjects(taskManager);
    }

    @Test
    void getSameTaskByIdAsWasCreated() {
        Assertions.assertEquals(testObjects.task2.toString(),
                taskManager.getTask(2).toString(), taskInDBNotEqualToCreated);
        Assertions.assertEquals(testObjects.epic2.toString(), taskManager.getTask(5).toString(),
                taskInDBNotEqualToCreated);
        Assertions.assertEquals(testObjects.subtask4.toString(), taskManager.getTask(9).toString(),
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
        Task newTask = new Task(testObjects.task2);
        newTask.setId(2);
        newTask.setStatus(Status.DONE);
        taskManager.updateTask(newTask);
        correctFormingPoolsOfTasks();
        assertEquals(newTask.toString(), taskManager.getTask(2).toString(), incorrectUpdating);
    }

    @Test
    void correctUpdateEpic() {
        Epic newEpic = new Epic(testObjects.epic2);
        newEpic.setId(5);
        newEpic.addSubTasks(9);
        newEpic.setStartTime(LocalDateTime.of(2024, 7, 3, 15, 15));
        newEpic.setEndTime(LocalDateTime.of(2024, 7, 4, 15, 15));
        newEpic.setDuration(Duration.ofDays(1));
        taskManager.updateTask(newEpic);
        correctFormingPoolsOfTasks();
        assertEquals(newEpic.toString(), taskManager.getTask(5).toString(), incorrectUpdating);
    }

    @Test
    void correctUpdateSubtaskAndConnectedEpicStatus() {
        Subtask newSubtask3 = new Subtask(testObjects.subtask3);
        Subtask newSubtask4 = new Subtask(testObjects.subtask4);
        newSubtask3.setId(8);
        newSubtask3.setStatus(Status.IN_PROGRESS);
        newSubtask4.setId(9);
        newSubtask4.setStatus(Status.DONE);
        taskManager.updateTask(newSubtask3);
        taskManager.updateTask(newSubtask4);
        correctFormingPoolsOfTasks();
        assertEquals(newSubtask3.toString(), taskManager.getTask(8).toString(), incorrectUpdating);
        assertEquals(newSubtask4.toString(), taskManager.getTask(9).toString(), incorrectUpdating);
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

        assertThrows(NotFoundException.class, () -> {
            Task taskInManager = taskManager.getTask(16);
        }, crossingTaskPeriodError);

        Task taskInManager = taskManager.getTask(10);
        taskInManager.setStatus(Status.DONE);
        assertEquals("Task{id='10, 'name='Name', description='Description', status=NEW'}",
                taskManager.getTask(10).toString(), userChangeTaskWithoutTaskManager);
    }

    @Test
    void taskIntervalCrossingCheckWhenCreated() {
        assertThrows(PeriodCrossingException.class, () -> {
            taskManager.createTask(new Task("NewTask", "Task to complete", Status.NEW,
                    LocalDateTime.of(2024, 5, 13, 18, 30), Duration.ofDays(2)));
        }, crossingTaskPeriodError);
        assertThrows(NotFoundException.class, () -> taskManager.getTask(10), crossingTaskPeriodError);
    }

    @Test
    void taskIntervalCrossingCheckWhenUpdated() {
        Subtask updatedSubtask = new Subtask("Subtask2", "Second Subtask to first epic", Status.NEW,
                4, LocalDateTime.of(2024, 6, 3, 9, 30), Duration.ofDays(1));
        updatedSubtask.setId(7);
        assertThrows(PeriodCrossingException.class, () -> taskManager.updateTask(updatedSubtask), crossingTaskPeriodError);
        Assertions.assertEquals(testObjects.subtask2.toString(), taskManager.getTask(7).toString(),
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
        Task task2updated = new Task(testObjects.task2);
        task2updated.setStartTime(LocalDateTime.of(2024, 4, 20, 0, 0));
        task2updated.setDuration(Duration.ofHours(1));
        task2updated.setId(2);
        Subtask subtask4updated = new Subtask(testObjects.subtask4);
        subtask4updated.setStartTime(null);
        subtask4updated.setDuration(null);
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
        taskManager.createTask(new Task("Name2", "Description2", Status.NEW, LocalDateTime.of(2025, 4, 30, 0, 15), Duration.ofDays(2)));
        assertEquals(1, taskManager.getHistory().size(), "Задача в историю добавляется при создании, а не просмотре");
        taskManager.getTask(2);
        assertEquals(2, taskManager.getHistory().size(), "Задача не добавлена в историю после просмотра");
    }

    @Test
    void correctSizeHistoryControl() {
        taskManager.getTask(1);
        for (int i = 1; i <= 13; i++) {
            taskManager.createTask(new Task("Name" + i, "Description" + i, Status.NEW, LocalDateTime.of(2024, 4, i, 0, 15), Duration.ofDays(1)));
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
        Task updateTask = new Task("Name", "Task done", Status.DONE);
        updateTask.setId(1);
        taskManager.updateTask(updateTask);
        taskManager.getTask(1);
        List<Task> tasks = taskManager.getHistory();
        assertEquals(1, tasks.size(), historySizeError);
        assertEquals(updateTask.toString(), tasks.get(0).toString(), "Вторая версия задачи не корректно сохранена");
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


    @Test
    void epicStatusCheckWhenAllSubtasksNew() {
        Epic epic = (Epic) taskManager.getTask(4);
        assertEquals(Status.NEW, epic.getStatus(), epicStatusError);
    }

    @Test
    void epicStatusCheckWhenAllSubtasksDone() {
        List<Subtask> updatesSubtasks = new ArrayList<>();
        updatesSubtasks.add(new Subtask(testObjects.subtask1));
        updatesSubtasks.add(new Subtask(testObjects.subtask2));
        updatesSubtasks.add(new Subtask(testObjects.subtask3));
        for (int i = 0; i < 3; i++) {
            Subtask subtask = updatesSubtasks.get(i);
            subtask.setId(i + 6);
            subtask.setStatus(Status.DONE);
            taskManager.updateTask(subtask);
        }
        Epic epic = (Epic) taskManager.getTask(4);
        assertEquals(Status.DONE, epic.getStatus(), epicStatusError);
    }

    @Test
    void epicStatusCheckWhenSubtasksNewAndDone() {
        Subtask subtask = new Subtask(testObjects.subtask1);
        subtask.setId(6);
        subtask.setStatus(Status.DONE);
        taskManager.updateTask(subtask);
        Epic epic = (Epic) taskManager.getTask(4);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), epicStatusError);
    }

    @Test
    void epicStatusCheckWhenSubtasksInProgress() {
        List<Subtask> updatesSubtasks = new ArrayList<>();
        updatesSubtasks.add(new Subtask(testObjects.subtask1));
        updatesSubtasks.add(new Subtask(testObjects.subtask2));
        updatesSubtasks.add(new Subtask(testObjects.subtask3));
        for (int i = 0; i < 3; i++) {
            Subtask subtask = updatesSubtasks.get(i);
            subtask.setId(i + 6);
            subtask.setStatus(Status.IN_PROGRESS);
            taskManager.updateTask(subtask);
        }
        Epic epic = (Epic) taskManager.getTask(4);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), epicStatusError);
    }
}
