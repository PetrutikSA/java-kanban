package tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    Epic epic1;
    Epic epic2;

    @Test
    public void subtasksEqualsIfIdEquals () {
        assertEquals(epic1,epic2, "Эпики не равны");
    }

    @Test
    void addSubTasks() {
        epic1.addSubTasks(2);
        epic1.addSubTasks(1);
        assertEquals(1, epic1.getSubTasksId().size(), "Эпик добавил самого себя в свои подзадачи");
    }

    @BeforeEach
    void beforeEach() {
        epic1 = new Epic ("task1", "description1");
        epic2 = new Epic ("task2", "description2");
        epic1.setId(1);
        epic2.setId(1);
    }
}