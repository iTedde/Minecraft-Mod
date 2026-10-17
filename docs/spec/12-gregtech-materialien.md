# 12 – GregTech-Materialien für die Pack-Rezepte

Entscheidung des Nutzers: Die GregTech-Rezepte werden **wie im Pack** umgesetzt (finale GT-Varianten, siehe die
[GT⚠]-Einträge in `08-rezepte.md`). Dafür werden GT-Materialien benötigt. **Wie** sie hergestellt werden (GT-Maschinen
nachbauen oder einfacherer Weg), ist noch offen und wird später besprochen. Diese Datei listet den Bedarf und den
Herstellungsweg im Pack als Grundlage.

Quelle: `reference/decompiled/gregtech/` (`GT_Mod.java`, `common/GT_OreDictHandler.java`). GT-Maschinenwerte
(`addXRecipe(…, Dauer, EU/t[, Hitze])`) sind dort vollständig hinterlegt.

## 1. Bedarf je Rezept

| IC2-/BC-Item | Benötigtes GT-Material | Rezept im Pack |
|---|---|---|
| Mischmetallbarren | Platten: raff. Eisen (oder Nickel/Invar/Stahl/Titan/Wolfram/Wolframstahl) + Bronze (oder Messing) + Zinn (oder Zink/Aluminium) | Werkbank senkrecht `X/Y/Z` → 1–6 Barren je nach Metallen |
| Dichte Kupferplatte | 8 Kupferplatten | Compressor |
| Iridiumplatte | Iridium-Legierungsbarren ← (Iridiumbarren + Diamantstaub + fortg. Legierung) | Werkbank/Walzmaschine `IAI/ADA/IAI` → Implosionskompressor (8 Industrie-TNT) → Iridiumplatte + 4 dunkle Asche (`13-gregtech-maschinen.md` §8) |
| Solarpanel | Siliziumplatte | `GGG/SPS/CHC` |
| Windmühle | – (Kohleplatten = IC2) | Assembler: Generator + 4 Kohleplatten |
| Wassermühle (×2) | 4 Aluminiumplatten | Assembler: Generator + 4 Aluminiumplatten |
| Bergbaubohrer, Kettensäge, el. Hacke, el. Baumzapfer, el. Schraubenschlüssel | Stahlplatten (`ElectricSteelTools=true`) | Werkbank |
| Diamantbohrer | 2 Titanplatten | Werkbank |
| Bergbaulaser | Rubin, 2 Titanplatten, Helium-Kühlzelle (GT) | Werkbank |
| Nanosäbel | 2 Platinplatten, Datenkontrollschaltungen (`craftingCircuitTier07`, GT) | Werkbank |
| Dicker Neutronenreflektor | Berylliumzelle | Werkbank |
| Uranzelle (aus Barren), Bio-/Kohle-/Wasser-/Lavazelle | – | GT-Canning-Machine |
| Quarry (BC) | – (fortg. Schaltkreis, Diamantbohrer) | Werkbank – bereits übernehmbar |

## 2. Herstellung im Pack (GT-Maschinen)

| Material | GT-Item | Weg im Pack |
|---|---|---|
| Platten (Eisen, raff. Eisen, Kupfer, Zinn, Bronze, Gold, Aluminium, Stahl, Titan, Platin …) | `GT_MetaItem_Material` 64–84 | **Plattenbieger** (Bender): 1 Barren → 1 Platte, 50 Ticks, 20 EU/t |
| Stahlbarren | Material 26 | **Hochofen** (Multiblock, Heizspulen): raff. Eisenbarren + 2 Kohlestaub → Stahl + 2 dunkle Asche (oder Stahlstaub, 100 Ticks), 500 Ticks, 128 EU/t, 1000 K |
| Titanbarren | Material 19 | Hochofen: Titanstaub → Titanbarren, 1000 Ticks, 128 EU/t, 1500 K; Titanstaub aus Bauxit (Elektrolyseur/Zentrifuge), Erz nur GT-Weltgen |
| Aluminiumbarren | Material 18 | Hochofen: Aluminiumstaub, 200 Ticks, 128 EU/t, 1700 K; Staub aus Bauxit |
| Siliziumplatte | Material 36 | Hochofen: 2 Siliziumzellen → 1 Siliziumplatte + 2 leere Zellen, 1000 Ticks, 128 EU/t, 1500 K; Silizium aus Sand/Feuerstein (Elektrolyseur/Zentrifuge) |
| Platinbarren | Material 27 | aus Sheldonit/Cooperit (GT-Erz, Industrial Grinder) bzw. UU-Materie (Platinstaub) |
| Iridiumbarren | Material 16 | Compressor: IC2-Iridiumerz → Iridiumbarren |
| Diamantstaub | Dust 36 | Macerator: Diamant / Industriediamant |
| Iridium-Legierungsbarren | Material 4 | `IAI/ADA/IAI` (I = Iridiumbarren, A = fortg. Legierung, D = Diamantstaub) |
| Iridiumplatte (IC2) | – | **Implosionskompressor** (Multiblock): Legierungsbarren + 8 Industrie-TNT |
| Berylliumzelle | Cell 10 | Elektrolyseur/Zentrifuge (Smaragdstaub, Enderperlenstaub) |
| Heliumzelle / Helium-Kühlzelle | Cell 3 / GT-Item 34/36 | Zentrifuge (u. a. Lava, Glowstone); Kühlzelle aus Heliumzellen + Zinn |
| Rubin | Material 32 | GT-Erz (Wüsten), UU-Materie |
| Datenkontrollschaltung | GT-Component | Assembler aus fortg. Schaltkreisen + GT-Teilen |

