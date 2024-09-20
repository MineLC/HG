package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.database.DatabaseManager;
import me.isra.hgkits.enums.GameState;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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
        DatabaseManager.getDatabase().load(player, () -> plugin.updatePlayerScore(player));

        if(HGKits.GAMESTATE == GameState.PREGAME) {
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(plugin.getRandomSpawnLocation());
            player.setAllowFlight(true);
            giveItems(event.getPlayer().getInventory());
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

    public void giveItems(final PlayerInventory inventory) {
        ItemStack item = new ItemStack(Material.BOW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Selector de KIT");
        item.setItemMeta(meta);
        inventory.setItem(0, item);

        item = new ItemStack(Material.BOOK);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Estadísticas");
        item.setItemMeta(meta);
        inventory.setItem(4, item);

        item = new ItemStack(Material.LEATHER);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Top de kills");
        item.setItemMeta(meta);
        inventory.setItem(7, item);

        item = new ItemStack(Material.BONE);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Top de muertes");
        item.setItemMeta(meta);
        inventory.setItem(8, item);
    }
}