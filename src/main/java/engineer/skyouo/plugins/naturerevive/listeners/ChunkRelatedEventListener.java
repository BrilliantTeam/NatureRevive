package engineer.skyouo.plugins.naturerevive.listeners;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import engineer.skyouo.plugins.naturerevive.NatureRevive;
import engineer.skyouo.plugins.naturerevive.structs.ChunkPos;
import engineer.skyouo.plugins.naturerevive.structs.PositionInfo;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;

public class ChunkRelatedEventListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (event.isCancelled())
            return;


        log(event, event.getBlock().getLocation());
        flagChunk(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;

        log(event, event.getBlock().getLocation());
        flagChunk(event.getBlock().getLocation());
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

        log(event, event.getBlock().getLocation());
        flagChunk(event.getBlock().getLocation());
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player)
            return;

        if (event.getEntity().getKiller() == null)
            return;

        log(event, event.getEntity().getLocation());
        flagChunk(event.getEntity().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockExplodeEvent(BlockExplodeEvent event) {
        if (event.isCancelled())
            return;

        for (Block block : event.blockList()) {
            flagChunk(block.getLocation());
            log(event, block.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityExplodeEvent(EntityExplodeEvent event) {
        if (event.isCancelled())
            return;

        for (Block block : event.blockList()) {
            log(event, block.getLocation());
            flagChunk(block.getLocation());
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onBrewEvent(BrewEvent event) {
        log(event, event.getBlock().getLocation());
        flagChunk(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFurnaceBurnEvent(FurnaceBurnEvent event) {
        log(event, event.getBlock().getLocation());
        flagChunk(event.getBlock().getLocation());
    }

    protected static void flagChunk(Location location) {
        if (NatureRevive.residenceApi != null && !NatureRevive.readonlyConfig.residenceStrictCheck) {
            if (NatureRevive.residenceApi.getByLoc(location) != null) {
                return;
            }
        }

        PositionInfo positionInfo = new PositionInfo(location, NatureRevive.readonlyConfig.ttlDuration);

        NatureRevive.databaseConfig.set(positionInfo);
    }

    private void log(Event event, Location location) {
        if (NatureRevive.residenceApi != null && !NatureRevive.readonlyConfig.residenceStrictCheck) {
            if (NatureRevive.residenceApi.getByLoc(location) != null) {
                return;
            }
        }

        if (NatureRevive.databaseConfig.get(location) != null)
            return;

        if (NatureRevive.readonlyConfig.debug)
            NatureRevive.logger.info("[DEBUG] " + new PositionInfo(location, 0).toString() + " was flagged by event " + event.getEventName());
    }
}
