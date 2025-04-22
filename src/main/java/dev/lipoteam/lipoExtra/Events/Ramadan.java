package dev.lipoteam.lipoExtra.Events;

import dev.lipoteam.lipoExtra.Manager.DataManager;
import dev.lipoteam.lipoExtra.Files.RamadanConfig;
import dev.lipoteam.lipoExtra.LipoExtra;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class Ramadan implements Listener {

    private final DataManager dataManager;
    private final LipoExtra plugin;
    private RamadanConfig config;

    private List<PotionEffectType> sahureffects;

    private Boolean enabled;
    private Integer sahuramplifier;
    private Integer sahurduration;
    private String prefix;
    private World world;

    public Ramadan(RamadanConfig config, LipoExtra plugin) {

        this.plugin = plugin;
        dataManager = new DataManager(plugin);
        setConfig(config);

    }

    public void setConfig(RamadanConfig config) {
        enabled = config.enabled();
        sahureffects = config.sahurEffect();
        sahuramplifier = config.sahurAmplifier();
        sahurduration = config.sahurDuration();
        world = config.getWorld();
        prefix = config.prefix();
        this.config = config;
    }

    @EventHandler
    public void PlayerEat(PlayerItemConsumeEvent e) {

        if (!enabled) {
            return;
        }

        var mm = MiniMessage.miniMessage();

        Player player = e.getPlayer();

        if (!((world.getTime() > 13000) && (world.getTime() < 23500))) {
            plugin.adventure().player(player).sendMessage(mm.deserialize(prefix + " <red>You cannot consume anything at this time!"));
            e.setCancelled(true);
        }

        if (world.getTime() > 23000 && world.getTime() < 23500) {
            for (PotionEffectType effect : sahureffects) {
                player.addPotionEffect(effect.createEffect(sahurduration * 20, sahuramplifier));
            }
        }

    }

    @EventHandler
    public void HungerDrop(FoodLevelChangeEvent e) {
        if (e.getEntityType() != EntityType.PLAYER || !enabled) {
            return;
        }

        if (!(world.getTime() > 13000)
                && (world.getTime() < 23500)
                && e.getFoodLevel() <= 7) {
            e.setCancelled(true);
        }

    }

}
