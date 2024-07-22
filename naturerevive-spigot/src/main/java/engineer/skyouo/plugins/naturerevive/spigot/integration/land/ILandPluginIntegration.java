package engineer.skyouo.plugins.naturerevive.spigot.integration.land;

import engineer.skyouo.plugins.naturerevive.spigot.integration.IDependency;
import org.bukkit.Chunk;
import org.bukkit.Location;

public interface ILandPluginIntegration extends IDependency {
    boolean checkHasLand(Chunk chunk);
    boolean isInLand(Location location);

    boolean isStrictMode();
}
