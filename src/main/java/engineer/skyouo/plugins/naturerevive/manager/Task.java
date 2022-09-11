package engineer.skyouo.plugins.naturerevive.manager;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import engineer.skyouo.plugins.naturerevive.NatureRevive;
import engineer.skyouo.plugins.naturerevive.constants.OreBlocks;
import engineer.skyouo.plugins.naturerevive.listeners.ObfuscateLootListener;
import engineer.skyouo.plugins.naturerevive.structs.BlockWithPos;
import engineer.skyouo.plugins.naturerevive.structs.NbtWithPos;
import engineer.skyouo.plugins.naturerevive.structs.PositionInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_19_R1.CraftChunkSnapshot;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;
import org.bukkit.plugin.Plugin;

import java.io.*;

import java.nio.file.Files;
import java.util.*;

import static engineer.skyouo.plugins.naturerevive.NatureRevive.*;

public class Task {
    private Plugin plugin;
    private Location location;
    private long ttl;

    public Task(Plugin plugin, Location location, long ttl) {
        this.location = location;
        this.ttl = ttl;
        this.plugin = plugin;
    }

    public Task(PositionInfo positionInfo) {
        this.location = positionInfo.getLocation();
        this.ttl = positionInfo.getTTL();
    }

    public Location getLocation() {
        return location;
    }

    public long getTTL() {
        return ttl;
    }

