package com.enchantmodal.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.function.BiConsumer;

public class PresetNameScreen extends Screen {
    private final Screen parent;
    private final Map<Identifier, Integer> enchantments;
    private EditBox nameField;

    public PresetNameScreen(Screen parent, Map<Identifier, Integer> enchantments) {
        super(Component.translatable("gui.enchantmodal.preset_name_title"));
        this.parent = parent;
        this.enchantments = enchantments;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.nameField = new EditBox(this.font, centerX - 100, centerY - 20, 200, 20,
            Component.translatable("gui.enchantmodal.preset_name"));
        this.nameField.setMaxLength(50);
        this.nameField.setFocused(true);
        this.addRenderableWidget(this.nameField);

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.enchantmodal.save"),
            btn -> {
                String name = this.nameField.getValue().trim();
                if (!name.isEmpty()) {
                    PresetManager.savePreset(name, enchantments);
                    this.minecraft.setScreen(parent);
                }
            }
        ).bounds(centerX - 105, centerY + 10, 100, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.enchantmodal.cancel"),
            btn -> this.minecraft.setScreen(parent)
        ).bounds(centerX + 5, centerY + 10, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
