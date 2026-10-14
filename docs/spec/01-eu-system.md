# 01 – EU-System

Quelle: IC2 1.115.231-lf (`ic2.core.EnergyNet`, `ic2.core.block.wiring.TileEntityCable`, `ic2.api.energy.*`,
`ic2.core.ExplosionIC2`, `ic2.core.item.ElectricItem`). Alle Werte gelten pro Tick (20 t/s).

## 1. Grundbegriffe

- **EU** (Energy Unit) ist ganzzahlig (`int`). Es gibt keine Bruchteile.
- Energie wird in **Paketen** übertragen. Jede Quelle ruft pro Tick ein- oder mehrmals `emit(amount)` auf; jeder
  Aufruf ist ein Paket der Größe `amount`.
- Die **Spannung** eines Pakets ist seine Größe in EU. Überspannung = Paketgröße > maximale Eingangsspannung des Empfängers.

### Spannungsstufen (Tiers)

| Tier | Name | Max. Paket (EU/p) | Typische Quellen/Speicher |
|-----:|------|------------------:|---------------------------|
| 1 | LV (Low Voltage) | 32 | BatBox, LV-Trafo (niedrige Seite), Generatoren |
| 2 | MV (Medium Voltage) | 128 | MFE, LV-Trafo (hohe Seite), MV-Trafo (niedrige Seite) |
| 3 | HV (High Voltage) | 512 | MFSU, MV-Trafo (hohe Seite), HV-Trafo (niedrige Seite) |
| 4 | EV (Extreme Voltage) | 2048 | HV-Trafo (hohe Seite) |

Item-Tiers (Laden/Entladen) nutzen dieselbe Zahl: Ein Item mit Tier *t* kann nur von einem Gerät mit Tier ≥ *t*
geladen/entladen werden (siehe §6).

## 2. Energienetz (Topologie)

Teilnehmer (`IEnergyTile`) melden sich beim Laden an (`EnergyTileLoadEvent`) und beim Entladen/Entfernen ab.
Rollen:

- **Quelle** (`IEnergySource`, Emitter): `getMaxEnergyOutput()`, gibt Pakete ab.
- **Senke** (`IEnergySink`, Acceptor): `demandsEnergy()` (wie viel EU sie noch aufnehmen will),
  `injectEnergy(dir, amount)` → gibt nicht angenommene EU zurück, `getMaxSafeInput()`.
- **Leiter** (`IEnergyConductor` = Acceptor + Emitter): `getConductionLoss()`, `getInsulationEnergyAbsorption()`,
  `getInsulationBreakdownEnergy()`, `getConductorBreakdownEnergy()`, `removeInsulation()`, `removeConductor()`.

Zwei benachbarte Tiles sind verbunden, wenn das sendende `emitsEnergyTo(target, dir)` **und** das empfangende
`acceptsEnergyFrom(emitter, inverseDir)` liefert. Seiten-Codes: 0 = unten, 1 = oben, 2 = Nord (‑Z), 3 = Süd (+Z),
4 = West (‑X), 5 = Ost (+X).

### Pfadsuche (`discover`)

Für jede Quelle wird beim ersten Emit ein Pfad-Cache aufgebaut (Dijkstra-ähnliche Breitensuche):

1. Start bei der Quelle mit Verlust 0.
2. Für jeden gültigen Nachbarn: Ist er ein Leiter, kommt `max(conductionLoss, 0.0001)` hinzu.
   Wird der kumulierte Verlust ≥ `quelle.getMaxEnergyOutput()`, wird dieser Leiter nicht weiterverfolgt.
3. Ein Tile wird (neu) eingetragen, wenn es noch nicht erreicht wurde oder der neue Verlust kleiner ist. Nur Leiter
   werden weiter expandiert; Senken sind Endpunkte (Energie fließt nicht durch Maschinen hindurch).
4. Jede erreichte Senke ergibt einen **Pfad** mit `loss = max(kumulierter Verlust, 0.1)`, der Liste der Leiter auf dem
   Pfad, Bounding-Box der Leiter, sowie Minima von `insulationEnergyAbsorption`, `insulationBreakdownEnergy` und
   `conductorBreakdownEnergy` über alle Leiter des Pfades.

