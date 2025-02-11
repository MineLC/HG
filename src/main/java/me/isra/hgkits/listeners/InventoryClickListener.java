package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.data.KitInventory;
import me.isra.hgkits.database.DatabaseManager;
import me.isra.hgkits.database.User;
import me.isra.hgkits.enums.GameState;
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
            User user = DatabaseManager.getDatabase().getCached(event.getWhoClicked().getUniqueId());
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

                if(event.isLeftClick()) {
                    if (((selectedKit.cost() > 0 && !user.purchasedKits.contains(kitName)) && !user.allKits)) {
                        player.sendMessage(
                                ChatColor.RED + "No tienes permiso para seleccionar el kit " + selectedKit.name() + ".");
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1F, 1F);
                        return;
                    }

                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            translateManager.getMessage("kit-selected").replace("%kitName%", selectedKit.name())));
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
                    kitManager.addSelectedKit(player, selectedKit);
                    player.closeInventory();
                }else if(event.isRightClick()){
                    if(selectedKit.cost() > 0){
                        if(!user.purchasedKits.contains(kitName)){
                            if(!HGKits.getInstance().getEconomy().has(player, selectedKit.cost())){
                                player.sendMessage(
                                        ChatColor.RED + "No tienes suficientes LCoins.");
                                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1F, 1F);
                                return;
                            }
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eCompraste el Kit &6&o"+kitName+"&e."));
                            user.purchasedKits.add(kitName);
                            HGKits.getInstance().getEconomy().withdrawPlayer(player, selectedKit.cost());
                            player.closeInventory();
                            DatabaseManager.getDatabase().save(player);
                        }
                    }
                }
            }
            return;
        }
        
        if (HGKits.GAMESTATE == GameState.PREGAME) {
            event.setCancelled(true);
        }
    }
}
