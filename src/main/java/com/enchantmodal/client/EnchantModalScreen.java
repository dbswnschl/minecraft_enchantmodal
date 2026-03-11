package com.enchantmodal.client;

import com.enchantmodal.network.ApplyEnchantmentsPacket;
import com.enchantmodal.network.ModPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraftforge.network.PacketDistributor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantModalScreen extends Screen {
    private EnchantmentListWidget enchantmentList;

    public EnchantModalScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();

        int listWidth = 280;
        int listLeft = (this.width - listWidth) / 2;
        int listTop = 32;
        int listBottom = this.height - 52;

        this.enchantmentList = new EnchantmentListWidget(
            this.minecraft, listWidth, listBottom - listTop, listTop, 24
        );
        this.enchantmentList.setX(listLeft);

        ItemStack heldItem = Minecraft.getInstance().player.getMainHandItem();
        ItemEnchantments existingEnchants = heldItem.getEnchantments();

        HolderLookup.RegistryLookup<Enchantment> lookup = Minecraft.getInstance().player.connection
            .registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        List<Holder.Reference<Enchantment>> holders = lookup.listElements()
            .sorted(Comparator.comparing(h -> h.key().identifier().toString()))
            .toList();

        for (Holder.Reference<Enchantment> holder : holders) {
            int existingLevel = existingEnchants.getLevel(holder);
            boolean enabled = existingLevel > 0;
            int level = enabled ? existingLevel : 1;
            this.enchantmentList.add(
                new EnchantmentListWidget.EnchantmentEntry(holder, enabled, level, 255)
            );
        }

        this.addRenderableWidget(this.enchantmentList);

        // 하단 버튼 배치: [프리셋 저장] [프리셋 불러오기] [적용] [취소]
        int buttonWidth = 80;
        int buttonY = this.height - 40;
        int totalWidth = buttonWidth * 4 + 5 * 3; // 4 buttons + 3 gaps
        int startX = (this.width - totalWidth) / 2;

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.enchantmodal.preset_save"),
            btn -> openPresetSave()
        ).bounds(startX, buttonY, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.enchantmodal.preset_load"),
            btn -> openPresetLoad()
        ).bounds(startX + buttonWidth + 5, buttonY, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.enchantmodal.apply"),
            btn -> applyEnchantments()
        ).bounds(startX + (buttonWidth + 5) * 2, buttonY, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.enchantmodal.cancel"),
            btn -> onClose()
        ).bounds(startX + (buttonWidth + 5) * 3, buttonY, buttonWidth, 20).build());
    }

    private Map<Identifier, Integer> collectCurrentEnchantments() {
        Map<Identifier, Integer> selected = new HashMap<>();
        for (int i = 0; i < this.enchantmentList.children().size(); i++) {
            EnchantmentListWidget.EnchantmentEntry entry = this.enchantmentList.children().get(i);
            if (entry.isEnabled()) {
                selected.put(entry.getEnchantmentId(), entry.getLevel());
            }
        }
        return selected;
    }

    private void openPresetSave() {
        Map<Identifier, Integer> current = collectCurrentEnchantments();
        this.minecraft.setScreen(new PresetNameScreen(this, current));
    }

    private void openPresetLoad() {
        this.minecraft.setScreen(new PresetListScreen(this, this::applyPreset));
    }

    private void applyPreset(Map<String, Integer> preset) {
        for (int i = 0; i < this.enchantmentList.children().size(); i++) {
            EnchantmentListWidget.EnchantmentEntry entry = this.enchantmentList.children().get(i);
            Identifier id = entry.getEnchantmentId();
            if (id != null && preset.containsKey(id.toString())) {
                entry.setEnabled(true);
                entry.setLevel(preset.get(id.toString()));
            } else {
                entry.setEnabled(false);
                entry.setLevel(1);
            }
        }
    }

    private void applyEnchantments() {
        Map<Identifier, Integer> selected = collectCurrentEnchantments();
        ModPacketHandler.CHANNEL.send(new ApplyEnchantmentsPacket(selected), PacketDistributor.SERVER.noArg());
        onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