Cache-Invalidierung: Wenn eine Senke oder ein Leiter hinzukommt/entfernt wird, werden die Caches aller Quellen, die
dieses Tile (rückwärts gesucht) erreichen, verworfen. Beim Entfernen einer reinen Senke wird nur deren Pfad aus den
Caches gelöscht. Eine Quelle, die entfernt wird, verliert ihren Cache.

### Verteilung eines Pakets (`emitEnergyFrom(source, amount)`)

1. Aktive Pfade = alle Pfade, deren Senke `demandsEnergy() > 0` hat **und** `pfad.loss < amount`.
2. Reihenfolge zufällig mischen (`Collections.shuffle`). Gibt es mehr aktive Pfade als `amount` EU, werden die
   überzähligen (vom Listenende) entfernt.
3. `totalInvLoss = Σ 1/loss` über aktive Pfade.
4. Schleife, solange aktive Pfade existieren und `amount > 0`:
   - Für jeden Pfad: `provided = floor(round(amount / totalInvLoss / loss · 100000) / 100000)`;
     `lossEU = floor(loss)`.
   - Ist `provided > lossEU`: `returned = senke.injectEnergy(dir, provided − lossEU)`.
     - `returned == 0` und Senke will weiter → Pfad bleibt für die nächste Runde aktiv.
     - `consumed += provided − returned`; `injected(pfad) += provided − lossEU − returned`.
   - Sonst bleibt der Pfad aktiv (bekommt in dieser Runde nichts).
   - Wurde in einer Runde nichts verbraucht, wird der letzte aktive Pfad entfernt (Abbruchgarantie).
   - `amount −= consumed`.
5. Rückgabe: nicht verteilte EU. Die Quelle behält diese (z. B. Speicherblöcke ziehen nur `output − rest` ab;
   Generatoren verwerfen den Rest bzw. behalten ihn im Puffer – siehe jeweilige Spezifikation).

Die Verteilung ist also **umgekehrt proportional zum Pfadverlust**: nähere Senken bekommen mehr. Der Verlust wird
**pro Paket** abgezogen (abgerundet), nicht pro EU.

Beispiel: Generator (10 EU/p) → 5 Blöcke Zinnkabel (5 × 0,025 = 0,125) → Maschine: loss = 0,125 → floor = 0 → 10 EU kommen an.
10 Blöcke isoliertes Kupfer: Die Verluste werden als `double` aufsummiert (0,2 zehnmal = 1,9999999999999998) → floor = 1 → **9 EU** kommen an (Originalverhalten, so übernommen); erst ab 11 Blöcken (2,2) gehen 2 EU verloren.

### Nachbearbeitung je versorgtem Pfad (pro Paket)

Mit `injected` = tatsächlich in die Senke eingespeiste EU dieses Pfades:

1. `totalEnergyConducted += injected` (für EU-Meter/Detektor-Kabel).
2. **Stromschlag** (wenn `injected > minInsulationEnergyAbsorption`): Alle `EntityLiving` in der Pfad-Bounding-Box
   ±1 Block (genauer: Box von min−1 bis max+2). Für jede Entity wird über die Leiter, deren 3×3×3-Umgebung die Entity
   schneidet, das Maximum von `injected − leiter.insulationEnergyAbsorption` gebildet und auf einen
   Schadens-Akkumulator pro Entity addiert. Am Tick-Anfang (Welt-Tick) erhält jede Entity
   `ceil(akku / 64)` = `(akku + 63) / 64` Schaden (DamageSource `electricity`), danach wird der Akku geleert.
3. **Isolationsdurchschlag** (wenn `injected ≥ minInsulationBreakdownEnergy`): `removeInsulation()` auf den betroffenen
   Leitern. In IC2 1.115 ist `insulationBreakdownEnergy` immer 9001 und `removeInsulation()` leer → praktisch wirkungslos.
4. **Kabelschmelze** (wenn `injected ≥ minConductorBreakdownEnergy`): jeder Leiter mit
   `injected ≥ conductorBreakdownEnergy` wird entfernt (Block → Luft, Sound `random.fizz` 0,5 / 2,6±0,4 und
   8 Partikel `largesmoke`). **Kein Drop.**

