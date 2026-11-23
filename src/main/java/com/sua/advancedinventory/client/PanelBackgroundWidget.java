package com.sua.advancedinventory.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Shared Sakura glass backdrop and decorative motion layer for the two mod screens. */
final class PanelBackgroundWidget extends AbstractWidget {
    private static final Identifier PANEL_TEXTURE = Identifier.fromNamespaceAndPath(
        "advanced_inventory", "textures/gui/sakura_panel.png"
    );
    private static final Identifier PETAL_TEXTURE = Identifier.fromNamespaceAndPath(
        "advanced_inventory", "textures/gui/sakura_petal.png"
    );
    private static final int TEXTURE_WIDTH = 700;
    private static final int TEXTURE_HEIGHT = 410;

    private final int panelX;
    private final int panelY;
    private final int panelWidth;
    private final int panelHeight;

    PanelBackgroundWidget(int screenWidth, int screenHeight,
                          int panelX, int panelY, int panelWidth, int panelHeight) {
        super(0, 0, screenWidth, screenHeight, Component.empty());
        this.panelX = panelX;
        this.panelY = panelY;
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        this.active = false;
        this.setFocused(false);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, this.width, this.height, 0x820A0610);
        graphics.blit(RenderPipelines.GUI_TEXTURED, PANEL_TEXTURE,
            panelX, panelY, 0.0F, 0.0F, panelWidth, panelHeight,
            TEXTURE_WIDTH, TEXTURE_HEIGHT);

        if (panelWidth >= 680) {
            drawInventoryCards(graphics);
        } else {
            drawEnchantmentCards(graphics);
        }
        drawPetals(graphics);
        drawAmbientSparkles(graphics);
    }

    private void drawInventoryCards(GuiGraphicsExtractor graphics) {
        // Left navigation, item browser, search strip, and selected-item inspector.
        card(graphics, panelX + 10, panelY + 55, 112, 268,
            0x92301D37, 0xB8755376, 0x66F2C6D9);
        card(graphics, panelX + 124, panelY + 72, 322, 228,
            0x94301D38, 0xB8755376, 0x66F2C6D9);
        card(graphics, panelX + 124, panelY + 27, 322, 39,
            0xA52B1933, 0xB8755376, 0x88F2C6D9);
        card(graphics, panelX + 474, panelY + 48, 174, 282,
            0x96301D37, 0xC07E5A7C, 0x66F2C6D9);
        card(graphics, panelX + 12, panelY + 350, 676, 40,
            0x79301D37, 0x8D805A7B, 0x44F2C6D9);

        // Fine separators make the three working areas read as one designed interface.
        graphics.fill(panelX + 130, panelY + 75, panelX + 440, panelY + 76, 0x77E8AFC9);
        graphics.fill(panelX + 482, panelY + 130, panelX + 640, panelY + 131, 0x668D668C);
        graphics.fill(panelX + 482, panelY + 220, panelX + 640, panelY + 221, 0x668D668C);
        graphics.fill(panelX + 23, panelY + 321, panelX + 109, panelY + 322, 0x668D668C);
    }

    private void drawEnchantmentCards(GuiGraphicsExtractor graphics) {
        card(graphics, panelX + 14, panelY + 37, panelWidth - 28, 32,
            0xA52B1933, 0xB8755376, 0x88F2C6D9);

        for (int column = 0; column < 2; column++) {
            for (int row = 0; row < 8; row++) {
                int x = panelX + 14 + column * 300;
                int y = panelY + 78 + row * 31;
                card(graphics, x, y, 292, 27,
                    0x85301D38, 0x7D855E83, 0x44F2C6D9);
            }
        }

        card(graphics, panelX + 14, panelY + 343, panelWidth - 28, 32,
            0x79301D37, 0x8D805A7B, 0x44F2C6D9);
    }

    private static void card(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
                             int fill, int border, int highlight) {
        graphics.fill(x, y, x + width, y + height, fill);
        graphics.outline(x, y, width, height, border);
        graphics.fill(x + 8, y + 2, x + width - 8, y + 3, highlight);
    }

    private void drawPetals(GuiGraphicsExtractor graphics) {
        long now = System.currentTimeMillis();
        int usableHeight = Math.max(1, panelHeight - 78);
        for (int i = 0; i < 22; i++) {
            double speed = 0.22 + (i % 5) * 0.055;
            double phase = ((now * speed + i * 613L) % 12000L) / 12000.0;
            double sway = Math.sin(now * 0.00085 + i * 1.37) * (5 + (i % 4) * 2);
            int y = panelY + 42 + (int) (phase * usableHeight);
            int lane = i % 4;
            int baseX;
            if (lane == 0) baseX = panelX + 10 + (i % 3) * 9;
            else if (lane == 1) baseX = panelX + panelWidth - 25 - (i % 3) * 9;
            else if (lane == 2) baseX = panelX + 150 + (i % 5) * 52;
            else baseX = panelX + 285 + (i % 4) * 43;

            // Keep most petals near the edge so text fields and item icons remain clear.
            int x = baseX + (int) sway;
            if (lane >= 2) {
                y = panelY + 10 + (int) ((phase * 1.15) % 1.0 * 22);
            }
            int size = 5 + (i % 4) * 2;
            graphics.blit(RenderPipelines.GUI_TEXTURED, PETAL_TEXTURE,
                x, y, 0.0F, 0.0F, size, size, 32, 32);
        }
    }

    private void drawAmbientSparkles(GuiGraphicsExtractor graphics) {
        long now = System.currentTimeMillis();
        for (int i = 0; i < 9; i++) {
            double wave = 0.5 + 0.5 * Math.sin(now * 0.0011 + i * 1.9);
            int alpha = 38 + (int) (75 * wave);
            int color = (alpha << 24) | 0xF4C7DC;
            int x = panelX + 38 + (i * 79) % Math.max(1, panelWidth - 76);
            int y = panelY + 8 + (i * 37) % 27;
            graphics.fill(x, y, x + 2, y + 2, color);
            graphics.fill(x - 1, y + 1, x + 3, y + 2, color);
        }

        int pulse = (int) (22 + 17 * (0.5 + 0.5 * Math.sin(now * 0.00135)));
        int tint = (pulse << 24) | 0xF0B8D0;
        graphics.fill(panelX + 42, panelY + 4, panelX + panelWidth - 42, panelY + 6, tint);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        // Decorative layer; it has no narration or interaction.
    }
}
