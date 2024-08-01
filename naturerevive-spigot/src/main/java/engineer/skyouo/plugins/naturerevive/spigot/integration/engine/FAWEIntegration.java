package engineer.skyouo.plugins.naturerevive.spigot.integration.engine;

import engineer.skyouo.plugins.naturerevive.spigot.NatureReviveComponentLogger;
import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.managers.FaweImplRegeneration;
import engineer.skyouo.plugins.naturerevive.spigot.util.ScheduleUtil;
import org.bukkit.Chunk;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public class FAWEIntegration implements IEngineIntegration {
    @Override
    public String getPluginName() {
        return "FastAsyncWorldEdit";
    }

    @Override
    public Type getType() {
        return Type.ENGINE;
    }

    @Override
    public boolean load() {
        Plugin plugin = NatureRevivePlugin.instance.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit");

        if (plugin != null) {
            String version = plugin.getDescription().getVersion().split(";")[0];
            List<Integer> num = Arrays.stream(version.split("\\."))
                    .map(Integer::valueOf)
                    .toList();

            if (num.get(0) > 2 || (num.get(0) == 2 && num.get(1) > 9) || (num.get(0) == 2 && num.get(1) == 9 && num.get(2) > 2)) {
                NatureReviveComponentLogger.warning("當前版本的 NatureRevive 暫不兼容 FastAsyncWorldEdit 2.9.2 以上的版本。");
                NatureReviveComponentLogger.warning("若想使用 FAWE 重生引擎，建議安裝 FastAsyncWorldEdit 2.9.2 版本。");
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean isEnabled() {
        return NatureRevivePlugin.readonlyConfig.regenerationEngine.equalsIgnoreCase("fawe");
    }

    @Override
    public boolean shouldExitOnFatal() {
        return NatureRevivePlugin.readonlyConfig.regenerationEngine.equalsIgnoreCase("fawe");
    }

    @Override
    public void regenerateChunk(Plugin plugin, Chunk chunk, Runnable postTask) {
        ScheduleUtil.GLOBAL.runTaskAsynchronously(plugin, () -> {
            FaweImplRegeneration.regenerate(chunk, false, () -> {
                ScheduleUtil.REGION.runTask(plugin, chunk, postTask);
            });
        });
    }
}
