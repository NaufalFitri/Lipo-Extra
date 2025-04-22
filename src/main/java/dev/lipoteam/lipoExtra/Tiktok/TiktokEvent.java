package dev.lipoteam.lipoExtra.Tiktok;

import dev.lipoteam.lipoExtra.Files.TikTokConfig;
import dev.lipoteam.lipoExtra.LipoExtra;

public class TiktokEvent {

    private TikTokConfig config;
    private LipoExtra plugin;

    public TiktokEvent(TikTokConfig config, LipoExtra plugin) {
        this.plugin = plugin;
        setConfig(config);


    }

    public void setConfig(TikTokConfig config) {
        this.config = config;
    }



}
