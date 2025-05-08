package dev.lipoteam.lipoExtra.Events;

import dev.lipoteam.lipoExtra.Files.SpawnerConfig;
import dev.lipoteam.lipoExtra.LipoExtra;
import dev.lipoteam.lipoExtra.Manager.DataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Spawner implements Listener {

    private SpawnerConfig config;
    private final LipoExtra plugin;
    private final DataManager dataManager;
    private final BukkitScheduler scheduler;

    private int initialseconds;
    private int secperstack;
    private int maxstacking;
    private int maxspawn;
    private List<String> worlds;
    private String spawnerName;
    private String spawnerTime;
    private String prefix;
    private boolean enabled;
    private final Random random;

    public final Map<Location, BukkitTask> activeTasks = new HashMap<>();

    private String maxmsg;
    MiniMessage mm;

    public Spawner(SpawnerConfig config, LipoExtra plugin) {

        this.plugin = plugin;
        dataManager = new DataManager(plugin);
        scheduler = plugin.getServer().getScheduler();
        random = new Random();
        mm = MiniMessage.miniMessage();
        setConfig(config);

    }

    public void setConfig(SpawnerConfig config) {
        this.config = config;
        prefix = config.prefix();
        enabled = config.enabled();
        spawnerName = config.spawnerName();
        spawnerTime = config.spawnerTime();
        worlds = config.worlds();
        initialseconds = config.initialSeconds();
        secperstack = config.secStacking();
        maxstacking = config.maxStacking();
        maxmsg = config.MaxMsg();
        maxspawn = config.multiplerMaxSpawn();
    }

    @EventHandler
    public void onSpawnerBreak(BlockBreakEvent e) {
        if (enabled && e.getPlayer().hasPermission("lipo.spawner.break")) {
            Block block = e.getBlock();
            Player p = e.getPlayer();
            if (block.getType() != Material.SPAWNER) return;

            BlockState state = block.getState();
            if (!(state instanceof CreatureSpawner spawner)) return;

            if (dataManager.hasData(block, "stack")) {
                int stack = (int) dataManager.getdata(block, "stack", false);

                removeSpawner(block);

                ItemStack itemstack = new ItemStack(Material.SPAWNER);
                ItemMeta meta = itemstack.getItemMeta();

                CreatureSpawner thespawner;
                BlockStateMeta blockstate = null;
                if (meta instanceof BlockStateMeta blockStateMeta) {
                    blockstate = blockStateMeta;
                    BlockState itemState = blockStateMeta.getBlockState();
                    if ((itemState instanceof CreatureSpawner itemSpawner)) {
                        thespawner = itemSpawner;
                    } else {
                        thespawner = null;
                    }
                } else {
                    thespawner = null;
                }

                if (blockstate == null || thespawner == null) return;

                thespawner.setSpawnedType(spawner.getSpawnedType());
                blockstate.setBlockState(thespawner);


                if (!p.isSneaking()) {

                    meta.displayName(mm.deserialize("<!i>" + spawner.getSpawnedType() + " Spawner <dark_gray>(" + stack + ")"));
                    itemstack.setItemMeta(blockstate);

                    dataManager.setdata(itemstack, "stack", stack);
                } else {

                    itemstack.setItemMeta(blockstate);

                    if (stack > 1) {

                        scheduler.runTaskLater(plugin, () -> {
                            Block newblock = block.getLocation().getBlock();
                            newblock.setType(Material.SPAWNER);
                            BlockState newstate = newblock.getState();
                            if (newstate instanceof CreatureSpawner newspawner) {
                                newspawner.setSpawnedType(thespawner.getSpawnedType());
                                newspawner.update();

                                p.sendBlockChange(newblock.getLocation(), newblock.getBlockData());

                                if (stack > 2) {
                                    dataManager.setdata(block.getLocation().getBlock(), "stack", stack - 1);
                                    StackingSystem(block.getLocation().getBlock(), stack - 1, spawner);
                                }

                            }
                        }, 1L);

                    }

                }

                List<Item> excluded = new ArrayList<>(List.of());

                for (Entity ne : block.getLocation().getNearbyEntitiesByType(EntityType.ITEM.getEntityClass(), 2)) {
                    Item i = (Item) ne;
                    if (i.getItemStack().getType().equals(itemstack.getType())) {
                        excluded.add(i);
                    }
                }

                scheduler.runTaskLater(plugin, () -> {
                    boolean found = false;
                    for (Entity ne : block.getLocation().getNearbyEntitiesByType(EntityType.ITEM.getEntityClass(), 2)) {
                        Item i = (Item) ne;
                        if (i.getItemStack().getAmount() == 1 && i.getItemStack().getType().equals(itemstack.getType()) && !excluded.contains(i)) {
                            found = true;
                            i.setItemStack(itemstack);
                            break;
                        }
                    }
                    if (!found) {
                        if (stack > 1) {
                            if (dataManager.hasData(itemstack, "stack")) {
                                if (stack > 2) {
                                    dataManager.setdata(itemstack, "stack", stack - 1);
                                    ItemMeta newmeta = itemstack.getItemMeta();
                                    newmeta.displayName(mm.deserialize("<!i>" + spawner.getSpawnedType() + " Spawner <dark_gray>(" + (stack - 1) + ")"));
                                    itemstack.setItemMeta(newmeta);
                                } else {
                                    ItemMeta newmeta = itemstack.getItemMeta();
                                    newmeta.displayName(mm.deserialize("<!i>Monster Spawner"));
                                    itemstack.setItemMeta(newmeta);
                                    dataManager.unsetdata(itemstack, "stack");
                                }
                            } else {
                                return;
                            }

                            block.getWorld().dropItemNaturally(block.getLocation(), itemstack);

                        }
                    }
                }, 1L);
            }
        }

    }

    public void removeSpawner(Block block) {
        Location loc = block.getLocation();
        if (activeTasks.containsKey(loc)) {
            activeTasks.get(loc).cancel();
            activeTasks.remove(loc);
        }

        if (dataManager.hasData(block, "display0") && dataManager.hasData(block, "display1")) {
            UUID uuid0 = UUID.fromString((String) dataManager.getdata(block, "display0", false));
            UUID uuid1 = UUID.fromString((String) dataManager.getdata(block, "display1", false));
            Entity display0 = block.getWorld().getEntity(uuid0);
            Entity display1 = block.getWorld().getEntity(uuid1);
            if (display0 != null) {
                display0.remove();
                dataManager.unsetdata(block, "display0");
            }
            if (display1 != null) {
                display1.remove();
                dataManager.unsetdata(block, "display1");
            }
        } else {
            for (Entity e : block.getLocation().getNearbyEntitiesByType(EntityType.TEXT_DISPLAY.getEntityClass(), 1, 2)) {
                e.remove();
            }
        }

    }

    @EventHandler
    public void SpawnSpawner(SpawnerSpawnEvent e) {
        if (enabled) {
            if (e.getSpawner() != null) {
                Block block = e.getSpawner().getBlock();
                if (dataManager.hasData(block, "stack")) {
                    e.setCancelled(true);
                }
            }
        }

    }

    @EventHandler
    public void SpawnerOnOff(PlayerInteractEvent e) {
        if (enabled && e.getPlayer().hasPermission("lipo.spawner.toggle")) {
            Block block = e.getClickedBlock();

            if (block == null) return;

            if (!e.getAction().isRightClick()) return;

            BlockData blockData = block.getBlockData();
            if (blockData instanceof Powerable po) {
                BlockFace[] directions = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
                if (!po.isPowered()) {
                    for (BlockFace face : directions) {
                        Block nearby = block.getRelative(face);
                        if (nearby.getType() == Material.SPAWNER) {

                            if (!activeTasks.containsKey(nearby.getLocation())) {
                                BlockState state = nearby.getState();
                                if (!(state instanceof CreatureSpawner spawner)) continue;

                                if (dataManager.hasData(nearby, "stack")) {
                                    int stack = (int) dataManager.getdata(nearby, "stack", false);
                                    StackingSystem(nearby, stack, spawner);
                                }
                            } else {
                                activeTasks.get(nearby.getLocation()).cancel();
                                activeTasks.remove(nearby.getLocation());
                                deactivateSpawner(nearby);
                            }

                        }
                    }
                }
            }
        }
    }

    public void deactivateSpawner(Block block) {

        try {
            UUID displayUUID = UUID.fromString((String) dataManager.getdata(block, "display0", false));
            Entity entity = block.getWorld().getEntity(displayUUID);
            if (entity instanceof TextDisplay display) {
                Component text = display.text();
                display.text(MiniMessage.miniMessage().deserialize("<red>").append(text));
            }
        } catch (Exception ex) {
            plugin.getLogger().warning("Failed to modify TextDisplay at " + block.getLocation() + ": " + ex.getMessage());
        }

    }

    @EventHandler
    public void SpawnerStack(BlockPlaceEvent e) {
        if ((enabled && worlds.contains(e.getPlayer().getWorld().getName())) || e.getPlayer().hasPermission("lipo.spawner.stack")) {

            Block placedBlock = e.getBlockPlaced();
            Block againstBlock = e.getBlockAgainst();
            Player p = e.getPlayer();
            ItemStack item = e.getItemInHand();
            BlockState state = againstBlock.getState();

            if (placedBlock.getType() != Material.SPAWNER) return;

            EntityType handSpawnerType = null;
            if (item.hasItemMeta() && item.getItemMeta() instanceof BlockStateMeta meta) {
                BlockState itemState = meta.getBlockState();
                if (itemState instanceof CreatureSpawner itemSpawner) {
                    handSpawnerType = itemSpawner.getSpawnedType();
                }
            }

            if (againstBlock.getType() != Material.SPAWNER || (state instanceof CreatureSpawner spawner && spawner.getSpawnedType() != handSpawnerType)) {
                if (dataManager.hasData(item, "stack")) {
                    int stackitem = (int) dataManager.getdata(item, "stack", false);
                    scheduler.runTaskLater(plugin, () -> {

                        Block block = placedBlock.getLocation().getBlock();
                        BlockState blockState = block.getState();
                        if ((blockState instanceof CreatureSpawner newspawner)) {
                            dataManager.setdata(block, "stack", stackitem);
                            StackingSystem(block, stackitem, newspawner);
                        }
                    }, 1L);
                    return;
                }
            }

            if (handSpawnerType == null) return;

            if (!(state instanceof CreatureSpawner spawner)) return;

            if (spawner.getSpawnedType() != handSpawnerType) return;

            e.setCancelled(true);

            if (dataManager.hasData(item, "stack")) {
                int stackitem = (int) dataManager.getdata(item, "stack", false);
                if (dataManager.hasData(againstBlock, "stack")) {
                    int stack = (int) dataManager.getdata(againstBlock, "stack", false);
                    if (stack + stackitem > maxstacking) {
                        plugin.adventure().player(p).sendMessage(mm.deserialize(maxmsg.replace("[prefix]", prefix)));
                        return;
                    } else {
                        dataManager.setdata(againstBlock, "stack", stack + stackitem);
                        StackingSystem(againstBlock, stack + stackitem, spawner);
                    }
                } else {
                    if (stackitem + 1 > maxstacking) {
                        plugin.adventure().player(p).sendMessage(mm.deserialize(maxmsg.replace("[prefix]", prefix)));
                        return;
                    } else {
                        dataManager.setdata(againstBlock, "stack", stackitem + 1);
                        StackingSystem(againstBlock, stackitem + 1, spawner);
                    }
                }
            } else {
                if (dataManager.hasData(againstBlock, "stack")) {
                    int stack = (int) dataManager.getdata(againstBlock, "stack", false);
                    if (stack >= maxstacking) {
                        plugin.adventure().player(p).sendMessage(mm.deserialize(maxmsg.replace("[prefix]", prefix)));
                        return;
                    } else {
                        dataManager.setdata(againstBlock, "stack", stack + 1);
                        StackingSystem(againstBlock, stack + 1, spawner);
                    }
                } else {
                    dataManager.setdata(againstBlock, "stack", 2);
                    StackingSystem(againstBlock, 2, spawner);
                }
            }

            if (p.getGameMode() != GameMode.CREATIVE) {
                ItemStack inHand = p.getInventory().getItemInMainHand();
                ItemStack offHand = p.getInventory().getItemInOffHand();
                if (inHand.getType().equals(Material.SPAWNER)) {
                    inHand.setAmount(inHand.getAmount() - 1);
                } else {
                    offHand.setAmount(offHand.getAmount() - 1);
                }
            }
        }
    }

    private void StackingSystem(Block block, int stack, CreatureSpawner spawner) {

        TextDisplay display0;
        TextDisplay display1;
        int time;
        if (dataManager.hasData(block, "display0") && dataManager.hasData(block, "display1")) {

            display0 = (TextDisplay) block.getWorld().getEntity(UUID.fromString((String) dataManager.getdata(block, "display0", false)));
            display1 = (TextDisplay) block.getWorld().getEntity(UUID.fromString((String) dataManager.getdata(block, "display1", false)));

        } else {
            display0 = (TextDisplay) block.getWorld().spawnEntity(block.getLocation().add(0.5,1.5,0.5), EntityType.TEXT_DISPLAY);
            display1 = (TextDisplay) block.getWorld().spawnEntity(block.getLocation().add(0.5,1.25,0.5), EntityType.TEXT_DISPLAY);
            dataManager.setdata(block, "display0", display0.getUniqueId().toString());
            dataManager.setdata(block, "display1", display1.getUniqueId().toString());
        }
        time = initialseconds + ((stack - 1) * secperstack);
        dataManager.setdata(block, "time", time);

        if (display0 != null && display1 != null) {
            if (spawner.getSpawnedType() != null) {

                dataManager.setdata(block, "spawner", spawner.getSpawnedType().name());

                if (activeTasks.containsKey(block.getLocation())) {
                    BukkitTask prevTask = activeTasks.get(block.getLocation());
                    prevTask.cancel();
                    activeTasks.remove(block.getLocation());
                }

                AtomicInteger finalTime = new AtomicInteger(time);

                spawner.setMinSpawnDelay(20);
                spawner.setMaxSpawnDelay((time * 20));
                spawner.setMinSpawnDelay(time * 20);
                spawner.setDelay(time * 20);
                spawner.setSpawnCount(stack);
                spawner.setMaxNearbyEntities(stack * maxspawn);
                spawner.update();

                int spawnrange = spawner.getSpawnRange();
                AtomicReference<EntityType> type = new AtomicReference<>(spawner.getSpawnedType());

                display0.text(mm.deserialize(spawnerName.replace("[spawner]", spawner.getSpawnedType().name()).replace("[stack]", String.valueOf(stack)).replace("[amount]", String.valueOf(spawner.getSpawnCount()))));
                display1.text(mm.deserialize(spawnerTime.replace("[time]", String.valueOf(time)).replace("[amount]", String.valueOf(spawner.getSpawnCount()))));
                display0.setAlignment(TextDisplay.TextAlignment.CENTER);
                display1.setAlignment(TextDisplay.TextAlignment.CENTER);
                display0.setBillboard(Display.Billboard.CENTER);
                display1.setBillboard(Display.Billboard.CENTER);

                BukkitTask task = scheduler.runTaskTimerAsynchronously(plugin, () -> {

                    Block checkblock = block.getLocation().getBlock();
                    if (!dataManager.hasData(checkblock, "stack")) {
                        if (checkblock.getType() == Material.SPAWNER) {
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                BlockState state = checkblock.getState();
                                if (state instanceof CreatureSpawner newSpawner) {
                                    if (spawner.getSpawnedType() != newSpawner.getSpawnedType()) {
                                        removeSpawner(checkblock);
                                        dataManager.setdata(checkblock, "stack", stack);
                                        StackingSystem(block, stack, newSpawner);
                                    }
                                }
                            });
                        } else {
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                removeSpawner(checkblock);
                            });
                        }
                    }

                    if (spawner.isActivated()) {
                        finalTime.getAndDecrement();

                        Component newText1 = mm.deserialize(spawnerTime.replace("[time]", String.valueOf(finalTime.get())).replace("[amount]", String.valueOf(spawner.getSpawnCount())));

                        if (!newText1.equals(display1.text())) {
                            display1.text(newText1);
                            display1.isValid();
                        }
                    }

                    if (finalTime.get() <= 0) {
                        finalTime.set(time);

                        Location baseLoc = block.getLocation().clone().add(0.5, 0, 0.5);
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            for (int i = 0; i < spawner.getSpawnCount(); i++) {

                                int nearby = (int) block.getWorld().getNearbyEntities(block.getBoundingBox().expand(spawnrange * 2)).stream()
                                        .filter(entity -> entity.getType() == type.get())
                                        .count();

                                if (nearby >= spawner.getMaxNearbyEntities()) return;

                                Location newloc = baseLoc.clone().add(
                                        random.nextInt(-spawnrange, spawnrange + 1),
                                        0,
                                        random.nextInt(-spawnrange, spawnrange + 1)
                                );
                                if (!newloc.getBlock().getType().isAir()) {
                                    continue;
                                }
                                newloc.setYaw(random.nextFloat(-180, 180));

                                block.getWorld().spawnParticle(Particle.CLOUD, newloc,3,0, 1, 0, 0.1);
                                block.getWorld().spawnEntity(newloc, type.get());
                            }
                        });

                    }

                }, 20L, 20L);

                activeTasks.put(block.getLocation(), task);

            }
        }

    }

}
