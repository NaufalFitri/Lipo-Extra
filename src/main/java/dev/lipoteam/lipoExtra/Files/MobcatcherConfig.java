package dev.lipoteam.lipoExtra.Files;

import dev.lipoteam.lipoExtra.LipoExtra;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MobcatcherConfig {

    private final FileConfiguration config;
    private final LipoExtra plugin;

    public MobcatcherConfig(FileConfiguration config, LipoExtra plugin) {

        this.plugin = plugin;
        this.config = config;

    }

    public boolean enabled() { return config.getBoolean("enabled"); }

    public String prefix() { return config.getString("prefix"); }

    public String mobcatch() { return config.getString( "lang.catch"); }

    public String mobfail() { return config.getString("lang.fail"); }

    public String itemname() { return config.getString("item.name"); }

    public List<String> itemlore() { return config.getStringList("item.lore"); }

    public double chances() { return config.getDouble("chances"); }

    public int resetuncatch() { return config.getInt("reset-mob-uncatchable"); }

    public String claim() { return config.getString("lang.claim"); }

    public ItemStack item() {
        var mm = MiniMessage.miniMessage();
        String material = config.getString("item.material");
        String name = itemname();
        ItemStack item = ItemStack.of(Material.valueOf(material));
        ItemMeta meta = item.getItemMeta();
        if (name != null) {
            meta.displayName(mm.deserialize(name));
        }
        item.setItemMeta(meta);
        return item;
    }

}
