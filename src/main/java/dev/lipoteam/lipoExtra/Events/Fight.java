package dev.lipoteam.lipoHud.Events;

import dev.lipoteam.lipoHud.Commands.FightCommands;
import dev.lipoteam.lipoHud.DataManager;
import dev.lipoteam.lipoHud.Files.FightConfig;
import dev.lipoteam.lipoHud.LipoHud;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Team;

import java.util.Objects;

public class Fight implements Listener {

    private final DataManager dataManager;
    private final LipoHud plugin;

    private String prefix;
    private String lostmsg;
    private String winmsg;
    private World arenaworld;
    private int winback;
    private String commandprevent;
    private String drawmsg;
    private String wbroadcast;
    private String cantteleport;
    private final ForwardingAudience broadcast;
    private Team spetators;

    private FightCommands fightCommands;

    public Fight(FightConfig config, LipoHud plugin) {

        this.plugin = plugin;
        dataManager = new DataManager(plugin);
        setConfig(config);
        broadcast = (ForwardingAudience) Bukkit.getServer();

    }

    public void setConfig(FightConfig config) {
        prefix = config.prefix();
        lostmsg = config.LostMessage();
        winmsg = config.WinMessage();
        winback = config.WinFightBack();
        commandprevent = config.CannotCommand();
        arenaworld = config.ArenaWorld();
        drawmsg = config.DrawMessage();
        wbroadcast = config.WinBroadcast();
        cantteleport = config.CannotTeleport();
        spetators = config.getSpectatorsTeam();
    }

    public void setFightCommands(FightCommands fightCommands) {
        this.fightCommands = fightCommands;
    }

    @EventHandler
    public void PlayerKill(PlayerDeathEvent e) {
        Player p = e.getEntity();
        Player k = p.getKiller();

        if (k != null) {

            if (p.hasMetadata("infight") && k.hasMetadata("infight")) {

                WinLoseChallenge(p, k);

            }
        }
    }

    @EventHandler
    public void PlayerCommand(PlayerCommandPreprocessEvent e) {
        var mm = MiniMessage.miniMessage();

        Player p = e.getPlayer();
        Audience a = plugin.adventure().player(p);
        if (p.hasMetadata("infight") && !e.getMessage().contains("fight leave")) {
            Player k;
            Component msg = null;
            if (p.hasMetadata("challenging")) {
                k = (Player) p.getMetadata("challenging").getFirst().value();
                if (k != null) {
                    msg = mm.deserialize(commandprevent.replace("[challenger]", k.getName()).replace("[accepter]", p.getName()).replace("[prefix]", prefix));
                }
            } else if (p.hasMetadata("accepting")) {
                k = (Player) p.getMetadata("accepting").getFirst().value();
                if (k != null) {
                    msg = mm.deserialize(commandprevent.replace("[challenger]", p.getName()).replace("[accepter]", k.getName()).replace("[prefix]", prefix));
                }
            }

            if (msg != null) {
                a.sendMessage(msg);
            }

            e.setCancelled(true);
        }

    }

