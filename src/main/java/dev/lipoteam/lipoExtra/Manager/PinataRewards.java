package dev.lipoteam.lipoExtra.Manager;

import dev.lipoteam.lipoExtra.Files.PinataConfig;
import dev.lipoteam.lipoExtra.LipoExtra;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.intellij.lang.annotations.Subst;

import java.util.List;

public class PinataRewards {

    private final String type;
    private final int chance;
    private final List<String> commands;
    private Sound sound;

    public PinataRewards(String type, int chance, List<String> commands, @Subst("") String sound) {
        this.type = type;
        this.chance = chance;
        this.commands = commands;
        if (!sound.isEmpty()) {
            this.sound = Sound.sound(Key.key(sound), Sound.Source.PLAYER,
                    (float) 1, (float) 1);
        }
    }

    public String getType() { return type; }

    public int getChance() { return chance; }

    public List<String> getCommands() { return commands; }

    public Sound getSound() {
        return sound;
    }

}
