/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 */
package fr.matis.sologates;

import fr.matis.sologates.SoloGatesConfig;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

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

    private GateRank(ChatFormatting color, int minMobCount, int maxMobCount, int maxSameMob) {
        this.color = color;
        this.minMobCount = minMobCount;
        this.maxMobCount = maxMobCount;
        this.maxSameMob = maxSameMob;
    }

    public int minMobCount() {
        return this.minMobCount;
    }

    public int maxMobCount() {
        return this.maxMobCount;
    }

    public int maxSameMob() {
        return this.maxSameMob;
    }

    public MutableComponent displayName() {
        return Component.m_237113_((String)("Portail rang " + this.name())).m_130940_(this.color);
    }

    public List<? extends String> mobs() {
        return switch (this) {
            default -> throw new IncompatibleClassChangeError();
            case E -> (List)SoloGatesConfig.RANK_E_MOBS.get();
            case C -> (List)SoloGatesConfig.RANK_C_MOBS.get();
            case B -> (List)SoloGatesConfig.RANK_B_MOBS.get();
            case A -> (List)SoloGatesConfig.RANK_A_MOBS.get();
            case S -> (List)SoloGatesConfig.RANK_S_MOBS.get();
        };
    }

    public List<? extends String> rewards() {
        return switch (this) {
            default -> throw new IncompatibleClassChangeError();
            case E -> (List)SoloGatesConfig.RANK_E_REWARDS.get();
            case C -> (List)SoloGatesConfig.RANK_C_REWARDS.get();
            case B -> (List)SoloGatesConfig.RANK_B_REWARDS.get();
            case A -> (List)SoloGatesConfig.RANK_A_REWARDS.get();
            case S -> (List)SoloGatesConfig.RANK_S_REWARDS.get();
        };
    }
}

