package fr.matis.sologates.client;

import fr.matis.sologates.GateRank;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientPlayerData {

    private static GateRank rank = GateRank.E;
    private static int streak = 0;
    private static int crimeStat = 0;

    private static long streakFlashUntil = 0L;
    private static boolean pendingStreakSound = false;

    public static void update(GateRank newRank, int newStreak, int newCrimeStat) {
        if (newStreak > streak && newStreak >= 2) {
            streakFlashUntil = System.currentTimeMillis() + 2000L;
            pendingStreakSound = true;
        }
        rank = newRank;
        streak = newStreak;
        crimeStat = newCrimeStat;
    }

    public static GateRank rank() { return rank; }
    public static int streak() { return streak; }
    public static int crimeStat() { return crimeStat; }

    public static boolean isStreakFlashing() {
        return System.currentTimeMillis() < streakFlashUntil;
    }

    public static boolean takePendingStreakSound() {
        boolean v = pendingStreakSound;
        pendingStreakSound = false;
        return v;
    }

    private ClientPlayerData() {}
}
