package dev.lipoteam.lipoExtra.Commands;

import com.google.common.util.concurrent.AtomicDouble;
import com.xxmicloxx.NoteBlockAPI.songplayer.PositionSongPlayer;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.lipoteam.lipoExtra.Events.Pinata;
import dev.lipoteam.lipoExtra.Manager.DataManager;
import dev.lipoteam.lipoExtra.Files.PinataConfig;
import dev.lipoteam.lipoExtra.LipoExtra;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class PinataCommands {

    private PinataConfig config;
    private LipoExtra plugin;

    private String prefix;
    private boolean enabled;

    private Location pinataloc;
    private final Random random;

    private final BukkitScheduler scheduler;
    private final DataManager dataManager;
    private PositionSongPlayer psp;

    private String voteRemove;
    private String voteAdd;
    private String voteReset;

    public PinataCommands(PinataConfig config, LipoExtra plugin) {
        this.plugin = plugin;
        setConfig(config);
        createCommand();
        scheduler = plugin.getServer().getScheduler();
        dataManager = new DataManager(plugin);
        random = new Random();
    }

    public void setConfig(PinataConfig config) {
        this.config = config;
        voteAdd = config.voteAddMsg();
        voteRemove = config.voteRemoveMsg();
        voteReset = config.voteResetMsg();
        pinataloc = config.PinataLocation();
        areawhack = config.WhackArea();
        prefix = config.Prefix();
        enabled = config.Enabled();
        whackenabled = config.WhackEnabled();
        whackisbaby = config.WhackIsBaby();
        whackentity = config.WhackEntity();
        broadcastchat = config.broadcastchat();
        broadcastactionbar = config.broadcastactionbar();
        broadcastsubtitle = config.broadcastsubtitle();
        broadcasttitle = config.broadcasttitle();
        broadcastend = config.broadcastend();
        time = config.broadcasttime();
        spawnParticle = config.WspawnParticle();
        spawnParticleCount = config.WspawnParticleCount();
        spawnParticleSpeed = config.WspawnParticleSpeed();
        whackgametime = config.WhackDuration();
        whackdespawntime = config.WhackDurationDespawn();
        fadein = config.TitleFadeIn();
        fadeout = config.TitleFadeOut();
        stay = config.TitleFadeStay();
        psp = config.PinataSong();
        broadcastSound = config.PinataSoundStart();
        soundcounter = config.SoundCounterName();
        whackchangesound = config.WhackSoundChange();
        whackmultiplier = config.WhackMultiplier();
    }

    private void createCommand() {
        var mm = MiniMessage.miniMessage();

        if (!enabled) return;
        new CommandAPICommand("pinata")
                .withPermission("lipo.command.pinata")
                .withSubcommand(new CommandAPICommand("start")
                        .withArguments(new StringArgument("mode").replaceSuggestions(ArgumentSuggestions.strings("whackit", "pinatarun", "pinatahang", "random")))
                        .executes((sender, args) -> {
                            String mode = (String) args.get("mode");
                            if (sender instanceof Player p) {
                                if ("random".equalsIgnoreCase(mode)) {
                                    int select = random.nextInt(1,3);
                                    switch (select) {
                                        case 1 -> {
                                            if (whackenabled) {
                                                WhackIt();
                                                Broadcast();
                                            } else {
                                                dataManager.sendMessage(p, mm.deserialize("[prefix] <red>Whack-it is not enabled!".replace("[prefix]", prefix)));
                                            }
                                        }
                                        case 2 -> PinataRun();
                                        case 3 -> PinataHang();
                                    }
                                    return;
                                }
                                switch (mode) {
                                    case "whackit" -> {
                                        if (whackenabled) {
                                            WhackIt();
                                            Broadcast();
                                        } else {
                                            dataManager.sendMessage(p, mm.deserialize("[prefix] <red>Whack-it is not enabled!".replace("[prefix]", prefix)));
                                        }
                                    }
                                    case "pinatarun" -> PinataRun();
                                    case "pinatahang" -> PinataHang();
                                    case null, default -> plugin.getLogger().warning("There's no mode by this name!");
                                }

                            } else {
                                if ("random".equalsIgnoreCase(mode)) {
                                    int select = random.nextInt(1,3);
                                    switch (select) {
                                        case 1 -> {
                                            if (whackenabled) {
                                                WhackIt();
                                                Broadcast();
                                            } else {
                                                plugin.adventure().console().sendMessage(mm.deserialize("[prefix] <red>Whack-it is not enabled!".replace("[prefix]", prefix)));
                                            }
                                        }
                                        case 2 -> PinataRun();
                                        case 3 -> PinataHang();
                                    }
                                    return;
                                }
                                switch (mode) {
                                    case "whackit" -> {
                                        if (whackenabled) {
                                            WhackIt();
                                            Broadcast();
                                        } else {
                                            plugin.adventure().console().sendMessage(mm.deserialize("[prefix] <red>Whack-it is not enabled!".replace("[prefix]", prefix)));
                                        }
                                    }
                                    case "pinatarun" -> PinataRun();
                                    case "pinatahang" -> PinataHang();
                                    case null, default -> plugin.getLogger().warning("There's no mode by this name!");
                                }
                            }

                        }))
                .withSubcommand(new CommandAPICommand("vote")
                        .withArguments(new StringArgument("method").replaceSuggestions(ArgumentSuggestions.strings("add", "remove", "reset")))
                        .withOptionalArguments(new IntegerArgument("num"))
                        .withOptionalArguments(new StringArgument("extra").replaceSuggestions(ArgumentSuggestions.strings("-s")))
                        .executes((sender, args) -> {
                            String method = (String) args.get("method");
                            String extra = (String) args.get("extra");
                            int num = 0;
                            try {
                                num = (int) args.get("num");
                            } catch (NullPointerException e) {
                                //
                            }

                            if (method != null) {
                                if (sender instanceof Player p) {
                                    switch (method) {
                                        case "add" -> {
                                            if (num > 0) {
                                                Pinata.voteCurrent += num;
                                            } else {
                                                num = 1;
                                                Pinata.voteCurrent++;
                                            }

                                            if (extra == null || !extra.equalsIgnoreCase("-s")) {
                                                p.sendMessage(mm.deserialize(voteAdd.replace("[prefix]", prefix).replace("[number]", String.valueOf(num))));
                                            }

                                        }
                                        case "remove" -> {
                                            if (num > 0) {
                                                Pinata.voteCurrent -= num;
                                            } else {
                                                num = 1;
                                                Pinata.voteCurrent--;
                                            }
                                            if (extra == null || !extra.equalsIgnoreCase("-s")) {
                                                p.sendMessage(mm.deserialize(voteRemove.replace("[prefix]", prefix).replace("[number]", String.valueOf(num))));
                                            }
                                        }
                                        case "reset" -> {
                                            Pinata.voteCurrent = 0;
                                            if (extra == null || !extra.equalsIgnoreCase("-s")) {
                                                p.sendMessage(mm.deserialize(voteReset.replace("[prefix]", prefix).replace("[number]", String.valueOf(num))));
                                            }
                                        }
                                        default -> p.sendMessage(mm.deserialize(prefix + "<red> An error occurred while adding vote"));
                                    }

                                } else {
                                    switch (method) {
                                        case "add" -> {
                                            if (num > 0) {
                                                Pinata.voteCurrent += num;
                                            } else {
                                                num = 1;
                                                Pinata.voteCurrent++;
                                            }

                                            if (extra == null || !extra.equalsIgnoreCase("-s")) {
                                                Bukkit.getConsoleSender().sendMessage(mm.deserialize(voteAdd.replace("[prefix]", prefix).replace("[number]", String.valueOf(num))));
                                            }

                                        }
                                        case "remove" -> {
                                            if (num > 0) {
                                                Pinata.voteCurrent -= num;
                                            } else {
                                                num = 1;
                                                Pinata.voteCurrent--;
                                            }
                                            if (extra == null || !extra.equalsIgnoreCase("-s")) {
                                                Bukkit.getConsoleSender().sendMessage(mm.deserialize(voteRemove.replace("[prefix]", prefix).replace("[number]", String.valueOf(num))));
                                            }
                                        }
                                        case "reset" -> {
                                            Pinata.voteCurrent = 0;
                                            if (extra == null || !extra.equalsIgnoreCase("-s")) {
                                                Bukkit.getConsoleSender().sendMessage(mm.deserialize(voteReset.replace("[prefix]", prefix).replace("[number]", String.valueOf(num))));
                                            }
                                        }
                                        default -> Bukkit.getConsoleSender().sendMessage(mm.deserialize(prefix + "<red> An error occurred while adding vote"));
                                    }
                                }

                            }

                        }))
                .register("lipo");

    }

    private String broadcastchat;
    private String broadcasttitle;
    private String broadcastsubtitle;
    private String broadcastactionbar;
    private String broadcastend;
    private Sound broadcastSound;
    private String soundcounter;
    private int time;
    private int fadein;
    private int stay;
    private int fadeout;

    private void Broadcast() {
        AtomicInteger temptime = new AtomicInteger(time);
        var mm = MiniMessage.miniMessage();
        plugin.getServer().getOnlinePlayers().forEach(p -> {
            if (!broadcasttitle.isEmpty() && broadcastsubtitle.isEmpty()) {
                dataManager.sendTitle(mm.deserialize(broadcasttitle.replace("[prefix]", prefix).replace("[time]", String.valueOf(time))), mm.deserialize(""), fadein, stay, fadeout, p);
            } else if (broadcasttitle.isEmpty() && !broadcastsubtitle.isEmpty()) {
                dataManager.sendTitle(mm.deserialize(""), mm.deserialize(broadcastsubtitle.replace("[prefix]", prefix).replace("[time]", String.valueOf(time))), fadein, stay, fadeout, p);
            } else if (!broadcasttitle.isEmpty()) {
                dataManager.sendTitle(mm.deserialize(broadcasttitle.replace("[prefix]", prefix).replace("[time]", String.valueOf(time))), mm.deserialize(broadcastsubtitle.replace("[prefix]", prefix).replace("[time]", String.valueOf(time))), fadein, stay, fadeout, p);
            }

            if (!broadcastchat.isEmpty()) {
                dataManager.sendMessage(p, mm.deserialize(broadcastchat.replace("[prefix]", prefix).replace("[time]", String.valueOf(time))));
            }

            if (!broadcastactionbar.isEmpty()) {
                dataManager.sendActionbar(mm.deserialize(broadcastactionbar.replace("[prefix]", prefix).replace("[time]", String.valueOf(time))), p);
            }

            p.playSound(broadcastSound);
        });

        AtomicDouble pitch = new AtomicDouble(1);

        BukkitTask task = scheduler.runTaskTimer(plugin, () -> {

            plugin.getServer().getOnlinePlayers().forEach(p -> {
                dataManager.sendActionbar(mm.deserialize(broadcastactionbar.replace("[prefix]", prefix).replace("[time]", String.valueOf(temptime))), p);
                plugin.adventure().player(p).playSound(config.PinataSoundCounter(soundcounter, (float) pitch.get()));
            });
            pitch.getAndAdd(0.1);
            temptime.getAndDecrement();

        }, 0L, 20L);

        scheduler.runTaskLater(plugin, task::cancel, 20L * time);
    }

    private boolean whackenabled;
    private boolean whackisbaby;
    private EntityType whackentity;
    private String areawhack;
    private String spawnParticle;
    private double spawnParticleSpeed;
    private int spawnParticleCount;
    private int whackdespawntime;
    private int whackgametime;
    private int whackmultiplier;
    private Sound whackchangesound;

    private void WhackIt() {

        var mm = MiniMessage.miniMessage();
        int areax = Integer.parseInt(areawhack.split("x")[0]);
        int areaz = Integer.parseInt(areawhack.split("x")[1]);

        scheduler.runTaskLater(plugin, () -> {

            if (psp != null) {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    psp.addPlayer(p);
                });
                psp.setPlaying(true);
            }

            BukkitTask task = scheduler.runTaskTimer(plugin, () -> {

                pinataloc.getNearbyPlayers(30).forEach(p -> {
                    plugin.adventure().player(p).playSound(whackchangesound);
                });

                for (int i = 0; i < areax * whackmultiplier; i++) {

                    int ranx = random.nextInt(-areax, areax);
                    int ranz = random.nextInt(-areaz, areaz);
                    float ranyaw = random.nextFloat(-180, 180);
                    Location spawnloc = pinataloc.clone().add(ranx, 0, ranz);
                    spawnloc.setYaw(ranyaw);
                    Entity pinata = pinataloc.getWorld().spawnEntity(spawnloc, whackentity);
                    pinataloc.getWorld().spawnParticle(Particle.valueOf(spawnParticle), pinata.getLocation().add(0, 0.5,0), spawnParticleCount, 0,0,0, spawnParticleSpeed, null);
                    if (pinata instanceof Ageable a && whackisbaby) {
                        a.setBaby();
                    }
                    pinata.setMetadata("pinata", new FixedMetadataValue(plugin, "whack"));
                    scheduler.runTaskLater(plugin, pinata::remove, 20L * whackdespawntime);
                }

            }, 0L, 20L * whackdespawntime);

            scheduler.runTaskLater(plugin, () -> {
                task.cancel();
                plugin.adventure().players().sendMessage(mm.deserialize(broadcastend.replace("[prefix]", prefix)));
                if (psp != null) psp.setPlaying(false);

            }, 20L * whackgametime);

        }, 20L * time);
    }

    private void PinataRun() {

    }

    private void PinataHang() {

    }

}
