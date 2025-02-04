package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.Team;
import me.isra.hgkits.TeamManager;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.database.DatabaseManager;
import me.isra.hgkits.database.User;
import me.isra.hgkits.managers.FameManager;
import me.isra.hgkits.managers.KitManager;
import me.isra.hgkits.menu.PlayerStatsMenu;
import me.isra.hgkits.tops.TopStorage;
import me.isra.hgkits.tops.TopType;
import me.isra.hgkits.tops.inventory.MainTopInventoryBuilder;
import me.isra.hgkits.tops.inventory.TopInventoryBuilder;
import me.isra.hgkits.translate.TranslateManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.util.List;

public class PlayerInteractListener implements Listener {

    private final HGKits plugin;
    private final KitManager kitManager;
    private final TranslateManager translateManager;
    private final Map<UUID, Long> cooldownsFlash = new HashMap<>();
    private final Map<UUID, Long> cooldownsThor = new HashMap<>();
    private final TeamManager teamManager;

    public PlayerInteractListener(HGKits plugin, KitManager kitManager, TeamManager teamManager) {
        this.plugin = plugin;
        this.kitManager = kitManager;
        this.translateManager = plugin.getTranslateManager();
        this.teamManager = teamManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block clickedBlock = event.getClickedBlock();
        ItemStack item = player.getInventory().getItemInHand();

        if (player.isSneaking() && event.getAction() == Action.RIGHT_CLICK_AIR) {
            Player target = getTargetPlayer(player);
            if (target != null) {
                teamManager.invitePlayer(player, target);
                return;
            }
        }

        if (HGKits.GAMESTATE == GameState.PREGAME) {
            handlePregameInteractions(event, player, action, clickedBlock, item);
            return;
        }
        Kit kit = kitManager.getKitByPlayer(player);
        handleGameInteractions(event, player, action, item, kit, clickedBlock);
    }

    private Player getTargetPlayer(Player player) {
        List<Player> nearbyPlayers = player.getNearbyEntities(5, 5, 5).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .toList();

        for (Player target : nearbyPlayers) {
            if (player.hasLineOfSight(target)) {
                return target;
            }
        }
        return null;
    }

    private void handlePregameInteractions(PlayerInteractEvent event, Player player, Action action, Block clickedBlock,
            ItemStack item) {
        if (action == Action.RIGHT_CLICK_BLOCK && clickedBlock != null && isRestrictedBlock(clickedBlock.getType())) {
            event.setCancelled(true);
        }

        if ((action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR)) {
            final Material type = item.getType();
            event.setCancelled(true);

            switch (type) {
                case SKULL_ITEM:
                    if (item.getDurability() == 3) {
                        new PlayerStatsMenu(player, plugin.getTranslateManager(), DatabaseManager.getDatabase().getCached(player.getUniqueId())).open(player);
                    }
                    return;
                case BOW:
                    player.performCommand("kit");
                    return;
                case PAPER:
                    new MainTopInventoryBuilder(player, plugin.getTranslateManager()).open(player);
                    return;
                default:
                    break;
            }
        }
    }

    private boolean isRestrictedBlock(Material material) {
        switch (material) {
            case CHEST:
            case TRAPPED_CHEST:
            case WOOD_BUTTON:
            case STONE_BUTTON:
            case TRAP_DOOR:
            case IRON_TRAPDOOR:
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
            case FENCE_GATE:
            case WOOD_PLATE:
            case STONE_PLATE:
            case IRON_PLATE:
            case LEVER:
            case FURNACE:
            case BURNING_FURNACE:
            case WORKBENCH:
            case DISPENSER:
            case DROPPER:
            case ENCHANTMENT_TABLE:
            case ANVIL:
            case BREWING_STAND:
            case HOPPER:
                return true;
            default:
                return false;
        }
    }