Benötigte GT-Maschinen für den vollständigen Pack-Weg: Plattenbieger, Hochofen (+ Heizspulen), Implosionskompressor,
Assembler, Canning Machine, Elektrolyseur, Zentrifuge, Industrial Grinder sowie GT-Erze (Bauxit, Sheldonit, Rubin …).

## 3. Sonderfall Massefabrikator

Im Pack ist das IC2-Rezept entfernt (`ic2recipes.Massfabricator=false`); stattdessen gab es GregTechs **Matter
Fabricator** (`MatterFabricationRate=16666666`). **Entschieden (2026-10-15):** IC2-Massefabrikator 1:1
(`04-maschinen.md`) mit dem IC2-Originalrezept (`08-rezepte.md`); der GT Matter Fabricator wird nicht nachgebaut.
Die von GT registrierten Verstärker gelten trotzdem (Redstone 5000, Glowstonestaub 25 000, Nikolit-/Manganstaub 5000,
Diamantstaub 125 000, Smaragdstaub 50 000 – die beiden Staube seit 6b.5b).

## 4. Entscheidungen und offene Punkte

1. **Entschieden (Phase 3):** Herstellungsweg = GT-Maschinen 1:1 nachbauen (Phase 6b). Ohne GT-Erze erreichbar:
   Siliziumzelle aus 16 Sand, 8 Tonstaub oder 8 Feuersteinstaub (GT-Elektrolyseur, z. B. Sand: 1000 Ticks, 25 EU/t,
   2 leere Zellen → Siliziumzelle + Luftzelle); Aluminiumstaub aus 8 Tonstaub (Elektrolyseur → u. a. 2 Aluminiumstaub,
   2 Siliziumzellen); Hochofen: 2 Siliziumzellen → Siliziumplatte (1000 Ticks, 128 EU/t, 1500 K), Aluminiumstaub →
   Aluminiumbarren (200 Ticks, 128 EU/t, 1700 K); Plattenbieger → Platte; Assembler: Generator + 4 Aluminiumplatten →
   2 Wassermühlen, Generator + 4 Kohleplatten → Windmühle (je 6400 Ticks, 8 EU/t). GT-Maschinenrezepte brauchen u. a.
   IC2-Elektrolyseur, Magnetisierer, Induktionsofen, fortg. Maschinenblock, Kupfernickel-Heizspulen, Förderband-Modul.
2. ~~GT-Erze generieren?~~ **Entschieden (2026-10-16):** alle GT-Erze wie im Pack (Phase 8); Spezifikation
   `07-weltgenerierung.md` §6.
3. ~~Massefabrikator vs. GT Matter Fabricator~~ – entschieden, siehe §3.

## 5. Phase 8e: Freischaltungen durch die GT-Erze (Spezifikation 2026-10-16)

