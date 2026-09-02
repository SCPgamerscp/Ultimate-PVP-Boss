package com.ailingmeng.ultimatepvpboss.event;

import com.ailingmeng.ultimatepvpboss.UltimatePvpBoss;
import com.ailingmeng.ultimatepvpboss.command.PvpBossCommands;
import com.ailingmeng.ultimatepvpboss.config.BossConfig;
import com.ailingmeng.ultimatepvpboss.entity.PvpBossEntity;
import com.ailingmeng.ultimatepvpboss.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingUseTotemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UltimatePvpBoss.MOD_ID)
public final class ModEvents {
    public static final String KILL_KEY = "ultimatepvpboss_villager_kills";

    private ModEvents() {}

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent event) {
        PvpBossCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void totem(LivingUseTotemEvent event) {
        if (event.getEntity() instanceof PvpBossEntity boss) {
            boss.onTotemPopped();
        }
    }

    @SubscribeEvent
    public static void hurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof PvpBossEntity boss
                && event.getSource().getEntity() instanceof LivingEntity living
                && living != boss) {
            boss.onAttackedBy(living);
        }
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Villager)) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        CompoundTag persistent = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        int kills = persistent.getInt(KILL_KEY) + 1;
        persistent.putInt(KILL_KEY, kills);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistent);

        int needed = BossConfig.VILLAGER_KILLS_TO_SPAWN.get();
        boolean shouldSpawn = BossConfig.REPEAT_SPAWN.get()
                ? kills % needed == 0
                : kills == needed;
        if (!shouldSpawn) {
            return;
        }
        PvpBossEntity boss = spawnNear(level, player.blockPosition(), player);
        if (boss != null && BossConfig.ANNOUNCE.get()) {
            Component msg = Component.translatable("message.ultimatepvpboss.awakened",
                    player.getDisplayName(), boss.getDisplayName());
            level.players().forEach(p -> p.sendSystemMessage(msg));
        }
    }

    public static int getKills(ServerPlayer player) {
        return player.getPersistentData()
                .getCompound(Player.PERSISTED_NBT_TAG)
                .getInt(KILL_KEY);
    }

    public static void resetKills(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persistent.putInt(KILL_KEY, 0);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistent);
    }

    public static PvpBossEntity spawnNear(ServerLevel level, BlockPos around, @javax.annotation.Nullable ServerPlayer target) {
        BlockPos spawn = findOpen(level, around);
        PvpBossEntity boss = ModEntities.PVP_BOSS.get().create(level);
        if (boss == null) {
            return null;
        }
        boss.moveTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
        boss.applyConfig();
        com.ailingmeng.ultimatepvpboss.entity.BossGear.equip(boss);
        if (target != null) {
            boss.onAttackedBy(target);
        }
        level.addFreshEntity(boss);
        return boss;
    }

    private static BlockPos findOpen(ServerLevel level, BlockPos around) {
        for (int i = 0; i < 16; i++) {
            int ox = 4 + level.random.nextInt(6);
            int oz = 4 + level.random.nextInt(6);
            if (level.random.nextBoolean()) ox = -ox;
            if (level.random.nextBoolean()) oz = -oz;
            BlockPos p = around.offset(ox, 0, oz);
            p = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p);
            if (level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir()) {
                return p;
            }
        }
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, around);
    }
}