    private void sendPlayerStats(final Player player) {
        final User data = DatabaseManager.getDatabase().getCached(player.getUniqueId());
        List<String> statsMessages = translateManager.getMessageList("statistics-messages");
        for (String message : statsMessages) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message
                    .replace("%kills%", String.valueOf(data.kills))
                    .replace("%deaths%", String.valueOf(data.deaths))
                    .replace("%kdr%", String.format("%.2f", data.getKdr()))
                    .replace("%wins%", String.valueOf(data.wins))
                    .replace("%fame%", String.valueOf(data.fame))
                    .replace("%rank%", FameManager.getRankByFame(data.fame))));
        }
    }

    private void handleGameInteractions(PlayerInteractEvent event, Player player, Action action, ItemStack item,
            Kit kit, Block clickedBlock) {
        if (kit == null || item == null)
            return;

        if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
            if (item.getType() == Material.MUSHROOM_SOUP && isHealingKit(kit.getName())) {
                healOrFeedPlayer(player, item);
                event.setCancelled(true);

            } else if (item.getType() == Material.REDSTONE_TORCH_ON && "Flash".equals(kit.getName())) {
                handleFlashTeleport(player);

            } else if (item.getType() == Material.FIREBALL && "Pyro".equals(kit.getName())) {
                handleLaunchFireball(player, item);

            } else if (item.getType() == Material.WATCH && "Meduza".equals(kit.getName())) {
                handleFreezePlayers(player, item);

            } else if (item.getType() == Material.FIREWORK && "Saltamontes".equals(kit.getName())) {
                event.setCancelled(true);
                handleFireworkJump(player);

            } else if (item.getType() == Material.COMPASS && action == Action.RIGHT_CLICK_AIR) {
                handleTeamCompassUsage(player);
            }
        }

        if (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) {
            if (item.getType() == Material.COMPASS) {
                handleEnemyCompassUsage(player);
            }
        }

        if (action == Action.RIGHT_CLICK_BLOCK) {
            if (item.getType() == Material.DIAMOND_AXE && "Thor".equals(kit.getName())) {
                handleStrikeLightning(player, clickedBlock.getLocation().add(0, 1, 0));
            }
        }
    }

    private boolean isHealingKit(String kitName) {
        return "Curandero".equals(kitName) || "Orco".equals(kitName) || "Canibal".equals(kitName)
                || "Coloso".equals(kitName);
    }

    private void healOrFeedPlayer(Player player, ItemStack item) {
        boolean hasFullHealth = player.getHealth() == player.getMaxHealth();
        boolean isHungry = player.getFoodLevel() < 20;

        if (!hasFullHealth) {
            double newHealth = Math.min(player.getHealth() + 6.0, player.getMaxHealth());
            player.setHealth(newHealth);
        } else if (isHungry) {
            int newFoodLevel = Math.min(player.getFoodLevel() + 6, 20);
            player.setFoodLevel(newFoodLevel);
        }

        item.setType(Material.BOWL);
        player.setItemInHand(item);
    }

    private void handleFlashTeleport(Player player) {
        UUID playerId = player.getUniqueId();
        if (cooldownsFlash.containsKey(playerId)) {
            long timeSinceLastUse = (System.currentTimeMillis() - cooldownsFlash.get(playerId)) / 1000;
            if (timeSinceLastUse < 60) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', translateManager
                        .getMessage("flash-cooldown").replace("%seconds%", String.valueOf(60 - timeSinceLastUse))));
                return;
            }
        }

        BlockIterator blockIterator = new BlockIterator(player, 100);
        Block hitBlock = null;

        while (blockIterator.hasNext()) {
            Block block = blockIterator.next();
            if (block.getType() != Material.AIR) {
                hitBlock = block;
                break;
            }
        }

        if (hitBlock != null) {
            double distance = player.getLocation().distance(hitBlock.getLocation());
            if (distance <= 50) {
                Location teleportLocation = hitBlock.getLocation().add(0, 1, 0);
                teleportLocation.setYaw(player.getLocation().getYaw());
                teleportLocation.setPitch(player.getLocation().getPitch());

                player.teleport(teleportLocation);
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);
                cooldownsFlash.put(playerId, System.currentTimeMillis());
            } else {
                player.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("flash-too-far")));
            }
        }
    }

    private void handleEnemyCompassUsage(Player player) {
        Player closestPlayer = null;
        double closestDistance = Double.MAX_VALUE;

        for (Player target : plugin.getPlayers()) {
            if (target.equals(player))
                continue;

            double distance = player.getLocation().distance(target.getLocation());

            if (distance < closestDistance) {
                closestDistance = distance;
                closestPlayer = target;
                player.setCompassTarget(target.getLocation());
            }
        }

        if (closestPlayer == null) {
            player.sendMessage(
                    ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("no-players-nearby")));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    translateManager.getMessage("player-distance").replace("%player%", closestPlayer.getName())
                            .replace("%distance%", String.valueOf((int) closestDistance))));
        }
    }

    private void handleTeamCompassUsage(Player player) {
        Team team = teamManager.getTeam(player);
        if (team == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("team-not-in-team")));
            return;
        }

        Player closestTeammate = null;
        double closestDistance = Double.MAX_VALUE;

        for (String memberName : team.getMembers()) {
            Player teammate = Bukkit.getPlayer(memberName);
            if (teammate == null || teammate.equals(player))
                continue;

            double distance = player.getLocation().distance(teammate.getLocation());

            if (distance < closestDistance) {
                closestDistance = distance;
                closestTeammate = teammate;
                player.setCompassTarget(teammate.getLocation());
            }
        }

        if (closestTeammate == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("no-players-nearby")));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    translateManager.getMessage("player-distance").replace("%player%", closestTeammate.getName())
                            .replace("%distance%", String.valueOf((int) closestDistance))));
        }
    }

    private void handleLaunchFireball(Player player, ItemStack item) {
        Fireball fireball = player.launchProjectile(Fireball.class);
        Vector direction = player.getLocation().getDirection().multiply(2);
        fireball.setVelocity(direction);

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().remove(item);
        }
    }

    private void handleFreezePlayers(Player player, ItemStack item) {
        if (plugin.getCooldownsMedusa().containsKey(player.getUniqueId())) {
            long timeSinceLastUse = (System.currentTimeMillis()
                    - plugin.getCooldownsMedusa().get(player.getUniqueId()));
            if (timeSinceLastUse < 30000) {
                player.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("medusa-cooldown")
                                .replace("%seconds%", String.valueOf((30000 - timeSinceLastUse) / 1000))));
                return;
            }
        }

        plugin.getCooldownsMedusa().put(player.getUniqueId(), System.currentTimeMillis());
        if (player.getInventory().getItemInHand().getAmount() > 0) {
            player.getInventory().getItemInHand().setAmount(player.getInventory().getItemInHand().getAmount() - 1);
        }

        Location playerLocation = player.getLocation();
        for (Player nearbyPlayer : plugin.getPlayers()) {
            if (nearbyPlayer.equals(player))
                continue; // Ignorar al jugador que usa el item
            if (nearbyPlayer.getLocation().distance(playerLocation) <= 25) {
                plugin.getFrozenPlayers().add(nearbyPlayer);
                nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 255));
                nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 0));
                nearbyPlayer.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("medusa-frozen")));
                nearbyPlayer.playSound(nearbyPlayer.getLocation(), Sound.AMBIENCE_CAVE, 1.0F, 1.0F);
                player.playSound(playerLocation, Sound.AMBIENCE_CAVE, 1.0F, 1.0F);
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : plugin.getFrozenPlayers()) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            translateManager.getMessage("medusa-unfrozen")));
                }
                plugin.removeAllFrozenPlayers();
            }
        }.runTaskLater(plugin, 100);
    }

    private void handleFireworkJump(Player player) {
        final Block b = player.getLocation().getBlock();
        if (b.getType() != Material.AIR || b.getRelative(BlockFace.DOWN).getType() != Material.AIR) {
            player.setFallDistance(-5.0f);
            final Vector vector = player.getEyeLocation().getDirection();
            vector.multiply(0.6f);
            vector.setY(1);
            player.setVelocity(vector);
        }
    }

    private void handleStrikeLightning(Player player, Location location) {
        if (cooldownsThor.containsKey(player.getUniqueId())) {
            long timeSinceLastUse = (System.currentTimeMillis() - cooldownsThor.get(player.getUniqueId()));
            if (timeSinceLastUse < 10000) {
                player.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("thor-cooldown")
                                .replace("%seconds%", String.valueOf((10000 - timeSinceLastUse) / 1000))));
                return;
            }
        }

        player.getWorld().strikeLightning(location);
        cooldownsThor.put(player.getUniqueId(), System.currentTimeMillis());
    }
}
