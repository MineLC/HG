package me.isra.hgkits.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import me.isra.hgkits.tops.TopStorage;
import me.isra.hgkits.tops.inventory.TopInventoryBuilder;

public class TopCommand implements TabExecutor {
    private final TopInventoryBuilder topInventoryBuilder = new TopInventoryBuilder();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return args.length == 0 ? List.of("kills", "deaths") : List.of();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("Formato: /top (kills/deaths)");
            return true;
        }
        switch (args[0]) {
            case "kills":
            case "asesinatos":
            case "asesinato":
            case "kill":
                topInventoryBuilder.build(player, TopStorage.kills(), "Top de kills");
                break;

            case "deaths":
            case "death":
            case "muertes":
            case "muerte":
                topInventoryBuilder.build(player, TopStorage.deaths(), "Top de muertes");
                break;
            default:
                break;
        }
        return false;
    }
}
