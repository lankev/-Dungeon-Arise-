package fr.matis.sologates;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import fr.matis.sologates.network.SoloGatesNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
                .then(Commands.literal("setrank")
                    .requires(src -> src.hasPermission(2))
                    .then(Commands.argument("player", StringArgumentType.word())
                        .then(Commands.argument("rank", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                for (GateRank r : GateRank.values()) builder.suggest(r.name());
                                return builder.buildFuture();
                            })
                            .executes(ctx -> setRank(ctx.getSource(),
                                StringArgumentType.getString(ctx, "player"),
                                StringArgumentType.getString(ctx, "rank"))))))
                .then(Commands.literal("criminals")
                    .executes(ctx -> showCriminals(ctx.getSource())))
                .then(Commands.literal("crimestat")
                    .executes(ctx -> showCrimeStat(ctx.getSource(), null))
                    .then(Commands.literal("get")
                        .executes(ctx -> showCrimeStat(ctx.getSource(), null))
                        .then(Commands.argument("player", StringArgumentType.word())
                            .requires(src -> src.hasPermission(2))
                            .executes(ctx -> showCrimeStat(ctx.getSource(), StringArgumentType.getString(ctx, "player")))))
                    .then(Commands.literal("set")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("player", StringArgumentType.word())
                            .then(Commands.argument("level", IntegerArgumentType.integer(0, 3))
                                .executes(ctx -> setCrimeStat(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "player"),
                                    IntegerArgumentType.getInteger(ctx, "level"))))))
                    .then(Commands.literal("clear")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("player", StringArgumentType.word())
                            .executes(ctx -> setCrimeStat(ctx.getSource(),
                                StringArgumentType.getString(ctx, "player"), 0)))))
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

    private static int setRank(CommandSourceStack source, String playerName, String rankName) {
        ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) {
            source.sendFailure(Component.translatable("sologates.command.player_not_found", playerName));
            return 0;
        }
        GateRank rank;
        try {
            rank = GateRank.valueOf(rankName.toUpperCase());
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.translatable("sologates.command.rank_unknown"));
            return 0;
        }
        PlayerSavedData psd = PlayerSavedData.get(source.getServer());
        PlayerData pd = psd.getOrCreate(target.getUUID());
        pd.setConfirmedRank(rank);
        psd.setDirty();
        SoloGatesNetwork.sendRankToPlayer(target, rank, pd.currentStreak());

        // Grant GameStage for modpack progression gating
        String stage = "rank_" + rank.name().toLowerCase();
        try {
            source.getServer().getCommands().performPrefixedCommand(
                source.getServer().createCommandSourceStack().withPermission(4),
                "gamestage add " + target.getName().getString() + " " + stage
            );
        } catch (Exception ignored) {}

        source.sendSuccess(() -> Component.translatable("sologates.command.setrank_success",
            target.getName(), Component.literal(rank.name()).withStyle(rank.color())), true);
        target.displayClientMessage(Component.translatable("sologates.message.rank_promoted",
            Component.literal(rank.name()).withStyle(rank.color())).withStyle(ChatFormatting.GOLD), false);
        Component broadcast = rank == GateRank.S
            ? Component.translatable("sologates.broadcast.rank_s", target.getName()).withStyle(ChatFormatting.GOLD)
            : Component.translatable("sologates.message.rank_broadcast",
                target.getName(), Component.literal(rank.name()).withStyle(rank.color()));
        source.getServer().getPlayerList().broadcastSystemMessage(broadcast, false);
        return 1;
    }

    private static int showCrimeStat(CommandSourceStack source, String targetName) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = findTarget(source, targetName);
        if (target == null) return 0;
        int level = CrimeStatManager.crimeLevel(target);
        source.sendSuccess(() -> Component.translatable("sologates.command.crimestat_get",
            target.getName(), Component.literal(String.valueOf(level)).withStyle(crimeColor(level))), false);
        return 1;
    }

    private static int setCrimeStat(CommandSourceStack source, String playerName, int level) {
        ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) {
            source.sendFailure(Component.translatable("sologates.command.player_not_found", playerName));
            return 0;
        }
        CrimeSavedData data = CrimeSavedData.get(source.getServer());
        CrimeStatData crime = data.getOrCreate(target.getUUID());
        crime.setLevel(level);
        data.setDirty();
        CrimeStatManager.syncCrimeStat(target);
        source.sendSuccess(() -> Component.translatable("sologates.command.crimestat_set",
            target.getName(), Component.literal(String.valueOf(level)).withStyle(crimeColor(level))), true);
        return 1;
    }

    private static int showCriminals(CommandSourceStack source) {
        CrimeSavedData data = CrimeSavedData.get(source.getServer());
        List<Map.Entry<UUID, CrimeStatData>> criminals = data.allEntries().entrySet().stream()
            .filter(e -> e.getValue().level() > 0)
            .sorted(Comparator.comparingInt((Map.Entry<UUID, CrimeStatData> e) -> e.getValue().level()).reversed())
            .toList();

        if (criminals.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("sologates.command.no_criminals")
                .withStyle(ChatFormatting.GREEN), false);
            return 1;
        }

        source.sendSuccess(() -> Component.translatable("sologates.command.criminals_header")
            .withStyle(ChatFormatting.RED), false);
        for (Map.Entry<UUID, CrimeStatData> entry : criminals) {
            String name = resolvePlayerName(source.getServer(), entry.getKey());
            int level = entry.getValue().level();
            Component line = Component.translatable("sologates.command.criminals_entry",
                Component.literal(name).withStyle(ChatFormatting.WHITE),
                Component.literal("Crime Stat " + level).withStyle(crimeColor(level)));
            source.sendSuccess(() -> line, false);
        }
        return 1;
    }

    private static String resolvePlayerName(MinecraftServer server, UUID uuid) {
        ServerPlayer online = server.getPlayerList().getPlayer(uuid);
        if (online != null) return online.getName().getString();
        return server.getProfileCache()
            .get(uuid)
            .map(com.mojang.authlib.GameProfile::getName)
            .orElse(uuid.toString().substring(0, 8));
    }

    private static ServerPlayer findTarget(CommandSourceStack source, String targetName) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (targetName == null) return source.getPlayerOrException();
        ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            source.sendFailure(Component.translatable("sologates.command.player_not_found", targetName));
            return null;
        }
        return target;
    }

    private static ChatFormatting crimeColor(int level) {
        return switch (level) {
            case 1 -> ChatFormatting.GOLD;
            case 2 -> ChatFormatting.RED;
            case 3 -> ChatFormatting.DARK_RED;
            default -> ChatFormatting.GRAY;
        };
    }

    private SoloGatesCommands() {}
}
