package me.isra.hgkits.translate;

import me.isra.hgkits.config.Config;
import me.isra.hgkits.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class TranslateManager {
    private final ConfigManager configManager;
    private Config messagesConfig;

    public TranslateManager(JavaPlugin plugin) {
        this.configManager = new ConfigManager(new org.yaml.snakeyaml.Yaml(), plugin);
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.messagesConfig = configManager.of(plugin.getDataFolder().getPath(), "messages");
        if (this.messagesConfig == null) {
            plugin.getLogger().severe("Failed to load messages.yml. Please ensure the file exists and is properly formatted.");
        }
    }

    public String getMessage(String key) {
        return messagesConfig.getString(key);
    }

    public String getMessageOrDefault(String key, String defaultValue) {
        return messagesConfig.getOrDefault(key, defaultValue);
    }

    public List<String> getMessageList(String key) {
        return (List<String>) messagesConfig.getList(key);
    }
}
