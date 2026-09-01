package com.ailingmeng.ultimatepvpboss.entity;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public final class BossGear {
    private BossGear() {}

    public static void equip(PvpBossEntity boss) {
        ItemStack helm = new ItemStack(Items.NETHERITE_HELMET);
        helm.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        helm.enchant(Enchantments.UNBREAKING, 3);
        helm.enchant(Enchantments.MENDING, 1);
        helm.enchant(Enchantments.RESPIRATION, 3);
        helm.enchant(Enchantments.AQUA_AFFINITY, 1);

        ItemStack chest = new ItemStack(Items.NETHERITE_CHESTPLATE);
        chest.enchant(Enchantments.BLAST_PROTECTION, 4);
        chest.enchant(Enchantments.UNBREAKING, 3);
        chest.enchant(Enchantments.MENDING, 1);

        ItemStack legs = new ItemStack(Items.NETHERITE_LEGGINGS);
        legs.enchant(Enchantments.BLAST_PROTECTION, 4);
        legs.enchant(Enchantments.UNBREAKING, 3);
        legs.enchant(Enchantments.MENDING, 1);

        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);
        boots.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        boots.enchant(Enchantments.FALL_PROTECTION, 4);
        boots.enchant(Enchantments.UNBREAKING, 3);
        boots.enchant(Enchantments.MENDING, 1);
        boots.enchant(Enchantments.SOUL_SPEED, 3);
        boots.enchant(Enchantments.DEPTH_STRIDER, 3);

        boss.setItemSlot(EquipmentSlot.HEAD, helm);
        boss.setItemSlot(EquipmentSlot.CHEST, chest);
        boss.setItemSlot(EquipmentSlot.LEGS, legs);
        boss.setItemSlot(EquipmentSlot.FEET, boots);
        boss.setItemSlot(EquipmentSlot.MAINHAND, sword());
        boss.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            boss.setDropChance(slot, 0.0F);
        }
    }

    public static ItemStack sword() {
        ItemStack s = new ItemStack(Items.NETHERITE_SWORD);
        s.enchant(Enchantments.SHARPNESS, 5);
        s.enchant(Enchantments.FIRE_ASPECT, 2);
        s.enchant(Enchantments.KNOCKBACK, 1);
        s.enchant(Enchantments.SWEEPING_EDGE, 3);
        s.enchant(Enchantments.UNBREAKING, 3);
        s.enchant(Enchantments.MENDING, 1);
        s.enchant(Enchantments.MOB_LOOTING, 3);
        return s;
    }

    public static ItemStack axe() {
        ItemStack s = new ItemStack(Items.NETHERITE_AXE);
        s.enchant(Enchantments.SHARPNESS, 5);
        s.enchant(Enchantments.UNBREAKING, 3);
        s.enchant(Enchantments.MENDING, 1);
        s.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        return s;
    }

    public static ItemStack bow() {
        ItemStack s = new ItemStack(Items.BOW);
        s.enchant(Enchantments.POWER_ARROWS, 5);
        s.enchant(Enchantments.PUNCH_ARROWS, 2);
        s.enchant(Enchantments.FLAMING_ARROWS, 1);
        s.enchant(Enchantments.INFINITY_ARROWS, 1);
        s.enchant(Enchantments.UNBREAKING, 3);
        return s;
    }

    public static ItemStack crossbow() {
        ItemStack s = new ItemStack(Items.CROSSBOW);
        s.enchant(Enchantments.PIERCING, 4);
        s.enchant(Enchantments.QUICK_CHARGE, 3);
        s.enchant(Enchantments.UNBREAKING, 3);
        s.enchant(Enchantments.MENDING, 1);
        return s;
    }

    public static ItemStack trident() {
        ItemStack s = new ItemStack(Items.TRIDENT);
        s.enchant(Enchantments.LOYALTY, 3);
        s.enchant(Enchantments.CHANNELING, 1);
        s.enchant(Enchantments.IMPALING, 5);
        s.enchant(Enchantments.UNBREAKING, 3);
        s.enchant(Enchantments.MENDING, 1);
        return s;
    }

    public static ItemStack pickaxe() {
        ItemStack s = new ItemStack(Items.NETHERITE_PICKAXE);
        s.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        s.enchant(Enchantments.UNBREAKING, 3);
        s.enchant(Enchantments.MENDING, 1);
        return s;
    }
}
