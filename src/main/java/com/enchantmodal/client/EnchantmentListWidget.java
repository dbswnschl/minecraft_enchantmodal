package com.enchantmodal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentListWidget extends ObjectSelectionList<EnchantmentListWidget.EnchantmentEntry> {

    public EnchantmentListWidget(Minecraft mc, int width, int height, int top, int itemHeight) {
        super(mc, width, height, top, itemHeight);
    }

    public void add(EnchantmentEntry entry) {
        this.addEntry(entry);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean flag) {
        EnchantmentEntry entry = this.getEntryAtPosition(event.x(), event.y());
        if (entry != null && event.button() == 0) {
            entry.handleClick(event.x());
            return true;
        }
        return super.mouseClicked(event, flag);
    }

    public static class EnchantmentEntry extends ObjectSelectionList.Entry<EnchantmentEntry> {
        private final Holder<Enchantment> enchantment;
        private boolean enabled;
        private int level;
        private final int maxLevel;
        private final Component displayName;
        private final Identifier enchantmentId;

        private static final int TOGGLE_WIDTH = 30;
        private static final int LEVEL_BTN_WIDTH = 16;

        public EnchantmentEntry(Holder<Enchantment> enchantment, boolean enabled, int level, int maxLevel) {
            this.enchantment = enchantment;
            this.enabled = enabled;
            this.level = level;
            this.maxLevel = maxLevel;
            this.displayName = enchantment.value().description();
            this.enchantmentId = enchantment.unwrapKey()
                .map(key -> key.identifier())
                .orElse(null);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = Math.max(1, Math.min(level, maxLevel));
        }

        public Identifier getEnchantmentId() {
            return enchantmentId;
        }

        @Override
        public Component getNarration() {
            return displayName;
        }

        @Override
        public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            Minecraft mc = Minecraft.getInstance();
            int x = this.getContentX();
            int y = this.getContentY();
            int w = this.getContentWidth();

            // [ON/OFF] 토글 (ARGB: 0xAARRGGBB)
            String toggleText = enabled ? "ON" : "OFF";
            int toggleColor = enabled ? 0xFF00FF00 : 0xFFFF5555;
            graphics.drawString(mc.font, toggleText, x + 2, y + 2, toggleColor);

            // 인챈트 이름
            int textColor = enabled ? 0xFF55FF55 : 0xFFAAAAAA;
            String name = displayName.getString();
            graphics.drawString(mc.font, name, x + TOGGLE_WIDTH + 4, y + 2, textColor);

            // 레벨: [- level +] (오른쪽 정렬)
            if (enabled) {
                int rightEnd = x + w;
                String levelStr = String.valueOf(level);
                int levelWidth = mc.font.width(levelStr);

                graphics.drawString(mc.font, "+", rightEnd - LEVEL_BTN_WIDTH, y + 2, 0xFFFFFF55);
                graphics.drawString(mc.font, levelStr,
                    rightEnd - LEVEL_BTN_WIDTH - levelWidth - 4, y + 2, 0xFFFFFFFF);
                graphics.drawString(mc.font, "-",
                    rightEnd - LEVEL_BTN_WIDTH - levelWidth - 4 - LEVEL_BTN_WIDTH, y + 2, 0xFFFFFF55);
            }
        }

        public void handleClick(double mouseX) {
            int x = this.getContentX();
            int w = this.getContentWidth();
            int rightEnd = x + w;

            if (enabled) {
                Minecraft mc = Minecraft.getInstance();
                String levelStr = String.valueOf(level);
                int levelWidth = mc.font.width(levelStr);

                // + 버튼 영역
                int plusX = rightEnd - LEVEL_BTN_WIDTH;
                if (mouseX >= plusX && mouseX < plusX + LEVEL_BTN_WIDTH) {
                    if (level < maxLevel) level++;
                    return;
                }

                // - 버튼 영역
                int minusX = rightEnd - LEVEL_BTN_WIDTH - levelWidth - 4 - LEVEL_BTN_WIDTH;
                if (mouseX >= minusX && mouseX < minusX + LEVEL_BTN_WIDTH) {
                    if (level > 1) level--;
                    return;
                }
            }

            // 나머지: ON/OFF 토글
            enabled = !enabled;
        }
    }
}
