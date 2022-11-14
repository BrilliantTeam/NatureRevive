package engineer.skyouo.plugins.naturerevive.spigot.commands;

import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BukkitPositionInfo;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class ForceRegenAllCommand implements CommandExecutor {
    private Plugin plugin;

    public ForceRegenAllCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!NatureRevivePlugin.enableRevive) {
            sender.sendMessage(
                    ChatColor.translateAlternateColorCodes(
                            '&', NatureRevivePlugin.readonlyConfig.forceRegenFailedDueRegenStopMessage
                    )
            );
            return true;
        }

        List<BukkitPositionInfo> positionInfos = NatureRevivePlugin.databaseConfig.values();
        for (BukkitPositionInfo positionInfo : positionInfos) {
            positionInfo.setTTL(0);
            NatureRevivePlugin.queue.add(positionInfo);
            NatureRevivePlugin.databaseConfig.unset(positionInfo);
        }

        return true;
    }
}
