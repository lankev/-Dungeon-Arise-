package fr.matis.sologates;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class SoloGatesCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("sologates")
                .then(Commands.literal("leave")
                    .executes(ctx -> leaveGate(ctx.getSource())))
                .then(Commands.literal("spawn")
                    .requires(src -> src.hasPermission(2))
                    .then(Commands.argument("rank", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (GateRank rank : GateRank.values()) builder.suggest(rank.name());
                            return builder.buildFuture();
                        })
                        .executes(ctx -> spawnGate(ctx.getSource(), StringArgumentType.getString(ctx, "rank")))))
                .then(Commands.literal("spawnboss")
                    .requires(src -> src.hasPermission(2))
                    .executes(ctx -> spawnBossGate(ctx.getSource())))
                .then(Commands.literal("status")
                    .requires(src -> src.hasPermission(2))
                    .executes(ctx -> showStatus(ctx.getSource())))
                .then(Commands.literal("stats")
                    .executes(ctx -> showStats(ctx.getSource(), null))
                    .then(Commands.argument("player", StringArgumentType.word())
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> showStats(ctx.getSource(), StringArgumentType.getString(ctx, "player")))))
        );
    }

    private static int spawnBossGate(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!GateManager.spawnManualBossGate(player)) {
            source.sendFailure(Component.translatable("sologates.command.boss_portal_no_place"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("sologates.command.boss_portal_created"), true);
        return 1;
    }

    private static int spawnGate(CommandSourceStack source, String rankName) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        GateRank rank;
        try {
            rank = GateRank.valueOf(rankName.toUpperCase());
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.translatable("sologates.command.rank_unknown"));
            return 0;
        }
        if (!GateManager.spawnManualGate(player, rank)) {
            source.sendFailure(Component.translatable("sologates.command.portal_no_place"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("sologates.command.portal_created", rank.name()), true);
        return 1;
    }

    private static int leaveGate(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (GateManager.leaveDungeon(player)) {
            source.sendSuccess(() -> Component.translatable("sologates.command.leave_success"), false);
            return 1;
        }
        source.sendFailure(Component.translatable("sologates.command.not_in_dungeon"));
        return 0;
    }

    private static int showStatus(CommandSourceStack source) {
        GateSavedData data = GateSavedData.get(source.getServer());
        if (data.gates().isEmpty()) {
            source.sendSuccess(() -> Component.translatable("sologates.command.no_active_gates")
                .withStyle(ChatFormatting.GRAY), false);
            return 1;
        }
        source.sendSuccess(() -> Component.translatable("sologates.command.active_gates_header")
            .withStyle(ChatFormatting.GOLD), false);
        for (GateRecord gate : data.gates()) {
            Component statusComp = gate.completed
                ? Component.translatable("sologates.command.status.done").withStyle(ChatFormatting.GREEN)
                : gate.failed
                    ? Component.translatable("sologates.command.status.failed").withStyle(ChatFormatting.RED)
                    : Component.translatable("sologates.command.status.active").withStyle(ChatFormatting.YELLOW);
            Component bossComp = gate.bossGate
                ? Component.literal(" [BOSS]").withStyle(ChatFormatting.GOLD)
                : Component.empty();
            Component entry = Component.translatable("sologates.command.gate_entry",
                Component.literal(gate.rank.name()).withStyle(gate.rank.color()),
                Component.literal(String.valueOf(gate.overworldPos.getX())),
                Component.literal(String.valueOf(gate.overworldPos.getY())),
                Component.literal(String.valueOf(gate.overworldPos.getZ())),
                Component.literal(String.valueOf(gate.mobs.size())),
                statusComp, bossComp);
            source.sendSuccess(() -> entry, false);
        }
        return 1;
    }

    private static int showStats(CommandSourceStack source, String targetName) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target;
        if (targetName != null) {
            target = source.getServer().getPlayerList().getPlayerByName(targetName);
            if (target == null) {
                source.sendFailure(Component.translatable("sologates.command.player_not_found", targetName));
                return 0;
            }
        } else {
            target = source.getPlayerOrException();
        }
        PlayerSavedData psd = PlayerSavedData.get(source.getServer());
        PlayerData pd = psd.getOrCreate(target.getUUID());
        source.sendSuccess(() -> pd.statsComponent(), false);
        return 1;
    }

    private SoloGatesCommands() {}
}
