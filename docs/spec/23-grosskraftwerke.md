# 23 – Großkraftwerke (Phase 21)

Neue Inhalte ohne Pack-Vorbild nach `docs/balancing.md` §3.8 (Richtung freigegeben vom Nutzer am 2026-10-20,
„Mach Phase 21 komplett“ – Zahlen wie im Vorschlag, Details hier festgelegt). Jede Nicht-Solar-Quelle bekommt eine
zweite, große Ausbaustufe; die kleinen Anlagen aus `14-kraftwerke.md` bleiben unverändert. Ziel: eine Großanlage
≈ ⅓–⅔ eines HV-Solarfelds (1536 EU/t am Tag), dafür Tag und Nacht.

Paket `dev.craft.powerplants`, Creative-Tab „Craft: Kraftwerke“. Alle Anlagen nutzen dieselben Bausteine wie
`14-kraftwerke.md` (Controller + Gehäuse mit `MultiblockLease`, Prüfung beim Setzen/Abbauen und alle 100 Ticks,
unvollständig = arbeitet nicht, Zustand bleibt). Die Logik von Kessel, Dampfturbine und Wasserturbine wird über eine
**Stufe** (klein/groß) parametrisiert, nicht kopiert. Reichweite der Strukturbenachrichtigung beim Setzen/Abbauen
eines Teils: 8 Blöcke (vorher 4; größte Entfernung Teil → Controller ist jetzt 8).

| Anlage | Struktur | Leistung | Stufe |
|---|---|---|---|
| Hochdruckkessel | 5×5×5 hohl, 97 Gehäuse + Feuerung | 160 mB/t Dampf bei 1500 °C | – |
| Große Dampfturbine | 5×5×9, Achse aus 7 Rotoren | bis 512 mB/t → 512 EU/t | HV (512) |
| Große Wasserturbine | 5×5×5 massiv, 124 Gehäuse + Controller | 1 EU/t je fließendem Block, Deckel 256 (Fluss 384) | HV (512) |
| Große Windturbine | Gondel auf **24** Mastblöcken, Rotor-Item 5×5 | y 128 ≈ 38, y 200 ≈ 82, y 319 ≈ 153, Deckel 192 | HV (512) |

Werte und Rezepte seit Phase 32 (Nutzer, 2026-10-26: gleiche Preis-Leistung) – Begründung und Kostenvergleich in
`32-kraftwerke-ausgleich.md`; die Abschnitte unten sind darauf angepasst.

---

## 1. Hochdruckkessel

**Struktur:** Würfel 5×5×5, Innenraum 3×3×3 **Luft**, Mittelblock einer Seitenfläche = **Hochdruck-Kesselfeuerung**
(Controller, Facing nach außen), restliche 97 Blöcke **Hochdruck-Kesselgehäuse**.

**Werte** (Ablauf wie Kessel `14` §3, nur andere Zahlen):

| Wert | Kessel | Hochdruckkessel |
|---|---:|---:|
| Wasser-/Dampftank | 16 000 mB | 64 000 mB |
| Höchsttemperatur | 1000 °C | **1500 °C** |
| Dampf bei Höchsttemperatur | 20 mB/t | **160 mB/t** (`160 · (T − 100) / 1400`) |
| Brennzeit Kohle | 800 Ticks (16 000 EU) | **150 Ticks (24 000 EU)** |
| Brennzeit Koks | 1 600 Ticks (32 000 EU) | **300 Ticks (48 000 EU)** |
| Explosion | Stärke 6 | **Stärke 10** |
| Dampfabgabe je Tick | 1 000 mB | 1 000 mB |

- Heizen +1 °C/Tick, Abkühlen −1 °C/Tick, Start 20 °C, Dampf ab 100 °C (wie klein) → ≈ 1 400 Ticks bis Volllast.
- Brennstoff wie klein: nur Kohle und Koks, voller Rate unabhängig von der Temperatur; Asche: Kohle +1, Koks +2,
  je 4 Punkte 1 Asche, voller Asche-Slot → kein Nachlegen.
- Überhitzung/Trockenregel wie klein (Explosion bei Wasser auf trockenen heißen Kessel oder trocken bei 1500 °C);
  zerstört alle Hochdruck-Kesselblöcke, Explosion `Ic2Explosion` Stärke 10 an der Strukturmitte.
- Fluid-Capability an allen Blöcken (Füllen nur Wasser, Entnehmen nur Dampf), Items wie klein (oben/Seiten →
  Brennstoff, unten → Asche), Behälter-Slots für Wasser.
- Wasserbedarf bei Volllast 160 mB/t (3,2 Eimer/s) – nur mit Pumpe/Rohren sinnvoll.

