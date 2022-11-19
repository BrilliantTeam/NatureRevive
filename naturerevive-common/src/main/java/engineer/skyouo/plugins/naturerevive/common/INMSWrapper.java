package engineer.skyouo.plugins.naturerevive.common;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.BooleanSupplier;

public interface INMSWrapper {
    List<String> getCompatibleNMSVersion();

    boolean regenerateChunk(World world, int x, int z);

    boolean regenerateChunk(World world, int x, int z, IPosCalculate filter);

    String getNbtAsString(World world, BlockState blockState);

    void setBlockNMS(World world, int x, int y, int z, BlockData data);

    void loadTileEntity(World world, int x, int y, int z, String nbt);

    void createTileEntity(World world, int x, int y, int z, BlockData data, String nbt);

    double[] getRecentTps();

    double getLuckForPlayer(Player player);

    BlockState convertBlockDataToBlockState(BlockData blockData);

    int getWorldMinHeight(World world);

    Material[] getOreBlocks();
}
