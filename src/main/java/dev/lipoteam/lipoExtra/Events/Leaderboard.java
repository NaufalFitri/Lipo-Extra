package dev.lipoteam.lipoExtra.Events;

import com.Zrips.CMI.CMI;
import dev.lipoteam.lipoExtra.Files.LeaderboardConfig;
import dev.lipoteam.lipoExtra.LipoExtra;
import dev.lipoteam.lipoExtra.Manager.LeaderboardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

import java.util.*;
import java.util.stream.Collectors;

public class Leaderboard implements Listener {

    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private List<LeaderboardManager> guis;

    private LeaderboardConfig config;
    private LipoExtra plugin;

    public Leaderboard(LeaderboardConfig config, LipoExtra plugin) {
        this.plugin = plugin;
        setConfig(config);
    }

    public void setConfig(LeaderboardConfig config) {
        this.config = config;
        this.guis = config.getListGui();
    }

    public void openGUI(Player player, int page, LeaderboardManager boardgui) {
        var mm = MiniMessage.miniMessage();
        Inventory gui = Bukkit.createInventory(null, 54, mm.deserialize(boardgui.title().replace("[page]", String.valueOf(page))));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<OfflinePlayer> sortedPlayers = Arrays.stream(Bukkit.getOfflinePlayers())
                    .sorted(Comparator.comparingDouble(p -> getAmount((OfflinePlayer) p, boardgui.type(), boardgui.currency())).reversed()) // Sort players by PlaceholderAPI balance
                    .toList();

            int startIndex = (page - 1) * 45;
            int endIndex = Math.min(startIndex + 45, sortedPlayers.size());

            List<ItemStack> skullItems = new ArrayList<>();
            for (int i = startIndex; i < endIndex; i++) {
                OfflinePlayer target = sortedPlayers.get(i);
                skullItems.add(createPlayerHead(target, getAmount(target, boardgui.type(), boardgui.currency()) ,boardgui, i+1));
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                for (int i = 0; i < skullItems.size(); i++) {
                    gui.setItem(i, skullItems.get(i));
                }
                ItemStack pane = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE, 1);
                ItemMeta meta = pane.getItemMeta();
                meta.displayName(mm.deserialize("<dark_gray> "));
                pane.setItemMeta(meta);
                for (int i = 45; i < 54; i ++) {
                    gui.setItem(i, pane);
                }

                gui.setItem(49, createPlayerHead(player, getAmount(player, boardgui.type(), boardgui.currency()), boardgui, sortedPlayers.indexOf(player) + 1));

                if (page > 1) gui.setItem(45, createItem(Material.ARROW, "§fPrevious"));
                if (endIndex < sortedPlayers.size()) gui.setItem(53, createItem(Material.ARROW, "§fNext"));

                playerPages.put(player.getUniqueId(), page);
                player.openInventory(gui);
            });
        });
    }

    private double getAmount(OfflinePlayer player, String type, String currency) {
        double value = 0;
        switch (type) {
            case "cmi" -> {
                switch (currency) {
                    case "money" -> value = CMI.getInstance().getPlayerManager().getUser(player.getUniqueId()).getBalance();
                    case "playtime" -> value = (double) CMI.getInstance().getPlayerManager().getUser(player.getUniqueId()).getTotalPlayTime() / 3600000;
                }
            }
            case "coinsengine" -> {
                Currency currency1 = CoinsEngineAPI.getCurrency(currency);
                if (currency1 != null) {
                    value = CoinsEngineAPI.getBalance(player.getUniqueId(), currency1);
                }
            }
        }

        value = Math.round(value * 100.0) / 100.0;

        return value;
    }

    private ItemStack createPlayerHead(OfflinePlayer player, double balance, LeaderboardManager gui, int num) {
        var mm = MiniMessage.miniMessage();
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        String name = player.getName() != null ? player.getName() : "Unknown";
        meta.setOwningPlayer(player);
        meta.displayName(mm.deserialize(gui.itemname().replace("[player]", name).replace("[balance]", String.valueOf(balance)).replace("[number]", String.valueOf(num))));
        List<String> lore = gui.itemlore();

        List<Component> formattedLore = lore.stream()
                .map(line -> mm.deserialize(line.replace("[balance]", String.valueOf(balance)).replace("[player]", name).replace("[number]", String.valueOf(num))))
                .collect(Collectors.toList());

        meta.lore(formattedLore);
        skull.setItemMeta(meta);
        return skull;
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        var mm = MiniMessage.miniMessage();
        if (!(event.getWhoClicked() instanceof Player player)) return;
        for (LeaderboardManager gui : guis) {
            int currentPage = playerPages.getOrDefault(player.getUniqueId(), 1);

            if (mm.deserialize(gui.title().replace("[page]", String.valueOf(currentPage))).equals(event.getView().title())) {
                event.setCancelled(true);
                ItemStack clicked = event.getCurrentItem();
                if (clicked == null || clicked.getType() == Material.AIR) return;

                if (clicked.getType() == Material.ARROW) {
                    if (clicked.getItemMeta().displayName().toString().contains("Previous")) {
                        openGUI(player, currentPage - 1, gui);
                    } else if (clicked.getItemMeta().displayName().toString().contains("Next")) {
                        openGUI(player, currentPage + 1, gui);
                    }
                }
            }
        }
    }

}
