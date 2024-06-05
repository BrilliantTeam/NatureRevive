package engineer.skyouo.plugins.naturerevive.spigot.managers;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.RegenOptions;
import engineer.skyouo.plugins.naturerevive.spigot.NatureReviveBukkitLogger;
import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

public class FaweImplRegeneration {
    public static void regenerate(Chunk chunk, boolean regenBiomes, Runnable afterTask) {
        long o = System.currentTimeMillis();
        BukkitWorld bukkitWorld = new BukkitWorld(chunk.getWorld());
        EditSession session = WorldEdit.getInstance().newEditSession(bukkitWorld);
        Mask mask = session.getMask();

        BlockVector3 one = BlockVector3.at(chunk.getX() << 4,-64,chunk.getZ() << 4);
        BlockVector3 two = BlockVector3.at((chunk.getX() << 4) +15,256,(chunk.getZ() << 4)+15);
        Region region = new CuboidRegion(bukkitWorld, one, two);

        boolean success;
        try {
            session.setMask(null);
            //FAWE start
            session.setSourceMask(null);
            //FAWE end
            RegenOptions options = RegenOptions.builder()
                    .seed(chunk.getWorld().getSeed())
                    .regenBiomes(regenBiomes)
                    .build();
            success = bukkitWorld.regenerate(region, session, options);
        } finally {
            session.setMask(mask);
            //FAWE start
            session.setSourceMask(mask);
            //FAWE end
        }
        if (success) {
            bukkitWorld.refreshChunk(chunk.getX(), chunk.getZ());
        }
        session.close();
        NatureReviveBukkitLogger.info("Regen time cost " + (System.currentTimeMillis() - o) + " ms");

        Bukkit.getScheduler().runTask(NatureRevivePlugin.instance, afterTask);
    }
}
