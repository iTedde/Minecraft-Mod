# 06 – Kernreaktor

Quelle: `ic2.core.block.generator.tileentity.TileEntityNuclearReactor{,Electric}`, `TileEntityReactorChamber{,Electric}`,
`BlockReactorChamber`, `ic2.core.item.reactor.*`, `ic2.api.IReactor`, `ic2.api.IReactorComponent`.
Config: `energyGeneratorNuclear = 5`, `explosionPowerReactorMax = 45`, `enableSteamReactor = false` (nur elektrisch).

## 1. Aufbau

- Reaktor = `blockGenerator` Meta 5 (Härte 3,0). Wrench-Drop-Rate 0,8, ohne Wrench droppt er einen Generator.
- Inventar 54 Slots = Matrix 9 Spalten × 6 Zeilen (`slot = x + y·9`).
- **Reaktorkammern** (`blockReactorChamber`, Härte 2,0): dürfen nur platziert werden, wenn **genau ein** Reaktor
  angrenzt (sonst fallen sie als Item ab). Jede angrenzende Kammer (max. 6) erweitert die nutzbare Breite um eine Spalte:
  `Breite = 3 + Anzahl Kammern` (3…9). Kammern sind Inventar-Proxys auf den Reaktor (Rechtsklick öffnet Reaktor-GUI,
  Automatisierung möglich). Wrench-Drop-Rate 0,8; ohne Wrench Drop = Maschinenblock.
- Slots außerhalb der aktuellen Breite werden beim nächsten Zyklus ausgeworfen; ebenso Items, die keine
  Reaktorkomponente sind (Ausnahme: angereicherte und fast erschöpfte Uranzellen dürfen liegen).
  Inventarzugriffe auf Spalten ≥ Breite werden auf die letzte gültige Spalte umgeleitet.
- Rechtsklick auf den Reaktor mit einer Reaktorkammer in der Hand öffnet keine GUI (Platzieren erlaubt).

## 2. Reaktor-Zyklus

Jeden Tick: **Energieabgabe** des zuletzt berechneten `output`:
`paket = output · energyGeneratorNuclear` (= ×5) wird vom Reaktor emittiert; der nicht abgenommene Rest wird
nacheinander von jeder angrenzenden Kammer (in Richtungsreihenfolge) erneut emittiert (ohne erneute ×5).
`getMaxEnergyOutput`: Reaktor 1512 · 5 = 7560, Kammer 240 · 5 = 1200 (nur für die Pfadsuche-Verlustgrenze).
Nicht abgenommene Energie geht verloren.

Alle **20 Ticks** (Zufallsversatz pro Reaktor) ein Reaktor-Zyklus (nur Server):
1. Sind die Chunks im Umkreis 2 nicht geladen → `output = 0`, Ende.
2. Unpassende Items auswerfen (s. o.).
3. Zurücksetzen: `output = 0`, `maxHeat = 10 000`, `hem` (Heat Effect Modifier) = 1,0.
4. **Komponenten verarbeiten**: Zeilen y = 0..5, darin Spalten x = 0..Breite−1, für jede Komponente
   `processChamber(reactor, stack, x, y)`.
5. **Hitzeeffekte** (s. §4). Explodiert der Reaktor → Ende.
6. Aktiv-Status: `heat ≥ 1000 || output > 0`.
7. `output` an Clients senden (Sound: Geigerzähler-Stufen bei < 40, < 80, ≥ 80).

`produceEnergy()` = Reaktor erhält Redstone-Signal (`isBlockIndirectlyGettingPowered`) **und**
`energyGeneratorNuclear ≠ 0`. Ohne Redstone machen Uranzellen nichts (weder Energie noch Hitze, kein Verschleiß);
alle anderen Komponenten (Vents, Switches …) arbeiten weiter.

Energie: Jeder „Puls“, den eine Komponente annimmt, die Energie erzeugt, ergibt `addOutput(1)` → **5 EU/t**
(da ×5) über 20 Ticks.

## 3. Komponenten

Gemeinsame Schnittstelle (`IReactorComponent`):
- `processChamber` – Aktion pro Zyklus.
- `acceptUraniumPulse(...)` – wird von benachbarten Uranzellen aufgerufen; `true` = Puls angenommen.
- `canStoreHeat`, `getMaxHeat`, `getCurrentHeat`, `alterHeat(heat)` – Rückgabe = nicht aufgenommene Hitze.
- `influenceExplosion` – Beitrag zur Explosionsstärke.

Nachbarn sind immer die 4 orthogonalen Felder (x±1, y±1) innerhalb der Matrix (außerhalb → null).

### 3.1 Uranzellen (`ItemReactorUranium`)

