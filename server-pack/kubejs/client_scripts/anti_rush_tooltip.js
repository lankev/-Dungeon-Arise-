// Anti-rush tooltips - shows the hunter rank required by locked items.

var LOCKED_ITEMS = {
    rank_c: [
        'skilltree:amnesia_scroll',
        'skilltree:wisdom_scroll'
    ],

    rank_b: [
        'apotheosis:ancient_material',
        'apotheosis:boss_summoner',
        'apotheosis:infused_breath',
        'apotheosis:mythic_material',
        'apotheosis:sigil_of_enhancement',
        'apotheosis:sigil_of_rebirth',
        'apotheosis:sigil_of_socketing',
        'apotheosis:sigil_of_withdrawal',
        'apotheosis:vial_of_expulsion',
        'apotheosis:vial_of_extraction',
        'apotheosis:warden_tendril',
        'cataclysm:annihilator',
        'cataclysm:astrape',
        'cataclysm:bulwark_of_the_flame',
        'cataclysm:ceraunus',
        'cataclysm:cursed_bow',
        'cataclysm:cursium_boots',
        'cataclysm:cursium_chestplate',
        'cataclysm:cursium_helmet',
        'cataclysm:cursium_leggings',
        'cataclysm:gauntlet_of_guard',
        'cataclysm:gauntlet_of_maelstrom',
        'cataclysm:ignitium_boots',
        'cataclysm:ignitium_chestplate',
        'cataclysm:ignitium_helmet',
        'cataclysm:ignitium_leggings',
        'cataclysm:incinerator',
        'cataclysm:infernal_forge',
        'cataclysm:laser_gatling',
        'cataclysm:meat_shredder',
        'cataclysm:soul_render',
        'cataclysm:tidal_claws',
        'cataclysm:void_core',
        'cataclysm:void_forge',
        'mowziesmobs:barako_mask',
        'mowziesmobs:earth_talisman',
        'mowziesmobs:earthrend_gauntlet',
        'mowziesmobs:grant_suns_blessing',
        'mowziesmobs:ice_crystal',
        'mowziesmobs:wrought_axe',
        'mowziesmobs:wrought_helmet'
    ],

    rank_a: [
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
        'cataclysm:zweiender'
    ]
};

var TOOLTIPS = {
    rank_c: Text.of('[Solo Gates] Rang chasseur C requis'),
    rank_b: Text.of('[Solo Gates] Rang chasseur B requis'),
    rank_a: Text.of('[Solo Gates] Rang chasseur A requis')
};
var ITEM_STAGE = buildItemStageIndex(LOCKED_ITEMS);

function buildItemStageIndex(lockedItems) {
    var index = {};
    Object.keys(lockedItems).forEach(function(stage) {
        lockedItems[stage].forEach(function(itemId) {
            index[itemId] = stage;
        });
    });
    return index;
}

ItemEvents.tooltip(function(event) {
    if (!event.item || event.item.isEmpty()) return;

    var stage = ITEM_STAGE[event.item.id];
    if (stage) event.add(TOOLTIPS[stage]);
});
