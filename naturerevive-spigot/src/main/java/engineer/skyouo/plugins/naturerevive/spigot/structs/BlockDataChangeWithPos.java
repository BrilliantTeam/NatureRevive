package engineer.skyouo.plugins.naturerevive.spigot.structs;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

public class BlockDataChangeWithPos {
    private Location location;
    private BlockData oldBlockData;
    private BlockData newBlockData;
    private int blockFailedCheckTime = 0;
    private Type type;

    public BlockDataChangeWithPos(Location location, BlockData oldBlockData, BlockData newBlockData, Type type) {
        this.location = location;
        this.oldBlockData = oldBlockData;
        this.newBlockData = newBlockData;
        this.type = type;
    }

    public Location getLocation() {
        return location;
    }

    public BlockData getNewBlockData() {
        return newBlockData;
    }

    public BlockData getOldBlockData() {
        return oldBlockData;
    }

    public Type getType() {
        return type;
    }

    public int getFailedTime() {
        return blockFailedCheckTime;
    }

    public void addFailedTime() {
        blockFailedCheckTime++;
    }

    public enum Type {
        REMOVAL,
        PLACEMENT,
        REPLACE
    }
}
