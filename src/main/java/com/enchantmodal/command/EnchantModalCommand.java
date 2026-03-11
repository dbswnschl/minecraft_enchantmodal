package com.enchantmodal.command;

import com.enchantmodal.network.ModPacketHandler;
import com.enchantmodal.network.OpenEnchantModalPacket;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

public class EnchantModalCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("enchantmodal")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ItemStack heldItem = player.getMainHandItem();

                    if (heldItem.isEmpty()) {
                        context.getSource().sendFailure(
                            Component.translatable("commands.enchantmodal.no_item")
                        );
                        return 0;
                    }

                    ModPacketHandler.CHANNEL.send(
                        new OpenEnchantModalPacket(),
                        PacketDistributor.PLAYER.with(player)
                    );

                    context.getSource().sendSuccess(
                        () -> Component.translatable("commands.enchantmodal.opened"),
                        false
                    );
                    return 1;
                })
        );
    }
}
