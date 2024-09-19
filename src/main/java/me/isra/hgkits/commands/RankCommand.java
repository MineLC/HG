package me.isra.hgkits.commands;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.managers.FameManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommand implements CommandExecutor {
    private final HGKits plugin;

    public RankCommand(HGKits plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (command.getName().equalsIgnoreCase("rank")) {
                plugin.getFameFromDatabase(player).thenAccept(fame -> {
                    String rank = FameManager.getRankByFame(fame);
                    player.sendMessage(ChatColor.GREEN + "Tu fama es de: " + fame);
                    player.sendMessage(ChatColor.GREEN + "Rango: " + rank);
                }).exceptionally(ex -> {
                    player.sendMessage("Hubo un error al obtener tu fama.");
                    plugin.getLogger().severe("Error al obtener la fama del jugador " + player.getName() + ": " + ex.getMessage());
                    return null;
                });
                return true;
            }
        }
        return false;
    }
}
