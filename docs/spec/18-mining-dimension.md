# 18 – Mining-Dimension und Turtles (Phase 15)

Nutzerwunsch 2026-10-17. **Kein 1:1-Nachbau:** Im Pack gibt es keine Mining-Dimension (geprüft: nur Twilight Forest,
Mystcraft, Factorization-Hammer-Dimension; die Galacticraft-Config ist ein Überbleibsel ohne Mod). Die Turtle stammt aus
ComputerCraft 1.5 (CCTurtle) im Pack. Alle Werte hier sind Nutzerentscheidungen bzw. eigene Festlegungen.

## 1. Entscheidungen des Nutzers (2026-10-17)

| Punkt | Entscheidung |
|---|---|
| Art | eigene Dimension, per Portal erreichbar wie der Nether |
| Erze | alle **Oberwelt**-Erze (Vanilla, IC2, GT) in **doppelter** Oberwelt-Menge (Nutzer, 2026-10-17, Phase 17.1); Rubin/Saphir/Bauxit überall (keine Oberwelt-Biome); Nether-/End-GT-Erze **nicht** |
| Portalrahmen | **Glatter Stein** (`minecraft:smooth_stone`), mit Feuerzeug gezündet |
| Aufbau | **flache Steinwelt für Quarrys** (Nutzer, 2026-10-17, ersetzt „wie Oberwelt-Untergrund mit Bedrock-Decke“): Bedrock-Boden, darüber massiver Stein/Tiefenschiefer mit Erzen bis zu einer **flachen Oberfläche bei y 64**, darüber offener Himmel; **ohne Höhlen**, **hell**. Man steht oben auf der Ebene und baut nach unten ab |
| Turtle | **CC: Tweaked mitinstallieren** (wie Xaero's Minimap, ohne eigene Integration) |

## 2. Dimension `craft:mining`

### 2.1 Dimensionstyp `craft:mining`

| Feld | Wert | Grund |
|---|---|---|
| `min_y` / `height` / `logical_height` | −64 / 384 / 384 | wie die Oberwelt (y −64 … 319): Erzhöhen und Tiefenschiefer wie dort, Bauhöhe über der Ebene |
| `has_skylight` / `has_ceiling` | true / false | offener Himmel über der Ebene |
| `ambient_light` | 1.0 | **hell**: auch Schächte und Stollen werden voll beleuchtet dargestellt, keine Fackeln nötig |
| `fixed_time` | 6000 | kein Tag-Nacht-Wechsel |
| `natural` | false | Kompass/Uhr drehen sich; keine Zombifizierten Piglins aus Nether-Portalen |
| `ultrawarm` / `piglin_safe` | false / false | Wasser verdunstet nicht |
| `bed_works` / `respawn_anchor_works` | true / false | Bett setzt den Spawnpunkt, explodiert nicht (Schlafen geht wegen `fixed_time` nicht) |
| `has_raids` | false | |
| `coordinate_scale` | 1.0 | Koordinaten 1:1 zur Oberwelt |
| `monster_spawn_light_level` / `monster_spawn_block_light_limit` | 0 / 0 | zusätzlich zur leeren Spawnliste des Bioms (§2.3) |
| `infiniburn` | `#minecraft:infiniburn_overworld` | |
| `effects` | `minecraft:overworld` | Himmel, Sonne, Wolken wie in der Oberwelt |

### 2.2 Gelände (Noise-Settings `craft:mining`)

- Flach: `final_density` = `y_clamped_gradient` (y 64 → 1, y 65 → −1), also massiv bis einschließlich y 64, darüber Luft.
  `default_block` Stein (auch die Oberfläche ist Stein), `default_fluid` Luft, Meeresspiegel −64.
- **Keine Höhlen:** keine Carver, keine Noise-Höhlen, `aquifers_enabled = false`, keine Lavaseen, kein Wasser.
- `ore_veins_enabled = false` (die großen 1.18-Kupfer-/Eisenadern hängen am Oberwelt-Noise; die normalen Erz-Features
  bleiben, §3).
- Oberflächenregeln wie die Oberwelt unten bzw. der Nether oben:
  - Bedrock-Boden: y −64 sicher, bis y −60 mit abnehmender Wahrscheinlichkeit (`vertical_gradient` wie Oberwelt).
  - Tiefenschiefer: unter y 0 sicher, y 0 … 8 Übergang (`vertical_gradient` wie Oberwelt).
- `spawn_target` leer, `disable_mob_generation = true`.

### 2.3 Biom `craft:mining`

Ein einziges Biom (Biomquelle `fixed`). Himmel- und Nebelfarbe wie in der Oberwelt, kein Niederschlag (`has_precipitation = false`),
Temperatur 0,5. **Keine Mob-Spawns** (alle Kategorien leer, keine Spawnkosten) – „hell“ heißt hier auch: keine
Monster. Keine Carver, keine Strukturen, keine Bäume/Pflanzen/Seen/Quellen/Geoden, **kein Erde-/Kies-/Granit-/Diorit-/
Andesit-/Tuff-/Ton-Feature, kein befallener Stein**.

## 3. Erze (Biom-Features, Schritt `underground_ores`)

**Doppelte Menge** (Nutzer, 2026-10-17, Phase 17.1): je Oberwelt-Feature eine Kopie `craft:mining/<name>` mit
gleicher Konfiguration (Adergröße, Ersetzungsregeln) und der Oberwelt-Platzierung, der ein `count: 2` vorangestellt
ist – jeder Aderwurf (Anzahl bzw. Seltenheit, Höhe, Lage) läuft also zweimal je Chunk. Oberwelt-Features bleiben
unverändert. Bereits erzeugte Chunks der Mining-Dimension behalten die alte Menge. Kopiert werden:

| Herkunft | Placed Features |
|---|---|
| Vanilla | `ore_coal_upper`, `ore_coal_lower`, `ore_iron_upper`, `ore_iron_middle`, `ore_iron_small`, `ore_gold`, `ore_gold_lower`, `ore_redstone`, `ore_redstone_lower`, `ore_diamond`, `ore_diamond_medium`, `ore_diamond_large`, `ore_diamond_buried`, `ore_lapis`, `ore_lapis_buried`, `ore_emerald` |
| IC2 (Craft) | `craft:ore_copper` (ersetzt Vanilla-Kupfer wie in der Oberwelt), `craft:ore_tin`, `craft:ore_uranium` |
| GT (Craft) | `craft:gt_galena_ore`, `craft:gt_iridium_ore`, `craft:gt_ruby_ore`, `craft:gt_sapphire_ore`, `craft:gt_bauxite_ore` |

- Smaragd ist in der Oberwelt nur in Berg-Biomen; hier überall (wie Rubin/Saphir/Bauxit).
- Rubin/Saphir/Bauxit: `craft:mining` wird in die Biom-Tags `#craft:has_gt_ruby_ore`, `…_sapphire_ore`,
  `…_bauxite_ore` aufgenommen (der Filter `craft:chunk_biome` greift damit überall).
- Ersetzbar wie in der Oberwelt (`#stone_ore_replaceables` / `#deepslate_ore_replaceables`), Tiefenschiefer-Varianten
  entstehen wie dort.
- Höhen absolut wie in der Oberwelt. Was oberhalb der Oberfläche (y 64) läge, entfällt (obere Kohle, Teile von Eisen
  oben, Smaragd und Bauxit); dafür wird nichts ausgeglichen.
- Nicht generiert: Öl (BuildCraft), Gummibäume, Nether-/End-GT-Erze.
- Miner/Scanner/Quarry funktionieren dort ohne Sonderbehandlung (Erzwerte unverändert).

## 4. Portal

### 4.1 Rahmen und Zünden

- Rahmen aus **Glattem Stein**, Form und Größe wie das Nether-Portal: rechteckig, Innenraum 2×3 bis 21×21, senkrecht
  in X- oder Z-Richtung, Ecken optional.
- Gezündet mit **Feuerzeug** (und Feuerkugel) an der Innenseite des Rahmens; Ablauf wie beim Nether-Portal, nur in der
  Oberwelt und in `craft:mining`. Ein Rahmen aus Obsidian bleibt ein Nether-Portal.
- Nether-Portale lassen sich in der Mining-Dimension wie in jeder Nicht-Vanilla-Dimension nicht zünden.

### 4.2 Portalblock `craft:mining_portal`

- Verhalten wie `minecraft:nether_portal` (Achse X/Z, unzerstörbar ohne Werkzeug, kein Item, zerfällt, wenn der
  Rahmen beschädigt wird, Partikel und Geräusche des Nether-Portals, Kollision keine, Licht 11).
- Eigene animierte Textur (Nether-Portal-Animation in Grau-/Steinblau umgefärbt).
- Übergangszeit: Kreativ sofort, sonst 80 Ticks wie das Nether-Portal; Abklingzeit wie Vanilla.
- Alle Entities (Spieler, Mobs, Items, Loren) reisen wie beim Nether-Portal.
- Kein Spawnen von Zombifizierten Piglins.

### 4.3 Zielsuche und Erzeugung

Oberwelt ↔ `craft:mining`, Koordinaten **1:1**.

1. **Suche:** eigener POI-Typ `craft:mining_portal`; vorhandenes Portal im Umkreis von **16** Blöcken (horizontal,
   Quadrat wie Vanilla) um die Zielposition, das nächstgelegene gewinnt. 16 statt 128/16, damit benachbarte
   Oberwelt-Portale nicht auf dasselbe Ziel fallen.
2. **Kein Portal gefunden:** in beiden Dimensionen Vanilla-Logik des `PortalForcer`: nächste freie Stelle im Umkreis 16,
   an der der 2×3-Rahmen auf festem Boden in freiem Raum steht (in der Mining-Dimension also auf der Ebene bei y 65),
   sonst Plattform (3×2, Glatter Stein) + Rahmen auf Quellhöhe, begrenzt auf y 70 … logische Höhe − 10. Rahmen immer aus
   Glattem Stein statt Obsidian.
3. Ankunft vor dem Portal wie beim Nether-Portal (Entity wird in den Portalbereich gesetzt).

## 5. Turtles: CC: Tweaked

- **CC: Tweaked** (offizieller ComputerCraft-Nachfolger, NeoForge 1.21.1) wird wie Xaero's Minimap nur mitinstalliert:
  `localRuntime` in `build.gradle` über das Modrinth-Maven, Version **1.120.2** (`cc_tweaked_version` in
  `gradle.properties` = Modrinth-Versions-ID `1ewzHZYg`, weil die Versionsnummer auch der Fabric-Build trägt; die Datei
  heißt `cc-tweaked-1.21.1-forge-1.120.2.jar`, ist aber der NeoForge-Build), dazu im CurseForge-Paket (`overrides/mods`).
- **Keine eigene Integration** (keine Peripherals, keine Turtle-Upgrades wie MiscPeripherals im Pack). Turtles können
  über die NeoForge-Item-Capability trotzdem in Craft-Maschinen/-Kisten legen und daraus nehmen.
- Pack-Config CCTurtle: `turtlesNeedFuel=true` → in CC: Tweaked ist `need_fuel = true` Standard; keine Config nötig.
  Übrige Pack-Werte (Speicher 2 MiB, HTTP an, Modemreichweite 64/384) entsprechen den CC:-Tweaked-Standards bzw. sind
  unkritisch – keine Anpassung.
- Prüfen in 15.3: Craft-Blöcke mit eigener Abbaulogik (unzerstörbare Personal-Blöcke, Reaktor) – Turtles dürfen
  Personal-Blöcke nicht abbauen (CC: Tweaked respektiert Härte −1).

## 6. Umsetzung 1.21

- Datapack-Einträge per Datagen (`DatapackBuiltinEntriesProvider`): `dimension_type`, `worldgen/noise_settings`,
  `worldgen/biome`, `dimension` (`craft:mining` mit `fixed`-Biomquelle) – neue Datagen-Klasse für das Mining-Paket.
- Paket `dev.craft.mining` (neue Inhalte, kein Ursprungs-Mod): `MiningDimension` (Keys), `MiningPortalBlock`
  (implementiert `net.minecraft.world.level.block.Portal`), `MiningPortalShape` (Rahmenprüfung nach dem Muster von
  `PortalShape`, aber eigener Code), `MiningPortalForcer`, POI-Registrierung; Zünden über
  `BlockEvent.NeighborNotifyEvent` (jedes gesetzte Feuer in Oberwelt/Mining-Dimension prüft den Rahmen).
- Lang (de/en): „Mining-Portal“ / „Mining Portal“, Dimension „Mining-Welt“ / „Mining World“.
- Abweichungen (`docs/abweichungen.md`): Mining-Dimension und CC: Tweaked als neue Inhalte ohne Pack-Vorbild.

## 7. Teilphasen

| Teilphase | Inhalt |
|---|---|
| 15.0 | diese Spezifikation |
| 15.1 | Dimension: Typ, Noise-Settings, Biom, Erze (erledigt; umgestellt auf flache Steinwelt) |
| 15.2 | Portal: Block, Textur, Rahmenprüfung, Zünden, Zielsuche/-erzeugung, POI (erledigt) |
| 15.3 | CC: Tweaked mitinstallieren, Test Turtle ↔ Craft-Inventare, CurseForge-Paket neu bauen (erledigt) |
| 17.1 | Erze in doppelter Menge (erledigt) |
