package engineer.skyouo.plugins.naturerevive.spigot.tasks;

public interface Task extends Runnable {
    boolean isAsync();

    long getDelay();

    long getRepeatTime();
}
