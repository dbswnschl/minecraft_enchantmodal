package com.enchantmodal.network;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ApplyEnchantmentsPacket {
    private final Map<Identifier, Integer> enchantments;

    public ApplyEnchantmentsPacket(Map<Identifier, Integer> enchantments) {
        this.enchantments = enchantments;
    }

    public static void encode(ApplyEnchantmentsPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.enchantments.size());
        msg.enchantments.forEach((id, level) -> {
            buf.writeIdentifier(id);
            buf.writeVarInt(level);
        });
    }

    public static ApplyEnchantmentsPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<Identifier, Integer> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            Identifier id = buf.readIdentifier();
            int level = buf.readVarInt();
            map.put(id, level);
        }
        return new ApplyEnchantmentsPacket(map);
    }

    public static void handle(ApplyEnchantmentsPacket msg, CustomPayloadEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) return;

        Registry<Enchantment> registry = ((ServerLevel) player.level()).getServer()
            .registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

        msg.enchantments.forEach((id, level) -> {
            Optional<Holder.Reference<Enchantment>> holder = registry.get(id);
            holder.ifPresent(ref -> {
                int clampedLevel = Math.max(1, Math.min(level, 255));
                mutable.set(ref, clampedLevel);
            });
        });

        stack.set(net.minecraft.core.component.DataComponents.ENCHANTMENTS, mutable.toImmutable());
        ctx.setPacketHandled(true);
    }
}
