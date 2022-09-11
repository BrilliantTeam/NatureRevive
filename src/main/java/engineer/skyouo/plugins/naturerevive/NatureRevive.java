package engineer.skyouo.plugins.naturerevive;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.api.ResidenceInterface;
import engineer.skyouo.plugins.naturerevive.commands.ForceRegenAllCommand;
import engineer.skyouo.plugins.naturerevive.commands.RevertCommand;
import engineer.skyouo.plugins.naturerevive.commands.SnapshotCommand;
import engineer.skyouo.plugins.naturerevive.commands.TestRandomizeOreCommand;
import engineer.skyouo.plugins.naturerevive.config.DatabaseConfig;
import engineer.skyouo.plugins.naturerevive.config.ReadonlyConfig;
import engineer.skyouo.plugins.naturerevive.listeners.ChunkRelatedEventListener;
import engineer.skyouo.plugins.naturerevive.listeners.ObfuscateLootListener;
import engineer.skyouo.plugins.naturerevive.manager.Queue;
import engineer.skyouo.plugins.naturerevive.manager.Task;
import engineer.skyouo.plugins.naturerevive.structs.PositionInfo;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.apache.logging.log4j.core.Core;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public final class NatureRevive extends JavaPlugin {
    static {
        ConfigurationSerialization.registerClass(PositionInfo.class, "PositionInfo");
    }

    public static ResidenceInterface residenceApi;
    public static CoreProtectAPI coreProtectAPI;

    public static DatabaseConfig databaseConfig;
    public static ReadonlyConfig readonlyConfig;

    public static Logger logger;

    public static JavaPlugin instance;

    public static Queue queue = new Queue();

    @Override
    public void onEnable() {
        // Plugin startup logic

        instance = this;

        databaseConfig = new DatabaseConfig();
        readonlyConfig = new ReadonlyConfig();

        residenceApi = ResidenceApi.getResidenceManager();
        coreProtectAPI = CoreProtect.getInstance().isEnabled() ? CoreProtect.getInstance().getAPI() : null;

        logger = getLogger();

        getCommand("snapshot").setExecutor(new SnapshotCommand(this));
        getCommand("revert").setExecutor(new RevertCommand(this));
        getCommand("forceregenall").setExecutor(new ForceRegenAllCommand(this));
        getCommand("testrandomizeore").setExecutor(new TestRandomizeOreCommand());

        getServer().getPluginManager().registerEvents(new ChunkRelatedEventListener(), this);
        getServer().getPluginManager().registerEvents(new ObfuscateLootListener(), this);

        getServer().getScheduler().runTaskTimer(this, () -> {
            List<PositionInfo> positionInfos = databaseConfig.values();
            for (PositionInfo positionInfo : positionInfos) {
                if (positionInfo.isOverTTL()) {
                    queue.add(new Task(positionInfo));
                    databaseConfig.unset(positionInfo);
                }
            }
        }, 20L, readonlyConfig.checkChunkTTLTick);

        getServer().getScheduler().runTaskTimer(this, () -> {
            if (queue.size() > 0) {
                for (int i = 0; i < readonlyConfig.taskPerProcess; i++) {
                    Task task = queue.pop();
                    if (PositionInfo.isResidence(task.getLocation()) && !readonlyConfig.residenceStrictCheck) return;
                    task.regenerateChunk();
                    if (readonlyConfig.debug)
                        logger.info(task.toString() + " was regenerated.");
                }
            }
        }, 20L, readonlyConfig.queuePerNTick);

        getServer().getScheduler().runTaskTimer(this, () -> {
            try {
                databaseConfig.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, readonlyConfig.dataSaveTime, readonlyConfig.dataSaveTime);
    }

    @Override
    public void onDisable() {
        // shutdown logic

        try {
            databaseConfig.save();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
