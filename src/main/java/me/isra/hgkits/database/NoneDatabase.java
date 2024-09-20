package me.isra.hgkits.database;

import java.util.Map;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;

public class NoneDatabase implements Database {

    private final Map<UUID, User> cache = new HashMap<>();

    @Override
    public void save(Player player) {}
    @Override
    public void saveAll(Collection<? extends Player> players, SupplyOperation supply) {
    }

    @Override
    public void load(Player player, SupplyOperation operation) {
        final User user = new User(player.getUniqueId(), player.getName());
        cache.put(player.getUniqueId(), user);
        operation.execute();
    }

    @Override
    public User getCached(UUID uuid) {
        return cache.get(uuid);
    }

    @Override
    public Map<UUID, User> getUsers() {
        return cache;
    }

    @Override
    public void close() {
        cache.clear();
    }
}
