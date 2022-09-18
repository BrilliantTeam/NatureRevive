package engineer.skyouo.plugins.naturerevive.structs;

import com.bekvon.bukkit.residence.protection.ResidenceManager;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.LinkedHashMap;
import java.util.Map;

import static engineer.skyouo.plugins.naturerevive.NatureRevive.GriefPreventionAPI;
import static engineer.skyouo.plugins.naturerevive.NatureRevive.residenceApi;

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
        return residenceApi != null && ((ResidenceManager) residenceApi).getByChunk(location.getChunk()).size() != 0;
    }

    public static boolean isGriefPrevention(Location location){
        return GriefPreventionAPI != null && GriefPreventionAPI.getClaims(location.getChunk().getX(), location.getChunk().getZ()).size() != 0;
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

    public void setTTL(long ttl) {
        this.ttl = ttl;
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
