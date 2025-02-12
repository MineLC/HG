package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.data.BlockBackup;
import me.isra.hgkits.data.Jaula;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.database.DatabaseManager;
import me.isra.hgkits.database.User;
import me.isra.hgkits.managers.KitManager;
import me.isra.hgkits.tops.TopManager;
import me.isra.hgkits.translate.TranslateManager;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerDeathListener implements Listener {
    private final HGKits plugin;
    private final KitManager kitManager;
    private final TranslateManager translateManager;

    public PlayerDeathListener(HGKits plugin, KitManager kitManager) {
        this.plugin = plugin;
        this.kitManager = kitManager;
        this.translateManager = plugin.getTranslateManager();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (HGKits.GAMESTATE == GameState.GAME) {
            Player player = event.getEntity();

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.AMBIENCE_THUNDER, 10000f, 2f);
            }

            Location deathLocation = player.getLocation();
            World world = deathLocation.getWorld();
            Kit playerKit = kitManager.getKitByPlayer(player);

            String deathMessage = event.getDeathMessage();
            event.setDeathMessage(ChatColor.RED + deathMessage);

            if (playerKit != null && (playerKit.name().equals("Creeper") || playerKit.name().equals("Ultracreeper"))) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (world != null) {
                            world.createExplosion(deathLocation, 4F);
                        }
                    }
                }.runTaskLater(plugin, 1L);
            }

            Player killer = player.getKiller();
            if (killer != null) {
                final User killerData = DatabaseManager.getDatabase().getCached(killer.getUniqueId());
                if (killerData != null) {
                    Kit killerKit = kitManager.getKitByPlayer(killer);

                    if (killerKit != null && (killerKit.name().equals("Guerrero") || killerKit.name().equals("Matasanos"))) {
                        if (killer.getFoodLevel() < 20) {
                            killer.setFoodLevel(20);
                        }
                    }

                    if(HGKits.getInstance().getPermission().playerInGroup(killer, "ruby")){
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lcadmin add 5 lcoins "+killer.getName());
                        killer.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"+5 LCoins");
                    }
                    else if(HGKits.getInstance().getPermission().playerInGroup(killer, "elite")){
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lcadmin add 4 lcoins "+killer.getName());
                        killer.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"+4 LCoins");
                    }
                    else if(HGKits.getInstance().getPermission().playerInGroup(killer, "svip")){
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lcadmin add 3 lcoins "+killer.getName());
                        killer.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"+3 LCoins");
                    }
                    else if(HGKits.getInstance().getPermission().playerInGroup(killer, "vip")){
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lcadmin add 2 lcoins "+killer.getName());
                        killer.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"+2 LCoins");
                    }
                    else {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lcadmin add 1 lcoins "+killer.getName());
                        killer.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"+1 LCoins");
                    }

                    killerData.kills++;
                    final double newFame = killerData.getKDR() * killerData.kills + (killerData.wins == 0 ? 0 : (double)(killerData.wins)/2D);
                    killerData.fame = (int)newFame;

                    if(kitManager.getKitByPlayer(killer).name().equals("Paladín")){
                        killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5*20, 4));
                        killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5*20, 4));
                    }

                    killer.sendMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("your-fame") + killerData.fame));

                    if (kitManager.getKitByPlayer(killer).name().equals("Vikingo")) {
                        ItemStack axe = getBetterAxe(killer);
                        if (axe != null) {
                            int currentLevel = getAxeLevel(axe);
                            int nextLevel = currentLevel + 1;
                            Material nextAxeType = getAxeTypeByLevel(nextLevel);

                            if (nextAxeType != null) {
                                axe.setType(nextAxeType);
                                killer.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "¡Has evolucionado tu hacha!");
                            }

                            if (axe.getEnchantments().containsKey(Enchantment.DAMAGE_ALL)) {
                                int level = axe.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
                                axe.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, level + 1);
                            } else {
                                axe.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
                            }
                        }
                    }


                    TopManager.calculateKills(killerData);
                    TopManager.calculateKdr(killerData);
                }
            }

            if (plugin.getPlayers().remove(player) && plugin.getPlayers().size() > 1) {
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("players-left").replace("%count%", String.valueOf(plugin.getPlayers().size()))));
            }

            List<Player> toRemove = new ArrayList<>();

            for (Map.Entry<Player, Jaula> playerJaulaEntry : EntityDamageByEntityListener.jaulas.entrySet()) {
                if (playerJaulaEntry.getValue().getPlayersInJaula(5) <= 1) {
                    playerJaulaEntry.getValue().restoreBlocks();
                    toRemove.add(playerJaulaEntry.getKey());
                }
            }

            toRemove.forEach(EntityDamageByEntityListener.jaulas::remove);

            player.setGameMode(GameMode.SPECTATOR);
            final User victim = DatabaseManager.getDatabase().getCached(player.getUniqueId());
            victim.deaths++;
            TopManager.calculateDeaths(victim);
            TopManager.calculateKdr(victim);
        }
    }

    @Nullable
    private ItemStack getBetterAxe(Player player) {
        ItemStack item = null;
        for (ItemStack is : player.getInventory().getContents()) {
            if(is != null) {
                if (isAxe(is.getType())){
                    if(item == null){
                        item = is;
                        continue;
                    }
                    if(getAxeLevel(is) >= getAxeLevel(item)){
                        item = is;
                    }
                }
            }
        }
        return item;
    }

    private int getAxeLevel(ItemStack ie){
        return switch (ie.getType()){
            case STONE_AXE -> 1;
            case IRON_AXE -> 2;
            case GOLD_AXE -> 3;
            case DIAMOND_AXE -> 4;
            default -> 0;
        };
    }

    @Nullable
    private Material getAxeTypeByLevel(int i){
        if(i > 4) return null;
        return switch (i){
            case 1 -> Material.STONE_AXE;
            case 2 -> Material.IRON_AXE;
            case 3 -> Material.GOLD_AXE;
            case 4 -> Material.DIAMOND_AXE;
            default -> Material.WOOD_AXE;
        };
    }

    private boolean isAxe(Material type) {
        return type == Material.DIAMOND_AXE || type == Material.GOLD_AXE || type == Material.IRON_AXE || type == Material.STONE_AXE || type == Material.WOOD_AXE;
    }
}
