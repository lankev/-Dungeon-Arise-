# Brief pour Claude - Anti-rush items / GameStages

Projet: Solo Gates / modpack Forge 1.20.1

Objectif: transformer la liste anti-rush en vraies regles GameStages/KubeJS, sans bloquer les objets que le joueur veut garder libres.

## Contexte important

- Le mod Solo Gates gere deja une progression de rangs: E, C, B, A, S.
- Solo Gates sauvegarde le rang joueur et les statistiques.
- Solo Gates ajoute automatiquement un GameStage quand le joueur monte de rang:
  - `rank_c`
  - `rank_b`
  - `rank_a`
  - `rank_s`
- Les quetes FTB existent deja par chapitres de rang dans:
  - `config/ftbquests/quests/chapters/rank_e.snbt`
  - `rank_d.snbt`
  - `rank_c.snbt`
  - `rank_b.snbt`
  - `rank_a.snbt`
  - `rank_s.snbt`
  - `rank_splus.snbt`

## Demande du joueur

Implementer des locks anti-rush pour certains items de mods, mais ne PAS bloquer:

- les items vanilla;
- les objets Waystones;
- les artefacts / Curios du mod Artifacts;
- les familles d'armes standards suivantes:
  - halberd
  - katana
  - longsword
  - spear
  - glaive
  - claymore
  - greataxe
  - hammer
  - scythe
  - chakram

Donc ne pas faire de regle globale qui bloque toutes les armes Simply Swords ou Better Weaponry par type.

## Stages souhaites

- Rang E: debut normal.
- Rang C: premiers locks legers.
- Rang B: gros objets de boss / magie / Apotheosis / Cataclysm.
- Rang A: objets finaux tres puissants.
- Rang S: pas de nouvelle section de lock pour le moment.

## Items a bloquer jusqu'au rang C

Ces items doivent necessiter au minimum `rank_c`.

```txt
skilltree:wisdom_scroll
skilltree:amnesia_scroll
```

## Items a bloquer jusqu'au rang B

Ces items doivent necessiter au minimum `rank_b`.

```txt
mowziesmobs:wrought_axe
mowziesmobs:wrought_helmet
mowziesmobs:ice_crystal
mowziesmobs:earth_talisman
mowziesmobs:earthrend_gauntlet
mowziesmobs:barako_mask
mowziesmobs:grant_suns_blessing
apotheosis:boss_summoner
apotheosis:mythic_material
apotheosis:ancient_material
apotheosis:sigil_of_socketing
apotheosis:sigil_of_withdrawal
apotheosis:sigil_of_rebirth
apotheosis:sigil_of_enhancement
apotheosis:vial_of_extraction
apotheosis:vial_of_expulsion
apotheosis:infused_breath
apotheosis:warden_tendril
cataclysm:infernal_forge
cataclysm:gauntlet_of_guard
cataclysm:void_core
cataclysm:bulwark_of_the_flame
cataclysm:cursed_bow
cataclysm:annihilator
cataclysm:soul_render
cataclysm:void_forge
cataclysm:incinerator
cataclysm:tidal_claws
cataclysm:meat_shredder
cataclysm:laser_gatling
cataclysm:gauntlet_of_maelstrom
cataclysm:ceraunus
cataclysm:astrape
cataclysm:ignitium_helmet
cataclysm:ignitium_chestplate
cataclysm:ignitium_leggings
cataclysm:ignitium_boots
cataclysm:cursium_helmet
cataclysm:cursium_chestplate
cataclysm:cursium_leggings
cataclysm:cursium_boots
```

## Items a bloquer jusqu'au rang A

Ces items doivent necessiter au minimum `rank_a`.

```txt
cataclysm:final_fractal
cataclysm:zweiender
cataclysm:void_assault_shoulder_weapon
cataclysm:wither_assault_shoulder_weapon
cataclysm:wrath_of_the_desert
cataclysm:immolator
cataclysm:gauntlet_of_bulwark
cataclysm:bloom_stone_pauldrons
cataclysm:monstrous_helm
cataclysm:vitality_ankh
cataclysm:berserker_soul_amulet
cataclysm:unbreakable_skull
cataclysm:witherite_ingot
cataclysm:enderite_ingot
cataclysm:ancient_metal_ingot
apotheosis:gem
apotheosis:gem_dust
apotheosis:gem_fused_slate
```

## A verifier manuellement

Ne pas bloquer automatiquement, mais tester en jeu:

```txt
mowziesmobs:geomancer_beads
mowziesmobs:sol_visage
mowziesmobs:naga_fang_dagger
irons_spellbooks:*
```

Pour `irons_spellbooks:*`, le joueur veut surtout eviter de bloquer trop large sans test. Idealement, identifier seulement les sorts/livres/armures vraiment end-game.

## Implementation souhaitee

Idealement:

1. Ajouter des regles KubeJS/GameStages dans l'instance modpack, pas dans le code Java Solo Gates sauf necessaire.
2. Bloquer l'utilisation/equipement des items si le joueur n'a pas le stage.
3. Si possible, cacher ou marquer les recettes/JEI pour les items lockes.
4. Prevoir des messages joueur clairs:
   - "Objet verrouille: rang C requis"
   - "Objet verrouille: rang B requis"
   - "Objet verrouille: rang A requis"
5. Ne pas bloquer les items deja explicitement exclus par le joueur.

## Fichier source de la liste

La liste de travail est aussi dans:

```txt
solo-gates-v2/docs/anti-rush-item-locks.md
```

Merci de ne pas toucher aux textures graphiques Solo Gates sans confirmation, car Codex s'occupe de la partie graphique.
