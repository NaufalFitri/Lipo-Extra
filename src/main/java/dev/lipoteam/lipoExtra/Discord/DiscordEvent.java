package dev.lipoteam.lipoHud.Discord;

import dev.lipoteam.lipoHud.Files.DiscordConfig;
import dev.lipoteam.lipoHud.LipoHud;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

import java.awt.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class DiscordEvent extends ListenerAdapter {

    private LipoHud plugin;
    private DiscordConfig config;
    private String token = null;
    private JDA jda = null;
    private BukkitScheduler scheduler;

    public DiscordEvent(DiscordConfig config, LipoHud plugin) {

        this.plugin = plugin;
        scheduler = plugin.getServer().getScheduler();
        setConfig(config);

    }

    public void setConfig(DiscordConfig config) {
        this.config = config;
        if (!config.Enabled()) {
            if (jda != null) {
                jda.shutdownNow();
                jda = null;
            }
            return;
        }

        if (jda == null || !Objects.equals(token, config.Token())) {
            if (jda != null) {
                jda.shutdownNow();
            }

            token = config.Token();
            jda = JDABuilder.createLight(token, EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT))
                    .addEventListeners(this)
                    .build();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        String input = e.getMessage().getContentRaw();
        MessageChannel channel = e.getChannel();
        Member member = e.getMember();

        if (input.isEmpty()) return;

        String symbol = String.valueOf(input.charAt(0));

        String[] splits = input.split(" ");

        String command = splits[0].substring(1);
        CachedMessages cachedMessage = config.getMessage(symbol, command);

        if (cachedMessage != null) {

            for (String roleId : cachedMessage.getPermissionRoleId()) {
                if (cachedMessage.getPermissionRoleId().contains("default")) break;
                if (member != null) {
                    try {
                        Role role = jda.getRoleById(Long.parseLong(roleId));
                        if (role != null && !member.getRoles().contains(role)) {
                            return;
                        }
                    } catch (NumberFormatException ee) {
                        System.out.println("Invalid role ID in config: " + roleId);
                    }
                }
            }

            if (!cachedMessage.getMessages().isEmpty()) {
                channel.sendMessage(String.join("\n", cachedMessage.getMessages())).queue();
            }

            for (String cmd : cachedMessage.getCommands()) {

                for (int i = 0; i < cachedMessage.getPlaceholders().size(); i++) {
                    String placeholder = cachedMessage.getPlaceholders().get(i);

                    if (splits.length > i + 1) {
                        cmd = cmd.replace(placeholder, splits[i + 1]);
                    }
                }

                String finalCmd = cmd;
                scheduler.runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd));
            }

            if (!cachedMessage.getEmbedWords().isEmpty()) {
                String url = cachedMessage.getEmbedUrl();
                String title = cachedMessage.getEmbedWords().getFirst();
                List<String> descriptionList = cachedMessage.getEmbedWords().subList(1, cachedMessage.getEmbedWords().size());

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle(title)
                        .setDescription(String.join("\n", descriptionList))
                        .setColor(Color.decode(cachedMessage.getEmbedColor()));

                if (!url.isEmpty()) {
                    embedBuilder.setThumbnail(url);
                }

                channel.sendMessageEmbeds(embedBuilder.build()).queue();
            }

        }

    }


}
