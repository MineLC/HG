package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.enums.GameState;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerDropItemListener implements Listener {

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if(HGKits.GAMESTATE == GameState.PREGAME) {
            ItemStack item = event.getItemDrop().getItemStack();
            if (item.getType() == Material.BOW) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && ChatColor.stripColor(meta.getDisplayName()).equals("Selector de KIT")) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
