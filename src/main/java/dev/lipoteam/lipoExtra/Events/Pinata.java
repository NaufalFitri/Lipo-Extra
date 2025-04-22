package dev.lipoteam.lipoExtra.Events;

import com.Zrips.CMI.CMI;
import dev.lipoteam.lipoExtra.Files.PinataConfig;
import dev.lipoteam.lipoExtra.LipoExtra;
import dev.lipoteam.lipoExtra.Manager.PinataRewards;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.*;

public class Pinata implements Listener {

    private final PinataConfig config;
    private final LipoExtra plugin;

    private final Random random;
    private List<PinataRewards> pinataRewards;
    private List<PinataRewards> weightedRewards;
    private List<String> startcmds;
    public static int voteCurrent;
    private int votethreshold;

    public Pinata(PinataConfig config, LipoExtra plugin) {

        this.plugin = plugin;
        this.config = config;
        random = new Random();
        setConfig(config);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            if (voteCurrent >= votethreshold) {
                for (String cmd : startcmds) {
                    Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), cmd);
                }
                voteCurrent = 0;
            }

            config.setCurrentVote(voteCurrent);

        }, 100L, 100L);

    }

    public void setConfig(PinataConfig config) {
        hitparticle = config.WhitParticle();
        hitparticlespeed = config.WhitParticleSpeed();
        hitparticlecount = config.WhitParticleCount();
        pinataRewards = config.Rewards();
        votethreshold = config.voteThreshold();
        voteCurrent = config.voteCurrent();
        startcmds = config.startCommands();
    }

    private String hitparticle;
    private double hitparticlespeed;
    private int hitparticlecount;

    @EventHandler(ignoreCancelled = true)
    private void PlayerHitPinata(EntityDamageByEntityEvent e) {
        Entity ent = e.getEntity();
        if (!ent.hasMetadata("pinata")) return;

        Entity dmg = e.getDamager();
        if (dmg instanceof Player p) {
            if (Objects.equals(ent.getMetadata("pinata").getFirst().value(), "whack")) {
                e.setCancelled(true);
                ent.remove();
                GiveReward(p);
                ent.getWorld().spawnParticle(Particle.valueOf(hitparticle), ent.getLocation().add(0, 0.5, 0), hitparticlecount, 0, 0, 0, hitparticlespeed, null);
            }

        }

    }

    private void GiveReward(Player p) {
        List<PinataRewards> weightedRewards = new ArrayList<>();
        for (PinataRewards reward : pinataRewards) {
            for (int i = 0; i < reward.getChance(); i++) {
                weightedRewards.add(reward);
            }
        }

        if (!weightedRewards.isEmpty()) {
            PinataRewards selectedReward = weightedRewards.get(random.nextInt(weightedRewards.size()));
            plugin.adventure().player(p).playSound(selectedReward.getSound());
            selectedReward.getCommands().forEach(command -> {
                Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), command.replace("[player]", p.getName()));
            });
        }
    }

}
