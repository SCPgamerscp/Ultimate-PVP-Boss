package com.ailingmeng.ultimatepvpboss.client;

import com.ailingmeng.ultimatepvpboss.UltimatePvpBoss;
import com.ailingmeng.ultimatepvpboss.registry.ModEntities;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UltimatePvpBoss.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.PVP_BOSS.get(), PvpBossRenderer::new);
        event.registerEntityRenderer(ModEntities.BOSS_TRIDENT.get(), ThrownTridentRenderer::new);
    }
}
