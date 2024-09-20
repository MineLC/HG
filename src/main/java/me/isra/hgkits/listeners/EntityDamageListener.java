package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.managers.KitManager;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityDamageListener implements Listener {
    private final KitManager kitManager;
    private final Map<UUID, Long> lastStrengthApplication = new HashMap<>();

    public EntityDamageListener(KitManager kitManager) {
        this.kitManager = kitManager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity damagedEntity = event.getEntity();
        boolean isPlayerDamaged = damagedEntity instanceof Player;

        // Si la entidad dañada es un jugador y no está en estado GAME, cancelar el evento
        if (isPlayerDamaged && HGKits.GAMESTATE != GameState.GAME) {
            event.setCancelled(true);
        }

        if (!isPlayerDamaged) {
            // otras entidades no jugadores
            if (HGKits.GAMESTATE == GameState.PREGAME) {
                event.setCancelled(true);
            }
        }

        if (isPlayerDamaged && HGKits.GAMESTATE != GameState.PREGAME) {
            Player player = (Player) damagedEntity;
            if (event.getCause() == EntityDamageEvent.DamageCause.FIRE || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                Kit kit = kitManager.getKitByPlayer(player);
                if (kit.getName().equals("Piromano") || kit.getName().equals("Pyro") || kit.getName().equals("Tanque")) {
                    if (!isInCooldown(player)) {
                        event.setCancelled(true);
                        applyFireStrengthEffect(player);
                    }
                }
            }
        }
    }

    private boolean isInCooldown(Player player) {
        long currentTime = System.currentTimeMillis();
        Long lastApplicationTime = lastStrengthApplication.get(player.getUniqueId());

        if (lastApplicationTime == null) {
            return false;
        }

        return (currentTime - lastApplicationTime) < 20000;
    }

    // Método para aplicar el efecto de fuerza una sola vez y activar el cooldown
    private void applyFireStrengthEffect(Player player) {
        if (player.getFireTicks() > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 260, 1));
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0F, 1.0F);

            // Actualizar el tiempo del último uso de fuerza
            lastStrengthApplication.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }
}
