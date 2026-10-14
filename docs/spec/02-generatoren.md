# 02 – Generatoren

Quelle: `ic2.core.block.generator.*`, Config-Werte aus `IC2.cfg` des Packs (alle Standard:
`energyGeneratorBase=10, Geo=20, Water=100, Solar=100, Wind=100, Nuclear=5`).

## Gemeinsames Verhalten (`TileEntityBaseGenerator`)

Block `blockGenerator` (Material Eisen, Härte 3,0, Metall-Sound). Meta: 0 Generator, 1 Geothermal, 2 Wassermühle,
3 Solarpanel, 4 Windmühle, 5 Kernreaktor.

Felder: `fuel` (int), `storage` (short, interner Puffer), `maxStorage`, `production` (EU/t), `tier = 1`.

Ablauf pro Tick (nur Server):

1. Wenn `needsFuel()` → `gainFuel()` (Brennstoff aus Slot nachlegen).
2. `active = gainEnergy()` (Standard: wenn `isConverting()` → `storage += production`, `fuel −= 1`).
   Standard-`isConverting`: `fuel > 0 && storage + production ≤ maxStorage`;
   Standard-`needsFuel`: `fuel ≤ 0 && storage + production ≤ maxStorage`.
3. `storage` auf `maxStorage` kappen.
4. Wenn `storage > 0`:
   - Item in Slot 0 ist elektrisch → mit Tier 1 laden (`charge(stack, storage, 1, false, false)`, also mit
     Transferlimit des Items), `storage −= geladen`.
   - `output = min(production, storage)`; ist `output > 0`: ein Paket `output` ins Netz; nicht verteilter Rest bleibt im
     `storage` (`storage += rest − output`).
5. Generatoren geben in **alle 6 Richtungen** ab (`emitsEnergyTo = true`), `getMaxEnergyOutput = production`
   (Ausnahme Windmühle: 10).
6. Aktiv-Anzeige: Generator/Geo/Reaktor sofort; Solar/Wasser/Wind verzögert („activityMeter“: alle 256 Ticks
   aktiv, wenn in diesem Fenster mehr aktive als inaktive Ticks).

Wrench-Drop-Rate 0,9 (Erfolgschance beim Abbauen mit Schraubenschlüssel). Ohne Wrench droppt der Block **immer einen
einfachen Generator** (Meta 0) – Ausnahme Wassermühle (droppt sich selbst). Inventar wird gedroppt.
Platzierung: Front zeigt zum Spieler (nur horizontal).

Sounds (Loop während aktiv): Generator `Generators/GeneratorLoop.ogg`, Geo `GeothermalLoop.ogg`,
Wasser `WatermillLoop.ogg`, Wind `WindGenLoop.ogg`.

---

## Generator (Meta 0)

| Wert | |
|---|---|
| Produktion | 10 EU/t (`energyGeneratorBase`) |
| Interner Puffer | 4000 EU |
| Slots | 0 = Lade-Slot (Items Tier 1), 1 = Brennstoff |
| Max. Ausgabe | 10 EU/p |

- **Brennstoff**: `fuel += Brennwert / 4` (Ganzzahldivision), wobei Brennwert = `TileEntityIronFurnace.getFuelValueFor`:
  - Flüssigkeitsbehälter mit Lava (über Liquid-Registry, z. B. **Lavazelle**): 2000 → 500 Ticks → **5000 EU**.
  - **Lavaeimer wird explizit abgelehnt.**
  - Gefüllter Brennstoffkanister: `value · 2`.
  - Sonst Vanilla-Ofenbrennwert (`TileEntityFurnace.getItemBurnTime`) inkl. Fuel-Handler.
  - Schrott nur, wenn `enableBurningScrap = true` (Pack: true).
- Nachlegen nur, wenn `fuel ≤ 0` und Platz im Puffer (`storage + 10 ≤ 4000`). Containeritems (z. B. Eimer) werden durch
  ihren Container ersetzt.
- **Abweichende Regel:** `isConverting = fuel > 0` (ignoriert vollen Puffer). Bei vollem Puffer verbrennt der
  Generator also weiter Brennstoff, die Energie über 4000 geht verloren (Kappung in Schritt 3).