Wichtig: Schmelzen/Schock werden mit der *nach Verlust* eingespeisten Menge des Pfades in diesem Paket geprüft.

## 3. Kabel

Alle Kabel sind **ein** Block (`blockCable`, Meta = Kabeltyp) mit Tile Entity. Härte 0,2 (Schaum s. u.),
Explosionsresistenz 6,0, abbaubar mit allem; Drop = Kabel-Item gleichen Typs. Kein Wrench-Abbau
(`wrenchCanRemove = false`, Drop-Rate 0).

| Meta | Name (Item-Key) | Verlust EU/Block | Max. Paket (schmilzt ab) | Isolation (Schock ab >) | Dicke (/16) | färbbar |
|----:|---|---:|---:|---:|---:|:-:|
| 0 | Isoliertes Kupferkabel (`itemCable`) | 0,2 | 32 (33) | 32 | 6 | ja |
| 1 | Kupferkabel (`itemCableO`) | 0,3 | 32 (33) | 8 | 4 | nein |
| 2 | Goldkabel (`itemGoldCable`) | 0,5 | 128 (129) | 8 | 3 | nein |
| 3 | Isoliertes Goldkabel (`itemGoldCableI`) | 0,45 | 128 (129) | 32 | 5 | ja |
| 4 | 2× isoliertes Goldkabel (`itemGoldCableII`) | 0,4 | 128 (129) | 128 | 6 | ja |
| 5 | HV-Kabel / Eisen (`itemIronCable`) | 1,0 | 2048 (2049) | 0 | 6 | nein |
| 6 | 1× isoliertes HV-Kabel (`itemIronCableI`) | 0,95 | 2048 (2049) | 128 | 8 | ja |
| 7 | 2× isoliertes HV-Kabel (`itemIronCableII`) | 0,9 | 2048 (2049) | 512 | 10 | ja |
| 8 | 4× isoliertes HV-Kabel (`itemIronCableIIII`) | 0,8 | 2048 (2049) | 9001 | 12 | ja |
| 9 | Glasfaserkabel (`itemGlassCable`) | 0,025 | 512 (513) | 9001 | 4 | ja |
| 10 | Ultra-Low-Current-Kabel / Zinn (`itemTinCable`) | 0,025 | 5 (6) | 3 | 5 | nein |
| 11 | EU-Detektor-Kabel (`itemDetectorCable`) | 0,5 | 2048 (2049) | 9001 | 8 | nein |
| 12 | EU-Splitter-Kabel (`itemSplitterCable`) | 0,5 | 2048 (2049) | 9001 | 8 | nein |

„Max. Paket“: Ein Paket mit `injected ≥ Schmelzwert` zerstört das Kabel. „Isolation“: Ein Paket mit
`injected > Wert` schockt Lebewesen in der Nähe.

### Farben

- Feld `color` (0 = keine Farbe, 1–15 = Farben rot … weiß über den Farbroller/Painter, Farbwert = Farbstoff-Meta). **Originalfehler:** Der schwarze Farbroller hat Farbwert 0 und entfernt damit die Farbe (schwarz lackierte Kabel verbinden sich mit allen).
- Zwei Kabel verbinden sich nur, wenn mindestens eines Farbe 0 hat oder beide dieselbe Farbe haben.
- Nicht färbbar (Typen 1, 2, 5, 10, 11, 12) – außer im geschäumten Zustand, dann wird die Schaumfarbe gesetzt.

### Isolierung ändern (Isolierungsschneider, `ItemToolCutter`, 512 Haltbarkeit)

- **Linksklick** auf Kabel: Isolierung entfernen, droppt 1 Gummi, Schneider −3 Haltbarkeit.
  Übergänge: 0→1, 3→2, 4→3, 6→5, 7→6, 8→7.
- **Rechtsklick** auf Kabel: Isolierung hinzufügen, verbraucht 1 Gummi aus dem Inventar, Schneider −1.
  Übergänge: 1→0, 2→3, 3→4, 5→6, 6→7, 7→8. Ohne Gummi wird die Änderung zurückgenommen.
