package engineer.skyouo.plugins.naturerevive.manager;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import engineer.skyouo.plugins.naturerevive.structs.BlockWithPos;
import engineer.skyouo.plugins.naturerevive.structs.NbtWithPos;
import engineer.skyouo.plugins.naturerevive.structs.PositionInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;
import org.bukkit.plugin.Plugin;

import java.io.*;

import java.nio.file.Files;
import java.util.*;

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
        if (!location.getChunk().isLoaded()) {
            location.getChunk().load();
        }

        location.getWorld().regenerateChunk(location.getChunk().getX(), location.getChunk().getZ());
        location.getWorld().refreshChunk(location.getChunk().getX(), location.getChunk().getZ());

        location.getChunk().unload(true);
    }

    public File takeSnapshot(Chunk chunk) throws IOException {
        final int minY = chunk.getWorld().getMinHeight();
        final int maxY = chunk.getWorld().getMaxHeight();


        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF(chunk.getX() + "," + chunk.getZ());

        for (int x = 0; x <= 15; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = 0; z <= 15; ++z) {
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
            String[] argument = new String[0];
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
                System.out.println(Arrays.toString(argument));
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
}
