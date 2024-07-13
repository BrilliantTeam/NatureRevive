package engineer.skyouo.plugins.naturerevive.spigot.integration.land;

import org.bukkit.Chunk;
import org.bukkit.Location;

public interface ILandPluginIntegration {
    public boolean checkHasLand(Chunk chunk);
    public boolean isInLand(Location location);
}
