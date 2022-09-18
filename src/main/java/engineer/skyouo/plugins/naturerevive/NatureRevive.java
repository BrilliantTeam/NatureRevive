package engineer.skyouo.plugins.naturerevive;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.api.ResidenceInterface;
import engineer.skyouo.plugins.naturerevive.commands.*;
import engineer.skyouo.plugins.naturerevive.config.DatabaseConfig;
import engineer.skyouo.plugins.naturerevive.config.ReadonlyConfig;
import engineer.skyouo.plugins.naturerevive.listeners.ChunkRelatedEventListener;
import engineer.skyouo.plugins.naturerevive.listeners.ObfuscateLootListener;
import engineer.skyouo.plugins.naturerevive.manager.Queue;
import engineer.skyouo.plugins.naturerevive.manager.Task;
import engineer.skyouo.plugins.naturerevive.structs.PositionInfo;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
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
    public static DataStore GriefPreventionAPI;

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

        logger = getLogger();

        if (!ChickSoftDependPlugin()){
            logger.warning("disable plugin!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getCommand("snapshot").setExecutor(new SnapshotCommand(this));
        getCommand("revert").setExecutor(new RevertCommand(this));
        getCommand("forceregenall").setExecutor(new ForceRegenAllCommand(this));
        getCommand("testrandomizeore").setExecutor(new TestRandomizeOreCommand());
        getCommand("reloadreviveconfig").setExecutor(new ReloadCommand());

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
                    if (PositionInfo.isGriefPrevention(task.getLocation()) && !readonlyConfig.GriefPreventionStrictCheck) return;
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

    private boolean ChickSoftDependPlugin(){
        Plugin coreProtectPlugin = getServer().getPluginManager().getPlugin("CoreProtect");
        coreProtectAPI = coreProtectPlugin != null ? CoreProtect.getInstance().getAPI() : null;
        if (coreProtectAPI != null){
            logger.info("CoreProtect plugin found and Hook!");
        }

        if (readonlyConfig.residenceStrictCheck){
            Plugin residencePlugin = getServer().getPluginManager().getPlugin("Residence");
            residenceApi = residencePlugin != null ? ResidenceApi.getResidenceManager() : null;
            if (residenceApi == null){
                logger.warning("Residence plugin not found!");
                return false;
            }
            logger.info("Residence plugin found and Hook!");
        }

        if (readonlyConfig.GriefPreventionStrictCheck){
            Plugin GriefPreventionPlugin = getServer().getPluginManager().getPlugin("GriefPrevention");
            GriefPreventionAPI = GriefPreventionPlugin != null ? GriefPrevention.instance.dataStore : null;
            if (GriefPreventionAPI == null){
                logger.warning("GriefPrevention plugin not found!");
                return false;
            }
            logger.info("GriefPrevention plugin found and Hook!");
        }

        return true;
    }
}
