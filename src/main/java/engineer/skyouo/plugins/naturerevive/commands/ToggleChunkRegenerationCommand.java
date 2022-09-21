package engineer.skyouo.plugins.naturerevive.commands;

import engineer.skyouo.plugins.naturerevive.NatureRevive;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ToggleChunkRegenerationCommand  implements CommandExecutor {
    public ToggleChunkRegenerationCommand() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (NatureRevive.enableRevive) {
            NatureRevive.suspendedZone.frozen();
        } else {
            NatureRevive.suspendedZone.resume();
        }

        sender.sendMessage(
                ChatColor.translateAlternateColorCodes(
                        '&', NatureRevive.enableRevive ? NatureRevive.readonlyConfig.startChunkRegenerationMessage : NatureRevive.readonlyConfig.stopChunkRegenerationMessage
                )
        );
        return true;
    }
}
