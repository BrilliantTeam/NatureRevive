package engineer.skyouo.plugins.naturerevive.commands;

import engineer.skyouo.plugins.naturerevive.NatureRevive;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            NatureRevive.readonlyConfig.reloadConfig();
            sender.sendMessage(ChatColor.translateAlternateColorCodes(
                    '&', NatureRevive.readonlyConfig.reloadSuccessMessage
            ));
        } catch (Exception e) {
            sender.sendMessage(
                    ChatColor.translateAlternateColorCodes(
                    '&', NatureRevive.readonlyConfig.reloadFailureMessage
                    )
            );

            e.printStackTrace();
        }
        return true;
    }

}