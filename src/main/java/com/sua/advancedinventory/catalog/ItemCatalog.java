package com.sua.advancedinventory.catalog;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Registry-backed catalogue. Every registered item from vanilla and installed mods is
 * discovered from the live item registry. Weapon/explosive-oriented identifiers are
 * deliberately filtered out; all other registered mod items remain searchable.
 */
public final class ItemCatalog {
    private static final List<String> BLOCKED_ID_PARTS = List.of(
        "sword", "bow", "crossbow", "trident", "mace", "shield",
        "arrow", "knife", "dagger", "spear", "gun", "rifle", "pistol",
        "launcher", "cannon", "grenade", "bullet", "ammo", "warhammer",
        "battleaxe", "musket", "firearm", "explosive", "blade", "weapon",
        "tnt", "end_crystal", "rocket", "shotgun", "sniper", "revolver",
        "machinegun", "machine_gun", "turret", "bomb", "mine",
        "ak47", "ak74", "ar15", "m4a1", "m16", "mp5", "uzi", "smg",
        "assault_rifle", "submachine", "shotgun", "sniper", "revolver",
        "bayonet", "katana", "scythe", "halberd", "lance", "flamethrower",
        "artillery", "mortar", "missile", "warhead", "tacz", "cgm",
        "vic_modernwarfare", "modernwarfare", "weaponry", "firearms"
    );

    // Initialized lazily when /inv is first opened, after the game has loaded mod registries.
    private static final List<Item> ITEMS = loadItems();

    private ItemCatalog() { }

    /** Snapshot of all compatible entries in the live registry, including installed mods. */
    private static List<Item> loadItems() {
        return BuiltInRegistries.ITEM.stream()
            .filter(item -> item != Items.AIR)
            .filter(ItemCatalog::isAllowed)
            .sorted(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()))
            .toList();
    }

    public static List<Item> all() {
        return ITEMS;
    }

    public static boolean isAllowed(Item item) {
        if (item == null || item == Items.AIR) return false;
        String id = BuiltInRegistries.ITEM.getKey(item).toString().toLowerCase(Locale.ROOT);
        for (String blocked : BLOCKED_ID_PARTS) {
            if (id.contains(blocked)) return false;
        }
        return true;
    }

    public static boolean isModded(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString();
        return !id.startsWith("minecraft:");
    }

    public static int moddedCount() {
        return (int) ITEMS.stream().filter(ItemCatalog::isModded).count();
    }

    public static boolean matchesCategory(Item item, String category) {
        if ("all".equals(category)) return true;
        if ("modded".equals(category)) return isModded(item);
        if ("vanilla".equals(category)) return !isModded(item);

        String key = BuiltInRegistries.ITEM.getKey(item).toString();
        String path = key.substring(key.indexOf(':') + 1).toLowerCase(Locale.ROOT);

        return switch (category) {
            case "blocks" -> item instanceof BlockItem;
            case "food" -> path.contains("apple") || path.contains("bread")
                || path.contains("beef") || path.contains("pork") || path.contains("chicken")
                || path.contains("carrot") || path.contains("potato") || path.contains("stew")
                || path.contains("soup") || path.contains("fish") || path.contains("cookie")
                || path.contains("melon") || path.contains("berries") || path.contains("pie")
                || path.contains("rabbit") || path.contains("mutton") || path.contains("cod")
                || path.contains("salmon") || path.contains("honey_bottle");
            case "tools" -> path.endsWith("_pickaxe") || path.endsWith("_shovel")
                || path.endsWith("_hoe") || path.contains("shears") || path.contains("fishing_rod")
                || path.contains("brush") || path.contains("flint_and_steel")
                || path.contains("compass") || path.contains("clock");
            case "redstone" -> path.contains("redstone") || path.contains("repeater")
                || path.contains("comparator") || path.contains("piston") || path.contains("observer")
                || path.contains("hopper") || path.contains("lever") || path.contains("pressure_plate")
                || path.contains("rail") || path.contains("target") || path.contains("dispenser")
                || path.contains("dropper") || path.contains("daylight_detector");
            case "misc" -> !(item instanceof BlockItem)
                && !matchesCategory(item, "food")
                && !matchesCategory(item, "tools")
                && !matchesCategory(item, "redstone");
            default -> true;
        };
    }

    public static boolean matchesSearch(Item item, String query) {
        if (query == null || query.isBlank()) return true;
        String normalized = query.toLowerCase(Locale.ROOT).trim();
        String id = BuiltInRegistries.ITEM.getKey(item).toString().toLowerCase(Locale.ROOT);
        String name = new ItemStack(item).getHoverName().getString().toLowerCase(Locale.ROOT);
        return id.contains(normalized) || name.contains(normalized);
    }
}
