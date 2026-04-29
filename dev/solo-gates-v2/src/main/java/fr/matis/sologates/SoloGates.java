package fr.matis.sologates;

import com.mojang.logging.LogUtils;
import fr.matis.sologates.registry.ModBlocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SoloGatesConfig.SPEC);
        SoloGatesCriteria.register();
    }
}
