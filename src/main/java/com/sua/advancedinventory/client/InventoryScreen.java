package com.sua.advancedinventory.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sua.advancedinventory.catalog.ItemCatalog;
import com.sua.advancedinventory.service.ItemDropService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Main searchable item browser. All item creation occurs on the integrated server. */
public final class InventoryScreen extends Screen {
    private static final int PANEL_WIDTH = 700;
    private static final int PANEL_HEIGHT = 410;
    private static final int GRID_COLUMNS = 9;
    private static final int GRID_ROWS = 5;
    private static final int CELL_SIZE = 34;
    private static final int ITEMS_PER_PAGE = GRID_COLUMNS * GRID_ROWS;

    private static final List<Category> CATEGORIES = List.of(
        new Category("all", "All items"),
        new Category("blocks", "Blocks"),
        new Category("food", "Food"),
        new Category("tools", "Tools"),
        new Category("redstone", "Redstone"),
        new Category("misc", "Miscellaneous")
    );

    private final Map<String, Integer> enchantments = new LinkedHashMap<>();
    private Item selectedItem = Items.STONE;
    private String selectedCategory = "all";
    private String searchText = "";
    private List<Item> filteredItems = new ArrayList<>();
    private int page;
    private EditBox searchBox;
    private EditBox quantityBox;

    public InventoryScreen() {
        super(Component.literal("Advanced Inventory"));
        updateFilteredItems();
    }

