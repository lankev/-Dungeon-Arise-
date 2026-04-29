package fr.matis.sologates;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

import java.util.Set;

public class GateMilestoneTrigger extends SimpleCriterionTrigger<GateMilestoneTrigger.TriggerInstance> {
    public static final ResourceLocation ID = new ResourceLocation(SoloGates.MOD_ID, "gate_milestone");

    private static final Set<Integer> MILESTONE_VALUES = Set.of(10, 40, 100);

    @Override
    public ResourceLocation getId() { return ID; }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext ctx) {
        int count = GsonHelper.getAsInt(json, "count", 1);
        return new TriggerInstance(player, count);
    }

    public void trigger(ServerPlayer player, int totalCompletions) {
        if (MILESTONE_VALUES.contains(totalCompletions)) {
            this.trigger(player, inst -> inst.matches(totalCompletions));
        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final int requiredCount;

        public TriggerInstance(ContextAwarePredicate player, int requiredCount) {
            super(ID, player);
            this.requiredCount = requiredCount;
        }

        public boolean matches(int total) {
            return total >= requiredCount;
        }
    }
}