Quelle: `GT_Mod.java` (Item-Tabellen Z. 720–1000, Rezepte Z. 2270–2610, 4020–4100, 4380–4450, 4590–4610,
Schrottbox Z. 2110–2160), `common/GT_OreDictHandler.java` (Staub-/Barren-/Platten-Handler), Pack-Config
`config/GregTech/*.cfg`. Maschinenwerte wie `13-gregtech-maschinen.md`; „Zellen“ = leere Zellen als Eingabe 2.
Pack-Config: `basicmetalblastfurnacerequirement` Titan/Chrom = true → **kein** Ofenrezept für Titan-/Chromstaub;
`TitaniumDiamondDrill`, `ExpensiveLaser`, `Helium2Coolantcell`, `Ruby2Energycrystal`, `Sapphire2Lapotron` = true.
Railcraft = false → Walzmaschinen-Rezepte als Werkbankrezept (wie Kupfernickel-Spule, 6b.3).

### 5.1 Neue Items

| Item | GT-Meta (Textur) | OreDict |
|---|---|---|
| Silberbarren, Silberplatte | Material 17, 69 (`materials.png`) | `ingotSilver`, `plateSilver` |
| Zinkbarren, Zinkplatte | Material 24, 82 | `ingotZinc`, `plateZinc` |
| Titanbarren, Titanplatte | Material 19, 77 | `ingotTitanium`, `plateTitanium` |
| Chrombarren, Chromplatte | Material 20, 76 | `ingotChrome`, `plateChrome` |
| Platinplatte | Material 79 | `platePlatinum` |
| Magnaliumplatte | Material 13 | `plateMagnalium` |
| Magnesium-, Mangan-, Titan-, Chromstaub | Dust 13, 12, 19, 20 (`dusts.png`) | `dust…` |
| kl. Titan-, kl. Chrom-, kl. Magnesiumstaub | SmallDust 19, 20, 13 (`dustssmall.png`) | `dustSmall…` |
| Pyrop-, Almandin-, Spessartin-, Andradit-, Grossular-, Uwarowitstaub | Dust 56–61 | `dustPyrope` … |
| Datenspeicher-Schaltung („Data Storage Circuit“) | Component 3 (`components.png`) | `craftingCircuitTier05` |
| Datenkontroll-Schaltung („Data Control Circuit“) | Component 1 | `eliteCircuit` = `craftingCircuitTier06` |
| Energiefluss-Schaltung („Energy Flow Circuit“) | Component 0 | `craftingCircuitTier07` |
| Elite-Platine („Elite Circuit Board“) | Component 50 | `craftingCircuitBoardTier06` |
| Titan-Maschinenhülle („Titanium Machine Hull“) | Component 36 | `craftingRawMachineTier03` (im Pack ohne Verwendung) |
| Hochentwickelter Maschinenblock („Highly Advanced Machineblock“) | `BlockMetaID_Block` 10: Härte 10, Explosionsresistenz 200, Textur `blocks.png` Index Meta + Seite · 16 | `craftingRawMachineTier04`; **kein** Maschinengehäuse (löst keine Strukturprüfung aus) |

Kleine Stäube jeweils 4 ↔ 1 an der Werkbank (`dustSmall`-Handler); bei Mangan jetzt auch 4 kl. → 1 Staub.

### 5.2 Herstellung und Verarbeitung

