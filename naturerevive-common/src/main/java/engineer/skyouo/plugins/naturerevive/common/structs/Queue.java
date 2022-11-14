package engineer.skyouo.plugins.naturerevive.common.structs;

import java.util.ArrayDeque;
import java.util.Iterator;

public class Queue<T> {
    private java.util.Queue<T> taskQueue;

    public Queue() {
        taskQueue = new ArrayDeque<>();
    }

    public Queue(java.util.Queue queue) { taskQueue = queue; }

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

    public Iterator<T> iterator() { return taskQueue.iterator(); }

    public void load(java.util.Queue queue) { taskQueue = queue; }

    public Queue<T> clone() { return new Queue<>(((ArrayDeque) taskQueue).clone()); }
}
