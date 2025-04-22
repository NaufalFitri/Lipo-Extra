package dev.lipoteam.lipoHud.Events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTList;
import dev.lipoteam.lipoHud.DataManager;
import dev.lipoteam.lipoHud.Files.Configurations;
import dev.lipoteam.lipoHud.LipoHud;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Event implements Listener {

    private final DataManager dataManager;

    public static ConcurrentHashMap<UUID, String> playerClient = new ConcurrentHashMap<>();
    private final HashMap<String, BukkitTask> bukkitTasks = new HashMap<>();
    private ConcurrentHashMap<String, String> bedrockInvTitle = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, HashMap<String, Integer>> cIList = new ConcurrentHashMap<>();
    private final LinkedHashMap<UUID, BukkitTask> flyparticles = new LinkedHashMap<>();
    public static HashSet<UUID> stopparticles = new HashSet<>();
    private final HashSet<UUID> flymove = new HashSet<>();
    private List<World> disabledParticleWorlds = new ArrayList<>();
    private final BukkitScheduler scheduler;

    private final LipoHud lipo;

    private final List<UUID> delayPlayer = new ArrayList<>();
    private final List<UUID> delayPlayerCmd = new ArrayList<>();

    private Sound onCmdSound;
    private long delayPeriod;
    private String javatext;
    private String bedrocktext;
    private Boolean java;
    private Boolean bedrock;
    private Boolean override;
    private Boolean commandInterrupt;
    private Boolean commandSound;
    private Boolean actionbarFilter;
    private List<String> expectedtext;
    private Particle flyParticle;
    private Double flyParticleSpeed;
    private Double flyParticleSpeedMove;
    private int flyParticleCount;

    public Event(Configurations config, LipoHud lipoHud) {

        this.lipo = lipoHud;
        scheduler = lipo.getServer().getScheduler();
        dataManager = new DataManager(lipo);
        setConfig(config);

        ProtocolManager protocolmanager = ProtocolLibrary.getProtocolManager();

        protocolmanager.addPacketListener(DetectActionBar());
        protocolmanager.addPacketListener(DetectActionBar1());
//        protocolmanager.addPacketListener(ModifyPlayerOpenInventory());

    }

    private PacketListener DetectActionBar() {
        return new PacketAdapter(lipo, ListenerPriority.HIGH, PacketType.Play.Server.SET_ACTION_BAR_TEXT) {
            @Override
            public void onPacketSending(PacketEvent event) {

                if (!actionbarFilter) {
                    return;
                }

                UUID playerId = event.getPlayer().getUniqueId();
                String clientType = playerClient.get(playerId);

                if (clientType == null) return;

                String text = event.getPacket().getChatComponents().readSafely(0).getJson();

                for (String extext : expectedtext) {
                    if (text.contains(extext)) {
                        if (!delayPlayer.contains(playerId)) {
                            delayPlayer.add(playerId);

                            new BukkitRunnable() {
                                @Override
                                public  void run() {
                                    delayPlayer.remove(playerId);
                                }
                            }.runTaskLater(plugin, delayPeriod);
                        }
                        break;

                    }
                }

            }
        };
    }

    private PacketListener DetectActionBar1() {
        return new PacketAdapter(lipo, ListenerPriority.HIGH, PacketType.Play.Server.SYSTEM_CHAT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Boolean actionbar = event.getPacket().getBooleans().readSafely(0);
                if (!actionbarFilter) {
                    return;
                }

                if (!actionbar) {
                    return;
                }

                UUID playerId = event.getPlayer().getUniqueId();
                String clientType = playerClient.get(playerId);

                if (clientType == null) return;

                String text = event.getPacket().getChatComponents().readSafely(0).getJson();

                for (String extext : expectedtext) {
                    if (text.contains(extext)) {
                        if (!delayPlayer.contains(playerId)) {
                            delayPlayer.add(playerId);

                            new BukkitRunnable() {
                                @Override
                                public  void run() {
                                    delayPlayer.remove(playerId);
                                }
                            }.runTaskLater(plugin, delayPeriod);
                        }
                        break;

                    }
                }

            }
        };
    }

