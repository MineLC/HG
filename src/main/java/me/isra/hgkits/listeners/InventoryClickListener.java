package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.data.KitInventory;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.managers.KitManager;
import me.isra.hgkits.tops.inventory.TopInventoryHolder;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryClickListener implements Listener {

    private final KitManager kitManager;

    public InventoryClickListener(KitManager kitManager) {
        this.kitManager = kitManager;
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }
        final InventoryHolder holder = event.getClickedInventory().getHolder();
        if (holder instanceof TopInventoryHolder) {
            event.setCancelled(true); 
            return;
        }

        if (holder instanceof KitInventory) {
            event.setCancelled(true);

            final ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) {
                return;
            }

            final ItemMeta itemMeta = clickedItem.getItemMeta();
            final String kitNameInGreen = itemMeta.getDisplayName();
            final String kitName = ChatColor.stripColor(kitNameInGreen);
            final Kit selectedKit = kitManager.getKit(kitName);

            if (selectedKit != null) {
                final Player player = (Player) event.getWhoClicked();
                player.sendMessage(ChatColor.GREEN + "Seleccionaste " + ChatColor.DARK_GREEN + ChatColor.ITALIC + selectedKit.getName() + ChatColor.RESET + ChatColor.GREEN + " como tu kit.");
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
                kitManager.addSelectedKit(player, selectedKit);
                player.closeInventory();
            }
            return;
        }
        if(HGKits.GAMESTATE == GameState.PREGAME) {
            event.setCancelled(true);
        }
    }
}
