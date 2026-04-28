package fr.matis.sologates.registry;

import fr.matis.sologates.GateRank;
import fr.matis.sologates.SoloGates;
import fr.matis.sologates.block.GateBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
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
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SoloGates.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SoloGates.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SoloGates.MOD_ID);

    // Gate blocks
    public static final RegistryObject<Block> GATE_E = gate("gate_e", GateRank.E);
    public static final RegistryObject<Block> GATE_C = gate("gate_c", GateRank.C);
    public static final RegistryObject<Block> GATE_B = gate("gate_b", GateRank.B);
    public static final RegistryObject<Block> GATE_A = gate("gate_a", GateRank.A);
    public static final RegistryObject<Block> GATE_S = gate("gate_s", GateRank.S);

    // Coins
    public static final RegistryObject<Item> BRONZE_COIN  = ITEMS.register("bronze_coin",  () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SILVER_COIN  = ITEMS.register("silver_coin",  () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLD_COIN    = ITEMS.register("gold_coin",    () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PLATINUM_COIN = ITEMS.register("platinum_coin", () -> new Item(new Item.Properties()));

    // E-rank armor
    public static final RegistryObject<Item> RANK_E_HELMET     = ITEMS.register("rank_e_helmet",     () -> new ArmorItem(ModArmorMaterials.RANK_E, ArmorItem.Type.HELMET,     new Item.Properties()));
    public static final RegistryObject<Item> RANK_E_CHESTPLATE = ITEMS.register("rank_e_chestplate", () -> new ArmorItem(ModArmorMaterials.RANK_E, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> RANK_E_LEGGINGS   = ITEMS.register("rank_e_leggings",   () -> new ArmorItem(ModArmorMaterials.RANK_E, ArmorItem.Type.LEGGINGS,   new Item.Properties()));
    public static final RegistryObject<Item> RANK_E_BOOTS      = ITEMS.register("rank_e_boots",      () -> new ArmorItem(ModArmorMaterials.RANK_E, ArmorItem.Type.BOOTS,      new Item.Properties()));

    // C-rank armor
    public static final RegistryObject<Item> RANK_C_HELMET     = ITEMS.register("rank_c_helmet",     () -> new ArmorItem(ModArmorMaterials.RANK_C, ArmorItem.Type.HELMET,     new Item.Properties()));
    public static final RegistryObject<Item> RANK_C_CHESTPLATE = ITEMS.register("rank_c_chestplate", () -> new ArmorItem(ModArmorMaterials.RANK_C, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> RANK_C_LEGGINGS   = ITEMS.register("rank_c_leggings",   () -> new ArmorItem(ModArmorMaterials.RANK_C, ArmorItem.Type.LEGGINGS,   new Item.Properties()));
    public static final RegistryObject<Item> RANK_C_BOOTS      = ITEMS.register("rank_c_boots",      () -> new ArmorItem(ModArmorMaterials.RANK_C, ArmorItem.Type.BOOTS,      new Item.Properties()));

    // B-rank armor
    public static final RegistryObject<Item> RANK_B_HELMET     = ITEMS.register("rank_b_helmet",     () -> new ArmorItem(ModArmorMaterials.RANK_B, ArmorItem.Type.HELMET,     new Item.Properties()));
    public static final RegistryObject<Item> RANK_B_CHESTPLATE = ITEMS.register("rank_b_chestplate", () -> new ArmorItem(ModArmorMaterials.RANK_B, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> RANK_B_LEGGINGS   = ITEMS.register("rank_b_leggings",   () -> new ArmorItem(ModArmorMaterials.RANK_B, ArmorItem.Type.LEGGINGS,   new Item.Properties()));
    public static final RegistryObject<Item> RANK_B_BOOTS      = ITEMS.register("rank_b_boots",      () -> new ArmorItem(ModArmorMaterials.RANK_B, ArmorItem.Type.BOOTS,      new Item.Properties()));

    // A-rank armor
    public static final RegistryObject<Item> RANK_A_HELMET     = ITEMS.register("rank_a_helmet",     () -> new ArmorItem(ModArmorMaterials.RANK_A, ArmorItem.Type.HELMET,     new Item.Properties()));
    public static final RegistryObject<Item> RANK_A_CHESTPLATE = ITEMS.register("rank_a_chestplate", () -> new ArmorItem(ModArmorMaterials.RANK_A, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> RANK_A_LEGGINGS   = ITEMS.register("rank_a_leggings",   () -> new ArmorItem(ModArmorMaterials.RANK_A, ArmorItem.Type.LEGGINGS,   new Item.Properties()));
    public static final RegistryObject<Item> RANK_A_BOOTS      = ITEMS.register("rank_a_boots",      () -> new ArmorItem(ModArmorMaterials.RANK_A, ArmorItem.Type.BOOTS,      new Item.Properties()));

    // S-rank armor
    public static final RegistryObject<Item> RANK_S_HELMET     = ITEMS.register("rank_s_helmet",     () -> new ArmorItem(ModArmorMaterials.RANK_S, ArmorItem.Type.HELMET,     new Item.Properties()));
    public static final RegistryObject<Item> RANK_S_CHESTPLATE = ITEMS.register("rank_s_chestplate", () -> new ArmorItem(ModArmorMaterials.RANK_S, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> RANK_S_LEGGINGS   = ITEMS.register("rank_s_leggings",   () -> new ArmorItem(ModArmorMaterials.RANK_S, ArmorItem.Type.LEGGINGS,   new Item.Properties()));
    public static final RegistryObject<Item> RANK_S_BOOTS      = ITEMS.register("rank_s_boots",      () -> new ArmorItem(ModArmorMaterials.RANK_S, ArmorItem.Type.BOOTS,      new Item.Properties()));

    // Creative tab
    public static final RegistryObject<CreativeModeTab> SOLO_GATES_TAB = CREATIVE_TABS.register("solo_gates", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.sologates.solo_gates"))
            .icon(() -> new ItemStack(GOLD_COIN.get()))
            .displayItems((params, output) -> {
                output.accept(GATE_E.get()); output.accept(GATE_C.get());
                output.accept(GATE_B.get()); output.accept(GATE_A.get()); output.accept(GATE_S.get());
                output.accept(BRONZE_COIN.get()); output.accept(SILVER_COIN.get());
                output.accept(GOLD_COIN.get()); output.accept(PLATINUM_COIN.get());
                output.accept(RANK_E_HELMET.get()); output.accept(RANK_E_CHESTPLATE.get());
                output.accept(RANK_E_LEGGINGS.get()); output.accept(RANK_E_BOOTS.get());
                output.accept(RANK_C_HELMET.get()); output.accept(RANK_C_CHESTPLATE.get());
                output.accept(RANK_C_LEGGINGS.get()); output.accept(RANK_C_BOOTS.get());
                output.accept(RANK_B_HELMET.get()); output.accept(RANK_B_CHESTPLATE.get());
                output.accept(RANK_B_LEGGINGS.get()); output.accept(RANK_B_BOOTS.get());
                output.accept(RANK_A_HELMET.get()); output.accept(RANK_A_CHESTPLATE.get());
                output.accept(RANK_A_LEGGINGS.get()); output.accept(RANK_A_BOOTS.get());
                output.accept(RANK_S_HELMET.get()); output.accept(RANK_S_CHESTPLATE.get());
                output.accept(RANK_S_LEGGINGS.get()); output.accept(RANK_S_BOOTS.get());
            }).build());

    private static RegistryObject<Block> gate(String name, GateRank rank) {
        RegistryObject<Block> block = BLOCKS.register(name, () ->
            new GateBlock(rank, BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .strength(-1f, 3_600_000f)
                .noOcclusion()
                .lightLevel(s -> 13)
                .sound(SoundType.GLASS)));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    public static Block gateBlock(GateRank rank) {
        return switch (rank) {
            case E -> GATE_E.get();
            case C -> GATE_C.get();
            case B -> GATE_B.get();
            case A -> GATE_A.get();
            case S -> GATE_S.get();
        };
    }

    private ModBlocks() {}

    @Mod.EventBusSubscriber(modid = SoloGates.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class CreativeTabEvents {
        @SubscribeEvent
        public static void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == CreativeModeTabs.COMBAT) {
                event.accept(GATE_E); event.accept(GATE_C);
                event.accept(GATE_B); event.accept(GATE_A); event.accept(GATE_S);
            }
        }
    }
}
