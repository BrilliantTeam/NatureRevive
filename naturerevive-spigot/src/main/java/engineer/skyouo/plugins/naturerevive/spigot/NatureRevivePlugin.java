package engineer.skyouo.plugins.naturerevive.spigot;

import engineer.skyouo.plugins.naturerevive.common.INMSWrapper;
import engineer.skyouo.plugins.naturerevive.common.structs.Queue;
import engineer.skyouo.plugins.naturerevive.spigot.api.IAPIMain;
import engineer.skyouo.plugins.naturerevive.spigot.api.IIntegrationManager;
import engineer.skyouo.plugins.naturerevive.spigot.commands.*;
import engineer.skyouo.plugins.naturerevive.spigot.config.DatabaseConfig;
import engineer.skyouo.plugins.naturerevive.spigot.config.ReadonlyConfig;
import engineer.skyouo.plugins.naturerevive.spigot.config.adapters.SQLDatabaseAdapter;
import engineer.skyouo.plugins.naturerevive.spigot.constants.OreBlocksCompat;
import engineer.skyouo.plugins.naturerevive.spigot.integration.IntegrationManager;
import engineer.skyouo.plugins.naturerevive.spigot.integration.IntegrationUtil;
import engineer.skyouo.plugins.naturerevive.spigot.integration.land.ILandPluginIntegration;
import engineer.skyouo.plugins.naturerevive.spigot.integration.logging.ILoggingIntegration;
import engineer.skyouo.plugins.naturerevive.spigot.listeners.ChunkRelatedEventListener;
import engineer.skyouo.plugins.naturerevive.spigot.listeners.ObfuscateLootListener;
import engineer.skyouo.plugins.naturerevive.spigot.managers.features.ElytraRegeneration;
import engineer.skyouo.plugins.naturerevive.spigot.stats.Metrics;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BlockDataChangeWithPos;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BlockStateWithPos;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BukkitPositionInfo;
import engineer.skyouo.plugins.naturerevive.spigot.structs.SQLCommand;
import engineer.skyouo.plugins.naturerevive.spigot.util.ScheduleUtil;
import engineer.skyouo.plugins.naturerevive.spigot.util.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

public class NatureRevivePlugin extends JavaPlugin implements IAPIMain {
    public static boolean enableRevive = true;

    static {
        ConfigurationSerialization.registerClass(BukkitPositionInfo.class, "PositionInfo");
    }

    public static NatureRevivePlugin instance;
    public static IntegrationManager integrationManager;
    public static INMSWrapper nmsWrapper;
    public static ReadonlyConfig readonlyConfig;
    public static DatabaseConfig databaseConfig;

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

        try {
            databaseConfig = readonlyConfig.determineDatabase();
        } catch (Exception ex) {
            NatureReviveBukkitLogger.error("&c資料庫初始化失敗!");
            NatureReviveBukkitLogger.warning("&c倘若您使用 MySQL，請確認好您以創建對應的 database。");

            getPluginLoader().disablePlugin(this);
            return;
        }

        nmsWrapper = Util.getNMSWrapper();

        if (nmsWrapper == null) {
            NatureReviveBukkitLogger.error("&c無法加載 NMS 兼容項目!");
            NatureReviveBukkitLogger.warning("&c您的版本有可能不支援 NatureRevive: " + getServer().getVersion());

            getPluginLoader().disablePlugin(this);
            return;
        }

        for (Material ore : nmsWrapper.getOreBlocks()) {
            OreBlocksCompat.addMaterial(ore);
        }

        suspendedZone = new SuspendedZone();
        integrationManager = new IntegrationManager();

        if (!checkSoftDependPlugins()) {
            NatureReviveBukkitLogger.error("&c由於您於設置中開啟了部分功能, 且 NatureRevive 無法載入對應的依賴插件, 因此 NatureRevive 將會停止載入.");
            getPluginLoader().disablePlugin(this);

            return;
        }