| Eingabe | Maschine | Ausgabe | Ticks / EU/t / Hitze |
|---|---|---|---|
| 2 Galenastaub | Hochofen | Silberbarren, Bleibarren | 20 / 128 / 1500 K |
| Silberstaub | Ofen | Silberbarren | – |
| Silberbarren | Macerator | Silberstaub | – |
| Zinkstaub | Ofen | Zinkbarren | – |
| Zinkbarren | Macerator | Zinkstaub | – |
| 9 Rubinstaub, 3 Zellen | Elektrolyseur | 2 Aluminium-, 1 Chromstaub, 3 Luftzellen | 500 / 50 |
| 9 Olivinstaub, 3 Zellen | Elektrolyseur | 2 Magnesium-, 2 Eisenstaub, Siliziumzelle, 2 Luftzellen | 600 / 60 |
| 12 Bauxitstaub, 8 Zellen | Elektrolyseur | 8 Aluminiumstaub, 2 kl. Titanstaub, 5 Wasserstoff-, 3 Luftzellen | 2000 / 128 |
| 16 roter Granatstaub | Zentrifuge | 3 Pyrop-, 5 Almandin-, 8 Spessartinstaub | 3000 |
| 16 gelber Granatstaub | Zentrifuge | 5 Andradit-, 8 Grossular-, 3 Uwarowitstaub | 3500 |
| 20 Pyropstaub, 9 Zellen | Elektrolyseur | 3 Magnesium-, 2 Aluminiumstaub, 3 Siliziumzellen, 6 Luftzellen | 1790 / 50 |
| 20 Almandinstaub, 9 Zellen | Elektrolyseur | 3 Eisen-, 2 Aluminiumstaub, 3 Siliziumzellen, 6 Luftzellen | 1640 / 50 |
| 20 Spessartinstaub, 9 Zellen | Elektrolyseur | 2 Aluminium-, 3 Manganstaub, 3 Siliziumzellen, 6 Luftzellen | 1810 / 50 |
| 20 Andraditstaub, 12 Zellen | Elektrolyseur | 3 Calciumzellen, 2 Eisenstaub, 3 Siliziumzellen, 6 Luftzellen | 1280 / 50 |
| 20 Grossularstaub, 12 Zellen | Elektrolyseur | 3 Calciumzellen, 2 Aluminiumstaub, 3 Siliziumzellen, 6 Luftzellen | 2050 / 50 |
| 20 Uwarowitstaub, 12 Zellen | Elektrolyseur | 3 Calciumzellen, 2 Chromstaub, 3 Siliziumzellen, 6 Luftzellen | 2200 / 50 |
| Titanstaub | Hochofen | Titanbarren | 1000 / 128 / 1500 K |
| Chromstaub | Hochofen | Chrombarren | 800 / 128 / 1700 K |
| Titan-, Chrombarren | Macerator | 1 Staub | – |
| Silber-, Zink-, Titan-, Chrom-, Platinbarren | Plattenbieger | 1 Platte | 50 / 20 |
| UU-Materie `UUU/ U / U ` (fest) | Werkbank | 2 Titanstaub | – |
| UU-Materie `   /U U/U  ` (fest) | Werkbank | 10 Zinkstaub | – |
| 8 Olivin bzw. 8 Olivinstaub + fortg. Schaltkreis | Assembler | 4 Datenspeicher-Schaltungen | 6400 / 8 |
| 8 Smaragd + fortg. Schaltkreis | Assembler | 4 Datenspeicher-Schaltungen | 3200 / 4 |
| 8 Smaragdstaub + fortg. Schaltkreis | Assembler | 4 Datenspeicher-Schaltungen | 6400 / 8 |
| Platinplatte + fortg. Schaltkreis | Assembler | Elite-Platine | 3200 / 4 |
| Elite-Platine + Datenspeicher-Schaltung | Assembler | Datenkontroll-Schaltung | 3200 / 4 |
| Elite-Platine + Lapotronkristall (beliebige Ladung) | Assembler | Energiefluss-Schaltung | 3200 / 4 |
| `AEA/EIE/AEA`, A = fortg. Schaltkreis, E = Datenspeicher-Schaltung, I = Iridiumplatte | Werkbank | 4 Datenkontroll-Schaltungen | – |
| 8 Titanplatten + Maschinenteile | Assembler | Titan-Maschinenhülle | 400 / 8 |
| `AAA/MMM/AAA`, A = Aluminiumplatte, M = Magnesiumstaub | Werkbank (Walzmaschine) | 3 Magnaliumplatten | – |
| `CTC/TMT/CTC`, C = Chromplatte, T = Titanplatte, M = Stahl-Maschinenhülle (`craftingRawMachineTier02`) | Werkbank | Hochentwickelter Maschinenblock | – |

Magnesium-/Manganbarren gibt es in GT 2.90 nicht (Handler nur für Fremd-Mods) → entfallen.
Massefabrikator-Verstärker: Manganstaub 5000; nachgetragen (fehlte seit 8c) Wolframstaub 50 000.
Schrottbox (GT-Liste): Chrom-, Titan-, Magnesiumstaub je 0,03; Elite-Platine 0,2 (mit 8e.3); nachgetragen Netherrackstaub 4,0
(fehlte seit 8d.2). Zinkstaub 0,5 war schon da.
UU-Materie (GT, `uumrecipe` im Pack alle true, feste Position): 2 Titanstaub `UUU/ U / U `, 10 Zinkstaub `   /U U/U  `;
nachgetragen (fehlten seit 8c): 2 Rubine ` UU/UUU/UU `, 2 Saphire `UU /UUU/ UU`, 2 grüne Saphire ` UU/UUU/ UU`, 2 Olivine
`UU /UUU/UU `, 1 Platinstaub `  U/UUU/UUU`, 6 Wolframstaub `U  /UUU/UUU`.

