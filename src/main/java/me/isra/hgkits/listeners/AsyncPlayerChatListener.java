package me.isra.hgkits.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import me.isra.hgkits.HGKits;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.database.DatabaseManager;
import me.isra.hgkits.database.User;
import me.isra.hgkits.managers.FameManager;
import me.isra.hgkits.managers.KitManager;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

public class AsyncPlayerChatListener implements Listener {
    private final KitManager kitManager;
    private final HGKits plugin;
    private final String chatFormat;
    private final String spectatorChatFormat;
    private final List<String> hoverText;

    public AsyncPlayerChatListener(KitManager kitManager, HGKits plugin) {
        this.kitManager = kitManager;
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.chatFormat = config.getString("chat.format", "[%fame%] <%kit% %player%> %message%");
        this.spectatorChatFormat = config.getString("chat.spectator-format", "&7[Spectator] %player%: %message%");
        this.hoverText = config.getStringList("chat.hover.text");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Kit kit = kitManager.getKitByPlayer(player);
        String kitName = (kit != null && kit.getName() != null) ? kit.getName() : "Default";
        User user = DatabaseManager.getDatabase().getCached(player.getUniqueId());
        if (user == null) {
            return;
        }
        String fameRank = FameManager.getFameRank(user.fame);
        String rankColor = FameManager.getRankColor(user.fame);
        String rank = FameManager.getRankByFame(user.fame);
        String message = event.getMessage();

        String format = player.getGameMode() == GameMode.SPECTATOR ? spectatorChatFormat : chatFormat;

        String formattedMessage = format
                .replace("%nivel-color%", rankColor)
                .replace("%nivel%", rank)
                .replace("%kit%", ChatColor.DARK_GRAY + "[" + kitName + "]" + ChatColor.RESET)
                .replace("%player%", player.getDisplayName())
                .replace("%kills%", String.valueOf(user.kills))
                .replace("%deaths%", String.valueOf(user.deaths))
                .replace("%wins%", String.valueOf(user.wins))
                .replace("%kdr%", String.format("%.2f", user.getKDR()))
                .replace("%message%", message);

        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            formattedMessage = PlaceholderAPI.setPlaceholders(player, formattedMessage);
        }

        formattedMessage = ChatColor.translateAlternateColorCodes('&', formattedMessage);

        if (formattedMessage.length() > 256) {
            formattedMessage = formattedMessage.substring(0, 256);
        }

        TextComponent textComponent = new TextComponent(formattedMessage);
        String hoverTextFormatted = String.join("\n", hoverText)
                .replace("%nivel-color%", rankColor)
                .replace("%nivel%", rank)
                .replace("%kit%", ChatColor.DARK_GRAY + "[" + kitName + "]" + ChatColor.RESET)
                .replace("%player%", player.getDisplayName())
                .replace("%kills%", String.valueOf(user.kills))
                .replace("%deaths%", String.valueOf(user.deaths))
                .replace("%wins%", String.valueOf(user.wins))
                .replace("%kdr%", String.format("%.2f", user.getKDR()));

        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            hoverTextFormatted = PlaceholderAPI.setPlaceholders(player, hoverTextFormatted);
        }

        hoverTextFormatted = ChatColor.translateAlternateColorCodes('&', hoverTextFormatted);

        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverTextFormatted).create()));

        event.setCancelled(true);
        for (Player p : event.getRecipients()) {
            p.spigot().sendMessage(textComponent);
        }
    }
}
