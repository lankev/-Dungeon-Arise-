package fr.matis.sologates;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CrimeSavedData extends SavedData {
    private static final String NAME = "sologates_crime_stats";
    private final Map<UUID, CrimeStatData> players = new HashMap<>();

    public static CrimeSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(CrimeSavedData::load, CrimeSavedData::new, NAME);
    }

    public CrimeStatData getOrCreate(UUID playerId) {
        return players.computeIfAbsent(playerId, CrimeStatData::new);
    }

    public Optional<CrimeStatData> get(UUID playerId) {
        return Optional.ofNullable(players.get(playerId));
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (CrimeStatData data : players.values()) {
            list.add(data.save());
        }
        tag.put("Players", list);
        return tag;
    }

    public static CrimeSavedData load(CompoundTag tag) {
        CrimeSavedData saved = new CrimeSavedData();
        ListTag list = tag.getList("Players", Tag.TAG_COMPOUND);
        for (Tag entry : list) {
            CrimeStatData data = CrimeStatData.load((CompoundTag) entry);
            saved.players.put(data.playerId, data);
        }
        return saved;
    }
}
