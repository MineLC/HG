package me.isra.hgkits.database.mongodb;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bukkit.configuration.ConfigurationSection;

import com.mongodb.Block;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.connection.ClusterSettings;

public final class MongoDBManager {

    public MongoDBImpl load(final ConfigurationSection mongodb) {
        final String database = mongodb.getString("database", "minelc");
        final String collection = mongodb.getString("collection", "data");
    
        final MongoCredential credential = MongoCredential.createCredential(
            mongodb.getString("user"),
            database,
            mongodb.getString("pass").toCharArray()
        );

        final Block<ClusterSettings.Builder> localhost = builder -> builder.hosts(List.of(new ServerAddress(
            mongodb.getString("ip", "localhost"),
            mongodb.getInt("port", 27017))));

        final MongoClientSettings settings = MongoClientSettings.builder()
            .applyToClusterSettings(localhost)
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .credential(credential)
            .build();

        Logger.getLogger("org.mongodb").setLevel(Level.SEVERE);
        Logger.getLogger("com.mongodb").setLevel(Level.SEVERE);

        final MongoClient client = MongoClients.create(settings);

        final MongoDatabase mongoDatabase = client.getDatabase(database);
        final MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collection);
        final Document document = mongoCollection.find().limit(1).first();

        if (document == null) {
            mongoDatabase.createCollection(collection);
            mongoCollection.insertOne(new Document());
        }
        int threads = mongodb.getInt("executor-threads");
        if (threads == 0) {
            threads = 1;
        }
        return new MongoDBImpl(client, mongoCollection, Executors.newFixedThreadPool(threads));
    }
}