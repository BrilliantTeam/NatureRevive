package engineer.skyouo.plugins.naturerevive.spigot.integration;

import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.integration.land.GriefDefenderIntegration;
import engineer.skyouo.plugins.naturerevive.spigot.integration.land.GriefPreventionIntegration;
import engineer.skyouo.plugins.naturerevive.spigot.integration.land.ILandPluginIntegration;
import engineer.skyouo.plugins.naturerevive.spigot.integration.land.ResidenceIntegration;

public class IntegrationUtil {
    private static final ResidenceIntegration residenceIntegration = new ResidenceIntegration();
    private static final GriefPreventionIntegration griefPreventionIntegration = new GriefPreventionIntegration();
    private static final GriefDefenderIntegration griefDefenderIntegration = new GriefDefenderIntegration();

    public static ILandPluginIntegration pickLandPluginIntegration() {
        if (NatureRevivePlugin.residenceAPI != null && NatureRevivePlugin.readonlyConfig.residenceStrictCheck)
            return residenceIntegration;

        if (NatureRevivePlugin.griefPreventionAPI != null && NatureRevivePlugin.readonlyConfig.griefPreventionStrictCheck)
            return griefPreventionIntegration;

        if (NatureRevivePlugin.griefDefenderAPI != null && NatureRevivePlugin.readonlyConfig.griefDefenderStrictCheck)
            return griefDefenderIntegration;

        return null;
    }
}
