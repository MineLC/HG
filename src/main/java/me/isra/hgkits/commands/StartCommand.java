package me.isra.hgkits.commands;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.enums.GameState;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommand implements CommandExecutor {
    private final HGKits plugin;

    public StartCommand(HGKits plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (command.getName().equalsIgnoreCase("start")) {
            if (!(sender.hasPermission("start"))) {
                sender.sendMessage(ChatColor.RED + "Necesitas ser " + ChatColor.GREEN + "VIP" + ChatColor.RED + " como mínimo para iniciar la partida");
                return true;
            }
            if(HGKits.GAMESTATE == GameState.PREGAME) {
                if (plugin.getPlayers().size() <= 1) {
                    sender.sendMessage(ChatColor.RED + "Necesita haber como mínimo 2 jugadores");
                    return true;
                }
                if (sender instanceof Player) {
                    plugin.startGame();
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "Este comando solo puede ser ejecutado por un jugador.");
                    return false;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "La partida ya ha empezado!.");
                return false;
            }
        }
        return false;
    }
}
