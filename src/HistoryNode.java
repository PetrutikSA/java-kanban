import tasks.Task;

public class HistoryNode {
    private Task task;
    private HistoryNode previous;
    private HistoryNode next;

    public HistoryNode(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public HistoryNode getPrevious() {
        return previous;
    }

    public void setPrevious(HistoryNode previous) {
        this.previous = previous;
    }

    public HistoryNode getNext() {
        return next;
    }

    public void setNext(HistoryNode next) {
        this.next = next;
    }
}
