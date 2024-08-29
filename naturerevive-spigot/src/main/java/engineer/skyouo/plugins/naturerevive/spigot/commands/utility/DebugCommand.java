package engineer.skyouo.plugins.naturerevive.spigot.commands.utility;

import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.commands.SubCommand;
import engineer.skyouo.plugins.naturerevive.spigot.config.adapters.MySQLDatabaseAdapter;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BukkitPositionInfo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class DebugCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("============================");
        sender.sendMessage("Queue tasks: ");
        for (Iterator<BukkitPositionInfo> it = NatureRevivePlugin.queue.iterator(); it.hasNext(); ) {
            BukkitPositionInfo task = it.next();
            sender.sendMessage(task.getLocation().toString() + " - " + task.getTTL());
        }

        sender.sendMessage(" ");

        sender.sendMessage("Database tasks: ");
        for (BukkitPositionInfo positionInfo : NatureRevivePlugin.databaseConfig.values()) {
            sender.sendMessage(positionInfo.getLocation().toString() + " - " + positionInfo.getTTL());
        }

        sender.sendMessage(" ");
        sender.sendMessage("Database no cache tasks: ");
        try {
            for (BukkitPositionInfo positionInfo : NatureRevivePlugin.databaseConfig.values()) {
                BukkitPositionInfo positionInfoNoCache = ((MySQLDatabaseAdapter) NatureRevivePlugin.databaseConfig).getNoCache(positionInfo);
                sender.sendMessage(positionInfoNoCache.getLocation().toString() + " - " + positionInfoNoCache.getTTL());
            }
        } catch (Exception ignored) {}

        sender.sendMessage("Time now is: " + System.currentTimeMillis());
        sender.sendMessage("============================");
        return true;
    }

    @Override
    public String getName() {
        return "debug";
    }

    @Override
    public boolean hasPermissionToExecute(CommandSender sender) {
        return sender.hasPermission("naturerevive.navdebug");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
