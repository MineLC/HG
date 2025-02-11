package me.isra.hgkits.commands;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.database.DatabaseManager;
import me.isra.hgkits.database.User;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.data.KitInventory;
import me.isra.hgkits.managers.KitManager;
import me.isra.hgkits.translate.TranslateManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KitCommand implements CommandExecutor {

    private final KitManager kitManager;
    private final TranslateManager translateManager;

    public KitCommand(KitManager kitManager, TranslateManager translateManager) {
        this.kitManager = kitManager;
        this.translateManager = translateManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (command.getName().equalsIgnoreCase("kit")) {
            if(HGKits.GAMESTATE == GameState.PREGAME) {
                if (sender instanceof Player player) {
                    openKitMenu(player);
                    return true;
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("player-only-command")));
                    return false;
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("game-already-started")));
                return false;
            }
        }
        return false;
    }

    private static final KitInventory HOLDER = new KitInventory();

    private void openKitMenu(Player player) {
        Inventory menu = Bukkit.createInventory(HOLDER, 54, ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("kit-menu-title")));

        Map<String, Kit> kits = kitManager.getAllKits();

        int slot = 0;
        int slot2 = 53;

        User user = DatabaseManager.getDatabase().getCached(player.getUniqueId());

        for (Map.Entry<String, Kit> entry : kits.entrySet()) {
            String kitName = entry.getKey();
            if(kitName.equals("Default")) continue;
            //player.sendMessage(kitName);

            ItemStack item = getItemIcon(kitName, kitManager, user);

            if((entry.getValue().cost() > 0 && !user.purchasedKits.contains(kitName)) && !DatabaseManager.getDatabase().getCached(player.getUniqueId()).allKits) {
                menu.setItem(slot2, item);
                slot2--;
            } else {
                menu.setItem(slot, item);
                slot++;
            }
        }

        player.openInventory(menu);
    }

    private static ItemStack getItemIcon(String kitName, KitManager kitManager, User user) {
        ItemStack item;
        ItemMeta meta;
        Kit kit = kitManager.getKit(kitName);

        switch (kitName) {
            case "Asesino":
                item = new ItemStack(Material.IRON_SWORD);
                break;
            case "Camaleon":
                item = new ItemStack(Material.APPLE);
                break;
            case "Cambiador":
                item = new ItemStack(Material.SNOW_BALL);
                break;
            case "Arquero":
            case "Proarquero":
                item = new ItemStack(Material.ARROW);
                break;
            case "Encantador":
                item = new ItemStack(Material.ENCHANTMENT_TABLE);
                break;
            case "Hulk":
                item = new ItemStack(Material.STONE_SWORD);
                break;
            case "Explorador":
                item = new ItemStack(Material.DIAMOND_BOOTS);
                break;
            case "Barbaro":
                item = new ItemStack(Material.STONE_AXE);
                break;
            case "Minero":
                item = new ItemStack(Material.STONE_PICKAXE);
                break;
            case "Enderman":
                item = new ItemStack(Material.EYE_OF_ENDER);
                break;
            case "Domabestias":
            case "Domabestiaspro":
                item = new ItemStack(Material.MONSTER_EGG);
                break;
            case "Paladín":
                item = new ItemStack(Material.IRON_CHESTPLATE);
                break;
            case "Brujo":
                item = new ItemStack(Material.POTION);
                break;
            case "Piromano":
                item = new ItemStack(Material.FLINT_AND_STEEL);
                break;
            case "Pyro":
                item = new ItemStack(Material.FIREBALL);
                break;
            case "Guerrero":
                item = new ItemStack(Material.CHAINMAIL_LEGGINGS);
                break;
            case "Curandero":
                item = new ItemStack(Material.BOWL);
                break;
            case "Orco":
                item = new ItemStack(Material.GOLD_AXE);
                break;
            case "Ladron":
                item = new ItemStack(Material.STICK);
                break;
            case "Creeper":
                item = new ItemStack(Material.REDSTONE);
                break;
            case "Ultracreeper":
                item = new ItemStack(Material.TNT);
                break;
            case "Canibal":
                item = new ItemStack(Material.MUSHROOM_SOUP);
                break;
            case "Matasanos":
                item = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
                break;
            case "Caballero":
                item = new ItemStack(Material.COOKIE);
                break;
            case "Proladron":
                item = new ItemStack(Material.TRIPWIRE_HOOK);
                break;
            case "Coloso":
                item = new ItemStack(Material.IRON_BLOCK);
                break;
            case "Destructor":
                item = new ItemStack(Material.BLAZE_ROD);
                break;
            case "Serpiente":
                item = new ItemStack(Material.SOUL_SAND);
                break;
            case "Kratos":
                item = new ItemStack(Material.IRON_BARDING);
                break;
            case "Ironman":
                item = new ItemStack(Material.IRON_ORE);
                break;
            case "Prominero":
                item = new ItemStack(Material.DIAMOND_PICKAXE);
                break;
            case "Escudero":
                item = new ItemStack(Material.GOLD_SWORD);
                break;
            case "Tanque":
                item = new ItemStack(Material.GOLDEN_APPLE);
                break;
            case "Troll":
                item = new ItemStack(Material.WEB);
                break;
            case "Spiderman":
                item = new ItemStack(Material.STRING);
                break;
            case "Ultimátum":
                item = new ItemStack(Material.GLASS);
                break;
            case "Headshooter":
                item = new ItemStack(Material.BOW);
                break;
            case "Gusano":
                item = new ItemStack(Material.DIRT);
                break;
            case "Elite":
                item = new ItemStack(Material.SKULL_ITEM);
                break;
            case "Flash":
                item = new ItemStack(Material.REDSTONE_TORCH_ON);
                break;
            case "Vikingo":
                item = new ItemStack(Material.IRON_AXE);
                break;
            case "Thor":
                item = new ItemStack(Material.DIAMOND_AXE);
                break;
            case "Saltamontes":
                item = new ItemStack(Material.FIREWORK);
                break;
            case "Medusa":
                item = new ItemStack(Material.WATCH);
                break;
            default:
                item = new ItemStack(Material.FEATHER);
                break;
        }

        meta = item.getItemMeta();
        if (meta != null) {

            meta.setDisplayName(((kit.cost() > 0 && !user.purchasedKits.contains(kitName) && !user.allKits) ? ChatColor.RED : ChatColor.GREEN) + kitName + ChatColor.RESET);

            List<String> lore = new ArrayList<>();
            if(kit.cost() > 0 && !user.purchasedKits.contains(kitName)){
                lore.add(" ");
                lore.add(ChatColor.GRAY+"Costo: "+ChatColor.YELLOW+"$"+kit.cost()+" LCoins");
                lore.add(" ");
            }
            lore.addAll(kit.lore());
            lore.add(" ");
            lore.add(ChatColor.YELLOW+"¡Click Izquierdo para seleccionar!");

            if(kit.cost() > 0 && !user.purchasedKits.contains(kitName)){
                lore.add(ChatColor.YELLOW+"¡Click Derecho para comprar!");
            }

            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            item.setItemMeta(meta);
        }

        return item;
    }
}
