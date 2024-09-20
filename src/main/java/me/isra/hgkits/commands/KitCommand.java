package me.isra.hgkits.commands;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.data.KitInventory;
import me.isra.hgkits.managers.KitManager;
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

import java.util.Map;

public class KitCommand implements CommandExecutor {

    private final KitManager kitManager;

    public KitCommand(KitManager kitManager) {
        this.kitManager = kitManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (command.getName().equalsIgnoreCase("kit")) {
            if(HGKits.GAMESTATE == GameState.PREGAME) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    openKitMenu(player);
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "Este comando solo puede ser ejecutado por un jugador.");
                    return false;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "No puedes elegir kit una vez la partida ha empezado!.");
                return false;
            }
        }
        return false;
    }

    private static final KitInventory HOLDER = new KitInventory();

    private void openKitMenu(Player player) {
        Inventory menu = Bukkit.createInventory(HOLDER, 54, "Selecciona tu KIT");

        Map<String, Kit> kits = kitManager.getAllKits();

        int slot = 0;

        for (Map.Entry<String, Kit> entry : kits.entrySet()) {
            String kitName = entry.getKey();
            ItemStack item = getItemIcon(kitName, kitManager);
            menu.setItem(slot, item);
            slot++;
        }

        player.openInventory(menu);
    }

    private static ItemStack getItemIcon(String kitName, KitManager kitManager) {
        ItemStack item = null;
        ItemMeta meta;
        Kit kit = kitManager.getKit(kitName);

        switch (kitName) {
            case "Asesino":
                item = new ItemStack(Material.IRON_SWORD);
                break;
            case "Camaleon":
                item = new ItemStack(Material.APPLE);
                break;
            case "Sonic":
                item = new ItemStack(Material.FEATHER);
                break;
            case "Arquero":
            case "Proarquero":
                item = new ItemStack(Material.ARROW);
                break;
            case "Encantador":
                item = new ItemStack(Material.ENCHANTMENT_TABLE);
                break;
            case "Hulk":
            case "Explorador":
                item = new ItemStack(Material.STONE_SWORD);
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
            case "Brujo":
                item = new ItemStack(Material.POTION);
                break;
            case "Piromano":
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
                item = new ItemStack(Material.IRON_CHESTPLATE);
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
            case "Headshooter":
                item = new ItemStack(Material.BOW);
                break;
            case "Elite":
                item = new ItemStack(Material.SKULL_ITEM);
                break;
            case "Flash":
                item = new ItemStack(Material.REDSTONE_TORCH_ON);
                break;
            case "Thor":
                item = new ItemStack(Material.DIAMOND_AXE);
                break;
            case "Saltamontes":
                item = new ItemStack(Material.DIAMOND_BOOTS);
                break;
            case "Meduza":
                item = new ItemStack(Material.WATCH);
                break;
            default:
                item = new ItemStack(Material.FEATHER);
                break;
        }

        if (item != null) {
            meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + kitName + ChatColor.RESET);

                meta.setLore(kit.getLore());
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

                item.setItemMeta(meta);
            }
        }

        return item;
    }
}
