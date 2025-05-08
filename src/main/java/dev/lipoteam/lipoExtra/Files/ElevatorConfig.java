package dev.lipoteam.lipoExtra.Files;

import dev.lipoteam.lipoExtra.LipoExtra;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.intellij.lang.annotations.Subst;

import java.util.List;
import java.util.stream.Collectors;

public class ElevatorConfig {

    private final LipoExtra plugin;
    private final FileConfiguration config;

    public ElevatorConfig(FileConfiguration config, LipoExtra plugin) {

        this.plugin = plugin;
        this.config = config;

    }

    public boolean enabled() { return config.getBoolean("enabled"); }

    public String prefix() { return config.getString("prefix"); }

    public String title() { return config.getString("lang.title"); }

    public String subtitle() { return config.getString("lang.subtitle"); }

    public String actionbar() { return config.getString("lang.actionbar"); }

    public String bounded() { return config.getString("lang.bounded"); }

    public String notsafe() { return config.getString("lang.not-safe"); }

    public String occupied() { return config.getString("lang.occupied"); }

    public String unbound() { return config.getString("lang.unbound"); }

    public String claim() { return config.getString("lang.claim"); }

    public int mingap() { return config.getInt("min-gap"); }

    public int maxgap() { return config.getInt("max-gap"); }

    public Sound Elevated() {
        @Subst("") String string = config.getString("sound.elevated.sound");
        if (string != null) {
            return Sound.sound(Key.key(string), Sound.Source.valueOf(config.getString("sound.elevated.source")),
                    (float) config.getDouble("sound.elevated.radius"), (float) config.getDouble("sound.elevated.pitch"));
        }
        return null;
    }

    public ItemStack item() {
        var mm = MiniMessage.miniMessage();
        boolean glint = config.getBoolean("item.glint");
        String material = config.getString("item.material");
        String name = config.getString("item.name");
        ItemStack item = ItemStack.of(Material.valueOf(material));
        ItemMeta meta = item.getItemMeta();
        meta.setEnchantmentGlintOverride(glint);
        if (name != null) {
            meta.displayName(mm.deserialize(name));
        }
        item.setItemMeta(meta);
        return item;
    }

    public List<String> itemlore() { return config.getStringList("item.lore"); }

    public String itemname() { return config.getString("item.name"); }

}
