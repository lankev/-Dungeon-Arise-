/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.ChestBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.ChestBlockEntity
 *  net.minecraft.world.level.block.entity.SignBlockEntity
 *  net.minecraft.world.level.block.entity.SignText
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.phys.AABB
 *  net.minecraftforge.registries.ForgeRegistries
 */
package fr.matis.sologates;

import fr.matis.sologates.GateRank;
import fr.matis.sologates.GateRecord;
import fr.matis.sologates.GateSavedData;
import fr.matis.sologates.SoloGatesConfig;
import fr.matis.sologates.registry.ModBlocks;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

public final class GateManager {
    public static final ResourceKey<Level> DUNGEON_LEVEL = ResourceKey.m_135785_((ResourceKey)Registries.f_256858_, (ResourceLocation)new ResourceLocation("sologates", "dungeon"));
    private static final int TICKS_PER_SECOND = 20;
    private static final int DUNGEON_SPACING = 256;

    public static void serverTick(ServerLevel overworld) {
        GateSavedData data = GateSavedData.get(overworld.m_7654_());
        long gameTime = overworld.m_46467_();
        ArrayList<UUID> remove = new ArrayList<UUID>();
        for (GateRecord gate : data.gates()) {
            if (gate.failed) {
                remove.add(gate.id);
                continue;
            }
            if (gate.completed) {
                long completedLifetime = 2400L;
                ServerLevel dungeon = overworld.m_7654_().m_129880_(DUNGEON_LEVEL);
                boolean hasPlayersInside = dungeon != null && !dungeon.m_45976_(ServerPlayer.class, new AABB(gate.dungeonPos).m_82377_(64.0, 16.0, 64.0)).isEmpty();
                if (hasPlayersInside || overworld.m_46467_() - gate.createdTick <= completedLifetime) continue;
                GateManager.removeGateBlocks(overworld, gate.overworldPos);
                if (dungeon != null) {
                    GateManager.clearDungeonSpace(dungeon, gate.dungeonPos);
                }
                remove.add(gate.id);
                continue;
            }
            if (gate.mobs.isEmpty()) {
                GateManager.completeGate(overworld, gate);
                continue;
            }
            long lifetime = Math.min((Integer)SoloGatesConfig.GATE_LIFETIME_SECONDS.get(), 300) * 20;
            if (gameTime - gate.createdTick <= lifetime) continue;
            GateManager.expireGate(overworld, gate);
            remove.add(gate.id);
        }
        remove.forEach(data::removeGate);
        if (gameTime < data.nextWorldSpawnTick() || data.gates().size() >= (Integer)SoloGatesConfig.MAX_ACTIVE_GATES.get()) {
            return;
        }
        List<ServerPlayer> candidates = overworld.m_6907_().stream().filter(player -> !player.m_7500_() && !player.m_5833_()).filter(player -> gameTime >= data.playerCooldown(player.m_20148_())).toList();
        if (candidates.isEmpty()) {
            data.setNextWorldSpawnTick(gameTime + 1200L);
            return;
        }
        ServerPlayer player2 = candidates.get(overworld.f_46441_.m_188503_(candidates.size()));
        Optional<GateRank> spawnedRank = GateManager.trySpawnNearPlayer(overworld, data, player2);
        if (spawnedRank.isPresent()) {
            data.setNextWorldSpawnTick(gameTime + (long)(GateManager.rankSpawnIntervalSeconds(spawnedRank.get()) * 20));
            data.setPlayerCooldown(player2.m_20148_(), gameTime + (long)((Integer)SoloGatesConfig.PLAYER_COOLDOWN_SECONDS.get() * 20));
        } else {
            data.setNextWorldSpawnTick(gameTime + 1200L);
        }
    }

    public static void useGate(ServerPlayer player, BlockPos clickedPos) {
        if (player.m_284548_().m_46472_().equals(DUNGEON_LEVEL)) {
            GateManager.useReturnGate(player, clickedPos);
        } else {
            GateManager.enterGate(player, clickedPos);
        }
    }

    public static void enterGate(ServerPlayer player, BlockPos clickedPos) {
        ServerLevel overworld = player.m_284548_();
        GateSavedData data = GateSavedData.get(overworld.m_7654_());
        Optional<GateRecord> gate = data.gates().stream().filter(record -> record.overworldPos.m_123331_((Vec3i)clickedPos) <= 36.0).min(Comparator.comparingDouble(record -> record.overworldPos.m_123331_((Vec3i)clickedPos)));
        if (gate.isEmpty()) {
            player.m_5661_((Component)Component.m_237113_((String)"Ce portail n'est pas stabilise."), true);
            return;
        }
        ServerLevel dungeon = overworld.m_7654_().m_129880_(DUNGEON_LEVEL);
        if (dungeon == null) {
            player.m_5661_((Component)Component.m_237113_((String)"La dimension des donjons n'est pas chargee."), false);
            return;
        }
        GateRecord record2 = gate.get();
        record2.setReturnPosition(player.m_20148_(), player.m_20183_());
        data.m_77762_();
        player.m_8999_(dungeon, (double)record2.dungeonPos.m_123341_() + 0.5, (double)record2.dungeonPos.m_123342_() + 0.15, (double)record2.dungeonPos.m_123343_() + 0.5, player.m_146908_(), player.m_146909_());
        player.m_5661_((Component)Component.m_237113_((String)(record2.bossGate ? "Tu entres dans un portail boss." : "Tu entres dans un portail de rang " + record2.rank.name() + ".")), true);
    }

    public static boolean spawnManualGate(ServerPlayer player, GateRank rank) {
        ServerLevel overworld = player.m_284548_();
        if (!overworld.m_46472_().equals((Object)Level.f_46428_)) {
            player.m_5661_((Component)Component.m_237113_((String)"Les portails de test doivent etre crees dans l'Overworld."), false);
            return false;
        }
        BlockPos base = player.m_20183_().m_5484_(player.m_6350_(), 5);
        int y = overworld.m_6924_(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base.m_123341_(), base.m_123343_());
        BlockPos pos = new BlockPos(base.m_123341_(), y, base.m_123343_());
        if (!GateManager.canPlaceGate(overworld, pos)) {
            return false;
        }
        GateSavedData data = GateSavedData.get(overworld.m_7654_());
        BlockPos dungeonPos = GateManager.nextDungeonPos(data);
        GateRecord gate = new GateRecord(UUID.randomUUID(), rank, pos, dungeonPos, overworld.m_46467_());
        GateManager.placeGate(overworld, pos, rank);
        GateManager.buildDungeon(overworld.m_7654_().m_129880_(DUNGEON_LEVEL), gate);
        data.addGate(gate);
        overworld.m_5594_(null, pos, SoundEvents.f_12288_, SoundSource.BLOCKS, 2.0f, 0.6f);
        player.m_5661_((Component)rank.displayName().m_7220_((Component)Component.m_237113_((String)" de test cree devant toi.")), false);
        return true;
    }

    public static boolean spawnManualBossGate(ServerPlayer player) {
        ServerLevel overworld = player.m_284548_();
        if (!overworld.m_46472_().equals((Object)Level.f_46428_)) {
            player.m_5661_((Component)Component.m_237113_((String)"Les portails boss doivent etre crees dans l'Overworld."), false);
            return false;
        }
        BlockPos base = player.m_20183_().m_5484_(player.m_6350_(), 5);
        int y = overworld.m_6924_(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base.m_123341_(), base.m_123343_());
        BlockPos pos = new BlockPos(base.m_123341_(), y, base.m_123343_());
        if (!GateManager.canPlaceGate(overworld, pos)) {
            return false;
        }
        GateSavedData data = GateSavedData.get(overworld.m_7654_());
        GateRecord gate = new GateRecord(UUID.randomUUID(), GateRank.S, pos, GateManager.nextDungeonPos(data), overworld.m_46467_());
        gate.bossGate = true;
        GateManager.placeGate(overworld, pos, GateRank.S);
        GateManager.buildDungeon(overworld.m_7654_().m_129880_(DUNGEON_LEVEL), gate);
        data.addGate(gate);
        overworld.m_5594_(null, pos, SoundEvents.f_12563_, SoundSource.HOSTILE, 0.8f, 1.5f);
        player.m_5661_((Component)Component.m_237113_((String)"Portail boss de test cree devant toi."), false);
        return true;
    }

