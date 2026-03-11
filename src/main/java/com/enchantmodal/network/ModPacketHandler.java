package com.enchantmodal.network;

import com.enchantmodal.EnchantModal;
import net.minecraft.resources.Identifier;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

public class ModPacketHandler {

    public static final SimpleChannel CHANNEL = ChannelBuilder
        .named(Identifier.fromNamespaceAndPath(EnchantModal.MODID, "main"))
        .networkProtocolVersion(1)
        .optional()
        .simpleChannel();

    public static void init() {
        CHANNEL.messageBuilder(OpenEnchantModalPacket.class)
            .encoder(OpenEnchantModalPacket::encode)
            .decoder(OpenEnchantModalPacket::decode)
            .consumerMainThread(OpenEnchantModalPacket::handle)
            .add();

        CHANNEL.messageBuilder(ApplyEnchantmentsPacket.class)
            .encoder(ApplyEnchantmentsPacket::encode)
            .decoder(ApplyEnchantmentsPacket::decode)
            .consumerMainThread(ApplyEnchantmentsPacket::handle)
            .add();

        CHANNEL.messageBuilder(ApplyPotionEffectsPacket.class)
            .encoder(ApplyPotionEffectsPacket::encode)
            .decoder(ApplyPotionEffectsPacket::decode)
            .consumerMainThread(ApplyPotionEffectsPacket::handle)
            .add();
    }
}
