package dev.lipoteam.lipoExtra.Events;

import dev.lipoteam.lipoExtra.Discord.CachedMessages;
import dev.lipoteam.lipoExtra.Discord.DiscordEvent;
import dev.lipoteam.lipoExtra.Files.AntixrayConfig;
import dev.lipoteam.lipoExtra.LipoExtra;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class Antixray implements Listener {

    private AntixrayConfig config;
    private final LipoExtra plugin;
    private boolean enabled;
    private String prefix;
    private List<String> listoresnumtime = new ArrayList<>();
    private final ConcurrentHashMap<String, AtomicReference<List<UUID>>> currentdetect = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, LinkedHashMap<String, BukkitTask>> taskdetect = new ConcurrentHashMap<>();
    private String notifyMsg;
    private final BukkitScheduler scheduler;
    private final MiniMessage mm;
    private long discordChannel;
    private boolean discordenabled;
    private CachedMessages discordmsg;
    private TextChannel channel = null;
    private JDA jda;

    public Antixray(LipoExtra plugin, AntixrayConfig config) {

        this.plugin = plugin;
        scheduler = plugin.getServer().getScheduler();
        mm = MiniMessage.miniMessage();
        setConfig(config);
    }

    public void setConfig(AntixrayConfig config) {
        this.config = config;
        enabled = config.Enabled();
        prefix = config.prefix();
        listoresnumtime = config.ListOres();
        notifyMsg = config.NotifyMsg();
        discordenabled = config.DiscordEnabled();
        discordmsg = config.DiscordMsg();
        discordChannel = config.channel();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            jda = DiscordEvent.jda;
            if (jda != null) {
                channel = jda.getTextChannelById(discordChannel);
                plugin.getLogger().warning("Antixray hooked into " + channel.getGuild().getName() + " in channel " + channel.getName());
            }
        }, 30L);
    }

    @EventHandler
    private void PlayerBreak(BlockBreakEvent e) {
        if (!enabled) return;

        Player p = e.getPlayer();
        Material m = e.getBlock().getType();
        UUID id = p.getUniqueId();

        List<String> listores = new ArrayList<>();
        List<Integer> listnum = new ArrayList<>();
        List<Integer> listtime = new ArrayList<>();
        int index = -1;

        for (String list : listoresnumtime) {
            String[] split = list.split(":");
            listores.add(split[0]);
            listnum.add(Integer.parseInt(split[1]));
            listtime.add(Integer.parseInt(split[2]));
        }

        for (int i = 0; i < listores.size(); i++) {
            if (m.name().contains(listores.get(i))) {
                index = i;
                break;
            }
        }
        if (index == -1) return;

        int num = listnum.get(index);
        int time = listtime.get(index);

        AtomicReference<List<UUID>> idlist = currentdetect.computeIfAbsent(m.name(), key -> new AtomicReference<>(new CopyOnWriteArrayList<>()));

        if (!idlist.get().contains(id)) {
            p.setMetadata(m.name(), new FixedMetadataValue(plugin, 1));
            idlist.get().add(id);

            BukkitTask task = scheduler.runTaskLater(plugin, () -> {
                p.removeMetadata(m.name(), plugin);
                idlist.get().remove(id);
            }, time * 20L * 60L);

            taskdetect.putIfAbsent(id, new LinkedHashMap<>());
            taskdetect.get(id).putIfAbsent(m.name(), task);

        } else {
            if (p.hasMetadata(m.name())) {
                int amount = p.getMetadata(m.name()).getFirst().asInt();
                p.setMetadata(m.name(), new FixedMetadataValue(plugin, ++amount));

                if (amount >= num) {
                    int finalAmount = amount;
                    Bukkit.getOnlinePlayers().forEach(a -> {
                        if (a.hasPermission("lipo.antixray.notify")) {
                            plugin.adventure().player(a).sendMessage(mm.deserialize(notifyMsg.replace("[prefix]", prefix).replace("[player]", p.getName()).replace("[ore]", m.name()).replace("[time]", String.valueOf(time)).replace("[amount]", String.valueOf(finalAmount))));
                        }
                    });
                    if (discordenabled) {
                        if (channel != null && channel.getGuild().getName().equalsIgnoreCase(discordmsg.getGuild())) {
                            if (!discordmsg.getEmbedWords().isEmpty()) {
                                String url = discordmsg.getEmbedUrl();
                                String imgurl = discordmsg.getEmbedImgUrl();
                                String title = discordmsg.getEmbedTitle().replace("[player]", p.getName()).replace("[amount]", String.valueOf(finalAmount)).replace("[ore]", m.name()).replace("[time]", String.valueOf(time)).replace("[prefix]", prefix);
                                List<String> descriptionList = discordmsg.getEmbedWords();

                                int x = (int) p.getLocation().getX();
                                int y = (int) p.getLocation().getY();
                                int z = (int) p.getLocation().getZ();
                                String world = p.getWorld().getName();

                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setTitle(title)
                                        .setThumbnail(imgurl.replace("[player]", p.getName()))
                                        .setDescription(String.join("\n", descriptionList)
                                                .replace("[player]", p.getName()).replace("[amount]", String.valueOf(finalAmount)).replace("[ore]", m.name())
                                                .replace("[time]", String.valueOf(time)).replace("[prefix]", prefix)
                                                .replace("[position]", world + " " + x + "," + y + "," + z))
                                        .setColor(Color.decode(discordmsg.getEmbedColor()));

                                if (!url.isEmpty()) {
                                    embedBuilder.setThumbnail(url);
                                }

                                channel.sendMessageEmbeds(embedBuilder.build()).queue();
                            }
                        }
                    }
                    plugin.adventure().console().sendMessage(mm.deserialize(notifyMsg.replace("[prefix]", prefix).replace("[player]", p.getName()).replace("[ore]", m.name()).replace("[time]", String.valueOf(time)).replace("[amount]", String.valueOf(finalAmount))));
                    taskdetect.get(id).get(m.name()).cancel();
                    if (taskdetect.get(id).isEmpty()) {
                        taskdetect.remove(id);
                    }
                    p.removeMetadata(m.name(), plugin);
                    idlist.get().remove(id);
                }

            }
        }

    }

}
