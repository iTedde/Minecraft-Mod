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
   → Abbruch, sonst `reactor.heat −= drain`. Hinweis: `alterHeat` einer positiven Menge ist nie positiv (beim
   Überhitzen negativ), der Abbruch greift also nie – die Hülle wird auch beim Durchbrennen entleert, und das
   Selbstkühlen läuft auf dem schon entfernten Stack ins Leere.
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

`processChamber`: `reactor.maxHeat += wert`, `reactor.hem *= faktor`. Keine Hitze speicherbar. Platten sind stapelbar
(64); ein Stapel in einem Feld zählt einmal.

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

- Stapelbar (64). `maxPer = 1000`, `heatPer = 1`. `processChamber`: Nachbarn in der Reihenfolge x+1, x−1, y+1, y−1,
  jeweils **nur wenn `reactor.heat < 1000·stack`**,
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

## 5a. Block, GUI, Klang (Ergänzung Phase 7a)

- Reaktor: `blockGenerator` Meta 5, Rarität „uncommon“, Texturen `block_generator.png` Spalte 5 (Zeilen 0–5 inaktiv,
  6–11 aktiv). Ohne Schraubenschlüssel droppt er einen Generator, Schraubenschlüssel-Drop-Rate 0,8.
- Reaktorkammer: `block_0.png` unten 16, oben 17, Seiten 67; Rarität „uncommon“; Härte 2; Drop Maschinenblock.
  Rauch/Flammen (`randomDisplayTick`) wie der Reaktor: `puffs = heat/1000`, davon `rand(puffs)` Rauch bei y+0,95,
  dann `puffs −= rand(4)+3` Flammen bei y+1.
- GUI `GUI6by<Breite>.png` (176 × 222), Slots ab x = 89 − 9·Breite, y = 18, Raster 18; Spielerinventar y 140,
  Hotbar y 198; Titel zentriert y 6, „Inventory“ bei y 128. Keine Anzeigen. Die Breite wird beim Öffnen festgelegt.
- Synchronisiert werden nur `output` (jeder Zyklus, für den Klang) und – nur über die GUI – die Hüllenhitze; Rauch und
  Flammen sieht ein Client deshalb erst, nachdem er die GUI geöffnet hat (Originalverhalten).
- Klang: `NuclearReactorLoop.ogg` bei `output > 0`; Geigerzähler `GeigerLowEU` (< 40), `GeigerMedEU` (< 80),
  `GeigerHighEU` (≥ 80).
- Uranerz `block_0.png` 34, Uranblock (`blockMetal` Meta 3) oben/unten 79, Seiten 95. Items `item_0.png`: Brocken 13,
  Barren 12, Zellen 38/208/209, Kühlzellen 39/210/211, Platten 71/212/213, Tauscher 214–217, Ableiter 218/219/220/221,
  fortg. Ableiter 72, Isotopenzelle 40, angereichert 41, fast erschöpft 42, Heizzelle 222, Reflektoren 223/224,
  Kondensatoren 225/227.
- Uranzelle aus Barren nur im GT-Canner: Barren + leere Zelle, 100 Ticks, **2 EU/t** (`GT_OreDictHandler` 2420).

## 6. Kühlmittelzellen als Flüssigkeitscontainer

Kühlzellen sind in der Liquid-Registry als gefüllte Container für die Flüssigkeit „coolant“ registriert:
10k = 1000 mB, 30k = 3000 mB, 60k = 6000 mB (leerer Container: leere Zelle). Die Registry vergleicht Item **und**
Schaden → nur unbeschädigte (kalte) Zellen zählen. Registrierungsreihenfolge 60k, 30k, 10k: Forge behält für
„Kühlmittel + leere Zelle“ die letzte → Befüllen einer leeren Zelle (z. B. am BC-Tank, ab 1000 mB) ergibt die
10k-Zelle. Flüssigkeitssymbol = `itemCoolant` (`item_0.png` 202, kein Rezept, keine Funktion als Item). Nutzer im Pack:
BC-Verbrennungsmotor (5,0 Hitze/mB, `spec/09`), BC-Tank/-Raffinerie (Ein-/Auslagern). Umgesetzt in 13.2.

## 7. Hinweise zur Umsetzung

- Zyklusreihenfolge (Zeilen außen, Spalten innen) und die Reihenfolge der Nachbarn sind für das Ergebnis relevant
  (ganzzahlige Divisionen, Zerstörung während des Zyklus) und müssen exakt übernommen werden.
