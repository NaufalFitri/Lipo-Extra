package dev.lipoteam.lipoExtra.Files;

import dev.lipoteam.lipoExtra.LipoExtra;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.intellij.lang.annotations.Subst;

import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class LevelConfig {

    private LipoExtra plugin;
    private FileConfiguration config;

    public LevelConfig(FileConfiguration config, LipoExtra plugin) {

        this.config = config;
        this.plugin = plugin;

    }

    public String Prefix() {
        return config.getString("prefix");
    }

    public int Delay() {
        return config.getInt("delay");
    }

    public boolean Enabled() {
        return config.getBoolean("enabled");
    }

    public ConcurrentHashMap<String, Integer> worldlevel() {
        ConcurrentHashMap<String, Integer> worldlevel = new ConcurrentHashMap<>();
        for (String world : config.getConfigurationSection("worlds").getKeys(false)) {
            worldlevel.put(world, config.getInt("worlds." + world + ".level"));
        }
        return worldlevel;
    }

    public LinkedHashMap<String, Boolean> worldrtp() {
        LinkedHashMap<String, Boolean> worldrtp = new LinkedHashMap<>();
        for (String world : config.getConfigurationSection("worlds").getKeys(false)) {
            worldrtp.put(world, config.getBoolean("worlds." + world + ".rtp-on-enter"));
        }
        return worldrtp;
    }

    public String cannotEnter() {
        return config.getString("lang.cannot-enter");
    }

    public Sound SoundCantEnter() {
        @Subst("") String string = config.getString("sound.cannot-enter.sound");
        if (string != null) {
            return Sound.sound(Key.key(string), Sound.Source.valueOf(config.getString("sound.cannot-enter.source")),
                    (float) config.getDouble("sound.cannot-enter.radius"), (float) config.getDouble("sound.cannot-enter.pitch"));
        }
        return null;
    }


}
