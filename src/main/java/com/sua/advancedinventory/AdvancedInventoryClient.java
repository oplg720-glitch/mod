package com.sua.advancedinventory;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import com.sua.advancedinventory.client.InventoryScreen;

/** Client-only entry point. The feature is deliberately limited to Singleplayer. */
public final class AdvancedInventoryClient implements ClientModInitializer {
    public static final String MOD_ID = "advanced_inventory";

    @Override
    public void onInitializeClient() {
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
