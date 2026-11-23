package com.sua.advancedinventory.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sua.advancedinventory.catalog.ItemCatalog;
import com.sua.advancedinventory.service.ItemDropService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Sakura-themed item browser; the registry includes compatible items from installed mods. */
public final class InventoryScreen extends Screen {
    private static final int PANEL_WIDTH = 700;
    private static final int PANEL_HEIGHT = 410;
    private static final int GRID_COLUMNS = 9;
    private static final int GRID_ROWS = 5;
    private static final int CELL_SIZE = 34;
    private static final int ITEMS_PER_PAGE = GRID_COLUMNS * GRID_ROWS;

    private static final List<Category> CATEGORIES = List.of(
        new Category("all", "✿  Tất cả"),
        new Category("modded", "✿  Vật phẩm mod"),
        new Category("vanilla", "✿  Minecraft"),
        new Category("blocks", "Khối xây dựng"),
        new Category("food", "Thực phẩm"),
        new Category("tools", "Dụng cụ"),
        new Category("redstone", "Redstone"),
        new Category("misc", "Khác")
    );

    private final Map<String, Integer> enchantments = new LinkedHashMap<>();
    private Item selectedItem = Items.STONE;
    private String selectedCategory = "all";
    private String searchText = "";
    private List<Item> filteredItems = List.of();
    private final List<Button> categoryButtons = new java.util.ArrayList<>();
    private int page;
    private EditBox searchBox;
    private EditBox quantityBox;

    public InventoryScreen() {
        super(Component.literal("Sakura Item Garden"));
        updateFilteredItems();
    }

