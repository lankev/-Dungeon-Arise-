package fr.matis.sologates;

import fr.matis.sologates.registry.ModBlocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SoloGates.MOD_ID)
public class SoloGates {
    public static final String MOD_ID = "sologates";

    public SoloGates() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modBus);
        ModBlocks.ITEMS.register(modBus);
        ModBlocks.CREATIVE_TABS.register(modBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SoloGatesConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(new SoloGatesEvents());
    }
}
