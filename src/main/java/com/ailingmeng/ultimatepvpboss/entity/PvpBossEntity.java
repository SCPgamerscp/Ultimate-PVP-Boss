package com.ailingmeng.ultimatepvpboss.entity;

import com.ailingmeng.ultimatepvpboss.config.BossConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class PvpBossEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> SKIN =
            SynchedEntityData.defineId(PvpBossEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> TOTEMS =
            SynchedEntityData.defineId(PvpBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SLIM =
            SynchedEntityData.defineId(PvpBossEntity.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.literal(BossConfig.BOSS_NAME.get()),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.NOTCHED_10);

    private final BossCombat combat = new BossCombat(this);
    private boolean halfBuffsUsed;
    private boolean equipped;

    public PvpBossEntity(EntityType<? extends PvpBossEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.setCanPickUpLoot(false);
        this.xpReward = 250;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.10D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 128.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.35D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 12.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SKIN, "Steve");
        this.entityData.define(TOTEMS, 7);
        this.entityData.define(SLIM, false);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data, tag);
        applyConfig();
        BossGear.equip(this);
        this.equipped = true;
        this.setCustomNameVisible(true);
        return result;
    }

    public void applyConfig() {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(BossConfig.MAX_HEALTH.get());
        this.setHealth(this.getMaxHealth());
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(BossConfig.FOLLOW_RANGE.get());
        this.setTotemsLeft(BossConfig.TOTEM_COUNT.get());
        this.combat.initCounts();
        this.setSkinUsername(BossConfig.SKIN_USERNAME.get());
        this.setCustomName(Component.literal(BossConfig.formatColor(BossConfig.BOSS_NAME.get())));
        applyBossBarSettings();
        this.bossEvent.setName(this.getDisplayName());
    }

    public void applyBossBarSettings() {
        try {
            this.bossEvent.setColor(BossEvent.BossBarColor.valueOf(BossConfig.BOSS_BAR_COLOR.get().toUpperCase()));
        } catch (Exception ignored) {}
        try {
            this.bossEvent.setOverlay(BossEvent.BossBarOverlay.valueOf(BossConfig.BOSS_BAR_OVERLAY.get().toUpperCase()));
        } catch (Exception ignored) {}
    }

    public ServerBossEvent getBossEvent() {
        return this.bossEvent;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && !this.equipped) {
            BossGear.equip(this);
            this.equipped = true;
        }
        if (!this.level().isClientSide) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            this.bossEvent.setName(this.getDisplayName());
            this.combat.tick();
            cleanupLoyaltyTridents();
        }
    }

    private void cleanupLoyaltyTridents() {
        List<ThrownTrident> tridents = this.level().getEntitiesOfClass(ThrownTrident.class, this.getBoundingBox().inflate(2.0D),
                t -> t.getOwner() == this);
        for (ThrownTrident t : tridents) {
            if (t.tickCount > 10) {
                this.playSound(SoundEvents.ITEM_PICKUP, 0.6F, 1.2F);
                t.discard();
            }
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        this.bossEvent.removeAllPlayers();
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide && !this.dead) {
            dropVictoryChest();
            if (BossConfig.ANNOUNCE.get()) {
                Component msg = Component.translatable("message.ultimatepvpboss.fallen", this.getDisplayName());
                this.level().players().forEach(p -> p.sendSystemMessage(msg));
            }
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                this.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
        super.die(source);
    }

    private void dropVictoryChest() {
        BlockPos pos = this.blockPosition();
        this.level().setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
        if (this.level().getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            int slot = 0;
            int gapples = BossConfig.GAPLE_REWARD.get();
            while (gapples > 0 && slot < 27) {
                int n = Math.min(64, gapples);
                chest.setItem(slot++, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, n));
                gapples -= n;
            }
            int diamonds = BossConfig.DIAMOND_BLOCK_REWARD.get();
            while (diamonds > 0 && slot < 27) {
                int n = Math.min(64, diamonds);
                chest.setItem(slot++, new ItemStack(Items.DIAMOND_BLOCK, n));
                diamonds -= n;
            }
        }
    }

    public void onTotemPopped() {
        int left = Math.max(0, getTotemsLeft() - 1);
        setTotemsLeft(left);
        if (left > 0) {
            this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
        }
        if (BossConfig.ANNOUNCE.get() && this.level() instanceof ServerLevel server) {
            Component msg = Component.translatable("message.ultimatepvpboss.totem", this.getDisplayName(), left);
            server.players().forEach(p -> p.sendSystemMessage(msg));
        }
    }

    public void onAttackedBy(LivingEntity attacker) {
        if (attacker != null && attacker.isAlive() && attacker != this) {
            this.setTarget(attacker);
            this.combat.forceTarget(attacker);
        }
    }

    public void jumpNow() {
        this.jumpFromGround();
    }

    public void lookAtFast(LivingEntity target) {
        Vec3 eyes = target.getEyePosition();
        double dx = eyes.x - this.getX();
        double dy = eyes.y - this.getEyeY();
        double dz = eyes.z - this.getZ();
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * (180F / Math.PI)) - 90.0F;
        float pitch = (float) (-(Math.atan2(dy, horiz) * (180F / Math.PI)));
        this.setYRot(yaw);
        this.setXRot(pitch);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
        this.getLookControl().setLookAt(target, 180.0F, 180.0F);
    }

    public void teleportQuiet(double x, double y, double z) {
        this.teleportTo(x, y, z);
        this.fallDistance = 0.0F;
        this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                    this.getX(), this.getY() + 1.0, this.getZ(), 32, 0.4, 0.8, 0.4, 0.2);
        }
    }

    public boolean halfBuffsUsed() {
        return halfBuffsUsed;
    }

    public void setHalfBuffsUsed(boolean v) {
        this.halfBuffsUsed = v;
    }

    public String getSkinUsername() {
        return this.entityData.get(SKIN);
    }

    public void setSkinUsername(String name) {
        if (name == null || name.isBlank()) {
            name = "Steve";
        }
        this.entityData.set(SKIN, name);
    }

    public int getTotemsLeft() {
        return this.entityData.get(TOTEMS);
    }

    public void setTotemsLeft(int n) {
        this.entityData.set(TOTEMS, Math.max(0, n));
    }

    public boolean isSlimSkin() {
        return this.entityData.get(SLIM);
    }

    public void setSlimSkin(boolean slim) {
        this.entityData.set(SLIM, slim);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("SkinUsername", getSkinUsername());
        tag.putInt("TotemsLeft", getTotemsLeft());
        tag.putBoolean("HalfBuffs", halfBuffsUsed);
        tag.putBoolean("Slim", isSlimSkin());
        tag.putInt("GappleCount", combat.getGappleCount());
        tag.putInt("HealPotionCount", combat.getHealPotionCount());
        tag.putInt("PearlCount", combat.getPearlCount());
        tag.putInt("CrystalCount", combat.getCrystalCount());
        tag.putInt("AnchorCount", combat.getAnchorCount());
        tag.putInt("WebCount", combat.getWebCount());
        tag.putInt("PoisonCount", combat.getPoisonCount());
        tag.putInt("HoneyCount", combat.getHoneyCount());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SkinUsername")) {
            setSkinUsername(tag.getString("SkinUsername"));
        }
        if (tag.contains("TotemsLeft")) {
            setTotemsLeft(tag.getInt("TotemsLeft"));
        }
        halfBuffsUsed = tag.getBoolean("HalfBuffs");
        setSlimSkin(tag.getBoolean("Slim"));
        if (tag.contains("GappleCount")) combat.setGappleCount(tag.getInt("GappleCount"));
        if (tag.contains("HealPotionCount")) combat.setHealPotionCount(tag.getInt("HealPotionCount"));
        if (tag.contains("PearlCount")) combat.setPearlCount(tag.getInt("PearlCount"));
        if (tag.contains("CrystalCount")) combat.setCrystalCount(tag.getInt("CrystalCount"));
        if (tag.contains("AnchorCount")) combat.setAnchorCount(tag.getInt("AnchorCount"));
        if (tag.contains("WebCount")) combat.setWebCount(tag.getInt("WebCount"));
        if (tag.contains("PoisonCount")) combat.setPoisonCount(tag.getInt("PoisonCount"));
        if (tag.contains("HoneyCount")) combat.setHoneyCount(tag.getInt("HoneyCount"));
        this.bossEvent.setName(this.getDisplayName());
        this.equipped = true;
    }
}
