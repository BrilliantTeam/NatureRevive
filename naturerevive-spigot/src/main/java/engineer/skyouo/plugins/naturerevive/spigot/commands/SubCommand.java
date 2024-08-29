package engineer.skyouo.plugins.naturerevive.spigot.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public interface SubCommand extends TabExecutor {
    String getName();

    boolean hasPermissionToExecute(CommandSender sender);
}
