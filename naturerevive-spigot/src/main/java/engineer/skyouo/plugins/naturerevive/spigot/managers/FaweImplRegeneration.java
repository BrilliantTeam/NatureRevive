package engineer.skyouo.plugins.naturerevive.spigot.managers;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.RegenOptions;
import engineer.skyouo.plugins.naturerevive.spigot.NatureReviveBukkitLogger;
import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.util.ScheduleUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.jetbrains.annotations.Nullable;

import static engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin.nmsWrapper;

public class FaweImplRegeneration {
    public static void regenerate(Chunk chunk, boolean regenBiomes, @Nullable Runnable afterTask) {
        long o = System.currentTimeMillis();
        BukkitWorld bukkitWorld = new BukkitWorld(chunk.getWorld());
        try (EditSession session = WorldEdit.getInstance().newEditSession(bukkitWorld)) {
            Mask mask = session.getMask();

            BlockVector3 one = BlockVector3.at(chunk.getX() << 4, nmsWrapper.getWorldMinHeight(chunk.getWorld()),chunk.getZ() << 4);
            BlockVector3 two = BlockVector3.at((chunk.getX() << 4) +15,256,(chunk.getZ() << 4)+15);
            if (NatureRevivePlugin.readonlyConfig.debug)
                NatureReviveBukkitLogger.info(String.format("Regenerating From (%d, %d, %d) to (%d, %d, %d) in %s",
                    chunk.getX() << 4, nmsWrapper.getWorldMinHeight(chunk.getWorld()),chunk.getZ() << 4,
                    (chunk.getX() << 4) +15,256,(chunk.getZ() << 4)+15, bukkitWorld.getName()));
            Region region = new CuboidRegion(bukkitWorld, one, two);

            boolean success;
            try {
                session.setMask(null);
                session.setSourceMask(null);
                RegenOptions options = RegenOptions.builder()
                        .seed(chunk.getWorld().getSeed())
                        .regenBiomes(regenBiomes)
                        .build();
                success = bukkitWorld.regenerate(region, session, options);
            } finally {
                session.setMask(mask);
                session.setSourceMask(mask);
            }

            Operations.complete(session.commit());
            if (NatureRevivePlugin.readonlyConfig.debug)
                NatureReviveBukkitLogger.info("Regen time cost " + (System.currentTimeMillis() - o) + " ms");
        }
        if (afterTask != null)
            ScheduleUtil.REGION.runTask(NatureRevivePlugin.instance, chunk, afterTask);
    }
}
