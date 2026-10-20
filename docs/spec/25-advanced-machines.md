# 25 – Advanced Machines (Phase 24)

Nutzerwunsch (2026-10-20): das IC2-Addon **Advanced Machines 4.7b** aus dem Pack (`ftb ultimate/mods/advancedmachines.zip`,
dekompiliert nach `reference/decompiled/advancedmachines/`, die Zip enthält zusätzlich die Quelltexte unter `src/`)
1:1 nachbauen. Pack-Config `ftb ultimate/config/AdvancedMachines.cfg` = Standardwerte. GregTech ändert an den
Advanced Machines nichts (geprüft). Paket `dev.craft.advancedmachines`, eigener Creative-Tab „Craft: Advanced Machines“.

## 1. Inhalt

| ID (Craft) | Original | Name (de / en) |
|---|---|---|
| `craft:rotary_macerator` | `blockAdvMachine` Meta 0 | Rotations-Macerator / Rotary Macerator |
| `craft:singularity_compressor` | Meta 1 | Singularitätskompressor / Singularity Compressor |
| `craft:centrifuge_extractor` | Meta 2 | Zentrifugalextraktor / Centrifuge Extractor |
| `craft:refined_iron_dust` | `refinedIronDust` (Item, Textur `refinedIronDust.png`) | Raffinierter Eisenstaub / Refined Iron Dust |

Block: Material Eisen, Härte 2,0, Spitzhacke. Beim Setzen Facing horizontal zum Spieler (Blickrichtung umgekehrt:
`heading` 0 → 2, 1 → 5, 2 → 3, 3 → 4). Rauchpartikel (4 je Anzeigetick) bei Rotations-Macerator und
Singularitätskompressor, solange aktiv.

## 2. Rezepte (IC2-Werkbank, `Ic2ShapedRecipe`)

| Ergebnis | Muster | R | M | A |
|---|---|---|---|---|
| Rotations-Macerator | `RRR/RMR/RAR` | Raffiniertes Eisen | Macerator | Fortgeschrittenes Maschinengehäuse |
| Singularitätskompressor | `RRR/RMR/RAR` | Obsidian | Kompressor | Fortgeschrittenes Maschinengehäuse |
| Zentrifugalextraktor | `RRR/RMR/RAR` | Elektrolysierte Wasserzelle | Extraktor | Fortgeschrittenes Maschinengehäuse |
| Raffiniertes Eisen | Ofen: Raffinierter Eisenstaub, 1,0 XP | | | |

## 3. Maschinenlogik (`TileEntityAdvancedMachine` + `TileEntityBaseMachine`)

Konstanten: `MAX_PROGRESS = 4000`, Grund-Speicher 5000, Grund-Höchstdrehzahl 7500, Grund-Eingang 32.
Config: `baseEnergyConsumption = 3`, `baseMachineAcceleration = 2`, `overClockSpeedBonus = 500`,
`overClockEnergyRatio = 2,0`, `overClockAccelRatio = 1,6`, `overLoadInputRatio = 4,0`.

**Upgrades** (`setOverclockRates`, jeden Arbeitstick und vor jeder Energieannahme), 4 Upgrade-Slots, IC2-Upgrades
Overclocker O (max. 32 gezählt), Transformer T (max. 10), Energiespeicher S (unbegrenzt), Stapelgröße zählt:
- `energyConsume = (int)(3 · 2^O)`, `acceleration = (int)(2 · 1,6^O / 2)` (ohne Overclocker 1),
  `maxSpeed = 7500 + 500 · O`
- `maxInput = 32 · (int)4^T`, `maxEnergy = 5000 + 5000 · S + maxInput − 1`, `tier = 1 + T`

