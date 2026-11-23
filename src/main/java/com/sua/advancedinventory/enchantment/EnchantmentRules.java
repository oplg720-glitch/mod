package com.sua.advancedinventory.enchantment;

import java.util.List;
import java.util.Locale;

/**
 * Shared policy for enchantments exposed by the Singleplayer item editor.
 * The editor intentionally focuses on utility, mobility, fishing and defensive
 * enchantments; combat/weapon-oriented enchantments are not exposed.
 */
public final class EnchantmentRules {
    private static final List<String> BLOCKED_ID_PARTS = List.of(
        "sharpness", "smite", "bane_of_arthropods", "power", "punch", "flame",
        "fire_aspect", "knockback", "looting", "sweeping_edge", "impaling",
        "channeling", "riptide", "loyalty", "quick_charge", "multishot",
        "piercing", "density", "breach", "wind_burst", "thorns",
        "damage", "attack", "weapon", "critical", "crit", "lifesteal",
        "bleed", "execute", "rage", "fury", "crossbow",
        "trident", "sword", "bow", "mace", "dagger", "spear", "gun"
    );

    private EnchantmentRules() { }

    /** Accepts either a namespaced ID (modid:path) or a vanilla path. */
    public static boolean isAllowed(String rawId) {
        if (rawId == null || rawId.isBlank()) return false;
        String id = rawId.toLowerCase(Locale.ROOT);
        String path = id.substring(id.indexOf(':') >= 0 ? id.indexOf(':') + 1 : 0);
        for (String blocked : BLOCKED_ID_PARTS) {
            if (path.contains(blocked)) return false;
        }
        return true;
    }
}
