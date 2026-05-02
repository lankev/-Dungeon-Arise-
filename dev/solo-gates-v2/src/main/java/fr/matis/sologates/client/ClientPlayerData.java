package fr.matis.sologates.client;

import fr.matis.sologates.GateRank;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientPlayerData {

    private static GateRank rank = GateRank.E;
    private static int streak = 0;
    private static int crimeStat = 0;

    public static void update(GateRank newRank, int newStreak, int newCrimeStat) {
        rank = newRank;
        streak = newStreak;
        crimeStat = newCrimeStat;
    }

    public static GateRank rank() { return rank; }
    public static int streak() { return streak; }
    public static int crimeStat() { return crimeStat; }

    private ClientPlayerData() {}
}
