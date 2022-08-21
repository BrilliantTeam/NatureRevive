package engineer.skyouo.plugins.naturerevive.manager;

import java.util.ArrayDeque;

public class Queue {
    private final java.util.Queue<Task> taskQueue;

    public Queue() {
        taskQueue = new ArrayDeque<>();
    }

    public void add(Task task) {
        taskQueue.offer(task);
    }

    public Task pop() {
        return taskQueue.poll();
    }

    public int size() {
        return taskQueue.size();
    }
}
