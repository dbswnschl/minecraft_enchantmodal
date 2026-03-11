package com.enchantmodal;

import com.enchantmodal.command.EnchantModalCommand;
import com.enchantmodal.network.ModPacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.bus.EventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(EnchantModal.MODID)
public class EnchantModal {
    public static final String MODID = "enchantmodal";

    public EnchantModal(FMLJavaModLoadingContext context) {
        // 모드 버스: 라이프사이클 이벤트
        EventBus.create(context.getModBusGroup(), FMLCommonSetupEvent.class)
            .addListener(this::commonSetup);

        // Forge 버스: addListener를 위해 BusGroup 접근
        try {
            java.lang.reflect.Field groupField = net.minecraftforge.common.EventBusMigrationHelper.class
                .getDeclaredField("group");
            groupField.setAccessible(true);
            BusGroup forgeBusGroup = (BusGroup) groupField.get(MinecraftForge.EVENT_BUS);
            EventBus.create(forgeBusGroup, RegisterCommandsEvent.class)
                .addListener(this::onRegisterCommands);
            EventBus.create(forgeBusGroup, TickEvent.PlayerTickEvent.Post.class)
                .addListener(EquipmentEffectHandler::onPlayerTick);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to access Forge event bus group", e);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModPacketHandler.init();
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        EnchantModalCommand.register(event.getDispatcher());
    }
}
