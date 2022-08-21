package engineer.skyouo.plugins.naturerevive.structs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChunkPos {
    public World world;
    public int chunkX;
    public int chunkZ;

    public ChunkPos(String level, int x, int z) {
        world = Bukkit.getWorld(level);
        chunkX = x;
        chunkZ = z;
    }

    public ChunkPos(World level, int x, int z)
    {
        world = level;
        chunkX = x;
        chunkZ = z;
    }

    public Location toLocation() {
        return new Location(world, chunkX << 4, 64, chunkZ << 4);
    }

    public static ChunkPos fromLocation(Location location) {
        return new ChunkPos(location.getWorld(), location.getChunk().getX(), location.getChunk().getZ());
    }

    @Override
    public String toString() {
        return world.getName() + ":" + chunkX + ":" + chunkZ;
    }
}
