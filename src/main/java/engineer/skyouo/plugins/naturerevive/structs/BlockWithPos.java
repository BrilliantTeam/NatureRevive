package engineer.skyouo.plugins.naturerevive.structs;

import net.minecraft.world.level.block.Block;

import org.bukkit.Location;

public class BlockWithPos {
    private final Block block;
    private final Location location;

    public BlockWithPos(Block block, Location location) {
        this.block = block;
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public Block getBlock() {
        return block;
    }
}
