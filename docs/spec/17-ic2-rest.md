# 17 – Restliche IC2-Inhalte (Phase 12)

Umfang laut `16-feinschliff.md` §8 E4: alle IC2-Inhalte, die bisher keiner Phase zugeordnet waren, plus die offenen
GT-Änderungen ohne Rezeptcharakter (`08-rezepte.md` §11, offene Entscheidung 4 in `CLAUDE.md`). Mechanik 1:1 nach
`04-maschinen.md`/`05-items.md`/`08-rezepte.md`; diese Datei ordnet nur zu, listet Lücken und plant die Teilphasen.

## 1. Bestandsaufnahme (Stand 2026-10-17)

Abgleich der Item-/Blocktabellen aus `05-items.md` §1, §4, §7, §9, §10 und `04-maschinen.md` mit den Registrierungen
in `Ic2Items`/`Ic2Blocks`.

**Schon vorhanden und für Phase 12 nutzbar:** OD-/OV-Scanner (Layer-Scan für den Miner), IC2-Bergbaurohr,
IC2-Pumpe (Miner pumpt Flüssigkeiten über sie), Solarhelm, Hazmat-Set + Gummistiefel (Schutz vor Tesla-Spule),
Schaum-Zustand der Kabel (`foam = none/wet/hard`, Schaumfarbe im BlockEntity), Recycler-Blacklist als Tag
`craft:recycler_blacklist` (Glasscheibe, Stock, Schneeball), Canning Machine Modi 0–2, Upgrade-Module.

**Fehlt – Liste E4 (Phase 12 gesetzt):**

| Bereich | Inhalt | Spezifikation |
|---|---|---|
| Miner | Block, GUI, Abbau-Zyklus, Rohre einziehen/Loch füllen | `04` §Miner |
| Teleporter | Block, Frequenzsender (Item), Kosten aus angrenzenden Speichern | `04` §Teleporter, `05` §4 |
| Tesla-Spule | Block | `04` §Tesla-Spule |
| Terraformer | Block, 7 Blaupausen (leer + 6) | `04` §Terraformer (Algorithmen: `ic2/core/item/tfbp/*`) |
| Luminator | Block an/aus, Montageseite, Überspannung | `04` §Luminator |
| Personal-Blöcke | Persönlicher Safe, Trade-O-Mat, Energy-O-Mat | `04` §Personal-Blöcke |
| Bauschaum | Pellet, Sprüher, CF-Pack, Schaum-/Wandblock (färbbar), Canner-Modus 3, Schaum auf Kabeln | `05` §1, §4, §5.3, §10 |
| Crops | Crop-Stab (Block), 22 Sorten, Saatbeutel, Cropnalyzer, Dünger, Hydrierzelle, Weed-EX, Grinpulver, Terra-Warze | `05` §9 (Detail-Stats: `ic2/core/block/crop/*`) |
| Crop-Matron | Block | `04` §Crop-Matron |
| Kaffee/Fass | Kaffeebohnen/-pulver, Tasse (leer, kalt, heiß, Milchkaffee), Hopfen, Fass, Bier/Rum | `05` §9 (`ItemMugCoffee`, `ItemBooze`, `TileEntityBarrel`) |
| GT ohne Rezept | Upgrade-Stack 4, GT-Recycler-Blacklist, Miner-Lava | §3 |
| Compact Solars | Solar-Hüte LV/MV/HV | `11-compact-solars.md` §3 |

**Fehlt – bisher gar nicht zugeordnet** (in der Bestandsaufnahme gefunden, siehe Entscheidung E1):

| Inhalt | Spezifikation | Bemerkung |
|---|---|---|
| Isolierungsschneider | `05` §4, `01` §3 | Kabel abisolieren (Blocktausch existiert schon) |
| Farbroller + 16 Farben | `05` §4 | Kabel/Wolle/Schafe färben (`changeColor` existiert schon) |
| EU-Meter | `05` §4 | Messung über das EU-Netz |
| Werkzeugkasten | `05` §4 | tragbares Inventar |
| Dynamit, Klebedynamit, Dynamit-Block, Fernzünder | `05` §7 | `PointExplosion` 3×3×3 |
| Münze | `05` §1, `08` §1 | Config `enableCraftingCoin = true` |
| Gummimatte (Trampolin) | `05` §10 | |
| Holz-/Eisengerüst | `05` §10 | Bauschaumsprüher ersetzt Gerüste |
| Verstärkter Stein, verstärkte Tür | `05` §10 | Bauschaumwand-Pendant; Rezept in der Teilphase nachtragen |
| Debug-Item | `05` §4 | nur Entwickler – **entfällt** |

## 2. Lücken in der Spezifikation (in der jeweiligen Teilphase nachtragen)

Wie in den Phasen 6/8 werden Details erst in der Teilphase aus `reference/decompiled/ic2/` gelesen und als
„Details aus dem Original (nachgetragen für Phase 12.x)“ in `04`/`05` ergänzt:

