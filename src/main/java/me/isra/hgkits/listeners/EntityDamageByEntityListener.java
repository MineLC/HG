package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.managers.KitManager;
import me.isra.hgkits.managers.PlayerAttackManager;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EntityDamageByEntityListener implements Listener {
    private final KitManager kitManager;
    private final PlayerAttackManager attackManager;

    public EntityDamageByEntityListener(KitManager kitManager, PlayerAttackManager attackManager) {
        this.kitManager = kitManager;
        this.attackManager = attackManager;
    }

    @EventHandler
    public void onEntityDamagedByEntity(EntityDamageByEntityEvent event) {
        Entity damagedEntity = event.getEntity();

        // Verificar si la entidad dañada es un mob (Monster) y el atacante es un jugador
        if (damagedEntity instanceof Monster && event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Kit attackerKit = kitManager.getKitByPlayer(attacker);

            // Añadir al jugador al attackManager si tiene uno de los kits específicos
            if (attackerKit != null && (attackerKit.getName().equals("Enderman") ||
                    attackerKit.getName().equals("Domabestias") ||
                    attackerKit.getName().equals("Domabestiaspro"))) {
                attackManager.addPlayer(attacker);
            }
        }

        // Asegurarse de que la víctima y el atacante sean jugadores
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            if (HGKits.GAMESTATE == GameState.GAME) {
                Player victim = (Player) event.getEntity();
                Player attacker = (Player) event.getDamager();
                Kit attackerKit = kitManager.getKitByPlayer(attacker);

                // Si el atacante tiene un kit, aplicamos los efectos correspondientes
                if (attackerKit != null) {
                    applyKitEffects(event, attacker, attackerKit, victim);
                }
            }
        }
    }

    private void applyKitEffects(EntityDamageByEntityEvent event, Player attacker, Kit attackerKit, Player victim) {
        // Blindness para Troll
        if (attackerKit.getName().equals("Troll") && randomChance()) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
        }

        // Poison para Matasanos
        if (attackerKit.getName().equals("Matasanos") && randomChance()) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
        }

        // Weakness para Orco
        if (attackerKit.getName().equals("Orco") && randomChance()) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));
        }

        // Verificar si el daño fue causado por caída y si el atacante tiene el kit "Explorador" o "Saltamontes"
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL &&
                (attackerKit.getName().equals("Explorador") || attackerKit.getName().equals("Saltamontes"))) {

            double reducedDamage = 2.0;
            event.setDamage(reducedDamage);

            damageNearbyPlayers(attacker);
        }

        // Hulk: mayor daño si ataca con las manos vacías
        if (attackerKit.getName().equals("Hulk") && attacker.getItemInHand().getType() == Material.AIR) {
            event.setDamage(5.0);  // Ejemplo: Aumenta el daño en 5 puntos
        }

        // Ladron y Proladron: roban objetos si atacan con un palo
        if ((attackerKit.getName().equals("Ladron") || attackerKit.getName().equals("Proladron")) && attacker.getItemInHand().getType() == Material.STICK) {
            if (randomChance()) {
                ItemStack victimItem = victim.getItemInHand();
                if (victimItem != null && victimItem.getType() != Material.AIR) {
                    victim.setItemInHand(new ItemStack(Material.AIR));
                    attacker.setItemInHand(victimItem);
                }
            }
        }

        // Headshooter y Elite: matar instantáneamente disparando a más de 30 bloques
        if ((attackerKit.getName().equals("Headshooter") || attackerKit.getName().equals("Elite")) && event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player) {
                // Si el disparo fue hecho por el atacante y la distancia es mayor a 30 bloques
                if (attacker.getLocation().distance(victim.getLocation()) >= 30) {
                    victim.setHealth(0.0);
                    attacker.sendMessage("¡El enemigo " + victim.getName() + " murió al instante con un disparo de larga distancia!");
                }
            }
        }
    }

    private void damageNearbyPlayers(Player player) {
        // Obtener jugadores cercanos en un radio de 5 bloques
        player.getNearbyEntities(5, 5, 5).stream()
                .filter(entity -> entity instanceof Player && !entity.equals(player))
                .forEach(nearbyEntity -> {
                    Player nearbyPlayer = (Player) nearbyEntity;
                    double damage = 4.0 + (Math.random() * (8.0 - 4.0));
                    nearbyPlayer.damage(damage, player);
                });
    }

    private boolean randomChance() {
        return Math.random() < 0.3;
    }

}
