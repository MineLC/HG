package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.managers.KitManager;
import me.isra.hgkits.managers.PlayerAttackManager;

import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class EntityTargetLivingEntityListener implements Listener {
    private final KitManager kitManager;
    private final PlayerAttackManager attackManager;

    public EntityTargetLivingEntityListener(KitManager kitManager, PlayerAttackManager attackManager) {
        this.kitManager = kitManager;
        this.attackManager = attackManager;
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (HGKits.GAMESTATE == GameState.GAME) {
            if (event.getTarget() instanceof Player && event.getEntity() instanceof Monster) {
                Player player = (Player) event.getTarget();

                Kit kit = kitManager.getKitByPlayer(player);

                if (kit == null) {
                    return;
                }

                if (kit.getName().equals("Enderman") || kit.getName().equals("Domabestias") || kit.getName().equals("Domabestiaspro")) {
                    if (!attackManager.hasPlayerAttacked(player)) {
                        event.setCancelled(true);
                    }
                }
            }
        } else {
            event.setCancelled(true);
        }
    }
}