    public static boolean leaveDungeon(ServerPlayer player) {
        ServerLevel level = player.m_284548_();
        if (!level.m_46472_().equals(DUNGEON_LEVEL)) {
            return false;
        }
        GateSavedData data = GateSavedData.get(level.m_7654_());
        Optional<GateRecord> gate = data.gates().stream().filter(record -> record.dungeonPos.m_123331_((Vec3i)player.m_20183_()) < 10000.0).min(Comparator.comparingDouble(record -> record.dungeonPos.m_123331_((Vec3i)player.m_20183_())));
        if (gate.isEmpty()) {
            return false;
        }
        GateManager.teleportToOverworld(player, gate.get());
        return true;
    }

    private static void useReturnGate(ServerPlayer player, BlockPos clickedPos) {
        ServerLevel level = player.m_284548_();
        GateSavedData data = GateSavedData.get(level.m_7654_());
        Optional<GateRecord> gate = data.gates().stream().filter(record -> record.completed).filter(record -> record.dungeonPos.m_7918_(0, 1, -5).m_123331_((Vec3i)clickedPos) <= 36.0).min(Comparator.comparingDouble(record -> record.dungeonPos.m_7918_(0, 1, -5).m_123331_((Vec3i)clickedPos)));
        if (gate.isEmpty()) {
            player.m_5661_((Component)Component.m_237113_((String)"Ce portail de retour n'est pas actif."), true);
            return;
        }
        GateRecord record2 = gate.get();
        ArrayList playersInside = new ArrayList(level.m_45976_(ServerPlayer.class, new AABB(record2.dungeonPos).m_82377_(64.0, 16.0, 64.0)));
        for (ServerPlayer playerInside : playersInside) {
            if (record2.markRewarded(playerInside.m_20148_())) {
                GateManager.giveCompletionCoin(playerInside, record2);
            }
            GateManager.teleportToOverworld(playerInside, record2);
        }
        data.m_77762_();
        GateManager.clearDungeonSpace(level, record2.dungeonPos);
        GateManager.removeGateBlocks(level.m_7654_().m_129783_(), record2.overworldPos);
        data.removeGate(record2.id);
    }

    public static void onMobKilled(Entity entity) {
        Level level = entity.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        GateSavedData data = GateSavedData.get(level2.m_7654_());
        UUID mobId = entity.m_20148_();
        boolean dirty = false;
        for (GateRecord gate : data.gates()) {
            if (!gate.mobs.remove(mobId)) continue;
            dirty = true;
        }
        if (dirty) {
            data.m_77762_();
        }
    }

    public static void onPlayerDeath(ServerPlayer player) {
        ServerLevel level = player.m_284548_();
        if (!level.m_46472_().equals(DUNGEON_LEVEL)) {
            return;
        }
        GateSavedData data = GateSavedData.get(level.m_7654_());
        Optional<GateRecord> gate = data.gates().stream().filter(record -> record.dungeonPos.m_123331_((Vec3i)player.m_20183_()) < 10000.0).min(Comparator.comparingDouble(record -> record.dungeonPos.m_123331_((Vec3i)player.m_20183_())));
        gate.ifPresent(record -> GateManager.failGate(level, record));
    }

    public static boolean shouldCancelBreak(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        GateSavedData data = GateSavedData.get(serverLevel.m_7654_());
        if (serverLevel.m_46472_().equals(DUNGEON_LEVEL)) {
            return data.gates().stream().anyMatch(gate -> gate.dungeonPos.m_123331_((Vec3i)pos) <= 9216.0);
        }
        if (serverLevel.m_46472_().equals((Object)Level.f_46428_)) {
            return data.gates().stream().anyMatch(gate -> gate.overworldPos.m_123331_((Vec3i)pos) <= 196.0 && GateManager.isPortalStructureBlock(serverLevel.m_8055_(pos).m_60734_()));
        }
        return false;
    }

    private static Optional<GateRank> trySpawnNearPlayer(ServerLevel overworld, GateSavedData data, ServerPlayer player) {
        RandomSource random = overworld.f_46441_;
        int min = (Integer)SoloGatesConfig.SPAWN_RADIUS_MIN.get();
        int max = (Integer)SoloGatesConfig.SPAWN_RADIUS_MAX.get();
        for (int attempt = 0; attempt < 24; ++attempt) {
            int z;
            int y;
            double angle = random.m_188500_() * Math.PI * 2.0;
            int distance = min + random.m_188503_(Math.max(1, max - min));
            int x = player.m_20183_().m_123341_() + (int)Math.round(Math.cos(angle) * (double)distance);
            BlockPos pos = new BlockPos(x, y = overworld.m_6924_(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z = player.m_20183_().m_123343_() + (int)Math.round(Math.sin(angle) * (double)distance)), z);
            if (!GateManager.canPlaceGate(overworld, pos)) continue;
            boolean bossGate = random.m_188503_(100) < (Integer)SoloGatesConfig.BOSS_GATE_CHANCE_PERCENT.get();
            GateRank rank = bossGate ? GateRank.A : GateManager.randomRank(random);
            BlockPos dungeonPos = GateManager.nextDungeonPos(data);
            GateRecord gate = new GateRecord(UUID.randomUUID(), rank, pos, dungeonPos, overworld.m_46467_());
            gate.bossGate = bossGate;
            GateManager.placeGate(overworld, pos, rank);
            GateManager.buildDungeon(overworld.m_7654_().m_129880_(DUNGEON_LEVEL), gate);
            data.addGate(gate);
            overworld.m_5594_(null, pos, SoundEvents.f_12288_, SoundSource.BLOCKS, 2.0f, 0.6f);
            player.m_5661_((Component)(bossGate ? Component.m_237113_((String)"Un portail boss est apparu non loin de toi.") : rank.displayName().m_7220_((Component)Component.m_237113_((String)" est apparu non loin de toi."))), false);
            return Optional.of(rank);
        }
        return Optional.empty();
    }

    private static BlockPos nextDungeonPos(GateSavedData data) {
        int start = data.nextDungeonIndex();
        for (int offset = 0; offset < 128; ++offset) {
            int slot = Math.floorMod(start + offset, 128);
            BlockPos pos = new BlockPos(slot * 256, 80, 0);
            boolean used = data.gates().stream().anyMatch(gate -> gate.dungeonPos.equals((Object)pos));
            if (used) continue;
            return pos;
        }
        return new BlockPos(start * 256, 80, 0);
    }

    private static int rankSpawnIntervalSeconds(GateRank rank) {
        for (String entry : (List)SoloGatesConfig.RANK_SPAWN_INTERVALS.get()) {
            String[] split = entry.split("=");
            if (split.length != 2 || !split[0].equals(rank.name())) continue;
            try {
                return Math.max(60, Integer.parseInt(split[1]));
            }
            catch (NumberFormatException ignored) {
                return Math.max(60, (Integer)SoloGatesConfig.WORLD_SPAWN_INTERVAL_SECONDS.get());
            }
        }
        return Math.max(60, (Integer)SoloGatesConfig.WORLD_SPAWN_INTERVAL_SECONDS.get());
    }

    private static boolean canPlaceGate(ServerLevel level, BlockPos pos) {
        if (!level.m_7232_(new ChunkPos((BlockPos)pos).f_45578_, new ChunkPos((BlockPos)pos).f_45579_)) {
            return false;
        }
        for (int x = -6; x <= 6; ++x) {
            for (int y = 0; y <= 10; ++y) {
                for (int z = -1; z <= 3; ++z) {
                    BlockPos check = pos.m_7918_(x, y, z);
                    BlockPos floor = check.m_7495_();
                    if (y == 0 && !level.m_8055_(floor).m_60783_((BlockGetter)level, floor, Direction.UP)) {
                        return false;
                    }
                    if (y <= 0 || level.m_46859_(check)) continue;
                    return false;
                }
            }
        }
        return true;
    }

    private static GateRank randomRank(RandomSource random) {
        List entries = (List)SoloGatesConfig.RANK_WEIGHTS.get();
        int total = 0;
        ArrayList<WeightedRank> weights = new ArrayList<WeightedRank>();
        for (String entry : entries) {
            String[] split = entry.split("=");
            GateRank rank = GateRank.valueOf(split[0]);
            int weight = Integer.parseInt(split[1]);
            weights.add(new WeightedRank(rank, weight));
            total += weight;
        }
        int roll = random.m_188503_(Math.max(1, total));
        int cursor = 0;
        for (WeightedRank weighted : weights) {
            if (roll >= (cursor += weighted.weight)) continue;
            return weighted.rank;
        }
        return GateRank.E;
    }

