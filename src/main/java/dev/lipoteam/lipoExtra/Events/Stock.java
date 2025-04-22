package dev.lipoteam.lipoExtra.Events;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Worth.WorthItem;
import com.Zrips.CMI.Modules.Worth.WorthManager;
import com.Zrips.CMI.events.CMIPlayerItemsSellEvent;
import com.google.common.util.concurrent.AtomicDouble;
import dev.lipoteam.lipoExtra.Manager.DataManager;
import dev.lipoteam.lipoExtra.Files.StockConfig;
import dev.lipoteam.lipoExtra.LipoExtra;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import us.lynuxcraft.deadsilenceiv.advancedchests.events.PostChestSellEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Stock implements Listener {

    private final LipoExtra plugin;
    private final BukkitScheduler scheduler;
    private final StockConfig config;
    private static final ConcurrentHashMap<CMIMaterial, AtomicInteger> demand = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<CMIMaterial, AtomicDouble> prices = new ConcurrentHashMap<>();
    private BukkitTask task = null;
    private final DataManager dataManager;

    private boolean randomenabled;
    private boolean troubleshoot;
    private static boolean dynamic;
    private int pricechangedelay;
    private static int demandsf;
    private static int notchangeif;
    private static double randomfirst;
    private static double randomlast;
    private static double percentageupdown;
    private String announce;
    private String prefix;
    private static Random ran;
    private static long timemillis;

    public Stock(StockConfig config, LipoExtra plugin) {

        this.plugin = plugin;
        this.config = config;
        WorthManager manager = CMI.getInstance().getWorthManager();
        HashMap<CMIMaterial, List<WorthItem>> listitems = manager.getMap();
        for (CMIMaterial mat : listitems.keySet()) {
            demand.computeIfAbsent(mat, k -> new AtomicInteger(0));

            WorthItem worth = manager.getWorth(mat.newItemStack(1));

            if (worth == null) {
                prices.computeIfAbsent(mat, k -> new AtomicDouble(0));
                continue;
            }

            prices.computeIfAbsent(mat, k -> new AtomicDouble(worth.getSellPrice()));
        }
        scheduler = plugin.getServer().getScheduler();
        dataManager = new DataManager(plugin);
        setConfig(config);

    }

    public void setConfig(StockConfig config) {
        WorthManager manager = CMI.getInstance().getWorthManager();
        HashMap<CMIMaterial, List<WorthItem>> listitems = manager.getMap();
        for (CMIMaterial mat : listitems.keySet()) {

            WorthItem worth = manager.getWorth(mat.newItemStack(1));
            prices.clear();

            if (worth == null) {
                prices.computeIfAbsent(mat, k -> new AtomicDouble(0));
                continue;
            }

            prices.computeIfAbsent(mat, k -> new AtomicDouble(worth.getSellPrice()));
        }

        if (task != null) {
            task.cancel();
        }
        pricechangedelay = config.PriceChangeTime();
        percentageupdown = config.PercentageUpDown();
        randomenabled = config.RandomEnabled();
        randomfirst = config.RandomFirst();
        randomlast = config.RandomLast();
        troubleshoot = config.TroubleShoot();
        announce = config.AnnounceMessage();
        notchangeif = config.NotChangeIfDemand();
        demandsf = config.DemandScaleFactor();
        prefix = config.prefix();
        dynamic = config.Dynamic();
        ran = new Random();
        PriceChangeStart();
    }

    private void PriceChangeStart() {

        var mm = MiniMessage.miniMessage();
        WorthManager manager = CMI.getInstance().getWorthManager();
        timemillis = pricechangedelay * 20L * 60L * 50L + System.currentTimeMillis();

        if (!dynamic) {
            task = scheduler.runTaskTimerAsynchronously(plugin, () -> {

                timemillis = pricechangedelay * 20L * 60L * 50L + System.currentTimeMillis();

                HashMap<CMIMaterial, List<WorthItem>> listitems = manager.getMap();
                for (CMIMaterial mat : listitems.keySet()) {
                    AtomicInteger matdemand = demand.computeIfAbsent(mat, k -> new AtomicInteger(0));
                    updateMaterialPrice(mat, 1);
                }

                demand.forEach((mat, demand) -> demand.set(0));

                Bukkit.getOnlinePlayers().stream()
                        .filter(player -> player.hasPermission("lipo.market.announce"))
                        .forEach(player -> dataManager.sendMessage(player, mm.deserialize(announce.replace("[prefix]", prefix))));

            },pricechangedelay * 20L * 60L, pricechangedelay * 20L * 60L);
        } else {
            task = scheduler.runTaskTimerAsynchronously(plugin, () -> {
                timemillis = pricechangedelay * 20L * 60L * 50L + System.currentTimeMillis();

                demand.forEach((mat, demand) -> demand.set(0));

                if (!troubleshoot) manager.updatePriceInFile();

                Bukkit.getOnlinePlayers().stream()
                        .filter(player -> player.hasPermission("lipo.market.announce"))
                        .forEach(player -> dataManager.sendMessage(player, mm.deserialize(announce.replace("[prefix]", prefix))));

            },(pricechangedelay * 20L * 60L) - (20L * 5), pricechangedelay * 20L * 60L);
        }

    }

    @EventHandler
    private void WorthItem(CMIPlayerItemsSellEvent e) {
        e.getAmounts().forEach((mat, amount) -> {
            demand.computeIfAbsent(mat, k -> new AtomicInteger(0)).addAndGet(amount);
            if (dynamic) {
                updateMaterialPrice(mat, amount);
            }
        });
    }

    @EventHandler
    public void ChestSellEvent(PostChestSellEvent e) {
        e.getSoldItems().forEach(soldItem -> {
            ItemStack item = soldItem.getItem();
            int amount = item.getAmount();
            CMIMaterial mat = CMIMaterial.get(item.getType());
            demand.computeIfAbsent(mat, k -> new AtomicInteger(0)).addAndGet(amount);
            if (dynamic) {
                updateMaterialPrice(mat, amount);
            }
        });
    }

    private void updateMaterialPrice(CMIMaterial mat, int sold) {
        WorthManager manager = CMI.getInstance().getWorthManager();
        WorthItem worth = manager.getWorth(mat.newItemStack(1));

        if (!dynamic) sold = 1;

        if (worth == null) {
            plugin.getLogger().warning("CMI WorthItem is null for: " + mat.name());
            return; // Skip this material to prevent errors
        }

        int demands = demand.computeIfAbsent(mat, k -> new AtomicInteger(0)).get();
        AtomicDouble atomicPrice = prices.computeIfAbsent(mat, k -> new AtomicDouble(worth.getSellPrice()));
        double sellprice = atomicPrice.get();
        double ranprice = randomenabled ? (randomfirst + (ran.nextDouble() * (randomlast - randomfirst))) : 1;
        double demandFactor = Math.max((double) demands / demandsf, 1.0); // Cap at 1.0 (100%)

        double newPrice;
        if (demands > notchangeif) {
            if (demands >= StockConfig.DemandCeil(mat)) {
                double percentageChange = (sellprice * percentageupdown * ranprice * demandFactor * sold);
                newPrice = Math.max(sellprice - percentageChange, 0.01);
                atomicPrice.addAndGet(-percentageChange);
            } else if (demands <= StockConfig.DemandFloor(mat)) {
                double percentageChange = (sellprice * percentageupdown * ranprice * sold);
                newPrice = Math.min(sellprice + percentageChange, config.MaxPrice(mat));
                atomicPrice.addAndGet(percentageChange);
            } else {
                newPrice = sellprice;
            }
        } else {
            newPrice = sellprice;
        }

        // Update the price safely on the main thread
        scheduler.runTask(plugin, () -> worth.setSellPrice(newPrice));

        if (!troubleshoot && !dynamic) {
            manager.updatePriceInFile(worth);
        }
    }

    public static List<CMIMaterial> getTopHighestDemandWithValues(int i) {
        WorthManager manager = CMI.getInstance().getWorthManager();
        HashMap<CMIMaterial, List<WorthItem>> listitems = manager.getMap();

        // Create a list of entries sorted by demand in descending order
        List<Map.Entry<CMIMaterial, AtomicInteger>> sortedDemand = demand.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().get(), a.getValue().get())) // Descending order
                .limit(i) // Get the top `i` highest
                .toList(); // Collect as list

        return sortedDemand.stream().map(Map.Entry::getKey).toList();
    }

    public static double getApproximateValueIncrease(String mat, Inventory inv) {

        int totalitem = 1;
        if (dynamic) {
            for (ItemStack item : inv.getContents()) {
                if (item != null) {
                    if (item.getType().name().equals(mat)) {
                        totalitem += item.getAmount();
                    }
                }
            }
        }

        WorthManager manager = CMI.getInstance().getWorthManager();
        CMIMaterial material;
        if (mat.equalsIgnoreCase("nothing")) return 0;

        try {
            material = CMIMaterial.valueOf(mat);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("No material name: " + mat);
            return -1;
        }

        int idemand = demand.get(material).get();
        if (idemand < notchangeif) return 0;

        WorthItem worth = manager.getWorth(material.newItemStack(1));
        double demandFactor = Math.max((double) idemand / demandsf, 1.0); // Cap at 1.0 (100%)// Limit per transaction impact to 10%
        if (worth == null) return -1;

        double sellprice = prices.computeIfAbsent(material, k -> new AtomicDouble(worth.getSellPrice())).get();

        if (idemand >= StockConfig.DemandCeil(material)) {
            return (sellprice * percentageupdown * ran.nextDouble(randomfirst, randomlast) * demandFactor * totalitem);
        } else if (idemand <= StockConfig.DemandFloor(material)) {
            return (sellprice * percentageupdown * ran.nextDouble(randomfirst, randomlast) * totalitem);
        } else {
            return 0;
        }

    }

    public static int getDemand(String mat) {
        CMIMaterial material;
        if (mat.equalsIgnoreCase("nothing")) {
            return 0;
        }
        try {
           material = CMIMaterial.valueOf(mat);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("No material name: " + mat);
            return -1;
        }

        if (demand.containsKey(material)) {
            return demand.get(material).get();
        }

        return 0;

    }

    public static String TimeBeforeStockChange() {
        if (timemillis > 0) {

            long count = timemillis - System.currentTimeMillis();

            long hours = TimeUnit.MILLISECONDS.toHours(count);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(count) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(count) % 60;

            return String.format("%02dh %02dm %02ds", hours ,minutes, seconds);
        }
        return "0h";
    }

}
