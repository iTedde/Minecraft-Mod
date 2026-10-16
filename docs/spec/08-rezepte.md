# 08 – Rezepte (finaler Pack-Stand inkl. GregTech-Änderungen)

Quellen: `ic2.core.IC2.registerCraftingRecipes` und Maschinen-`init()`, `IC2.registerOre` (OreDict-Ereignisse),
`gregtechmod.GT_Mod.postload`, `gregtechmod.common.GT_OreDictHandler`, GT-Configs aus dem Pack
(`reference/config/GregTech/*.cfg`), BuildCraft/Forestry/Power-Converters-Rezeptregistrierung.

## 0. Konventionen

**Markierungen**

| Marke | Bedeutung |
|---|---|
| **[IC2]** | IC2-Original, im Pack unverändert aktiv |
| **[GT~]** | IC2-Rezept von GregTech ersetzt; der Ersatz nutzt nur übernommene Items → **finale Version, wird so umgesetzt** |
| **[GT+]** | Von GregTech zusätzlich hinzugefügt, nur übernommene Items → wird umgesetzt |
| **[GT+M]** | Von GregTech zusätzlich hinzugefügt und nutzt GT-Material → im Pack vorhanden, wird mit den GT-Materialien umgesetzt (`12-gregtech-materialien.md`) |
| **[GT−]** | Von GregTech entfernt → im Pack nicht vorhanden, wird ebenfalls nicht umgesetzt (Ausnahme Massefabrikator: IC2-Rezept wird umgesetzt) |
| **[GT⚠]** | GregTech ersetzt/entfernt das IC2-Rezept, der Ersatz benötigt **GT-Material oder GT-Maschinen** → wird **wie im Pack** umgesetzt (Nutzerentscheidung); Material siehe `12-gregtech-materialien.md` |
| *(versteckt)* | IC2 „hidden“-Rezept (im Original nicht in NEI angezeigt) |

**Muster-Notation**: Zeilen durch `/` getrennt, Leerzeichen = leeres Feld. Legende dahinter.
IC2-Rezepte sind spiegel-**un**symmetrisch (kein Spiegeln!) und kleiner als 3×3 frei verschiebbar (`AdvRecipe`).

**OreDict → 1.21-Tags** (Vorschlag): `ingotCopper` → `c:ingots/copper`, `ingotTin` → `c:ingots/tin`,
`ingotBronze` → `c:ingots/bronze`, `ingotRefinedIron` → `c:ingots/refined_iron`, `ingotUranium`, `ingotSilver`
(nur falls vorhanden), `dustCoal/Tin/Copper/Gold/Iron/Clay/Bronze/Silver` → `c:dusts/*`, `itemRubber` →
`c:rubber` (Mod-Tag), `plankWood` → `minecraft:planks`, `stickWood` → `c:rods/wooden`, `treeLeaves`/`treeSapling` →
`minecraft:leaves`/`saplings`, `blockUranium` → `c:storage_blocks/uranium`, Farbstoffe `dyeX` → `c:dyes/x`.

**Flüssigkeits-Zutat** `liquid$Wasser`: jeder registrierte gefüllte Wasserbehälter (Wassereimer **oder** Wasserzelle);
das leere Behältnis bleibt im Raster (Eimer) bzw. wird verbraucht (Zelle, kein Container-Item).

**Elektrische Zutaten**: Ladung wird aufs Ergebnis übertragen (`01-eu-system.md` §5). RE-Batterie: leere und geladene
Variante sind zwei Items; IC2 registriert Rezepte meist doppelt (beide Varianten).

**GT-Mechanik**: `GT_ModHandler.addCraftingRecipe` entfernt zuerst jedes Rezept mit identischem Raster (erste OreDict-
Variante) und fügt dann das neue hinzu. `addPulverisation/Compression/ExtractionRecipe` entfernen ein vorhandenes
IC2-Maschinenrezept für dieselbe Eingabe und fügen das neue ein.

---

## 1. Materialien und Komponenten

| Ergebnis | Muster / Zutaten | Status |
|---|---|---|
| Kupfer-/Zinn-/Bronze-/Uranblock | `MMM/MMM/MMM`, M = Barren | **[GT−]** (GT entfernt Crafting für Speicherblöcke, `storageblockcrafting.*=false`) |
| 9 Barren aus Block | `B` | **[GT−]** (`storageblockdecrafting.*=false`); Ersatz siehe Ofen/Compressor/Macerator unten |
| Speicherblock (Kupfer ausgenommen) | **Compressor**: 9 Barren → 1 Block | **[GT+]** (`StorageBlockCompressing`, Kupfer `=false`) |
| 9 Barren aus Speicherblock | **Ofen**: Block → 9 Barren | **[GT+]** |
| 9 Staub aus Speicherblock | **Macerator**: Block → 9 Staub (Kupfer, Zinn, Bronze; Uranblock → 9 GT-Uranstaub) | **[GT+]** (Uranstaub **[GT+M]**) |
| 8 raffiniertes Eisen | `M`, M = Maschinenblock | [IC2] |
| Maschinenblock | `III/I I/III`, I = raffiniertes Eisen | [IC2] |
| Fortg. Maschinenblock | ` A /CMC/ A ` und ` C /AMA/ C `, A = fortg. Legierung, C = Kohleplatte, M = Maschinenblock | [IC2] |
| Elektronischer Schaltkreis | `CCC/RIR/CCC` und `CRC/CIC/CRC`, C = isol. Kupferkabel, R = Redstone, I = raff. Eisen | **[GT~]** (identisch, OreDict `copperWire` = isol. Kupferkabel) |
| Fortgeschrittener Schaltkreis | `RGR/LCL/RGR` und `RLR/GCG/RLR`, R = Redstone, G = Glowstonestaub, L = Lapis, C = el. Schaltkreis | **[GT~]** (identisch, `itemLazurite` = Lapis) |
| 2 Mischmetallbarren | `III/BBB/TTT`, I = raff. Eisen, B = Bronze, T = Zinn | **[GT⚠]** entfernt; GT-Ersatz senkrecht `X/Y/Z` aus **Platten** (raff. Eisen/Bronze/Zinn → 1; Stahl/Invar/Titan/Wolfram … mehr) |
| Kohlefaser | `CC/CC`, C = Kohlestaub | [IC2] + **[GT+M]** formlos 9 GT-Kohlenstoffzellen → 1 Kohlefaser |
| Kohlenetz | formlos 2 Kohlefaser | [IC2] |
| Iridiumplatte | `IAI/ADA/IAI`, I = Iridiumerz, A = fortg. Legierung, D = Diamant **oder** Industriediamant | **[GT⚠]** beide entfernt; GT: Iridium-Legierungsbarren (Walzmaschine/Werkbank aus GT-Iridiumbarren + GT-Diamantstaub) → **Implosionskompressor** → Iridiumplatte |
| Kohleball | `CCC/CFC/CCC`, C = Kohlestaub, F = Feuerstein | [IC2] |
| Kohleklumpen | `###/#O#/###`, # = kompr. Kohleball, O = Obsidian; *(versteckt)* auch O = Eisenblock, O = Ziegelblock | [IC2] |
| Kleiner Eisenstaub | `CTC/TCT/CTC` und `TCT/CTC/TCT`, C = Kupferstaub, T = Zinnstaub *(versteckt)* | [IC2] |
| Eisenstaub | formlos 2 kleiner Eisenstaub | [IC2] |
| 2 Bronzestaub | formlos Zinnstaub + 3 Kupferstaub | [IC2] |
| 1 Bronzebarren | formlos 3 Kupferbarren + 1 Zinnbarren | **[GT+]** (`ForestryBronzeNerf=true` → 1 Stück) |
| 8 hydrierter Kohlestaub | `CCC/CWC/CCC`, C = Kohlestaub, W = Wasser | [IC2] |
| Hydrierter Kohlestaub | formlos Kohlestaub + Wasser | [IC2] |
| Diamant | formlos Industriediamant | [IC2] |
| 3 Bauschaum | formlos Tonstaub + Wasser + Redstone + Kohlestaub | [IC2] |
| Pflanzenball | `PPP/P P/PPP` aus Weizen, Zuckerrohr, Samen, Blätter (`treeLeaves`), Gummiblätter, Gras (`tallGrass`), Gummisetzling; 2 Stück aus Setzlingen (`treeSapling`) bzw. Gummisetzling (doppelt registriert) | [IC2] |
| 16 Münzen | `II/II`, I = raff. Eisen | [IC2] (Config `enableCraftingCoin`) |
| Raff. Eisenbarren | `III/III/III`, I = Münze | [IC2] |
| Schrottbox | `SSS/SSS/SSS`, S = Schrott | [IC2] |

