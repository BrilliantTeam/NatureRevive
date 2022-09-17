package engineer.skyouo.plugins.naturerevive.manager;

import java.util.ArrayDeque;

public class Queue<T> {
    private final java.util.Queue<T> taskQueue;

    public Queue() {
        taskQueue = new ArrayDeque<>();
    }

    public void add(T task) {
        taskQueue.offer(task);
    }

    public T pop() {
        return taskQueue.poll();
    }

    public boolean hasNext() {
        return taskQueue.size() > 0;
    }

    public int size() {
        return taskQueue.size();
    }
}
