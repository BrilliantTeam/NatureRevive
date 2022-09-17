package engineer.skyouo.plugins.naturerevive.structs;

import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;

public class BlockStateWithPos {
    private final BlockState blockState;
    private final Location location;

    public BlockStateWithPos(BlockState blockState, Location location) {
        this.blockState = blockState;
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public BlockState getBlockState() {
        return blockState;
    }
}