## 2. Kabel

| Ergebnis | Muster | Status |
|---|---|---|
| 6 isol. Kupferkabel | `RRR/CCC/RRR` und `RCR/RCR/RCR`, R = Gummi, C = Kupferbarren | [IC2] |
| 6 Kupferkabel | `CCC` | [IC2] |
| 12 Goldkabel | `GGG` | [IC2] |
| 4 isol. Goldkabel | ` R /RGR/ R ` | [IC2] |
| 4 doppelt isol. Goldkabel | `RRR/RGR/RRR` | **[GT+]** |
| 12 HV-Kabel | `III`, I = raff. Eisen | [IC2] |
| 4 isol. HV-Kabel | ` R /RIR/ R ` | [IC2] |
| 9 Zinnkabel | `TTT` | [IC2] |
| 4 Glasfaserkabel | `GGG/RDR/GGG`, G = Glas, R = Redstone, D = Diamant oder Industriediamant | **[GT~]** (identisch) |
| 6 Glasfaserkabel | `GGG/SDS/GGG`, S = Silberbarren | [IC2] (Silberbarren = GT-Material, siehe `12-gregtech-materialien.md`); **[GT+M]** 8 Stück mit Elektrumbarren |
| Detektorkabel | ` C /RIR/ R `, C = el. Schaltkreis, I = 4×isol. HV-Kabel | [IC2] |
| Splitterkabel | ` R /ILI/ R `, L = Hebel, I = 4×isol. HV-Kabel | [IC2] |
| Isolierung formlos | Gummi + Kupferkabel → isol. Kupfer; Gummi + Gold → isol. Gold; Gummi + isol. Gold → 2× isol. Gold; 2 Gummi + Gold → 2× isol. Gold; Gummi + HV → isol. HV; Gummi + isol. HV → 2×; Gummi + 2× → 4×; 2 Gummi + isol. HV → 4×; 2 Gummi + HV → 2×; 3 Gummi + HV → 4× | [IC2] |

## 3. Maschinen

| Ergebnis | Muster | Status |
|---|---|---|
| Eisenofen | `III/I I/III` und ` I /I I/IFI` (F = Ofen), I = Eisenbarren | [IC2] |
| Elektroofen | ` C /RFR`, C = el. Schaltkreis, R = Redstone, F = Eisenofen | [IC2] |
| Macerator | `FFF/SMS/ C `, F = Feuerstein, S = Bruchstein, M = Maschinenblock, C = el. Schaltkreis | **[GT~]** ersetzt durch `FDF/DMD/FAF`, F = Feuerstein, D = Diamant (oder Industriediamant), M = Maschinenblock, A = **fortg. Schaltkreis** + **[GT+M]** `FDF/AMA/FAF` mit GT-Schleifkopf (D) und el. Schaltkreisen (A) |
| Extractor | `TMT/TCT`, T = Baumzapfer | [IC2] |
| Compressor | `S S/SMS/SCS`, S = Stein | [IC2] |
| Miner | `CMC/ P / P `, P = Bergbaurohr | [IC2] |
| Pumpe | `cCc/cMc/PTP`, c = leere Zelle, T = Baumzapfer, P = Bergbaurohr | [IC2] |
| Magnetisierer | `RFR/RMR/RFR`, F = Eisenzaun | [IC2] |
| Elektrolyseur | `c c/cCc/EME`, c = isol. Kupferkabel, E = leere Zelle | [IC2] |
| Recycler | ` G /DMD/IDI`, G = Glowstonestaub, D = Erde, M = Compressor, I = raff. Eisen | [IC2] |
| Canning Machine | `TCT/TMT/TTT`, T = Zinnbarren | [IC2] |
| Induktionsofen | `CCC/CFC/CMC`, C = Kupferbarren, F = Elektroofen, M = fortg. Maschinenblock | [IC2] |
| Massefabrikator | `GCG/ALA/GCG`, G = Glowstonestaub, C = fortg. Schaltkreis, A = fortg. Maschinenblock, L = Lapotronkristall | [GT−] im Pack (`ic2recipes.Massfabricator=false`); **entschieden: IC2-Rezept wird trotzdem umgesetzt** → §10 |
| Terraformer | `GTG/DMD/GDG`, T = TFBP leer, G = Glowstonestaub, D = Erde, M = fortg. Maschinenblock | [IC2] |
| Teleporter | `GFG/CMC/GDG`, F = Frequenzsender, C = Glasfaserkabel, G = fortg. Schaltkreis, D = Industriediamant oder Diamant, M = fortg. Maschinenblock | [IC2] |
| Tesla-Spule | `RRR/RMR/ICI`, M = MV-Trafo | [IC2] |
| Persönlicher Safe | `c/M/C`, c = el. Schaltkreis, C = Truhe | [IC2] |
| Trade-O-Mat | `RRR/CMC`, C = Truhe | [IC2] |
| Energy-O-Mat | `RBR/CMC`, B = RE-Batterie (leer), C = isol. Kupferkabel | [IC2] |
| Crop-Matron | `cBc/CMC/CCC`, c = el. Schaltkreis, B = Truhe, C = Crop-Stab | [IC2] |
| 8 Luminator | `ICI/GTG/GGG`, I = raff. Eisen, C = isol. Kupferkabel, T = Zinnkabel, G = Glas | [IC2] |
| 8 Bergbaurohre | `I I/I I/ITI`, T = Baumzapfer | [IC2] |
| Reaktorkammer | ` C /CMC/ C `, C = dichte Kupferplatte | [IC2] |

