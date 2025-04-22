package dev.lipoteam.lipoExtra.Commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.lipoteam.lipoExtra.Files.TikTokConfig;
import dev.lipoteam.lipoExtra.LipoExtra;
import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.UUID;
import java.util.logging.Level;

public class TiktokCommands {

    private TikTokConfig config;
    private LipoExtra plugin;
    private String connectmsg;
    private String prefix;
    private String errormsg;
    private String notvalidmsg;
    private String notonlinemsg;
    private String notsetupmsg;
    private String broadcast;
    private String disconnectMsg;
    private Sound broadcastSound;
    private Sound disconnectSound;

    public TiktokCommands(TikTokConfig config, LipoExtra plugin) {

        var mm = MiniMessage.miniMessage();
        this.plugin = plugin;
        setConfig(config);

        new CommandAPICommand("live")
                .withPermission("lipo.live")
                .withSubcommand(new CommandAPICommand("start")
                        .withOptionalArguments(new StringArgument("extra").replaceSuggestions(ArgumentSuggestions.strings("-s")))
                        .executes((sender, args) -> {
                            if (sender instanceof Player p) {
                                String extra;
                                extra = (String) args.get("extra");
                                UUID id = p.getUniqueId();

                                // Run all the heavy logic async
                                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                    if (this.config.getCreators().containsKey(id)) {
                                        String hostname = this.config.getCreators().get(id);

                                        if (TikTokLive.isHostNameValid(hostname)) {
                                            if (TikTokLive.isLiveOnline(hostname)) {
                                                TikTokLive.newClient(hostname)
                                                        .configure(clientSettings ->
                                                        {
                                                            clientSettings.setHostName(hostname); // This method is useful in case you want change hostname later
                                                            clientSettings.setClientLanguage("en"); // Language
                                                            clientSettings.setLogLevel(Level.ALL); // Log level
                                                            clientSettings.setPrintToConsole(true); // Printing all logs to console even if log level is Level.OFF
                                                            clientSettings.setRetryOnConnectionFailure(true); // Reconnecting if TikTok user is offline
                                                            clientSettings.setRetryConnectionTimeout(Duration.ofSeconds(3)); // Timeout before next reconnection

                                                            clientSettings.getHttpSettings();
                                                        })
                                                        .onConnected((liveClient, e) -> {
                                                            // Send success message on main thread
                                                            if (extra == null || !extra.equalsIgnoreCase("-s")) {
                                                                plugin.adventure().players().playSound(broadcastSound);
                                                                plugin.adventure().players().sendMessage(mm.deserialize(broadcast.replace("[prefix]", prefix).replace("[player]", p.getName()).replace("[hostname]", hostname)));
                                                            }

                                                            plugin.adventure().player(p).sendMessage(mm.deserialize(connectmsg.replace("[prefix]", prefix)));
                                                            plugin.adventure().console().sendMessage(mm.deserialize(connectmsg.replace("[prefix]", prefix)));
                                                        })
                                                        .onError((liveClient, e) -> {
                                                            // Send error message on main thread
                                                            plugin.adventure().player(p).sendMessage(mm.deserialize(errormsg.replace("[prefix]", prefix)));
                                                            e.getException().printStackTrace();
                                                        })
                                                        .onDisconnected((liveClient, e) -> {

                                                            plugin.adventure().players().playSound(disconnectSound);
                                                            plugin.adventure().players().sendMessage(mm.deserialize(disconnectMsg.replace("[prefix]", prefix).replace("[player]", p.getName())));

                                                        })
                                                        .buildAndConnect();
                                            } else {
                                                plugin.adventure().player(p).sendMessage(mm.deserialize(notonlinemsg.replace("[prefix]", prefix)));
                                            }
                                        } else {
                                            plugin.adventure().player(p).sendMessage(mm.deserialize(notvalidmsg.replace("[prefix]", prefix).replace("[hostname]", hostname)));
                                        }
                                    } else {
                                        plugin.adventure().player(p).sendMessage(mm.deserialize(notsetupmsg.replace("[prefix]", prefix)));
                                    }
                                });
                            }
                        }))
                .register();

    }

    public void setConfig(TikTokConfig config) {
        this.config = config;
        prefix = config.prefix();
        connectmsg = config.connectedMsg();
        errormsg = config.errorMsg();
        notvalidmsg = config.notValidMsg();
        notonlinemsg = config.notOnlineMsg();
        notsetupmsg = config.notSetupMsg();
        broadcast = config.broadcast();
        broadcastSound = config.broadcastSound();
        disconnectSound = config.disconnectSound();
        disconnectMsg = config.disconnectMsg();
    }

}
