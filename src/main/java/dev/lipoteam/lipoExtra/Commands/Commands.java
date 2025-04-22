package dev.lipoteam.lipoExtra.Commands;

import com.jeff_media.customblockdata.CustomBlockData;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import de.tr7zw.changeme.nbtapi.NBTList;
import de.tr7zw.changeme.nbtapi.NBTType;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import dev.lipoteam.lipoExtra.BedrockForm;
import dev.lipoteam.lipoExtra.Manager.DataManager;
import dev.lipoteam.lipoExtra.Events.Event;
import dev.lipoteam.lipoExtra.Files.Configurations;
import dev.lipoteam.lipoExtra.LipoExtra;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Commands {
    private Configurations config;
    private final DataManager dataManager;

    public Commands(Configurations configurations, LipoExtra lipoExtra) {

        var mm = MiniMessage.miniMessage();
        setConfig(configurations);
        ConsoleCommandSender console = lipoExtra.getServer().getConsoleSender();
        dataManager = new DataManager(lipoExtra);

        new CommandAPICommand("lipoextra")
                .withAliases("li")
                .withSubcommand(new CommandAPICommand("reload")
                        .withPermission("lipoextra.commands.reload")
                        .executes((sender, args) -> {

                            lipoExtra.ReloadConfig();

                            if (sender instanceof Player p) {
                                dataManager.sendMessage(p, mm.deserialize(config.prefix() + "<white>Reloaded"));
                            } else {
                                dataManager.sendMessage(lipoExtra.adventure().console(), mm.deserialize(config.prefix() + "<white>Reloaded"));
                            }
                        }))
                .withSubcommand(new CommandAPICommand("test")
                        .withPermission("lipoextra.commands.test")
                        .executes((sender, args) -> {

                            if (sender instanceof Player p) {
                                p.sendMessage(Event.playerClient.get(p.getUniqueId()));
                                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("test"));
                            } else {
                                console.sendMessage("test");
                            }

                        }))
                .withSubcommand(new CommandAPICommand("form")
                        .withPermission("lipoextra.commands.form")
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
                        .withPermission("lipoextra.commands.cmdsound")
                        .withOptionalArguments(new PlayerArgument("player").replaceSafeSuggestions(SafeSuggestions.suggest(
                                info -> Bukkit.getOnlinePlayers().toArray(new Player[0])
                        )))
                        .executes((sender, args) -> {
                            if (sender instanceof Player p) {
                                if (!(boolean) dataManager.getdata(p, "cmdSound", false)) {
                                    dataManager.sendMessage(p, mm.deserialize(config.prefix() + "<white>You command tab complete are now have sound"));
                                    dataManager.setdata(p, "cmdSound", true);
                                } else {
                                    dataManager.sendMessage(p, mm.deserialize(config.prefix() + "<white>You command tab complete are now silent"));
                                    dataManager.setdata(p, "cmdSound", false);
                                }
                            } else {
                                Player p = (Player) args.get("player");
                                if (!(boolean) dataManager.getdata(p, "cmdSound", false)) {
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
                        .withPermission("lipoextra.commands.flyparticle")
                        .withArguments(new StringArgument("modes").replaceSuggestions(ArgumentSuggestions.strings("all", "minimal", "others", "next")))
                        .withOptionalArguments(new PlayerArgument("player").replaceSafeSuggestions(SafeSuggestions.suggest(
                                info -> Bukkit.getOnlinePlayers().toArray(new Player[0])
                        )))
                        .executes((sender, args) -> {
                            String modes = (String) args.get("modes");
                            if (modes == null) return;
                            if (sender instanceof Player p) {
                                if (p.hasPermission("lipoextra.commands.flyparticle.others")) {
                                    Player p2 = (Player) args.get("player");

                                    if (p2 != null) {

                                        if (modes.equalsIgnoreCase("next")) {
                                            String mode = "all";
                                            if (dataManager.getdata(p2, "flyparticle",false).equals("all")) {
                                                mode = "minimal";
                                            } else if (dataManager.getdata(p2, "flyparticle",false).equals("minimal")) {
                                                mode = "others";
                                            }
                                            dataManager.setdata(p2, "flyparticle", mode);
                                            dataManager.sendMessage(p2, mm.deserialize(modechange.replace("[prefix]", prefix).replace("[player]", p2.getName()).replace("[mode]", (String) args.get("modes"))));
                                            return;
                                        }

                                        dataManager.sendMessage(p2, mm.deserialize(modechange.replace("[prefix]", prefix).replace("[player]", p2.getName()).replace("[mode]", (String) args.get("modes"))));
                                        dataManager.setdata(p2, "flyparticle", args.get("modes"));
                                    } else {

                                        if (modes.equalsIgnoreCase("next")) {
                                            String mode = "all";
                                            if (dataManager.getdata(p, "flyparticle",false).equals("all")) {
                                                mode = "minimal";
                                            } else if (dataManager.getdata(p, "flyparticle",false).equals("minimal")) {
                                                mode = "others";
                                            }
                                            dataManager.setdata(p, "flyparticle", mode);
                                            dataManager.sendMessage(p, mm.deserialize(modechange.replace("[prefix]", prefix).replace("[player]", p.getName()).replace("[mode]", (String) args.get("modes"))));
                                            return;
                                        }

                                        dataManager.sendMessage(p, mm.deserialize(modechange.replace("[prefix]", prefix).replace("[player]", p.getName()).replace("[mode]", (String) args.get("modes"))));
                                        dataManager.setdata(p, "flyparticle", args.get("modes"));
                                    }
                                } else {

                                    if (modes.equalsIgnoreCase("next")) {
                                        String mode = "all";
                                        if (dataManager.getdata(p, "flyparticle",false).equals("all")) {
                                            mode = "minimal";
                                        } else if (dataManager.getdata(p, "flyparticle",false).equals("minimal")) {
                                            mode = "others";
                                        }
                                        dataManager.setdata(p, "flyparticle", mode);
                                        dataManager.sendMessage(p, mm.deserialize(modechange.replace("[prefix]", prefix).replace("[player]", p.getName()).replace("[mode]", (String) args.get("modes"))));
                                        return;
                                    }

                                    dataManager.sendMessage(p, mm.deserialize(modechange.replace("[prefix]", prefix).replace("[player]", p.getName()).replace("[mode]", (String) args.get("modes"))));
                                    dataManager.setdata(p, "flyparticle", args.get("modes"));
                                }

                            } else {
                                Player p2 = (Player) args.get("player");
                                if (p2 != null) {

                                    if (modes.equalsIgnoreCase("next")) {
                                        String mode = "all";
                                        if (dataManager.getdata(p2, "flyparticle",false).equals("all")) {
                                            mode = "minimal";
                                        } else if (dataManager.getdata(p2, "flyparticle",false).equals("minimal")) {
                                            mode = "others";
                                        }
                                        dataManager.setdata(p2, "flyparticle", mode);
                                        dataManager.sendMessage(p2, mm.deserialize(modechange.replace("[prefix]", prefix).replace("[player]", p2.getName()).replace("[mode]", mode)));
                                        return;
                                    }

                                    dataManager.sendMessage(p2, mm.deserialize(modechange.replace("[prefix]", prefix).replace("[player]", p2.getName()).replace("[mode]", (String) args.get("modes"))));
                                    dataManager.setdata(p2, "flyparticle", args.get("modes"));
                                } else {
                                    lipoExtra.adventure().console().sendMessage(mm.deserialize("[prefix] <white>There's no player with that name".replace("[prefix]", prefix)));
                                }
                            }
                        })
                )
                .withSubcommand(new CommandAPICommand("removedata")
                        .withPermission("lipoextra.commands.removedata")
                        .withArguments(new StringArgument("data").replaceSuggestions(ArgumentSuggestions.strings(
                                info -> {
                                    if (info.sender() instanceof Player p) {
                                        return p.getPersistentDataContainer().getKeys()
                                                .stream()
                                                .map(NamespacedKey::getKey)
                                                .toArray(String[]::new);
                                    }
                                    return new String[0];
                                }
                        )))
                        .withOptionalArguments(new PlayerArgument("player").replaceSafeSuggestions(SafeSuggestions.suggest(
                                info -> Bukkit.getOnlinePlayers().toArray(new Player[0])
                        )))
                        .executes((sender, args) -> {
                            if (sender instanceof Player p) {
                                Player p2 = (Player) args.get("player");

                                if (p2 != null) {
                                    p = p2;
                                }

                                String data = (String) args.get("data");
                                if (data != null) {
                                    dataManager.sendMessage(p, mm.deserialize(prefix + data + " <red>have been removed from player " + p.getName()));
                                    dataManager.unsetdata(p, data);
                                }

                            }
                        })
                )
                .withSubcommand(new CommandAPICommand("getnbtdata")
                        .withPermission("lipoextra.commands.getnbtdata")
                        .withArguments(new PlayerArgument("player").replaceSafeSuggestions(SafeSuggestions.suggest(
                                info -> Bukkit.getOnlinePlayers().toArray(new Player[0])
                        )))
                        .withArguments(new StringArgument("data").replaceSuggestions(ArgumentSuggestions.stringCollectionAsync((info) -> {
                                Player target = null;
                                try {
                                    target = (Player) info.previousArgs().get("player");
                                } catch (Exception ignored) {}

                                if (target == null) return CompletableFuture.completedFuture(Collections.emptyList());

                                Player finalTarget = target;
                                return CompletableFuture.supplyAsync(() -> {
                                            NBTEntity nbt = new NBTEntity(finalTarget); // shaded + deprecated is fine

                                            return new ArrayList<>(nbt.getKeys()); // return list of strings
                                        });
                                    })
                            ))
                        .executes((sender, args) -> {

                            if (!(sender instanceof Player p)) {
                                sender.sendMessage("Console can't use this command.");
                                return;
                            }

                            Player target = (Player) args.get("player");
                            String key = (String) args.get("data");

                            NBTEntity compound = new NBTEntity(target);
                            if (!compound.hasTag(key)) {
                                p.sendMessage(Component.text("§cNBT key not found: " + key));
                                return;
                            }
                            NBTType type = compound.getType(key);
                            String valueString;
                            try {
                                switch (compound.getType(key)) {
                                    case NBTTagByte -> valueString = Byte.toString(compound.getByte(key));
                                    case NBTTagShort -> valueString = Short.toString(compound.getShort(key));
                                    case NBTTagInt -> valueString = Integer.toString(compound.getInteger(key));
                                    case NBTTagLong -> valueString = Long.toString(compound.getLong(key));
                                    case NBTTagFloat -> valueString = Float.toString(compound.getFloat(key));
                                    case NBTTagDouble -> valueString = Double.toString(compound.getDouble(key));
                                    case NBTTagString -> valueString = compound.getString(key);
                                    case NBTTagCompound -> {
                                        NBTCompound subCompound = compound.getCompound(key);
                                        if (subCompound != null) {
                                            valueString = subCompound.toString();
                                        } else {
                                            valueString = "Compound is null";
                                        }
                                         // or maybe iterate its keys?
                                    }
                                    case NBTTagIntArray -> valueString = Arrays.toString(compound.getIntArray(key));
                                    case NBTTagLongArray -> valueString = Arrays.toString(compound.getLongArray(key));
                                    default -> valueString = "Unsupported NBT type." + compound.getType(key);
                                }
                            } catch (Exception ex) {
                                valueString = "Error reading key: " + ex.getMessage();
                            }

                            p.sendMessage(Component.text("NBT [" + key + "] (" + type + "): " + valueString));

                        }))
                .withSubcommand(new CommandAPICommand("getblockpdc")
                        .withPermission("lipoextra.commands.getblockpdc")
                        .withArguments(new LocationArgument("location"))
                        .executes((sender, args) -> {
                            Location loc = (Location) args.get("location");
                            if (sender instanceof Player p && loc != null) {
                                PersistentDataContainer pdc = new CustomBlockData(loc.getBlock(), lipoExtra);

                                for (NamespacedKey key : pdc.getKeys()) {
                                    String value = "unknown";

                                    if (pdc.has(key, PersistentDataType.STRING)) {
                                        value = pdc.get(key, PersistentDataType.STRING);
                                    } else if (pdc.has(key, PersistentDataType.INTEGER)) {
                                        value = String.valueOf(pdc.get(key, PersistentDataType.INTEGER));
                                    } else if (pdc.has(key, PersistentDataType.DOUBLE)) {
                                        value = String.valueOf(pdc.get(key, PersistentDataType.DOUBLE));
                                    } else if (pdc.has(key, PersistentDataType.LONG)) {
                                        value = String.valueOf(pdc.get(key, PersistentDataType.LONG));
                                    } else if (pdc.has(key, PersistentDataType.BYTE)) {
                                        value = String.valueOf(pdc.get(key, PersistentDataType.BYTE));
                                    }

                                    p.sendMessage(mm.deserialize("<gray>" + key + " <white>= <green>" + value));
                                }
                            }
                        }))
                .register("lipo");

    }

    private String prefix;
    private String modechange;

    public void setConfig(Configurations config) {

        this.config = config;
        this.prefix = config.prefix();
        this.modechange = config.ModeChangeMsg();
    }
}
