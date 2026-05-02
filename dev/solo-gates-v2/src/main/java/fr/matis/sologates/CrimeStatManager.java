package fr.matis.sologates;

import fr.matis.sologates.network.SoloGatesNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class CrimeStatManager {
    public static final long CRIME_WINDOW_TICKS = 24L * 60L * 60L * 20L;

    private CrimeStatManager() {}

    // SkillTree optional dependency — reflected once at class load, null if mod absent
    private static final boolean SKILLTREE_PRESENT;
    private static final Method  PST_HAS_SKILLS;
    private static final Method  PST_GET_SKILLS;
    private static final Method  PST_GET_POINTS;
    private static final Method  PST_SET_POINTS;
    private static final Method  PST_GRANT_POINTS;
    private static final SimpleChannel     PST_CHANNEL;
    private static final Constructor<?>    PST_SYNC_MSG;

    static {
        boolean present = false;
        Method hasSkills = null, getSkills = null, getPoints = null, setPoints = null, grantPoints = null;
        SimpleChannel channel = null;
        Constructor<?> syncMsg = null;
        try {
            Class<?> provider   = Class.forName("daripher.skilltree.capability.skill.PlayerSkillsProvider");
            Class<?> iSkills    = Class.forName("daripher.skilltree.capability.skill.IPlayerSkills");
            Class<?> dispatcher = Class.forName("daripher.skilltree.network.NetworkDispatcher");
            Class<?> syncMsgCls = Class.forName("daripher.skilltree.network.message.SyncPlayerSkillsMessage");
            hasSkills   = provider.getMethod("hasSkills", LivingEntity.class);
            getSkills   = provider.getMethod("get",       LivingEntity.class);
            getPoints   = iSkills.getMethod("getSkillPoints");
            setPoints   = iSkills.getMethod("setSkillPoints",  int.class);
            grantPoints = iSkills.getMethod("grantSkillPoints", int.class);
            channel     = (SimpleChannel) dispatcher.getField("network_channel").get(null);
            syncMsg     = syncMsgCls.getConstructor(ServerPlayer.class);
            present     = true;
        } catch (Exception ignored) {}
        SKILLTREE_PRESENT = present;
        PST_HAS_SKILLS    = hasSkills;
        PST_GET_SKILLS    = getSkills;
        PST_GET_POINTS    = getPoints;
        PST_SET_POINTS    = setPoints;
        PST_GRANT_POINTS  = grantPoints;
        PST_CHANNEL       = channel;
        PST_SYNC_MSG      = syncMsg;
    }

    // -------------------------------------------------------------------------

    public static void onPlayerKilled(ServerPlayer victim, Entity attacker) {
        if (!(attacker instanceof ServerPlayer killer)) return;
        if (killer.getUUID().equals(victim.getUUID())) return;

        CrimeSavedData data = CrimeSavedData.get(victim.server);
        CrimeStatData victimCrime = data.getOrCreate(victim.getUUID());
        CrimeStatData killerCrime = data.getOrCreate(killer.getUUID());
        long gameTime = victim.server.overworld().getGameTime();

        victimCrime.prune(gameTime);
        killerCrime.prune(gameTime);

        if (victimCrime.level() > 0) {
            rewardBounty(killer, victim, killerCrime, victimCrime);
        } else {
            applyInnocentKill(killer, victim, killerCrime, gameTime);
        }

        data.setDirty();
        syncCrimeStat(killer);
        syncCrimeStat(victim);
    }

    public static int crimeLevel(ServerPlayer player) {
        return CrimeSavedData.get(player.server)
            .get(player.getUUID())
            .map(CrimeStatData::level)
            .orElse(0);
    }

    public static void syncCrimeStat(ServerPlayer player) {
        PlayerData pd = PlayerSavedData.get(player.server).getOrCreate(player.getUUID());
        SoloGatesNetwork.sendRankToPlayer(player, pd.confirmedRank(), pd.currentStreak());
        SoloGatesNetwork.broadcastCrimeStat(player.server, player.getUUID(), crimeLevel(player));
    }

    // -------------------------------------------------------------------------

    private static void applyInnocentKill(ServerPlayer killer, ServerPlayer victim,
                                          CrimeStatData killerCrime, long gameTime) {
        int previousLevel = killerCrime.level();
        killerCrime.recordInnocentKill(gameTime);
        if (killerCrime.level() > previousLevel) {
            killer.displayClientMessage(Component.translatable("sologates.crime.level_up", killerCrime.level())
                .withStyle(ChatFormatting.RED), false);
        }
        stealSkillPoint(killer, victim, killerCrime, gameTime);
    }

    private static void stealSkillPoint(ServerPlayer killer, ServerPlayer victim,
                                        CrimeStatData killerCrime, long gameTime) {
        if (killerCrime.remainingSteals(gameTime) <= 0) {
            killer.displayClientMessage(Component.translatable("sologates.crime.steal_limit")
                .withStyle(ChatFormatting.GRAY), false);
            return;
        }

        int transferred = transferSkillPoints(victim, killer, 1);
        killerCrime.recordStolenPoint(gameTime);
        int remaining = killerCrime.remainingSteals(gameTime);

        if (transferred <= 0) {
            killer.displayClientMessage(Component.translatable("sologates.crime.point_steal_empty", remaining)
                .withStyle(ChatFormatting.GRAY), false);
            return;
        }

        victim.displayClientMessage(Component.translatable("sologates.crime.point_stolen")
            .withStyle(ChatFormatting.RED), false);
        killer.displayClientMessage(Component.translatable("sologates.crime.point_stolen_gain", remaining)
            .withStyle(ChatFormatting.DARK_RED), false);
    }

    private static void rewardBounty(ServerPlayer killer, ServerPlayer victim,
                                     CrimeStatData killerCrime, CrimeStatData victimCrime) {
        int bounty = victimCrime.level();
        victimCrime.clearCrimeStat();

        if (killerCrime.level() > 0) {
            victim.displayClientMessage(Component.translatable("sologates.crime.cleared")
                .withStyle(ChatFormatting.GREEN), false);
            return;
        }

        int transferred = transferSkillPoints(victim, killer, bounty);
        victim.displayClientMessage(Component.translatable("sologates.crime.bounty_paid", transferred)
            .withStyle(ChatFormatting.RED), false);
        killer.displayClientMessage(Component.translatable("sologates.crime.bounty_claimed", transferred)
            .withStyle(ChatFormatting.GOLD), false);
        if (bounty >= 3) {
            killer.server.getPlayerList().broadcastSystemMessage(
                Component.translatable("sologates.broadcast.cs3_eliminated",
                    killer.getName(), victim.getName()).withStyle(ChatFormatting.DARK_RED), false);
        }
    }

    // -------------------------------------------------------------------------

    private static int transferSkillPoints(ServerPlayer from, ServerPlayer to, int requested) {
        if (!SKILLTREE_PRESENT) return 0;
        try {
            if (!(boolean) PST_HAS_SKILLS.invoke(null, from) ||
                !(boolean) PST_HAS_SKILLS.invoke(null, to)) return 0;
            Object fromSkills = PST_GET_SKILLS.invoke(null, from);
            Object toSkills   = PST_GET_SKILLS.invoke(null, to);
            int available   = (int) PST_GET_POINTS.invoke(fromSkills);
            int transferred = Math.max(0, Math.min(3, Math.min(requested, available)));
            if (transferred <= 0) return 0;
            PST_SET_POINTS.invoke(fromSkills, available - transferred);
            PST_GRANT_POINTS.invoke(toSkills, transferred);
            syncPassiveSkillTree(from);
            syncPassiveSkillTree(to);
            return transferred;
        } catch (Exception e) {
            return 0;
        }
    }

    private static void syncPassiveSkillTree(ServerPlayer player) {
        if (!SKILLTREE_PRESENT) return;
        try {
            PST_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), PST_SYNC_MSG.newInstance(player));
        } catch (Exception ignored) {}
    }
}
