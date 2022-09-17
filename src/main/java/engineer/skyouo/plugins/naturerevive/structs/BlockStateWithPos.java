package engineer.skyouo.plugins.naturerevive.structs;

import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;

public class BlockStateWithPos {
    private final BlockState blockState;
    private final Location location;

    private String tileEntityNbt = null;

    public BlockStateWithPos(BlockState blockState, Location location) {
        this.blockState = blockState;
        this.location = location;
    }

    public BlockStateWithPos(BlockState blockState, Location location, String nbt) {
        this.blockState = blockState;
        this.location = location;
        this.tileEntityNbt = nbt;
    }

    public Location getLocation() {
        return location;
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public String getTileEntityNbt() {
        return tileEntityNbt;
    }
}