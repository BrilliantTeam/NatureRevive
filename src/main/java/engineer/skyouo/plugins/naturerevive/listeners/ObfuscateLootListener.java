package engineer.skyouo.plugins.naturerevive.listeners;

import com.google.common.collect.Lists;
import engineer.skyouo.plugins.naturerevive.NatureRevive;
import engineer.skyouo.plugins.naturerevive.constants.OreBlocks;
import engineer.skyouo.plugins.naturerevive.generator.PerlinNoise;
import it.unimi.dsi.fastutil.objects.ObjectDoubleImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTables;

import java.security.SecureRandom;
import java.util.*;

public class ObfuscateLootListener implements Listener {
    private static final Random secureRandom = new SecureRandom();
    private static final PerlinNoise perlinNoise = new PerlinNoise();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractionEvent(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock.getType().equals(Material.CHEST) || clickedBlock.getType().equals(Material.CHEST_MINECART) || clickedBlock.getType().equals(Material.TRAPPED_CHEST)) {
            Chest craftChest = (Chest) event.getClickedBlock().getState();
            if (craftChest.getLootTable() == null) {
                return;
            }

            if (!Objects.equals(craftChest.getLootTable(), LootTables.EMPTY.getLootTable())) {
                if (NatureRevive.readonlyConfig.debug)
                    NatureRevive.logger.info("[DEBUG] Lootable Chest re-seeded as " + craftChest.getLootTable());

                craftChest.setSeed(secureRandom.nextLong());

                craftChest.getBlockInventory().clear();

                craftChest.getLootTable().fillInventory(craftChest.getBlockInventory(), secureRandom,
                        new LootContext.Builder(craftChest.getLocation())
                                .luck(
                                        ((CraftPlayer) event.getPlayer()).getHandle().getLuck()
                                )
                                .killer(
                                        event.getPlayer()
                                ).build()
                );

                ChunkRelatedEventListener.flagChunk(event.getClickedBlock().getLocation());

                if (NatureRevive.readonlyConfig.debug)
                    NatureRevive.logger.info("[DEBUG] Lootable Chest regenerated, seed = " + craftChest.getSeed() + ".");
            }

            // Old method might replace the loot chest which has been looted.
            /*if (!Objects.equals(craftChest.getLootTable(), LootTables.EMPTY.getLootTable())) {
                System.out.println("[DEBUG] Lootable Chest regenerated as " + craftChest.getLootTable());
                Objects.requireNonNull(craftChest.getLootTable()).fillInventory(craftChest.getBlockInventory(), secureRandom,
                        new LootContext.Builder(craftChest.getLocation())
                                .luck(
                                        ((CraftPlayer) event.getPlayer()).getHandle().getLuck()
                                )
                                .killer(
                                        event.getPlayer()
                                ).build()
                );
            }*/
        }
    }

    // This method should be called after new chunk generated to obfuscate the chunk.
    public static void randomizeChunkOre(Chunk chunk) {
        if (chunk.getWorld().getEnvironment().equals(World.Environment.THE_END)) return; // Does not need since no ore
        Location location = new Location(chunk.getWorld(), chunk.getX() << 4, 256, chunk.getZ() << 4);
        List<ObjectDoublePair<Location>> pairList = new ArrayList<>();
        List<Block> oreList = new ArrayList<>();

        if (chunk.getWorld().getEnvironment().equals(World.Environment.NETHER)) {
            int surface = findSurface(location);
            if (surface < 140) surface = 140;

            //System.out.println(chunk.getWorld().getMaxHeight());
            //System.out.println(findSurface(location));
            for (int x = 0; x < 16; x++) {
                for (int y = chunk.getWorld().getMinHeight() + 1; y <= surface; y++) {
                    for (int z = 0; z < 16; z++) {
                        Block block = chunk.getBlock(x, y, z);
                        if (OreBlocks.contains(block.getType())) {
                            oreList.add(block);
                        }
                    }
                }

                for (int y = chunk.getWorld().getMaxHeight() - 1; y >= surface; y--) {
                    calculateExpectation(chunk, pairList, oreList, x, y);
                }
            }
        } else {
            int surface = findSurface(location);
            for (int x = 0; x < 16; x++) {
                for (int y = chunk.getWorld().getMinHeight(); y <= surface; y++) {
                    calculateExpectation(chunk, pairList, oreList, x, y);
                }
            }
        }

        pairList.sort(Comparator.comparingDouble(ObjectDoublePair::secondDouble));
        Lists.reverse(pairList);

        for (int i = 0; i < oreList.size(); i++) {
            Location loc = pairList.get(i).first();

            Block ore = oreList.get(i);
            Block replaced = chunk.getWorld().getBlockAt(loc);

            //System.out.println(ore);

            BlockData blockData = replaced.getBlockData().clone();

            chunk.getWorld().setBlockData(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), ore.getBlockData());
            chunk.getWorld().setBlockData(ore.getX(), ore.getY(), ore.getZ(), blockData);

            if (NatureRevive.readonlyConfig.debug)
                NatureRevive.logger.info("Swap %d,%d,%d (%s) to %d,%d,%d (%s)".formatted(
                    ore.getX(), ore.getY(), ore.getZ(), ore.getType().toString(),
                    replaced.getX(), replaced.getY(), replaced.getZ(), blockData.getMaterial().toString()
                ));
        }

        oreList.clear();
        pairList.clear();
    }

    private static void calculateExpectation(Chunk chunk, List<ObjectDoublePair<Location>> pairList, List<Block> oreList, int x, int y) {
        for (int z = 0; z < 16; z++) {
            Block block = chunk.getBlock(x, y, z);
            if (block.getType().equals(Material.AIR) || block.getType().equals(Material.WATER) || block.getType().equals(Material.LAVA)) continue;

            if (OreBlocks.contains(block.getType())) {
                oreList.add(block);
            } else {
                // double expectation = perlinNoise.noise((chunk.getX() << 4) + x, y, (chunk.getZ() << 4) + z);

                if (getIfRelativeIs(block, Material.AIR)) continue;

                pairList.add(
                        new ObjectDoubleImmutablePair<>(
                                new Location(chunk.getWorld(), (chunk.getX() << 4) + x, y, (chunk.getZ() << 4) + z),
                                secureRandom.nextDouble(10)
                        )
                );
            }
        }
    }

    // This method is not guaranteed to work in THE_END or THE_NETHER
    private static int findSurface(Location top) {
        Location result = top.clone();

        if (!top.getWorld().getEnvironment().equals(World.Environment.NETHER)) {
            int minHeight = top.getWorld().getMinHeight();

            while (!result.getBlock().getType().equals(Material.STONE) && !result.getBlock().getType().equals(Material.DEEPSLATE) &&
            result.getBlockY() > minHeight){
                result.add(0, -1, 0);
            }
        } else {
            top.setY(80);

            int maxHeight = top.getWorld().getMaxHeight();

            while (!result.getBlock().getType().equals(Material.NETHERRACK) && !result.getBlock().getType().equals(Material.BEDROCK) &&
            result.getBlockY() < maxHeight){
                result.add(0, 1, 0);
            }
        }

        return result.getBlockY();
    }

    private static BlockFace[] blockFaces = { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };

    private static boolean getIfRelativeIs(Block block, Material target) {
        for (BlockFace face : blockFaces) {
            if (block.getRelative(face).getType() == target) return true;
        }

        return false;
    }
}