## 4. Generatoren, Speicher, Trafos

| Ergebnis | Muster | Status |
|---|---|---|
| Generator | ` B /III/ F ` (B = RE-Batterie, I = raff. Eisen, F = Eisenofen) und `B/M/F` (F = Ofen, M = Maschinenblock), je mit leerer/geladener Batterie | **[GT~]** gleichwertig (B = jede `battery`, M = `craftingRawMachineTier01`); + **[GT+M]** Varianten mit Aluminium-/Invarbarren und raff.-Eisen-/Aluminium-/Invarplatten |
| 2 Wassermühlen | `SPS/PGP/SPS`, S = Stock, P = Bretter, G = Generator | **[GT⚠]** entfernt (`ExpensiveWatermillRecipe`); im Pack nur über GT-Assembler (Generator + 4 Aluminiumplatten → 2 Wassermühlen) |
| Windmühle | `I I/ G /I I`, I = Eisenbarren | **[GT⚠]** entfernt (`ExpensiveWindRecipe`); im Pack nur GT-Assembler (Generator + 4 Kohleplatten → Windmühle) |
| Solarpanel | `CgC/gCg/cGc`, C = Kohlestaub, g = Glas, c = el. Schaltkreis, G = Generator | **[GT⚠]** ersetzt: `GGG/SPS/CHC`, G = Glasscheibe, S = **Silizium-Platte (GT)**, P = Kohleplatte, C = el. Schaltkreis, H = Generator |
| Geothermiegenerator | `gCg/gCg/IGI`, g = Glas, C = leere Zelle, I = raff. Eisen | [IC2] |
| Kernreaktor | ` c /CCC/ G `, c = fortg. Schaltkreis, C = Reaktorkammer | [IC2] |
| BatBox | `PCP/BBB/PPP`, P = Bretter, C = isol. Kupferkabel, B = RE-Batterie | [IC2] |
| MFE | `cCc/CMC/cCc`, c = 2× isol. Goldkabel, C = Energiekristall, M = Maschinenblock | **[GT~]** gleichwertig (C = jeder Energiekristall unabhängig von Ladung) |
| MFSU | `LCL/LML/LAL`, L = Lapotronkristall, C = fortg. Schaltkreis, M = MFE, A = fortg. Maschinenblock | [IC2] |
| LV-Trafo | `PCP/ccc/PCP`, P = Bretter, C = isol. Kupferkabel, c = Kupferbarren | [IC2] |
| MV-Trafo | ` C / M / C `, C = 2× isol. Goldkabel | [IC2] |
| HV-Trafo | ` c /CED/ c `, c = 4× isol. HV-Kabel, C = el. Schaltkreis, E = MV-Trafo, D = Energiekristall | [IC2] |

## 5. Batterien

| Ergebnis | Muster | Status |
|---|---|---|
| RE-Batterie | ` C /TRT/TRT`, C = isol. Kupferkabel, T = Zinn, R = Redstone | [IC2] + **[GT+M]** 2 Stück aus Bleistaub, Schwefelsäurezelle, Zinnplatten; 32 SU-Batterien aus Quecksilber-/Schwefelsäurezellen (siehe GT-Code) |
| 5 SU-Batterien | `C/R/D` und `C/D/R`, D = Kohlestaub | [IC2] |
| 8 SU-Batterien | `c/C/R` und `c/R/C`, C = hydr. Kohlestaub | [IC2] |
| Energiekristall | `RRR/RDR/RRR`, D = Diamant oder Industriediamant | [IC2] + **[GT+M]** `DDD/DRD/DDD` mit Rubin (R) statt Diamant |
| Lapotronkristall | `LCL/LDL/LCL`, L = Lapis, C = el. Schaltkreis, D = Energiekristall | **[GT~]** gleichwertig (Energiekristall beliebiger Ladung; **[GT+M]** Saphir statt Energiekristall) |

## 6. Reaktor-Komponenten

