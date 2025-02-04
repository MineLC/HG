package me.isra.hgkits.menu;

import me.clip.placeholderapi.PlaceholderAPI;
import me.isra.hgkits.database.User;
import me.isra.hgkits.managers.FameManager;
import me.isra.hgkits.translate.TranslateManager;
import me.isra.hgkits.utils.ItemBuilder;
import me.isra.hgkits.utils.menu.Menu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PlayerStatsMenu extends Menu {

    private final TranslateManager translateManager;

    public PlayerStatsMenu(Player player, TranslateManager translateManager, User user) {
        super(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("statistics")), 6);
        this.translateManager = translateManager;

        String lcoins = PlaceholderAPI.setPlaceholders(player, "%lc_lcoins%");
        String vippoints = PlaceholderAPI.setPlaceholders(player, "%lc_vippoints%");

        addButton(10, new ItemBuilder(Material.DIAMOND_SWORD)
                .setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6Asesinatos"))
                .addLore(ChatColor.translateAlternateColorCodes('&', "&7" + user.kills))
                .build(), click -> {});

        addButton(12, new ItemBuilder(Material.SKULL_ITEM)
                .setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6Muertes"))
                .addLore(ChatColor.translateAlternateColorCodes('&', "&7" + user.deaths))
                .build(), click -> {});

        addButton(14, new ItemBuilder(Material.IRON_AXE)
                .setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6KDR"))
                .addLore(ChatColor.translateAlternateColorCodes('&', "&7" + String.format("%.2f", user.getKdr())))
                .build(), click -> {});

        addButton(16, new ItemBuilder(Material.GOLDEN_APPLE).setAmount(1)
                .setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6Victorias"))
                .addLore(ChatColor.translateAlternateColorCodes('&', "&7" + user.wins))
                .build(), click -> {});

        addButton(28, new ItemBuilder(Material.GOLD_INGOT)
                .setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6LCoins"))
                .addLore(ChatColor.translateAlternateColorCodes('&', "&7" + lcoins))
                .build(), click -> {});

        addButton(30, new ItemBuilder(Material.DIAMOND)
                .setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6VIPPoints"))
                .addLore(ChatColor.translateAlternateColorCodes('&', "&7" + vippoints))
                .build(), click -> {});

        addButton(32, new ItemBuilder(Material.EXP_BOTTLE)
                .setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6Fama"))
                .addLore(ChatColor.translateAlternateColorCodes('&', "&7" + user.fame))
                .build(), click -> {});

        addButton(34, new ItemBuilder(Material.NETHER_STAR)
                .setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6Nivel"))
                .addLore(ChatColor.translateAlternateColorCodes('&', "&7" + FameManager.getRankByFame(user.fame)))
                .build(), click -> {});
    }
}
