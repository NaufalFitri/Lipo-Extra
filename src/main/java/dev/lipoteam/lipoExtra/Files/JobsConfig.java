package dev.lipoteam.lipoExtra.Files;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.Job;
import dev.lipoteam.lipoExtra.LipoExtra;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class JobsConfig {

    private final Double default_limit = 1000.0;
    private LipoExtra plugin;
    private final FileConfiguration config;

    private static final ConcurrentHashMap<Job, Double> earningslimit = new ConcurrentHashMap<>();

    public JobsConfig(FileConfiguration config, LipoExtra plugin) {
        this.plugin = plugin;
        this.config = config;

        List<Job> jobs = Jobs.getJobs();
        for (Job job : jobs) {
            String path = "jobs-earn-limit." + job.getName();

            if (!config.contains(path)) {
                config.set(path, default_limit);
            }
            earningslimit.put(job, config.getDouble(path));
        }

        try {
            config.save(new File(plugin.getDataFolder() + File.separator + "/Extras", "jobs.yml"));
        } catch (IOException e) {
            Bukkit.getLogger().warning(String.valueOf(e));
        }

    }

    public static double EarningLimit(String job) {
        Job thejob = Jobs.getJob(job);
        if (job != null) {
            return earningslimit.getOrDefault(thejob, 0.0);
        }
        return 0.0;
    }

    public Boolean enabled() {
        return config.getBoolean("enabled");
    }

    public String prefix() {
        return config.getString("prefix");
    }

    public Double EarnLimit(Job job) {
        return earningslimit.getOrDefault(job, default_limit);
    }

    public int ResetTime() {
        return config.getInt("reset-every");
    }

    public String OverLimitMessage() {
        return config.getString("lang.over-limit");
    }

    public String CantEarnMessage() {
        return config.getString("lang.cannot-earn");
    }

    public String CantEarnRestartMsg() {
        return config.getString("lang.cant-earn-after-restart");
    }

    public boolean CantEarnAfterRestart() {
        return config.getBoolean("cannot-earn-after-restart");
    }

}