- Ein Reaktor-Planer/Simulator im Test-Code ist sinnvoll, um bekannte Designs aus dem Original zu vergleichen.

## 8. GregTech-Reaktorteile (Phase 7b, `gregtechmod.common.items`)

Pack-Config: `Helium2Coolantcell`, `NaKCoolantcell`, `IridiumNeutronReflector`, `BeryliumReflector` = true.
Texturen `items.png` (Index = Item-Nummer), Namen nur Englisch.

| Item | GT-Nr. | Klasse | Werte |
|---|---:|---|---|
| 60k/180k/360k Helium Coolant Cell | 34/35/36 | `GT_CoolantCell_Item` | Kapazität 60 000/180 000/360 000 |
| Iridium Neutron Reflector | 40 | `GT_NeutronReflector_Item` | Haltbarkeit 0 = unendlich |
| Thorium Cell / Double / Quad | 48/49/50 | `GT_RadioactiveCell_Item` | 25 000 Zyklen, Zellen 1/2/4, Pulsrate −5, kein Rest |
| Plutonium Cell / Double / Quad | 51/52/53 | `GT_RadioactiveCell_Item` | 20 000 Zyklen, Zellen 1/2/4, Pulsrate 2, Rest 4 fast erschöpfte Uranzellen |
| 60k/180k/360k NaK Coolantcell | 60–62 | `GT_CoolantCell_Item` | wie Helium (Kapazität 60 000/180 000/360 000, Zellen 1/3/6), Phase 8a |

**Helium-Kühlzelle:** Hitze wie die IC2-Kühlzelle (NBT `heat`, Zerstörung mit negativem Rest), Stack 1, nur **100**
Damage-Stufen für den Balken, Tooltip „Stored Heat: n“, `influenceExplosion = 1 + Kapazität/30000` (wird addiert).
Zurückkühlen nur im Vakuumgefrierer (`13-gregtech-maschinen.md` §11, Phase 8e.4).

**Iridium-Neutronenreflektor:** stapelbar (64), wirkt nur als Einzelstück (`stackSize > 1` → Puls abgelehnt). Ein
Puls wird an die pulsende Komponente zurückgegeben (`pulsing.acceptUraniumPulse(reactor, pulsing, reflektor, …)` →
bei der Uranzelle +1 Output); kein Verschleiß; Explosion −1.

**Thorium-/Plutoniumzelle** (`GT_RadioactiveCell_Item`, Stack 1, NBT `durability`, `pulse`, `output`):
- Nur als Einzelstück und nur mit `produceEnergy()`. Je Zelle (`cellCount`-mal): `1 + cellCount/2` Eigenpulse; dann
  wenn `pulserate ≠ 0` und `outputPulse` (Zähler `output` +1; wahr bei positiver Rate oder `output % 5 == 0`):
  Schleife `i < pulserate || i == 0` – Nachbarn pulsen (Zahl der Pulse **summiert sich** über die Durchläufe),
  Hitze `sumUp(pulse)·4` auf Nachbarn mit Wärmespeicher verteilen wie bei der Uranzelle, Rest in die Hülle.
- `acceptUraniumPulse`: Rate > 0 → `rate`-mal +1 Output; Rate < 0 → Zähler `pulse` +1, Output nur bei
  `pulse % 5 == 0`. Gibt `true` zurück (auch Thorium nimmt also Nachbarpulse an).
- Danach `durability ≥ max − 1` → Feld leer; Plutonium: je Zelle mit 1/4 Wahrscheinlichkeit 4 fast erschöpfte
  Uranzellen (stapelnd); sonst `durability + 1` (Balken = verbrauchter Anteil). Tooltip „Time left: n secs“.
- Explosion: `stackSize · cellCount · max(rate, 1)` (wird addiert).

**Material und Rezepte** (`GT_Mod`, `GT_OreDictHandler`):
- Uranstaub (Dust 16, leuchtet): Macerator Uranerz → 2, Uranblock → 9, Barren → 1; Compressor → Uranbarren;
  Massefabrikator-Verstärker 1 000 000. Thoriumstaub (80) / Plutoniumstaub (81) analog mit Thorium-/Plutoniumbarren
  (Material 38/39), Verstärker 500 000 / 2 000 000. Kleiner Plutoniumstaub (4 ↔ 1), Wolframstaub (Dust 22, 4 kleine ↔ 1).