    private static void placeGate(ServerLevel level, BlockPos base, GateRank rank) {
        for (int x = -5; x <= 5; ++x) {
            for (int y = 0; y <= 10; ++y) {
                boolean crown;
                BlockPos pos = base.m_7918_(x, y, 0);
                int absX = Math.abs(x);
                boolean portal = absX <= 1 && y >= 1 && y <= 6;
                boolean innerFrame = absX == 2 && y >= 0 && y <= 7 || absX <= 2 && (y == 0 || y == 7);
                boolean outerPillar = absX == 3 && y >= 0 && y <= 8;
                boolean sideSpike = absX == 4 && y >= 2 && y <= 8 && y % 2 == 0 || absX == 5 && y >= 4 && y <= 7 && y % 2 == 1;
                boolean bl = crown = absX <= 1 && y >= 8 && y <= 10 || absX == 2 && y == 9 || absX == 3 && y == 8;
                if (portal) {
                    level.m_46597_(pos, ModBlocks.gateBlock(rank).m_49966_());
                    continue;
                }
                if (innerFrame) {
                    level.m_46597_(pos, GateManager.frameBlock(rank));
                    continue;
                }
                if (outerPillar || crown) {
                    level.m_46597_(pos, Blocks.f_50735_.m_49966_());
                    continue;
                }
                if (!sideSpike) continue;
                level.m_46597_(pos, GateManager.accentBlock(rank));
            }
        }
        GateManager.placePortalDetails(level, base, rank);
    }

    private static void placePortalDetails(ServerLevel level, BlockPos base, GateRank rank) {
        int y;
        for (y = 2; y <= 6; y += 2) {
            level.m_46597_(base.m_7918_(-2, y, 1), GateManager.accentBlock(rank));
            level.m_46597_(base.m_7918_(2, y, 1), GateManager.accentBlock(rank));
        }
        level.m_46597_(base.m_7918_(0, 9, 1), GateManager.accentBlock(rank));
        level.m_46597_(base.m_7918_(0, 8, 1), GateManager.accentBlock(rank));
        for (y = 3; y <= 7; ++y) {
            level.m_46597_(base.m_7918_(-4, y, 0), Blocks.f_50184_.m_49966_());
            level.m_46597_(base.m_7918_(4, y, 0), Blocks.f_50184_.m_49966_());
        }
        for (int x = -3; x <= 3; ++x) {
            for (int z = 1; z <= 3; ++z) {
                boolean accent = (Math.abs(x) + z) % 3 == 0;
                level.m_46597_(base.m_7918_(x, 0, z), accent ? GateManager.accentBlock(rank) : Blocks.f_152555_.m_49966_());
            }
        }
        for (int side : new int[]{-4, 4}) {
            level.m_46597_(base.m_7918_(side, 0, 1), Blocks.f_50735_.m_49966_());
            level.m_46597_(base.m_7918_(side, 1, 1), Blocks.f_50735_.m_49966_());
            level.m_46597_(base.m_7918_(side, 2, 1), GateManager.fireBlock(rank));
            level.m_46597_(base.m_7918_(side, 0, 2), Blocks.f_50735_.m_49966_());
        }
    }

    private static BlockState frameBlock(GateRank rank) {
        return switch (rank) {
            default -> throw new IncompatibleClassChangeError();
            case GateRank.E, GateRank.C -> Blocks.f_50223_.m_49966_();
            case GateRank.B, GateRank.A -> Blocks.f_50735_.m_49966_();
            case GateRank.S -> Blocks.f_50723_.m_49966_();
        };
    }

    private static BlockState accentBlock(GateRank rank) {
        return switch (rank) {
            default -> throw new IncompatibleClassChangeError();
            case GateRank.E -> Blocks.f_152544_.m_49966_();
            case GateRank.C -> Blocks.f_50060_.m_49966_();
            case GateRank.B -> Blocks.f_152490_.m_49966_();
            case GateRank.A -> Blocks.f_50500_.m_49966_();
            case GateRank.S -> Blocks.f_50450_.m_49966_();
        };
    }

    private static BlockState fireBlock(GateRank rank) {
        return switch (rank) {
            default -> throw new IncompatibleClassChangeError();
            case GateRank.E, GateRank.C -> Blocks.f_50386_.m_49966_();
            case GateRank.B, GateRank.A -> Blocks.f_152490_.m_49966_();
            case GateRank.S -> Blocks.f_50083_.m_49966_();
        };
    }

    private static void removeGateBlocks(ServerLevel level, BlockPos base) {
        for (int x = -6; x <= 6; ++x) {
            for (int y = 0; y <= 10; ++y) {
                for (int z = 0; z <= 3; ++z) {
                    BlockPos pos = base.m_7918_(x, y, z);
                    if (!GateManager.isPortalStructureBlock(level.m_8055_(pos).m_60734_())) continue;
                    level.m_46597_(pos, Blocks.f_50016_.m_49966_());
                }
            }
        }
    }

    private static boolean isPortalStructureBlock(Block block) {
        return GateManager.isGateBlock(block) || block == Blocks.f_50723_ || block == Blocks.f_50223_ || block == Blocks.f_50735_ || block == Blocks.f_152544_ || block == Blocks.f_50060_ || block == Blocks.f_152490_ || block == Blocks.f_50500_ || block == Blocks.f_50450_ || block == Blocks.f_152555_ || block == Blocks.f_50184_ || block == Blocks.f_50682_ || block == Blocks.f_50386_ || block == Blocks.f_152492_ || block == Blocks.f_50083_;
    }

    private static boolean isGateBlock(Block block) {
        return block == ModBlocks.GATE_E.get() || block == ModBlocks.GATE_C.get() || block == ModBlocks.GATE_B.get() || block == ModBlocks.GATE_S.get();
    }

    private static void buildDungeon(ServerLevel dungeon, GateRecord gate) {
        if (dungeon == null) {
            return;
        }
        BlockPos center = gate.dungeonPos;
        GateManager.clearDungeonSpace(dungeon, center);
        List<DungeonRoom> rooms = GateManager.createRooms(dungeon.f_46441_, gate);
        for (DungeonRoom room : rooms) {
            GateManager.buildRoom(dungeon, room);
        }
        for (int i = 0; i < rooms.size() - 1; ++i) {
            GateManager.connectRooms(dungeon, rooms.get(i), rooms.get(i + 1));
        }
        for (DungeonRoom room : rooms) {
            GateManager.clearRoomInterior(dungeon, room);
            GateManager.decorateRoom(dungeon, room, gate.rank);
        }
        for (int i = 0; i < rooms.size() - 1; ++i) {
            GateManager.connectRoomsOpen(dungeon, rooms.get(i), rooms.get(i + 1));
        }
        GateManager.prepareEntrance(dungeon, center);
        if (gate.bossGate) {
            GateManager.spawnBoss(dungeon, gate, rooms.get(rooms.size() - 1));
        } else {
            GateManager.spawnDungeonMobs(dungeon, gate, rooms);
        }
    }

    private static void clearDungeonSpace(ServerLevel dungeon, BlockPos center) {
        for (int x = -64; x <= 64; ++x) {
            for (int y = -2; y <= 10; ++y) {
                for (int z = -64; z <= 64; ++z) {
                    dungeon.m_7731_(center.m_7918_(x, y, z), Blocks.f_50016_.m_49966_(), 3);
                }
            }
        }
    }

    private static List<DungeonRoom> createRooms(RandomSource random, GateRecord gate) {
        if (gate.rank == GateRank.E || gate.rank == GateRank.C) {
            return GateManager.createOverworldRooms(random, gate);
        }
        if (gate.rank == GateRank.B || gate.rank == GateRank.A) {
            return GateManager.createShadowRooms(random, gate);
        }
        int roomCount = switch (gate.rank) {
            default -> throw new IncompatibleClassChangeError();
            case GateRank.E -> 1 + random.m_188503_(2);
            case GateRank.C -> 2 + random.m_188503_(2);
            case GateRank.B -> 3 + random.m_188503_(2);
            case GateRank.A -> 4 + random.m_188503_(2);
            case GateRank.S -> 4 + random.m_188503_(2);
        };
        ArrayList<DungeonRoom> rooms = new ArrayList<DungeonRoom>();
        rooms.add(new DungeonRoom(gate.dungeonPos, 8, 8, RoomType.ENTRANCE));
        int pattern = random.m_188503_(4);
        for (int index = 1; index < roomCount; ++index) {
            BlockPos roomCenter = GateManager.patternedRoomCenter(random, gate.dungeonPos, index, pattern);
            rooms.add(new DungeonRoom(roomCenter, 5 + random.m_188503_(3), 5 + random.m_188503_(3), index == roomCount - 1 ? RoomType.BOSS : RoomType.COMBAT));
        }
        return rooms;
    }

