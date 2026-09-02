package com.ailingmeng.ultimatepvpboss.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class BossConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<String> BOSS_NAME;
    public static final ForgeConfigSpec.ConfigValue<String> SKIN_USERNAME;
    public static final ForgeConfigSpec.ConfigValue<String> BOSS_BAR_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> BOSS_BAR_OVERLAY;
    public static final ForgeConfigSpec.IntValue MAX_HEALTH;
    public static final ForgeConfigSpec.IntValue TOTEM_COUNT;
    public static final ForgeConfigSpec.IntValue GAPPLE_COUNT;
    public static final ForgeConfigSpec.IntValue HEAL_POTION_COUNT;
    public static final ForgeConfigSpec.IntValue PEARL_COUNT;
    public static final ForgeConfigSpec.IntValue CRYSTAL_COUNT;
    public static final ForgeConfigSpec.IntValue ANCHOR_COUNT;
    public static final ForgeConfigSpec.IntValue WEB_COUNT;
    public static final ForgeConfigSpec.IntValue POISON_COUNT;
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
        BOSS_NAME = b.comment("Default display name (supports color codes with & or §). Change in-game with /pvpboss name")
                .define("bossName", "The Legend");
        SKIN_USERNAME = b.comment("Minecraft username or direct image URL (http:// or https://) for the player skin")
                .define("skinUsername", "Steve");
        BOSS_BAR_COLOR = b.comment("Boss bar color: RED, BLUE, GREEN, YELLOW, PURPLE, WHITE, PINK")
                .define("bossBarColor", "RED");
        BOSS_BAR_OVERLAY = b.comment("Boss bar overlay style: PROGRESS, NOTCHED_6, NOTCHED_10, NOTCHED_12, NOTCHED_20")
                .define("bossBarOverlay", "NOTCHED_10");
        MAX_HEALTH = b.comment("Max health. A real player is 20. Legendary default is 40 plus 7 totems.")
                .defineInRange("maxHealth", 40, 20, 400);
        TOTEM_COUNT = b.comment("Totems of Undying the boss can pop")
                .defineInRange("totemCount", 7, 0, 64);
        GAPPLE_COUNT = b.comment("Enchanted golden apples the boss can eat")
                .defineInRange("gappleCount", 320, 0, 100000);
        HEAL_POTION_COUNT = b.comment("Healing potions the boss can drink")
                .defineInRange("healPotionCount", 160, 0, 100000);
        PEARL_COUNT = b.comment("Ender pearls the boss can throw/teleport")
                .defineInRange("pearlCount", 320, 0, 100000);
        CRYSTAL_COUNT = b.comment("End crystals the boss can place and detonate")
                .defineInRange("crystalCount", 1280, 0, 100000);
        ANCHOR_COUNT = b.comment("Respawn anchors the boss can place and detonate")
                .defineInRange("anchorCount", 320, 0, 100000);
        WEB_COUNT = b.comment("Cobwebs the boss can place")
                .defineInRange("webCount", 320, 0, 100000);
        POISON_COUNT = b.comment("Splash potions of poison the boss can throw")
                .defineInRange("poisonCount", 80, 0, 100000);
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

    public static String formatColor(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("(?i)&([0-9a-fk-or])", "§$1");
    }

    private BossConfig() {}
}
