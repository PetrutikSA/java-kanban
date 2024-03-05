package tasks;

import org.junit.jupiter.api.Test;
import tasks.enums.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {
    @Test
    public void tasksEqualsIfIdEquals () {
        Task task1 = new Task ("task1", "description1", Status.NEW);
        Task task2 = new Task ("task2", "description2", Status.NEW);
        task1.setId(1);
        task2.setId(1);
        assertEquals(task1,task2, "Задачи не равны");
    }
}