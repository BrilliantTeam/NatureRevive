package engineer.skyouo.plugins.naturerevive.spigot.integration;

import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.integration.engine.IEngineIntegration;
import engineer.skyouo.plugins.naturerevive.spigot.integration.land.ILandPluginIntegration;
import engineer.skyouo.plugins.naturerevive.spigot.integration.logging.ILoggingIntegration;

import java.util.List;

public class IntegrationUtil {
    private static List<ILandPluginIntegration> landPluginIntegrations;
    private static List<ILoggingIntegration> loggingIntegrations;
    private static IEngineIntegration engineIntegration;

    public static void reloadCache() {
        landPluginIntegrations = NatureRevivePlugin.integrationManager.getAvailableDependencies(IDependency.Type.LAND)
                .stream()
                .map(dependency -> (ILandPluginIntegration) dependency)
                .toList();

        loggingIntegrations = NatureRevivePlugin.integrationManager.getAvailableDependencies(IDependency.Type.LOGGING)
                .stream()
                .map(dependency -> (ILoggingIntegration) dependency)
                .toList();

        engineIntegration = NatureRevivePlugin.integrationManager.getAvailableDependencies(IDependency.Type.ENGINE)
                .stream()
                .map(dependency -> (IEngineIntegration) dependency)
                .findFirst().orElse(null);
    }

    public static List<ILandPluginIntegration> getLandIntegrations() {
        return landPluginIntegrations;
    }

    public static List<ILoggingIntegration> getLoggingIntegrations() {
        return loggingIntegrations;
    }

    public static IEngineIntegration getRegenEngine() {
        return engineIntegration;
    }

    public static boolean hasValidLoggingIntegration() {
        return !loggingIntegrations.isEmpty() && loggingIntegrations.stream().anyMatch(IDependency::isEnabled);
    }
}
