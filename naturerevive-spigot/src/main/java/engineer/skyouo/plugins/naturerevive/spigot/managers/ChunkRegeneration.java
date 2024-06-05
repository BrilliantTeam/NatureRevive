package engineer.skyouo.plugins.naturerevive.spigot.managers;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.constants.OreBlocksCompat;
import engineer.skyouo.plugins.naturerevive.spigot.listeners.ObfuscateLootListener;
import engineer.skyouo.plugins.naturerevive.spigot.managers.features.StructureRegeneration;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BlockDataChangeWithPos;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BlockStateWithPos;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BukkitPositionInfo;
import engineer.skyouo.plugins.naturerevive.spigot.structs.NbtWithPos;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.world.ChunkPopulateEvent;

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
            if (!residences.isEmpty()) {

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
            if (!griefPrevention.isEmpty()) {
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

        if (Objects.equals(readonlyConfig.regenerationEngine, "bukkit")) {
            chunk.getWorld().regenerateChunk(chunk.getX(), chunk.getZ());
            regenerateAfterWork(chunk, oldChunkSnapshot, nbtWithPos);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                FaweImplRegeneration.regenerate(chunk, false, () -> {
                    regenerateAfterWork(chunk, oldChunkSnapshot, nbtWithPos);
                });
            });
        }

    }

    private static void regenerateAfterWork(Chunk chunk, ChunkSnapshot oldChunkSnapshot, List<NbtWithPos> nbtWithPos) {
        ObfuscateLootListener.randomizeChunkOre(chunk);

        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            StructureRegeneration.savingMovableStructure(chunk, oldChunkSnapshot);

            if (residenceAPI != null && readonlyConfig.residenceStrictCheck)
                residenceOldStateRevert(chunk, oldChunkSnapshot, nbtWithPos);

            if (griefPreventionAPI != null && readonlyConfig.griefPreventionStrictCheck)
                griefPreventionOldStateRevert(chunk, oldChunkSnapshot, nbtWithPos);

            if (griefDefenderAPI != null && readonlyConfig.griefDefenderStrictCheck)
                griefDefenderOldStateRevert(chunk, oldChunkSnapshot, nbtWithPos);

            if (coreProtectAPI != null && readonlyConfig.coreProtectLogging)
                coreProtectAPILogging(chunk, oldChunkSnapshot);
        });

        Bukkit.getScheduler().runTaskLater(instance, () -> Bukkit.getPluginManager().callEvent(new ChunkPopulateEvent(chunk)), 4L);
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
        if (!residences.isEmpty()) {
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

        if (!claimUUIDList.isEmpty()) {
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

    public static void setBlocksSynchronous(Map<Location, BlockData> perversedBlocks, List<NbtWithPos> tileEntities) {
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
}