| Ergebnis | Muster | Status |
|---|---|---|
| Uranzelle | formlos leere Zelle + Uranbarren | **[GT⚠]** entfernt; im Pack nur GT-Canning-Machine |
| Uranzelle | formlos angereicherte Uranzelle + Kohlestaub | [IC2] |
| Depleted Isotope Cell (Damage 9999) | formlos fast erschöpfte Uranzelle + Kohlestaub | [IC2] |
| 8 fast erschöpfte Uranzellen | `CCC/CUC/CCC`, C = leere Zelle, U = Uranbarren | [IC2] |
| Doppel-Uranzelle | `UCU`, C = dichte Kupferplatte | [IC2] |
| Vierfach-Uranzelle | ` U /CCC/ U `, U = Doppelzelle | [IC2] |
| 10k-Kühlzelle | ` T /TWT/ T `, W = Wasser | [IC2] |
| 30k-Kühlzelle | `TTT/CCC/TTT` | [IC2] |
| 60k-Kühlzelle | `TCT/TcT/TCT`, c = dichte Kupferplatte | [IC2] |
| Reaktorplatte | formlos dichte Kupferplatte + fortg. Legierung | [IC2] |
| Wärmekap. Platte | formlos Platte + 2 dichte Kupferplatten | [IC2] |
| Containment-Platte | formlos Platte + 2 fortg. Legierung | [IC2] + **[GT+M]** formlos Platte + Bleiplatte (Phase 8a) |
| Wärmetauscher | ` c /TCT/ T `, c = el. Schaltkreis, C = dichte Kupferplatte | [IC2] |
| Reaktor-Wärmetauscher | `C/S/C` | [IC2] |
| Komponenten-Wärmetauscher | ` G /GSG/ G `, G = Gold | [IC2] |
| Fortg. Wärmetauscher | `GcG/SCS/GcG`, G = Glasfaserkabel | [IC2] |
| Wärmeableiter | `I#I/# #/I#I`, I = raff. Eisen, # = Eisengitter | [IC2] + **[GT+M]** `AIA/I I/AIA` mit Aluminiumplatten (A) und Eisengittern (I) |
| Reaktor-Wärmeableiter | `C/V/C` | [IC2] |
| Vergoldeter Wärmeableiter | `G/V/G`, V = Reaktor-Wärmeableiter | [IC2] |
| Komponenten-Wärmeableiter | `#T#/TVT/#T#` | [IC2] |
| Fortg. Wärmeableiter | `#V#/#D#/#V#` | [IC2] |
| Heizzelle | `c/L/C`, L = Lavazelle | [IC2] |
| Neutronenreflektor | `TcT/cCc/TcT`, T = Zinnstaub, c = Kohlestaub | [IC2] |
| Dicker Neutronenreflektor | ` R /RCR/ R ` | **[GT⚠]** ersetzt (`BeryliumReflector`): ` N /NBN/ N ` mit B = **Berylliumzelle (GT)** |
| RSH-Kondensator | `RRR/RVR/RSR`, V = Wärmeableiter, S = Wärmetauscher; Reparatur + Redstone (−10 000) | [IC2] |
| LZH-Kondensator | `RVR/CLC/RSR`, V = Reaktor-Wärmeableiter, S = Reaktor-Wärmetauscher, C = RSH, L = Lapisblock; Reparatur Redstone −5000, Lapis −40 000 | [IC2] |

## 7. Werkzeuge

| Ergebnis | Muster | Status |
|---|---|---|
| Baumzapfer | ` P /PPP/P  ` | [IC2] |
| Bronze-Spitzhacke/Axt/Hacke/Schwert/Schaufel | `BBB/ S / S `, `BB/SB/S `, `BB/S /S `, `B/B/S`, ` B / S / S ` | [IC2] – **entfällt** (Nutzerentscheidung) |
| Schraubenschlüssel | `B B/BBB/ B ` | [IC2] |
| Isolierungsschneider | `A A/ A /I I`, A = raff. Eisen, I = Eisen | [IC2] |
| Werkzeugkasten | `ICI/III`, C = Truhe | [IC2] |
| Farbroller | ` CC/ IC/I  `, C = Wolle, I = Eisen; Farbe: formlos Roller + Farbstoff; zurück: formlos | [IC2] (GT dupliziert die Rückrezepte identisch) |
| Bauschaumsprüher (leer, Dmg 1601) | `SS /Ss /  S`, S = Bruchstein, s = Stock; Füllen mit Pellets | [IC2] |
| Bergbaubohrer | ` I /ICI/IBI`, B = RE-Batterie | **[GT⚠]** ersetzt: ` S /SCS/SBS`, S = **Stahlplatte** (`ElectricSteelTools=true`; ohne: raff.-Eisen-Platte), C = el. Schaltkreis, B = Batterie |
| Diamantbohrer | ` D /DdD`, d = Bergbaubohrer | **[GT⚠]** ersetzt: ` D /DMD/TAT`, T = **Titanplatte**, A = fortg. Schaltkreis (`TitaniumDiamondDrill=true`) |
| Kettensäge | ` II/ICI/BI ` | **[GT⚠]** ersetzt: ` SS/SCS/BS ` (Stahlplatten) |
| Elektrische Hacke | `II / C / B ` | **[GT⚠]** ersetzt: `SS / C / B ` (Stahlplatten) |
| Elektrischer Baumzapfer | `  W/ C /B  ` (W = Baumzapfer) | **[GT⚠]** ersetzt: ` B /SCS/S  ` (Stahlplatten, ohne Baumzapfer) |
| Elektrischer Schraubenschlüssel | `  W/ C /B  ` (W = Schraubenschlüssel) | **[GT⚠]** ersetzt: `S S/SCS/ B ` (Stahlplatten) |
| OD-Scanner | ` G /CBC/ccc`, G = Glowstonestaub, B = RE-Batterie, c = isol. Kupferkabel | [IC2] |
| OV-Scanner | ` G /GCG/cSc`, S = OD-Scanner (bzw. geladene RE-Batterie – Originalfehler im zweiten Rezept), c = 2× isol. Goldkabel, C = fortg. Schaltkreis | [IC2] |
| EU-Meter | ` G /cCc/c c` | [IC2] |
| Bergbaulaser | `Rcc/AAC/ AA`, c = Energiekristall, A = fortg. Legierung, C = fortg. Schaltkreis | **[GT⚠]** entfernt (`ExpensiveLaser`); Ersatz `RHE/TTC/ AA` mit Rubin, Titanplatten, GT-Heliumkühlzelle |
| Nanosäbel | `GA /GA /CcC`, C = Kohleplatte, c = Energiekristall | **[GT⚠]** ersetzt: `PI /PI /CLC`, P = Platinplatte, I = Iridiumplatte, L = Lapotron, C = GT-Energiefluss-Schaltung (`craftingCircuitTier07`, Korrektur 8e) |
| Elektrische Hacke etc. siehe oben | | |
| Frequenzsender | formlos el. Schaltkreis + isol. Kupferkabel | [IC2] |
| Fernzünder | ` c /GCG/TTT` (T = TNT) und ` C /TLT/ F ` (T = Zinn, L = Lapis, F = Frequenzsender) | [IC2] |
| Cropnalyzer | `cc /RGR/RCR` | [IC2] |
| Diamantwerkzeuge aus Industriediamant | Spitzhacke, Hacke, Schaufel, Axt, Schwert (Vanilla-Muster) | [IC2] |

