# 07 – Weltgenerierung

Quellen: `ic2.core.IC2.generate`, `ic2.core.block.WorldGenRubTree`, `buildcraft.energy.OilPopulate`,
`gregtechmod.common.GT_Worldgenerator`, Pack-Configs.

## 1. IC2-Erze

Allgemein: `baseHeight = seaLevel + 1` (`provider.getAverageGroundLevel()`, Overworld 64 → 65),
`baseScale = round(baseHeight · oreDensityFactor)` (Pack 1,0 → 65). Anzahl Adern pro Chunk:
`count = round(nextGaussian · √base + base)` mit `base = X · baseScale / 64`. Position x/z zufällig im Chunk.
Vanilla-`WorldGenMinable` (Aderform).

| Erz | IC2-Standard aktiv? | Pack-Config | Adern/Chunk (base) | Adergröße | Höhe (y) |
|---|---|---|---:|---:|---|
| Kupfererz | ja | **deaktiviert** (`enableWorldGenOreCopper=false`) | 15·65/64 = 15 | 10 | `rand(40·h/64) + rand(20·h/64) + 10·h/64` ≈ 10…69 |
| Zinnerz | ja | **deaktiviert** (`enableWorldGenOreTin=false`) | 25·65/64 = 25 | 6 | `rand(40·h/64)` ≈ 0…39 |
| Uranerz | ja | aktiv | 20·65/64 = 20 | 3 | `rand(64·h/64)` ≈ 0…64 |

(h = baseHeight = 65.)

Im Pack kamen Kupfer und Zinn aus anderen Mods (Forestry/Thermal Expansion/RedPower). Da diese nicht übernommen werden,
**müssen** Kupfer- und Zinnerz im neuen Mod generiert werden. **Entschieden:** Kupfer = Vanilla-Kupfererz, dessen
Generierung auf die IC2-Werte (Zeile Kupfererz) angepasst wird; Zinn = eigenes Zinnerz mit den IC2-Werten
(siehe `docs/abweichungen.md`).

Erz-Eigenschaften siehe `05-items.md` §10.

## 2. Gummibäume

Pro Chunk, Biom am Punkt (chunkX·16+16, chunkZ·16+16), Biomname enthält:
- „taiga“: `rubbertrees += random(3)`
- „forest“ oder „jungle“: `+= random(5) + 1`
- „swamp“: `+= random(10) + 5`

Dann mit Wahrscheinlichkeit `rubbertrees · 2 %` (`random(100)+1 ≤ rubbertrees·2`) ein Baumcluster an zufälliger
Position im Chunk: `WorldGenRubTree.generate(x, count = rubbertrees, z)`:
- Solange count > 0: Boden suchen (von Welthöhe abwärts bis zum ersten Nicht-Luft-Block darunter), Baum wachsen lassen
  (`grow`); misslingt das → count −= 3; danach x/z um `random(15) − 7` verschieben, count −= 1.

**Baumform (`grow`)**, auch beim Setzling verwendet:
1. Wuchshöhe `h` = 1 + freie Luftblöcke über dem Standort (max. 8); Untergrund muss Pflanzen tragen (wie Setzling),
   Standort Luft oder Gummisetzling. `h < 2` → kein Baum.
2. Stammhöhe `height = h/2 + random(h − h/2 + 1)`.
3. Für jede Stammhöhe i: Gummiholz; mit Chance `treeholechance` (Start 25 %, nach jedem Treffer −10 %) ein Harzloch
   (Meta 2–5 = Seite zufällig, nass), sonst Meta 1.
4. Blätter: wenn `height < 4 || (height < 7 && i > 1) || i > 2`: im 5×5 um den Stamm: innerer 3×3 immer; Kanten
   (äußerer Rand ohne Ecken) mit Wahrscheinlichkeit `1/c` (`c = max(1, i + 4 − height)`); nur in Luft.
5. Spitze: `height/4 + random(2)` + 1 Blätter über dem Stamm.

Setzling: wächst bei Licht ≥ 9 mit 1/30 pro Random-Tick; Knochenmehl löst sofortiges Wachstum aus (verbraucht 1).

## 3. Weitere IC2-Weltinhalte

- Keine weiteren Strukturen. Dungeon-Loot (`IC2Loot`) – siehe Code (optional).

## 4. Erzwerte (Miner/Scanner, `valuableOres`)

Erste Registrierung gewinnt (IC2 `addValuableOre`). Pack-Werte für die übernommenen Blöcke:

