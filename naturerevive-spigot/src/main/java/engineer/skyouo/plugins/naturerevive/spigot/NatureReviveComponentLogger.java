package engineer.skyouo.plugins.naturerevive.spigot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;

public class NatureReviveComponentLogger {
    public static void ok(String message) {
        ok(message, TextColor.fromHexString("#FFFFFF"));
    }

    public static void ok(String message, TextColor color) {
        Bukkit.getConsoleSender()
                .sendMessage(
                        HeadComponentBuilder.build("OK", TextColor.fromHexString("#55FF55"))
                                .append(Component.text(" "))
                                .append(Component.text(message, color))
                );
    }

    public static void ok(String message, Object... args) {
        ok(String.format(message, args));
    }

    public static void ok(String message, TextColor color, Object... args) {
        ok(String.format(message, args), color);
    }

    public static void info(String message) {
        info(message, TextColor.fromHexString("#FFFFFF"));
    }

    public static void info(String message, TextColor color) {
        Bukkit.getConsoleSender()
                .sendMessage(
                        HeadComponentBuilder.build("INFO", TextColor.fromHexString("#55FFFF"))
                                .append(Component.text(" "))
                                .append(Component.text(message, color))
                );
    }

    public static void info(String message, Object... args) {
        info(String.format(message, args));
    }

    public static void info(String message, TextColor color, Object... args) {
        info(String.format(message, args), color);
    }

    public static void warning(String message) {
        warning(message, TextColor.fromHexString("#FFFFFF"));
    }

    public static void warning(String message, TextColor color) {
        Bukkit.getConsoleSender()
                .sendMessage(
                        HeadComponentBuilder.build("WARNING", TextColor.fromHexString("#FFFF55"))
                                .append(Component.text(" "))
                                .append(Component.text(message, color))
                );
    }

    public static void warning(String message, Object... args) {
        warning(String.format(message, args));
    }

    public static void warning(String message, TextColor color, Object... args) {
        warning(String.format(message, args), color);
    }

    public static void error(String message) {
        error(message, TextColor.fromHexString("#FFFFFF"));
    }

    public static void error(String message, TextColor color) {
        Bukkit.getConsoleSender()
                .sendMessage(
                        HeadComponentBuilder.build("ERROR", TextColor.fromHexString("#FF5555"))
                                .append(Component.text(" "))
                                .append(Component.text(message, color))
                );
    }

    public static void error(String message, Object... args) {
        error(String.format(message, args));
    }

    public static void error(String message, TextColor color, Object... args) {
        error(String.format(message, args), color);
    }

    public static void debug(String message) {
        debug(message, TextColor.fromHexString("#FFFFFF"));
    }

    public static void debug(String message, TextColor color) {
        if (!NatureRevivePlugin.readonlyConfig.debug)
            return;

        Bukkit.getConsoleSender()
                .sendMessage(
                        HeadComponentBuilder.build("DEBUG", TextColor.fromHexString("#AAAAAA"))
                                .append(Component.text(" "))
                                .append(Component.text(message, color))
                );
    }

    public static void debug(String message, Object... args) {
        debug(String.format(message, args));
    }

    public static void debug(String message, TextColor color, Object... args) {
        debug(String.format(message, args), color);
    }

    public static void log(Component header, Component body) {
        Bukkit.getConsoleSender()
                .sendMessage(
                        header
                                .append(Component.text(" "))
                                .append(body)
                );
    }

    public static class HeadComponentBuilder {
        public static Component build(String tag, TextColor tagColor) {
            return Component.text("[NatureRevive/")
                    .append(Component.text(tag, tagColor))
                    .append(Component.text("]"));
        }

        public static Component build(String feature, String tag, TextColor tagColor) {
            return Component.text("[NatureRevive/")
                    .append(Component.text(feature))
                    .append(Component.text("/"))
                    .append(Component.text(tag, tagColor))
                    .append(Component.text("]"));
        }

        public static Component build(String feature, TextColor featureColor, String tag, TextColor tagColor) {
            return Component.text("[NatureRevive/")
                    .append(Component.text(feature, featureColor))
                    .append(Component.text("/"))
                    .append(Component.text(tag, tagColor))
                    .append(Component.text("]"));
        }
    }
}
