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
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EntityDamageListener implements Listener {
    private final HGKits plugin;
    private final KitManager kitManager;
    private final Map<UUID, Long> lastStrengthApplication = new HashMap<>();

    public EntityDamageListener(HGKits plugin, KitManager kitManager) {
        this.plugin = plugin;
        this.kitManager = kitManager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity damagedEntity = event.getEntity();
        boolean isPlayerDamaged = damagedEntity instanceof Player;

        // Si la entidad dañada es un jugador y no está en estado GAME, cancelar el
        // evento
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
            if (event.getCause() == EntityDamageEvent.DamageCause.FIRE
                    || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK
                    || event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                Kit kit = kitManager.getKitByPlayer(player);
                if (kit.name().equals("Piromano") || kit.name().equals("Pyro")
                        || kit.name().equals("Tanque")) {
                            
                    event.setCancelled(true);
                    if (!isInCooldown(player)) {
                        applyFireStrengthEffect(player, kit);
                    }
                }
            }

            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                Kit kit = kitManager.getKitByPlayer(player);

                if (kit.name().equals("Saltamontes") || kit.name().equals("Explorador")) {
                    if (event.getDamage() > 4.0) {
                        event.setCancelled(true);
                        player.damage(4.0);
                        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0f, 1.0f);
                        damageNearbyPlayers(player, event);
                    }
                }
            }
        }
    }

    private void damageNearbyPlayers(Player player, EntityDamageEvent event) {
        final List<Entity> nearbyEntities = (List<Entity>) event.getEntity().getNearbyEntities(5.0, 5.0,
                5.0);
        for (final Entity target : nearbyEntities) {
            if (target instanceof Player) {
                final Player t = (Player) target;
                if (!plugin.getPlayers().contains(target)) {
                    continue;
                }
                if (t.getName() == player.getName()) {
                    continue;
                }
                if (t.isSneaking()) {
                    t.damage(event.getDamage() / 2.0, event.getEntity());
                } else {
                    t.damage(event.getDamage(), event.getEntity());
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
    private void applyFireStrengthEffect(Player player, Kit kit) {

        switch (kit.name()) {
            case "Pyro":
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 260, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 260, 1));
                break;
            case "Piromano":
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 260, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 260, 0));
                break;

            default:
                break;
        }

        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0F, 1.0F);
        // Actualizar el tiempo del último uso de fuerza
        lastStrengthApplication.put(player.getUniqueId(), System.currentTimeMillis());

    }
}
