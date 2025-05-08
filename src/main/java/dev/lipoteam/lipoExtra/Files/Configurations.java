package dev.lipoteam.lipoExtra.Files;

import dev.lipoteam.lipoExtra.Manager.DataManager;
import dev.lipoteam.lipoExtra.LipoExtra;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.intellij.lang.annotations.Subst;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Configurations {

    private final FileConfiguration config;

    private FileConfiguration coinsconfig;
    private FileConfiguration ranksconfig;

    private final Plugin plugin;
    private final DataManager manager;

    public Configurations(FileConfiguration config, LipoExtra plugin) {
        this.config = config;
        this.plugin = plugin;

        this.coinsconfig = plugin.CoinsConfig();
        this.ranksconfig = plugin.RanksConfig();

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

    public ConcurrentHashMap<String, Map.Entry<String, Integer>> CommandInterruptList() {
        ConcurrentHashMap<String, Map.Entry<String, Integer>> mapList = new ConcurrentHashMap<>();

        for (String command : config.getConfigurationSection("command.interrupt").getKeys(false)) {
            if (command.equalsIgnoreCase("enabled")) {
                continue;
            }

            String commandName = config.getString("command.interrupt." + command + ".command");
            int delay = config.getInt("command.interrupt." + command + ".delay");

            if (commandName != null) {
                mapList.put(command, new AbstractMap.SimpleEntry<>(commandName, delay));
            }
        }
        return mapList;
    }

    public Sound commandSound() {
        @Subst("") String string = config.getString("sound.on-command.sound");
        if (string != null) {
            return Sound.sound(Key.key(string), Sound.Source.PLAYER,
                    (float) config.getDouble("sound.on-command.volume"), (float) config.getDouble("sound.on-command.pitch"));
        }
        return null;
    }

    public Particle CVParticle() {
        return Particle.valueOf(config.getString("chunkvisualizer.particle"));
    }

    public double CVParticleSpeed() {
        return config.getDouble("chunkvisualizer.speed");
    }

    public int CVParticleCount() {
        return config.getInt("chunkvisualizer.count");
    }

    public String CVItem() { return config.getString("chunkvisualizer.item.material"); }

    public String CVItemName() { return config.getString( "chunkvisualizer.item.name"); }

    public List<String> CVItemLore() { return config.getStringList("chunkvisualizer.item.lore"); }

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

    public long FlyParticleTicks() { return config.getLong("particle.on-fly.ticks"); }

    public int FlyPartileRadius() { return config.getInt("particle.on-fly.radius"); }

    public boolean FlyCMIEnabled() { return config.getBoolean("cmi-addon.pve-end.enabled"); }

    public String FlyCMICommand() { return config.getString("cmi-addon.pve-end.command"); }

    public boolean RechargeLimit() { return config.getBoolean("cmi-addon.recharge-limit.enabled"); }

    public int RechargeLimitNum() { return config.getInt("cmi-addon.recharge-limit.limit"); }

    public String ChargeLimitMsg() { return config.getString("cmi-addon.recharge-limit.msg"); }

    public boolean ProcosmeticsHook() { return config.getBoolean("procosmetics.hook.enabled"); }

    public String ProcosmeticsCurrency() { return config.getString("procosmetics.hook.currency"); }

    public String ProcosmeticsParticle() { return config.getString("procosmetics.particle"); }

    public String ProgressBar() { return config.getString("placeholderapi.progress"); }

    public int ProgressLength() { return config.getInt("placeholderapi.bar-length"); }

    public String ProgressChar() { return config.getString("placeholderapi.bar-char"); }

    public boolean NoteBlockAPIHook() { return config.getBoolean("NoteBlockAPI.enabled"); }

    public String ModeChangeMsg() { return config.getString("lang.particle-mode-change"); }

    public ConcurrentHashMap<String, Integer> playerCoins() {
        ConcurrentHashMap<String, Integer> pc = new ConcurrentHashMap<>();

        if (coinsconfig.getConfigurationSection("players") == null) {
            return pc;
        }

        for (String name : coinsconfig.getConfigurationSection("players").getKeys(false)) {
            String convertname = name;

            if (name.startsWith("_") && !name.startsWith("__")) {
                convertname = name.replaceFirst("_", ".");
            }

            pc.put(convertname, coinsconfig.getInt("players." + name));
        }

        return pc;
    }

    public ConcurrentHashMap<String, String> playerRanks() {
        ConcurrentHashMap<String, String> pr = new ConcurrentHashMap<>();

        if (ranksconfig.getConfigurationSection("players") == null) {
            return pr;
        }

        for (String name : ranksconfig.getConfigurationSection("players").getKeys(false)) {
            String convertname = name;

            if (name.startsWith("_") && !name.startsWith("__")) {
                convertname = name.replaceFirst("_", ".");
            }

            String rank = ranksconfig.getString("players." + name);
            if (rank != null) {
                pr.put(convertname, rank);
            }
        }

        return pr;
    }

    public String FetchTitle() {
        return config.getString("fetch.title");
    }

    public String FetchSubtitle() {
        return config.getString("fetch.subtitle");
    }

    public List<Integer> FetchDuration() {
        List<Integer> duration = new ArrayList<>();
        duration.add(config.getInt("fetch.fade-in"));
        duration.add(config.getInt("fetch.stay"));
        duration.add(config.getInt("fetch.fade-out"));
        return duration;
    }

    public List<String> FetchCoinsMsg() {
        return config.getStringList("fetch.coins.messages");
    }

    public List<String> FetchRanksMsg() {
        return config.getStringList("fetch.ranks.messages");
    }

    public boolean FetchEnabled() {
        return config.getBoolean("fetch.enabled");
    }

//    beta-items:
//    enabled: true
//            # true = beta, false = notbeta
//    beta-or-notbeta: true
//    blacklist-items:
//            - SHULKER_BOX
//            - BUNDLE

    public boolean BetaItemsEnabled() { return config.getBoolean("beta-items.enabled");}

    public boolean BetaOrNotBeta() { return config.getBoolean("beta-items.beta-or-notbeta"); }

    public List<String> BlacklistBetaItems() { return config.getStringList("beta-items.blacklist-items"); }

    public int StarsScaleFactor() { return config.getInt("beta-items.stars-scale-factor"); }

}
