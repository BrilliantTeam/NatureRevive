package engineer.skyouo.plugins.naturerevive.config;

import engineer.skyouo.plugins.naturerevive.structs.PositionInfo;
import org.bukkit.Location;


import java.io.IOException;
import java.util.List;

public interface DatabaseConfig {
    void set(PositionInfo positionInfo);

    void unset(PositionInfo positionInfo);

    PositionInfo get(Location location);

    PositionInfo get(PositionInfo positionInfo);

    List<PositionInfo> values();

    void save() throws IOException;

    void close();
}
