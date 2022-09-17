package engineer.skyouo.plugins.naturerevive;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.api.ResidenceInterface;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import engineer.skyouo.plugins.naturerevive.commands.*;
import engineer.skyouo.plugins.naturerevive.config.DatabaseConfig;
import engineer.skyouo.plugins.naturerevive.config.ReadonlyConfig;
import engineer.skyouo.plugins.naturerevive.listeners.ChunkRelatedEventListener;
import engineer.skyouo.plugins.naturerevive.listeners.ObfuscateLootListener;
import engineer.skyouo.plugins.naturerevive.manager.Queue;
import engineer.skyouo.plugins.naturerevive.manager.Task;
import engineer.skyouo.plugins.naturerevive.structs.BlockStateWithPos;
import engineer.skyouo.plugins.naturerevive.structs.PositionInfo;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.TagParser;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;
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

    public static final Queue<Task> queue = new Queue<>();
    public static final Queue<BlockStateWithPos> blockStateWithPosQueue = new Queue<>();

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
                for (int i = 0; i < readonlyConfig.taskPerProcess && queue.hasNext(); i++) {
                    Task task = queue.pop();

                    if (PositionInfo.isResidence(task.getLocation()) && !readonlyConfig.residenceStrictCheck) return;

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
