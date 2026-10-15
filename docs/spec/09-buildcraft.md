# 09 – BuildCraft (MJ, Engines, Landmarks, Quarry, Pumpe, Tank, Raffinerie)

Quelle: BuildCraft **3.4.3** (Pack-Jar, dekompiliert unter `reference/decompiled/buildcraft/`). Ergänzend darf der
offizielle Quellcode `BuildCraft/BuildCraft` (Tag 3.4.3) genutzt werden. Pack-Config: `reference/config/buildcraft/main.conf`
(`power.framework = buildcraft.energy.PneumaticPowerFramework`, `modifyWorld = true`, `mining.enabled = true`,
`itemLifespan = 1200`, `current.continuous = false`).

Umfang laut Projekt: MJ-System, Redstone-/Stirling-/Combustion-Engine, Landmark, Quarry inkl. Rahmen, Bohrarm-Animation,
Öl (Weltgen + Eimer), Pumpe, Tank, Raffinerie, Mining Well (§7) und die Zahnräder/Schraubenschlüssel. Rohre optional
(spätere Phase).
MJ und EU sind getrennte Systeme.

## 1. MJ-System

### 1.1 PowerProvider (Empfänger-Puffer)

Jeder MJ-Empfänger (`IPowerReceptor`) besitzt einen `PowerProvider` mit:
`latency`, `minEnergyReceived`, `maxEnergyReceived`, `minActivationEnergy`, `maxEnergyStored`, `energyStored` (float),
`powerLoss = 1`, `powerLossRegularity = 100`.

- Pneumatic-Framework (Pack): `latency` wird immer auf **0** gesetzt → `doWork()` wird **jeden Tick** aufgerufen, wenn
  `energyStored ≥ minActivationEnergy`.
- **Verlust**: alle 100 Welt-Ticks −1 MJ (nicht unter 0) – für jeden Empfänger.
- `receiveEnergy(q, seite)`: `energyStored += q`, gekappt bei `maxEnergyStored`; markiert die Seite 2 Ticks lang als
  Stromquelle.
- `useEnergy(min, max, doUse)`: Ist `stored ≥ min`: liefert `min(stored, max)` und zieht ab; sonst 0.
- Update erfolgt in `TileBuildCraft.updateEntity` jedes Ticks.

### 1.2 Engines (gemeinsam, `TileEngine` + `Engine`)

Block `engine` (Härte 0,5), Meta 0 = Redstone-Engine (Holz), 1 = Stirling-Engine (Stein), 2 = Combustion-Engine (Eisen).

Felder: `energy` (float, interner Speicher), `maxEnergy`, `maxEnergyExtracted`, `orientation` (Ausgabeseite),
`progress` (Kolben 0..1), `energyStage` (Blue/Green/Yellow/Red/Explosion).

Pro Tick (Server):
1. `engine.update()`: **ohne Redstone-Signal** verliert die Engine 1 MJ/Tick (`energy −= 1`, min 0).
2. Kolben-Logik:
   - Läuft ein Hub (`progressPart ≠ 0`): `progress += pistonSpeed`. Beim Überschreiten von 0,5 (einmal pro Hub) wird
     in den Block in Ausgaberichtung Energie übertragen: `extract = engine.extractEnergy(empf.minEnergyReceived,
     empf.maxEnergyReceived)` mit `actualMax = min(max, maxEnergyExtracted)`; ist `actualMax < min` oder
     `energy < min` → 0. Sonst `min(energy, actualMax)` → `empf.receiveEnergy(extract)`. Bei `progress ≥ 1` Hub-Ende.
   - Kein Hub aktiv: Wenn Redstone-Signal **und** `engine.isActive()` und das Ziel ein MJ-Empfänger ist und eine
     Entnahme möglich wäre → neuer Hub startet (Engine „aktiv“).
3. `engine.burn()` (Brennstofflogik, s. u.).
4. Gültige Ziele: TileEntity mit `IPowerReceptor`, dessen Provider direkt von `PowerProvider` erbt.

Energie-Stufe (Farbe, Kolbengeschwindigkeit): nach `energy/maxEnergy` (Stirling/Redstone) bzw. `heat/MAX_HEAT`
(Combustion): ≤ 25 % Blau, ≤ 50 % Grün, ≤ 75 % Gelb, ≤ 100 % Rot, darüber **Explosion**.

