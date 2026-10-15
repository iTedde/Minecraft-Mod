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

### 3.1 Details (`BlockMarker`, `TileMarker`)

- Block: Material `circuits`, Härte 0, Lichtwert 0,5 (= 7), keine Kollision, droppt sich selbst. Auswahlbox je
  Ausrichtung 0,3 × 0,3 breit, 0,65 lang ab der Haltefläche.
- Halt nur an Nachbarn mit `renderAsNormalBlock` (volle undurchsichtige Würfel). **Eigenheit:** `onBlockAdded`
  überschreibt die beim Platzieren gewählte Seite mit dem ersten tragfähigen Nachbarn in der Reihenfolge −X, +X, −Z,
  +Z, −Y (Boden), +Y (Decke). Fällt der Halt weg, droppt der Landmark.
- Nachbaränderung → `updateSignals`: `showSignals = isBlockIndirectlyGettingPowered`.
- Gruppe: alle verknüpften Landmarks teilen ein `Origin`-Objekt (`vectO` = Ursprungs-Landmark, `vect[0..2]` = Partner
  auf X/Y/Z). `setVect(n)`: für j = 1..63 erst `+j`, dann `−j` prüfen; erster Landmark, mit dem `linkTo` gelingt,
  gewinnt. `linkTo` scheitert nur, wenn beide schon eine Gruppe haben; hat nur der Partner eine, tritt dieser
  Landmark dessen Gruppe bei und **überschreibt** dort `vect[n]` (Originalverhalten).
- Box: auf jeder verknüpften Achse von Ursprung bis Partner, sonst nur die Ursprungskoordinate. Ohne Gruppe ist der
  `IAreaProvider` der Block selbst (1×1×1).
- Signallaser (blau): je nicht verknüpfter Achse zwei Laser von Blockmitte −63 bis +63. Laser liegen in
  Blockmitte, 0,1 dick, und reichen von Mitte zu Mitte.
- Abbau eines Landmarks löst die ganze Gruppe auf (alle bekommen ein neues, leeres `Origin`).
- Speichern: nur der Ursprungs-Landmark speichert `vectO`/`vect0..2`; beim ersten Tick nach dem Laden verknüpft er
  die Partner neu.

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

### 4.4 Details (`TileQuarry`, `BlockQuarry`, `BlockFrame`, `EntityRobot`, `BptBuilderBlueprint`)

- Quarry-Block: Härte 1,5, Explosionsresistenz wie Stein, Material Eisen (Spitzhacke nötig), Stein-Sound, droppt sich
  selbst. Front = Blockstate-Richtung (zum Spieler; Spielerposition gegen die Blockecke, nicht die Mitte). Textur:
  Front 39, oben 40, sonst 41 (`blocks.png`). Rechtsklick mit BC-Schraubenschlüssel (nicht schleichend) →
  `reinitalize`: `builderDone = false`, Blueprint neu, `isDigging = true`.
- **Ablauf je Tick (Server):** zuerst `PowerProvider.update`. Ist die Quarry nicht `isAlive` (kein Chunkloading),
  passiert sonst nichts. Bewegt sich der Kopf (`inProcess`) → Bewegung (§4.3) und jeden Tick Sync an Clients. Steht
  er und `isDigging`: `createUtilsIfNeeded` (Box/Blueprint anlegen; Rahmen fertig → Arm erzeugen, Kopf ggf. auf
  `(xMin+1, y+2, zMin+1)` setzen; sonst Streifenlaser zeigen), dann `builderDone = blueprint.done`; nicht fertig →
  `buildFrame`, sonst warten bis der Roboter leer ist (dann Roboter + Laser weg) und `dig`.
