package engineer.skyouo.plugins.naturerevive.spigot.integration.engine;

import engineer.skyouo.plugins.naturerevive.spigot.integration.IDependency;
import org.bukkit.Chunk;
import org.bukkit.plugin.Plugin;

public interface IEngineIntegration extends IDependency {
    void regenerateChunk(Plugin plugin, Chunk chunk, Runnable postTask);
}
