package me.isra.hgkits;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.isra.hgkits.database.DatabaseManager;
import me.isra.hgkits.database.User;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.managers.FameManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HexFormat;

import static me.isra.hgkits.HGKits.GAMESTATE;

public class HGExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "hgkits";
    }

    @Override
    public @NotNull String getAuthor() {
        return "MineLC";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        User user = DatabaseManager.getDatabase().getCached(player.getUniqueId());

        if(user != null){
            int playerCount = Bukkit.getOnlinePlayers().size();
            if (GAMESTATE == GameState.PREGAME || GAMESTATE == GameState.INVICIBILITY || GAMESTATE == GameState.GAME) {
                playerCount = HGKits.getInstance().getPlayers().size();
            }
            String rankColor = FameManager.getRankColor(user.fame);
            String rank = FameManager.getRankByFame(user.fame);
            return switch (params.toLowerCase()){
                case "kills" -> user.kills+"";
                case "wins" -> user.wins+"";
                case "deaths" -> user.deaths+"";
                case "kdr" -> user.getFormattedKDR();
                case "date" -> HGKits.getInstance().getDate();
                case "nivel" -> rank;
                case "nivel-color" -> rankColor;
                case "fame" -> user.fame+"";
                case "count" -> playerCount == 0 ? "-" : playerCount+"";
                default -> "%hgkits_"+params+"%";
            };
        }
        return "No encontrado";
    }
}
