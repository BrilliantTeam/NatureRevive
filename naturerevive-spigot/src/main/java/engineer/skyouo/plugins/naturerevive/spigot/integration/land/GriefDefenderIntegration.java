package engineer.skyouo.plugins.naturerevive.spigot.integration.land;

import com.griefdefender.api.Core;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class GriefDefenderIntegration implements ILandPluginIntegration {
    private static Core griefDefenderAPI;
    private static final UUID emptyUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    @Override
    public boolean checkHasLand(Chunk chunk) {
        // todo: improve the check
        return griefDefenderAPI.getAllClaims()
                .stream().anyMatch(claim -> claim.getChunkPositions().stream().anyMatch(pos -> pos.getX() == chunk.getX() && pos.getZ() == chunk.getZ()));
    }

    @Override
    public boolean isInLand(Location location) {
        Claim claim = griefDefenderAPI.getClaimAt(location);
        return claim != null && !claim.getUniqueId().equals(emptyUUID);
    }

    @Override
    public boolean isStrictMode() {
        return NatureRevivePlugin.readonlyConfig.griefDefenderStrictCheck;
    }

    @Override
    public String getPluginName() {
        return "GriefDefender";
    }

    @Override
    public Type getType() {
        return Type.LAND;
    }

    @Override
    public boolean load() {
        Plugin GriefDefenderAPI = NatureRevivePlugin.instance.getServer().getPluginManager().getPlugin("GriefDefender");
        griefDefenderAPI = GriefDefenderAPI != null ? GriefDefender.getCore() : null;
        return griefDefenderAPI != null;
    }

    @Override
    public boolean isEnabled() {
        return griefDefenderAPI != null;
    }

    @Override
    public boolean shouldExitOnFatal() {
        return isStrictMode();
    }
}
