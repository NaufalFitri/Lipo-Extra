package dev.lipoteam.lipoExtra.Files;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Worth.WorthItem;
import com.Zrips.CMI.Modules.Worth.WorthManager;
import com.google.common.util.concurrent.AtomicDouble;
import dev.lipoteam.lipoExtra.LipoExtra;
import net.Zrips.CMILib.Items.CMIMaterial;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StockConfig {

    private final FileConfiguration config;
    private LipoExtra plugin;
    double defaultmax = 0.600;
    static int defaultceil = 1300;
    static int defaultfloor = 900;

    private final ConcurrentHashMap<CMIMaterial, AtomicDouble> marketbaseprice = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<CMIMaterial, AtomicDouble> marketminmax = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<CMIMaterial, AtomicInteger> marketfloor = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<CMIMaterial, AtomicInteger> marketceil = new ConcurrentHashMap<>();

    public StockConfig(FileConfiguration config, LipoExtra plugin) {

        this.config = config;
        this.plugin = plugin;
        WorthManager manager = CMI.getInstance().getWorthManager();
        HashMap<CMIMaterial, List<WorthItem>> listitems = manager.getMap();

        for (CMIMaterial item : listitems.keySet()) {
            String path = "market-sell." + item.getName().replace(" ", "_");
            String base_price = path + ".base-price";
            String minmax = path + ".minmax-price";
            String demandfloor = path + ".demand-floor";
            String demandceil = path + ".demand-ceil";

            double sellprice = listitems.get(item).getFirst().getSellPrice();
            double maxvalue = defaultmax;
            int floordemand = defaultfloor;
            int ceildemand = defaultceil;


            if (!config.contains(path)) {
                config.set(base_price, sellprice);
                config.set(minmax, defaultmax);
                config.set(demandfloor, defaultfloor);
                config.set(demandceil, defaultceil);
            } else {

                if (!config.contains(base_price)) config.set(base_price, sellprice);
                if (!config.contains(minmax)) config.set(minmax, defaultmax);
                if (!config.contains(demandfloor)) config.set(demandfloor, defaultfloor);
                if (!config.contains(demandceil)) config.set(demandceil, defaultceil);

                sellprice = config.getDouble(base_price);

                if (TroubleShoot()) {
                    WorthItem worth = manager.getWorth(item.newItemStack(1));
                    if (worth != null) {
                        worth.setSellPrice(sellprice);
                        manager.updatePriceInFile(worth);
                    }
                }

                floordemand = config.getInt(demandfloor);
                ceildemand = config.getInt(demandceil);
                maxvalue = config.getDouble(minmax);
            }

            marketfloor.put(item, new AtomicInteger(floordemand));
            marketceil.put(item, new AtomicInteger(ceildemand));
            marketbaseprice.put(item, new AtomicDouble(sellprice));
            marketminmax.put(item, new AtomicDouble(maxvalue));

        }

        try {
            config.save(Paths.get(plugin.getDataFolder().toString(), "Extras", "stockmarket.yml").toFile());
        } catch (IOException e) {
            plugin.getLogger().warning(String.valueOf(e));
        }

    }

    public String prefix() {
        return config.getString("prefix");
    }

    public double getBasePrice(CMIMaterial mat) {
        return marketbaseprice.computeIfAbsent(mat, k -> new AtomicDouble(5.0)).get();
    }

    public int NotChangeIfDemand() {
        return config.getInt("not-change-if-under");
    }

    public int DemandScaleFactor() {
        return config.getInt("demand-scale-factor");
    }

    public String AnnounceMessage() {
        return config.getString("lang.announce");
    }

    public double MaxPrice(CMIMaterial mat) {
        AtomicDouble maxpercent = marketminmax.computeIfAbsent(mat, k -> new AtomicDouble(defaultmax));
        AtomicDouble baseprice = marketbaseprice.computeIfAbsent(mat, k -> new AtomicDouble(10.0));
        double percent = maxpercent.get();
        double base = baseprice.get();

        return base + (base + percent);
    }

    public static int DemandFloor(CMIMaterial mat) {
        return marketfloor.computeIfAbsent(mat, k -> new AtomicInteger(defaultfloor)).get();
    }

    public static  int DemandCeil(CMIMaterial mat) {
        return marketceil.computeIfAbsent(mat, k -> new AtomicInteger(defaultceil)).get();
    }

    public double PercentageUpDown() {
        return config.getDouble("percentage-up-down");
    }

    public int PriceChangeTime() {
        return config.getInt("price-change-every");
    }

    public int DemandThreshold() {
        return config.getInt("demand-threshold");
    }

    public boolean RandomEnabled() {
        return config.getBoolean("random-bound.enabled");
    }

    public double RandomFirst() {
        return config.getDouble("random-bound.first");
    }

    public double RandomLast() {
        return config.getDouble("random-bound.last");
    }

    public boolean TroubleShoot() {
        return config.getBoolean("troubleshoot");
    }

    public boolean Dynamic() { return config.getBoolean("dynamic"); }

    public double MaxSellFactor() { return config.getDouble("sell-factor-max"); }

    public int SoldFactor() { return config.getInt("sold-scale-factor"); }

}
