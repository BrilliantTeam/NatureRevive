package engineer.skyouo.plugins.naturerevive.spigot.events;

import org.bukkit.Chunk;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChunkRegenEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private Chunk chunk;
    private LocalDateTime dateTime;

    public ChunkRegenEvent(Chunk chunk, LocalDateTime dateTime) {
        this.chunk = chunk;
        this.dateTime = dateTime;
    }

    public Chunk getChunk() {
        return chunk;
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
