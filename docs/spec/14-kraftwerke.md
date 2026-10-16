# 14 – Neue Kraftwerke und Balancing von Wasser/Wind (Phase 9)

Neue Inhalte (kein Vorbild im Pack) nach `docs/balancing.md` §3.2–3.4, freigegeben vom Nutzer am 2026-10-16
(Koks = 32 000 EU, Wassermühle und Windmühle neu, Wasserturbine). Paket `dev.craft.powerplants`, eigener Creative-Tab
„Craft: Kraftwerke“. Texturen/GUIs: Railcraft aus dem Pack (`ftb ultimate/mods/railcraft.jar`: `gui_coke_oven.png`,
`gui_boiler_solid.png`, `gui_engine_steam.png`, `liquids.png`, `machines.png`), sonst eigene.

Entscheidungen des Nutzers (2026-10-16): Koks-Brennwert wie Kohle; Dampf per direktem Kontakt + Fluid-Capability;
Bauformen und Rezepte wie hier beschrieben; Wasserturbine ohne GUI. Die früheren Vorschlagsmarkierungen sind damit
entschieden.

Alle Multiblocks nutzen `core/multiblock` (Controller + Gehäuse mit `MultiblockLease`). Die Struktur wird beim
Setzen/Abbauen eines Teils und spätestens alle 100 Ticks geprüft; eine unvollständige Struktur arbeitet nicht
(Zustand bleibt erhalten). Abbauen eines Teils droppt nur den Block selbst, der Controller droppt zusätzlich seinen
Inhalt (Flüssigkeiten gehen verloren).

---

## 1. Gemeinsame Inhalte

| Inhalt | ID | Werte |
|---|---|---|
| Koks (Item) | `craft:coke` | Ofen-Brennwert **1600 Ticks** (wie Kohle); Tag `c:coal_coke` |
| Koksblock | `craft:coke_block` | 9 Koks ⇄ 1 Block, Brennwert 16 000 Ticks (wie Kohleblock) |
| Dampf (Flüssigkeit) | `craft:steam` | nur in Tanks (kein Weltblock, kein Eimer), gasförmig (`density −1000`, Temperatur 373 K) |
| Asche | `craft:ash` (vorhanden, GT „Ashes“) | Nebenprodukt des Kessels |

Folge: Koks bringt im IC2-Generator (4 000 EU), im Ofen und in der Stirling-Engine dasselbe wie Kohle; den Mehrwert
(32 000 EU) gibt es nur im Kohlekraftwerk (Nutzer, 2026-10-16).

---

## 2. Koksofen

**Struktur:** Würfel 3×3×3, Mitte **Luft**, Mittelblock einer Seitenfläche = **Koksofen** (Controller, Facing nach
außen), restliche 25 Blöcke **Koksofenziegel**. Controller nur an einer Seitenfläche (nicht oben/unten).

**Werte (Balancing §3.4):**
- 1 Kohle → 1 Koks in **900 Ticks**, kein Strom, eine Kohle gleichzeitig. Holzkohle wird nicht angenommen.
- Fortschritt bleibt bei fehlender Kohle stehen (kein Rücksetzen); bei voller Ausgabe wartet der Ofen.
- Keine Asche, kein Kreosot.

**GUI:** Railcraft `gui_coke_oven.png` ohne Tank: Eingabe-Slot (nur Kohle), Ausgabe-Slot, Flammen-/Fortschritts-
anzeige. Automatisierung: Item-Capability am Controller und an allen Ziegeln (oben/Seiten → Eingabe, unten → Ausgabe).

**Rezepte:**
- Koksofenziegel ×2: `BSB/SCS/BSB` – B Ziegel (Item), S Sand, C Ton (Ball).
- Koksofen: `BBB/BFB/BBB` – B Koksofenziegel, F Ofen.

Blockwerte: Ziegel und Controller Härte 2,0, Resistenz 10, Spitzhacke.

---

## 3. Kessel (Kohlekraftwerk, Teil 1)

**Struktur:** Würfel 3×3×3, Mitte **Luft**, Mittelblock einer Seitenfläche = **Kesselfeuerung** (Controller),
restliche 25 Blöcke **Kesselgehäuse**.

**Tanks:** Wasser 16 000 mB, Dampf 16 000 mB.

**Brennstoff:** nur Kohle und Koks (keine Blöcke, keine Holzkohle).
- Ist die Brennzeit 0, der Dampftank nicht voll, Wasser > 0 und der Asche-Slot nicht voll, wird 1 Brennstoff verbraucht:
  Kohle **800 Ticks**, Koks **1 600 Ticks** Brennzeit.
