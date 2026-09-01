package com.ailingmeng.ultimatepvpboss.registry;

import com.ailingmeng.ultimatepvpboss.UltimatePvpBoss;
import com.ailingmeng.ultimatepvpboss.entity.PvpBossEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, UltimatePvpBoss.MOD_ID);

    public static final RegistryObject<EntityType<PvpBossEntity>> PVP_BOSS = ENTITIES.register("pvp_boss",
            () -> EntityType.Builder.of(PvpBossEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build("pvp_boss"));

    private ModEntities() {}
}
