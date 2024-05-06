import tasks.Subtask;
import tasks.enums.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class SubtaskHandlerTest extends CrossingPeriodHandlerTest<Subtask>{
    @Override
    protected void init() {
        basePath = "/subtasks";
        classOfT = Subtask.class;

        objectsInDB = new ArrayList<>();
        Subtask subtask1 = new Subtask(testObjects.subtask1);
        subtask1.setId(6);
        Subtask subtask2 = new Subtask(testObjects.subtask2);
        subtask2.setId(7);
        Subtask subtask3 = new Subtask(testObjects.subtask3);
        subtask3.setId(8);
        Subtask subtask4 = new Subtask(testObjects.subtask4);
        subtask4.setId(9);
        objectsInDB.add(subtask1);
        objectsInDB.add(subtask2);
        objectsInDB.add(subtask3);
        objectsInDB.add(subtask4);

        newObject = new Subtask("NewSubtask", "NewSubtaskDescription", Status.IN_PROGRESS, 4,
                LocalDateTime.of(2024,7,1,18,0), Duration.ofDays(1));
        updatedObject = new Subtask(subtask4);
        updatedObject.setStatus(Status.DONE);
        updatedObject.setDuration(Duration.ofHours(2));

        notInDBObjectToUpdate = new Subtask("NewSubtask", "NewSubtaskDescription", Status.IN_PROGRESS, 4,
                LocalDateTime.of(2024,7,1,18,0), Duration.ofDays(1));

        crossingPeriodNewObject = new Subtask(newObject);
        crossingPeriodNewObject.setStartTime(subtask2.getStartTime());

        crossingPeriodObjectToUpdate = new Subtask(updatedObject);
        crossingPeriodObjectToUpdate.setStartTime(subtask2.getStartTime().minusDays(1));
        crossingPeriodObjectToUpdate.setDuration(Duration.ofDays(2));
    }
}