- Partikel (Rauch + Flamme) an der Frontseite, wenn aktiv.

### Brennwerte (Ofen-Ticks, Minecraft 1.4.7 + IC2-Fuel-Handler) → EU im Generator

| Item | Ofen-Ticks | Generator-Ticks | EU |
|---|---:|---:|---:|
| Kohle / Holzkohle | 1600 | 400 | 4000 |
| Holz (Stamm), Bretter, Holz-Blöcke (Werkbank, Truhe, Zaun …, Material Holz) | 300 | 75 | 750 |
| Holzstufe | 150 | 37 | 370 |
| Stock | 100 | 25 | 250 |
| Setzling (Vanilla) | 100 | 25 | 250 |
| Holzwerkzeug/-schwert/-hacke | 200 | 50 | 500 |
| Lohenrute | 2400 | 600 | 6000 |
| Lavaeimer | – | abgelehnt | – |
| Lavazelle (IC2) | 2000* | 500 | 5000 |
| Gummibaum-Setzling (IC2) | 80 | 20 | 200 |
| Zuckerrohr (IC2-Handler) | 50 | 12 | 120 |
| Kaktus (IC2-Handler) | 50 | 12 | 120 |
| Schrott (IC2) | 350 | 87 | 870 |
| Schrottbox (IC2) | 3150 | 787 | 7870 |

\* über Liquid-Registry (`getFuelValueFor`). Der IC2-Fuel-Handler meldet für die Lavazelle zusätzlich den
Lavaeimer-Wert (20000) an Vanilla-Öfen.

Abweichung 1.21: Vanilla 1.21 kennt weitere/andere Brennstoffe (Bambus, Kohleblock, Seetang …). Siehe
`docs/abweichungen.md` (Regel: Vanilla-Brennwert von 1.21 für alles, was nicht explizit oben steht).

---

## Geothermiegenerator (Meta 1)

| Wert | |
|---|---|
| Produktion | 20 EU/t (`energyGeneratorGeo`) |
| Interner Puffer | 32 EU (`max(20, 32)`) |
| Lava-Tank | 24 000 mB (Feld `fuel`, 1 mB = 1 Tick Betrieb) |
| Slots | 0 = Lade-Slot, 1 = Lava-Behälter |

- 1 Tick Betrieb verbraucht 1 mB Lava → 1000 mB = 1000 Ticks = **20 000 EU** pro Eimer/Zelle.
- `needsFuel = fuel ≤ 24000` (versucht immer nachzulegen); nachgelegt wird nur bei ≥ 1000 mB freiem Platz:
  - Lavaeimer → +1000, wird zu leerem Eimer.
  - Lavazelle → +1000, Zelle wird **verbraucht** (keine leere Zelle zurück).
  - Andere Lava-Flüssigkeitsbehälter → +1000, Container zurück.
- Flüssigkeits-Tank-Schnittstelle (`ITankContainer`): nimmt Lava von allen Seiten an (bis Tank voll), gibt nichts ab.
- Beim Abbau wird der Tankinhalt als „LiquidSpilledEvent“ gemeldet (verloren).
- Erzeugt nur, wenn `storage + 20 ≤ 32`, d. h. wenn der Puffer abfließt.

---

## Solarpanel (Meta 3)

| Wert | |
|---|---|
| Produktion | 1 EU/t bei Sonne (`energyGeneratorSolar = 100` → `ceil(100/100)`) |
| Interner Puffer | 1 EU |
| Slots | 0 = Lade-Slot |

- Sonnenprüfung alle 128 Ticks (zufälliger Startversatz) und beim Laden, an der Position **über** dem Panel:
  `world.isDaytime() && !provider.hasNoSky && canBlockSeeTheSky(x, y+1, z) && (Biom ist Wüste || (!regen && !gewitter))`.
- Wenn sichtbar: jeden Tick `storage += 1`.
  (Allgemein: `gen = Solar/100`; bei `gen < 1` nur jeden `(100 − Solar)`-ten Tick.)
- Kein Brennstoff.

---

## Wassermühle (Meta 2)

