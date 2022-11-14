package engineer.skyouo.plugins.naturerevive.spigot.commands;

import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ToggleChunkRegenerationCommand  implements CommandExecutor {
    public ToggleChunkRegenerationCommand() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (NatureRevivePlugin.enableRevive) {
            NatureRevivePlugin.suspendedZone.frozen();
        } else {
            NatureRevivePlugin.suspendedZone.resume();
        }

        sender.sendMessage(
                ChatColor.translateAlternateColorCodes(
                        '&', NatureRevivePlugin.enableRevive ? NatureRevivePlugin.readonlyConfig.startChunkRegenerationMessage : NatureRevivePlugin.readonlyConfig.stopChunkRegenerationMessage
                )
        );
        return true;
    }
}