- Typwechsel meldet das Kabel ab und wieder an (Cache-Invalidierung).

### Bauschaum (Construction Foam) auf Kabeln

Zustand `foamed`: 0 = normal, 1 = nasser Schaum, 2 = ausgehärteter Schaum (sieht aus wie Wand, volle Blockgröße).

- Nasser Schaum: Schaumfarbe 7 (hellgrau). Jeden Tick: mit Wahrscheinlichkeit 1/500 und wenn
  `Lichtlevel·6 ≥ random(1000)` härtet er aus. Rechtsklick mit **Sand** härtet sofort aus (verbraucht 1 Sand).
- Härte: nass 0,01; ausgehärtet 3,0. Explosionsresistenz ausgehärtet 90, sonst 6.
- Abbauen eines geschäumten Kabels entfernt nur den Schaum (Kabel bleibt).

### Detektor- und Splitter-Kabel

- **Detektor** (11): alle 20 Ticks wird `totalEnergyEmitted` (über das Kabel geflossene EU) verglichen; ist der Wert
  gestiegen → aktiv → gibt schwaches Redstone-Signal aus; sonst inaktiv.
- **Splitter** (12): alle 20 Ticks: Bekommt der Block Redstone (`isBlockGettingPowered`), meldet er sich vom Netz ab
  (leitet nicht). Ohne Redstone ist er im Netz.

### Platzierung

Kabel-Item setzt den Kabelblock wie ein normales Block-Item (Schnee/Ranken/Gras werden ersetzt), verbraucht 1 Item
(nicht im Kreativmodus). Kollisionsbox richtet sich nach der Dicke.

## 4. Überspannung und Explosionen

### Maschinen und Speicher

Jede Senke prüft in `injectEnergy`: `amount > maxSafeInput` → **Explosion** an der Position (`IC2.explodeMachineAt`):

1. Block wird sofort entfernt (ohne Drops des Blocks selbst; Inventar geht verloren).
2. `ExplosionIC2(power = 2,5, dropRate = 0,75, entityDamage = 0,75)` am Blockmittelpunkt.

`maxSafeInput` je Gerät: siehe Maschinen-/Speicher-/Generator-Spezifikationen (Standard-Maschine 32, MFE 128 …).
Transformatoren und Überspannung: siehe `03-speicher-trafos.md`.

### Explosionsalgorithmus `ExplosionIC2` (auch für Reaktor, Nuke, ITNT)

Parameter: `power`, `dropRate`, `entityDamage` (unbenutzt außer Speicherung), DamageSource, optional Zünder-Name.

1. `maxDistance = power / 0,4`. Betroffene Entities (Living + Items) im Würfel ±maxDistance werden nach quadrierter
   Distanz sortiert.
2. Strahlen: `steps = ceil(π / atan(1 / maxDistance))`; für `phi_n ∈ [0, 2·steps)`, `theta_n ∈ [0, steps)`:
   `phi = 2π/steps · phi_n`, `theta = π/steps · theta_n`. Strahl mit Startkraft `power`. Nur Strahlen mit
   `phi_n % 8 == 0 && theta_n % 8 == 0` können Entities treffen (falls Entities in Reichweite).
3. Pro Strahlschritt (Schrittweite 1 Block entlang Richtungsvektor):
   - `absorption = 0,5`; ist dort ein Block: `+ (explosionResistance + 4) · 0,3`.
   - `absorption > 1000` und Block **nicht** auf der Whitelist → Block wird ignoriert (`absorption = 0,5`),
     d. h. extrem resistente Blöcke (Bedrock, …) werden durchdrungen aber nicht zerstört.
     Whitelist (werden normal behandelt, also nicht zerstört): Bedrock.
   - Sonst: `absorption > restkraft` → Strahl endet. Ansonsten wird der Block zum Zerstören markiert; bei
     `restkraft > 8` als „ohne Drop/Effekt“ (false), sonst (wenn noch nicht markiert) als „mit Drop/Effekt“ (true).
   - Entity-Treffer (bei markierten Strahlen, Schritte 4, 12, 20, …, Restkraft ≥ 0,25): alle Entities im Abstand
     ≤ 5 vom Strahlpunkt erhalten `(int)(32 · kraft / 8)` Schaden und Rückstoß `0,7 · kraft / 8` vom Zentrum weg.
   - Ist `absorption > 10`: 5 Sekundärstrahlen in zufällige Richtungen mit Kraft `absorption · 0,4` (ohne Entity-Schaden).
   - `restkraft −= absorption`; Ende bei y ≤ 0 oder y ≥ Welthöhe.