        IntegrationUtil.reloadCache();

        if (IntegrationUtil.getRegenEngine() == null) {
            NatureReviveBukkitLogger.error("找不到可用的重生引擎，請您確定是否正確設置 regenerate-engine 選項!");

            getPluginLoader().disablePlugin(this);

            return;
        }

        if (!Util.isPaper()) {
            NatureReviveBukkitLogger.error("您運行的軟體不包含 NatureRevive 運行所需的修補!");
            NatureReviveBukkitLogger.error("建議您切換至 Paper，Paper 是 Spigot 的分支之一，包含眾多優化修補，也不須透過 BuildTools 來獲取軟體構建。");

            getPluginLoader().disablePlugin(this);

            return;
        }

        registerCommand("forceregenall", new ForceRegenAllCommand(this));
        registerCommand("regenthischunk", new RegenThisChunkCommand());
        registerCommand("testrandomizeore", new TestRandomizeOreCommand());
        registerCommand("reloadreviveconfig", new ReloadCommand());
        registerCommand("togglerevive", new ToggleChunkRegenerationCommand());
        registerCommand("navdebug", new DebugCommand());
        registerCommand("navmigrate", new MigrateCommand());

        getServer().getPluginManager().registerEvents(new ChunkRelatedEventListener(), this);
        getServer().getPluginManager().registerEvents(new ObfuscateLootListener(), this);

        // todo: move this to another class

