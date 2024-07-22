package engineer.skyouo.plugins.naturerevive.spigot.api;

import engineer.skyouo.plugins.naturerevive.spigot.integration.IDependency;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IIntegrationManager {
    boolean init(Plugin plugin);

    @Nullable IDependency getAvailableDependency(String name);
    @Nullable IDependency getAvailableDependency(IDependency.Type type);

    List<IDependency> getAvailableDependencies(IDependency.Type type);

    boolean registerIntegration(Plugin plugin, IDependency dependency);
    boolean unregisterIntegration(Plugin plugin, IDependency dependency);
}
