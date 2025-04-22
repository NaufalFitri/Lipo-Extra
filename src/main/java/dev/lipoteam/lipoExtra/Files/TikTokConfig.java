package dev.lipoteam.lipoExtra.Files;

import dev.lipoteam.lipoExtra.LipoExtra;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.Subst;
import se.file14.procosmetics.cosmetic.pet.P;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TikTokConfig {

    private FileConfiguration config;
    private LipoExtra plugin;
    private Map<UUID, String> creators = new HashMap<>();

    public TikTokConfig(FileConfiguration config, LipoExtra plugin) {
        this.plugin = plugin;
        this.config = config;
        listCreators();
    }

    public String prefix() { return config.getString("prefix"); }

    public void listCreators() {
        for (String uuidStr : config.getConfigurationSection("creators").getKeys(false)) {
            String tiktok = config.getString("creators." + uuidStr);
            if (tiktok != null) {
                UUID id = UUID.fromString(uuidStr);
                creators.put(id, tiktok);
            }
        }
    }

    public String connectedMsg() { return config.getString("lang.connected"); }

    public String errorMsg() { return config.getString("lang.error"); }

    public String notValidMsg() { return config.getString("lang.not-valid"); }

    public String notOnlineMsg() { return config.getString("lang.not-online"); }

    public String notSetupMsg() { return config.getString("lang.not-setup"); }

    public String broadcast() { return config.getString("lang.broadcast"); }

    public String disconnectMsg() { return config.getString("lang.disconnect"); }

    public Sound broadcastSound() {
        @Subst("") String string = config.getString("sound.broadcast.sound");
        if (string != null) {
            return Sound.sound(Key.key(string), Sound.Source.valueOf(config.getString("sound.broadcast.source")),
                    (float) config.getDouble("sound.broadcast.radius"), (float) config.getDouble("sound.broadcast.pitch"));
        }
        return null;
    }

    public Sound disconnectSound() {
        @Subst("") String string = config.getString("sound.disconnect.sound");
        if (string != null) {
            return Sound.sound(Key.key(string), Sound.Source.valueOf(config.getString("sound.disconnect.source")),
                    (float) config.getDouble("sound.disconnect.radius"), (float) config.getDouble("sound.disconnect.pitch"));
        }
        return null;
    }

    public Map<UUID, String> getCreators() {
        return creators;
    }

}
