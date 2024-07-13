package engineer.skyouo.plugins.naturerevive.spigot.integration.land;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.integration.IDependency;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

import static engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin.griefDefenderAPI;

public class GriefDefenderIntegration implements ILandPluginIntegration, IDependency {
    private static final UUID emptyUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    @Override
    public boolean checkHasLand(Chunk chunk) {
        // always skip check for has land as I didn't find a better way
        // to get claims from chunks
        return true;
    }

    @Override
    public boolean isInLand(Location location) {
        Claim claim = griefDefenderAPI.getClaimAt(location);
        return claim != null && !claim.getUniqueId().equals(emptyUUID);
    }

    @Override
    public String getPluginName() {
        return "GriefDefender";
    }

    @Override
    public boolean load() {
        Plugin GriefDefenderAPI = NatureRevivePlugin.instance.getServer().getPluginManager().getPlugin("GriefDefender");
        NatureRevivePlugin.griefDefenderAPI = GriefDefenderAPI != null ? GriefDefender.getCore() : null;
        return NatureRevivePlugin.griefDefenderAPI != null;
    }

    @Override
    public boolean shouldExitOnFatal() {
        return NatureRevivePlugin.readonlyConfig.griefDefenderStrictCheck;
    }
}
