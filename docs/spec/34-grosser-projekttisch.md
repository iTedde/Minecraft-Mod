# 34 – Großer Projekttisch

Neuer Inhalt ohne Pack-Vorbild (Nutzer, 2026-10-27): „Es sollte ein teurerer Project Table werden, der quasi eine
Holzchest als Platz hatte.“ Ein zweiter Projekttisch, teurer als der aus Phase 18 (`20-werkbaenke.md` §1), mit einem
Vorrat so groß wie eine Holztruhe (**27 Slots**, 3 Reihen à 9) statt 18. Der bisherige Projekttisch bleibt unverändert.

## 1. Block

| Eigenschaft | Umsetzung |
|---|---|
| ID / Name | `craft:large_project_table`, „Large Project Table“ / „Großer Projekttisch“ (Vorschlag, **F1**) |
| Material / Härte | wie Projekttisch: Stein, 2,0; `mineable/pickaxe` + `requiresCorrectToolForDrops` |
| Ausrichtung | `HORIZONTAL_FACING` (Vorderseite zum Spieler) |
| Texturen | Projekttisch-Kacheln mit Eisenrand (Python/Pillow aus den vorhandenen Texturen), damit beide unterscheidbar sind (**F3**) |
| Tick | keiner |
| Rechtsklick | schleichend → nichts; sonst GUI |
| Tab | „Craft: RedPower“, direkt hinter dem Projekttisch |

## 2. Inventar (37 Slots)

| Slots | Inhalt |
|---|---|
| 0–8 | Werkbankraster 3×3 (bleibt beim Schließen im Tisch) |
| 9 | Bauplan-Slot (wie Projekttisch) |
| 10–36 | Vorrat, **27 Slots** (3 Reihen à 9) |

- Automatisierung: Item-Capability auf **allen Seiten** = nur Vorrat 10–36 (wie Projekttisch).
- Abbau: alle 37 Slots droppen (wie die Craft-Fassung des Projekttischs, F2 aus Spec 20).
- Speicherung wie Projekttisch (`Items` + Ausrichtung).

## 3. Handwerk

Identisch mit dem Projekttisch (`20-werkbaenke.md` §1.3–1.5): Schattenraster, Nachschub aus dem Vorrat, Baupläne,
Bauplan-Knopf, Komponenten egal beim Abgleich. Einziger Unterschied: der Vorrat umfasst 27 statt 18 Slots. Die Logik
wird dafür auf eine Vorratsgröße als Parameter umgestellt (kein kopierter Code).

## 4. GUI

- Hintergrund: `advbench.png`-Aufbau, Vorrat um eine Reihe (18 px) erhöht → 176 × 240; Spielerinventar rutscht 18 px
  nach unten. Bild per Python/Pillow aus der vorhandenen Projekttisch-GUI erzeugt.
- Titel „Großer Projekttisch“ / „Large Project Table“.
- EMI: „+“-Füllen wie beim Projekttisch (`CraftingTableEmiHandlers`, Vorrat 27 Slots), als Werkbank-Workstation
  eingetragen.

## 5. Rezept (Vorschlag, **F2**)

`III/CPC/III` (geformt, spiegelbar):

| Zeichen | Zutat |
|---|---|
| I | Eisenbarren (`#c:ingots/iron`) |
| C | Holztruhe (`#c:chests/wooden`) |
| P | Projekttisch (`craft:project_table`) |

Kosten damit: Projekttisch (Stein, Bretter, Werkbank, Truhe) + 2 Truhen + 6 Eisen. Ein Umbau mit Inhalt gibt es nicht:
der alte Tisch wird beim Abbau geleert (alles droppt), dann verbaut.

## 6. Texte, Wiki, Tests

- Lang de/en, Tooltip „27 Vorratsplätze“ / „27 storage slots“.
- EMI-Info-Seite, Wiki (RedPower-/Werkbank-Seite, Änderungsliste), Herkunft (`24-herkunft.md`-Mechanismus).
- GameTests: Nachschub aus Vorratsslot 27–36 (dritte Reihe), Item-Capability = 27 Slots, Abbau droppt alle Slots,
  Rezept vorhanden.
- Testplan: GUI im Client (Slots, Bauplan, EMI-„+“), Holzrohr/Trichter am Vorrat.

## 7. Offene Entscheidungen

| Nr. | Frage | Vorschlag |
|---|---|---|
| F1 | Name | „Großer Projekttisch“ / „Large Project Table“ |
| F2 | Rezept | `III/CPC/III` (Projekttisch + 2 Truhen + 6 Eisenbarren) |
| F3 | Aussehen | Projekttisch-Textur mit Eisenrand |

## 8. Teilphasen

- **34.0** Spezifikation (diese Datei) – Freigabe offen.
- **34.1** Umsetzung: Block, Block-Entity, Menü/Screen, Logik mit Vorratsgröße als Parameter, Rezept, Lang, Texturen,
  EMI-Handler, GameTests.
- **34.2** EMI-Info, Wiki, Herkunft, Testplan, Paket.
