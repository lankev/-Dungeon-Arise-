package fr.matis.sologates;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SoloGatesEvents {

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        long tick = event.getServer().getTickCount();
        // Main gate tick every 5 seconds
        if (tick % 100 == 0) {
            ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
            if (overworld != null) {
                GateManager.serverTick(overworld);
            }
        }
        // Mob count HUD update every 30 seconds for players inside a dungeon
        if (tick % 600 == 0) {
            ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
            if (overworld != null) {
                GateManager.sendMobCountUpdates(overworld);
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof ServerPlayer player) {
            GateManager.onPlayerDeath(player);
        } else {
            GateManager.onMobKilled((Entity) entity);
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        SoloGatesCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (GateManager.shouldCancelBreak(event.getPlayer().level(), event.getPos())) {
            event.setCanceled(true);
        }
    }
}
