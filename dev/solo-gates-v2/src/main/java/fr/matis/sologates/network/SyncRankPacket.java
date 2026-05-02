package fr.matis.sologates.network;

import fr.matis.sologates.GateRank;
import fr.matis.sologates.client.ClientPlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncRankPacket {

    private final GateRank rank;
    private final int streak;
    private final int crimeStat;

    public SyncRankPacket(GateRank rank, int streak, int crimeStat) {
        this.rank = rank;
        this.streak = streak;
        this.crimeStat = crimeStat;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(rank);
        buf.writeVarInt(streak);
        buf.writeVarInt(crimeStat);
    }

    public static SyncRankPacket decode(FriendlyByteBuf buf) {
        return new SyncRankPacket(buf.readEnum(GateRank.class), buf.readVarInt(), buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientPlayerData.update(rank, streak, crimeStat));
        ctx.get().setPacketHandled(true);
    }
}