**GUI:** dieselbe wie der Kessel (`gui/boiler.png`); Temperatur- und Tankbalken skalieren auf die Werte der Stufe
(werden mit dem Menü übertragen).

**Rezepte:**
- Hochdruck-Kesselgehäuse ×8: `SBS/BKB/SBS` – S Stahlplatte, B Kesselgehäuse, K Ziegelblock.
- Hochdruck-Kesselfeuerung: `CCC/CFC/CCC` – C Hochdruck-Kesselgehäuse, F Kesselfeuerung.

Blockwerte: Härte 5,0, Resistenz 10, Spitzhacke, Metall; Feuerung leuchtet beim Brennen (13).

---

## 2. Große Dampfturbine

**Struktur:** Quader 5×5×9 (lange Achse horizontal). Mitte einer 5×5-Stirnseite = **Große Dampfturbine**
(Controller, Facing nach außen). Innenraum 3×3×7: Mittelachse = 7 **Große Turbinenrotoren**, die übrigen 56
Innenblöcke **Luft**. Hülle (161 Blöcke ohne Controller) = **Große Turbinengehäuse**.

**Werte:**
- Dampftank 64 000 mB, Annahme über alle Blöcke (nur Dampf).
- Jeden Tick `min(Dampf, 512, Pufferplatz)` mB → ebenso viele EU (1 mB = 1 EU). Bis zu 3 Hochdruckkessel
  (3 × 160 = 480 mB/t) sind voll nutzbar.
- EU-Quelle am Controller, Paket bis **512 EU (HV)**, Puffer 1 024 EU. Jedes Große Turbinengehäuse einer geformten
  Turbine leitet verlustfrei (wie `14` §4).
- Keine Explosion, kein Verschleiß.

**GUI:** dieselbe wie die Dampfturbine (`gui/steam_turbine.png`), Tank skaliert auf die Stufe.

**Rezepte:**
- Großes Turbinengehäuse ×8: `PIP/IAI/PIP` – P Stahlplatte, I Raffinierte-Eisen-Platte, A Fortgeschrittenes
  Maschinengehäuse (bis Phase 31: 8 Stahlplatten).
- Großer Turbinenrotor: `STS/SRS/STS` – T Titanplatte, S Stahlplatte, R Turbinenrotor (bis Phase 31: 4 Titan + 4
  Wolframstahl).
- Große Dampfturbine: `CRC/GHG/CRC` – C Großes Turbinengehäuse, R Großer Turbinenrotor, G Dampfturbine,
  H HV-Transformator.

Blockwerte: Härte 5,0, Resistenz 10, Spitzhacke, Metall.

---

## 3. Große Wasserturbine

**Struktur:** Würfel 5×5×5, massiv, Mittelblock einer Seitenfläche = **Große Wasserturbine** (Controller),
restliche 124 Blöcke **Große Wasserturbinengehäuse**.

**Werte** (Ablauf wie Wasserturbine `14` §5):

| Wert | Wasserturbine | Große Wasserturbine |
|---|---:|---:|
| Messbereich seitlich | 5 Blöcke | **7 Blöcke** |
| Messbereich über dem Deckel | 20 Blöcke | **24 Blöcke** |
| Mindestens fließend | 16 | **64** |
| Ertrag je fließendem Block | 0,25 EU/t | **1,0 EU/t** |
| Fluss-Biom | ×1,5 | ×1,5 |
| Deckel / Paket | 32 EU/t, Fluss 48 (Pakete bis 32, LV) | **256 EU/t**, Fluss **384** (Paket bis 384, Stufe HV) |

- Messbereich: 19×19 Blöcke, von der unteren Lage bis 24 Blöcke über der oberen Lage, ohne den Würfel selbst
  (10 344 Positionen), nur fließendes Wasser, alle 128 Ticks (Zufallsversatz), ungeladene Positionen zählen nicht.
- Deckel erreicht ab 256 fließenden Blöcken, im Fluss-Biom ebenfalls ab 256 (dann 384 EU/t; bis Phase 31 blieb der
  Deckel im Fluss 256 und wurde nur früher erreicht).
- Jeder Gehäuseblock einer geformten Turbine leitet (wie klein).

**GUI:** keine; Rechtsklick zeigt wie klein „Fließendes Wasser: n, Leistung x EU/t“.

**Rezepte:**
- Großes Wasserturbinengehäuse ×6 (bis Phase 31 ×8): `SRS/RMR/SRS` – S Stahlplatte, R Raffinierte-Eisen-Platte, M Maschinengehäuse.
- Große Wasserturbine: `WCW/CHC/WCW` – W Wasserturbine, C Großes Wasserturbinengehäuse, H HV-Transformator.

---

