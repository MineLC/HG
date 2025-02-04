package me.isra.hgkits.commands;

import java.util.List;

import me.isra.hgkits.tops.TopType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import me.isra.hgkits.tops.TopStorage;
import me.isra.hgkits.tops.inventory.TopInventoryBuilder;
import me.isra.hgkits.translate.TranslateManager;
import net.md_5.bungee.api.ChatColor;

public class TopCommand implements TabExecutor {
    private final TopInventoryBuilder topInventoryBuilder = new TopInventoryBuilder();
    private final TranslateManager translateManager;

    public TopCommand(TranslateManager translateManager) {
        this.translateManager = translateManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return args.length == 0 ? List.of("kills", "deaths", "wins", "kdr") : List.of();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("top-command-format")));
            return true;
        }
        switch (args[0]) {
            case "kills":
            case "asesinatos":
            case "asesinato":
            case "kill":
                topInventoryBuilder.build(player, TopStorage.kills(), ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("top-kills-title")), TopType.KILLS);
                break;

            case "deaths":
            case "death":
            case "muertes":
            case "muerte":
                topInventoryBuilder.build(player, TopStorage.deaths(), ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("top-deaths-title")), TopType.DEATHS);
                break;

            case "wins":
            case "victorias":
            case "victoria":
                topInventoryBuilder.build(player, TopStorage.wins(), ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("top-wins-title")), TopType.WINS);
                break;

            case "kdr":
                topInventoryBuilder.build(player, TopStorage.kdr(), ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("top-kdr-title")), TopType.KDR);
                break;

            default:
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("top-command-format")));
                break;
        }
        return false;
    }
}
