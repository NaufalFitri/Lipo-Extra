package dev.lipoteam.lipoHud.Commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.SafeSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.lipoteam.lipoHud.BedrockForm;
import dev.lipoteam.lipoHud.DataManager;
import dev.lipoteam.lipoHud.Events.Event;
import dev.lipoteam.lipoHud.Files.Configurations;
import dev.lipoteam.lipoHud.LipoHud;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class Commands {
    private Configurations config;
    private final DataManager dataManager;



    public Commands(Configurations configurations, LipoHud lipoHud) {

        var mm = MiniMessage.miniMessage();
        setConfig(configurations);
        ConsoleCommandSender console = lipoHud.getServer().getConsoleSender();
        dataManager = new DataManager(lipoHud);

        new CommandAPICommand("lipohud")
                .withAliases("lh")
                .withPermission("lipohud.commands")
                .withSubcommand(new CommandAPICommand("reload")
                        .withPermission("lipohud.commands.reload")
                        .executes((sender, args) -> {

                            lipoHud.ReloadConfig();

                            if (sender instanceof Player p) {
                                dataManager.sendMessage(p, mm.deserialize(config.prefix() + "<white>Reloaded"));
                            } else {
                                dataManager.sendMessage(lipoHud.adventure().console(), mm.deserialize(config.prefix() + "<white>Reloaded"));
                            }
                        }))
                .withSubcommand(new CommandAPICommand("test")
                        .withPermission("lipohud.commands.test")
                        .executes((sender, args) -> {

                            if (sender instanceof Player p) {
                                p.sendMessage(Event.playerClient.get(p.getUniqueId()));
                                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("test"));
                            } else {
                                console.sendMessage("test");
                            }

                        }))
                .withSubcommand(new CommandAPICommand("form")
                        .withPermission("lipohud.commands.form")
                        .withArguments(new StringArgument("form type")
                                .replaceSuggestions(ArgumentSuggestions
                                        .strings("simple", "custom", "modal")))
                        .withArguments(new PlayerArgument("player").replaceSafeSuggestions(SafeSuggestions.suggest(
                                info -> Bukkit.getOnlinePlayers().toArray(new Player[0])
                        )))
                        .withArguments(new StringArgument("string"))
                        .executes((sender, args) -> {

                            if (args.get("player") != null && args.get("string") != null && args.get("form type") != null) {

                                Player player = (Player) args.get("player");

                                if (sender instanceof Player p) {
                                    if (player == null) return;
                                    if (Event.playerClient.get(player.getUniqueId()).equalsIgnoreCase("bedrock")) {

                                        BedrockForm.OpenForm(player, (String) args.get("string"), (String) Objects.requireNonNull(args.get("form type")));

                                    } else {
                                        p.sendMessage("They are not allowed to use this");
                                    }
                                } else {
                                    if (player == null) return;
                                    if (Event.playerClient.get(player.getUniqueId()).equalsIgnoreCase("bedrock")) {

                                        BedrockForm.OpenForm(player, (String) args.get("string"), (String) Objects.requireNonNull(args.get("form type")));

                                    } else {
                                        console.sendMessage("They are not allowed to use this");
                                    }
                                }
                            }


                        }))
                .withSubcommand(new CommandAPICommand("commandsound")
                        .withPermission("lipohud.commands.cmdsound")
                        .executes((sender, args) -> {
                            if (sender instanceof Player p) {
                                if (dataManager.hasData(p, "cmdSound")) {
                                    dataManager.sendMessage(p, mm.deserialize(config.prefix() + "<white>You command tab complete are now have sound"));
                                    dataManager.setdata(p, "cmdSound", true);
                                } else {
                                    dataManager.sendMessage(p, mm.deserialize(config.prefix() + "<white>You command tab complete are now silent"));
                                    dataManager.setdata(p, "cmdSound", false);
                                }
                            }
                        })
                )
                .withSubcommand(new CommandAPICommand("flyparticle")
                        .withPermission("lipohud.commands.flyparticle")
                        .executes((sender, args) -> {
                            if (sender instanceof Player p) {
                                if (Event.stopparticles.contains(p.getUniqueId())) {
                                    Event.stopparticles.remove(p.getUniqueId());
                                } else {
                                    Event.stopparticles.add(p.getUniqueId());
                                }
                            }
                        })
                )
                .register("lipo");

    }

    public void setConfig(Configurations config) {
        this.config = config;
    }
}
