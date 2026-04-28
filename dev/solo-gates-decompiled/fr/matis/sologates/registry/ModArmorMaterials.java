/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.util.LazyLoadedValue
 *  net.minecraft.world.item.ArmorItem$Type
 *  net.minecraft.world.item.ArmorMaterial
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.level.ItemLike
 */
package fr.matis.sologates.registry;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public enum ModArmorMaterials implements ArmorMaterial
{
    RANK_E("rank_e", 18, Map.of(ArmorItem.Type.HELMET, 2, ArmorItem.Type.CHESTPLATE, 5, ArmorItem.Type.LEGGINGS, 4, ArmorItem.Type.BOOTS, 2), 12, SoundEvents.f_11677_, 0.5f, 0.0f, () -> Ingredient.m_43929_((ItemLike[])new ItemLike[]{Items.f_42416_}));

    private static final Map<ArmorItem.Type, Integer> DURABILITY;
    private final String name;
    private final int durabilityMultiplier;
    private final Map<ArmorItem.Type, Integer> protections;
    private final int enchantmentValue;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final LazyLoadedValue<Ingredient> repairIngredient;

    private ModArmorMaterials(String name, int durabilityMultiplier, Map<ArmorItem.Type, Integer> protections, int enchantmentValue, SoundEvent equipSound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protections = protections;
        this.enchantmentValue = enchantmentValue;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = new LazyLoadedValue(repairIngredient);
    }

    public int m_266425_(ArmorItem.Type type) {
        return DURABILITY.get(type) * this.durabilityMultiplier;
    }

    public int m_7366_(ArmorItem.Type type) {
        return this.protections.get(type);
    }

    public int m_6646_() {
        return this.enchantmentValue;
    }

    public SoundEvent m_7344_() {
        return this.equipSound;
    }

    public Ingredient m_6230_() {
        return (Ingredient)this.repairIngredient.m_13971_();
    }

    public String m_6082_() {
        return "sologates:" + this.name;
    }

    public float m_6651_() {
        return this.toughness;
    }

    public float m_6649_() {
        return this.knockbackResistance;
    }

    static {
        DURABILITY = new EnumMap<ArmorItem.Type, Integer>(ArmorItem.Type.class);
        DURABILITY.put(ArmorItem.Type.HELMET, 11);
        DURABILITY.put(ArmorItem.Type.CHESTPLATE, 16);
        DURABILITY.put(ArmorItem.Type.LEGGINGS, 15);
        DURABILITY.put(ArmorItem.Type.BOOTS, 13);
    }
}

