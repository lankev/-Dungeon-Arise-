package fr.matis.sologates;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.nbt.CompoundTag;

import java.util.EnumMap;
import java.util.Map;

public class PlayerData {
    public final java.util.UUID playerId;
    private final Map<GateRank, Integer> completions = new EnumMap<>(GateRank.class);
    private final Map<GateRank, Integer> kills = new EnumMap<>(GateRank.class);
    private int totalCompletions;
    private int totalKills;
    private int currentStreak;
    private int bestStreak;
    private GateRank confirmedRank = GateRank.E;

    public PlayerData(java.util.UUID playerId) {
        this.playerId = playerId;
    }

    public void recordCompletion(GateRank rank) {
        completions.merge(rank, 1, Integer::sum);
        totalCompletions++;
    }

    public void recordKills(GateRank rank, int count) {
        kills.merge(rank, count, Integer::sum);
        totalKills += count;
    }

    public void incrementStreak() {
        currentStreak++;
        if (currentStreak > bestStreak) bestStreak = currentStreak;
    }

    public void resetStreak() {
        currentStreak = 0;
    }

    public int currentStreak() { return currentStreak; }
    public int bestStreak() { return bestStreak; }
    public GateRank confirmedRank() { return confirmedRank; }
    public void setConfirmedRank(GateRank rank) { this.confirmedRank = rank; }

    /** Bonus items à donner directement au joueur selon la série. */
    public int streakBonusRolls() {
        if (currentStreak >= 16) return 3;
        if (currentStreak >= 10) return 2;
        if (currentStreak >= 6) return 1;
        return 0;
    }

    public int completions(GateRank rank) {
        return completions.getOrDefault(rank, 0);
    }

    public int kills(GateRank rank) {
        return kills.getOrDefault(rank, 0);
    }

    public int totalCompletions() { return totalCompletions; }
    public int totalKills() { return totalKills; }

    /** Hunter rank based on total completions. */
    public GateRank hunterRank() {
        if (totalCompletions >= 100) return GateRank.S;
        if (totalCompletions >= 40) return GateRank.A;
        if (totalCompletions >= 15) return GateRank.B;
        if (totalCompletions >= 5) return GateRank.C;
        return GateRank.E;
    }

    public MutableComponent statsComponent() {
        GateRank hr = hunterRank();
        MutableComponent msg = Component.literal("=== Solo Gates : Statistiques ===\n")
            .withStyle(ChatFormatting.GOLD);
        msg.append(Component.literal("Rang chasseur : ").withStyle(ChatFormatting.GRAY));
        msg.append(Component.literal(hr.name()).withStyle(hr.color()));
        msg.append(Component.literal("\nDonjons terminés : " + totalCompletions).withStyle(ChatFormatting.WHITE));
        msg.append(Component.literal("\nMonstres tués : " + totalKills).withStyle(ChatFormatting.WHITE));
        msg.append(Component.literal("\nSérie actuelle : " + currentStreak).withStyle(ChatFormatting.GOLD));
        msg.append(Component.literal(" | Meilleure série : " + bestStreak).withStyle(ChatFormatting.YELLOW));
        msg.append(Component.literal("\n--- Par rang ---").withStyle(ChatFormatting.GRAY));
        for (GateRank rank : GateRank.values()) {
            int c = completions(rank);
            int k = kills(rank);
            if (c > 0 || k > 0) {
                msg.append(Component.literal("\n" + rank.name() + " : ").withStyle(rank.color()));
                msg.append(Component.literal(c + " terminés, " + k + " kills").withStyle(ChatFormatting.WHITE));
            }
        }
        return msg;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("PlayerId", playerId);
        tag.putInt("TotalCompletions", totalCompletions);
        tag.putInt("TotalKills", totalKills);
        CompoundTag completionsTag = new CompoundTag();
        for (Map.Entry<GateRank, Integer> entry : completions.entrySet()) {
            completionsTag.putInt(entry.getKey().name(), entry.getValue());
        }
        tag.put("Completions", completionsTag);
        CompoundTag killsTag = new CompoundTag();
        for (Map.Entry<GateRank, Integer> entry : kills.entrySet()) {
            killsTag.putInt(entry.getKey().name(), entry.getValue());
        }
        tag.put("Kills", killsTag);
        tag.putInt("CurrentStreak", currentStreak);
        tag.putInt("BestStreak", bestStreak);
        tag.putString("ConfirmedRank", confirmedRank.name());
        return tag;
    }

    public static PlayerData load(CompoundTag tag) {
        PlayerData data = new PlayerData(tag.getUUID("PlayerId"));
        data.totalCompletions = tag.getInt("TotalCompletions");
        data.totalKills = tag.getInt("TotalKills");
        CompoundTag completionsTag = tag.getCompound("Completions");
        for (GateRank rank : GateRank.values()) {
            if (completionsTag.contains(rank.name())) {
                data.completions.put(rank, completionsTag.getInt(rank.name()));
            }
        }
        CompoundTag killsTag = tag.getCompound("Kills");
        for (GateRank rank : GateRank.values()) {
            if (killsTag.contains(rank.name())) {
                data.kills.put(rank, killsTag.getInt(rank.name()));
            }
        }
        data.currentStreak = tag.getInt("CurrentStreak");
        data.bestStreak = tag.getInt("BestStreak");
        try { data.confirmedRank = GateRank.valueOf(tag.getString("ConfirmedRank")); }
        catch (IllegalArgumentException ignored) {}
        return data;
    }
}
