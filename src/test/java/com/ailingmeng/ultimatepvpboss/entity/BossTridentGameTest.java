package com.ailingmeng.ultimatepvpboss.entity;

import com.ailingmeng.ultimatepvpboss.UltimatePvpBoss;
import com.ailingmeng.ultimatepvpboss.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Loaded only by runGameTestServer, never included in the distributed mod JAR. */
@GameTestHolder(UltimatePvpBoss.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BossTridentGameTest {
    @GameTest(template = "empty")
    public static void spawnPacketRetainsTypeOwnerAndMotion(GameTestHelper helper) {
        PvpBossEntity boss = ModEntities.PVP_BOSS.get().create(helper.getLevel());
        helper.assertTrue(boss != null, "Boss factory must work");
        boss.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 2, 1))));
        helper.getLevel().addFreshEntity(boss);
        BossTridentEntity original = new BossTridentEntity(helper.getLevel(), boss);
        original.setDeltaMovement(0.1, 0.2, 2.6);
        helper.assertTrue(original.getAddEntityPacket() instanceof ClientboundAddEntityPacket,
                "Must use the owner-aware vanilla projectile packet");
        ClientboundAddEntityPacket packet = (ClientboundAddEntityPacket) original.getAddEntityPacket();
        helper.assertTrue(packet.getType() == ModEntities.BOSS_TRIDENT.get(), "Packet must retain custom type");
        helper.assertTrue(packet.getData() == boss.getId(), "Packet must include owner ID");
        helper.assertTrue(Math.abs(packet.getZa() - 2.6) < 0.001, "Packet must include velocity");
        BossTridentEntity replica = (BossTridentEntity) packet.getType().create(helper.getLevel());
        helper.assertTrue(replica != null, "Client-style type factory must instantiate guarded class");
        replica.recreateFromPacket(packet);
        helper.assertTrue(replica.getOwner() == boss, "Packet reconstruction must resolve Loyalty owner");
        boss.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void savedKitAndLoyaltyReturnSurvive(GameTestHelper helper) {
        PvpBossEntity boss = ModEntities.PVP_BOSS.get().create(helper.getLevel());
        helper.assertTrue(boss != null, "Boss factory must work");
        boss.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 2, 1))));
        helper.getLevel().addFreshEntity(boss);
        BossTridentEntity original = new BossTridentEntity(helper.getLevel(), boss);
        CompoundTag tag = new CompoundTag();
        original.addAdditionalSaveData(tag);
        ItemStack kit = ItemStack.of(tag.getCompound("Trident"));
        helper.assertTrue(EnchantmentHelper.getLoyalty(kit) == 3, "Loyalty III must persist");
        helper.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.IMPALING, kit) == 5,
                "Impaling V must persist");
        BossTridentEntity loaded = ModEntities.BOSS_TRIDENT.get().create(helper.getLevel());
        helper.assertTrue(loaded != null, "Saved entity factory must work");
        loaded.readAdditionalSaveData(tag);
        helper.assertTrue(loaded.getOwner() == boss && loaded.isChanneling() && loaded.isFoil(),
                "Owner, Channeling and enchanted appearance must survive reload");
        loaded.setPos(boss.getEyePosition().add(4, 0, 0));
        loaded.setNoPhysics(true);
        helper.assertTrue(loaded.findHitEntity(null, null) == null,
                "Returning flight must bypass the entity query entirely");
        loaded.tick();
        helper.assertTrue(loaded.getDeltaMovement().x < 0, "Loyalty must accelerate toward the owner");
        boss.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void serverHitsStillDamageButRepeatedQueriesStop(GameTestHelper helper) {
        Vec3 start = Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 2, 1)));
        Pig target = EntityType.PIG.create(helper.getLevel());
        helper.assertTrue(target != null, "Target factory must work");
        target.setNoAi(true);
        target.setPos(start.add(0, -0.4, 2));
        helper.getLevel().addFreshEntity(target);
        BossTridentEntity trident = ModEntities.BOSS_TRIDENT.get().create(helper.getLevel());
        helper.assertTrue(trident != null, "Trident factory must work");
        trident.setPos(start);
        trident.setDeltaMovement(0, 0, 2.6);
        helper.assertTrue(trident.findHitEntity(start, start.add(0, 0, 2.6)) != null,
                "First server query must detect the target");
        helper.assertTrue(trident.findHitEntity(null, null) == null,
                "Second query must stop before entering world collision code");
        float health = target.getHealth();
        trident.setPierceLevel((byte) 4);
        trident.tick();
        helper.assertTrue(target.getHealth() < health, "Server tick must still deal trident damage");
        target.discard();
        helper.succeed();
    }
}
