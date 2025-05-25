package me.isra.hgkits.data;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;


public enum ServerRank {
    DEFAULT(null, null),
    VIP("&b&lVIP", "chg.vip"),
    SVIP("&a&lSVIP", "chg.svip"),
    ELITE("&6&lÉLITE", "chg.elite"),
    OPTIMUM("&c&lOPTIMUM", "chg.optimum");

    @Getter
    final @Nullable String prefix, permission;

    ServerRank(@Nullable String prefix, @Nullable String permission) {
        this.prefix = prefix;
        this.permission = permission;
    }
}
