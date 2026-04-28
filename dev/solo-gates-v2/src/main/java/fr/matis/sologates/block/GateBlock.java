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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class GateBlock extends Block {
    private final GateRank rank;

    public GateBlock(GateRank rank, BlockBehaviour.Properties properties) {
        super(properties);
        this.rank = rank;
    }

    public GateRank getRank() { return rank; }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            GateManager.useGate(serverPlayer, pos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
