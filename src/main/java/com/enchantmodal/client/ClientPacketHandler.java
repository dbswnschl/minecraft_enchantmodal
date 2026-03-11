package com.enchantmodal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ClientPacketHandler {

    public static void openEnchantScreen() {
        Minecraft.getInstance().setScreen(
            new EnchantModalScreen(Component.translatable("gui.enchantmodal.title"))
        );
    }
}
