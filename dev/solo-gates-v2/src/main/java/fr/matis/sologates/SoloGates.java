package fr.matis.sologates;

import com.mojang.logging.LogUtils;
import fr.matis.sologates.client.GateBlockEntityRenderer;
import fr.matis.sologates.client.GateEntityRenderer;
import fr.matis.sologates.registry.ModBlockEntities;
import fr.matis.sologates.registry.ModBlocks;
import fr.matis.sologates.registry.ModEntities;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(SoloGates.MOD_ID)
public class SoloGates {
    public static final String MOD_ID = "sologates";
    static final Logger LOGGER = LogUtils.getLogger();

    public SoloGates() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modBus);
        ModBlocks.ITEMS.register(modBus);
        ModBlocks.CREATIVE_TABS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SoloGatesConfig.SPEC);
        SoloGatesCriteria.register();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(SoloGates::onClientSetup);
        }
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityRenderers.register(ModBlockEntities.GATE.get(), GateBlockEntityRenderer::new);
            EntityRenderers.register(ModEntities.GATE.get(), GateEntityRenderer::new);
        });
    }
}
