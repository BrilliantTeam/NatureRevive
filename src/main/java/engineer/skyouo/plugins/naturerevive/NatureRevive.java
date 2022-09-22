package engineer.skyouo.plugins.naturerevive;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.api.ResidenceInterface;
import com.griefdefender.api.Core;
import com.griefdefender.api.GriefDefender;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import engineer.skyouo.plugins.naturerevive.commands.*;
import engineer.skyouo.plugins.naturerevive.config.DatabaseConfig;
import engineer.skyouo.plugins.naturerevive.config.ReadonlyConfig;
import engineer.skyouo.plugins.naturerevive.listeners.ChunkRelatedEventListener;
import engineer.skyouo.plugins.naturerevive.listeners.ObfuscateLootListener;
import engineer.skyouo.plugins.naturerevive.manager.Queue;
import engineer.skyouo.plugins.naturerevive.manager.SuspendedZone;
import engineer.skyouo.plugins.naturerevive.manager.Task;
import engineer.skyouo.plugins.naturerevive.structs.BlockDataChangeWithPos;
import engineer.skyouo.plugins.naturerevive.structs.BlockStateWithPos;
import engineer.skyouo.plugins.naturerevive.structs.ChunkPos;
import engineer.skyouo.plugins.naturerevive.structs.PositionInfo;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.TagParser;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public final class NatureRevive extends JavaPlugin {
    static {
        ConfigurationSerialization.registerClass(PositionInfo.class, "PositionInfo");
    }

    public static boolean enableRevive = true;

    public static ResidenceInterface residenceAPI;
    public static CoreProtectAPI coreProtectAPI;
    public static DataStore griefPreventionAPI;
    public static Core griefDefenderAPI;

    public static DatabaseConfig databaseConfig;
    public static ReadonlyConfig readonlyConfig;

    public static Logger logger;

    public static JavaPlugin instance;

    public static SuspendedZone suspendedZone;

    public static Queue<Task> queue = new Queue<>();
    public static final Queue<BlockStateWithPos> blockStateWithPosQueue = new Queue<>();
    public static final Queue<BlockDataChangeWithPos> blockDataChangeWithPos = new Queue<>();
    // reserved for synchronous CoreProtect logging

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        readonlyConfig = new ReadonlyConfig();
        databaseConfig = readonlyConfig.determineDatabase();

        suspendedZone = new SuspendedZone();

        logger = getLogger();

        if (readonlyConfig.debug) {
            logger.info("[DEBUG] =============");
            for (World world : getServer().getWorlds()) {
                logger.info(world.getName() + " - " + world.getEnvironment().name());
            }
            logger.info("[DEBUG] =============");
        }

        if (!checkSoftDependPlugins()){
            logger.warning("Disabling plugin due to lack of dependencies and enable the feature requires extra dependencies!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getCommand("snapshot").setExecutor(new SnapshotCommand(this));
        getCommand("revert").setExecutor(new RevertCommand(this));
        getCommand("forceregenall").setExecutor(new ForceRegenAllCommand(this));
        getCommand("testrandomizeore").setExecutor(new TestRandomizeOreCommand());
        getCommand("reloadreviveconfig").setExecutor(new ReloadCommand());
        getCommand("togglerevive").setExecutor(new ToggleChunkRegenerationCommand());
        getCommand("navdebug").setExecutor(new DebugCommand());

        getServer().getPluginManager().registerEvents(new ChunkRelatedEventListener(), this);
        getServer().getPluginManager().registerEvents(new ObfuscateLootListener(), this);

        getServer().getScheduler().runTaskTimer(this, () -> {
                    if (!readonlyConfig.regenerationStrategy.equalsIgnoreCase("passive") && !readonlyConfig.regenerationStrategy.equalsIgnoreCase("average")) {
                        List<PositionInfo> positionInfos = databaseConfig.values();
                        for (PositionInfo positionInfo : positionInfos) {
                            if (positionInfo.isOverTTL()) {
                                queue.add(new Task(positionInfo));
                                databaseConfig.unset(positionInfo);
                            }
                        }
                    }

                    if (readonlyConfig.regenerationStrategy.equalsIgnoreCase("average")) {
                        for (Player player : getServer().getOnlinePlayers()) {
                            for (int x = -1; x < 2; x++)
                                for (int z = -1; z < 2; z++) {
                                    if (x == z && x == 0)
                                        continue;

                                    PositionInfo positionInfo = databaseConfig.get(new ChunkPos(player.getWorld(), player.getLocation().getChunk().getX() + x, player.getLocation().getChunk().getZ() + z).toLocation());

                                    if (positionInfo == null)
                                        continue;

                                    if (positionInfo.isOverTTL()) {
                                        queue.add(new Task(positionInfo));
                                        databaseConfig.unset(positionInfo);
                                    }
                                }
                        }
                    }
        }, 20L, readonlyConfig.checkChunkTTLTick);

        getServer().getScheduler().runTaskTimer(this, () -> {
            if (queue.size() > 0 && isSuitableForChunkRegeneration()) {
                for (int i = 0; i < readonlyConfig.taskPerProcess && queue.hasNext(); i++) {
                    Task task = queue.pop();

                    if (NatureRevive.readonlyConfig.ignoredWorld.contains(task.getLocation().getWorld().getName()))
                        continue;

                    if (PositionInfo.isResidence(task.getLocation()) && !readonlyConfig.residenceStrictCheck)
                        continue;

                    if (PositionInfo.isGriefPrevention(task.getLocation()) && !readonlyConfig.griefPreventionStrictCheck)
                        continue;

                    if (PositionInfo.isGriefDefender(task.getLocation()) && !readonlyConfig.griefDefenderStrictCheck) {
                        continue;
                    }

                    task.regenerateChunk();

                    if (readonlyConfig.debug)
                        logger.info(task.toString() + " was regenerated.");
                }
            }
        }, 20L, readonlyConfig.queuePerNTick);

        getServer().getScheduler().runTaskTimer(this, () -> {
            for (int i = 0; i < readonlyConfig.blockPutPerTick && blockStateWithPosQueue.hasNext(); i++) {
                BlockStateWithPos blockStateWithPos = blockStateWithPosQueue.pop();

                Location location = blockStateWithPos.getLocation();

                BlockPos bp = new BlockPos(location.getX(), location.getY(), location.getZ());
                ((CraftWorld) location.getWorld()).getHandle().setBlock(bp, blockStateWithPos.getBlockState(), 3);

                if (blockStateWithPos.getTileEntityNbt() != null) {
                    try {
                        ((CraftWorld) location.getWorld()).getHandle()
                                .getBlockEntity(bp)
                                .load(TagParser.parseTag(blockStateWithPos.getTileEntityNbt()));
                    } catch (CommandSyntaxException | NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 20L, readonlyConfig.blockPutActionPerNTick);

        getServer().getScheduler().runTaskTimer(this, () -> {
            if (blockDataChangeWithPos.hasNext()) {
                for (int i = 0; i < 200 && blockDataChangeWithPos.hasNext(); i++) {
                    BlockDataChangeWithPos blockDataChangeWithPosObject = blockDataChangeWithPos.pop();

                    try {
                        if (blockDataChangeWithPosObject.getType().equals(BlockDataChangeWithPos.Type.REMOVAL) || blockDataChangeWithPosObject.getType().equals(BlockDataChangeWithPos.Type.REPLACE))
                            coreProtectAPI.logRemoval(readonlyConfig.coreProtectUserName, blockDataChangeWithPosObject.getLocation(), blockDataChangeWithPosObject.getOldBlockData().getMaterial(), blockDataChangeWithPosObject.getOldBlockData());

                        if (blockDataChangeWithPosObject.getType().equals(BlockDataChangeWithPos.Type.PLACEMENT) || blockDataChangeWithPosObject.getType().equals(BlockDataChangeWithPos.Type.REPLACE))
                            coreProtectAPI.logPlacement(readonlyConfig.coreProtectUserName, blockDataChangeWithPosObject.getLocation(), blockDataChangeWithPosObject.getNewBlockData().getMaterial(), blockDataChangeWithPosObject.getNewBlockData());
                    } catch (IllegalStateException e) {
                        if (e.getMessage().contains("asynchronous")) {
                            blockDataChangeWithPosObject.addFailedTime();
                            if (blockDataChangeWithPosObject.getFailedTime() > (blockDataChangeWithPos.size() > 0 ? 120 : 45)) {
                                e.printStackTrace();
                                continue;
                            }
                            blockDataChangeWithPos.add(blockDataChangeWithPosObject);
                        }

                        e.printStackTrace();
                    }
                }
            };
        }, 20L, 2L);

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
            while (queue.hasNext()) {
                databaseConfig.set(queue.pop().toPositionInfo());
            }

            databaseConfig.save();
            databaseConfig.close();

            suspendedZone.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        getServer().getScheduler().cancelTasks(this);
    }

    public static boolean checkSoftDependPlugins(){
        try {
            Plugin coreProtectPlugin = instance.getServer().getPluginManager().getPlugin("CoreProtect");
            coreProtectAPI = coreProtectPlugin != null ? CoreProtect.getInstance().getAPI() : null;
            if (coreProtectAPI != null) {
                logger.info("CoreProtect plugin is found and hooked!");
            }
        } catch (Exception e) {
            logger.info("CoreProtect plugin is not found, will not support CoreProtect's features!");
        }

        try {
            Plugin residencePlugin = instance.getServer().getPluginManager().getPlugin("Residence");
            residenceAPI = residencePlugin != null ? ResidenceApi.getResidenceManager() : null;
            if (residenceAPI == null) {
                logger.warning("Residence plugin is not found, will not support Residence's features!");
                if (readonlyConfig.residenceStrictCheck) {

                    return false;
                }
            }
            logger.info("Residence plugin is found and hooked!");
        } catch (Exception e) {
            logger.warning("Residence plugin is not found, will not support Residence's features!");
            if (readonlyConfig.residenceStrictCheck) {

                return false;
            }
        }

        try {
            Plugin GriefPreventionPlugin = instance.getServer().getPluginManager().getPlugin("GriefPrevention");
            griefPreventionAPI = GriefPreventionPlugin != null ? GriefPrevention.instance.dataStore : null;

            if (griefPreventionAPI == null) {
                logger.warning("GriefPrevention plugin is not found, will not support GriefPrevention's features!");

                if (readonlyConfig.griefPreventionStrictCheck) {
                    return false;
                }
            }
            logger.info("GriefPrevention plugin is found and hooked!");
        } catch (Exception e) {
            logger.warning("GriefPrevention plugin is not found, will not support GriefPrevention's features!");

            if (readonlyConfig.griefPreventionStrictCheck) {
                return false;
            }
        }

        try {
            Plugin GriefDefenderAPI = instance.getServer().getPluginManager().getPlugin("GriefDefender");
            griefDefenderAPI = GriefDefenderAPI != null ? GriefDefender.getCore() : null;

            if (griefDefenderAPI == null) {
                logger.warning("GriefDefender plugin is not found, will not support GriefDefender's features!");

                if (readonlyConfig.griefDefenderStrictCheck) {
                    return false;
                }
            }
            logger.info("GriefDefender plugin is found and hooked!");
        } catch (Exception e) {
            logger.warning("GriefDefender plugin is not found, will not support GriefDefender's features!");

            if (readonlyConfig.griefDefenderStrictCheck) {
                return false;
            }
        }

        return true;
    }

    private boolean isSuitableForChunkRegeneration() {
        return getServer().getOnlinePlayers().size() < readonlyConfig.maxPlayersCountForRegeneration && ((CraftServer) getServer()).getHandle().getServer().recentTps[0] > readonlyConfig.minTPSCountForRegeneration && enableRevive;
    }
}
