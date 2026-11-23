package com.sua.advancedinventory.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sua.advancedinventory.enchantment.EnchantmentRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

/** Registry-backed Sakura enchantment editor with search and pagination. */
public final class EnchantmentScreen extends Screen {
    private static final int PANEL_WIDTH = 640;
    private static final int PANEL_HEIGHT = 390;
    private static final int COLUMNS = 2;
    private static final int ROWS = 8;
    private static final int PAGE_SIZE = COLUMNS * ROWS;

    private final InventoryScreen parent;
    private final Map<String, Integer> selected;
    private final Map<String, EditBox> levelFields = new LinkedHashMap<>();
    private final List<AbstractWidget> rowWidgets = new ArrayList<>();
    private List<EnchantmentEntry> allEnchantments = List.of();
    private List<EnchantmentEntry> filteredEnchantments = List.of();
    private EditBox searchBox;
    private String searchText = "";
    private int page;

    public EnchantmentScreen(InventoryScreen parent) {
        super(Component.literal("Sakura Enchantment Garden"));
        this.parent = parent;
        this.selected = parent.copyEnchantments();
        reloadRegistryEnchantments();
        updateFilteredEnchantments();
    }

    private void reloadRegistryEnchantments() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            allEnchantments = List.of();
            return;
        }

        Registry<Enchantment> registry = client.level.registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT);
        List<EnchantmentEntry> found = new ArrayList<>();
        for (Map.Entry<ResourceKey<Enchantment>, Enchantment> entry : registry.entrySet()) {
            String id = entry.getKey().identifier().toString();
            if (!EnchantmentRules.isAllowed(id)) continue;
            Enchantment enchantment = entry.getValue();
            found.add(new EnchantmentEntry(id, enchantment.description()));
        }
        found.sort(Comparator.comparing(entry -> entry.displayName().getString(), String.CASE_INSENSITIVE_ORDER));
        allEnchantments = List.copyOf(found);
    }

    private void updateFilteredEnchantments() {
        String needle = searchText.toLowerCase(java.util.Locale.ROOT).trim();
        filteredEnchantments = allEnchantments.stream()
            .filter(entry -> needle.isEmpty()
                || entry.id().toLowerCase(java.util.Locale.ROOT).contains(needle)
                || entry.displayName().getString().toLowerCase(java.util.Locale.ROOT).contains(needle))
            .toList();
        int maxPage = Math.max(0, (filteredEnchantments.size() - 1) / PAGE_SIZE);
        page = Math.min(page, maxPage);
    }

    @Override
    protected void init() {
        int left = panelLeft();
        int top = panelTop();
        levelFields.clear();
        rowWidgets.clear();

        this.addRenderableWidget(new PanelBackgroundWidget(
            this.width, this.height, left, top, PANEL_WIDTH, PANEL_HEIGHT
        ));

        searchBox = new EditBox(this.font, left + 20, top + 43, 390, 20,
            Component.literal("Tìm enchantment theo tên hoặc ID"));
        searchBox.setMaxLength(120);
        searchBox.setValue(searchText);
        searchBox.setResponder(value -> {
            captureVisibleLevels();
            searchText = value;
            page = 0;
            updateFilteredEnchantments();
            refreshVisibleRows();
        });
        this.addRenderableWidget(searchBox);
        addVisibleRows(left, top);

        this.addRenderableWidget(Button.builder(Component.literal("◀"), button -> {
            captureVisibleLevels();
            page = Math.max(0, page - 1);
            refreshVisibleRows();
        }).bounds(left + 20, top + 350, 38, 22).build());
        this.addRenderableWidget(Button.builder(Component.literal("▶"), button -> {
            captureVisibleLevels();
            int maxPage = Math.max(0, (filteredEnchantments.size() - 1) / PAGE_SIZE);
            page = Math.min(maxPage, page + 1);
            refreshVisibleRows();
        }).bounds(left + 66, top + 350, 38, 22).build());
        this.addRenderableWidget(Button.builder(Component.literal("XÓA TẤT CẢ"), button -> {
            selected.clear();
            parent.setEnchantments(Map.of());
            Minecraft.getInstance().setScreen(parent);
        }).bounds(left + PANEL_WIDTH - 210, top + 350, 94, 22).build());
        this.addRenderableWidget(Button.builder(Component.literal("LƯU"), button -> saveAndReturn())
            .bounds(left + PANEL_WIDTH - 106, top + 350, 86, 22).build());
    }

    /** Refresh list controls only; keep search focus and fixed navigation buttons intact. */
    private void refreshVisibleRows() {
        for (AbstractWidget widget : rowWidgets) {
            this.removeWidget(widget);
        }
        rowWidgets.clear();
        levelFields.clear();
        addVisibleRows(panelLeft(), panelTop());
    }

    private void addVisibleRows(int left, int top) {
        int start = page * PAGE_SIZE;
        int end = Math.min(filteredEnchantments.size(), start + PAGE_SIZE);
        for (int index = start; index < end; index++) {
            EnchantmentEntry entry = filteredEnchantments.get(index);
            String id = entry.id();
            int localIndex = index - start;
            int column = localIndex / ROWS;
            int row = localIndex % ROWS;
            int x = left + 20 + column * 300;
            int y = top + 82 + row * 31;
            boolean active = selected.containsKey(id);

            Button toggle = Button.builder(Component.literal(active ? "BẬT" : "TẮT"), button -> {
                captureVisibleLevels();
                if (selected.containsKey(id)) {
                    selected.remove(id);
                    button.setMessage(Component.literal("TẮT"));
                } else {
                    selected.put(id, 1);
                    button.setMessage(Component.literal("BẬT"));
                }
            }).bounds(x + 154, y, 44, 20).build();
            rowWidgets.add(this.addRenderableWidget(toggle));

            EditBox level = new EditBox(this.font, x + 205, y, 54, 20, Component.literal("Cấp"));
            level.setMaxLength(3);
            level.setValue(Integer.toString(selected.getOrDefault(id, 1)));
            level.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.literal("Cấp độ từ 1 đến 255 · " + id)
            ));
            levelFields.put(id, level);
            rowWidgets.add(this.addRenderableWidget(level));
        }
    }

    private void captureVisibleLevels() {
        for (Map.Entry<String, EditBox> entry : levelFields.entrySet()) {
            if (!selected.containsKey(entry.getKey())) continue;
            int level = parseLevel(entry.getValue().getValue());
            selected.put(entry.getKey(), level);
        }
    }

    private static int parseLevel(String text) {
        try {
            return Math.max(1, Math.min(255, Integer.parseInt(text.trim())));
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    private void saveAndReturn() {
        captureVisibleLevels();
        parent.setEnchantments(selected);
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void onClose() {
        captureVisibleLevels();
        Minecraft.getInstance().setScreen(parent);
    }

    private int panelLeft() {
        return Math.max(8, (this.width - PANEL_WIDTH) / 2);
    }

    private int panelTop() {
        return Math.max(8, (this.height - PANEL_HEIGHT) / 2);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int left = panelLeft();
        int top = panelTop();
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        long now = System.currentTimeMillis();
        int pulse = (int) (215 + 35 * (0.5 + 0.5 * Math.sin(now * 0.0018)));
        graphics.text(this.font, Component.literal("SAKURA  /  ENCHANTMENT STUDIO"),
            left + 20, top + 12, (pulse << 24) | 0x00FFEAF2, true);
        graphics.text(this.font, Component.literal("VƯỜN PHÙ PHÉP  ·  TÙY CHỈNH CẤP ĐỘ 1–255"),
            left + 20, top + 28, 0xFFE8BDD0, false);
        graphics.text(this.font, Component.literal("LỌC"),
            left + 424, top + 50, 0xFFFFDDEC, true);
        graphics.text(this.font, Component.literal("✿"),
            left + PANEL_WIDTH - 33, top + 43, 0xFFFFC5DF, true);

        int start = page * PAGE_SIZE;
        int end = Math.min(filteredEnchantments.size(), start + PAGE_SIZE);
        for (int index = start; index < end; index++) {
            EnchantmentEntry entry = filteredEnchantments.get(index);
            int localIndex = index - start;
            int column = localIndex / ROWS;
            int row = localIndex % ROWS;
            int x = left + 20 + column * 300;
            int y = top + 82 + row * 31;
            boolean active = selected.containsKey(entry.id());

            graphics.fill(x - 3, y + 7, x, y + 14,
                active ? 0xFFFFC8DE : 0x777A5B78);
            String name = entry.displayName().getString();
            if (name.length() > 20) name = name.substring(0, 18) + "…";
            graphics.text(this.font, Component.literal(name), x + 4, y + 6,
                active ? 0xFFFFF1F7 : 0xFFE9C9DA, active);
        }

        int pages = Math.max(1, (filteredEnchantments.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        graphics.text(this.font, Component.literal("TRANG " + (page + 1) + " / " + pages),
            left + 119, top + 357, 0xFFFFEAF2, true);
        graphics.text(this.font, Component.literal(filteredEnchantments.size() + " ENCHANTMENT"),
            left + 217, top + 357, 0xFFDDBBD0, false);
        graphics.text(this.font, Component.literal("ĐÃ CHỌN: " + selected.size()),
            left + PANEL_WIDTH - 315, top + 357, 0xFFFFDDEC, true);
        graphics.text(this.font, Component.literal("LEVEL 1–255"),
            left + PANEL_WIDTH - 112, top + 357, 0xFFE8BDD0, false);

        if (filteredEnchantments.isEmpty()) {
            graphics.text(this.font, Component.literal("Không tìm thấy enchantment phù hợp."),
                left + 185, top + 190, 0xFFFFD7E5, true);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    private record EnchantmentEntry(String id, Component displayName) { }
}
