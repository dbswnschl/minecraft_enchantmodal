package com.enchantmodal.client;

import com.enchantmodal.network.ApplyEnchantmentsPacket;
import com.enchantmodal.network.ApplyPotionEffectsPacket;
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
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

public class EnchantModalScreen extends Screen {
    private EnchantmentListWidget enchantmentList;
    private PotionEffectListWidget potionEffectList;
    private Map<String, Integer> pendingPreset;

    private enum Tab { ENCHANTMENT, POTION }
    private Tab currentTab = Tab.ENCHANTMENT;

    private Button tabEnchantBtn;
    private Button tabPotionBtn;

    public EnchantModalScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();

        int listWidth = 300;
        int listLeft = (this.width - listWidth) / 2;
        int listTop = 48;
        int listBottom = this.height - 52;

        // === 탭 버튼 ===
        int tabWidth = 100;
        int tabY = 24;
        tabEnchantBtn = this.addRenderableWidget(Button.builder(
            Component.translatable("gui.enchantmodal.tab_enchant"),
            btn -> switchTab(Tab.ENCHANTMENT)
        ).bounds(this.width / 2 - tabWidth - 2, tabY, tabWidth, 20).build());

        tabPotionBtn = this.addRenderableWidget(Button.builder(
            Component.translatable("gui.enchantmodal.tab_potion"),
            btn -> switchTab(Tab.POTION)
        ).bounds(this.width / 2 + 2, tabY, tabWidth, 20).build());

        // === 인챈트 목록 ===
        this.enchantmentList = new EnchantmentListWidget(
            this.minecraft, listWidth, listBottom - listTop, listTop, 24
        );
        this.enchantmentList.setX(listLeft);

        ItemStack heldItem = Minecraft.getInstance().player.getMainHandItem();
        ItemEnchantments existingEnchants = heldItem.getEnchantments();

        HolderLookup.RegistryLookup<Enchantment> enchLookup = Minecraft.getInstance().player.connection
            .registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        List<Holder.Reference<Enchantment>> enchHolders = enchLookup.listElements()
            .sorted(Comparator.comparing(h -> h.key().identifier().toString()))
            .toList();

        for (Holder.Reference<Enchantment> holder : enchHolders) {
            int existingLevel = existingEnchants.getLevel(holder);
            boolean enabled = existingLevel > 0;
            int level = enabled ? existingLevel : 1;
            this.enchantmentList.add(
                new EnchantmentListWidget.EnchantmentEntry(holder, enabled, level, 255)
            );
        }

        // === 포션 효과 목록 ===
        this.potionEffectList = new PotionEffectListWidget(
            this.minecraft, listWidth, listBottom - listTop, listTop, 24
        );
        this.potionEffectList.setX(listLeft);

        // 기존 포션 효과 읽기
        PotionContents potionContents = heldItem.get(
            net.minecraft.core.component.DataComponents.POTION_CONTENTS);
        Map<String, MobEffectInstance> existingEffects = new HashMap<>();
        if (potionContents != null) {
            for (MobEffectInstance inst : potionContents.getAllEffects()) {
                inst.getEffect().unwrapKey().ifPresent(key ->
                    existingEffects.put(key.identifier().toString(), inst));
            }
        }

        HolderLookup.RegistryLookup<MobEffect> effectLookup = Minecraft.getInstance().player.connection
            .registryAccess().lookupOrThrow(Registries.MOB_EFFECT);

        List<Holder.Reference<MobEffect>> effectHolders = effectLookup.listElements()
            .sorted(Comparator.comparing(h -> h.key().identifier().toString()))
            .toList();

        for (Holder.Reference<MobEffect> holder : effectHolders) {
            String idStr = holder.key().identifier().toString();
            MobEffectInstance existing = existingEffects.get(idStr);
            boolean enabled = existing != null;
            int amplifier = enabled ? existing.getAmplifier() : 0;
            this.potionEffectList.add(
                new PotionEffectListWidget.EffectEntry(holder, enabled, amplifier)
            );
        }

