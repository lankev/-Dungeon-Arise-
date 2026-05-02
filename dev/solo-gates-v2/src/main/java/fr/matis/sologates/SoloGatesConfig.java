package fr.matis.sologates;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public final class SoloGatesConfig {
    public static final ForgeConfigSpec SPEC;

    // Spawn
    public static final ForgeConfigSpec.IntValue MAX_ACTIVE_GATES;
    public static final ForgeConfigSpec.IntValue WORLD_SPAWN_INTERVAL_SECONDS;
    public static final ForgeConfigSpec.IntValue PLAYER_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.IntValue SPAWN_RADIUS_MIN;
    public static final ForgeConfigSpec.IntValue SPAWN_RADIUS_MAX;
    public static final ForgeConfigSpec.IntValue GATE_LIFETIME_SECONDS;
    public static final ForgeConfigSpec.IntValue BOSS_GATE_CHANCE_PERCENT;
    public static final ForgeConfigSpec.IntValue PLAYER_RANK_GATE_BONUS_PERCENT;
    public static final ForgeConfigSpec.IntValue BOSS_MOB_COUNT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_WEIGHTS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_SPAWN_INTERVALS;

    // Mobs
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_E_MOBS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_C_MOBS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_B_MOBS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_A_MOBS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_S_MOBS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BOSS_FALLBACK_MOBS;

    // Rewards
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_E_REWARDS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_C_REWARDS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_B_REWARDS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_A_REWARDS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_S_REWARDS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BOSS_REWARDS;

    // XP rewards per rank
    public static final ForgeConfigSpec.IntValue XP_REWARD_E;
    public static final ForgeConfigSpec.IntValue XP_REWARD_C;
    public static final ForgeConfigSpec.IntValue XP_REWARD_B;
    public static final ForgeConfigSpec.IntValue XP_REWARD_A;
    public static final ForgeConfigSpec.IntValue XP_REWARD_S;

    // Mob health multipliers per rank
    public static final ForgeConfigSpec.DoubleValue MOB_HP_MULT_E;
    public static final ForgeConfigSpec.DoubleValue MOB_HP_MULT_C;
    public static final ForgeConfigSpec.DoubleValue MOB_HP_MULT_B;
    public static final ForgeConfigSpec.DoubleValue MOB_HP_MULT_A;
    public static final ForgeConfigSpec.DoubleValue MOB_HP_MULT_S;

    // Invasion
    public static final ForgeConfigSpec.IntValue INVASION_PERCENT;

    private static ForgeConfigSpec.ConfigValue<List<? extends String>> mobList(
            ForgeConfigSpec.Builder builder, String name, List<String> defaults) {
        return builder.comment("Ids d'entites possibles pour ce rang.")
            .defineList(name, defaults, value -> value instanceof String s && s.contains(":"));
    }

    private static ForgeConfigSpec.ConfigValue<List<? extends String>> rewardList(
            ForgeConfigSpec.Builder builder, String name, List<String> defaults) {
        return builder.comment("Items: namespace:id:min:max:poids. Ex: minecraft:diamond:1:3:20")
            .defineList(name, defaults, value -> value instanceof String s && s.split(":").length >= 4);
    }

    private static boolean isWeightedRank(Object value) {
        if (!(value instanceof String s) || !s.contains("=")) return false;
        String rankStr = s.substring(0, s.indexOf('='));
        try {
            GateRank.valueOf(rankStr);
            return Integer.parseInt(s.substring(s.indexOf('=') + 1)) > 0;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private SoloGatesConfig() {}

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("spawn");
        MAX_ACTIVE_GATES = builder.comment("Maximum de portails actifs en meme temps.").defineInRange("maxActiveGates", 4, 1, 16);
        WORLD_SPAWN_INTERVAL_SECONDS = builder.comment("Temps minimum entre deux spawns globaux.").defineInRange("worldSpawnIntervalSeconds", 600, 60, 86400);
        PLAYER_COOLDOWN_SECONDS = builder.comment("Cooldown par joueur entre deux portails.").defineInRange("playerCooldownSeconds", 600, 60, 86400);
        SPAWN_RADIUS_MIN = builder.defineInRange("spawnRadiusMin", 50, 8, 512);
        SPAWN_RADIUS_MAX = builder.defineInRange("spawnRadiusMax", 80, 16, 1024);
        GATE_LIFETIME_SECONDS = builder.comment("Temps avant disparition d'un portail non termine.").defineInRange("gateLifetimeSeconds", 300, 60, 172800);
        RANK_WEIGHTS = builder.comment("Poids des rangs: RANG=poids.").defineList("rankWeights", List.of("E=58","C=24","B=11","A=5","S=2"), SoloGatesConfig::isWeightedRank);
        RANK_SPAWN_INTERVALS = builder.comment("Cooldown global par rang: RANG=secondes.").defineList("rankSpawnIntervals", List.of("E=600","C=900","B=1200","A=1500","S=1800"), SoloGatesConfig::isWeightedRank);
        BOSS_GATE_CHANCE_PERCENT = builder.comment("Chance (%) qu'un portail devienne un portail boss.").defineInRange("bossGateChancePercent", 1, 0, 100);
        PLAYER_RANK_GATE_BONUS_PERCENT = builder.comment("Bonus (%) qu'un portail naturel autour d'un joueur soit de son rang confirme. 0 = desactive.").defineInRange("playerRankGateBonusPercent", 5, 0, 100);
        BOSS_MOB_COUNT = builder.comment("Nombre de mobs dans un portail boss.").defineInRange("bossMobCount", 3, 1, 16);
        builder.pop();

        builder.push("mobs");
        RANK_E_MOBS = mobList(builder, "rankE", List.of("minecraft:zombie","minecraft:skeleton","minecraft:spider","minecraft:husk","minecraft:drowned","minecraft:slime"));
        RANK_C_MOBS = mobList(builder, "rankC", List.of("minecraft:zombie","minecraft:skeleton","minecraft:spider","minecraft:husk","minecraft:drowned","minecraft:slime","minecraft:witch","minecraft:pillager","minecraft:ravager","minecraft:stray","minecraft:blaze","minecraft:magma_cube"));
        RANK_B_MOBS = mobList(builder, "rankB", List.of("minecraft:witch","minecraft:pillager","minecraft:ravager","minecraft:drowned","minecraft:blaze","minecraft:husk","minecraft:cave_spider","minecraft:vindicator","minecraft:wither_skeleton","minecraft:magma_cube"));
        RANK_A_MOBS = mobList(builder, "rankA", List.of("minecraft:wither_skeleton","minecraft:magma_cube","minecraft:piglin","minecraft:hoglin","minecraft:zoglin","minecraft:evoker","minecraft:husk","cataclysm:ignited_revenant","minecraft:cave_spider","minecraft:blaze"));
        RANK_S_MOBS = mobList(builder, "rankS", List.of("cataclysm:ignited_revenant","minecraft:blaze","mowziesmobs:ferrous_wroughtnaut","minecraft:husk"));
        BOSS_FALLBACK_MOBS = mobList(builder, "bossFallback", List.of("cataclysm:ignited_revenant","minecraft:blaze","mowziesmobs:ferrous_wroughtnaut","minecraft:husk"));
        builder.pop();

        builder.push("rewards");
        RANK_E_REWARDS = rewardList(builder, "rankE", List.of("minecraft:iron_ingot:2:4:38","minecraft:bread:2:5:34","minecraft:coal:4:10:28","minecraft:torch:8:16:24","minecraft:arrow:6:14:18","sologates:bronze_coin:1:2:18","minecraft:string:2:5:14","minecraft:bone:2:6:14","minecraft:slime_ball:1:3:8","minecraft:emerald:1:1:8","minecraft:diamond:1:1:2"));
        RANK_C_REWARDS = rewardList(builder, "rankC", List.of("minecraft:gold_ingot:2:5:30","minecraft:experience_bottle:4:8:28","minecraft:lapis_lazuli:6:14:24","minecraft:redstone:8:18:22","minecraft:glowstone_dust:3:8:18","sologates:silver_coin:1:2:16","minecraft:iron_ingot:3:7:16","minecraft:blaze_powder:2:5:12","minecraft:diamond:1:2:12","minecraft:golden_apple:1:1:5"));
        RANK_B_REWARDS = rewardList(builder, "rankB", List.of("minecraft:diamond:2:5:28","minecraft:experience_bottle:8:16:24","minecraft:emerald:2:6:18","sologates:gold_coin:1:2:16","minecraft:blaze_rod:1:3:15","minecraft:golden_apple:1:1:14","irons_spellbooks:common_ink:1:2:14","minecraft:netherite_scrap:1:1:8","minecraft:ghast_tear:1:1:8","minecraft:amethyst_shard:4:10:8","irons_spellbooks:uncommon_ink:1:1:6","minecraft:enchanted_golden_apple:1:1:2"));
        RANK_A_REWARDS = rewardList(builder, "rankA", List.of("minecraft:diamond:3:7:26","minecraft:experience_bottle:12:24:20","minecraft:netherite_scrap:1:2:16","minecraft:golden_apple:1:2:16","sologates:gold_coin:2:4:14","irons_spellbooks:uncommon_ink:1:2:10","minecraft:echo_shard:1:3:8","minecraft:totem_of_undying:1:1:6","sologates:platinum_coin:1:1:5","irons_spellbooks:rare_ink:1:1:5","minecraft:heart_of_the_sea:1:1:4","minecraft:enchanted_golden_apple:1:1:3"));
        RANK_S_REWARDS = rewardList(builder, "rankS", List.of("minecraft:diamond:4:10:22","minecraft:netherite_scrap:1:3:18","minecraft:experience_bottle:18:32:18","sologates:platinum_coin:1:2:14","minecraft:totem_of_undying:1:1:10","irons_spellbooks:rare_ink:1:2:8","minecraft:dragon_breath:1:3:8","minecraft:echo_shard:2:5:8","minecraft:netherite_ingot:1:1:7","minecraft:enchanted_golden_apple:1:1:5","minecraft:nether_star:1:1:2"));
        BOSS_REWARDS = rewardList(builder, "boss", List.of("sologates:platinum_coin:2:4:18","minecraft:experience_bottle:24:48:18","minecraft:netherite_ingot:1:2:16","minecraft:totem_of_undying:1:1:16","irons_spellbooks:rare_ink:1:2:10","minecraft:dragon_breath:2:5:10","minecraft:echo_shard:3:6:10","minecraft:enchanted_golden_apple:1:1:10","minecraft:nether_star:1:1:5"));
        builder.pop();

        builder.push("xp");
        XP_REWARD_E = builder.comment("XP accordee au joueur apres completion rang E.").defineInRange("xpRewardE", 15, 0, 10000);
        XP_REWARD_C = builder.comment("XP accordee au joueur apres completion rang C.").defineInRange("xpRewardC", 30, 0, 10000);
        XP_REWARD_B = builder.comment("XP accordee au joueur apres completion rang B.").defineInRange("xpRewardB", 60, 0, 10000);
        XP_REWARD_A = builder.comment("XP accordee au joueur apres completion rang A.").defineInRange("xpRewardA", 100, 0, 10000);
        XP_REWARD_S = builder.comment("XP accordee au joueur apres completion rang S.").defineInRange("xpRewardS", 200, 0, 10000);
        builder.pop();

        builder.push("difficulty");
        MOB_HP_MULT_E = builder.comment("Multiplicateur de PV des mobs rang E. 1.0 = normal.").defineInRange("mobHpMultE", 1.0, 0.1, 20.0);
        MOB_HP_MULT_C = builder.comment("Multiplicateur de PV des mobs rang C.").defineInRange("mobHpMultC", 1.5, 0.1, 20.0);
        MOB_HP_MULT_B = builder.comment("Multiplicateur de PV des mobs rang B.").defineInRange("mobHpMultB", 2.5, 0.1, 20.0);
        MOB_HP_MULT_A = builder.comment("Multiplicateur de PV des mobs rang A.").defineInRange("mobHpMultA", 3.4, 0.1, 20.0);
        MOB_HP_MULT_S = builder.comment("Multiplicateur de PV des mobs rang S.").defineInRange("mobHpMultS", 5.95, 0.1, 20.0);
        INVASION_PERCENT = builder.comment("Pourcentage des mobs restants qui envahissent l'Overworld si echec (0-100).").defineInRange("invasionPercent", 60, 0, 100);
        builder.pop();

        SPEC = builder.build();
    }
}
