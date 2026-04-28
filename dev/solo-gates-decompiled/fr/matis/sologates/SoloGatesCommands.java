/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 */
package fr.matis.sologates;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.matis.sologates.GateManager;
import fr.matis.sologates.GateRank;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class SoloGatesCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"sologates").then(Commands.m_82127_((String)"leave").executes(context -> SoloGatesCommands.leaveGate((CommandSourceStack)context.getSource())))).then(((LiteralArgumentBuilder)Commands.m_82127_((String)"spawn").requires(source -> source.m_6761_(2))).then(Commands.m_82129_((String)"rank", (ArgumentType)StringArgumentType.word()).suggests((context, builder) -> {
            for (GateRank rank : GateRank.values()) {
                builder.suggest(rank.name());
            }
            return builder.buildFuture();
        }).executes(context -> SoloGatesCommands.spawnGate((CommandSourceStack)context.getSource(), StringArgumentType.getString((CommandContext)context, (String)"rank")))))).then(((LiteralArgumentBuilder)Commands.m_82127_((String)"spawnboss").requires(source -> source.m_6761_(2))).executes(context -> SoloGatesCommands.spawnBossGate((CommandSourceStack)context.getSource()))));
    }

    private static int spawnBossGate(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.m_81375_();
        if (!GateManager.spawnManualBossGate(player)) {
            source.m_81352_((Component)Component.m_237113_((String)"Impossible de placer un portail boss ici. Cherche une zone plate et degagee."));
            return 0;
        }
        source.m_288197_(() -> Component.m_237113_((String)"Portail boss cree."), true);
        return 1;
    }

    private static int spawnGate(CommandSourceStack source, String rankName) throws CommandSyntaxException {
        GateRank rank;
        ServerPlayer player = source.m_81375_();
        try {
            rank = GateRank.valueOf(rankName.toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            source.m_81352_((Component)Component.m_237113_((String)"Rang inconnu. Utilise E, C, B ou S."));
            return 0;
        }
        boolean spawned = GateManager.spawnManualGate(player, rank);
        if (!spawned) {
            source.m_81352_((Component)Component.m_237113_((String)"Impossible de placer un portail ici. Cherche une zone plate et degagee."));
            return 0;
        }
        source.m_288197_(() -> Component.m_237113_((String)("Portail rang " + rank.name() + " cree.")), true);
        return 1;
    }

    private static int leaveGate(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.m_81375_();
        if (GateManager.leaveDungeon(player)) {
            source.m_288197_(() -> Component.m_237113_((String)"Retour dans l'Overworld."), false);
            return 1;
        }
        source.m_81352_((Component)Component.m_237113_((String)"Tu n'es pas dans un donjon Solo Gates."));
        return 0;
    }

    private SoloGatesCommands() {
    }
}