**Tick (Server):**
1. Wenn `energy ≤ maxEnergy`: Energie aus Slot 0 (siehe unten).
2. `progress ≥ 4000` → `operate()`, `progress = 0`, aktiv aus, Ereignis „Stopp“ (2).
3. `canOperate` = Eingabe vorhanden, Rezept vorhanden, Ausgabeslots fassen zusammen das ganze Ergebnis.
4. Wenn `energy > 0` **und** (`canOperate` **oder Redstone-Signal**): Raten neu setzen; ist `speed < maxSpeed`:
   `speed += acceleration`, `energy −= energyConsume`; sonst `speed = maxSpeed`, `energy −= 3` (Grundverbrauch,
   **auch mit Overclockern**). Aktiv, Ereignis „Start“ (0).
   Sonst: `speed −= min(speed, 4)`; fällt `speed` dabei auf 0 → Ereignis „Unterbrechung“ (1).
   (Energie darf negativ werden, wie im Original.)
5. Aktiv-Logik: lief schon (`progress ≠ 0`) und kein Rezept oder `speed = 0` → inaktiv (ohne Rezept auch
   `progress = 0`); sonst mit Rezept und `speed ≠ 0` aktiv; ohne Rezept `progress = 0`.
6. Aktiv und Rezept → `progress += speed / 30` (Ganzzahl).

Folgen (für Wiki/EMI): Hochlaufen von 0 auf 7500 dauert ohne Overclocker 7500 Ticks (6¼ min, 22 500 EU); bei voller
Drehzahl `progress` +250 je Tick → **16 Ticks je Vorgang** bei 3 EU/t (48 EU je Item). Ein Redstone-Signal hält die
Maschine ohne Eingabe auf Drehzahl (verbraucht weiter Energie).

**`operate`:** Ergebnis (Kopie) auf die Ausgabeslots in Reihenfolge verteilen; trifft es auf einen **leeren** Slot,
kommt der Rest komplett dorthin. Danach `onFinishedProcessingItem`, leere Eingabe entfernen.

**Energieannahme** (`injectEnergy`, alle Seiten): Paket > `maxInput` → `explodeMachineAt` (IC2-Explosion wie
Maschinen, `spec 01` §4); sonst `energy += Paket`, Überschuss über `maxEnergy` zurück. `demandsEnergy = maxEnergy − energy`.

**Slot 0 (Energie):** elektrisches Item mit `canProvideEnergy` → entladen bis `maxEnergy − energy` mit der Stufe
`tier`; **Redstone** → `energy += maxEnergy` (kann über den Speicher gehen), 1 verbraucht; **Einweg-Batterie**
(`suBattery`) → +1000, 1 verbraucht.

**Speichern (NBT):** Energie, Inventar, Facing, `speed`, `progress`. Der Zusatz-Zähler des Rotations-Macerators
(§4) wird **nicht** gespeichert (wie im Original).

## 4. Die drei Maschinen

| | Rotations-Macerator | Singularitätskompressor | Zentrifugalextraktor |
|---|---|---|---|
| Rezepte | Macerator (`craft:macerator`) | Kompressor (`craft:compressor`) | Extraktor (`craft:extractor`) |
| Slots | 0 Energie, 1 Eingabe, 2–3 Ausgabe, 4–7 Upgrades, **8 Zusatz** | 0, 1, 2 Ausgabe, 3–6 Upgrades | 0, 1, 2–4 Ausgabe, 5–8 Upgrades |
| GUI-Anzeige | „Speed:“ `%5d RPM` (speed × 1) | `%6d PSI` (speed × 10) | `%5d M/S` (speed × 1) |
| GUI-Textur | `GUIRotary.png` | `GUISingularity.png` | `GUICenterfuge.png` |
| Geräusch | IC2 `MaceratorOp` | `CompressorOp` | `ExtractorOp` |

Gemeinsam: Unterbrechungsgeräusch IC2 `InterruptOne`. GUI: Energiebalken bei (56, 36) 14 px hoch aus (176, 0),
Fortschrittspfeil bei (75, 34) aus (176, 14), Breite `progress · 24 / 4000 + 1`; Titel bei (49, 6); Slotlagen
aus den `Container*`-Klassen (Eingabe 56/17, Energie 56/53, Upgrades x 152, y 6/24/42/60; RM-Ausgaben 115/25 und
115/46, Zusatz 75/17; SC-Ausgabe 115/35; CE-Ausgaben 115/17, 35, 53).

