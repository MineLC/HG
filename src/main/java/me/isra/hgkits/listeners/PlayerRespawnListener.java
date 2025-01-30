package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.translate.TranslateManager;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.entity.Player;

public class PlayerRespawnListener implements Listener {

    private final HGKits plugin;
    private final TranslateManager translateManager;

    public PlayerRespawnListener(HGKits plugin) {
        this.plugin = plugin;
        this.translateManager = plugin.getTranslateManager();
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
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("world-not-loaded")));
            event.setRespawnLocation(player.getWorld().getSpawnLocation());
        }
    }
}
