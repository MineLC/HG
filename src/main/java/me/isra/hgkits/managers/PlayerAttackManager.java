package me.isra.hgkits.managers;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class PlayerAttackManager {

    private final Set<Player> playersWhoAttacked = new HashSet<>(); //enderman

    // Añade un jugador a la lista de atacantes
    public void addPlayer(Player player) {
        playersWhoAttacked.add(player);
    }

    // Verifica si el jugador está en la lista de atacantes
    public boolean hasPlayerAttacked(Player player) {
        return playersWhoAttacked.contains(player);
    }

    // Elimina un jugador de la lista de atacantes
    public void removePlayer(Player player) {
        playersWhoAttacked.remove(player);
    }

    // Obtén el conjunto completo de jugadores que han atacado
    public Set<Player> getPlayersWhoAttacked() {
        return playersWhoAttacked;
    }
}
