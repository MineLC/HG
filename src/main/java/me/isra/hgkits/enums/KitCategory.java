package me.isra.hgkits.enums;

import java.util.HashMap;
import java.util.Map;

public enum KitCategory {
    DEFAULT("chg.default"),
    VIP("chg.vip"),
    SVIP("chg.svip"),
    ELITE("chg.elite"),
    OPTIMUM("chg.optimum");

    private final String permission;
    private static final Map<String, KitCategory> KIT_MAP = new HashMap<>();

    // Constructor del enum
    KitCategory(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

    // Registrar los kits en un Map estático para búsqueda rápida
    static {
        registerKits(DEFAULT, "default", "sonic", "piromano", "enderman", "orco", "minero", "ladron",
                "hulk", "guerrero", "explorador", "encantador", "domabestias", "curandero",
                "creeper", "camaleon", "brujo", "barbaro", "asesino", "arquero");
        
        registerKits(VIP, "ultracreeper", "domabestiaspro", "escudero", "headshooter", 
                "ironman", "prominero", "proladron", "thor");

        registerKits(SVIP, "tanque", "saltamontes", "proarquero", "matasanos", "escudero", 
                "caballero", "pyro");

        registerKits(ELITE, "elite", "flash", "meduza", "troll", "canibal");

        registerKits(OPTIMUM, "spiderman", "coloso", "kratos");
    }

    // Método auxiliar para registrar kits en el Map
    private static void registerKits(KitCategory category, String... kits) {
        for (String kit : kits) {
            KIT_MAP.put(kit.toLowerCase(), category);
        }
    }

    // Método para obtener la categoría de un kit por su nombre
    public static KitCategory fromKitName(String kitName) {
        return KIT_MAP.get(kitName.toLowerCase());
    }
}