| Item | Zellen (n) | Haltbarkeit | Explosion |
|---|---:|---:|---:|
| Uranzelle (`reactorUraniumSimple`) | 1 | 10 000 Zyklen | +2 |
| Doppel-Uranzelle (`reactorUraniumDual`) | 2 | 10 000 | +4 |
| Vierfach-Uranzelle (`reactorUraniumQuad`) | 4 | 10 000 | +8 |

`processChamber` (nur wenn `produceEnergy()`), **n-mal** wiederholt:
1. `pulses = 1 + n/2` (Ganzzahl: n=1 → 1, n=2 → 2, n=4 → 3); so oft `acceptUraniumPulse` auf sich selbst
   → je `addOutput(1)`.
2. Für jeden der 4 Nachbarn: `acceptUraniumPulse(nachbar)`; jeder angenommene Puls → `pulses += 1`.
3. `heat = sumUp(pulses) · 4` mit `sumUp(p) = 1 + 2 + … + p`.
4. Hitze verteilen: Liste der Nachbarn mit `canStoreHeat`. Solange Liste nicht leer und `heat > 0`:
   `d = heat / listengröße` (Ganzzahl); `heat −= d`; `rest = erster.alterHeat(d)`; `heat += rest`; ersten entfernen.
5. Übrige Hitze → Reaktorhülle (`addHeat`).

Danach Verschleiß: ist `damage ≥ maxDamage − 1` → mit Wahrscheinlichkeit 1/3 wird das Feld durch **n fast erschöpfte
Uranzellen** ersetzt (Stack der Größe n), sonst leer; sonst `damage += 1`.

`acceptUraniumPulse` (als Nachbar): `addOutput(1)`, `true`.

Beispiel Einzelzelle allein: 1 Puls → 5 EU/t, Hitze `sumUp(1)·4 = 4` pro Zyklus.
Zwei benachbarte Einzelzellen: je 2 Pulse → je 10 EU/t, je Hitze `sumUp(2)·4 = 12`.

### 3.2 Kühlzellen (`ItemReactorHeatStorage`)

| Item | Wärmekapazität |
|---|---:|
| 10k-Kühlzelle (`reactorCoolantSimple`) | 10 000 |
| 30k-Kühlzelle (`reactorCoolantTriple`) | 30 000 |
| 60k-Kühlzelle (`reactorCoolantSix`) | 60 000 |

- Hitze im NBT `heat`; Haltbarkeitsbalken `damage = (int)(maxDamage · heat/kapazität)` (max. maxDamage−1),
  maxDamage 10 000.
- `alterHeat(h)`: `neu = heat + h`; `neu > kapazität` → **Komponente zerstört** (Feld leer), Rückgabe
  `kapazität − neu + 1` (negativ!). `neu < 0` → Rückgabe `neu` (negativer Rest), heat = 0. Sonst heat = neu, Rückgabe 0.
- `canStoreHeat = true`. Keine eigene Aktion.

Hinweis Originalverhalten: Der negative Rückgabewert beim Überhitzen wird vom Aufrufer **addiert** (z. B. reduziert er
die Rest-Hitze einer Uranzelle). Exakt übernehmen.

### 3.3 Wärmetauscher (`ItemReactorHeatSwitch`, erbt Kühlzellen-Verhalten)

| Item | Kapazität | Seitentausch (switchSide) | Hüllentausch (switchReactor) |
|---|---:|---:|---:|
| Wärmetauscher (`reactorHeatSwitch`) | 2 500 | 12 | 4 |
| Reaktor-Wärmetauscher (`reactorHeatSwitchCore`) | 5 000 | 0 | 72 |
| Komponenten-Wärmetauscher (`reactorHeatSwitchSpread`) | 5 000 | 36 | 0 |
| Fortg. Wärmetauscher (`reactorHeatSwitchDiamond`) | 10 000 | 24 | 8 |

`processChamber`:
1. `med = eigeneHitze / eigeneKapazität`, `c = 1`.
2. Wenn `switchReactor > 0`: `c += 1`, `med += reactor.heat / reactor.maxHeat`.
3. Wenn `switchSide > 0`: für Nachbarn (Reihenfolge x−1, x+1, y−1, y+1) mit `canStoreHeat`: in Liste aufnehmen und
   `med += cur/max` (bei max ≤ 0: +0).
4. `med /= c + Listengröße`.
5. Für jeden Listen-Nachbarn: `add = (int)(med · max) − cur`, begrenzt auf ±switchSide;
   `myHeat −= add`; `rest = nachbar.alterHeat(add)`; `myHeat += rest`.
