import tasks.*;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        //Тестирование
        Task task1 = new Task("Task1", "First task to complete", Status.NEW);
        Task task2 = new Task("Task2", "Second task to complete", Status.NEW);
        Epic epic1 = new Epic("Epic1", "First epic to complete");
        Epic epic2 = new Epic("Epic2", "Second epic to complete");
        Subtask subtask1 = new Subtask("Subtask1", "First tasks.Subtask to first epic", Status.NEW, epic1);
        Subtask subtask2 = new Subtask("Subtask2", "Second tasks.Subtask to first epic", Status.NEW, epic1);
        Subtask subtask3 = new Subtask("Subtask3", "First tasks.Subtask to second epic", Status.NEW, epic2);

        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(epic1);
        taskManager.createTask(epic2);
        taskManager.createTask(subtask1);
        taskManager.createTask(subtask2);
        taskManager.createTask(subtask3);

        printAll(taskManager);

        Subtask taskUpdate1 = new Subtask("Subtask3", "Changed connected epic", Status.IN_PROGRESS, epic1);
        taskUpdate1.setId(7);
        taskManager.updateTask(taskUpdate1);
        task1.setStatus(Status.DONE);
        taskManager.updateTask(task1);
        printAll(taskManager);

        Epic taskUpdate2 = new Epic("Epic1", "Changed epic and subtasks");
        taskUpdate2.addSubTasks(taskUpdate1);
        taskUpdate2.setId(3);
        taskUpdate1.setStatus(Status.DONE);
        taskManager.updateTask(taskUpdate2);
        taskManager.removeTask(2);
        printAll(taskManager);

        taskManager.removeTaskPool(TaskTypes.EPIC);
        printAll(taskManager);
    }

    public static void printAll(TaskManager taskManager) {
        System.out.println(taskManager.getTaskPool(TaskTypes.TASK));
        System.out.println(taskManager.getTaskPool(TaskTypes.EPIC));
        System.out.println(taskManager.getTaskPool(TaskTypes.SUBTASK));
        System.out.println();
    }
}
