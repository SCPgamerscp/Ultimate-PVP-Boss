package com.ailingmeng.ultimatepvpboss.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class BossConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<String> BOSS_NAME;
    public static final ForgeConfigSpec.ConfigValue<String> SKIN_USERNAME;
    public static final ForgeConfigSpec.IntValue MAX_HEALTH;
    public static final ForgeConfigSpec.IntValue TOTEM_COUNT;
    public static final ForgeConfigSpec.IntValue VILLAGER_KILLS_TO_SPAWN;
    public static final ForgeConfigSpec.BooleanValue REPEAT_SPAWN;
    public static final ForgeConfigSpec.IntValue FOLLOW_RANGE;
    public static final ForgeConfigSpec.IntValue PEARL_DISTANCE;
    public static final ForgeConfigSpec.BooleanValue ALLOW_GRIEF;
    public static final ForgeConfigSpec.BooleanValue CHANNELING_ALWAYS;
    public static final ForgeConfigSpec.IntValue GAPLE_REWARD;
    public static final ForgeConfigSpec.IntValue DIAMOND_BLOCK_REWARD;
    public static final ForgeConfigSpec.BooleanValue ANNOUNCE;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("boss");
        BOSS_NAME = b.comment("Default display name. Change in-game with /pvpboss name")
                .define("bossName", "The Legend");
        SKIN_USERNAME = b.comment("Minecraft username used for the player skin")
                .define("skinUsername", "Steve");
        MAX_HEALTH = b.comment("Max health. A real player is 20. Legendary default is 40 plus 7 totems.")
                .defineInRange("maxHealth", 40, 20, 400);
        TOTEM_COUNT = b.comment("Totems of Undying the boss can pop")
                .defineInRange("totemCount", 7, 0, 64);
        VILLAGER_KILLS_TO_SPAWN = b.comment("Villagers a player must kill to summon the boss")
                .defineInRange("villagerKillsToSpawn", 10, 1, 1000);
        REPEAT_SPAWN = b.comment("If true, every N villager kills summons another boss")
                .define("repeatSpawn", true);
        FOLLOW_RANGE = b.comment("How far the boss will hunt a target")
                .defineInRange("followRange", 128, 16, 256);
        PEARL_DISTANCE = b.comment("Distance at which the boss ender pearls toward the target")
                .defineInRange("pearlDistance", 16, 8, 64);
        ALLOW_GRIEF = b.comment("Place/break blocks, crystals, anchors, cobwebs, lava")
                .define("allowGrief", true);
        CHANNELING_ALWAYS = b.comment("Loyalty tridents also call lightning on hit, even without a thunderstorm")
                .define("channelingAlways", true);
        GAPLE_REWARD = b.comment("Enchanted golden apples in the victory chest")
                .defineInRange("gappleReward", 1000, 0, 10000);
        DIAMOND_BLOCK_REWARD = b.comment("Diamond blocks in the victory chest")
                .defineInRange("diamondBlockReward", 200, 0, 10000);
        ANNOUNCE = b.comment("Broadcast spawn/death/totem messages")
                .define("announce", true);
        b.pop();
        SPEC = b.build();
    }

    private BossConfig() {}
}
