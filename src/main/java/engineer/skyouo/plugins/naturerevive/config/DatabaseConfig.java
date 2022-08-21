package engineer.skyouo.plugins.naturerevive.config;

import engineer.skyouo.plugins.naturerevive.structs.PositionInfo;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
public class DatabaseConfig {
    private final File file = new File("plugins/NatureRevive/database.yml");

    private final YamlConfiguration configuration;

    public DatabaseConfig() {
        new File("plugins/NatureRevive").mkdirs();
        configuration = YamlConfiguration.loadConfiguration(file);
        System.out.println(configuration);
    }

    public void set(PositionInfo positionInfo) {
        configuration.set(formatLocation(positionInfo.getLocation()), positionInfo);
    }

    public void unset(PositionInfo positionInfo) {
        configuration.set(formatLocation(positionInfo.getLocation()), null);
    }

    public PositionInfo get(Location location) {
        return (PositionInfo) configuration.get(formatLocation(location));
    }

    public PositionInfo get(PositionInfo positionInfo) {
        return (PositionInfo) configuration.get(formatLocation(positionInfo.getLocation()), positionInfo);
    }

    public List<PositionInfo> values() {
        ArrayList<PositionInfo> positionInfos = new ArrayList<>();
        Set<String> keys = configuration.getKeys(false);

        for (String key : keys) {
            PositionInfo positionInfo = (PositionInfo) configuration.get(key);
            positionInfos.add(positionInfo);
        }

        System.out.println(positionInfos);

        return positionInfos;
    }

    public void save() throws IOException {
        configuration.save(file);
    }

    private String formatLocation(Location location) {
        return new String(Base64.getEncoder().encode((location.getWorld().getName() + "|" + location.getChunk().getX() + "|" + location.getChunk().getZ()).getBytes()));
    }
}