    private static List<DungeonRoom> createShadowRooms(RandomSource random, GateRecord gate) {
        int variant = random.m_188503_(3);
        BlockPos origin = gate.dungeonPos;
        ArrayList<DungeonRoom> rooms = new ArrayList<DungeonRoom>();
        boolean aRank = gate.rank == GateRank.A;
        switch (variant) {
            case 0: {
                rooms.add(new DungeonRoom(origin, 8, 8, RoomType.ENTRANCE));
                rooms.add(new DungeonRoom(origin.m_7918_(0, 0, 22), 9, 7, RoomType.COMBAT));
                rooms.add(new DungeonRoom(origin.m_7918_(22, 0, 22), 7, 8, RoomType.CHAINS));
                rooms.add(new DungeonRoom(origin.m_7918_(-22, 0, 22), 8, 8, RoomType.CRYSTAL));
                if (!aRank) break;
                rooms.add(new DungeonRoom(origin.m_7918_(0, 0, 46), 9, 9, RoomType.RITUAL));
                break;
            }
            case 1: {
                rooms.add(new DungeonRoom(origin, 8, 8, RoomType.ENTRANCE));
                rooms.add(new DungeonRoom(origin.m_7918_(20, 0, 0), 8, 7, RoomType.CHAINS));
                rooms.add(new DungeonRoom(origin.m_7918_(40, 0, 0), 9, 8, RoomType.COMBAT));
                rooms.add(new DungeonRoom(origin.m_7918_(40, 0, 22), 8, 8, RoomType.CRYSTAL));
                if (!aRank) break;
                rooms.add(new DungeonRoom(origin.m_7918_(18, 0, 36), 9, 9, RoomType.THRONE));
                break;
            }
            default: {
                rooms.add(new DungeonRoom(origin, 7, 9, RoomType.ENTRANCE));
                rooms.add(new DungeonRoom(origin.m_7918_(-20, 0, 18), 8, 8, RoomType.CRYSTAL));
                rooms.add(new DungeonRoom(origin.m_7918_(20, 0, 18), 8, 8, RoomType.CHAINS));
                rooms.add(new DungeonRoom(origin.m_7918_(0, 0, 38), 10, 8, RoomType.RITUAL));
                if (!aRank) break;
                rooms.add(new DungeonRoom(origin.m_7918_(0, 0, 60), 9, 9, RoomType.THRONE));
            }
        }
        return rooms;
    }

    private static List<DungeonRoom> createOverworldRooms(RandomSource random, GateRecord gate) {
        int variant = random.m_188503_(3);
        BlockPos origin = gate.dungeonPos;
        ArrayList<DungeonRoom> rooms = new ArrayList<DungeonRoom>();
        if (gate.rank == GateRank.E) {
            switch (variant) {
                case 0: {
                    rooms.add(new DungeonRoom(origin, 7, 7, RoomType.ENTRANCE));
                    rooms.add(new DungeonRoom(origin.m_7918_(0, 0, 20), 8, 6, RoomType.COMBAT));
                    rooms.add(new DungeonRoom(origin.m_7918_(0, 0, 38), 6, 6, RoomType.TREASURE));
                    break;
                }
                case 1: {
                    rooms.add(new DungeonRoom(origin, 7, 7, RoomType.ENTRANCE));
                    rooms.add(new DungeonRoom(origin.m_7918_(20, 0, 0), 7, 7, RoomType.COMBAT));
                    rooms.add(new DungeonRoom(origin.m_7918_(-20, 0, 0), 6, 8, RoomType.PUZZLE));
                    rooms.add(new DungeonRoom(origin.m_7918_(0, 0, 22), 6, 6, RoomType.TREASURE));
                    break;
                }
                default: {
                    rooms.add(new DungeonRoom(origin, 6, 9, RoomType.ENTRANCE));
                    rooms.add(new DungeonRoom(origin.m_7918_(18, 0, 16), 8, 6, RoomType.COMBAT));
                    rooms.add(new DungeonRoom(origin.m_7918_(-18, 0, 16), 6, 6, RoomType.TREASURE));
                    break;
                }
            }
        } else {
            switch (variant) {
                case 0: {
                    rooms.add(new DungeonRoom(origin, 8, 8, RoomType.ENTRANCE));
                    rooms.add(new DungeonRoom(origin.m_7918_(0, 0, 22), 9, 8, RoomType.COMBAT));
                    rooms.add(new DungeonRoom(origin.m_7918_(22, 0, 22), 7, 7, RoomType.PUZZLE));
                    rooms.add(new DungeonRoom(origin.m_7918_(0, 0, 44), 8, 7, RoomType.TREASURE));
                    break;
                }
                case 1: {
                    rooms.add(new DungeonRoom(origin, 8, 8, RoomType.ENTRANCE));
                    rooms.add(new DungeonRoom(origin.m_7918_(22, 0, 0), 8, 8, RoomType.COMBAT));
                    rooms.add(new DungeonRoom(origin.m_7918_(-22, 0, 0), 7, 9, RoomType.PUZZLE));
                    rooms.add(new DungeonRoom(origin.m_7918_(0, 0, 24), 9, 7, RoomType.COMBAT));
                    rooms.add(new DungeonRoom(origin.m_7918_(0, 0, 44), 7, 7, RoomType.TREASURE));
                    break;
                }
                default: {
                    rooms.add(new DungeonRoom(origin, 7, 9, RoomType.ENTRANCE));
                    rooms.add(new DungeonRoom(origin.m_7918_(0, 0, 22), 10, 7, RoomType.PUZZLE));
                    rooms.add(new DungeonRoom(origin.m_7918_(24, 0, 22), 8, 8, RoomType.COMBAT));
                    rooms.add(new DungeonRoom(origin.m_7918_(24, 0, 44), 8, 7, RoomType.TREASURE));
                }
            }
        }
        return rooms;
    }

    private static BlockPos patternedRoomCenter(RandomSource random, BlockPos origin, int index, int pattern) {
        int distance = 18 + index * 7;
        return switch (pattern) {
            case 0 -> origin.m_7918_(index % 2 == 0 ? distance : -distance, 0, random.m_188503_(15) - 7);
            case 1 -> origin.m_7918_(random.m_188503_(15) - 7, 0, index % 2 == 0 ? distance : -distance);
            case 2 -> {
                switch (index % 4) {
                    case 0: {
                        yield origin.m_7918_(distance, 0, 0);
                    }
                    case 1: {
                        yield origin.m_7918_(0, 0, distance);
                    }
                    case 2: {
                        yield origin.m_7918_(-distance, 0, 0);
                    }
                }
                yield origin.m_7918_(0, 0, -distance);
            }
            default -> origin.m_7918_(index * 13 - 18, 0, (index % 2 == 0 ? 18 : -18) + random.m_188503_(9) - 4);
        };
    }

    private static void buildRoom(ServerLevel dungeon, DungeonRoom room) {
        for (int x = -room.halfX; x <= room.halfX; ++x) {
            for (int y = -1; y <= 6; ++y) {
                for (int z = -room.halfZ; z <= room.halfZ; ++z) {
                    boolean wall = Math.abs(x) == room.halfX || Math.abs(z) == room.halfZ || y == -1 || y == 6;
                    dungeon.m_7731_(room.center.m_7918_(x, y, z), wall ? GateManager.dungeonWallBlock(dungeon.f_46441_) : Blocks.f_50016_.m_49966_(), 3);
                }
            }
        }
    }

    private static void clearRoomInterior(ServerLevel dungeon, DungeonRoom room) {
        int x;
        for (x = -room.halfX + 1; x <= room.halfX - 1; ++x) {
            for (int y = 0; y <= 5; ++y) {
                for (int z = -room.halfZ + 1; z <= room.halfZ - 1; ++z) {
                    dungeon.m_7731_(room.center.m_7918_(x, y, z), Blocks.f_50016_.m_49966_(), 3);
                }
            }
        }
        for (x = -room.halfX + 1; x <= room.halfX - 1; ++x) {
            for (int z = -room.halfZ + 1; z <= room.halfZ - 1; ++z) {
                dungeon.m_7731_(room.center.m_7918_(x, -1, z), Blocks.f_152555_.m_49966_(), 3);
            }
        }
    }

