package engineer.skyouo.plugins.naturerevive.spigot.structs;

public class SQLCommand {
    private BukkitPositionInfo bukkitPositionInfo;
    private Type type;

    public SQLCommand(BukkitPositionInfo positionInfo, Type commandType) {
        bukkitPositionInfo = positionInfo;
        type = commandType;
    }

    public BukkitPositionInfo getBukkitPositionInfo() {
        return bukkitPositionInfo;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        INSERT, UPDATE, DELETE
    }
}
