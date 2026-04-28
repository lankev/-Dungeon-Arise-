/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.common.ForgeConfigSpec
 *  net.minecraftforge.common.ForgeConfigSpec$Builder
 *  net.minecraftforge.common.ForgeConfigSpec$ConfigValue
 *  net.minecraftforge.common.ForgeConfigSpec$IntValue
 */
package fr.matis.sologates;

import fr.matis.sologates.GateRank;
import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;

public final class SoloGatesConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue MAX_ACTIVE_GATES;
    public static final ForgeConfigSpec.IntValue WORLD_SPAWN_INTERVAL_SECONDS;
    public static final ForgeConfigSpec.IntValue PLAYER_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.IntValue SPAWN_RADIUS_MIN;
    public static final ForgeConfigSpec.IntValue SPAWN_RADIUS_MAX;
    public static final ForgeConfigSpec.IntValue GATE_LIFETIME_SECONDS;
    public static final ForgeConfigSpec.IntValue BOSS_GATE_CHANCE_PERCENT;
    public static final ForgeConfigSpec.IntValue BOSS_MOB_COUNT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_WEIGHTS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_SPAWN_INTERVALS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_E_MOBS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_C_MOBS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_B_MOBS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_A_MOBS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_S_MOBS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BOSS_FALLBACK_MOBS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_E_REWARDS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_C_REWARDS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_B_REWARDS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_A_REWARDS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANK_S_REWARDS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BOSS_REWARDS;

    private static ForgeConfigSpec.ConfigValue<List<? extends String>> mobList(ForgeConfigSpec.Builder builder, String name, List<String> defaults) {
        return builder.comment("Ids d'entites possibles pour ce rang.").defineList(name, defaults, value -> {
            String string;
            return value instanceof String && (string = (String)value).contains(":");
        });
    }

    private static ForgeConfigSpec.ConfigValue<List<? extends String>> rewardList(ForgeConfigSpec.Builder builder, String name, List<String> defaults) {
        return builder.comment("Items possibles: item:min:max:poids. Exemple minecraft:diamond:1:3:20. L'ancien format item:min:max marche aussi.").defineList(name, defaults, value -> {
            String string;
            return value instanceof String && (string = (String)value).split(":").length >= 4;
        });
    }

    private static boolean isWeightedRank(Object value) {
        String string;
        if (!(value instanceof String) || !(string = (String)value).contains("=")) {
            return false;
        }
        String rank = string.substring(0, string.indexOf(61));
        try {
            GateRank.valueOf(rank);
            return Integer.parseInt(string.substring(string.indexOf(61) + 1)) > 0;
        }
        catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private SoloGatesConfig() {
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("spawn");
        MAX_ACTIVE_GATES = builder.comment("Maximum de portails actifs en meme temps dans l'Overworld. Garder bas pour serveur.").defineInRange("maxActiveGates", 2, 1, 16);
        WORLD_SPAWN_INTERVAL_SECONDS = builder.comment("Temps minimum entre deux tentatives de spawn globales.").defineInRange("worldSpawnIntervalSeconds", 600, 60, 86400);
        PLAYER_COOLDOWN_SECONDS = builder.comment("Cooldown par joueur avant qu'un portail puisse apparaitre autour de lui.").defineInRange("playerCooldownSeconds", 1200, 60, 86400);
        SPAWN_RADIUS_MIN = builder.defineInRange("spawnRadiusMin", 50, 8, 512);
        SPAWN_RADIUS_MAX = builder.defineInRange("spawnRadiusMax", 80, 16, 1024);
        GATE_LIFETIME_SECONDS = builder.comment("Temps avant disparition d'un portail non termine. 300 = environ 5 minutes.").defineInRange("gateLifetimeSeconds", 300, 60, 172800);
        RANK_WEIGHTS = builder.comment("Poids des rangs: RANG=poids. Plus le poids est haut, plus le rang sort souvent.").defineList("rankWeights", List.of("E=58", "C=24", "B=11", "A=5", "S=2"), SoloGatesConfig::isWeightedRank);
        RANK_SPAWN_INTERVALS = builder.comment("Cooldown global apres spawn selon le rang: RANG=secondes. Les rangs faciles reviennent plus vite.").defineList("rankSpawnIntervals", List.of("E=600", "C=900", "B=1200", "A=1500", "S=1800"), SoloGatesConfig::isWeightedRank);
        BOSS_GATE_CHANCE_PERCENT = builder.comment("Chance qu'une tentative de portail devienne un portail boss. 1 = tres rare.").defineInRange("bossGateChancePercent", 1, 0, 100);
        BOSS_MOB_COUNT = builder.comment("Nombre de mobs forts dans un portail boss. Pas de boss bar speciale.").defineInRange("bossMobCount", 3, 1, 16);
        builder.pop();
        builder.push("mobs");
        RANK_E_MOBS = SoloGatesConfig.mobList(builder, "rankE", List.of("minecraft:zombie", "minecraft:skeleton", "minecraft:spider", "minecraft:husk", "minecraft:drowned", "minecraft:slime"));
        RANK_C_MOBS = SoloGatesConfig.mobList(builder, "rankC", List.of("minecraft:zombie", "minecraft:skeleton", "minecraft:spider", "minecraft:husk", "minecraft:drowned", "minecraft:slime", "minecraft:witch", "minecraft:pillager", "minecraft:ravager", "minecraft:stray", "minecraft:blaze", "minecraft:magma_cube"));
        RANK_B_MOBS = SoloGatesConfig.mobList(builder, "rankB", List.of("minecraft:witch", "minecraft:pillager", "minecraft:ravager", "minecraft:drowned", "minecraft:blaze", "minecraft:husk", "minecraft:cave_spider", "minecraft:vindicator", "minecraft:wither_skeleton", "minecraft:magma_cube"));
        RANK_A_MOBS = SoloGatesConfig.mobList(builder, "rankA", List.of("minecraft:wither_skeleton", "minecraft:magma_cube", "minecraft:piglin", "minecraft:hoglin", "minecraft:zoglin", "minecraft:evoker", "minecraft:husk", "cataclysm:ignited_revenant", "minecraft:enderman", "minecraft:cave_spider", "minecraft:blaze"));
        RANK_S_MOBS = SoloGatesConfig.mobList(builder, "rankS", List.of("cataclysm:ignited_revenant", "minecraft:blaze", "mowziesmobs:ferrous_wroughtnaut", "minecraft:husk"));
        BOSS_FALLBACK_MOBS = SoloGatesConfig.mobList(builder, "bossFallback", List.of("cataclysm:ignited_revenant", "minecraft:blaze", "mowziesmobs:ferrous_wroughtnaut", "minecraft:husk"));
        builder.pop();
        builder.push("rewards");
        RANK_E_REWARDS = SoloGatesConfig.rewardList(builder, "rankE", List.of("minecraft:iron_ingot:2:6:45", "minecraft:bread:3:8:35", "minecraft:emerald:1:2:12", "minecraft:diamond:1:1:3"));
        RANK_C_REWARDS = SoloGatesConfig.rewardList(builder, "rankC", List.of("minecraft:gold_ingot:3:8:35", "minecraft:experience_bottle:6:14:30", "minecraft:diamond:1:3:18", "minecraft:golden_apple:1:1:7"));
        RANK_B_REWARDS = SoloGatesConfig.rewardList(builder, "rankB", List.of("minecraft:diamond:3:8:35", "minecraft:netherite_scrap:1:2:14", "minecraft:golden_apple:1:2:18", "minecraft:enchanted_golden_apple:1:1:3"));
        RANK_A_REWARDS = SoloGatesConfig.rewardList(builder, "rankA", List.of("minecraft:diamond:5:10:32", "minecraft:netherite_scrap:1:3:18", "minecraft:golden_apple:1:3:18", "minecraft:totem_of_undying:1:1:5", "minecraft:enchanted_golden_apple:1:1:4"));
        RANK_S_REWARDS = SoloGatesConfig.rewardList(builder, "rankS", List.of("minecraft:diamond:8:16:28", "minecraft:netherite_scrap:2:5:20", "minecraft:netherite_ingot:1:2:8", "minecraft:totem_of_undying:1:1:8", "minecraft:enchanted_golden_apple:1:2:4"));
        BOSS_REWARDS = SoloGatesConfig.rewardList(builder, "boss", List.of("minecraft:netherite_ingot:2:4:20", "minecraft:totem_of_undying:1:2:18", "minecraft:enchanted_golden_apple:1:2:12", "minecraft:nether_star:1:1:4"));
        builder.pop();
        SPEC = builder.build();
    }
}