        // 현재 탭에 맞는 위젯 표시
        if (currentTab == Tab.ENCHANTMENT) {
            this.addRenderableWidget(this.enchantmentList);
        } else {
            this.addRenderableWidget(this.potionEffectList);
        }

        // 프리셋 적용
        if (this.pendingPreset != null) {
            if (currentTab == Tab.ENCHANTMENT) {
                applyEnchantPreset(this.pendingPreset);
            } else {
                applyPotionPreset(this.pendingPreset);
            }
            this.pendingPreset = null;
        }

        // === 하단 버튼 ===
        int buttonWidth = 80;
        int buttonY = this.height - 40;
        int totalBtnWidth = buttonWidth * 4 + 5 * 3;
        int startX = (this.width - totalBtnWidth) / 2;

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
            btn -> applyAll()
        ).bounds(startX + (buttonWidth + 5) * 2, buttonY, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.enchantmodal.cancel"),
            btn -> onClose()
        ).bounds(startX + (buttonWidth + 5) * 3, buttonY, buttonWidth, 20).build());

        updateTabButtons();
    }

    private void switchTab(Tab tab) {
        this.currentTab = tab;
        this.rebuildWidgets();
    }

    private void updateTabButtons() {
        tabEnchantBtn.active = currentTab != Tab.ENCHANTMENT;
        tabPotionBtn.active = currentTab != Tab.POTION;
    }

    // === 프리셋 ===

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
        if (currentTab == Tab.ENCHANTMENT) {
            Map<Identifier, Integer> current = collectCurrentEnchantments();
            this.minecraft.setScreen(new PresetNameScreen(this, current));
        } else {
            Map<Identifier, Integer> current = collectCurrentPotionEffects();
            this.minecraft.setScreen(new PresetNameScreen(this, current));
        }
    }

    private Map<Identifier, Integer> collectCurrentPotionEffects() {
        Map<Identifier, Integer> selected = new HashMap<>();
        for (int i = 0; i < this.potionEffectList.children().size(); i++) {
            PotionEffectListWidget.EffectEntry entry = this.potionEffectList.children().get(i);
            if (entry.isEnabled()) {
                selected.put(entry.getEffectId(), entry.getAmplifier());
            }
        }
        return selected;
    }

    private void openPresetLoad() {
        this.minecraft.setScreen(new PresetListScreen(this, preset -> {
            this.pendingPreset = preset;
        }));
    }

    private void applyEnchantPreset(Map<String, Integer> preset) {
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

    private void applyPotionPreset(Map<String, Integer> preset) {
        for (int i = 0; i < this.potionEffectList.children().size(); i++) {
            PotionEffectListWidget.EffectEntry entry = this.potionEffectList.children().get(i);
            Identifier id = entry.getEffectId();
            if (id != null && preset.containsKey(id.toString())) {
                entry.setEnabled(true);
                entry.setAmplifier(preset.get(id.toString()));
            } else {
                entry.setEnabled(false);
                entry.setAmplifier(0);
            }
        }
    }

    // === 적용 ===

    private void applyAll() {
        // 인챈트 적용
        Map<Identifier, Integer> enchants = collectCurrentEnchantments();
        ModPacketHandler.CHANNEL.send(
            new ApplyEnchantmentsPacket(enchants), PacketDistributor.SERVER.noArg());

        // 포션 효과 적용
        List<ApplyPotionEffectsPacket.EffectData> effects = new ArrayList<>();
        for (int i = 0; i < this.potionEffectList.children().size(); i++) {
            PotionEffectListWidget.EffectEntry entry = this.potionEffectList.children().get(i);
            if (entry.isEnabled()) {
                effects.add(new ApplyPotionEffectsPacket.EffectData(
                    entry.getEffectId(), entry.getAmplifier()
                ));
            }
        }
        ModPacketHandler.CHANNEL.send(
            new ApplyPotionEffectsPacket(effects), PacketDistributor.SERVER.noArg());

        onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
