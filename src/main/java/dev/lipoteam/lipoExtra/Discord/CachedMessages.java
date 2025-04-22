package dev.lipoteam.lipoExtra.Discord;

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
    private boolean LockThread = false;
    private boolean UnlockThread = false;
    private String embedTitle;
    private String msgType;
    private String guild;
    private String imgUrl;

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

    public boolean getLockThread() { return this.LockThread; }

    public void setLockThread() { this.LockThread = true; }

    public void setUnlockThread() { this.UnlockThread = true; }

    public boolean getUnlockThread() { return this.UnlockThread; }

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

    public void setGuild(String guild) { this.guild = guild; }

    public String getGuild() { return guild; }

    public void setType(String type) { msgType = type; }

    public String getType() { return msgType; }

    public void setEmbedTitle(String title) { embedTitle = title; }

    public void setEmbedImgUrl(String url) { imgUrl = url; }

    public String getEmbedImgUrl() { return imgUrl; }

    public String getEmbedTitle() { return embedTitle; }

    public void setEmbedWords(List<String> embedWords) {
        this.embedWords = embedWords;
    }

}
