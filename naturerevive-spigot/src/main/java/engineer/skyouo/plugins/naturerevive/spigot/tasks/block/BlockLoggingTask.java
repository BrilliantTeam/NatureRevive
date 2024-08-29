package engineer.skyouo.plugins.naturerevive.spigot.tasks.block;

import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.integration.IntegrationUtil;
import engineer.skyouo.plugins.naturerevive.spigot.integration.logging.ILoggingIntegration;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BlockDataChangeWithPos;
import engineer.skyouo.plugins.naturerevive.spigot.tasks.Task;
import engineer.skyouo.plugins.naturerevive.spigot.util.ScheduleUtil;

import static engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin.blockDataChangeWithPos;

public class BlockLoggingTask implements Task {
    @Override
    public void run() {
        if (blockDataChangeWithPos.hasNext()) {
            for (int i = 0; i < 200 && blockDataChangeWithPos.hasNext(); i++) {
                BlockDataChangeWithPos blockDataChangeWithPosObject = blockDataChangeWithPos.pop();

                ScheduleUtil.REGION.runTask(NatureRevivePlugin.instance, blockDataChangeWithPosObject.getLocation(), () -> {
                    synchronized (blockDataChangeWithPosObject) {
                        try {
                            for (ILoggingIntegration integration : IntegrationUtil.getLoggingIntegrations()) {
                                if (!integration.isEnabled()) continue;

                                integration.log(blockDataChangeWithPosObject);
                            }
                        } catch (IllegalStateException e) {
                            if (e.getMessage().contains("asynchronous")) {
                                blockDataChangeWithPosObject.addFailedTime();
                                if (blockDataChangeWithPosObject.getFailedTime() > (blockDataChangeWithPos.size() > 1 ? 120 : 45)) {
                                    blockDataChangeWithPos.add(blockDataChangeWithPosObject);
                                }
                            }

                            e.printStackTrace();
                        }
                    }
                });
            }
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
        return 2L;
    }
}
