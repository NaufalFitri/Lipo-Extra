package dev.lipoteam.lipoExtra.Commands;

import com.jeff_media.customblockdata.CustomBlockData;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import de.tr7zw.changeme.nbtapi.NBTList;
import de.tr7zw.changeme.nbtapi.NBTType;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import dev.lipoteam.lipoExtra.BedrockForm;
import dev.lipoteam.lipoExtra.Files.ElevatorConfig;
import dev.lipoteam.lipoExtra.Files.MobcatcherConfig;
import dev.lipoteam.lipoExtra.Manager.DataManager;
import dev.lipoteam.lipoExtra.Events.Event;
import dev.lipoteam.lipoExtra.Files.Configurations;
import dev.lipoteam.lipoExtra.LipoExtra;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import se.file14.procosmetics.cosmetic.pet.P;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Commands {
    private Configurations config;
    private LipoExtra plugin;
    private final DataManager dataManager;
    private final MiniMessage mm;

    public Commands(Configurations configurations, LipoExtra lipoExtra) {

        mm = MiniMessage.miniMessage();
        this.plugin = lipoExtra;
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
                        .withArguments(new LocationArgument("location").replaceSafeSuggestions(SafeSuggestions.suggestCollection(
                                info -> {
                                    if (info.sender() instanceof Player p) {
                                        Block target = p.getTargetBlockExact(5);
                                        if (target != null) {
                                            return List.of(target.getLocation());
                                        }
                                    }
                                    return List.of();
                                }
                        )))
                        .executes((sender, args) -> {
                            Location loc = (Location) args.get("location");
                            if (sender instanceof Player p && loc != null) {
                                PersistentDataContainer pdc = new CustomBlockData(loc.getBlock(), lipoExtra);

                                if (pdc.getKeys().isEmpty()) {
                                    p.sendMessage(mm.deserialize(prefix + "<gray>No PersistentData were found"));
                                }
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
                .withSubcommand(new CommandAPICommand("chunkvisual")
                        .withPermission("lipoextra.commands.chunkvisual")
                        .withOptionalArguments(new PlayerArgument("player").replaceSafeSuggestions(SafeSuggestions.suggest(
                                info -> Bukkit.getOnlinePlayers().toArray(new Player[0])
                        )))
                        .withOptionalArguments(new IntegerArgument("amount"))
                        .withOptionalArguments(new IntegerArgument("uses"))
                        .executes((sender, args) -> {
                            Player p1 = (Player) args.get("player");
                            int amount;
                            int uses;
                            try {
                                uses = (int) args.get("uses");
                            } catch (NullPointerException ex) {
                                uses = 0;
                            }
                            try {
                                amount = (int) args.get("amount");
                            } catch (NullPointerException ex) {
                                amount = 0;
                            }

                            if (amount == 0) {
                                amount = 1;
                            }
                            if (uses == 0) {
                                uses = 1;
                            }

                            if (sender instanceof Player p) {

                                GiveChunkVisualizer(Objects.requireNonNullElse(p1, p), amount, uses);

                            } else {
                                if (p1 != null) {
                                    GiveChunkVisualizer(p1, amount,uses);
                                } else {
                                    lipoExtra.adventure().console().sendMessage(mm.deserialize(prefix + "<red>Please specify the player!"));
                                }
                            }
                        })
                )
                .withSubcommand(new CommandAPICommand("elevatorkit")
                        .withPermission("lipoextra.commands.elevator")
                        .withOptionalArguments(new PlayerArgument("player").replaceSafeSuggestions(SafeSuggestions.suggest(
                                info -> Bukkit.getOnlinePlayers().toArray(new Player[0])
                        )))
                        .withOptionalArguments(new IntegerArgument("amount"))
                        .executes((sender, args) -> {
                            Player p1 = (Player) args.get("player");
                            int amount;
                            try {
                                amount = (int) args.get("amount");
                            } catch (NullPointerException ex) {
                                amount = 0;
                            }

                            if (amount == 0) {
                                amount = 1;
                            }

                            if (sender instanceof Player p) {

                                GiveElevatorKit(Objects.requireNonNullElse(p1, p), amount);

                            } else {
                                if (p1 != null) {
                                    GiveElevatorKit(p1, amount);
                                } else {
                                    lipoExtra.adventure().console().sendMessage(mm.deserialize(prefix + "<red>Please specify the player!"));
                                }
                            }
                        })
                ).withSubcommand(new CommandAPICommand("mobcatcher")
                        .withPermission("lipoextra.commands.mobcatcher")
                        .withOptionalArguments(new PlayerArgument("player").replaceSafeSuggestions(SafeSuggestions.suggest(
                                info -> Bukkit.getOnlinePlayers().toArray(new Player[0])
                        )))
                        .withOptionalArguments(new IntegerArgument("amount"))
                        .executes((sender, args) -> {
                            Player p1 = (Player) args.get("player");
                            int amount;
                            try {
                                amount = (int) args.get("amount");
                            } catch (NullPointerException ex) {
                                amount = 0;
                            }

                            if (amount == 0) {
                                amount = 1;
                            }

                            if (sender instanceof Player p) {

                                GiveMobCatcher(Objects.requireNonNullElse(p1, p), amount);

                            } else {
                                if (p1 != null) {
                                    GiveMobCatcher(p1, amount);
                                } else {
                                    lipoExtra.adventure().console().sendMessage(mm.deserialize(prefix + "<red>Please specify the player!"));
                                }
                            }
                        })
                )
                .register("lipo");

    }

    private String prefix;
    private String modechange;

    private String cvitem;
    private String cvitemname;
    private List<String> cvitemlore;

    private void GiveChunkVisualizer(Player p, int amount, int uses) {
        p.sendMessage(mm.deserialize(prefix + "<reset>You have been given " + amount + " <gradient:#fff500:#ffc700>ChunkVisualizer"));
        ItemStack chunkitem = ItemStack.of(Material.valueOf(cvitem), amount);
        dataManager.setdata(chunkitem, "uses", uses);
        ItemMeta meta = chunkitem.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(cvitemname));
            List<Component> lore = cvitemlore.stream()
                    .map(u -> mm.deserialize(u.replace("[uses]", String.valueOf(uses))))
                    .collect(Collectors.toList());
            meta.lore(lore);
            chunkitem.setItemMeta(meta);
        }
        HashMap<Integer, ItemStack> leftovers = p.getInventory().addItem(chunkitem);
        for (ItemStack leftover : leftovers.values()) {
            p.getWorld().dropItemNaturally(p.getLocation(), leftover);
        }
    }

    private ItemStack elevatorkit;
    private String elevatorprefix;
    private List<String> elevatorlore;
    private String elevatorname;

    private void GiveElevatorKit(Player p, int amount) {
        p.sendMessage(mm.deserialize(elevatorprefix + "<reset>You have been given " + amount + " " + elevatorname));
        ItemStack item = elevatorkit;
        item.setAmount(amount);
        dataManager.setdata(item, "kit", true);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<Component> lore = elevatorlore.stream()
                    .map(u -> mm.deserialize(u.replace("[location]", "")))
                    .collect(Collectors.toList());
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        HashMap<Integer, ItemStack> leftovers = p.getInventory().addItem(item);
        for (ItemStack leftover : leftovers.values()) {
            p.getWorld().dropItemNaturally(p.getLocation(), leftover);
        }
    }

    private ItemStack mobcatcheritem;
    private String mobcatcherprefix;
    private String mobcatchername;
    private List<String> mobcatcherlore;

    private void GiveMobCatcher(Player p, int amount) {
        p.sendMessage(mm.deserialize(mobcatcherprefix + "<reset>You have been given " + amount + " " + mobcatchername));
        ItemStack item = mobcatcheritem;
        item.setAmount(amount);
        dataManager.setdata(item, "catcher", UUID.randomUUID().toString());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<Component> lore = mobcatcherlore.stream()
                    .map(mm::deserialize)
                    .collect(Collectors.toList());
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        HashMap<Integer, ItemStack> leftovers = p.getInventory().addItem(item);
        for (ItemStack leftover : leftovers.values()) {
            p.getWorld().dropItemNaturally(p.getLocation(), leftover);
        }
    }

    public void setConfig(Configurations config) {

        this.config = config;
        this.prefix = config.prefix();
        this.modechange = config.ModeChangeMsg();
        cvitem = config.CVItem();

        cvitemname = config.CVItemName();
        cvitemlore = config.CVItemLore();

        ElevatorConfig elevatorConfig = plugin.getElevatorConfig();
        elevatorkit = elevatorConfig.item();
        elevatorprefix = elevatorConfig.prefix();
        elevatorlore = elevatorConfig.itemlore();
        elevatorname = elevatorConfig.itemname();

        MobcatcherConfig mobcatcherConfig = plugin.getMobCatcherConfig();
        mobcatcheritem = mobcatcherConfig.item();
        mobcatcherprefix = mobcatcherConfig.prefix();
        mobcatchername = mobcatcherConfig.itemname();
        mobcatcherlore = mobcatcherConfig.itemlore();


    }
}
