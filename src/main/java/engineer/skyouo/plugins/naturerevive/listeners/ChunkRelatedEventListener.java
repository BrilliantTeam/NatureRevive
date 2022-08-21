package engineer.skyouo.plugins.naturerevive.listeners;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import engineer.skyouo.plugins.naturerevive.NatureRevive;
import engineer.skyouo.plugins.naturerevive.structs.PositionInfo;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;

public class ChunkRelatedEventListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        flagChunk(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        flagChunk(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBurnEvent(BlockBurnEvent event) {
        flagChunk(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockCookEvent(BlockCookEvent event) {
        flagChunk(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeavesDecayEvent(LeavesDecayEvent event) {
        flagChunk(event.getBlock().getLocation());
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }

        if (event.getEntity().getKiller() == null) {
            return;
        }

        flagChunk(event.getEntity().getLocation());
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onBrewEvent(BrewEvent event) {
        flagChunk(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFurnaceBurnEvent(FurnaceBurnEvent event) {
        flagChunk(event.getBlock().getLocation());
    }

    private void flagChunk(Location location) {
        if (ResidenceApi.getResidenceManager() != null) {
            if (ResidenceApi.getResidenceManager().getByLoc(location) != null) {
                return;
            }
        }

        PositionInfo positionInfo = new PositionInfo(location, NatureRevive.readonlyConfig.ttlDay * 86400L * 1000L);

        System.out.println(positionInfo);

        NatureRevive.databaseConfig.set(positionInfo);
    }
}
