package fr.matis.sologates;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import fr.matis.sologates.network.SoloGatesNetwork;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SoloGates.MOD_ID)
public final class SoloGatesEvents {

    private SoloGatesEvents() {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        long tick = event.getServer().getTickCount();
        if (tick % 100 == 0) {
            ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
            if (overworld != null) GateManager.serverTick(overworld);
        }
        if (tick % 600 == 0) {
            ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
            if (overworld != null) {
                GateManager.sendMobCountUpdates(overworld);
                ServerLevel dungeon = event.getServer().getLevel(GateManager.DUNGEON_LEVEL);
                if (dungeon != null)
                    GateManager.retargetMobsToCriminals(dungeon, GateSavedData.get(event.getServer()));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof ServerPlayer player) {
            CrimeStatManager.onPlayerKilled(player, event.getSource().getEntity());
            GateManager.onPlayerDeath(player);
        } else {
            GateManager.onMobKilled((Entity) entity);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        SoloGatesCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        GateManager.cleanupOrphanedGates(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CrimeStatManager.syncCrimeStat(player);
        CrimeSavedData crimeData = CrimeSavedData.get(player.server);
        crimeData.allEntries().forEach((uuid, data) -> {
            if (data.level() > 0)
                SoloGatesNetwork.sendCrimeStatToPlayer(player, uuid, data.level());
        });
        // Si le joueur était dans le donjon au moment de la déconnexion, le renvoyer en Overworld
        if (player.serverLevel().dimension().equals(GateManager.DUNGEON_LEVEL)) {
            if (!GateManager.leaveDungeon(player)) {
                // Gate introuvable (donjon nettoyé au redémarrage) → spawn Overworld
                ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
                if (overworld != null) {
                    net.minecraft.core.BlockPos spawn = overworld.getSharedSpawnPos();
                    player.teleportTo(overworld,
                        spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                        player.getYRot(), player.getXRot());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer)) return;
        if (victim.serverLevel().dimension().equals(GateManager.DUNGEON_LEVEL)) {
            event.setCanceled(false);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (GateManager.shouldCancelBreak(event.getPlayer().level(), event.getPos())) {
            event.setCanceled(true);
        }
    }
}
