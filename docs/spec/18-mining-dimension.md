# 18 – Mining-Dimension und Turtles (Phase 15)

Nutzerwunsch 2026-10-17. **Kein 1:1-Nachbau:** Im Pack gibt es keine Mining-Dimension (geprüft: nur Twilight Forest,
Mystcraft, Factorization-Hammer-Dimension; die Galacticraft-Config ist ein Überbleibsel ohne Mod). Die Turtle stammt aus
ComputerCraft 1.5 (CCTurtle) im Pack. Alle Werte hier sind Nutzerentscheidungen bzw. eigene Festlegungen.

## 1. Entscheidungen des Nutzers (2026-10-17)

| Punkt | Entscheidung |
|---|---|
| Art | eigene Dimension, per Portal erreichbar wie der Nether |
| Erze | alle **Oberwelt**-Erze (Vanilla, IC2, GT) mit Oberwelt-Mengen; Rubin/Saphir/Bauxit überall (keine Oberwelt-Biome); Nether-/End-GT-Erze **nicht** |
| Portalrahmen | **Glatter Stein** (`minecraft:smooth_stone`), mit Feuerzeug gezündet |
| Aufbau | wie der Oberwelt-Untergrund (Stein/Tiefenschiefer, Bedrock-Boden und -Decke), **ohne Höhlen**, **hell** |
| Turtle | **CC: Tweaked mitinstallieren** (wie Xaero's Minimap, ohne eigene Integration) |

## 2. Dimension `craft:mining`

### 2.1 Dimensionstyp `craft:mining`

| Feld | Wert | Grund |
|---|---|---|
| `min_y` / `height` / `logical_height` | −64 / 192 / 192 | y −64 … 127; Vanilla-Erzhöhen und Tiefenschiefer wie in der Oberwelt |
| `has_skylight` / `has_ceiling` | false / true | kein Himmel, Decke wie Nether |
| `ambient_light` | 1.0 | **hell**: alle Blöcke werden voll beleuchtet dargestellt, keine Fackeln nötig |
| `fixed_time` | 6000 | kein Tag-Nacht-Wechsel |
| `natural` | false | Kompass/Uhr drehen sich; keine Zombifizierten Piglins aus Nether-Portalen |
| `ultrawarm` / `piglin_safe` | false / false | Wasser verdunstet nicht |
| `bed_works` / `respawn_anchor_works` | true / false | Bett setzt den Spawnpunkt, explodiert nicht (Schlafen geht wegen `fixed_time` nicht) |
| `has_raids` | false | |
| `coordinate_scale` | 1.0 | Koordinaten 1:1 zur Oberwelt |
| `monster_spawn_light_level` / `monster_spawn_block_light_limit` | 0 / 0 | zusätzlich zur leeren Spawnliste des Bioms (§2.3) |
| `infiniburn` | `#minecraft:infiniburn_overworld` | |
| `effects` | `minecraft:overworld` | Himmel ist durch die Decke ohnehin nicht sichtbar |

### 2.2 Gelände (Noise-Settings `craft:mining`)

- Durchgehend massiv: `final_density` konstant 1, `default_block` Stein, `default_fluid` Luft, Meeresspiegel −64.
- **Keine Höhlen:** keine Carver, keine Noise-Höhlen, `aquifers_enabled = false`, keine Lavaseen, kein Wasser.
- `ore_veins_enabled = false` (die großen 1.18-Kupfer-/Eisenadern hängen am Oberwelt-Noise; die normalen Erz-Features
  bleiben, §3).
- Oberflächenregeln wie die Oberwelt unten bzw. der Nether oben:
  - Bedrock-Boden: y −64 sicher, bis y −60 mit abnehmender Wahrscheinlichkeit (`vertical_gradient` wie Oberwelt).
  - Tiefenschiefer: unter y 0 sicher, y 0 … 8 Übergang (`vertical_gradient` wie Oberwelt).
  - Bedrock-Decke: y 127 sicher, bis y 123 abnehmend (wie Nether-Decke, gespiegelt).
- `spawn_target` leer, `disable_mob_generation = true`.

### 2.3 Biom `craft:mining`

Ein einziges Biom (Biomquelle `fixed`). Graue Nebel-/Himmelsfarbe, kein Niederschlag (`has_precipitation = false`),
Temperatur 0,5. **Keine Mob-Spawns** (alle Kategorien leer, keine Spawnkosten) – „hell“ heißt hier auch: keine
Monster. Keine Carver, keine Strukturen, keine Bäume/Pflanzen/Seen/Quellen/Geoden, **kein Erde-/Kies-/Granit-/Diorit-/
Andesit-/Tuff-/Ton-Feature, kein befallener Stein**.

## 3. Erze (Biom-Features, Schritt `underground_ores`)

Es werden die bestehenden Placed Features wiederverwendet, damit die Mengen exakt der Oberwelt entsprechen:

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
- Höhen absolut wie in der Oberwelt. Was oberhalb y 127 läge (Kohle oben, Teile von Eisen oben und Smaragd), entfällt;
  dafür wird nichts ausgeglichen.
- Nicht generiert: Öl (BuildCraft), Gummibäume, Nether-/End-GT-Erze.
- Miner/Scanner/Quarry funktionieren dort ohne Sonderbehandlung (Erzwerte unverändert).

## 4. Portal

### 4.1 Rahmen und Zünden

- Rahmen aus **Glattem Stein**, Form und Größe wie das Nether-Portal: rechteckig, Innenraum 2×3 bis 21×21, senkrecht
  in X- oder Z-Richtung, Ecken optional.
- Gezündet mit **Feuerzeug** (und Feuerkugel) an der Innenseite des Rahmens; Ablauf wie beim Nether-Portal, nur in der
  Oberwelt und in `craft:mining`. Ein Rahmen aus Obsidian bleibt ein Nether-Portal.
- Nether-Portale funktionieren in der Mining-Dimension wie in jeder Nicht-Nether-Dimension (führen in den Nether).

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
2. **Kein Portal gefunden:**
   - **Mining-Dimension:** Portal (2×3, gleiche Achse) an x/z des Ziels, y = Quell-y, begrenzt auf −58 … 118.
     Rahmen aus Glattem Stein; ringsum wird ein Raum freigeräumt: Portalbreite + 2 Blöcke links/rechts,
     3 Blöcke vor und hinter der Portalebene, Höhe Rahmen + 1. Boden unter dem Raum wird mit Stein aufgefüllt, wo er
     fehlt (z. B. bei Stollen). Abgebaute Blöcke droppen nichts.
   - **Oberwelt:** Vanilla-Logik des `PortalForcer` (passende Stelle im Umkreis 16 suchen, sonst Plattform +
     Rahmen erzeugen), aber mit Glattem Stein statt Obsidian.
3. Ankunft vor dem Portal wie beim Nether-Portal (Entity wird in den Portalbereich gesetzt).

## 5. Turtles: CC: Tweaked

- **CC: Tweaked** (offizieller ComputerCraft-Nachfolger, NeoForge 1.21.1) wird wie Xaero's Minimap nur mitinstalliert:
  `localRuntime` in `build.gradle` (Version in `gradle.properties`, Maven `https://maven.squiddev.cc`, sonst Modrinth-Maven),
  dazu im CurseForge-Paket (`overrides/mods`).
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
  `PortalShape`, aber eigener Code), `MiningPortalForcer`, POI-Registrierung, Zünden über ein
  `BlockEvent.EntityPlaceEvent`/Feuer-Hook bzw. `UseItemOnBlockEvent` für Feuerzeug.
- Lang (de/en): „Mining-Portal“ / „Mining Portal“, Dimension „Mining-Welt“ / „Mining World“.
- Abweichungen (`docs/abweichungen.md`): Mining-Dimension und CC: Tweaked als neue Inhalte ohne Pack-Vorbild.

## 7. Teilphasen

| Teilphase | Inhalt |
|---|---|
| 15.0 | diese Spezifikation |
| 15.1 | Dimension: Typ, Noise-Settings, Biom, Erze; per `/execute in craft:mining run tp` testbar |
| 15.2 | Portal: Block, Textur, Rahmenprüfung, Zünden, Zielsuche/-erzeugung, POI |
| 15.3 | CC: Tweaked mitinstallieren, Test Turtle ↔ Craft-Inventare, CurseForge-Paket neu bauen |
