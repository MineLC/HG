package me.isra.hgkits.commands;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.translate.TranslateManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommand implements CommandExecutor {
    private final HGKits plugin;
    private final TranslateManager translateManager;

    public StartCommand(HGKits plugin) {
        this.plugin = plugin;
        this.translateManager = plugin.getTranslateManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (command.getName().equalsIgnoreCase("start")) {
            if (!(sender.hasPermission("start"))) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("no-permission")));
                return true;
            }
            if (HGKits.GAMESTATE == GameState.PREGAME) {
                if (Bukkit.getOnlinePlayers().size() <= 1) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("not-enough-players")));
                    return true;
                }
                if (sender instanceof Player) {
                    plugin.startGame();
                    return true;
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("player-only-command")));
                    return false;
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("game-already-started")));
                return false;
            }
        }
        return false;
    }
}
