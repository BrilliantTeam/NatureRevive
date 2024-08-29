package engineer.skyouo.plugins.naturerevive.spigot.commands;

import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@ApiStatus.Experimental
public class NatureReviveMainCommand implements TabExecutor {
    private final Set<SubCommand> subCommands = new HashSet<>();

    public NatureReviveMainCommand() {

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1)
            return showHelpMessage(sender);

        SubCommand subCommand = subCommands.stream()
                .filter(x -> x.hasPermissionToExecute(sender))
                .filter(x -> x.getName().equalsIgnoreCase(args[0]))
                .findFirst().orElse(null);

        if (subCommand == null)
            return showHelpMessage(sender);

        if (!subCommand.hasPermissionToExecute(sender))
            return false;

       return subCommand.onCommand(sender, command, label, (String[])
               Arrays.stream(args).skip(1).toList().toArray(new String[]{}));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(x -> x.hasPermissionToExecute(sender))
                    .map(SubCommand::getName)
                    .filter(x -> x.startsWith(args[0]))
                    .toList();
        } else if (args.length > 1) {
            SubCommand subCommand = subCommands.stream()
                    .filter(x -> x.hasPermissionToExecute(sender))
                    .filter(x -> x.getName().equalsIgnoreCase(args[0]))
                    .findFirst().orElse(null);

            if (subCommand != null)
                return subCommand
                        .onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toList().toArray(new String[]{}));
        }

        return List.of();
    }

    public void addSubCommand(SubCommand command) {
        subCommands.add(command);
    }

    private boolean showHelpMessage(CommandSender sender) {
        Component component = Component.text("----------- ")
                .append(Component.text("["))
                .append(Component.text("NatureRevive", TextColor.fromHexString("#F6E0A4")))
                .append(Component.text("]"))
                .append(Component.text(" -----------"))
                .appendNewline();

        for (SubCommand subCommand : subCommands) {
            if (!subCommand.hasPermissionToExecute(sender)) continue;

            List<String> opinions = subCommand.onTabComplete(sender, null, "", new String[]{});

            component = component.append(Component.text("/naturerevive "))
                .append(Component.text(subCommand.getName(), TextColor.fromHexString("#E6BBF6")));

            if (opinions != null && !opinions.isEmpty()) {
                component = component.append(
                        Component.text(" ")
                        .append(Component.text("<"))
                        .append(Component.text(String.join("/", opinions)))
                        .append(Component.text(">"))
                );
            }

            component = component.appendNewline();
        }

        sender.sendMessage(
                component
        );
        return true;
    }
}
