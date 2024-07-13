package engineer.skyouo.plugins.naturerevive.spigot.integration.land;

import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.integration.IDependency;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.Collection;

import static engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin.griefPreventionAPI;

public class GriefPreventionIntegration implements ILandPluginIntegration, IDependency {
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
    public String getPluginName() {
        return "GriefPrevention";
    }

    @Override
    public boolean load() {
        Plugin GriefPreventionPlugin = NatureRevivePlugin.instance.getServer().getPluginManager().getPlugin("GriefPrevention");
        NatureRevivePlugin.griefPreventionAPI = GriefPreventionPlugin != null ? GriefPrevention.instance.dataStore : null;
        return griefPreventionAPI != null;
    }

    @Override
    public boolean shouldExitOnFatal() {
        return NatureRevivePlugin.readonlyConfig.griefPreventionStrictCheck;
    }
}
