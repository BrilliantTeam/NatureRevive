package engineer.skyouo.plugins.naturerevive.commands;

import engineer.skyouo.plugins.naturerevive.NatureRevive;
import engineer.skyouo.plugins.naturerevive.config.adapters.MySQLDatabaseAdapter;
import engineer.skyouo.plugins.naturerevive.manager.Task;
import engineer.skyouo.plugins.naturerevive.structs.PositionInfo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Iterator;

public class DebugCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("============================");
        sender.sendMessage("Queue tasks: ");
        for (Iterator<Task> it = NatureRevive.queue.iterator(); it.hasNext(); ) {
            Task task = it.next();
            sender.sendMessage(task.getLocation().toString() + " - " + task.getTTL());
        }

        sender.sendMessage(" ");

        sender.sendMessage("Database tasks: ");
        for (PositionInfo positionInfo : NatureRevive.databaseConfig.values()) {
            sender.sendMessage(positionInfo.getLocation().toString() + " - " + positionInfo.getTTL());
        }

        sender.sendMessage(" ");
        sender.sendMessage("Database no cache tasks: ");
        for (PositionInfo positionInfo : NatureRevive.databaseConfig.values()) {
            PositionInfo positionInfoNoCache = ((MySQLDatabaseAdapter) NatureRevive.databaseConfig).getNoCache(positionInfo);
            sender.sendMessage(positionInfoNoCache.getLocation().toString() + " - " + positionInfoNoCache.getTTL());
        }

        sender.sendMessage("Time now is: " + System.currentTimeMillis());
        sender.sendMessage("============================");
        return true;
    }
}
