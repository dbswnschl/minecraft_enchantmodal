package com.enchantmodal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PresetListScreen extends Screen {
    private final Screen parent;
    private final Consumer<Map<String, Integer>> onSelect;
    private PresetList presetList;
    private Button loadButton;
    private Button deleteButton;

    public PresetListScreen(Screen parent, Consumer<Map<String, Integer>> onSelect) {
        super(Component.translatable("gui.enchantmodal.preset_load_title"));
        this.parent = parent;
        this.onSelect = onSelect;
    }

    @Override
    protected void init() {
        super.init();

        int listWidth = 240;
        int listLeft = (this.width - listWidth) / 2;
        int listTop = 32;
        int listBottom = this.height - 52;

        this.presetList = new PresetList(this.minecraft, listWidth, listBottom - listTop, listTop, 20);
        this.presetList.setX(listLeft);

        for (String name : PresetManager.getPresetNames()) {
            this.presetList.add(new PresetList.PresetEntry(name));
        }

        this.addRenderableWidget(this.presetList);

        int buttonY = this.height - 40;
        int centerX = this.width / 2;

        this.loadButton = this.addRenderableWidget(Button.builder(
            Component.translatable("gui.enchantmodal.load"),
            btn -> {
                PresetList.PresetEntry entry = this.presetList.getSelected();
                if (entry != null) {
                    Map<String, Integer> preset = PresetManager.getPreset(entry.getName());
                    if (preset != null) {
                        onSelect.accept(preset);
                        this.minecraft.setScreen(parent);
                    }
                }
            }
        ).bounds(centerX - 162, buttonY, 100, 20).build());

        this.deleteButton = this.addRenderableWidget(Button.builder(
            Component.translatable("gui.enchantmodal.delete"),
            btn -> {
                PresetList.PresetEntry entry = this.presetList.getSelected();
                if (entry != null) {
                    PresetManager.deletePreset(entry.getName());
                    this.minecraft.setScreen(new PresetListScreen(parent, onSelect));
                }
            }
        ).bounds(centerX - 50, buttonY, 100, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.enchantmodal.cancel"),
            btn -> this.minecraft.setScreen(parent)
        ).bounds(centerX + 62, buttonY, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);

        boolean hasSelection = this.presetList.getSelected() != null;
        this.loadButton.active = hasSelection;
        this.deleteButton.active = hasSelection;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static class PresetList extends ObjectSelectionList<PresetList.PresetEntry> {
        public PresetList(Minecraft mc, int width, int height, int top, int itemHeight) {
            super(mc, width, height, top, itemHeight);
        }

        public void add(PresetEntry entry) {
            this.addEntry(entry);
        }

        public static class PresetEntry extends ObjectSelectionList.Entry<PresetEntry> {
            private final String name;

            public PresetEntry(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }

            @Override
            public Component getNarration() {
                return Component.literal(name);
            }

            @Override
            public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean isHovered, float partialTick) {
                Minecraft mc = Minecraft.getInstance();
                int x = this.getContentX();
                int y = this.getContentY();
                int color = isHovered ? 0xFFFFFF55 : 0xFFFFFFFF;
                graphics.drawString(mc.font, name, x + 4, y + 3, color);
            }
        }
    }
}