### 5.3 Freigeschaltete Rezepte bestehender Items

| Item | Rezept (Pack) |
|---|---|
| Diamantbohrer | ` D /DMD/TAT`, D = Diamant, M = Bergbaubohrer, T = Titanplatte, A = fortg. Schaltkreis → damit ist auch die Quarry herstellbar |
| Bergbaulaser | `RHE/TTC/ AA`, R = Rubin, H = 60k-Helium-Kühlzelle, E = Energiekristall (`crafting100kEUStore`), T = Titanplatte, C = fortg. Schaltkreis, A = fortg. Legierung |
| Nanosäbel | `PI /PI /CLC`, P = Platinplatte, I = Iridiumplatte, L = Lapotronkristall, C = **Energiefluss-Schaltung** (`craftingCircuitTier07`; Korrektur zu `08-rezepte.md`, dort stand „Datenkontrollschaltung“) |
| Kanthal-Heizspule | `AAA/BCC/BBC` → 3, A = raff. Eisen-, B = Chrom-, C = Aluminiumbarren (Walzmaschine) |
| Nichrom-Heizspule | ` B /BAB/ B `, A = Chrom-, B = Nickelbarren (Walzmaschine) |
| Fortgeschrittenes Maschinengehäuse (GT, 4 Stück) | `TTT/CMC/TTT`, T = Chromplatte, C = Datenkontroll-Schaltung, M = Hochentwickelter Maschinenblock |
| Mischmetallbarren | senkrecht `X/Y/Z`: Titanplatte + Bronze + Zinn/Zink/Aluminium → 3/3/4; Zink als Z bei raff. Eisen (1), Nickel (1), Stahl (2) |
| Kolben | `WWW/CBC/CRC` (Holz, Bruchstein, Redstone) mit Aluminium-, Stahl- oder Titanbarren als B (Aluminium/Stahl fehlten bisher) |
| Energiekristall | `DDD/DRD/DDD`, D = Redstone, R = Rubin |
| Lapotronkristall | `LCL/LSL/LCL`, L = Lapis/Lazuritstaub (`itemLazurite`), C = el. Schaltkreis, S = Saphir |
| Glasfaserkabel | `GGG/XDX/GGG` → 6 mit X = Silberbarren (Glas, Diamant) |
| Windmühle | Assembler: Generator + 2 Magnaliumplatten → Windmühle, 6400 / 8 |

### 5.4 Nicht im Umfang 8e

- **Wolfram-Kette – entschieden (Nutzer, 2026-10-16): Teilpaket 8e.4 zusammen mit dem Vakuumgefrierer**, spezifiziert
  in §5.6 bzw. `13-gregtech-maschinen.md` §11.
- **Platinen 48/49/24** (Basis-/fortg. Platine, fortg. Schaltkreisteile): brauchen Elektrumplatten; Elektrum gibt es
  nur über die Legierungsschmelze (nicht im Umfang) → entfallen, ebenso ihre Schrottbox-Drops.
  **Korrektur (13.3, 2026-10-17):** falsch – GT hat die unbedingten Werkbankrezepte Silber- + Goldstaub → 2
  Elektrumstaub und 2 kl. Silber- + 2 kl. Goldstaub → 1 (`GT_Mod` 3951–3953), Elektrumstaub wird im Ofen zum Barren;
  ebenso 3 Kupfer- + 1 Zinkstaub → 4 Messingstaub (3955). Elektrumstaub ist seit 13.3 umgesetzt; Elektrumplatte,
  Platinen 48/49/24 und Messing seit Phase 14 umgesetzt (§5.7).
- GT-eigene Items (Jackhammer, Cloaking Device, Lapotronic Energy Orb, Datenkugel, Sonictron, Fusion,
  Railcraft-Teile) und Nuggets/Speicherblöcke (bisher nie umgesetzt, außer Iridiumnugget) → nicht im Umfang.

### 5.5 Teilpakete (Vorschlag)

- **8e.1 Materialien:** alle Items aus §5.1 außer Schaltungen/Platine/Hülle/Block, Rezepte §5.2 bis „UU-Materie“,
  Verstärker, Schrottbox.
- **8e.2 Freigeschaltete Rezepte:** §5.3 ohne Gehäuse (Diamantbohrer, Bergbaulaser, Spulen, Mischmetall, Kolben,
  Energie-/Lapotronkristall, Glasfaser, Windmühle).