| Wert | |
|---|---|
| Produktion | variabel (s. u.), Puffer 2 EU |
| Wasserspeicher | 2000 (`maxWater`) |
| Slots | 0 = Lade-Slot, 1 = Wasser-Behälter |

**Mit Wasserbehältern** (nur wenn ≥ 500 Platz):
- Wassereimer: `fuel += 500`, `production = 1`, Eimer wird leer → 500 Ticks × 1 = **500 EU**.
- Wasserzelle: `fuel += 500`, `production = 2`, Zelle verbraucht → 500 × 2 = **1000 EU**.
- Andere Wasser-Flüssigkeitsbehälter: +500; mit Containeritem `production = 1`, sonst `2`.

**Passiv** (nur wenn kein Behälter nachgelegt werden kann **und** `fuel ≤ 0`):
- Alle 128 Ticks (Zufallsversatz) Zählung der Wasserblöcke (fließend oder still) im 3×3×3-Würfel um die Mühle
  (max. 26).
- Jeden Tick: `microStorage += wasser · energyGeneratorWater / 100`; `production = microStorage / 100`;
  `microStorage −= production · 100`; ist `production > 0` → `fuel = 1` (ein Tick Erzeugung).
- Ergebnis: durchschnittlich `Wasserblöcke / 100` EU/t (max. 0,26 EU/t).

Original-Eigenheit (übernehmen): Liegt ein **Nicht-Wasser-Item** im Behälter-Slot und ist ≥ 500 Platz frei, wird der
passive Zweig nicht ausgeführt → keine passive Erzeugung.

Ohne Wrench droppt die Wassermühle sich selbst.

---

## Windmühle (Meta 4)

| Wert | |
|---|---|
| Produktion | variabel, Puffer 4 EU, `getMaxEnergyOutput = 10` |
| Slots | 0 = Lade-Slot |

**Globale Windstärke** (`IC2.windStrength`, nur Overworld):
- Start: `10 + random(10)` (10–19) beim Mod-Start.
- Alle 128 Welt-Ticks (Zähler zählt Ticks *aller* Welten; nur Dimension 0 verändert den Wert):
  `up = 10`, `down = 10`; ist Wind > 20: `up −= Wind − 20`; ist Wind < 10: `down −= 10 − Wind`.
  `random(100) ≤ up` → Wind + 1; sonst `random(100) ≤ down` → Wind − 1.

**Pro Windmühle:**
- Alle 128 Ticks (Zufallsversatz):
  - Alle 8 × 128 Ticks: **Hindernisse** zählen = Anzahl Nicht-Luft-Blöcke im Bereich x −4..+4, y −2..+4,
    z −4..+4 um die Mühle, minus 1 (die Mühle selbst).
  - `sub = windStrength · (y − 64 − hindernisse) / 750`. Bei `sub ≤ 0` → keine Erzeugung in diesem Tick.
  - Gewitter: `sub × 1,5`, sonst Regen: `sub × 1,2`.
  - **Überlast:** Ist `sub > 5` und `random(5000) ≤ sub − 5` → Windmühle zerfällt: wird zum einfachen Generator
    (Block-Ersatz), droppt `random(5)` (0–4) Eisenbarren, `sub = 0`.
  - `sub = sub · energyGeneratorWind / 100`.
- Jeden Tick: `substorage += sub`; `production = (int) substorage`.
  Ist `storage + production ≥ 4` → `substorage = 0` (Energie verloren), keine Erzeugung. Sonst
  `storage += production`, `substorage −= production`.
- GUI zeigt Leistung (`sub/3`) und Überhitzung (`(sub−5)/5`).

Beispiel: y = 100, Wind 15, freie Umgebung: `15 · 36 / 750 = 0,72 EU/t`.

---

## Kernreaktor (Meta 5)

Siehe `06-reaktor.md`. Wrench-Drop-Rate 0,8. Ohne Wrench droppt er einen einfachen Generator.

---

## Eisenofen (zum Vergleich, Brennstofflogik)

Nicht elektrisch, aber Brennwert-Funktion wird vom Generator genutzt; Details in `04-maschinen.md`.
