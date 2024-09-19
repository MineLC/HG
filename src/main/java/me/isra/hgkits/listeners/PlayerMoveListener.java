package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.enums.GameState;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    private HGKits plugin;

    public PlayerMoveListener(HGKits plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        if(HGKits.GAMESTATE != GameState.GAME) {
            if (location.getY() < -1) {
                Location spawnLocation = plugin.getRandomSpawnLocation();
                player.teleport(spawnLocation);
            }
        }

        if (plugin.getFrozenPlayers().contains(player)) {
            event.setCancelled(true);
        }
    }
}
