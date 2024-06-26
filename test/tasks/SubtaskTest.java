package tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.enums.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubtaskTest {
    private Subtask subtask1;
    private Subtask subtask2;

    @Test
    public void subtasksEqualsIfIdEquals() {
        assertEquals(subtask1, subtask2, "Подзадачи не равны");
    }

    @Test
    void setEpicId() {
        subtask1.setEpicId(3);
        subtask1.setEpicId(1);
        assertEquals(3, subtask1.getEpicId(), "Id эпика равен Id подзадачи");
    }

    @BeforeEach
    void beforeEach() {
        subtask1 = new Subtask("task1", "description1", Status.NEW, 2);
        subtask2 = new Subtask("task2", "description2", Status.NEW, 2);
        subtask1.setId(1);
        subtask2.setId(1);
    }
}