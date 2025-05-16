package dev.lipoteam.lipoExtra.Events;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.Zrips.CMI.events.CMIPvEEndEventAsync;
import com.Zrips.CMI.events.CMIPvEStartEventAsync;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import dev.lipoteam.lipoExtra.Commands.BetaCommands;
import dev.lipoteam.lipoExtra.Files.TagConfig;
import dev.lipoteam.lipoExtra.Manager.DataManager;
import dev.lipoteam.lipoExtra.Files.Configurations;
import dev.lipoteam.lipoExtra.LipoExtra;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import se.file14.procosmetics.api.events.PlayerEquipCosmeticEvent;
import se.file14.procosmetics.api.events.PlayerUnequipCosmeticEvent;
import se.file14.procosmetics.cosmetic.pet.P;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Event implements Listener {

    private final DataManager dataManager;

    public static ConcurrentHashMap<UUID, String> playerClient = new ConcurrentHashMap<>();
    private final HashMap<String, BukkitTask> bukkitTasks = new HashMap<>();
    private final HashMap<UUID, AtomicInteger> chargelimit = new HashMap<>();
    private ConcurrentHashMap<String, String> bedrockInvTitle = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Map.Entry<String, Integer>> cIList = new ConcurrentHashMap<>();
    private final LinkedHashMap<UUID, BukkitTask> flyparticles = new LinkedHashMap<>();
    public static HashSet<UUID> stopparticles = new HashSet<>();
    private final HashSet<UUID> flymove = new HashSet<>();
    private List<World> disabledParticleWorlds = new ArrayList<>();
    private final BukkitScheduler scheduler;
    public static ConcurrentHashMap<UUID, List<String>> playertags = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Integer> p2coins = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> p2ranks = new ConcurrentHashMap<>();

    private final LipoExtra lipo;

    private final List<UUID> delayPlayer = new ArrayList<>();
    private final List<UUID> delayPlayerCmd = new ArrayList<>();

    private Sound onCmdSound;
    private long delayPeriod;
    private String javatext;
    private String bedrocktext;
    private String chargeoverlimitmsg;
    private String procosmeticparticle;
    private Boolean java;
    private Boolean bedrock;
    private Boolean override;
    private Boolean commandInterrupt;
    private Boolean commandSound;
    private Boolean actionbarFilter;
    private Boolean flightlimit;
    private boolean fetchenabled;
    private List<String> expectedtext;
    private List<String> blacklistbetaitems;
    private Particle flyParticle;
    private Double flyParticleSpeed;
    private Double flyParticleSpeedMove;
    private int flyParticleCount;
    private long flyParticleTicks;
    private int flyParticleRadius;
    private int flightlimitint;
    private boolean betaornot;
    private TagConfig tagConfig;
    private boolean flyCMI;
    private String flyCMICommand;

    private Particle cvparticle;
    private double cvspeed;
    private int cvcount;
    private String cvitemname;
    private List<String> cvitemlore;

    private final List<UUID> delayCV = new ArrayList<>();
    private Component CVUsed;
    private Component TamedCantTp;

    private String prefix;

    MiniMessage mm = MiniMessage.miniMessage();

    public Event(Configurations config, LipoExtra lipoHud) {

        this.lipo = lipoHud;
        scheduler = lipo.getServer().getScheduler();
        dataManager = new DataManager(lipo);
        setConfig(config);

        ProtocolManager protocolmanager = ProtocolLibrary.getProtocolManager();

        protocolmanager.addPacketListener(DetectActionBar());
        protocolmanager.addPacketListener(DetectActionBar1());

        PacketType packetType = PacketType.Play.Client.WINDOW_CLICK;

    }

    private final List<UUID> delayexploit = new ArrayList<>();

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.getType().name().contains("BUNDLE") && event.getAction().isRightClick()) {
            event.setCancelled(true);

            Player player = event.getPlayer();

            BundleMeta bundleMeta = (BundleMeta) item.getItemMeta();
            if (bundleMeta != null) {
                List<ItemStack> items = bundleMeta.getItems();

                if (!items.isEmpty()) {
                    ItemStack drop = items.getFirst();
                    List<ItemStack> newitems = new ArrayList<>(bundleMeta.getItems());
                    newitems.removeFirst();
                    bundleMeta.setItems(newitems);
                    item.setItemMeta(bundleMeta);
                    player.getWorld().dropItem(player.getLocation(), drop);
                }
            }

            UUID id = event.getPlayer().getUniqueId();
            if (!delayexploit.contains(id)) {
                event.getPlayer().sendMessage(mm.deserialize("<red>Drop item from bundle is using alternative method due to a known crash exploit."));
                delayexploit.add(id);
                Bukkit.getScheduler().runTaskLater(lipo, () -> delayexploit.remove(id), 180L);
            }
        }
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

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onInventoryOpen(InventoryOpenEvent event) {
        if (!override) {
            return;
        }


        Inventory inv = event.getInventory();

        for (ItemStack item : inv.getContents()) {
            if (item != null) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {

                    String name = meta.getDisplayName();
                    if (!name.isEmpty()) {
                        String parsedName = PlaceholderAPI.setPlaceholders((OfflinePlayer) event.getPlayer(), name);
                        meta.displayName(mm.deserialize(convert(parsedName, true, '§', true)).decoration(TextDecoration.ITALIC, false));
                    }

                    List<String> lores = meta.getLore();
                    if (lores != null && !lores.isEmpty()) {
                        List<Component> newLore = new ArrayList<>();
                        for (String line : lores) {
                            String parsedLore = PlaceholderAPI.setPlaceholders((OfflinePlayer) event.getPlayer(), line);
                            newLore.add(mm.deserialize(convert(parsedLore, true, '§', true)).decoration(TextDecoration.ITALIC, false));
                        }
                        meta.lore(newLore);
                    }

                    item.setItemMeta(meta);

                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void InventoryClick(InventoryClickEvent event) {
        if (!override) {
            return;
        }

        Inventory inv = event.getInventory();

        if (event.getWhoClicked() instanceof Player p) {
            for (ItemStack item : inv.getContents()) {
                if (item != null) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {

                        String name = meta.getDisplayName();
                        if (!name.isEmpty()) {
                            String parsedName = PlaceholderAPI.setPlaceholders((OfflinePlayer) p, name);
                            meta.displayName(mm.deserialize(convert(parsedName, true, '§', true)).decoration(TextDecoration.ITALIC, false));
                        }

                        List<String> lores = meta.getLore();
                        if (lores != null && !lores.isEmpty()) {
                            List<Component> newLore = new ArrayList<>();
                            for (String line : lores) {
                                String parsedLore = PlaceholderAPI.setPlaceholders((OfflinePlayer) p, line);
                                newLore.add(mm.deserialize(convert(parsedLore, true, '§', true)).decoration(TextDecoration.ITALIC, false));
                            }
                            meta.lore(newLore);
                        }

                        item.setItemMeta(meta);

                    }
                }
            }
        }
    }

    private String convert(String legacy, boolean concise, char charCode, boolean rgb) {
        // Convert legacy color codes to MiniMessage format

        if (rgb) {
            Pattern pattern = Pattern.compile("§x(§[0-9a-fA-F]){6}");
            Matcher matcher = pattern.matcher(legacy);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                // Extract RGB characters
                String hexColor = matcher.group().replace("§x", "").replace("§", "");
                matcher.appendReplacement(sb, "<#" + hexColor + ">");
            }
            matcher.appendTail(sb);
            legacy = sb.toString();
        }

        String miniMessage = legacy
                .replace(charCode + "0", "<black>")
                .replace(charCode + "1", "<dark_blue>")
                .replace(charCode + "2", "<dark_green>")
                .replace(charCode + "3", "<dark_aqua>")
                .replace(charCode + "4", "<dark_red>")
                .replace(charCode + "5", "<dark_purple>")
                .replace(charCode + "6", "<gold>")
                .replace(charCode + "7", "<gray>")
                .replace(charCode + "8", "<dark_gray>")
                .replace(charCode + "9", "<blue>")
                .replace(charCode + "a", "<green>")
                .replace(charCode + "b", "<aqua>")
                .replace(charCode + "c", "<red>")
                .replace(charCode + "d", "<light_purple>")
                .replace(charCode + "e", "<yellow>")
                .replace(charCode + "f", "<white>");

        // Convert formatting codes
        if (concise) {
            miniMessage = miniMessage
                    .replace(charCode + "n", "<u>")
                    .replace(charCode + "m", "<st>")
                    .replace(charCode + "k", "<obf>")
                    .replace(charCode + "o", "<i>")
                    .replace(charCode + "l", "<b>")
                    .replace(charCode + "r", "<reset>");
        } else {
            miniMessage = miniMessage
                    .replace(charCode + "n", "<underlined>")
                    .replace(charCode + "m", "<strikethrough>")
                    .replace(charCode + "k", "<obfuscated>")
                    .replace(charCode + "o", "<italic>")
                    .replace(charCode + "l", "<bold>")
                    .replace(charCode + "r", "<reset>");
        }

        // Convert RGB hex codes (e.g., & #ff00ff -> <#ff00ff>)

        return miniMessage;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void PlayerCommand(PlayerCommandPreprocessEvent e) {
        if (!commandInterrupt) return;

        String[] args = e.getMessage().substring(1).split(" ");
        if (args.length < 2) return; // Must have at least command + target

        String baseCommand = args[0] + " " + args[1];

        for (String key : cIList.keySet()) {
            String commandKey = key.replace("_", " "); // e.g. "cmi_tpa" -> "cmi tpa"
            if (baseCommand.equalsIgnoreCase(commandKey)) {

                String targetName = args[1];
                Player target = Bukkit.getPlayer(targetName);

                if (target != null) {
                    UUID targetUUID = target.getUniqueId();

                    if (playerClient.get(targetUUID).equalsIgnoreCase("bedrock") && !delayPlayerCmd.contains(targetUUID)) {
                        String interruptCmd = cIList.get(key).getKey().replace("%receiver-bedrock%", target.getName());

                        delayPlayerCmd.add(targetUUID);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                delayPlayerCmd.remove(targetUUID);
                            }
                        }.runTaskLater(lipo, cIList.get(key).getValue() * 20L);

                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), interruptCmd);
                        e.setCancelled(true); // Cancel the original command
                        return;
                    }
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
        deltaX = Math.abs(from.getX() - to.getX());
        deltaZ = Math.abs(from.getZ() - to.getZ());
        double movementThreshold = 0.02; // Ignore tiny movements

        boolean isMoving = (deltaX > movementThreshold || deltaZ > movementThreshold);

        // Only update metadata when state actually changes
        if (!flymove.contains(p.getUniqueId())) {
            flymove.add(p.getUniqueId());
        } else if (!isMoving) {
            flymove.remove(p.getUniqueId());
        }
    }

    @EventHandler
    private void PlayerEquipCosmetics(PlayerEquipCosmeticEvent e) {
        if (e.getCosmeticType().getCategoryPath().contains(procosmeticparticle)) {
            stopparticles.add(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    private void PlayerUnequipCosmetics(PlayerUnequipCosmeticEvent e) {
        if (e.getCosmeticType().getCategoryPath().contains(procosmeticparticle)) {
            stopparticles.remove(e.getPlayer().getUniqueId());
        }
    }

    // CAN BE IMPROVISED
    @EventHandler
    private void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();
        UUID playerId = p.getUniqueId();
        CMIUser user = CMI.getInstance().getPlayerManager().getUser(p);

        if (user != null && user.isVanished()) {
            removeFlyParticles(playerId);
            return;
        }

        if (e.isFlying()) {
            removeFlyParticles(playerId);

            BukkitTask task = scheduler.runTaskTimer(lipo, () -> {
                if (!p.isFlying() || disabledParticleWorlds.contains(p.getWorld()) || stopparticles.contains(playerId)) {
                    removeFlyParticles(playerId);
                    return;
                }

                String particleMode = (String) dataManager.getdata(p, "flyparticle", false);
                if (particleMode != null) {
                    boolean isMoving = flymove.contains(playerId);
                    double speed = isMoving ? flyParticleSpeedMove : flyParticleSpeed;

                    if (particleMode.equals("minimal") || particleMode.equals("all")) {
                        p.spawnParticle(flyParticle, p.getLocation().add(0, -0.1, 0), flyParticleCount, 0, 0, 0, speed, null);
                    }

                    for (Player nearby : p.getLocation().getNearbyPlayers(flyParticleRadius)) {
                        if (nearby.equals(p)) continue;
                        String nearbyMode = (String) dataManager.getdata(nearby, "flyparticle", false);
                        if ("all".equals(nearbyMode) || "others".equals(nearbyMode)) {
                            if (!disabledParticleWorlds.contains(p.getWorld())) {
                                if (user != null) {
                                    if (!user.isVanished()) {
                                        nearby.spawnParticle(flyParticle, p.getLocation().add(0, -0.1, 0), flyParticleCount, 0, 0, 0, speed, null);
                                    }
                                } else {
                                    nearby.spawnParticle(flyParticle, p.getLocation().add(0, -0.1, 0), flyParticleCount, 0, 0, 0, speed, null);
                                }
                            }
                        }
                    }
                }
            }, 1L, flyParticleTicks);

            flyparticles.put(playerId, task);
        } else {
            removeFlyParticles(playerId);
        }
    }

    // Helper method to remove particles
    private void removeFlyParticles(UUID playerId) {
        if (flyparticles.containsKey(playerId)) {
            flyparticles.get(playerId).cancel();
            flyparticles.remove(playerId);
        }
    }

    @EventHandler
    private void PlayerJoin(PlayerJoinEvent e) {

        Player player = e.getPlayer();
        FloodgateApi floodgate = FloodgateApi.getInstance();
        UUID uuid = player.getUniqueId();

        List<String> ptags = new ArrayList<>(List.of());

        if (floodgate.isFloodgatePlayer(uuid)) {
            playerClient.put(uuid, "bedrock");
        } else {
            playerClient.put(uuid, "java");
        }

        if (!player.hasPlayedBefore()) {
            Currency currency = CoinsEngineAPI.getCurrency("stars");
            if (currency != null) {
                CoinsEngineAPI.addBalance(player, currency, getConvertedStars(player.getUniqueId()));
            }

            Bukkit.dispatchCommand(lipo.getServer().getConsoleSender(), "lp user " + player.getUniqueId() + " permission set lipo.tag.beta");
            ptags.add("beta");
        }

        for (String tag : tagConfig.getTags()) {
            if (player.hasPermission(tagConfig.getPerms(tag))) {
                if (!ptags.contains(tag)) {
                    ptags.add(tag);
                }
            }
        }

        playertags.put(player.getUniqueId(), ptags);

        if (!dataManager.hasData(player, "cmdSound")) {
            dataManager.setdata(player, "cmdSound", true);
        }

        if (!dataManager.hasData(player, "flyparticle")) {
            dataManager.setdata(player, "flyparticle", "all");
        }

        Currency currency = CoinsEngineAPI.getCurrency("coins");

        if (currency != null && fetchenabled) {
            scheduler.runTaskLater(lipo, () -> {
                String name = player.getName();

                if (!dataManager.hasData(player, "getcoins")) {
                    if (p2coins.containsKey(name)) {

                        int coins = p2coins.get(name);

                        if (coins != 0) {

                            if (coins <= 1000) {
                                coins = 1;
                            } else {
                                coins /= 1000;
                            }

                            lipo.adventure().player(player).playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.PLAYER, 1F, 1F));
                            dataManager.sendTitle(mm.deserialize(fetchtitle), mm.deserialize(fetchsubtitle), fetchduration.getFirst(), fetchduration.get(1), fetchduration.getLast(), player);
                            dataManager.sendMessage(player, mm.deserialize(String.join("<br>", fetchcoinsmsg).replace("[coins]", String.valueOf(coins))));
                            dataManager.setdata(player, "getcoins", true);
                            CoinsEngineAPI.setBalance(player, currency, coins);
                        }
                    }
                }

                if (!dataManager.hasData(player, "getranks")) {
                    scheduler.runTaskLater(lipo, () -> {
                        String newrank = "default";

                        if (p2ranks.containsKey(name)) {
                            String prevrank = p2ranks.get(name);
                            if (newrank.equals(p2ranks.get(name))) return;

                            if (prevrank.equalsIgnoreCase("aetherian_survival")) {
                                newrank = "dato";
                            } else if (prevrank.equalsIgnoreCase("celestial_survival")) {
                                newrank = "wira";
                            } else if (prevrank.equalsIgnoreCase("luminary_survival") || prevrank.equalsIgnoreCase("nebulon_survival")) {
                                newrank = "bandar";
                            } else if (prevrank.equalsIgnoreCase("media_survival")) {
                                newrank = "media";
                            } else if (prevrank.equalsIgnoreCase("developer")) {
                                newrank = "dev";
                            } else if (prevrank.equalsIgnoreCase("helper")) {
                                newrank = "helper";
                            } else if (prevrank.equalsIgnoreCase("owner_survival")) {
                                newrank = "owner";
                            }

                            lipo.adventure().player(player).playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.PLAYER, 0.5F, 1F));
                            dataManager.sendMessage(player, mm.deserialize(String.join("<br>", fetchranksmsg).replace("[rank]", newrank)));

                            dataManager.setdata(player, "getranks", true);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user [player] parent add [rank]".replace("[player]", String.valueOf(player.getUniqueId())).replace("[rank]", newrank));
                        }
                    }, 20L * 5);
                }


            }, 20L * 7);
        }

    }

    @EventHandler
    private void TameEntityTeleport(PlayerTeleportEvent e) {

        if (e.getTo().getWorld() != e.getFrom().getWorld()) {
            Location loc = e.getFrom();
            Player p = e.getPlayer();

            Bukkit.getScheduler().runTaskLater(lipo, () -> {
                Collection<Tameable> nearbyEntities = loc.getNearbyEntitiesByType(Tameable.class, 5);
                nearbyEntities.stream()
                        .filter(t -> t.getOwner() != null && t.getOwner().getUniqueId().equals(p.getUniqueId()))
                        .forEach(t -> teleportTamedEntity(p, t));
            }, 1L);
        }
    }

    private void teleportTamedEntity(Player p, Tameable t) {

        if (!t.isInvulnerable()) {
            Bukkit.getScheduler().runTaskLater(lipo, () -> t.setInvulnerable(false), 100L);
        }

        Location tploc = p.getLocation().clone();
        tploc.setY(getGroundY(tploc));

        if (tploc.getBlockY() >= 256 || tploc.getBlockY() <= -64) {
            p.sendMessage(TamedCantTp);
            return;
        }

        if (!t.isLeashed() && (!(t instanceof Sittable s) || !s.isSitting())) {
            t.setInvulnerable(true);
            t.teleport(tploc);
        }

    }


    private int getGroundY(Location loc) {
        double y = loc.getY();

        if (loc.getBlock().getType().isAir()) {
            while (loc.getBlock().getType().isAir() && y > -64) {
                y--;
                loc.setY(y);
            }
            if (y > -64) {
                y++;
            }
        } else {
            while (!loc.getBlock().getType().isAir() && y < 256) {
                y++;
                loc.setY(y);
            }
        }

        return (int) y;
    }

    @EventHandler
    private void ChunkVisualizerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        ItemStack item = e.getItem();
        Player p = e.getPlayer();

        if (delayCV.contains(p.getUniqueId())) return;

        if (item == null || !dataManager.hasData(item, "uses")) return;

        int uses = (int) dataManager.getdata(item, "uses", false);

        Chunk chunk = p.getLocation().getChunk();
        World world = chunk.getWorld();

        int baseX = chunk.getX() << 4;
        int baseZ = chunk.getZ() << 4;
        double y = p.getLocation().getY();

        List<Location> borderLocations = new ArrayList<>(64);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (x == 0 || x == 15 || z == 0 || z == 15) {
                    borderLocations.add(new Location(world, baseX + x + 0.5, y, baseZ + z + 0.5));
                }
            }
        }

        final int maxTicks = 16;
        final int interval = 2;

        final int[] repeat = {0};
        Bukkit.getScheduler().runTaskTimer(lipo, d -> {
            if (repeat[0] >= 3) {
                d.cancel();
                return;
            }

            final int[] ticks = {0};

            Bukkit.getScheduler().runTaskTimer(lipo, t -> {
                if (ticks[0] >= maxTicks) {
                    t.cancel();
                    return;
                }

                for (Location loc : borderLocations) {
                    Location theloc = loc.clone();
                    theloc.setY(y + ticks[0]);
                    p.spawnParticle(cvparticle, theloc, cvcount, 0, 0, 0, cvspeed);
                }

                ticks[0]++;
            }, 0L, interval);

            repeat[0]++;
        }, 0L, (maxTicks * interval) + 15L);

        delayCV.add(p.getUniqueId());
        Bukkit.getScheduler().runTaskLater(lipo, () -> {
            delayCV.remove(p.getUniqueId());
        }, 60L);

        if (uses > 1) {
            dataManager.setdata(item, "uses", uses - 1);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(mm.deserialize(cvitemname));
                List<Component> lore = cvitemlore.stream()
                        .map(u -> mm.deserialize(u.replace("[uses]", String.valueOf(uses - 1))))
                        .collect(Collectors.toList());
                meta.lore(lore);
                item.setItemMeta(meta);
            }
            p.sendMessage(CVUsed);
        } else {
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1); // just subtract 1
            } else {
                if (e.getHand() == EquipmentSlot.HAND) {
                    p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                } else if (e.getHand() == EquipmentSlot.OFF_HAND) {
                    p.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                }
            }
        }
    }

    @EventHandler
    private void PlayerLeave(PlayerQuitEvent e) {
        UUID playerID = e.getPlayer().getUniqueId();
        playerClient.remove(playerID);
        removeFlyParticles(playerID);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void RechargeInv(InventoryClickEvent e) {
        if (!flightlimit) return;
        if (mm.serialize(e.getView().title()).contains("recharge")) {
            if (e.getWhoClicked() instanceof Player p) {

                if (e.isShiftClick() && e.isLeftClick()) {
                    chargelimit.computeIfAbsent(p.getUniqueId(), k -> new AtomicInteger(0)).addAndGet(10);
                } else if (e.isShiftClick() && e.isRightClick()) {
                    chargelimit.computeIfAbsent(p.getUniqueId(), k -> new AtomicInteger(0)).addAndGet(1000);
                } else if (e.isLeftClick()) {
                    chargelimit.computeIfAbsent(p.getUniqueId(), k -> new AtomicInteger(0)).addAndGet(1);
                } else if (e.isRightClick()) {
                    chargelimit.computeIfAbsent(p.getUniqueId(), k -> new AtomicInteger(0)).addAndGet(100);
                }

                int currcharge = chargelimit.computeIfAbsent(p.getUniqueId(), k -> new AtomicInteger(0)).get();
                if (currcharge >= flightlimitint) {
                    lipo.adventure().player(p).sendMessage(mm.deserialize(chargeoverlimitmsg));
                    e.setCancelled(true);
                }

            }
        }
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
        }, 0, 20);
        bukkitTasks.put("actionbar", task);

    }

    @EventHandler
    private void BetaInv(InventoryClickEvent e) {
        if (!e.getView().title().equals(mm.deserialize("<dark_gray>Beta Items"))) {
            return;
        }

        Player p = (Player) e.getWhoClicked();
        boolean isPlayerInventory = Objects.equals(e.getClickedInventory(), p.getInventory());

        if (e.getSlot() != 4 && !isPlayerInventory) {
            e.setCancelled(true);
        }

        if (e.isShiftClick()) {
            e.setCancelled(true);
            return;
        }

        if (e.getClick() == ClickType.NUMBER_KEY && !isPlayerInventory) {
            e.setCancelled(true);
            return;
        }

        if (e.getClick() == ClickType.SWAP_OFFHAND && !isPlayerInventory) {
            e.setCancelled(true);
            return;
        }

        ItemStack item = e.getCursor();

        if (!betaornot) {
            if (!isPlayerInventory) {
                if (Objects.equals(e.getView().getTopInventory().getItem(4), ItemStack.of(Material.AIR))) {
                    e.setCancelled(true);
                    return;
                } else if (!Objects.equals(e.getView().getTopInventory().getItem(4), ItemStack.of(Material.AIR)) && !Objects.equals(item, ItemStack.of(Material.AIR))) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        if (item.getAmount() > 1 && !item.isEmpty() && !isPlayerInventory) {
            e.setCancelled(true);
            return;
        }

        for (String blacklist : blacklistbetaitems) {
            if (item.getType().name().contains(blacklist) && !isPlayerInventory) {
                e.setCancelled(true);
                return;
            }
        }

        if (betaornot) {
            Bukkit.getScheduler().runTaskLater(lipo, () -> {
                ItemStack newItem = e.getView().getTopInventory().getItem(4); // Or re-check the GUI slot if custom inventory
                BetaCommands.setBetaItemsCache(p.getUniqueId(), newItem);
                saveBetaItem(p.getUniqueId(), newItem);
            }, 1L);
        } else {
            BetaCommands.setBetaItemsCache(p.getUniqueId(), ItemStack.of(Material.AIR));
            removeBetaFile(p.getUniqueId());
        }

    }

    @EventHandler
    private void onDrag(InventoryDragEvent e) {
        if (!e.getView().title().equals(mm.deserialize("<dark_gray>Beta Items"))) return;

        if (e.getRawSlots().contains(4)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void PlayerStartPVE(CMIPvEStartEventAsync e) {
        if (flyCMI) {
            if (e.getPlayer().isFlying()) {
                e.getPlayer().setMetadata("fly", new FixedMetadataValue(lipo, true));
            }
        }
    }

    @EventHandler
    private void PlayerEndPVE(CMIPvEEndEventAsync e) {
        if (flyCMI) {
            if (e.getPlayer().hasMetadata("fly")) {
                boolean fly = e.getPlayer().getMetadata("fly").getFirst().asBoolean();
                if (fly) {
                    Bukkit.getScheduler().runTask(lipo, () -> Bukkit.dispatchCommand(e.getPlayer(), flyCMICommand));
                    e.getPlayer().removeMetadata("fly", lipo);
                }
            }
        }
    }

    public int getConvertedStars(UUID id) {
        File folder = new File(lipo.getDataFolder(), "BetaItems");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, id.toString() + ".yml");

        if (!file.exists()) return 0;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getInt("converted-stars");
    }

    public String getConvertedTag(UUID id) {
        File folder = new File(lipo.getDataFolder(), "BetaItems");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, id.toString() + ".yml");

        if (!file.exists()) return "";

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getString("tag");
    }

    public void saveBetaItem(UUID uuid, ItemStack item) {
        File folder = new File(lipo.getDataFolder(), "BetaItems");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, uuid.toString() + ".yml");

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("itemstack", item);

        try {
            config.save(file);
        } catch (IOException e) {
            lipo.getLogger().warning("Failed to save beta item for " + uuid + "\n" + e.getMessage());
        }
    }

    public void removeBetaFile(UUID id) {
        File folder = new File(lipo.getDataFolder(), "BetaItems");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, id.toString() + ".yml");
        file.delete();
    }

    public List<UUID> getDelayPlayer() {
        return delayPlayer;
    }

    private List<String> fetchcoinsmsg = new ArrayList<>();
    private List<String> fetchranksmsg = new ArrayList<>();
    private List<Integer> fetchduration = new ArrayList<>();
    private String fetchtitle;
    private String fetchsubtitle;

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
        flyParticleTicks = config.FlyParticleTicks();
        fetchenabled = config.FetchEnabled();
        disabledParticleWorlds = config.DisableParticleWorlds();
        flyParticleRadius = config.FlyPartileRadius();
        flightlimit = config.RechargeLimit();
        flightlimitint = config.RechargeLimitNum();
        chargeoverlimitmsg = config.ChargeLimitMsg();
        procosmeticparticle = config.ProcosmeticsParticle();

        p2coins = config.playerCoins();
        p2ranks = config.playerRanks();
        fetchcoinsmsg = config.FetchCoinsMsg();
        fetchranksmsg = config.FetchRanksMsg();
        fetchduration = config.FetchDuration();
        fetchtitle = config.FetchTitle();
        fetchsubtitle = config.FetchSubtitle();

        betaornot = config.BetaOrNotBeta();
        blacklistbetaitems = config.BlacklistBetaItems();

        tagConfig = lipo.getTagConfig();
        flyCMI = config.FlyCMIEnabled();
        flyCMICommand = config.FlyCMICommand();

        cvparticle = config.CVParticle();
        cvspeed = config.CVParticleSpeed();
        cvcount = config.CVParticleCount();
        cvitemname = config.CVItemName();
        cvitemlore = config.CVItemLore();

        prefix = config.prefix();
        CVUsed = mm.deserialize(prefix + "Chunk visualized! <red>-1 uses");
        TamedCantTp = mm.deserialize(prefix + "<red>Tamed animals cannot find suitable location!");

        RunActionBar();
    }
}
