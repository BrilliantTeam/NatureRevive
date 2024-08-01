package engineer.skyouo.plugins.naturerevive.spigot.commands;

import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.integration.IDependency;
import engineer.skyouo.plugins.naturerevive.spigot.integration.IntegrationUtil;
import engineer.skyouo.plugins.naturerevive.spigot.integration.engine.IEngineIntegration;
import engineer.skyouo.plugins.naturerevive.spigot.managers.ChunkRegeneration;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BukkitPositionInfo;
import engineer.skyouo.plugins.naturerevive.spigot.util.ScheduleUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RegenThisChunkCommand implements CommandExecutor {

    public RegenThisChunkCommand() {

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    ChatColor.translateAlternateColorCodes(
                            '&', "&cYou are not player."
                    )
            );
            return true;
        }

        ScheduleUtil.REGION.runTask(NatureRevivePlugin.instance, player.getLocation(), () -> {
            String engine = NatureRevivePlugin.readonlyConfig.regenerationEngine;
            IEngineIntegration engineIntegration = IntegrationUtil.getRegenEngine();

            if (args.length > 0) {
                NatureRevivePlugin.readonlyConfig.regenerationEngine = args[0];
                engineIntegration = NatureRevivePlugin.integrationManager.getAvailableDependencies(IDependency.Type.ENGINE)
                        .stream()
                        .map(dependency -> (IEngineIntegration) dependency)
                        .filter(IDependency::isEnabled)
                        .findFirst()
                        .orElse(null);
                NatureRevivePlugin.readonlyConfig.regenerationEngine = engine;
            }

            if (engineIntegration == null) {
                sender.sendMessage(
                        ChatColor.translateAlternateColorCodes(
                                '&', "&cNo engine found."
                        )
                );

                return;
            }

            ChunkRegeneration.regenerateChunk(new BukkitPositionInfo(player.getLocation(), 0L), engineIntegration);
        });
        return true;
    }
}
