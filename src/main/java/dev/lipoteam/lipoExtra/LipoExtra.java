package dev.lipoteam.lipoExtra;

import com.griefdefender.api.Core;
import com.griefdefender.api.GriefDefender;
import com.jeff_media.customblockdata.CustomBlockData;

import de.tr7zw.changeme.nbtapi.NBT;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.lipoteam.lipoExtra.Commands.*;
import dev.lipoteam.lipoExtra.Discord.DiscordEvent;
import dev.lipoteam.lipoExtra.Events.*;
import dev.lipoteam.lipoExtra.Files.*;
import dev.lipoteam.lipoExtra.Manager.DataManager;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.nullness.qual.NonNull;
import se.file14.procosmetics.ProCosmetics;
import se.file14.procosmetics.api.ProCosmeticsProvider;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public final class LipoExtra extends JavaPlugin {

    private final ConsoleCommandSender consolesender = getServer().getConsoleSender();
    private Configurations config;
    private RamadanConfig ramadanconfig;
    private FightConfig fightConfig;
    private JobsConfig jobsConfig;
    private StockConfig stockConfig;
    private DiscordConfig discordConfig;
    private LevelConfig levelConfig;
    private PinataConfig pinataConfig;
    private LeaderboardConfig leaderboardConfig;
    private AntixrayConfig antixrayConfig;
    private TagConfig tagConfig;
    private SpawnerConfig spawnerConfig;
    private TikTokConfig tikTokConfig;
    private ElevatorConfig elevatorConfig;
    private MobcatcherConfig mobCatcherConfig;

    private Commands commands;
    private FightCommands fightCommands;
    private PinataCommands pinataCommands;
    private BoardCommands boardCommands;
    private BetaCommands betaCommands;
    private TagCommands tagCommands;
    private TiktokCommands tiktokCommands;

    private Event event;
    private Ramadan ramadanevent;
    private Fight fightevent;
    private Jobs jobsevent;
    private Stock stockevent;
    private DiscordEvent discordEvent;
    private LevelBound levelEvent;
    private Pinata pinataEvent;
    private Leaderboard leaderboardevent;
    private Antixray xrayEvent;
    private Spawner spawnerEvent;
    private Elevator elevatorEvent;
    private Mobcatcher mobcatcherEvent;

    private BedrockForm form;

    private File ramadanfile;
    private File fightfile;
    private File jobsfile;
    private File stockfile;
    private File discordfile;
    private File levelfile;
    private File pinatafile;
    private File songs;
    private File betaitems;
    private File coinsfile;
    private File ranksfile;
    private File boardfile;
    private File xrayfile;
    private File tiktokfile;
    private File tagfile;
    private File spawnerfile;
    private File elevatorfile;
    private File mobfile;
    private FileConfiguration coins;
    private FileConfiguration ranks;
    private FileConfiguration jobs;
    private FileConfiguration ramadan;
    private FileConfiguration fight;
    private FileConfiguration stock;
    private FileConfiguration discord;
    private FileConfiguration levelBound;
    private FileConfiguration pinata;
    private FileConfiguration leaderboard;
    private FileConfiguration antixray;
    private FileConfiguration tiktok;
    private FileConfiguration tags;
    private FileConfiguration spawner;
    private FileConfiguration elevator;
    private FileConfiguration mobcatcher;

    private static DataManager dataManager;
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

//        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
//        if (service != null) {
//            voicechatPlugin = new ExampleVoicechatPlugin();
//            service.registerPlugin(voicechatPlugin);
//            getLogger().info("Successfully registered example plugin");
//        } else {
//            getLogger().info("Failed to register example plugin");
//        }

        CustomBlockData.registerListener(this);

        var mm = MiniMessage.miniMessage();
        dataManager = new DataManager(this);

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
        levelConfig = new LevelConfig(levelBound, this);
        pinataConfig = new PinataConfig(pinata, this);
        leaderboardConfig = new LeaderboardConfig(leaderboard, this);
        tagConfig = new TagConfig(tags, this);
        antixrayConfig = new AntixrayConfig(this, antixray);
        spawnerConfig = new SpawnerConfig(spawner, this);
        tikTokConfig = new TikTokConfig(tiktok, this);
        elevatorConfig = new ElevatorConfig(elevator, this);
        mobCatcherConfig = new MobcatcherConfig(mobcatcher, this);

        config = new Configurations(getConfig(), this);

        if (config.NoteBlockAPIHook()) {
            if (!Bukkit.getPluginManager().isPluginEnabled("NoteBlockAPI")){
                getLogger().severe("*** NoteBlockAPI is not installed or not enabled. ***");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }

        if (config.ProcosmeticsHook()) {
            if (Bukkit.getPluginManager().getPlugin("CoinsEngine") == null) {
                getLogger().severe("CoinsEngine is missing! Disabling plugin...");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            ProcosmeticsHook();
        }

        if (!NBT.preloadApi()) {
            getLogger().warning("NBT-API wasn't initialized properly, disabling the plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            RegisterEvents();
            PlaceholderAPIHook.registerHook(config, this);
        } else {
            getLogger().warning("Could not find PlaceholderAPI! This plugin is required."); //
            Bukkit.getPluginManager().disablePlugin(this);
        }

        RegisterCommands();

        dataManager.sendMessage(console, mm.deserialize(config.prefix() + "<white>Enabled"));

    }

    private void RegisterCommands() {
        commands = new Commands(config, this);
        fightCommands = new FightCommands(fightConfig, this);
        pinataCommands = new PinataCommands(pinataConfig, this);
        boardCommands = new BoardCommands(leaderboardConfig, this, leaderboardevent);
        betaCommands = new BetaCommands(config, this, betaitems);
        tagCommands = new TagCommands(tagConfig, this);
        tiktokCommands = new TiktokCommands(tikTokConfig, this);
    }

    private void RegisterEvents() {

        Configurations config = new Configurations(getConfig(), this);
        event = new Event(config, this);
        ramadanevent = new Ramadan(ramadanconfig, this);
        fightevent = new Fight(fightConfig, this);
        jobsevent = new Jobs(jobsConfig, this);
        discordEvent = new DiscordEvent(discordConfig, this);
        stockevent = new Stock(stockConfig, this);
        levelEvent = new LevelBound(levelConfig, this);
        pinataEvent = new Pinata(pinataConfig, this);
        leaderboardevent = new Leaderboard(leaderboardConfig, this);
        xrayEvent = new Antixray(this, antixrayConfig);
        spawnerEvent = new Spawner(spawnerConfig, this);
        elevatorEvent = new Elevator(elevatorConfig, this);
        mobcatcherEvent = new Mobcatcher(mobCatcherConfig, this);

        getServer().getPluginManager().registerEvents(event, this);
        getServer().getPluginManager().registerEvents(ramadanevent, this);
        getServer().getPluginManager().registerEvents(fightevent, this);
        getServer().getPluginManager().registerEvents(jobsevent, this);
        getServer().getPluginManager().registerEvents(stockevent, this);
        getServer().getPluginManager().registerEvents(levelEvent, this);
        getServer().getPluginManager().registerEvents(pinataEvent, this);
        getServer().getPluginManager().registerEvents(leaderboardevent, this);
        getServer().getPluginManager().registerEvents(xrayEvent, this);
        getServer().getPluginManager().registerEvents(spawnerEvent, this);
        getServer().getPluginManager().registerEvents(elevatorEvent, this);
        getServer().getPluginManager().registerEvents(mobcatcherEvent, this);

        if (Bukkit.getPluginManager().isPluginEnabled("floodgate")) {
            form = new BedrockForm(config, this);
        }

    }

    public static LipoExtra getInstance() {

        return getPlugin(LipoExtra.class);

    }

    public void ReloadConfig() {

        this.reloadConfig();
        saveConfig();

        ramadan = YamlConfiguration.loadConfiguration(ramadanfile);
        fight = YamlConfiguration.loadConfiguration(fightfile);
        jobs = YamlConfiguration.loadConfiguration(jobsfile);
        stock = YamlConfiguration.loadConfiguration(stockfile);
        discord = YamlConfiguration.loadConfiguration(discordfile);
        levelBound = YamlConfiguration.loadConfiguration(levelfile);
        pinata = YamlConfiguration.loadConfiguration(pinatafile);
        coins = YamlConfiguration.loadConfiguration(coinsfile);
        ranks = YamlConfiguration.loadConfiguration(ranksfile);
        leaderboard = YamlConfiguration.loadConfiguration(boardfile);
        antixray = YamlConfiguration.loadConfiguration(xrayfile);
        tags = YamlConfiguration.loadConfiguration(tagfile);
        spawner = YamlConfiguration.loadConfiguration(spawnerfile);
        tiktok = YamlConfiguration.loadConfiguration(tiktokfile);
        elevator = YamlConfiguration.loadConfiguration(elevatorfile);
        mobcatcher = YamlConfiguration.loadConfiguration(mobfile);

        try {
            ramadan.save(ramadanfile);
            fight.save(fightfile);
            jobs.save(jobsfile);
            stock.save(stockfile);
            discord.save(discordfile);
            levelBound.save(levelfile);
            pinata.save(pinatafile);
            coins.save(coinsfile);
            ranks.save(ranksfile);
            leaderboard.save(boardfile);
            antixray.save(xrayfile);
            tags.save(tagfile);
            spawner.save(spawnerfile);
            tiktok.save(tiktokfile);
            elevator.save(elevatorfile);
            mobcatcher.save(mobfile);
        } catch (IOException e) {
            getLogger().info(e.getMessage());
        }

        config = new Configurations(this.getConfig(), this);
        ramadanconfig = new RamadanConfig(ramadan, this);
        fightConfig = new FightConfig(fight, this);
        jobsConfig = new JobsConfig(jobs, this);
        stockConfig = new StockConfig(stock, this);
        discordConfig = new DiscordConfig(discord, this);
        levelConfig = new LevelConfig(levelBound, this);
        pinataConfig = new PinataConfig(pinata, this);
        leaderboardConfig = new LeaderboardConfig(leaderboard, this);
        antixrayConfig = new AntixrayConfig(this, antixray);
        tagConfig = new TagConfig(tags, this);
        spawnerConfig = new SpawnerConfig(spawner, this);
        tikTokConfig = new TikTokConfig(tiktok, this);
        elevatorConfig = new ElevatorConfig(elevator, this);
        mobCatcherConfig = new MobcatcherConfig(mobcatcher, this);

        ramadanevent.setConfig(ramadanconfig);
        event.setConfig(config);
        fightevent.setConfig(fightConfig);
        stockevent.setConfig(stockConfig);
        discordEvent.setConfig(discordConfig);
        jobsevent.setConfig(jobsConfig);
        levelEvent.setConfig(levelConfig);
        pinataEvent.setConfig(pinataConfig);
        leaderboardevent.setConfig(leaderboardConfig);
        xrayEvent.setConfig(antixrayConfig);
        spawnerEvent.setConfig(spawnerConfig);
        elevatorEvent.setConfig(elevatorConfig);
        mobcatcherEvent.setConfig(mobCatcherConfig);

        commands.setConfig(config);
        fightCommands.setConfig(fightConfig);
        pinataCommands.setConfig(pinataConfig);
        boardCommands.setConfig(leaderboardConfig);
        betaCommands.setConfig(config);
        tagCommands.setConfig(tagConfig);
        tiktokCommands.setConfig(tikTokConfig);

        if (Bukkit.getPluginManager().isPluginEnabled("floodgate")) {
            form.setConfig(config);
        }

        PlaceholderAPIHook.setConfig(config);

    }

    private void createCustomConfigs() {

        File eventsFolder = new File(getDataFolder(), "Events");
        File extrasFolder = new File(getDataFolder(), "Extras");
        File songsFolder = new File(getDataFolder(), "Songs");
        File fetchFolder = new File(getDataFolder(), "Fetches");
        betaitems = new File(getDataFolder(), "BetaItems");

        if (!songsFolder.exists()) {
            songsFolder.getParentFile().mkdirs();
            saveResource("Songs", false);
        }

        ramadanfile = new File(eventsFolder, "ramadan.yml");
        fightfile = new File(extrasFolder, "fight.yml");
        jobsfile = new File(extrasFolder, "jobs.yml");
        stockfile = new File(extrasFolder, "stockmarket.yml");
        discordfile = new File(extrasFolder, "discord.yml");
        levelfile = new File(extrasFolder, "levelbound.yml");
        pinatafile = new File(extrasFolder, "pinata.yml");
        coinsfile = new File(fetchFolder, "coins.yml");
        ranksfile = new File(fetchFolder, "ranks.yml");
        boardfile = new File(extrasFolder, "leaderboardgui.yml");
        xrayfile = new File(extrasFolder, "antixray.yml");
        tagfile = new File(extrasFolder, "tag.yml");
        spawnerfile = new File(extrasFolder, "spawner.yml");
        tiktokfile = new File(extrasFolder, "tiktok.yml");
        elevatorfile = new File(extrasFolder, "elevator.yml");
        mobfile = new File(extrasFolder, "mobcatcher.yml");

        if (!mobfile.exists()) {
            mobfile.getParentFile().mkdirs();
            saveResource("Extras/mobcatcher.yml", false);
        }
        if (!elevatorfile.exists()) {
            elevatorfile.getParentFile().mkdirs();
            saveResource("Extras/elevator.yml", false);
        }
        if (!tiktokfile.exists()) {
            tiktokfile.getParentFile().mkdirs();
            saveResource("Extras/tiktok.yml", false);
        }
        if (!spawnerfile.exists()) {
            spawnerfile.getParentFile().mkdirs();
            saveResource("Extras/spawner.yml", false);
        }
        if (!betaitems.exists()) {
            betaitems.mkdirs();
        }
        if (!tagfile.exists()) {
            tagfile.getParentFile().mkdirs();
            saveResource("Extras/tag.yml", false);
        }
        if (!xrayfile.exists()) {
            xrayfile.getParentFile().mkdirs();
            saveResource("Extras/antixray.yml", false);
        }
        if (!coinsfile.exists()) {
            coinsfile.getParentFile().mkdirs();
            saveResource("Fetches/coins.yml", false);
        }
        if (!ranksfile.exists()) {
            ranksfile.getParentFile().mkdirs();
            saveResource("Fetches/ranks.yml", false);
        }
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
        if (!levelfile.exists()) {
            levelfile.getParentFile().mkdirs();
            saveResource("Extras/levelbound.yml", false);
        }
        if (!pinatafile.exists()) {
            pinatafile.getParentFile().mkdirs();
            saveResource("Extras/pinata.yml", false);
        }
        if (!boardfile.exists()) {
            boardfile.getParentFile().mkdirs();
            saveResource("Extras/leaderboardgui.yml", false);
        }

        spawner = YamlConfiguration.loadConfiguration(spawnerfile);
        levelBound = YamlConfiguration.loadConfiguration(levelfile);
        stock = YamlConfiguration.loadConfiguration(stockfile);
        jobs = YamlConfiguration.loadConfiguration(jobsfile);
        fight = YamlConfiguration.loadConfiguration(fightfile);
        ramadan = YamlConfiguration.loadConfiguration(ramadanfile);
        discord = YamlConfiguration.loadConfiguration(discordfile);
        pinata = YamlConfiguration.loadConfiguration(pinatafile);
        coins = YamlConfiguration.loadConfiguration(coinsfile);
        ranks = YamlConfiguration.loadConfiguration(ranksfile);
        leaderboard = YamlConfiguration.loadConfiguration(boardfile);
        antixray = YamlConfiguration.loadConfiguration(xrayfile);
        tags = YamlConfiguration.loadConfiguration(tagfile);
        tiktok = YamlConfiguration.loadConfiguration(tiktokfile);
        elevator = YamlConfiguration.loadConfiguration(elevatorfile);
        mobcatcher = YamlConfiguration.loadConfiguration(mobfile);

    }

    public void ProcosmeticsHook() {
        var mm = MiniMessage.miniMessage();
        ProCosmetics procosmetics = ProCosmeticsProvider.get();

        procosmetics.getEconomyManager().register(new EconomyProvider(this, config));
        dataManager.sendMessage(console, mm.deserialize(config.prefix() + "<white>Procosmetics Hook Enabled"));
    }

    public Fight getFightevent() {
        return fightevent;
    }

    public Event getEvent() {
        return event;
    }

    public FileConfiguration CoinsConfig() {
        return coins;
    }

    public FileConfiguration RanksConfig() {
        return ranks;
    }

    public ElevatorConfig getElevatorConfig() { return elevatorConfig; }

    public MobcatcherConfig getMobCatcherConfig() { return mobCatcherConfig; }

    public TagConfig getTagConfig() {
        return tagConfig;
    }

    public File getSongFolder() { return songs; }

    @Override
    public void onDisable() {
        pinataConfig.setCurrentVote(Pinata.voteCurrent);

        Map<Location, BukkitTask> tasks = new HashMap<>(spawnerEvent.activeTasks);

        tasks.values().forEach(BukkitTask::cancel);

        for (Location spawnerloc : tasks.keySet()) {
            Block block = spawnerloc.getBlock();
            if (dataManager.hasData(block, "display0")) {
                spawnerEvent.deactivateSpawner(block);
            }
        }

        var mm = MiniMessage.miniMessage();
        DataManager dataManager = new DataManager(this);

        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }

        dataManager.sendMessage(console, mm.deserialize(config.prefix() + "<white>Disabled"));
    }

    public CoreProtectAPI getCoreProtect() {
        Plugin plugin = getServer().getPluginManager().getPlugin("CoreProtect");

        // Check that CoreProtect is loaded
        if (!(plugin instanceof CoreProtect)) {
            return null;
        }

        // Check that the API is enabled
        CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
        if (!CoreProtect.isEnabled()) {
            return null;
        }

        // Check that a compatible version of the API is loaded
        if (CoreProtect.APIVersion() < 10) {
            return null;
        }

        return CoreProtect;
    }

    public Core getGriefDefender() {

        Plugin plugin = getServer().getPluginManager().getPlugin("GriefDefender");

        // Check that CoreProtect is loaded
        if (plugin == null) {
            return null;
        }

        return GriefDefender.getCore();

    }


}
