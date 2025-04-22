package dev.lipoteam.lipoExtra.Files;

import dev.lipoteam.lipoExtra.LipoExtra;
import dev.lipoteam.lipoExtra.Manager.LeaderboardManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardConfig {

    private LipoExtra plugin;
    private final FileConfiguration config;
    private final List<LeaderboardManager> list = new ArrayList<>();

    public LeaderboardConfig(FileConfiguration config, LipoExtra plugin) {
        this.plugin = plugin;
        this.config = config;
        createListGui();
    }

    public boolean Enabled() { return config.getBoolean("enabled"); }

//guis:
//  money:
//  title: "Money Top %page%/%max_page%"
//  placeholder: %cmi_user_balance%
//  # in minutes
//  item:
//      material: player_head
//      name: "<yellow>%player_name%"
//      lore:
//        - "money: %cmi_user_balance%"


    public void createListGui() {
        for (String gui : config.getConfigurationSection("guis").getKeys(false)) {
            String type = config.getString("guis." + gui + ".type");
            String title = config.getString("guis." + gui + ".title");
            String currency = config.getString("guis." + gui + ".currency");
            String itemmaterial = config.getString("guis." + gui + ".item.material");
            String itemname = config.getString("guis." + gui + ".item.name");
            String command = config.getString("guis." + gui + ".command");
            List<String> itemlore = config.getStringList("guis." + gui + ".item.lore");
            list.add(new LeaderboardManager(type, title, currency, itemmaterial, itemname, itemlore, command));
        }
    }

    public List<LeaderboardManager> getListGui() {
        return list;
    }

}
