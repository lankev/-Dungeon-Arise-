package fr.matis.sologates;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

import java.util.Optional;

public class GateCompleteTrigger extends SimpleCriterionTrigger<GateCompleteTrigger.TriggerInstance> {
    public static final ResourceLocation ID = new ResourceLocation(SoloGates.MOD_ID, "gate_complete");

    @Override
    public ResourceLocation getId() { return ID; }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext ctx) {
        String rankStr = GsonHelper.getAsString(json, "rank", "ANY");
        boolean bossOnly = GsonHelper.getAsBoolean(json, "boss_only", false);
        Optional<GateRank> rank = Optional.empty();
        if (!rankStr.equals("ANY")) {
            try { rank = Optional.of(GateRank.valueOf(rankStr.toUpperCase())); }
            catch (IllegalArgumentException ignored) {}
        }
        return new TriggerInstance(player, rank, bossOnly);
    }

    public void trigger(ServerPlayer player, GateRank rank, boolean bossGate) {
        this.trigger(player, inst -> inst.matches(rank, bossGate));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<GateRank> rank;
        private final boolean bossOnly;

        public TriggerInstance(ContextAwarePredicate player, Optional<GateRank> rank, boolean bossOnly) {
            super(ID, player);
            this.rank = rank;
            this.bossOnly = bossOnly;
        }

        public boolean matches(GateRank rank, boolean bossGate) {
            if (bossOnly && !bossGate) return false;
            return this.rank.isEmpty() || this.rank.get() == rank;
        }
    }
}