4. Bei DamageSource `nuke`: alle Lebewesen im Würfel ±100 ohne vollständige Hazmat-Rüstung erhalten
   Hunger (`120·(100 − d)` Ticks) und Radioaktivität (`80·(30 − d)` Ticks), wenn die Dauer ≥ 0 ist.
5. Zerstörte Blöcke: Für „mit Drop“-Blöcke werden die normalen Drops mit Wahrscheinlichkeit `dropRate` je Stack
   gesammelt, in 2×2-Säulen (x/2, z/2) gebündelt und als Stacks ≤ 64 auf der höchsten Y der Säule +0,5 gespawnt
   (Pickup-Delay 10). Alle markierten Blöcke werden entfernt und `onBlockDestroyedByExplosion` aufgerufen.

Von IC2 erhöhte Vanilla-Explosionsresistenzen: Obsidian, Zaubertisch, Endertruhe, Amboss = 60;
Wasser (fließend/still), Lava (still) = 30.

## 5. Elektrische Items (Ladung)

Ladung steht im NBT-Feld `charge` (int). Jedes elektrische Item hat `maxCharge`, `transferLimit` (EU pro Tick bei
normalem Laden), `tier`, `canProvideEnergy` (dient als Batterie für Maschinen/Speicher), optional getrennte
„leere“/„geladene“ Item-IDs.

- `charge(stack, amount, tier, ignoreLimit, simulate)`: nur wenn Stackgröße ≤ 1 und `item.tier ≤ tier`.
  Begrenzt auf `transferLimit` (falls nicht ignoriert) und Restkapazität.
- `discharge(...)`: analog, begrenzt auf vorhandene Ladung.
- Haltbarkeitsbalken: `damage = 1 + (max − charge) · (maxDamage − 2) / max` (bei maxDamage > 2), Standard-maxDamage 27.
- `use(stack, amount, player)`: zuerst Laden aus Rüstung (`chargeFromArmor`), dann nur wenn volle Menge vorhanden
  abziehen, danach erneut aus Rüstung nachladen.
- `chargeFromArmor`: Jedes Rüstungsteil mit `canProvideEnergy` und Tier ≥ Item-Tier füllt das Item maximal auf
  (ohne Transferlimit). Betrifft nur BatPack und LapPack (die einzigen Rüstungsteile mit `canProvideEnergy = true`).
- Crafting (`AdvRecipe`): Wird ein elektrisches Item als Zutat verwendet (Item-ID-Vergleich, Meta egal), wird seine
  gesamte Ladung auf das Ergebnis übertragen, sofern das Ergebnis elektrisch ist.

Ladestationen (BatBox/MFE/MFSU, Maschinen) laden/entladen mit ihrem eigenen Tier (siehe jeweilige Spezifikation).

## 6. Nicht-elektrische Energiequellen in Batterie-Slots

- **Redstone-Staub** im Batterie-Slot:
  - Standard-Elektromaschine (`TileEntityElectricMachine`): +`Grundverbrauch × Grunddauer` EU (genau ein Vorgang).
  - Andere `TileEntityElecMachine`: +`maxEnergy` EU.
  - Speicherblock: +500 EU (nur wenn es komplett hineinpasst).
- **Single-Use-Batterie (SU-Battery)**: +1000 EU (Maschine immer; Speicherblock nur wenn es komplett passt).

## 7. Offene Punkte für die Umsetzung

- Das Paketmodell inkl. Verlust pro Paket, zufälliger Verteilung und Schmelzen muss exakt nachgebaut werden.
  Ein eigenes Energienetz (kein NeoForge `IEnergyStorage`/FE als Kern) ist nötig, da FE keine Spannung kennt.
- Stromschlagschaden wird am Anfang des nächsten Welt-Ticks angewendet.
