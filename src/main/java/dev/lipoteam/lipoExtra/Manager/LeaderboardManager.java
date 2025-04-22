package dev.lipoteam.lipoExtra.Manager;

import java.util.List;

public record LeaderboardManager(String type, String title, String currency, String itemmaterial, String itemname,
                                 List<String> itemlore, String command) {

}
