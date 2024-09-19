package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.enums.GameState;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerJoinListener implements Listener {

    private final HGKits plugin;

    public PlayerJoinListener(HGKits plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENDERDRAGON_WINGS, 1.0f, 1.0f);
        }

        Player player = event.getPlayer();
        plugin.createPlayerInDatabase(player);
        plugin.setupScoreboard(player);

        if(HGKits.GAMESTATE == GameState.PREGAME) {
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(plugin.getRandomSpawnLocation());
            player.setAllowFlight(true);
            giveKitSelectorBow(event.getPlayer());
            if (!plugin.isCountdownRunning()) {
                if(Bukkit.getOnlinePlayers().size() == 1) {
                    player.sendMessage(ChatColor.RED + "Se necesitan mínimo 2 jugadores para comenzar la partida");
                }
                if(Bukkit.getOnlinePlayers().size() == 2) {
                    plugin.startCountdown();
                }
            }
        } else {
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

    public void giveKitSelectorBow(Player player) {
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta meta = bow.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Selector de KIT");
            bow.setItemMeta(meta);
        }

        player.getInventory().addItem(bow);
    }
}
