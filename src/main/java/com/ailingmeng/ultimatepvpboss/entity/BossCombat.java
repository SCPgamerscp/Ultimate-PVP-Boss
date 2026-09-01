package com.ailingmeng.ultimatepvpboss.entity;

import com.ailingmeng.ultimatepvpboss.config.BossConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BossCombat {
    private final PvpBossEntity boss;

    private int swordCd;
    private int axeCd;
    private int bowCd;
    private int burstCd;
    private int burstLeft;
    private int crystalCd;
    private int anchorCd;
    private int webCd;
    private int lavaCd;
    private int poisonCd;
    private int honeyTicks;
    private int pearlCd;
    private int tridentCd;
    private int tridentLeft;
    private int healCd;
    private int gappleCd;
    private int speedCd;
    private int critJumpTicks;
    private int retreatTicks;
    private int strafeSign = 1;
    private int strafeFlip;
    private int terrainCd;
    private BlockPos pendingLava;
    private int lavaPlaceTicks;
    private LivingEntity forced;

    public BossCombat(PvpBossEntity boss) {
        this.boss = boss;
    }

    public void forceTarget(LivingEntity target) {
        this.forced = target;
    }

    public void tick() {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }
        tickCooldowns();
        if (honeyTicks == 1) {
            drinkHoney();
        }
        if (pendingLava != null && lavaPlaceTicks == 0) {
            if (grief()) {
                placeIfReplaceable(pendingLava, Blocks.LAVA.defaultBlockState());
            }
            pendingLava = null;
        }

        LivingEntity target = selectTarget(level);
        if (target == null) {
            boss.setSprinting(false);
            return;
        }
        boss.setTarget(target);
        boss.lookAtFast(target);
        boss.setSprinting(true);

        double dist = boss.distanceTo(target);
        float hp = boss.getHealth() / boss.getMaxHealth();

        if (hp <= 0.5F) {
            applyHalfHealthBuffs();
        }
        if (tryHeal(target, hp, dist)) {
            return;
        }
        if (dist > BossConfig.PEARL_DISTANCE.get()) {
            tryPearlToward(target);
            trySpeed();
        }

        handleTerrain(target, dist);

        boolean blocking = target instanceof Player p && p.isBlocking();

        if (burstLeft > 0 && boss.tickCount % 2 == 0) {
            firePiercingBolt(target);
            burstLeft--;
            boss.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, BossGear.crossbow());
            return;
        }
        if (tridentLeft > 0 && boss.tickCount % 6 == 0) {
            throwTrident(target);
            tridentLeft--;
            return;
        }

        if (dist <= 3.7) {
            if (poisonCd == 0 && dist < 3.2) {
                throwPoison(target);
            }
            if (webCd == 0) {
                placeWebThenLava(target);
            }
            if (blocking && axeCd == 0) {
                axeHit(target);
            } else if (swordCd == 0) {
                swordCrit(target);
            }
            circleStrafe(target);
        } else if (dist <= 8.5) {
            boolean acted = false;
            if (crystalCd == 0 && grief()) {
                acted = placeAndDetonateCrystal(target);
            }
            if (!acted && anchorCd == 0 && grief()) {
                acted = placeAndDetonateAnchor(target);
            }
            if (!acted && webCd == 0 && grief()) {
                placeWebThenLava(target);
                acted = true;
            }
            if (blocking && burstCd == 0) {
                startCrossbowBurst(target);
                acted = true;
            }
            if (!acted) {
                boss.getNavigation().moveTo(target, 1.25);
            }
        } else {
            if (blocking && burstCd == 0) {
                startCrossbowBurst(target);
            } else {
                if (tridentCd == 0 && dist < 28) {
                    startTridentVolley();
                }
                if (bowCd == 0) {
                    fireBow(target);
                }
            }
            if (dist > 14) {
                tryPearlToward(target);
            }
            boss.getNavigation().moveTo(target, 1.3);
        }
    }

    private void tickCooldowns() {
        if (swordCd > 0) swordCd--;
        if (axeCd > 0) axeCd--;
        if (bowCd > 0) bowCd--;
        if (burstCd > 0) burstCd--;
        if (crystalCd > 0) crystalCd--;
        if (anchorCd > 0) anchorCd--;
        if (webCd > 0) webCd--;
        if (lavaCd > 0) lavaCd--;
        if (poisonCd > 0) poisonCd--;
        if (pearlCd > 0) pearlCd--;
        if (tridentCd > 0) tridentCd--;
        if (healCd > 0) healCd--;
        if (gappleCd > 0) gappleCd--;
        if (speedCd > 0) speedCd--;
        if (honeyTicks > 0) honeyTicks--;
        if (retreatTicks > 0) retreatTicks--;
        if (lavaPlaceTicks > 0) lavaPlaceTicks--;
        if (terrainCd > 0) terrainCd--;
        if (critJumpTicks > 0) critJumpTicks--;
        if (strafeFlip++ > 40) {
            strafeFlip = 0;
            strafeSign = -strafeSign;
        }
    }

    private LivingEntity selectTarget(ServerLevel level) {
        if (forced != null && forced.isAlive() && !forced.isRemoved()) {
            if (forced.distanceTo(boss) < BossConfig.FOLLOW_RANGE.get()) {
                return forced;
            }
        }
        LivingEntity current = boss.getTarget();
        if (current != null && current.isAlive() && current.distanceTo(boss) < BossConfig.FOLLOW_RANGE.get()) {
            return current;
        }
        Player nearest = level.getNearestPlayer(boss, BossConfig.FOLLOW_RANGE.get());
        if (nearest != null && nearest.isAlive() && !nearest.isCreative() && !nearest.isSpectator()) {
            return nearest;
        }
        return null;
    }

    private void applyHalfHealthBuffs() {
        boolean needStrength = !boss.hasEffect(MobEffects.DAMAGE_BOOST);
        boolean needTurtle = !boss.hasEffect(MobEffects.DAMAGE_RESISTANCE);
        if (needStrength) {
            boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 90, 1, false, true, true));
        }
        if (needTurtle) {
            boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 20, 2, false, true, true));
            boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 20, 2, false, true, true));
        }
        if (needStrength || needTurtle) {
            boss.playSound(SoundEvents.GENERIC_DRINK, 1.0F, 1.0F);
            boss.setHalfBuffsUsed(true);
        }
    }

    private boolean tryHeal(LivingEntity target, float hpFrac, double dist) {
        if (hpFrac > 0.38F && retreatTicks == 0) {
            return false;
        }
        if (healCd > 0 && gappleCd > 0 && retreatTicks == 0) {
            return false;
        }
        if (retreatTicks == 0) {
            retreatTicks = 35;
            pearlAway(target);
            trySpeed();
        }
        if (gappleCd == 0) {
            drinkGapple();
        } else if (healCd == 0) {
            drinkHeals();
        }
        if (retreatTicks > 8) {
            return true;
        }
        return false;
    }

    private void drinkGapple() {
        gappleCd = 120;
        boss.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 20, 1));
        boss.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 120, 3));
        boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 300, 0));
        boss.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 300, 0));
        boss.heal(4.0F);
        boss.playSound(SoundEvents.PLAYER_BURP, 0.8F, 1.0F);
    }

    private void drinkHeals() {
        healCd = 45;
        boss.heal(16.0F);
        boss.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 22, 1));
        boss.playSound(SoundEvents.GENERIC_DRINK, 1.0F, 1.2F);
    }

    private void trySpeed() {
        if (speedCd > 0) {
            return;
        }
        if (boss.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            return;
        }
        speedCd = 20 * 16;
        boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 16, 1));
        boss.playSound(SoundEvents.GENERIC_DRINK, 0.8F, 1.4F);
    }

    private void tryPearlToward(LivingEntity target) {
        if (pearlCd > 0) {
            return;
        }
        Vec3 dest = findSafeNear(target.position(), 3.0, false);
        if (dest == null) {
            return;
        }
        pearlCd = 28;
        boss.teleportQuiet(dest.x, dest.y, dest.z);
    }

    private void pearlAway(LivingEntity target) {
        if (pearlCd > 0 && pearlCd < 20) {
            return;
        }
        Vec3 away = boss.position().subtract(target.position());
        if (away.lengthSqr() < 0.01) {
            away = new Vec3(boss.getRandom().nextGaussian(), 0, boss.getRandom().nextGaussian());
        }
        away = away.normalize().scale(12.0);
        Vec3 dest = findSafeNear(boss.position().add(away), 4.0, true);
        if (dest == null) {
            dest = boss.position().add(away.x, 0, away.z);
        }
        pearlCd = 20;
        boss.teleportQuiet(dest.x, dest.y, dest.z);
    }

    @javax.annotation.Nullable
    private Vec3 findSafeNear(Vec3 around, double radius, boolean preferOpen) {
        ServerLevel level = (ServerLevel) boss.level();
        for (int i = 0; i < 12; i++) {
            double ox = (boss.getRandom().nextDouble() - 0.5) * radius * 2;
            double oz = (boss.getRandom().nextDouble() - 0.5) * radius * 2;
            BlockPos pos = BlockPos.containing(around.x + ox, around.y, around.z + oz);
            pos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
            BlockState ground = level.getBlockState(pos.below());
            BlockState feet = level.getBlockState(pos);
            BlockState head = level.getBlockState(pos.above());
            if (ground.isSolid() && feet.isAir() && head.isAir()) {
                return Vec3.atBottomCenterOf(pos);
            }
            if (preferOpen && feet.canBeReplaced()) {
                return Vec3.atBottomCenterOf(pos);
            }
        }
        return around;
    }

    private void swordCrit(LivingEntity target) {
        if (critJumpTicks == 0 && boss.onGround()) {
            boss.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, BossGear.sword());
            boss.jumpNow();
            critJumpTicks = 4;
            return;
        }
        if (critJumpTicks > 0 && (boss.fallDistance > 0.0F || !boss.onGround())) {
            meleeHit(target, BossGear.sword(), true);
            swordCd = 10;
            critJumpTicks = 0;
        } else if (critJumpTicks == 0) {
            meleeHit(target, BossGear.sword(), false);
            swordCd = 10;
        }
    }

    private void axeHit(LivingEntity target) {
        meleeHit(target, BossGear.axe(), false);
        axeCd = 12;
        if (target instanceof Player player && player.isBlocking()) {
            player.disableShield(true);
        }
    }

    private void meleeHit(LivingEntity target, ItemStack weapon, boolean crit) {
        boss.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, weapon);
        boss.lookAtFast(target);
        float dmg = 8.0F + EnchantmentHelper.getDamageBonus(weapon, target.getMobType());
        if (crit) {
            dmg *= 1.5F;
        }
        if (boss.hasEffect(MobEffects.DAMAGE_BOOST)) {
            int amp = boss.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier();
            dmg += 3.0F * (amp + 1);
        }
        boolean hit = target.hurt(boss.damageSources().mobAttack(boss), dmg);
        boss.swing(InteractionHand.MAIN_HAND);
        if (hit) {
            int fire = EnchantmentHelper.getFireAspect(boss);
            if (fire > 0) {
                target.setSecondsOnFire(fire * 4);
            }
            if (crit) {
                boss.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.0F, 1.0F);
            } else {
                boss.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 1.0F);
            }
        }
    }

    private void fireBow(LivingEntity target) {
        boss.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, BossGear.bow());
        boss.lookAtFast(target);
        Arrow arrow = new Arrow(boss.level(), boss);
        Vec3 start = boss.getEyePosition();
        Vec3 aim = predict(target, start, 3.0F);
        Vec3 dir = aim.subtract(start);
        double len = dir.length();
        if (len < 0.001) {
            return;
        }
        dir = dir.normalize();
        arrow.setPos(start);
        arrow.shoot(dir.x, dir.y, dir.z, 3.15F, 0.4F);
        arrow.setBaseDamage(12.0D);
        arrow.setKnockback(2);
        arrow.setSecondsOnFire(100);
        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        boss.level().addFreshEntity(arrow);
        boss.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F);
        boss.swing(InteractionHand.MAIN_HAND);
        bowCd = 18;
    }

    private void startCrossbowBurst(LivingEntity target) {
        burstLeft = 5;
        burstCd = 90;
        boss.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, BossGear.crossbow());
        firePiercingBolt(target);
        burstLeft--;
    }

    private void firePiercingBolt(LivingEntity target) {
        boss.lookAtFast(target);
        Arrow arrow = new Arrow(boss.level(), boss);
        Vec3 start = boss.getEyePosition();
        Vec3 aim = predict(target, start, 3.2F);
        Vec3 dir = aim.subtract(start).normalize();
        arrow.setPos(start);
        arrow.shoot(dir.x, dir.y, dir.z, 3.4F, 0.15F);
        arrow.setBaseDamage(10.0D);
        arrow.setPierceLevel((byte) 4);
        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        boss.level().addFreshEntity(arrow);
        boss.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.15F);
        boss.swing(InteractionHand.MAIN_HAND);
    }

    private void startTridentVolley() {
        tridentLeft = 3;
        tridentCd = 55;
        boss.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, BossGear.trident());
    }

    private void throwTrident(LivingEntity target) {
        boss.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, BossGear.trident());
        boss.lookAtFast(target);
        ThrownTrident trident = new ThrownTrident(boss.level(), boss, BossGear.trident());
        Vec3 start = boss.getEyePosition();
        Vec3 aim = predict(target, start, 2.5F);
        Vec3 dir = aim.subtract(start).normalize();
        trident.shoot(dir.x, dir.y, dir.z, 2.6F, 0.3F);
        boss.level().addFreshEntity(trident);
        boss.playSound(SoundEvents.TRIDENT_THROW, 1.0F, 1.0F);
        boss.swing(InteractionHand.MAIN_HAND);
        if (BossConfig.CHANNELING_ALWAYS.get() && boss.level().canSeeSky(target.blockPosition())) {
            if (boss.getRandom().nextFloat() < 0.45F) {
                net.minecraft.world.entity.LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(boss.level());
                if (bolt != null) {
                    bolt.moveTo(target.getX(), target.getY(), target.getZ());
                    boss.level().addFreshEntity(bolt);
                }
            }
        }
    }

    private void throwPoison(LivingEntity target) {
        poisonCd = 90;
        honeyTicks = 18;
        boss.lookAtFast(target);
        ThrownPotion potion = new ThrownPotion(boss.level(), boss);
        ItemStack stack = new ItemStack(Items.SPLASH_POTION);
        PotionUtils.setPotion(stack, Potions.STRONG_POISON);
        potion.setItem(stack);
        Vec3 dir = target.getEyePosition().subtract(boss.getEyePosition());
        potion.shoot(dir.x, dir.y + 0.15, dir.z, 0.65F, 2.0F);
        boss.level().addFreshEntity(potion);
        boss.playSound(SoundEvents.SPLASH_POTION_THROW, 1.0F, 1.0F);
        boss.swing(InteractionHand.MAIN_HAND);
    }

    private void drinkHoney() {
        boss.removeEffect(MobEffects.POISON);
        boss.playSound(SoundEvents.HONEY_DRINK, 1.0F, 1.0F);
    }

    private void placeWebThenLava(LivingEntity target) {
        if (!grief()) {
            return;
        }
        BlockPos feet = target.blockPosition();
        if (placeIfReplaceable(feet, Blocks.COBWEB.defaultBlockState())) {
            webCd = 55;
            boss.playSound(SoundEvents.WOOL_PLACE, 1.0F, 0.8F);
            if (lavaCd == 0) {
                pendingLava = feet;
                lavaPlaceTicks = 8;
                lavaCd = 80;
            }
        }
    }

    private boolean placeAndDetonateCrystal(LivingEntity target) {
        BlockPos base = findAdjacentFloor(target.blockPosition());
        if (base == null) {
            crystalCd = 10;
            return false;
        }
        BlockPos obsidian = base;
        if (!boss.level().getBlockState(obsidian).is(Blocks.OBSIDIAN)) {
            if (!placeIfReplaceable(obsidian, Blocks.OBSIDIAN.defaultBlockState())
                    && !boss.level().getBlockState(obsidian).isSolid()) {
                return false;
            }
        }
        BlockPos crystalPos = obsidian.above();
        if (!boss.level().getBlockState(crystalPos).isAir()) {
            return false;
        }
        EndCrystal crystal = EntityType.END_CRYSTAL.create(boss.level());
        if (crystal == null) {
            return false;
        }
        crystal.moveTo(crystalPos.getX() + 0.5, crystalPos.getY(), crystalPos.getZ() + 0.5, 0, 0);
        crystal.setShowBottom(false);
        boss.level().addFreshEntity(crystal);
        crystalCd = 28;
        double selfDist = boss.distanceTo(crystal);
        double theirDist = target.distanceTo(crystal);
        if (selfDist > 3.2 && theirDist < 5.5) {
            crystal.hurt(boss.damageSources().mobAttack(boss), 1.0F);
        }
        return true;
    }

    private boolean placeAndDetonateAnchor(LivingEntity target) {
        BlockPos pos = findAdjacentFloor(target.blockPosition());
        if (pos == null) {
            anchorCd = 12;
            return false;
        }
        BlockState charged = Blocks.RESPAWN_ANCHOR.defaultBlockState().setValue(RespawnAnchorBlock.CHARGE, 4);
        if (!placeIfReplaceable(pos, charged) && !boss.level().getBlockState(pos).is(Blocks.RESPAWN_ANCHOR)) {
            return false;
        }
        boss.level().setBlock(pos, charged, 3);
        Vec3 center = Vec3.atCenterOf(pos);
        boss.level().explode(boss, center.x, center.y, center.z, 5.0F, true,
                net.minecraft.world.level.Level.ExplosionInteraction.BLOCK);
        boss.level().removeBlock(pos, false);
        anchorCd = 48;
        return true;
    }

    @javax.annotation.Nullable
    private BlockPos findAdjacentFloor(BlockPos around) {
        Direction[] dirs = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        for (Direction d : dirs) {
            BlockPos p = around.relative(d);
            BlockState st = boss.level().getBlockState(p);
            if (st.canBeReplaced() || st.isAir() || st.is(Blocks.COBWEB)) {
                BlockState below = boss.level().getBlockState(p.below());
                if (below.isSolid()) {
                    return p;
                }
                return p.below();
            }
        }
        return around;
    }

    private boolean placeIfReplaceable(BlockPos pos, BlockState state) {
        BlockState current = boss.level().getBlockState(pos);
        if (current.isAir() || current.canBeReplaced() || current.is(Blocks.COBWEB) || current.is(Blocks.LAVA) || current.is(Blocks.WATER)) {
            return boss.level().setBlock(pos, state, 3);
        }
        return false;
    }

    private void handleTerrain(LivingEntity target, double dist) {
        if (!grief() || terrainCd > 0) {
            return;
        }
        terrainCd = 4;
        double dy = target.getY() - boss.getY();
        if (dy > 2.2 && dist < 6) {
            pillarUp();
        }
        mineToward(target);
    }

    private void pillarUp() {
        if (!boss.onGround()) {
            return;
        }
        BlockPos feet = boss.blockPosition();
        boss.jumpNow();
        BlockPos under = feet;
        if (boss.level().getBlockState(under).canBeReplaced() || boss.level().getBlockState(under).isAir()) {
            boss.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new ItemStack(Items.OBSIDIAN));
            boss.level().setBlock(under, Blocks.COBBLESTONE.defaultBlockState(), 3);
            boss.playSound(SoundEvents.STONE_PLACE, 1.0F, 1.0F);
        }
    }

    private void mineToward(LivingEntity target) {
        Vec3 from = boss.getEyePosition();
        Vec3 to = target.getEyePosition();
        HitResult hit = boss.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, boss));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = boss.level().getBlockState(pos);
        float hardness = state.getDestroySpeed(boss.level(), pos);
        if (hardness < 0 || hardness > 50) {
            return;
        }
        if (state.is(Blocks.OBSIDIAN) && boss.distanceToSqr(Vec3.atCenterOf(pos)) > 16) {
            return;
        }
        boss.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, BossGear.pickaxe());
        boss.level().destroyBlock(pos, true, boss);
        boss.swing(InteractionHand.MAIN_HAND);
        boss.gameEvent(GameEvent.BLOCK_DESTROY);
    }

    private void circleStrafe(LivingEntity target) {
        Vec3 to = target.position().subtract(boss.position());
        Vec3 perp = new Vec3(-to.z, 0, to.x);
        if (perp.lengthSqr() < 0.0001) {
            return;
        }
        perp = perp.normalize().scale(0.18 * strafeSign);
        Vec3 motion = boss.getDeltaMovement().add(perp);
        boss.setDeltaMovement(motion.x, boss.getDeltaMovement().y, motion.z);
        if (boss.distanceTo(target) > 2.4) {
            boss.getNavigation().moveTo(target, 1.15);
        }
    }

    private Vec3 predict(LivingEntity target, Vec3 from, float speed) {
        Vec3 vel = target.getDeltaMovement();
        double dist = from.distanceTo(target.getEyePosition());
        double t = Mth.clamp(dist / speed, 0.05, 1.6);
        return target.getEyePosition().add(vel.scale(t * 8.0));
    }

    private boolean grief() {
        return BossConfig.ALLOW_GRIEF.get();
    }

    @SuppressWarnings("unused")
    private boolean isPassive(LivingEntity e) {
        return e instanceof Animal;
    }
}