6. Wenn `switchReactor > 0`: `add = (int)(med · reactor.maxHeat) − reactor.heat`, begrenzt auf ±switchReactor;
   `myHeat −= add`; `reactor.heat += add`.
7. `this.alterHeat(myHeat)` (kann den Tauscher bei Überhitzung zerstören).

### 3.4 Wärmeableiter / Vents (`ItemReactorVent`, erbt Kühlzellen-Verhalten)

| Item | Kapazität | selfVent (eigene Kühlung/Zyklus) | reactorVent (zieht aus Hülle) |
|---|---:|---:|---:|
| Wärmeableiter (`reactorVent`) | 1 000 | 6 | 0 |
| Reaktor-Wärmeableiter (`reactorVentCore`) | 1 000 | 5 | 5 |
| Vergoldeter Wärmeableiter (`reactorVentGold`) | 1 000 | 20 | 36 |
| Fortg. Wärmeableiter (`reactorVentDiamond`) | 1 000 | 12 | 0 |

`processChamber`:
1. Wenn `reactorVent > 0`: `drain = min(reactor.heat, reactorVent)`; `rest = alterHeat(+drain)`; ist `rest > 0`
   → **Abbruch** (Hülle unverändert, kein Selbstkühlen; Komponente ist dann bereits zerstört). Sonst
   `reactor.heat −= drain`.
2. `alterHeat(−selfVent)`.

### 3.5 Komponenten-Wärmeableiter (`ItemReactorVentSpread`, `reactorVentSpread`)

- Stackgröße 1. Kein eigener Wärmespeicher (`canStoreHeat = false`).
- `processChamber`: jedem der 4 Nachbarn mit `canStoreHeat` `alterHeat(−4)`.

### 3.6 Reaktorplatten (`ItemReactorPlating`)

| Item | +maxHeat | hem-Faktor | Explosion |
|---|---:|---:|---|
| Reaktorplatte (`reactorPlating`) | +1 000 | ×0,95 | Faktor 0,95 |
| Wärmekapazitive Reaktorplatte (`reactorPlatingHeat`) | +2 000 | ×0,99 | Faktor 0,99 |
| Containment-Reaktorplatte (`reactorPlatingExplosive`) | +500 | ×0,90 | Faktor 0,9 |

`processChamber`: `reactor.maxHeat += wert`, `reactor.hem *= faktor`. Keine Hitze speicherbar.

### 3.7 Neutronenreflektoren (`ItemReactorReflector`, Haltbarkeit über NBT `advDmg`)

| Item | Haltbarkeit (Pulse) |
|---|---:|
| Neutronenreflektor (`reactorReflector`) | 10 000 |
| Dicker Neutronenreflektor (`reactorReflectorThick`) | 40 000 |

- `acceptUraniumPulse`: `addOutput(1)`, `advDmg += 1`; bei `advDmg + 1 ≥ max` → zerstört. `true` (reflektiert
  also auch die Hitzeformel-Pulse der Uranzelle).
- Explosion −1 (additiv).

### 3.8 Kondensatoren (`ItemReactorCondensator`, NBT `advDmg`)

| Item | Kapazität (maxDmg) |
|---|---:|
| RSH-Kondensator (`reactorCondensator`) | 20 001 (Parameter 20 000 + 1) |
| LZH-Kondensator (`reactorCondensatorLap`) | 100 001 (100 000 + 1) |

- `canStoreHeat` solange `advDmg + 1 < maxDmg`; `getMaxHeat = maxDmg`; `getCurrentHeat = 0` (!).
- `alterHeat(h)`: nimmt `min(h, maxDmg − (advDmg+1))` auf (`advDmg += …`), Rest zurück. Negative Hitze wird
  „aufgenommen“ (advDmg sinkt; nicht geprüft) – Originalverhalten.
- Kein Selbstkühlen; Reparatur nur per Crafting (`RecipeGradual`): RSH + n Redstone → `advDmg −= 10 000·n`;
  LZH + n Redstone → `−5 000·n`, LZH + n Lapis → `−40 000·n` (Untergrenze 0).

### 3.9 Heizzelle (`ItemReactorHeatpack`, `reactorHeatpack`)

- `maxPer = 1000`, `heatPer = 1`. `processChamber`: für jeden Nachbarn, **nur wenn `reactor.heat < 1000·stack`**,
  und der Nachbar `canStoreHeat`: `add = min(1·stack, 1000·stack − cur)`, wenn > 0 → `alterHeat(add)`.
- Explosion `+stack/10` (Ganzzahldivision → 0 bei < 10).

### 3.10 Depleted Isotope Cell / Wiederanreicherung (`ItemReactorDepletedUranium`, `reactorIsotopeCell`)