    private static boolean isOverworldTheme(GateRank rank) {
        return rank == GateRank.E || rank == GateRank.C;
    }

    private static boolean isShadowTheme(GateRank rank) {
        return rank == GateRank.B || rank == GateRank.A;
    }

    private static BlockState dungeonWallBlock(RandomSource random) {
        int roll = random.m_188503_(10);
        if (roll < 2) {
            return Blocks.f_152594_.m_49966_();
        }
        if (roll < 4) {
            return Blocks.f_152559_.m_49966_();
        }
        return Blocks.f_152555_.m_49966_();
    }

    private static BlockState floorAccentBlock(GateRank rank) {
        return switch (rank) {
            default -> throw new IncompatibleClassChangeError();
            case GateRank.E -> Blocks.f_50223_.m_49966_();
            case GateRank.C -> Blocks.f_50377_.m_49966_();
            case GateRank.B -> Blocks.f_152490_.m_49966_();
            case GateRank.A -> Blocks.f_50500_.m_49966_();
            case GateRank.S -> Blocks.f_50450_.m_49966_();
        };
    }

    private static BlockState pillarBlock(GateRank rank) {
        return switch (rank) {
            default -> throw new IncompatibleClassChangeError();
            case GateRank.E -> Blocks.f_50222_.m_49966_();
            case GateRank.C -> Blocks.f_152589_.m_49966_();
            case GateRank.B, GateRank.A, GateRank.S -> Blocks.f_50735_.m_49966_();
        };
    }

    private static BlockState lightBlock(GateRank rank) {
        return switch (rank) {
            default -> throw new IncompatibleClassChangeError();
            case GateRank.E -> Blocks.f_50681_.m_49966_();
            case GateRank.C -> Blocks.f_50386_.m_49966_();
            case GateRank.B, GateRank.A -> Blocks.f_152490_.m_49966_();
            case GateRank.S -> Blocks.f_50083_.m_49966_();
        };
    }

    private static void connectRooms(ServerLevel dungeon, DungeonRoom first, DungeonRoom second) {
        BlockPos cursor = first.center;
        while (cursor.m_123341_() != second.center.m_123341_()) {
            cursor = cursor.m_7918_(cursor.m_123341_() < second.center.m_123341_() ? 1 : -1, 0, 0);
            GateManager.carveCorridor(dungeon, cursor);
        }
        while (cursor.m_123343_() != second.center.m_123343_()) {
            cursor = cursor.m_7918_(0, 0, cursor.m_123343_() < second.center.m_123343_() ? 1 : -1);
            GateManager.carveCorridor(dungeon, cursor);
        }
    }

    private static void carveCorridor(ServerLevel dungeon, BlockPos center) {
        for (int x = -2; x <= 2; ++x) {
            for (int y = -1; y <= 5; ++y) {
                for (int z = -2; z <= 2; ++z) {
                    boolean shell = Math.abs(x) == 2 || Math.abs(z) == 2 || y == -1 || y == 5;
                    dungeon.m_7731_(center.m_7918_(x, y, z), shell ? Blocks.f_152589_.m_49966_() : Blocks.f_50016_.m_49966_(), 3);
                }
            }
        }
    }

    private static void connectRoomsOpen(ServerLevel dungeon, DungeonRoom first, DungeonRoom second) {
        BlockPos cursor = first.center;
        while (cursor.m_123341_() != second.center.m_123341_()) {
            cursor = cursor.m_7918_(cursor.m_123341_() < second.center.m_123341_() ? 1 : -1, 0, 0);
            GateManager.carveOpenCorridor(dungeon, cursor);
        }
        while (cursor.m_123343_() != second.center.m_123343_()) {
            cursor = cursor.m_7918_(0, 0, cursor.m_123343_() < second.center.m_123343_() ? 1 : -1);
            GateManager.carveOpenCorridor(dungeon, cursor);
        }
    }