## 8. Rüstung

| Ergebnis | Muster | Status |
|---|---|---|
| Bronze-Rüstung | Vanilla-Muster mit Bronzebarren | [IC2] – **entfällt** (Nutzerentscheidung) |
| Kompositweste | `A A/ALA/AIA` und `A A/AIA/ALA`, L = Lederrüstung (Brust), I = Eisenbrustplatte, A = fortg. Legierung | [IC2] |
| Nano-Helm/-Brust/-Hose/-Stiefel | `CcC/CGC`, `C C/CcC/CCC`, `CcC/C C/C C`, `C C/CcC`; C = Kohleplatte, c = Energiekristall, G = Glas | [IC2] |
| Quantum-Helm | ` n /ILI/CGC`, n = Nano-Helm, I = Iridiumplatte, L = Lapotron, G = verst. Glas, C = fortg. Schaltkreis | [IC2] (`QSuithelmet=true`) |
| Quantum-Brust | `AnA/ILI/IAI` | [IC2] |
| Quantum-Hose | `MLM/InI/G G`, M = Maschinenblock, G = Glowstonestaub | [IC2] |
| Quantum-Stiefel | `InI/RLR`, R = Gummistiefel | [IC2] |
| Hazmat-Helm/-Anzug/-Hose | ` O /RGR/R#R`, `R R/ROR/ROR`, `ROR/R R/R R`; O = oranger Farbstoff, # = Eisengitter | [IC2] |
| Gummistiefel | `R R/R R/RWR`, W = Wolle | [IC2] |
| Jetpack (leer, Dmg 18001) | `ICI/IFI/R R`, F = leerer Kanister | [IC2] |
| El. Jetpack | `ICI/IBI/G G`, C = fortg. Schaltkreis, B = BatBox | [IC2] |
| BatPack | `BCB/BTB/B B`, B = RE-Batterie, T = Zinn | [IC2] |
| LapPack | `LAL/LBL/L L`, L = Lapisblock, A = fortg. Schaltkreis, B = BatPack | **[GT~]** gleichwertig |
| CF-Pack (Dmg 259) | `SCS/FTF/F F`, S = leerer Schaumsprüher, F = leerer Kanister | [IC2] |
| Solarhelm | `III/ISI/CCC` und ` H / S /CCC` (H = Eisenhelm), S = Solarpanel | [IC2] |
| Static Boots | `I I/ISI/CCC` und ` H / S /CCC` (H = Eisenstiefel), S = Wolle | [IC2] |
| Nachtsichtbrille | – | kein Rezept in IC2 1.115 und GT |

## 9. Sonstiges

| Ergebnis | Muster | Status |
|---|---|---|
| 8 verst. Stein | `SSS/SAS/SSS`, S = Stein | [IC2] |
| 7 verst. Glas | `GAG/GGG/GAG` und `GGG/AGA/GGG` | [IC2] |
| Verst. Tür | `SS/SS/SS`, S = verst. Stein | [IC2] |
| 3 Gummimatten | `RRR/RRR` | [IC2] |
| 4 Fackeln *(versteckt)* | `R/I`, R = Harz | [IC2] |
| Holzgerüst | `PPP/ s /s s` → **16** | **[GT~]** → **4** Stück |
| 16 Eisengerüst | `PPP/ s /s s`, P = raff. Eisen, s = Eisenzaun | [IC2] |
| 12 Eisenzaun | `III/III` | [IC2] |
| 4 ITNT | `FFF/TTT/FFF` und `FTF/FTF/FTF`, F = Feuerstein | [IC2] + **[GT+M]** 5 ITNT aus `FFF/TTT/FFF` mit Feuersteinstaub |
| Nuke | `RCR/UMU/RCR`, R = angereicherte Uranzelle, C = fortg. Schaltkreis, U = Uranblock, M = fortg. Maschinenblock | [IC2] (GT-Ersatz greift nicht, Raster passt nicht) |
| 8 Dynamit | formlos ITNT + Faden | [IC2] |
| 8 Klebedynamit | `DDD/DRD/DDD`, R = Harz | [IC2] |
| Klebekolben *(versteckt)* | formlos Kolben + Harz | [IC2] |
| Kolben aus Bronze *(versteckt)* | `TTT/#X#/#R#` | **[GT~]** ersetzt durch gleichwertiges Muster `WWW/CBC/CRC` mit B = Bronze bzw. raff. Eisen (bzw. Eisen) |
| Glowstonestaub *(versteckt)* | `RGR/GRG/RGR`, G = Goldstaub | [IC2] |
| 3 Schwarzpulver *(versteckt)* | `RCR/CRC/RCR`, C = Kohlestaub | [IC2] |
| Eimer *(versteckt)* | `T T/ T ` | [IC2] |
| 8 Schienen *(versteckt)* | `B B/BsB/B B`, B = Bronze | [IC2] |
| 2 Crop-Stäbe | `S S/S S` | [IC2] |
| 2 Dünger | formlos Schrott + Knochenmehl; formlos 2 Schrott + Dünger | [IC2] |
| 2 Dünger | formlos Dünger + Knochenmehl | **[GT+]** |
| Weed-EX | `R/G/C`, G = Grinpulver, C = leere Zelle | [IC2] |
| Tasse | `SS /SSS/SS `, S = Stein | [IC2] |
| Kaffeepulver | formlos Kaffeebohnen | [IC2] |
| Kalter Kaffee | formlos Tasse + Kaffeepulver + Wasser | [IC2] |
| Milchkaffee | formlos Kaffee (Meta 1) + Zucker + Milcheimer | [IC2] |
| Fass | `P/W/P`, P = Bretter, W = Gummiholz; leeren: Fass → Fass; Tasse (Bier) → leere Tasse | [IC2] |
| Bretter (Tropenholz) ×3 | `W`, W = Gummiholz | [IC2] |
| **4** leere Zellen | ` T /T T/ T ` | **[GT~]** (IC2: 16; GT `TincellsPer4Tin=4`) |
| Kanister | ` TT/T T/TTT` | [IC2] |
| 4 Zinndosen | `T T/TTT` | [IC2] |
| Wasser-/Lavazelle | formlos leere Zelle + Wasser-/Lavaeimer | **[GT⚠]** entfernt (GT-Canning-Machine) – Wasser-/Lavazellen bleiben über Rechtsklick und Pumpe erhältlich |
| Bio-/hydr. Kohlezelle | formlos Zelle + kompr. Pflanzenball / hydr. Kohleklumpen | **[GT⚠]** entfernt (GT-Canning-Machine) |
| Leere Zelle | formlos Luftzelle | **[GT+]** |
| Obsidian | formlos 2 Wasser- + 2 Lavazellen | [IC2] |
| Upgrades | Overclocker `CCC/WEW` (C = 10k-Kühlzelle, W = isol. Kupferkabel, E = el. Schaltkreis); Transformer `GGG/WTW/GEG` (G = Glas, W = 2× isol. Goldkabel, T = MV-Trafo); Energiespeicher `www/WBW/wEw` (w = Bretter, B = RE-Batterie) | [IC2] + **[GT+M]** 2 Overclocker ` H /WCW` mit Helium-/NaK-Kühlzelle (H) |
| TFBP leer | ` C / A /R R` | [IC2] |
| TFBP-Varianten | ` S /S#S/ S ` mit Samen (Cultivation), Sand (Desertification), Wassereimer (Irrigation), Schneeball (Chilling), Erde (Flatification); Mushroom `mMm/M#M/mMm` (M = brauner Pilz, m = Myzel); zurück formlos (außer Mushroom) | [IC2] |

