package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.managers.KitManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PlayerDeathListener implements Listener {
    private final HGKits plugin;
    private final KitManager kitManager;

    public PlayerDeathListener(HGKits plugin, KitManager kitManager) {
        this.plugin = plugin;
        this.kitManager = kitManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (HGKits.GAMESTATE == GameState.GAME) {
            Player player = event.getEntity();
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
                UUID killerUUID = killer.getUniqueId();
                Kit killerKit = kitManager.getKitByPlayer(killer);

                if (killerKit != null && (killerKit.getName().equals("Guerrero") || killerKit.getName().equals("Matasanos"))) {
                    if (killer.getFoodLevel() < 20) {
                        killer.setFoodLevel(20);
                    }
                }

                int numeroDeAsesinatos = plugin.getAsesinatos().getOrDefault(killerUUID, 0) + 1;
                plugin.getAsesinatos().put(killerUUID, numeroDeAsesinatos);

                // Calcular la fama ganada de forma exponencial (2^(n-1))
                int famaGanada = (int) Math.pow(2, numeroDeAsesinatos - 1);
                int famaTotal = plugin.getFama().getOrDefault(killerUUID, 0) + famaGanada;
                plugin.getFama().put(killerUUID, famaTotal);

                killer.sendMessage(ChatColor.GREEN + "Has recibido " + famaGanada + " de fama.");
                plugin.updateFameInDatabase(player, famaTotal);
            }


            if (plugin.getPlayers().remove(player) && plugin.getPlayers().size() > 1) {
                Bukkit.broadcastMessage(ChatColor.RED + "Quedan " + plugin.getPlayers().size() + " jugadores vivos.");
            }
            plugin.updateStatsInDatabase(player);



            if (world != null) {
                world.strikeLightningEffect(deathLocation);
            }

            player.setGameMode(GameMode.SPECTATOR);
        }
    }
}
