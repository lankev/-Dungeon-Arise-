package fr.matis.sologates;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public enum GateRank {
    E(ChatFormatting.GREEN, 6, 9, 2),
    C(ChatFormatting.AQUA, 9, 13, 2),
    B(ChatFormatting.LIGHT_PURPLE, 11, 16, 2),
    A(ChatFormatting.DARK_PURPLE, 13, 18, 2),
    S(ChatFormatting.RED, 12, 16, 3);

    private final ChatFormatting color;
    private final int minMobCount;
    private final int maxMobCount;
    private final int maxSameMob;

    GateRank(ChatFormatting color, int minMobCount, int maxMobCount, int maxSameMob) {
        this.color = color;
        this.minMobCount = minMobCount;
        this.maxMobCount = maxMobCount;
        this.maxSameMob = maxSameMob;
    }

    public int minMobCount() { return minMobCount; }
    public int maxMobCount() { return maxMobCount; }
    public int maxSameMob() { return maxSameMob; }
    public ChatFormatting color() { return color; }

    public MutableComponent displayName() {
        return Component.literal("Portail rang " + name()).withStyle(color);
    }

    public List<? extends String> mobs() {
        return switch (this) {
            case E -> SoloGatesConfig.RANK_E_MOBS.get();
            case C -> SoloGatesConfig.RANK_C_MOBS.get();
            case B -> SoloGatesConfig.RANK_B_MOBS.get();
            case A -> SoloGatesConfig.RANK_A_MOBS.get();
            case S -> SoloGatesConfig.RANK_S_MOBS.get();
        };
    }

    public List<? extends String> rewards() {
        return switch (this) {
            case E -> SoloGatesConfig.RANK_E_REWARDS.get();
            case C -> SoloGatesConfig.RANK_C_REWARDS.get();
            case B -> SoloGatesConfig.RANK_B_REWARDS.get();
            case A -> SoloGatesConfig.RANK_A_REWARDS.get();
            case S -> SoloGatesConfig.RANK_S_REWARDS.get();
        };
    }

    public int xpReward() {
        return switch (this) {
            case E -> SoloGatesConfig.XP_REWARD_E.get();
            case C -> SoloGatesConfig.XP_REWARD_C.get();
            case B -> SoloGatesConfig.XP_REWARD_B.get();
            case A -> SoloGatesConfig.XP_REWARD_A.get();
            case S -> SoloGatesConfig.XP_REWARD_S.get();
        };
    }

    public double mobHealthMultiplier() {
        return switch (this) {
            case E -> SoloGatesConfig.MOB_HP_MULT_E.get();
            case C -> SoloGatesConfig.MOB_HP_MULT_C.get();
            case B -> SoloGatesConfig.MOB_HP_MULT_B.get();
            case A -> SoloGatesConfig.MOB_HP_MULT_A.get();
            case S -> SoloGatesConfig.MOB_HP_MULT_S.get();
        };
    }
}
