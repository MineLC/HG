package me.isra.hgkits.commands;

import me.isra.hgkits.database.DatabaseManager;
import me.isra.hgkits.database.User;
import me.isra.hgkits.managers.FameManager;
import me.isra.hgkits.translate.TranslateManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommand implements CommandExecutor {
    private final TranslateManager translateManager;

    public RankCommand(TranslateManager translateManager) {
        this.translateManager = translateManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (command.getName().equalsIgnoreCase("rank")) {
                final User user = DatabaseManager.getDatabase().getCached(player.getUniqueId());
                String rank = FameManager.getRankByFame(user.fame);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("your-fame") + user.fame));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("your-rank") + rank));
                return true;
            }
        }
        return false;
    }
}
