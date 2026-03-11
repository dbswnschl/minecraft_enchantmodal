package com.enchantmodal.network;

import com.enchantmodal.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;

public class OpenEnchantModalPacket {

    public OpenEnchantModalPacket() {
    }

    public static void encode(OpenEnchantModalPacket msg, FriendlyByteBuf buf) {
    }

    public static OpenEnchantModalPacket decode(FriendlyByteBuf buf) {
        return new OpenEnchantModalPacket();
    }

    public static void handle(OpenEnchantModalPacket msg, CustomPayloadEvent.Context ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientPacketHandler::openEnchantScreen);
        ctx.setPacketHandled(true);
    }
}
