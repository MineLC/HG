package me.isra.hgkits.listeners;

import me.isra.hgkits.utils.menu.Menu;
import me.isra.hgkits.utils.menu.MenuButton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if (e.getClickedInventory() == null) {
            return;
        }
        if(e.getClickedInventory().getHolder() == null) return;
        if(e.getClickedInventory().getHolder() instanceof Menu){
            e.setCancelled(true);
            Menu menu = (Menu) e.getClickedInventory().getHolder();
            for (MenuButton button : menu.getButtons()) {
                if(e.getSlot() == button.getSlot()) button.getAction().accept(e.getClick());
            }
        }
    }

}
