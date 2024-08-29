package engineer.skyouo.plugins.naturerevive.spigot.tasks.regen;

import engineer.skyouo.plugins.naturerevive.spigot.listeners.ChunkRelatedEventListener;
import engineer.skyouo.plugins.naturerevive.spigot.tasks.Task;

import static engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin.blockQueue;
import static engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin.readonlyConfig;

public class RegenDelayedFlagChunkTask implements Task {
    @Override
    public void run() {
        for (int i = 0; i < readonlyConfig.blockProcessingAmountPerProcessing && blockQueue.hasNext(); i++) {
            ChunkRelatedEventListener.flagChunk(blockQueue.pop());
        }
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public long getDelay() {
        return 20L;
    }

    @Override
    public long getRepeatTime() {
        return readonlyConfig.blockProcessingTick;
    }
}
