// Anti-rush item locks - Solo Gates progression.
// Blocks use/equipment of selected items until the required hunter rank GameStage.

var GameStageHelper = Java.loadClass('net.darkhax.gamestages.GameStageHelper');
var EquipmentSlot = Java.loadClass('net.minecraft.world.entity.EquipmentSlot');
var ItemStack = Java.loadClass('net.minecraft.world.item.ItemStack');

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

var STAGE_ORDER = ['rank_c', 'rank_b', 'rank_a', 'rank_s'];
var MESSAGES = {
    rank_c: '[Verrouille] Rang chasseur C requis.',
    rank_b: '[Verrouille] Rang chasseur B requis.',
    rank_a: '[Verrouille] Rang chasseur A requis.'
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

function hasRequiredStage(player, stage) {
    var requiredIndex = STAGE_ORDER.indexOf(stage);
    if (requiredIndex < 0) return GameStageHelper.hasStage(player, stage);

    for (var i = requiredIndex; i < STAGE_ORDER.length; i++) {
        if (GameStageHelper.hasStage(player, STAGE_ORDER[i])) return true;
    }
    return false;
}

function isLocked(player, itemId) {
    var stage = ITEM_STAGE[itemId];
    return !!stage && !hasRequiredStage(player, stage);
}

function sendLockMessage(player, itemId) {
    var stage = ITEM_STAGE[itemId];
    if (stage) player.displayClientMessage(Text.of(MESSAGES[stage]), true);
}

function cancelLockedItemUse(event, player, item) {
    if (!player || !item || item.isEmpty() || !isLocked(player, item.id)) return;

    event.cancel();
    sendLockMessage(player, item.id);
}

ItemEvents.rightClicked(function(event) {
    cancelLockedItemUse(event, event.player, event.item);
});

BlockEvents.rightClicked(function(event) {
    cancelLockedItemUse(event, event.player, event.item);
});

var ARMOR_SLOTS = [EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET];

PlayerEvents.tick(function(event) {
    var player = event.player;
    if (!player || player.tickCount % 20 !== 0) return;

    ARMOR_SLOTS.forEach(function(slot) {
        var item = player.getItemBySlot(slot);
        if (!item || item.isEmpty() || !isLocked(player, item.id)) return;

        player.setItemSlot(slot, ItemStack.EMPTY);
        player.drop(item, false);
        sendLockMessage(player, item.id);
    });
});

console.log('[Anti-Rush] Loaded ' + Object.keys(ITEM_STAGE).length + ' Solo Gates item locks.');
