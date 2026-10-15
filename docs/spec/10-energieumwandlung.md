# 10 – Energieumwandlung (Forestry Electrical Engine, Power Converters)

## A. Forestry Electrical Engine (EU → MJ)

Quelle: Forestry **2.0.0.11** (`forestry.energy.gadgets.EngineTin`, `forestry.core.gadgets.Engine`/`TileEngine`,
`forestry.energy.circuits.*`, `forestry.core.circuits.*`, `PluginForestryEnergy/Core/Factory`, `MachineCarpenter`).

### A.1 Grundwerte

| Wert | |
|---|---:|
| maxEnergy (MJ-Puffer) | 10 000 |
| maxEnergyExtracted (MJ pro Hub) | 400 |
| maxHeat | 3 000 |
| EU pro Arbeitstakt (`euForCycle`) | 6 |
| MJ pro Arbeitstakt (`mjPerCycle`) | 2 |
| EU-Speicher (`euStorage`) | 12 |
| Zusatzannahme über Speicher (`euMaxAccept`) | 512 |
| Platinen-Sockel | 1 |
| Batterie-Slot | 1 (elektrische Items mit „liefert Energie“) |

→ Standard: **6 EU → 2 MJ pro Tick** (Verhältnis 3 EU : 1 MJ).

### A.2 Ablauf pro Tick (Server, `TileEngine.updateEntity`)

1. `engine.updateServerSide()`: Temperaturzustand prüfen; ist der Zustand „MELTING“ (heat ≥ maxHeat) und heat > 0 →
   `forceCooldown = true` (Zwangsabkühlung) bis heat = 0. Meldet sich beim ersten Update im IC2-Energienetz an.
   Batterie: liegt ein Item im Slot und ist die Engine aktiviert (Redstone) und `euStored < euStorage`:
   entlädt `min(euStorage − euStored, 3·euForCycle)` EU (Tier ignoriert, ohne Transferlimit).
   Alle 80 Ticks Fehlerstatus (NOFUEL/OK) aktualisieren.
2. Kolben-/Übertragungslogik wie BuildCraft-Engine (siehe `09-buildcraft.md` §1.2): Hub startet nur, wenn
   **aktiviert (Redstone-Signal)** und das Ziel ein MJ-Empfänger ist und Entnahme möglich wäre; bei progress > 0,5
   Übertragung `extractEnergy(min, max)` (Deckel 400).
3. `dissipateHeat()`: Ist heat > 0: −1, wenn nicht brennend oder nicht aktiviert; zusätzlich −1, wenn Zustand
   „OPERATING_TEMPERATURE“ oder „OVERHEATING“.
4. `generateHeat()`: Wenn aktiviert und brennend: +1, und +1 zusätzlich, wenn der MJ-Puffer > 50 % voll ist
   (Deckel maxHeat).
5. Wenn `mayBurn()` (keine Zwangsabkühlung): `burn()`: wenn aktiviert und `euStored ≥ euForCycle` →
   `addEnergy(mjPerCycle)` (Deckel maxEnergy, **keine Explosion**), `euStored −= euForCycle`.
   Sonst (Zwangsabkühlung): `extractEnergy(0, 2)` – vernichtet bis zu 2 MJ/Tick aus dem Puffer.

Temperaturzustände nach `heat/maxHeat`: < 0,2 COOL, < 0,45 WARMED_UP, < 0,65 OPERATING_TEMPERATURE, < 0,85 RUNNING_HOT,
< 1,0 OVERHEATING, sonst MELTING. Kolbengeschwindigkeit 0,03 / 0,04 / 0,05 / 0,06 / 0,07 / 0,08.

### A.3 EU-Seite

- `acceptsEnergyFrom`: alle Seiten. `demandsEnergy = euStorage − euStored`.
- `injectEnergy(amount)`: nimmt `min(euStorage + 512 − 1 − euStored, amount)` an, Rest zurück. **Keine
  Überspannungsprüfung, keine Explosion** (beliebige Paketgröße wird angenommen, soweit Platz).
