package com.sua.advancedinventory.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/** A non-interactive first-layer widget that paints the screen dimmer and main panel. */
final class PanelBackgroundWidget extends AbstractWidget {
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
        graphics.fill(0, 0, this.width, this.height, 0x98060A12);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xF0182230);
        graphics.outline(panelX, panelY, panelWidth, panelHeight, 0xFF637B96);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 34, 0xFF26384D);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        // Decorative layer; it has no narration or interaction.
    }
}