| Block | Wert |
|---|---:|
| Kohleerz | 1 |
| Golderz | 3 |
| Eisenerz | 4 |
| Lapiserz | 3 |
| Redstone-Erz (beide) | 3 |
| Diamanterz | 5 |
| Smaragderz | 5 |
| Kupfererz (oreCopper) | 2 |
| Zinnerz (oreTin) | 2 |
| Uranerz (oreUranium) | 4 |
| sonstige `ore*`-Einträge | 1 |

Neue 1.21-Erze (Deepslate-Varianten, Nethergold, Netherquarz …): siehe Abweichungen (gleicher Wert wie Basiserz).

## 5. Öl (BuildCraft 3.4.3)

`OilPopulate` auf `PopulateChunkEvent.Post` (Pack `modifyWorld = true`), nicht im Nether/End.
Biom an (x+16, z+16).

1. **Wüsten-Oberflächenpfütze**: Biom = Wüste und `rand > 0,97` (3 %): Startpunkt (x+2..11, z+2..11), von y = 128
   abwärts bis y = 66 erster Nicht-Luft-Block; ist es Sand → Oberflächenvorkommen Radius 3.
2. **Mittleres Vorkommen**: Wahrscheinlichkeit 0,0015 je Chunk; **großes** 0,00005.
   - Kugel aus Öl (Quellblöcke) bei (chunkX·16, `20 + rand(10)`, chunkZ·16), Radius groß `8 + rand(9)`, mittel `4 + rand(4)`.
   - Steigrohr: von y = 128 abwärts bis zur Kugel: Ab dem ersten Block, der nicht Luft/Blätter/Holz/Gras ist,
     wird Öl gesetzt (bis zur Kugel). Dort zusätzlich Oberflächenvorkommen (Radius groß `20 + rand(20)`, mittel
     `5 + rand(5)`) und eine Ölsäule nach oben (groß bis +30, mittel bis +4, max. y 128) – die bekannten „Ölfontänen“.
3. **Oberflächenvorkommen** (`generateSurfaceDeposit`): Zentrum fest; Ringe w = 1..r mit Wahrscheinlichkeit
   `(r − w + 4)/(r + 4)`; gesetzt wird nur, wenn 2 Blöcke darunter nicht Luft und benachbart bereits Öl liegt; setzt Öl
   eine Ebene tiefer und ersetzt den Block selbst durch Luft (bzw. Öl, falls dort Wasser/Öl war). Danach Lücken füllen.

Umsetzung 1.21 (entschieden): Feature `craft:oil` (`buildcraft.world.OilFeature`); Kugel y 20–29 absolut, Suchen ab
Geländeoberkante statt y 128, Blöcke außerhalb der 3×3 Nachbarchunks werden übersprungen (siehe Abweichungen).

## 6. GregTech-Weltgenerierung (GT 2.90, Phase 8)

**Entschieden (2026-10-16): alle GT-Erze wie im Pack.** Quellen: `GT_Worldgenerator`, `GT_MinableOreGenerator`,
`blocks/GT_BlockMetaID_Ore`, `GT_Config` Z. 150–163, `GT_Mod` Z. 505–516 (OreDict), `GT_OreDictHandler` (Verarbeitung).

### 6.1 Pack-Config (`GregTech.cfg`, `worldgeneration`)

Alle Erze an, `IridiumProbability=20`, `EnderAsteroids=true`. `Silverore=false` – der Schlüssel wird gelesen, aber
nirgends ausgewertet (es gibt kein GT-Silbererz; Meta 0 „GT_Ore“ wird nie generiert und droppt nichts).

### 6.2 Erzblock (`GT_BlockMetaID_Ore`, ein Block mit Meta 1–12)

Material Stein, Steinklang, Explosionsresistenz 6 (1.4.7 `setResistance(10)` → 30/5), Drachen zerstört ihn nicht
(`canDragonDestroy = false`), Stapelgröße 64 (`MaxOreStackSize`). Behutsamkeit liefert den Erzblock (Item mit
Subtypen) **ohne** XP; XP nur beim normalen Abbau.

