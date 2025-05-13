package dev.lipoteam.lipoExtra.Events;

import com.griefdefender.api.Core;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import dev.lipoteam.lipoExtra.Files.MobcatcherConfig;
import dev.lipoteam.lipoExtra.LipoExtra;
import dev.lipoteam.lipoExtra.Manager.DataManager;
import dev.lipoteam.lipoExtra.Manager.MobData;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class Mobcatcher implements Listener {

    private final LipoExtra plugin;
    private MobcatcherConfig config;
    private final DataManager dataManager;
    private Random random;
    private boolean enabled;
    private String prefix;
    private String catched;
    private String failed;
    private String claimmsg;
    private List<String> moblore;
    private double chances;
    private final MiniMessage mm;
    private int reset;
    private final CoreProtectAPI coreapi;
    private final Core gdapi;

    public Mobcatcher(MobcatcherConfig config, LipoExtra plugin) {

        this.plugin = plugin;
        mm = MiniMessage.miniMessage();
        dataManager = new DataManager(plugin);
        random = new Random();
        coreapi = plugin.getCoreProtect();
        gdapi = plugin.getGriefDefender();
        setConfig(config);

    }

    public void setConfig(MobcatcherConfig config) {
        this.config = config;
        enabled = config.enabled();
        prefix = config.prefix();
        catched = config.mobcatch();
        failed = config.mobfail();
        chances = config.chances();
        reset = config.resetuncatch();
        claimmsg = config.claim();
        moblore = config.itemlore();
    }

    @EventHandler
    private void InteractEntity(PlayerInteractEntityEvent e) {
        if (!enabled) return;

        if (e.getRightClicked() instanceof LivingEntity l && e.getHand() == EquipmentSlot.HAND) {

            if (e.getRightClicked().getType().equals(EntityType.PLAYER)) return;
            if (l instanceof Monster) return;

            Player p = e.getPlayer();
            ItemStack item = p.getInventory().getItemInMainHand();
            Entity en = e.getRightClicked();

            if (gdapi != null) {
                final Claim claim = gdapi.getClaimAt(en.getLocation());

                if (claim != null && !claim.isWilderness()) {
                    if (!claim.getOwnerUniqueId().equals(p.getUniqueId()) &&
                            !claim.getUserTrusts().contains(p.getUniqueId())) {
                        p.sendMessage(mm.deserialize(claimmsg.replace("[prefix]", prefix)));
                        return;
                    }
                }
            }

            if (en instanceof Tameable t) {
                if (!Objects.equals(t.getOwnerUniqueId(), p.getUniqueId())) {
                    return;
                }
            }

            if (en.hasMetadata("catched")) return;

            if (dataManager.hasData(item, "catcher") && !dataManager.hasData(item, "mob")) {

                double ran = random .nextDouble();

                if (ran > chances) {
                    p.sendMessage(mm.deserialize(failed.replace("[prefix]", prefix).replace("[mob]", en.getType().name())));
                    en.setMetadata("catched", new FixedMetadataValue(plugin, true));
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {

                        if (en.isValid()) {
                            en.removeMetadata("catched", plugin);
                        }

                    }, reset * 20L * 60);
                    return;
                }

                e.setCancelled(true);

                if (ran <= chances) {

                    dataManager.setdata(item, "mob", true);
                    boolean invulnerable = en.isInvulnerable();
                    en.setInvulnerable(true);
                    en.setMetadata("catched", new FixedMetadataValue(plugin, true));
                    double[] tick = new double[]{1.0};

                    Bukkit.getScheduler().runTaskTimer(plugin, t -> {

                        if (tick[0] <= 0) {
                            if (!l.isDead()) {
                                boolean baby = false;
                                if (l instanceof Ageable a) {
                                    baby = !a.isAdult();
                                }
                                DyeColor color = null;
                                boolean saddled = false;
                                if (l instanceof Steerable st) {
                                    saddled = st.hasSaddle();
                                }
                                if (l instanceof Sheep s) {
                                    color = s.getColor();
                                }
                                UUID owner = null;
                                String cattype = null;
                                if (l instanceof Cat c) {
                                    cattype = c.getCatType().key().asString();
                                    owner = c.getOwnerUniqueId();
                                }
                                Rabbit.Type rabbittype = null;
                                if (l instanceof Rabbit r) {
                                    rabbittype = r.getRabbitType();
                                }
                                Horse.Color horsecolor = null;
                                Horse.Style horsestyle = null;
                                if (l instanceof Horse h) {
                                    horsecolor = h.getColor();
                                    horsestyle = h.getStyle();
                                    owner = h.getOwnerUniqueId();
                                }
                                Llama.Color lcolor = null;
                                ItemStack carpet = null;
                                boolean chest = false;
                                int invsize = 0;
                                ItemStack[] contents = null;
                                if (l instanceof Llama ll) {
                                    lcolor = ll.getColor();
                                    owner = ll.getOwnerUniqueId();
                                    carpet = ll.getInventory().getDecor();
                                    chest = ll.isCarryingChest();
                                    contents = ll.getInventory().getStorageContents();
                                    invsize = ll.getInventory().getSize();
                                }
                                String variant = null;
                                if (l instanceof Wolf w) {
                                    variant = w.getVariant().key().asString();
                                    owner = w.getOwnerUniqueId();
                                }
                                String fvariant = null;
                                if (l instanceof Frog f) {
                                    fvariant = f.getVariant().key().asString();
                                }
                                ItemStack helmet = null;
                                ItemStack chestplate = null;
                                ItemStack leggings = null;
                                ItemStack boots = null;
                                ItemStack mainhand = null;
                                ItemStack offhand = null;
                                if (l.getEquipment() != null) {
                                    helmet = l.getEquipment().getHelmet();
                                    chestplate = l.getEquipment().getChestplate();
                                    leggings = l.getEquipment().getLeggings();
                                    boots = l.getEquipment().getBoots();
                                    mainhand = l.getEquipment().getItemInMainHand();
                                    offhand = l.getEquipment().getItemInOffHand();
                                }
                                int size = 0;
                                if (l instanceof Slime s) {
                                    size = s.getSize();
                                }
                                dataManager.setdata(item, "mob", new MobData(l.getType(), l.getCustomName(), l.getHealth(), helmet, chestplate
                                        , leggings, boots, mainhand, offhand, l.getActivePotionEffects(),
                                        baby, l.isGlowing(), l.isInvisible(), l.hasAI(), invulnerable, l.isSilent(), color, cattype, rabbittype, horsecolor,
                                        horsestyle, lcolor, carpet, contents, chest, invsize, variant, owner, fvariant, size, saddled));
                                try {
                                    l.remove();
                                } catch (Exception ex) {
                                    plugin.getLogger().warning(p.getName() + " Unable to catch a " + en.getType().name());
                                }

                                ItemMeta meta = item.getItemMeta();
                                if (meta != null) {
                                    meta.setEnchantmentGlintOverride(true);
                                    List<Component> lore = moblore.stream()
                                            .map(lo -> mm.deserialize(lo.replace("[mob]", en.getType().name())))
                                            .collect(Collectors.toList());
                                    meta.lore(lore);
                                    item.setItemMeta(meta);
                                }
                                p.sendMessage(mm.deserialize(catched.replace("[prefix]", prefix).replace("[mob]", en.getType().name())));
                                if (coreapi != null) {
                                    boolean success = coreapi.logInteraction(p.getName() + "Catch" + en.getType().name(), p.getWorld().getHighestBlockAt(en.getLocation()).getLocation());
                                }
                            }
                            t.cancel();
                        }

                        if (l.getAttribute(Attribute.SCALE) != null) {
                            Objects.requireNonNull(l.getAttribute(Attribute.SCALE)).setBaseValue(tick[0]);
                        }

                        tick[0] -= 0.05;

                    }, 0L, 1L);
                }

            }

        }
    }

    @EventHandler
    private void InteractBlock(PlayerInteractEvent e) {
        if (!enabled) return;
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType().isAir()) return;
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;

        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        Location loc = e.getClickedBlock().getLocation().clone().add(0, 1, 0);

        if (item == null) return;

        if (dataManager.hasData(item, "mob")) {
            if (dataManager.getdata(item, "mob", false) instanceof Boolean) return;
            e.setCancelled(true);

            if (gdapi != null) {
                final Claim claim = gdapi.getClaimAt(loc);

                if (claim != null && !claim.isWilderness()) {
                    if (!claim.getOwnerUniqueId().equals(p.getUniqueId()) &&
                            !claim.getUserTrusts().contains(p.getUniqueId())) {
                        return;
                    }
                }
            }

            MobData mob;
            try {
                mob = (MobData) dataManager.getdata(item, "mob", true);
            } catch (Exception ex) {
                p.sendMessage(mm.deserialize(prefix + " Your mobcatcher is outdated, please contact administrator to replace your item"));
                p.getInventory().removeItem(item);
                return;
            }

            loc.setYaw(random.nextFloat(-180, 180));
            if (mob.type().getEntityClass() != null) {
                Entity spawn = e.getClickedBlock().getWorld().spawn(loc, mob.type().getEntityClass());
                
                if (mob.owner() != null) {
                    if (spawn instanceof Tameable t) {
                        if (p.getUniqueId().equals(mob.owner())) {
                            t.setTamed(true);
                            t.setOwner(p);
                        }
                    }
                    if (spawn instanceof Llama l) {
                        if (p.getUniqueId().equals(mob.owner())) {
                            l.setTamed(true);
                            l.setOwner(p);
                        }
                    }
                }
                if (mob.customName() != null) {
                    spawn.setCustomName(mob.customName());
                }
                if (spawn instanceof Slime slime) {
                    slime.setSize(mob.size());
                }
                if (spawn instanceof LivingEntity le) {
                    EntityEquipment eq = le.getEquipment();
                    if (eq != null) {
                        eq.setHelmet(mob.helmet());
                        eq.setChestplate(mob.chestplate());
                        eq.setLeggings(mob.leggings());
                        eq.setBoots(mob.boots());
                        eq.setItemInMainHand(mob.mainHand());
                        eq.setItemInOffHand(mob.offHand());
                    }
                    if (mob.activeEffects() != null) {
                        for (PotionEffect po : mob.activeEffects()) {
                            le.addPotionEffect(po);
                        }
                    }
                }

                if (spawn instanceof Ageable ageable) {
                    if (mob.isBaby()) ageable.setBaby();
                }

                if (spawn instanceof Llama llama) {
                    if (mob.llamacolor() != null) {
                        llama.setColor(mob.llamacolor());
                    }

                    if (mob.havechest()) {
                        llama.setCarryingChest(true);

                        int expectedSlots = mob.llamastorage().length;
                        int requiredStrength = expectedSlots / 3;
                        llama.setStrength(requiredStrength);
                    }

                    if (mob.llamadecor() != null) {
                        llama.getInventory().setDecor(mob.llamadecor());
                    }

                    if (mob.llamastorage() != null) {
                        llama.getInventory().setContents(mob.llamastorage());
                    }
                }

                spawn.setGlowing(mob.isGlowing());
                spawn.setInvisible(mob.isInvisible());

                if (spawn instanceof LivingEntity le) {

                    AttributeInstance attr = le.getAttribute(Attribute.MAX_HEALTH);
                    if (attr != null) {
                        le.setHealth(Math.min(mob.health(), attr.getValue()));
                    }

                    le.setAI(mob.ai());
                    le.setInvulnerable(mob.invulnerable());
                    le.setSilent(mob.silent());
                }

                if (spawn instanceof Sheep sheep) {
                    sheep.setColor(mob.color());
                }
                if (spawn instanceof Cat cat) {
                    if (mob.catType() != null) {
                        Cat.Type type = Registry.CAT_VARIANT.get(NamespacedKey.fromString(mob.catType()));
                        cat.setCatType(type);
                    }
                }
                if (spawn instanceof Rabbit rabbit) {
                    rabbit.setRabbitType(mob.rabbitType());
                }
                if (spawn instanceof Horse horse) {
                    horse.setColor(mob.horseColor());
                    horse.setStyle(mob.horseStyle());
                }
                if (spawn instanceof Wolf wolf) {
                    if (mob.wolfVariant() != null) {
                        Wolf.Variant variant = Registry.WOLF_VARIANT.get(NamespacedKey.fromString(mob.wolfVariant()));
                        if (variant != null) {
                            wolf.setVariant(variant);
                        }
                    }

                }
                if (spawn instanceof Frog frog) {
                    if (mob.frogVariant() != null) {
                        Frog.Variant variant = Registry.FROG_VARIANT.get(NamespacedKey.fromString(mob.frogVariant()));
                        if (variant != null) {
                            frog.setVariant(variant);
                        }
                    }
                }
                if (spawn instanceof Steerable st) {
                    if (mob.saddled()) st.setSaddle(true);
                }
            }

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setEnchantmentGlintOverride(false);
                List<Component> lore = moblore.stream()
                        .map(lo -> mm.deserialize(lo.replace("[mob]", "")))
                        .collect(Collectors.toList());
                meta.lore(lore);
                item.setItemMeta(meta);
            }
            dataManager.unsetdata(item, "mob");

        }

    }

}
