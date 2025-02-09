package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.data.KitInventory;
import me.isra.hgkits.database.DatabaseManager;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.enums.KitCategory;
import me.isra.hgkits.managers.KitManager;
import me.isra.hgkits.tops.inventory.TopInventoryHolder;
import me.isra.hgkits.translate.TranslateManager;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class InventoryClickListener implements Listener {

    private final KitManager kitManager;
    private final TranslateManager translateManager;

    public InventoryClickListener(KitManager kitManager, TranslateManager translateManager) {
        this.kitManager = kitManager;
        this.translateManager = translateManager;
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
                // Obtener la categoría del kit directamente desde el Map
                KitCategory category = KitCategory.fromKitName(kitName);

                String requiredPermission = category.getPermission();

                if (!player.hasPermission(requiredPermission) && !DatabaseManager.getDatabase().getCached(player.getUniqueId()).allKits) {
                    player.sendMessage(
                            ChatColor.RED + "No tienes permiso para seleccionar el kit " + selectedKit.getName() + ".");
                    player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1F, 1F);
                    return;
                }

                Map<String, String> variables = new HashMap<>();
                variables.put("kitName", selectedKit.getName());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        translateManager.getMessage("kit-selected").replace("%kitName%", selectedKit.getName())));
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
                kitManager.addSelectedKit(player, selectedKit);
                player.closeInventory();
            }
            return;
        }
        
        if (HGKits.GAMESTATE == GameState.PREGAME) {
            event.setCancelled(true);
        }
    }
}