- Brennstoff brennt immer mit voller Rate, unabhängig von der Temperatur (Kaltstart-Verlust ist gewollt).

**Temperatur:** Start 20 °C. Jeden Tick mit Brennzeit > 0: `T = min(T + 1, 1000)`; sonst `T = max(T − 1, 20)`.

**Dampf:** ab T > 100 °C jeden Tick `dampf = 20 · (T − 100) / 900` mB (Bruchteile akkumuliert), begrenzt durch
Wasser und freien Dampfplatz; je 1 mB Dampf wird 1 mB Wasser verbraucht. Bei 1000 °C also 20 mB/t.

**Asche:** Zähler je verbranntem Brennstoff: Kohle +1, Koks +2; bei ≥ 4 → 1 Asche in den Asche-Slot, Zähler −4.
Voller Asche-Slot (64) → kein Nachlegen (laufende Brennzeit brennt zu Ende).

**Überhitzung:** Ist der Wassertank leer und T > 100 °C, wird der Kessel „trocken“. Trocken + Wasser kommt hinein
(Tank > 0) → Explosion. Trocken + T erreicht 1000 °C → Explosion. Sinkt T auf ≤ 100 °C, endet „trocken“.
Explosion: `Ic2Explosion` Stärke **6**, Mittelpunkt = Strukturmitte, alle Kessel-Blöcke werden zerstört.

**Dampfabgabe:** Jeden Tick schiebt der Kessel bis zu 1 000 mB Dampf in angrenzende Flüssigkeitsempfänger, die nicht
zum eigenen Kessel gehören (alle Außenflächen aller Kessel-Blöcke, reihum). Fluid-Capability an allen
Kessel-Blöcken: Füllen nur Wasser, Entnehmen nur Dampf (für BC-Rohre in Phase 10).

**Wasser:** über Fluid-Capability (IC2-Pumpe, BC-Pumpe/Tank) oder Behälter-Slot (Eimer/Wasserzelle → leerer Behälter
im Ausgabe-Slot, über `LiquidContainers`).

**GUI:** Railcraft `gui_boiler_solid.png`: Brennstoff-Slot, Asche-Slot, Wasser-Behälter ein/aus, Wasser- und
Dampftank, Temperaturbalken, Flamme (Brennzeit).

**Rezepte:**
- Kesselgehäuse ×4: `PBP/BFB/PBP` – P Eisenplatte (GT), B Ziegelblock, F Ofen.
- Kesselfeuerung: `CCC/CIC/CCC` – C Kesselgehäuse, I Eisenofen (IC2).

Blockwerte (ergänzt in 9.3a, wie Koksofen): Gehäuse und Feuerung Härte 2,0, Resistenz 10, Spitzhacke; Feuerung
leuchtet beim Brennen (13). Automatisierung Items: oben/Seiten → Brennstoff-Slot, unten → Asche. Bei der Explosion
geht der Inhalt der Feuerung verloren (Drop-Rate/Entity-Schaden wie `explodeMachineAt`: 0,75/0,75).

---

## 4. Dampfturbine (Kohlekraftwerk, Teil 2)

**Struktur:** Quader 3×3×5 (lange Achse horizontal). Innere Achse (1×1×3) = 3 **Turbinenrotoren**. Mitte einer
3×3-Stirnseite = **Dampfturbine** (Controller, Facing nach außen), restliche 41 Blöcke **Turbinengehäuse**.

**Werte (Balancing §3.4):**
- Dampftank 16 000 mB; Annahme über Fluid-Capability aller Turbinen-Blöcke (nur Dampf).
- Jeden Tick: `verbrauch = min(Dampf, 64)` mB → `verbrauch` EU in den internen Puffer (1 mB = 1 EU).
  Bis zu 3 Kessel (je 20 mB/t) sind damit voll nutzbar.
- EU-Quelle am **Controller**, Ausgabe-Paket bis **64 EU**, Stufe MV (Puffer 128 EU); ist der Puffer voll,
  wird kein Dampf verbraucht.
- Keine Explosion, kein Verschleiß.

**GUI:** Railcraft `gui_engine_steam.png`-Stil: Dampftank, aktuelle Leistung in EU/t, Pufferanzeige.

