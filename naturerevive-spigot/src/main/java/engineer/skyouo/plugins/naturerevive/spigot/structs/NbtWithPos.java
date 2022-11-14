package engineer.skyouo.plugins.naturerevive.spigot.structs;

import org.bukkit.Location;
import org.bukkit.World;

public class NbtWithPos {
    private String nbt;
    private Location location;

    public NbtWithPos(String nbt, Location location) {
        this.nbt = nbt;
        this.location = location;
    }

    public NbtWithPos(String nbt, World world, int x, int y, int z) {
        this.nbt = nbt;
        this.location = new Location(world, x, y, z);
    }

    public String getNbt() {
        return nbt;
    }

    public Location getLocation() {
        return location;
    }
}

