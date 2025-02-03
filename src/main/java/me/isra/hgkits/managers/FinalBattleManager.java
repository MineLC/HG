package me.isra.hgkits.managers;

import lc.schematicapi.internal.PasteV1_8R3;
import lc.schematicapi.paste.Schematic;
import lombok.Data;
import me.isra.hgkits.HGKits;
import me.isra.hgkits.translate.TranslateManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.SplittableRandom;

@Data
public class FinalBattleManager {

    private final HGKits plugin;
    private Schematic schematic;

    private int radius;

    private SplittableRandom random = new SplittableRandom();
    private Location mainBlock;

    public FinalBattleManager(HGKits plugin) {
        this.plugin = plugin;
    }

    public void update(Schematic newSchematic, int newRadius) {
        schematic = newSchematic;
        radius = newRadius;
    }

    public void createBattle() {
        mainBlock = getPlugin().getCurrentWorld().getSpawnLocation().clone();
        mainBlock.setY(100D);

        if (schematic == null) {
            plugin.getLogger().warning("Error on paste schematic. Not schematic found");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> new PasteV1_8R3().paste(mainBlock.getBlockX(), mainBlock.getBlockY(), mainBlock.getBlockZ(), mainBlock.getWorld(), schematic));
    }

    public void teleportGamers() {
        final int centerX = mainBlock.getBlockX() + (schematic.amountBlocksX / 2);
        final int centerY = mainBlock.getBlockY();
        final int centerZ = mainBlock.getBlockZ() + (schematic.amountBlocksZ / 2);
        final int newRadius = radius * 2;

        for (Player p : Bukkit.getOnlinePlayers()) {
            final Location location = new Location(
                    p.getWorld(),
                    centerX + genRandomPosition(newRadius),
                    centerY,
                    centerZ + genRandomPosition(newRadius));

            p.teleport(location);
            p.leaveVehicle();
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getTranslateManager().getMessage("cage-teleport")));
        }
    }

    private int genRandomPosition(int newRadius) {
        return random.nextInt(newRadius) - radius;
    }

}
