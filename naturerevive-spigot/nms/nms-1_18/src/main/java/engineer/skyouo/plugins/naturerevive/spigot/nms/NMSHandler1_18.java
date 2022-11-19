package engineer.skyouo.plugins.naturerevive.spigot.nms;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import engineer.skyouo.plugins.naturerevive.common.INMSWrapper;
import engineer.skyouo.plugins.naturerevive.common.IPosCalculate;
import engineer.skyouo.plugins.naturerevive.common.VersionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftBlockStates;
import org.bukkit.craftbukkit.v1_18_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NMSHandler1_18 implements INMSWrapper {

    /**
     * The following code is referenced from PaperMC/Paper 'b4c1ae6' patches/server/0807-Implement-regenerateChunk.patch,
     * It's under GPLv3 license, the gpl license is located as 'naturerevive-spigot/src/main/resources/GPL.txt' or 'GPL.txt' in jar distribution.
     *
     * The following code is not modified.
     */
    private static final ChunkStatus[] REGEN_CHUNK_STATUSES = {ChunkStatus.BIOMES, ChunkStatus.NOISE, ChunkStatus.SURFACE, ChunkStatus.CARVERS, ChunkStatus.LIQUID_CARVERS, ChunkStatus.FEATURES};
    // end

    @Override
    public List<String> getCompatibleNMSVersion() {
        return List.of("1.18", "1.18.1");
    }

    @Override
    public boolean regenerateChunk(World world, int x, int z) {
        /**
         * The following code is referenced from PaperMC/Paper 'b4c1ae6' patches/server/0807-Implement-regenerateChunk.patch,
         * It's under GPLv3 license, the gpl license is located as 'naturerevive-spigot/src/main/resources/GPL.txt' or 'GPL.txt' in jar distribution.
         *
         * The following code is not modified and served as original as PaperMC did.
         */
        ServerLevel serverLevel = ((CraftWorld) world).getHandle();

        if (!serverLevel.hasChunk(x, z))
            return false;

        final net.minecraft.server.level.ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
        final ChunkPos chunkPos = new ChunkPos(x, z);
        final net.minecraft.world.level.chunk.LevelChunk levelChunk = serverChunkCache.getChunk(chunkPos.x, chunkPos.z, true);
        for (final BlockPos blockPos : BlockPos.betweenClosed(chunkPos.getMinBlockX(), serverLevel.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), serverLevel.getMaxBuildHeight() - 1, chunkPos.getMaxBlockZ())) {
            levelChunk.removeBlockEntity(blockPos);
            serverLevel.setBlock(blockPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 16);
        }

        for (final ChunkStatus chunkStatus : REGEN_CHUNK_STATUSES) {
            final List<ChunkAccess> list = new ArrayList<>();
            final int range = Math.max(1, chunkStatus.getRange());
            for (int chunkX = chunkPos.z - range; chunkX <= chunkPos.z + range; chunkX++) {
                for (int chunkZ = chunkPos.x - range; chunkZ <= chunkPos.x + range; chunkZ++) {
                    ChunkAccess chunkAccess = serverChunkCache.getChunk(chunkZ, chunkX, chunkStatus.getParent(), true);
                    if (chunkAccess instanceof ImposterProtoChunk accessProtoChunk) {
                        chunkAccess = new ImposterProtoChunk(accessProtoChunk.getWrapped(), true);
                    } else if (chunkAccess instanceof net.minecraft.world.level.chunk.LevelChunk accessLevelChunk) {
                        chunkAccess = new ImposterProtoChunk(accessLevelChunk, true);
                    }
                    list.add(chunkAccess);
                }
            }

            final java.util.concurrent.CompletableFuture<com.mojang.datafixers.util.Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> future = chunkStatus.generate(
                    Runnable::run,
                    serverLevel,
                    serverChunkCache.getGenerator(),
                    serverLevel.getStructureManager(),
                    serverChunkCache.getLightEngine(),
                    chunk -> {
                        throw new UnsupportedOperationException("Not creating full chunks here");
                    },
                    list,
                    true
            );
            serverChunkCache.mainThreadProcessor.managedBlock(future::isDone);
            if (chunkStatus == ChunkStatus.NOISE) {
                future.join().left().ifPresent(chunk -> net.minecraft.world.level.levelgen.Heightmap.primeHeightmaps(chunk, ChunkStatus.POST_FEATURES));
            }
        }

        for (final BlockPos blockPos : BlockPos.betweenClosed(chunkPos.getMinBlockX(), serverLevel.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), serverLevel.getMaxBuildHeight() - 1, chunkPos.getMaxBlockZ())) {
            serverChunkCache.blockChanged(blockPos);
        }

        final Set<ChunkPos> chunksToRelight = new HashSet<>(9);
        for (int chunkX = chunkPos.x - 1; chunkX <= chunkPos.x + 1; chunkX++) {
            for (int chunkZ = chunkPos.z - 1; chunkZ <= chunkPos.z + 1; chunkZ++) {
                chunksToRelight.add(new ChunkPos(chunkX, chunkZ));
            }
        }
        serverChunkCache.getLightEngine().relight(chunksToRelight, pos -> {}, relit -> {});
        return true;
        // end
    }

    @Override
    public boolean regenerateChunk(World world, int x, int z, IPosCalculate filter) {
        /**
         * The following code is referenced from PaperMC/Paper 'b4c1ae6' patches/server/0807-Implement-regenerateChunk.patch,
         * It's under GPLv3 license, the gpl license is located as 'naturerevive-spigot/src/main/resources/GPL.txt' or 'GPL.txt' in jar distribution.
         *
         * The following code is modified at line 128 - 129 which help us to skip the un-need position to clear.
         */
        ServerLevel serverLevel = ((CraftWorld) world).getHandle();

        if (!serverLevel.hasChunk(x, z))
            return false;

        final net.minecraft.server.level.ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
        final ChunkPos chunkPos = new ChunkPos(x, z);
        final net.minecraft.world.level.chunk.LevelChunk levelChunk = serverChunkCache.getChunk(chunkPos.x, chunkPos.z, true);
        for (final BlockPos blockPos : BlockPos.betweenClosed(chunkPos.getMinBlockX(), serverLevel.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), serverLevel.getMaxBuildHeight() - 1, chunkPos.getMaxBlockZ())) {
            if (filter.get(blockPos.getX(), blockPos.getY(), blockPos.getZ()))
                continue;

            levelChunk.removeBlockEntity(blockPos);
            serverLevel.setBlock(blockPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 16);
        }

        for (final ChunkStatus chunkStatus : REGEN_CHUNK_STATUSES) {
            final List<ChunkAccess> list = new ArrayList<>();
            final int range = Math.max(1, chunkStatus.getRange());
            for (int chunkX = chunkPos.z - range; chunkX <= chunkPos.z + range; chunkX++) {
                for (int chunkZ = chunkPos.x - range; chunkZ <= chunkPos.x + range; chunkZ++) {
                    ChunkAccess chunkAccess = serverChunkCache.getChunk(chunkZ, chunkX, chunkStatus.getParent(), true);
                    if (chunkAccess instanceof ImposterProtoChunk accessProtoChunk) {
                        chunkAccess = new ImposterProtoChunk(accessProtoChunk.getWrapped(), true);
                    } else if (chunkAccess instanceof net.minecraft.world.level.chunk.LevelChunk accessLevelChunk) {
                        chunkAccess = new ImposterProtoChunk(accessLevelChunk, true);
                    }
                    list.add(chunkAccess);
                }
            }

            final java.util.concurrent.CompletableFuture<com.mojang.datafixers.util.Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> future = chunkStatus.generate(
                    Runnable::run,
                    serverLevel,
                    serverChunkCache.getGenerator(),
                    serverLevel.getStructureManager(),
                    serverChunkCache.getLightEngine(),
                    chunk -> {
                        throw new UnsupportedOperationException("Not creating full chunks here");
                    },
                    list,
                    true
            );
            serverChunkCache.mainThreadProcessor.managedBlock(future::isDone);
            if (chunkStatus == ChunkStatus.NOISE) {
                future.join().left().ifPresent(chunk -> net.minecraft.world.level.levelgen.Heightmap.primeHeightmaps(chunk, ChunkStatus.POST_FEATURES));
            }
        }

        for (final BlockPos blockPos : BlockPos.betweenClosed(chunkPos.getMinBlockX(), serverLevel.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), serverLevel.getMaxBuildHeight() - 1, chunkPos.getMaxBlockZ())) {
            serverChunkCache.blockChanged(blockPos);
        }

        final Set<ChunkPos> chunksToRelight = new HashSet<>(9);
        for (int chunkX = chunkPos.x - 1; chunkX <= chunkPos.x + 1 ; chunkX++) {
            for (int chunkZ = chunkPos.z - 1; chunkZ <= chunkPos.z + 1 ; chunkZ++) {
                chunksToRelight.add(new ChunkPos(chunkX, chunkZ));
            }
        }
        serverChunkCache.getLightEngine().relight(chunksToRelight, pos -> {}, relit -> {});
        return true;
        // end
    }

    @Override
    public String getNbtAsString(World world, BlockState blockState) {
        return ((CraftWorld) world).getHandle().getBlockEntity(new BlockPos(blockState.getX(), blockState.getY(), blockState.getZ()))
                .saveWithFullMetadata().getAsString();
    }

    @Override
    public void setBlockNMS(World world, int x, int y, int z, BlockData data) {
        ((CraftWorld) world).getHandle().setBlock(
                new BlockPos(x, y, z), ((CraftBlockData) data).getState(), 3
        );
    }

    @Override
    public void loadTileEntity(World world, int x, int y, int z, String nbt) {
        try {
            ((CraftWorld) world).getHandle().getBlockEntity(new BlockPos(x, y, z))
                    .load(TagParser.parseTag(nbt));
        } catch (CommandSyntaxException | NullPointerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createTileEntity(World world, int x, int y, int z, BlockData data, String nbt) {
        setBlockNMS(world, x, y, z, data);
        loadTileEntity(world, x, y, z, nbt);
    }

    @Override
    public double[] getRecentTps() {
        return MinecraftServer.getServer().recentTps;
    }

    @Override
    public double getLuckForPlayer(Player player) {
        return ((CraftPlayer) player).getHandle().getLuck();
    }

    @Override
    public BlockState convertBlockDataToBlockState(BlockData blockData) {
        return CraftBlockStates.getBlockState(((CraftBlockData) blockData).getState(), null);
    }

    @Override
    public int getWorldMinHeight(World world) {
        return ((CraftWorld) world).getHandle().getMinBuildHeight();
    }

    Material[] oreBlocks = new Material[] {
            Material.COAL_ORE, Material.COPPER_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.LAPIS_ORE, Material.REDSTONE_ORE, Material.DEEPSLATE_COAL_ORE, Material.DEEPSLATE_COPPER_ORE, Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_GOLD_ORE, Material.DEEPSLATE_DIAMOND_ORE, Material.DEEPSLATE_EMERALD_ORE, Material.DEEPSLATE_LAPIS_ORE, Material.DEEPSLATE_REDSTONE_ORE, Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE, Material.ANCIENT_DEBRIS
    };

    @Override
    public Material[] getOreBlocks() {
        return oreBlocks;
    }
}