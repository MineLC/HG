package me.isra.hgkits.utils.menu;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public interface MenuButton {

    int getSlot();
    ItemStack getItem();
    Consumer<ClickType> getAction();
}
