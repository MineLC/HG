package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.database.DatabaseManager;
import me.isra.hgkits.database.User;
import me.isra.hgkits.managers.FameManager;
import me.isra.hgkits.managers.KitManager;
import me.isra.hgkits.tops.TopStorage;
import me.isra.hgkits.tops.inventory.TopInventoryBuilder;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

public class PlayerInteractListener implements Listener {

    private final HGKits plugin;
    private final KitManager kitManager;
    private final Map<UUID, Long> cooldownsFlash = new HashMap<>();
    //private final Map<UUID, Long> cooldownsMedusa = new HashMap<>();
    private final Map<UUID, Long> cooldownsSaltamontes = new HashMap<>();
    private final Map<UUID, Long> cooldownsThor = new HashMap<>();

    public PlayerInteractListener(HGKits plugin, KitManager kitManager) {
        this.plugin = plugin;
        this.kitManager = kitManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block clickedBlock = event.getClickedBlock();
        ItemStack item = player.getInventory().getItemInHand();

        if (HGKits.GAMESTATE == GameState.PREGAME) {
            handlePregameInteractions(event, player, action, clickedBlock, item);
            return;
        }
        Kit kit = kitManager.getKitByPlayer(player);
        //INCLUYE EL ESTADO DE JUEGO INVINCIBILITY.
        //LAS ACCIONES SE EJECUTARÁN TAMBIÉN CUANDO EL JUEGO
        //ESTE EN PERIODO DE INVENCIBILIDAD.
        handleGameInteractions(event, player, action, item, kit, clickedBlock);
    }

    private void handlePregameInteractions(PlayerInteractEvent event, Player player, Action action, Block clickedBlock, ItemStack item) {
        if (action == Action.RIGHT_CLICK_BLOCK && clickedBlock != null && isRestrictedBlock(clickedBlock.getType())) {
            event.setCancelled(true);
        }

        if ((action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR)) {
            final Material type = item.getType();
            event.setCancelled(true);

            switch (type) {
                case BOOK:
                    sendPlayerStats(player);
                    return;
                case BOW:
                    player.performCommand("kit");    
                    return;
                case BONE:
                    new TopInventoryBuilder().build(player, TopStorage.deaths(), "Top de muertes");
                    return;
                case LEATHER:
                    new TopInventoryBuilder().build(player, TopStorage.kills(), "Top de kills");
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
        player.sendMessage(
            "\n "+
            "\n §6§lEstadísticas" +
            "\n "+
            "\n §fKills: §6" + data.kills +
            "\n §fMuertes: §c" + data.deaths +
            "\n §fKDR: §d" + String.format("%.2f", data.getKdr()) +
            "\n "+    
            "\n §fVictorias: §a" + data.wins +
            "\n "+    
            "\n §fFama: §b" + data.fame +
            "\n §fRango: §e" + FameManager.getRankByFame(data.fame) +
            "\n "
        );
    }

    private void handleGameInteractions(PlayerInteractEvent event, Player player, Action action, ItemStack item, Kit kit, Block clickedBlock) {
        if (kit == null || item == null) return;

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

            }else if (item.getType() == Material.FIREWORK && "Saltamontes".equals(kit.getName())){
                event.setCancelled(true);
                handleFireworkJump(player);

            }else if (item.getType() == Material.COMPASS) {
                handleCompassUsage(player);
            }
        }

        if (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) {
            if (item.getType() == Material.COMPASS) {
                handleCompassUsage(player);
            }
        }

        if (action == Action.RIGHT_CLICK_BLOCK) {
            if(item.getType() == Material.DIAMOND_AXE && "Thor".equals(kit.getName())) {
                handleStrikeLightning(player, clickedBlock.getLocation().add(0, 1, 0));
            }
        }
    }