    @Override
    protected void init() {
        int left = panelLeft();
        int top = panelTop();
        categoryButtons.clear();

        // The first widget is the decorative layer, so every control renders above it.
        this.addRenderableWidget(new PanelBackgroundWidget(
            this.width, this.height, left, top, PANEL_WIDTH, PANEL_HEIGHT
        ));

        searchBox = new EditBox(this.font, left + 130, top + 48, 315, 20,
            Component.literal("Tìm theo tên, ID hoặc namespace mod"));
        searchBox.setMaxLength(100);
        searchBox.setValue(searchText);
        searchBox.setResponder(value -> {
            searchText = value;
            page = 0;
            updateFilteredItems();
        });
        this.addRenderableWidget(searchBox);

        quantityBox = new EditBox(this.font, left + 485, top + 139, 86, 20,
            Component.literal("Số lượng"));
        quantityBox.setMaxLength(5);
        quantityBox.setValue("64");
        this.addRenderableWidget(quantityBox);

        int categoryY = top + 82;
        for (int i = 0; i < CATEGORIES.size(); i++) {
            Category category = CATEGORIES.get(i);
            String label = category.label();
            if (category.id().equals(selectedCategory)) label = "❀ " + label.replace("✿  ", "");
            Button button = Button.builder(Component.literal(label), press -> {
                selectedCategory = category.id();
                page = 0;
                updateFilteredItems();
                updateCategoryButtonLabels();
            }).bounds(left + 16, categoryY + i * 29, 104, 23).build();
            categoryButtons.add(button);
            this.addRenderableWidget(button);
        }

        this.addRenderableWidget(Button.builder(Component.literal("−1"), button -> adjustQuantity(-1))
            .bounds(left + 485, top + 166, 48, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+1"), button -> adjustQuantity(1))
            .bounds(left + 537, top + 166, 48, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+16"), button -> adjustQuantity(16))
            .bounds(left + 485, top + 192, 48, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+64"), button -> adjustQuantity(64))
            .bounds(left + 537, top + 192, 48, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("✿  Enchantment"), button ->
            Minecraft.getInstance().setScreen(new EnchantmentScreen(this))
        ).bounds(left + 485, top + 226, 150, 22).build());
        this.addRenderableWidget(Button.builder(Component.literal("THẢ VẬT PHẨM"), button -> dropSelectedItem())
            .bounds(left + 485, top + 258, 150, 28).build());
        this.addRenderableWidget(Button.builder(Component.literal("◀"), button -> {
            page = Math.max(0, page - 1);
        }).bounds(left + 130, top + 294, 34, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("▶"), button -> {
            int maxPage = Math.max(0, (filteredItems.size() - 1) / ITEMS_PER_PAGE);
            page = Math.min(maxPage, page + 1);
        }).bounds(left + 170, top + 294, 34, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("MAX"), button ->
            quantityBox.setValue(Integer.toString(maxQuantityForSelectedItem()))
        ).bounds(left + 589, top + 166, 46, 46).build());
        this.addRenderableWidget(Button.builder(Component.literal("Xóa enchant"), button -> enchantments.clear())
            .bounds(left + 485, top + 294, 150, 20).build());
    }

    private void updateCategoryButtonLabels() {
        for (int i = 0; i < CATEGORIES.size() && i < categoryButtons.size(); i++) {
            Category category = CATEGORIES.get(i);
            String label = category.label().replace("✿  ", "");
            if (category.id().equals(selectedCategory)) label = "❀ " + label;
            categoryButtons.get(i).setMessage(Component.literal(label));
        }
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
            showMessage("Hãy nhập số lượng hợp lệ.");
            return;
        }
        int maxForItem = maxQuantityForSelectedItem();
        if (quantity < 1 || quantity > maxForItem) {
            showMessage("Số lượng phải nằm trong khoảng 1–" + maxForItem + ".");
            return;
        }
        ItemDropService.drop(selectedItem, quantity, Map.copyOf(enchantments));
    }

    private void showMessage(String message) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(Component.literal(message), false);
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

        long now = System.currentTimeMillis();
        int shimmer = (int) (210 + 35 * (0.5 + 0.5 * Math.sin(now * 0.0017)));
        int titleColor = (shimmer << 24) | 0x00FFEAF2;

        // Header: the small gold-pink status chip distinguishes the local world mode.
        graphics.text(this.font, Component.literal("SAKURA  /  ITEM ATELIER"),
            left + 20, top + 12, titleColor, true);
        graphics.text(this.font, Component.literal("KHU VƯỜN VẬT PHẨM"),
            left + 20, top + 27, 0xFFE9BDD0, false);
        graphics.text(this.font, Component.literal("● SINGLEPLAYER"),
            left + PANEL_WIDTH - 137, top + 13, 0xFFFFD7E7, true);
        graphics.text(this.font, Component.literal("K  ·  PHÍM MỞ NHANH"),
            left + PANEL_WIDTH - 164, top + 27, 0xFFCAA9C7, false);

        graphics.text(this.font, Component.literal("DANH MỤC"),
            left + 21, top + 62, 0xFFFFDDEC, true);
        graphics.text(this.font, Component.literal("TÌM KIẾM VẬT PHẨM"),
            left + 136, top + 34, 0xFFFFDDEC, true);
        graphics.text(this.font, Component.literal("✧"),
            left + 429, top + 49, 0xFFFFC5DF, true);
        graphics.text(this.font, Component.literal("VẬT PHẨM ĐANG CHỌN"),
            left + 486, top + 57, 0xFFFFDDEC, true);

        // Draw each item slot with a soft inset and a breathing highlight for the selection.
        int start = page * ITEMS_PER_PAGE;
        int selectedPulse = (int) (185 + 55 * (0.5 + 0.5 * Math.sin(now * 0.004)));
        for (int slot = 0; slot < ITEMS_PER_PAGE; slot++) {
            int index = start + slot;
            int column = slot % GRID_COLUMNS;
            int row = slot / GRID_COLUMNS;
            int x = gridLeft() + column * CELL_SIZE;
            int y = gridTop() + row * CELL_SIZE;
            boolean hovered = mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE;
            Item item = index < filteredItems.size() ? filteredItems.get(index) : null;
            boolean selected = item != null && item == selectedItem;

            int cellFill = selected ? 0xE04B2D4C : hovered ? 0xD64A304B : 0xA9291B32;
            int cellBorder = selected ? (selectedPulse << 24) | 0x00FFD9E9
                : hovered ? 0xFFEBC0D7 : 0x827F5D78;
            graphics.fill(x + 1, y + 1, x + CELL_SIZE - 1, y + CELL_SIZE - 1, cellFill);
            graphics.fill(x + 3, y + 3, x + CELL_SIZE - 3, y + 4,
                selected ? 0x66FFE5F0 : 0x225E3D5D);
            graphics.outline(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2, cellBorder);
            if (item != null) {
                ItemStack stack = new ItemStack(item);
                graphics.item(stack, x + 9, y + 8);
                if (hovered) graphics.setTooltipForNextFrame(this.font, stack, mouseX, mouseY);
            } else {
                graphics.fill(x + 15, y + 15, x + 19, y + 19, 0x553E2944);
            }
        }

        if (selectedItem != null) {
            ItemStack selectedStack = new ItemStack(selectedItem);
            String itemId = BuiltInRegistries.ITEM.getKey(selectedItem).toString();
            String itemName = selectedStack.getHoverName().getString();
            if (itemName.length() > 19) itemName = itemName.substring(0, 17) + "…";
            if (itemId.length() > 22) itemId = itemId.substring(0, 20) + "…";

            graphics.fill(left + 484, top + 76, left + 638, top + 120, 0xD52A1B32);
            graphics.outline(left + 484, top + 76, 154, 44, 0xC9DFA5C5);
            graphics.fill(left + 485, top + 77, left + 637, top + 78, 0x88F7C8DD);
            graphics.item(selectedStack, left + 493, top + 89);
            graphics.text(this.font, Component.literal(itemName),
                left + 516, top + 85, 0xFFFFF2F7, true);
            graphics.text(this.font, Component.literal(itemId),
                left + 516, top + 101, 0xFFE3BBD0, false);
            graphics.text(this.font, Component.literal("STACK TỐI ĐA: " + selectedStack.getMaxStackSize()),
                left + 487, top + 110, 0xFFCAA8C2, false);
        } else {
            graphics.text(this.font, Component.literal("Không có vật phẩm phù hợp"),
                left + 490, top + 92, 0xFFFFD9E8, false);
        }

        int pages = Math.max(1, (filteredItems.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        graphics.text(this.font, Component.literal("TRANG " + (page + 1) + " / " + pages),
            left + 221, top + 300, 0xFFFFEAF2, true);
        graphics.text(this.font, Component.literal(filteredItems.size() + " VẬT PHẨM"),
            left + 314, top + 300, 0xFFDDBBD0, false);
        graphics.text(this.font, Component.literal("SỐ LƯỢNG"),
            left + 486, top + 124, 0xFFFFDDEC, true);
        graphics.text(this.font, Component.literal("ENCHANT ĐÃ CHỌN: " + enchantments.size()),
            left + 486, top + 326, 0xFFE8BDD0, false);

        graphics.text(this.font, Component.literal("✿  " + ItemCatalog.moddedCount()
            + " VẬT PHẨM TỪ MOD KHÁC ĐÃ ĐƯỢC NHẬN DIỆN"),
            left + 23, top + 365, 0xFFE8BDD0, false);
        graphics.text(this.font, Component.literal("SAKURA EDITION  ·  K ĐỂ MỞ NHANH"),
            left + PANEL_WIDTH - 246, top + 365, 0xFFCFAAC5, false);

        if (filteredItems.isEmpty()) {
            graphics.text(this.font, Component.literal("Không tìm thấy vật phẩm. Thử từ khóa khác nhé."),
                left + 170, top + 175, 0xFFFFDCEC, true);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    private record Category(String id, String label) { }
}