| Meta | Name (en) | OreDict | Härte | Werkzeug | Drop ohne Behutsamkeit (Glück f) | XP | Erzwert |
|---:|---|---|---:|---|---|---|---:|
| 1 | Galena Ore | `oreGalena` | 3 | Stein | Erz selbst | – | 4 |
| 2 | Iridium Ore | `oreIridium` | 20 | Diamant | IC2-Iridiumerz × (1 + rand(1 + f/2)), f/2 ganzzahlig | 30–50 | 10 |
| 3 | Ruby Ore | `oreRuby` | 4 | Eisen | Rubin × (1 + rand(1 + f)); + 1 roter Granat mit 1/max(1, 32/(f+1)) | 3–7 | 4 |
| 4 | Sapphire Ore | `oreSapphire` | 4 | Eisen | Saphir × (1 + rand(1 + f)); + 1 grüner Saphir mit 1/max(1, 64/(f+1)) | 3–7 ¹ | 4 |
| 5 | Bauxite Ore | `oreBauxite` | 3 | Stein | Erz selbst | – | 2 |
| 6 | Pyrite Ore | `oreNetherPyrite` | 2 | Stein | Pyritstaub × (2 + rand(1 + f)) | 1 | 1 |
| 7 | Cinnabar Ore | `oreNetherCinnabar` | 3 | Eisen | Zinnoberstaub × (2 + rand(1 + f)); + 1 Redstone mit 1/max(1, 4/(f+1)) | 3–5 | 3 |
| 8 | Sphalerite Ore | `oreNetherSphalerite` | 2 | Stein | Sphaleritstaub × (2 + rand(1 + f)); + 1 Zinkstaub mit 1/max(1, 4/(f+1)); + 1 gelber Granat mit 1/max(1, 32/(f+1)) | 1 | 2 |
| 9 | Tungstate Ore | `oreEndTungstate` | 4 | Eisen | Erz selbst | – | 4 |
| 10 | Sheldonite Ore (intern Cooperite) | `oreEndCooperite` | 3,5 | Diamant | Erz selbst | – | 10 |
| 11 | Olivine Ore | `oreEndOlivine` | 3 | Diamant | Olivin × (1 + rand(1 + f)) | – | 4 |
| 12 | Sodalite Ore | `oreEndSodalite` | 3 | Eisen | Sodalithstaub × (6 + 3 · rand(1 + f)); + 1 Aluminiumstaub mit 1/max(1, 4/(f+1)) | – | 3 |

¹ Originalfehler: `case 4` fällt ohne `break` in `case 5`/`default` – Ergebnis trotzdem 3–7 wie Rubin.
Werkzeug = Forge-Harvest-Level (1 Stein, 2 Eisen, 3 Diamant). Mit f = 0 heißt `rand(1)` immer 0 (Pyrit/Sphalerit
also immer genau 1 XP). Erzwert = `addValuableOre` (Miner/Scanner, §4).

**Textur:** `gregtechmod/textures/blocks.png`, Index `128 + meta + side · 16` – je Erz **sechs** verschiedene
Flächenbilder (Zeilen 8–13), die Fläche wählt `|side ^ x ^ y ^ z| % 6` (positionsabhängig gemischt). Nether-/End-Erze
haben Netherrack- bzw. Endstein-Hintergrund im Bild.

### 6.3 Generierung

`IWorldGenerator` je Chunk. Dimension über das Biom bei Blockkoordinate **(chunkX + 8, chunkZ + 8)** (Originalfehler:
Chunk- statt Blockkoordinaten – wirkt nur als Dimensionsprüfung): „Hell“ → Nether, „Sky“ → End, sonst Oberwelt.
x/z unten = Chunk-Ecke (Blockkoordinaten).

**Adergenerator** (`GT_MinableOreGenerator`): identisch mit Vanilla-1.4.7-`WorldGenMinable` (Zentrum +8/+8,
Länge `n/8`, Kugeln Radius bis `n/16`) mit erweiterter Ersetzungsregel: Stein, **Netherrack, Endstein** (in jeder
Dimension) sowie bei `allowVoid` auch **Luft**. Einzelblöcke (`placeSingleOreBlock`): gleiche Ersetzungsregel ohne Luft,
Position direkt im Chunk (kein +8).

**Oberwelt** (Biom für Rubin/Saphir/Bauxit an Block x+8/z+8):

| Erz | Bedingung | Anzahl je Chunk | Form | Höhe y |
|---|---|---|---|---|
| Galena | immer | 1 | Ader 16 | rand(32) = 0–31 |
| Iridium | `rand(100) < 20` | 1 (= 1 + 20/100) | Einzelblock | rand(99) = 0–98 |
| Rubin | Biom in Rubinliste | 8 | Einzelblock | 0–31 |
| Saphir | Biom in Saphirliste | 8 | Einzelblock | 0–31 |
| Bauxit | Biom in Bauxitliste | 2 | Ader 16 | 32 + rand(48) = 32–79 |

