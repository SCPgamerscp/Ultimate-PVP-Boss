package com.ailingmeng.ultimatepvpboss.command;

import com.ailingmeng.ultimatepvpboss.config.BossConfig;
import com.ailingmeng.ultimatepvpboss.entity.PvpBossEntity;
import com.ailingmeng.ultimatepvpboss.event.ModEvents;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class PvpBossCommands {
    private PvpBossCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pvpboss")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("spawn")
                        .executes(PvpBossCommands::spawnSelf)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(PvpBossCommands::spawnAt)))
                .then(Commands.literal("name")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(PvpBossCommands::rename)))
                .then(Commands.literal("skin")
                        .then(Commands.literal("reset")
                                .executes(PvpBossCommands::skinReset))
                        .then(Commands.argument("username", StringArgumentType.greedyString())
                                .executes(PvpBossCommands::skin)))
                .then(Commands.literal("remove")
                        .executes(PvpBossCommands::remove))
                .then(Commands.literal("kills")
                        .executes(PvpBossCommands::killsSelf)
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
        String name = StringArgumentType.getString(ctx, "name");
        BossConfig.BOSS_NAME.set(name);
        List<PvpBossEntity> bosses = bossesNear(ctx);
        for (PvpBossEntity boss : bosses) {
            boss.setCustomName(Component.literal(name));
            boss.setCustomNameVisible(true);
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("command.ultimatepvpboss.named", name), true);
        return Math.max(1, bosses.size());
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
