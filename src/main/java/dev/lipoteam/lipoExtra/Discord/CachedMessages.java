package dev.lipoteam.lipoHud.Discord;

import java.util.ArrayList;
import java.util.List;

public class CachedMessages {

    private final List<String> permissionRoleId;
    private List<String> messages = new ArrayList<>();
    private List<String> commands = new ArrayList<>();
    private List<String> placeholders = new ArrayList<>();
    private String embedColor;
    private String embedUrl;
    private List<String> embedWords = new ArrayList<>();

    public CachedMessages(List<String> permissionRoleId) {
        this.permissionRoleId = permissionRoleId;
    }

    public List<String> getPermissionRoleId() {
        return permissionRoleId;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public void setPlaceholders(List<String> placeholders) {
        this.placeholders = placeholders;
    }

    public List<String> getPlaceholders() {
        return placeholders;
    }

    public String getEmbedColor() {
        return embedColor;
    }

    public void setEmbedColor(String embedColor) {
        this.embedColor = embedColor;
    }

    public String getEmbedUrl() {
        return embedUrl;
    }

    public void setEmbedUrl(String embedUrl) {
        this.embedUrl = embedUrl;
    }

    public List<String> getEmbedWords() {
        return embedWords;
    }

    public void setEmbedWords(List<String> embedWords) {
        this.embedWords = embedWords;
    }

}
