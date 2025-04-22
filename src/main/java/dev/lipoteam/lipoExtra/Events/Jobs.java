package dev.lipoteam.lipoHud.Events;

import com.gamingmesh.jobs.api.JobsPrePaymentEvent;
import com.gamingmesh.jobs.container.Job;
import com.google.common.util.concurrent.AtomicDouble;
import dev.lipoteam.lipoHud.DataManager;
import dev.lipoteam.lipoHud.Files.JobsConfig;
import dev.lipoteam.lipoHud.LipoHud;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Jobs implements Listener {

    private final LipoHud plugin;
    private final DataManager dataManager;
    private JobsConfig config;
    private boolean enabled;
    private static final ConcurrentHashMap<Player, ConcurrentHashMap<Job, AtomicDouble>> earnings = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Player, ConcurrentHashMap<Job, Long>> earningtimes = new ConcurrentHashMap<>();
    private final List<Player> delayPlayer = new ArrayList<>();
    private final List<Player> LimitDelay = new ArrayList<>();

    private String overlimitmsg;
    private String prefix;
    private String cantearnmsg;
    private String allcantearnmsg;
    private int resettime;
    private final BukkitScheduler scheduler;
    private boolean allcannotearn;
    private final String restarttime;

    public Jobs(JobsConfig config, LipoHud plugin) {

        this.plugin = plugin;
        dataManager = new DataManager(plugin);
        scheduler = Bukkit.getScheduler();
        setConfig(config);

        Calendar restart = Calendar.getInstance();
        restart.add(Calendar.MINUTE, resettime);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE HH:mm:ss");
        restarttime = sdf.format(restart.getTime());

        allcannotearn = config.CantEarnAfterRestart();

        scheduler.runTaskLaterAsynchronously(plugin, () -> {
            allcannotearn = false;
        }, resettime * 20L * 60L);

    }

    public void setConfig(JobsConfig config) {
        this.config = config;
        enabled = config.enabled();
        overlimitmsg = config.OverLimitMessage();
        prefix = config.prefix();
        resettime = config.ResetTime();
        cantearnmsg = config.CantEarnMessage();
        allcantearnmsg = config.CantEarnRestartMsg();
    }

    @EventHandler
    private void PreventEarn(JobsPrePaymentEvent e) {
        if (!enabled) return;
        Player p = e.getPlayer().getPlayer();

        if (p != null) {

            if (allcannotearn) {
                if (!delayPlayer.contains(p)) {
                    dataManager.sendMessage(p, ReformatText(allcantearnmsg, e.getJob().getName(), restarttime));
                    delayPlayer.add(p);
                    scheduler.runTaskLater(plugin, () -> delayPlayer.remove(p), 60L);
                }
                e.setCancelled(true);
                return;
            }

            ConcurrentHashMap<Job, AtomicDouble> earningjobs = earnings.computeIfAbsent(p, k -> new ConcurrentHashMap<>());
            ConcurrentHashMap<Job, Long> earningtime = earningtimes.computeIfAbsent(p, k -> new ConcurrentHashMap<>());

            AtomicDouble earning = earningjobs.computeIfAbsent(e.getJob(), k -> new AtomicDouble(0.0));
            double income = e.getAmount();
            if (earning.get() + income > config.EarnLimit(e.getJob())) {
                scheduler.runTaskAsynchronously(plugin, () -> {
                    long now = System.currentTimeMillis();
                    long resetTimeMillis = now + (resettime * 60 * 1000L);

                    if (!LimitDelay.contains(p)) {
                        String time = new SimpleDateFormat("EEEE HH:mm:ss").format(new Date(resetTimeMillis));

                        dataManager.sendMessage(p, ReformatText(cantearnmsg, e.getJob().getName(), time));
                        LimitDelay.add(p);

                        scheduler.runTaskLater(plugin, () -> {
                            LimitDelay.remove(p);
                            earningjobs.get(e.getJob()).set(0.0);
                        }, resettime * 20L * 60L);

                        earningtime.put(e.getJob(), resetTimeMillis);
                    } else {
                        long count = Math.max(earningtime.getOrDefault(e.getJob(), now) - now, 0);
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(count);
                        long seconds = TimeUnit.MILLISECONDS.toSeconds(count) % 60;
                        String formattedTime = String.format("%02d:%02d", minutes, seconds);

                        if (!delayPlayer.contains(p)) {
                            dataManager.sendMessage(p, ReformatText(overlimitmsg, e.getJob().getName(), formattedTime));
                            delayPlayer.add(p);
                            scheduler.runTaskLater(plugin, () -> delayPlayer.remove(p), 60L);
                        }
                    }
                });

                e.setCancelled(true);
            } else {
                // AtomicDouble already change it, therefore does not necessarily to update the earnings
                earning.addAndGet(income);
            }
        }
    }



    public Component ReformatText(String text, String... text2) {
        var mm = MiniMessage.miniMessage();
        String job = text2.length > 0 ? text2[0] : "";
        String time = text2.length > 1 ? text2[1] : "";

        return mm.deserialize(text.replace("[prefix]", prefix).replace("[job]", job).replace("[time]", time));
    }

    public static Double currentEarning(Player p, String jobname) {
        Job job = com.gamingmesh.jobs.Jobs.getJob(jobname);
        if (job != null && earnings.get(p) != null) {
            return earnings.get(p).computeIfAbsent(job, k -> new AtomicDouble(0.0)).get();
        }
        return 0.0;
    }

    public static String TimeBeforeStartEarning(Player p, String jobname) {
        Job job = com.gamingmesh.jobs.Jobs.getJob(jobname);
        if (job != null && earnings.get(p) != null) {

            long count = earningtimes.get(p).get(job) - System.currentTimeMillis();

            long minutes = TimeUnit.MILLISECONDS.toMinutes(count);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(count) % 60;

            return String.format("%02d:%02d", minutes, seconds);
        }
        return "0:0";
    }

}