    private static void carveOpenCorridor(ServerLevel dungeon, BlockPos center) {
        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                dungeon.m_7731_(center.m_7918_(x, -1, z), Blocks.f_152589_.m_49966_(), 3);
                for (int y = 0; y <= 4; ++y) {
                    dungeon.m_7731_(center.m_7918_(x, y, z), Blocks.f_50016_.m_49966_(), 3);
                }
            }
        }
    }

    /*
     * Exception decompiling
     */
    private static void decorateRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * java.lang.ClassCastException: class org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement cannot be cast to class org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement (org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement and org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement are in unnamed module of loader 'app')
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.SwitchExpressionRewriter$LValueSingleUsageCheckingRewriter.rewriteExpression(SwitchExpressionRewriter.java:96)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression.applyExpressionRewriter(LValueExpression.java:84)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.rewriters.AbstractExpressionRewriter.rewriteExpression(AbstractExpressionRewriter.java:14)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithmeticMonOperation.applyExpressionRewriter(ArithmeticMonOperation.java:82)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.rewriters.AbstractExpressionRewriter.rewriteExpression(AbstractExpressionRewriter.java:14)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithmeticOperation.applyExpressionRewriter(ArithmeticOperation.java:171)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.rewriters.AbstractExpressionRewriter.rewriteExpression(AbstractExpressionRewriter.java:14)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.statement.AssignmentSimple.rewriteExpressions(AssignmentSimple.java:167)
         *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredFor.rewriteExpressions(StructuredFor.java:194)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.ExpressionRewriterTransformer.transform(ExpressionRewriterTransformer.java:24)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.transform(Op04StructuredStatement.java:680)
         *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.Block.transformStructuredChildren(Block.java:421)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.ExpressionRewriterTransformer.transform(ExpressionRewriterTransformer.java:25)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.transform(Op04StructuredStatement.java:680)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.SwitchExpressionRewriter.rewriteBlockSwitches(SwitchExpressionRewriter.java:140)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.SwitchExpressionRewriter.transform(SwitchExpressionRewriter.java:71)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.switchExpression(Op04StructuredStatement.java:101)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:909)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    private static void decorateShadowRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        GateManager.decorateShadowFloor(dungeon, room, rank);
        GateManager.decorateShadowPillars(dungeon, room, rank);
        switch (room.type) {
            case ENTRANCE: 
            case COMBAT: {
                GateManager.decorateShadowCombat(dungeon, room, rank);
                break;
            }
            case CHAINS: {
                GateManager.decorateChainRoom(dungeon, room, rank);
                break;
            }
            case CRYSTAL: {
                GateManager.decorateCrystalRoom(dungeon, room, rank);
                break;
            }
            case RITUAL: {
                GateManager.decorateRitualRoom(dungeon, room, rank);
                break;
            }
            case THRONE: 
            case BOSS: {
                GateManager.decorateThroneRoom(dungeon, room, rank);
                break;
            }
            default: {
                GateManager.decorateShadowCombat(dungeon, room, rank);
            }
        }
    }

    private static void decorateShadowFloor(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int x = -room.halfX + 1; x <= room.halfX - 1; ++x) {
            for (int z = -room.halfZ + 1; z <= room.halfZ - 1; ++z) {
                int pattern = Math.abs(x * 19 + z * 23) % 13;
                if (pattern == 0) {
                    dungeon.m_7731_(room.center.m_7918_(x, -1, z), Blocks.f_50734_.m_49966_(), 3);
                    continue;
                }
                if (pattern == 1) {
                    dungeon.m_7731_(room.center.m_7918_(x, -1, z), Blocks.f_152594_.m_49966_(), 3);
                    continue;
                }
                if (pattern == 2) {
                    dungeon.m_7731_(room.center.m_7918_(x, -1, z), rank == GateRank.A ? Blocks.f_50500_.m_49966_() : Blocks.f_152490_.m_49966_(), 3);
                    continue;
                }
                dungeon.m_7731_(room.center.m_7918_(x, -1, z), Blocks.f_152589_.m_49966_(), 3);
            }
        }
    }

    private static void decorateShadowPillars(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int x : new int[]{-room.halfX + 2, room.halfX - 2}) {
            for (int z : new int[]{-room.halfZ + 2, room.halfZ - 2}) {
                for (int y = 0; y <= 3; ++y) {
                    dungeon.m_7731_(room.center.m_7918_(x, y, z), Blocks.f_50735_.m_49966_(), 3);
                }
                dungeon.m_7731_(room.center.m_7918_(x, 4, z), rank == GateRank.A ? Blocks.f_50500_.m_49966_() : Blocks.f_152490_.m_49966_(), 3);
            }
        }
    }

    private static void decorateShadowCombat(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int x = -3; x <= 3; ++x) {
            for (int z = -3; z <= 3; ++z) {
                if (Math.abs(x) != 3 && Math.abs(z) != 3 && Math.abs(x) != Math.abs(z)) continue;
                dungeon.m_7731_(room.center.m_7918_(x, -1, z), rank == GateRank.A ? Blocks.f_50500_.m_49966_() : Blocks.f_152490_.m_49966_(), 3);
            }
        }
        dungeon.m_7731_(room.center.m_7918_(0, 0, 0), Blocks.f_50684_.m_49966_(), 3);
    }

    private static void decorateChainRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int x = -room.halfX + 3; x <= room.halfX - 3; x += 3) {
            for (int z = -room.halfZ + 3; z <= room.halfZ - 3; z += 3) {
                for (int y = 4; y >= 1; --y) {
                    dungeon.m_7731_(room.center.m_7918_(x, y, z), Blocks.f_50184_.m_49966_(), 3);
                }
                dungeon.m_7731_(room.center.m_7918_(x, 0, z), Blocks.f_50183_.m_49966_(), 3);
            }
        }
        GateManager.decorateShadowCombat(dungeon, room, rank);
    }

    private static void decorateCrystalRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        int count = rank == GateRank.A ? 10 : 7;
        for (int i = 0; i < count; ++i) {
            int x = dungeon.f_46441_.m_188503_(Math.max(1, room.halfX * 2 - 4)) - room.halfX + 2;
            int z = dungeon.f_46441_.m_188503_(Math.max(1, room.halfZ * 2 - 4)) - room.halfZ + 2;
            BlockPos pos = room.center.m_7918_(x, 0, z);
            dungeon.m_7731_(pos, Blocks.f_152490_.m_49966_(), 3);
            dungeon.m_7731_(pos.m_7494_(), Blocks.f_152494_.m_49966_(), 3);
        }
    }

    private static void decorateRitualRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int x = -4; x <= 4; ++x) {
            for (int z = -4; z <= 4; ++z) {
                int dist = x * x + z * z;
                if (dist < 12 || dist > 20) continue;
                dungeon.m_7731_(room.center.m_7918_(x, -1, z), rank == GateRank.A ? Blocks.f_50500_.m_49966_() : Blocks.f_152490_.m_49966_(), 3);
            }
        }
        dungeon.m_7731_(room.center.m_7918_(0, 0, 0), Blocks.f_50201_.m_49966_(), 3);
        for (int x : new int[]{-4, 4}) {
            for (int z : new int[]{-4, 4}) {
                dungeon.m_7731_(room.center.m_7918_(x, 0, z), Blocks.f_50684_.m_49966_(), 3);
            }
        }
    }

    private static void decorateThroneRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int z = room.halfZ - 4; z <= room.halfZ - 1; ++z) {
            for (int x = -2; x <= 2; ++x) {
                dungeon.m_7731_(room.center.m_7918_(x, 0, z), Blocks.f_50735_.m_49966_(), 3);
            }
        }
        dungeon.m_7731_(room.center.m_7918_(0, 1, room.halfZ - 2), Blocks.f_50707_.m_49966_(), 3);
        dungeon.m_7731_(room.center.m_7918_(0, 2, room.halfZ - 2), rank == GateRank.A ? Blocks.f_50500_.m_49966_() : Blocks.f_152490_.m_49966_(), 3);
        GateManager.decorateShadowCombat(dungeon, room, rank);
    }

    private static void decorateOverworldRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        GateManager.decorateOverworldFloor(dungeon, room, rank);
        GateManager.decorateOverworldCorners(dungeon, room, rank);
        GateManager.decorateOverworldWalls(dungeon, room, rank);
        switch (room.type) {
            case ENTRANCE: {
                GateManager.decorateEntranceRoom(dungeon, room, rank);
                break;
            }
            case COMBAT: {
                GateManager.decorateCombatRoom(dungeon, room, rank);
                break;
            }
            case TREASURE: {
                GateManager.decorateTreasureRoom(dungeon, room, rank);
                break;
            }
            case PUZZLE: {
                GateManager.decoratePuzzleRoom(dungeon, room, rank);
                break;
            }
            case BOSS: {
                GateManager.decorateCombatRoom(dungeon, room, rank);
                break;
            }
            default: {
                GateManager.decorateCombatRoom(dungeon, room, rank);
            }
        }
    }

    private static void decorateOverworldFloor(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int x = -room.halfX + 1; x <= room.halfX - 1; ++x) {
            for (int z = -room.halfZ + 1; z <= room.halfZ - 1; ++z) {
                int pattern = Math.abs(x * 31 + z * 17) % 11;
                if (pattern == 0) {
                    dungeon.m_7731_(room.center.m_7918_(x, -1, z), Blocks.f_50223_.m_49966_(), 3);
                    continue;
                }
                if (pattern == 1) {
                    dungeon.m_7731_(room.center.m_7918_(x, -1, z), Blocks.f_50224_.m_49966_(), 3);
                    continue;
                }
                if (rank == GateRank.C && pattern == 2) {
                    dungeon.m_7731_(room.center.m_7918_(x, -1, z), Blocks.f_50377_.m_49966_(), 3);
                    continue;
                }
                dungeon.m_7731_(room.center.m_7918_(x, -1, z), Blocks.f_50222_.m_49966_(), 3);
            }
        }
    }

    private static void decorateOverworldCorners(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int x : new int[]{-room.halfX + 2, room.halfX - 2}) {
            for (int z : new int[]{-room.halfZ + 2, room.halfZ - 2}) {
                for (int y = 0; y <= 2; ++y) {
                    dungeon.m_7731_(room.center.m_7918_(x, y, z), Blocks.f_50079_.m_49966_(), 3);
                }
                dungeon.m_7731_(room.center.m_7918_(x, 3, z), rank == GateRank.C ? Blocks.f_50386_.m_49966_() : Blocks.f_50141_.m_49966_(), 3);
            }
        }
    }

    private static void decorateOverworldWalls(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int x = -room.halfX + 2; x <= room.halfX - 2; x += 4) {
            dungeon.m_7731_(room.center.m_7918_(x, 0, -room.halfZ + 1), rank == GateRank.C ? Blocks.f_50060_.m_49966_() : Blocks.f_152544_.m_49966_(), 3);
            dungeon.m_7731_(room.center.m_7918_(x, 0, room.halfZ - 1), rank == GateRank.C ? Blocks.f_50377_.m_49966_() : Blocks.f_50079_.m_49966_(), 3);
        }
        for (int z = -room.halfZ + 2; z <= room.halfZ - 2; z += 4) {
            dungeon.m_7731_(room.center.m_7918_(-room.halfX + 1, 0, z), rank == GateRank.C ? Blocks.f_50060_.m_49966_() : Blocks.f_152544_.m_49966_(), 3);
            dungeon.m_7731_(room.center.m_7918_(room.halfX - 1, 0, z), rank == GateRank.C ? Blocks.f_50377_.m_49966_() : Blocks.f_50079_.m_49966_(), 3);
        }
    }

    private static void decorateEntranceRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int z = -room.halfZ + 2; z <= room.halfZ - 2; ++z) {
            if (z % 3 != 0) continue;
            dungeon.m_7731_(room.center.m_7918_(0, -1, z), rank == GateRank.C ? Blocks.f_50377_.m_49966_() : Blocks.f_50223_.m_49966_(), 3);
        }
    }

    private static void decorateCombatRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int x = -2; x <= 2; ++x) {
            for (int z = -2; z <= 2; ++z) {
                if (Math.abs(x) != 2 && Math.abs(z) != 2) continue;
                dungeon.m_7731_(room.center.m_7918_(x, -1, z), rank == GateRank.C ? Blocks.f_50377_.m_49966_() : Blocks.f_50223_.m_49966_(), 3);
            }
        }
        dungeon.m_7731_(room.center.m_7918_(0, 0, 0), Blocks.f_50683_.m_49966_(), 3);
    }

    private static void decorateTreasureRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        dungeon.m_7731_(room.center.m_7918_(0, 0, 0), Blocks.f_50074_.m_49966_(), 3);
        dungeon.m_7731_(room.center.m_7918_(0, 1, 0), Blocks.f_50618_.m_49966_(), 3);
        for (int x : new int[]{-2, 2}) {
            for (int z : new int[]{-2, 2}) {
                dungeon.m_7731_(room.center.m_7918_(x, 0, z), rank == GateRank.C ? Blocks.f_50386_.m_49966_() : Blocks.f_50141_.m_49966_(), 3);
            }
        }
    }

    private static void decoratePuzzleRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int x = -room.halfX + 3; x <= room.halfX - 3; ++x) {
            if (x % 2 != 0) continue;
            dungeon.m_7731_(room.center.m_7918_(x, -1, 0), Blocks.f_49990_.m_49966_(), 3);
        }
        for (int z = -room.halfZ + 3; z <= room.halfZ - 3; ++z) {
            if (z % 2 != 0) continue;
            dungeon.m_7731_(room.center.m_7918_(0, -1, z), Blocks.f_49990_.m_49966_(), 3);
        }
        dungeon.m_7731_(room.center.m_7918_(0, 0, 0), rank == GateRank.C ? Blocks.f_50386_.m_49966_() : Blocks.f_50141_.m_49966_(), 3);
    }

    private static void spawnBoss(ServerLevel dungeon, GateRecord gate, DungeonRoom room) {
        for (int index = 0; index < (Integer)SoloGatesConfig.BOSS_MOB_COUNT.get(); ++index) {
            GateManager.randomEntityType((List)SoloGatesConfig.BOSS_FALLBACK_MOBS.get(), dungeon.f_46441_).ifPresent(type -> {
                Entity entity = type.m_20615_((Level)dungeon);
                if (entity instanceof Mob) {
                    Mob mob = (Mob)entity;
                    BlockPos pos = room.center.m_7918_(dungeon.f_46441_.m_188503_(7) - 3, 1, dungeon.f_46441_.m_188503_(7) - 3);
                    mob.m_7678_((double)pos.m_123341_() + 0.5, (double)pos.m_123342_(), (double)pos.m_123343_() + 0.5, dungeon.f_46441_.m_188501_() * 360.0f, 0.0f);
                    mob.m_21530_();
                    dungeon.m_7967_((Entity)mob);
                    gate.mobs.add(mob.m_20148_());
                }
            });
        }
    }

    private static void prepareEntrance(ServerLevel dungeon, BlockPos center) {
        for (int x = -3; x <= 3; ++x) {
            for (int z = -3; z <= 3; ++z) {
                dungeon.m_7731_(center.m_7918_(x, -1, z), Blocks.f_152555_.m_49966_(), 3);
                for (int y = 0; y <= 4; ++y) {
                    dungeon.m_7731_(center.m_7918_(x, y, z), Blocks.f_50016_.m_49966_(), 3);
                }
            }
        }
        dungeon.m_7731_(center.m_7918_(0, -1, 0), Blocks.f_50729_.m_49966_(), 3);
    }

    private static void spawnDungeonMobs(ServerLevel dungeon, GateRecord gate, List<DungeonRoom> rooms) {
        RandomSource random = dungeon.f_46441_;
        List<? extends String> mobs = gate.rank.mobs();
        HashMap<String, Integer> spawnedById = new HashMap<String, Integer>();
        int mobCount = gate.rank.minMobCount() + random.m_188503_(Math.max(1, gate.rank.maxMobCount() - gate.rank.minMobCount() + 1));
        for (int index = 0; index < mobCount; ++index) {
            GateManager.randomMobId(gate.rank, mobs, spawnedById, random).flatMap(EntityType::m_20632_).ifPresent(type -> {
                Entity entity = type.m_20615_((Level)dungeon);
                if (entity instanceof Mob) {
                    Mob mob = (Mob)entity;
                    String mobId = EntityType.m_20613_((EntityType)type).toString();
                    spawnedById.put(mobId, spawnedById.getOrDefault(mobId, 0) + 1);
                    DungeonRoom room = (DungeonRoom)rooms.get(random.m_188503_(rooms.size()));
                    BlockPos pos = room.center.m_7918_(random.m_188503_(Math.max(1, room.halfX * 2 - 3)) - room.halfX + 2, 1, random.m_188503_(Math.max(1, room.halfZ * 2 - 3)) - room.halfZ + 2);
                    mob.m_7678_((double)pos.m_123341_() + 0.5, (double)pos.m_123342_(), (double)pos.m_123343_() + 0.5, random.m_188501_() * 360.0f, 0.0f);
                    mob.m_21530_();
                    dungeon.m_7967_((Entity)mob);
                    gate.mobs.add(mob.m_20148_());
                }
            });
        }
    }

    private static Optional<String> randomMobId(GateRank rank, List<? extends String> mobs, Map<String, Integer> spawnedById, RandomSource random) {
        if (mobs.isEmpty()) {
            return Optional.of("minecraft:zombie");
        }
        for (int attempt = 0; attempt < 40; ++attempt) {
            String mobId = mobs.get(random.m_188503_(mobs.size()));
            if (rank == GateRank.S && GateManager.isLimitedRankSMob(mobId) && spawnedById.getOrDefault(mobId, 0) >= 1 || spawnedById.getOrDefault(mobId, 0) >= rank.maxSameMob() || !EntityType.m_20632_((String)mobId).isPresent()) continue;
            return Optional.of(mobId);
        }
        return Optional.of("minecraft:zombie");
    }

    private static boolean isLimitedRankSMob(String mobId) {
        return mobId.equals("cataclysm:ignited_revenant") || mobId.equals("mowziesmobs:ferrous_wroughtnaut");
    }

    private static Optional<EntityType<?>> randomEntityType(List<? extends String> mobs, RandomSource random) {
        if (mobs.isEmpty()) {
            return Optional.of(EntityType.f_20501_);
        }
        for (int attempt = 0; attempt < 20; ++attempt) {
            Optional type;
            String mobId = mobs.get(random.m_188503_(mobs.size()));
            if (GateManager.isBlockedBossBarEntity(mobId) || !(type = EntityType.m_20632_((String)mobId)).isPresent()) continue;
            return type;
        }
        return Optional.of(EntityType.f_20501_);
    }

    private static boolean isBlockedBossBarEntity(String mobId) {
        return mobId.equals("mowziesmobs:frostmaw") || mobId.equals("cataclysm:ender_golem") || mobId.equals("cataclysm:ender_guardian") || mobId.equals("cataclysm:netherite_monstrosity") || mobId.equals("cataclysm:ignis") || mobId.equals("cataclysm:the_harbinger") || mobId.equals("cataclysm:the_leviathan") || mobId.equals("cataclysm:ancient_remnant") || mobId.equals("cataclysm:maledictus");
    }

    private static void completeGate(ServerLevel overworld, GateRecord gate) {
        gate.completed = true;
        ServerLevel dungeon = overworld.m_7654_().m_129880_(DUNGEON_LEVEL);
        if (dungeon != null) {
            GateManager.placeReturnGate(dungeon, gate);
            for (ServerPlayer player : dungeon.m_45976_(ServerPlayer.class, new AABB(gate.dungeonPos).m_82377_(64.0, 16.0, 64.0))) {
                player.m_5661_((Component)Component.m_237113_((String)"Donjon termine. Clique sur le portail de retour pour sortir."), false);
            }
        }
        GateManager.removeGateBlocks(overworld, gate.overworldPos);
        BlockPos chestPos = gate.overworldPos.m_7918_(0, 1, 2);
        overworld.m_46597_(chestPos, (BlockState)Blocks.f_50087_.m_49966_().m_61124_((Property)ChestBlock.f_51478_, (Comparable)(overworld.f_46441_.m_188499_() ? Direction.NORTH : Direction.SOUTH)));
        BlockEntity blockEntity = overworld.m_7702_(chestPos);
        if (blockEntity instanceof ChestBlockEntity) {
            ChestBlockEntity chest = (ChestBlockEntity)blockEntity;
            GateManager.fillRewardChest(chest, gate, overworld.f_46441_);
        }
        overworld.m_5594_(null, chestPos, SoundEvents.f_12275_, SoundSource.BLOCKS, 1.4f, 0.8f);
    }

    private static void placeReturnGate(ServerLevel dungeon, GateRecord gate) {
        BlockPos base = gate.dungeonPos.m_7918_(0, 0, -5);
        for (int x = -1; x <= 1; ++x) {
            for (int y = 0; y <= 3; ++y) {
                BlockPos pos = base.m_7918_(x, y, 0);
                boolean frame = Math.abs(x) == 1 || y == 0 || y == 3;
                dungeon.m_46597_(pos, frame ? Blocks.f_50735_.m_49966_() : ModBlocks.gateBlock(gate.rank).m_49966_());
            }
        }
        BlockPos signPos = base.m_7918_(0, 1, -1);
        dungeon.m_46597_(signPos, Blocks.f_50095_.m_49966_());
        BlockEntity blockEntity = dungeon.m_7702_(signPos);
        if (blockEntity instanceof SignBlockEntity) {
            SignBlockEntity sign = (SignBlockEntity)blockEntity;
            SignText text = sign.m_277157_(true).m_276913_(0, (Component)Component.m_237113_((String)"Clique sur")).m_276913_(1, (Component)Component.m_237113_((String)"le portail")).m_276913_(2, (Component)Component.m_237113_((String)"pour sortir")).m_276913_(3, (Component)Component.m_237113_((String)"recompense"));
            sign.m_276956_(text, true);
        }
    }

    private static void giveCompletionCoin(ServerPlayer player, GateRecord gate) {
        ItemStack stack = switch (gate.rank) {
            default -> throw new IncompatibleClassChangeError();
            case GateRank.E -> new ItemStack((ItemLike)ModBlocks.BRONZE_COIN.get());
            case GateRank.C -> new ItemStack((ItemLike)ModBlocks.SILVER_COIN.get());
            case GateRank.B -> new ItemStack((ItemLike)ModBlocks.GOLD_COIN.get());
            case GateRank.A -> new ItemStack((ItemLike)ModBlocks.GOLD_COIN.get(), 2);
            case GateRank.S -> new ItemStack((ItemLike)ModBlocks.PLATINUM_COIN.get());
        };
        player.m_36356_(stack);
    }

    private static void expireGate(ServerLevel overworld, GateRecord gate) {
        ServerLevel dungeon = overworld.m_7654_().m_129880_(DUNGEON_LEVEL);
        if (dungeon != null) {
            AABB arena = new AABB(gate.dungeonPos).m_82377_(64.0, 16.0, 64.0);
            for (ServerPlayer player : dungeon.m_45976_(ServerPlayer.class, arena)) {
                GateManager.teleportToOverworld(player, gate);
                player.m_5661_((Component)Component.m_237113_((String)"Le portail s'est referme."), false);
            }
            for (UUID mobId : gate.mobs) {
                Entity mob = dungeon.m_8791_(mobId);
                if (mob == null) continue;
                mob.m_146870_();
            }
            GateManager.clearDungeonSpace(dungeon, gate.dungeonPos);
        }
        GateManager.removeGateBlocks(overworld, gate.overworldPos);
        overworld.m_5594_(null, gate.overworldPos, SoundEvents.f_12287_, SoundSource.BLOCKS, 1.0f, 0.5f);
    }

    private static void teleportToOverworld(ServerPlayer player, GateRecord gate) {
        ServerLevel overworld = player.m_20194_().m_129783_();
        BlockPos exit = gate.returnPosition(player.m_20148_()).orElse(gate.overworldPos.m_7918_(0, 1, -3));
        player.m_8999_(overworld, (double)exit.m_123341_() + 0.5, (double)exit.m_123342_(), (double)exit.m_123343_() + 0.5, player.m_146908_(), player.m_146909_());
    }

    private static void failGate(ServerLevel dungeon, GateRecord gate) {
        ServerLevel overworld = dungeon.m_7654_().m_129783_();
        gate.failed = true;
        for (UUID mobId : gate.mobs) {
            Entity original = dungeon.m_8791_(mobId);
            if (!(original instanceof Mob)) continue;
            Mob mob = (Mob)original;
            Entity copy = mob.m_6095_().m_20615_((Level)overworld);
            if (copy instanceof Mob) {
                Mob invasionMob = (Mob)copy;
                BlockPos spawn = gate.overworldPos.m_7918_(overworld.f_46441_.m_188503_(9) - 4, 1, overworld.f_46441_.m_188503_(9) - 4);
                invasionMob.m_7678_((double)spawn.m_123341_() + 0.5, (double)spawn.m_123342_(), (double)spawn.m_123343_() + 0.5, overworld.f_46441_.m_188501_() * 360.0f, 0.0f);
                invasionMob.m_21530_();
                overworld.m_7967_((Entity)invasionMob);
            }
            original.m_146870_();
        }
        AABB arena = new AABB(gate.dungeonPos).m_82377_(64.0, 16.0, 64.0);
        for (ServerPlayer player : dungeon.m_45976_(ServerPlayer.class, arena)) {
            if (player.m_21224_()) continue;
            GateManager.teleportToOverworld(player, gate);
            player.m_5661_((Component)Component.m_237113_((String)"Le donjon s'effondre."), false);
        }
        GateManager.clearDungeonSpace(dungeon, gate.dungeonPos);
        GateManager.removeGateBlocks(overworld, gate.overworldPos);
        overworld.m_5594_(null, gate.overworldPos, SoundEvents.f_12563_, SoundSource.HOSTILE, 1.0f, 1.2f);
        GateSavedData.get(dungeon.m_7654_()).removeGate(gate.id);
    }

    private static void fillRewardChest(ChestBlockEntity chest, GateRecord gate, RandomSource random) {
        GateRank rank = gate.rank;
        List rewards = gate.bossGate ? (List)SoloGatesConfig.BOSS_REWARDS.get() : rank.rewards();
        int rolls = switch (rank) {
            default -> throw new IncompatibleClassChangeError();
            case GateRank.E -> 2 + random.m_188503_(2);
            case GateRank.C -> 3 + random.m_188503_(2);
            case GateRank.B -> 4 + random.m_188503_(3);
            case GateRank.A -> 5 + random.m_188503_(3);
            case GateRank.S -> 6 + random.m_188503_(3);
        };
        for (int i = 0; i < rolls; ++i) {
            ItemStack stack = GateManager.randomReward(rewards, random);
            if (stack.m_41619_()) continue;
            chest.m_6836_(random.m_188503_(chest.m_6643_()), stack);
        }
    }

    private static ItemStack randomReward(List<? extends String> rewards, RandomSource random) {
        if (rewards.isEmpty()) {
            return ItemStack.f_41583_;
        }
        int totalWeight = 0;
        for (String string : rewards) {
            totalWeight += GateManager.rewardWeight(string);
        }
        if (totalWeight <= 0) {
            return ItemStack.f_41583_;
        }
        int roll = random.m_188503_(Math.max(1, totalWeight));
        boolean bl = false;
        for (String string : rewards) {
            if (roll >= (var4_7 += GateManager.rewardWeight(string))) continue;
            return GateManager.parseReward(string, random);
        }
        return ItemStack.f_41583_;
    }

    private static int rewardWeight(String reward) {
        String[] split = reward.split(":");
        if (split.length < 4) {
            return 0;
        }
        try {
            return split.length >= 5 ? Math.max(1, Integer.parseInt(split[4])) : 10;
        }
        catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static ItemStack parseReward(String reward, RandomSource random) {
        String[] split = reward.split(":");
        if (split.length < 4) {
            return ItemStack.f_41583_;
        }
        ResourceLocation itemId = new ResourceLocation(split[0], split[1]);
        Item item = (Item)ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null) {
            return ItemStack.f_41583_;
        }
        try {
            int min = Integer.parseInt(split[2]);
            int max = Integer.parseInt(split[3]);
            min = Math.max(1, min);
            max = Math.max(min, max);
            int count = min + random.m_188503_(Math.max(1, max - min + 1));
            return new ItemStack((ItemLike)item, count);
        }
        catch (NumberFormatException ex) {
            return ItemStack.f_41583_;
        }
    }

    private GateManager() {
    }

    private record WeightedRank(GateRank rank, int weight) {
    }

    private record DungeonRoom(BlockPos center, int halfX, int halfZ, RoomType type) {
    }

    private static enum RoomType {
        ENTRANCE,
        COMBAT,
        TREASURE,
        PUZZLE,
        BOSS,
        CHAINS,
        CRYSTAL,
        RITUAL,
        THRONE;

    }
}

