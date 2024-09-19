package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.enums.GameState;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final HGKits plugin;

    public PlayerQuitListener(HGKits plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if(HGKits.GAMESTATE == GameState.PREGAME) {
                p.playSound(p.getLocation(), Sound.EXPLODE, 1.0f, 1.0f);
            }
        }

        Player player = event.getPlayer();
        plugin.removeInventory(player);
        plugin.getScoreboards().remove(player.getUniqueId());

        if(plugin.getPlayers().remove(player)) {
            Location quitLocation = player.getLocation();
            World world = quitLocation.getWorld();

            if (world != null) {
                world.strikeLightningEffect(quitLocation);
            }

            if (!plugin.getPlayers().isEmpty()) {
                String quitMessage = event.getQuitMessage();
                event.setQuitMessage(ChatColor.RED + quitMessage);
                Bukkit.broadcastMessage(ChatColor.RED + "Quedan " + plugin.getPlayers().size() + " jugadores vivos.");
            }
        }
    }
}
