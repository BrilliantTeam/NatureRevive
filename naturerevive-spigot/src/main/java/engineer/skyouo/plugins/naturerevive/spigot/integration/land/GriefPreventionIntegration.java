package engineer.skyouo.plugins.naturerevive.spigot.integration.land;

import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.Collection;

public class GriefPreventionIntegration implements ILandPluginIntegration {
    private static DataStore griefPreventionAPI;
    @Override
    public boolean checkHasLand(Chunk chunk) {
        Collection<Claim> griefPrevention = griefPreventionAPI.getClaims(chunk.getX(), chunk.getZ());
        return !griefPrevention.isEmpty();
    }

    @Override
    public boolean isInLand(Location location) {
        return griefPreventionAPI.getClaimAt(location, true, null) != null;
    }

    @Override
    public boolean isStrictMode() {
        return NatureRevivePlugin.readonlyConfig.griefPreventionStrictCheck;
    }

    @Override
    public String getPluginName() {
        return "GriefPrevention";
    }

    @Override
    public Type getType() {
        return Type.LAND;
    }

    @Override
    public boolean load() {
        Plugin GriefPreventionPlugin = NatureRevivePlugin.instance.getServer().getPluginManager().getPlugin("GriefPrevention");
        griefPreventionAPI = GriefPreventionPlugin != null ? GriefPrevention.instance.dataStore : null;
        return griefPreventionAPI != null;
    }

    @Override
    public boolean isEnabled() {
        return griefPreventionAPI != null;
    }

    @Override
    public boolean shouldExitOnFatal() {
        return isStrictMode();
    }
}
