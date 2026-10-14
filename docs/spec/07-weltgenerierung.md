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

Abweichung 1.21: Weltgen über `BiomeModifier`/`PlacedFeature`; Welthöhe −64..320 – die Original-Y-Werte (20..30 für
Kugeln, 128 als Obergrenze) beziehen sich auf 1.4.7-Welten (0..255, Meeresspiegel 63). Vorschlag: Werte relativ zum
Meeresspiegel übernehmen (siehe Abweichungen).

## 6. GregTech-Weltgenerierung (Umfang offen)

GT generiert u. a. **Iridiumerz** (Overworld, 20 % Chance pro Chunk, Einzelblock y 0..98), Rubin/Saphir/Bauxit usw.
Iridiumerz droppt das IC2-Item „Iridiumerz“. Da die GT-Materialien in den Umfang kommen (`spec/12-gregtech-materialien.md`),
werden GT-Erze ggf. benötigt (Bauxit für Aluminium/Titan, Sheldonit für Platin, Rubin, Iridium). Ob und welche
GT-Erze generiert werden, wird zusammen mit dem Herstellungsweg der GT-Materialien entschieden. Werte:
`reference/decompiled/gregtech/gregtechmod/common/GT_Worldgenerator.java` und Pack-Config `GregTech.cfg`
(`worldgeneration`: Silver aus, alle anderen an, `IridiumProbability=20`).

## 7. Compact Solars, Forestry, Power Converters

Keine Weltgenerierung im Umfang (Forestry-Erze/Torf etc. entfallen).
