package com.ailingmeng.ultimatepvpboss.registry;

import com.ailingmeng.ultimatepvpboss.UltimatePvpBoss;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, UltimatePvpBoss.MOD_ID);

    public static final RegistryObject<Item> PVP_BOSS_SPAWN_EGG = ITEMS.register("pvp_boss_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.PVP_BOSS, 0x1B1210, 0x8F2F2F, new Item.Properties()));

    private ModItems() {}
}