- **8e.3 Schaltungen und Gehäuse:** Datenspeicher-/Datenkontroll-/Energiefluss-Schaltung, Elite-Platine,
  Titan-Maschinenhülle, Hochentwickelter Maschinenblock, fortg. Maschinengehäuse, Nanosäbel.
- **8e.4 Wolfram-Kette + Vakuumgefrierer:** §5.6 und `13-gregtech-maschinen.md` §11.

### 5.6 Phase 8e.4: Wolfram-Kette (Spezifikation 2026-10-16)

Quelle: `GT_Mod.java` Z. 730–824 (Items), 1720–1730 (Gefrierer-Rezepte), 2338–2371 (Mischmetall), 2443 (Energiefluss),
2579 (Schleifkopf), 4263–4292 (Macerator); `GT_OreDictHandler.java` Z. 2160–2180 (`dustTungsten`), 2357–2361
(`ingot*`: Bender, Compressor → Block), 2635–2665 (`ingotTungsten`/`ingotSteel`), 3014–3087 (`block*`). Pack-Config
`RecipesAdvancedConfig.cfg` (Abschnitte `blastfurnace`, `bender`, `vacuumfreezer`, `storageblock*`),
`GregTech.cfg` `basicmetalblastfurnacerequirement` Tungsten = true.

**Neue Items/Blöcke:**

| Item | GT-Meta (Textur) | OreDict / Tag |
|---|---|---|
| Wolframbarren („Tungsten Ingot“, Tooltip „W“) | Material 22 (`materials.png`) | `ingotTungsten` |
| Wolframplatte („Tungsten Plate“, „W“) | Material 80 | `plateTungsten` |
| Heißer Wolframstahlbarren („Hot Tungstensteel Ingot“, ohne Tooltip) | Material 5 | – (kein OreDict) |
| Wolframstahlbarren („Tungstensteel Ingot“, „Vacuum Hardened“) | Material 6 | `ingotTungstenSteel` |
| Wolframstahlplatte („Tungstensteel Plate“, „Vacuum Hardened“) | Material 83 | `plateTungstenSteel` |
| Wolfram-Schleifkopf („Wolframium Grinder“, „Regular Grinding Head“) | Komponente 23 (`components.png`) | `craftingGrinder` |
| Eiszelle („Ice Cell“, „H2O“) | GT-Zelle 23 (`cells.png`) | – |
| Stahlblock („Block of Steel“) – **Nutzerentscheidung 2026-10-16** (nur für den Schleifkopf) | `BlockMetaID_Block` 11: Härte 3, Explosionsresistenz 100, Textur `blocks.png` Index 11 (alle Seiten), Eisenspitzhacke, keine Mob-Spawns, **kein** Maschinengehäuse | `blockSteel` |

Der heiße Barren ist ein gewöhnliches Item (in GT 2.90 kein Schaden, keine Abkühlung mit der Zeit).

**Rezepte:**

| Eingabe | Maschine | Ausgabe | Ticks / EU/t / Hitze |
|---|---|---|---|
| Wolframstaub | Hochofen | Wolframbarren | 2000 / 128 / 2500 K |
| Wolframbarren + Stahlbarren (beliebige Reihenfolge, je 1) | Hochofen | 2 heiße Wolframstahlbarren, 4 dunkle Asche | 2000 / 128 / 3000 K |
| Heißer Wolframstahlbarren | Vakuumgefrierer | Wolframstahlbarren | 450 / 128 |
| Wolframbarren | Macerator | Wolframstaub | – |
| Wolframbarren, Wolframstahlbarren | Plattenbieger | 1 Platte | 50 / 20 |
| Wolframplatte | Macerator / Ofen | Wolframstaub / Wolframbarren | – (Platten-Nebenrezepte wie `13-…` §3) |
| Wolframstahlplatte | Ofen | Wolframstahlbarren | – (kein Wolframstahlstaub) |
| 9 Stahlbarren | Compressor | Stahlblock | – (`storageblockcompressing` true) |
| Stahlblock | Ofen / Macerator | 9 Stahlbarren / 9 Stahlstaub | – (`furnace`/`storageblockmacerating` true; Werkbank-Ver- und -Entpacken im Pack **aus**) |
| `TST/SBS/TST`, T = Wolframplatte, S = Stahlplatte, B = Stahlblock | Werkbank | 2 Wolfram-Schleifkopf | – |
| `AWA/LIL/AWA`, A = fortg. Schaltkreis, W = Wolframplatte, L = Lapotronkristall (`crafting1kkEUStore`, beliebige Ladung), I = Iridiumplatte | Werkbank | 4 Energiefluss-Schaltungen | – |
| Mischmetall senkrecht `X/Y/Z`: X = Wolframplatte, Y = Bronzeplatte, Z = Zinn-/Zink-/Aluminiumplatte | Werkbank | 3 / 3 / 4 Mischmetallbarren | – |
| Mischmetall: X = Wolframstahlplatte, Y = Bronzeplatte, Z = Zinn-/Zink-/Aluminiumplatte | Werkbank | 5 / 5 / 6 Mischmetallbarren | – |
| Wasserzelle | Vakuumgefrierer | Eiszelle | 50 / 128 |
| Eiszelle | Zentrifuge | Eis + leere Zelle | 40 |

