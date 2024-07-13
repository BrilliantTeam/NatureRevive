package engineer.skyouo.plugins.naturerevive.spigot.integration;

public interface IDependency {
    String getPluginName();
    boolean load();
    boolean shouldExitOnFatal();
}