//    private PacketListener ModifyPlayerOpenInventory() {
//        return new PacketAdapter(lipo, ListenerPriority.NORMAL, PacketType.Play.Server.OPEN_WINDOW) {
//            @Override
//            public void onPacketSending(PacketEvent event) {
//                if (!override) {
//                    return;
//                }
//
//                WrappedChatComponent text = event.getPacket().getChatComponents().read(0);
//                String json = text.getJson();
//
//                if (text.getJson().contains("\"translate\"")) {
//                    return;
//                }
//
//                if (playerClient.get(event.getPlayer().getUniqueId()).equalsIgnoreCase("bedrock")) {
//
//                    Pattern pattern = Pattern.compile("(<([^>-]+)-([^>]+)>)");
//                    Matcher matcher = pattern.matcher(json);
//
//                    while (matcher.find()) {
//
//                        String integer = matcher.group(3);
//
//                        if (bedrockInvTitle.containsKey(integer)) {
//                            String titletext = PlaceholderAPI.setPlaceholders(event.getPlayer(), bedrockInvTitle.get(integer).replace('&', '§'));
//                            event.getPacket().getChatComponents().write(0, WrappedChatComponent.fromLegacyText(titletext));
//                            break;
//                        }
//
//                    }
//
//                    return;
//                }
//
//                if (json.startsWith("\"") && json.endsWith("\"")) {
//                    json = json.substring(1, json.length() - 1);
//                }
//
//                String PlaceholderText = PlaceholderAPI.setPlaceholders(event.getPlayer(), json);
//                BaseComponent newtext = formatJsonText(PlaceholderText);
//                event.getPacket().getChatComponents().write(0, WrappedChatComponent.fromJson(ComponentSerializer.toString(newtext)));
//
//            }
//        };
//    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onInventoryOpen(InventoryOpenEvent event) {
        if (!override) {
            return;
        }
        var mm = MiniMessage.miniMessage();
        Inventory inv = event.getInventory();

        for (ItemStack item : inv.getContents()) {
            if (item != null) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {

                    NBT.modifyComponents(item, nbt -> {
                        if (nbt.hasTag("minecraft:custom_name")) {
                            String name = nbt.getString("minecraft:custom_name");
                            nbt.setString("minecraft:custom_name", PlaceholderAPI.setPlaceholders((OfflinePlayer) event.getPlayer(), JSONComponentSerializer.json().serialize(mm.deserialize(name))));
                        }
                        if (nbt.hasTag("minecraft:lore")) {
                            ReadWriteNBTList<String> loreList = nbt.getStringList("minecraft:lore");
                            int i = 0;
                            for (String line : loreList) {
                                String lore = PlaceholderAPI.setPlaceholders((OfflinePlayer) event.getPlayer(), JSONComponentSerializer.json().serialize(mm.deserialize(line)));
                                loreList.set(i, lore);
                                i++;
                            }
                        }
                    });

                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void PlayerCommand(PlayerCommandPreprocessEvent e) {
        if (!commandInterrupt) return;
        String playercmd = e.getMessage();
        for (String command : cIList.keySet()) {
            String commandwspace = command.replace("_", " ");
            if (playercmd.contains(commandwspace)) {
                if (playercmd.substring(1, commandwspace.length() + 1).equalsIgnoreCase(commandwspace)) {
                    String[] listcmd = playercmd.split(" ");
                    String interruptcmd = cIList.get(command).keySet().iterator().next();
                    for (String name : listcmd) {
                        if (Bukkit.getPlayer(name) != null) {
                            UUID player1 = Objects.requireNonNull(Bukkit.getPlayer(name)).getUniqueId();
                            if (playerClient.get(player1).equalsIgnoreCase("bedrock") && !delayPlayerCmd.contains(player1)) {
                                interruptcmd = interruptcmd.replace("%receiver-bedrock%", name);
                                delayPlayerCmd.add(player1);
                                new BukkitRunnable() {
                                    @Override
                                    public  void run() {
                                        delayPlayer.remove(player1);
                                    }
                                }.runTaskLater(lipo, cIList.get(command).get(interruptcmd) * 20L);
                            }

                        }
                    }
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), interruptcmd);
                }
            }

        }
    }

    @EventHandler
    private void PlayerTabComplete(TabCompleteEvent e) {
        if (!commandSound) return;

        Player player = Bukkit.getPlayer(e.getSender().getName());

        if (!((Boolean) dataManager.getdata(player, "cmdSound", false))) return;

        if (player != null) {
            lipo.adventure().player(player).playSound(onCmdSound);
        }

    }

    @EventHandler
    private void PlayerMoveFly(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        // Ignore head rotation & vertical movement for checking horizontal movement
        Location from = e.getFrom();
        Location to = e.getTo();

        double deltaX = 0;
        double deltaZ = 0;
        if (to != null) {
            deltaX = Math.abs(from.getX() - to.getX());
            deltaZ = Math.abs(from.getZ() - to.getZ());
        }
        double movementThreshold = 0.02; // Ignore tiny movements

        boolean isMoving = (deltaX > movementThreshold || deltaZ > movementThreshold);

        // Only update metadata when state actually changes
        if (!flymove.contains(p.getUniqueId())) {
            flymove.add(p.getUniqueId());
        } else if (!isMoving) {
            flymove.remove(p.getUniqueId());
        }
    }

    // CAN BE IMPROVISED
    @EventHandler
    private void PlayerFly(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();
        UUID playerId = p.getUniqueId();

        if (e.isFlying()) {
            if (flyparticles.containsKey(playerId)) {
                flyparticles.get(playerId).cancel();
            }

            BukkitTask task = scheduler.runTaskTimer(lipo, () -> {
                if (p.isFlying() && !disabledParticleWorlds.contains(p.getWorld())) {
                    if (!stopparticles.contains(p.getUniqueId())) {
                        if (!flymove.contains(p.getUniqueId())) {
                            p.spawnParticle(flyParticle, p.getLocation(), flyParticleCount, 0D,0D,0D, flyParticleSpeed, null);
                        } else {
                            p.spawnParticle(flyParticle, p.getLocation(), flyParticleCount, 0D,0D,0D, flyParticleSpeedMove, null);
                        }
                    }
                } else {
                    flyparticles.get(playerId).cancel();
                    flyparticles.remove(playerId);
                }
            }, 0L, 5L);

            flyparticles.put(playerId, task);
        } else {
            if (flyparticles.containsKey(playerId)) {
                flyparticles.get(playerId).cancel();
                flyparticles.remove(playerId);
            }
        }
    }

    @EventHandler
    private void PlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        FloodgateApi floodgate = FloodgateApi.getInstance();
        UUID uuid = player.getUniqueId();

        if (floodgate.isFloodgatePlayer(uuid)) {
            playerClient.put(uuid, "bedrock");
        } else {
            playerClient.put(uuid, "java");
        }

        if (!dataManager.hasData(player, "cmdSound")) {
            dataManager.setdata(player, "cmdSound", true);
        }
    }

    @EventHandler
    private void PlayerLeave(PlayerQuitEvent e) {
        UUID playerID = e.getPlayer().getUniqueId();
        playerClient.remove(playerID);
    }

    private void RunActionBar() {

        BukkitScheduler scheduler = lipo.getServer().getScheduler();

        BukkitTask task = scheduler.runTaskTimerAsynchronously(lipo, () -> {
            var mm = MiniMessage.miniMessage();
            playerClient.forEach((UUID, client) -> {
                OfflinePlayer player = lipo.getServer().getOfflinePlayer(UUID);
                if (Objects.equals(client, "java") && java && player.isOnline() && !delayPlayer.contains(UUID)) {
                    Component parsed = mm.deserialize(PlaceholderAPI.setPlaceholders(player, javatext));
                    lipo.adventure().player(UUID).sendActionBar(parsed);
                } else if (Objects.equals(client, "bedrock") && bedrock && player.isOnline() && !delayPlayer.contains(UUID)){
                    Component parsed = mm.deserialize(PlaceholderAPI.setPlaceholders(player, bedrocktext));
                    lipo.adventure().player(UUID).sendActionBar(parsed);
                 }
             });
        }, 0, 40);
        bukkitTasks.put("actionbar", task);

    }

    public List<UUID> getDelayPlayer() {
        return delayPlayer;
    }

    public void setConfig(Configurations config) {

        for (BukkitTask task : bukkitTasks.values()) {
            task.cancel();
        }

        bukkitTasks.clear();

        bedrockInvTitle = config.InvTitleBedrock();
        javatext = config.JavaText();
        bedrocktext = config.BedrockText();
        override = config.OverrideEnabled();
        java = config.JavaEnabled();
        bedrock = config.BedrockEnabled();
        commandInterrupt = config.CommandInterrupt();
        cIList = config.CommandInterruptList();
        commandSound = config.SoundOnCommand();
        onCmdSound = config.commandSound();
        expectedtext = config.actionBarListFilter();
        delayPeriod = config.delayPeriod();
        actionbarFilter = config.actionBarFilter();
        flyParticle = config.FlyParticle();
        flyParticleSpeed = config.FlyParticleSpeed();
        flyParticleSpeedMove = config.FlyParticleSpeedMove();
        flyParticleCount = config.FlyParticleCount();
        disabledParticleWorlds = config.DisableParticleWorlds();

        RunActionBar();
    }
}
