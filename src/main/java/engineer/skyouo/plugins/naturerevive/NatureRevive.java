package engineer.skyouo.plugins.naturerevive;

import engineer.skyouo.plugins.naturerevive.commands.RevertCommand;
import engineer.skyouo.plugins.naturerevive.commands.SnapshotCommand;
import engineer.skyouo.plugins.naturerevive.config.DatabaseConfig;
import engineer.skyouo.plugins.naturerevive.config.ReadonlyConfig;
import engineer.skyouo.plugins.naturerevive.listeners.ChunkRelatedEventListener;
import engineer.skyouo.plugins.naturerevive.manager.Queue;
import engineer.skyouo.plugins.naturerevive.manager.Task;
import engineer.skyouo.plugins.naturerevive.structs.ChunkPos;
import engineer.skyouo.plugins.naturerevive.structs.PositionInfo;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.List;

public final class NatureRevive extends JavaPlugin {
    static {
        ConfigurationSerialization.registerClass(PositionInfo.class, "PositionInfo");
    }

    public static DatabaseConfig databaseConfig;
    public static ReadonlyConfig readonlyConfig;

    private Queue queue = new Queue();

    @Override
    public void onEnable() {
        // Plugin startup logic

        databaseConfig = new DatabaseConfig();
        readonlyConfig = new ReadonlyConfig();

        getCommand("snapshot").setExecutor(new SnapshotCommand(this));
        getCommand("revert").setExecutor(new RevertCommand(this));

        getServer().getPluginManager().registerEvents(new ChunkRelatedEventListener(), this);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            List<PositionInfo> positionInfos = databaseConfig.values();
            for (PositionInfo positionInfo : positionInfos) {
                System.out.println("Checking " + positionInfo);
                if (positionInfo.isOverTTL()) {
                    queue.add(new Task(positionInfo));
                    databaseConfig.unset(positionInfo);
                }
            }
        }, 20L, readonlyConfig.checkChunkTTLTick);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (queue.size() > 0) {
                Task task = queue.pop();
                if (PositionInfo.isResidence(task.getLocation())) return;
                task.regenerateChunk();
            }
        }, 20L, readonlyConfig.taskPerTick);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            try {
                databaseConfig.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, readonlyConfig.dataSaveTime, readonlyConfig.dataSaveTime);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