`addEnergy(x)`: `energy += x`; ist die Stufe danach „Explosion“ → `world.createExplosion(null, x, y, z,
explosionRange, true)` (Vanilla-Explosion mit Feuer); danach Kappung auf `maxEnergy`.

Engines nehmen selbst MJ an (Ketten): Provider `configure(0, minEnergyReceived, maxEnergyReceived, 1, maxEnergy)`;
`doWork()` übernimmt `useEnergy(1, maxEnergyReceived) · 0,95` per `addEnergy` (5 % Verlust pro Engine in der Kette).

Ausrichtung: Beim Platzieren und mit BC-Schraubenschlüssel (`switchOrientation`) wird die nächste Seite gewählt, an der
ein MJ-Empfänger sitzt (zyklisch ab aktueller Seite).

| | Redstone-Engine | Stirling-Engine | Combustion-Engine |
|---|---:|---:|---:|
| maxEnergy (MJ) | 1 000 | 10 000 | 100 000 |
| maxEnergyExtracted (MJ pro Hub) | 1 | 100 | 500 |
| minEnergyReceived / maxEnergyReceived (Ketten-Eingang) | 1 / 50 | 2 / 200 | 2 / 2 000 |
| explosionRange | 1 | 4 | 8 |
| Kolbengeschw. Blau/Grün/Gelb/Rot | 0,01/0,02/0,04/0,08 | 0,02/0,04/0,08/0,16 | 0,04/0,05/0,06/0,07 |
| Erzeugung | +1 MJ alle 20 Welt-Ticks bei Redstone (= 0,05 MJ/t) | +1 MJ/Tick während Brenndauer | s. u. |

**Redstone-Engine**: erzeugt nur bei Redstone-Signal, `energy++` direkt (keine Explosion möglich). Da
`maxEnergyExtracted = 1`, kann sie keine Empfänger mit `minEnergyReceived > 1` versorgen (z. B. Quarry: 25/30).
Eigenheit: Die Redstone-Engine erhöht `energy` ungekappt. Läuft sie ohne Empfänger lange mit Signal, steigt die Energie über 1000 → Stufe „Explosion“ → Kolbengeschwindigkeit 0; ein später begonnener Hub bleibt dann hängen (keine Explosion). Bleibt 1:1.

**Stirling-Engine**: 1 Slot Brennstoff. Brennt nur mit Redstone-Signal: neuer Brennstoff wird genommen, wenn
`burnTime == 0` und Redstone an (Vanilla-Ofenbrennwert in Ticks, `Utils.consumeItem`). Pro Brenn-Tick `addEnergy(1)`.
Explosion, wenn sie voll ist (10 000) und weiter brennt (10 000 + 1 > 100 %). Hitze-Anzeige = gespeicherte Energie.
Beispiel: Kohle 1600 Ticks → 1600 MJ.

**Combustion-Engine**: Tanks Brennstoff 10 000 mB (oben) und Kühlmittel 10 000 mB (unten); 1 Item-Slot (Behälter werden
eingefüllt, Eis wird ab Hitze > 49 000 wie Wassereimer behandelt).
- Brennen (nur Redstone und `penaltyCooling ≤ 0`): pro mB Brennstoff `burnTime = totalBurningTime/1000` Ticks; pro Tick
  `addEnergy(powerPerCycle)` und `heat += powerPerCycle`.
- Wird Redstone weggenommen, während sie lief → `penaltyCooling = 600`; Abkühlstrafe zählt erst herunter, wenn
  `heat == 0`.
- Kühlung: über `COOLANT_THRESHOLD = 49 000` wird Kühlmittel verbraucht (`coolingPerUnit` je mB) bis die Hitze wieder
  49 000 ist. Ohne Strom/bei Strafe −10 Hitze/Tick.
- Stufe nach Hitze (`MAX_HEAT = 100 000`): über 100 000 → Explosion bei der nächsten Energiezufuhr.

| Brennstoff | MJ/Tick | Ticks pro Eimer (1000 mB) | MJ pro Eimer |
|---|---:|---:|---:|
| Lava | 1 | 20 000 | 20 000 |
| Öl | 3 | 20 000 | 60 000 |
| Treibstoff (Fuel) | 6 | 100 000 | 600 000 |

