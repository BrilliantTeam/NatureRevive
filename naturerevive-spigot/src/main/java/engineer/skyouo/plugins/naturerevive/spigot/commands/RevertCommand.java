package engineer.skyouo.plugins.naturerevive.spigot.commands;

import engineer.skyouo.plugins.naturerevive.spigot.managers.ChunkRegeneration;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class RevertCommand implements CommandExecutor {
    private final Plugin plugin;

    public RevertCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        if (args.length < 1) return true;

        Player player = (Player) sender;

        try {
            long time = System.nanoTime();

            Chunk chunk = ChunkRegeneration.revertSnapshot(player.getWorld(), new File("plugins/NatureRevive/snapshots/" + args[0] + ".snapshot"));

            time = System.nanoTime() - time;

            player.sendMessage(ChatColor.GOLD + "" + chunk.toString() + " (" + DecimalFormat.getInstance().format(time / 1000000.0D) + "ms)");
        } catch (IOException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Check console for more details.");
        }
        return true;
    }
}
