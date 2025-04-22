package dev.lipoteam.lipoHud;

import dev.lipoteam.lipoHud.Events.Jobs;
import dev.lipoteam.lipoHud.Events.Stock;
import dev.lipoteam.lipoHud.Files.JobsConfig;
import dev.lipoteam.lipoHud.Files.StockConfig;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.Zrips.CMILib.Items.CMIMaterial;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static dev.lipoteam.lipoHud.Events.Event.playerClient;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "lipo";
    }

    @Override
    public @NotNull String getAuthor() {
        return "lipo";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offplayer, @NotNull String params) {

        if (offplayer != null && offplayer.isOnline()) {
            Player player = offplayer.getPlayer();

            if (params.equalsIgnoreCase("name")) {
                return player.getName();
            } else if (params.equalsIgnoreCase("client")) {
                return playerClient.get(player.getUniqueId());
            } else if (params.contains("earnings")) {
                String[] splits = params.split("_");
                return String.format("%.2f", Jobs.currentEarning(player, splits[0]));
            } else if (params.contains("limit")) {
                String[] splits = params.split("_");
                return String.format("%.2f", JobsConfig.EarningLimit(splits[0]));
            }

            else if (params.contains("endtime")) {
                String[] splits = params.split("_");
                return Jobs.TimeBeforeStartEarning(player, splits[0]);
            } else if (params.contains("demand_floor")) {

                if (player != null && player.isOnline()) {
                    if (!player.hasPermission("lipo.market.demand")) return "Restricted";
                } else {
                    return "-1";
                }

                int index = params.indexOf("demand_floor");
                String item = params.substring(0, index - 1).toUpperCase().replace(" ", "_");
                CMIMaterial material;
                try {
                    material = CMIMaterial.valueOf(item);
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("No material name: " + item);
                    return "-1";
                }

                return String.valueOf(StockConfig.DemandFloor(material));
            } else if (params.contains("demand_ceil")) {

                if (player != null && player.isOnline()) {
                    if (!player.hasPermission("lipo.market.demand")) return "Restricted";
                } else {
                    return "-1";
                }

                int index = params.indexOf("demand_ceil");
                String item = params.substring(0, index - 1).toUpperCase().replace(" ", "_");
                CMIMaterial material;
                try {
                    material = CMIMaterial.valueOf(item);
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("No material name: " + item);
                    return "-1";
                }
                return String.valueOf(StockConfig.DemandCeil(material));
            } else if (params.contains("demand")) {
                int index = params.indexOf("demand");
                String item = params.substring(0, index - 1).toUpperCase().replace(" ", "_");
                return String.valueOf(Stock.getDemand(item));
            } else if (params.contains("market_highest")) {
                String[] numbers = params.split("_");
                if (numbers.length > 3 && numbers[2].matches("\\d+")) {

                    List<CMIMaterial> tops = Stock.getTopHighestDemandWithValues(20);
                    int number = Integer.parseInt(numbers[2]);

                    if (number >= 0 && number < tops.size()) {
                        if (Objects.equals(numbers[3], "name")) {
                            return tops.get(number).getName();
                        }
                    }
                }

                return "Nothing";

            } else if (params.contains("market_approx")) {

                if (player != null && player.isOnline()) {
                    if (!player.hasPermission("lipo.market.approx")) return "Restricted";
                } else {
                    return "-1";
                }

                String[] item = params.split("_");
                if (item.length > 2 && !item[2].isEmpty()) { // Ensure item[2] exists and is not empty
                    try {
                        return String.format("%.4f", Stock.getApproximateValueIncrease(item[2].replace(" ", "_").toUpperCase()));
                    } catch (Exception e) {
                        return "Invalid item"; // Prevent crashes if item[2] is not valid
                    }
                }
                return "0.0";
            } else if (params.equalsIgnoreCase("market_reset")) {
                return Stock.TimeBeforeStockChange();
            }

        }

        return null;
    }

    public static void registerHook() {
        new PlaceholderAPIHook().register();
    }

}
