package engineer.skyouo.plugins.naturerevive.spigot.integration;

import engineer.skyouo.plugins.naturerevive.spigot.NatureReviveComponentLogger;
import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.api.IIntegrationManager;
import engineer.skyouo.plugins.naturerevive.spigot.integration.engine.DefaultEngineIntegration;
import engineer.skyouo.plugins.naturerevive.spigot.integration.engine.FAWEIntegration;
import engineer.skyouo.plugins.naturerevive.spigot.integration.land.GriefDefenderIntegration;
import engineer.skyouo.plugins.naturerevive.spigot.integration.land.GriefPreventionIntegration;
import engineer.skyouo.plugins.naturerevive.spigot.integration.land.ResidenceIntegration;
import engineer.skyouo.plugins.naturerevive.spigot.integration.logging.CoreProtectIntegration;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IntegrationManager implements IIntegrationManager {
    private static final Set<IDependency> dependencies = new HashSet<>();
    private static final Set<IDependency> builtinDependencies = new HashSet<>();
    static {
        builtinDependencies.add(new CoreProtectIntegration());
        builtinDependencies.add(new ResidenceIntegration());
        builtinDependencies.add(new GriefDefenderIntegration());
        builtinDependencies.add(new GriefPreventionIntegration());
        builtinDependencies.add(new DefaultEngineIntegration());
        builtinDependencies.add(new FAWEIntegration());
    }

    @Override
    public boolean init(Plugin plugin) {
        for (IDependency dependency : builtinDependencies) {
            boolean result = false;
            try {
                result = registerIntegration(plugin, dependency);
            } catch (Exception ignored) {
                unregisterIntegration(plugin, dependency);
            }

            if (result) {
                NatureReviveComponentLogger.info(
                        "NatureRevive 成功載入 %s 插件的支援項目。",
                        dependency.getPluginName()
                );

                if (!dependency.getType().equals(IDependency.Type.LAND) && !dependency.isEnabled()) {
                    NatureReviveComponentLogger.info(
                            "雖然 NatureRevive 發現了 %s 插件，但對應的功能在 NatureRevive 並未被啟用。",
                            dependency.getPluginName()
                    );

                    unregisterIntegration(plugin, dependency);
                }
            }

            if (!result && dependency.shouldExitOnFatal()) {
                NatureReviveComponentLogger.error(
                        "由於 %s 尚未被載入，且被 NatureRevive 的設置選項依賴，因此無法啟用 NatureRevive。",
                        dependency.getPluginName()
                );

                NatureReviveComponentLogger.warning("建議您在設置中關閉相對應的選項，或安裝對應的插件。");
                return false;
            }
        }

        builtinDependencies.clear();
        return true;
    }

    @Override
    public @Nullable IDependency getAvailableDependency(String name) {
        for (IDependency dependency : dependencies) {
            if (dependency.getPluginName().equals(name))
                return dependency;
        }

        return null;
    }

    @Override
    public @Nullable IDependency getAvailableDependency(IDependency.Type type) {
        for (IDependency dependency : dependencies) {
            if (dependency.getType().equals(type))
                return dependency;
        }

        return null;
    }

    @Override
    public List<IDependency> getAvailableDependencies(IDependency.Type type) {
        List<IDependency> result = new ArrayList<>();

        for (IDependency dependency : dependencies) {
            if (dependency.getType().equals(type))
                result.add(dependency);
        }

        return result;
    }

    @Override
    public boolean registerIntegration(Plugin plugin, IDependency dependency) {
        if (dependencies.contains(dependency))
            return false;

        NatureReviveComponentLogger.debug(
                "Plugin %s tried to register integration of %s.", TextColor.fromHexString("#AAAAAA"), plugin.getName(), dependency.getPluginName()
        );

        if (!dependency.load()) {
            NatureReviveComponentLogger.debug(
                    "Plugin %s failed to register integration of %s.", TextColor.fromHexString("#AAAAAA"), plugin.getName(), dependency.getPluginName()
            );

            return false;
        }

        return dependencies.add(dependency);
    }

    @Override
    public boolean unregisterIntegration(Plugin plugin, IDependency dependency) {
        if (!dependencies.contains(dependency))
            return false;

        NatureReviveComponentLogger.debug(
                "Plugin %s tried to unregister integration of %s.", TextColor.fromHexString("#AAAAAA"), plugin.getName(), dependency.getPluginName()
        );

        return dependencies.remove(dependency);
    }
}
