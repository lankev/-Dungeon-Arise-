# Brief Claude - Solo Gates v0.2.1

Date: 02/05/2026
Version mod: 0.2.1
Minecraft: 1.20.1
Forge: 47.3.22

Ce fichier sert de passation pour reprendre le projet sans perdre le contexte.

## Regle de collaboration

Codex a surtout travaille sur:
- visuels des gates et armures;
- configuration gameplay Solo Gates;
- loot, spawn et equilibre;
- documentation joueur;
- systeme Crime Stat v0.2.1.

L'objectif est d'eviter d'ecraser du travail en cours. Avant de modifier une zone deja touchee, relire les fichiers concernes.

## Version 0.2.1

Le mod est passe de `0.2.0` a `0.2.1` dans `gradle.properties`.

Jar installe dans l'instance CurseForge:
- `C:\Users\matis\curseforge\minecraft\Instances\level\mods\sologates-0.2.1.jar`

Ancien jar deplace pour eviter les doublons de mod id:
- `C:\Users\matis\curseforge\minecraft\Instances\level\mods\_old_sologates\sologates-0.2.0.jar`

## Crime Stat

Nouveau systeme PvP ajoute dans Solo Gates.

Fichiers principaux:
- `src/main/java/fr/matis/sologates/CrimeStatData.java`
- `src/main/java/fr/matis/sologates/CrimeSavedData.java`
- `src/main/java/fr/matis/sologates/CrimeStatManager.java`
- `src/main/java/fr/matis/sologates/SoloGatesEvents.java`
- `src/main/java/fr/matis/sologates/network/SyncRankPacket.java`
- `src/main/java/fr/matis/sologates/network/SoloGatesNetwork.java`
- `src/main/java/fr/matis/sologates/client/ClientPlayerData.java`
- `src/main/java/fr/matis/sologates/client/HunterRankHud.java`

### Regles Crime Stat

- Un joueur qui tue un citoyen passe Crime Stat 1.
- Deux kills citoyens: Crime Stat 2.
- Trois kills citoyens: Crime Stat 3.
- Le Crime Stat est bloque a 3 maximum.
- Le Crime Stat est permanent.
- Il est efface uniquement quand le criminel est tue par un autre joueur.
- Le Crime Stat s'affiche sous le rang de chasseur dans le HUD.

### Vol de points Passive Skill Tree

Integration directe avec Passive Skill Tree:
- `daripher.skilltree.capability.skill.PlayerSkillsProvider`
- `IPlayerSkills#getSkillPoints`
- `IPlayerSkills#setSkillPoints`
- `IPlayerSkills#grantSkillPoints`
- sync client via `SyncPlayerSkillsMessage`

Regles:
- Un criminel peut voler 1 point de competence maximum par kill citoyen.
- Crime Stat 1: maximum 2 tentatives de vol sur 24h.
- Crime Stat 2: maximum 3 tentatives de vol sur 24h.
- Crime Stat 3: maximum 4 tentatives de vol sur 24h.
- Si la victime a 0 point disponible, le criminel ne gagne rien.
- Meme si rien n'est vole, la tentative consomme quand meme une charge de vol.
- La limite de 24h est stockee par timestamps dans `CrimeStatData`.

### Primes

Quand un citoyen tue un criminel:
- le Crime Stat du criminel est efface;
- le citoyen recupere une prime en points disponibles;
- Crime Stat 1: jusqu'a 1 point;
- Crime Stat 2: jusqu'a 2 points;
- Crime Stat 3: jusqu'a 3 points;
- si le criminel a 0 point disponible, la prime vaut 0.

Note importante:
- Le systeme transfere uniquement les points de competence disponibles/non depenses.
- Il ne retire pas une competence deja apprise dans Passive Skill Tree.

## Dependances

Ajout dans `build.gradle`:

```gradle
compileOnly files('C:/Users/matis/curseforge/minecraft/Instances/level/mods/PassiveSkillTree-1.20.1-BETA-0.7.4-all.jar')
```

Ajout dans `mods.toml`:
- dependance obligatoire `skilltree`
- versionRange `[0.7.4,)`
- ordering `AFTER`

## Spawns de gates

Le maximum de portails actifs est passe a 4:
- valeur par defaut dans `SoloGatesConfig.java`;
- configs de mondes existants patchees avec `maxActiveGates = 4`.

Bonus de spawn selon rang joueur:
- nouvelle option `playerRankGateBonusPercent = 5`;
- si un portail naturel spawn autour d'un joueur rang C/B/A/S, il a 5% de chance d'etre du rang confirme du joueur;
- les boss gates ne sont pas affectees;
- rang E ne force rien.

## Loots des coffres de fin de gates

Les listes de recompenses par rang ont ete diversifiees dans `SoloGatesConfig.java`.

Objectif:
- plus de types d'items;
- pas forcement plus de quantite;
- garder une progression lisible par rang.

Ajouts notables:
- ressources utilitaires bas rang;
- pieces Solo Gates;
- XP bottles;
- ressources Nether;
- echo shards;
- dragon breath;
- primes plus rares haut rang.

Les `sologates-server.toml` des mondes CurseForge existants ont aussi ete mis a jour.

## Difficultes et mobs

Endermen:
- `minecraft:enderman` retire de la liste par defaut des mobs rang A.
- Attention: verifier les vieux `sologates-server.toml` si un monde a ete ouvert pendant un patch precedent.

PV mobs:
- Rang A: multiplicateur baisse a `3.4`.
- Rang S: multiplicateur baisse a `5.95`.
- Les configs de monde existantes ont ete patchees.

## Visuels

Armures:
- textures `rank_e/c/b/a/s_layer_1.png` et `layer_2.png` refaites;
- but: rendu plus propre et plus lisible en jeu.

Gates:
- texture `gate_vortex.png` retravaillee;
- effet vortex plus lisible;
- bords irreguliers;
- opacite reduite pour garder le portail visible.

Portail retour:
- le retour utilise maintenant aussi une entite de gate;
- correction du portail retour invisible.

HUD armure:
- affichage Solo Gates autour de l'armure retire/desactive car trop envahissant.

## Changelog joueur

Le changelog joueur est dans:
- `changelog.txt`

La section v0.2.1 a ete ajoutee en haut du fichier.

## Build

Commande de build utilisee avec succes:

```powershell
$env:JAVA_HOME='C:\Users\matis\curseforge\minecraft\Install\runtime\java-runtime-gamma\windows-x64\java-runtime-gamma'; $env:PATH="$env:JAVA_HOME\bin;$env:PATH"; $env:GRADLE_USER_HOME=(Join-Path (Get-Location) '.gradle-cache'); .\gradlew.bat build
```

Dernier build:
- success;
- jar genere: `build/libs/sologates-0.2.1.jar`.

## Points a surveiller

- Tester le Crime Stat en vrai multi avec deux joueurs.
- Verifier que Passive Skill Tree met bien a jour le HUD client apres transfert de points.
- Verifier que le Crime Stat ne s'applique pas aux morts non-PvP.
- Verifier que les primes ne creent pas d'abus avec des comptes secondaires.
- Si besoin plus tard: ajouter une commande admin pour lire/clear le Crime Stat d'un joueur.
