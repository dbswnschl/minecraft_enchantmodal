package com.enchantmodal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;

public class PotionEffectListWidget extends ObjectSelectionList<PotionEffectListWidget.EffectEntry> {

    public PotionEffectListWidget(Minecraft mc, int width, int height, int top, int itemHeight) {
        super(mc, width, height, top, itemHeight);
    }

    public void add(EffectEntry entry) {
        this.addEntry(entry);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean flag) {
        EffectEntry entry = this.getEntryAtPosition(event.x(), event.y());
        if (entry != null && event.button() == 0) {
            entry.handleClick(event.x());
            return true;
        }
        return super.mouseClicked(event, flag);
    }

    public static class EffectEntry extends ObjectSelectionList.Entry<EffectEntry> {
        private final Holder<MobEffect> effect;
        private boolean enabled;
        private int amplifier; // 0 = I, 1 = II, ...
        private final Component displayName;
        private final Identifier effectId;

        private static final int TOGGLE_WIDTH = 30;
        private static final int BTN_WIDTH = 16;
        private static final int MAX_AMPLIFIER = 255;

        public EffectEntry(Holder<MobEffect> effect, boolean enabled, int amplifier) {
            this.effect = effect;
            this.enabled = enabled;
            this.amplifier = amplifier;
            this.displayName = effect.value().getDisplayName();
            this.effectId = effect.unwrapKey()
                .map(key -> key.identifier())
                .orElse(null);
        }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getAmplifier() { return amplifier; }
        public void setAmplifier(int amplifier) { this.amplifier = Math.max(0, Math.min(amplifier, MAX_AMPLIFIER)); }
        public Identifier getEffectId() { return effectId; }

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

            // ON/OFF 토글
            String toggleText = enabled ? "ON" : "OFF";
            int toggleColor = enabled ? 0xFF00FF00 : 0xFFFF5555;
            graphics.drawString(mc.font, toggleText, x + 2, y + 2, toggleColor);

            // 효과 이름
            int textColor = enabled ? 0xFF55FF55 : 0xFFAAAAAA;
            graphics.drawString(mc.font, displayName.getString(), x + TOGGLE_WIDTH + 4, y + 2, textColor);

            // 레벨: [- lv +] (오른쪽 정렬)
            if (enabled) {
                int rightEnd = x + w;
                String ampStr = String.valueOf(amplifier + 1);
                int ampWidth = mc.font.width(ampStr);

                graphics.drawString(mc.font, "+", rightEnd - BTN_WIDTH, y + 2, 0xFFFFFF55);
                graphics.drawString(mc.font, ampStr,
                    rightEnd - BTN_WIDTH - ampWidth - 4, y + 2, 0xFFFFFFFF);
                graphics.drawString(mc.font, "-",
                    rightEnd - BTN_WIDTH - ampWidth - 4 - BTN_WIDTH, y + 2, 0xFFFFFF55);
            }
        }

        public void handleClick(double mouseX) {
            int x = this.getContentX();
            int w = this.getContentWidth();
            int rightEnd = x + w;

            if (enabled) {
                Minecraft mc = Minecraft.getInstance();
                String ampStr = String.valueOf(amplifier + 1);
                int ampWidth = mc.font.width(ampStr);

                // + 버튼
                int plusX = rightEnd - BTN_WIDTH;
                if (mouseX >= plusX && mouseX < plusX + BTN_WIDTH) {
                    if (amplifier < MAX_AMPLIFIER) amplifier++;
                    return;
                }

                // - 버튼
                int minusX = rightEnd - BTN_WIDTH - ampWidth - 4 - BTN_WIDTH;
                if (mouseX >= minusX && mouseX < minusX + BTN_WIDTH) {
                    if (amplifier > 0) amplifier--;
                    return;
                }
            }

            // 나머지: ON/OFF 토글
            enabled = !enabled;
        }
    }
}
