package me.isra.hgkits.commands;

import me.isra.hgkits.database.DatabaseManager;
import me.isra.hgkits.database.User;
import me.isra.hgkits.managers.FameManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (command.getName().equalsIgnoreCase("rank")) {
                final User user = DatabaseManager.getDatabase().getCached(player.getUniqueId());
                String rank = FameManager.getRankByFame(user.fame);
                player.sendMessage(ChatColor.GREEN + "Tu fama es de: " + user.fame);
                player.sendMessage(ChatColor.GREEN + "Rango: " + rank);
                return true;
            }
        }
        return false;
    }
}