**Rezepte:**
- Turbinengehäuse ×4: `PPP/PMP/PPP` – P Stahlplatte, M Maschinengehäuse.
- Turbinenrotor: `PSP/SIS/PSP` – P Stahlplatte, S Stahlbarren, I Eisenblock.
- Dampfturbine: `CRC/GMG/CRC` – C Turbinengehäuse, R Turbinenrotor, G Generator (IC2), M MV-Transformator.

---

## 5. Wasserturbine

**Struktur:** Würfel 3×3×3, voll (Mitte = Wasserturbinengehäuse), Mittelblock einer Seitenfläche =
**Wasserturbine** (Controller mit Rotor-Textur), restliche 26 Blöcke **Wasserturbinengehäuse**.

**Werte (Balancing §3.2):**
- Ring: alle Positionen im 7×7×3-Quader um das Gehäuse (gleiche Höhe wie die 3 Lagen) außer dem Gehäuse selbst =
  120 Positionen. Gezählt werden nur **fließende** Wasserblöcke (keine Quellen), alle 128 Ticks (Zufallsversatz).
- Unter **16** fließenden Blöcken: 0 EU/t.
- Leistung `0,25 EU/t · fließend`, × 1,5 in Fluss-Biomen (`#minecraft:is_river` am Controller), Deckel **32 EU/t**.
- EU-Quelle am Controller, Paket bis 32 (LV), Bruchteile akkumuliert.

**GUI:** keine; Rechtsklick zeigt Chat-Meldung „Fließendes Wasser: n, Leistung x EU/t“ bzw. „Struktur
unvollständig“.

**Rezepte (Richtwert „4 Wassermühlen + Generator + Maschinengehäuse/raff. Eisen“):**
- Wasserturbinengehäuse ×4: `RRR/RMR/RRR` – R Raffinierte-Eisen-Platte, M Maschinengehäuse.
- Wasserturbine: `WCW/CGC/WCW` – W Wassermühle, C Wasserturbinengehäuse, G Generator.

---

## 6. Wassermühle (geändert, Balancing §3.2)

Ändert nur den **passiven** Zweig aus `02-generatoren.md`; Behälter-Betrieb, Slots, Drop und die Original-Eigenheit
(Fremd-Item im Behälter-Slot blockiert den passiven Zweig) bleiben.
- Zählung alle 128 Ticks im Bereich x −2..+2, y −1..+1, z −2..+2 (5×5×3 ohne die Mühle, max. 74).
- Ertrag je Block: Quelle 0,02 EU/t, fließend 0,04 EU/t, geteilt durch die Zahl der Wassermühlen, deren Bereich
  diesen Block enthält (Mühlen mit < 5 Blöcken Abstand teilen sich Wasser).
- Biom-Faktor am Mühlenblock: Fluss × 1,5, Ozean (`#minecraft:is_ocean`) × 1,25, sonst × 1,0.
- Deckel **2 EU/t**; Puffer 2 → 4 EU, Ausgabe bis 2 EU/t.
- Weiter über `microStorage` (Bruchteile in 1/100 EU) wie bisher; `energyGeneratorWater` (Config 100) bleibt Faktor.

## 7. Windmühle (geändert, Balancing §3.3)

Ändert gegenüber `02-generatoren.md`:
- `sub = windStrength · (y − 64 − hindernisse) / 750 · 2,5`.
- Gewitter × **3,0** (statt 1,5), sonst Regen × **1,5** (statt 1,2).
- Überlast erst ab `sub > 12,5`: `random(5000) ≤ sub − 12,5` → Zerfall wie bisher.
- Grenze `storage + production ≥ 16` (statt 4); `getMaxEnergyOutput` 16 (statt 10).
- GUI-Anzeigen: Leistung `sub/3` bleibt, Überhitzung `(sub − 12,5)/12,5`.

Beispiele: y 128, Wind 15, frei → 3,2 EU/t; Gewitter 9,6 EU/t; y 200 → 6,8 EU/t (Gewitter 20 → durch die
16er-Grenze gekappt).

---

## 8. Tests (GameTests)

Koksofen (Struktur, 900 Ticks, Holzkohle abgelehnt), Kessel (Aufheizen, Dampfrate bei 1000 °C, Asche, Explosion bei
Wasser auf trockenen heißen Kessel), Turbine (64-mB-Deckel, 1 mB = 1 EU, Abgabe ins Netz), Wasserturbine (16er-Schwelle,
Deckel 32), Wassermühle (Bereich, Teilung, Deckel), Windmühle (Faktoren, Grenze 16).