## 4. Große Windturbine

**Struktur:** senkrechte Säule: **Große Windturbine** (Gondel, Controller, Facing horizontal = Rotorseite) auf
**24 Windturbinenmasten** direkt darunter (bis Phase 31: 8). Gesetzte Masten weiter als 8 Blöcke von der Gondel meldet
erst die regelmäßige Strukturprüfung (≤ 100 Ticks); abgebaute Masten melden sich sofort. Der Rotor ist kein Block, sondern ein **Rotor-Item** in der Gondel; er
dreht sich als 5×5-Rad (Durchmesser 5) vor der Gondel (Darstellung per Blockentity-Renderer, ohne Kollision).

**Rotor:** Rechtsklick mit einem Rotor setzt ihn ein (ein vorhandener wird getauscht), Schleichen + Rechtsklick mit
leerer Hand nimmt ihn heraus, sonst Rechtsklick → Chat-Meldung „Hindernisse: n, Leistung x EU/t“ bzw. „Kein
Rotor“/„Struktur unvollständig“. Der Rotor wird beim Abbau gedroppt. Kein Verschleiß, kein Zerfall (wie die
Windmühle, Nutzer 2026-10-17).

| Rotor | Faktor |
|---|---:|
| Eisen-Windrotor | 0,5 |
| Stahl-Windrotor | 0,75 |
| Carbon-Windrotor | 1,0 |

**Leistung:** alle 128 Ticks (Zufallsversatz)
`P = min(12 · sub · Faktor, 192)` EU/t (bis Phase 31: 20 und 384) mit `sub` = Windmühlenformel (`14` §7, `windStrength · (y − 64 − Hindernisse)
/ 750 · 2,5`, Regen ×1,5, Gewitter ×3,0), y = Höhe der Gondel, `energyGeneratorWind` (100 %) als Faktor.
Hindernisse = nicht-Luft-Blöcke im Bereich x/z ±5, y −3…+5 um die Gondel, ohne die eigenen Strukturblöcke; alle
1 024 Ticks neu gezählt. Bruchteile werden akkumuliert, Puffer höchstens 2 · 192.
Beispiele (Wind 15, frei, Carbon): y 128 → 38,4 EU/t, y 200 → 81,6 EU/t, y 319 → 153 EU/t, y 200 Gewitter → 192 EU/t
(Deckel).

**Abgabe:** EU-Quelle an der Gondel, Paket bis 192 (Stufe HV); jeder Mast einer geformten Turbine leitet
verlustfrei (Kabel am Mastfuß).

**Rezepte:**
- Windturbinenmast ×4: `S S/SSS/S S` – S Stahlplatte (bis Phase 31: ×8 aus Raffiniertem Eisen).
- Große Windturbine: `WAW/AHA/WAW` – W Windmühle, A Fortgeschrittenes Maschinengehäuse, H HV-Transformator (bis Phase
  31: `WAW/GHG/WAW` mit 2 Generatoren).
- Eisen-Windrotor: `PIP/IBI/PIP` – P Eisenplatte, I Eisenbarren, B Eisenblock.
- Stahl-Windrotor: `PPP/PRP/PPP` – P Stahlplatte, R Eisen-Windrotor (bis Phase 31: 4 Platten + 4 Stahlbarren).
- Carbon-Windrotor: `CAC/ARA/CAC` – C Carbonplatte, A Fortgeschrittene Legierung, R Stahl-Windrotor.

Blockwerte: Härte 5,0, Resistenz 10, Spitzhacke, Metall. Rotoren stapeln nicht (1).

---

## 5. Texturen, EMI, Wiki

- Eigene Texturen (abgeleitet aus den kleinen Anlagen: dunkler/stahlfarben, Nieten), Rotorblätter im Renderer
  mit der Textur des eingesetzten Rotors.
- EMI: Info-Seiten je Großanlage (Bauanleitung), Kessel-Kategorie um die Hochdruck-Brennstoffe, Dampfturbinen-
  Kategorie um die große Turbine ergänzt; Windrotoren mit Faktor-Tooltip.
- Wiki-Texte und Vergleichstabelle in `balancing.md` §4 ergänzen.

## 6. Tests (GameTests)

Hochdruckkessel (Struktur, Aufheizen bis 1500 °C, 160 mB/t, Brennzeiten, Explosion Stärke 10 bei Wasser auf
trockenen Kessel), große Dampfturbine (Struktur, 512-Deckel, Abgabe über ein Gehäuse), große Wasserturbine (64er-
Schwelle, 1 EU/Block, Deckel 256), große Windturbine (Struktur, Formel mit Rotorfaktor, Deckel 384, ohne Rotor 0,
Abgabe über den Mast).
