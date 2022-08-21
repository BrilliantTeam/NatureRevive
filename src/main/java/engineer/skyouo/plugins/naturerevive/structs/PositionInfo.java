package engineer.skyouo.plugins.naturerevive.structs;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.api.ResidenceInterface;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.LinkedHashMap;
import java.util.Map;

@SerializableAs("PositionInfo")
public class PositionInfo implements ConfigurationSerializable {
    private ChunkPos location;
    private long ttl;

    public PositionInfo(Location location, long ttl) {
        this.location = ChunkPos.fromLocation(location);
        this.ttl = System.currentTimeMillis() + ttl;
    }

    public PositionInfo(Map<String, Object> map) {
        this.location = new ChunkPos((String) map.get("world"), (int) map.get("chunkX"), (int) map.get("chunkZ"));
        this.ttl = (long) map.get("ttl");
    }

    public static boolean isResidence(Location location) {
        ResidenceInterface residenceManager = ResidenceApi.getResidenceManager();

        return residenceManager != null && residenceManager.getByLoc(location) != null;
    }

    public boolean isOverTTL() {
        return System.currentTimeMillis() > ttl;
    }

    public Location getLocation() {
        return location.toLocation();
    }

    public long getTTL() {
        return ttl;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("world", location.world.getName());
        result.put("chunkX", location.chunkX);
        result.put("chunkZ", location.chunkZ);
        result.put("ttl", ttl);
        return result;
    }

    @Override
    public String toString() {
        return location.toString() + ":" + ttl;
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }
}