Kühlmittel: Wasser (1,0 Hitze pro mB). (Andere Mods registrieren weitere – außer Umfang.) Behälter im Slot: Eimer (→ leerer Eimer) und die IC2-Wasser-/Lavazelle (1000 mB, wird **ohne** leere Zelle verbraucht, da BC `consumeItem` nur Container-Items zurückgibt).
Eigenheiten (1:1): Beim Abbau der Combustion-Engine gehen Slot-Inhalt und Tanks verloren (`EngineIron.delete` droppt nichts). Der gewählte Brennstoff (`currentFuel`) wird erst zurückgesetzt, wenn Tank und Brennzeit leer sind. Der Puffer (`PowerProvider`) der Engines wird nicht gespeichert.

Treibstoff entsteht nur in der **Raffinerie** (Öl → Treibstoff, 12 MJ, 1:1), siehe §7.3.

## 2. Öl

- Flüssigkeit „Oil“ (Blöcke `oilStill`/`oilMoving`, Material Wasser), Öleimer (`bucketOil`), Treibstoff „Fuel“ und
  Treibstoffeimer (`bucketFuel`, Stack 1).
- Fließen wie Wasser in 1.4.7 (alle 5 Ticks, 7 Blöcke weit, Gefälle-Suche 4), aber **ohne** neue Quellen (`BlockOilFlowing`
  zählt benachbarte Quellen nicht). Öleimer Stack 1, mit leerem Eimer nur Quellblöcke aufnehmbar (`OilBucketHandler`).
  Treibstoff hat keinen Block; `bucketFuel` lässt sich nicht ausleeren.
- Lava neben Öl erstarrt (Material.water): Quelle → Obsidian, Stufe 1–4 → Bruchstein.
- Weltgenerierung: siehe `07-weltgenerierung.md` §5.

## 3. Landmarks (`markerBlock`)

- Fackelartiger Block (an Flächen platzierbar). Reichweite **64** Blöcke je Achse.
- Rechtsklick: Verbindungsversuch auf allen drei Achsen, die noch nicht belegt sind: entlang ±X, ±Y, ±Z wird der
  nächste Landmark im Abstand 1..63 gesucht (abwechselnd + und −) und verknüpft (wenn dieser nicht anderweitig
  gebunden ist).
- Mit Redstone-Signal zeigt der Landmark blaue Signallaser (63 Blöcke) auf noch nicht verknüpften Achsen.
- Verknüpfte Landmarks spannen eine rote Laserbox auf (Origin + bis zu 3 Achsen). Die Box ist ein `IAreaProvider`.
- Wird die Box von einer Maschine (Quarry/Filler/Builder) übernommen (`removeFromWorld`), werden alle beteiligten
  Landmarks entfernt und als Item gedroppt.

## 4. Quarry

Block `quarry` (Material Eisen, Härte 1,5). Platzierung: Front zeigt zum Spieler (2D), Meta = Gegenrichtung.

### 4.1 Arbeitsbereich

- Beim Initialisieren: Chunk-Ticket anfordern (Forge-Chunkloading). Ist kein Ticket verfügbar → Quarry arbeitet nicht,
  Chatnachricht „[BUILDCRAFT] The quarry at … will not work because there are no more chunkloaders available“.
- Angrenzender `IAreaProvider` (Landmark-Box; Reihenfolge +X, −X, +Z, −Z, +Y, −Y). Sonst Standardbereich.
- Prüfung: `xSize < 3 || zSize < 3 || (xSize·zSize) >> 8 ≥ maxChunkListDepth` → Meldung „Quarry size is outside of
  chunkloading bounds or too small x z (n)“ und Standardbereich. Pack: `maximumChunksPerTicket = 25` → Fläche < 6400
  (z. B. max. 79×79; 64×64 geht).
- Höhe mindestens 5 (`ySize < 5` → 5).
- **Standardbereich**: 11×5×11 (Innenraum 9×9), hinter der Quarry (entgegen der Front): Quarry sitzt mittig an der
  Kante (z. B. Front Nord → `xMin = x−5`, `zMin = z−11`).
- Gechunkloadet werden der Quarry-Chunk und alle Chunks des Bereichs (Chatmeldung mit Anzahl).
- Landmarks werden beim Übernehmen entfernt (droppen).

### 4.2 Rahmenbau

