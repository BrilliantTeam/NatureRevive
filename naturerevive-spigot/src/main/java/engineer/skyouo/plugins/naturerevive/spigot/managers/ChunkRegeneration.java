package engineer.skyouo.plugins.naturerevive.spigot.managers;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import engineer.skyouo.plugins.naturerevive.common.IPosCalculate;
import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.constants.OreBlocksCompat;
import engineer.skyouo.plugins.naturerevive.spigot.listeners.ObfuscateLootListener;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BlockDataChangeWithPos;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BlockStateWithPos;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BukkitPositionInfo;
import engineer.skyouo.plugins.naturerevive.spigot.structs.NbtWithPos;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin.*;

public class ChunkRegeneration {
    private static final UUID emptyUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static void regenerateChunk(BukkitPositionInfo bukkitPositionInfo) {
        Location location = bukkitPositionInfo.getLocation();

        List<NbtWithPos> nbtWithPos = new ArrayList<>();

        Chunk chunk = location.getChunk();

        if (!chunk.isLoaded()) {
            chunk.load();
        }

        ChunkSnapshot oldChunkSnapshot = chunk.getChunkSnapshot();

        // todo: make this asynchronous.
        if (residenceAPI != null && NatureRevivePlugin.readonlyConfig.residenceStrictCheck) {
            List<ClaimedResidence> residences = ((ResidenceManager) residenceAPI).getByChunk(chunk);
            if (residences.size() > 0) {

                for (BlockState blockState : chunk.getTileEntities()) {
                    if (residenceAPI.getByLoc(new Location(location.getWorld(), blockState.getX(), blockState.getY(), blockState.getZ())) != null) {
                        String nbt = nmsWrapper.getNbtAsString(chunk.getWorld(), blockState);

                        nbtWithPos.add(new NbtWithPos(nbt, chunk.getWorld(), blockState.getX(), blockState.getY(), blockState.getZ()));
                    }
                }
            }
        }

        if (griefPreventionAPI != null && NatureRevivePlugin.readonlyConfig.griefPreventionStrictCheck) {
            Collection<me.ryanhamshire.GriefPrevention.Claim> griefPrevention = griefPreventionAPI.getClaims(chunk.getX(), chunk.getZ());
            if (griefPrevention.size() > 0) {
                for (BlockState blockState : chunk.getTileEntities()) {
                    if (griefPreventionAPI.getClaimAt(new Location(location.getWorld(), blockState.getX(), blockState.getY(), blockState.getZ()), true, null) != null){
                       String nbt = nmsWrapper.getNbtAsString(chunk.getWorld(), blockState);

                       nbtWithPos.add(new NbtWithPos(nbt, chunk.getWorld(), blockState.getX(), blockState.getY(), blockState.getZ()));
                    }
                }
            }
        }

        if (griefDefenderAPI != null && readonlyConfig.griefDefenderStrictCheck) {
            for (BlockState blockState : chunk.getTileEntities()){
                UUID uuid = griefDefenderAPI.getClaimAt(new Location(location.getWorld(), blockState.getX(), blockState.getY(), blockState.getZ())).getOwnerUniqueId();
                if (!uuid.equals(emptyUUID)) {
                    String nbt = nmsWrapper.getNbtAsString(chunk.getWorld(), blockState);

                    nbtWithPos.add(new NbtWithPos(nbt, chunk.getWorld(), blockState.getX(), blockState.getY(), blockState.getZ()));
                }
            }
        }

        nmsWrapper.regenerateChunk(chunk.getWorld(), chunk.getX(), chunk.getZ(), (x, y, z) -> {
            if (residenceAPI != null && NatureRevivePlugin.readonlyConfig.residenceStrictCheck) {
                List<ClaimedResidence> residences = ((ResidenceManager) residenceAPI).getByChunk(chunk);
                if (residences.size() > 0) {
                    if (residenceAPI.getByLoc(new Location(location.getWorld(), x, y, z)) != null)
                        return true;
                }
            }

            if (griefPreventionAPI != null && NatureRevivePlugin.readonlyConfig.griefPreventionStrictCheck) {
                Collection<me.ryanhamshire.GriefPrevention.Claim> griefPrevention = griefPreventionAPI.getClaims(chunk.getX(), chunk.getZ());
                if (griefPrevention.size() > 0) {
                     if (griefPreventionAPI.getClaimAt(new Location(location.getWorld(), x, y, z), true, null) != null)
                         return true;
                }
            }

            if (griefDefenderAPI != null && readonlyConfig.griefDefenderStrictCheck) {
                UUID uuid = griefDefenderAPI.getClaimAt(new Location(location.getWorld(), x, y, z)).getOwnerUniqueId();
                if (!uuid.equals(emptyUUID))
                    return true;
            }

            return false;
        });

        ObfuscateLootListener.randomizeChunkOre(chunk);

        /*if (blocks.size() > 0) {
            for (BlockStateWithPos blockWithPos : blocks) {
                BlockPos bp = new BlockPos(blockWithPos.getLocation().getBlockX(), blockWithPos.getLocation().getBlockY(), blockWithPos.getLocation().getBlockZ());
                (((CraftWorld) location.getWorld()).getHandle()).setBlock(bp, blockWithPos.getBlockState(), 3);
            }
        }

        if (nbtWithPos.size() > 0) {
            for (NbtWithPos tileEntityPos : nbtWithPos) {
                BlockEntity tileEntity = (((CraftWorld) location.getWorld()).getHandle()).getBlockEntity(new BlockPos(tileEntityPos.getLocation().getBlockX(), tileEntityPos.getLocation().getBlockY(), tileEntityPos.getLocation().getBlockZ()));
                try {
                    tileEntity.load(tileEntityPos.getNbt());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }*/

        // location.getWorld().refreshChunk(chunk.getX(), chunk.getZ());

        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            savingMovableStructure(chunk, oldChunkSnapshot);

            if (residenceAPI != null && readonlyConfig.residenceStrictCheck)
                residenceOldStateRevert(chunk, oldChunkSnapshot, nbtWithPos);

            if (griefPreventionAPI != null && readonlyConfig.griefPreventionStrictCheck)
                griefPreventionOldStateRevert(chunk, oldChunkSnapshot, nbtWithPos);

            if (griefDefenderAPI != null && readonlyConfig.griefDefenderStrictCheck)
                griefDefenderOldStateRevert(chunk, oldChunkSnapshot, nbtWithPos);

            if (coreProtectAPI != null && readonlyConfig.coreProtectLogging)
                coreProtectAPILogging(chunk, oldChunkSnapshot);
        });

        location.getChunk().unload(true);
    }

    private static void coreProtectAPILogging(Chunk chunk, ChunkSnapshot oldChunkSnapshot) {
        synchronized (blockDataChangeWithPos) {
            for (int x = 0; x < 16; x++) {
                for (int y = nmsWrapper.getWorldMinHeight(chunk.getWorld()); y < chunk.getWorld().getMaxHeight(); y++) {
                    for (int z = 0; z < 16; z++) {
                        Block newBlock = chunk.getBlock(x, y, z);

                        Material oldBlockType = oldChunkSnapshot.getBlockType(x, y, z);
                        Material newBlockType = newBlock.getType();

                        if (OreBlocksCompat.contains(oldBlockType)) continue;

                        if (!oldBlockType.equals(newBlockType)) {
                            Location location = new Location(chunk.getWorld(), (chunk.getX() << 4) + x, y, (chunk.getZ() << 4) + z);
                            BlockData oldBlockData = oldChunkSnapshot.getBlockData(x, y, z);
                            BlockData newBlockData = newBlock.getBlockData();
                            if (oldBlockType.equals(Material.AIR)) {
                                // new block put
                                //coreProtectAPI.logPlacement(readonlyConfig.coreProtectUserName, location, newBlockType, newBlock.getBlockData());
                                blockDataChangeWithPos.add(new BlockDataChangeWithPos(location, oldBlockData, newBlockData, BlockDataChangeWithPos.Type.PLACEMENT));
                            } else {
                                // Block break

                                //coreProtectAPI.logRemoval(readonlyConfig.coreProtectUserName, location, oldBlockType, oldBlockData);
                                if (!newBlockType.equals(Material.AIR)) {
                                    blockDataChangeWithPos.add(new BlockDataChangeWithPos(location, oldBlockData, newBlockData, BlockDataChangeWithPos.Type.REPLACE));
                                    //coreProtectAPI.logPlacement(readonlyConfig.coreProtectUserName, location, newBlockType, newBlock.getBlockData());
                                } else {
                                    blockDataChangeWithPos.add(new BlockDataChangeWithPos(location, oldBlockData, newBlockData, BlockDataChangeWithPos.Type.REMOVAL));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void residenceOldStateRevert(Chunk chunk, ChunkSnapshot oldChunkSnapshot, List<NbtWithPos> tileEntities) {
        Map<Location, BlockData> perversedBlocks = new HashMap<>();

        List<ClaimedResidence> residences = ((ResidenceManager) residenceAPI).getByChunk(chunk);
        if (residences.size() > 0) {
            for (int x = 0; x < 16; x++) {
                for (int y = nmsWrapper.getWorldMinHeight(chunk.getWorld()); y <= chunk.getWorld().getMaxHeight(); y++) {
                    for (int z = 0; z < 16; z++) {
                        Location targetLocation = new Location(chunk.getWorld(), (chunk.getX() << 4) + x, y, (chunk.getZ() << 4) + z);
                        if (residenceAPI.getByLoc(targetLocation) != null) {
                            BlockData block = oldChunkSnapshot.getBlockData(x, y, z);
                            if (!chunk.getBlock(x, y, z).getBlockData().equals(block)) {
                                perversedBlocks.put(targetLocation, block);
                            }
                        }
                    }
                }
            }

            setBlocksSynchronous(perversedBlocks, tileEntities);

            /*if (tileEntities.size() > 0) {
                for (NbtWithPos tileEntityPos : tileEntities) {
                    BlockEntity tileEntity = (((CraftWorld) location.getWorld()).getHandle()).getBlockEntity(new BlockPos(tileEntityPos.getLocation().getBlockX(), tileEntityPos.getLocation().getBlockY(), tileEntityPos.getLocation().getBlockZ()));
                    try {
                        tileEntity.load(tileEntityPos.getNbt());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }*/
        }
    }

    private static void griefPreventionOldStateRevert(Chunk chunk, ChunkSnapshot oldChunkSnapshot, List<NbtWithPos> tileEntities){
        Map<Location, BlockData> perversedBlocks = new HashMap<>();

        Collection<me.ryanhamshire.GriefPrevention.Claim> GriefPrevention = griefPreventionAPI.getClaims(chunk.getX(), chunk.getZ());
        if (GriefPrevention.size() > 0) {
            for (int x = 0; x < 16; x++) {
                for (int y = nmsWrapper.getWorldMinHeight(chunk.getWorld()); y <= chunk.getWorld().getMaxHeight() - 1; y++) {
                    for (int z = 0; z < 16; z++) {
                        Location targetLocation = new Location(chunk.getWorld(), (chunk.getX() << 4) + x, y, (chunk.getZ() << 4) + z);
                        if (griefPreventionAPI.getClaimAt(targetLocation, true, null) != null){
                            try {
                                BlockData block = oldChunkSnapshot.getBlockData(x, y, z);
                                if (!chunk.getBlock(x, y, z).getBlockData().equals(block)) {
                                    perversedBlocks.put(targetLocation, block);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            setBlocksSynchronous(perversedBlocks, tileEntities);
        }
    }

    private static void griefDefenderOldStateRevert(Chunk chunk, ChunkSnapshot oldChunkSnapshot, List<NbtWithPos> tileEntities) {
        Map<Location, BlockData> perversedBlocks = new HashMap<>();

        List<UUID> claimUUIDList = new ArrayList<>();for (int x = 0; x < 16; x++) {
            for (int y = nmsWrapper.getWorldMinHeight(chunk.getWorld()); y < chunk.getWorld().getMaxHeight() - 1; y++) {
                for (int z = 0; z < 16; z++) {
                    Location claimLocation = chunk.getBlock(x, y, z).getLocation();
                    UUID uuid = griefDefenderAPI.getClaimAt(claimLocation).getOwnerUniqueId();

                    if (!uuid.equals(emptyUUID)) {
                        com.griefdefender.api.claim.Claim claim = griefDefenderAPI.getClaimAt(claimLocation);
                        UUID claimUUID = claim.getUniqueId();
                        if (!claimUUIDList.contains(claimUUID)) {
                            claimUUIDList.add(claimUUID);
                        }
                    }
                }
            }
        }

        if (claimUUIDList.size() > 0) {
            for (int x = 0; x < 16; x++) {
                for (int y = nmsWrapper.getWorldMinHeight(chunk.getWorld()); y < chunk.getWorld().getMaxHeight() - 1 ; y++) {
                    for (int z = 0; z < 16; z++) {
                        Location targetLocation = new Location(chunk.getWorld(), (chunk.getX() << 4) + x, y, (chunk.getZ() << 4) + z);
                        UUID uuid = griefDefenderAPI.getClaimAt(targetLocation).getOwnerUniqueId();
                        if (!uuid.equals(emptyUUID)) {
                            try {
                                BlockData block = oldChunkSnapshot.getBlockData(x, y, z);
                                if (!chunk.getBlock(x, y, z).getBlockData().equals(block)) {
                                    perversedBlocks.put(targetLocation, block);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        setBlocksSynchronous(perversedBlocks, tileEntities);
    }

    private static void savingMovableStructure(Chunk chunk, ChunkSnapshot oldChunkSnapshot) {
        Map<Location, BlockData> perversedBlocks = new HashMap<>();
        for (int x = 0; x < 16; x++) {
            for (int y = nmsWrapper.getWorldMinHeight(chunk.getWorld()); y < chunk.getWorld().getMaxHeight(); y++) {
                for (int z = 0; z < 16; z++) {
                    Material blockType = oldChunkSnapshot.getBlockType(x, y, z);
                    Location originLocation = new Location(chunk.getWorld(), (chunk.getX() << 4) + x, y, (chunk.getZ() << 4) + z);

                    if ((blockType.equals(Material.END_PORTAL) || blockType.equals(Material.END_GATEWAY)) && !perversedBlocks.containsKey(originLocation)) {

                        for (int i = -2; i <= 2; i++)
                            for (int j = -2; j <= 2; j++)
                                for (int k = -2; k <= 2; k++) {
                                    Location neighborLocation = originLocation.clone().add(i, j, k);
                                    if (isNotInTheChunk(chunk, neighborLocation)) continue;

                                    int[] xyz = convertLocationToInChunkXYZ(neighborLocation);

                                    Material targetType = oldChunkSnapshot.getBlockType(xyz[0], xyz[1], xyz[2]);

                                    if (targetType.equals(Material.END_PORTAL) || targetType.equals(Material.END_GATEWAY) || targetType.equals(Material.BEDROCK))
                                        perversedBlocks.put(neighborLocation, oldChunkSnapshot.getBlockData(xyz[0], xyz[1], xyz[2]));
                                }
                    } else if (blockType.equals(Material.NETHER_PORTAL) && !perversedBlocks.containsKey(originLocation)) {
                        for (int i = -1; i <= 1; i++)
                            for (int j = -1; j <= 1; j++)
                                for (int k = -1; k <= 1; k++) {
                                    Location neighborLocation = originLocation.clone().add(i, j, k);
                                    if (isNotInTheChunk(chunk, neighborLocation)) continue;

                                    int[] xyz = convertLocationToInChunkXYZ(neighborLocation);

                                    Material targetType = oldChunkSnapshot.getBlockType(xyz[0], xyz[1], xyz[2]);

                                    if (targetType.equals(Material.NETHER_PORTAL) || targetType.equals(Material.OBSIDIAN))
                                        perversedBlocks.put(neighborLocation, oldChunkSnapshot.getBlockData(xyz[0], xyz[1], xyz[2]));
                                }
                    } else if (blockType.equals(Material.BEDROCK)) {
                        if (!chunk.getWorld().getEnvironment().equals(World.Environment.THE_END))
                            continue;

                        if (isInSpecialChunks(chunk)) {
                            perversedBlocks.put(originLocation, oldChunkSnapshot.getBlockData(x, y, z));

                            Location neighborLocation = originLocation.clone().add(0, 1, 0);
                            perversedBlocks.put(neighborLocation, oldChunkSnapshot.getBlockData(x, y + 1, z));
                            continue;
                        }

                        for (int i = -2; i <= 2; i++)
                            for (int j = -2; j <= 2; j++)
                                for (int k = -2; k <= 2; k++) {
                                    Location neighborLocation = originLocation.clone().add(i, j, k);
                                    Material neighborBlockType = neighborLocation.getBlock().getType();

                                    if (perversedBlocks.containsKey(neighborLocation) || (neighborBlockType.equals(Material.END_GATEWAY)) || neighborBlockType.equals(Material.END_PORTAL)) {
                                        perversedBlocks.put(originLocation, oldChunkSnapshot.getBlockData(x, y, z));
                                        break;
                                    }
                                }
                    } else if (blockType.equals(Material.OBSIDIAN)) {
                        for (int i = -1; i <= 1; i++)
                            for (int j = -1; j <= 1; j++)
                                for (int k = -1; k <= 1; k++) {
                                    Location neighborLocation = originLocation.clone().add(i, j, k);
                                    if (isNotInTheChunk(chunk, neighborLocation)) continue;

                                    if (perversedBlocks.containsKey(neighborLocation)) {
                                        perversedBlocks.put(originLocation, oldChunkSnapshot.getBlockData(x, y, z));
                                        break;
                                    }
                                }
                    } else if (blockType.equals(Material.DRAGON_EGG)) {
                        Location neighborLocation = originLocation.clone().add(0, -1, 0);
                        if (perversedBlocks.containsKey(neighborLocation)) {
                            perversedBlocks.put(originLocation, oldChunkSnapshot.getBlockData(x, y, z));
                        }
                    } else if (blockType.equals(Material.WALL_TORCH)) {
                        if (!chunk.getWorld().getEnvironment().equals(World.Environment.THE_END))
                            continue;

                        if (isInSpecialChunks(chunk)) {
                            perversedBlocks.put(originLocation, oldChunkSnapshot.getBlockData(x, y, z));
                            continue;
                        }

                        for (int i = -1; i <= 1; i++)
                            for (int k = -1; k <= 1; k++) {
                                Location neighborLocation = originLocation.clone().add(i, 0, k);
                                if (isNotInTheChunk(chunk, neighborLocation)) continue;

                                if (perversedBlocks.containsKey(neighborLocation)) {
                                    perversedBlocks.put(originLocation, oldChunkSnapshot.getBlockData(x, y, z));
                                    break;
                                }
                            }
                    }
                }
            }
        }

        setBlocksSynchronous(perversedBlocks, Collections.EMPTY_LIST);
    }

    private static void setBlocksSynchronous(Map<Location, BlockData> perversedBlocks, List<NbtWithPos> tileEntities) {
        synchronized (blockStateWithPosQueue) {
            for (Location location : perversedBlocks.keySet()) {
                boolean findTheNbt = isFindTheNbt(perversedBlocks, tileEntities, location);

                if (!findTheNbt)
                    blockStateWithPosQueue.add(new BlockStateWithPos(nmsWrapper.convertBlockDataToBlockState(perversedBlocks.get(location)), location));
            }
        }
    }

    private static boolean isFindTheNbt(Map<Location, BlockData> perversedBlocks, List<NbtWithPos> tileEntities, Location location) {
        boolean findTheNbt = false;
        for (NbtWithPos nbtWithPos : tileEntities) {
            if (nbtWithPos.getLocation().equals(location)) {
                blockStateWithPosQueue.add(new BlockStateWithPos(nmsWrapper.convertBlockDataToBlockState(perversedBlocks.get(location)), location, nbtWithPos.getNbt()));
                findTheNbt = true;
                break;
            }
        }
        return findTheNbt;
    }

    public static File takeSnapshot(Chunk chunk) throws IOException {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF(chunk.getX() + "," + chunk.getZ());

        for (int x = 0; x < 16; x++) {
            for (int y = nmsWrapper.getWorldMinHeight(chunk.getWorld()); y <= chunk.getWorld().getMaxHeight(); y++) {
                for (int z = 0; z < 16; z++) {
                    Block block = chunk.getBlock(x, y, z);
                    String nbt = block.getBlockData().getAsString();

                    out.writeUTF(x + ";" + y + ";" + z + ";" + block.getType() + ";" + nbt);
                }
            }
        }

        for (BlockState blockState : chunk.getTileEntities()) {
            String nbt = nmsWrapper.getNbtAsString(chunk.getWorld(), blockState);
            out.writeUTF(blockState.getX() + ";" + blockState.getY() + ";" + blockState.getZ() + ";" + nbt);
        }

        new File("plugins/NatureRevive/snapshots").mkdirs();

        FileOutputStream outputStream = new FileOutputStream("plugins/NatureRevive/snapshots/" + chunk.hashCode() + ".snapshot");

        outputStream.write(out.toByteArray());
        outputStream.close();

        return new File("plugins/NatureRevive/snapshots/" + chunk.hashCode() + ".snapshot");
    }

    public static Chunk revertSnapshot(World world, File file) throws IOException {
        ByteArrayDataInput inputStream = ByteStreams.newDataInput(Files.readAllBytes(file.toPath()));
        String[] coordsInString = inputStream.readUTF().split(",");

        Chunk chunk = world.getChunkAt(Integer.parseInt(coordsInString[0]), Integer.parseInt(coordsInString[1]));

        if (!chunk.isLoaded())
            chunk.load();

        List<BlockStateWithPos> blockList = new ArrayList<>();
        List<NbtWithPos> nbtList = new ArrayList<>();

        while (true) {
            String[] argument = null;
            try {
                String data = inputStream.readUTF();
                if (data.equals("\n")) continue;

                argument = data.split(";");

                if (argument.length == 5) {
                    blockList.add(new BlockStateWithPos(nmsWrapper.convertBlockDataToBlockState(Bukkit.createBlockData(argument[4])), new Location(world, Integer.parseInt(argument[0]), Integer.parseInt(argument[1]), Integer.parseInt(argument[2]))));
                } else {
                    nbtList.add(new NbtWithPos(argument[3], new Location(world, Integer.parseInt(argument[0]), Integer.parseInt(argument[1]), Integer.parseInt(argument[2]))));
                }
            } catch (Exception e) {
                break;
            }
        }
        
        for (BlockStateWithPos block : blockList) {
            nmsWrapper.setBlockNMS(chunk.getWorld(), (chunk.getX() << 4) + block.getLocation().getBlockX(), block.getLocation().getBlockY(), (chunk.getZ() << 4) + block.getLocation().getBlockZ(), block.getBlockState().getBlockData());
        }

        for (NbtWithPos nbtWithPos : nbtList) {
            try {
                nmsWrapper.loadTileEntity(chunk.getWorld(), nbtWithPos.getLocation().getBlockX(), nbtWithPos.getLocation().getBlockY(), nbtWithPos.getLocation().getBlockZ(), nbtWithPos.getNbt());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return chunk;
    }

    private static Location getMiddleOfLocation(Location location) {
        Chunk chunk = location.getChunk();
        return new Location(location.getWorld(), (chunk.getX() << 4) + 8, location.getBlockY(), (chunk.getZ() << 4) + 8);
    }

    private static boolean isNotInTheChunk(Chunk chunk, Location location) {
        return location.getChunk().getX() != chunk.getX() || location.getChunk().getZ() != chunk.getZ();
    }

    // The method is hardcoded to detect the end gateway.
    private static boolean isInSpecialChunks(Chunk chunk) {
        return (chunk.getX() == 0 || chunk.getX() == -1) && (chunk.getZ() == 0 || chunk.getZ() == -1);
    }

    private static int[] convertLocationToInChunkXYZ(Location location) {
        return new int[] { (location.getBlockX() - ((location.getBlockX() >> 4) << 4)), location.getBlockY(), (location.getBlockZ() - ((location.getBlockZ() >> 4) << 4)) };
    };

    private class BlockDataWithPos {
        private final BlockData blockData;
        private final Location location;

        public BlockDataWithPos(BlockData blockState, Location location) {
            this.blockData = blockState;
            this.location = location;
        }

        public Location getLocation() {
            return location;
        }

        public BlockData getBlockData() {
            return blockData;
        }
    }
}
