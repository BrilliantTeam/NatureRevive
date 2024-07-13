package engineer.skyouo.plugins.naturerevive.spigot.commands;

import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.listeners.ObfuscateLootListener;
import engineer.skyouo.plugins.naturerevive.spigot.util.ScheduleUtil;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestRandomizeOreCommand implements CommandExecutor {
    public TestRandomizeOreCommand() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Chunk chunk = ((Player) sender).getLocation().getChunk();
        ScheduleUtil.REGION.runTask(NatureRevivePlugin.instance, chunk, () -> ObfuscateLootListener.randomizeChunkOre(chunk));
        return true;
    }
}
