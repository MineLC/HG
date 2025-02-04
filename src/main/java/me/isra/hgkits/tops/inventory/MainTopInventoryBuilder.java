package me.isra.hgkits.tops.inventory;

import me.isra.hgkits.tops.Top;
import me.isra.hgkits.tops.TopStorage;
import me.isra.hgkits.tops.TopType;
import me.isra.hgkits.translate.TranslateManager;
import me.isra.hgkits.utils.ItemBuilder;
import me.isra.hgkits.utils.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class MainTopInventoryBuilder extends Menu {

    private static final TopInventoryHolder TOP_HOLDER = new TopInventoryHolder();

    private final TranslateManager translateManager;

    public MainTopInventoryBuilder(Player viewer, TranslateManager translateManager){
        super(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("top-main-title")), 54);
        this.translateManager = translateManager;

        addButton(11, new ItemBuilder(Material.PAPER).setDisplayName("&a&lAsesinatos")
                .addLore("&7Click para ver a los jugadores con")
                .addLore("&7más asesinatos.").build(),
                clickType -> {
            new TopInventoryBuilder().build(viewer, TopStorage.kills(), ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("top-kills-title")), TopType.KILLS);
        });

        addButton(15, new ItemBuilder(Material.PAPER).setDisplayName("&c&lMuertes")
                        .addLore("&7Click para ver a los jugadores con")
                        .addLore("&7más muertes.").build(),
                clickType -> {
                    new TopInventoryBuilder().build(viewer, TopStorage.deaths(), ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("top-deaths-title")), TopType.DEATHS);
                });

        addButton(38, new ItemBuilder(Material.PAPER).setDisplayName("&e&lVictorias")
                        .addLore("&7Click para ver a los jugadores con")
                        .addLore("&7más victorias.").build(),
                clickType -> {
                    new TopInventoryBuilder().build(viewer, TopStorage.wins(), ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("top-wins-title")), TopType.WINS);
                });

        addButton(42, new ItemBuilder(Material.PAPER).setDisplayName("&6&lKDR")
                        .addLore("&7Click para ver a los jugadores con")
                        .addLore("&7mejor KDR.").build(),
                clickType -> {
                    new TopInventoryBuilder().build(viewer, TopStorage.kdr(), ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("top-kdr-title")), TopType.KDR);
                });

    }

}
