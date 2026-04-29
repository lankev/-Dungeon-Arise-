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
    public static final ResourceLocation ID = new ResourceLocation("sologates", "gate_complete");

    @Override
    public ResourceLocation getId() { return ID; }

    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext ctx) {
        String rank = GsonHelper.getAsString(json, "rank", "ANY");
        boolean bossOnly = GsonHelper.getAsBoolean(json, "boss_only", false);
        return new TriggerInstance(player, rank, bossOnly);
    }

    public void trigger(ServerPlayer player, GateRank rank, boolean bossGate) {
        this.trigger(player, inst -> inst.matches(rank, bossGate));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final String rank;
        private final boolean bossOnly;

        public TriggerInstance(Optional<ContextAwarePredicate> player, String rank, boolean bossOnly) {
            super(ID, player);
            this.rank = rank;
            this.bossOnly = bossOnly;
        }

        public boolean matches(GateRank rank, boolean bossGate) {
            if (bossOnly && !bossGate) return false;
            return this.rank.equals("ANY") || this.rank.equalsIgnoreCase(rank.name());
        }
    }
}
