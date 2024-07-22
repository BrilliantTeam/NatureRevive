package engineer.skyouo.plugins.naturerevive.spigot.integration.land;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.api.ResidenceInterface;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.integration.IDependency;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class ResidenceIntegration implements ILandPluginIntegration {
    private static ResidenceInterface residenceAPI;
    @Override
    public boolean checkHasLand(Chunk chunk) {
        List<ClaimedResidence> residences = ((ResidenceManager) residenceAPI).getByChunk(chunk);
        return !residences.isEmpty();
    }

    @Override
    public boolean isInLand(Location location) {
        return residenceAPI.getByLoc(location) != null;
    }

    @Override
    public boolean isStrictMode() {
        return NatureRevivePlugin.readonlyConfig.residenceStrictCheck;
    }

    @Override
    public String getPluginName() {
        return "Residence";
    }

    @Override
    public Type getType() {
        return Type.LAND;
    }

    @Override
    public boolean load() {
        Plugin residencePlugin = NatureRevivePlugin.instance.getServer().getPluginManager().getPlugin("Residence");
        residenceAPI = residencePlugin != null ? ResidenceApi.getResidenceManager() : null;
        return residenceAPI != null;
    }

    @Override
    public boolean isEnabled() {
        return residenceAPI != null;
    }

    @Override
    public boolean shouldExitOnFatal() {
        return isStrictMode();
    }
}