### UU-Materie (alle *(versteckt)*; M = UU-Materie)

| Muster | IC2-Original | Finaler Pack-Stand |
|---|---|---|
| `   / M /   ` | 16 Stein | 16 Stein [GT~ identisch] |
| ` M /M M/ M ` | 32 Glas | 32 Glas |
| `   /M  /M  ` | 16 Gras | 16 Gras |
| `   / M /M M` | 16 bemooster Bruchstein | gleich |
| `   /  M/ M ` | 16 Sandstein | gleich |
| `M M/   /   ` | 4 Schneeblock | **16 Schneeblock** [GT~] |
| `   / M / M ` | 1 Wasser | gleich |
| ` M / M / M ` | 1 Lava | gleich |
| `M M/ M /M M` | 2 Eisenerz | **4 Eisenstaub** [GT~] |
| ` M /MMM/ M ` | 2 Golderz | **2 Goldstaub** [GT~] |
| `M M/M M/   ` | 12 Obsidian | gleich |
| `  M/ M /M  ` | 16 Netherrack | gleich |
| ` M /M M/MMM` | 8 Glowstone (Block) | **32 Glowstonestaub** [GT~] |
| ` M /   /   ` | 8 Holz (Eiche) | gleich; [GT+] `M  /   /   ` 8 Fichte, `  M/   /   ` 8 Birke, `   /M  /   ` 8 Tropenholz |
| ` M /MMM/M M` | 48 Kaktus | gleich |
| `M  /M  /M  ` | 24 Ranken | gleich |
| `M M/   / M ` | 12 Wolle | **32 Faden** [GT~] |
| `  M/M  /  M` | 20 Kohle | **8 Kohle** [GT~] |
| `MMM/MMM/MMM` | 1 Diamant | gleich |
| `   / M /MMM` | 24 Redstone | gleich |
| ` M / M / MM` | 9 Lapis | gleich |
| ` M / M /M M` | 32 Federn | gleich |
| `   /   /MMM` | 16 Schneebälle | **entfernt** [GT−] |
| `MMM/M  /MMM` | 15 Schwarzpulver | gleich |
| `MM /M  /MM ` | 48 Ton | gleich |
| `MM /  M/MM ` | 32 Kakaobohnen | gleich |
| ` MM/ MM/ M ` | 48 Tintenbeutel | gleich |
| `M M/M M/M M` | 48 Zuckerrohr | gleich |
| ` M /MM /MM ` | 32 Feuerstein | gleich |
| `M  /MM /M  ` | 32 Knochen | gleich |
| `M M/   /M M` | 21 Harz | gleich |
| `MMM/ M /MMM` | 1 Iridiumerz | gleich |
| `   /M M/MMM` | 24 Myzel | gleich |
| `MM /MM /M  ` | 48 gemeißelter Steinziegel | gleich |
| `  M/M M/   ` | 10 Kupferstaub (Kupfererz-Gen aus) | 10 Kupferstaub |
| `   /M M/  M` | 10 Zinnstaub | 10 Zinnstaub |
| `UUU/UUU/ U ` | – | **[GT+]** 2 Smaragde |
| `UUU/U U/UUU` | – | **[GT+]** 1 Enderperle |
| `U U/UU /U U` | – | **[GT+]** 4 Lohenruten |
| `U U/UUU/ U ` | – | **[GT+]** 32 Seerosen |
| `U U/ U /UUU` | – | **[GT+]** 32 Leder |
| ` U /UUU/UUU` | – | **[GT+]** 14 Silberstaub (IC2-Item) |
| Rubin, Saphire, Olivin, Zink-, Nickel-, Blei-, Platin-, Wolfram-, Nikolit-, Osmium-, Titan-, Aluminium-, Plutoniumstaub | – | **[GT+M]** (Muster siehe `GT_Mod.java` Z. 4969–5032) |

## 10. Maschinenrezepte

### 10.1 Ofen (Vanilla-Ofen, Eisenofen, Elektroofen, Induktionsofen)

| Eingabe | Ausgabe | XP | Status |
|---|---|---:|---|
| Kupfererz / Zinnerz | Kupfer-/Zinnbarren | 0,5 | [IC2] |
| Gummiholz | Tropenholz-Stamm | 0,1 | [IC2] |
| Eisenbarren | **raffiniertes Eisen** | 0,2 | [IC2] |
| Eisen-, Gold-, Zinn-, Kupfer-, Bronzestaub | jeweiliger Barren | 0 | [IC2] |
| Silberstaub | Silberbarren (nur wenn vorhanden) | 0,8 | [IC2] |
| Hydrierter Kohlestaub | Kohlestaub | 0 | [IC2] (GT dupliziert) |
| Harz | Gummi | 0,3 | [IC2] |
| Kalter Kaffee (Meta 0) | Kaffee (Meta 1) | 0,1 | [IC2] |
| Maschinenblock | 8 raff. Eisen | 0 | **[GT+]** |
| Dichte Kupferplatte | 8 Kupferbarren | 0 | **[GT+]** |
| Speicherblöcke Kupfer/Zinn/Bronze/Uran | 9 Barren | 0 | **[GT+]** |
| Schleimball | Harz | 0 | **[GT+]** |
| Schraubenschlüssel | 6 Bronzebarren | 0 | **[GT+]** |
| Kanister | 7 Zinnbarren | 0 | **[GT+]** |
| Zinndose | 1 Zinnbarren | 0 | **[GT+]** |
| Vanilla: Kompass 4 Eisen, Taschenuhr 4 Gold, Schere 2 Eisen, Lore 5, Eimer 3, Eisentür 6, Amboss 31/20/10 Eisen | | 0 | [GT+] (Vanilla-Items, optional) |

