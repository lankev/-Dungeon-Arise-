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
            source.sendFailure(Component.literal("Impossible de placer un portail boss ici. Cherche une zone plate et degagee."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Portail boss cree."), true);
        return 1;
    }

    private static int spawnGate(CommandSourceStack source, String rankName) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        GateRank rank;
        try {
            rank = GateRank.valueOf(rankName.toUpperCase());
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Rang inconnu. Utilise E, C, B, A ou S."));
            return 0;
        }
        if (!GateManager.spawnManualGate(player, rank)) {
            source.sendFailure(Component.literal("Impossible de placer un portail ici. Cherche une zone plate et degagee."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Portail rang " + rank.name() + " cree."), true);
        return 1;
    }

    private static int leaveGate(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (GateManager.leaveDungeon(player)) {
            source.sendSuccess(() -> Component.literal("Retour dans l'Overworld."), false);
            return 1;
        }
        source.sendFailure(Component.literal("Tu n'es pas dans un donjon Solo Gates."));
        return 0;
    }

    private static int showStatus(CommandSourceStack source) {
        GateSavedData data = GateSavedData.get(source.getServer());
        if (data.gates().isEmpty()) {
            source.sendSuccess(() -> Component.literal("Aucun portail actif.").withStyle(ChatFormatting.GRAY), false);
            return 1;
        }
        source.sendSuccess(() -> Component.literal("=== Portails actifs ===").withStyle(ChatFormatting.GOLD), false);
        for (GateRecord gate : data.gates()) {
            String info = String.format("[%s] Pos: %d,%d,%d | Mobs: %d | %s%s",
                gate.rank.name(),
                gate.overworldPos.getX(), gate.overworldPos.getY(), gate.overworldPos.getZ(),
                gate.mobs.size(),
                gate.completed ? "TERMINE" : gate.failed ? "ECHOUE" : "EN COURS",
                gate.bossGate ? " [BOSS]" : "");
            source.sendSuccess(() -> Component.literal(info).withStyle(gate.rank.color()), false);
        }
        return 1;
    }

    private static int showStats(CommandSourceStack source, String targetName) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target;
        if (targetName != null) {
            target = source.getServer().getPlayerList().getPlayerByName(targetName);
            if (target == null) {
                source.sendFailure(Component.literal("Joueur '" + targetName + "' introuvable."));
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
