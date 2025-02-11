package me.isra.hgkits.managers;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class KitManager {
    private HGKits plugin;

    private final Map<String, Kit> kits = new HashMap<>();
    private final Map<Player, Kit> selectedKits = new HashMap<>();

    public KitManager(HGKits plugin) {
        this.plugin = plugin;
    }

    public void addKit(String name, Kit kit) {
        kits.put(name, kit);
    }

    public Kit getKit(String name) {
        return kits.get(name);
    }

    public Map<String, Kit> getAllKits() {
        return new HashMap<>(kits);
    }

    public void addSelectedKit(Player player, Kit kit) {
        selectedKits.put(player, kit);
    }

    public Set<Map.Entry<Player, Kit>> getSelectedKits() {
        return selectedKits.entrySet();
    }

    public Kit getKitByPlayer(Player p) {
        return selectedKits.get(p);
    }

    public void loadKits() {
        for (Map.Entry<Player, Kit> entry : selectedKits.entrySet()) {
            Player p = entry.getKey();

            plugin.removeInventory(p);

            Kit k = entry.getValue();

            for (String objetoYcantidad : k.items()) {
                String[] fields = objetoYcantidad.split(":");
                String objeto = fields[0];
                int cantidad = Integer.parseInt(fields[1]);

                Material material = Material.matchMaterial(objeto.toUpperCase());
                if (material != null) {
                    if (material == Material.MONSTER_EGG) {
                        String eggType = fields[2];
                        handleMonsterEggs(p, cantidad, eggType);
                    } else if (material == Material.POTION) {
                        String potionType = fields[2];
                        handlePotions(p, cantidad, potionType);
                    } else {
                        if (fields.length > 2) {
                            String enchant = fields[2];
                            int level = Integer.parseInt(fields[3]);
                            handleEnchants(p, material, cantidad, enchant, level);
                        } else {
                            handleItem(p, material, cantidad);
                        }
                    }
                }
            }

            for (String efecto : k.effects()) {
                String[] fields = efecto.split(":");
                String tipoEfecto = fields[0];

                if (!tipoEfecto.equals("No effects")) {
                    int duracion = Integer.parseInt(fields[1]);
                    int nivel = Integer.parseInt(fields[2]);

                    if (duracion == -1) {
                        duracion = Integer.MAX_VALUE;
                    } else {
                        duracion *= 20;
                    }

                    PotionEffectType potionEffectType = PotionEffectType.getByName(tipoEfecto.toUpperCase());
                    if (potionEffectType != null) {
                        PotionEffect potionEffect = new PotionEffect(potionEffectType, duracion, nivel);
                        p.addPotionEffect(potionEffect);
                    } else {
                        p.sendMessage("El tipo de efecto " + tipoEfecto + " no es válido.");
                    }
                }
            }

            ItemStack compass = new ItemBuilder(Material.COMPASS)
                    .setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getTranslateManager().getMessage("compass-title")))
                    .addLore(ChatColor.translateAlternateColorCodes('&', plugin.getTranslateManager().getMessage("compass-lore-left")))
                    .addLore(ChatColor.translateAlternateColorCodes('&', plugin.getTranslateManager().getMessage("compass-lore-right")))
                    .build();
            p.getInventory().addItem(compass);
        }
    }

    private void addItemToPlayer(ItemStack itemStack, Player player) {
        if (isArmor(itemStack)) {
            equipArmor(player, itemStack);
        } else {
            player.getInventory().addItem(itemStack);
        }
    }

    private void handleItem(Player p, Material material, int cantidad) {
        ItemStack itemStack = new ItemStack(material, cantidad);

        addItemToPlayer(itemStack, p);
    }

    private void handlePotions(Player p, int cantidad, String potionName) {

        // (short) 16384 hace que la pocion sea arrojadiza.
        ItemStack potionItem = new ItemStack(Material.POTION, cantidad, (short) 16384);
        PotionMeta meta = (PotionMeta) potionItem.getItemMeta();

        switch (potionName) {
            case "SPEED":
                meta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(potionName), 20 * 90, 1), true);
                break;
            case "POISON":
                meta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(potionName), 20 * 12, 1), true);
                break;
            case "HEAL":
            case "HARM":
                meta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(potionName), 1, 1), true);
                break;
            case "INCREASE_DAMAGE":
                meta.addCustomEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 90, 0), true);
            default:
                break;
        }
        meta.setDisplayName("Poción de " + potionName);
        potionItem.setItemMeta(meta);

        addItemToPlayer(potionItem, p);
    }

    private void handleMonsterEggs(Player p, int cantidad, String eggTypeStr) {
        short eggType = Short.parseShort(eggTypeStr);
        ItemStack egg = new ItemStack(Material.MONSTER_EGG, cantidad, eggType);

        addItemToPlayer(egg, p);
    }

    private void handleEnchants(Player p, Material material, int cantidad, String enchant, int level) {
        ItemStack itemStack = new ItemStack(material, cantidad);
        Enchantment enchantment = Enchantment.getByName(enchant.toUpperCase());
        Kit kit = getKitByPlayer(p);

        if (enchantment != null) {
            switch (material) {
                case DIAMOND_PICKAXE:
                    if (kit.name().equals("Prominero")) {
                        itemStack.addUnsafeEnchantment(enchantment, level);
                    }
                    break;
                case GOLDEN_APPLE:
                    if (kit.name().equals("Troll")) {
                        itemStack.addUnsafeEnchantment(enchantment, level);
                    }
                    break;
                default:
                    itemStack.addEnchantment(enchantment, level);
            }
        } else {
            p.sendMessage("Encantamiento no válido: " + enchant);
        }

        addItemToPlayer(itemStack, p);
    }

    private boolean isArmor(ItemStack itemStack) {
        switch (itemStack.getType()) {
            case DIAMOND_HELMET:
            case CHAINMAIL_HELMET:
            case IRON_HELMET:
            case GOLD_HELMET:
            case LEATHER_HELMET:

            case DIAMOND_CHESTPLATE:
            case CHAINMAIL_CHESTPLATE:
            case IRON_CHESTPLATE:
            case GOLD_CHESTPLATE:
            case LEATHER_CHESTPLATE:

            case DIAMOND_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
            case IRON_LEGGINGS:
            case GOLD_LEGGINGS:
            case LEATHER_LEGGINGS:

            case DIAMOND_BOOTS:
            case CHAINMAIL_BOOTS:
            case IRON_BOOTS:
            case GOLD_BOOTS:
            case LEATHER_BOOTS:
                return true;

            default:
                return false;
        }
    }

    private void equipArmor(Player player, ItemStack armor) {
        PlayerInventory inventory = player.getInventory();

        Kit kit = getKitByPlayer(player);

        // Definir los colores para cada tipo de armadura de cuero SOLO si es Spiderman
        Map<Material, Color> armorColors = new HashMap<>();
        if (kit != null && kit.name().equalsIgnoreCase("Spiderman")) {
            armorColors.put(Material.LEATHER_HELMET, Color.RED);
            armorColors.put(Material.LEATHER_CHESTPLATE, Color.BLUE);
            armorColors.put(Material.LEATHER_LEGGINGS, Color.RED);
            armorColors.put(Material.LEATHER_BOOTS, Color.BLUE);
        }

        // Verificar si el armor es cuero
        if (armor.getType().toString().contains("LEATHER")) {
            LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta();

            // Establecer el color según el tipo de armadura
            Color color = armorColors.get(armor.getType());
            if (color != null) {
                meta.setColor(color);
                armor.setItemMeta(meta);
            }
        }

        // Asignar la armadura al inventario del jugador
        switch (armor.getType()) {
            case DIAMOND_HELMET:
            case CHAINMAIL_HELMET:
            case IRON_HELMET:
            case GOLD_HELMET:
            case LEATHER_HELMET:
                inventory.setHelmet(armor);
                break;

            case DIAMOND_CHESTPLATE:
            case CHAINMAIL_CHESTPLATE:
            case IRON_CHESTPLATE:
            case GOLD_CHESTPLATE:
            case LEATHER_CHESTPLATE:
                inventory.setChestplate(armor);
                break;

            case DIAMOND_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
            case IRON_LEGGINGS:
            case GOLD_LEGGINGS:
            case LEATHER_LEGGINGS:
                inventory.setLeggings(armor);
                break;

            case DIAMOND_BOOTS:
            case CHAINMAIL_BOOTS:
            case IRON_BOOTS:
            case GOLD_BOOTS:
            case LEATHER_BOOTS:
                inventory.setBoots(armor);
                break;

            default:
                // Si no es un tipo de armadura válido, no hacer nada.
                break;
        }
    }
}