- Explosion: Forestry-Engines explodieren nicht (Zwangsabkühlung statt Explosion).

### A.4 Upgrades (Electron Tubes auf Platinen)

Eine Platine (Circuit Board) im Sockel der Engine. Platinen haben je nach Typ Sockel für Röhren:
Basic 1, Enhanced 2, Refined 3, Intricate 4. Röhren werden mit dem **Lötkolben** (Soldering Iron, Item-GUI) auf
eine Platine mit Layout „forestry.engine.tin“ gelötet; pro Schaltung gilt ein Limit pro Platine.

| Röhre (tubes Meta) | Schaltung | Änderung EU/Takt | Änderung MJ/Takt | Limit je Platine |
|---|---|---:|---:|---:|
| Kupfer (0) | Electric Choke I | −2 | −1 | 1 |
| Zinn (1) | Electric Boost I | +7 | +2 | 2 |
| Bronze (2) | Electric Boost II | +15 | +4 | 2 |
| Eisen (3) | Electric Efficiency I | −1 | 0 | 1 |

Jede Schaltung ändert zusätzlich `euStorage` um `2 · EU-Änderung`. Beim Einsetzen/Laden wird addiert, beim Entfernen
abgezogen.

Beispiele: Intricate mit 2× Boost II: 6+30 = 36 EU → 2+8 = 10 MJ pro Tick; Speicher 12 + 60 = 72.
Boost I + Efficiency: 12 EU → 4 MJ (3:1); 2× Boost I + Efficiency: 19 EU → 6 MJ.

### A.5 Benötigte Forestry-Gegenstände und Rezepte (Original)

