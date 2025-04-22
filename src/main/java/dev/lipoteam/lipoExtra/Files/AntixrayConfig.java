package dev.lipoteam.lipoExtra.Files;

import dev.lipoteam.lipoExtra.Discord.CachedMessages;
import dev.lipoteam.lipoExtra.LipoExtra;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class AntixrayConfig {

    private FileConfiguration config;
    private LipoExtra plugin;

    public AntixrayConfig(LipoExtra plugin, FileConfiguration config) {

        this.plugin = plugin;
        this.config = config;

    }

    public boolean Enabled() { return config.getBoolean("enabled"); }

    public String prefix() { return config.getString("prefix"); }

    public List<String> ListOres() {
        return config.getStringList("detect-ores");
    }

    public String NotifyMsg() {
        return config.getString("lang.notify-admin");
    }

    public boolean DiscordEnabled() { return config.getBoolean("discord.enabled"); }

    public long channel() { return config.getLong("discord.channel"); }

//    discord:
//    enabled: false
//    channel: ''
//    messages:
//      type: embed
    //  color:
    //  url:
//      messages:
//          title: [player]
//          words:
//            - Mined [amount]x [ore] under [time] minutes!
//            - `/tp [player]`

    public CachedMessages DiscordMsg() {

        String title = "";
        String color = "";
        String url = "";
        String guild = "";
        String imgurl = "";
        List<String> words = List.of();
        CachedMessages discordmsg = new CachedMessages(List.of());
        String type = config.getString("discord.messages.type");
        guild = config.getString("discord.guild");
        if (type != null) {
            if (type.equalsIgnoreCase("embed")) {
                title = config.getString("discord.messages.messages.title");
                words = config.getStringList("discord.messages.messages.words");
                color = config.getString("discord.messages.color");
                url = config.getString("discord.messages.url");
                imgurl = config.getString("discord.messages.messages.img-url");
            }
        }
        discordmsg.setType(type);
        discordmsg.setEmbedTitle(title);
        discordmsg.setEmbedWords(words);
        discordmsg.setEmbedUrl(url);
        discordmsg.setEmbedColor(color);
        discordmsg.setGuild(guild);
        discordmsg.setEmbedImgUrl(imgurl);
        return discordmsg;
    }
}
