package fr.matis.sologates.registry;

import fr.matis.sologates.SoloGates;
import fr.matis.sologates.block.GateBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SoloGates.MOD_ID);

    public static final RegistryObject<BlockEntityType<GateBlockEntity>> GATE =
        BLOCK_ENTITIES.register("gate", () -> BlockEntityType.Builder
            .of(GateBlockEntity::new,
                ModBlocks.GATE_E.get(), ModBlocks.GATE_C.get(),
                ModBlocks.GATE_B.get(), ModBlocks.GATE_A.get(), ModBlocks.GATE_S.get())
            .build(null));

    private ModBlockEntities() {}
}
