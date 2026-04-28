package fr.matis.sologates.registry;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public enum ModArmorMaterials implements ArmorMaterial {
    RANK_E("rank_e", 18,
        Map.of(ArmorItem.Type.HELMET, 2, ArmorItem.Type.CHESTPLATE, 5,
               ArmorItem.Type.LEGGINGS, 4, ArmorItem.Type.BOOTS, 2),
        12, SoundEvents.ARMOR_EQUIP_CHAIN, 0.5f, 0.0f,
        () -> Ingredient.of(Items.IRON_INGOT)),

    RANK_C("rank_c", 24,
        Map.of(ArmorItem.Type.HELMET, 3, ArmorItem.Type.CHESTPLATE, 7,
               ArmorItem.Type.LEGGINGS, 5, ArmorItem.Type.BOOTS, 3),
        14, SoundEvents.ARMOR_EQUIP_IRON, 0.5f, 0.0f,
        () -> Ingredient.of(Items.GOLD_INGOT)),

    RANK_B("rank_b", 30,
        Map.of(ArmorItem.Type.HELMET, 3, ArmorItem.Type.CHESTPLATE, 8,
               ArmorItem.Type.LEGGINGS, 6, ArmorItem.Type.BOOTS, 3),
        16, SoundEvents.ARMOR_EQUIP_DIAMOND, 1.0f, 0.0f,
        () -> Ingredient.of(Items.DIAMOND)),

    RANK_A("rank_a", 36,
        Map.of(ArmorItem.Type.HELMET, 4, ArmorItem.Type.CHESTPLATE, 9,
               ArmorItem.Type.LEGGINGS, 7, ArmorItem.Type.BOOTS, 4),
        18, SoundEvents.ARMOR_EQUIP_NETHERITE, 2.0f, 0.1f,
        () -> Ingredient.of(Items.NETHERITE_INGOT)),

    RANK_S("rank_s", 44,
        Map.of(ArmorItem.Type.HELMET, 4, ArmorItem.Type.CHESTPLATE, 10,
               ArmorItem.Type.LEGGINGS, 8, ArmorItem.Type.BOOTS, 4),
        22, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0f, 0.2f,
        () -> Ingredient.of(Items.NETHERITE_INGOT));

    private static final Map<ArmorItem.Type, Integer> DURABILITY_BASE = new EnumMap<>(ArmorItem.Type.class);
    static {
        DURABILITY_BASE.put(ArmorItem.Type.HELMET, 11);
        DURABILITY_BASE.put(ArmorItem.Type.CHESTPLATE, 16);
        DURABILITY_BASE.put(ArmorItem.Type.LEGGINGS, 15);
        DURABILITY_BASE.put(ArmorItem.Type.BOOTS, 13);
    }

    private final String name;
    private final int durabilityMultiplier;
    private final Map<ArmorItem.Type, Integer> protections;
    private final int enchantmentValue;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final LazyLoadedValue<Ingredient> repairIngredient;

    ModArmorMaterials(String name, int durabilityMultiplier, Map<ArmorItem.Type, Integer> protections,
                      int enchantmentValue, SoundEvent equipSound, float toughness,
                      float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protections = protections;
        this.enchantmentValue = enchantmentValue;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = new LazyLoadedValue<>(repairIngredient);
    }

    @Override public int getDurabilityForType(ArmorItem.Type type) { return DURABILITY_BASE.get(type) * durabilityMultiplier; }
    @Override public int getDefenseForType(ArmorItem.Type type) { return protections.get(type); }
    @Override public int getEnchantmentValue() { return enchantmentValue; }
    @Override public SoundEvent getEquipSound() { return equipSound; }
    @Override public Ingredient getRepairIngredient() { return repairIngredient.get(); }
    @Override public String getName() { return "sologates:" + name; }
    @Override public float getToughness() { return toughness; }
    @Override public float getKnockbackResistance() { return knockbackResistance; }
}
