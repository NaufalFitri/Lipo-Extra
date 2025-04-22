package dev.lipoteam.lipoHud.Files;

import dev.lipoteam.lipoHud.DataManager;
import dev.lipoteam.lipoHud.LipoHud;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RamadanConfig {

    private final FileConfiguration config;
    private final Plugin plugin;
    private final DataManager manager;

    public RamadanConfig(FileConfiguration config, LipoHud plugin) {
        this.config = config;
        this.plugin = plugin;
        manager = new DataManager(plugin);
    }

    public String prefix(String text) {
        String newtext = "";
        if (config.getString("prefix") != null)
            newtext = Objects.requireNonNull(config.getString("prefix")) + " " + text;
        return manager.hex(newtext);
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


}