- **Blueprint:** Größe der Box, aber Basis-y = **y der Quarry** (nicht `box.yMin`; Eigenheit bei Landmarks auf
  anderer Höhe – die Streifenlaser liegen dann an der Landmark-Höhe). Rahmen an den 12 Kanten, Rest Luft.
  - *Räumliste* (alle Positionen, Ebenen von oben nach unten, je Ebene x dann z): Position ungültig (Block/Meta ≠
    Soll) und kein weicher Block → Auftrag „entfernen, wenn ungültig“.
  - *Bauliste* (Ebenen von unten nach oben): Position ungültig → Auftrag „bauen“ (Rahmen setzen bzw. Luft setzen –
    auch weiche Blöcke wie Wasser/Pflanzen werden so entfernt). Der Rahmen ist nicht undurchsichtig → alles in der
    Sekundärliste.
  - `getNextBlock` nimmt aus der ersten nicht leeren Liste den ersten ungültigen Eintrag (gültige werden verworfen);
    sind alle Listen leer → `done`. Jeder Aufruf kostet 25 MJ, auch wenn nichts mehr zu tun ist.
- **Roboter** (`EntityRobot`): Warteschlange max. 20. Pro Tick: `wait` = 0 → `wait = 20 − Anzahl + 2`, sonst
  `wait − 1`; bei `wait ≤ 0` und `canChangeBlock(Ziel)` wird der erste Auftrag ausgeführt und entfernt.
  **Eigenheit:** Ist das Ziel nicht änderbar (Lava, Öl, Bedrock im Rahmenbereich), bleibt der Auftrag ewig stehen –
  der Rahmen wird nie fertig. Darstellung: 8×8×8-px-Würfel (`robot.png`), startet in der Boxmitte, wählt zufällig
  ein weiches Feld im ±1-Würfel um sein Ziel innerhalb der um +1 **verschobenen** Box (`Box.expand` verschiebt),
  Geschwindigkeit `(Ziel − Pos) / 75 · (Leistungsmittel / 2 + 1)`; Leistungsmittel = Mittel über 100 Ticks von
  `Anzahl/20 · 4`. Energielaser (`laser_1..4.png` nach Mittel ≤1/≤2/≤3/>3) vom Roboter zur Mitte des Zielblocks.
- **Rahmen:** Material Glas, Härte 0,5, kein Drop, Random-Tick: Meta 1 → mit `nextInt(10) > 5` (4/10) entfernen.
  Form wie altes Rohr: Kern 0,25–0,75, Arme zu benachbarten Rahmen (Kollision und Auswahl ebenso). Beim Abbau der
  Quarry werden die Rahmen an den 12 Kanten von `box` (y aus der Box!) auf Meta 1 gesetzt.
- **Arm:** Wurzel `(xMin + 0,75, y + sizeY − 1 + 0,25, zMin + 0,75)`, X-Balken Länge `sizeX − 2 + 0,5`, Z-Balken
  `sizeZ − 2 + 0,5`, Querschnitt 0,5; senkrechter Balken vom Kopf + 1 bis zur Wurzel; Kopf 0,2 × 1 × 0,2 bei
  `(kx + 0,4, ky, kz + 0,4)`. Kopfposition auf 1/32 gerastert. Texturen 33 (Balken) und 42 (Kopf).
- **Ausgabe** `addToRandomInventory`: alle 6 Nachbarn mit Inventar außer Engines, die mindestens 1 Item aufnehmen;
  zufällig einer davon bekommt den Stack. Rest → Rohr (Phase 9), Rest → Item-Entity.
- **Gespeichert:** Energie, Box, Ziel, Kopfposition. **Nicht** gespeichert: `builderDone`, `inProcess`, `isDigging`,
  Roboter – nach dem Laden wird der Rahmen geprüft (25 MJ) und das Graben neu begonnen.
- Chunkloading: Forge-Ticket (`maximumChunksPerTicket = 25`, max. 200 Tickets je Mod laut `forgeChunkLoading.cfg`).
  Chatmeldungen nur an den Spieler, der die Quarry gesetzt hat (nur in derselben Sitzung bekannt).
- Rezept: Pack-Rezept braucht den IC2-Diamantbohrer (Titanplatten, Phase 6/6b) → Quarry-Rezept erst mit dem Bohrer.

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

### 7.5 Details (`TileTank`, `BlockTank`, `TilePump`, `BlockPump`, `TileRefinery`, `BlockRefinery`, `GuiRefinery`, `TileMiningWell`, `BlockMiningWell`, `BlockPlainPipe`, `TileMachine`)

**Tank**
- Block: Material Glas, Härte 0,5, Box `0.125–0.875` horizontal, volle Höhe, nicht opak. Texturen (`blocks.png`):
  oben/unten 98, Seiten 96; Seiten 97, wenn darunter ebenfalls ein Tank steht.
