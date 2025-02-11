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
        if(e.getClickedInventory().getHolder() instanceof Menu menu){
            e.setCancelled(true);
            for (MenuButton button : menu.getButtons()) {
                if(e.getSlot() == button.getSlot()) button.getAction().accept(e.getClick());
            }
        }
    }

}
