package com.ailingmeng.ultimatepvpboss.command;

import com.ailingmeng.ultimatepvpboss.config.BossConfig;
import com.ailingmeng.ultimatepvpboss.entity.BossCombat;
import com.ailingmeng.ultimatepvpboss.entity.PvpBossEntity;
import com.ailingmeng.ultimatepvpboss.event.ModEvents;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class PvpBossCommands {
    private static final List<String> BAR_COLORS = List.of("RED", "BLUE", "GREEN", "YELLOW", "PURPLE", "WHITE", "PINK");
    private static final List<String> BAR_STYLES = List.of("PROGRESS", "NOTCHED_6", "NOTCHED_10", "NOTCHED_12", "NOTCHED_20");

    private PvpBossCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pvpboss")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("spawn")
                        .executes(PvpBossCommands::spawnSelf)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(PvpBossCommands::spawnAt)))
                .then(Commands.literal("name")
                        .then(Commands.literal("reset")
                                .executes(PvpBossCommands::nameReset))
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        ctx.getSource().getOnlinePlayerNames(), builder
                                ))
                                .executes(PvpBossCommands::rename)))
                .then(Commands.literal("skin")
                        .then(Commands.literal("reset")
                                .executes(PvpBossCommands::skinReset))
                        .then(Commands.argument("username", StringArgumentType.greedyString())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        ctx.getSource().getOnlinePlayerNames(), builder
                                ))
                                .executes(PvpBossCommands::skin)))
                .then(Commands.literal("bar")
                        .then(Commands.literal("reset")
                                .executes(PvpBossCommands::barReset))
                        .then(Commands.literal("color")
                                .then(Commands.argument("color", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(BAR_COLORS, builder))
                                        .executes(PvpBossCommands::barColor)))
                        .then(Commands.literal("style")
                                .then(Commands.argument("style", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(BAR_STYLES, builder))
                                        .executes(PvpBossCommands::barStyle))))
                .then(Commands.literal("reset")
                        .executes(PvpBossCommands::resetAll)
                        .then(Commands.literal("all")
                                .executes(PvpBossCommands::resetAll))
                        .then(Commands.literal("skin")
                                .executes(PvpBossCommands::skinReset))
                        .then(Commands.literal("name")
                                .executes(PvpBossCommands::nameReset))
                        .then(Commands.literal("bar")
                                .executes(PvpBossCommands::barReset))
                        .then(Commands.literal("kills")
                                .executes(PvpBossCommands::killsResetSelf)
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(PvpBossCommands::killsResetOther))))
                .then(Commands.literal("items")
                        .executes(PvpBossCommands::items))
                .then(Commands.literal("remove")
                        .executes(PvpBossCommands::remove))
                .then(Commands.literal("kills")
                        .executes(PvpBossCommands::killsSelf)
                        .then(Commands.literal("reset")
                                .executes(PvpBossCommands::killsResetSelf)
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(PvpBossCommands::killsResetOther)))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(PvpBossCommands::killsOther))));
    }

    private static int spawnSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        ServerLevel level = ctx.getSource().getLevel();
        PvpBossEntity boss = ModEvents.spawnNear(level, ctx.getSource().getEntity() != null
                ? ctx.getSource().getEntity().blockPosition()
                : level.getSharedSpawnPos(), player);
        if (boss == null) {
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.spawned", boss.getDisplayName()), true);
        return 1;
    }

    private static int spawnAt(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        PvpBossEntity boss = ModEvents.spawnNear(player.serverLevel(), player.blockPosition(), player);
        if (boss == null) {
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.spawned", boss.getDisplayName()), true);
        return 1;
    }

    private static int rename(CommandContext<CommandSourceStack> ctx) {
        String raw = StringArgumentType.getString(ctx, "name");
        if (raw.equalsIgnoreCase("reset") || raw.equalsIgnoreCase("default")) {
            return nameReset(ctx);
        }
        String formatted = BossConfig.formatColor(raw);
        BossConfig.BOSS_NAME.set(raw);
        List<PvpBossEntity> bosses = bossesNear(ctx);
        for (PvpBossEntity boss : bosses) {
            boss.setCustomName(Component.literal(formatted));
            boss.setCustomNameVisible(true);
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.named", formatted), true);
        return Math.max(1, bosses.size());
    }

    private static int nameReset(CommandContext<CommandSourceStack> ctx) {
        String def = "The Legend";
        BossConfig.BOSS_NAME.set(def);
        List<PvpBossEntity> bosses = bossesNear(ctx);
        for (PvpBossEntity boss : bosses) {
            boss.setCustomName(Component.literal(def));
            boss.setCustomNameVisible(true);
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.name_reset", def), true);
        return Math.max(1, bosses.size());
    }

    private static int barColor(CommandContext<CommandSourceStack> ctx) {
        String colorName = StringArgumentType.getString(ctx, "color").toUpperCase();
        try {
            net.minecraft.world.BossEvent.BossBarColor color = net.minecraft.world.BossEvent.BossBarColor.valueOf(colorName);
            BossConfig.BOSS_BAR_COLOR.set(colorName);
            List<PvpBossEntity> bosses = bossesNear(ctx);
            for (PvpBossEntity boss : bosses) {
                boss.getBossEvent().setColor(color);
            }
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.bar_color", colorName), true);
            return Math.max(1, bosses.size());
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.translatable("command.ultimatepvpboss.bar_invalid_color"));
            return 0;
        }
    }

    private static int barStyle(CommandContext<CommandSourceStack> ctx) {
        String styleName = StringArgumentType.getString(ctx, "style").toUpperCase();
        try {
            net.minecraft.world.BossEvent.BossBarOverlay overlay = net.minecraft.world.BossEvent.BossBarOverlay.valueOf(styleName);
            BossConfig.BOSS_BAR_OVERLAY.set(styleName);
            List<PvpBossEntity> bosses = bossesNear(ctx);
            for (PvpBossEntity boss : bosses) {
                boss.getBossEvent().setOverlay(overlay);
            }
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.bar_style", styleName), true);
            return Math.max(1, bosses.size());
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.translatable("command.ultimatepvpboss.bar_invalid_style"));
            return 0;
        }
    }

    private static int barReset(CommandContext<CommandSourceStack> ctx) {
        BossConfig.BOSS_BAR_COLOR.set("RED");
        BossConfig.BOSS_BAR_OVERLAY.set("NOTCHED_10");
        List<PvpBossEntity> bosses = bossesNear(ctx);
        for (PvpBossEntity boss : bosses) {
            boss.applyBossBarSettings();
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.bar_reset"), true);
        return Math.max(1, bosses.size());
    }

    private static int resetAll(CommandContext<CommandSourceStack> ctx) {
        nameReset(ctx);
        skinReset(ctx);
        barReset(ctx);
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player != null) {
            ModEvents.resetKills(player);
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.all_reset"), true);
        return 1;
    }

    private static int killsResetSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.translatable("command.ultimatepvpboss.none"));
            return 0;
        }
        ModEvents.resetKills(player);
        ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.kills_reset", player.getDisplayName()), true);
        return 1;
    }

    private static int killsResetOther(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        ModEvents.resetKills(player);
        ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.kills_reset", player.getDisplayName()), true);
        return 1;
    }

    private static int skin(CommandContext<CommandSourceStack> ctx) {
        String username = StringArgumentType.getString(ctx, "username");
        if (username.equalsIgnoreCase("reset") || username.equalsIgnoreCase("default")) {
            return skinReset(ctx);
        }
        BossConfig.SKIN_USERNAME.set(username);
        List<PvpBossEntity> bosses = bossesNear(ctx);
        for (PvpBossEntity boss : bosses) {
            boss.setSkinUsername(username);
        }
        if (bosses.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.skin_saved", username), true);
        } else {
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.skin", username), true);
        }
        return Math.max(1, bosses.size());
    }

    private static int skinReset(CommandContext<CommandSourceStack> ctx) {
        String def = "Steve";
        BossConfig.SKIN_USERNAME.set(def);
        List<PvpBossEntity> bosses = bossesNear(ctx);
        for (PvpBossEntity boss : bosses) {
            boss.setSkinUsername(def);
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.skin_reset"), true);
        return Math.max(1, bosses.size());
    }

    private static int items(CommandContext<CommandSourceStack> ctx) {
        List<PvpBossEntity> bosses = bossesNear(ctx);
        if (!bosses.isEmpty()) {
            PvpBossEntity boss = bosses.get(0);
            BossCombat combat = boss.getCombat();
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.items_header", boss.getDisplayName()), false);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.items_totem", boss.getTotemsLeft()), false);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.items_gapple", combat.getGappleCount()), false);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.items_heal", combat.getHealPotionCount()), false);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.items_pearl", combat.getPearlCount()), false);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.items_crystal", combat.getCrystalCount()), false);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.items_anchor", combat.getAnchorCount()), false);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.items_web", combat.getWebCount()), false);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.items_poison", combat.getPoisonCount()), false);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.items_honey", combat.getHoneyCount()), false);
            ctx.getSource().sendSuccess(() -> Component.literal("§7=================================="), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.items_header_default"), false);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.items_totem", BossConfig.TOTEM_COUNT.get()), false);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.items_gapple", BossConfig.GAPPLE_COUNT.get()), false);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.items_heal", BossConfig.HEAL_POTION_COUNT.get()), false);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.items_pearl", BossConfig.PEARL_COUNT.get()), false);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.items_crystal", BossConfig.CRYSTAL_COUNT.get()), false);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.items_anchor", BossConfig.ANCHOR_COUNT.get()), false);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.items_web", BossConfig.WEB_COUNT.get()), false);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.items_poison", BossConfig.POISON_COUNT.get()), false);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.items_honey", BossConfig.HONEY_COUNT.get()), false);
            ctx.getSource().sendSuccess(() -> Component.literal("§7=================================="), false);
        }
        return 1;
    }

    private static int remove(CommandContext<CommandSourceStack> ctx) {
        List<PvpBossEntity> bosses = bossesNear(ctx);
        for (PvpBossEntity boss : bosses) {
            boss.discard();
        }
        int n = bosses.size();
        ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.removed", n), true);
        return n;
    }

    private static int killsSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.translatable("command.ultimatepvpboss.none"));
            return 0;
        }
        return sendKills(ctx, player);
    }

    private static int killsOther(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return sendKills(ctx, EntityArgument.getPlayer(ctx, "player"));
    }

    private static int sendKills(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        int kills = ModEvents.getKills(player);
        int need = BossConfig.VILLAGER_KILLS_TO_SPAWN.get();
        ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.kills",
                player.getDisplayName(), kills, need), false);
        return 1;
    }

    private static List<PvpBossEntity> bossesNear(CommandContext<CommandSourceStack> ctx) {
        Entity src = ctx.getSource().getEntity();
        ServerLevel level = ctx.getSource().getLevel();
        if (src != null) {
            AABB box = src.getBoundingBox().inflate(96);
            List<PvpBossEntity> near = level.getEntitiesOfClass(PvpBossEntity.class, box);
            if (!near.isEmpty()) {
                return near;
            }
        }
        List<PvpBossEntity> all = new java.util.ArrayList<>();
        level.getAllEntities().forEach(e -> {
            if (e instanceof PvpBossEntity boss) {
                all.add(boss);
            }
        });
        return all;
    }
}
