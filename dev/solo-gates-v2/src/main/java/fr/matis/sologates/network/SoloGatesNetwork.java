package fr.matis.sologates.network;

import fr.matis.sologates.GateRank;
import fr.matis.sologates.CrimeStatManager;
import fr.matis.sologates.SoloGates;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class SoloGatesNetwork {

    private static final String PROTOCOL = "2";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(SoloGates.MOD_ID, "main"),
        () -> PROTOCOL,
        PROTOCOL::equals,
        PROTOCOL::equals
    );

    public static void register() {
        CHANNEL.registerMessage(0, SyncRankPacket.class,
            SyncRankPacket::encode,
            SyncRankPacket::decode,
            SyncRankPacket::handle,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public static void sendRankToPlayer(ServerPlayer player, GateRank rank, int streak) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncRankPacket(rank, streak, CrimeStatManager.crimeLevel(player)));
    }

    private SoloGatesNetwork() {}
}
