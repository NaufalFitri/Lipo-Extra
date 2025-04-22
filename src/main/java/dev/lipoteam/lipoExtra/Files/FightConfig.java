package dev.lipoteam.lipoHud.Files;

import dev.lipoteam.lipoHud.DataManager;
import dev.lipoteam.lipoHud.LipoHud;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class FightConfig {

    private final FileConfiguration config;
    private final Plugin plugin;
    private final DataManager manager;

    private final Team spectators;

    public FightConfig(FileConfiguration config, LipoHud plugin) {
        this.config = config;
        this.plugin = plugin;
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        Scoreboard board = scoreboardManager.getMainScoreboard();
        if (!board.getTeams().contains(board.getTeam("fight_spectators"))) {
            spectators = board.registerNewTeam("fight_spectators");
        } else {
            spectators = board.getTeam("fight_spectators");
        }
        if (spectators != null) {
            spectators.setCanSeeFriendlyInvisibles(true);
            spectators.setAllowFriendlyFire(false);
        }

        manager = new DataManager(plugin);
    }

    public Team getSpectatorsTeam() {
        return spectators;
    }

    public String prefix() {
        return config.getString("prefix");
    }

    public String FightMessage() {
        return Objects.requireNonNull(config.getString("lang.fight-message"));
    }

    public String FightAlreadyChallenge() {
        return Objects.requireNonNull(config.getString("lang.fight-already-challenge"));
    }

    public String FightWaitChallenger() {
        return Objects.requireNonNull(config.getString("lang.fight-wait-challenger"));
    }

    public String AlreadyInFight() {
        return Objects.requireNonNull(config.getString("lang.already-in-fight"));
    }

    public String DeclineMessage() {
        return Objects.requireNonNull(config.getString("lang.decline-fight"));
    }

    public String AcceptMessage() {
        return Objects.requireNonNull(config.getString("lang.accept-fight"));
    }

    public String FullMessage() {
        return Objects.requireNonNull(config.getString("lang.arena-full"));
    }

    public String LeaveMessage() {
        return Objects.requireNonNull(config.getString("lang.leave-fight"));
    }

    public String LostMessage() {
        return Objects.requireNonNull(config.getString("lang.lose-fight"));
    }

    public String WinMessage() {
        return Objects.requireNonNull(config.getString("lang.win-fight"));
    }

    public String CannotCommand() {
        return Objects.requireNonNull(config.getString("lang.prevent-command"));
    }

    public String CannotSpectate() {
        return Objects.requireNonNull(config.getString("lang.cant-spectate"));
    }

    public String CannotTeleport() {
        return Objects.requireNonNull(config.getString("lang.cant-teleport"));
    }

    public String CurrentlySpectate() {
        return Objects.requireNonNull(config.getString("lang.currently-spectate"));
    }

    public String DurationMessage() {
        return Objects.requireNonNull(config.getString("lang.fight-duration"));
    }

    public String DrawMessage() {
        return Objects.requireNonNull(config.getString("lang.draw-fight"));
    }

    public String WinBroadcast() {
        return Objects.requireNonNull(config.getString("lang.win-broadcast"));
    }

    public int commandExpired() {
        return config.getInt("challenge-command-expired");
    }

    public int WinFightBack() {
        return config.getInt("win-fight-back-wait");
    }

    public int FightDuration() {
        return config.getInt("fight-duration");
    }

    public List<String> ConsoleCommands() {
        return config.getStringList("console-commands");
    }

    public Sound ChallengeSound() {
        @Subst("") String string = config.getString("sound.challenge-send.sound");
        if (string != null) {
            return Sound.sound(Key.key(string), Sound.Source.valueOf(config.getString("sound.challenge-send.source")),
                    (float) config.getDouble("sound.challenge-send.radius"), (float) config.getDouble("sound.challenge-send.pitch"));
        }
        return null;
    }
    public Sound ChallengeAcceptSound() {
        @Subst("") String string = config.getString("sound.challenge-accept.sound");
        if (string != null) {
            return Sound.sound(Key.key(string), Sound.Source.valueOf(config.getString("sound.challenge-accept.source")),
                    (float) config.getDouble("sound.challenge-accept.radius"), (float) config.getDouble("sound.challenge-accept.pitch"));
        }
        return null;
    }

    public Sound TeleportSpectateSound() {
        @Subst("") String string = config.getString("sound.spectate-teleport.sound");
        if (string != null) {
            return Sound.sound(Key.key(string), Sound.Source.valueOf(config.getString("sound.spectate-teleport.source")),
                    (float) config.getDouble("sound.spectate-teleport.radius"), (float) config.getDouble("sound.spectate-teleport.pitch"));
        }
        return null;
    }


    public World ArenaWorld() {
        return plugin.getServer().getWorld(Objects.requireNonNull(config.getString("arenas.world")));
    }

    public List<ConcurrentHashMap<Location, Location>> Arenas() {
        List<ConcurrentHashMap<Location, Location>> arenas = new ArrayList<>();
        World world = plugin.getServer().getWorld(Objects.requireNonNull(config.getString("arenas.world")));

        for (String locinstr : config.getStringList("arenas.locations")) {
            String[] parts = locinstr.split(" ");

            String[] first = parts[0].split(",");
            String[] second = parts[1].split(",");

            try {
                ConcurrentHashMap<Location, Location> locMap = getLocationLocationConcurrentHashMap(first, second, world);
                arenas.add(locMap);
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid location format in arenas: " + locinstr);
            }
        }

        return arenas;

    }

    private static @NotNull ConcurrentHashMap<Location, Location> getLocationLocationConcurrentHashMap(String[] first, String[] second, World world) {
        double x = Double.parseDouble(first[0]);
        double y = Double.parseDouble(first[1]);
        double z = Double.parseDouble(first[2]);
        float yaw = Float.parseFloat(first[3]);
        float pitch = Float.parseFloat(first[4]);

        double x2 = Double.parseDouble(second[0]);
        double y2 = Double.parseDouble(second[1]);
        double z2 = Double.parseDouble(second[2]);
        float yaw2 = Float.parseFloat(second[3]);
        float pitch2 = Float.parseFloat(second[4]);

        Location loc1 = new Location(world, x, y, z, yaw, pitch);
        Location loc2 = new Location(world, x2, y2, z2, yaw2, pitch2);

        ConcurrentHashMap<Location, Location> locMap = new ConcurrentHashMap<>();
        locMap.put(loc1, loc2);
        return locMap;
    }

    public List<Location> SpectateArea() {

        List<Location> locs = new ArrayList<>();
        World world = plugin.getServer().getWorld(Objects.requireNonNull(config.getString("arenas.world")));

        for (String locinstr : config.getStringList("arenas.locations")) {
            List<String> splits = Arrays.stream(locinstr.split(" ")).toList();
            double x,y,z,yaw,pitch;
            List<String> xyzsplit1 = Arrays.stream(splits.getLast().split(",")).toList();

            x = Double.parseDouble(xyzsplit1.getFirst()); y = Double.parseDouble(xyzsplit1.get(1)); z = Double.parseDouble(xyzsplit1.get(2)); yaw = Double.parseDouble(xyzsplit1.get(3)); pitch = Double.parseDouble(xyzsplit1.getLast());
            locs.add(new Location(world,x,y,z,(float)yaw,(float)pitch));
        }

        return locs;

    }

}
