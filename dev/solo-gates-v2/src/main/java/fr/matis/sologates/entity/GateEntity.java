package fr.matis.sologates.entity;

import fr.matis.sologates.GateManager;
import fr.matis.sologates.GateRank;
import fr.matis.sologates.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public class GateEntity extends Entity {

    private static final EntityDataAccessor<String> DATA_RANK =
        SynchedEntityData.defineId(GateEntity.class, EntityDataSerializers.STRING);

    private DustParticleOptions cachedDust;

    public GateEntity(EntityType<? extends GateEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.noCulling = true;
        this.setInvulnerable(true);
    }

    public static GateEntity create(Level level, GateRank rank, BlockPos pos) {
        GateEntity e = new GateEntity(ModEntities.GATE.get(), level);
        e.entityData.set(DATA_RANK, rank.name());
        e.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        return e;
    }

    public GateRank getRank() {
        try { return GateRank.valueOf(entityData.get(DATA_RANK)); }
        catch (IllegalArgumentException ignored) { return GateRank.E; }
    }

    @Override
    public boolean shouldBeSaved() { return true; }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel serverLevel)) return;
        if (cachedDust == null) {
            float[] c = getRank().renderColor();
            cachedDust = new DustParticleOptions(new Vector3f(c[0], c[1], c[2]), 1.2f);
        }
        for (int i = 0; i < 6; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = 1.0 + random.nextDouble() * 0.6;
            serverLevel.sendParticles(cachedDust,
                getX() + Math.cos(angle) * radius,
                getY() + random.nextDouble() * 3.0,
                getZ() + Math.sin(angle) * radius,
                1, 0, 0.03, 0, 0.01);
        }
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_RANK, GateRank.E.name());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Rank")) {
            try {
                entityData.set(DATA_RANK, GateRank.valueOf(tag.getString("Rank")).name());
                cachedDust = null;
            } catch (IllegalArgumentException ignored) {}
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("Rank", entityData.get(DATA_RANK));
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide && player instanceof ServerPlayer sp) {
            GateManager.useGate(sp, this.blockPosition());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) { return false; }

    @Override
    public boolean isPickable() { return true; }

    @Override
    public boolean isPushable() { return false; }

    @Override
    public void push(Entity entity) {}
}
