package dev.lipoteam.lipoExtra.Commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.lipoteam.lipoExtra.Events.Event;
import dev.lipoteam.lipoExtra.Files.TagConfig;
import dev.lipoteam.lipoExtra.LipoExtra;
import dev.lipoteam.lipoExtra.Manager.DataManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagCommands {

    private TagConfig config;
    private LipoExtra plugin;
    private DataManager dataManager;
    private ConcurrentHashMap.KeySetView<String, String> tags;
    private ConcurrentHashMap<String, String> perms = new ConcurrentHashMap<>();
    private String appliedmsg;
    private String prefix;
    private String noperm;
    private String unequip;
    private String nonetag;

    public TagCommands(TagConfig config, LipoExtra plugin) {

        var mm = MiniMessage.miniMessage();
        this.plugin = plugin;
        setConfig(config);
        dataManager = new DataManager(plugin);
        new CommandAPICommand("lipotag")
                .withArguments(new StringArgument("tag").replaceSuggestions(ArgumentSuggestions.strings(
                        info -> {
                            if (info.sender() instanceof Player p) {
                                List<String> tags = Event.playertags.get(p.getUniqueId());
                                tags.add("empty");
                                return tags.toArray(new String[0]);
                            }
                            return new String[]{"empty"};
                        }
                )))
                .executes((sender, args) -> {
                    if (sender instanceof Player p) {
                        String tag = (String) args.get("tag");

                        if (tag != null) {
                            if (tag.equalsIgnoreCase("empty")) {
                                plugin.adventure().player(p).sendMessage(mm.deserialize(unequip.replace("[prefix]", prefix)));
                                if (dataManager.hasData(p, "tag")) {
                                    dataManager.unsetdata(p, "tag");
                                }
                                return;
                            }

                            if (tags.contains(tag)) {
                                if (p.hasPermission(this.config.getPerms(tag))) {
                                    plugin.adventure().player(p).sendMessage(mm.deserialize(convert(appliedmsg.replace("[tag]", this.config.getTagname(tag)), true, '&', true).replace("[prefix]", prefix)));
                                    dataManager.setdata(p, "tag", tag);
                                } else {
                                    plugin.adventure().player(p).sendMessage(mm.deserialize(convert(noperm.replace("[tag]", this.config.getTagname(tag)), true, '&', true).replace("[prefix]", prefix)));
                                }
                            } else {
                                plugin.adventure().player(p).sendMessage(mm.deserialize(convert(nonetag.replace("[tag]", tag), true, '&', true).replace("[prefix]", prefix)));
                            }

                        }

                    }
                })
                .register();

    }

    private String convert(String legacy, boolean concise, char charCode, boolean rgb) {
        // Convert legacy color codes to MiniMessage format

        if (rgb) {
            Pattern pattern = Pattern.compile("#([0-9a-fA-F]{6})");
            Matcher matcher = pattern.matcher(legacy);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                // Extract RGB characters
                String hexColor = matcher.group();
                matcher.appendReplacement(sb, "<" + hexColor + ">");
            }
            matcher.appendTail(sb);
            legacy = sb.toString();
        }

        String miniMessage = legacy
                .replace(charCode + "0", "<black>")
                .replace(charCode + "1", "<dark_blue>")
                .replace(charCode + "2", "<dark_green>")
                .replace(charCode + "3", "<dark_aqua>")
                .replace(charCode + "4", "<dark_red>")
                .replace(charCode + "5", "<dark_purple>")
                .replace(charCode + "6", "<gold>")
                .replace(charCode + "7", "<gray>")
                .replace(charCode + "8", "<dark_gray>")
                .replace(charCode + "9", "<blue>")
                .replace(charCode + "a", "<green>")
                .replace(charCode + "b", "<aqua>")
                .replace(charCode + "c", "<red>")
                .replace(charCode + "d", "<light_purple>")
                .replace(charCode + "e", "<yellow>")
                .replace(charCode + "f", "<white>");

        // Convert formatting codes
        if (concise) {
            miniMessage = miniMessage
                    .replace(charCode + "n", "<u>")
                    .replace(charCode + "m", "<st>")
                    .replace(charCode + "k", "<obf>")
                    .replace(charCode + "o", "<i>")
                    .replace(charCode + "l", "<b>")
                    .replace(charCode + "r", "<reset>");
        } else {
            miniMessage = miniMessage
                    .replace(charCode + "n", "<underlined>")
                    .replace(charCode + "m", "<strikethrough>")
                    .replace(charCode + "k", "<obfuscated>")
                    .replace(charCode + "o", "<italic>")
                    .replace(charCode + "l", "<bold>")
                    .replace(charCode + "r", "<reset>");
        }

        // Convert RGB hex codes (e.g., & #ff00ff -> <#ff00ff>)

        return miniMessage;
    }

    public void setConfig(TagConfig config) {
        this.config = config;
        tags = config.getTags();
        perms = config.getPerms();
        appliedmsg = config.AppliedMsg();
        prefix = config.prefix();
        noperm = config.NoPerms();
        unequip = config.UnequipMsg();
        nonetag = config.NoneTag();
    }

}