    public void regenerateChunk() {
        List<BlockWithPos> blocks = new ArrayList<>();
        List<NbtWithPos> nbtWithPos = new ArrayList<>();

        Chunk chunk = location.getChunk();

        if (!chunk.isLoaded()) {
            chunk.load();
        }

        ChunkSnapshot oldChunkSnapshot = null;

        if (residenceApi != null && NatureRevive.readonlyConfig.residenceStrictCheck) {
            List<ClaimedResidence> residences = ((ResidenceManager) residenceApi).getByChunk(chunk);
            if (residences.size() > 0) {
                for (int x = 0; x < 16; x++) {
                    for (int y = chunk.getWorld().getMinHeight(); y <= chunk.getWorld().getMaxHeight(); y++) {
                        for (int z = 0; z < 16; z++) {
                            if (residenceApi.getByLoc(new Location(location.getWorld(), (chunk.getX() << 4) + x, y, (chunk.getZ() << 4) + z)) != null) {
                                Block block = chunk.getBlock(x, y, z);

                                blocks.add(new BlockWithPos(((CraftBlockData) Bukkit.createBlockData(block.getBlockData().getAsString())).getState().getBlock(), block.getLocation()));
                            }
                        }
                    }
                }

                for (BlockState blockState : chunk.getTileEntities()) {
                    if (residenceApi.getByLoc(new Location(location.getWorld(), blockState.getX(), blockState.getY(), blockState.getZ())) != null) {
                        BlockEntity tileEntity = ((CraftWorld) chunk.getWorld()).getHandle().getBlockEntity(new BlockPos(blockState.getX(), blockState.getY(), blockState.getZ()));
                        String nbt = tileEntity.saveWithFullMetadata().getAsString();

                        nbtWithPos.add(new NbtWithPos(nbt, chunk.getWorld(), tileEntity.getBlockPos().getX(), tileEntity.getBlockPos().getY(), tileEntity.getBlockPos().getZ()));
                    }
                }
            }
        }

        if (coreProtectAPI != null) {
            oldChunkSnapshot = chunk.getChunkSnapshot();
        }

        location.getWorld().regenerateChunk(chunk.getX(), chunk.getZ());

        ObfuscateLootListener.randomizeChunkOre(chunk);

        if (blocks.size() > 0) {
            for (BlockWithPos blockWithPos : blocks) {
                BlockPos bp = new BlockPos(blockWithPos.getLocation().getX(), blockWithPos.getLocation().getY(), blockWithPos.getLocation().getZ());
                (((CraftWorld) location.getWorld()).getHandle()).setBlock(bp, blockWithPos.getBlock().defaultBlockState(), 3);
            }
        }

        if (nbtWithPos.size() > 0) {
            for (NbtWithPos tileEntityPos : nbtWithPos) {
                BlockEntity tileEntity = (((CraftWorld) location.getWorld()).getHandle()).getBlockEntity(new BlockPos(tileEntityPos.getLocation().getX(), tileEntityPos.getLocation().getY(), tileEntityPos.getLocation().getZ()));
                try {
                    tileEntity.load(tileEntityPos.getNbt());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        location.getWorld().refreshChunk(chunk.getX(), chunk.getZ());

        if (coreProtectAPI != null && oldChunkSnapshot != null) {
            ChunkSnapshot finalOldChunkSnapshot = oldChunkSnapshot;
            Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                for (int x = 0; x < 16; x++) {
                    for (int y = chunk.getWorld().getMinHeight(); y < chunk.getWorld().getMaxHeight(); y++) {
                        for (int z = 0; z < 16; z++) {
                            Block newBlock = chunk.getBlock(x, y, z);

                            Material oldBlockType = finalOldChunkSnapshot.getBlockType(x, y, z);
                            Material newBlockType = newBlock.getType();

                            if (OreBlocks.contains(oldBlockType)) continue;

                            if (!oldBlockType.equals(newBlockType)) {
                                Location location = new Location(chunk.getWorld(), (chunk.getX() << 4) + x, y, (chunk.getZ() << 4) + z);
                                if (oldBlockType.equals(Material.AIR)) {
                                    coreProtectAPI.logPlacement(readonlyConfig.coreProtectUserName, location, newBlockType, newBlock.getBlockData());
                                } else {
                                    BlockData oldBlockData = finalOldChunkSnapshot.getBlockData(x, y, z);

                                    coreProtectAPI.logRemoval(readonlyConfig.coreProtectUserName, location, oldBlockType, oldBlockData);
                                    if (newBlockType.equals(Material.AIR)) {
                                        coreProtectAPI.logPlacement(readonlyConfig.coreProtectUserName, location, newBlockType, newBlock.getBlockData());
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }

        location.getChunk().unload(true);
    }

    public File takeSnapshot(Chunk chunk) throws IOException {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF(chunk.getX() + "," + chunk.getZ());

        for (int x = 0; x < 16; x++) {
            for (int y = chunk.getWorld().getMinHeight(); y <= chunk.getWorld().getMaxHeight(); y++) {
                for (int z = 0; z < 16; z++) {
                    Block block = chunk.getBlock(x, y, z);
                    String nbt = block.getBlockData().getAsString();

                    out.writeUTF(x + ";" + y + ";" + z + ";" + block.getType() + ";" + nbt);
                }
            }
        }

        for (BlockState blockState : chunk.getTileEntities()) {
            BlockEntity tileEntity = ((CraftWorld) chunk.getWorld()).getHandle().getBlockEntity(new BlockPos(blockState.getX(), blockState.getY(), blockState.getZ()));
            String nbt = tileEntity.saveWithFullMetadata().getAsString();
            out.writeUTF(blockState.getX() + ";" + blockState.getY() + ";" + blockState.getZ() + ";" + nbt);
        }

        new File("plugins/NatureRevive/snapshots").mkdirs();

        FileOutputStream outputStream = new FileOutputStream("plugins/NatureRevive/snapshots/" + chunk.hashCode() + ".snapshot");

        outputStream.write(out.toByteArray());
        outputStream.close();

        return new File("plugins/NatureRevive/snapshots/" + chunk.hashCode() + ".snapshot");
    }

    public Chunk revertSnapshot(World world, File file) throws IOException {
        ByteArrayDataInput inputStream = ByteStreams.newDataInput(Files.readAllBytes(file.toPath()));
        String[] coordsInString = inputStream.readUTF().split(",");

        Chunk chunk = world.getChunkAt(Integer.parseInt(coordsInString[0]), Integer.parseInt(coordsInString[1]));

        if (!chunk.isLoaded()) {
            chunk.load();
        }

        List<BlockWithPos> blockList = new ArrayList<>();
        List<NbtWithPos> nbtList = new ArrayList<>();

        while (true) {
            String[] argument = null;
            try {
                String data = inputStream.readUTF();
                if (data.equals("\n")) continue;

                argument = data.split(";");

                if (argument.length == 5) {
                    net.minecraft.world.level.block.Block block = ((CraftBlockData) Bukkit.createBlockData(argument[4])).getState().getBlock();
                    blockList.add(new BlockWithPos(block, new Location(world, Integer.parseInt(argument[0]), Integer.parseInt(argument[1]), Integer.parseInt(argument[2]))));
                } else {
                    nbtList.add(new NbtWithPos(argument[3], new Location(world, Integer.parseInt(argument[0]), Integer.parseInt(argument[1]), Integer.parseInt(argument[2]))));
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }

        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();

        for (BlockWithPos block : blockList) {
            BlockPos bp = new BlockPos((chunk.getX() << 4) + block.getLocation().getX(), block.getLocation().getY(), (chunk.getZ() << 4) + block.getLocation().getZ());
            nmsWorld.setBlock(bp, block.getBlock().defaultBlockState(), 3);
        }

        for (NbtWithPos nbtWithPos : nbtList) {
            BlockEntity tileEntity = nmsWorld.getBlockEntity(new BlockPos(nbtWithPos.getLocation().getX(), nbtWithPos.getLocation().getY(), nbtWithPos.getLocation().getZ()));
            try {
                tileEntity.load(nbtWithPos.getNbt());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return chunk;
    }

    @Override
    public String toString() {
        return location.toString() + ":" + ttl;
    }
}
