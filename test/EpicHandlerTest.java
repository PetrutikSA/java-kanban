import tasks.Epic;

import java.time.Duration;
import java.util.ArrayList;

public class EpicHandlerTest extends BaseTaskHandlerTest<Epic> {

    @Override
    protected void init() {
        basePath = "/epics";
        classOfT = Epic.class;

        objectsInDB = new ArrayList<>();
        Epic epic1 = new Epic(testObjects.epic1);
        epic1.setId(4);
        epic1.setStartTime(testObjects.subtask1.getStartTime());
        epic1.setEndTime(testObjects.subtask3.getStartTime().plus(testObjects.subtask4.getDuration()));
        epic1.setDuration(Duration.between(epic1.getStartTime(), epic1.getEndTime()));

        Epic epic2 = new Epic(testObjects.epic1);
        epic2.setStartTime(testObjects.subtask4.getStartTime());
        epic2.setEndTime(testObjects.subtask1.getStartTime().plus(testObjects.subtask4.getDuration()));
        epic2.setDuration(Duration.between(epic2.getStartTime(), epic2.getEndTime()));
        epic2.setId(5);
        objectsInDB.add(epic1);
        objectsInDB.add(epic2);

        newObject = new Epic("NewEpic", "NewEpicDescription");
        updatedObject = new Epic(epic2.getName(), "UpdatedDescription");
        updatedObject.setId(epic2.getId());
        updatedObject.setSubTasksIds(epic2.getSubTasksIds());

        notInDBObjectToUpdate = new Epic("NewEpic", "NewEpicDescription");
        notInDBObjectToUpdate.setId(20);
    }
}
