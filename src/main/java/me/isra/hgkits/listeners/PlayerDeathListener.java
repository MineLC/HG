package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.database.DatabaseManager;
import me.isra.hgkits.database.User;
import me.isra.hgkits.managers.KitManager;
import me.isra.hgkits.tops.TopManager;
import me.isra.hgkits.translate.TranslateManager;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerDeathListener implements Listener {
    private final HGKits plugin;
    private final KitManager kitManager;
    private final TranslateManager translateManager;

    public PlayerDeathListener(HGKits plugin, KitManager kitManager) {
        this.plugin = plugin;
        this.kitManager = kitManager;
        this.translateManager = plugin.getTranslateManager();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (HGKits.GAMESTATE == GameState.GAME) {
            Player player = event.getEntity();

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.AMBIENCE_THUNDER, 2f, 1f);
            }

            Location deathLocation = player.getLocation();
            World world = deathLocation.getWorld();
            Kit playerKit = kitManager.getKitByPlayer(player);

            String deathMessage = event.getDeathMessage();
            event.setDeathMessage(ChatColor.RED + deathMessage);

            if (playerKit != null && (playerKit.getName().equals("Creeper") || playerKit.getName().equals("Ultracreeper"))) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (world != null) {
                            world.createExplosion(deathLocation, 4F);
                        }
                    }
                }.runTaskLater(plugin, 1L);
            }

            Player killer = player.getKiller();
            if (killer != null) {
                final User killerData = DatabaseManager.getDatabase().getCached(killer.getUniqueId());
                if (killerData != null) {
                    Kit killerKit = kitManager.getKitByPlayer(killer);

                    if (killerKit != null && (killerKit.getName().equals("Guerrero") || killerKit.getName().equals("Matasanos"))) {
                        if (killer.getFoodLevel() < 20) {
                            killer.setFoodLevel(20);
                        }
                    }

                    killerData.kills++;
                    final double newFame = killerData.getKdr() * killerData.kills + (killerData.wins == 0 ? 0 : (double)(killerData.wins)/2D);
                    killerData.fame = (int)newFame;

                    plugin.updatePlayerScore(killer);
                    killer.sendMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("your-fame") + killerData.fame));
                
                    TopManager.calculateKills(killerData);
                }
            }

            if (plugin.getPlayers().remove(player) && plugin.getPlayers().size() > 1) {
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("players-left").replace("%count%", String.valueOf(plugin.getPlayers().size()))));
            }

            player.setGameMode(GameMode.SPECTATOR);
            final User victim = DatabaseManager.getDatabase().getCached(player.getUniqueId());
            victim.deaths++;
            plugin.updatePlayerScore(player);
            TopManager.calculateDeaths(victim);
        }
    }
}
