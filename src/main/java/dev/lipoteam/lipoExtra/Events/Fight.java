package dev.lipoteam.lipoExtra.Events;

import dev.lipoteam.lipoExtra.Commands.FightCommands;
import dev.lipoteam.lipoExtra.Manager.DataManager;
import dev.lipoteam.lipoExtra.Files.FightConfig;
import dev.lipoteam.lipoExtra.LipoExtra;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Team;

import java.util.Objects;

public class Fight implements Listener {

    private final DataManager dataManager;
    private final LipoExtra plugin;

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
    private boolean autoheal;
    private Team spectators;

    private FightCommands fightCommands;

    public Fight(FightConfig config, LipoExtra plugin) {

        this.plugin = plugin;
        dataManager = new DataManager(plugin);
        setConfig(config);
        broadcast = Bukkit.getServer();

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
        spectators = config.getSpectatorsTeam();
        autoheal = config.isAutoHeal();
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
        } else {
            if (p.hasMetadata("infight")) {
                if (p.hasMetadata("challenging")) {
                    Player ki = (Player) p.getMetadata("challenging").getFirst().value();
                    if (ki != null) {
                        WinLoseChallenge(p, ki);
                    }
                } else if (p.hasMetadata("accepting")) {
                    Player ki = (Player) p.getMetadata("accepting").getFirst().value();
                    if (ki != null) {
                        WinLoseChallenge(p, ki);
                    }
                }
            }
        }
    }

    @EventHandler
    public void PlayerCommand(PlayerCommandPreprocessEvent e) {
        var mm = MiniMessage.miniMessage();

        Player p = e.getPlayer();
        Audience a = plugin.adventure().player(p);
        if (p.hasMetadata("infight") && !e.getMessage().contains("fight leave") && !p.hasPermission("lipo.fight.usecommands")) {
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

        } else if (p.hasMetadata("spectating") && (e.getMessage().contains("fight accept") || e.getMessage().contains("fight with"))) {

            Component msg;
            msg = mm.deserialize(commandprevent.replace("[prefix]", prefix));
            a.sendMessage(msg);
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
            spectators.removePlayer(p);
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void PlayerTeleport(PlayerTeleportEvent e) {
        if (e.isCancelled()) return;

        var mm = MiniMessage.miniMessage();
        Player p = e.getPlayer();
        if (p.hasMetadata("spectating")) {
            if (Objects.equals(e.getFrom().getWorld(), arenaworld) && !Objects.equals(e.getTo().getWorld(), arenaworld)) {
                int arena = p.getMetadata("spectating").getFirst().asInt();

                Player a = fightCommands.arenainuse().get(arena);
                if (a != null && a.hasMetadata("accepting")) {
                    Player ac = (Player) a.getMetadata("accepting").getFirst().value();
                    if (ac != null) {
                        HandleSpectator(p, a, ac);
                    }
                } else {
                    p.kick(mm.deserialize("<red>Something went wrong in fight!"));
                }
            }
        } else if (p.hasMetadata("infight")) {
            if (!e.getTo().getWorld().equals(arenaworld)) {
                e.setCancelled(true);
            }
        } else if (!isParticipant(p) && !isAccepting(p)) {
            if (Objects.equals(e.getTo().getWorld(), arenaworld) && !p.hasPermission("lipo.fight.teleport")) {
                dataManager.sendMessage(p, fightCommands.ReformatText(cantteleport));
                e.setCancelled(true);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    p.teleport(e.getFrom());
                }, 1L);
            }
        }

    }

    private boolean isParticipant(Player p) {
        return !p.hasMetadata("lastloc") &&
                p.hasMetadata("infight") &&
                (p.hasMetadata("challenging") || p.hasMetadata("accepting"));
    }

    private boolean isAccepting(Player p) {
        return (p.hasMetadata("accepting") || p.hasMetadata("challenging"))
                && !p.hasMetadata("infight");
    }

    private void HandleSpectator(Player spectator, Player accepter, Player challenger) {
        challenger.showPlayer(plugin, spectator);
        accepter.showPlayer(plugin, spectator);
        fightCommands.removeDelayEvent(spectator);
        spectator.removeMetadata("spectating", plugin);
        spectator.removeMetadata("lastloc", plugin);
        spectator.removePotionEffect(PotionEffectType.INVISIBILITY);
        spectators.removePlayer(spectator);
    }

    public void WinLoseChallenge(Player p, Player k) {

        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        Location lastloc = (Location) k.getMetadata("infight").getFirst().value();
        scheduler.runTaskLater(plugin, () -> {

            Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.hasMetadata("spectating"))
                    .forEach(player -> {
                        Location spectatorlastloc = (Location) player.getMetadata("lastloc").getFirst().value();
                        HandleSpectator(player, p, k);
                        if (spectatorlastloc != null) {
                            player.teleport(spectatorlastloc);
                        } else {
                            Bukkit.dispatchCommand(player, "spawn");
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

            if (lastloc != null) {
                k.teleport(lastloc);
            } else {
                Bukkit.dispatchCommand(k, "spawn");
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

        if (autoheal) {
            double health1 = acceptor.getMetadata("lasthealth").getFirst().asDouble();
            int hunger1 = acceptor.getMetadata("lasthunger").getFirst().asInt();
            double health2 = challenger.getMetadata("lasthealth").getFirst().asDouble();
            int hunger2 = challenger.getMetadata("lasthunger").getFirst().asInt();

            acceptor.setHealth(health1);
            acceptor.setFoodLevel(hunger1);
            challenger.setHealth(health2);
            challenger.setFoodLevel(hunger2);
        }

        acceptor.removeMetadata("lasthealth", plugin);
        acceptor.removeMetadata("lasthunger", plugin);
        challenger.removeMetadata("lasthealth", plugin);
        challenger.removeMetadata("lasthunger", plugin);

        challenger.removeMetadata("infight", plugin);
        acceptor.removeMetadata("infight", plugin);
        acceptor.removeMetadata("arena", plugin);

        arenaworld.getEntities().forEach(e -> {
            EntityType type = e.getType();
            if (type.equals(EntityType.ARROW) || type.equals(EntityType.TRIDENT) || type.equals(EntityType.SPECTRAL_ARROW)) {
                e.remove();
            }
        });

        fightCommands.arenainuse().remove(arena);
        fightCommands.getTask(acceptor, challenger).cancel();
    }

    private void FightEndMessage(Player winner, Player loser) {
        Component lostMsg = fightCommands.ReformatText(lostmsg, loser.getName(), winner.getName());
        Component winMsg = fightCommands.ReformatText(winmsg, loser.getName(), winner.getName(), String.valueOf(winback));
        ItemStack item = winner.getInventory().getItemInMainHand();
        String itemName;

        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            Component displayName = item.getItemMeta().displayName();
            itemName = MiniMessage.miniMessage().serialize(displayName);
        } else {
            itemName = item.getType().name().toLowerCase().replace('_', ' ');
            if (itemName.contains("AIR")) {
                itemName = "TRIDENT";
            }
        }
        Component broadcastMsg = fightCommands.ReformatText(wbroadcast.replace("[item]", itemName), loser.getName(), winner.getName(), "", winner.getName());

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
        p.removeMetadata("infight", plugin);
        k.removeMetadata("infight", plugin);
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

    }


}
