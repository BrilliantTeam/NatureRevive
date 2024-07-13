package engineer.skyouo.plugins.naturerevive.spigot.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LootChestRegenEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private Player player;
    private Location location;
    private LocalDateTime dateTime;

    public LootChestRegenEvent(Player player, Location location, LocalDateTime dateTime) {
        this.player = player;
        this.location = location;
        this.dateTime = dateTime;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getLocation() {
        return location;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getDateTimePretty() {
        return DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(dateTime);
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
