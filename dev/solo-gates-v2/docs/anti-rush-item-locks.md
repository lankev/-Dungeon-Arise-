# Anti-rush item locks

Objectif: eviter que les joueurs obtiennent trop vite des objets qui cassent la progression du serveur.

Cette liste est une base de travail pour GameStages / KubeJS. Elle ne bloque rien toute seule.

## Stages proposes

- `rank_e`: debut normal.
- `rank_c`: premiers boss/mods intermediaires.
- `rank_b`: debut mid-game avance, magie serieuse, gros donjons.
- `rank_a`: end-game, objets de boss et puissance avancee.
- `rank_s`: contenu boss final, armes uniques, objets qui changent completement le combat.

## A bloquer jusqu'au rang C

Ces objets donnent trop de puissance directe pour le tout debut.

- `skilltree:wisdom_scroll`
- `skilltree:amnesia_scroll`

## A bloquer jusqu'au rang B

Ces objets accelerent fortement les combats ou donnent des pouvoirs de boss.

- `mowziesmobs:wrought_axe`
- `mowziesmobs:wrought_helmet`
- `mowziesmobs:ice_crystal`
- `mowziesmobs:earth_talisman`
- `mowziesmobs:earthrend_gauntlet`
- `mowziesmobs:barako_mask`
- `mowziesmobs:grant_suns_blessing`
- `apotheosis:boss_summoner`
- `apotheosis:mythic_material`
- `apotheosis:ancient_material`
- `apotheosis:sigil_of_socketing`
- `apotheosis:sigil_of_withdrawal`
- `apotheosis:sigil_of_rebirth`
- `apotheosis:sigil_of_enhancement`
- `apotheosis:vial_of_extraction`
- `apotheosis:vial_of_expulsion`
- `apotheosis:infused_breath`
- `apotheosis:warden_tendril`
- `cataclysm:infernal_forge`
- `cataclysm:gauntlet_of_guard`
- `cataclysm:void_core`
- `cataclysm:bulwark_of_the_flame`
- `cataclysm:cursed_bow`
- `cataclysm:annihilator`
- `cataclysm:soul_render`
- `cataclysm:void_forge`
- `cataclysm:incinerator`
- `cataclysm:tidal_claws`
- `cataclysm:meat_shredder`
- `cataclysm:laser_gatling`
- `cataclysm:gauntlet_of_maelstrom`
- `cataclysm:ceraunus`
- `cataclysm:astrape`
- `cataclysm:ignitium_helmet`
- `cataclysm:ignitium_chestplate`
- `cataclysm:ignitium_leggings`
- `cataclysm:ignitium_boots`
- `cataclysm:cursium_helmet`
- `cataclysm:cursium_chestplate`
- `cataclysm:cursium_leggings`
- `cataclysm:cursium_boots`

## A bloquer jusqu'au rang A

Ces objets doivent rester des objectifs finaux ou des recompenses tres rares.

- `cataclysm:final_fractal`
- `cataclysm:zweiender`
- `cataclysm:void_assault_shoulder_weapon`
- `cataclysm:wither_assault_shoulder_weapon`
- `cataclysm:wrath_of_the_desert`
- `cataclysm:immolator`
- `cataclysm:gauntlet_of_bulwark`
- `cataclysm:bloom_stone_pauldrons`
- `cataclysm:monstrous_helm`
- `cataclysm:vitality_ankh`
- `cataclysm:berserker_soul_amulet`
- `cataclysm:unbreakable_skull`
- `cataclysm:witherite_ingot`
- `cataclysm:enderite_ingot`
- `cataclysm:ancient_metal_ingot`
- `apotheosis:gem`
- `apotheosis:gem_dust`
- `apotheosis:gem_fused_slate`

## Better Weaponry

Ce mod est marque `All Rights Reserved` dans l'audit CurseForge. Si on le garde dans le pack, il faut verifier l'export CurseForge.

Locks recommandes si conserve:

- Ne pas verrouiller les familles d'armes standards: halberd, katana, longsword, spear, glaive, claymore, greataxe, hammer, scythe et chakram.
- Tester seulement les objets non standards ou effets vraiment exceptionnels avant de les mettre dans les loots ou boutiques.

## A verifier en jeu

Certains objets peuvent etre moins forts selon leur config, mais il faut les tester:

- `mowziesmobs:geomancer_beads`
- `mowziesmobs:sol_visage`
- `mowziesmobs:naga_fang_dagger`
- `irons_spellbooks:*` sorts de haut niveau, livres de sorts rares et armures de mage.

## Recommandation serveur

Pour un serveur public, commencer par bloquer:

1. Armes et objets de boss: Cataclysm, Mowzie's Mobs.
2. Puissance d'enchant/gemmes: Apotheosis.
3. Points de competence: Passive Skill Tree.

Ensuite seulement, ajuster selon les retours des joueurs.