    private boolean isHealingKit(String kitName) {
        return "Curandero".equals(kitName) || "Orco".equals(kitName) || "Canibal".equals(kitName) || "Coloso".equals(kitName);
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
                player.sendMessage(ChatColor.RED + "¡Debes esperar " + (60 - timeSinceLastUse) + " segundos para teletransportarte de nuevo!" + ChatColor.RESET);
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
                player.sendMessage(ChatColor.RED + "¡No puedes teletransportarte tan lejos!" + ChatColor.RESET);
            }
        }
    }

    private void handleCompassUsage(Player player) {
        Player closestPlayer = null;
        double closestDistance = Double.MAX_VALUE;

        for (Player target : plugin.getPlayers()) {
            if (target.equals(player)) continue;

            double distance = player.getLocation().distance(target.getLocation());

            if (distance < closestDistance) {
                closestDistance = distance;
                closestPlayer = target;
                player.setCompassTarget(target.getLocation());
            }
        }

        if (closestPlayer == null) {
            player.sendMessage(ChatColor.RED + "No hay jugadores cerca.");
        } else {
            player.sendMessage(ChatColor.GREEN + "El jugador " + ChatColor.RESET + ChatColor.DARK_GREEN + closestPlayer.getName() + ChatColor.RESET + ChatColor.GREEN + " está a " + (int) closestDistance + " bloques de distancia.");
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
            long timeSinceLastUse = (System.currentTimeMillis() - plugin.getCooldownsMedusa().get(player.getUniqueId()));
            if (timeSinceLastUse < 30000) {
                player.sendMessage(ChatColor.RED + "¡Debes esperar " + ((30000 - timeSinceLastUse) / 1000) + " segundos para usar esto de nuevo!" + ChatColor.RESET);
                return;
            }
        }

        plugin.getCooldownsMedusa().put(player.getUniqueId(), System.currentTimeMillis());
        if(player.getInventory().getItemInHand().getAmount() > 0) {
            player.getInventory().getItemInHand().setAmount(player.getInventory().getItemInHand().getAmount() - 1);
        }


        Location playerLocation = player.getLocation();
        for (Player nearbyPlayer : playerLocation.getWorld().getPlayers()) {
            if (nearbyPlayer.equals(player)) continue; // Ignorar al jugador que usa el item
            if (nearbyPlayer.getLocation().distance(playerLocation) <= 25) {
                plugin.getFrozenPlayers().add(nearbyPlayer);
                nearbyPlayer.sendMessage(ChatColor.RED + "¡Estás congelado por la Medusa!" + ChatColor.RESET);
                nearbyPlayer.playSound(nearbyPlayer.getLocation(), Sound.ORB_PICKUP, 1.0F, 1.0F);
            }
        }

        // Descongelar después de 30 segundos (600 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                // Enviar mensaje de descongelación a todos los jugadores que estaban congelados
                for (Player p : plugin.getFrozenPlayers()) {
                    p.sendMessage(ChatColor.GREEN + "¡Has sido descongelado!" + ChatColor.RESET);
                }
                plugin.removeAllFrozenPlayers();
            }
        }.runTaskLater(plugin, 100);
    }


    private void handleFireworkJump(Player player) {
        if (cooldownsSaltamontes.containsKey(player.getUniqueId())) {
            long timeSinceLastUse = (System.currentTimeMillis() - cooldownsSaltamontes.get(player.getUniqueId()));
            if (timeSinceLastUse < 4000) {
                player.sendMessage(ChatColor.RED + "¡Debes esperar " + ((4000 - timeSinceLastUse) / 1000) + " segundos para usar esto de nuevo!" + ChatColor.RESET);
                return;
            }
        }

        Vector velocity = player.getVelocity();
        velocity.setY(1.5);
        velocity.setX(player.getLocation().getDirection().getX() * 0.5);
        velocity.setZ(player.getLocation().getDirection().getZ() * 0.5);
        player.setVelocity(velocity);
        cooldownsSaltamontes.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private void handleStrikeLightning(Player player, Location location) {
        if (cooldownsThor.containsKey(player.getUniqueId())) {
            long timeSinceLastUse = (System.currentTimeMillis() - cooldownsThor.get(player.getUniqueId()));
            if (timeSinceLastUse < 10000) {
                player.sendMessage(ChatColor.RED + "¡Debes esperar " + ((10000 - timeSinceLastUse) / 1000) + " segundos para usar esto de nuevo!" + ChatColor.RESET);
                return;
            }
        }

        player.getWorld().strikeLightning(location);
        cooldownsThor.put(player.getUniqueId(), System.currentTimeMillis());
    }
}
