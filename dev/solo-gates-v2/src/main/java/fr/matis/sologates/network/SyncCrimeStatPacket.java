package fr.matis.sologates.network;

import fr.matis.sologates.client.ClientCrimeData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncCrimeStatPacket {
    private final UUID uuid;
    private final int level;

    public SyncCrimeStatPacket(UUID uuid, int level) {
        this.uuid = uuid;
        this.level = level;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeVarInt(level);
    }

    public static SyncCrimeStatPacket decode(FriendlyByteBuf buf) {
        return new SyncCrimeStatPacket(buf.readUUID(), buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientCrimeData.update(uuid, level));
        ctx.get().setPacketHandled(true);
    }
}
