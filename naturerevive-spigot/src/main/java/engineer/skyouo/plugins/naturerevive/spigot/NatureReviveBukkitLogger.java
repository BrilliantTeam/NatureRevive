package engineer.skyouo.plugins.naturerevive.spigot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * @deprecated - Due to the future cross-platform compatibility,
 * this class has been deprecated, see {@link NatureReviveComponentLogger} for better logging system implementation.
 * */
@Deprecated(forRemoval = true, since = "2.1.0")
public class NatureReviveBukkitLogger {
    public static void ok(String message) {
        Bukkit.getConsoleSender()
                .sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "[NatureRevive/&aOK&r] " + message));
    }

    public static void info(String message) {
        Bukkit.getConsoleSender()
                .sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "[NatureRevive/&bINFO&r] " + message));
    }

    public static void warning(String message) {
        Bukkit.getConsoleSender()
                .sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "[NatureRevive/&eWARNING&r] " + message));
    }

    public static void error(String message) {
        Bukkit.getConsoleSender()
                .sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "[NatureRevive/&cERROR&r] " + message));
    }

    public static void debug(String message) {
        Bukkit.getConsoleSender()
                .sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "[NatureRevive/&7DEBUG&r] " + message));
    }
}