- Tick (Server): hat der Tank Inhalt → `moveLiquidBelow`: Tank darunter `fill(gesamter Inhalt)`, dieser Betrag wird
  hier abgezogen (bis zu 16 000 mB pro Tick, nicht gedrosselt). Netzwerkupdate höchstens alle 2 Ticks.
- `fill` (jede Seite): geht an den untersten Tank des Stapels; ist dieser nicht leer und enthält eine andere
  Flüssigkeit → 0. Sonst von unten nach oben auffüllen, bis die Menge verbraucht ist (Tanks weiter oben werden nicht
  auf ihre Sorte geprüft – Originaleigenheit).
- `drain` (jede Seite): immer aus dem untersten Tank (höchstens dessen Inhalt).
- Anzeige/`getTanks`: Verbund = Inhalt des untersten + aller darüber mit gleicher Sorte (leere übersprungen, Abbruch
  bei anderer Sorte), Kapazität = 16 000 × Anzahl der Tanks bis zum Abbruch.
- Rechtsklick mit gefülltem Behälter (Eimer, Zelle …): `fill` mit dessen Inhalt; ist etwas angekommen (auch nur
  teilweise) und nicht Creative → Behälter verbraucht (leerer Behälter zurück). Immer „benutzt“.
  Rechtsklick mit leerem Behälter: Behälter mit dem Verbundinhalt füllen; Stack > 1 → gefüllter Behälter ins Inventar
  (passt nicht → nichts passiert), sonst ersetzt; dann `drain(Behältermenge)` am untersten Tank. Creative: Behälter
  unverändert, Flüssigkeit wird trotzdem bewegt.
- Inhalt bleibt beim Abbau nicht erhalten (Block droppt sich selbst).

**Pumpe**
- Block: Material Eisen, Härte 5, Vollblock. Texturen: unten 100, oben 101, Seiten 99. Rohr (Entity in 1.4.7)
  Textur 102, Querschnitt 0,5 × 0,5 (x+0.25, z+0.25), reicht von `tubeY` bis zur Pumpenunterseite.
- Initialisierung: `tubeY` = Pumpenhöhe, `aimY` = Pumpenhöhe (wenn 0).
- Tick (Server):
  1. Rohr senkt sich, solange `tubeY − aimY > 0,01`: `tubeY −= 0,01`, sonst nichts in diesem Tick (auch keine Abgabe).
  2. Tank leer: nächster Block = **letzter** Eintrag der obersten nicht leeren Schicht. Ist er eine Quelle und passen
     1000 mB in den Tank und liefert `useEnergy(10, 10)` genau 10 → **vordersten** Eintrag entnehmen und diesen Block
     entfernen (Wasser nur bei `consumeWaterSources`), Tank mit der geprüften Flüssigkeit füllen (Originaleigenheit:
     geprüfter und entfernter Block können verschieden sein).
     Kein pumpbarer Block und `worldTime % 100 == 0` → Flood-Fill ab `(x, aimY, z)`; ist danach nichts zu pumpen, von
     `y−1` abwärts suchen: erste Flüssigkeit → `aimY = y`; erster Nicht-Luft-Block → Abbruch.
  3. Abgabe: Tankinhalt nacheinander an die 6 Nachbarn mit Flüssigkeitsbehälter (`fill` von der Gegenseite).
- Flood-Fill: Startblock muss Flüssigkeit sein; gleiche Flüssigkeit (Quelle und fließend), 4 horizontale Nachbarn in
  derselben Schicht und der Block darüber (Schicht `y+1`), `dx²+dz² > 4096` → überspringen. Quellen werden vorne in die
  Schichtliste eingefügt, alle Flüssigkeitsblöcke weiter durchsucht. Bestehende Listen werden ergänzt (Markierungen
  gelten nur innerhalb eines Durchlaufs, Doppeleinträge möglich).
- `fill` immer 0; `drain` aus dem internen Tank (jede Seite).
- MJ-Bedarf (`TileMachine.powerRequest`): nur wenn aktiv (oberste Schicht liefert eine Quelle), dann
  `ceil(min(maxReceived, maxStored − stored))`, sonst 0.
