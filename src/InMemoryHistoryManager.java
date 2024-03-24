import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, HistoryNode> historyLinkedMap = new HashMap<>();
    private HistoryNode head; //самая старая запись
    private HistoryNode tail; //самая новая запись

    @Override
    public void addTaskToHistory(Task task) {
        int taskId = task.getId();
        HistoryNode node = new HistoryNode(task);
        remove(taskId);
        if (head == null) { //Если голова нулл то и хвост нулл
            head = node;
            tail = node;
        } else {
            HistoryNode oldTail = tail;
            tail = node;
            oldTail.setNext(node);
            node.setPrevious(oldTail);
        }
        historyLinkedMap.put(taskId, node);
    }

    @Override
    public void remove(int id) {
        if (historyLinkedMap.containsKey(id)) {
            HistoryNode nodeToRemove = historyLinkedMap.get(id);
            HistoryNode prevNode = nodeToRemove.getPrevious();
            HistoryNode nextNode = nodeToRemove.getNext();
            if (prevNode == null && nextNode == null) {
                head = null;
                tail = null;
            } else if (prevNode == null) {
                nextNode.setPrevious(null);
                head = nextNode;
            } else if (nextNode == null) {
                prevNode.setNext(null);
                tail = prevNode;
            } else {
                prevNode.setNext(nextNode);
                nextNode.setPrevious(prevNode);
            }
            historyLinkedMap.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> tasksHistory = new ArrayList<>();
        if (head != null) {
            HistoryNode node = head; //от старых к новым
            while (node != null) {
                tasksHistory.add(node.getTask());
                node = node.getNext();
            }
        }
        return tasksHistory;
    }
}
