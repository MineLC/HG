package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.entity.Player;

public class PlayerRespawnListener implements Listener {

    private final HGKits plugin;

    public PlayerRespawnListener(HGKits plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (plugin.currentWorld != null) {
            // Obtener una ubicación de respawn en el mundo actual (currentWorld)
            Location spawnLocation = plugin.getRandomSpawnLocation();
            event.setRespawnLocation(spawnLocation);
        } else {
            // Si el mundo no está cargado, respawnear en el spawn del mundo por defecto
            player.sendMessage("El mundo actual no está cargado, reapareciendo en el mundo predeterminado.");
            event.setRespawnLocation(player.getWorld().getSpawnLocation());
        }
    }
}