Seitenzugriff (`ISidedInventory`): **unten** Slot 0 (Energie), **oben** die Eingabe, **Seiten** die Ausgaben. Der
Zusatzslot 8 ist nicht automatisierbar.

**Rotations-Macerator – Zusatzslot** (`getSpecialResultFor`, Zusatz = Slot 8):

| Normales Ergebnis | Zusatz | Sonderergebnis | Items je Zusatz |
|---|---|---|---|
| Eisenstaub | Kohle oder Holzkohle (Item `coal`, jede Meta) | Raffinierter Eisenstaub × Ergebnisanzahl | 128 |
| Kupferstaub | Item, dessen Macerator-Ergebnis Zinnstaub ist (Zinnerz, -barren …) | Bronzestaub × Ergebnisanzahl | 4 |
| Kohlestaub | Wasserzelle | Hydrierter Kohlestaub (**immer 1**) | 8 |

Zähler `supplementedItemsLeft`: ist er 0 und gibt es ein Sonderergebnis, wird er auf „Items je Zusatz“ gesetzt
(schon bei der Rezeptprüfung). Solange er > 0 ist, gilt **nur** das Sonderergebnis – passt die Eingabe nicht zum
Zusatz, gibt es gar kein Ergebnis und die Maschine steht (Originaleigenheit, bis der Zusatz entnommen wird). Nach
jedem fertigen Vorgang −1; bei 1 wird ein Zusatz-Item verbraucht (die Wasserzelle ohne leere Zelle zurück).

## 5. Abbau und Schraubenschlüssel

- Abbau ohne Schraubenschlüssel: droppt ein **Fortgeschrittenes Maschinengehäuse** und den Inventarinhalt.
- IC2-Schraubenschlüssel (`IWrenchable`): drehen (nur horizontal, nicht auf dieselbe Seite), Schleichen + Rechtsklick
  entfernt mit Drop-Rate 1,0 → die Maschine selbst.
- Originalfehler nicht übernommen: Ein Rechtsklick mit dem Schraubenschlüssel, den das Werkzeug nicht verarbeitet,
  invalidiert im Original das Tile-Entity (Maschine tot bis zum Neuladen). In Craft passiert dabei nichts
  (`abweichungen.md`).

## 6. Texturen

`block_advmachine.png` (256×256, 16er-Raster): Spalte = Meta (0–2), Zeile = IC2-Seitenversatz
(`sideAndFacingToSpriteOffset`: 0 unten, 1 oben, 2 hinten, 3 vorne, 4/5 Seiten), aktive Variante Zeile + 6.
Item-Ansicht = Facing 3. Raffinierter Eisenstaub: `refinedIronDust.png`. GUIs aus den drei PNGs.

## 7. Integration

- EMI: die drei Maschinen als Workstation der Macerator-/Kompressor-/Extraktor-Kategorie; eigene Kategorie
  „Rotations-Macerator: Zusatz“ mit den drei Sonderrezepten; Info-Seite zu Hochlauf und Redstone.
- Herkunftstext (spec 24) für den Raffinierten Eisenstaub; Bronzestaub und Hydrierter Kohlestaub um den neuen Weg
  ergänzen.
- Wiki: Mod-Seite „Advanced Machines“ (Werte, Hochlauf, Zusatzslot); Energie-Steckbrief erfasst die Maschinen
  automatisch (Getter `getMaxEnergy`, `getEnergyConsume` …).
- `abweichungen.md`: eigener Creative-Tab, Wrench-Fehler, Namen.

## 8. Teilphasen

| Teilphase | Inhalt |
|---|---|
| 24.0 | diese Spezifikation |
| 24.1 | Blöcke, Logik, GUIs, Rezepte, Texturen, Geräusche, EMI, Herkunftstexte, Wiki, GameTests |
