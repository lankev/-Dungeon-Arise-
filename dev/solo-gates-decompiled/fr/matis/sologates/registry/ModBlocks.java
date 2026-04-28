/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.ArmorItem
 *  net.minecraft.world.item.ArmorItem$Type
 *  net.minecraft.world.item.ArmorMaterial
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.CreativeModeTabs
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraftforge.event.BuildCreativeModeTabContentsEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package fr.matis.sologates.registry;

import fr.matis.sologates.GateRank;
import fr.matis.sologates.block.GateBlock;
import fr.matis.sologates.registry.ModArmorMaterials;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.BLOCKS, (String)"sologates");
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.ITEMS, (String)"sologates");
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create((ResourceKey)Registries.f_279569_, (String)"sologates");
    public static final RegistryObject<Block> GATE_E = ModBlocks.gate("gate_e", GateRank.E);
    public static final RegistryObject<Block> GATE_C = ModBlocks.gate("gate_c", GateRank.C);
    public static final RegistryObject<Block> GATE_B = ModBlocks.gate("gate_b", GateRank.B);
    public static final RegistryObject<Block> GATE_A = ModBlocks.gate("gate_a", GateRank.A);
    public static final RegistryObject<Block> GATE_S = ModBlocks.gate("gate_s", GateRank.S);
    public static final RegistryObject<Item> BRONZE_COIN = ITEMS.register("bronze_coin", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SILVER_COIN = ITEMS.register("silver_coin", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLD_COIN = ITEMS.register("gold_coin", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PLATINUM_COIN = ITEMS.register("platinum_coin", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RANK_E_HELMET = ITEMS.register("rank_e_helmet", () -> new ArmorItem((ArmorMaterial)ModArmorMaterials.RANK_E, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> RANK_E_CHESTPLATE = ITEMS.register("rank_e_chestplate", () -> new ArmorItem((ArmorMaterial)ModArmorMaterials.RANK_E, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> RANK_E_LEGGINGS = ITEMS.register("rank_e_leggings", () -> new ArmorItem((ArmorMaterial)ModArmorMaterials.RANK_E, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> RANK_E_BOOTS = ITEMS.register("rank_e_boots", () -> new ArmorItem((ArmorMaterial)ModArmorMaterials.RANK_E, ArmorItem.Type.BOOTS, new Item.Properties()));
    public static final RegistryObject<CreativeModeTab> SOLO_GATES_TAB = CREATIVE_TABS.register("solo_gates", () -> CreativeModeTab.builder().m_257941_((Component)Component.m_237115_((String)"itemGroup.sologates.solo_gates")).m_257737_(() -> new ItemStack((ItemLike)GOLD_COIN.get())).m_257501_((parameters, output) -> {
        output.m_246326_((ItemLike)GATE_E.get());
        output.m_246326_((ItemLike)GATE_C.get());
        output.m_246326_((ItemLike)GATE_B.get());
        output.m_246326_((ItemLike)GATE_A.get());
        output.m_246326_((ItemLike)GATE_S.get());
        output.m_246326_((ItemLike)BRONZE_COIN.get());
        output.m_246326_((ItemLike)SILVER_COIN.get());
        output.m_246326_((ItemLike)GOLD_COIN.get());
        output.m_246326_((ItemLike)PLATINUM_COIN.get());
        output.m_246326_((ItemLike)RANK_E_HELMET.get());
        output.m_246326_((ItemLike)RANK_E_CHESTPLATE.get());
        output.m_246326_((ItemLike)RANK_E_LEGGINGS.get());
        output.m_246326_((ItemLike)RANK_E_BOOTS.get());
    }).m_257652_());

    private static RegistryObject<Block> gate(String name, GateRank rank) {
        RegistryObject block = BLOCKS.register(name, () -> new GateBlock(rank, BlockBehaviour.Properties.m_284310_().m_284180_(MapColor.f_283889_).m_60913_(-1.0f, 3600000.0f).m_60955_().m_60953_(state -> 13).m_60918_(SoundType.f_154654_)));
        ITEMS.register(name, () -> new BlockItem((Block)block.get(), new Item.Properties()));
        return block;
    }

    public static Block gateBlock(GateRank rank) {
        return switch (rank) {
            default -> throw new IncompatibleClassChangeError();
            case GateRank.E -> (Block)GATE_E.get();
            case GateRank.C -> (Block)GATE_C.get();
            case GateRank.B -> (Block)GATE_B.get();
            case GateRank.A -> (Block)GATE_A.get();
            case GateRank.S -> (Block)GATE_S.get();
        };
    }

    private ModBlocks() {
    }

    @Mod.EventBusSubscriber(modid="sologates", bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class CreativeTabEvents {
        @SubscribeEvent
        public static void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == CreativeModeTabs.f_256791_) {
                event.accept(GATE_E);
                event.accept(GATE_C);
                event.accept(GATE_B);
                event.accept(GATE_A);
                event.accept(GATE_S);
                event.accept(BRONZE_COIN);
                event.accept(SILVER_COIN);
                event.accept(GOLD_COIN);
                event.accept(PLATINUM_COIN);
                event.accept(RANK_E_HELMET);
                event.accept(RANK_E_CHESTPLATE);
                event.accept(RANK_E_LEGGINGS);
                event.accept(RANK_E_BOOTS);
            }
        }
    }
}

