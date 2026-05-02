package fr.matis.sologates;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeStatData {
    public final UUID playerId;
    private final List<Long> innocentKillTicks = new ArrayList<>();
    private final List<Long> stolenPointTicks = new ArrayList<>();
    private int level;

    public CrimeStatData(UUID playerId) {
        this.playerId = playerId;
    }

    public int level() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(0, Math.min(3, level));
        if (this.level == 0) {
            innocentKillTicks.clear();
            stolenPointTicks.clear();
        }
    }

    public void recordInnocentKill(long gameTime) {
        prune(gameTime);
        innocentKillTicks.add(gameTime);
        level = Math.min(3, level + 1);
    }

    public int remainingSteals(long gameTime) {
        prune(gameTime);
        return Math.max(0, stealLimit() - stolenPointTicks.size());
    }

    public void recordStolenPoint(long gameTime) {
        prune(gameTime);
        stolenPointTicks.add(gameTime);
    }

    public int stealLimit() {
        return switch (level) {
            case 1 -> 2;
            case 2 -> 3;
            case 3 -> 4;
            default -> 0;
        };
    }

    public void clearCrimeStat() {
        level = 0;
        innocentKillTicks.clear();
        stolenPointTicks.clear();
    }

    public void prune(long gameTime) {
        long minTick = gameTime - CrimeStatManager.CRIME_WINDOW_TICKS;
        innocentKillTicks.removeIf(tick -> tick < minTick);
        stolenPointTicks.removeIf(tick -> tick < minTick);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("PlayerId", playerId);
        tag.putInt("Level", level);
        ListTag kills = new ListTag();
        for (long tick : innocentKillTicks) {
            CompoundTag kill = new CompoundTag();
            kill.putLong("Tick", tick);
            kills.add(kill);
        }
        tag.put("InnocentKills", kills);
        ListTag stolen = new ListTag();
        for (long tick : stolenPointTicks) {
            CompoundTag point = new CompoundTag();
            point.putLong("Tick", tick);
            stolen.add(point);
        }
        tag.put("StolenPoints", stolen);
        return tag;
    }

    public static CrimeStatData load(CompoundTag tag) {
        CrimeStatData data = new CrimeStatData(tag.getUUID("PlayerId"));
        data.level = Math.max(0, Math.min(3, tag.getInt("Level")));
        ListTag kills = tag.getList("InnocentKills", Tag.TAG_COMPOUND);
        for (Tag entry : kills) {
            data.innocentKillTicks.add(((CompoundTag) entry).getLong("Tick"));
        }
        ListTag stolen = tag.getList("StolenPoints", Tag.TAG_COMPOUND);
        for (Tag entry : stolen) {
            data.stolenPointTicks.add(((CompoundTag) entry).getLong("Tick"));
        }
        return data;
    }
}
