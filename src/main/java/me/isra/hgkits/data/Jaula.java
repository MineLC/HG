package me.isra.hgkits.data;

import lombok.Getter;
import me.isra.hgkits.HGKits;
import me.isra.hgkits.listeners.EntityDamageByEntityListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Material.AIR;

@Getter
public class Jaula {
    private final Player player;
    private Location center;
    private final List<BlockBackup> oldBlocks = new ArrayList<>();

    public Jaula(Player player) {
        this.player = player;
    }

    public void build(){
        center = new Location(player.getLocation().getWorld(),
                player.getLocation().getBlockX() + 0.5,
                player.getLocation().getBlockY(),
                player.getLocation().getBlockZ() + 0.5
        );

        World world = center.getWorld();
        int radius = 5;

        Bukkit.getScheduler().runTask(HGKits.getInstance(), () -> {
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        double distance = Math.sqrt(x * x + y * y + z * z);

                        if (distance >= radius - 0.5 && distance <= radius + 0.5) {
                            Location blockLocation = center.clone().add(x, y, z);
                            Block block = world.getBlockAt(blockLocation);

                            BlockBackup blockBackup = new BlockBackup(block);
                            oldBlocks.add(blockBackup);
                            block.setType(Material.GLASS);
                        }
                    }
                }
            }
        });
    }

    public void restoreBlocks(){
        EntityDamageByEntityListener.jaulas.remove(player);
        for(BlockBackup backup : oldBlocks){
            if(backup.getMaterial() == AIR){
                backup.getBlock().breakNaturally();
            } else {
                backup.getBlock().setType(backup.getMaterial());
                backup.getBlock().setData(backup.getData());
            }
        }
    }

    public int getPlayersInJaula(double radius) {
        int count = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if(!HGKits.getInstance().getPlayers().contains(player)) continue;

            Location loc = player.getLocation();
            double distance = loc.distance(center);

            if (distance <= radius) {
                count++;
            }
        }

        return count;
    }
}
