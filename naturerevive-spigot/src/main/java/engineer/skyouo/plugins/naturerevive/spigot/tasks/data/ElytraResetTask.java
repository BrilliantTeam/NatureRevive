package engineer.skyouo.plugins.naturerevive.spigot.tasks.data;

import engineer.skyouo.plugins.naturerevive.spigot.NatureReviveComponentLogger;
import engineer.skyouo.plugins.naturerevive.spigot.managers.features.ElytraRegeneration;
import engineer.skyouo.plugins.naturerevive.spigot.tasks.Task;

public class ElytraResetTask implements Task {
    @Override
    public void run() {
        if (ElytraRegeneration.checkResetLimitTime()) {
            NatureReviveComponentLogger.info("The elytra regeneration limit has been reset.");
        }
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public long getDelay() {
        return 20L;
    }

    @Override
    public long getRepeatTime() {
        return 600L;
    }
}
