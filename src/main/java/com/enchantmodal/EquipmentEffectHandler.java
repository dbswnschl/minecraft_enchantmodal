package com.enchantmodal;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraftforge.event.TickEvent;

import java.util.HashSet;
import java.util.Set;

public class EquipmentEffectHandler {

    private static final EquipmentSlot[] TRACKED_SLOTS = {
        EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
        EquipmentSlot.HEAD, EquipmentSlot.CHEST,
        EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public static void onPlayerTick(TickEvent.PlayerTickEvent.Post event) {
        Player player = event.player();
        if (player.level().isClientSide()) return;

        // 60틱(3초)마다 체크
        if (player.tickCount % 60 != 0) return;

        for (EquipmentSlot slot : TRACKED_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;

            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            if (contents == null) continue;

            for (MobEffectInstance effect : contents.getAllEffects()) {
                // 200틱(10초) 지속 — 3초마다 갱신하므로 항상 유지됨
                player.addEffect(new MobEffectInstance(
                    effect.getEffect(),
                    200,
                    effect.getAmplifier(),
                    true,  // ambient (파티클 줄임)
                    false, // 파티클 숨김
                    true   // 아이콘 표시
                ));
            }
        }
    }
}
