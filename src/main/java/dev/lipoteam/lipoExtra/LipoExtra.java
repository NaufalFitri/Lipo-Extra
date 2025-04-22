package dev.lipoteam.lipoExtra;

import de.tr7zw.changeme.nbtapi.NBT;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.lipoteam.lipoExtra.Commands.Commands;
import dev.lipoteam.lipoExtra.Commands.FightCommands;
import dev.lipoteam.lipoExtra.Discord.DiscordEvent;
import dev.lipoteam.lipoExtra.Events.*;
import dev.lipoteam.lipoExtra.Files.*;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.IOException;

public final class LipoHud extends JavaPlugin {

    private final ConsoleCommandSender consolesender = getServer().getConsoleSender();
    private Configurations config;
    private RamadanConfig ramadanconfig;
    private FightConfig fightConfig;
    private JobsConfig jobsConfig;
    private StockConfig stockConfig;
    private DiscordConfig discordConfig;
    private LevelConfig levelConfig;

    private Commands commands;
    private FightCommands fightCommands;

    private Event event;
    private Ramadan ramadanevent;
    private Fight fightevent;
    private Jobs jobsevent;
    private Stock stockevent;
    private DiscordEvent discordEvent;

    private BedrockForm form;

    private File ramadanfile;
    private File fightfile;
    private File jobsfile;
    private File stockfile;
    private File discordfile;
    private File levelfile;
    private FileConfiguration jobs;
    private FileConfiguration ramadan;
    private FileConfiguration fight;
    private FileConfiguration stock;
    private FileConfiguration discord;
    private FileConfiguration levelBound;

    private BukkitAudiences adventure;
    private Audience console;

    public @NonNull BukkitAudiences adventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(true).silentLogs(true));
    }

    @Override
    public void onEnable() {
        var mm = MiniMessage.miniMessage();
        DataManager dataManager = new DataManager(this);

        CommandAPI.onEnable();

        this.adventure = BukkitAudiences.create(this);
        console = adventure.console();

        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            saveDefaultConfig();
        }
        reloadConfig();

        createCustomConfigs();

        ramadanconfig = new RamadanConfig(ramadan, this);
        fightConfig = new FightConfig(fight, this);
        jobsConfig = new JobsConfig(jobs, this);
        stockConfig = new StockConfig(stock, this);
        discordConfig = new DiscordConfig(discord, this);
        config = new Configurations(getConfig(), this);

        dataManager.sendMessage(console, mm.deserialize(config.prefix() + "<white>Enabled"));

        if (!NBT.preloadApi()) {
            getLogger().warning("NBT-API wasn't initialized properly, disabling the plugin");
            getPluginLoader().disablePlugin(this);
            return;
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            RegisterEvents();
            PlaceholderAPIHook.registerHook();
        } else {
            getLogger().warning("Could not find PlaceholderAPI! This plugin is required."); //
            Bukkit.getPluginManager().disablePlugin(this);
        }

        RegisterCommands();

    }

    private void RegisterCommands() {
        commands = new Commands(config, this);
        fightCommands = new FightCommands(fightConfig, this);
    }

    private void RegisterEvents() {

        Configurations config = new Configurations(getConfig(), this);
        event = new Event(config, this);
        ramadanevent = new Ramadan(ramadanconfig, this);
        fightevent = new Fight(fightConfig, this);
        jobsevent = new Jobs(jobsConfig, this);
        discordEvent = new DiscordEvent(discordConfig, this);
        stockevent = new Stock(stockConfig, this);

        getServer().getPluginManager().registerEvents(event, this);
        getServer().getPluginManager().registerEvents(ramadanevent, this);
        getServer().getPluginManager().registerEvents(fightevent, this);
        getServer().getPluginManager().registerEvents(jobsevent, this);
        getServer().getPluginManager().registerEvents(stockevent, this);

        if (Bukkit.getPluginManager().isPluginEnabled("floodgate")) {
            form = new BedrockForm(config, this);
        }

    }

    public static LipoHud getInstance() {

        return getPlugin(LipoHud.class);

    }

    public void ReloadConfig() {

        this.reloadConfig();
        saveConfig();

        ramadan = YamlConfiguration.loadConfiguration(ramadanfile);
        fight = YamlConfiguration.loadConfiguration(fightfile);
        jobs = YamlConfiguration.loadConfiguration(jobsfile);
        stock = YamlConfiguration.loadConfiguration(stockfile);
        discord = YamlConfiguration.loadConfiguration(discordfile);

        try {
            ramadan.save(ramadanfile);
            fight.save(fightfile);
            jobs.save(jobsfile);
            stock.save(stockfile);
            discord.save(discordfile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        config = new Configurations(this.getConfig(), this);
        ramadanconfig = new RamadanConfig(ramadan, this);
        fightConfig = new FightConfig(fight, this);
        jobsConfig = new JobsConfig(jobs, this);
        stockConfig = new StockConfig(stock, this);
        discordConfig = new DiscordConfig(discord, this);

        ramadanevent.setConfig(ramadanconfig);
        event.setConfig(config);
        fightevent.setConfig(fightConfig);
        stockevent.setConfig(stockConfig);
        discordEvent.setConfig(discordConfig);
        jobsevent.setConfig(jobsConfig);

        commands.setConfig(config);
        fightCommands.setConfig(fightConfig);

        if (Bukkit.getPluginManager().isPluginEnabled("floodgate")) {
            form.setConfig(config);
        }

    }

    private void createCustomConfigs() {

        File eventsFolder = new File(getDataFolder(), "Events");
        File extrasFolder = new File(getDataFolder(), "Extras");

        ramadanfile = new File(eventsFolder, "ramadan.yml");
        fightfile = new File(extrasFolder, "fight.yml");
        jobsfile = new File(extrasFolder, "jobs.yml");
        stockfile = new File(extrasFolder, "stockmarket.yml");
        discordfile = new File(extrasFolder, "discord.yml");
        levelfile = new File(extrasFolder, "levelbound.yml");

        if (!ramadanfile.exists()) {
            ramadanfile.getParentFile().mkdirs();
            saveResource("Events/ramadan.yml", false);
        }
        if (!fightfile.exists()) {
            fightfile.getParentFile().mkdirs();
            saveResource("Extras/fight.yml", false);
        }
        if (!jobsfile.exists()) {
            jobsfile.getParentFile().mkdirs();
            saveResource("Extras/jobs.yml", false);
        }
        if (!stockfile.exists()) {
            stockfile.getParentFile().mkdirs();
            saveResource("Extras/stockmarket.yml", false);
        }
        if (!discordfile.exists()) {
            discordfile.getParentFile().mkdirs();
            saveResource("Extras/discord.yml", false);
        }
        if (!levelfile.exists()) {
            levelfile.getParentFile().mkdirs();
            saveResource("Extras/levelbound.yml", false);
        }

        levelBound = YamlConfiguration.loadConfiguration(levelfile);
        stock = YamlConfiguration.loadConfiguration(stockfile);
        jobs = YamlConfiguration.loadConfiguration(jobsfile);
        fight = YamlConfiguration.loadConfiguration(fightfile);
        ramadan = YamlConfiguration.loadConfiguration(ramadanfile);
        discord = YamlConfiguration.loadConfiguration(discordfile);

    }

    public Fight getFightevent() {
        return fightevent;
    }

    public Event getEvent() {
        return event;
    }

    @Override
    public void onDisable() {
        var mm = MiniMessage.miniMessage();
        DataManager dataManager = new DataManager(this);

        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }

        dataManager.sendMessage(console, mm.deserialize(config.prefix() + "<white>Disabled"));
    }


}
