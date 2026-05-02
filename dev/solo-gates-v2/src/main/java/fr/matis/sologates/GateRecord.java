package fr.matis.sologates;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.*;

public class GateRecord {
    public final UUID id;
    public final GateRank rank;
    public final BlockPos overworldPos;
    public final BlockPos dungeonPos;
    public final long createdTick;
    public final Set<UUID> mobs = new HashSet<>();
    public final Set<UUID> rewardedPlayers = new HashSet<>();
    public final Map<UUID, BlockPos> returnPositions = new HashMap<>();
    public boolean bossGate;
    public boolean completed;
    public boolean failed;

    // Multiplayer
    public final Set<UUID> waitingPlayers = new LinkedHashSet<>();
    public long entryWindowEndTick = -1;  // -1 = window not open yet
    public boolean entryStarted = false;
    public int playerCount = 1;
    public final Set<UUID> activeParticipants = new HashSet<>();
    public long dungeonStartTick = -1;

    public GateRecord(UUID id, GateRank rank, BlockPos overworldPos, BlockPos dungeonPos, long createdTick) {
        this.id = id;
        this.rank = rank;
        this.overworldPos = overworldPos;
        this.dungeonPos = dungeonPos;
        this.createdTick = createdTick;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putString("Rank", rank.name());
        tag.putLong("CreatedTick", createdTick);
        tag.putLong("OverworldPos", overworldPos.asLong());
        tag.putLong("DungeonPos", dungeonPos.asLong());
        tag.putBoolean("BossGate", bossGate);
        tag.putBoolean("Completed", completed);
        tag.putBoolean("Failed", failed);
        tag.putLong("EntryWindowEndTick", entryWindowEndTick);
        tag.putBoolean("EntryStarted", entryStarted);
        tag.putInt("PlayerCount", playerCount);
        tag.putLong("DungeonStartTick", dungeonStartTick);

        ListTag mobList = new ListTag();
        for (UUID uuid : mobs) {
            CompoundTag mobTag = new CompoundTag();
            mobTag.putUUID("Mob", uuid);
            mobList.add(mobTag);
        }
        tag.put("Mobs", mobList);

        ListTag rewardedList = new ListTag();
        for (UUID player : rewardedPlayers) {
            CompoundTag rewardedTag = new CompoundTag();
            rewardedTag.putUUID("Player", player);
            rewardedList.add(rewardedTag);
        }
        tag.put("RewardedPlayers", rewardedList);

        ListTag returnList = new ListTag();
        returnPositions.forEach((playerId, pos) -> {
            CompoundTag returnTag = new CompoundTag();
            returnTag.putUUID("Player", playerId);
            returnTag.putLong("Pos", pos.asLong());
            returnList.add(returnTag);
        });
        tag.put("ReturnPositions", returnList);

        ListTag waitingList = new ListTag();
        for (UUID playerId : waitingPlayers) {
            CompoundTag waitingTag = new CompoundTag();
            waitingTag.putUUID("Player", playerId);
            waitingList.add(waitingTag);
        }
        tag.put("WaitingPlayers", waitingList);

        ListTag activeList = new ListTag();
        for (UUID playerId : activeParticipants) {
            CompoundTag activeTag = new CompoundTag();
            activeTag.putUUID("Player", playerId);
            activeList.add(activeTag);
        }
        tag.put("ActiveParticipants", activeList);

        return tag;
    }

    public static GateRecord load(CompoundTag tag) {
        GateRecord record = new GateRecord(
            tag.getUUID("Id"),
            GateRank.valueOf(tag.getString("Rank")),
            BlockPos.of(tag.getLong("OverworldPos")),
            BlockPos.of(tag.getLong("DungeonPos")),
            tag.getLong("CreatedTick")
        );
        record.bossGate = tag.getBoolean("BossGate");
        record.completed = tag.getBoolean("Completed");
        record.failed = tag.getBoolean("Failed");
        record.entryWindowEndTick = tag.contains("EntryWindowEndTick") ? tag.getLong("EntryWindowEndTick") : -1;
        record.entryStarted = tag.getBoolean("EntryStarted");
        record.playerCount = tag.contains("PlayerCount") ? tag.getInt("PlayerCount") : 1;
        record.dungeonStartTick = tag.contains("DungeonStartTick") ? tag.getLong("DungeonStartTick") : -1;

        ListTag mobList = tag.getList("Mobs", 10);
        for (Tag entry : mobList) record.mobs.add(((CompoundTag) entry).getUUID("Mob"));

        ListTag rewardedList = tag.getList("RewardedPlayers", 10);
        for (Tag entry : rewardedList) record.rewardedPlayers.add(((CompoundTag) entry).getUUID("Player"));

        ListTag returnList = tag.getList("ReturnPositions", 10);
        for (Tag entry : returnList) {
            CompoundTag returnTag = (CompoundTag) entry;
            record.returnPositions.put(returnTag.getUUID("Player"), BlockPos.of(returnTag.getLong("Pos")));
        }

        ListTag waitingList = tag.getList("WaitingPlayers", 10);
        for (Tag entry : waitingList) record.waitingPlayers.add(((CompoundTag) entry).getUUID("Player"));

        ListTag activeList = tag.getList("ActiveParticipants", 10);
        for (Tag entry : activeList) record.activeParticipants.add(((CompoundTag) entry).getUUID("Player"));

        return record;
    }

    public void setReturnPosition(UUID playerId, BlockPos pos) {
        returnPositions.put(playerId, pos);
    }

    public Optional<BlockPos> returnPosition(UUID playerId) {
        return Optional.ofNullable(returnPositions.get(playerId));
    }

    public boolean markRewarded(UUID playerId) {
        return rewardedPlayers.add(playerId);
    }
}
