package fr.matis.sologates;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class GateSavedData extends SavedData {
    private static final String NAME = "sologates_gates";
    private final Map<UUID, GateRecord> gates = new HashMap<>();
    private final Map<UUID, Long> playerCooldowns = new HashMap<>();
    private long nextWorldSpawnTick;
    private int nextDungeonIndex;

    public static GateSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(GateSavedData::load, GateSavedData::new, NAME);
    }

    public Collection<GateRecord> gates() { return gates.values(); }

    public Optional<GateRecord> byId(UUID id) { return Optional.ofNullable(gates.get(id)); }

    public void addGate(GateRecord gate) {
        gates.put(gate.id, gate);
        setDirty();
    }

    public void removeGate(UUID id) {
        gates.remove(id);
        setDirty();
    }

    public long nextWorldSpawnTick() { return nextWorldSpawnTick; }

    public void setNextWorldSpawnTick(long tick) {
        nextWorldSpawnTick = tick;
        setDirty();
    }

    public long playerCooldown(UUID playerId) {
        return playerCooldowns.getOrDefault(playerId, 0L);
    }

    public void setPlayerCooldown(UUID playerId, long tick) {
        playerCooldowns.put(playerId, tick);
        setDirty();
    }

    public int nextDungeonIndex() {
        ++nextDungeonIndex;
        setDirty();
        return nextDungeonIndex;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong("NextWorldSpawnTick", nextWorldSpawnTick);
        tag.putInt("NextDungeonIndex", nextDungeonIndex);

        ListTag gateList = new ListTag();
        for (GateRecord gate : gates.values()) {
            gateList.add(gate.save());
        }
        tag.put("Gates", gateList);

        ListTag cooldownList = new ListTag();
        playerCooldowns.forEach((playerId, tick) -> {
            CompoundTag cooldown = new CompoundTag();
            cooldown.putUUID("Player", playerId);
            cooldown.putLong("Tick", tick);
            cooldownList.add(cooldown);
        });
        tag.put("PlayerCooldowns", cooldownList);
        return tag;
    }

    public static GateSavedData load(CompoundTag tag) {
        GateSavedData data = new GateSavedData();
        data.nextWorldSpawnTick = tag.getLong("NextWorldSpawnTick");
        data.nextDungeonIndex = tag.getInt("NextDungeonIndex");

        ListTag gateList = tag.getList("Gates", 10);
        for (Tag gateTag : gateList) {
            GateRecord gate = GateRecord.load((CompoundTag) gateTag);
            data.gates.put(gate.id, gate);
        }

        ListTag cooldownList = tag.getList("PlayerCooldowns", 10);
        for (Tag cooldownTag : cooldownList) {
            CompoundTag cooldown = (CompoundTag) cooldownTag;
            data.playerCooldowns.put(cooldown.getUUID("Player"), cooldown.getLong("Tick"));
        }
        return data;
    }
}
