package com.sua.advancedinventory.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Only non-combat utility/defensive enchantments are offered here. */
public final class EnchantmentScreen extends Screen {
    private static final int PANEL_WIDTH = 610;
    private static final int PANEL_HEIGHT = 390;
    private static final List<String> ENCHANTMENTS = List.of(
        "unbreaking", "mending", "efficiency", "silk_touch", "fortune",
        "aqua_affinity", "respiration", "feather_falling", "depth_strider",
        "frost_walker", "soul_speed", "swift_sneak", "luck_of_the_sea", "lure",
        "protection", "fire_protection", "blast_protection", "projectile_protection"
    );

    private final InventoryScreen parent;
    private final Map<String, Integer> selected;
    private final Map<String, EditBox> levelFields = new LinkedHashMap<>();

    public EnchantmentScreen(InventoryScreen parent) {
        super(Component.literal("Utility Enchantments"));
        this.parent = parent;
        this.selected = parent.copyEnchantments();
    }

    @Override
    protected void init() {
        int left = Math.max(8, (this.width - PANEL_WIDTH) / 2);
        int top = Math.max(8, (this.height - PANEL_HEIGHT) / 2);

        // Keep the decorative panel beneath the buttons and level fields.
        this.addRenderableWidget(new PanelBackgroundWidget(
            this.width, this.height, left, top, PANEL_WIDTH, PANEL_HEIGHT
        ));

        for (int i = 0; i < ENCHANTMENTS.size(); i++) {
            String id = ENCHANTMENTS.get(i);
            int column = i / 9;
            int row = i % 9;
            int x = left + 20 + column * 292;
            int y = top + 58 + row * 30;
            boolean active = selected.containsKey(id);

            Button toggle = Button.builder(Component.literal(active ? "ON" : "OFF"), button -> {
                if (selected.containsKey(id)) {
                    selected.remove(id);
                    button.setMessage(Component.literal("OFF"));
                } else {
                    selected.put(id, 1);
                    button.setMessage(Component.literal("ON"));
                }
            }).bounds(x + 165, y, 46, 20).build();
            this.addRenderableWidget(toggle);

            EditBox level = new EditBox(this.font, x + 218, y, 48, 20, Component.literal("Level"));
            level.setMaxLength(3);
            level.setValue(Integer.toString(selected.getOrDefault(id, 1)));
            levelFields.put(id, level);
            this.addRenderableWidget(level);
        }

        this.addRenderableWidget(Button.builder(Component.literal("Save"), button -> saveAndReturn())
            .bounds(left + PANEL_WIDTH - 190, top + PANEL_HEIGHT - 38, 78, 22).build());
        this.addRenderableWidget(Button.builder(Component.literal("Clear all"), button -> {
            selected.clear();
            parent.setEnchantments(Map.of());
            Minecraft.getInstance().setScreen(parent);
        }).bounds(left + PANEL_WIDTH - 102, top + PANEL_HEIGHT - 38, 82, 22).build());
    }

    private void saveAndReturn() {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (String id : ENCHANTMENTS) {
            if (!selected.containsKey(id)) continue;
            int level = 1;
            try {
                level = Integer.parseInt(levelFields.get(id).getValue().trim());
            } catch (NumberFormatException ignored) {
                // Keep a safe default for an empty or malformed field.
            }
            result.put(id, Math.max(1, Math.min(255, level)));
        }
        parent.setEnchantments(result);
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int left = Math.max(8, (this.width - PANEL_WIDTH) / 2);
        int top = Math.max(8, (this.height - PANEL_HEIGHT) / 2);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.text(this.font, Component.literal("UTILITY ENCHANTMENTS"), left + 18, top + 12, 0xFFFFFFFF, true);
        graphics.text(this.font, Component.literal("Choose non-combat enchantments. Levels are saved from 1 to 255."),
            left + 20, top + 40, 0xFFB8CBE0, false);

        for (int i = 0; i < ENCHANTMENTS.size(); i++) {
            String id = ENCHANTMENTS.get(i);
            int column = i / 9;
            int row = i % 9;
            int x = left + 20 + column * 292;
            int y = top + 64 + row * 30;
            String label = toDisplayName(id);
            graphics.text(this.font, Component.literal(label), x, y + 6, 0xFFE5EDF5, true);
        }
    }

    private static String toDisplayName(String id) {
        String[] parts = id.split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return result.toString();
    }
}