### 10.2 Macerator (Eingabemenge 1, außer angegeben)

| Eingabe | Ausgabe | Status |
|---|---|---|
| Eisenerz | 2 Eisenstaub | [IC2] (GT bestätigt) |
| Golderz | 2 Goldstaub | [IC2] |
| Kupfererz / Zinnerz | 2 Kupfer-/Zinnstaub | [IC2] (über OreDict) |
| Silbererz | 2 Silberstaub | [IC2] (falls vorhanden) |
| Kohle | Kohlestaub | [IC2] |
| Holzkohle | GT-Holzkohlestaub | **[GT+M]** |
| Kohleerz | 2 Kohlestaub | **[GT+]** |
| Lapiserz | 12 Lapis | **[GT+]** |
| Redstone-Erz | 10 Redstone | **[GT+]** |
| Diamant-/Smaragd-/Uranerz | GT-Staub | **[GT+M]** 2 GT-Staub (Uran: + Plutonium als Nebenprodukt nur in GT-Maschinen) |
| Eisen-, Gold-, Kupfer-, Zinn-, raff. Eisenbarren | 1 Staub (raff. Eisen → Eisenstaub) | [IC2] |
| Bronzebarren | Bronzestaub | **[GT+]** |
| Speicherblöcke Kupfer/Zinn/Bronze | 9 Staub | **[GT+]** |
| Wolle (jede Farbe) | Faden (IC2: 1) → **2 Faden** | **[GT~]** |
| Kies | Feuerstein | [IC2] |
| Stein | Bruchstein | [IC2] |
| Bruchstein | Sand | [IC2] |
| Sandstein | Sand | [IC2] |
| Eis | Schneeball | [IC2] |
| Schneeblock | 4 Schneebälle | **[GT+]** |
| Tonblock | 2 Tonstaub | [IC2] (GT bestätigt) |
| Glowstone | 4 Glowstonestaub | [IC2] |
| Knochen | 5 Knochenmehl | [IC2] |
| Pflanzenball | 8 Erde → **1 Erde** | **[GT~]** |
| Kaffeebohnen | 3 Kaffeepulver | [IC2] |
| Lohenrute | 5 Lohenstaub | [IC2] |
| Spinnenauge | 2 Grinpulver | [IC2] |
| Dichte Kupferplatte | 8 Kupferstaub | **[GT+]** |
| Maschinenblock | 8 Eisenstaub | **[GT+]** |
| Melone (Block) | 8 Melonenscheiben | **[GT+]** |
| Kürbis | 4 Kürbiskerne | **[GT+]** |
| Melonenscheibe | 1 Melonenkern | **[GT+]** |
| Schraubenschlüssel | 6 Bronzestaub | **[GT+]** |
| Kanister | 7 Zinnstaub | **[GT+]** |
| Vanilla-Eisenteile (Kompass, Taschenuhr, Schere, Lore, Eimer, Eisentür) | Eisen-/Goldstaub entsprechend | [GT+] |
| Steinknopf/Druckplatte Stein, Hebel | Sand | [GT+] |
| Stock, Crop-Stab, Holzgegenstände | GT-Holzstaub | **[GT+M]** |

### 10.3 Compressor

| Eingabe | Ausgabe | Status |
|---|---|---|
| Pflanzenball | kompr. Pflanzenball | [IC2] |
| 8 Weizen / 8 Zuckerrohr / 8 Kaktus / 8 Rosen / 8 Löwenzahn | kompr. Pflanzenball | **[GT+]** (Blumen: alle per Knochenmehl wachsenden Grasblumen) |
| Hydr. Kohlestaub | hydr. Kohleklumpen | [IC2] |
| 3 Netherrack | Netherziegel | [IC2] |
| Sand (1) | Sandstein | **[GT~]** → **4 Sand** |
| Schneeball | Eis | [IC2] |
| Wasserzelle | Schneeball | [IC2] |
| Mischmetallbarren | fortg. Legierung | [IC2] |
| Kohlenetz | Kohleplatte | [IC2] |
| Kohleball | kompr. Kohleball | [IC2] |
| Kohleklumpen | Diamant → **Industriediamant** | **[GT~]** |
| Bauschaum | Bauschaum-Pellet | [IC2] |
| Leere Zelle | Luftzelle | [IC2] |
| 8 Kupferbarren | dichte Kupferplatte | **[GT⚠]** entfernt (nur noch 8 GT-Kupferplatten → dichte Platte) |
| Uranerz / Uranbrocken | Uranbarren | [IC2] |
| 4 Ton (Klumpen) | Tonblock | **[GT+]** |
| 4 Glowstonestaub | Glowstone | **[GT+]** |
| 9 Barren (Zinn, Bronze, Uran; nicht Kupfer) | Speicherblock | **[GT+]** |
| Iridiumerz | GT-Iridiumbarren | **[GT+M]** (benötigt für die Iridiumplatte) |
| Bonus (Pumpe) | Schneeball aus Wasser unter Pumpe | [IC2] |

### 10.4 Extractor

| Eingabe | Ausgabe | Status |
|---|---|---|
| Gummisetzling | Gummi | [IC2] |
| Harz | 3 Gummi | [IC2] |
| Gummiholz (`woodRubber`) | Gummi | [IC2] |
| Biozelle / hydr. Kohlezelle | Bio-/Kohle-Brennstoffzelle | [IC2] |
| Wasserzelle | Hydrierzelle | [IC2] |
| Schleimball | 2 Gummi | **[GT+]** |
| Luftzelle | leere Zelle | **[GT+]** |
| Gefüllte Zinndose (Meta 0 / 1) | leere Zinndose | **[GT+]** |
| Wolle (jede Farbe) | weiße Wolle | **[GT+]** |
| Rose / Löwenzahn | 3 roter / 3 gelber Farbstoff | **[GT+]** |