- Abbau: Rohr verschwindet, Liste verfällt; Block droppt sich selbst. NBT: `tank`, `aimY`, `tubeY` (float), Power.

**Raffinerie**
- Block: Material Eisen, Härte 0,5, nicht opak, Modell-Darstellung. Ausrichtung beim Setzen: zum Spieler (4
  Richtungen). BC-Schraubenschlüssel dreht: W→S, E→N, N→W, S→E. Schleichen → keine Aktion.
- Rechtsklick mit gefülltem Behälter: `fill`; ist etwas angekommen und nicht Creative → Behälter verbraucht. Sonst GUI.
- `fill`: ohne Filter erst Zutat 1, Rest in Zutat 2; ist mind. ein Filter gesetzt, nur in die Tanks, deren Filter zur
  Flüssigkeit passt (Zutat 1 zuerst). Die Seite spielt keine Rolle. `drain` nur aus dem Ergebnis.
- Tick (Server): Rezept = erstes passendes für (Zutat 1, Zutat 2). Kein Rezept, Ergebnis mit anderer Sorte oder ohne
  Platz, oder Zutaten fehlen → Animation verlangsamen, inaktiv. Sonst aktiv; Animation beschleunigen, wenn
  `stored ≥ Rezeptenergie`, sonst verlangsamen; sind `delay` Ticks seit dem letzten Versuch vergangen: `useEnergy(E, E)`
  ≠ 0 → Zutaten abziehen, Ergebnis einfüllen.
- Animation: `speed` (Start 1), `stage` (0). Beschleunigen: `speed < 2 → 2`, sonst bis 5 in +0,1-Schritten;
  `stage += speed` (int), `> 300 → 100`. Verlangsamen: `speed ≥ 1 → speed −= 0,1, stage += speed`, sonst `stage −1`
  bis 0. Client: `speed > 1 → stage += speed`, sonst `stage −1`. `speed` wird synchronisiert.
- Rezepte im Pack: Öl 1 → Treibstoff 1, 12 MJ, Verzögerung 1 (BuildCraft); Forestry Biomasse 4 → Biotreibstoff 1,
  10 MJ (entfällt, Forestry-Flüssigkeiten nicht im Umfang).
- GUI `refinery_filter.png` 175 × 207: Filterfelder 38/54 (Zutat 1) und 126/54 (Zutat 2), Ergebnisanzeige 82/54;
  Texte: Titel zentriert y 6, „->“ 63/59, „<-“ 106/59, „Inventar“ 8/(ySize−94); Spielerinventar ab 8/123, Hotbar y 181.
  Filter setzen: mit gefülltem Behälter am Mauszeiger auf ein Filterfeld klicken (Filter lassen sich nicht löschen).
  Ergebnisfeld zeigt das Rezeptergebnis der Filter.
- NBT: `ingredient1`, `ingredient2`, `result`, `animationStage`, `animationSpeed`, Filter, Power.

**Mining Well**
- Block: Material Erde, Härte 1,5, Explosionswiderstand 10, Steinsound. Front zum Spieler (4 Richtungen). Texturen:
  Front 35, oben 36, unten und Rückseite 38, sonst 37; Item: Front Süd.
- `doWork` (Power-Provider-Takt): `useEnergy(25, 25) < 25` → nichts. Sonst unter den Rohrstrang gehen; Weltboden
  nicht unterschritten und `canChangeBlock` → Drops holen, Block durch Rohr ersetzen (auch Luft), Drops wie Quarry
  (zufälliges Nachbarinventar → Rohr → Item mit Aufwärtsimpuls 1,0, Lebensdauer 1200, Aufheben nach 10 Ticks);
  sonst `isDigging = false` (bis zum Neuladen). Die Energie ist in jedem Fall verbraucht.
- Abbau: Rohrstrang darunter wird bis zum ersten Nicht-Rohr entfernt. MJ-Bedarf wie Pumpe, nur solange `isDigging`.

**Rohr (`plainPipe`)**
- Material Glas, Box `0.25–0.75` horizontal, volle Höhe, nicht opak, kein Drop, Textur 32, Härte 0. Nicht im
  Creative-Tab.
