package dev.lipoteam.lipoHud.Events;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Worth.WorthItem;
import com.Zrips.CMI.Modules.Worth.WorthManager;
import com.Zrips.CMI.events.CMIPlayerItemsSellEvent;
import com.Zrips.CMI.events.CMIUserBalanceChangeEvent;
import dev.lipoteam.lipoHud.DataManager;
import dev.lipoteam.lipoHud.Files.StockConfig;
import dev.lipoteam.lipoHud.LipoHud;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Stock implements Listener {

    private LipoHud plugin;
    private BukkitScheduler scheduler;
    private StockConfig config;
    private static final ConcurrentHashMap<CMIMaterial, AtomicInteger> demand = new ConcurrentHashMap<>();
    private BukkitTask task = null;
    private final DataManager dataManager;

    private boolean randomenabled;
    private boolean troubleshoot;
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

    public Stock(StockConfig config, LipoHud plugin) {

        this.plugin = plugin;
        this.config = config;
        scheduler = plugin.getServer().getScheduler();
        dataManager = new DataManager(plugin);
        setConfig(config);
    }

    public void setConfig(StockConfig config) {
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
        ran = new Random();
        PriceChangeStart();
    }

    private void PriceChangeStart() {

        var mm = MiniMessage.miniMessage();
        WorthManager manager = CMI.getInstance().getWorthManager();
        timemillis = pricechangedelay * 20L * 60L * 50L + System.currentTimeMillis();

        task = scheduler.runTaskTimerAsynchronously(plugin, () -> {

            timemillis = pricechangedelay * 20L * 60L * 50L + System.currentTimeMillis();
            double ranprice = randomenabled ? (randomfirst + (ran.nextDouble() * (randomlast - randomfirst))) : 1;

            HashMap<CMIMaterial, List<WorthItem>> listitems = manager.getMap();
            for (CMIMaterial mat : listitems.keySet()) {
                AtomicInteger matdemand = demand.computeIfAbsent(mat, k -> new AtomicInteger(0));
                WorthItem worth = manager.getWorth(mat.newItemStack(1));

                if (worth == null) {
                    plugin.getLogger().warning("CMI WorthItem is null for: " + mat.name());
                    continue; // Skip this material to prevent errors
                }

                double sellprice = worth.getSellPrice();
                double percentageChange = sellprice * percentageupdown * ranprice;
                matdemand.set(0);

                double newPrice;

                if (matdemand.get() < notchangeif) {
                    if (matdemand.get() >= StockConfig.DemandCeil(mat)) {
                        newPrice = Math.max(sellprice - (percentageChange + ((double) matdemand.get() / demandsf)), 0.01);
                    } else if (matdemand.get() <= StockConfig.DemandFloor(mat)) {
                        if ((sellprice + percentageChange) < config.MaxPrice(mat)) {
                            if (sellprice + (percentageChange + ((double) matdemand.get() / demandsf)) >= config.MaxPrice(mat)) {
                                newPrice = config.MaxPrice(mat);
                            } else {
                                newPrice = sellprice;
                            }
                        } else {
                            newPrice = sellprice;
                        }
                    } else {
                        newPrice = sellprice;
                    }
                } else {
                    newPrice = sellprice;
                }

                scheduler.runTask(plugin, () -> worth.setSellPrice(newPrice));

                if (!troubleshoot) {
                    manager.updatePriceInFile(worth);
                }

            }

            Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.hasPermission("lipo.market.announce"))
                    .forEach(player -> dataManager.sendMessage(player, mm.deserialize(announce.replace("[prefix]", prefix))));

        },pricechangedelay * 20L * 60L, pricechangedelay * 20L * 60L);
    }

    @EventHandler
    private void WorthItem(CMIPlayerItemsSellEvent e) {
        e.getAmounts().forEach((mat, amount) ->
                demand.computeIfAbsent(mat, k -> new AtomicInteger(0)).addAndGet(amount)
        );
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

    public static double getApproximateValueIncrease(String mat) {
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
        if (worth == null) return -1;
        double sellprice = worth.getSellPrice();
        return ((double) idemand / demandsf) + (sellprice * percentageupdown * ran.nextDouble(randomfirst, randomlast));
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
