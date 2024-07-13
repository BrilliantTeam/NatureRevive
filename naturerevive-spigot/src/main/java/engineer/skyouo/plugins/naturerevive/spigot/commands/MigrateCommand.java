package engineer.skyouo.plugins.naturerevive.spigot.commands;

import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.config.DatabaseConfig;
import engineer.skyouo.plugins.naturerevive.spigot.config.adapters.MySQLDatabaseAdapter;
import engineer.skyouo.plugins.naturerevive.spigot.config.adapters.SQLDatabaseAdapter;
import engineer.skyouo.plugins.naturerevive.spigot.config.adapters.SQLiteDatabaseAdapter;
import engineer.skyouo.plugins.naturerevive.spigot.config.adapters.YamlDatabaseAdapter;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BukkitPositionInfo;
import engineer.skyouo.plugins.naturerevive.spigot.structs.SQLCommand;
import engineer.skyouo.plugins.naturerevive.spigot.util.ScheduleUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin.*;

public class MigrateCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length != 1) {
            commandSender.sendMessage(
                    ChatColor.translateAlternateColorCodes('&',
                            "[NatureRevive] &cUsage: /navmigrate <yaml/sqlite/mysql>"
                    )
            );
            return true;
        }

        if (!List.of("yaml", "sqlite", "mysql").contains(strings[0])) {
            commandSender.sendMessage(
                    ChatColor.translateAlternateColorCodes('&',
                            "[NatureRevive] &cUsage: /navmigrate <yaml/sqlite/mysql>"
                    )
            );
            return true;
        }


        ScheduleUtil.GLOBAL.runTaskAsynchronously(instance, () -> {

            try {
                DatabaseConfig config = getDatabase(strings[0]);

                if (config.getClass().equals(databaseConfig.getClass())) {
                    commandSender.sendMessage(
                            ChatColor.translateAlternateColorCodes('&',
                                    "[NatureRevive] &cThere's no need to migrate to the same database storage type!"
                            )
                    );
                    return;
                }

                commandSender.sendMessage(
                        ChatColor.translateAlternateColorCodes('&',
                                String.format(
                                        "[NatureRevive] &bBegin to migrate to %s, the old data will be still available.",
                                        strings[0]
                                )
                        )
                );

                for (BukkitPositionInfo data : NatureRevivePlugin.databaseConfig.values()) {
                    config.set(data);
                }

                if (config instanceof SQLDatabaseAdapter adapter) {
                    List<SQLCommand> sqlCommands = new ArrayList<>();

                    while (sqlCommandQueue.hasNext()) {
                        sqlCommands.add(sqlCommandQueue.pop());
                    }

                    adapter.massExecute(sqlCommands);
                }

                commandSender.sendMessage(
                        ChatColor.translateAlternateColorCodes('&',
                                "[NatureRevive] &bMigrate successfully, please restart the plugin and switch the storage method in config.yml."
                        )
                );
            } catch (Exception ex) {
                ex.printStackTrace();

                commandSender.sendMessage(
                        ChatColor.translateAlternateColorCodes('&',
                                "[NatureRevive] &cFailed to migrate, please see console, and edit your config."
                        )
                );
            }
        });
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of("yaml", "sqlite", "mysql");
    }

    private DatabaseConfig getDatabase(String db) {
        return switch (db) {
            case "sqlite" -> new SQLiteDatabaseAdapter();
            case "mysql" -> new MySQLDatabaseAdapter();
            default -> new YamlDatabaseAdapter();
        };
    }
}
