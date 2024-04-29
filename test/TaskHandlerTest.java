import tasks.Task;
import tasks.enums.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class TaskHandlerTest extends CrossingPeriodHandlerTest<Task>{
    @Override
    protected void init() {
        basePath = "/tasks";
        classOfT = Task.class;

        objectsInDB = new ArrayList<>();
        Task task1 = new Task(testObjects.task1);
        task1.setId(1);
        Task task2 = new Task(testObjects.task2);
        task2.setId(1);
        Task task3 = new Task(testObjects.task3);
        task3.setId(1);

        objectsInDB.add(task1);
        objectsInDB.add(task2);
        objectsInDB.add(task3);

        newObject = new Task("New", "NewDescription", Status.NEW,
                LocalDateTime.of(2024,7,1,18,0), Duration.ofDays(1));
        updatedObject = new Task(testObjects.task3);
        updatedObject.setId(3);
        updatedObject.setStatus(Status.DONE);
        updatedObject.setDuration(Duration.ofHours(2));

        notInDBObjectToUpdate = new Task("New", "NewDescription", Status.NEW,
                LocalDateTime.of(2024,7,1,18,0), Duration.ofDays(1));

        crossingPeriodNewObject = new Task(newObject);
        crossingPeriodNewObject.setStartTime(task3.getStartTime());

        crossingPeriodObjectToUpdate = new Task(updatedObject);
        crossingPeriodNewObject.setStartTime(task2.getStartTime().minusDays(1));
        crossingPeriodObjectToUpdate.setDuration(Duration.ofDays(2));
    }
}
