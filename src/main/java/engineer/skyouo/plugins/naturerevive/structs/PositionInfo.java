package engineer.skyouo.plugins.naturerevive.structs;

import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.griefdefender.api.claim.Claim;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.*;

import static engineer.skyouo.plugins.naturerevive.NatureRevive.*;

@SerializableAs("PositionInfo")
public class PositionInfo implements ConfigurationSerializable {
    private ChunkPos location;
    private long ttl;

    private static UUID emptyUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public PositionInfo(Location location, long ttl) {
        this.location = ChunkPos.fromLocation(location);
        this.ttl = System.currentTimeMillis() + ttl;
    }

    public PositionInfo(ChunkPos chunkPos, long ttl) {
        this.location = chunkPos;
        this.ttl = ttl;
    }

    public PositionInfo(int x, int z, long ttl, String levelName) {
        this.location = new ChunkPos(levelName, x, z);
        this.ttl = ttl;
    }

    public PositionInfo(Map<String, Object> map) {
        this.location = new ChunkPos((String) map.get("world"), (int) map.get("chunkX"), (int) map.get("chunkZ"));
        this.ttl = (long) map.get("ttl");
    }

    public static boolean isResidence(Location location) {
        return residenceAPI != null && ((ResidenceManager) residenceAPI).getByChunk(location.getChunk()).size() != 0;
    }

    public static boolean isGriefPrevention(Location location){
        return griefPreventionAPI != null && griefPreventionAPI.getClaims(location.getChunk().getX(), location.getChunk().getZ()).size() != 0;
    }

    public static boolean isGriefDefender(Location location){
        if (griefDefenderAPI == null)
            return false;

        Chunk chunk = location.getChunk();
        List<UUID> claimUUIDList = new ArrayList<>();
        for (int x = 0; x < 16; x++) {
            for (int y = chunk.getWorld().getMinHeight(); y < chunk.getWorld().getMaxHeight() - 1; y++) {
                for (int z = 0; z < 16; z++) {
                    Location claimLocation = chunk.getBlock(x, y, z).getLocation();
                    Claim claim = griefDefenderAPI.getClaimAt(claimLocation);
                    UUID uuid = claim.getOwnerUniqueId();
                    if (!uuid.equals(emptyUUID)) {
                        UUID claimUUID = claim.getUniqueId();
                        if (!claimUUIDList.contains(claimUUID)){
                            claimUUIDList.add(claimUUID);
                        }
                    }
                }
            }
        }

        return griefDefenderAPI != null && claimUUIDList.size() != 0;
    }

    public boolean isOverTTL() {
        return System.currentTimeMillis() > ttl;
    }

    public Location getLocation() {
        return location.toLocation();
    }

    public ChunkPos getChunkPos() { return location; }

    public long getTTL() {
        return ttl;
    }

    public void setTTL(long ttl) {
        this.ttl = ttl;
    }

    public static PositionInfo fromExistingTask(Location location, long ttl) {
        return new PositionInfo(ChunkPos.fromLocation(location), ttl);
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
