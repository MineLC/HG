package me.isra.hgkits.database;

import org.bukkit.plugin.java.JavaPlugin;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.database.mongodb.MongoDBManager;

public final class DatabaseManager {

    private static Database database;

    private Database load(final JavaPlugin plugin) {
        try {
            return new MongoDBManager().load(plugin.getConfig().getConfigurationSection("mongodb"));
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Error trying to enable the mongodb", e);
        }
        return new NoneDatabase();
    }

    public static Database getDatabase() {
        if (database != null) {
            return database;
        }
        database = new DatabaseManager().load(JavaPlugin.getPlugin(HGKits.class));
        return database;
    }
}