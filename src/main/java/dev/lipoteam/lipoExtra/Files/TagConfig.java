package dev.lipoteam.lipoExtra.Files;

import dev.lipoteam.lipoExtra.LipoExtra;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TagConfig {

    private FileConfiguration config;
    private LipoExtra plugin;
    public ConcurrentHashMap<String, String> tags = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, String> perms = new ConcurrentHashMap<>();

    public TagConfig(FileConfiguration config, LipoExtra plugin) {

        this.config = config;
        this.plugin = plugin;
        setTags();

    }

    private void setTags() {

        for (String tag : config.getConfigurationSection("tags").getKeys(false)) {
            tags.put(tag, config.getString("tags." + tag + ".tagname"));
            perms.put(tag, config.getString("tags." + tag + ".permission"));
        }

    }

    public String AppliedMsg() {
        return config.getString("lang.applied");
    }

    public String NoPerms() {
        return config.getString("lang.no-perm");
    }

    public String prefix() { return config.getString("prefix"); }

    public String UnequipMsg() { return config.getString("lang.unequip"); }
    public String NoneTag() { return config.getString("lang.none-tag"); }

    public String getTagname(String tag) { return tags.get(tag); }

    public ConcurrentHashMap<String, String> getPerms() { return perms; }

    public String getPerms(String tag) { return perms.get(tag); }

    public ConcurrentHashMap.KeySetView<String, String> getTags() {
        return tags.keySet();
    }
}
