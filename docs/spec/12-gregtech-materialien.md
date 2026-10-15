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
| Iridiumplatte | Iridium-Legierungsbarren ← (Iridiumbarren + Diamantstaub + fortg. Legierung) | Werkbank/Walzmaschine `IAI/ADA/IAI` → Implosionskompressor (8 TNT) → Iridiumplatte + 4 dunkle Asche |
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
| Diamantstaub | Dust 36 | Macerator: Diamant / Diamanterz |
| Iridium-Legierungsbarren | Material 4 | `IAI/ADA/IAI` (I = Iridiumbarren, A = fortg. Legierung, D = Diamantstaub) |
| Iridiumplatte (IC2) | – | **Implosionskompressor** (Multiblock): Legierungsbarren + 8 TNT/ITNT |
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
Die von GT registrierten Verstärker gelten trotzdem (Redstone 5000, Glowstonestaub 25 000, Nikolit-/Manganstaub 5000).

## 4. Entscheidungen und offene Punkte

1. **Entschieden (Phase 3):** Herstellungsweg = GT-Maschinen 1:1 nachbauen (Phase 6b). Ohne GT-Erze erreichbar:
   Siliziumzelle aus 16 Sand, 8 Tonstaub oder 8 Feuersteinstaub (GT-Elektrolyseur, z. B. Sand: 1000 Ticks, 25 EU/t,
   2 leere Zellen → Siliziumzelle + Luftzelle); Aluminiumstaub aus 8 Tonstaub (Elektrolyseur → u. a. 2 Aluminiumstaub,
   2 Siliziumzellen); Hochofen: 2 Siliziumzellen → Siliziumplatte (1000 Ticks, 128 EU/t, 1500 K), Aluminiumstaub →
   Aluminiumbarren (200 Ticks, 128 EU/t, 1700 K); Plattenbieger → Platte; Assembler: Generator + 4 Aluminiumplatten →
   2 Wassermühlen, Generator + 4 Kohleplatten → Windmühle (je 6400 Ticks, 8 EU/t). GT-Maschinenrezepte brauchen u. a.
   IC2-Elektrolyseur, Magnetisierer, Induktionsofen, fortg. Maschinenblock, Kupfernickel-Heizspulen, Förderband-Modul.
2. GT-Erze (Bauxit, Sheldonit, Rubin, Iridium …) generieren?
3. ~~Massefabrikator vs. GT Matter Fabricator~~ – entschieden, siehe §3.