Kein Ofenrezept für Wolframstaub/-erz → Barren (`basicmetalblastfurnacerequirement` Tungsten = true). Schleifkopf-
Verwendungen: Industrial Grinder (`EAP/GGG/ABA`, G = **beliebiger** Schleifkopf, Tag `craft:crafting_grinder` =
Diamant- + Wolfram-Schleifkopf) und – **nachgetragen**, fehlte seit 8d.2 – Macerator-Variante `FDF/AMA/FAF`
(F = Feuerstein, D = Schleifkopf, A = el. Schaltkreis `craftingCircuitTier02`, M = Maschinenblock), zusätzlich zur
Diamant-Variante (`08-rezepte.md`).

**Entfällt:** ~~Messing-Varianten des Mischmetalls~~ (seit Phase 14 umgesetzt, §5.7);
Wolframiumzelle (GT-Zelle 4, Canner Wolframstaub + Zelle, nur für den Fusionsreaktor); **Supraleiter** (Komponente 2,
`CCC/WIW/LLL`; alle Verwendungen – Fusion, Plasmagenerator, Lapotronic Orb … – nicht im Umfang; Nutzerentscheidung
2026-10-16); Plasmagenerator (Wolframstahlplatten); Wolframstahl-Wände (`BlockMetaID_Block2` 8/9, brauchen IC2-Stahlbeton
bzw. Iridium-verstärkten Stein, nicht vorhanden); Wolframblock/-nugget; Railcraft-Teile.

### 5.7 Phase 14: Elektrum-Platinen, Messing, Mörser (Spezifikation 2026-10-17)

Korrigiert §5.4 und §5.6 „Entfällt: Messing-Varianten“: Elektrum- und Messingstaub sind über die unbedingten
Werkbankrezepte erreichbar (`GT_Mod` 3951–3957). Quellen: `GT_Mod.java` 733/740/744/767/777/827/978/989/990 (Items),
1005–1006 (Mörser), 1675 (Weizen), 1769 (fortg. Schaltkreis), 2120–2141 (Schrottbox), 2279–2371 (Mischmetall),
3923–3945 (Mörser-Rezepte), 3955–3957 (Messingstaub); `GT_OreDictHandler.java` 1638/3433 (Schaltkreisteile),
1703 (`dustBrass`), 2281 (`dustWheat`), 2696 (`ingotBrass`), 3174–3262 (`plate*`), 3600–3615 (`copperWire`).

**Neue Items:**

| Item | GT (Textur) | Tooltip | OreDict / Tag |
|---|---|---|---|
| Elektrumplatte („Electrum Plate“) | Material 71 | AgAu | `plateElectrum` / `c:plates/electrum` |
| Basis-Platine („Basic Circuit Board“, ID `gt_basic_circuit_board`, Forestry hat `basic_circuit_board`) | Komponente 48 | „Just a simple Circuit Plate“ | `craftingCircuitBoardTier02` |
| Fortg. Platine („Advanced Circuit Board“, ID `gt_advanced_circuit_board`) | Komponente 49 | „Standard Circuit Plate“ | `craftingCircuitBoardTier04` |
| Fortg. Schaltkreisteile („Advanced Circuit Parts“) | Komponente 24 | „Part of advanced Circuitry“ | `craftingCircuitPartsTier04` |
| Messingstaub / -barren / -platte („Brass Dust/Ingot/Plate“) | Dust 25 / Material 25 / 81 | ZnCu3 | `c:dusts/brass`, `c:ingots/brass`, `c:plates/brass` |
| Mehl („Flour“) | Material 14 | – | `dustWheat` |
| Eisenmörser („Iron Mortar“) | `items.png` 30 | „Can pulverize Ingots to Dust“ | – |
| Feuersteinmörser („Flint Mortar“) | `items.png` 31 | – | – |

