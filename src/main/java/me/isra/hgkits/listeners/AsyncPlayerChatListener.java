package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.managers.FameManager;
import me.isra.hgkits.managers.KitManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.concurrent.ExecutionException;

public class AsyncPlayerChatListener implements Listener {
    private final HGKits plugin;
    private final KitManager kitManager;

    public AsyncPlayerChatListener(HGKits plugin, KitManager kitManager) {
        this.plugin = plugin;
        this.kitManager = kitManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Kit kit = kitManager.getKitByPlayer(player);
        String kitName = (kit != null && kit.getName() != null) ? kit.getName() : "Default";
        try {
            String formattedMessage = ChatColor.WHITE + "[" + ChatColor.RESET + ChatColor.GREEN + FameManager.getFameRank(plugin.getFameFromDatabase(player).get()) + ChatColor.RESET + ChatColor.WHITE + "] " + ChatColor.RESET + "<" + ChatColor.DARK_GRAY + "[" + kitName + "] " + ChatColor.RESET + ChatColor.GRAY + player.getName() + ChatColor.RESET + "> " + event.getMessage();
            event.setFormat(formattedMessage);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
