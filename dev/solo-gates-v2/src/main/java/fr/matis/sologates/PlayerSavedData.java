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

public class PlayerSavedData extends SavedData {
    private static final String NAME = "sologates_players";
    private final Map<UUID, PlayerData> players = new HashMap<>();

    public static PlayerSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(PlayerSavedData::load, PlayerSavedData::new, NAME);
    }

    public PlayerData getOrCreate(UUID playerId) {
        return players.computeIfAbsent(playerId, PlayerData::new);
    }

    public Optional<PlayerData> get(UUID playerId) {
        return Optional.ofNullable(players.get(playerId));
    }

    public void markDirty() {
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (PlayerData pd : players.values()) {
            list.add(pd.save());
        }
        tag.put("Players", list);
        return tag;
    }

    public static PlayerSavedData load(CompoundTag tag) {
        PlayerSavedData data = new PlayerSavedData();
        ListTag list = tag.getList("Players", 10);
        for (Tag entry : list) {
            PlayerData pd = PlayerData.load((CompoundTag) entry);
            data.players.put(pd.playerId, pd);
        }
        return data;
    }
}
