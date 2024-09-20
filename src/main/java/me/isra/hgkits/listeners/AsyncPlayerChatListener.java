package me.isra.hgkits.listeners;

import me.isra.hgkits.data.Kit;
import me.isra.hgkits.database.DatabaseManager;
import me.isra.hgkits.managers.FameManager;
import me.isra.hgkits.managers.KitManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;


public class AsyncPlayerChatListener implements Listener {
    private final KitManager kitManager;

    public AsyncPlayerChatListener(KitManager kitManager) {
        this.kitManager = kitManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Kit kit = kitManager.getKitByPlayer(player);
        String kitName = (kit != null && kit.getName() != null) ? kit.getName() : "Default";
        String formattedMessage = ChatColor.WHITE + "[" + ChatColor.RESET + ChatColor.GREEN + FameManager.getFameRank(DatabaseManager.getDatabase().getCached(player.getUniqueId()).fame) + ChatColor.RESET + ChatColor.WHITE + "] " + ChatColor.DARK_GRAY + "[" + kitName + "] " + ChatColor.RESET + ChatColor.GRAY + player.getName() + ChatColor.RESET + " > " + event.getMessage();
        event.setFormat(formattedMessage);
    }
}
