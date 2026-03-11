package com.enchantmodal.network;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.*;

public class ApplyPotionEffectsPacket {

    public record EffectData(Identifier id, int amplifier) {}

    private final List<EffectData> effects;

    public ApplyPotionEffectsPacket(List<EffectData> effects) {
        this.effects = effects;
    }

    public static void encode(ApplyPotionEffectsPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.effects.size());
        for (EffectData data : msg.effects) {
            buf.writeIdentifier(data.id());
            buf.writeVarInt(data.amplifier());
        }
    }

    public static ApplyPotionEffectsPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<EffectData> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Identifier id = buf.readIdentifier();
            int amplifier = buf.readVarInt();
            list.add(new EffectData(id, amplifier));
        }
        return new ApplyPotionEffectsPacket(list);
    }

    public static void handle(ApplyPotionEffectsPacket msg, CustomPayloadEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) return;

        Registry<MobEffect> registry = ((ServerLevel) player.level()).getServer()
            .registryAccess().lookupOrThrow(Registries.MOB_EFFECT);

        List<MobEffectInstance> effectInstances = new ArrayList<>();

        for (EffectData data : msg.effects) {
            Optional<Holder.Reference<MobEffect>> holder = registry.get(data.id());
            holder.ifPresent(ref -> {
                int amp = Math.max(0, Math.min(data.amplifier(), 255));
                // 지속시간은 장착 이벤트에서 갱신하므로 고정값 사용
                effectInstances.add(new MobEffectInstance(ref, 200, amp, true, false, true));
            });
        }

        // 아이템에 포션 효과 저장 (장착 시 EquipmentEffectHandler에서 자동 적용)
        PotionContents contents = new PotionContents(
            Optional.empty(), Optional.empty(), effectInstances, Optional.empty()
        );
        stack.set(DataComponents.POTION_CONTENTS, contents);
        ctx.setPacketHandled(true);
    }
}
