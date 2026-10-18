# 21 – Long Fall Boots (Portal Gun)

Quelle: `ftb ultimate/mods/PortalGun1.4.7v5.zip` (iChun, Portal Gun 1.4.7v5), Pack-Config
`ftb ultimate/config/PortalGun.cfg`. Dekompiliert (Nutzer, 2026-10-18; nur die betroffenen Klassen, Vanilla-Namen
obfuskiert): `reference/decompiled/portalgun/` – `CommonProxy` (Items, Rezepte), `Settings.handleLFBRecipe`
(Stiefelrezept), `ItemArmour`, `ItemPlaceholder`, `EventHandler.onLivingFall`, `EntityHelper.isEntityWearingLFB`.
Obfuskierte Vanilla-Namen über `ftb ultimate/instmods/minecraft.jar` aufgelöst (`up.o` = Eisenbarren, `up.n` =
Diamant, `amq.as` = Obsidian, `sv.e` = `EnumArmorMaterial.DIAMOND`).

Umfang (Nutzer, 2026-10-18): **nur die Long Fall Boots** – keine Portal Gun, Würfel, Geschütze, Faith Plates,
Platten, Enderperlenstaub.

## 1 Items

| Item | ID | Original | Werte |
|---|---|---|---|
| Long Fall Boot (einzeln) | `craft:long_fall_boot` | `ItemPlaceholder` `PortalLFB`, Icon 9 | Stapel 2, ohne Funktion |
| Long Fall Boots | `craft:long_fall_boots` | `ItemArmour` `PortalLFBs`, Icon 10 | Stiefel (Typ 3), Material Diamant: Rüstung 3, Verzauberbarkeit 10; `setMaxDamage(0)` → **nutzen sich nie ab**; Stapel 1 |

- Material als eigenes `ArmorMaterial` `craft:long_fall_boots` mit den 1.4.7-Diamantwerten (3/8/6/3, keine Härte)
  und der Original-Rüstungstextur `skin_lfb.png` (`textures/models/armor/long_fall_boots_layer_1.png`).
- Kreativ: wie im Original im Kampf-Tab (das eigene „Portal Gun“-Tab entfällt).
- Tag `minecraft:foot_armor` (Stiefel-Verzauberungen wie Federfall).

## 2 Rezepte (Pack: `recipeEasyLongFallBoots=0`)

| Ergebnis | Rezept |
|---|---|
| 1 Long Fall Boot | `#O#` / `#D#` / `#D#` – `#` Eisenbarren, `O` Obsidian, `D` Diamant |
| 1 Long Fall Boots | formlos: 2 Long Fall Boot |
| 2 Long Fall Boot | formlos: 1 Long Fall Boots |

Die „einfache“ Variante (`# #`/`#O#`/`#O#`) ist im Pack abgeschaltet und wird nicht umgesetzt.

## 3 Wirkung

`EventHandler.onLivingFall` (nur Server): trägt ein **Spieler** oder **Zombie** die Long Fall Boots im Stiefelslot,
wird die Fallhöhe auf 0 gesetzt – kein Fallschaden, egal aus welcher Höhe. 1.21: `LivingFallEvent.setDistance(0)`
für `Player` und `Zombie` (inkl. Unterklassen: Wüstenzombie, Ertrunkener, Zombiedorfbewohner, Zombifizierter Piglin –
in 1.4.7 erbte der Schweinezombie ebenfalls von `EntityZombie`). Andere Mobs profitieren nicht.

## 4 Tests

`PortalGunGameTests`: Zombie mit Long Fall Boots nimmt bei 20 Blöcken Fall keinen Schaden, mit Diamantstiefeln
schon; Stiefel nicht beschädigbar, Rüstung 3, Einzelstiefel stapelt bis 2.