- Blueprint: Rahmenblöcke (`frameBlock`) an den 12 Kanten der Box (unten, oben, 4 Säulen); Inneres = Luft.
- Phase „Bauen“ (`buildFrame`): Provider `configure(20, 25, 100, 25, 15000)`. Je erfolgreichem `useEnergy(25, 25)` wird der
  Bau-Roboter (`EntityRobot`, schwebt mit Laser durch die Box) beauftragt, den nächsten Blueprint-Block zu setzen
  (max. 20 Aufträge in der Warteschlange; Wartezeit pro Auftrag `20 − Anzahl + 2` Ticks).
- Störende Blöcke im Blueprint werden entfernt (ClearIfInvalid), Rahmen ohne Materialkosten gesetzt.
- Gelbe/schwarze Streifen-Laser zeigen den Bereich während des Baus.
- Rahmen (Härte 0,5) droppen nichts. Beim Abbau der Quarry werden alle Rahmen markiert (Meta 1) und zerfallen per
  Random-Tick (Chance 4/10 je Tick-Prüfung).

### 4.3 Abbau

Nach dem Rahmen wird der **Bohrarm** (`EntityMechanicalArm`, 3 Balken + Kopf, noclip) erzeugt.

Pro Tick:
- Bewegt sich der Kopf (`inProcess`): `e = 2 + stored/500`; `used = useEnergy(e, e)`; wenn > 0 →
  `moveHead(0,1 + used/200)` Blöcke/Tick. Horizontal entlang des Winkels zum Ziel, vertikal getrennt. Sind beide Achsen
  innerhalb `2·speed`, wird die Position gesetzt und `positionReached()` aufgerufen.
- Steht der Kopf: `dig()`: Provider `configure(20, 30, 500, 50, 15000)`; benötigt **60 MJ** (`useEnergy(60, 60)`) zum
  Start der nächsten Bewegung. Ziel bestimmen; gibt es keins mehr → Kopf fährt zur Startecke, Quarry fertig.
- `positionReached()`: Block unter dem Kopf (Ziel-y − 1) wird abgebaut, wenn abbaubar (kein Bedrock/unzerstörbar
  (Härte < 0), keine Lava, kein Öl, kein „weicher“ Block). Drops: `getBlockDropped(…, fortune 0)` (kein Silk Touch).
  Zusätzlich werden alle Item-Entities im Würfel ±2..3 um den Kopf eingesammelt.
- Ausgabe jedes Stacks: zuerst in ein **zufälliges angrenzendes Inventar** der Quarry, Rest in ein zufälliges
  angrenzendes Rohr, Rest wird über der Quarry ausgeworfen (motionY ≈ 1, Lebensdauer 1200 Ticks).
- Sound/Partikel: Block-Abbau-Effekt (2001).

**Weiche Blöcke** (werden übersprungen/zählen als frei): Luft, alle Flüssigkeiten (`BlockFluid`), alle Pflanzen
(`IPlantable`), Schneeschicht, Ranken.

**Abbaureihenfolge** (`createColumnVisitList`): Ebenen von `y_quarry + 3` abwärts bis 0. Innenraum
(`sizeX−2 × sizeZ−2`). Serpentinenmuster: gerade Ebenen X aufsteigend, ungerade absteigend; Z-Richtung wechselt je nach
`x % 2 == y % 2`. Säulen mit nicht änderbarem Block (Bedrock, Lava, Öl, unzerstörbar) werden ab dort gesperrt (darunter
wird nicht mehr gegraben). Die Liste wird in Portionen von max. `2·sizeX·sizeZ` Einträgen aufgebaut. Vor jedem Ziel wird
geprüft, dass darüber (bis `y_quarry + 3`) kein fester Block mehr liegt; sonst wird die Liste neu aufgebaut.

**Energiebilanz (Richtwert)**: Pro Block 60 MJ Start + Bewegungsenergie (`2 + stored/500` MJ pro Tick der Bewegung).
Viel gespeicherte Energie → schnellerer, aber teurerer Kopf. Empfang max. 500 MJ pro Engine-Hub, Speicher 15 000 MJ.
Mindestempfang 25 (Bau) bzw. 30 MJ (Abbau) pro Übertragung → Stirling/Combustion nötig.

Schraubenschlüssel (BC) auf Quarry: `reinitalize()` – Rahmen wird neu geprüft/gebaut.

## 5. Weitere Items

| Item | Rezept-Rolle |
|---|---|
| Holz-, Stein-, Eisen-, Gold-, Diamant-Zahnrad | Zutaten |
| BC-Schraubenschlüssel (`wrench`) | Engines drehen, Quarry neu initialisieren |

