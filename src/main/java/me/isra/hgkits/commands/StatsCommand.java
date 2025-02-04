package me.isra.hgkits.commands;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.database.DatabaseManager;
import me.isra.hgkits.database.User;
import me.isra.hgkits.menu.PlayerStatsMenu;
import me.isra.hgkits.translate.TranslateManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {
    private final HGKits plugin;
    private final TranslateManager translateManager;

    public StatsCommand(HGKits plugin) {
        this.plugin = plugin;
        this.translateManager = plugin.getTranslateManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            String message = translateManager.getMessage("player-only-command");
            if (message != null) {
                sender.sendMessage(message);
            } else {
                sender.sendMessage("Only players can use this command.");
            }
            return true;
        }

        Player player = (Player) sender;
        User user = DatabaseManager.getDatabase().getCached(player.getUniqueId());

        if (user != null) {
            new PlayerStatsMenu(player, translateManager, user).open(player);
        } else {
            player.sendMessage("Failed to load your stats.");
        }

        return true;
    }
}
