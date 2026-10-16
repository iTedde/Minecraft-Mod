# Spezifikation – Übersicht

Versionen der Vorlage (FTB Ultimate 1.1.2, Minecraft 1.4.7, Forge 6.6.2.534):

| Mod | Version | Dekompiliert unter |
|---|---|---|
| IndustrialCraft 2 | 1.115.231-lf | `reference/decompiled/ic2/` |
| GregTech | 2.90 (MC146_V2.90) | `reference/decompiled/gregtech/` |
| BuildCraft | 3.4.3 | `reference/decompiled/buildcraft/` |
| Forestry | 2.0.0.11 | `reference/decompiled/forestry/` |
| Power Converters (+ PowerCrystalsCore) | Pack-Version | `reference/decompiled/PowerConverters/`, `…/PowerCrystalsCore/` |
| Compact Solars | 4.0 (nicht im Pack-Download; Quellcode von GitHub) | `reference/source/compactsolars/v4.0/` |

Der Code wurde vor dem Dekompilieren mit den MCP-Mappings von Forge 1.4.7 (`tools/mcp/mcp.srg`) remappt, d. h.
Vanilla-Klassen/-Felder tragen lesbare Namen (`Block.cobblestone`, `Item.redstone` …).

| Datei | Inhalt |
|---|---|
| `01-eu-system.md` | Spannungsstufen, Energienetz-Algorithmus, Kabel, Überspannung, Explosionen, elektrische Items |
| `02-generatoren.md` | Generator, Geothermie, Solar, Wasser, Wind (Reaktor → 06) |
| `03-speicher-trafos.md` | BatBox, MFE, MFSU, Trafos |
| `04-maschinen.md` | alle Maschinen inkl. Upgrades, Miner, Pumpe, Teleporter, Terraformer, Personal-Blöcke |
| `05-items.md` | Materialien, Batterien, Werkzeuge, Rüstung, Sprengstoffe, Crops, Blockwerte |
| `06-reaktor.md` | Reaktor, Komponenten, Hitze- und Energieformeln, Explosion |
| `07-weltgenerierung.md` | Erze, Gummibäume, Öl, Erzwerte |
| `08-rezepte.md` | alle Rezepte mit GregTech-Markierung |
| `09-buildcraft.md` | MJ, Engines, Landmarks, Quarry, Pumpe, Tank, Raffinerie, Mining Well |
| `10-energieumwandlung.md` | Forestry Electrical Engine, Thermionic Fabricator, Carpenter, Power Converters |
| `11-compact-solars.md` | Compact Solars (Arrays, Solar-Hüte) |
| `12-gregtech-materialien.md` | benötigte GT-Materialien und Herstellungsweg im Pack |
| `13-gregtech-maschinen.md` | GT-Maschinen 1:1 (Basis BasicMachine, Upgrades, Explosionen, GUI; Plattenbieger, Platten, Mischmetall) |
| `14-kraftwerke.md` | neue Kraftwerke (Koksofen, Kessel, Dampfturbine, Wasserturbine), Balancing Wassermühle/Windmühle (Phase 9) |
| `15-rohre.md` | BuildCraft-Rohre: Item-, Flüssigkeits-, Leistungs- und Strukturrohre (Phase 10, ohne Gates/Drähte/Fassaden) |
| `16-feinschliff.md` | Feinschliff: EMI-Kategorien, Xaero's Minimap, Texturen, Forestry-Ledger, Tests, Teilphasen (Phase 11) |

Ergänzend: `docs/balancing.md` (Vorschlag, nicht freigegeben), `docs/abweichungen.md`.

Bei Detailfragen ist der dekompilierte Code die maßgebliche Referenz; die Spezifikation nennt jeweils die Klasse.
