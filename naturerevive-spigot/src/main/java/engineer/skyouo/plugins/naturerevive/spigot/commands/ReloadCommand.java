package engineer.skyouo.plugins.naturerevive.spigot.commands;

import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            NatureRevivePlugin.readonlyConfig.reloadConfig();
            NatureRevivePlugin.checkSoftDependPlugins();

            NatureRevivePlugin.taskManager.unregisterTasks();
            NatureRevivePlugin.taskManager.init();

            sender.sendMessage(ChatColor.translateAlternateColorCodes(
                    '&', NatureRevivePlugin.readonlyConfig.reloadSuccessMessage
            ));
        } catch (Exception e) {
            sender.sendMessage(
                    ChatColor.translateAlternateColorCodes(
                    '&', NatureRevivePlugin.readonlyConfig.reloadFailureMessage
                    )
            );

            e.printStackTrace();
        }
        return true;
    }

}