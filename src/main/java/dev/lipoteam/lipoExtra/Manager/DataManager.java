package dev.lipoteam.lipoExtra.Manager;

import com.jeff_media.customblockdata.CustomBlockData;
import dev.lipoteam.lipoExtra.LipoExtra;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataManager {

    private final LipoExtra plugin;

    public DataManager(LipoExtra plugin) {
        this.plugin = plugin;
    }

    public static Logger logger = Logger.getLogger("");

    public void setdata(Object what, String whatkey, Object data) {
        NamespacedKey key = new NamespacedKey(plugin, whatkey);

        if (what instanceof ItemStack item) {
            ItemMeta itemMeta = item.getItemMeta();

            if (!Objects.isNull(itemMeta)) {
                PersistentDataContainer itemData = itemMeta.getPersistentDataContainer();

                switch (data) {
                    case String s -> itemData.set(key, PersistentDataType.STRING, s);
                    case Integer i -> itemData.set(key, PersistentDataType.INTEGER, i);
                    case Boolean b -> itemData.set(key, PersistentDataType.BOOLEAN, b);
                    case Double d -> itemData.set(key, PersistentDataType.DOUBLE, d);
                    case null, default -> itemData.set(key, PersistentDataType.STRING, serialize(data));
                }
                item.setItemMeta(itemMeta);

            }
        } else if (what instanceof Player) {

            PersistentDataContainer playerData = ((Player) what).getPersistentDataContainer();
            switch (data) {
                case String s -> playerData.set(key, PersistentDataType.STRING, s);
                case Integer i -> playerData.set(key, PersistentDataType.INTEGER, i);
                case Boolean b -> playerData.set(key, PersistentDataType.BOOLEAN, b);
                case Double d -> playerData.set(key, PersistentDataType.DOUBLE, d);
                case null, default -> playerData.set(key, PersistentDataType.STRING, serialize(data));
            }

        } else if (what instanceof Block) {
            PersistentDataContainer blockData = new CustomBlockData((Block) what, plugin);
            switch (data) {
                case String s -> blockData.set(key, PersistentDataType.STRING, s);
                case Integer i -> blockData.set(key, PersistentDataType.INTEGER, i);
                case Boolean b -> blockData.set(key, PersistentDataType.BOOLEAN, b);
                case Double d -> blockData.set(key, PersistentDataType.DOUBLE, d);
                case null, default -> blockData.set(key, PersistentDataType.STRING, serialize(data));
            }
        } else if (what instanceof Entity) {
            PersistentDataContainer entityData = ((Entity) what).getPersistentDataContainer();
            switch (data) {
                case String s -> entityData.set(key, PersistentDataType.STRING, s);
                case Integer i -> entityData.set(key, PersistentDataType.INTEGER, i);
                case Boolean b -> entityData.set(key, PersistentDataType.BOOLEAN, b);
                case Double d -> entityData.set(key, PersistentDataType.DOUBLE, d);
                case null, default -> entityData.set(key, PersistentDataType.STRING, serialize(data));
            }
        }

    }

    public void unsetdata(Object what, String whatkey) {
        NamespacedKey key = new NamespacedKey(plugin, whatkey);

        if (what instanceof ItemStack item) {
            ItemMeta itemMeta = item.getItemMeta();

            if (!Objects.isNull(itemMeta)) {
                PersistentDataContainer itemData = itemMeta.getPersistentDataContainer();

                itemData.remove(key);
                item.setItemMeta(itemMeta);

            }
        } else if (what instanceof Player p) {

            PersistentDataContainer playerData = p.getPersistentDataContainer();
            playerData.remove(key);

        } else if (what instanceof Block b) {

            PersistentDataContainer blockData = new CustomBlockData(b, plugin);
            blockData.remove(key);
        }

    }

    public Object getdata(Object what, String whatkey, boolean deep) {
        NamespacedKey key = new NamespacedKey(plugin, whatkey);
        Object thedata = null;

        if (what instanceof ItemStack item) {
            ItemMeta itemMeta = item.getItemMeta();
            if (!Objects.isNull(itemMeta)) {
                PersistentDataContainer itemData = itemMeta.getPersistentDataContainer();

                if (itemData.has(key, PersistentDataType.STRING)) {
                    thedata = itemData.get(key, PersistentDataType.STRING);

                    if (deep) {
                        thedata = deserialize((String) thedata);
                    }

                } else if (itemData.has(key, PersistentDataType.INTEGER)) {
                    thedata = itemData.get(key, PersistentDataType.INTEGER);
                } else if (itemData.has(key, PersistentDataType.BOOLEAN)) {
                    thedata = itemData.get(key, PersistentDataType.BOOLEAN);
                } else if (itemData.has(key, PersistentDataType.DOUBLE)) {
                    thedata = itemData.get(key, PersistentDataType.DOUBLE);
                }
            }

        } else if (what instanceof Player p) {
            PersistentDataContainer playerData = p.getPersistentDataContainer();

            if (playerData.has(key, PersistentDataType.STRING)) {
                thedata = playerData.get(key, PersistentDataType.STRING);

                if (deep) {
                    thedata = deserialize((String) thedata);
                }

            } else if (playerData.has(key, PersistentDataType.INTEGER)) {
                thedata = playerData.get(key, PersistentDataType.INTEGER);
            } else if (playerData.has(key, PersistentDataType.BOOLEAN)) {
                thedata = playerData.get(key, PersistentDataType.BOOLEAN);
            } else if (playerData.has(key, PersistentDataType.DOUBLE)) {
                thedata = playerData.get(key, PersistentDataType.DOUBLE);
            }

        } else if (what instanceof Block b) {

            PersistentDataContainer blockData = new CustomBlockData(b, plugin);

            if (blockData.has(key, PersistentDataType.STRING)) {
                thedata = blockData.get(key, PersistentDataType.STRING);

                if (deep) {
                    thedata = deserialize((String) thedata);
                }

            } else if (blockData.has(key, PersistentDataType.INTEGER)) {
                thedata = blockData.get(key, PersistentDataType.INTEGER);
            } else if (blockData.has(key, PersistentDataType.BOOLEAN)) {
                thedata = blockData.get(key, PersistentDataType.BOOLEAN);
            } else if (blockData.has(key, PersistentDataType.DOUBLE)) {
                thedata = blockData.get(key, PersistentDataType.DOUBLE);
            }

        }
        return thedata;
    }

    public boolean hasData(Object what, String keyvalue) {

        NamespacedKey key = new NamespacedKey(plugin, keyvalue);

        if (what instanceof ItemStack i) {
            if (i.hasItemMeta()) {
                ItemMeta whatMeta = ((ItemStack) what).getItemMeta();
                assert whatMeta != null;
                PersistentDataContainer data = whatMeta.getPersistentDataContainer();
                return data.has(key);
            }
        } else if (what instanceof Player p) {
            PersistentDataContainer data = p.getPersistentDataContainer();
            return data.has(key);
        } else if (what instanceof Block b) {
            PersistentDataContainer data = new CustomBlockData(b, plugin);
            return data.has(key);
        }

        return false;
    }

    public void CalcData(Object what, String whatkey, double num, String operation) {

        Object data = getdata(what, whatkey, false);
        switch (operation) {
            case "+" -> setdata(what, whatkey, (double) data + num);
            case "-" -> setdata(what, whatkey, (double) data - num);
            case "*" -> setdata(what, whatkey, (double) data * num);
            case "/" -> setdata(what, whatkey, (double) data / num);
        }

    }

    public String serialize(Object item) {

        String encodedObject;

        try {

            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);
            os.writeObject(item);
            os.flush();

            byte[] serializedObject = io.toByteArray();

            encodedObject = new String(Base64.getEncoder().encode(serializedObject));

            return encodedObject;


        } catch (IOException ex) {
            logger.warning(ex.toString());
        }

        return null;
    }

    public Object deserialize(String encoded) {

        try {

            byte[] serializedObject;
            serializedObject = Base64.getDecoder().decode(encoded);
            ByteArrayInputStream in = new ByteArrayInputStream(serializedObject);

            BukkitObjectInputStream is = new BukkitObjectInputStream(in);

            return is.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            logger.warning(ex.toString());
        }
        return null;
    }

    public String hex(String message) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public void sendMessage(Player player, Component message) {
        plugin.adventure().player(player).sendMessage(message);
    }

    public void sendMessage(Audience player, Component message) {
        player.sendMessage(message);
    }

    public void sendMessage(Component message, Player... players) {
        for (Player player: players) {
            plugin.adventure().player(player).sendMessage(message);
        }
    }

    public void sendMessage(Component message, Audience... players) {
        for (Audience player: players) {
            player.sendMessage(message);
        }
    }

    public void sendActionbar(Component message, Player... player) {
        for (Player p : player) {
            plugin.adventure().player(p).sendActionBar(message);
        }
    }

    public void sendTitle(Component title, Component subtitle, long fadein, long stay, long fadeout, Audience... player) {

        final Title.Times times = Title.Times.times(Duration.ofMillis(fadein*50), Duration.ofMillis(stay*50), Duration.ofMillis(fadeout*50));
        final Title thetitle = Title.title(title, subtitle, times);

        for (Audience p : player) {
            p.showTitle(thetitle);
        }
    }

    public void sendTitle(Component title, Component subtitle, long fadein, long stay, long fadeout, Player... player) {

        final Title.Times times = Title.Times.times(Duration.ofMillis(fadein*50), Duration.ofMillis(stay*50), Duration.ofMillis(fadeout*50));
        final Title thetitle = Title.title(title, subtitle, times);

        for (Player p : player) {
            plugin.adventure().player(p).showTitle(thetitle);
        }
    }

}
