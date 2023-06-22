package engineer.skyouo.plugins.naturerevive.spigot.commands;

import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.config.adapters.MySQLDatabaseAdapter;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BukkitPositionInfo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Iterator;

public class DebugCommand implements CommandExecutor {

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
}
