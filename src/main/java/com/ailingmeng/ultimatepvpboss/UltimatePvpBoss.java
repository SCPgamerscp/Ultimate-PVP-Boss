package com.ailingmeng.ultimatepvpboss;

import com.ailingmeng.ultimatepvpboss.config.BossConfig;
import com.ailingmeng.ultimatepvpboss.registry.ModEntities;
import com.ailingmeng.ultimatepvpboss.registry.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(UltimatePvpBoss.MOD_ID)
public class UltimatePvpBoss {
    public static final String MOD_ID = "ultimatepvpboss";
    public static final Logger LOGGER = LogManager.getLogger();

    public UltimatePvpBoss() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BossConfig.SPEC);
    }
}