- Zentrifuge (5 EU/t): fast erschöpfte Uranzelle → Thoriumstaub + leere Zelle (500); 8 angereicherte Uranzellen →
  3 fast erschöpfte, Plutoniumstaub, 4 Thoriumstaub, 5 leere Zellen (20 000); 4 Uranstaub + 4 Zellen → 4 Uranzellen,
  kleiner Plutoniumstaub, 2 Thoriumstaub, Wolframstaub (10 000).
- GT-Canner (100 Ticks, 2 EU/t): Thorium-/Plutoniumbarren + leere Zelle → Thorium-/Plutoniumzelle.
- Doppel-/Vierfachzelle: Thorium `CPC` / ` C /PPP/ C ` mit Kupferplatten, Plutonium dasselbe mit dichten Kupferplatten.
- Helium: 60k ` T /THT/ T ` (H = Heliumzelle, T = Zinnbarren), 180k `TTT/HHH/TTT` (60k), 360k `THT/TCT/THT`
  (180k, C = dichte Kupferplatte); 2 Overclocker ` H /WCW` (H = 60k-Heliumzelle, W = isol. Kupferkabel,
  C = el. Schaltkreis).
- Dicker Neutronenreflektor (ersetzt): ` N /NBN/ N ` (N = Neutronenreflektor, B = Berylliumzelle); Iridium-Reflektor
  `NNN/NIN/NNN` (N = dicker Reflektor, I = Iridiumplatte).
- Berylliumzelle (Cell 10 „Berylium Cell“): Elektrolyseur 29 Smaragdstaub + 18 Zellen → 2 Aluminiumstaub,
  3 Berylliumzellen, 6 Siliziumzellen, 9 Luftzellen (600 Ticks, 50 EU/t).
- Wärmeableiter GT-Variante `AIA/I I/AIA` (A = Aluminiumplatte, I = Eisengitter).

**Phase 8a – NaK-Kühlzellen und Bleiplatte** (`GT_Mod` Z. 3772–3818, 2587, 4598; `GT_OreDictHandler` Z. 1957–2224):
- NaK (Config `NaKCoolantcell`): 60k `TNT/KCK/TNT` **und** `TKT/NCN/TKT` (N = Natriumzelle, K = Kaliumzelle, T = Zinnbarren,
  C = unbenutzte 10k-Kühlzelle), 180k `TTT/HHH/TTT`, 360k `THT/TCT/THT` (C = dichte Kupferplatte), 2 Overclocker ` H /WCW`
  mit 60k-NaK-Zelle. Namen „60k/180k/360k NaK Coolantcell“ (so im Original). Vakuumgefrierer laut Pack-Config 5000/15000/30000 Ticks (Code 500/1500/3000), Phase 8e.4.
- Natrium: Lapis → Zentrifuge → kl. Sodalithstaub (4 → 1 Sodalithstaub, Staub 5) → Elektrolyseur 23 Sodalithstaub +
  8 Zellen → 4 Natrium-, 3 Aluminiumstaub, 3 Silizium-, 1 Chloritzelle (1350 Ticks, 90 EU/t).
- Kalium: Enderperle → Macerator → Enderperlenstaub (Staub 0, „BeK4N5Cl6“, Verstärker 50 000) → Elektrolyseur
  16 Staub + 16 Zellen → 5 Stickstoff-, 1 Beryllium-, 4 Kalium-, 6 Chloritzellen (1300 Ticks, 50 EU/t).
- Blei: 2 Silberstaub → Zentrifuge → kl. Bleistaub (2400) → 4 → Bleistaub (Staub 23); UU-Materie `UUU/UUU/U  ` →
  14 Bleistaub; Bleistaub → Ofen → Bleibarren (Material 23) → Plattenbieger → Bleiplatte (Material 74); Macerator
  Barren/Platte → Staub. Rückweg 2 Bleistaub → Zentrifuge → kl. Silberstaub (SmallDust 246, 4 ↔ 1 Silberstaub).
- Containment-Reaktorplatte zusätzlich formlos aus Reaktorplatte + Bleiplatte (IC2-Rezept bleibt).
- Zellen 13 Chlorit (Cl), 14 Kalium (K), 15 Stickstoff (N). Schrottbox: Silber/Blei 0,5, Schwefel 1,5, Salpeter/Lazurit/
  Pyrit/Sodalith 2,0, Wolfram 0,03 (jetzt vorhanden).
