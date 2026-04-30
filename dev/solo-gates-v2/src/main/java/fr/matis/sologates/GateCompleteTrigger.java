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
        int count = GsonHelper.getAsInt(json, "count", 0);
        Optional<GateRank> rank = Optional.empty();
        if (!rankStr.equals("ANY")) {
            try { rank = Optional.of(GateRank.valueOf(rankStr.toUpperCase())); }
            catch (IllegalArgumentException ignored) {}
        }
        return new TriggerInstance(player, rank, bossOnly, count);
    }

    public void trigger(ServerPlayer player, GateRank rank, boolean bossGate, int completionCount) {
        this.trigger(player, inst -> inst.matches(rank, bossGate, completionCount));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<GateRank> rank;
        private final boolean bossOnly;
        private final int requiredCount;

        public TriggerInstance(ContextAwarePredicate player, Optional<GateRank> rank, boolean bossOnly, int requiredCount) {
            super(ID, player);
            this.rank = rank;
            this.bossOnly = bossOnly;
            this.requiredCount = requiredCount;
        }

        public boolean matches(GateRank rank, boolean bossGate, int completionCount) {
            if (bossOnly && !bossGate) return false;
            if (requiredCount > 0 && completionCount < requiredCount) return false;
            return this.rank.isEmpty() || this.rank.get() == rank;
        }
    }
}