    @Override
    protected void init() {
        int left = panelLeft();
        int top = panelTop();

        // Add this first so the panel is extracted beneath all interactive widgets.
        this.addRenderableWidget(new PanelBackgroundWidget(
            this.width, this.height, left, top, PANEL_WIDTH, PANEL_HEIGHT
        ));

        searchBox = new EditBox(this.font, left + 130, top + 48, 315, 20, Component.literal("Search items"));
        searchBox.setMaxLength(80);
        searchBox.setValue(searchText);
        searchBox.setResponder(value -> {
            searchText = value;
            page = 0;
            updateFilteredItems();
        });
        this.addRenderableWidget(searchBox);

        quantityBox = new EditBox(this.font, left + 485, top + 136, 86, 20, Component.literal("Quantity"));
        quantityBox.setMaxLength(5);
        quantityBox.setValue("64");
        this.addRenderableWidget(quantityBox);

        int categoryY = top + 82;
        for (int i = 0; i < CATEGORIES.size(); i++) {
            Category category = CATEGORIES.get(i);
            Button button = Button.builder(Component.literal(category.label()), press -> {
                selectedCategory = category.id();
                page = 0;
                updateFilteredItems();
            }).bounds(left + 16, categoryY + i * 31, 100, 24).build();
            this.addRenderableWidget(button);
        }

        this.addRenderableWidget(Button.builder(Component.literal("-1"), button -> adjustQuantity(-1))
            .bounds(left + 485, top + 164, 48, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+1"), button -> adjustQuantity(1))
            .bounds(left + 537, top + 164, 48, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+16"), button -> adjustQuantity(16))
            .bounds(left + 485, top + 190, 48, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+64"), button -> adjustQuantity(64))
            .bounds(left + 537, top + 190, 48, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Enchantments"), button ->
            Minecraft.getInstance().setScreen(new EnchantmentScreen(this))
        ).bounds(left + 485, top + 226, 150, 22).build());
        this.addRenderableWidget(Button.builder(Component.literal("DROP ITEMS"), button -> dropSelectedItem())
            .bounds(left + 485, top + 265, 150, 28).build());
        this.addRenderableWidget(Button.builder(Component.literal("Previous"), button -> {
            page = Math.max(0, page - 1);
        }).bounds(left + 130, top + 294, 78, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Next"), button -> {
            int maxPage = Math.max(0, (filteredItems.size() - 1) / ITEMS_PER_PAGE);
            page = Math.min(maxPage, page + 1);
        }).bounds(left + 214, top + 294, 78, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("MAX"), button ->
            quantityBox.setValue(Integer.toString(maxQuantityForSelectedItem()))
        ).bounds(left + 589, top + 164, 46, 46).build());
        this.addRenderableWidget(Button.builder(Component.literal("Clear enchants"), button -> enchantments.clear())
            .bounds(left + 485, top + 300, 150, 20).build());
    }

    private int panelLeft() {
        return Math.max(8, (this.width - PANEL_WIDTH) / 2);
    }

    private int panelTop() {
        return Math.max(8, (this.height - PANEL_HEIGHT) / 2);
    }

    private int gridLeft() {
        return panelLeft() + 130;
    }

    private int gridTop() {
        return panelTop() + 82;
    }

    private void updateFilteredItems() {
        filteredItems = ItemCatalog.all().stream()
            .filter(item -> ItemCatalog.matchesCategory(item, selectedCategory))
            .filter(item -> ItemCatalog.matchesSearch(item, searchText))
            .toList();
        int maxPage = Math.max(0, (filteredItems.size() - 1) / ITEMS_PER_PAGE);
        page = Math.min(page, maxPage);
        if (filteredItems.isEmpty()) {
            selectedItem = null;
        } else if (!filteredItems.contains(selectedItem)) {
            selectedItem = filteredItems.get(0);
        }
    }

    private void adjustQuantity(int delta) {
        int current = readQuantity();
        int next = Math.max(1, Math.min(maxQuantityForSelectedItem(), current + delta));
        quantityBox.setValue(Integer.toString(next));
    }

    private int readQuantity() {
        try {
            int value = Integer.parseInt(quantityBox.getValue().trim());
            return Math.max(1, Math.min(maxQuantityForSelectedItem(), value));
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    private int maxQuantityForSelectedItem() {
        return selectedItem == null ? ItemDropService.MAX_QUANTITY : ItemDropService.maxQuantityFor(selectedItem);
    }

    private void dropSelectedItem() {
        if (selectedItem == null) return;
        int quantity;
        try {
            quantity = Integer.parseInt(quantityBox.getValue().trim());
        } catch (NumberFormatException exception) {
            showMessage("Enter a valid whole number for quantity.");
            return;
        }
        int maxForItem = maxQuantityForSelectedItem();
        if (quantity < 1 || quantity > maxForItem) {
            showMessage("Quantity for this item must be between 1 and " + maxForItem + ".");
            return;
        }
        ItemDropService.drop(selectedItem, quantity, Map.copyOf(enchantments));
    }

    private void showMessage(String message) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal(message));
        }
    }

    public Map<String, Integer> copyEnchantments() {
        return new LinkedHashMap<>(enchantments);
    }

    public void setEnchantments(Map<String, Integer> values) {
        enchantments.clear();
        values.forEach((id, level) -> {
            if (ItemDropService.isAllowedEnchantment(id) && level != null) {
                enchantments.put(id, Math.max(1, Math.min(255, level)));
            }
        });
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (event.buttonInfo().button() == 0) {
            int gridX = gridLeft();
            int gridY = gridTop();
            int gridWidth = GRID_COLUMNS * CELL_SIZE;
            int gridHeight = GRID_ROWS * CELL_SIZE;
            if (mouseX >= gridX && mouseX < gridX + gridWidth
                && mouseY >= gridY && mouseY < gridY + gridHeight) {
                int column = (int) ((mouseX - gridX) / CELL_SIZE);
                int row = (int) ((mouseY - gridY) / CELL_SIZE);
                int index = page * ITEMS_PER_PAGE + row * GRID_COLUMNS + column;
                if (index >= 0 && index < filteredItems.size()) {
                    selectedItem = filteredItems.get(index);
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int left = panelLeft();
        int top = panelTop();
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.text(this.font, Component.literal("ADVANCED INVENTORY"), left + 16, top + 12, 0xFFFFFFFF, true);
        graphics.text(this.font, Component.literal("Singleplayer mode"), left + PANEL_WIDTH - 125, top + 12, 0xFFB8D8F2, true);
        graphics.text(this.font, Component.literal("Categories"), left + 16, top + 62, 0xFFB8CBE0, true);
        graphics.text(this.font, Component.literal("Search the item registry"), left + 130, top + 34, 0xFFB8CBE0, true);
        graphics.text(this.font, Component.literal("Selected item"), left + 485, top + 56, 0xFFB8CBE0, true);

        int start = page * ITEMS_PER_PAGE;
        for (int slot = 0; slot < ITEMS_PER_PAGE; slot++) {
            int index = start + slot;
            int column = slot % GRID_COLUMNS;
            int row = slot / GRID_COLUMNS;
            int x = gridLeft() + column * CELL_SIZE;
            int y = gridTop() + row * CELL_SIZE;
            boolean hovered = mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE;
            Item item = index < filteredItems.size() ? filteredItems.get(index) : null;
            boolean selected = item != null && item == selectedItem;

            graphics.fill(x + 1, y + 1, x + CELL_SIZE - 1, y + CELL_SIZE - 1,
                selected ? 0xFF526F8E : hovered ? 0xFF344B63 : 0xFF202D3B);
            graphics.outline(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2,
                selected ? 0xFFE1F1FF : 0xFF40566D);
            if (item != null) {
                ItemStack stack = new ItemStack(item);
                graphics.item(stack, x + 9, y + 8);
                if (hovered) {
                    graphics.setTooltipForNextFrame(this.font, stack, mouseX, mouseY);
                }
            }
        }

        if (selectedItem != null) {
            ItemStack selectedStack = new ItemStack(selectedItem);
            graphics.fill(left + 485, top + 76, left + 635, top + 118, 0xFF202D3B);
            graphics.item(selectedStack, left + 493, top + 87);
            graphics.text(this.font, selectedStack.getHoverName(), left + 515, top + 86, 0xFFFFFFFF, true);
            String itemId = BuiltInRegistries.ITEM.getKey(selectedItem).toString();
            graphics.text(this.font, Component.literal(itemId), left + 485, top + 112, 0xFF9DB4C9, false);
        }

        int pages = Math.max(1, (filteredItems.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        graphics.text(this.font, Component.literal("Page " + (page + 1) + " / " + pages
            + "   •   " + filteredItems.size() + " items"), left + 305, top + 299, 0xFFB8CBE0, true);
        graphics.text(this.font, Component.literal("Quantity"), left + 485, top + 122, 0xFFB8CBE0, true);
        graphics.text(this.font, Component.literal("Utility enchantments selected: " + enchantments.size()),
            left + 485, top + 326, 0xFF9DB4C9, false);
        graphics.text(this.font, Component.literal("Max for selected item: " + maxQuantityForSelectedItem()),
            left + 16, top + 378, 0xFF8DA4BA, false);
    }

    private record Category(String id, String label) { }
}
