package com.sua.advancedinventory;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.Command;
import com.sua.advancedinventory.client.InventoryScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Client-only entry point. The feature is deliberately limited to Singleplayer. */
public final class AdvancedInventoryClient implements ClientModInitializer {
    public static final String MOD_ID = "advanced_inventory";

    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath(MOD_ID, "general")
    );
    private static KeyMapping openInventoryKey;

    @Override
    public void onInitializeClient() {
        openInventoryKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.advanced_inventory.open",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_K,
            KEY_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openInventoryKey.consumeClick()) {
                if (client.player == null || client.getSingleplayerServer() == null) continue;
                if (client.screen instanceof InventoryScreen) {
                    client.setScreen(null);
                } else if (client.screen == null) {
                    client.setScreen(new InventoryScreen());
                }
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommands.literal("inv").executes(context -> {
                Minecraft client = Minecraft.getInstance();

                if (client.getSingleplayerServer() == null || client.player == null) {
                    context.getSource().sendFeedback(Component.literal(
                        "Advanced Inventory chỉ hoạt động trong Singleplayer."
                    ));
                    return 0;
                }

                client.execute(() -> client.setScreen(new InventoryScreen()));
                return Command.SINGLE_SUCCESS;
            }))
        );
    }
}
