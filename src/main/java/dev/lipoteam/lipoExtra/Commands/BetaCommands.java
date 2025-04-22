package dev.lipoteam.lipoExtra.Commands;

import com.Zrips.CMI.CMI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.lipoteam.lipoExtra.Files.Configurations;
import dev.lipoteam.lipoExtra.LipoExtra;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BetaCommands {

    private File betaitems;

    private Configurations config;
    private LipoExtra plugin;
    private boolean enabled;
    private boolean betaornot;
    private int scalefactor;
    private static final Map<UUID, ItemStack> betaItemsCache = new HashMap<>();
    private final Inventory betaInv;

    public BetaCommands(Configurations config, LipoExtra plugin, File betaitems) {

        var mm = MiniMessage.miniMessage();
        betaInv = Bukkit.createInventory(null, InventoryType.DROPPER,  mm.deserialize("<dark_gray>Beta Items"));
        for (int i = 0; i < 9; i++) {
            if (i != 4) {
                ItemStack pane = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE, 1);
                ItemMeta meta = pane.getItemMeta();
                meta.displayName(mm.deserialize("<dark_gray> "));
                pane.setItemMeta(meta);
                betaInv.setItem(i, pane);
            }
        }

        setConfig(config);

        this.plugin = plugin;

        File[] files = betaitems.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                String fileName = file.getName().replace(".yml", "");

                UUID uuid = UUID.fromString(fileName);
                ItemStack item = yaml.getItemStack("itemstack");

                if (item != null) {
                    betaItemsCache.put(uuid, item);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load beta item file: " + file.getName() + "\n" + e.getMessage());
            }
        }

        if (enabled) {
            new CommandAPICommand("betaitems")
                    .withPermission("lipo.betaitems")
                    .executes((sender, args) -> {
                        if (sender instanceof Player p) {

                            long playtime = CMI.getInstance().getPlayerManager().getUser(p).getTotalPlayTime();
                            if (betaornot) {
                                if (playtime >= 8.64e+7) {
                                    Inventory inv = Bukkit.createInventory(null, InventoryType.DROPPER,  mm.deserialize("<dark_gray>Beta Items"));
                                    inv.setContents(betaInv.getContents());
                                    inv.setItem(4, betaItemsCache.getOrDefault(p.getUniqueId(), ItemStack.of(Material.AIR)));
                                    p.openInventory(inv);
                                } else {
                                    plugin.adventure().player(p).sendMessage(mm.deserialize("<red>You are not over 24 hours yet!"));
                                }
                            } else {
                                Inventory inv = Bukkit.createInventory(null, InventoryType.DROPPER,  mm.deserialize("<dark_gray>Beta Items"));
                                inv.setContents(betaInv.getContents());
                                inv.setItem(4, betaItemsCache.getOrDefault(p.getUniqueId(), ItemStack.of(Material.AIR)));
                                p.openInventory(inv);
                            }

                        }
                    })
                    .withSubcommand(new CommandAPICommand("fetch")
                            .withPermission("lipo.betaitems.fetch")
                            .executes((sender, args) -> {
                                if (sender instanceof Player p) {
                                    plugin.adventure().player(p).sendMessage(mm.deserialize("<green>Fetching player playtime and convert it..."));
                                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                       Arrays.stream(Bukkit.getOfflinePlayers()).toList().forEach(op -> {
                                           int convert = 0;
                                           convert = (int) ((CMI.getInstance().getPlayerManager().getUser(op).getTotalPlayTime()) / scalefactor);
                                           saveBeta(op.getUniqueId(), convert, "Beta");
                                       });

                                        Bukkit.getScheduler().runTask(plugin, () -> {
                                            plugin.adventure().player(p).sendMessage(mm.deserialize("<green>All data has been successfully fetched and saved."));
                                        });
                                    });
                                }
                            }))
                    .register();

        }

    }

    public void saveBeta(UUID uuid, int amount, String tag) {
        File folder = new File(plugin.getDataFolder(), "BetaItems");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, uuid.toString() + ".yml");

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("converted-stars", amount);
        config.set("tag", tag);

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save beta item for " + uuid + "\n" + e.getMessage());
        }
    }

    public void setConfig(Configurations config) {
        this.config = config;
        enabled = config.BetaItemsEnabled();
        betaornot = config.BetaOrNotBeta();
        scalefactor = config.StarsScaleFactor();
    }

    public static void setBetaItemsCache(UUID id, ItemStack item) {
        betaItemsCache.put(id, item);
    }

}
