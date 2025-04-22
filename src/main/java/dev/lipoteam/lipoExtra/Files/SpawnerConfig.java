package dev.lipoteam.lipoExtra.Files;

import dev.lipoteam.lipoExtra.LipoExtra;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class SpawnerConfig {

    private FileConfiguration config;
    private LipoExtra plugin;

    public SpawnerConfig(FileConfiguration config, LipoExtra plugin) {

        this.config = config;
        this.plugin = plugin;

    }

    public boolean enabled() { return config.getBoolean("enabled"); }

    public String prefix() { return config.getString("prefix"); }

    public List<String> worlds() {
        return config.getStringList("worlds");
    }

    public int maxStacking() { return config.getInt("max-stacking"); }

    public int secStacking() { return config.getInt("sec-per-stacking"); }

    public int initialSeconds() { return config.getInt("initial-seconds"); }

    public String spawnerName() { return config.getString("hologram.name"); }

    public String spawnerTime() { return config.getString("hologram.time"); }

    public String MaxMsg() { return config.getString("lang.max"); }

}
