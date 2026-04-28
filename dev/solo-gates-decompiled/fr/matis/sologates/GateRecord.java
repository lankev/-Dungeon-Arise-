/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 */
package fr.matis.sologates;

import fr.matis.sologates.GateRank;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class GateRecord {
    public final UUID id;
    public final GateRank rank;
    public final BlockPos overworldPos;
    public final BlockPos dungeonPos;
    public final long createdTick;
    public final Set<UUID> mobs = new HashSet<UUID>();
    public final Set<UUID> rewardedPlayers = new HashSet<UUID>();
    public final Map<UUID, BlockPos> returnPositions = new HashMap<UUID, BlockPos>();
    public boolean bossGate;
    public boolean completed;
    public boolean failed;

    public GateRecord(UUID id, GateRank rank, BlockPos overworldPos, BlockPos dungeonPos, long createdTick) {
        this.id = id;
        this.rank = rank;
        this.overworldPos = overworldPos;
        this.dungeonPos = dungeonPos;
        this.createdTick = createdTick;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.m_128362_("Id", this.id);
        tag.m_128359_("Rank", this.rank.name());
        tag.m_128356_("CreatedTick", this.createdTick);
        tag.m_128356_("OverworldPos", this.overworldPos.m_121878_());
        tag.m_128356_("DungeonPos", this.dungeonPos.m_121878_());
        tag.m_128379_("BossGate", this.bossGate);
        tag.m_128379_("Completed", this.completed);
        tag.m_128379_("Failed", this.failed);
        ListTag mobList = new ListTag();
        for (UUID uUID : this.mobs) {
            CompoundTag mobTag = new CompoundTag();
            mobTag.m_128362_("Mob", uUID);
            mobList.add((Object)mobTag);
        }
        tag.m_128365_("Mobs", (Tag)mobList);
        ListTag rewardedList = new ListTag();
        for (UUID player : this.rewardedPlayers) {
            CompoundTag rewardedTag = new CompoundTag();
            rewardedTag.m_128362_("Player", player);
            rewardedList.add((Object)rewardedTag);
        }
        tag.m_128365_("RewardedPlayers", (Tag)rewardedList);
        ListTag listTag = new ListTag();
        this.returnPositions.forEach((playerId, pos) -> {
            CompoundTag returnTag = new CompoundTag();
            returnTag.m_128362_("Player", playerId);
            returnTag.m_128356_("Pos", pos.m_121878_());
            returnList.add((Object)returnTag);
        });
        tag.m_128365_("ReturnPositions", (Tag)listTag);
        return tag;
    }

    public static GateRecord load(CompoundTag tag) {
        GateRecord record = new GateRecord(tag.m_128342_("Id"), GateRank.valueOf(tag.m_128461_("Rank")), BlockPos.m_122022_((long)tag.m_128454_("OverworldPos")), BlockPos.m_122022_((long)tag.m_128454_("DungeonPos")), tag.m_128454_("CreatedTick"));
        record.bossGate = tag.m_128471_("BossGate");
        record.completed = tag.m_128471_("Completed");
        record.failed = tag.m_128471_("Failed");
        ListTag mobList = tag.m_128437_("Mobs", 10);
        for (Object mobEntry : mobList) {
            record.mobs.add(((CompoundTag)mobEntry).m_128342_("Mob"));
        }
        ListTag rewardedList = tag.m_128437_("RewardedPlayers", 10);
        for (Tag rewardedEntry : rewardedList) {
            record.rewardedPlayers.add(((CompoundTag)rewardedEntry).m_128342_("Player"));
        }
        ListTag returnList = tag.m_128437_("ReturnPositions", 10);
        for (Tag returnEntry : returnList) {
            CompoundTag returnTag = (CompoundTag)returnEntry;
            record.returnPositions.put(returnTag.m_128342_("Player"), BlockPos.m_122022_((long)returnTag.m_128454_("Pos")));
        }
        return record;
    }

    public void setReturnPosition(UUID playerId, BlockPos pos) {
        this.returnPositions.put(playerId, pos);
    }

    public Optional<BlockPos> returnPosition(UUID playerId) {
        return Optional.ofNullable(this.returnPositions.get(playerId));
    }

    public boolean markRewarded(UUID playerId) {
        return this.rewardedPlayers.add(playerId);
    }
}

