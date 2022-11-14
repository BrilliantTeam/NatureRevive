package engineer.skyouo.plugins.naturerevive.spigot;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.api.ResidenceInterface;
import com.griefdefender.api.Core;
import com.griefdefender.api.GriefDefender;
import engineer.skyouo.plugins.naturerevive.common.INMSWrapper;
import engineer.skyouo.plugins.naturerevive.common.structs.Queue;
import engineer.skyouo.plugins.naturerevive.spigot.config.DatabaseConfig;
import engineer.skyouo.plugins.naturerevive.spigot.config.ReadonlyConfig;
import engineer.skyouo.plugins.naturerevive.spigot.config.adapters.SQLDatabaseAdapter;
import engineer.skyouo.plugins.naturerevive.spigot.constants.OreBlocksCompat;
import engineer.skyouo.plugins.naturerevive.spigot.listeners.ChunkRelatedEventListener;
import engineer.skyouo.plugins.naturerevive.spigot.listeners.ObfuscateLootListener;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BlockDataChangeWithPos;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BlockStateWithPos;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BukkitPositionInfo;
import engineer.skyouo.plugins.naturerevive.spigot.structs.SQLCommand;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NatureRevivePlugin extends JavaPlugin {
    public static boolean enableRevive = true;

    static {
        ConfigurationSerialization.registerClass(BukkitPositionInfo.class, "PositionInfo");
    }

    public static NatureRevivePlugin instance;
    public static INMSWrapper nmsWrapper;
    public static ReadonlyConfig readonlyConfig;
    public static DatabaseConfig databaseConfig;

    public static ResidenceInterface residenceAPI;
    public static CoreProtectAPI coreProtectAPI;
    public static DataStore griefPreventionAPI;
    public static Core griefDefenderAPI;

    public static SuspendedZone suspendedZone;

    public static Queue<BukkitPositionInfo> queue = new Queue<>();
    public static Queue<Location> blockQueue = new Queue<>();
    public static final Queue<BlockStateWithPos> blockStateWithPosQueue = new Queue<>();
    public static final Queue<BlockDataChangeWithPos> blockDataChangeWithPos = new Queue<>();
    public static final Queue<SQLCommand> sqlCommandQueue = new Queue<>();

    @Override
    public void onEnable() {
        instance = this;

        try {
            readonlyConfig = new ReadonlyConfig();
        } catch (IOException e) {
            e.printStackTrace();
            NatureReviveBukkitLogger.error("無法載入配置檔案!");
        }

        nmsWrapper = Util.getNMSWrapper();

        for (Material ore : nmsWrapper.getOreBlocks()) {
            OreBlocksCompat.addMaterial(ore);
        }

        suspendedZone = new SuspendedZone();

        if (nmsWrapper == null) {
            NatureReviveBukkitLogger.error("&a無法加載 NMS 兼容項目!");
            NatureReviveBukkitLogger.warning("&c您的版本有可能不支援 NatureRevive: " + getServer().getVersion());

            getPluginLoader().disablePlugin(this);
            return;
        }

        if (!checkSoftDependPlugins()) {
            NatureReviveBukkitLogger.error("&c由於您於設置中開啟了部分功能, 且 NatureRevive 無法載入對應的依賴插件, 因此 NatureRevive 將會停止載入.");
            getPluginLoader().disablePlugin(this);

            return;
        }

        getServer().getPluginManager().registerEvents(new ChunkRelatedEventListener(), this);
        getServer().getPluginManager().registerEvents(new ObfuscateLootListener(), this);

        // todo

        getServer().getScheduler().runTaskTimer(this, () -> {
            if (!readonlyConfig.regenerationStrategy.equalsIgnoreCase("passive") && !readonlyConfig.regenerationStrategy.equalsIgnoreCase("average")) {
                List<BukkitPositionInfo> positionInfos = databaseConfig.values();
                for (BukkitPositionInfo positionInfo : positionInfos) {
                    if (positionInfo.isOverTTL()) {
                        queue.add(positionInfo);
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

                            BukkitPositionInfo positionInfo = databaseConfig.get(new BukkitPositionInfo(player.getWorld().getName(), player.getLocation().getChunk().getX() + x, player.getLocation().getChunk().getZ() + z, 0).getLocation());

                            if (positionInfo == null)
                                continue;

                            if (positionInfo.isOverTTL()) {
                                queue.add(positionInfo);
                                databaseConfig.unset(positionInfo);
                            }
                        }
                }
            }
        }, 20L, readonlyConfig.checkChunkTTLTick);

        getServer().getScheduler().runTaskTimer(this, () -> {
            if (queue.size() > 0 && isSuitableForChunkRegeneration()) {
                for (int i = 0; i < readonlyConfig.taskPerProcess && queue.hasNext(); i++) {
                    BukkitPositionInfo task = queue.pop();

                    if (readonlyConfig.ignoredWorld.contains(task.getLocation().getWorld().getName()))
                        continue;

                    if (BukkitPositionInfo.isResidence(task.getLocation()) && !readonlyConfig.residenceStrictCheck)
                        continue;

                    if (BukkitPositionInfo.isGriefPrevention(task.getLocation()) && !readonlyConfig.griefPreventionStrictCheck)
                        continue;

                    if (BukkitPositionInfo.isGriefDefender(task.getLocation()) && !readonlyConfig.griefDefenderStrictCheck) {
                        continue;
                    }

                    task.regenerateChunk();

                    if (readonlyConfig.debug)
                        NatureReviveBukkitLogger.debug("&7" + task.toString() + " was regenerated.");
                }
            }
        }, 20L, readonlyConfig.queuePerNTick);

        getServer().getScheduler().runTaskTimer(this, () -> {
            for (int i = 0; i < readonlyConfig.blockPutPerTick && blockStateWithPosQueue.hasNext(); i++) {
                BlockStateWithPos blockStateWithPos = blockStateWithPosQueue.pop();

                Location location = blockStateWithPos.getLocation();

                nmsWrapper.setBlockNMS(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), blockStateWithPos.getBlockState().getBlockData());

                if (blockStateWithPos.getTileEntityNbt() != null) {
                    try {
                        nmsWrapper.loadTileEntity(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), blockStateWithPos.getTileEntityNbt());
                    } catch (RuntimeException e) {
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
                            if (blockDataChangeWithPosObject.getFailedTime() > (blockDataChangeWithPos.size() > 1 ? 120 : 45)) {
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

        if (databaseConfig instanceof SQLDatabaseAdapter) {
            getServer().getScheduler().runTaskTimer(this, () -> {
                List<SQLCommand> sqlCommands = new ArrayList<>();

                while (sqlCommandQueue.hasNext()) {
                    sqlCommands.add(sqlCommandQueue.pop());
                }

                ((SQLDatabaseAdapter) databaseConfig).massExecute(sqlCommands);
            }, 2L, readonlyConfig.sqlProcessingTick);
        }

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (int i = 0; i < readonlyConfig.blockProcessingAmountPerProcessing && blockQueue.hasNext(); i++) {
                ChunkRelatedEventListener.flagChunk(blockQueue.pop());
            }
        }, 20L, readonlyConfig.blockProcessingTick);

        getServer().getScheduler().runTaskTimer(this, () -> {
            try {
                databaseConfig.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, readonlyConfig.dataSaveTime, readonlyConfig.dataSaveTime);
    }

    public static boolean checkSoftDependPlugins() {
        try {
            Plugin coreProtectPlugin = instance.getServer().getPluginManager().getPlugin("CoreProtect");
            coreProtectAPI = coreProtectPlugin != null ? CoreProtect.getInstance().getAPI() : null;
            if (coreProtectAPI != null) {
                NatureReviveBukkitLogger.info("&a已發現 CoreProtect, 將會啟用 CoreProtect 支援!");
            }
        } catch (Exception e) {
            NatureReviveBukkitLogger.warning("&eCoreProtect 插件並未載入, 將禁用 CoreProtect 支援!");
        }

        try {
            Plugin residencePlugin = instance.getServer().getPluginManager().getPlugin("Residence");
            residenceAPI = residencePlugin != null ? ResidenceApi.getResidenceManager() : null;
            if (residenceAPI == null) {
                NatureReviveBukkitLogger.warning("&e未成功載入 Residence 領地插件!");
                if (readonlyConfig.residenceStrictCheck) {
                    return false;
                }
            } else {
                NatureReviveBukkitLogger.info("&a發現 Residence 領地支援, 將載入 Residence 支援!");
            }
        } catch (Exception e) {
            NatureReviveBukkitLogger.warning("&e未發現 Residence 領地插件!");
            if (readonlyConfig.residenceStrictCheck) {

                return false;
            }
        }

        try {
            Plugin GriefPreventionPlugin = instance.getServer().getPluginManager().getPlugin("GriefPrevention");
            griefPreventionAPI = GriefPreventionPlugin != null ? GriefPrevention.instance.dataStore : null;

            if (griefPreventionAPI == null) {
                NatureReviveBukkitLogger.warning("&e未成功載入 GriefPrevention 領地插件!");

                if (readonlyConfig.griefPreventionStrictCheck) {
                    return false;
                }
            } else {
                NatureReviveBukkitLogger.info("&a發現 GriefPrevention 領地支援, 將載入 GriefPrevention 支援!");
            }
        } catch (Exception e) {
            NatureReviveBukkitLogger.warning("&e未發現 GriefPrevention 領地插件!");

            if (readonlyConfig.griefPreventionStrictCheck) {
                return false;
            }
        }

        try {
            Plugin GriefDefenderAPI = instance.getServer().getPluginManager().getPlugin("GriefDefender");
            griefDefenderAPI = GriefDefenderAPI != null ? GriefDefender.getCore() : null;

            if (griefDefenderAPI == null) {
                NatureReviveBukkitLogger.warning("&e未成功載入 GriefDefender 領地插件!");

                if (readonlyConfig.griefDefenderStrictCheck) {
                    return false;
                }
            } else {
                NatureReviveBukkitLogger.info("&a發現 GriefDefender 領地支援, 將載入 GriefDefender 支援!");
            }
        } catch (Exception e) {
            NatureReviveBukkitLogger.warning("&e未發現 GriefDefender 領地插件!");

            if (readonlyConfig.griefDefenderStrictCheck) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onDisable() {

    }

    private boolean isSuitableForChunkRegeneration() {
        return getServer().getOnlinePlayers().size() < readonlyConfig.maxPlayersCountForRegeneration && nmsWrapper.getRecentTps()[0] > readonlyConfig.minTPSCountForRegeneration && enableRevive;
    }
}