        ScheduleUtil.GLOBAL.runTaskTimer(this, () -> {
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
                    for (int x = -1; x < readonlyConfig.chunkRegenerateRadiusOnAverageApplied; x++)
                        for (int z = -1; z < readonlyConfig.chunkRegenerateRadiusOnAverageApplied; z++) {
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

        ScheduleUtil.GLOBAL.runTaskTimer(this, () -> {
            if (queue.size() > 0 && isSuitableForChunkRegeneration()) {
                for (int i = 0; i < readonlyConfig.taskPerProcess && queue.hasNext(); i++) {
                    BukkitPositionInfo task = queue.pop();

                    if (readonlyConfig.ignoredWorld.contains(task.getLocation().getWorld().getName()))
                        continue;

                    List<ILandPluginIntegration> integrations = IntegrationUtil.getLandIntegrations();

                    if (!integrations.isEmpty() &&
                            integrations.stream().anyMatch(integration -> integration.isInLand(task.getLocation()) && !integration.isStrictMode()))
                        continue;

                    ScheduleUtil.REGION.runTask(this, task.getLocation(), () -> {
                        task.regenerateChunk();

                        if (readonlyConfig.debug)
                            NatureReviveBukkitLogger.debug("&7" + task + " was regenerated.");
                    });
                }
            } else {
                // 未達成 無法生成區塊 清除序列
                while (queue.hasNext()){
                    queue.pop();
                }
            }
        }, 20L, readonlyConfig.queuePerNTick);

        ScheduleUtil.GLOBAL.runTaskTimer(this, () -> {
            for (int i = 0; i < readonlyConfig.blockPutPerTick && blockStateWithPosQueue.hasNext(); i++) {
                BlockStateWithPos blockStateWithPos = blockStateWithPosQueue.pop();

                Location location = blockStateWithPos.getLocation();

                ScheduleUtil.REGION.runTask(this, location, () -> {
                    nmsWrapper.setBlockNMS(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), blockStateWithPos.getBlockState().getBlockData());

                    if (blockStateWithPos.getTileEntityNbt() != null) {
                        try {
                            nmsWrapper.loadTileEntity(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), blockStateWithPos.getTileEntityNbt());
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }, 20L, readonlyConfig.blockPutActionPerNTick);

        ScheduleUtil.GLOBAL.runTaskTimer(this, () -> {
            if (blockDataChangeWithPos.hasNext()) {
                for (int i = 0; i < 200 && blockDataChangeWithPos.hasNext(); i++) {
                    BlockDataChangeWithPos blockDataChangeWithPosObject = blockDataChangeWithPos.pop();

                    ScheduleUtil.REGION.runTask(this, blockDataChangeWithPosObject.getLocation(), () -> {
                        synchronized (blockDataChangeWithPosObject) {
                            try {
                                for (ILoggingIntegration integration : IntegrationUtil.getLoggingIntegrations()) {
                                    if (!integration.isEnabled()) continue;

                                    integration.log(blockDataChangeWithPosObject);
                                }
                            } catch (IllegalStateException e) {
                                if (e.getMessage().contains("asynchronous")) {
                                    blockDataChangeWithPosObject.addFailedTime();
                                    if (blockDataChangeWithPosObject.getFailedTime() > (blockDataChangeWithPos.size() > 1 ? 120 : 45)) {
                                        blockDataChangeWithPos.add(blockDataChangeWithPosObject);
                                    }
                                }

                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }, 20L, 2L);

        ScheduleUtil.GLOBAL.runTaskTimerAsynchronously(this, () -> {
            for (int i = 0; i < readonlyConfig.blockProcessingAmountPerProcessing && blockQueue.hasNext(); i++) {
                ChunkRelatedEventListener.flagChunk(blockQueue.pop());
            }
        }, 20L, readonlyConfig.blockProcessingTick);

        ScheduleUtil.GLOBAL.runTaskTimerAsynchronously(this, () -> {
            if (databaseConfig instanceof SQLDatabaseAdapter adapter) {
                List<SQLCommand> sqlCommands = new ArrayList<>();

                int i = 0;

                while (sqlCommandQueue.hasNext() && i < readonlyConfig.sqlProcessingCount) {
                    sqlCommands.add(sqlCommandQueue.pop());
                    i++;
                }

                adapter.massExecute(sqlCommands);
            } else {
                ScheduleUtil.GLOBAL.runTask(this, () -> {
                    try {
                        databaseConfig.save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }, readonlyConfig.dataSaveTime, readonlyConfig.dataSaveTime);

        ScheduleUtil.GLOBAL.runTaskTimer(this, () -> {
            if (ElytraRegeneration.checkResetLimitTime()) {
                NatureReviveBukkitLogger.info("The elytra regeneration limit has been reset.");
            }
        }, 20L, 600L);

        new Metrics(this, 16446)
                .addCustomChart(new Metrics.SimplePie("regeneration_engine", new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return readonlyConfig.regenerationEngine;
                    }
                }));
    }

    public static boolean checkSoftDependPlugins() {
        if (!readonlyConfig.regenerationEngine.equalsIgnoreCase("fawe") &&
                !readonlyConfig.regenerationEngine.equalsIgnoreCase("bukkit")) {
            NatureReviveBukkitLogger.warning("請將 regeneration-strategy 修正為 bukkit 或 fawe.");
            return false;
        }

        return integrationManager.init(instance);
    }

    @Override
    public void onDisable() {

    }

    private boolean isSuitableForChunkRegeneration() {
        // 新增時間閥
        return getServer().getOnlinePlayers().size() < readonlyConfig.maxPlayersCountForRegeneration && (Util.isFolia() ? Bukkit.getTPS()[0] : nmsWrapper.getRecentTps()[0]) > readonlyConfig.minTPSCountForRegeneration && enableRevive && readonlyConfig.isCurrentTimeAllowForRSC();
    }

    private boolean registerCommand(String commandName, CommandExecutor executor) {
        try {
            PluginCommand command = getCommand(commandName);

            if (command == null)
                return false;

            command.setExecutor(executor);

            if (executor instanceof TabExecutor tabExecutor) {
                command.setTabCompleter(tabExecutor);
            }

            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public @NotNull IIntegrationManager getIntegrationManager() {
        return integrationManager;
    }
}
