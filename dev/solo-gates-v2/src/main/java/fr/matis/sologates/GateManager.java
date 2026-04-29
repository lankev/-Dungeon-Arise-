package fr.matis.sologates;

import com.mojang.logging.LogUtils;
import fr.matis.sologates.entity.GateEntity;
import fr.matis.sologates.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public final class GateManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceKey<Level> DUNGEON_LEVEL = ResourceKey.create(
        Registries.DIMENSION, new ResourceLocation("sologates", "dungeon"));

    private static final int DUNGEON_SPACING = 256;

    // -------------------------------------------------------------------------
    // Server tick
    // -------------------------------------------------------------------------

    public static void serverTick(ServerLevel overworld) {
        GateSavedData data = GateSavedData.get(overworld.getServer());
        long gameTime = overworld.getGameTime();
        List<UUID> toRemove = new ArrayList<>();

        for (GateRecord gate : data.gates()) {
            if (gate.failed) { toRemove.add(gate.id); continue; }

            if (gate.completed) {
                long completedLifetime = 2400L;
                ServerLevel dungeon = overworld.getServer().getLevel(DUNGEON_LEVEL);
                boolean hasPlayers = dungeon != null &&
                    !dungeon.getEntitiesOfClass(ServerPlayer.class,
                        new AABB(gate.dungeonPos).inflate(64, 16, 64)).isEmpty();
                if (!hasPlayers && overworld.getGameTime() - gate.createdTick > completedLifetime) {
                    removeGateBlocks(overworld, gate.overworldPos);
                    if (dungeon != null) clearDungeonSpace(dungeon, gate.dungeonPos);
                    toRemove.add(gate.id);
                }
                continue;
            }

            if (gate.mobs.isEmpty()) {
                completeGate(overworld, gate);
                continue;
            }

            long lifetime = Math.min(SoloGatesConfig.GATE_LIFETIME_SECONDS.get(), 300) * 20L;
            if (gameTime - gate.createdTick > lifetime) {
                expireGate(overworld, gate);
                toRemove.add(gate.id);
            }
        }
        toRemove.forEach(data::removeGate);

        // Attempt to spawn a new gate
        if (gameTime < data.nextWorldSpawnTick() ||
            data.gates().size() >= SoloGatesConfig.MAX_ACTIVE_GATES.get()) return;

        List<ServerPlayer> candidates = overworld.players().stream()
            .filter(p -> !p.isSpectator() && !p.isSleeping())
            .filter(p -> gameTime >= data.playerCooldown(p.getUUID()))
            .toList();

        if (candidates.isEmpty()) {
            data.setNextWorldSpawnTick(gameTime + 1200L);
            return;
        }

        ServerPlayer player = candidates.get(overworld.random.nextInt(candidates.size()));
        Optional<GateRank> spawnedRank = trySpawnNearPlayer(overworld, data, player);
        if (spawnedRank.isPresent()) {
            data.setNextWorldSpawnTick(gameTime + (long)(rankSpawnIntervalSeconds(spawnedRank.get()) * 20));
            data.setPlayerCooldown(player.getUUID(), gameTime + (long)(SoloGatesConfig.PLAYER_COOLDOWN_SECONDS.get() * 20));
        } else {
            data.setNextWorldSpawnTick(gameTime + 1200L);
        }
    }

    /** Called every 30 seconds: send mob count to players inside dungeons. */
    public static void sendMobCountUpdates(ServerLevel overworld) {
        ServerLevel dungeon = overworld.getServer().getLevel(DUNGEON_LEVEL);
        if (dungeon == null) return;
        GateSavedData data = GateSavedData.get(overworld.getServer());
        for (GateRecord gate : data.gates()) {
            if (gate.completed || gate.failed || gate.mobs.isEmpty()) continue;
            List<ServerPlayer> inside = dungeon.getEntitiesOfClass(ServerPlayer.class,
                new AABB(gate.dungeonPos).inflate(64, 16, 64));
            if (inside.isEmpty()) continue;
            int remaining = gate.mobs.size();
            Component msg = Component.translatable("sologates.message.mob_count",
                Component.literal(gate.rank.name()).withStyle(gate.rank.color()),
                Component.literal(String.valueOf(remaining)).withStyle(ChatFormatting.WHITE));
            for (ServerPlayer p : inside) {
                p.displayClientMessage(msg, true);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Gate interaction
    // -------------------------------------------------------------------------

    public static void useGate(ServerPlayer player, BlockPos clickedPos) {
        if (player.serverLevel().dimension().equals(DUNGEON_LEVEL)) {
            useReturnGate(player, clickedPos);
        } else {
            enterGate(player, clickedPos);
        }
    }

    public static void enterGate(ServerPlayer player, BlockPos clickedPos) {
        ServerLevel overworld = player.serverLevel();
        GateSavedData data = GateSavedData.get(overworld.getServer());
        Optional<GateRecord> gateOpt = data.gates().stream()
            .filter(r -> r.overworldPos.distSqr(clickedPos) <= 36.0)
            .min(Comparator.comparingDouble(r -> r.overworldPos.distSqr(clickedPos)));

        if (gateOpt.isEmpty()) {
            player.displayClientMessage(Component.translatable("sologates.message.gate_not_stabilized"), true);
            return;
        }
        ServerLevel dungeon = overworld.getServer().getLevel(DUNGEON_LEVEL);
        if (dungeon == null) {
            player.displayClientMessage(Component.translatable("sologates.message.dimension_not_loaded"), false);
            return;
        }
        GateRecord record = gateOpt.get();
        record.setReturnPosition(player.getUUID(), player.blockPosition());
        data.setDirty();

        player.teleportTo(dungeon,
            record.dungeonPos.getX() + 0.5,
            record.dungeonPos.getY() + 0.15,
            record.dungeonPos.getZ() + 0.5,
            player.getYRot(), player.getXRot());

        // Announce dungeon info
        int mobs = record.mobs.size();
        long timeLeft = Math.min(SoloGatesConfig.GATE_LIFETIME_SECONDS.get(), 300) * 20L
            - (overworld.getGameTime() - record.createdTick);
        long secsLeft = timeLeft / 20;
        String infoKey = record.bossGate ? "sologates.message.boss_gate_info" : "sologates.message.gate_info";
        player.displayClientMessage(
            Component.translatable(infoKey, record.rank.displayName(),
                Component.literal(String.valueOf(mobs)),
                Component.literal(String.valueOf(secsLeft))), false);
    }

    public static boolean spawnManualGate(ServerPlayer player, GateRank rank) {
        ServerLevel overworld = player.serverLevel();
        if (!overworld.dimension().equals(Level.OVERWORLD)) {
            player.displayClientMessage(Component.translatable("sologates.message.gate_must_be_overworld"), false);
            return false;
        }
        BlockPos base = player.blockPosition().relative(player.getDirection(), 5);
        int y = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base.getX(), base.getZ());
        BlockPos pos = new BlockPos(base.getX(), y, base.getZ());
        if (!canPlaceGate(overworld, pos)) return false;

        GateSavedData data = GateSavedData.get(overworld.getServer());
        BlockPos dungeonPos = nextDungeonPos(data);
        GateRecord gate = new GateRecord(UUID.randomUUID(), rank, pos, dungeonPos, overworld.getGameTime());
        placeGate(overworld, pos, rank);
        buildDungeon(overworld.getServer().getLevel(DUNGEON_LEVEL), gate);
        data.addGate(gate);
        overworld.playSound(null, pos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 2f, 0.6f);
        player.displayClientMessage(Component.translatable("sologates.message.gate_created", rank.displayName()), false);
        return true;
    }

    public static boolean spawnManualBossGate(ServerPlayer player) {
        ServerLevel overworld = player.serverLevel();
        if (!overworld.dimension().equals(Level.OVERWORLD)) {
            player.displayClientMessage(Component.translatable("sologates.message.boss_gate_must_be_overworld"), false);
            return false;
        }
        BlockPos base = player.blockPosition().relative(player.getDirection(), 5);
        int y = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base.getX(), base.getZ());
        BlockPos pos = new BlockPos(base.getX(), y, base.getZ());
        if (!canPlaceGate(overworld, pos)) return false;

        GateSavedData data = GateSavedData.get(overworld.getServer());
        GateRecord gate = new GateRecord(UUID.randomUUID(), GateRank.S, pos,
            nextDungeonPos(data), overworld.getGameTime());
        gate.bossGate = true;
        placeGate(overworld, pos, GateRank.S);
        buildDungeon(overworld.getServer().getLevel(DUNGEON_LEVEL), gate);
        data.addGate(gate);
        overworld.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 0.8f, 1.5f);
        player.displayClientMessage(Component.translatable("sologates.message.boss_gate_created"), false);
        return true;
    }

    public static boolean leaveDungeon(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (!level.dimension().equals(DUNGEON_LEVEL)) return false;
        GateSavedData data = GateSavedData.get(level.getServer());
        Optional<GateRecord> gate = data.gates().stream()
            .filter(r -> r.dungeonPos.distSqr(player.blockPosition()) < 10000.0)
            .min(Comparator.comparingDouble(r -> r.dungeonPos.distSqr(player.blockPosition())));
        if (gate.isEmpty()) return false;
        teleportToOverworld(player, gate.get());
        return true;
    }

    private static void useReturnGate(ServerPlayer player, BlockPos clickedPos) {
        ServerLevel level = player.serverLevel();
        GateSavedData data = GateSavedData.get(level.getServer());
        BlockPos target = clickedPos; // clicked on the return portal
        Optional<GateRecord> gateOpt = data.gates().stream()
            .filter(r -> r.completed)
            .filter(r -> r.dungeonPos.offset(0, 1, -5).distSqr(clickedPos) <= 36.0)
            .min(Comparator.comparingDouble(r -> r.dungeonPos.offset(0, 1, -5).distSqr(clickedPos)));

        if (gateOpt.isEmpty()) {
            player.displayClientMessage(Component.translatable("sologates.message.return_gate_inactive"), true);
            return;
        }
        GateRecord record = gateOpt.get();
        List<ServerPlayer> inside = new ArrayList<>(level.getEntitiesOfClass(ServerPlayer.class,
            new AABB(record.dungeonPos).inflate(64, 16, 64)));
        for (ServerPlayer p : inside) {
            if (record.markRewarded(p.getUUID())) {
                giveCompletionCoin(p, record);
                giveXpReward(p, record.rank);
            }
            teleportToOverworld(p, record);
        }
        data.setDirty();
        clearDungeonSpace(level, record.dungeonPos);
        removeGateBlocks(level.getServer().overworld(), record.overworldPos);
        data.removeGate(record.id);
    }

    // -------------------------------------------------------------------------
    // Mob events
    // -------------------------------------------------------------------------

    public static void onMobKilled(Entity entity) {
        Level level = entity.level();
        if (!(level instanceof ServerLevel serverLevel)) return;
        GateSavedData data = GateSavedData.get(serverLevel.getServer());
        UUID mobId = entity.getUUID();
        for (GateRecord gate : data.gates()) {
            if (gate.mobs.remove(mobId)) {
                // Track kill in player stats for players inside this dungeon
                ServerLevel dungeon = serverLevel.getServer().getLevel(DUNGEON_LEVEL);
                if (dungeon != null && serverLevel.dimension().equals(DUNGEON_LEVEL)) {
                    PlayerSavedData psd = PlayerSavedData.get(serverLevel.getServer());
                    List<ServerPlayer> inside = dungeon.getEntitiesOfClass(ServerPlayer.class,
                        new AABB(gate.dungeonPos).inflate(64, 16, 64));
                    for (ServerPlayer p : inside) {
                        psd.getOrCreate(p.getUUID()).recordKills(gate.rank, 1);
                    }
                    psd.markDirty();
                }
                data.setDirty();
                break;
            }
        }
    }

    public static void onPlayerDeath(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (!level.dimension().equals(DUNGEON_LEVEL)) return;
        GateSavedData data = GateSavedData.get(level.getServer());
        data.gates().stream()
            .filter(r -> r.dungeonPos.distSqr(player.blockPosition()) < 10000.0)
            .min(Comparator.comparingDouble(r -> r.dungeonPos.distSqr(player.blockPosition())))
            .ifPresent(gate -> failGate(level, gate));
    }

    public static boolean shouldCancelBreak(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return false;
        if (!serverLevel.dimension().equals(DUNGEON_LEVEL)) return false;
        GateSavedData data = GateSavedData.get(serverLevel.getServer());
        return data.gates().stream().anyMatch(g -> g.dungeonPos.distSqr(pos) <= 9216.0);
    }

    // -------------------------------------------------------------------------
    // Spawning logic
    // -------------------------------------------------------------------------

    private static Optional<GateRank> trySpawnNearPlayer(ServerLevel overworld, GateSavedData data, ServerPlayer player) {
        RandomSource random = overworld.random;
        int min = SoloGatesConfig.SPAWN_RADIUS_MIN.get();
        int max = SoloGatesConfig.SPAWN_RADIUS_MAX.get();
        for (int attempt = 0; attempt < 24; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            int distance = min + random.nextInt(Math.max(1, max - min));
            int x = player.blockPosition().getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = player.blockPosition().getZ() + (int) Math.round(Math.sin(angle) * distance);
            int y = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (!canPlaceGate(overworld, pos)) continue;

            boolean bossGate = random.nextInt(100) < SoloGatesConfig.BOSS_GATE_CHANCE_PERCENT.get();
            GateRank rank = bossGate ? GateRank.S : randomRank(random);
            BlockPos dungeonPos = nextDungeonPos(data);
            GateRecord gate = new GateRecord(UUID.randomUUID(), rank, pos, dungeonPos, overworld.getGameTime());
            gate.bossGate = bossGate;
            placeGate(overworld, pos, rank);
            buildDungeon(overworld.getServer().getLevel(DUNGEON_LEVEL), gate);
            data.addGate(gate);
            overworld.playSound(null, pos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 2f, 0.6f);

            Component msg = bossGate
                ? Component.translatable("sologates.message.boss_gate_appeared").withStyle(ChatFormatting.RED)
                : Component.translatable("sologates.message.gate_appeared", rank.displayName());
            player.displayClientMessage(msg, false);
            return Optional.of(rank);
        }
        return Optional.empty();
    }

    private static BlockPos nextDungeonPos(GateSavedData data) {
        int start = data.nextDungeonIndex();
        for (int offset = 0; offset < 128; offset++) {
            int slot = Math.floorMod(start + offset, 128);
            BlockPos pos = new BlockPos(slot * DUNGEON_SPACING, 80, 0);
            boolean used = data.gates().stream().anyMatch(g -> g.dungeonPos.equals(pos));
            if (!used) return pos;
        }
        return new BlockPos(start * DUNGEON_SPACING, 80, 0);
    }

    private static int rankSpawnIntervalSeconds(GateRank rank) {
        for (String entry : SoloGatesConfig.RANK_SPAWN_INTERVALS.get()) {
            String[] split = entry.split("=");
            if (split.length == 2 && split[0].equals(rank.name())) {
                try { return Math.max(60, Integer.parseInt(split[1])); }
                catch (NumberFormatException ignored) {}
            }
        }
        return Math.max(60, SoloGatesConfig.WORLD_SPAWN_INTERVAL_SECONDS.get());
    }

    private static boolean canPlaceGate(ServerLevel level, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        if (!level.hasChunk(chunkPos.x, chunkPos.z)) return false;
        for (int x = -6; x <= 6; x++) {
            for (int y = 0; y <= 10; y++) {
                for (int z = -1; z <= 3; z++) {
                    BlockPos check = pos.offset(x, y, z);
                    BlockPos floor = check.below();
                    if (y == 0 && !level.getBlockState(floor).isFaceSturdy((BlockGetter) level, floor, Direction.UP)) return false;
                    if (y > 0 && !level.isEmptyBlock(check)) return false;
                }
            }
        }
        return true;
    }

    private static GateRank randomRank(RandomSource random) {
        List<? extends String> entries = SoloGatesConfig.RANK_WEIGHTS.get();
        int total = 0;
        List<WeightedRank> weights = new ArrayList<>();
        for (String entry : entries) {
            String[] split = entry.split("=");
            if (split.length != 2) continue;
            try {
                GateRank rank = GateRank.valueOf(split[0]);
                int weight = Integer.parseInt(split[1]);
                weights.add(new WeightedRank(rank, weight));
                total += weight;
            } catch (Exception ignored) {}
        }
        if (total == 0) return GateRank.E;
        int roll = random.nextInt(total);
        int cursor = 0;
        for (WeightedRank w : weights) {
            cursor += w.weight();
            if (roll < cursor) return w.rank();
        }
        return GateRank.E;
    }

    // -------------------------------------------------------------------------
    // Gate structure placement
    // -------------------------------------------------------------------------

    private static void placeGate(ServerLevel level, BlockPos base, GateRank rank) {
        GateEntity gateEntity = GateEntity.create(level, rank, base.offset(0, 1, 0));
        level.addFreshEntity(gateEntity);
    }

    private static void removeGateBlocks(ServerLevel level, BlockPos base) {
        level.getEntitiesOfClass(GateEntity.class, new AABB(base).inflate(6, 10, 6))
             .forEach(Entity::discard);
    }

    // -------------------------------------------------------------------------
    // Dungeon building
    // -------------------------------------------------------------------------

    private static void buildDungeon(ServerLevel dungeon, GateRecord gate) {
        if (dungeon == null) return;
        BlockPos center = gate.dungeonPos;
        clearDungeonSpace(dungeon, center);
        List<DungeonRoom> rooms = createRooms(dungeon.random, gate);
        for (DungeonRoom room : rooms) buildRoom(dungeon, room);
        for (int i = 0; i < rooms.size() - 1; i++) connectRooms(dungeon, rooms.get(i), rooms.get(i + 1));
        for (DungeonRoom room : rooms) {
            clearRoomInterior(dungeon, room);
            decorateRoom(dungeon, room, gate.rank);
        }
        for (int i = 0; i < rooms.size() - 1; i++) connectRoomsOpen(dungeon, rooms.get(i), rooms.get(i + 1));
        prepareEntrance(dungeon, center);
        if (gate.bossGate) {
            spawnBoss(dungeon, gate, rooms.get(rooms.size() - 1));
        } else {
            spawnDungeonMobs(dungeon, gate, rooms);
        }
    }

    private static void clearDungeonSpace(ServerLevel dungeon, BlockPos center) {
        for (int x = -64; x <= 64; x++) {
            for (int y = -2; y <= 10; y++) {
                for (int z = -64; z <= 64; z++) {
                    dungeon.setBlock(center.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    private static List<DungeonRoom> createRooms(RandomSource random, GateRecord gate) {
        if (gate.rank == GateRank.E || gate.rank == GateRank.C) {
            return createOverworldRooms(random, gate);
        }
        if (gate.rank == GateRank.B || gate.rank == GateRank.A) {
            return createShadowRooms(random, gate);
        }
        // S rank — large shadow dungeon
        int roomCount = 4 + random.nextInt(2);
        List<DungeonRoom> rooms = new ArrayList<>();
        rooms.add(new DungeonRoom(gate.dungeonPos, 8, 8, RoomType.ENTRANCE));
        int pattern = random.nextInt(4);
        for (int i = 1; i < roomCount; i++) {
            BlockPos rc = patternedRoomCenter(random, gate.dungeonPos, i, pattern);
            rooms.add(new DungeonRoom(rc, 5 + random.nextInt(3), 5 + random.nextInt(3),
                i == roomCount - 1 ? RoomType.BOSS : RoomType.COMBAT));
        }
        return rooms;
    }

    private static List<DungeonRoom> createShadowRooms(RandomSource random, GateRecord gate) {
        int variant = random.nextInt(3);
        BlockPos o = gate.dungeonPos;
        boolean aRank = gate.rank == GateRank.A;
        List<DungeonRoom> rooms = new ArrayList<>();
        switch (variant) {
            case 0 -> {
                rooms.add(new DungeonRoom(o, 8, 8, RoomType.ENTRANCE));
                rooms.add(new DungeonRoom(o.offset(0, 0, 22), 9, 7, RoomType.COMBAT));
                rooms.add(new DungeonRoom(o.offset(22, 0, 22), 7, 8, RoomType.CHAINS));
                rooms.add(new DungeonRoom(o.offset(-22, 0, 22), 8, 8, RoomType.CRYSTAL));
                if (aRank) rooms.add(new DungeonRoom(o.offset(0, 0, 46), 9, 9, RoomType.RITUAL));
            }
            case 1 -> {
                rooms.add(new DungeonRoom(o, 8, 8, RoomType.ENTRANCE));
                rooms.add(new DungeonRoom(o.offset(20, 0, 0), 8, 7, RoomType.CHAINS));
                rooms.add(new DungeonRoom(o.offset(40, 0, 0), 9, 8, RoomType.COMBAT));
                rooms.add(new DungeonRoom(o.offset(40, 0, 22), 8, 8, RoomType.CRYSTAL));
                if (aRank) rooms.add(new DungeonRoom(o.offset(18, 0, 36), 9, 9, RoomType.THRONE));
            }
            default -> {
                rooms.add(new DungeonRoom(o, 7, 9, RoomType.ENTRANCE));
                rooms.add(new DungeonRoom(o.offset(-20, 0, 18), 8, 8, RoomType.CRYSTAL));
                rooms.add(new DungeonRoom(o.offset(20, 0, 18), 8, 8, RoomType.CHAINS));
                rooms.add(new DungeonRoom(o.offset(0, 0, 38), 10, 8, RoomType.RITUAL));
                if (aRank) rooms.add(new DungeonRoom(o.offset(0, 0, 60), 9, 9, RoomType.THRONE));
            }
        }
        return rooms;
    }

    private static List<DungeonRoom> createOverworldRooms(RandomSource random, GateRecord gate) {
        int variant = random.nextInt(3);
        BlockPos o = gate.dungeonPos;
        List<DungeonRoom> rooms = new ArrayList<>();
        if (gate.rank == GateRank.E) {
            switch (variant) {
                case 0 -> {
                    rooms.add(new DungeonRoom(o, 7, 7, RoomType.ENTRANCE));
                    rooms.add(new DungeonRoom(o.offset(0, 0, 20), 8, 6, RoomType.COMBAT));
                    rooms.add(new DungeonRoom(o.offset(0, 0, 38), 6, 6, RoomType.TREASURE));
                }
                case 1 -> {
                    rooms.add(new DungeonRoom(o, 7, 7, RoomType.ENTRANCE));
                    rooms.add(new DungeonRoom(o.offset(20, 0, 0), 7, 7, RoomType.COMBAT));
                    rooms.add(new DungeonRoom(o.offset(-20, 0, 0), 6, 8, RoomType.PUZZLE));
                    rooms.add(new DungeonRoom(o.offset(0, 0, 22), 6, 6, RoomType.TREASURE));
                }
                default -> {
                    rooms.add(new DungeonRoom(o, 6, 9, RoomType.ENTRANCE));
                    rooms.add(new DungeonRoom(o.offset(18, 0, 16), 8, 6, RoomType.COMBAT));
                    rooms.add(new DungeonRoom(o.offset(-18, 0, 16), 6, 6, RoomType.TREASURE));
                }
            }
        } else { // C rank
            switch (variant) {
                case 0 -> {
                    rooms.add(new DungeonRoom(o, 8, 8, RoomType.ENTRANCE));
                    rooms.add(new DungeonRoom(o.offset(0, 0, 22), 9, 8, RoomType.COMBAT));
                    rooms.add(new DungeonRoom(o.offset(22, 0, 22), 7, 7, RoomType.PUZZLE));
                    rooms.add(new DungeonRoom(o.offset(0, 0, 44), 8, 7, RoomType.TREASURE));
                }
                case 1 -> {
                    rooms.add(new DungeonRoom(o, 8, 8, RoomType.ENTRANCE));
                    rooms.add(new DungeonRoom(o.offset(22, 0, 0), 8, 8, RoomType.COMBAT));
                    rooms.add(new DungeonRoom(o.offset(-22, 0, 0), 7, 9, RoomType.PUZZLE));
                    rooms.add(new DungeonRoom(o.offset(0, 0, 24), 9, 7, RoomType.COMBAT));
                    rooms.add(new DungeonRoom(o.offset(0, 0, 44), 7, 7, RoomType.TREASURE));
                }
                default -> {
                    rooms.add(new DungeonRoom(o, 7, 9, RoomType.ENTRANCE));
                    rooms.add(new DungeonRoom(o.offset(0, 0, 22), 10, 7, RoomType.PUZZLE));
                    rooms.add(new DungeonRoom(o.offset(24, 0, 22), 8, 8, RoomType.COMBAT));
                    rooms.add(new DungeonRoom(o.offset(24, 0, 44), 8, 7, RoomType.TREASURE));
                }
            }
        }
        return rooms;
    }

    private static BlockPos patternedRoomCenter(RandomSource random, BlockPos origin, int index, int pattern) {
        int distance = 18 + index * 7;
        return switch (pattern) {
            case 0 -> origin.offset(index % 2 == 0 ? distance : -distance, 0, random.nextInt(15) - 7);
            case 1 -> origin.offset(random.nextInt(15) - 7, 0, index % 2 == 0 ? distance : -distance);
            case 2 -> switch (index % 4) {
                case 0 -> origin.offset(distance, 0, 0);
                case 1 -> origin.offset(0, 0, distance);
                case 2 -> origin.offset(-distance, 0, 0);
                default -> origin.offset(0, 0, -distance);
            };
            default -> origin.offset(index * 13 - 18, 0, (index % 2 == 0 ? 18 : -18) + random.nextInt(9) - 4);
        };
    }

    private static void buildRoom(ServerLevel dungeon, DungeonRoom room) {
        for (int x = -room.halfX(); x <= room.halfX(); x++) {
            for (int y = -1; y <= 6; y++) {
                for (int z = -room.halfZ(); z <= room.halfZ(); z++) {
                    boolean wall = Math.abs(x) == room.halfX() || Math.abs(z) == room.halfZ()
                        || y == -1 || y == 6;
                    dungeon.setBlock(room.center().offset(x, y, z),
                        wall ? dungeonWallBlock(dungeon.random) : Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    private static void clearRoomInterior(ServerLevel dungeon, DungeonRoom room) {
        for (int x = -room.halfX() + 1; x <= room.halfX() - 1; x++) {
            for (int y = 0; y <= 5; y++) {
                for (int z = -room.halfZ() + 1; z <= room.halfZ() - 1; z++) {
                    dungeon.setBlock(room.center().offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        for (int x = -room.halfX() + 1; x <= room.halfX() - 1; x++) {
            for (int z = -room.halfZ() + 1; z <= room.halfZ() - 1; z++) {
                dungeon.setBlock(room.center().offset(x, -1, z), Blocks.DEEPSLATE_TILES.defaultBlockState(), 3);
            }
        }
    }

    private static boolean isOverworldTheme(GateRank rank) { return rank == GateRank.E || rank == GateRank.C; }

    private static BlockState dungeonWallBlock(RandomSource random) {
        int roll = random.nextInt(10);
        if (roll < 2) return Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        if (roll < 4) return Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState();
        return Blocks.DEEPSLATE_TILES.defaultBlockState();
    }

    private static BlockState floorAccentBlock(GateRank rank) {
        return switch (rank) {
            case E -> Blocks.COBBLESTONE.defaultBlockState();
            case C -> Blocks.MOSSY_COBBLESTONE.defaultBlockState();
            case B -> Blocks.AMETHYST_BLOCK.defaultBlockState();
            case A -> Blocks.PURPUR_BLOCK.defaultBlockState();
            case S -> Blocks.NETHER_WART_BLOCK.defaultBlockState();
        };
    }

    private static BlockState pillarBlock(GateRank rank) {
        return switch (rank) {
            case E -> Blocks.OAK_LOG.defaultBlockState();
            case C -> Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
            default -> Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        };
    }

    private static BlockState lightBlock(GateRank rank) {
        return switch (rank) {
            case E -> Blocks.TORCH.defaultBlockState();
            case C -> Blocks.FIRE.defaultBlockState();
            case B, A -> Blocks.AMETHYST_BLOCK.defaultBlockState();
            case S -> Blocks.SOUL_FIRE.defaultBlockState();
        };
    }

    // -------------------------------------------------------------------------
    // Room decoration (reconstructed + improved)
    // -------------------------------------------------------------------------

    private static void decorateRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        if (isOverworldTheme(rank)) {
            decorateOverworldRoom(dungeon, room, rank);
        } else {
            decorateShadowRoom(dungeon, room, rank);
        }
    }

    private static void decorateShadowRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        decorateShadowFloor(dungeon, room, rank);
        decorateShadowPillars(dungeon, room, rank);
        switch (room.type()) {
            case ENTRANCE, COMBAT -> decorateShadowCombat(dungeon, room, rank);
            case CHAINS  -> decorateChainRoom(dungeon, room, rank);
            case CRYSTAL -> decorateCrystalRoom(dungeon, room, rank);
            case RITUAL  -> decorateRitualRoom(dungeon, room, rank);
            case THRONE, BOSS -> decorateThroneRoom(dungeon, room, rank);
            default -> decorateShadowCombat(dungeon, room, rank);
        }
    }

    private static void decorateShadowFloor(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int x = -room.halfX() + 1; x <= room.halfX() - 1; x++) {
            for (int z = -room.halfZ() + 1; z <= room.halfZ() - 1; z++) {
                int pattern = Math.abs(x * 19 + z * 23) % 13;
                BlockState bs;
                if (pattern == 0) bs = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
                else if (pattern == 1) bs = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
                else if (pattern == 2) bs = (rank == GateRank.A) ? Blocks.PURPUR_BLOCK.defaultBlockState() : Blocks.AMETHYST_BLOCK.defaultBlockState();
                else bs = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
                dungeon.setBlock(room.center().offset(x, -1, z), bs, 3);
            }
        }
    }

    private static void decorateShadowPillars(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int x : new int[]{-room.halfX() + 2, room.halfX() - 2}) {
            for (int z : new int[]{-room.halfZ() + 2, room.halfZ() - 2}) {
                for (int y = 0; y <= 3; y++) {
                    dungeon.setBlock(room.center().offset(x, y, z), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 3);
                }
                dungeon.setBlock(room.center().offset(x, 4, z),
                    (rank == GateRank.A) ? Blocks.PURPUR_BLOCK.defaultBlockState() : Blocks.AMETHYST_BLOCK.defaultBlockState(), 3);
            }
        }
    }

    private static void decorateShadowCombat(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                if (Math.abs(x) != 3 && Math.abs(z) != 3 && Math.abs(x) != Math.abs(z)) continue;
                dungeon.setBlock(room.center().offset(x, -1, z),
                    (rank == GateRank.A) ? Blocks.PURPUR_BLOCK.defaultBlockState() : Blocks.AMETHYST_BLOCK.defaultBlockState(), 3);
            }
        }
        dungeon.setBlock(room.center().offset(0, 0, 0), Blocks.SOUL_TORCH.defaultBlockState(), 3);
    }

    private static void decorateChainRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int x = -room.halfX() + 3; x <= room.halfX() - 3; x += 3) {
            for (int z = -room.halfZ() + 3; z <= room.halfZ() - 3; z += 3) {
                for (int y = 4; y >= 1; y--) {
                    dungeon.setBlock(room.center().offset(x, y, z), Blocks.CHAIN.defaultBlockState(), 3);
                }
                dungeon.setBlock(room.center().offset(x, 0, z), Blocks.LANTERN.defaultBlockState(), 3);
            }
        }
        decorateShadowCombat(dungeon, room, rank);
    }

    private static void decorateCrystalRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        int count = rank == GateRank.A ? 10 : 7;
        for (int i = 0; i < count; i++) {
            int x = dungeon.random.nextInt(Math.max(1, room.halfX() * 2 - 4)) - room.halfX() + 2;
            int z = dungeon.random.nextInt(Math.max(1, room.halfZ() * 2 - 4)) - room.halfZ() + 2;
            BlockPos pos = room.center().offset(x, 0, z);
            dungeon.setBlock(pos, Blocks.AMETHYST_BLOCK.defaultBlockState(), 3);
            dungeon.setBlock(pos.above(), Blocks.BUDDING_AMETHYST.defaultBlockState(), 3);
        }
    }

    private static void decorateRitualRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                int dist = x * x + z * z;
                if (dist < 12 || dist > 20) continue;
                dungeon.setBlock(room.center().offset(x, -1, z),
                    (rank == GateRank.A) ? Blocks.PURPUR_BLOCK.defaultBlockState() : Blocks.AMETHYST_BLOCK.defaultBlockState(), 3);
            }
        }
        dungeon.setBlock(room.center().offset(0, 0, 0), Blocks.ENCHANTING_TABLE.defaultBlockState(), 3);
        for (int x : new int[]{-4, 4}) {
            for (int z : new int[]{-4, 4}) {
                dungeon.setBlock(room.center().offset(x, 0, z), Blocks.SOUL_TORCH.defaultBlockState(), 3);
            }
        }
    }

    private static void decorateThroneRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int z = room.halfZ() - 4; z <= room.halfZ() - 1; z++) {
            for (int x = -2; x <= 2; x++) {
                dungeon.setBlock(room.center().offset(x, 0, z), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 3);
            }
        }
        dungeon.setBlock(room.center().offset(0, 1, room.halfZ() - 2), Blocks.NETHER_BRICKS.defaultBlockState(), 3);
        dungeon.setBlock(room.center().offset(0, 2, room.halfZ() - 2),
            (rank == GateRank.A) ? Blocks.PURPUR_BLOCK.defaultBlockState() : Blocks.AMETHYST_BLOCK.defaultBlockState(), 3);
        decorateShadowCombat(dungeon, room, rank);
    }

    private static void decorateOverworldRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        decorateOverworldFloor(dungeon, room, rank);
        decorateOverworldCorners(dungeon, room, rank);
        decorateOverworldWalls(dungeon, room, rank);
        switch (room.type()) {
            case ENTRANCE -> decorateEntranceRoom(dungeon, room, rank);
            case COMBAT   -> decorateCombatRoom(dungeon, room, rank);
            case TREASURE -> decorateTreasureRoom(dungeon, room, rank);
            case PUZZLE   -> decoratePuzzleRoom(dungeon, room, rank);
            default       -> decorateCombatRoom(dungeon, room, rank);
        }
    }

    private static void decorateOverworldFloor(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int x = -room.halfX() + 1; x <= room.halfX() - 1; x++) {
            for (int z = -room.halfZ() + 1; z <= room.halfZ() - 1; z++) {
                int pattern = Math.abs(x * 31 + z * 17) % 11;
                BlockState bs;
                if (pattern == 0) bs = Blocks.COBBLESTONE.defaultBlockState();
                else if (pattern == 1) bs = Blocks.STONE_BRICKS.defaultBlockState();
                else if (rank == GateRank.C && pattern == 2) bs = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                else bs = Blocks.STONE.defaultBlockState();
                dungeon.setBlock(room.center().offset(x, -1, z), bs, 3);
            }
        }
    }

    private static void decorateOverworldCorners(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int x : new int[]{-room.halfX() + 2, room.halfX() - 2}) {
            for (int z : new int[]{-room.halfZ() + 2, room.halfZ() - 2}) {
                for (int y = 0; y <= 2; y++) {
                    dungeon.setBlock(room.center().offset(x, y, z), Blocks.OAK_FENCE.defaultBlockState(), 3);
                }
                dungeon.setBlock(room.center().offset(x, 3, z),
                    rank == GateRank.C ? Blocks.FIRE.defaultBlockState() : Blocks.TORCH.defaultBlockState(), 3);
            }
        }
    }

    private static void decorateOverworldWalls(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int x = -room.halfX() + 2; x <= room.halfX() - 2; x += 4) {
            dungeon.setBlock(room.center().offset(x, 0, -room.halfZ() + 1),
                rank == GateRank.C ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState() : Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 3);
            dungeon.setBlock(room.center().offset(x, 0,  room.halfZ() - 1),
                rank == GateRank.C ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.OAK_FENCE.defaultBlockState(), 3);
        }
        for (int z = -room.halfZ() + 2; z <= room.halfZ() - 2; z += 4) {
            dungeon.setBlock(room.center().offset(-room.halfX() + 1, 0, z),
                rank == GateRank.C ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState() : Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 3);
            dungeon.setBlock(room.center().offset( room.halfX() - 1, 0, z),
                rank == GateRank.C ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.OAK_FENCE.defaultBlockState(), 3);
        }
    }

    private static void decorateEntranceRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int z = -room.halfZ() + 2; z <= room.halfZ() - 2; z++) {
            if (z % 3 != 0) continue;
            dungeon.setBlock(room.center().offset(0, -1, z),
                rank == GateRank.C ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState(), 3);
        }
    }

    private static void decorateCombatRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) != 2 && Math.abs(z) != 2) continue;
                dungeon.setBlock(room.center().offset(x, -1, z),
                    rank == GateRank.C ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState(), 3);
            }
        }
        dungeon.setBlock(room.center().offset(0, 0, 0), Blocks.TORCH.defaultBlockState(), 3);
    }

    private static void decorateTreasureRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        dungeon.setBlock(room.center().offset(0, 0, 0), Blocks.GOLD_BLOCK.defaultBlockState(), 3);
        dungeon.setBlock(room.center().offset(0, 1, 0), Blocks.GLOWSTONE.defaultBlockState(), 3);
        for (int x : new int[]{-2, 2}) {
            for (int z : new int[]{-2, 2}) {
                dungeon.setBlock(room.center().offset(x, 0, z),
                    rank == GateRank.C ? Blocks.FIRE.defaultBlockState() : Blocks.TORCH.defaultBlockState(), 3);
            }
        }
    }

    private static void decoratePuzzleRoom(ServerLevel dungeon, DungeonRoom room, GateRank rank) {
        for (int x = -room.halfX() + 3; x <= room.halfX() - 3; x++) {
            if (x % 2 == 0)
                dungeon.setBlock(room.center().offset(x, -1, 0), Blocks.TUFF.defaultBlockState(), 3);
        }
        for (int z = -room.halfZ() + 3; z <= room.halfZ() - 3; z++) {
            if (z % 2 == 0)
                dungeon.setBlock(room.center().offset(0, -1, z), Blocks.TUFF.defaultBlockState(), 3);
        }
        dungeon.setBlock(room.center().offset(0, 0, 0),
            rank == GateRank.C ? Blocks.FIRE.defaultBlockState() : Blocks.TORCH.defaultBlockState(), 3);
    }

    // -------------------------------------------------------------------------
    // Corridors
    // -------------------------------------------------------------------------

    private static void connectRooms(ServerLevel dungeon, DungeonRoom first, DungeonRoom second) {
        BlockPos cursor = first.center();
        while (cursor.getX() != second.center().getX()) {
            cursor = cursor.offset(cursor.getX() < second.center().getX() ? 1 : -1, 0, 0);
            carveCorridor(dungeon, cursor);
        }
        while (cursor.getZ() != second.center().getZ()) {
            cursor = cursor.offset(0, 0, cursor.getZ() < second.center().getZ() ? 1 : -1);
            carveCorridor(dungeon, cursor);
        }
    }

    private static void carveCorridor(ServerLevel dungeon, BlockPos center) {
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 5; y++) {
                for (int z = -2; z <= 2; z++) {
                    boolean shell = Math.abs(x) == 2 || Math.abs(z) == 2 || y == -1 || y == 5;
                    dungeon.setBlock(center.offset(x, y, z),
                        shell ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    private static void connectRoomsOpen(ServerLevel dungeon, DungeonRoom first, DungeonRoom second) {
        BlockPos cursor = first.center();
        while (cursor.getX() != second.center().getX()) {
            cursor = cursor.offset(cursor.getX() < second.center().getX() ? 1 : -1, 0, 0);
            carveOpenCorridor(dungeon, cursor);
        }
        while (cursor.getZ() != second.center().getZ()) {
            cursor = cursor.offset(0, 0, cursor.getZ() < second.center().getZ() ? 1 : -1);
            carveOpenCorridor(dungeon, cursor);
        }
    }

    private static void carveOpenCorridor(ServerLevel dungeon, BlockPos center) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                dungeon.setBlock(center.offset(x, -1, z), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 3);
                for (int y = 0; y <= 4; y++) {
                    dungeon.setBlock(center.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Mob spawning (improved: skip entrance room, apply HP multiplier)
    // -------------------------------------------------------------------------

    private static void spawnBoss(ServerLevel dungeon, GateRecord gate, DungeonRoom room) {
        for (int i = 0; i < SoloGatesConfig.BOSS_MOB_COUNT.get(); i++) {
            randomEntityType(SoloGatesConfig.BOSS_FALLBACK_MOBS.get(), dungeon.random).ifPresent(type -> {
                Entity entity = type.create(dungeon);
                if (entity instanceof Mob mob) {
                    BlockPos pos = room.center().offset(
                        dungeon.random.nextInt(7) - 3, 1, dungeon.random.nextInt(7) - 3);
                    mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                        dungeon.random.nextFloat() * 360f, 0);
                    applyHealthMultiplier(mob, GateRank.S);
                    mob.setPersistenceRequired();
                    dungeon.addFreshEntity(mob);
                    gate.mobs.add(mob.getUUID());
                }
            });
        }
    }

    private static void spawnDungeonMobs(ServerLevel dungeon, GateRecord gate, List<DungeonRoom> rooms) {
        RandomSource random = dungeon.random;
        List<? extends String> mobs = gate.rank.mobs();
        Map<String, Integer> spawnedById = new HashMap<>();
        int mobCount = gate.rank.minMobCount()
            + random.nextInt(Math.max(1, gate.rank.maxMobCount() - gate.rank.minMobCount() + 1));

        // Only non-entrance rooms for spawning
        List<DungeonRoom> combatRooms = rooms.stream()
            .filter(r -> r.type() != RoomType.ENTRANCE && r.type() != RoomType.TREASURE)
            .toList();
        if (combatRooms.isEmpty()) combatRooms = rooms;

        for (int i = 0; i < mobCount; i++) {
            final List<DungeonRoom> spawnRooms = combatRooms;
            randomMobId(gate.rank, mobs, spawnedById, random).flatMap(EntityType::byString).ifPresent(type -> {
                Entity entity = type.create(dungeon);
                if (entity instanceof Mob mob) {
                    String mobId = EntityType.getKey(type).toString();
                    spawnedById.merge(mobId, 1, Integer::sum);
                    DungeonRoom room = spawnRooms.get(random.nextInt(spawnRooms.size()));
                    BlockPos pos = room.center().offset(
                        random.nextInt(Math.max(1, room.halfX() * 2 - 3)) - room.halfX() + 2, 1,
                        random.nextInt(Math.max(1, room.halfZ() * 2 - 3)) - room.halfZ() + 2);
                    mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                        random.nextFloat() * 360f, 0);
                    applyHealthMultiplier(mob, gate.rank);
                    mob.setPersistenceRequired();
                    dungeon.addFreshEntity(mob);
                    gate.mobs.add(mob.getUUID());
                }
            });
        }
    }

    /** Scale max HP by rank multiplier. */
    private static void applyHealthMultiplier(Mob mob, GateRank rank) {
        double mult = rank.mobHealthMultiplier();
        if (mult <= 1.0) return;
        var attr = mob.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) {
            double newMax = attr.getBaseValue() * mult;
            attr.setBaseValue(newMax);
            mob.setHealth((float) newMax);
        }
    }

    private static Optional<String> randomMobId(GateRank rank, List<? extends String> mobs,
            Map<String, Integer> spawnedById, RandomSource random) {
        if (mobs.isEmpty()) return Optional.of("minecraft:zombie");
        for (int attempt = 0; attempt < 40; attempt++) {
            String mobId = mobs.get(random.nextInt(mobs.size()));
            if (rank == GateRank.S && isLimitedRankSMob(mobId) && spawnedById.getOrDefault(mobId, 0) >= 1) continue;
            if (spawnedById.getOrDefault(mobId, 0) >= rank.maxSameMob()) continue;
            if (!EntityType.byString(mobId).isPresent()) continue;
            return Optional.of(mobId);
        }
        return Optional.of("minecraft:zombie");
    }

    private static boolean isLimitedRankSMob(String id) {
        return id.equals("cataclysm:ignited_revenant") || id.equals("mowziesmobs:ferrous_wroughtnaut");
    }

    private static Optional<EntityType<?>> randomEntityType(List<? extends String> mobs, RandomSource random) {
        if (mobs.isEmpty()) return Optional.of(EntityType.ZOMBIE);
        for (int attempt = 0; attempt < 20; attempt++) {
            String mobId = mobs.get(random.nextInt(mobs.size()));
            if (isBlockedBossBarEntity(mobId)) continue;
            Optional<EntityType<?>> type = EntityType.byString(mobId);
            if (type.isPresent()) return type;
        }
        return Optional.of(EntityType.ZOMBIE);
    }

    private static boolean isBlockedBossBarEntity(String id) {
        return id.equals("mowziesmobs:frostmaw") || id.equals("cataclysm:ender_golem")
            || id.equals("cataclysm:ender_guardian") || id.equals("cataclysm:netherite_monstrosity")
            || id.equals("cataclysm:ignis") || id.equals("cataclysm:the_harbinger")
            || id.equals("cataclysm:the_leviathan") || id.equals("cataclysm:ancient_remnant")
            || id.equals("cataclysm:maledictus");
    }

    // -------------------------------------------------------------------------
    // Gate completion / expiry / failure
    // -------------------------------------------------------------------------

    private static void completeGate(ServerLevel overworld, GateRecord gate) {
        gate.completed = true;
        ServerLevel dungeon = overworld.getServer().getLevel(DUNGEON_LEVEL);
        if (dungeon != null) {
            placeReturnGate(dungeon, gate);
            for (ServerPlayer player : dungeon.getEntitiesOfClass(ServerPlayer.class,
                    new AABB(gate.dungeonPos).inflate(64, 16, 64))) {
                player.displayClientMessage(
                    Component.translatable("sologates.message.dungeon_complete").withStyle(ChatFormatting.GREEN), false);
            }
        }
        removeGateBlocks(overworld, gate.overworldPos);
        BlockPos chestPos = gate.overworldPos.offset(0, 1, 2);
        overworld.setBlockAndUpdate(chestPos,
            Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, overworld.random.nextBoolean() ? Direction.NORTH : Direction.SOUTH));
        BlockEntity be = overworld.getBlockEntity(chestPos);
        if (be instanceof ChestBlockEntity chest) {
            fillRewardChest(chest, gate, overworld.random);
        }
        overworld.playSound(null, chestPos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.4f, 0.8f);

        if (dungeon != null) {
            PlayerSavedData psd = PlayerSavedData.get(overworld.getServer());
            for (ServerPlayer p : dungeon.getEntitiesOfClass(ServerPlayer.class,
                    new AABB(gate.dungeonPos).inflate(64, 16, 64))) {
                PlayerData pd = psd.getOrCreate(p.getUUID());
                pd.recordCompletion(gate.rank);
                SoloGatesCriteria.GATE_COMPLETE.trigger(p, gate.rank, gate.bossGate);
                SoloGatesCriteria.GATE_MILESTONE.trigger(p, pd.totalCompletions());
            }
            psd.markDirty();
        }
    }

    private static void placeReturnGate(ServerLevel dungeon, GateRecord gate) {
        BlockPos base = gate.dungeonPos.offset(0, 0, -5);
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 3; y++) {
                BlockPos pos = base.offset(x, y, 0);
                boolean frame = Math.abs(x) == 1 || y == 0 || y == 3;
                dungeon.setBlockAndUpdate(pos,
                    frame ? Blocks.POLISHED_BLACKSTONE.defaultBlockState()
                          : ModBlocks.gateBlock(gate.rank).defaultBlockState());
            }
        }
        BlockPos signPos = base.offset(0, 1, -1);
        dungeon.setBlockAndUpdate(signPos, Blocks.OAK_SIGN.defaultBlockState());
        BlockEntity be = dungeon.getBlockEntity(signPos);
        if (be instanceof SignBlockEntity sign) {
            SignText text = sign.getText(true)
                .setMessage(0, Component.translatable("sologates.sign.return.line1"))
                .setMessage(1, Component.translatable("sologates.sign.return.line2"))
                .setMessage(2, Component.translatable("sologates.sign.return.line3"))
                .setMessage(3, Component.translatable("sologates.sign.return.line4"));
            sign.setText(text, true);
        }
    }

    private static void giveCompletionCoin(ServerPlayer player, GateRecord gate) {
        ItemStack stack = switch (gate.rank) {
            case E -> new ItemStack((ItemLike) ModBlocks.BRONZE_COIN.get());
            case C -> new ItemStack((ItemLike) ModBlocks.SILVER_COIN.get());
            case B -> new ItemStack((ItemLike) ModBlocks.GOLD_COIN.get());
            case A -> new ItemStack((ItemLike) ModBlocks.GOLD_COIN.get(), 2);
            case S -> new ItemStack((ItemLike) ModBlocks.PLATINUM_COIN.get());
        };
        player.addItem(stack);
    }

    private static void giveXpReward(ServerPlayer player, GateRank rank) {
        int xp = rank.xpReward();
        if (xp > 0) {
            player.giveExperiencePoints(xp);
        }
    }

    private static void expireGate(ServerLevel overworld, GateRecord gate) {
        ServerLevel dungeon = overworld.getServer().getLevel(DUNGEON_LEVEL);
        if (dungeon != null) {
            AABB arena = new AABB(gate.dungeonPos).inflate(64, 16, 64);
            for (ServerPlayer player : dungeon.getEntitiesOfClass(ServerPlayer.class, arena)) {
                teleportToOverworld(player, gate);
                player.displayClientMessage(Component.translatable("sologates.message.gate_closed").withStyle(ChatFormatting.RED), false);
            }
            for (UUID mobId : gate.mobs) {
                Entity mob = dungeon.getEntity(mobId);
                if (mob != null) mob.discard();
            }
            clearDungeonSpace(dungeon, gate.dungeonPos);
        }
        removeGateBlocks(overworld, gate.overworldPos);
        overworld.playSound(null, gate.overworldPos, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 1f, 0.5f);
    }

    private static void teleportToOverworld(ServerPlayer player, GateRecord gate) {
        ServerLevel overworld = player.getServer().overworld();
        BlockPos exit = gate.returnPosition(player.getUUID())
            .orElse(gate.overworldPos.offset(0, 1, -3));
        player.teleportTo(overworld,
            exit.getX() + 0.5, exit.getY(), exit.getZ() + 0.5,
            player.getYRot(), player.getXRot());
    }

    private static void failGate(ServerLevel dungeon, GateRecord gate) {
        ServerLevel overworld = dungeon.getServer().overworld();
        gate.failed = true;

        // Scale invasion size by rank and config
        int invasionPercent = SoloGatesConfig.INVASION_PERCENT.get();
        List<UUID> invasionPool = new ArrayList<>(gate.mobs);
        int invasionCount = (int) Math.ceil(invasionPool.size() * invasionPercent / 100.0);

        for (int i = 0; i < invasionCount && i < invasionPool.size(); i++) {
            Entity original = dungeon.getEntity(invasionPool.get(i));
            if (!(original instanceof Mob mob)) continue;
            Entity copy = mob.getType().create(overworld);
            if (copy instanceof Mob invasionMob) {
                BlockPos spawn = gate.overworldPos.offset(
                    overworld.random.nextInt(9) - 4, 1, overworld.random.nextInt(9) - 4);
                invasionMob.moveTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                    overworld.random.nextFloat() * 360f, 0);
                invasionMob.setPersistenceRequired();
                overworld.addFreshEntity(invasionMob);
            }
            original.discard();
        }
        // Discard remaining dungeon mobs
        for (UUID mobId : gate.mobs) {
            Entity mob = dungeon.getEntity(mobId);
            if (mob != null) mob.discard();
        }

        // Eject live players
        AABB arena = new AABB(gate.dungeonPos).inflate(64, 16, 64);
        for (ServerPlayer player : dungeon.getEntitiesOfClass(ServerPlayer.class, arena)) {
            if (!player.isDeadOrDying()) {
                teleportToOverworld(player, gate);
                player.displayClientMessage(
                    Component.translatable("sologates.message.dungeon_collapse")
                        .withStyle(ChatFormatting.DARK_RED), false);
            }
        }
        clearDungeonSpace(dungeon, gate.dungeonPos);
        removeGateBlocks(overworld, gate.overworldPos);
        overworld.playSound(null, gate.overworldPos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1f, 1.2f);
        GateSavedData.get(dungeon.getServer()).removeGate(gate.id);
    }

    // -------------------------------------------------------------------------
    // Entrance preparation
    // -------------------------------------------------------------------------

    private static void prepareEntrance(ServerLevel dungeon, BlockPos center) {
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                dungeon.setBlock(center.offset(x, -1, z), Blocks.DEEPSLATE_TILES.defaultBlockState(), 3);
                for (int y = 0; y <= 4; y++) {
                    dungeon.setBlock(center.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        dungeon.setBlock(center.offset(0, -1, 0), Blocks.RESPAWN_ANCHOR.defaultBlockState(), 3);
    }

    // -------------------------------------------------------------------------
    // Reward chest
    // -------------------------------------------------------------------------

    private static void fillRewardChest(ChestBlockEntity chest, GateRecord gate, RandomSource random) {
        GateRank rank = gate.rank;
        List<? extends String> rewards = gate.bossGate
            ? SoloGatesConfig.BOSS_REWARDS.get() : rank.rewards();
        int rolls = switch (rank) {
            case E -> 2 + random.nextInt(2);
            case C -> 3 + random.nextInt(2);
            case B -> 4 + random.nextInt(3);
            case A -> 5 + random.nextInt(3);
            case S -> 6 + random.nextInt(3);
        };
        if (gate.bossGate) rolls += 2;
        for (int i = 0; i < rolls; i++) {
            ItemStack stack = randomReward(rewards, random);
            if (!stack.isEmpty()) {
                chest.setItem(random.nextInt(chest.getContainerSize()), stack);
            }
        }
    }

    private static ItemStack randomReward(List<? extends String> rewards, RandomSource random) {
        if (rewards.isEmpty()) return ItemStack.EMPTY;
        int totalWeight = 0;
        for (String r : rewards) totalWeight += rewardWeight(r);
        if (totalWeight <= 0) return ItemStack.EMPTY;

        int roll = random.nextInt(Math.max(1, totalWeight));
        int cursor = 0;
        for (String r : rewards) {
            cursor += rewardWeight(r);
            if (roll < cursor) return parseReward(r, random);
        }
        return ItemStack.EMPTY;
    }

    private static int rewardWeight(String reward) {
        String[] split = reward.split(":");
        if (split.length < 4) return 0;
        try {
            return split.length >= 5 ? Math.max(1, Integer.parseInt(split[4])) : 10;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static ItemStack parseReward(String reward, RandomSource random) {
        String[] split = reward.split(":");
        if (split.length < 4) return ItemStack.EMPTY;
        ResourceLocation itemId = new ResourceLocation(split[0], split[1]);
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null) return ItemStack.EMPTY;
        try {
            int min = Math.max(1, Integer.parseInt(split[2]));
            int max = Math.max(min, Integer.parseInt(split[3]));
            int count = min + random.nextInt(Math.max(1, max - min + 1));
            return new ItemStack((ItemLike) item, count);
        } catch (NumberFormatException e) {
            return ItemStack.EMPTY;
        }
    }

    // -------------------------------------------------------------------------
    // Inner types
    // -------------------------------------------------------------------------

    private record WeightedRank(GateRank rank, int weight) {}
    private record DungeonRoom(BlockPos center, int halfX, int halfZ, RoomType type) {}

    private enum RoomType {
        ENTRANCE, COMBAT, TREASURE, PUZZLE, BOSS,
        CHAINS, CRYSTAL, RITUAL, THRONE
    }

    private GateManager() {}
}
