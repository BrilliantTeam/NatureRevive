package engineer.skyouo.plugins.naturerevive.spigot.util;

import engineer.skyouo.plugins.naturerevive.spigot.tasks.WrappedTask;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class ScheduleUtil {
    private static final boolean isFolia;
    static {
        isFolia = Util.isFolia();
    }

    public static class GLOBAL {
        public static WrappedTask runTask(Plugin plugin, Runnable task) {
            if (isFolia) {
                return new WrappedTask(Bukkit.getGlobalRegionScheduler().run(plugin, (ignored) -> task.run()));
            } else {
                return new WrappedTask(Bukkit.getScheduler().runTask(plugin, task));
            }
        }

        public static WrappedTask runTaskLater(Plugin plugin, Runnable task, long delay) {
            if (isFolia) {
                return new WrappedTask(Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (ignored) -> task.run(), delay));
            } else {
                return new WrappedTask(Bukkit.getScheduler().runTaskLater(plugin, task, delay));
            }
        }

        public static WrappedTask runTaskTimer(Plugin plugin, Runnable task, long delay, long fixedRate) {
            if (isFolia) {
                return new WrappedTask(Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (ignored) -> task.run(), delay, fixedRate));
            } else {
                return new WrappedTask(Bukkit.getScheduler().runTaskTimer(plugin, task, delay, fixedRate));
            }
        }

        public static WrappedTask runTaskAsynchronously(Plugin plugin, Runnable task) {
            if (isFolia) {
                return new WrappedTask(Bukkit.getAsyncScheduler().runNow(plugin, (ignored) -> task.run()));
            } else {
                return new WrappedTask(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
            }
        }

        public static WrappedTask runTaskLaterAsynchronously(Plugin plugin, Runnable task, long delay) {
            if (isFolia) {
                return new WrappedTask(Bukkit.getAsyncScheduler().runDelayed(plugin, (ignored) -> task.run(), delay * 50L, TimeUnit.MILLISECONDS));
            } else {
                return new WrappedTask(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay));
            }
        }

        public static WrappedTask runTaskTimerAsynchronously(Plugin plugin, Runnable task, long delay, long fixedRate) {
            if (isFolia) {
                return new WrappedTask(Bukkit.getAsyncScheduler().runAtFixedRate(plugin, (ignored) -> task.run(), delay * 50L, fixedRate * 50L, TimeUnit.MILLISECONDS));
            } else {
                return new WrappedTask(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, fixedRate));
            }
        }
    }

    public static class REGION {
        public static WrappedTask runTask(Plugin plugin, Chunk chunk, Runnable task) {
            if (isFolia) {
                return new WrappedTask(Bukkit.getRegionScheduler().run(plugin, chunk.getWorld(), chunk.getX(), chunk.getZ(), (ignored) -> task.run()));
            } else {
                return new WrappedTask(Bukkit.getScheduler().runTask(plugin, task));
            }
        }

        public static WrappedTask runTask(Plugin plugin, Location location, Runnable task) {
            if (isFolia) {
                return new WrappedTask(Bukkit.getRegionScheduler().run(plugin, location, (ignored) -> task.run()));
            } else {
                return new WrappedTask(Bukkit.getScheduler().runTask(plugin, task));
            }
        }

        public static WrappedTask runTaskLater(Plugin plugin, Chunk chunk, Runnable task, long delay) {
            if (isFolia) {
                return new WrappedTask(Bukkit.getRegionScheduler().runDelayed(plugin, chunk.getWorld(), chunk.getX(), chunk.getZ(), (ignored) -> task.run(), delay));
            } else {
                return new WrappedTask(Bukkit.getScheduler().runTaskLater(plugin, task, delay));
            }
        }

        public static WrappedTask runTaskLater(Plugin plugin, Location location, Runnable task, long delay) {
            if (isFolia) {
                return new WrappedTask(Bukkit.getRegionScheduler().runDelayed(plugin, location, (ignored) -> task.run(), delay));
            } else {
                return new WrappedTask(Bukkit.getScheduler().runTaskLater(plugin, task, delay));
            }
        }

        public static WrappedTask runTaskTimer(Plugin plugin, Chunk chunk, Runnable task, long delay, long fixedRate) {
            if (isFolia) {
                return new WrappedTask(Bukkit.getRegionScheduler().runAtFixedRate(plugin, chunk.getWorld(), chunk.getX(), chunk.getZ(), (ignored) -> task.run(), delay, fixedRate));
            } else {
                return new WrappedTask(Bukkit.getScheduler().runTaskTimer(plugin, task, delay, fixedRate));
            }
        }

        public static WrappedTask runTaskTimer(Plugin plugin, Location location, Runnable task, long delay, long fixedRate) {
            if (isFolia) {
                return new WrappedTask(Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, (ignored) -> task.run(), delay, fixedRate));
            } else {
                return new WrappedTask(Bukkit.getScheduler().runTaskTimer(plugin, task, delay, fixedRate));
            }
        }
    }

    public static class ENTITY {
        public static WrappedTask runTask(Plugin plugin, Entity entity, Runnable task) {
            if (isFolia) {
                return new WrappedTask(entity.getScheduler().run(plugin, (ignored) -> task.run(), null));
            } else {
                return new WrappedTask(Bukkit.getScheduler().runTask(plugin, task));
            }
        }

        public static WrappedTask runTaskLater(Plugin plugin, Entity entity, Runnable task, long delay) {
            if (isFolia) {
                return new WrappedTask(entity.getScheduler().runDelayed(plugin, (ignored) -> task.run(), null, delay));
            } else {
                return new WrappedTask(Bukkit.getScheduler().runTaskLater(plugin, task, delay));
            }
        }

        public static WrappedTask runTaskTimer(Plugin plugin, Entity entity, Runnable task, long delay, long fixedRate) {
            if (isFolia) {
                return new WrappedTask(entity.getScheduler().runAtFixedRate(plugin, (ignored) -> task.run(), null, delay, fixedRate));
            } else {
                return new WrappedTask(Bukkit.getScheduler().runTaskTimer(plugin, task, delay, fixedRate));
            }
        }
    }
}
