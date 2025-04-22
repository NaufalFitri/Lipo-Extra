package dev.lipoteam.lipoExtra.Files;

import dev.lipoteam.lipoExtra.Discord.CachedMessages;
import dev.lipoteam.lipoExtra.LipoExtra;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscordConfig {

    private final LipoExtra plugin;
    private FileConfiguration config;

    private final Map<String, Map<String, CachedMessages>> messages = new HashMap<>();

    public DiscordConfig(FileConfiguration config, LipoExtra plugin) {

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
                    } else if ("lockthread".equalsIgnoreCase(type)) {
                        cachedMessage.setLockThread();
                    } else if ("unlockthread".equalsIgnoreCase(type)) {
                        cachedMessage.setUnlockThread();
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
