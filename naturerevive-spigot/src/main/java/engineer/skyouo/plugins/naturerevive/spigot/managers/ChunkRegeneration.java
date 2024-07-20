package engineer.skyouo.plugins.naturerevive.spigot.managers;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import engineer.skyouo.plugins.naturerevive.spigot.NatureReviveBukkitLogger;
import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.constants.OreBlocksCompat;
import engineer.skyouo.plugins.naturerevive.spigot.events.ChunkRegenEvent;
import engineer.skyouo.plugins.naturerevive.spigot.integration.IntegrationUtil;
import engineer.skyouo.plugins.naturerevive.spigot.integration.land.ILandPluginIntegration;
import engineer.skyouo.plugins.naturerevive.spigot.listeners.ObfuscateLootListener;
import engineer.skyouo.plugins.naturerevive.spigot.managers.features.ElytraRegeneration;
import engineer.skyouo.plugins.naturerevive.spigot.managers.features.StructureRegeneration;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BlockDataChangeWithPos;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BlockStateWithPos;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BukkitPositionInfo;
import engineer.skyouo.plugins.naturerevive.spigot.structs.NbtWithPos;
import engineer.skyouo.plugins.naturerevive.spigot.util.ScheduleUtil;
import engineer.skyouo.plugins.naturerevive.spigot.util.Util;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;

import java.time.LocalDateTime;
import java.util.*;

import static engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin.*;

public class ChunkRegeneration {
    public static void regenerateChunk(BukkitPositionInfo bukkitPositionInfo) {
        Location location = bukkitPositionInfo.getLocation();

        List<NbtWithPos> nbtWithPos = new ArrayList<>();

        World world = location.getWorld();
        // Thanks to xuan
        int centerX = location.getBlockX() >> 4;
        int centerY = location.getBlockZ() >> 4;
        int radius = 2;
        for (int x = -radius; x < (radius + 1); x++) {
            for (int z = -radius; z < (radius + 1); z++) {
                world.addPluginChunkTicket(centerX + x, centerY + z, instance);
            }
        }

        Chunk chunk = location.getChunk();

        if (!location.getWorld().isChunkGenerated(chunk.getX(), chunk.getZ())) {
            return;
        }

        boolean checkBiomes = !readonlyConfig.ignoredBiomes.isEmpty();
        ChunkSnapshot oldChunkSnapshot = chunk.getChunkSnapshot(checkBiomes, checkBiomes, false);

        if (checkBiomes) {
            for (int x = 0; x < 16; x++) {
               for (int z = 0; z < 16; z++) {
                   for (int y = nmsWrapper.getWorldMinHeight(chunk.getWorld()) + 1; y <= oldChunkSnapshot.getHighestBlockYAt(x, z); y++) {
                        Biome biome = oldChunkSnapshot.getBiome(x, y, z);

                        if (readonlyConfig.ignoredBiomes.contains(biome.getKey().getKey()))
                            return;
                   }
                }
            }
        }

        // todo: make this asynchronous.
        ILandPluginIntegration integration = IntegrationUtil.pickLandPluginIntegration();

        if (integration != null && integration.checkHasLand(chunk)) {
            for (BlockState blockState : chunk.getTileEntities()) {
                if (integration.isInLand(new Location(location.getWorld(), blockState.getX(), blockState.getY(), blockState.getZ()))) {
                    String nbt = nmsWrapper.getNbtAsString(chunk.getWorld(), blockState);

                    nbtWithPos.add(new NbtWithPos(nbt, chunk.getWorld(), blockState.getX(), blockState.getY(), blockState.getZ()));
                }
            }
        }

        if (Objects.equals(readonlyConfig.regenerationEngine, "bukkit")) {
            try {
                chunk.getWorld().regenerateChunk(chunk.getX(), chunk.getZ());
            } catch (Exception ex) {
                NatureReviveBukkitLogger.warning(String.format("NatureRevive 在重生世界 %s 區塊 (%d, %d) 時遇到了問題。", chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
                ex.printStackTrace();
            }
            regenerateAfterWork(chunk, oldChunkSnapshot, integration, nbtWithPos);
        } else {
            ScheduleUtil.GLOBAL.runTaskAsynchronously(instance, () -> {
                try {
                    FaweImplRegeneration.regenerate(chunk, false, () -> {
                        ScheduleUtil.REGION.runTask(instance, chunk, () -> {
                            regenerateAfterWork(chunk, oldChunkSnapshot, integration, nbtWithPos);
                        });
                    });
                } catch (Exception ex) {
                    NatureReviveBukkitLogger.warning(String.format("NatureRevive 在重生世界 %s 區塊 (%d, %d) 時遇到了問題。", chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
                    ex.printStackTrace();
                }
            });
        }

    }

    private static void regenerateAfterWork(Chunk chunk, ChunkSnapshot oldChunkSnapshot, ILandPluginIntegration integration, List<NbtWithPos> nbtWithPos) {
        int radius = 2;
        for (int x = -radius; x < (radius + 1); x++) {
            for (int z = -radius; z < (radius + 1); z++) {
                chunk.getWorld().removePluginChunkTicket(chunk.getX() + x, chunk.getZ() + z, instance);
            }
        }

        if (readonlyConfig.enableOreObfuscation)
            ObfuscateLootListener.randomizeChunkOre(chunk);

        ChunkSnapshot newChunkSnapshot = chunk.getChunkSnapshot();

        // We can offload it to other thread if not on folia
        if (!Util.isFolia()) {
            ScheduleUtil.GLOBAL.runTaskAsynchronously(instance, () -> {
                ElytraRegeneration.isEndShip(integration, chunk, newChunkSnapshot);

                StructureRegeneration.savingMovableStructure(chunk, oldChunkSnapshot);

                if (integration != null)
                    landOldStateRevert(integration, chunk, oldChunkSnapshot, nbtWithPos);

                if (coreProtectAPI != null && readonlyConfig.coreProtectLogging)
                    coreProtectAPILogging(chunk, oldChunkSnapshot);
            });
        } else {
            ElytraRegeneration.isEndShip(integration, chunk, newChunkSnapshot);

            StructureRegeneration.savingMovableStructure(chunk, oldChunkSnapshot);

            if (integration != null)
                landOldStateRevert(integration, chunk, oldChunkSnapshot, nbtWithPos);

            if (coreProtectAPI != null && readonlyConfig.coreProtectLogging)
                coreProtectAPILogging(chunk, oldChunkSnapshot);
        }

        ScheduleUtil.GLOBAL.runTaskLater(instance, () -> Bukkit.getPluginManager().callEvent(new ChunkRegenEvent(chunk, LocalDateTime.now())), 4L);
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

    private static void landOldStateRevert(ILandPluginIntegration integration, Chunk chunk, ChunkSnapshot oldChunkSnapshot, List<NbtWithPos> tileEntities) {
        Map<Location, BlockData> perversedBlocks = new HashMap<>();

        if (integration.checkHasLand(chunk)) {
            for (int x = 0; x < 16; x++) {
                for (int y = nmsWrapper.getWorldMinHeight(chunk.getWorld()); y <= chunk.getWorld().getMaxHeight(); y++) {
                    for (int z = 0; z < 16; z++) {
                        Location targetLocation = new Location(chunk.getWorld(), (chunk.getX() << 4) + x, y, (chunk.getZ() << 4) + z);
                        if (integration.isInLand(targetLocation)) {
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
