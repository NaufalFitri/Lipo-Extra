package dev.lipoteam.lipoExtra.Manager;

import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

public record MobData(EntityType type,
                      String customName,
                      double health,// Optional if you spawn at clicked block
                      ItemStack helmet,
                      ItemStack chestplate,
                      ItemStack leggings,
                      ItemStack boots,
                      ItemStack mainHand,
                      ItemStack offHand,
                      Collection<PotionEffect> activeEffects,
                      boolean isBaby,
                      boolean isGlowing,
                      boolean isInvisible,
                      boolean ai,
                      boolean invulnerable,
                      boolean silent,
                      DyeColor color, // For sheep, shulkers, etc.
                      String catType, // For cats
                      Rabbit.Type rabbitType, // For rabbits
                      Horse.Color horseColor, // For horses
                      Horse.Style horseStyle,
                      Llama.Color llamacolor,
                      ItemStack llamadecor,
                      ItemStack[] llamastorage,
                      boolean havechest,
                      String wolfVariant,
                      UUID owner,
                      String frogVariant,
                      int size,
                      boolean saddled
                      ) implements Serializable {
}
