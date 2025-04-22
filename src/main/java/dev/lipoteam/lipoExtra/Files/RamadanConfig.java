package dev.lipoteam.lipoExtra.Files;

import dev.lipoteam.lipoExtra.Manager.DataManager;
import dev.lipoteam.lipoExtra.LipoExtra;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class RamadanConfig {

    private final FileConfiguration config;
    private final Plugin plugin;
    private final DataManager manager;

    public RamadanConfig(FileConfiguration config, LipoExtra plugin) {
        this.config = config;
        this.plugin = plugin;
        manager = new DataManager(plugin);
    }

    public String prefix() {
        return config.getString("prefix");
    }

    public Boolean enabled() {
        return config.getBoolean("enabled");
    }

    public List<PotionEffectType> sahurEffect() {
        List<PotionEffectType> list = new ArrayList<>();
        config.getStringList("eat-sahur-effects").forEach(effect -> {
            list.add(Registry.EFFECT.get(NamespacedKey.minecraft(effect)));
        });
        return list;
    }

    public Integer sahurDuration() {
        return config.getInt("sahur-effect-duration");
    }

    public Integer sahurAmplifier() {
        return config.getInt("sahur-effect-amplifier");
    }

    public World getWorld() {
        return Bukkit.getWorld(config.getString("world"));
    }

}
