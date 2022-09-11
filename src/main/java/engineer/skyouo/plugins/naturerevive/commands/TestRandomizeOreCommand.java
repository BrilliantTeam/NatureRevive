package engineer.skyouo.plugins.naturerevive.commands;

import engineer.skyouo.plugins.naturerevive.listeners.ObfuscateLootListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestRandomizeOreCommand implements CommandExecutor {
    public TestRandomizeOreCommand() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ObfuscateLootListener.randomizeChunkOre(((Player) sender).getLocation().getChunk());
        return true;
    }
}
