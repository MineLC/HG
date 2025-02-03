package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.managers.KitManager;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

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
            createWebStructure(projectile.getLocation());
        }
    }

    public void createWebStructure(Location hitLocation) {
        Random random = new Random();
        int webCount = random.nextInt(3) + 1; // Genera entre 1 y 3 telarañas
    
        for (int i = 0; i < webCount; i++) {
            // Genera la posición aleatoria dentro del área de 2x2 en el suelo
            int offsetX = random.nextInt(2); // Rango: 0 a 1
            int offsetZ = random.nextInt(2); // Rango: 0 a 1
    
            // Obtiene la ubicación del bloque en el suelo
            Location webLocation = hitLocation.clone().add(offsetX, 0, offsetZ);
            Block groundBlock = webLocation.getBlock();
    
            // Asegura que la telaraña se coloque sobre un bloque sólido
            if (groundBlock.getType() == Material.AIR) {
                Block blockBelow = groundBlock.getRelative(0, -1, 0); // Bloque debajo
                if (blockBelow.getType().isSolid()) { // Solo colocar telaraña si hay suelo
                    groundBlock.setType(Material.WEB);
                }
            }
        }
    }
}
