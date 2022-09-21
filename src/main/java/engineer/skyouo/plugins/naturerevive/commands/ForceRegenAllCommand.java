package engineer.skyouo.plugins.naturerevive.commands;

import engineer.skyouo.plugins.naturerevive.NatureRevive;
import engineer.skyouo.plugins.naturerevive.manager.Task;
import engineer.skyouo.plugins.naturerevive.structs.PositionInfo;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.List;

import static engineer.skyouo.plugins.naturerevive.NatureRevive.databaseConfig;
import static engineer.skyouo.plugins.naturerevive.NatureRevive.queue;

public class ForceRegenAllCommand implements CommandExecutor {
    private Plugin plugin;

    public ForceRegenAllCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!NatureRevive.enableRevive) {
            sender.sendMessage(
                    ChatColor.translateAlternateColorCodes(
                            '&', NatureRevive.readonlyConfig.forceRegenFailedDueRegenStopMessage
                    )
            );
            return true;
        }

        List<PositionInfo> positionInfos = databaseConfig.values();
        for (PositionInfo positionInfo : positionInfos) {
            positionInfo.setTTL(0);
            queue.add(new Task(positionInfo));
            databaseConfig.unset(positionInfo);
        }

        return true;
    }
}
