package dev.lipoteam.lipoHud.Files;

import dev.lipoteam.lipoHud.DataManager;
import dev.lipoteam.lipoHud.LipoHud;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.intellij.lang.annotations.Subst;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Configurations {

    private final FileConfiguration config;
    private final Plugin plugin;
    private final DataManager manager;

    public Configurations(FileConfiguration config, LipoHud plugin) {
        this.config = config;
        this.plugin = plugin;
        manager = new DataManager(plugin);
    }

    public String prefix() {
        return config.getString("prefix");
    }

    public Boolean actionBarFilter() {
        return config.getBoolean("displays.ac-delay-if-interrupt");
    }

    public List<String> actionBarListFilter() {
        return config.getStringList("displays.ac-delay-if-string");
    }

    public long delayPeriod() {
        return config.getLong("displays.ac-delay-period");
    }

    public String JavaText() {
        return config.getString("displays.java.action-bar.text");
    }

    public String BedrockText() {
        return config.getString("displays.bedrock.action-bar.text");
    }

    public boolean JavaEnabled() {
        return config.getBoolean("displays.java.action-bar.enabled");
    }

    public boolean BedrockEnabled() {
        return config.getBoolean("displays.bedrock.action-bar.enabled");
    }

    public Set<String> ListCForms() {
        return Objects.requireNonNull(config.getConfigurationSection("displays.bedrock.custom-form")).getKeys(false);
    }

    public Set<String> ListCFormSections(String form) {
        return Objects.requireNonNull(config.getConfigurationSection("displays.bedrock.custom-form." + form)).getKeys(false);
    }

    public Set<String> ListSFormSections(String form) {
        return Objects.requireNonNull(config.getConfigurationSection("displays.bedrock.simple-form." + form)).getKeys(false);
    }

    public String getSectionSValue(String form, String section, String key) {
        return config.getString("displays.bedrock.custom-form." + form + "." + section + "." + key);
    }

    public List<String> getSectionSValues(String form, String section, String key) {
        return config.getStringList("displays.bedrock.custom-form." + form + "." + section + "." + key);
    }

    public List<Float> getSectionIValues(String form, String section, String key) {
        return config.getFloatList("displays.bedrock.custom-form." + form + "." + section + "." + key);
    }

    public Set<String> ListSForms() {
        return Objects.requireNonNull(config.getConfigurationSection("displays.bedrock.simple-form")).getKeys(false);
    }

    public String getSValue(String form, String key) {
        return config.getString("displays.bedrock.simple-form." + form + "." + key);
    }

    public String getSSectionSValue(String form, String section, String key) {
        return config.getString("displays.bedrock.simple-form." + form + "." + section + "." + key);
    }

    public Boolean OverrideEnabled() {
        return config.getBoolean("displays.inv-text-override");
    }

    public ConcurrentHashMap<String, String> InvTitleBedrock() {
        ConcurrentHashMap<String, String> MapList = new ConcurrentHashMap<>();

        Set<String> list = Objects.requireNonNull(config.getConfigurationSection("displays.inv-bedrock-title")).getKeys(false);

        for (String id : list) {
            MapList.put(id, Objects.requireNonNull(config.getString("displays.inv-bedrock-title." + id)));
        }

        return MapList;
    }

    public Boolean SoundOnCommand() { return config.getBoolean("sound.on-command.enabled"); }

    public Boolean CommandInterrupt() { return config.getBoolean("command.interrupt.enabled");}

    public ConcurrentHashMap<String, HashMap<String, Integer>> CommandInterruptList() {
        ConcurrentHashMap<String, HashMap<String, Integer>> MapList = new ConcurrentHashMap<>();
        for (String command : config.getConfigurationSection("command.interrupt").getKeys(false)) {
            if (command.equalsIgnoreCase("enabled")) {
                continue;
            }
            HashMap<String, Integer> newCommand = new HashMap<>();
            newCommand.put(config.getString("command.interrupt." + command + ".command"), config.getInt("command.interrupt." + command + ".delay"));
            if (newCommand != null) {
                MapList.put(command, newCommand);
            }

        }
        return MapList;
    }

    public Sound commandSound() {
        @Subst("") String string = config.getString("sound.on-command.sound");
        if (string != null) {
            return Sound.sound(Key.key(string), Sound.Source.PLAYER,
                    (float) config.getDouble("sound.on-command.volume"), (float) config.getDouble("sound.on-command.pitch"));
        }
        return null;
    }

    public Particle FlyParticle() {
        return Particle.valueOf(config.getString("particle.on-fly.particle"));
    }

    public double FlyParticleSpeed() {
        return config.getDouble("particle.on-fly.speed");
    }

    public double FlyParticleSpeedMove() {
        return config.getDouble("particle.on-fly.speed-move");
    }

    public int FlyParticleCount() {
        return config.getInt("particle.on-fly.count");
    }

    public List<World> DisableParticleWorlds() {
        List<World> worlds = new ArrayList<>();
        for (String world : config.getStringList("particle.on-fly.disabled-worlds")) {
            worlds.add(plugin.getServer().getWorld(world));
        }
        return worlds;
    }


}