### 10.5 Recycler / Massefabrikator / Schrottbox

- Recycler-Blacklist: IC2-Standard + Config; GT ergänzt Einträge (u. a. Mob-Grinder-Drops) – **keine Rezeptänderung**,
  siehe §11.
- Massefabrikator-Verstärker: Schrott 5000, Schrottbox 45 000 [IC2]; **[GT+]** Redstone 5000, Glowstonestaub 25 000
  (`GT_OreDictHandler` → `addIC2MatterAmplifier`); **[GT+M]** Nikolit- und Manganstaub je 5000.
- Schrottbox-Drops: GT **ersetzt** die IC2-Liste (Schrott 200 + GT-Liste), siehe `05-items.md` §1.1.

## 11. GregTech-Änderungen, die keine Rezepte sind (nicht automatisch übernommen)

| Änderung | Pack-Wert | Vorschlag |
|---|---|---|
| Upgrade-Module max. Stack | 4 (`UpgradeStacksize`) | offen |
| Schrottbox-Drops ersetzt | Schrott 200 + GT-Liste | **übernommen** (2026-10-15), GT-Items ab Phase 6b |
| Recycler-Blacklist erweitert | GT-Liste | offen |
| Stackgrößen diverser Vanilla-Blöcke | 64 | nicht übernehmen (Vanilla) |
| Miner: Lava als „wertvoll“ (`MinerListLavaAddition`) | an | offen |
| Erzwerte (valuableOres) | GT ergänzt | IC2-Werte gewinnen (erste Registrierung), siehe Weltgen |

## 12. BuildCraft-Rezepte (3.4.3)

| Ergebnis | Muster | Status |
|---|---|---|
| Holz-Zahnrad | ` S /S S/ S `, S = Stock | [BC] |
| Stein-Zahnrad | ` I /IGI/ I `, I = Bruchstein, G = Holz-Zahnrad | [BC] |
| Eisen-Zahnrad | Eisenbarren + Stein-Zahnrad | [BC] |
| Gold-Zahnrad | Goldbarren + Eisen-Zahnrad | [BC] |
| Diamant-Zahnrad | Diamant + Gold-Zahnrad | [BC] |
| BC-Schraubenschlüssel | `I I/ G / I `, G = Stein-Zahnrad | [BC] |
| Redstone-Engine | `www/ g /GpG`, w = Bretter, g = Glas, G = Holz-Zahnrad, p = Kolben | [BC] |
| Stirling-Engine | dito, w = Bruchstein, G = Stein-Zahnrad | [BC] |
| Combustion-Engine | dito, w = Eisenbarren, G = Eisen-Zahnrad | [BC] |
| Quarry | `ipi/gig/dDd`, i = Eisen-Zahnrad, p = Redstone, g = Gold-Zahnrad, d = Diamant-Zahnrad, D = Diamantspitzhacke | **[GT~]** ersetzt (`ExpensiveBCQuarryRecipe`): `ICI/GIG/DPD`, I = Eisen-Zahnrad, C = **fortg. Schaltkreis (IC2)**, G = Gold-Zahnrad, D = Diamant-Zahnrad, P = **Diamantbohrer (IC2, beliebige Ladung)** |
| Landmark | `l /r `, l = Lapis, r = Redstone-Fackel | [BC] |
| Öleimer/Treibstoffeimer | nur durch Befüllen | [BC] |
| Tank | `ggg/g g/ggg`, g = Glas | [BC] |
| Pumpe | `T/W`, T = Tank, W = Mining Well | [BC] |
| Raffinerie | `   /RTR/TGT`, R = Redstone-Fackel, T = Tank, G = Diamant-Zahnrad | [BC] |
| Mining Well | `ipi/igi/iPi`, i = Eisen, p = Redstone, g = Eisen-Zahnrad, P = Eisenspitzhacke | [BC] |

## 13. Forestry (Electrical Engine, Thermionic Fabricator, Carpenter)

Siehe `10-energieumwandlung.md` §A.5 und §A.6 (Engine, Zinn-Zahnrad, Robustes Gehäuse, Thermionic Fabricator, Carpenter, Röhren, Platinen, Lötkolben). GregTech ändert nichts.

## 14. Power Converters

Siehe `10-energieumwandlung.md` §B (Energy Bridge, Charger, BC- und IC2-Konverter). GregTech ändert nichts.

## 15. Compact Solars

Siehe `11-compact-solars.md` (LV/MV/HV: `SSS/SXS/SSS` aus 8 Solarpanels bzw. Arrays der Vorstufe + LV-/MV-/HV-Trafo; Solar-Hüte formlos Array + Eisenhelm). GregTech ändert nichts.

## 16. Umgang mit [GT⚠] / [GT−]

Im Pack waren diese IC2-Items nur mit **GregTech-Material (Platten, Stahl, Titan, Silizium, Beryllium, Platin …)
oder GT-Maschinen (Plattenbieger, Assembler, Canning Machine, Implosionskompressor)** herstellbar.

Betroffen: Mischmetallbarren, dichte Kupferplatte (Compressor), Iridiumplatte, Solarpanel, Windmühle, Wassermühle,
Bergbaubohrer, Diamantbohrer, Kettensäge, el. Hacke, el. Baumzapfer, el. Schraubenschlüssel, Bergbaulaser, Nanosäbel,
dicker Neutronenreflektor, Uranzelle (aus Barren), Bio-/Kohle-/Wasser-/Lavazellen per Crafting, Massefabrikator.

**Entscheidung (Nutzer):** Alle [GT⚠]/[GT−]-Fälle werden **wie im Pack** umgesetzt – also die finalen GT-Rezepte, kein
IC2-Original als Ersatz. Die benötigten GT-Materialien und ihr Herstellungsweg im Pack stehen in
`12-gregtech-materialien.md`; ob dafür GT-Maschinen nachgebaut werden, wird später besprochen.

Massefabrikator: **entschieden (2026-10-15)** – IC2-Rezept wird trotzdem umgesetzt (siehe
`12-gregtech-materialien.md` §3); verfügbar, sobald der fortg. Maschinenblock ein Rezept hat (Phase 6b).
