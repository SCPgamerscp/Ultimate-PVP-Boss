package com.ailingmeng.ultimatepvpboss.entity;

import com.ailingmeng.ultimatepvpboss.config.BossConfig;
import com.ailingmeng.ultimatepvpboss.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/** Keeps vanilla flight, damage and Loyalty, but bounds the boss's entity collision path. */
public final class BossTridentEntity extends ThrownTrident {
    private final TridentCollisionBudget collisionBudget = new TridentCollisionBudget();

    // Both client spawning and world loading must use our registered type. The vanilla
    // owner/stack constructor hardcodes EntityType.TRIDENT and loses this subclass.
    public BossTridentEntity(EntityType<? extends BossTridentEntity> type, Level level) {
        super(type, level);
        // Vanilla's private stack/Loyalty have no public setter. Use its NBT API,
        // retaining all other projectile defaults. Later world loading restores saved data.
        CompoundTag data = new CompoundTag();
        super.addAdditionalSaveData(data);
        data.put("Trident", BossGear.trident().save(new CompoundTag()));
        super.readAdditionalSaveData(data);
        pickup = Pickup.CREATIVE_ONLY;
    }

    public BossTridentEntity(Level level, PvpBossEntity owner) {
        this(ModEntities.BOSS_TRIDENT.get(), level);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1F, owner.getZ());
    }

    @Override
    public void tick() {
        collisionBudget.beginTick();
        super.tick();
    }

    @Nullable
    @Override
    protected EntityHitResult findHitEntity(Vec3 start, Vec3 end) {
        // All three supplied stall samples enter this entity-query path on the client.
        // Entity hits are server-authoritative; clients retain vanilla flight/visuals
        // and receive the server's movement and Loyalty no-physics flag.
        if (!collisionBudget.tryQuery(level().isClientSide, isNoPhysics())) return null;
        return super.findHitEntity(start, end);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide && BossConfig.CHANNELING_ALWAYS.get() && !level().isThundering()) {
            Entity target = result.getEntity();
            BlockPos pos = target.blockPosition();
            if (level().canSeeSky(pos)) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level());
                if (bolt != null) {
                    bolt.moveTo(Vec3.atBottomCenterOf(pos));
                    Entity owner = getOwner();
                    if (owner instanceof ServerPlayer player) {
                        bolt.setCause(player);
                    }
                    level().addFreshEntity(bolt);
                    playSound(SoundEvents.TRIDENT_THUNDER, 5.0F, 1.0F);
                }
            }
        }
    }

    @Override
    public boolean isFoil() {
        // This entity always uses the boss's enchanted kit, also after NBT loading.
        return true;
    }

    // Inherit Projectile's ClientboundAddEntityPacket and recreateFromPacket. They
    // carry our registered entity type AND the owner ID needed for client Loyalty.
    // Forge's generic spawn packet would lose the owner without extra spawn data.
}
