package com.ailingmeng.ultimatepvpboss.event;

import com.ailingmeng.ultimatepvpboss.UltimatePvpBoss;
import com.ailingmeng.ultimatepvpboss.entity.PvpBossEntity;
import com.ailingmeng.ultimatepvpboss.registry.ModEntities;
import com.ailingmeng.ultimatepvpboss.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UltimatePvpBoss.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModBusEvents {
    private ModBusEvents() {}

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.PVP_BOSS.get(), PvpBossEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.PVP_BOSS_SPAWN_EGG);
        }
    }
}
