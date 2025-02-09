package me.isra.hgkits.tops.inventory;

import me.isra.hgkits.database.DatabaseManager;
import me.isra.hgkits.tops.TopType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.isra.hgkits.tops.Top;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class TopInventoryBuilder {
    
    private static final TopInventoryHolder TOP_HOLDER = new TopInventoryHolder();
    
    public void build(Player player, Top top, String title, TopType type) {
        final int amountTops = top.getPlayers().length;
        Inventory inventory = Bukkit.createInventory(TOP_HOLDER, calculateRows(amountTops), title);

        for (int i = 0; i < amountTops; i++) {
            Top.Player topPlayer = top.getPlayers()[i];
            if (topPlayer == null) {
                break; 
            }
            int topPos = i + 1;
            ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
            meta.setOwner(topPlayer.name);
            meta.setDisplayName("§6§l#" + topPos + " §8- §c" + topPlayer.name);
            String value = type != TopType.KDR ? String.valueOf(topPlayer.value) : String.format("%.2f", DatabaseManager.getDatabase().getCached(player.getUniqueId()).getKDR());
            meta.setLore(List.of(
                    "§7" + value + " " + type.displayName + (type != TopType.KDR ? (topPlayer.value != 1 ? "s" : "") : "")
            ));

            itemStack.setItemMeta(meta);
            inventory.setItem(i, itemStack);
        } 
        player.openInventory(inventory);
    }

    
    private int calculateRows(int amountTops) {
        if (amountTops <= 9) {
            return 9; 
        }
        if (amountTops % 9 == 0) {
            return amountTops;    
        }
        return 9 * (amountTops / 9 + 1);
    }
}