Eisenmörser (`GT_Mortar_Item`): Stapel 1, max. Schaden 63, nicht reparierbar/verzauberbar; bleibt beim Craften im
Raster mit Schaden +1, bei Schaden 63 wird er stattdessen zu Eisenstaub (64 Benutzungen). Feuersteinmörser
(`GT_Generic_Item`): Stapel 64, wird verbraucht. Elektrumbarren bekommt den Tag `c:ingots/electrum`.

**Rezepte:**

| Eingabe | Maschine | Ausgabe | Ticks / EU/t |
|---|---|---|---|
| Elektrumbarren | Plattenbieger | Elektrumplatte | 50 / 20 (+ Platten-Nebenrezepte Ofen/Macerator) |
| raff. Eisen- **oder** Aluminiumplatte + 2 Elektrumplatten | Assembler | 2 Basis-Platinen | 800 / 1 |
| el. Schaltkreis + 2 Elektrumplatten | Assembler | 1 fortg. Platine | 1600 / 2 |
| Siliziumplatte + 4 Elektrumplatten | Assembler | 2 fortg. Platinen | 1600 / 2 |
| Lazurit (`itemLazurite`: Lapis, Lazuritstaub) + Glowstonestaub | Assembler | 2 fortg. Schaltkreisteile | 800 / 2 |
| fortg. Platine + 2 fortg. Schaltkreisteile | Assembler | fortg. Schaltkreis | 1600 / 2 |
| Basis-Platine + 3 isol. Kupferkabel (`copperWire`) | Assembler | el. Schaltkreis | 800 / 1 |
| el. Schaltkreis + isol. Kupferkabel | Assembler | Frequenzsender (nachgetragen, gleicher Handler) | 800 / 1 |
| 3 Kupfer- + 1 Zinkstaub (formlos) | Werkbank | 4 Messingstaub | – |
| 3 kl. Kupfer- + 1 kl. Zinkstaub (formlos) | Werkbank | 1 Messingstaub | – |
| Messingstaub | Ofen | Messingbarren | – (`furnace` dustBrass true) |
| Messingbarren | Macerator / Plattenbieger | Messingstaub / Messingplatte (50 / 20) | – |
| Messingstaub | Zentrifuge | 3 kl. Kupfer-, 1 kl. Zinkstaub | 1500 (ForestryBronzeNerf: 1 Staub) |
| Mischmetall `X/Y/Z` mit Messingplatte als Y | Werkbank | wie mit Bronze (`13-…` §3) | – |
| Schüssel + Feuerstein (formlos) | Werkbank | Feuersteinmörser | – |
| ` R /SRS/SSS`, R = raff. Eisenbarren, S = Steinziegel (nur Meta 0) | Werkbank | Eisenmörser | – |
| Mörser + Kohle / Goldbarren / Tonblock / Kupfer- / Zinn- / Silber- / Elektrumbarren / Weizen | Werkbank | **hydr. Kohlestaub** (GT-Eigenheit: `getIC2Item("hydratedCoalDust")`) / Gold- / 1 Ton- / Kupfer- / Zinn- / Silber- / Elektrumstaub / Mehl | beide Mörser |
| Eisenmörser + Bronze- / Messing- / Eisen- / raff. Eisenbarren / Kies | Werkbank | Bronze- / Messing- / Eisen- / Eisenstaub / Feuerstein | nur Eisenmörser |
| Weizen | Macerator | Mehl | – |
| Mehl | Ofen | Brot | – |

Schrottbox: fortg. Schaltkreisteile 1,2, Basis-Platine 1,8, fortg. Platine 0,4, Messingstaub 0,5.

**Entfällt:** Messing-Maschinenhülle (Komponente 34, „Cheap Machine Block“; nur für GT-Automatisierungsblöcke, nicht im
Umfang), Messing-/Elektrumblock und -nuggets (Speicherblöcke/Nuggets nie im Umfang), Legierungsschmelze und
TE-Induktionsschmelze (3 Kupfer + Zink → 4 Messing), alle Elektrumplatten-Verwendungen in GT-Automatisierungsblöcken.
