package dev.lipoteam.lipoExtra.Commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.RegisteredCommand;
import dev.lipoteam.lipoExtra.Events.Leaderboard;
import dev.lipoteam.lipoExtra.Files.LeaderboardConfig;
import dev.lipoteam.lipoExtra.LipoExtra;
import dev.lipoteam.lipoExtra.Manager.LeaderboardManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BoardCommands {

    private LeaderboardConfig config;
    private final LipoExtra plugin;
    private final HashSet<String> listcommands = new HashSet<>();
    private final Leaderboard event;

    public BoardCommands(LeaderboardConfig config, LipoExtra plugin, Leaderboard event) {

        this.plugin = plugin;
        this.event = event;
        setConfig(config);

    }

    public void setConfig(LeaderboardConfig config) {
        this.config = config;

        for (String registeredcmd : listcommands) {
            CommandAPI.unregister(registeredcmd);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            listcommands.clear();
            createCommands();
        }, 2L);

    }

    private void createCommands() {

        for (LeaderboardManager gui : config.getListGui()) {
            CommandAPICommand command = new CommandAPICommand(gui.command())
                    .executes((sender, args) -> {
                        if (sender instanceof Player p) {

                            event.openGUI(p, 1, gui);
                        }
                    });
            listcommands.add(command.getName());
            command.register();
        }

    }

}