Biomlisten (Namensvergleich; Vanilla-1.4.7-Biome + Namen aus ExtrabiomesXL/Twilight Forest u. a.):
- **Rubin:** Desert, DesertHills; Mountainous Desert, Mountain Ridge, Savanna, Shrubland, Wasteland, Fire Swamp,
  Highlands, Volcano, Steppe, Scrubland, Prairie, Lush Desert, Deadlands, Badlands, Dunes, Mesa, Promised Land.
- **Saphir:** Ocean, Beach; Twilight Lake, Twilight Stream, Lake Border, Glacier, Mangrove, Oasis, Origin Valley,
  Sacred Springs, Tropics, Promised Land.
- **Bauxit:** Plains, Forest, ForestHills; Rainforest, Autumn Woods, Birch Forest, Forested Hills, Forested Island,
  Green Hills, Meadow, Redwood Forest, Redwood Lush, Woodlands, Twilight Forest, Dense Twilight Forest, Dark Forest,
  Enchanted Forest, Woodland, Thicket, Seasonal Forest, Quagmire, Orchard, Pasture, Mystic Grove, Marsh, Maple Woods,
  Grove, Grassland, Garden, Fungi Forest, Deciduous Forest, Coniferous Forest, Cherry Blossom Grove.

**Nether** (Anzahl `q` wird immer gewürfelt, auch wenn das Erz aus ist):

| Erz | q | Adern | Adergröße | Höhe y |
|---|---|---:|---:|---|
| Pyrit | (1+rand 2)·(1+rand 2)·(1+rand 2)·(1+rand 2) = 1–16 | q | 2q | rand(64) = 0–63 |
| Zinnober | (1+rand 2)·(1+rand 4) = 1–8 | q | 2q | 64 + rand(64) = 64–127 |
| Sphalerit | (1+rand 2)·(1+rand 2)·(1+rand 4) = 1–16 | q | 2q | 32 + rand(64) = 32–95 |

**End** – Grundgenerierung (in Endstein, ohne Luft), jeder Chunk:

| Erz | Adern | Größe | Höhe y |
|---|---:|---:|---|
| Wolframit (Tungstate) | 4 | 16 | rand(128) |
| Sheldonit | 1 | 4 | rand(128) |
| Olivin | 5 | 8 | rand(128) |
| Sodalith | 8 | 16 | rand(128) |

**End – Asteroiden** (`EnderAsteroids`), davor, mit Wahrscheinlichkeit 1/100 je Chunk; `Y = 10 + rand(237)` (10–246):
1. Mit 1/25 zusätzlich ein **großer Asteroid**: Endstein-Ader Größe `100 + rand(101)` bei (x+rand 16, Y, z+rand 16),
   danach in `x−8+rand(24)`, `Y + rand(41)`, `z−8+rand(24)`: 5× Wolframit 16, 2× Sheldonit 4, 7× Olivin 8,
   12× Sodalith 16.
2. Immer: 5 Endstein-Adern Größe `30 + rand(31)` bei (x+rand 16, `Y + rand(51) − 25`, z+rand 16), dann in
   `x−8+rand(24)`, `Y + rand(41) − 20`, `z−8+rand(24)`: 5× Wolframit 12, 1× Sheldonit 4, 3× Olivin 8 (kein Sodalith).

Alle Asteroiden-Adern mit `allowVoid` (ersetzen auch Luft → frei schwebende Brocken). Der „große Asteroid“ reicht bis
rund ±50 Blöcke über den Chunk hinaus.

### 6.4 Verarbeitung im Pack (was die Erze freischalten)

Nether-/End-Präfix: `oreNether*`/`oreEnd*` werden zum Basisnamen gekürzt; die Nether-Mengen (`aNether`) wirken bei
Pyrit/Zinnober/Sphalerit nicht (feste Werte). **Macerator** (IC2, nur Hauptausgabe; Nebenprodukt nur im TE-Pulverizer):

| Erz | Macerator | Ofen | Sonstiges |
|---|---|---|---|
| Galena | 2 Galenastaub | – | – |
| Iridium | 2 IC2-Iridiumerz | – | – |
| Rubin / Saphir / Olivin | 2 Rubin- / Saphir- / Olivinstaub | – | – |
| Bauxit | 4 Bauxitstaub | – | – |
| Pyrit | 5 Pyritstaub | 1 Eisenbarren | Hochofen: + Calciumcarbonatzelle → 2 raff. Eisen + leere Zelle, 100 t, 128 EU/t, 1500 K |
| Zinnober | 3 Zinnoberstaub | – | – |
| Sphalerit | 4 Sphaleritstaub | – | – |
| Wolframit | 2 Wolframstaub | 1 Wolframstaub (`Tungsten`-Hochofenpflicht an) | – |
| Sheldonit | 2 Platinstaub | 1 Platinbarren | – |
| Sodalith | 12 Sodalithstaub | – | – |