| Item | Original-Herstellung |
|---|---|
| Electrical Engine | Werkbank: `###`/` X `/`YVY`, # = Zinnbarren, X = Glas, Y = Zinn-Zahnrad, V = Kolben |
| Zinn-Zahnrad (`gearTin`) | ` # `/`#Y#`/` # `, # = Zinnbarren, Y = BC-Stein-Zahnrad (mit BuildCraft; ohne: Kupferbarren in der Mitte) |
| Electron Tubes (×4) | **Thermionic Fabricator** (Forestry-Maschine): 500 mB geschmolzenes Glas + Muster ` X `/`#X#`/`XXX`, # = Redstone, X = Kupfer / Zinn / Bronze / Eisen |
| Platinen Basic/Enhanced/Refined/Intricate | **Carpenter** (Forestry-Maschine), je 1000 mB Wasser: Basic `R R`/`R#R`/`R R` (# Zinn, 20 Schritte); Enhanced `R#R`×3 (# Bronze, 40); Refined `R#R`×3 (# Eisen, 80); Intricate `R#R`×3 (# Gold, 80) – siehe §A.6 |
| Lötkolben | **Carpenter**: ` # `/`# #`/`  B`, # = Eisen, B = Bronze, 1000 mB Wasser, 40 Schritte |

Thermionic Fabricator, Carpenter und das Robuste Gehäuse sind seit Nutzerentscheidung **im Umfang** (siehe §A.6);
Röhren, Platinen und Lötkolben werden 1:1 wie im Original hergestellt.

GregTech im Pack ändert an diesen Rezepten nichts (die Config-Schlüssel `circuitry.*` existieren, werden vom
GT-2.90-Code aber nicht gelesen).

### A.6 Thermionic Fabricator, Carpenter, Robustes Gehäuse (im Umfang)

Quelle: `forestry.factory.gadgets.MachineFabricator`, `MachineCarpenter`, `forestry.core.gadgets.TileMachine`,
`forestry.factory.PackagesFactory`. Forestry-Spielmodus im Pack: `EASY` (`energy.demand.modifier = 1.0`).

**Gemeinsame MJ-Logik (`TileMachine`)**: BuildCraft-PowerProvider mit Paket-Konfiguration
`(latency, minEnergyReceived, maxEnergyReceived, minActivationEnergy, maxEnergy)`, Leistungsverlust 1 MJ/100 Ticks.
Alle **5 Welt-Ticks** wird `machine.doWork()` aufgerufen (sofern ≥ Aktivierungsenergie gespeichert); liefert es `true`,
werden **`minActivationEnergy` MJ** verbraucht.

**Robustes Gehäuse (`sturdyCasing`)**: Werkbank `###/# #/###`, # = Bronzebarren.

#### Thermionic Fabricator

- Energie `(10, 5, 110, 25, 250)` → 25 MJ pro Arbeitsschritt (alle 5 Ticks).
- Rezept: `X#X/#Y#/XZX`, X = Goldbarren, # = Glas, Y = Robustes Gehäuse, Z = Truhe.
- Slots: 0 Schmelzgut, 1 Plan, 2 Ergebnis, 3–11 Raster (3×3), 12–29 Vorrat (18). Tank „geschmolzenes Glas“ 2000 mB.
- Hitze 0–5000: jeder Arbeitsschritt +25 (Deckel 5000); pro Tick −2 (über 2500) bzw. −1.
- Schmelzen (pro Tick): Liegt Schmelzgut in Slot 0, ist Platz im Tank und `Hitze ≥ Schmelzpunkt` → 1 Item wird
  eingeschmolzen (Menge wird über die folgenden Ticks in den Tank gefüllt).
  Glas → 1000 mB (Schmelzpunkt 1000), Glasscheibe → 375 mB (1000), Sand → 1000 mB (3000).
- Liegt die Hitze unter dem Schmelzpunkt der Tankflüssigkeit, verliert der Tank **5 mB pro Tick** (erstarrt).
- Herstellen: Ergebnis wird beim Herausnehmen erzeugt; Zutaten zuerst aus dem Vorrat (Slots 12–29), sonst aus dem
  Raster; verbraucht die Rezeptmenge Flüssigglas.
- Röhren-Rezepte (je 500 mB Flüssigglas, ohne Plan, ergeben 4 Röhren): Muster ` X /#X#/XXX`, # = Redstone,
  X = Kupfer (0), Zinn (1), Bronze (2), Eisen (3), Gold (4), weitere Metas für andere Forestry-Schaltungen
  (nur 0–3 werden für die Electrical Engine benötigt; 4+ optional).
- Automatisierung: Schmelzgut von oben in Slot 0, alles andere in den Vorrat; Entnahme liefert das Ergebnis.

#### Carpenter

- Energie `(20, 1, 110, 25, 400)` → 25 MJ pro Arbeitsschritt (alle 5 Ticks).
- Rezept: `X#X/XYX/X#X`, X = Bronzebarren, # = Glas, Y = Robustes Gehäuse.
- Wassertank 10 000 mB; Flüssigkeitsbehälter im Behälterslot werden alle 20 Ticks eingefüllt.
- Rezeptsuche alle 40 Ticks aus Tank + Kistenslot + 3×3-Raster (Raster dient als Muster, Zutaten aus dem Vorrat).
- Arbeit: `packagingTime` = Anzahl Arbeitsschritte (à 5 Ticks und 25 MJ). Am Ende werden Zutaten und Flüssigkeit
  abgezogen und das Produkt ausgegeben.
- Rezepte im Umfang (je 1000 mB Wasser):

| Ergebnis | Muster | Schritte | Ticks | MJ |
|---|---|---:|---:|---:|
| Basic-Platine | `R R/R#R/R R`, # = Zinn, R = Redstone | 20 | 100 | 500 |
| Enhanced-Platine | `R#R/R#R/R#R`, # = Bronze | 40 | 200 | 1000 |
| Refined-Platine | `R#R/R#R/R#R`, # = Eisen | 80 | 400 | 2000 |
| Intricate-Platine | `R#R/R#R/R#R`, # = Gold | 80 | 400 | 2000 |
| Lötkolben | ` # /# #/  B`, # = Eisen, B = Bronze | 40 | 200 | 1000 |

Weitere Carpenter-Rezepte von Forestry (Bienen, Post …) sind nicht im Umfang.

**Lötkolben** (Item, Stack 1, Haltbarkeit 5): Rechtsklick öffnet GUI mit Platinen-Slot, Röhren-Slots (Anzahl =
Sockel der Platine) und Ergebnis; das Layout (hier „forestry.engine.tin“) wird in der GUI gewählt. Pro Röhre wird
die Schaltung eingetragen, sofern ihr Limit pro Platine nicht überschritten ist.

### A.7 Details aus dem Original (nachgetragen für Phase 5a)

Quelle: `MachineFabricator`, `MachineCarpenter`, `ContainerFabricator/Carpenter`, `GuiFabricator/Carpenter`,
`TileMachine`, `BlockMachine`, `RenderMachine`, `RenderBlock`, `ItemSolderingIron`, `ContainerSolderingIron`,
`GuiSolderingIron`, `CircuitBoard`, `ItemCircuitBoard`, `EnumCircuitBoardType`, `PluginForestryCore/Energy/Factory`.

**Arbeitstakt (`TileMachine.doWork`)**: wird vom PowerProvider jeden Tick gerufen, solange ≥ Aktivierungsenergie
gespeichert ist; nur wenn `Weltzeit % 5 == 0` → `machine.doWork()`; bei `true` werden genau 25 MJ abgezogen.
`updateServerSide()` läuft unabhängig davon jeden Tick. Leistungsverlust 1 MJ / 100 Ticks.

**Block (`BlockMachine`)**: Material.rock (Spitzhacke nötig), Härte 1,5 (Explosionswiderstand 1,5). Ausrichtung beim
Platzieren zum Spieler (Blickrichtung 0/1/2/3 → N/O/S/W), nur horizontal; kein Drehen per Schraubenschlüssel.
Schleichen + Rechtsklick öffnet nichts. Abbau droppt das Inventar (`IInventory`): Carpenter Slots 9–29 (nicht das
Muster), Fabricator alle 30 Slots. Tankinhalte gehen verloren.

**Darstellung (TESR)**:
- Carpenter (`RenderMachine`, Textur `carpenter_`): Bodenplatte `addBox(-8,-8,-8, 16,4,16)`, Deckplatte
  `(-8,4,-8, 16,4,16)` (beide `base.png`), Ressourcentank `(-6,-8,-6, 12,16,6)` mit `tank_resource_<stufe>.png`,
  Produkttank `(-6,-8,0, 12,16,6)` immer `tank_product_empty.png`; Rotationspunkt (8,8,8). Stufe nach
  `Füllstand·100/10000`: < 5 leer, < 30 low, < 60 medium, < 90 high, sonst maximum.
  Drehwinkel: EAST (Y π, Z −π/2), WEST (Z π/2), UP keine, DOWN (Z π), SOUTH (X π/2, Z π/2), NORTH (X −π/2, Z π/2).
  Inventar: UP, leer.
- Fabricator (`RenderBlock`, `fabricator_block.png`): Würfel `(-8,-8,-8, 16,16,16)`, gleiche Drehwinkel;
  Inventar: EAST.

**Thermionic Fabricator** (Slots/Ablauf):
- Pro Tick: (1) ist der Tank nicht leer und Hitze < Schmelzpunkt der Tankflüssigkeit → −5 mB; (2) gibt es eine
  ausstehende Schmelze, wird sie in den Tank gefüllt (Rest bleibt ausstehend); sonst, wenn der Tank nicht voll ist
  und Slot 0 schmelzbar mit Schmelzpunkt ≤ Hitze → 1 Item verbrauchen, Menge wird ausstehend; (3) Hitze −2 (> 2500)
  bzw. −1 (> 0). `doWork()` = Hitze +25, `false` bei Hitze ≥ 5000 (dann kein MJ-Verbrauch).
- Rezeptsuche: Plan (`null` im Rezept passt immer), Raster (Slots 3–11, geformt, gespiegelt erlaubt, Ore-Dictionary),
  Tank: gleiche Flüssigkeit und Menge ≥ Rezeptmenge.
- Ergebnis-Slot ist virtuell (zeigt das Rezeptergebnis). Herausnehmen (GUI): zuerst je ein Satz der Rasterzutaten
  aus dem Vorrat (12–29); reicht der Vorrat nicht, wird aus jedem belegten Rasterslot 1 Item entfernt; Flüssigglas
  wird abgezogen. Automatische Entnahme (`extractItem`) nur, wenn der Vorrat reicht.
- Automatisches Einfügen (`addItem`): von oben schmelzbare Items in Slot 0, alles andere in den Vorrat.
- Kein Flüssigkeitsanschluss (Tank nur über Schmelzen).
- GUI `fabricator.png` 176×211: Vorrat (8+18k, 84+18i), Schmelzslot (26,21), Plan (139,17), Ergebnis (139,53),
  Raster (67+18k, 17+18l), Spielerinventar ab y 129 / Hotbar y 187. Tankanzeige (26,48) 16×16 (Füllung
  `Menge·16/2000`). Hitzebalken bei (55, 17+52−h) aus Textur (192, 52−h), 4×h mit `h = Hitze·52/5000`;
  Schmelzpunkt-Marke bei (52, 15+52−m) aus (196,0) 10×5, `m = Schmelzpunkt·52/5000` (Schmelzpunkt der
  Tankflüssigkeit, sonst des Items in Slot 0). Titel zentriert y 6, Farbe 0x404040.

**Carpenter** (Slots/Ablauf):
- Innere Slots: 0–8 Muster (Phantom-Slots: Linksklick mit Item legt 1 Kopie ab, Rechtsklick leert, leere Hand +
  Linksklick tut nichts), 9 Kiste, 10 Produkt, 11 Behälter, 12–29 Vorrat.
- Alle 20 Ticks (Weltzeit): Behälter in Slot 11 mit einer Rezeptflüssigkeit (hier nur Wasser) einfüllen, wenn der
  ganze Inhalt passt (Eimer → leerer Eimer, Zelle wird verbraucht). Alle 40 Ticks: aktuelles Rezept suchen, falls
  keines gesetzt ist. Das Rezept wird außerdem bei jeder Änderung von Muster/Kistenslot in der GUI neu bestimmt.
- Rezept passt: Flüssigkeit gleich (Menge egal), Kiste falls gefordert, Muster (geformt, gespiegelt erlaubt,
  Ore-Dictionary). Ressourcen reichen: Tankmenge ≥ Rezeptmenge, Kiste vorhanden falls gefordert, und der Vorrat
  enthält mindestens einen Satz der Musterzutaten (gleiche Items, Anzahl je Item summiert).
- `doWork()`: Läuft ein Auftrag (`packageTime > 0`): −1; passt das Rezept nicht mehr oder fehlen Ressourcen →
  Abbruch (`false`); bei 0 → Ressourcen abziehen (Flüssigkeit, ggf. Kiste, ein Satz aus dem Vorrat), Produkt wird
  „ausstehend“ und in Slot 10 gelegt (falls belegt/voll: bleibt ausstehend, `false`). Sonst ausstehendes Produkt
  ablegen; sonst bei passendem Rezept + Ressourcen neuen Auftrag mit `packageTime = Schritte` starten (`true`).
- Automatisches Einfügen: Rezeptflüssigkeits-Behälter → Slot 11; Zutat des aktuellen Rezepts → Vorrat; sonst in
  den Kistenslot (Originaleigenheit: jedes Rezept ohne Kiste „akzeptiert“ jede Kiste). Entnahme: 1 Item aus Slot 10.
- Flüssigkeit: `fill` nimmt **jede** Flüssigkeit in den 10 000-mB-Tank (keine Prüfung), keine Entnahme.
- GUI `carpenter.png` 176×215: Vorrat (8+18k, 90+18i), Behälter (120,20), Kiste (83,20), Produkt (120,56),
  Ergebnisanzeige (80,51, gesperrt), Muster (10+18k, 20+18l), Spielerinventar ab y 136 / Hotbar y 194.
  Tankanzeige (150,17) 16×58 mit Skala-Overlay aus (176,0) 16×60. Fortschritt bei (98, 51+16−p) aus
  (176, 76−p), 4×p mit `p = 16 − packageTime·16/totalTime`, nur wenn die Maschine arbeitet.

**Platinen (`chipsets`)**: Typ = Meta 0–3 (Basic/Enhanced/Refined/Intricate, Sockel 1–4). NBT: Layout-UID und
Schaltungen je Sockel; frisch hergestellte Platinen haben keine Daten. Farben (Pass 0 Icon 23 / Pass 1 Icon 22):
primär 0x191919, sekundär Basic 0x6DCFF6, Enhanced 0xCB7C32, Refined 0xC9C9C9, Intricate 0xE2CB6B. Tooltip:
„§6<Verwendung des Layouts>:“, dann je Schaltung „ - <Beschreibung>“.

**Schaltungen** (Layout `forestry.engine.tin`, einziges Layout im Umfang):

| Röhre | UID | Name (en) | Beschreibung |
|---|---|---|---|
| Kupfer | `forestry.energyChoke1` | Electric Choke | „Reduces ouput by 1 MJ/t“, „Reduces intake by 2 EU/t“ |
| Zinn | `forestry.energyBoost1` | Electric Boost I | „Increases output by 2 MJ/t“, „Increases intake by 7 EU/t“ |
| Bronze | `forestry.energyBoost2` | Electric Boost II | „Increases output by 4 MJ/t“, „Increases intake by 15 EU/t“ |
| Eisen | `forestry.energyEfficiency1` | Electric Efficiency | „Reduces intake by 1 EU/t“ |

**Röhren (`thermionicTubes`)**: Pass 0 Icon 41 (Farbe 0xFFFFFF), Pass 1 Icon 42 mit Kupfer 0xE3B78E, Zinn 0xE1EEF4,
Bronze 0xDDC276, Eisen 0xD8D8D8 (`items.png`). Robustes Gehäuse Icon 9, Lötkolben Icon 11. Flüssigglas: Icon 49 in
`fluids.png`, animiert mit `TextureLiquidsFX` (r/g/b 210–238, Alpha 146 + 50·v²).

**Lötkolben** (`ItemSolderingIron`, Stack 1, Haltbarkeit 5 – wird **nie** verbraucht): GUI `solder.png` 176×205,
Slots: Platine (152,12), Ergebnis (152,92), Röhren (12,32/52/72/92); Spielerinventar ab y 123 / Hotbar y 181, der
Slot mit dem Lötkolben ist gesperrt. Knöpfe „<“ (12,10) und „>“ (130,10), je 12×18, wechseln das Layout. Layoutname
zentriert in 138 px ab x 8, y 16; je Röhrenslot Text bei (32, 36+20·i): „<Schaltung> (<Limit>)“ oder „(No effect)“,
Farbe weiß. Bei jeder Inventaränderung `trySolder`: genau 1 Platine im Platinenslot, Ergebnisslot leer, mindestens
eine gültige Röhre in den ersten *Sockel* Röhrenslots (Limit je Schaltung) → nur wenn die Anzahl gültiger Röhren
gleich der Sockelzahl ist: je 1 Röhre verbrauchen, Platine (gleicher Typ, Layout, Schaltungen) in den Ergebnisslot.
Das Inventar wird nicht gespeichert: beim Schließen fallen alle Items vor den Spieler.

## B. Power Converters (MJ ↔ EU)

Quelle: Power Converters (PowerCrystals, Pack-Jar) + PowerCrystalsCore (`PowerProviderAdvanced`).
Pack-Config `PowerConverters.cfg`: BuildCraft 4375/4375, IndustrialCraft 1800/1800 interne Einheiten (PCU) je Ein-/Ausgabe.

**Umrechnung**: 1 MJ = 4375 PCU, 1 EU = 1800 PCU → **1 MJ = 2,43 EU**, 1 EU ≈ 0,411 MJ. Verlustfrei abgesehen von
Rundung (s. u.).

Umfang: Energy Bridge, Universal Charger, BC Consumer/Producer, IC2 Consumer/Producer (LV/MV/HV/EV).
(Steam/UE/Factorization-Konverter entfallen.)

### B.1 Energy Bridge (`converterBlockCommon` Meta 0)

- Puffer 16 000 000 PCU (≈ 3657 MJ ≈ 8888 EU).
- Consumer speisen in angrenzende Bridges ein (`storeEnergy`). Producer werden von der Bridge versorgt.
- Pro Tick: `rest = min(stored, max)`; für jeden angrenzenden Producer (Richtungsreihenfolge): `rest = producer.produce(rest)`
  (Producer gibt nicht verbrauchte PCU zurück); `stored = rest`. Ausgaberate je Seite (für die GUI) =
  verbrauchte PCU / energyPerOutput.
- GUI zeigt je Seite Typ, Spannung, Rate und „Input limited“.
- Härte 1,0.

### B.2 IC2 Consumer (EU → PCU), Meta 0/2/4/6 = LV/MV/HV/EV

- IC2-Senke von allen Seiten. `demandsEnergy = Σ(freier Bridge-Puffer) / 1800`.
- `injectEnergy(amount)`: `amount · 1800` PCU in die Bridges; nicht gespeicherte PCU / 1800 werden zurückgegeben.
- **Max. sichere Spannung**: LV 32, MV 128, HV 512, EV unbegrenzt → Überspannung = Explosion (`explodeMachineAt`,
  IC2-Standard).
- Rezepte: `G G`/` T `/`G G` mit G = Goldbarren, T = LV-Trafo (LV), MV-Trafo (MV), HV-Trafo (HV), **MFSU** (EV).
  Consumer ↔ Producer gleicher Stufe formlos umwandelbar.

### B.3 IC2 Producer (PCU → EU), Meta 1/3/5/7

- IC2-Quelle in alle Richtungen, `maxEnergyOutput` = 32/128/512/2048.
- `produceEnergy(pcu)`: `eu = pcu / 1800`. Ist `eu < maxOutput` → **gibt nichts ab** (es wird nur in vollen Paketen der
  Nennspannung gesendet). Sonst ein Paket `maxOutput`; Rückgabe an die Bridge
  `(eu − (maxOutput − nichtVerteilt)) · 1800` – die Rest-PCU unter 1800 (Bruchteil-EU) gehen dabei **verloren**.
- Pro Tick also maximal **ein Paket** der Nennspannung.

### B.4 BC Consumer (MJ → PCU), `converterBlockBuildCraft` Meta 0

- MJ-Empfänger: `PowerProviderAdvanced` mit `configure(latency 25, min 2, max 100, activation 1, maxStored 1000)`;
  **kein Leistungsverlust** über Zeit; `update()` macht nichts.
- Pro Tick: gespeicherte MJ (abgerundet) ≥ 1 → komplett entnehmen und als `MJ · 4375` PCU in die Bridges speichern
  (Überschuss über Bridge-Kapazität geht verloren).
- Rezept: `G G`/` E `/`G G`, G = Gold, E = **Stirling-Engine** (BC Meta 1). Formlos ↔ Producer.

### B.5 BC Producer (PCU → MJ), Meta 1

- Pro Tick: `mj = pcu / 4375`; für jeden angrenzenden MJ-Empfänger mit `minEnergyReceived ≤ mj`:
  `menge = min(maxEnergyReceived, mj, maxStored − floor(stored))` → `receiveEnergy`; PCU entsprechend abziehen.
  (Wirkt direkt, ohne Kolben, jeden Tick.)

### B.6 Universal Charger (Meta 2)

- Producer (IC2-System). Steht ein Spieler auf dem Block (Kollision), lädt er dessen Inventar (Spieler zählt bis
  Abstand > 2), sonst angrenzende Inventare (sided: passende Slots).
- Laden elektrischer IC2-Items: `eu = pcu / 1800`, `ElectricItem.charge(stack, eu, item.tier, false, false)` (mit
  Transferlimit), pro Tick nur das erste Item, das etwas aufnimmt.
- Rezept: `GRG`/`ICI`/`GRG`, G = Gold, R = Redstone, I = Eisen, C = Truhe.

### B.7 Rezept Energy Bridge

`GRG`/`LDL`/`GRG`: G = Gold, R = Redstone, L = Glas, D = Diamant.