- GUI-Layouts (Slots, Fortschritts-/Energieanzeigen) aller neuen Blöcke.
- Miner: Fortschrittsanzeige, Verhalten ohne Scanner/mit leerem Scanner, Drop-Verteilung bei vollem Inventar.
- Terraformer: die sechs `TerraformerBlueprint`-Algorithmen vollständig (Blocklisten 1.4.7 → 1.21 abbilden;
  Mushroom setzt das Biom – in 1.21 nur über Biom-Container möglich, ggf. Abweichung).
- Bauschaum: Aushärtezeit (Licht), Flood-Fill-Reihenfolge, Färben der Wand, Schaum auf Kabeln aufsprühen.
- Crops: `stat(0..4)`, Wachstumsdauern, Sonderregeln je Sorte, Saatbeutel-Darstellung (Scanlevel), Kreuzungsgewichte.
- Fass: Brauzustände (Wasser, Weizen/Hopfen/Zuckerrohr, Zeit, Alkoholstärke), Trinkeffekte von Bier/Rum.
- Personal-Blöcke: Besitzerprüfung, getrennte GUIs für Besitzer/Kunden.
- Texturen: Sprites aus den IC2-Original-Sheets schneiden (wie bisher, Phase-11-Entscheidung E1).

## 3. GT-Änderungen ohne Rezeptcharakter (Pack-Werte)

| Änderung | Pack-Wert (Config) | Wirkung |
|---|---|---|
| Upgrade-Stack | `GregTech.cfg` `UpgradeStacksize=4` | Overclocker/Transformer/Energiespeicher-Upgrade stapeln nur bis 4 (IC2: 64) |
| Recycler-Blacklist | `IC2.cfg` `recyclerBlacklist=102, 280, 332, 4081` (Glasscheibe, Stock, Schneeball, Fremdmod); GT `DisableEasyMobGrindersInRecycler=false`, `DisableStoneInRecycler=false` | Weil beide GT-Schalter aus sind, ergänzt GT nur die vier unbedingten Einträge (`GT_Mod` Z. 2080–2083: 1 Item, 3 Vanilla-Blöcke) und die GT-Kleinstäube (nicht im Umfang). Zuordnung der IDs in der Teilphase |
| Miner: Lava wertvoll | `GregTech.cfg` `MinerListLavaAddition=true` | Miner mit Scanner und angrenzender Pumpe zielt auf Lava-Quellblöcke (schon in `04` §Miner beschrieben) |
| Miner: Lapotron | `IC2.cfg` `enableMinerLapotron=false` | Tier 1 (wie spezifiziert) |
| Teleporter-Inventar | `IC2.cfg` `enableTeleporterInventory=true` | Inventar zählt zum Gewicht (wie spezifiziert) |

## 4. EMI

Neue Kategorien in der jeweiligen Teilphase (der GameTest `RecipeCategoryGameTests` fängt neue Rezeptarten):
Fass (Brauen), Crops (Basissaaten → Sorte, Ernte als Infoseite), Terraformer-Blaupausen (Infoseite), Bauschaum
(Canner-Modus 3 in der Canner-Kategorie). Werkbankrezepte zeigt EMI ohnehin.

## 5. Teilphasen (Vorschlag)

| Teilphase | Inhalt |
|---|---|
| 12.0 | diese Spezifikation |
| 12.1 | Kleine Werkzeuge und Blöcke: Isolierungsschneider, Farbroller, EU-Meter, Werkzeugkasten, Münze, Gummimatte, verstärkter Stein + Tür |
| 12.2 | Bauschaum komplett (Pellet, Sprüher, CF-Pack, Schaum/Wand, Gerüste, Canner-Modus 3, Kabelschaum) + Luminator |
| 12.3 | Miner (+ Miner-Lava, §3) |
| 12.4 | Teleporter + Frequenzsender, Tesla-Spule |
| 12.5 | Terraformer + Blaupausen |
| 12.6 | Personal-Blöcke (Safe, Trade-O-Mat, Energy-O-Mat) |
| 12.7a | Crops Grundgerüst: Crop-Stab, Umwelt/Wachstum/Kreuzung/Ernte, Saatbeutel, Pflege-Items, Cropnalyzer, Weed |
| 12.7b | alle übrigen Crop-Sorten |
| 12.7c | Crop-Matron |
| 12.8 | Kaffee, Hopfen, Fass, Bier/Rum |
| 12.9 | Dynamit, Klebedynamit, Dynamit-Block, Fernzünder |
| 12.10 | GT-Änderungen §3, Testplan ergänzen, Abschluss |

## 6. Entscheidungen (Nutzer, 2026-10-17)

- **E1 Umfang:** alle bisher nicht zugeordneten IC2-Inhalte aus §1 kommen in Phase 12 (12.1, 12.9); Debug-Item
  entfällt.
- **E2 Upgrade-Stack 4:** wie im Pack.
- **E3 Recycler-Blacklist:** GT-Einträge wie im Pack ergänzt.
- **E4 Miner-Lava:** wie im Pack (an).
- **E5 Solar-Hüte:** entfallen (Solarhelm existiert; mit dem Controller als Zutat zu billig). In `abweichungen.md`
  vermerkt.