**Industrial Grinder** (GT-Multiblock, **noch nicht nachgebaut**; Eingabe Erz + Wasserzelle, alternativ Wassereimer →
leerer Eimer; Ausgabe 4 = leere Zelle):

| Erz | mit Wasser | mit Sonderzelle |
|---|---|---|
| Galena | 2 Galenastaub, 1 Schwefelstaub | Quecksilberzelle: + 1 Silberstaub |
| Iridium | 2 Iridiumerz, 2 kl. Platinstaub | Quecksilberzelle: 2 Iridiumerz, 1 Platinstaub |
| Rubin | 1 Rubin, 6 kl. Rubinstaub, 2 kl. roter Granatstaub | – |
| Saphir | 1 Saphir, 6 kl. Saphirstaub, 2 kl. grüner Saphirstaub | – |
| Olivin | 1 Olivin, 6 kl. Olivinstaub, 2 kl. Smaragdstaub | – |
| Bauxit | 4 Bauxitstaub, 1 Aluminiumstaub | – |
| Pyrit | 5 Pyritstaub, 2 Schwefelstaub | – |
| Zinnober | 5 Zinnoberstaub, 2 kl. Redstone, 1 kl. Glowstonestaub | – |
| Sphalerit | 5 Sphaleritstaub, 1 Zinkstaub, 1 kl. gelber Granatstaub | Natriumpersulfatzelle: 3 Zinkstaub statt 1 |
| Wolframit | 2 Wolframstaub, 3 kl. Eisenstaub, 3 kl. Manganstaub | – |
| Sheldonit | 2 Platinstaub, 1 Nickelstaub, 2 Iridiumnuggets | Quecksilberzelle: 3 Platinstaub |
| Sodalith | 12 Sodalithstaub, 3 Aluminiumstaub | – |

Weiterverarbeitung der Stäube/Edelsteine (Zentrifuge, Elektrolyseur, Hochofen, Implosionskompressor …): Zeilen mit
Status „mit GT-Erzen“ in `13-gregtech-maschinen.md` und `12-gregtech-materialien.md`.

### 6.5 Umsetzung 1.21 (Vorschlag, nach den bisherigen Entscheidungen)

- Zwölf eigene Blöcke statt Meta (`craft:galena_ore` …); Werkzeugstufe über `needs_stone/iron/diamond_tool`, Tag
  `#minecraft:dragon_immune`, `c:ores/*`. Drops als Loot-Tabellen (Glück-Formeln über `uniform_bonus_count`/
  `table_bonus`; Iridium und Sodalith brauchen eine eigene Loot-Funktion), XP über den Block (`ExperienceRange`-Ersatz
  mit Behutsamkeitsprüfung wie Vanilla).
- Höhen absolut wie im Original, keine Erze unter y 0 (wie Zinn/Uran). Tiefenschiefer-Varianten für Galena, Iridium,
  Rubin, Saphir (Höhenbereich reicht in die Tiefenschieferzone; wie Zinn/Uran); Bauxit (ab y 32) nicht.
- Ersetzbar: Oberwelt `#stone_ore_replaceables` (Tiefenschiefer → Tiefenschiefer-Variante), Nether nur Netherrack,
  End Endstein (+ Luft bei Asteroiden).
- Biom: eigener Platzierungsfilter, Biom an (Chunk + 8, Oberfläche, Chunk + 8) statt 3D-Biom am Erzort.
- Asteroiden: Blöcke außerhalb der 3×3 beschreibbaren Chunks werden übersprungen (wie Öl-Weltgen).
- Textur: sechs Flächenbilder → pro Erz sechs Modellvarianten mit unterschiedlich verteilten Flächen (zufällig je
  Position), da die Flächenwahl per Positions-XOR in Standardmodellen nicht geht.
- **Entschieden (Nutzer, 2026-10-16):** Biomlisten mit 1.21-Entsprechungen: Rubin `desert`, `#is_badlands`,
  `#is_savanna`; Saphir `#is_ocean`, `#is_beach`, `mangrove_swamp`; Bauxit `plains`, `sunflower_plains`, `meadow`,
  `#is_forest`, `cherry_grove`.
