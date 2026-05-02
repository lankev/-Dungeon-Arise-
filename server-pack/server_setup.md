# Dungeon Arise — Configuration Serveur Public

## 1. server.properties (à appliquer sur le serveur dédié)

```properties
# Performance — 50 joueurs
view-distance=7
simulation-distance=5
max-players=50

# Sécurité
online-mode=true
enforce-whitelist=false
spawn-protection=16

# Monde
allow-flight=false
difficulty=hard
gamemode=survival
pvp=true
```

> `view-distance=7` : rendu côté serveur (chunks visibles). Défaut = 10, coûteux à 50 joueurs.
> `simulation-distance=5` : entités/mobs actifs autour du joueur. Défaut = 10.

---

## 2. World Border — à exécuter une seule fois en console

```
# Centrer sur le spawn
/worldborder center 0 0

# Taille : 25000x25000 blocs (12500 de rayon)
# = ~625 000 000 blocs² / 50 joueurs = 12,5M blocs par joueur
/worldborder set 25000

# Dommages si le joueur sort (0.5 HP/s après 5 blocs hors limite)
/worldborder damage amount 0.5
/worldborder damage buffer 5

# Avertissement à 500 blocs du bord
/worldborder warning distance 500

# Vérification
/worldborder get
```

---

## 3. Commandes Solo Gates

### Gestion des portails
```
# Spawn un portail d'un rang précis devant soi
/sologates spawn E|C|B|A|S

# Spawn un portail boss rang S devant soi
/sologates spawnboss

# Voir tous les portails actifs (pos, mobs, statut)
/sologates status

# Ses propres statistiques
/sologates stats

# Stats d'un autre joueur (OP requis)
/sologates stats <joueur>

# Définir le rang chasseur d'un joueur manuellement (déclenche GameStage)
/sologates setrank <joueur> C|B|A|S

# Quitter un donjon (retour overworld sans récompense)
/sologates leave
```

### Commandes FTB Quests (pour commander rewards)
```
# Ajouter le rang C à un joueur via reward de quête :
/sologates setrank {player} C
```

---

## 4. Commandes GameStages

```
# Voir les stages d'un joueur
/gamestage info <joueur>

# Ajouter un stage manuellement
/gamestage add <joueur> rank_c|rank_b|rank_a|rank_s

# Retirer un stage (reset de rang)
/gamestage remove <joueur> rank_b

# Vérifier si un joueur a un stage
/gamestage check <joueur> rank_b
```

---

## 5. Commandes FTB Quests

```
# Ouvrir les quêtes d'un joueur (admin)
/ftbquests open_book <joueur>

# Compléter une quête manuellement pour un joueur
/ftbquests complete_quest <joueur> <quest_id>

# Voir les quêtes complétées
/ftbquests query_quests <joueur>
```

---

## 6. Commandes FTB Teams

```
# Voir les teams actives
/ftbteams list

# Infos d'une team
/ftbteams info <team>

# Dissoudre une team (admin)
/ftbteams delete_team <team>
```

---

## 7. Commandes de modération courantes

```
# Téléporter un joueur hors d'un donjon planté
/sologates leave           # joueur le fait lui-même
/tp <joueur> 0 100 0       # admin force

# Voir où se trouve un joueur
/data get entity <joueur> Pos

# Vérifier les chunks chargés (surcharge)
/forge tps

# Vider les drops au sol (lag)
/kill @e[type=item]

# Redémarrage propre (si script de redémarrage configuré)
/stop
```

---

## 8. Paramètres recommandés (world settings)

À mettre dans la console ou dans un script de démarrage :

```
# Désactiver les raids (trop de lag à 50 joueurs)
/gamerule disableRaids true

# Réduire les foudres aléatoires
/gamerule doFireTick true

# Garder les items à la mort (selon choix serveur)
/gamerule keepInventory false

# Annonces de mort
/gamerule showDeathMessages true

# Limiter les entités par chunk (réduit le lag)
/gamerule maxEntityCramming 24
```

---

## 9. Anti-rush — rappel des mécanismes actifs

| Mécanisme | Fichier |
|---|---|
| Blocage utilisation items lockés | `kubejs/server_scripts/anti_rush.js` |
| Tooltip rang requis (JEI + inventaire) | `kubejs/client_scripts/anti_rush_tooltip.js` |
| Retrait items OP des coffres de structures | `kubejs/server_scripts/loot_anti_rush.js` |
| Rang requis pour entrer dans une gate | `SoloGatesEvents / GateManager.java` |
| Armure lockée éjectée au tick | `kubejs/server_scripts/anti_rush.js` |
| Loot per-joueur dans toutes les structures | `config/lootr-common.toml` |

## 10. Lootr — Commandes utiles

```
# Forcer le rechargement du loot d'un coffre pour un joueur
# (pas de commande Lootr directe — géré automatiquement)

# Si un joueur signale un bug de coffre vide :
# → vérifier que le coffre est bien un coffre Lootr (texture différente)
# → si non, la structure n'a pas de loot table enregistrée → ajouter dans loot_anti_rush.js

# Debug : identifier la loot table d'un coffre
# Activer showcontainerloottable dans config/lootintegrations.json
# Ouvrir le coffre → l'ID apparaît dans le chat
```

**Comportement Lootr configuré :**
- Chaque joueur reçoit son propre loot une seule fois par coffre (pas de reset)
- Les coffres Solo Gates sont exclus (gérés par le mod Java)
- La dimension donjon Solo Gates est exclue
- Les coffres résistent aux creepers (`blast_resistant = true`)
- Les joueurs ne peuvent pas casser les coffres à la main (`disable_break = true`)
- Le coffre de départ Minecraft est exclu (partagé)

---

## 10. Notes de maintenance

- **Orphaned gates** : nettoyées automatiquement au démarrage du serveur.
- **Déconnexion en donjon** : le joueur est renvoyé en overworld à sa reconnexion.
- **Loot tables** : si un item OP apparaît encore en coffre, activer `showcontainerloottable: true` dans `config/lootintegrations.json` pour identifier la loot table, puis ajouter l'ID dans `loot_anti_rush.js`.
