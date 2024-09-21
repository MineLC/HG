package me.isra.hgkits.database;

import java.util.Map;
import java.util.UUID;
import java.util.Collection;

import org.bukkit.entity.Player;

public interface Database {
    
    void save(final Player player);
    void saveAll(final Collection<? extends Player> players);
    void load(final Player player, final SupplyOperation supply);
    User getCached(UUID uuid);
    Map<UUID, User> getUsers();
    void close();
}