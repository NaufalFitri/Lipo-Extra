package dev.lipoteam.lipoHud.Events;

import dev.lipoteam.lipoHud.DataManager;
import dev.lipoteam.lipoHud.Files.RamadanConfig;
import dev.lipoteam.lipoHud.LipoHud;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class Ramadan implements Listener {

    private final DataManager dataManager;
    private final Plugin plugin;
    private RamadanConfig config;

    private List<PotionEffectType> sahureffects;

    private Boolean enabled;
    private Integer sahuramplifier;
    private Integer sahurduration;

    public Ramadan(RamadanConfig config, LipoHud plugin) {

        this.plugin = plugin;
        dataManager = new DataManager(plugin);
        setConfig(config);

    }

    public void setConfig(RamadanConfig config) {
        enabled = config.enabled();
        sahureffects = config.sahurEffect();
        sahuramplifier = config.sahurAmplifier();
        sahurduration = config.sahurDuration();
        this.config = config;
    }

    @EventHandler
    public void PlayerEat(PlayerItemConsumeEvent e) {

        if (!enabled) {
            return;
        }

        Player player = e.getPlayer();

        if (!((Bukkit.getServer().getWorld("world").getTime() > 13000) && (Bukkit.getServer().getWorld("world").getTime() < 23500))) {
            player.sendMessage(config.prefix("&cYou cannot consume anything at this time!"));
            e.setCancelled(true);
        }

        if (Bukkit.getServer().getWorld("world").getTime() > 23000 && Bukkit.getServer().getWorld("world").getTime() < 23500) {
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

        if (!((Bukkit.getServer().getWorld("world").getTime() > 13000)
                && (Bukkit.getServer().getWorld("world").getTime() < 23500))
                && e.getFoodLevel() <= 7) {
            e.setCancelled(true);
        }

    }

}
