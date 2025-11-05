package com.darksoldier1404.dpban.commands;

import com.darksoldier1404.dpban.functions.DPBFunction;
import com.darksoldier1404.dppc.builder.command.CommandBuilder;
import org.bukkit.entity.Player;

import static com.darksoldier1404.dpban.Ban.plugin;

public class DPBCommand {
    private final CommandBuilder builder = new CommandBuilder(plugin);

    public DPBCommand() {
        builder.addSubCommand("ban", "dpban.ban", "/dpban ban <player> <reason>", true, (p, args) -> {
            if (args.length >= 3) {
                String playerName = args[0];
                String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                DPBFunction.banPlayer(p, playerName, reason);
                return true;
            }
            return true;
        });

        builder.addSubCommand("unban", "dpban.unban", "/dpban unban <player>", true, (p, args) -> {
            if (args.length == 2) {
                String playerName = args[1];
                DPBFunction.unbanPlayer(p, playerName);
                return true;
            }
            return true;
        });

        builder.addSubCommand("tempban", "dpban.tempban", "/dpban tempban <player> <duration> <reason>", true, (p, args) -> {
            if (args.length >= 4) {
                String playerName = args[1];
                String duration = args[2];
                String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
                DPBFunction.tempBanPlayer(p, playerName, duration, reason);
                return true;
            }
            return true;
        });

        builder.addSubCommand("gui", "dpban.gui", "/dpban gui", true, (p, args) -> {
            DPBFunction.openBanGUI((Player) p);
            return true;
        });
    }

    public CommandBuilder getExecutor() {
        return builder;
    }
}