    @EventHandler
    public void OnPlayerLeft(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        if (p.hasMetadata("infight")) {
            if (p.hasMetadata("challenging")) {
                Player k = (Player) p.getMetadata("challenging").getFirst().value();
                if (k != null) {
                    WinLoseChallenge(p, k);
                }
            } else if (p.hasMetadata("accepting")) {
                Player k = (Player) p.getMetadata("accepting").getFirst().value();
                if (k != null) {
                    WinLoseChallenge(p, k);
                }
            }

        } else if (p.hasMetadata("spectating")) {
            spetators.removePlayer(p);
            p.removeMetadata("spectating", plugin);
            p.removeMetadata("lastloc", plugin);
            p.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }

    @EventHandler
    public void PlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (p.hasMetadata("spectating")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void PlayerDamage(EntityDamageEvent e) {
        if (e.getDamageSource().getCausingEntity() != null) {
            if (e.getDamageSource().getCausingEntity().getType().equals(EntityType.PLAYER)) {
                Player damager = (Player) e.getDamageSource().getCausingEntity();
                if (damager != null) {
                    if (damager.hasMetadata("spectating")) {
                        e.setCancelled(true);
                    }
                }
            }
        }


    }

    @EventHandler
    public void PlayerTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (p.hasMetadata("spectating")) {
            if (Objects.equals(e.getFrom().getWorld(), arenaworld) && !Objects.equals(e.getTo().getWorld(), arenaworld)) {
                int arena = p.getMetadata("spectating").getFirst().asInt();

                Player a = fightCommands.arenainuse().get(arena);
                Player ac = (Player) a.getMetadata("accepting").getFirst().value();

                if (ac != null) {
                    HandleSpectator(p, a, ac);
                }
            }
        } else if (!p.hasMetadata("lastloc") && !(p.hasMetadata("challenging") || p.hasMetadata("accepting")) && !p.hasMetadata("infight")) {
            if (Objects.equals(e.getTo().getWorld(), arenaworld) && !p.hasPermission("lipo.fight.teleport")) {
                dataManager.sendMessage(p, fightCommands.ReformatText(cantteleport));
                e.setCancelled(true);
                e.setTo(e.getFrom());
            }
        }
    }

    private void HandleSpectator(Player spectator, Player accepter, Player challenger) {
        challenger.showPlayer(plugin, spectator);
        accepter.showPlayer(plugin, spectator);
        fightCommands.removeDelayEvent(spectator);
        spectator.removeMetadata("spectating", plugin);
        spectator.removePotionEffect(PotionEffectType.INVISIBILITY);
        spetators.removePlayer(spectator);
    }

    public void WinLoseChallenge(Player p, Player k) {

        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        Location lastloc = (Location) k.getMetadata("infight").getFirst().value();
        scheduler.runTaskLater(plugin, () -> {

            if (lastloc != null) {
                k.teleport(lastloc);
            } else {
                Bukkit.dispatchCommand(k, "spawn");
            }
            Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.hasMetadata("spectating"))
                    .forEach(player -> {
                        HandleSpectator(player, p, k);
                        Location spectatorlastloc = (Location) player.getMetadata("lastloc").getFirst().value();
                        if (spectatorlastloc != null) {
                            player.teleport(spectatorlastloc);
                            player.removeMetadata("lastloc", plugin);
                        }
                    });

            if (p.hasMetadata("challenging")) {
                handleFightEnd(k, p);
                p.removeMetadata("challenging", plugin);
                k.removeMetadata("accepting", plugin);
            } else if (p.hasMetadata("accepting")) {
                handleFightEnd(p, k);
                p.removeMetadata("accepting", plugin);
                k.removeMetadata("challenging", plugin);
            }

        }, winback * 20L);

        if (p.hasMetadata("challenging")) {
            FightEndMessage(k, p);
        } else if (p.hasMetadata("accepting")) {
            FightEndMessage(k, p);
        }

    }

    private void handleFightEnd(Player acceptor, Player challenger) {
        int arena = acceptor.getMetadata("arena").getFirst().asInt();

        challenger.removeMetadata("infight", plugin);
        acceptor.removeMetadata("infight", plugin);
        acceptor.removeMetadata("arena", plugin);

        fightCommands.arenainuse().remove(arena);
        fightCommands.getTask(acceptor, challenger).cancel();
    }

    private void FightEndMessage(Player winner, Player loser) {
        Component lostMsg = fightCommands.ReformatText(lostmsg, loser.getName(), winner.getName());
        Component winMsg = fightCommands.ReformatText(winmsg, loser.getName(), winner.getName(), String.valueOf(winback));
        Component broadcastMsg = fightCommands.ReformatText(wbroadcast, loser.getName(), winner.getName(), "", winner.getName());

        broadcast.sendMessage(broadcastMsg);
        dataManager.sendMessage(loser, lostMsg);
        dataManager.sendMessage(winner, winMsg);
    }

    public void Draw(Player p, Player k) {
        var mm = MiniMessage.miniMessage();
        ForwardingAudience a = (ForwardingAudience) Bukkit.getServer();

        int arena = p.getMetadata("arena").getFirst().asInt();
        fightCommands.arenainuse().remove(arena);

        Component msg = mm.deserialize(drawmsg.replace("[challenger]", k.getName()).replace("[accepter]", p.getName()).replace("[prefix]", prefix));
        a.sendMessage(msg);
        k.removeMetadata("challenging", plugin);
        p.removeMetadata("accepting", plugin);
        p.removeMetadata("arena", plugin);

        Location lastloc1 = (Location) k.getMetadata("infight").getFirst().value();
        Location lastloc2 = (Location) p.getMetadata("infight").getFirst().value();
        if (lastloc1 != null) {
            k.teleport(lastloc1);
        } else {
            Bukkit.dispatchCommand(k, "spawn");
        }
        if (lastloc2 != null) {
            p.teleport(lastloc2);
        } else {
            Bukkit.dispatchCommand(p, "spawn");
        }

        p.removeMetadata("infight", plugin);
        k.removeMetadata("infight", plugin);
    }


}
