package dev.lipoteam.lipoExtra.Files;

import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.PositionSongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import dev.lipoteam.lipoExtra.LipoExtra;
import dev.lipoteam.lipoExtra.Manager.PinataRewards;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.intellij.lang.annotations.Subst;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PinataConfig {

    private final FileConfiguration config;
    private final LipoExtra plugin;

    public PinataConfig(FileConfiguration config, LipoExtra plugin) {

        this.config = config;
        this.plugin = plugin;

    }

    public String Prefix() { return config.getString("prefix"); }

    public boolean Enabled() { return config.getBoolean("enabled"); }

    public Location PinataLocation() {
        String loc = config.getString("pinata-spawn");
        if (loc != null) {
            String world = loc.split(" ")[0];
            World world1 = Bukkit.getWorld(world);
            double x = Double.parseDouble(loc.split(" ")[1].split(",")[0]);
            double y = Double.parseDouble(loc.split(" ")[1].split(",")[1]);
            double z = Double.parseDouble(loc.split(" ")[1].split(",")[2]);
            if (world1 != null) {
                return new Location(world1, x,y,z);
            }
        }
        return null;
    }

    public boolean WhackEnabled() { return config.getBoolean("party-types.whack-it.enabled"); }

    public EntityType WhackEntity() {
        EntityType entity;
        String type = config.getString("party-types.whack-it.entity");
        if (type != null) {
            entity = EntityType.valueOf(type);
            return entity;
        }
        return null;
    }

    public boolean WhackIsBaby() {
        return config.getBoolean("party-types.whack-it.is-baby");
    }

    public String broadcastchat() {
        if (config.getStringList("broadcast.types").contains("chat")) {
            return config.getString("lang.broadcast-chat");
        } else {
            return "";
        }
    }

    public String broadcastactionbar() {
        if (config.getStringList("broadcast.types").contains("actionbar")) {
            return config.getString("lang.broadcast-actionbar");
        } else {
            return "";
        }
    }

    public String broadcasttitle() {
        if (config.getStringList("broadcast.types").contains("title")) {
            return config.getString("lang.broadcast-title");
        } else {
            return "";
        }
    }

    public String broadcastsubtitle() {
        if (config.getStringList("broadcast.types").contains("subtitle")) {
            return config.getString("lang.broadcast-subtitle");
        } else {
            return "";
        }
    }

    public String broadcastend() {
        return config.getString("lang.broadcast-end");
    }

    public List<String> startCommands() {
        return config.getStringList("party-start-commands");
    }

    public void setCurrentVote(int vote) {
        config.set("vote-current", vote);
        try {
            config.save(Paths.get(plugin.getDataFolder().toString(), "Extras", "pinata.yml").toFile());
        } catch (IOException e) {
            plugin.getLogger().warning(String.valueOf(e));
        }
    }
    public int broadcasttime() { return config.getInt("broadcast.time"); }

    public String voteRemoveMsg() { return config.getString("lang.vote-remove"); }

    public String voteAddMsg() { return config.getString("lang.vote-add"); }

    public String voteResetMsg() { return config.getString("lang.vote-reset"); }

    public int voteThreshold() { return config.getInt("vote-threshold"); }

    public int voteCurrent() { return config.getInt("vote-current"); }

    public int TitleFadeIn() { return config.getInt("broadcast.title-fadein"); }

    public int TitleFadeOut() { return config.getInt("broadcast.title-fadeout"); }

    public int TitleFadeStay() { return config.getInt("broadcast.title-stay"); }

    public int WhackDuration() { return config.getInt("party-types.whack-it.duration-game"); }

    public int WhackDurationDespawn() { return config.getInt("party-types.whack-it.duration-despawn"); }

    public int WspawnParticleCount() { return config.getInt("party-types.whack-it.particle-spawn.count"); }

    public double WspawnParticleSpeed() { return config.getDouble("party-types.whack-it.particle-spawn.speed"); }

    public String WspawnParticle() { return config.getString("party-types.whack-it.particle-spawn.particle"); }

    public int WhitParticleCount() { return config.getInt("party-types.whack-it.particle-hit.count"); }

    public double WhitParticleSpeed() { return config.getDouble("party-types.whack-it.particle-hit.speed"); }

    public String WhitParticle() { return config.getString("party-types.whack-it.particle-hit.particle"); }

    public List<PinataRewards> Rewards() {
        List<Map<?,?>> rewards = config.getMapList("rewards");
        List<PinataRewards> pinataRewards = new ArrayList<>();

        for (Map<?, ?> child : rewards) {
            String type = (String) child.get("type");
            int chance = (int) child.get("chance");
            List<String> commands = (List<String>) child.get("commands");
            String sound = (String) child.get("sound");
            pinataRewards.add(new PinataRewards(type, chance, commands, sound));
        }

        return pinataRewards;

    }

    public PositionSongPlayer PinataSong() {
        String path = config.getString("song.path");

        if (Objects.equals(path, "nothing") || path == null) return null;

        File songFile = new File(path);

        if (!songFile.exists()) {
            plugin.getLogger().warning("Song file not found at: " + songFile.getAbsolutePath());
            return null;
        }

        Song song = NBSDecoder.parse(songFile);
        PositionSongPlayer psp = new PositionSongPlayer(song);
        psp.setTargetLocation(PinataLocation());
        psp.setDistance(16);
        return psp;
    }

    public String WhackArea() {
        return config.getString("party-types.whack-it.area"); }

    public String SoundCounterName() { return config.getString("broadcast.sound-count"); }

    public Sound WhackSoundChange() {
        @Subst("") String string = config.getString("party-types.whack-it.sound-change");
        if (string != null) {
            return Sound.sound(Key.key(string), Sound.Source.PLAYER,
                    (float) 1, (float) 1);
        }
        return null;
    }

    public Sound PinataSoundCounter(@Subst("") String sound, float pitch) {
        if (sound != null) {
            return Sound.sound(Key.key(sound), Sound.Source.PLAYER,
                    (float) 1, pitch);
        }
        return null;
    }

    public Sound PinataSoundStart() {
        @Subst("") String string = config.getString("broadcast.sound");
        if (string != null) {
            return Sound.sound(Key.key(string), Sound.Source.PLAYER,
                    (float) 1, (float) 1);
        }
        return null;
    }

    public int WhackMultiplier() {
        return config.getInt("party-types.whack-it.multiplier");
    }

}
