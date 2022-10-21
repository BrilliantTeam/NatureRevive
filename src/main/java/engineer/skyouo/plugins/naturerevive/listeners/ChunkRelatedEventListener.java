package engineer.skyouo.plugins.naturerevive.listeners;

import engineer.skyouo.plugins.naturerevive.NatureRevive;
import engineer.skyouo.plugins.naturerevive.manager.Task;
import engineer.skyouo.plugins.naturerevive.structs.ChunkPos;
import engineer.skyouo.plugins.naturerevive.structs.PositionInfo;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.UUID;

public class ChunkRelatedEventListener implements Listener {
    private static UUID emptyUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (event.isCancelled())
            return;


        NatureRevive.blockExplosionQueue.add(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;

        NatureRevive.blockExplosionQueue.add(event.getBlock().getLocation());
    }

    /*
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBurnEvent(BlockBurnEvent event) {
        flagChunk(event.getBlock().getLocation());
        log(event, event.getBlock().getLocation());
    }
    */

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockCookEvent(BlockCookEvent event) {
        if (event.isCancelled())
            return;

        NatureRevive.blockExplosionQueue.add(event.getBlock().getLocation());
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player)
            return;

        if (event.getEntity().getKiller() == null)
            return;

        NatureRevive.blockExplosionQueue.add(event.getEntity().getLocation());
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

            NatureRevive.blockExplosionQueue.add(block.getLocation());
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

            NatureRevive.blockExplosionQueue.add(block.getLocation());
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onBrewEvent(BrewEvent event) {
        NatureRevive.blockExplosionQueue.add(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFurnaceBurnEvent(FurnaceBurnEvent event) {
        NatureRevive.blockExplosionQueue.add(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoadEvent(ChunkLoadEvent e) {
        if (NatureRevive.readonlyConfig.regenerationStrategy.equalsIgnoreCase("passive")) {
            Location location = new ChunkPos(e.getWorld(), e.getChunk().getX(), e.getChunk().getZ()).toLocation();
            PositionInfo positionInfo = NatureRevive.databaseConfig.get(location);

            if (positionInfo == null)
                return;

            if (positionInfo.isOverTTL()) {
                NatureRevive.queue.add(new Task(positionInfo));
                NatureRevive.databaseConfig.unset(positionInfo);
            }
        }
    }

    public static void flagChunk(Location location) {
        if (NatureRevive.readonlyConfig.ignoredWorld.contains(location.getWorld().getName()))
            return;

        if (NatureRevive.residenceAPI != null && !NatureRevive.readonlyConfig.residenceStrictCheck) {
            if (NatureRevive.residenceAPI.getByLoc(location) != null) {
                return;
            }
        }

        if (NatureRevive.griefPreventionAPI != null && !NatureRevive.readonlyConfig.griefPreventionStrictCheck){
            if (NatureRevive.griefPreventionAPI.getClaimAt(location, true, null) != null){
                return;
            }
        }

        if (NatureRevive.griefDefenderAPI != null && !NatureRevive.readonlyConfig.griefDefenderStrictCheck){
            UUID uuid = NatureRevive.griefDefenderAPI.getClaimAt(location).getOwnerUniqueId();
            if (!uuid.equals(emptyUUID)){
                return;
            }
        }

        PositionInfo positionInfo = new PositionInfo(location, NatureRevive.readonlyConfig.ttlDuration);

        NatureRevive.databaseConfig.set(positionInfo);
    }

    private void log(Event event, Location location) {
        if (NatureRevive.residenceAPI != null && !NatureRevive.readonlyConfig.residenceStrictCheck) {
            if (NatureRevive.residenceAPI.getByLoc(location) != null) {
                return;
            }
        }

        if (NatureRevive.griefPreventionAPI != null && !NatureRevive.readonlyConfig.griefPreventionStrictCheck){
            if (NatureRevive.griefPreventionAPI.getClaimAt(location, true, null) != null){
                return;
            }
        }

        if (NatureRevive.griefDefenderAPI != null && !NatureRevive.readonlyConfig.griefDefenderStrictCheck){
            UUID uuid = NatureRevive.griefDefenderAPI.getClaimAt(location).getOwnerUniqueId();
            if (!uuid.equals(emptyUUID)){
                return;
            }
        }

        if (NatureRevive.databaseConfig.get(location) != null)
            return;

        if (NatureRevive.readonlyConfig.debug)
            NatureRevive.logger.info("[DEBUG] " + new PositionInfo(location, 0).toString() + " was flagged by event " + event.getEventName());
    }
}
