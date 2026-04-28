/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.world.level.saveddata.SavedData
 */
package fr.matis.sologates;

import fr.matis.sologates.GateRecord;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public class GateSavedData
extends SavedData {
    private static final String NAME = "sologates_gates";
    private final Map<UUID, GateRecord> gates = new HashMap<UUID, GateRecord>();
    private final Map<UUID, Long> playerCooldowns = new HashMap<UUID, Long>();
    private long nextWorldSpawnTick;
    private int nextDungeonIndex;

    public static GateSavedData get(MinecraftServer server) {
        return (GateSavedData)server.m_129783_().m_8895_().m_164861_(GateSavedData::load, GateSavedData::new, NAME);
    }

    public Collection<GateRecord> gates() {
        return this.gates.values();
    }

    public Optional<GateRecord> byId(UUID id) {
        return Optional.ofNullable(this.gates.get(id));
    }

    public void addGate(GateRecord gate) {
        this.gates.put(gate.id, gate);
        this.m_77762_();
    }

    public void removeGate(UUID id) {
        this.gates.remove(id);
        this.m_77762_();
    }

    public long nextWorldSpawnTick() {
        return this.nextWorldSpawnTick;
    }

    public void setNextWorldSpawnTick(long tick) {
        this.nextWorldSpawnTick = tick;
        this.m_77762_();
    }

    public long playerCooldown(UUID playerId) {
        return this.playerCooldowns.getOrDefault(playerId, 0L);
    }

    public void setPlayerCooldown(UUID playerId, long tick) {
        this.playerCooldowns.put(playerId, tick);
        this.m_77762_();
    }

    public int nextDungeonIndex() {
        ++this.nextDungeonIndex;
        this.m_77762_();
        return this.nextDungeonIndex;
    }

    public CompoundTag m_7176_(CompoundTag tag) {
        tag.m_128356_("NextWorldSpawnTick", this.nextWorldSpawnTick);
        tag.m_128405_("NextDungeonIndex", this.nextDungeonIndex);
        ListTag gateList = new ListTag();
        for (GateRecord gate : this.gates.values()) {
            gateList.add((Object)gate.save());
        }
        tag.m_128365_("Gates", (Tag)gateList);
        ListTag cooldownList = new ListTag();
        this.playerCooldowns.forEach((playerId, tick) -> {
            CompoundTag cooldown = new CompoundTag();
            cooldown.m_128362_("Player", playerId);
            cooldown.m_128356_("Tick", tick.longValue());
            cooldownList.add((Object)cooldown);
        });
        tag.m_128365_("PlayerCooldowns", (Tag)cooldownList);
        return tag;
    }

    public static GateSavedData load(CompoundTag tag) {
        GateSavedData data = new GateSavedData();
        data.nextWorldSpawnTick = tag.m_128454_("NextWorldSpawnTick");
        data.nextDungeonIndex = tag.m_128451_("NextDungeonIndex");
        ListTag gateList = tag.m_128437_("Gates", 10);
        for (Tag gateTag : gateList) {
            GateRecord gate = GateRecord.load((CompoundTag)gateTag);
            data.gates.put(gate.id, gate);
        }
        ListTag cooldownList = tag.m_128437_("PlayerCooldowns", 10);
        for (Tag cooldownTag : cooldownList) {
            CompoundTag cooldown = (CompoundTag)cooldownTag;
            data.playerCooldowns.put(cooldown.m_128342_("Player"), cooldown.m_128454_("Tick"));
        }
        return data;
    }
}

