package engineer.skyouo.plugins.naturerevive.spigot.integration.logging;

import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BlockDataChangeWithPos;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.plugin.Plugin;

import static engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin.readonlyConfig;

public class CoreProtectIntegration implements ILoggingIntegration {
    private static CoreProtectAPI coreProtectAPI;
    @Override
    public String getPluginName() {
        return "CoreProtect";
    }

    @Override
    public Type getType() {
        return Type.LOGGING;
    }

    @Override
    public boolean load() {
        Plugin coreProtectPlugin = NatureRevivePlugin.instance.getServer().getPluginManager().getPlugin("CoreProtect");
        coreProtectAPI = coreProtectPlugin != null ? CoreProtect.getInstance().getAPI() : null;
        return coreProtectAPI != null;
    }

    @Override
    public boolean isEnabled() {
        return coreProtectAPI != null && readonlyConfig.coreProtectLogging;
    }

    @Override
    public boolean shouldExitOnFatal() {
        return readonlyConfig.coreProtectLogging;
    }

    @Override
    public boolean log(BlockDataChangeWithPos data) {
        if (data.getType().equals(BlockDataChangeWithPos.Type.REMOVAL) || data.getType().equals(BlockDataChangeWithPos.Type.REPLACE))
            coreProtectAPI.logRemoval(readonlyConfig.coreProtectUserName, data.getLocation(), data.getOldBlockData().getMaterial(), data.getOldBlockData());

        if (data.getType().equals(BlockDataChangeWithPos.Type.PLACEMENT) || data.getType().equals(BlockDataChangeWithPos.Type.REPLACE))
            coreProtectAPI.logPlacement(readonlyConfig.coreProtectUserName, data.getLocation(), data.getNewBlockData().getMaterial(), data.getNewBlockData());

        return true;
    }
}
