package me.isra.hgkits.config;

import me.isra.hgkits.HGKits;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.Map;

public class DatabaseConfig {

    private String host;
    private int port;
    private String username;
    private String password;
    private String name;
    private String collection;


    public DatabaseConfig(HGKits plugin) {
        Map<String, Object> dbConfig = getStringObjectMap(plugin);

        this.host = (String) dbConfig.getOrDefault("host", "localhost");
        this.port = (Integer) dbConfig.getOrDefault("port", 27017);
        this.username = (String) dbConfig.getOrDefault("username", "No username");
        this.password = (String) dbConfig.getOrDefault("password", "No password");
        this.name = (String) dbConfig.getOrDefault("name", "");
        this.collection = (String) dbConfig.getOrDefault("collection", "No collection");

    }

    private static Map<String, Object> getStringObjectMap(HGKits plugin) {
        ConfigManager configManager = new ConfigManager(new Yaml(), plugin);
        configManager.createIfAbsent("db", "config");
        File dbConfigFile = new File(plugin.getDataFolder() + "/db/config.yml");
        Config config = configManager.of(dbConfigFile);

        return config.getMap("database");
    }


    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getCollection() {
        return collection;
    }
}
