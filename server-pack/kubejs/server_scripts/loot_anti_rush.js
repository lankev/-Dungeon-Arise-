// Optional loot anti-rush.
// Requires LootJS. Without LootJS, item use/equipment protection still works via anti_rush.js.

try {
    if (typeof LootJS === 'undefined') throw new Error('LootJS not available');

    var ALWAYS_REMOVE = [
        'apotheosis:gem',
        'apotheosis:gem_dust',
        'apotheosis:gem_fused_slate',
        'cataclysm:ancient_metal_ingot',
        'cataclysm:berserker_soul_amulet',
        'cataclysm:bloom_stone_pauldrons',
        'cataclysm:enderite_ingot',
        'cataclysm:final_fractal',
        'cataclysm:gauntlet_of_bulwark',
        'cataclysm:immolator',
        'cataclysm:monstrous_helm',
        'cataclysm:unbreakable_skull',
        'cataclysm:vitality_ankh',
        'cataclysm:void_assault_shoulder_weapon',
        'cataclysm:wither_assault_shoulder_weapon',
        'cataclysm:witherite_ingot',
        'cataclysm:wrath_of_the_desert',
        'cataclysm:zweiender',
        'mowziesmobs:barako_mask',
        'mowziesmobs:earth_talisman',
        'mowziesmobs:earthrend_gauntlet',
        'mowziesmobs:grant_suns_blessing',
        'mowziesmobs:ice_crystal',
        'mowziesmobs:wrought_axe',
        'mowziesmobs:wrought_helmet'
    ];

    var REMOVE_FROM_STRUCTURES = ALWAYS_REMOVE.concat([
        'apotheosis:ancient_material',
        'apotheosis:boss_summoner',
        'apotheosis:mythic_material',
        'apotheosis:sigil_of_enhancement',
        'apotheosis:sigil_of_rebirth',
        'apotheosis:sigil_of_socketing',
        'apotheosis:sigil_of_withdrawal',
        'cataclysm:annihilator',
        'cataclysm:cursed_bow',
        'cataclysm:cursium_boots',
        'cataclysm:cursium_chestplate',
        'cataclysm:cursium_helmet',
        'cataclysm:cursium_leggings',
        'cataclysm:ignitium_helmet',
        'cataclysm:ignitium_chestplate',
        'cataclysm:ignitium_leggings',
        'cataclysm:ignitium_boots',
        'cataclysm:incinerator',
        'cataclysm:soul_render'
    ]);

    var STRUCTURE_TABLES = [
        'dungeons_arise:chests/abandoned_scripts',
        'dungeons_arise:chests/arena',
        'dungeons_arise:chests/bandit_towers',
        'dungeons_arise:chests/beholder_dungeon',
        'dungeons_arise:chests/desert_temple',
        'dungeons_arise:chests/enchanted_volcanic_dungeon',
        'dungeons_arise:chests/forge',
        'dungeons_arise:chests/illagers_lair',
        'dungeons_arise:chests/jungle_dungeons',
        'dungeons_arise:chests/lost_city',
        'dungeons_arise:chests/mechanical_nest',
        'dungeons_arise:chests/mushroom_castle',
        'dungeons_arise:chests/plague_dungeon',
        'dungeons_arise:chests/shiraz_palace',
        'dungeons_arise:chests/skeleton_horsemen',
        'dungeons_arise:chests/thornborn_towers',
        'dungeons_arise:chests/undead_navy',
        'dungeons_arise:chests/volcanic_dungeon',
        'dungeon_crawl:treasure',
        'dungeon_crawl:chest',
        'dungeon_crawl:equipment',
        'dungeon_crawl:equipment_enchanted',
        'dungeon_crawl:reward',
        'dungeon_crawl:secret_room',
        'yungsbetterdungeons:chests/skeleton_dungeon',
        'yungsbetterdungeons:chests/zombie_dungeon',
        'yungsbetterdungeons:chests/spider_dungeon',
        'yungsbettermineshafts:mineshaft_chest'
    ];

    var CATACLYSM_BOSSES = [
        'cataclysm:entities/ignited_revenant',
        'cataclysm:entities/netherite_monstrosity',
        'cataclysm:entities/ender_golem',
        'cataclysm:entities/ender_guardian',
        'cataclysm:entities/ignis',
        'cataclysm:entities/the_harbinger'
    ];

    LootJS.modifiers(function(event) {
        STRUCTURE_TABLES.forEach(function(tableId) {
            event.addTableModifier(tableId)
                .removeLoot(function(entry) { return REMOVE_FROM_STRUCTURES.indexOf(entry.getId()) !== -1; });
        });

        CATACLYSM_BOSSES.forEach(function(tableId) {
            event.addTableModifier(tableId)
                .removeLoot(function(entry) { return ALWAYS_REMOVE.indexOf(entry.getId()) !== -1; });
        });
    });

    console.log('[Anti-Rush] Loot table modifiers loaded with LootJS.');
} catch (e) {
    console.info('[Anti-Rush] LootJS not installed; loot_anti_rush.js skipped.');
}
