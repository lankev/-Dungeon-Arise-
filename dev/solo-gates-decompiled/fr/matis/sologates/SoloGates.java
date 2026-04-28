/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.fml.ModLoadingContext
 *  net.minecraftforge.fml.common.Mod
 *  net.minecraftforge.fml.config.IConfigSpec
 *  net.minecraftforge.fml.config.ModConfig$Type
 *  net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
 */
package fr.matis.sologates;

import fr.matis.sologates.SoloGatesConfig;
import fr.matis.sologates.SoloGatesEvents;
import fr.matis.sologates.registry.ModBlocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(value="sologates")
public class SoloGates {
    public static final String MOD_ID = "sologates";

    public SoloGates() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modBus);
        ModBlocks.ITEMS.register(modBus);
        ModBlocks.CREATIVE_TABS.register(modBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, (IConfigSpec)SoloGatesConfig.SPEC);
        MinecraftForge.EVENT_BUS.register((Object)new SoloGatesEvents());
    }
}

