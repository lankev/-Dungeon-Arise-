/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 */
package fr.matis.sologates.block;

import fr.matis.sologates.GateManager;
import fr.matis.sologates.GateRank;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class GateBlock
extends Block {
    private final GateRank rank;

    public GateBlock(GateRank rank, BlockBehaviour.Properties properties) {
        super(properties);
        this.rank = rank;
    }

    public GateRank rank() {
        return this.rank;
    }

    public RenderShape m_7514_(BlockState state) {
        return RenderShape.MODEL;
    }

    public InteractionResult m_6227_(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.f_46443_ && player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            GateManager.useGate(serverPlayer, pos);
        }
        return InteractionResult.m_19078_((boolean)level.f_46443_);
    }
}

