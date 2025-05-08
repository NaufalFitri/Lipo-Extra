package dev.lipoteam.lipoExtra.Events;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Ranks.CMIRank;
import dev.lipoteam.lipoExtra.Files.LevelConfig;
import dev.lipoteam.lipoExtra.LipoExtra;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.PortalCreateEvent;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LevelBound implements Listener {

    private LipoExtra plugin;
    private LevelConfig config;

    private String cantenter;
    private String prefix;
    private static ConcurrentHashMap<String, Integer> worldlevel = new ConcurrentHashMap<>();
    private LinkedHashMap<String, Boolean> worldrtp = new LinkedHashMap<>();
    private final HashSet<UUID> delayPlayer = new HashSet<>();
    private int delay;
    private boolean enabled;
    private Sound soundcantenter;

    public LevelBound(LevelConfig config, LipoExtra plugin) {

        this.config = config;
        this.plugin = plugin;
        setConfig(config);
    }

    public void setConfig(LevelConfig config) {

        prefix = config.Prefix();
        cantenter = config.cannotEnter();
        soundcantenter = config.SoundCantEnter();
        worldlevel = config.worldlevel();
        worldrtp = config.worldrtp();
        delay = config.Delay();
        enabled = config.Enabled();

    }

    @EventHandler
    private void PortalEnter(PlayerTeleportEvent e) {
        if (e.getPlayer().isOp()) return;

        if (CMI.getInstance().getPlayerManager().getUser(e.getPlayer()).isVanished()) return;

        if (!enabled) return;

        Player p = e.getPlayer();
        UUID id = p.getUniqueId();
        var mm = MiniMessage.miniMessage();

        CMIRank rank = CMI.getInstance().getPlayerManager().getUser(p).getRank();
        String rankname = rank.getName();
        if (rankname.matches("\\d+")) {
            if (e.getTo() != null) {
                World world = e.getTo().getWorld();
                if (world != null) {
                    String worldname = world.getName();
                    if (worldlevel.containsKey(worldname)) {
                        int level = worldlevel.get(worldname);
                        if (Integer.parseInt(rankname) < level) {
                            Audience a = plugin.adventure().player(p);
                            if (!delayPlayer.contains(id)) {
                                a.sendMessage(mm.deserialize(cantenter.replace("[prefix]", prefix)
                                        .replace("[world]", worldname).replace("[level]", String.valueOf(level))));
                                a.playSound(soundcantenter);
                                delayPlayer.add(id);
                                Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                                    delayPlayer.remove(id);
                                }, delay * 20L);
                            }
                            e.setCancelled(true);
                        } else {
                            if (!worldname.contains("nether")) {
                                if (worldrtp.get(worldname)) Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), "rt " + p.getName() + " " + worldname);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private void OnPlayerCommand(PlayerCommandPreprocessEvent e) {
        var mm = MiniMessage.miniMessage();
        Player p = e.getPlayer();

        CMIRank rank = CMI.getInstance().getPlayerManager().getUser(p).getRank();
        String rankname = rank.getName();

        UUID id = p.getUniqueId();

        if (e.getMessage().contains("rtp")) {

            for (String worldname : worldlevel.keySet()) {
                if (e.getMessage().contains(worldname)) {
                    if (rankname.matches("\\d+")) {
                        if (worldlevel.containsKey(worldname)) {
                            int level = worldlevel.get(worldname);
                            if (Integer.parseInt(rankname) < level) {
                                Audience a = plugin.adventure().player(p);
                                if (!delayPlayer.contains(id)) {
                                    a.sendMessage(mm.deserialize(cantenter.replace("[prefix]", prefix)
                                            .replace("[world]", worldname).replace("[level]", String.valueOf(level))));
                                    a.playSound(soundcantenter);
                                    delayPlayer.add(id);
                                    Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                                        delayPlayer.remove(id);
                                    }, delay * 20L);
                                }
                                e.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent e) {
        if (!enabled) return;

        Player p = e.getPlayer();

        if (e.getTo().getWorld().getName().contains("nether")) {
            if (worldrtp.get(e.getTo().getWorld().getName())) {
                e.setCancelled(true); // cancel the portal teleport
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rt " + p.getName() + " " + e.getTo().getWorld().getName());
            }
        }
    }

    public static int getLevel(String world) {
        return worldlevel.getOrDefault(world, -1);
    }
}
