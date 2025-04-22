package dev.lipoteam.lipoExtra;

import dev.lipoteam.lipoExtra.Events.Jobs;
import dev.lipoteam.lipoExtra.Events.LevelBound;
import dev.lipoteam.lipoExtra.Events.Pinata;
import dev.lipoteam.lipoExtra.Events.Stock;
import dev.lipoteam.lipoExtra.Files.Configurations;
import dev.lipoteam.lipoExtra.Files.JobsConfig;
import dev.lipoteam.lipoExtra.Files.StockConfig;
import dev.lipoteam.lipoExtra.Files.TagConfig;
import dev.lipoteam.lipoExtra.Manager.DataManager;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.Zrips.CMILib.Items.CMIMaterial;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static dev.lipoteam.lipoExtra.Events.Event.playerClient;

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

    private static String progressBar;
    private static int progressLength;
    private static String progressChar;
    private static LipoExtra plugin;

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
                    if (!player.hasPermission("lipo.market.demand")) return "***";
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
                    if (!player.hasPermission("lipo.market.demand")) return "***";
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
                    if (!player.hasPermission("lipo.market.approx")) return "***";
                } else {
                    return "-1";
                }

                String[] item = params.split("_");
                if (item.length > 2 && !item[2].isEmpty()) { // Ensure item[2] exists and is not empty
                    try {
                        return String.format("%.6f", Stock.getApproximateValueIncrease(item[2].replace(" ", "_").toUpperCase(), player.getInventory()));
                    } catch (Exception e) {
                        return "Invalid item"; // Prevent crashes if item[2] is not valid
                    }
                }
                return "0.0";
            } else if (params.equalsIgnoreCase("market_reset")) {
                return Stock.TimeBeforeStockChange();
            } else if (params.contains("level")) {
                if (params.startsWith("level_")) {
                    String trimmed = params.substring(6);
                    return trimmed.isEmpty() ? null : String.valueOf(LevelBound.getLevel(trimmed));
                } else {
                    return null;
                }
            } else if (params.contains("progress")) {
                String[] splits = params.split("_");
                if (splits.length > 2) {
                    return generateProgressBar(splits[1], Integer.parseInt(splits[2]));
                }
                return "0";
            } else if (params.equalsIgnoreCase("flyparticle")) {
                if (player != null && player.isOnline()) {
                    if (dataManager.hasData(player, "flyparticle")) {
                        return (String) dataManager.getdata(player, "flyparticle", false);
                    }
                }
                return "null";

            } else if (params.equalsIgnoreCase("tag")) {
                TagConfig tagConfig = LipoExtra.getInstance().getTagConfig();
                if (player != null) {
                    if (dataManager.hasData(player, "tag")) {
                        String ptag = (String) dataManager.getdata(player, "tag", false);
                        if (player.hasPermission(tagConfig.getPerms(ptag))) {
                            return tagConfig.getTagname(ptag);
                        } else {
                            return "";
                        }
                    } else {
                        return "";
                    }
                } else {
                    return "";
                }
            } else if (params.equalsIgnoreCase("voteparty")) {
                return String.valueOf(Pinata.voteCurrent);
            }

        }

        return null;
    }

    public static String generateProgressBar(String time, int totalSeconds) {

        String[] parts = time.split(":");
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);
        int elapsedSeconds = (minutes * 60) + seconds;

        // Calculate progress percentage
        double percentage = (double) elapsedSeconds / totalSeconds;

        // Calculate number of filled and empty bars
        int filledBars = (int) Math.round(percentage * progressLength);
        int emptyBars = progressLength - filledBars;

        return progressBar.replace("%progress%", progressChar.repeat(emptyBars)).replace("progress-done", progressChar.repeat(filledBars));
    }

    private static DataManager dataManager;

    public static void registerHook(Configurations config, LipoExtra plugin) {
        dataManager = new DataManager(LipoExtra.getInstance());
        new PlaceholderAPIHook().register();
    }

    public static void setConfig(Configurations config) {
        progressBar = config.ProgressBar();
        progressLength = config.ProgressLength();
        progressChar = config.ProgressChar();
    }

    @Override
    public boolean persist() {
        return true;
    }
}
