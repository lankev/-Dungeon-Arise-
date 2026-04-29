package fr.matis.sologates;

import net.minecraft.advancements.CriteriaTriggers;

public final class SoloGatesCriteria {
    public static final GateCompleteTrigger GATE_COMPLETE = new GateCompleteTrigger();
    public static final GateMilestoneTrigger GATE_MILESTONE = new GateMilestoneTrigger();

    public static void register() {
        CriteriaTriggers.register(GATE_COMPLETE);
        CriteriaTriggers.register(GATE_MILESTONE);
    }

    private SoloGatesCriteria() {}
}