Rezepte: `08-rezepte.md` §BuildCraft.

## 6. Rohre (optional, spätere Phase)

Nicht spezifiziert in Phase 1 außer: Quarry gibt Items an angrenzende Rohre ab (`Utils.addToRandomPipeEntry`);
Leistungsrohre (Holz/Stein/Gold „Conductive Pipe“) übertragen MJ mit Verlust (`power.usePipeLoss = true`).
Bei Bedarf Spezifikation in der Rohr-Phase aus `reference/decompiled/buildcraft/buildcraft/transport/`.

## 7. Pumpe, Tank, Raffinerie, Mining Well (im Umfang seit Nutzerentscheidung)

Quelle: `buildcraft.factory.TilePump`, `TileTank`, `TileRefinery`, `TileMiningWell`, `api.recipes.RefineryRecipe`.
Pack-Config: `mining.enabled = true`, `consumeWaterSources = true` (Standard).

### 7.1 Tank

- 16 000 mB pro Block. Übereinander gestapelte Tanks bilden einen Verbund: Befüllen beginnt beim **untersten** Tank und
  läuft nach oben; jeder Tank gibt pro Tick Flüssigkeit an den Tank darunter ab. Entnahme immer aus dem untersten.
- Nur eine Flüssigkeitssorte (bestimmt durch den untersten Tank).
- Rezept: `ggg/g g/ggg`, g = Glas.

### 7.2 Pumpe

- MJ-Empfänger: `configure(20, 1, 10, 10, 100)` → min 1, max 10 MJ pro Übertragung, Aktivierung 10, Speicher 100.
- Interner Tank 1000 mB.
- Senkt ein Rohr (Animation, 0,01 Blöcke/Tick) bis zur Flüssigkeitsoberfläche unter sich (`aimY`).
- Ist der Tank leer: nächsten Quellblock pumpen – Kosten **10 MJ** pro 1000 mB; der Quellblock wird entfernt (auch
  Wasser, da `consumeWaterSources = true`).
- Reihenfolge: Flood-Fill ab Rohrposition (gleiche Flüssigkeit, horizontal und nach oben, max. 64 Blöcke Radius
  (`dx²+dz² ≤ 4096`)); es wird immer aus der **obersten** Schicht gepumpt. Alle 100 Ticks Neuberechnung bzw. Rohr
  weiter absenken.
- Gibt den Tankinhalt jeden Tick an angrenzende Flüssigkeitsbehälter (Tanks, Engines, Raffinerie …) ab.
- Rezept: `T/W`, T = Tank, W = Mining Well (Variante mit `mining.enabled = true`).

### 7.3 Raffinerie

- 3 Tanks à 4000 mB: Zutat 1, Zutat 2, Ergebnis. Filter je Zutat-Tank (per GUI); ohne Filter füllt `fill` erst Zutat 1,
  dann 2. Entnahme (`drain`) nur aus dem Ergebnistank.
- MJ-Empfänger: `configure(20, 25, 100, 25, 1000)` → min 25, max 100 MJ pro Übertragung, Speicher 1000.
- Rezept Öl → Treibstoff: 1 mB Öl → 1 mB Treibstoff, **12 MJ**, Verzögerung 1 Tick. Pro Tick (wenn genug Energie
  gespeichert, Zutaten vorhanden, Platz im Ergebnis) wird einmal verarbeitet → max. 1 mB/Tick (= 12 MJ/t),
  1 Eimer Treibstoff = 12 000 MJ und 1000 Ticks.
- Animation: Geschwindigkeit steigt bei Energie, sinkt ohne.
- Rezept: `   /RTR/TGT`, R = Redstone-Fackel, T = Tank, G = Diamant-Zahnrad.

### 7.4 Mining Well

- MJ-Empfänger `configure(50, 1, 25, 25, 1000)`; pro `doWork` (nur bei ≥ 25 MJ) 25 MJ: bohrt den nächsten Block unter
  dem Rohrstrang ab (nicht bei unzerstörbaren Blöcken, Lava, Öl), ersetzt ihn durch ein Rohr (`plainPipe`), Drops wie
  Quarry (Inventar → Rohr → auswerfen).
- Rezept: `ipi/igi/iPi`, i = Eisenbarren, p = Redstone, g = Eisen-Zahnrad, P = Eisenspitzhacke.
- Wird hauptsächlich als Zutat der Pumpe benötigt, ist aber eine eigenständige Maschine.