- Wird mit Damage 9999 hergestellt (fast erschöpfte Uranzelle + Kohlestaub).
- `acceptUraniumPulse`: `level = damage − 1 − reactor.heat / 3000`; `level ≤ 0` → wird zur **angereicherten
  Uranzelle** (`reEnrichedUraniumCell`); sonst `damage = level`. `true` → zählt als Puls (die Uranzelle erzeugt
  dafür Hitze/Output nach Formel), selbst **kein** `addOutput`.
- Heißer Reaktor beschleunigt das Anreichern (bei 9000 Hitze: −4 pro Puls).

## 4. Hitze und Explosion

Variablen: `heat` (Hülle, int), `maxHeat` (10 000 + Platten), `hem` (Produkt der Plattenfaktoren).

Nach dem Verarbeiten, nur wenn `heat ≥ 4000` und `explosionPowerReactorMax > 0`:
`power = heat / maxHeat`:

| Bedingung | Effekt (je Zyklus) |
|---|---|
| `power ≥ 1,0` | **Explosion** (s. u.) |
| `power ≥ 0,85` | mit Wahrscheinlichkeit `0,2 · hem`: zufälliger Block im Würfel ±2 (nicht Reaktorposition): Luft → Feuer; Block mit Härte ≤ −1 (unzerstörbar): Material Stein/Eisen/Lava/Erde/Ton → fließende Lava (Meta 15), sonst Feuer. (Originalbedingung „Härte ≤ −1“ übernehmen.) |
| `power ≥ 0,7` | alle Lebewesen im Würfel x−3..x+4 (usw.) erhalten `(int)(random(4) · hem)` Strahlungsschaden (ignoriert Rüstung; Hazmat-Vollschutz absorbiert, siehe Rüstung) |
| `power ≥ 0,5` | mit Wahrscheinlichkeit `hem`: zufälliger Block ±2 aus Material Wasser → entfernt |
| `power ≥ 0,4` | mit Wahrscheinlichkeit `hem`: zufälliger Block ±2 aus Holz/Blättern/Wolle → Feuer |

Alle zutreffenden Stufen werden pro Zyklus ausgeführt (von oben nach unten).

**Explosion (`explode`)**:
1. `boomPower = 10`, `boomMod = 1`. Für jede Komponente in der Matrix (Breite): `f = influenceExplosion`;
   `0 < f < 1` → `boomMod *= f`; sonst `boomPower += f`. Alle Matrixfelder werden geleert.
2. `boomPower *= hem · boomMod`; Begrenzung auf `explosionPowerReactorMax` (45).
3. Log-Eintrag „Nuclear Reactor at dim:(x,y,z) melted (explosion power …)“.
4. Alle angrenzenden Reaktorkammern und der Reaktor werden entfernt (keine Drops).
5. `ExplosionIC2(power = boomPower, dropRate 0,01, entityDamage 1,5, DamageSource nuke)` an der Reaktor-Ecke
   (x, y, z – ohne +0,5). Inkl. Nuke-Strahlungseffekt auf Lebewesen im Umkreis 100 (siehe `01-eu-system.md` §4).

## 5. Uran-Kreislauf (Items)

- **Uranerz** droppt **Uranerz-Brocken** (`itemOreUran`, OreDict `dropUranium`) + 1–3 XP.
- Brocken/Erz im Compressor → **Uranbarren**.
- Uranbarren + leere Zelle (formlos) → Uranzelle. 8 leere Zellen um einen Uranbarren → 8 fast erschöpfte Uranzellen.
- Fast erschöpfte Uranzelle + Kohlestaub → Depleted Isotope Cell (Damage 9999) → im Reaktor neben Uranzellen
  anreichern → angereicherte Uranzelle; + Kohlestaub → Uranzelle.
- Rezepte und GregTech-Änderungen: `08-rezepte.md`.

## 6. Kühlmittelzellen als Flüssigkeitscontainer

Kühlzellen sind in der Liquid-Registry als gefüllte Container für die Flüssigkeit „coolant“ registriert:
10k = 1000 mB, 30k = 3000 mB, 60k = 6000 mB (leerer Container: leere Zelle). In 1.21 nur relevant, falls
Fluid-Handling gewünscht ist (siehe Abweichungen).

## 7. Hinweise zur Umsetzung

- Zyklusreihenfolge (Zeilen außen, Spalten innen) und die Reihenfolge der Nachbarn sind für das Ergebnis relevant
  (ganzzahlige Divisionen, Zerstörung während des Zyklus) und müssen exakt übernommen werden.
- Ein Reaktor-Planer/Simulator im Test-Code ist sinnvoll, um bekannte Designs aus dem Original zu vergleichen.
