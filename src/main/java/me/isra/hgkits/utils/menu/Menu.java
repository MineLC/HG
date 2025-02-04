package me.isra.hgkits.utils.menu;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class Menu implements InventoryHolder {

    private final Inventory inventory;
    @Getter
    private final Set<MenuButton> buttons = new HashSet<>();

    public Menu(String title, int rows) {
        inventory = Bukkit.createInventory(this, rows*9, title);
    }

    public void open(Player player){
        write();
        player.openInventory(inventory);
    }

    public void refresh() {
        inventory.clear();
        write();
    }

    public void write(){
        for(MenuButton button : buttons){
            inventory.setItem(button.getSlot(), button.getItem());
        }
    }

    public void addButton(MenuButton button){
        buttons.add(button);
    }
    public void addButton(int slot, ItemStack item, Consumer<ClickType> onclick){
        buttons.add(new MenuButton() {
            @Override
            public int getSlot() {
                return slot;
            }

            @Override
            public ItemStack getItem() {
                return item;
            }

            @Override
            public Consumer<ClickType> getAction() {
                return onclick;
            }

        });
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
