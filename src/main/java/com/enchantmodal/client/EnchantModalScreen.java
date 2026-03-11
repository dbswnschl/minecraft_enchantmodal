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

        // listElements()로 클라이언트에 동기화된 인챈트 홀더 목록 가져오기
        List<Holder.Reference<Enchantment>> holders = lookup.listElements()
            .sorted(Comparator.comparing(h -> h.key().identifier().toString()))
            .toList();

        for (Holder.Reference<Enchantment> holder : holders) {
            int existingLevel = existingEnchants.getLevel(holder);
            boolean enabled = existingLevel > 0;
            int level = enabled ? existingLevel : 1;
            int maxLevel = holder.value().getMaxLevel();
            this.enchantmentList.add(
                new EnchantmentListWidget.EnchantmentEntry(holder, enabled, level, 255)
            );
        }

        this.addRenderableWidget(this.enchantmentList);

        int buttonWidth = 80;
        int buttonY = this.height - 40;
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.enchantmodal.apply"),
            btn -> applyEnchantments()
        ).bounds(this.width / 2 - buttonWidth - 5, buttonY, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.enchantmodal.cancel"),
            btn -> onClose()
        ).bounds(this.width / 2 + 5, buttonY, buttonWidth, 20).build());
    }

    private void applyEnchantments() {
        Map<Identifier, Integer> selected = new HashMap<>();

        for (int i = 0; i < this.enchantmentList.children().size(); i++) {
            EnchantmentListWidget.EnchantmentEntry entry = this.enchantmentList.children().get(i);
            if (entry.isEnabled()) {
                selected.put(entry.getEnchantmentId(), entry.getLevel());
            }
        }

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
