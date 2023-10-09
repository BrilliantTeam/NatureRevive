package engineer.skyouo.plugins.naturerevive.spigot.listeners;


import engineer.skyouo.plugins.naturerevive.spigot.NatureReviveBukkitLogger;
import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BukkitPositionInfo;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
//import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.UUID;

public class ChunkRelatedEventListener implements Listener {
    private static UUID emptyUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (event.isCancelled())
            return;

        NatureRevivePlugin.blockQueue.add(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;

        NatureRevivePlugin.blockQueue.add(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null)
            return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Location thisPoint = event.getClickedBlock().getLocation();
        int count = Math.abs(NatureRevivePlugin.readonlyConfig.suppressNearbyChunkCount);
        for (int i = -count;
             i < count + 1; i++) {
            for (int j = -count; j < count + 1; j++) {
                Location newLocation = new Location(thisPoint.getWorld(),
                        thisPoint.getBlockX() + 16 * i,
                        thisPoint.getBlockY(),
                        thisPoint.getBlockZ() + 16 * j);
                flagChunk(newLocation);
                log(event, newLocation);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockGrowEvent(BlockGrowEvent event) {
        Location thisPoint = event.getBlock().getLocation();
        int count = Math.abs(NatureRevivePlugin.readonlyConfig.suppressNearbyChunkCount);
        for (int i = -count;
             i < count + 1; i++) {
            for (int j = -count; j < count + 1; j++) {
                Location newLocation = new Location(thisPoint.getWorld(),
                        thisPoint.getBlockX() + 16 * i,
                        thisPoint.getBlockY(),
                        thisPoint.getBlockZ() + 16 * j);
                flagChunk(newLocation);
                log(event, newLocation);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockRedstoneEvent(BlockRedstoneEvent event) {
        Location thisPoint = event.getBlock().getLocation();
        int count = Math.abs(NatureRevivePlugin.readonlyConfig.suppressNearbyChunkCount);
        for (int i = -count;
             i < count + 1; i++) {
            for (int j = -count; j < count + 1; j++) {
                Location newLocation = new Location(thisPoint.getWorld(),
                        thisPoint.getBlockX() + 16 * i,
                        thisPoint.getBlockY(),
                        thisPoint.getBlockZ() + 16 * j);
                flagChunk(newLocation);
                log(event, newLocation);
            }
        }
    }

    /*
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBurnEvent(BlockBurnEvent event) {
        flagChunk(event.getBlock().getLocation());
        log(event, event.getBlock().getLocation());
    }
    */

    /*
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockCookEvent(BlockCookEvent event) {
        if (event.isCancelled())
            return;

        NatureRevivePlugin.blockQueue.add(event.getBlock().getLocation());
    }
     */


    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player)
            return;

        if (event.getEntity().getKiller() == null)
            return;

        NatureRevivePlugin.blockQueue.add(event.getEntity().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockExplodeEvent(BlockExplodeEvent event) {
        if (event.isCancelled())
            return;


        for (Block block : event.blockList()) {
            /*
            flagChunk(block.getLocation());
            log(event, block.getLocation());
             */

            NatureRevivePlugin.blockQueue.add(block.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityExplodeEvent(EntityExplodeEvent event) {
        if (event.isCancelled())
            return;

        for (Block block : event.blockList()) {
            /*
            log(event, block.getLocation());
            flagChunk(block.getLocation());
             */

            NatureRevivePlugin.blockQueue.add(block.getLocation());
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onBrewEvent(BrewEvent event) {
        NatureRevivePlugin.blockQueue.add(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFurnaceBurnEvent(FurnaceBurnEvent event) {
        NatureRevivePlugin.blockQueue.add(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoadEvent(ChunkLoadEvent e) {
        if (NatureRevivePlugin.readonlyConfig.regenerationStrategy.equalsIgnoreCase("passive")) {
            BukkitPositionInfo dummy = new BukkitPositionInfo(e.getWorld().getName(), e.getChunk().getX(), e.getChunk().getZ(), 0);
            BukkitPositionInfo positionInfo = NatureRevivePlugin.databaseConfig.get(dummy);

            if (positionInfo == null)
                return;

            if (positionInfo.isOverTTL()) {
                NatureRevivePlugin.queue.add(positionInfo);
                NatureRevivePlugin.databaseConfig.unset(positionInfo);
            }
        }
    }

    public static void flagChunk(Location location) {
        if (NatureRevivePlugin.readonlyConfig.ignoredWorld.contains(location.getWorld().getName()))
            return;

        if (NatureRevivePlugin.residenceAPI != null && !NatureRevivePlugin.readonlyConfig.residenceStrictCheck) {
            if (NatureRevivePlugin.residenceAPI.getByLoc(location) != null) {
                return;
            }
        }

        if (NatureRevivePlugin.griefPreventionAPI != null && !NatureRevivePlugin.readonlyConfig.griefPreventionStrictCheck){
            if (NatureRevivePlugin.griefPreventionAPI.getClaimAt(location, true, null) != null){
                return;
            }
        }

        if (NatureRevivePlugin.griefDefenderAPI != null && !NatureRevivePlugin.readonlyConfig.griefDefenderStrictCheck){
            UUID uuid = NatureRevivePlugin.griefDefenderAPI.getClaimAt(location).getOwnerUniqueId();
            if (!uuid.equals(emptyUUID)){
                return;
            }
        }

        BukkitPositionInfo positionInfo = new BukkitPositionInfo(location, System.currentTimeMillis() + NatureRevivePlugin.readonlyConfig.ttlDuration);

        NatureRevivePlugin.databaseConfig.set(positionInfo);
    }

    private void log(Event event, Location location) {
        if (NatureRevivePlugin.residenceAPI != null && !NatureRevivePlugin.readonlyConfig.residenceStrictCheck) {
            if (NatureRevivePlugin.residenceAPI.getByLoc(location) != null) {
                return;
            }
        }

        if (NatureRevivePlugin.griefPreventionAPI != null && !NatureRevivePlugin.readonlyConfig.griefPreventionStrictCheck){
            if (NatureRevivePlugin.griefPreventionAPI.getClaimAt(location, true, null) != null){
                return;
            }
        }

        if (NatureRevivePlugin.griefDefenderAPI != null && !NatureRevivePlugin.readonlyConfig.griefDefenderStrictCheck){
            UUID uuid = NatureRevivePlugin.griefDefenderAPI.getClaimAt(location).getOwnerUniqueId();
            if (!uuid.equals(emptyUUID)){
                return;
            }
        }

        if (NatureRevivePlugin.databaseConfig.get(location) != null)
            return;

        if (NatureRevivePlugin.readonlyConfig.debug)
            NatureReviveBukkitLogger.info(new BukkitPositionInfo(location, 0).toString() + " was flagged by event " + event.getEventName());
    }
}

