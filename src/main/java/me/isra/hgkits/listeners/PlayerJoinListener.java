package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.database.DatabaseManager;
import me.isra.hgkits.database.User;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.managers.KitManager;
import me.isra.hgkits.translate.TranslateManager;

import java.util.Collection;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {

    private final HGKits plugin;
    private final TranslateManager translateManager;

    public PlayerJoinListener(HGKits plugin) {
        this.plugin = plugin;
        this.translateManager = plugin.getTranslateManager();
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENDERDRAGON_WINGS, 1.0f, 1.0f);
        }
        Player player = event.getPlayer();
        DatabaseManager.getDatabase().load(player);

        if(HGKits.GAMESTATE == GameState.PREGAME) {
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(plugin.getRandomSpawnLocation());
            player.setAllowFlight(true);
            giveItems(event.getPlayer().getInventory(), player);
            if (!plugin.isCountdownRunning()) {
                if(Bukkit.getOnlinePlayers().size() == 1) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("not-enough-players")));
                }
                if(Bukkit.getOnlinePlayers().size() == 2) {
                    plugin.startCountdown();
                }
            }
        } else {
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(plugin.getRandomSpawnLocation());
        }
    }

    public void giveItems(final PlayerInventory inventory, Player player) {
        ItemStack item = new ItemStack(Material.BOW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("kit-selector")));
        item.setItemMeta(meta);
        inventory.setItem(0, item);

        item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
        skullMeta.setOwner(player.getName());
        skullMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("statistics")));
        item.setItemMeta(skullMeta);
        inventory.setItem(4, item);

        item = new ItemStack(Material.PAPER);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("tops")));
        item.setItemMeta(meta);
        inventory.setItem(8, item);
    }
}