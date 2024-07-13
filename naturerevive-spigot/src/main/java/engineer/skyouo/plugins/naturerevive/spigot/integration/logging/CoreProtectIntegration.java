package engineer.skyouo.plugins.naturerevive.spigot.integration.logging;

import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.integration.IDependency;
import net.coreprotect.CoreProtect;
import org.bukkit.plugin.Plugin;

public class CoreProtectIntegration implements IDependency {
    @Override
    public String getPluginName() {
        return "CoreProtect";
    }

    @Override
    public boolean load() {
        Plugin coreProtectPlugin = NatureRevivePlugin.instance.getServer().getPluginManager().getPlugin("CoreProtect");
        NatureRevivePlugin.coreProtectAPI = coreProtectPlugin != null ? CoreProtect.getInstance().getAPI() : null;
        return NatureRevivePlugin.coreProtectAPI != null;
    }

    @Override
    public boolean shouldExitOnFatal() {
        return NatureRevivePlugin.readonlyConfig.coreProtectLogging;
    }
}
