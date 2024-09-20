package me.isra.hgkits.tops;

import me.isra.hgkits.database.User;

public final class TopManager {
    public static void calculateKills(User player) {
        Top.Player topPlayer = new Top.Player(player.name, 0);
        topPlayer.value = player.kills;
        calculatePosition(topPlayer, TopStorage.kills());
    }

    public static void calculateDeaths(User player) {
        Top.Player topPlayer = new Top.Player(player.name, 0);
        topPlayer.value = player.deaths;
        calculatePosition(topPlayer, TopStorage.deaths());
    }

    public static void calculatePosition(Top.Player player, Top top) {
        int score = player.value;
        if (score == 0) {
            return; 
        }
        delete(player.name, top);
        Top.Player toMove = null;
        int i = 0;
        final int length = top.getPlayers().length;

        while (i < length) {
            Top.Player playerInTop = top.getPlayers()[i];
            if (playerInTop == null) {
                top.getPlayers()[i] = player;
                return;
            } 
            if (playerInTop.value >= score) {
                i++;
                continue;
            } 
            toMove = playerInTop;
            break;
        } 
        if (toMove == null) {
            return;
        } 
        moveOneLeft(i, toMove, top);
        top.getPlayers()[i] = player;
    }
  
    private static void delete(String playerName, Top top) {
        for (int i = 0; i < (top.getPlayers()).length; i++) {
            Top.Player playerInTop = top.getPlayers()[i];
            if (playerInTop == null)
                return; 
            if (playerInTop.name.equals(playerName)) {
                top.getPlayers()[i] = null;
                return;
            } 
        }
    }
  
    public static void moveOneLeft(int i, Top.Player next, Top top) {
        Top.Player current = top.getPlayers()[i];
        top.getPlayers()[i] = next;
        if (current == null || i + 1 == (top.getPlayers()).length) {
            return;
        }
        moveOneLeft(i + 1, current, top);
    }
}
