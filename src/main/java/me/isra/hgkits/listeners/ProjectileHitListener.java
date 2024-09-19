package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.managers.KitManager;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

public class ProjectileHitListener implements Listener {

    private final KitManager kitManager;

    public ProjectileHitListener(KitManager kitManager) {
        this.kitManager = kitManager;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (HGKits.GAMESTATE == GameState.PREGAME) {
            return;
        }

        Projectile projectile = event.getEntity();
        Entity shooter = projectile.getShooter() instanceof Entity ? (Entity) projectile.getShooter() : null;

        if (!(shooter instanceof Player)) {
            return;
        }

        Player player = (Player) shooter;
        Kit kit = kitManager.getKitByPlayer(player);

        if (kit == null) {
            return;
        }

        if (projectile instanceof Arrow && kit.getName().equalsIgnoreCase("Proarquero")) {
            projectile.getWorld().createExplosion(projectile.getLocation(), 2.0F);
            projectile.remove();
        }

        if (projectile instanceof Snowball && kit.getName().equalsIgnoreCase("Spiderman")) {
            createWebStructure(projectile);
        }
    }

    private void createWebStructure(Projectile projectile) {
        Vector hitLocation = projectile.getLocation().toVector();

        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 2; y++) { // Altura ajustada a 3 bloques (0, 1, 2)
                for (int z = -1; z <= 1; z++) {
                    Vector blockLocation = hitLocation.clone().add(new Vector(x, y, z));
                    Material blockType = projectile.getWorld().getBlockAt(blockLocation.toLocation(projectile.getWorld())).getType();

                    if (blockType == Material.AIR) {
                        projectile.getWorld().getBlockAt(blockLocation.toLocation(projectile.getWorld())).setType(Material.WEB);
                    }
                }
            }
        }
    }
}
