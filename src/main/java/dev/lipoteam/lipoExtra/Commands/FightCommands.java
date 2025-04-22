package dev.lipoteam.lipoHud.Commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.SafeSuggestions;
import dev.lipoteam.lipoHud.DataManager;
import dev.lipoteam.lipoHud.Events.Event;
import dev.lipoteam.lipoHud.Events.Fight;
import dev.lipoteam.lipoHud.Files.FightConfig;
import dev.lipoteam.lipoHud.LipoHud;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FightCommands {

    private FightConfig config;

    private String fightmsg;
    private String prefix;
    private String FAC;
    private String FWC;
    private String AIF;
    private String declinemsg;
    private String acceptmsg;
    private String arenafull;
    private String leavemsg;
    private int commandExpired;
    private int fightduration;
    private Sound fightsendsound;
    private Sound fightacceptsound;
    private Sound teleportspectate;
    private String cantspectate;
    private String currentlyspectate;
    private String durationmsg;

    private DataManager dataManager;

    private Fight fightevent;
    private static Event event;
    private final LipoHud plugin;

    private List<ConcurrentHashMap<Location, Location>> arenas = new ArrayList<>();
    private List<Location> spectatearea = new ArrayList<>();
    private List<String> consolecmds = new ArrayList<>();
    private static final ConcurrentHashMap<Integer, Player> arenainuse = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Player, BukkitTask> duration  = new ConcurrentHashMap<>();
    private final List<Player> waitPlayer = new ArrayList<>();
    private Team spectators;

    public FightCommands(FightConfig configurations, LipoHud lipoHud) {

        plugin = lipoHud;
        dataManager = new DataManager(plugin);
        setConfig(configurations);
        Random ran = new Random();
        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        new CommandAPICommand("fight")
                .withSubcommand(new CommandAPICommand("with")
                        .withArguments(new PlayerArgument("player").replaceSafeSuggestions(SafeSuggestions.suggest(
                                info -> Bukkit.getOnlinePlayers().toArray(new Player[0])
                        )))
                        .executes((sender, args) -> {
                            if (sender instanceof  Player p) {
                                Player p2 = (Player) args.get("player");
                                if (p2 != null && p2 != p) {
                                    if (!p.hasMetadata("challenging") && !waitPlayer.contains(p) && !p.hasMetadata("infight")) {

                                        if (!p2.hasMetadata("accepting") && !waitPlayer.contains(p2) && !p2.hasMetadata("infight")) {

                                            p.setMetadata("challenging", new FixedMetadataValue(plugin, p2));
                                            p2.setMetadata("accepting", new FixedMetadataValue(plugin, p));
                                            Audience a2 = plugin.adventure().player(p2);
                                            dataManager.sendMessage(a2, ReformatText(fightmsg, p.getName(), p2.getName()));
                                            a2.playSound(fightsendsound);

                                            waitPlayer.addAll(List.of(p, p2));
                                            scheduler.runTaskLater(plugin, () -> {

                                                if (waitPlayer.contains(p) && waitPlayer.contains(p2)) {
                                                    if (!p.hasMetadata("infight")) {
                                                        p2.removeMetadata("accepting", plugin);
                                                    }
                                                    if (!p2.hasMetadata("infight")) {
                                                        p.removeMetadata("challenging", plugin);
                                                    }
                                                    waitPlayer.removeAll(List.of(p, p2));
                                                }

                                            }, commandExpired * 20L);

                                        } else if (p2.hasMetadata("infight")) {
                                            dataManager.sendMessage(p, ReformatText(AIF, p.getName(), p2.getName()));
                                        } else if (p2.hasMetadata("accepting")) {
                                            dataManager.sendMessage(p, ReformatText(FAC, p.getName(), p2.getName()));
                                        }
                                    } else {
                                        dataManager.sendMessage(p, ReformatText(FWC, p.getName(), p2.getName()));
                                    }
                                }


                            }
                        }))
                .withSubcommand(new CommandAPICommand("decline")
                        .executes((sender, args) -> {
                            if (sender instanceof Player p ) {
                                if (p.hasMetadata("challenging")) {

                                    Player p2 = (Player) p.getMetadata("challenging").getFirst().value();
                                    Component msg = Component.empty();
                                    if (p2 != null) {
                                        msg = ReformatText(declinemsg, p.getName(), p2.getName());
                                        dataManager.sendMessage(p2, msg);
                                        p2.removeMetadata("accepting", plugin);
                                    }
                                    dataManager.sendMessage(p, msg);
                                    p.removeMetadata("challenging", plugin);

                                } else if (p.hasMetadata("accepting")) {

                                    Player p2 = (Player) p.getMetadata("accepting").getFirst().value();
                                    Component msg = Component.empty();
                                    if (p2 != null) {
                                        msg = ReformatText(declinemsg, p.getName(), p2.getName());
                                        dataManager.sendMessage(p2, msg);
                                        p2.removeMetadata("challenging", plugin);
                                    }
                                    dataManager.sendMessage(p, msg);
                                    p.removeMetadata("accepting", plugin);

                                }
                            }
                        }))
                .withSubcommand(new CommandAPICommand("accept")
                        .executes((sender, args) -> {
                            if (sender instanceof Player p ) {
                                if (p.hasMetadata("challenging" ) && !p.hasMetadata("infight")) {

                                    Player p2 = (Player) p.getMetadata("challenging").getFirst().value();
                                    if (p2 != null) dataManager.sendMessage(p, ReformatText(FWC, p.getName(), p2.getName()));

                                } else if (p.hasMetadata("accepting") && !p.hasMetadata("infight")) {

                                    Player p2 = (Player) p.getMetadata("accepting").getFirst().value();

                                    Audience a = plugin.adventure().player(p);
                                    Audience a2 = plugin.adventure().player(p2);

                                    ConcurrentHashMap<Location, Location> loc = new ConcurrentHashMap<>();
                                    int arena = ran.nextInt(0, arenas.size());
                                    if (arenainuse.containsKey(arena)) {
                                        boolean fail = false;
                                        for (int i = 0; i < arenas.size(); i++) {
                                            if (arenainuse.containsKey(i)) {
                                                fail = true;
                                            } else {
                                                fail = false;
                                                arenainuse.put(i, p);
                                                loc = arenas.get(i);
                                                break;
                                            }

                                        }
                                        if (fail) {
                                            dataManager.sendMessage(ReformatText(arenafull, p.getName(), p2.getName()), a, a2);
                                            if (!p.hasMetadata("infight")) {
                                                p.removeMetadata("accepting", plugin);
                                            }
                                            if (!p2.hasMetadata("infight")) {
                                                p2.removeMetadata("challenging", plugin);
                                            }
                                            return;
                                        }
                                    } else {
                                        arenainuse.put(arena, p);
                                        loc = arenas.get(arena);
                                    }

                                    p.setMetadata("arena", new FixedMetadataValue(plugin, arena));

                                    Component msg = ReformatText(acceptmsg, p.getName(), p2.getName());

                                    a.playSound(fightacceptsound);
                                    a2.playSound(fightacceptsound);
                                    dataManager.sendMessage(msg, a, a2);

                                    p.setMetadata("infight", new FixedMetadataValue(plugin, p.getLocation()));
                                    p2.setMetadata("infight", new FixedMetadataValue(plugin, p2.getLocation()));

                                    Location floc = loc.keySet().stream().toList().getFirst();
                                    Location sloc = loc.get(floc);

                                    p.teleport(sloc);
                                    p2.teleport(floc);

                                    for (String cmds : consolecmds) {
                                        ConsoleCommandSender console = plugin.getServer().getConsoleSender();
                                        Bukkit.dispatchCommand(console, cmds.replace("[candidates]", p.getName()));
                                        Bukkit.dispatchCommand(console, cmds.replace("[candidates]", p2.getName()));
                                    }

                                    event.getDelayPlayer().addAll(List.of(p2.getUniqueId(), p.getUniqueId()));

                                    AtomicInteger minutes = new AtomicInteger(fightduration);
                                    AtomicInteger seconds = new AtomicInteger();

                                    scheduler.runTaskTimerAsynchronously(plugin, (task) -> {

                                        if (minutes.get() == fightduration) {
                                            duration.put(p, task);
                                        }

                                        Component time = ReformatText(durationmsg, p.getName(), p2.getName(), minutes + ":" + seconds);
                                        dataManager.sendActionbar(time, p, p2);
                                        Bukkit.getOnlinePlayers().forEach(player -> {
                                            if (player.hasMetadata("spectating")) {
                                                if (player.getMetadata("spectating").getFirst().asInt() == arena) {
                                                    dataManager.sendActionbar(time, player);
                                                }
                                            }
                                        });

                                        if (seconds.get() <= 0) {
                                            if (minutes.get() <= 0) {
                                                event.getDelayPlayer().removeAll(List.of(p2.getUniqueId(), p.getUniqueId()));
                                                scheduler.runTask(plugin, () -> {
                                                    Bukkit.getOnlinePlayers().forEach(player -> {
                                                        if (player.hasMetadata("spectating")) {
                                                            if (player.getMetadata("spectating").getFirst().asInt() == arena) {
                                                                Location lastloc = (Location) player.getMetadata("lastloc").getFirst().value();
                                                                if (lastloc != null) player.teleport(lastloc);

                                                                player.removeMetadata("lastloc", plugin);
                                                            }
                                                        }
                                                    });
                                                    fightevent.Draw(p, p2);
                                                });
                                                task.cancel();
                                            } else {
                                                minutes.getAndDecrement();
                                                seconds.set(60);
                                            }
                                        }

                                        seconds.getAndDecrement();

                                    },0, 20L);

                                }
                            }
                        }))
                .withSubcommand(new CommandAPICommand("leave")
                        .executes((sender, args) -> {
                            if (sender instanceof Player p) {
                                if (p.hasMetadata("infight")) {

                                    Location lastloc = (Location) p.getMetadata("infight").getFirst().value();
                                    if (lastloc != null) p.teleport(lastloc);

                                    if (p.hasMetadata("challenging")) {
                                        Player c = (Player) p.getMetadata("challenging").getFirst().value();
                                        if (c != null) {
                                            duration.get(c).cancel();
                                            removeDelayEvent(c, p);
                                            dataManager.sendMessage(p, ReformatText(leavemsg, p.getName(), c.getName()));
                                            fightevent.WinLoseChallenge(p, c);
                                        }


                                    } else if (p.hasMetadata("accepting")) {

                                        Player c = (Player) p.getMetadata("accepting").getFirst().value();
                                        duration.get(p).cancel();
                                        if (c != null) {
                                            removeDelayEvent(c, p);
                                            dataManager.sendMessage(p, ReformatText(leavemsg, c.getName(), p.getName()));
                                            fightevent.WinLoseChallenge(p, c);
                                        }

                                    }

                                }
                            }
                        })
                )
                .withSubcommand(new CommandAPICommand("spectate")
                        .withArguments(new IntegerArgument("arena").replaceSafeSuggestions(SafeSuggestions.suggest(
                                info -> arenainuse().keySet().toArray(new Integer[0])
                        )))
                        .executes((sender, args) -> {
                            if (sender instanceof Player p) {
                                int arena = 0;
                                try {
                                    arena = (int) args.get("arena");
                                } catch (NullPointerException e) {
                                    // Nothing
                                }

                                Audience a = plugin.adventure().player(p);
                                if (p.hasMetadata("challenging") || p.hasMetadata("accepting") || p.hasMetadata("infight") || p.hasMetadata("spectating")) {
                                    dataManager.sendMessage(a, ReformatText(cantspectate));
                                } else {

                                    if (arenainuse.get(arena) != null) {
                                        Player ac = arenainuse.get(arena);
                                        Player c = (Player) ac.getMetadata("accepting").getFirst().value();

                                        if (c != null) {
                                            dataManager.sendMessage(a, ReformatText(currentlyspectate, c.getName(), ac.getName()));
                                            c.hidePlayer(plugin, p);
                                            ac.hidePlayer(plugin, p);
                                        }

                                        event.getDelayPlayer().add(p.getUniqueId());

                                        p.setMetadata("spectating", new FixedMetadataValue(plugin, arena));

                                        p.setMetadata("lastloc", new FixedMetadataValue(plugin, p.getLocation()));
                                        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
                                        a.playSound(teleportspectate);

                                        spectators.addPlayer(p);
                                        spectators.setCanSeeFriendlyInvisibles(true);
                                        spectators.setAllowFriendlyFire(false);

                                        Location loc = spectatearea.get(arena);
                                        p.teleport(loc);


                                    }

                                }
                            }
                        }))
                .register("lipo");

    }

    public ConcurrentHashMap<Integer, Player> arenainuse() {
        return arenainuse;
    }

    public BukkitTask getTask(Player p, Player p2) {
        removeDelayEvent(p);
        removeDelayEvent(p2);
        return duration.get(p);
    }

    public void removeDelayEvent(Player... ps) {
        for (Player p : ps) {
            event.getDelayPlayer().remove(p.getUniqueId());
        }
    }

    public Component ReformatText(String text1, String... text2) {
        String challenger = text2.length > 0 ? text2[0] : "";
        String accepter = text2.length > 1 ? text2[1] : "";
        String time = text2.length > 2 ? text2[2] : "";
        String winner = text2.length > 3 ? text2[3] : "";

        var mm = MiniMessage.miniMessage();

        return mm.deserialize(text1.replace("[challenger]", challenger)
                .replace("[accepter]", accepter)
                .replace("[time]", time)
                .replace("[prefix]", prefix)
                .replace("[winner]", winner));
    }

    public void setConfig(FightConfig config) {
        fightmsg = config.FightMessage();
        prefix = config.prefix();
        FAC = config.FightAlreadyChallenge();
        FWC = config.FightWaitChallenger();
        AIF = config.AlreadyInFight();
        declinemsg = config.DeclineMessage();
        acceptmsg = config.AcceptMessage();
        commandExpired = config.commandExpired();
        arenas = config.Arenas();
        fightsendsound = config.ChallengeSound();
        fightacceptsound = config.ChallengeAcceptSound();
        leavemsg = config.LeaveMessage();
        arenafull = config.FullMessage();
        cantspectate = config.CannotSpectate();
        spectatearea = config.SpectateArea();
        currentlyspectate = config.CurrentlySpectate();
        teleportspectate = config.TeleportSpectateSound();
        fightevent = plugin.getFightevent();
        fightevent.setFightCommands(this);
        event = plugin.getEvent();
        consolecmds = config.ConsoleCommands();
        fightduration = config.FightDuration();
        durationmsg = config.DurationMessage();
        spectators = config.getSpectatorsTeam();
    }

}
