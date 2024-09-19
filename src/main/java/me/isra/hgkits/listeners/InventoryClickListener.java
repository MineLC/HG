package me.isra.hgkits.listeners;

import me.isra.hgkits.data.Kit;
import me.isra.hgkits.managers.KitManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryClickListener implements Listener {

    private final KitManager kitManager;

    public InventoryClickListener(KitManager kitManager) {
        this.kitManager = kitManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getView().getTitle().equals("Selecciona tu KIT")) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) {
                return;
            }

            ItemMeta itemMeta = clickedItem.getItemMeta();
            String kitNameInGreen = itemMeta.getDisplayName();
            String kitName = ChatColor.stripColor(kitNameInGreen);
            Kit selectedKit = kitManager.getKit(kitName);

            if (selectedKit != null) {
                player.sendMessage(ChatColor.GREEN + "Seleccionaste " + ChatColor.DARK_GREEN + ChatColor.ITALIC + selectedKit.getName() + ChatColor.RESET + ChatColor.GREEN + " como tu kit.");
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
                kitManager.addSelectedKit(player, selectedKit);
                player.closeInventory();
            }
        }
    }
}
