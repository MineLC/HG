package me.isra.hgkits.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

public final class ConfigManager {
    private final JavaPlugin optimum;
    private final Yaml yaml;

    public ConfigManager(Yaml yaml, JavaPlugin optimum) {
        this.yaml = yaml;
        this.optimum = optimum;
    }

    public Config of(final File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {   
            @SuppressWarnings("unchecked")
            final Map<String, Object> values = yaml.loadAs(reader, Map.class);
            return new Config(values);
        } catch (Exception e) {
            optimum.getLogger().log(Level.SEVERE, " Error reading the file " + ((file == null) ? "null" : file.getAbsolutePath()), e);
            return null;
        }
    }
    public Config of(String directory, final String file) {
        directory = (directory == null) ? "" : directory + '/';
        return of(new File(directory + file + ".yml"));
    }

    public void createIfAbsent(String directory, final String... files) {
        directory = (directory == null) ? "" : directory + '/';

        for (String filePath : files) {
            filePath = directory + filePath + ".yml";
            final File file = new File(optimum.getDataFolder(), filePath);
            if (file.exists()) {
                continue;
            }
            optimum.saveResource(filePath, false);
        }
    }

    public void create(String directory, final String... files) {
        directory = (directory == null) ? "" : directory + '/';
        for (final String filePath : files) {
            optimum.saveResource(directory + filePath + ".yml", false);
        }
    }

    public FileConfiguration config() {
        return optimum.getConfig();
    }

    public File datafolder() {
        return optimum.getDataFolder();
    }

    public Logger logger() {
        return optimum.getLogger();
    }
}
