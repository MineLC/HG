package me.isra.hgkits.database.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.Set;
import java.util.HashMap;
import java.util.UUID;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.entity.Player;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import me.isra.hgkits.database.Database;
import me.isra.hgkits.database.SupplyOperation;
import me.isra.hgkits.database.User;

final class MongoDBImpl implements Database {

    private final Map<UUID, User> cache = new HashMap<>();
    private final MongoClient client;
    private final MongoCollection<Document> collection;
    private final ExecutorService service;

    private static final String
        KILLS = "kills",
        DEATHS = "deaths",
        FAME = "fame",
        WINS = "wins";

    MongoDBImpl(MongoClient client, MongoCollection<Document> collection, ExecutorService service) {
        this.client = client;
        this.collection = collection;
        this.service = service;
    }
    
    @Override
    public User getCached(UUID uuid) {
        return cache.get(uuid);
    }

    @Override
    public void save(final Player player) {
        final User data = cache.remove(player.getUniqueId());
        if (data == null) {
            return;
        }
        if (data.isNew()) {
            service.submit(() -> collection.insertOne(getNew(data)));
            return;
        }

        final Bson query = createUpdateQuery(data);
        if (query != null) {
            service.submit(() -> collection.updateOne(Filters.eq("_id", player.getName()), query));
        }
    }
    private Document getNew(final User user) {
        final Document document = new Document();

        document.put("_id", user.name);
        document.put(KILLS, user.kills);

        return document;
    }

    private Bson createUpdateQuery(final User data) {
        final List<Bson> update = new ArrayList<>();

        setIf(update, KILLS, data.kills, 0);
        setIf(update, DEATHS, data.deaths, 0);
        setIf(update, FAME, data.fame, 0);
        setIf(update, WINS, data.wins, 0);

        if (update.isEmpty()) {
            return null;
        } 
        return Updates.combine(update);
    }

    private void setIf(final List<Bson> updates, final String name, final Object value, final Object compare) {
        if (value != compare && !value.equals(compare)) {
            updates.add(Updates.set(name, value));
        }
    }

    @Override
    public void load(final Player player, final SupplyOperation operation) {
        service.submit(() -> {
            final Document document = collection.find(Filters.eq("_id", player.getName())).limit(1).first();
            if (document == null) {
                final User user = new User(player.getUniqueId(), player.getName());
                cache.put(player.getUniqueId(), user);
                operation.execute();
                return;
            }
        
            final User user = new User(
                player.getUniqueId(),
                player.getName()
            );
    
            user.kills = getOrDefault(document.getInteger(KILLS), 0);
            user.deaths = getOrDefault(document.getInteger(DEATHS), 0);
            user.fame = getOrDefault(document.getInteger(FAME), 0);
            user.wins = getOrDefault(document.getInteger(WINS), 0);

            cache.put(player.getUniqueId(), user);
            operation.execute();
        });
    }

    private <T> T getOrDefault(final T value, final T returnDefault) {
        return (value == null) ? returnDefault : value;
    }
    
    @Override
    public void close() {
        if (cache.isEmpty()) {
            service.shutdown();
            client.close();
            return;
        }
        final Set<Entry<UUID, User>> entries = cache.entrySet();
        final List<Document> toInsert = new ArrayList<>();

        for (final Entry<UUID, User> entry : entries) {
            if (entry.getValue().isNew()) {
                toInsert.add(getNew(entry.getValue()));
                return;
            }
            final Bson query = createUpdateQuery(entry.getValue());
            if (query != null) {
                collection.updateOne(Filters.eq("_id", entry.getValue().name), query);
            }
        }
        if (!toInsert.isEmpty()) {
            collection.insertMany(toInsert);
        }

        service.shutdown();
        client.close();
    }

    @Override
    public Map<UUID, User> getUsers() {
        return cache;
    }
}