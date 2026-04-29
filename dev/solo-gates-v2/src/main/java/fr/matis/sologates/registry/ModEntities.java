package fr.matis.sologates.registry;

import fr.matis.sologates.SoloGates;
import fr.matis.sologates.entity.GateEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SoloGates.MOD_ID);

    public static final RegistryObject<EntityType<GateEntity>> GATE =
        ENTITIES.register("gate_portal", () -> EntityType.Builder
            .<GateEntity>of(GateEntity::new, MobCategory.MISC)
            .sized(3.0f, 3.0f)
            .noSummon()
            .build("gate_portal"));

    private ModEntities() {}
}
