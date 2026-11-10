package com.sua.advancedinventory.service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.sua.advancedinventory.catalog.ItemCatalog;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.network.chat.Component;

/** Creates item entities on the integrated server thread; never edits the client inventory. */
public final class ItemDropService {
    public static final int MAX_QUANTITY = 4096;
    private static final int MAX_ENTITIES_PER_REQUEST = 128;

    private static final Set<String> SAFE_ENCHANTMENTS = Set.of(
        "unbreaking", "mending", "efficiency", "silk_touch", "fortune",
        "aqua_affinity", "respiration", "feather_falling", "depth_strider",
        "frost_walker", "soul_speed", "swift_sneak", "luck_of_the_sea", "lure",
        "protection", "fire_protection", "blast_protection", "projectile_protection"
    );

    private ItemDropService() { }

    public static boolean isAllowedEnchantment(String id) {
        return SAFE_ENCHANTMENTS.contains(id);
    }

    /** Returns the safe per-request quantity for an item, respecting its stack size and entity cap. */
    public static int maxQuantityFor(Item item) {
        int stackLimit = Math.max(1, new ItemStack(item).getMaxStackSize());
        return Math.min(MAX_QUANTITY, stackLimit * MAX_ENTITIES_PER_REQUEST);
    }

    public static void drop(Item item, int requestedAmount, Map<String, Integer> enchantments) {
        Minecraft client = Minecraft.getInstance();
        MinecraftServer server = client.getSingleplayerServer();

        if (server == null || client.player == null || !ItemCatalog.isAllowed(item)) {
            notifyClient("Không thể tạo vật phẩm: chỉ hỗ trợ vật phẩm không chiến đấu trong Singleplayer.");
            return;
        }

        int itemLimit = maxQuantityFor(item);
        if (requestedAmount < 1 || requestedAmount > itemLimit) {
            notifyClient("Số lượng vật phẩm này phải nằm trong khoảng 1–" + itemLimit + ".");
            return;
        }

        UUID playerId = client.player.getUUID();
        server.execute(() -> {
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player == null) return;

            ItemStack probe = new ItemStack(item);
            int stackLimit = Math.max(1, probe.getMaxStackSize());
            int entityCount = (requestedAmount + stackLimit - 1) / stackLimit;
            if (entityCount > MAX_ENTITIES_PER_REQUEST) {
                player.sendSystemMessage(Component.literal(
                    "Yêu cầu bị từ chối để tránh lag: vật phẩm này cần " + entityCount
                        + " entity, giới hạn hiện tại là " + MAX_ENTITIES_PER_REQUEST + "."
                ));
                return;
            }

            ItemEnchantments.Mutable enchantmentData = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                String enchantmentId = entry.getKey();
                int level = Math.max(1, Math.min(255, entry.getValue()));
                if (!SAFE_ENCHANTMENTS.contains(enchantmentId)) continue;

                ResourceKey<Enchantment> key = ResourceKey.create(
                    Registries.ENCHANTMENT,
                    Identifier.fromNamespaceAndPath("minecraft", enchantmentId)
                );
                try {
                    Holder<Enchantment> holder = server.registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(key);
                    enchantmentData.set(holder, level);
                } catch (RuntimeException ignored) {
                    // A missing enchantment should not abort creation of otherwise valid items.
                }
            }

            ServerLevel level = player.level();
            net.minecraft.world.item.enchantment.ItemEnchantments finalEnchantments = enchantmentData.toImmutable();
            int remaining = requestedAmount;
            int spawned = 0;
            while (remaining > 0) {
                int amount = Math.min(stackLimit, remaining);
                ItemStack stack = new ItemStack(item, amount);
                if (!finalEnchantments.isEmpty()) {
                    stack.set(DataComponents.ENCHANTMENTS, finalEnchantments);
                }

                double angle = spawned * 2.399963229728653;
                double radius = 0.55 + (spawned % 3) * 0.12;
                double x = player.getX() + Math.cos(angle) * radius;
                double y = player.getY() + 0.25;
                double z = player.getZ() + Math.sin(angle) * radius;

                ItemEntity entity = new ItemEntity(level, x, y, z, stack);
                entity.setPickUpDelay(10);
                entity.setDeltaMovement(
                    Math.cos(angle) * 0.035,
                    0.12,
                    Math.sin(angle) * 0.035
                );
                if (!level.addFreshEntity(entity)) {
                    player.sendSystemMessage(Component.literal(
                        "Chỉ thả thành công " + (requestedAmount - remaining) + "/" + requestedAmount
                            + " vật phẩm. Phần còn lại chưa được tạo do thế giới từ chối entity."
                    ));
                    return;
                }

                remaining -= amount;
                spawned++;
            }

            player.sendSystemMessage(Component.literal(
                "Đã thả " + requestedAmount + " × " + new ItemStack(item).getHoverName().getString()
                    + " thành " + spawned + " stack ngoài thế giới."
            ));
        });
    }

    private static void notifyClient(String message) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.player.sendSystemMessage(Component.literal(message));
        }
    }
}
