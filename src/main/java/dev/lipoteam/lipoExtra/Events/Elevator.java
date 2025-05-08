package dev.lipoteam.lipoExtra.Events;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.griefdefender.api.Core;
import com.griefdefender.api.claim.Claim;
import dev.lipoteam.lipoExtra.Files.ElevatorConfig;
import dev.lipoteam.lipoExtra.LipoExtra;
import dev.lipoteam.lipoExtra.Manager.DataManager;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Elevator implements Listener {

    private final LipoExtra plugin;
    private ElevatorConfig config;
    private final DataManager dataManager;
    private MiniMessage mm;

    private String bounded;
    private String actionbar;
    private String title;
    private String subtitle;
    private String notsafe;
    private String prefix;
    private String occupied;
    private String unbound;
    private String claimmsg;
    private List<String> itemlore;
    private Sound elevated;
    private int mingap;
    private int maxgap;
    private final Core gdapi;
    private final HashMap<UUID, BukkitTask> actionbartask = new HashMap<>();

    public Elevator(ElevatorConfig config, LipoExtra plugin) {

        this.plugin = plugin;
        dataManager = new DataManager(plugin);
        mm = MiniMessage.miniMessage();
        gdapi = plugin.getGriefDefender();
        setConfig(config);

    }

    public void setConfig(ElevatorConfig config) {

        this.config = config;
        bounded = config.bounded();
        title = config.title();
        subtitle = config.subtitle();
        actionbar = config.actionbar();
        notsafe = config.notsafe();
        elevated = config.Elevated();
        itemlore = config.itemlore();
        mingap = config.mingap();
        maxgap = config.maxgap();
        prefix = config.prefix();
        occupied = config.occupied();
        unbound = config.unbound();
        claimmsg = config.claim();
    }

    @EventHandler
    private void ClickElevatorKit(PlayerInteractEvent e) {

        Player p = e.getPlayer();
        UUID uid = p.getUniqueId();
        ItemStack item = e.getItem();

        if (e.getAction().isRightClick() && (e.getClickedBlock() != null) && e.getHand() == EquipmentSlot.HAND) {

            if (e.getClickedBlock().getType().isAir()) return;

            Block block = e.getClickedBlock();

            if (dataManager.hasData(item, "kit")) {

                e.setCancelled(true);

                if (gdapi != null) {
                    final Claim claim = gdapi.getClaimAt(e.getClickedBlock().getLocation());

                    if (claim != null && !claim.isWilderness()) {
                        if (!claim.getOwnerUniqueId().equals(p.getUniqueId()) &&
                                !claim.getUserTrusts().contains(p.getUniqueId())) {
                            p.sendMessage(mm.deserialize(claimmsg.replace("[prefix]", prefix)));
                            return;
                        }
                    }
                }

                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

                    if (p.isSneaking()) {
                        if (dataManager.hasData(item, "loc")) {

                            Location loc = (Location) dataManager.getdata(item, "loc", true);

                            p.sendMessage(mm.deserialize(unbound.replace("[prefix]", prefix)));
                            if (item != null) {
                                ItemMeta meta = item.getItemMeta();
                                if (meta != null) {
                                    List<Component> lore = itemlore.stream()
                                            .map(u -> mm.deserialize(u.replace("[location]", "")))
                                            .collect(Collectors.toList());
                                    meta.lore(lore);
                                    Bukkit.getScheduler().runTask(plugin, () -> item.setItemMeta(meta));
                                }
                            }
                            if (actionbartask.containsKey(uid)) {
                                actionbartask.get(uid).cancel();
                                actionbartask.remove(uid);
                            }
                            p.clearTitle();
                            removeAllFloor(loc);
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                dataManager.unsetdata(item, "loc");
                                dataManager.setdata(item, "kit", true);
                            });
                        }
                        return;
                    }

                    if (dataManager.hasData(item, "loc")) {

                        Location loc = (Location) dataManager.getdata(item, "loc", true);
                        UUID id = UUID.fromString((String) dataManager.getdata(item, "kit", false));

                        if (hasElevator(loc)) {
                            if (!Objects.equals(getBound(loc), id)) {
                                p.sendMessage(mm.deserialize(occupied.replace("[prefix]", prefix)));
                                if (item != null) {
                                    ItemMeta meta = item.getItemMeta();
                                    if (meta != null) {
                                        List<Component> lore = itemlore.stream()
                                                .map(u -> mm.deserialize(u.replace("[location]", "")))
                                                .collect(Collectors.toList());
                                        meta.lore(lore);
                                        Bukkit.getScheduler().runTask(plugin, () -> item.setItemMeta(meta));
                                    }
                                }
                                removeAllFloor(loc);
                                if (actionbartask.containsKey(uid)) {
                                    actionbartask.get(uid).cancel();
                                    actionbartask.remove(uid);
                                }
                                p.clearTitle();
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    dataManager.unsetdata(item, "loc");
                                    dataManager.setdata(item, "kit", true);
                                });
                                return;
                            }
                        }

                        if (!dataManager.hasData(block, "elevator")) {
                            if (loc.getBlockX() == block.getX() && loc.getBlockZ() == block.getZ()) {
                                if (loc.getBlockY() == block.getY()) {
                                    dataManager.setdata(block, "elevator", 1);
                                    dataManager.setdata(item, "loc", loc);
                                    setFloor(loc);
                                } else {
                                    dataManager.setdata(block, "elevator", "?");
                                    dataManager.setdata(block, "base", loc);
                                    setFloor(loc);
                                }
                                p.sendMessage(mm.deserialize(bounded.replace("[prefix]", prefix)));
                            }
                        }
                    } else {

                        UUID id = UUID.randomUUID();
                        Location loc = block.getLocation();

                        if (hasElevator(loc)) {
                            p.sendMessage(mm.deserialize(occupied.replace("[prefix]", prefix)));
                            return;
                        }

                        p.sendMessage(mm.deserialize(bounded.replace("[prefix]", prefix)));
                        if (item != null) {
                            ItemMeta meta = item.getItemMeta();
                            if (meta != null) {
                                List<Component> lore = itemlore.stream()
                                        .map(u -> mm.deserialize(u.replace("[location]", loc.getBlockX() + "x " + loc.getBlockY() + "y " + loc.getBlockZ() + "z " + loc.getWorld().getName())))
                                        .collect(Collectors.toList());
                                meta.lore(lore);
                                Bukkit.getScheduler().runTask(plugin, () -> item.setItemMeta(meta));
                            }
                        }

                        dataManager.setdata(block, "elevator", 1);
                        dataManager.setdata(block, "bound", id.toString());
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            dataManager.setdata(item, "loc", loc);
                            dataManager.setdata(item, "kit", id.toString());
                        });
                    }
                });
            }
        }

    }

    private void setFloor(Location loc) {

        Location checkloc0 = loc.clone().add(0, -1, 0);
        Location checkloc1 = loc.clone().add(0, 1, 0);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int num0 = 1;
            int y0 = checkloc0.getBlockY();
            while(y0 > -1000) {
                if (dataManager.hasData(checkloc0.getBlock(), "elevator")) {
                    --num0;
                    dataManager.setdata(checkloc0.getBlock(), "elevator", num0);
                }
                checkloc0.setY(--y0);
            }

            dataManager.setdata(loc.getBlock(), "minfloor", num0);

            int num1 = 1;
            int y1 = checkloc1.getBlockY();
            while(y1 < 1000) {
                if (dataManager.hasData(checkloc1.getBlock(), "elevator")) {
                    ++num1;
                    dataManager.setdata(checkloc1.getBlock(), "elevator", num1);
                }
                checkloc1.setY(++y1);
            }

            dataManager.setdata(loc.getBlock(), "maxfloor", num1);
        });

    }

    private boolean hasElevator(Location loc) {

        Location checkloc = loc.clone();
        checkloc.setY(1000);

        int y0 = checkloc.getBlockY();
        while(y0 > -1000) {
            if (dataManager.hasData(checkloc.getBlock(), "elevator")) {
                return true;
            }
            checkloc.setY(--y0);
        }
        return false;
    }

    private UUID getBound(Location loc) {

        Location checkloc = loc.clone();
        checkloc.setY(1000);

        int y0 = checkloc.getBlockY();
        while(y0 > -1000) {
            if (dataManager.hasData(checkloc.getBlock(), "bound")) {
                return UUID.fromString((String) dataManager.getdata(checkloc.getBlock(), "bound", false));
            }
            checkloc.setY(--y0);
        }
        return null;
    }

    @EventHandler
    private void PlayerBreak(BlockBreakEvent e) {

        Block block = e.getBlock();
        Player p = e.getPlayer();
        UUID id = p.getUniqueId();

        if (dataManager.hasData(block, "elevator")) {
            if (dataManager.hasData(block, "base")) {
                Location base = (Location) dataManager.getdata(block, "base", true);
                setFloor(base);
            }  else {
                removeAllFloor(block.getLocation());
            }
            if (actionbartask.containsKey(id)) {
                actionbartask.get(id).cancel();
                actionbartask.remove(id);
            }
            p.clearTitle();
        }
    }

    private void removeAllFloor(Location loc) {

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Location checkloc = loc.clone();
            checkloc.setY(1000);

            int y0 = checkloc.getBlockY();
            while(y0 > -1000) {
                if (dataManager.hasData(checkloc.getBlock(), "elevator")) {
                    dataManager.unsetdata(checkloc.getBlock(), "elevator");
                    dataManager.unsetdata(checkloc.getBlock(), "base");
                    dataManager.unsetdata(checkloc.getBlock(), "bound");
                }
                checkloc.setY(--y0);
            }
        });
    }

    @EventHandler
    private void PlayerMove(PlayerMoveEvent e) {

        Player p = e.getPlayer();
        int xto = e.getTo().getBlockX();
        int zto = e.getTo().getBlockZ();
        int yto = e.getTo().getBlockY();
        int xfrom = e.getFrom().getBlockX();
        int zfrom = e.getFrom().getBlockZ();
        int yfrom = e.getFrom().getBlockY();

        if ((xto != xfrom) || (zto != zfrom) || (yto != yfrom)) {

            Block block = e.getTo().clone().add(0, -1, 0).getBlock();
            Block block1 = e.getFrom().clone().add(0, -1, 0).getBlock();

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                if (dataManager.hasData(block, "elevator")) {

                    int floor = (int) dataManager.getdata(block, "elevator", false);
                    int minfloor;
                    int maxfloor;

                    if (dataManager.hasData(block, "base")) {
                        Location base = (Location) dataManager.getdata(block, "base", true);
                        minfloor = (int) dataManager.getdata(base.getBlock(), "minfloor", false);
                        maxfloor = (int) dataManager.getdata(base.getBlock(), "maxfloor", false);
                    } else {
                        minfloor = (int) dataManager.getdata(block, "minfloor", false);
                        maxfloor = (int) dataManager.getdata(block, "maxfloor", false);
                    }

                    dataManager.sendTitle(mm.deserialize(title.replace("[floor]", String.valueOf(floor)).replace("[maxfloor]", String.valueOf(maxfloor)).replace("[minfloor]", String.valueOf(minfloor))), mm.deserialize(subtitle.replace("[floor]", String.valueOf(floor)).replace("[maxfloor]", String.valueOf(maxfloor)).replace("[minfloor]", String.valueOf(minfloor))), 0, 10000, 0, p);

                    if (!actionbartask.containsKey(p.getUniqueId())) {
                        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                            dataManager.sendActionbar(mm.deserialize(actionbar.replace("[floor]", String.valueOf(floor)).replace("[maxfloor]", String.valueOf(maxfloor)).replace("[minfloor]", String.valueOf(minfloor))), p);
                        }, 0L, 20L);
                        actionbartask.put(p.getUniqueId(), task);
                    }
                } else if (dataManager.hasData(block1, "elevator")) {
                    if (actionbartask.containsKey(p.getUniqueId())) {
                        actionbartask.get(p.getUniqueId()).cancel();
                        actionbartask.remove(p.getUniqueId());
                    }
                    p.clearTitle();
                }
            });

        }

    }

    @EventHandler
    private void PlayerSneak(PlayerToggleSneakEvent e) {

        Player p = e.getPlayer();
        Block block = e.getPlayer().getLocation().clone().add(0, -0.25, 0).getBlock();

        CMIUser user = CMI.getInstance().getPlayerManager().getUser(p);
        Location lastloc;
        if (user.getLastTeleportLocation() != null) {
            lastloc = user.getLastTeleportLocation();
        } else {
            lastloc = null;
        }

        if (!p.isSneaking()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                if (dataManager.hasData(block, "elevator")) {
                    Location loc = block.getLocation().clone().add(0.5,0,0.5);
                    int y = loc.getBlockY() - mingap;
                    boolean found = false;
                    int floor = 1;
                    for (int i = -mingap; i >= -maxgap; i--) {
                        y--;
                        loc.setY(y);
                        if (dataManager.hasData(loc.getBlock(), "elevator")) {
                            found = true;
                            floor = (int) dataManager.getdata(loc.getBlock(), "elevator", false);
                            break;
                        }
                    }
                    if (found) {
                        loc.setY(++y);
                        if (!loc.getBlock().getType().isAir()) {
                            p.sendMessage(mm.deserialize(notsafe.replace("[prefix]", prefix)));
                            return;
                        }
                        int minfloor;
                        int maxfloor;

                        if (dataManager.hasData(block, "base")) {
                            Location base = (Location) dataManager.getdata(block, "base", true);
                            minfloor = (int) dataManager.getdata(base.getBlock(), "minfloor", false);
                            maxfloor = (int) dataManager.getdata(base.getBlock(), "maxfloor", false);
                        } else {
                            minfloor = (int) dataManager.getdata(block, "minfloor", false);
                            maxfloor = (int) dataManager.getdata(block, "maxfloor", false);
                        }

                        dataManager.sendTitle(mm.deserialize(title.replace("[floor]", String.valueOf(floor)).replace("[maxfloor]", String.valueOf(maxfloor)).replace("[minfloor]", String.valueOf(minfloor))), mm.deserialize(subtitle.replace("[floor]", String.valueOf(floor)).replace("[maxfloor]", String.valueOf(maxfloor)).replace("[minfloor]", String.valueOf(minfloor))), 0, 10000, 0, p);
                        dataManager.sendActionbar(mm.deserialize(actionbar.replace("[floor]", String.valueOf(floor)).replace("[maxfloor]", String.valueOf(maxfloor)).replace("[minfloor]", String.valueOf(minfloor))), p);
                        p.playSound(elevated);
                        loc.setYaw(p.getYaw());
                        loc.setPitch(p.getPitch());
                        p.teleportAsync(loc);
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (lastloc != null) {
                                user.setLastTeleportLocation(lastloc, true);
                            }
                        });
                    }
                }
            });
        }
    }

    @EventHandler
    private void PlayerJump(PlayerJumpEvent e) {
        Player p = e.getPlayer();
        Block block = e.getPlayer().getLocation().clone().add(0, -0.25, 0).getBlock();

        CMIUser user = CMI.getInstance().getPlayerManager().getUser(p);
        Location lastloc;
        if (user.getLastTeleportLocation() != null) {
            lastloc = user.getLastTeleportLocation();
        } else {
            lastloc = null;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (dataManager.hasData(block, "elevator")) {
                Location loc = block.getLocation().clone().add(0.5,0,0.5);
                int y = loc.getBlockY() + mingap;
                boolean found = false;
                int floor = 1;
                for (int i = mingap; i <= maxgap; i ++) {
                    y++;
                    loc.setY(y);
                    if (dataManager.hasData(loc.getBlock(), "elevator")) {
                        found = true;
                        floor = (int) dataManager.getdata(loc.getBlock(), "elevator", false);
                        break;
                    }
                }
                if (found) {
                    loc.setY(++y);
                    if (!loc.getBlock().getType().isAir()) {
                        p.sendMessage(mm.deserialize(notsafe.replace("[prefix]", prefix)));
                        return;
                    }
                    int minfloor;
                    int maxfloor;

                    if (dataManager.hasData(block, "base")) {
                        Location base = (Location) dataManager.getdata(block, "base", true);
                        minfloor = (int) dataManager.getdata(base.getBlock(), "minfloor", false);
                        maxfloor = (int) dataManager.getdata(base.getBlock(), "maxfloor", false);
                    } else {
                        minfloor = (int) dataManager.getdata(block, "minfloor", false);
                        maxfloor = (int) dataManager.getdata(block, "maxfloor", false);
                    }


                    dataManager.sendTitle(mm.deserialize(title.replace("[floor]", String.valueOf(floor)).replace("[maxfloor]", String.valueOf(maxfloor)).replace("[minfloor]", String.valueOf(minfloor))), mm.deserialize(subtitle.replace("[floor]", String.valueOf(floor)).replace("[maxfloor]", String.valueOf(maxfloor)).replace("[minfloor]", String.valueOf(minfloor))), 0, 10000, 0, p);
                    dataManager.sendActionbar(mm.deserialize(actionbar.replace("[floor]", String.valueOf(floor)).replace("[maxfloor]", String.valueOf(maxfloor)).replace("[minfloor]", String.valueOf(minfloor))), p);
                    p.playSound(elevated);
                    loc.setYaw(p.getYaw());
                    loc.setPitch(p.getPitch());
                    p.teleportAsync(loc);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (lastloc != null) {
                            user.setLastTeleportLocation(lastloc, true);
                        }
                    });
                }
            }
        });
    }

}
