package dev.lipoteam.lipoHud.Files;

import dev.lipoteam.lipoHud.Discord.CachedMessages;
import dev.lipoteam.lipoHud.LipoHud;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscordConfig {

    private final LipoHud plugin;
    private FileConfiguration config;

    private final Map<String, Map<String, CachedMessages>> messages = new HashMap<>();

    public DiscordConfig(FileConfiguration config, LipoHud plugin) {

        this.plugin = plugin;
        this.config = config;
        MessagesReceived();

    }

    public String Token() {
        return config.getString("token");
    }

    public boolean Enabled() {
        return config.getBoolean("enabled");
    }

    public void MessagesReceived() {
        List<Map<?, ?>> messageGroups = config.getMapList("message-received");

        for (Map<?, ?> group : messageGroups) {
            String symbol = (String) group.get("symbol");
            if (symbol == null) continue;

            Map<String, CachedMessages> symbolMessages = new HashMap<>();
            List<Map<?, ?>> messagesList = (List<Map<?, ?>>) group.get("messages");

            for (Map<?, ?> messageData : messagesList) {
                String contains = (String) messageData.get("contains");
                List<String> permissionRoleId = (List<String>) messageData.get("permission-role-id");

                List<Map<?, ?>> replies = (List<Map<?, ?>>) messageData.get("reply");
                CachedMessages cachedMessage = new CachedMessages(permissionRoleId);

                for (Map<?, ?> reply : replies) {
                    String type = (String) reply.get("type");

                    if ("messages".equalsIgnoreCase(type)) {
                        cachedMessage.setMessages((List<String>) reply.get("words"));
                    } else if ("embed".equalsIgnoreCase(type)) {
                        cachedMessage.setEmbedColor((String) reply.get("color"));
                        cachedMessage.setEmbedUrl((String) reply.get("url"));
                        cachedMessage.setEmbedWords((List<String>) reply.get("words"));
                    } else if ("commands".equalsIgnoreCase(type)) {
                        cachedMessage.setPlaceholders((List<String>) reply.get("placeholders"));
                        cachedMessage.setCommands((List<String>) reply.get("commands"));
                    }
                }

                symbolMessages.put(contains.toLowerCase(), cachedMessage);
            }
            messages.put(symbol, symbolMessages);
        }
    }

    public CachedMessages getMessage(String symbol, String input) {
        return messages.containsKey(symbol) ? messages.get(symbol).get(input.toLowerCase()) : null;
    }

}
