package fr.matis.sologates;

import daripher.skilltree.capability.skill.IPlayerSkills;
import daripher.skilltree.capability.skill.PlayerSkillsProvider;
import daripher.skilltree.network.NetworkDispatcher;
import daripher.skilltree.network.message.SyncPlayerSkillsMessage;
import fr.matis.sologates.network.SoloGatesNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.PacketDistributor;

public final class CrimeStatManager {
    public static final long CRIME_WINDOW_TICKS = 24L * 60L * 60L * 20L;

    private CrimeStatManager() {}

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
        PlayerSavedData psd = PlayerSavedData.get(player.server);
        PlayerData pd = psd.getOrCreate(player.getUUID());
        SoloGatesNetwork.sendRankToPlayer(player, pd.confirmedRank(), pd.currentStreak());
    }

    private static void applyInnocentKill(ServerPlayer killer, ServerPlayer victim, CrimeStatData killerCrime, long gameTime) {
        int previousLevel = killerCrime.level();
        killerCrime.recordInnocentKill(gameTime);
        int newLevel = killerCrime.level();
        if (newLevel > previousLevel) {
            killer.displayClientMessage(Component.translatable("sologates.crime.level_up", newLevel)
                .withStyle(ChatFormatting.RED), false);
        }
        stealSkillPoint(killer, victim, killerCrime, gameTime);
    }

    private static void stealSkillPoint(ServerPlayer killer, ServerPlayer victim, CrimeStatData killerCrime, long gameTime) {
        if (killerCrime.remainingSteals(gameTime) <= 0) {
            killer.displayClientMessage(Component.translatable("sologates.crime.steal_limit")
                .withStyle(ChatFormatting.GRAY), false);
            return;
        }

        int transferred = transferSkillPoints(victim, killer, 1);
        killerCrime.recordStolenPoint(gameTime);
        if (transferred <= 0) {
            killer.displayClientMessage(Component.translatable("sologates.crime.point_steal_empty", killerCrime.remainingSteals(gameTime))
                .withStyle(ChatFormatting.GRAY), false);
            return;
        }

        victim.displayClientMessage(Component.translatable("sologates.crime.point_stolen")
            .withStyle(ChatFormatting.RED), false);
        killer.displayClientMessage(Component.translatable("sologates.crime.point_stolen_gain", killerCrime.remainingSteals(gameTime))
            .withStyle(ChatFormatting.DARK_RED), false);
    }

    private static void rewardBounty(ServerPlayer killer, ServerPlayer victim, CrimeStatData killerCrime, CrimeStatData victimCrime) {
        int bounty = victimCrime.level();
        if (killerCrime.level() > 0) {
            victimCrime.clearCrimeStat();
            victim.displayClientMessage(Component.translatable("sologates.crime.cleared")
                .withStyle(ChatFormatting.GREEN), false);
            return;
        }

        int transferred = transferSkillPoints(victim, killer, bounty);
        victimCrime.clearCrimeStat();
        victim.displayClientMessage(Component.translatable("sologates.crime.bounty_paid", transferred)
            .withStyle(ChatFormatting.RED), false);
        killer.displayClientMessage(Component.translatable("sologates.crime.bounty_claimed", transferred)
            .withStyle(ChatFormatting.GOLD), false);
    }

    private static int transferSkillPoints(ServerPlayer from, ServerPlayer to, int requested) {
        if (!PlayerSkillsProvider.hasSkills(from) || !PlayerSkillsProvider.hasSkills(to)) return 0;
        IPlayerSkills fromSkills = PlayerSkillsProvider.get(from);
        IPlayerSkills toSkills = PlayerSkillsProvider.get(to);
        int transferred = Math.max(0, Math.min(3, Math.min(requested, fromSkills.getSkillPoints())));
        if (transferred <= 0) return 0;

        fromSkills.setSkillPoints(fromSkills.getSkillPoints() - transferred);
        toSkills.grantSkillPoints(transferred);
        syncPassiveSkillTree(from);
        syncPassiveSkillTree(to);
        return transferred;
    }

    private static void syncPassiveSkillTree(ServerPlayer player) {
        NetworkDispatcher.network_channel.send(
            PacketDistributor.PLAYER.with(() -> player),
            new SyncPlayerSkillsMessage(player)
        );
    }
}
