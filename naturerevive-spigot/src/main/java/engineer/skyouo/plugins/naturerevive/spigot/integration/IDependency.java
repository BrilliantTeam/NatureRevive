package engineer.skyouo.plugins.naturerevive.spigot.integration;

public interface IDependency {
    String getPluginName();
    Type getType();
    boolean load();
    boolean isEnabled();
    boolean shouldExitOnFatal();

    public enum Type {
        LOGGING, ENGINE, LAND
    }
}
