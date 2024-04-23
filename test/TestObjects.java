import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.enums.Status;

import java.time.Duration;
import java.time.LocalDateTime;

public class TestObjects {
    public Task task1 = new Task("Task1", "First task to complete", Status.NEW,
            LocalDateTime.of(2024, 5, 12, 18, 30), Duration.ofDays(2));
    public Task task2 = new Task("Task2", "Second task to complete", Status.NEW,
            LocalDateTime.of(2024, 4, 30, 12, 0), Duration.ofHours(3));
    public Task task3 = new Task("Task3", "Third task to complete", Status.NEW,
            LocalDateTime.of(2024, 4, 29, 21, 0), Duration.ofMinutes(30));
    public Epic epic1 = new Epic("Epic1", "First epic to complete");
    public Epic epic2 = new Epic("Epic2", "Second epic to complete");
    public Subtask subtask1 = new Subtask("Subtask1", "First Subtask to first epic", Status.NEW,
            4, LocalDateTime.of(2024, 4, 28, 9, 0), Duration.ofHours(8));
    public Subtask subtask2 = new Subtask("Subtask2", "Second Subtask to first epic", Status.NEW,
            4, LocalDateTime.of(2024, 5, 15, 10, 30), Duration.ofMinutes(25));
    public Subtask subtask3 = new Subtask("Subtask3", "Third Subtask to first epic", Status.NEW,
            4, LocalDateTime.of(2024, 6, 1, 9, 30), Duration.ofDays(4));
    public Subtask subtask4 = new Subtask("Subtask4", "First Subtask to second epic", Status.NEW,
            5, LocalDateTime.of(2024, 7, 3, 15, 15), Duration.ofDays(1));
}
