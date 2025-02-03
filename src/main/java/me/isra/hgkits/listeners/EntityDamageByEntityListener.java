package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.managers.KitManager;
import me.isra.hgkits.managers.PlayerAttackManager;
import me.isra.hgkits.translate.TranslateManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class EntityDamageByEntityListener implements Listener {
    private final HGKits plugin;
    private final KitManager kitManager;
    private final PlayerAttackManager attackManager;
    private final TranslateManager translateManager;
    private final ProjectileHitListener projectileHitListener;

    public EntityDamageByEntityListener(HGKits plugin, KitManager kitManager, PlayerAttackManager attackManager,
            TranslateManager translateManager, ProjectileHitListener projectileHitListener) {
        this.plugin = plugin;
        this.kitManager = kitManager;
        this.attackManager = attackManager;
        this.translateManager = translateManager;
        this.projectileHitListener = projectileHitListener;
    }

    @EventHandler
    public void onEntityDamagedByEntity(EntityDamageByEntityEvent event) {
        Entity damagedEntity = event.getEntity();
        Entity damagerEntity = event.getDamager();

        // Cuando un jugador daña a un Monster (mob)
        if (damagedEntity instanceof Monster && damagerEntity instanceof Player) {
            Player attacker = (Player) damagerEntity;
            Kit attackerKit = kitManager.getKitByPlayer(attacker);

            if (attackerKit != null && (attackerKit.getName().equals("Enderman") ||
                    attackerKit.getName().equals("Domabestias") ||
                    attackerKit.getName().equals("Domabestiaspro"))) {
                attackManager.addPlayer(attacker);
            }
        }

        // Cuando tanto el atacante como la víctima son jugadores
        if (damagedEntity instanceof Player && damagerEntity instanceof Player && HGKits.GAMESTATE == GameState.GAME) {
            Player victim = (Player) damagedEntity;
            Player attacker = (Player) damagerEntity;
            Kit attackerKit = kitManager.getKitByPlayer(attacker);

            if (attackerKit != null) {
                applyKitEffects(event, attacker, attackerKit, victim);
            }
        }

        // Cuando un jugador es dañado por una Snowball
        if (damagedEntity instanceof Player && damagerEntity instanceof Snowball) {
            Snowball snowball = (Snowball) damagerEntity;
            if (snowball.getShooter() instanceof Player) {
                Player victim = (Player) damagedEntity;
                Player attacker = (Player) snowball.getShooter();
                Kit attackerKit = kitManager.getKitByPlayer(attacker);
                if (attackerKit != null) {
                    applyKitEffects(event, attacker, attackerKit, victim);
                }
            }
        }

        // Cuando un jugador es dañado por una Flecha
        if (damagedEntity instanceof Player && damagerEntity instanceof Arrow && HGKits.GAMESTATE == GameState.GAME) {
            Arrow arrow = (Arrow) damagerEntity;
            if (arrow.getShooter() instanceof Player) {
                Player victim = (Player) damagedEntity;
                Player attacker = (Player) arrow.getShooter();
                Kit attackerKit = kitManager.getKitByPlayer(attacker);
                if (attackerKit != null) {
                    applyKitEffects(event, attacker, attackerKit, victim);
                }
            }
        }
    }

    private void applyKitEffects(EntityDamageByEntityEvent event, Player attacker, Kit attackerKit, Player victim) {
        switch (attackerKit.getName()) {
            case "Canibal":
                applyCanibalEffect(event, attacker);
                break;
            case "Spiderman":
                if (event.getDamager() instanceof Snowball) {
                    projectileHitListener.createWebStructure(victim.getLocation());
                }
                break;
            case "Meduza":
                freezeVictimIfFrozen(victim, attacker);
                break;
            case "Troll":
                if (randomChance())
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                    playSound(attacker);
                break;
            case "Matasanos":
                if (randomChance())
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
                    playSound(attacker);
                break;
            case "Orco":
                if (randomChance())
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));
                    playSound(attacker);
                break;
            case "Hulk":
                applyHulkEffect(event, attacker);
                break;
            case "Ladron":
            case "Proladron":
                applyLadronEffect(attacker, victim);
                break;
            case "Headshooter":
            case "Elite":
                applyHeadshooterEffect(attacker, victim);
                break;
        }
    }

    private void applyCanibalEffect(EntityDamageByEntityEvent event, Player attacker) {
        if (event.getEntity() instanceof Player && attacker.getItemInHand().toString().contains("SWORD") && randomChance()) {
            double damage = event.getDamage();
            double healAmount = damage / 2.0;
            double newHealth = Math.min(attacker.getHealth() + healAmount, attacker.getMaxHealth());
            attacker.setHealth(newHealth);
            playSound(attacker);
        }
    }

    private void freezeVictimIfFrozen(Player victim, Player attacker) {
        if (plugin.getFrozenPlayers().contains(victim)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> victim.setVelocity(new Vector(0, 0, 0)), 1L);
            playSound(attacker);
        }
    }

    private void applyHulkEffect(EntityDamageByEntityEvent event, Player attacker) {
        if (attacker.getItemInHand().getType() == Material.AIR) {
            event.setDamage(5.0);
        }
    }

    private void applyLadronEffect(Player attacker, Player victim) {
        if (attacker.getItemInHand().getType() == Material.STICK && randomChance()) {
            ItemStack victimItem = victim.getItemInHand();
            if (victimItem != null && victimItem.getType() != Material.AIR) {
                victim.setItemInHand(new ItemStack(Material.AIR));
                attacker.setItemInHand(victimItem);
                playSound(attacker);
            }
        }
    }

    private void applyHeadshooterEffect(Player attacker, Player victim) {
        if (attacker.getLocation().distance(victim.getLocation()) >= 25) {
            playSound(attacker);
            victim.setHealth(0.0);
            Location l = victim.getLocation();
            plugin.getServer().getWorlds().get(0).strikeLightningEffect(l.add(0.0, 100.0, 0.0));
            attacker.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    translateManager.getMessage("long-distance-kill").replace("%victim%", victim.getName())));
        }
    }

    private void playSound(Player attacker) {
        attacker.playSound(attacker.getLocation(), Sound.ORB_PICKUP, 1.0f, 1.0f);
    }

    private boolean randomChance() {
        return Math.random() < 0.3;
    }
}
