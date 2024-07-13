package engineer.skyouo.plugins.naturerevive.spigot.structs;

import static engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin.*;

import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.griefdefender.api.claim.Claim;
import engineer.skyouo.plugins.naturerevive.common.structs.PositionInfo;
import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.managers.ChunkRegeneration;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.*;

/*
* The BukkitPositionInfo class is quite different from old PositionInfo class in 1.x version,
* The new class allow chunk position instead of fixed x-z position for reduce class usage.
* */
@SerializableAs("PositionInfo")
public final class BukkitPositionInfo extends PositionInfo implements ConfigurationSerializable {
    private static final UUID emptyUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public BukkitPositionInfo(Location location, long ttl) {
        super(location.getWorld().getName(), location.getChunk().getX(), location.getChunk().getZ(), ttl);
    }

    public BukkitPositionInfo(String worldName, int x, int z, long ttl) {
        super(worldName, x, z, ttl);
    }

    public BukkitPositionInfo(Map<String, Object> map) {
        super((String) map.get("world"), (int) map.get("chunkX"), (int) map.get("chunkZ"), (long) map.get("ttl"));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("world", worldName);
        result.put("chunkX", x);
        result.put("chunkZ", z);
        result.put("ttl", ttl);

        return result;
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(worldName), (x << 4), -64, (z << 4));
    }

    public long getTTL() {
        return ttl;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setTTL(long ttl) {
        this.ttl = ttl;
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
            for (int y = nmsWrapper.getWorldMinHeight(chunk.getWorld()); y < chunk.getWorld().getMaxHeight() - 1; y++) {
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

        return griefDefenderAPI != null && !claimUUIDList.isEmpty();
    }

    public void regenerateChunk() {
        ChunkRegeneration.regenerateChunk(this);
    }

    @Override
    public String toString() {
        return "BukkitPositionInfo{x = " + x + ", z = " + z + ", ttl = " + ttl + "}";
    }
}
