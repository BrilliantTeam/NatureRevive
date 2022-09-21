package engineer.skyouo.plugins.naturerevive.config.adapters;

import engineer.skyouo.plugins.naturerevive.structs.PositionInfo;

import java.util.Set;

public interface SQLDatabaseAdapter {
    void massUpdate(Set<PositionInfo> positionInfoSet);

    void massInsert(Set<PositionInfo> positionInfoSet);
}