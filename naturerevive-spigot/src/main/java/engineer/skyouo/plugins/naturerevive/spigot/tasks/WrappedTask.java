package engineer.skyouo.plugins.naturerevive.spigot.tasks;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.scheduler.BukkitTask;

public class WrappedTask {
    private final Object task;
    public WrappedTask(Object wrappedTask) {
        if (!(wrappedTask instanceof ScheduledTask) && !(wrappedTask instanceof BukkitTask))
            throw new RuntimeException(String.format("Wrapped task should be ScheduledTask or BukkitTask, got %s",
                    wrappedTask == null ? null : wrappedTask.getClass()));

        task = wrappedTask;
    }

    public void cancel() {
        if (task instanceof ScheduledTask foliaTask)
            foliaTask.cancel();

        if (task instanceof BukkitTask bukkitTask)
            bukkitTask.cancel();
    }
}