- **Entschieden (Nutzer, 2026-10-16):** Industrial Grinder wird 1:1 nachgebaut (Teilpaket 8d, nach den Erzen).

### 6.6 Folgerezepte der Erz-Stäube und Edelsteine (`GT_OreDictHandler`, nachgetragen in Phase 8c)

Maschinenwerte wie in `13-gregtech-maschinen.md` (Zentrifuge immer 5 EU/t, Hochofen 128 EU/t). „Zellen“ = leere
Zellen als Eingabe 2. Umfang: **8c** umgesetzt, sonst fehlt das genannte Item noch.

| Eingabe | Maschine | Ausgabe | Ticks / EU/t | Umfang |
|---|---|---|---|---|
| Rubin / Saphir / grüner Saphir / Olivin / roter / gelber Granat | Macerator | 1 Staub des Edelsteins | – | 8c |
| Platinbarren | Macerator | 1 Platinstaub | – | 8c |
| Platinstaub | Ofen | Platinbarren | – | 8c |
| Eisenerz + Calciumcarbonatzelle | Hochofen | 3 raff. Eisen, leere Zelle | 100, 1000 K | 8c |
| Calcitstaub + leere Zelle | Canning Machine | Calciumcarbonatzelle | 100 / 1 | 8c |
| Calciumcarbonatzelle | Zentrifuge | Calcitstaub, leere Zelle | 40 | 8c |
| 10 Calcitstaub, 7 Zellen | Elektrolyseur | 2 Calcium-, 2 Kohlenstoff-, 3 Luftzellen | 700 / 80 | 8c |
| 2 Sphaleritstaub | Elektrolyseur | Zinkstaub, Schwefelstaub | 150 / 100 | 8c |
| 2 Zinnoberstaub, 1 Zelle | Elektrolyseur | Quecksilberzelle, Schwefelstaub | 100 / 128 | 8c |
| 2 Galenastaub | Elektrolyseur | 3 kl. Silber-, 3 kl. Blei-, 2 kl. Schwefelstaub | 1000 / 120 | 8c |
| 2 Galenastaub | Hochofen | Silberbarren, Bleibarren | 20, 1500 K | später (Silberbarren) |
| Zinkstaub | Zentrifuge | kl. Zinnstaub | 1050 | 8c |
| Zinkstaub | Ofen | Zinkbarren | – | später (Zinkbarren) |
| 8 Saphirstaub, 3 Zellen | Elektrolyseur | 2 Aluminiumstaub, 3 Luftzellen | 400 / 50 | 8c |
| grüner Saphirstaub | Elektrolyseur | Saphirstaub | 100 / 50 | 8c |
| 9 Rubinstaub, 3 Zellen | Elektrolyseur | 2 Aluminium-, 1 Chromstaub, 3 Luftzellen | 500 / 50 | später (Chrom) |
| 9 Olivinstaub, 3 Zellen | Elektrolyseur | 2 Magnesium-, 2 Eisenstaub, Siliziumzelle, 2 Luftzellen | 600 / 60 | später (Magnesium) |
| 12 Bauxitstaub, 8 Zellen | Elektrolyseur | 8 Aluminiumstaub, 2 kl. Titanstaub, 5 Wasserstoff-, 3 Luftzellen | 2000 / 128 | später (Titan) |
| Platinstaub | Zentrifuge | Iridiumnugget, kl. Nickelstaub | 3000 | 8d.2 |
| 16 roter / gelber Granatstaub | Zentrifuge | Pyrop/Almandin/Spessartin bzw. Andradit/Grossular/Uwarowit | 3000 / 3500 | später |
| 8 Olivin(staub) + fortg. Schaltkreis | Assembler | 4 Datenspeicherchips (Component 3) | 6400 / 8 | später |
| 4 Edelsteinstaub + ITNT | Implosionskompressor | siehe `13-gregtech-maschinen.md` §8 | 20 / 32 | 8c |

Massefabrikator-Verstärker: Rubin-, Saphir-, grüner Saphir-, Olivinstaub je 50 000, Platinstaub 100 000.
Kleiner Schwefelstaub (`dustSmallSulfur`, `dustssmall.png` Index 8) kommt mit der Galena-Elektrolyse; kleine Stäube
Calcit/Zink/Schwefel jeweils 4 ↔ 1 an der Werkbank.

## 7. Compact Solars, Forestry, Power Converters

Keine Weltgenerierung im Umfang (Forestry-Erze/Torf etc. entfallen).
