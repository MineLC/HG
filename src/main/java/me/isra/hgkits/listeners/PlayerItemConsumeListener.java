package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.managers.KitManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerItemConsumeListener implements Listener {
    private final HGKits plugin;
    private final KitManager kitManager;
    private final Set<UUID> playersWithCookieEffect = new HashSet<>();
    private final Set<UUID> playersWithAppleEffect = new HashSet<>();

    public PlayerItemConsumeListener(HGKits plugin, KitManager kitManager) {
        this.plugin = plugin;
        this.kitManager = kitManager;
    }

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Kit kit = kitManager.getKitByPlayer(player);

        if(kit == null ) {
            return;
        }

        if (kit.getName().equals("Asesino") || kit.getName().equals("Brujo") || kit.getName().equals("Caballero")) {
            if (event.getItem().getType() == Material.COOKIE) {
                if (!playersWithCookieEffect.contains(player.getUniqueId())) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 0));
                    playersWithCookieEffect.add(player.getUniqueId());

                    player.getServer().getScheduler().runTaskLater(plugin, () -> {
                        playersWithCookieEffect.remove(player.getUniqueId());
                    }, 100);
                }
            }
        }

        if (kit.getName().equals("Camaleon")) {
            if (event.getItem().getType() == Material.APPLE) {
                if (!playersWithAppleEffect.contains(player.getUniqueId())) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0));
                    playersWithAppleEffect.add(player.getUniqueId());

                    player.getServer().getScheduler().runTaskLater(plugin, () -> {
                        playersWithAppleEffect.remove(player.getUniqueId());
                    }, 200);
                }
            }
        }
    }
}
