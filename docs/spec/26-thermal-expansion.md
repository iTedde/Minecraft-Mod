# 26 – Thermal Expansion (Phase 25)

Nutzerwunsch (2026-10-20): **Thermal Expansion 2.2.2.2** (Team CoFH) aus dem Pack (`ftb ultimate/mods/thermalexpansion.zip`
+ `CoFHCore.zip`, dekompiliert nach `reference/decompiled/thermalexpansion/` und `…/CoFHCore/`, ohne MCP-Remapping)
nachbauen. Pack-Config `ftb ultimate/config/cofh/thermalexpansion.cfg`. Paket `dev.craft.thermalexpansion`, eigener
Creative-Tab „Craft: Thermal Expansion“. Energie ist **MJ** (BuildCraft-`IPowerReceptor`, Craft `buildcraft/power`).

## 0. Entscheidungen (Nutzer, 2026-10-20)

- **E1 Materialien:** TE-Items, die es in Craft schon gibt, werden **nicht** dupliziert; TE-Rezepte nutzen die
  Craft-Items über Tags (im Pack per OreDictionary austauschbar). Zuordnung §2.
- **E2 Erze:** nur **Silber-, Blei- und Nickelerz** („Ferrous Ore“) mit TE-Werten; TE-Kupfer/-Zinn entfallen
  (Craft hat Kupfer/Zinn mit IC2-Verteilung). Auch in der Mining-Dimension (doppelte Menge, `spec 18`).
- **E3 Umfang:** alles, was TE 2.2.2.2 im Pack registriert, inkl. Tesserakte.

Nicht registriert in 2.2.2.2 (Code vorhanden, aber ohne Tile-Entity/Item-Registrierung) und daher **nicht** im
Umfang: Auto Crafter (`TileAutoCrafting`, Schematic, Craftinomicon, Sketch Pad), Charging Bench, Energy
Restrictor, Painter, Turtle-Upgrades (CC-Modul; CC: Tweaked hat andere Turtle-APIs).

## 1. Inhalt

**Blöcke**

| Craft-ID | Original | Name (en / de) |
|---|---|---|
| `silver_ore`, `lead_ore`, `nickel_ore` (+ Tiefenschiefer) | `blockOre` Meta 2/3/4 | Silver/Lead/Ferrous Ore · Silber-/Blei-/Nickelerz |
| `powered_furnace` | `blockMachine` 0 | Powered Furnace · Elektrischer Ofen (TE) |
| `pulverizer` | 1 | Pulverizer · Pulverisierer |
| `sawmill` | 2 | Sawmill · Sägewerk |
| `induction_smelter` | 3 | Induction Smelter · Induktionsschmelze |
| `magma_crucible` | 4 | Magma Crucible · Magmatiegel |
| `liquid_transposer` | 5 | Liquid Transposer · Flüssigkeitsumfüller |
| `glacial_precipitator` | 6 | Glacial Precipitator · Eisbereiter |
| `igneous_extruder` | 7 | Igneous Extruder · Gesteinsextruder |
| `aqueous_accumulator` | 8 | Aqueous Accumulator · Wassersammler |
| `portable_tank` | `blockTank` | Portable Tank · Tragbarer Tank |
| `hardened_glass` | `blockGlass` | Hardened Glass · Gehärtetes Glas |
| `rockwool_<farbe>` (16) | `blockRockwool` | Rockwool · Steinwolle |
| `steam_engine`, `magmatic_engine` | `blockEngine` 0/1 | Steam/Magmatic Engine · Dampf-/Magmamotor |
| `redstone_energy_cell` | `blockEnergyCell` | Redstone Energy Cell · Redstone-Energiezelle |
| `redstone_energy_conduit`, `liquiduct` | `blockConduit` | Redstone Energy Conduit · Redstone-Energieleitung; Liquiduct · Flüssigkeitsleitung |
| `energy_tesseract`, `liquid_tesseract`, `item_tesseract` | `blockTeleport` 0–2 | Energy/Liquid/Item Tesseract · Tesserakt |

**Items:** Invarstaub, Invarbarren, Holzschnitzel, Sägemehl, Gepresstes Sägemehl, Schlacke, Reiche Schlacke,
Pneumatischer Servo, Redstone-Empfangs-/Übertragungs-/Leitspule (Gold/Silber/Elektrum), Kupfer-/Zinn-/Invar-Zahnrad,
Maschinenrahmen, Energiezellenrahmen (leer/voll), Energieleitung (leer), Tesserakt-Rahmen, Unabgestimmter Tesserakt,
Crescent Hammer (Schraubenschlüssel), Multimeter. Flüssigkeiten **Geschmolzenes Redstone** und **Geschmolzenes Ender**
(wie im Original ohne Weltblock und ohne Behälter – nur in Tanks, Umfüller und Liquiducts).

## 2. Materialzuordnung (E1)

| TE | Craft | | TE | Craft |
|---|---|---|---|---|
| Pulverized Iron/Gold | `craft:iron_dust`/`gold_dust` | | Copper/Tin Ingot | `minecraft:copper_ingot` / `craft:tin_ingot` |
| Pulverized Copper/Tin | `copper_dust`/`tin_dust` | | Silver/Lead Ingot | GT `silver_ingot`/`lead_ingot` |
| Pulverized Obsidian | GT `obsidian_dust` | | Ferrous Ingot | GT `nickel_ingot` |
| Pulverized Silver/Lead | GT `silver_dust`/`lead_dust` | | Shiny Ingot | GT `platinum_ingot` |
| Pulverized Ferrous Metal | GT `nickel_dust` | | Electrum Ingot | GT `electrum_ingot` |
| Pulverized Shiny Metal | GT `platinum_dust` | | Sulfur | GT `sulfur_dust` |
| Electrum/Bronze/Brass Blend | GT `electrum_dust`/IC2 `bronze_dust`/GT `brass_dust` | | Niter | GT `saltpeter_dust` |

Neu sind nur Invarstaub/-barren und die TE-eigenen Teile. Platinbarren (TE „Shiny Ingot“) kommt wie im Pack in
Dungeon- und Pyramidenkisten (1–3 Stück, Gewicht 5).

## 3. Weltgenerierung (CoFH `WorldHandler`/`WorldGenMulti`)

Je Chunk, Oberwelt und alle Nicht-Nether/End-Dimensionen: `numClusters` Adern, Mittelpunkt `x/z + rand(16)`,
`y = minY + rand(maxY − minY)`, Vanilla-1.4.7-`WorldGenMinable` mit Größe `clusterSize`, **jeder Block gewichtet
gewürfelt** aus der Liste:

| Erz | Adern | Größe | y | Mischung | Drop/Ofen |
|---|---:|---:|---|---|---|
| Silber | 3 | 8 | 5–29 | 90 % Silber, 10 % Blei | Erz selbst; Ofen → Silberbarren (0,9 XP) |
| Blei | 4 | 8 | 10–34 | 80 % Blei, 20 % Silber | Ofen → Bleibarren (0,8 XP) |
| Nickel (Ferrous) | 2 | 4 | 5–19 | 100 % | Ofen → Nickelbarren (1,0 XP) |

Erzblöcke (`BlockOre`): Härte 3, `setResistance(5)` (= 3), Eisenspitzhacke (Stufe 2), droppen sich selbst, keine
Erfahrung. Tiefenschiefer-Varianten wie bei Zinn/Uran (+1,5 Härte, Muster auf Vanilla-Tiefenschiefer). GregTech
(`GT_OreDictHandler`) ergänzt: Macerator 2 Staub; Industrial Grinder Silber 2 + 2 kl. Blei (Quecksilber 3), Blei 2 +
kl. Silber (Quecksilber + Silberstaub), Nickel 3 + kl. Platin + kl. Kupfer (Quecksilber + Platinstaub); Erzwerte für
Miner/Scanner Silber 3, Blei 3, Nickel 4. Gehärtetes Glas: Härte 3, Widerstand 93, kein Drop (Behutsamkeit oder
Schleichen + Schraubenschlüssel). Steinwolle: Härte 0,8, Widerstand 6, brennt nicht, Vanilla-Wolltexturen.

## 4. Gemeinsame Maschinenlogik (`TileMachineRoot` + `TileMachinePower`)

- **Energie (MJ)**: `PowerProviderAdv.configure(maxReceived = 10 · maxPower, maxStored = maxEnergy)`.
  `maxEnergy = 1200 · maxPower`; Leistung je Tick `getPower()`: gespeichert > 80 % → `maxPower`, < 20 % →
  `minPower`, dazwischen linear (`energy / ramp`, ramp = 80 %·maxEnergy / maxPower, auf 0,1 abgerundet).
- **Ablauf**: aktiv → `processCur += power` (Energie abgezogen); fertig (`processCur ≥ processEnd`) →
  `processFinish`, Produkte weiter, Rest `processCur % processEnd` zurück in den Speicher, nächster Vorgang sofort
  oder inaktiv. Inaktiv: alle 40 Ticks Produkte ausgeben, bei Startbedingung starten. Redstone-Steuerung
  (aus / bei Signal / ohne Signal) über das GUI.
- **Seitenkonfiguration**: je Seite eine Gruppe (Farbe), mit Crescent Hammer bzw. GUI-Reiter umschaltbar; Facing =
  Front (nicht konfigurierbar). Werte je Maschine (`Machines.sideData`).
- Leuchtet aktiv: Ofen 14, Induktionsschmelze 15, Magmatiegel 15, Extruder 14.

| Maschine | minPower | maxPower (MJ/t) | Speicher (MJ) | Seitengruppen |
|---|---:|---:|---:|---:|
| Elektrischer Ofen | 0,5 | 2 | 2400 | 4 |
| Pulverisierer | 1 | 4 | 4800 | 6 |
| Sägewerk | 0,5 | 2 | 2400 | 6 |
| Induktionsschmelze | 1 | 4 | 4800 | 8 |
| Magmatiegel | 7,5 | 30 | 36 000 | 3 |
| Flüssigkeitsumfüller | 1 | 4 | 4800 | 5 |
| Eisbereiter | 0,5 | 2 | 2400 | 4 |
| Gesteinsextruder | – (kein Strom) | | | 4 |
| Wassersammler | – (kein Strom) | | | 2 |

Rezepte und Sonderlogik je Maschine: `factory/crafting/*Manager.java` (Energie in MJ je Vorgang, Nebenprodukte mit
Chance) und `factory/tileentity/Tile*.java`; in den Teilphasen hier nachtragen. Pack-Tweaks: Pulverisierer mit
Barren→Staub, Sandstein, Netherrack, Zuckerrohr, Knochen, Lohenrute (Wolle aus); Extruder Bruchstein 0/0, Stein
Lava 2/Wasser 1000, Obsidian 1000/1000 mB; Wassersammler 25 mB/Tick-Stufe; Netherrack im Magmatiegel 12 000 MJ →
1000 mB Lava; Lava = 20 000 MJ (`core.lava.mj`).

### 4.1 Elektrischer Ofen, Pulverisierer, Sägewerk (25.2)

Ablauf je Maschine (`TileFurnace`, `TilePulverizer`, `TileSawmill`): Start nur mit Rezept, Energie ≥ Rezeptenergie und
Platz für die Ausgabe; der Ofen nimmt immer **ein** Item, Pulverisierer/Sägewerk die Rezeptmenge. Hauptausgabe
Pulverisierer/Sägewerk über zwei Slots verteilt, Nebenprodukt (Slot 4) mit Chance in %. Auswurf im Leerlauf alle
40 Ticks bis zu 4 Items je Slot in ein Rohr oder Inventar an einer Ausgabeseite (reihum über die Seiten).
Seitengruppen: Ofen 0 keine · 1 Eingabe · 2 Ausgabe · 3 offen; Pulverisierer/Sägewerk 0 keine · 1 Eingabe ·
2 Haupt- · 3 Nebenausgabe · 4 beide Ausgaben (Auswurf) · 5 offen. Beim Platzieren: Front 0, links und hinten 1, Rest
Vorgabe (Ofen unten/oben 1, Seiten 2; Pulverisierer/Sägewerk unten/oben 3, Seiten 2). Redstone-Vorgabe: läuft nur
**ohne** Signal. GUI: Reiter Energie/Redstone/Konfiguration rechts, Information/Anleitung links (Pack-Config
`tabs.info`/`tabs.tutorial` = an, Slot-Rahmen an); Konfiguration: Klick weiter, Rechtsklick zurück, Shift-Klick
setzt die Seite auf 0 (auf der Front: alle Seiten zurücksetzen).

Rückfall-Rezepte: Ofen jedes Ofenrezept (160 MJ); Pulverisierer jedes Macerator-Rezept (400 MJ, GT
`addPulverisationRecipe`), Sägewerk Macerator-Rezepte mit Holzschliff als Ergebnis (80 MJ). Eigene Rezepte haben Vorrang.

**Elektrischer Ofen** (`FurnaceManager`): Kaktus → grüner Farbstoff 80; roher Schweinebraten/Kabeljau/Rindfleisch/
Hähnchen/Kartoffel → gegart 40 MJ.

**Pulverisierer** (`PulverizerManager`, Pack-Config: Barren→Staub, Sandstein, Netherrack, Zuckerrohr, Knochen,
Lohenrute an, Wolle aus) – Eingabe → Ausgabe (+ Nebenprodukt Chance), MJ:

| Eingabe | Ausgabe | MJ | | Eingabe | Ausgabe | MJ |
|---|---|---:|---|---|---|---:|
| Stein | Bruchstein | 320 | | Eisenerz | 2 Eisenstaub + Nickelstaub 10 % | 400 |
| Bruchstein | Sand + Kies 10 % | 320 | | Golderz | 2 Goldstaub | 400 |
| Kies | Feuerstein | 320 | | Kupfererz | 2 Kupferstaub + Goldstaub 10 % | 400 |
| Glas | Sand | 320 | | Zinnerz | 2 Zinnstaub + Eisenstaub 10 % | 400 |
| Steinziegel | rissige Steinziegel | 80 | | Silbererz | 2 Silberstaub + Bleistaub 10 % | 400 |
| Sandstein (3 Arten) | 2 Sand + Salpeter 10 % | 320 | | Bleierz | 2 Bleistaub + Silberstaub 10 % | 400 |
| Obsidian | Obsidianstaub | 400 | | Nickelerz | 2 Nickelstaub + Platinstaub 10 % | 400 |
| Netherrack | Bruchstein + Schwefel 10 % | 320 | | Eisen-/Goldbarren | Staub | 240 |
| Kohle-/Diamant-/Smaragderz | Kohle/Diamant/Smaragd | 80 | | Barren Kupfer, Zinn, Silber, Blei, Nickel, Platin, Elektrum, Invar, Bronze | Staub | 240 |
| Glowstone | 4 Glowstonestaub | 80 | | Kohle / Holzkohle | Kohle-/Holzkohlestaub | 160 |
| Lapis-/Redstone-Erz | 8 Lapis / 8 Redstone | 240 | | Zuckerrohr | 2 Zucker | 80 |
| Stamm (Eiche, Fichte, Birke, Tropen) | Holzschnitzel | 160 | | Knochen | 6 Knochenmehl | 160 |
| Lohenrute | 4 Lohenstaub + Schwefel 50 % | 160 | | | | |

Erze jeweils auch als Tiefenschiefer; Rohmetall (Eisen/Gold/Kupfer) zählt wie das Erz (wie im Macerator).
Schwefel-/Salpeter-/Zink-/Platinerz-Rezepte entfallen (diese Oredict-Erze gibt es in Craft nicht; GT-Erze laufen über
den Macerator-Rückfall).

**Sägewerk** (`SawmillManager`) – Nebenprodukt ohne Angabe 100 %:

| Eingabe | Ausgabe | MJ |
|---|---|---:|
| Stamm Eiche/Fichte/Birke/Tropen | 6 passende Bretter + Sägemehl | 80 |
| Notenblock | 8 Bretter + Redstone | 240 |
| Bett (jede Farbe) | 3 Bretter + 3 weiße Wolle | 240 |
| Bücherregal | 6 Bretter + 3 Bücher | 240 |
| Truhe / Werkbank | 8 / 4 Bretter | 240 |
| Schild, Druckplatte, Tür | 2 Bretter (Tür s. Abweichungen) | 240 |
| Plattenspieler | 8 Bretter + Diamant | 240 |
| Falltür / Boot | 3 / 5 Bretter | 240 |
| Zauntor | 2 Bretter + Sägemehl | 240 |
| Holzschwert/-schaufel/-spitzhacke/-axt/-hacke | 2/1/3/3/2 Bretter + Sägemehl | 160 |
| Gummiholz (IC2) | 4 Tropenholzbretter + Harz 33 % | 120 |

Bretter ohne Angabe = Eiche; Eingaben Schild/Tür/Druckplatte/Falltür/Zauntor/Boot = Eiche (1.4.7 kannte nur diese).

### 4.2 Induktionsschmelze, Magmatiegel, Flüssigkeitsumfüller (25.3)

**Induktionsschmelze** (`TileSmelter`): Eingaben Slot 1/2 in beliebiger Reihenfolge (Suche nach Item, Mengen prüft die
Maschine), Hauptausgabe über zwei Slots, Nebenprodukt mit Chance. Seitengruppen 0 keine · 1 beide Eingaben · 2 Haupt- ·
3 Nebenausgabe · 4 beide Ausgaben · 5 Eingabe 1 · 6 Eingabe 2 · 7 offen; beim Platzieren links 5, hinten 6.

| Eingaben | Ausgabe | MJ |
|---|---|---:|
| Erz (Eisen, Gold, Kupfer, Zinn, Silber, Blei, Nickel; auch Tiefenschiefer, Rohmetall) + Sand | 2 Barren + Reiche Schlacke 5 % | 320 |
| dasselbe Erz + Reiche Schlacke | 3 Barren + Schlacke 75 % | 400 |
| 2 Staub (Eisen, Gold, Kupfer, Zinn, Silber, Blei, Nickel, Platin, Elektrum, Invar, Bronze, Messing, Zink) + Sand | 2 Barren + Schlacke 25 % | 80 |
| Silberbarren + Goldbarren | 2 Elektrum | 240 |
| Nickelbarren + 2 Eisenbarren | 3 Invar | 240 |
| 3 Kupfer + 1 Zinn | 2 Bronze | 240 |
| Bleibarren oder Bleistaub + 2 Obsidianstaub | 2 Gehärtetes Glas | 400 |
| 4 Netherrack + Seelensand | 2 Netherziegel(-blöcke) + Schwefel 25 % | 400 |
| GT: Pyriterz + Sand / + Reiche Schlacke | 1 Eisen + Reiche Schlacke 10 % / 2 Eisen + Schlacke 95 % | 300 |
| GT: 2 Eisenbarren + Sand | 2 Raffiniertes Eisen + Schlacke 25 % | 400 |
| GT: 3 Kupfer + 1 Zink | 4 Messing | 400 |

**Magmatiegel** (`TileCrucible`): Tank 10 000 mB, gibt an Ausgabeseiten (Gruppe 2) bis 100 mB/Tick in angrenzende
Tanks ab und lässt sich dort leeren (nicht befüllen). Gruppen 0 keine · 1 Eingabe · 2 Flüssigkeitsausgabe.
Rezepte: Bruchstein/Stein/Obsidian → 1000 mB Lava (24 000 MJ = 6/5 · `core.lava.mj`), Netherrack → 1000 mB Lava
(12 000, Pack-Config), 4 Schneebälle → 500 mB Wasser (60), Schneeblock → 500 mB (60), Eis → 1000 mB (120), Redstone →
25 mB Geschmolzenes Redstone (1200), Enderperle → 250 mB Geschmolzenes Ender (2400). Originaleigenheit: die Suche
geht nur nach dem Item – auch 1–3 Schneebälle schmelzen zu 500 mB.

**Flüssigkeitsumfüller** (`TileTransposer`): Füllmodus füllt das Item in Slot 1 aus dem Tank, Entleermodus (Knopf)
leert es in den Tank; der Moduswechsel greift erst nach dem laufenden Vorgang. Tank 10 000 mB; Füllmodus: befüllbar an
Eingabeseiten (Gruppe 1); Entleermodus: leerbar an Gruppe 3 und Abgabe 100 mB/Tick dorthin. Gruppen 0 keine ·
1 Eingabe · 2 Ausgabe · 3 Flüssigkeitsausgabe · 4 offen. Rezepte (Füllen; „umkehrbar“ = auch Entleeren):
IC2-Modul leere Zelle ↔ Wasser-/Lavazelle 1000 mB (80 MJ); danach jeder registrierte Behälter (`LiquidContainers`:
Eimer Wasser/Lava/Öl/Treibstoff 80 MJ, Kühlmittelzelle 10k 160 MJ; ein schon vorhandenes Paar Behälter/Flüssigkeit
bleibt, daher keine 30k/60k-Zellen), umkehrbar. Energiezellenrahmen + 1000 mB Redstone → voll (1600), Tesserakt-Rahmen
+ 1000 mB Ender → Unabgestimmter Tesserakt (1600), nicht umkehrbar. GT: Kohlestaub + 125 mB Wasser → Hydrierter
Kohlestaub (125), Entleeren: Hydrierter Kohlestaub → Kohlestaub + 100 mB Wasser (125). Energieleitung + 50 mB Redstone
(80 MJ) folgt mit 25.6.

### 4.3 Eisbereiter, Gesteinsextruder, Wassersammler, Tragbarer Tank (25.4)

**Eisbereiter / Gesteinsextruder** (`TileIceGen` mit MJ, `TileRockGen` ohne Strom): im GUI eines von drei Produkten
wählen (`curSelection`); ein laufender Vorgang endet mit dem Produkt, mit dem er begann (`prevSelection`), die neue Wahl
greift sofort nur ohne laufenden Vorgang. Ausgabe Slot 0, Auswurf an Gruppe 2 (4 Items, reihum); Gruppen 0 keine ·
1 Flüssigkeitseingabe (Items nicht zugänglich) · 2 Ausgabe · 3 offen (Slot 0); beim Platzieren Front 0, links/hinten 1,
sonst unten/oben 2, Seiten 1. Tanks je 4000 mB, nur befüllbar (Eingabeseiten, Rechtsklick mit gefülltem Behälter),
nicht leerbar.

| Maschine | Auswahl 0 | Auswahl 1 | Auswahl 2 |
|---|---|---|---|
| Eisbereiter (min 0,5 / max 2 MJ/t, 2400 MJ) | 4 Schneebälle: 500 mB Wasser, 80 MJ | Schneeblock: 500 mB, 80 MJ | Eis: 1000 mB, 160 MJ |
| Gesteinsextruder (1 Fortschritt je Tick, leuchtet aktiv 14) | Bruchstein: 0/0 mB, 40 Ticks | Stein: 2 mB Lava + 1000 mB Wasser, 80 Ticks | Obsidian: 1000/1000 mB, 120 Ticks |

Originaleigenheiten: Der Extruder startet nur mit je **mindestens 1000 mB** Lava und Wasser, gleich welche Auswahl. Der
Eisbereiter prüft den Platz im Ausgabeslot gegen die Stapelgröße des Produkts der **vorigen** Auswahl.

**Wassersammler** (`TileWaterGen`, ohne Strom, kein Item-Inventar): Tank 4000 mB; alle 40 Ticks +1000 mB, wenn mindestens
zwei der vier waagerechten Nachbarn Wasser sind, sonst +1 mB (Pack-Config `factory.waterGen.rate = 25` → 25 · 40);
startet nicht im Nether-Biom. Gibt je Tick 25 mB an den nächsten angrenzenden Tank auf einer Seite der Gruppe 1 ab
(reihum; der erste gefundene Tank zählt, auch wenn er nichts nimmt). Gruppen 0 keine · 1 Ausgabe; alle Seiten außer der
Front starten in Gruppe 1. Sein Tank lässt sich weder befüllen noch leeren.

**Tragbarer Tank** (`BlockTank`/`TilePortableTank`): Glas, Härte 3, Widerstand 90, Maße 2–14 px, stapelt 1; 8000 mB einer
beliebigen Flüssigkeit, nur von oben und unten zugänglich. Rechtsklick mit Behälter füllt bzw. leert ihn, Schraubenschlüssel
schaltet den Ausgabemodus, Schleichen + Schraubenschlüssel baut ihn **mit** Inhalt ab (Tooltip „Flüssigkeit“/„Füllstand
x / 8000 mB“); normal abgebaut droppt er leer. Ausgabemodus: gibt nach unten ab – 500 mB/Tick in einen Tragbaren Tank
darunter, sonst 100 mB/Tick in jeden Tank; unten dann nicht zugänglich (außer auf einem Tragbaren Tank). Auf einen
Tragbaren Tank gesetzt startet er im Ausgabemodus. Was nicht passt, läuft in einen Tragbaren Tank darüber über.
Originalfehler: Lichtwert immer 0. Modell aus `liquid_tank.png` (Deckel, vier Wände, Boden; Wände/Boden im Ausgabemodus
anders), Flüssigkeit 0,1975–0,8025 breit von 0,0725 bis 0,99 in 100 Stufen. Rezept ` G /GTG/ G ` (Gehärtetes Glas,
Zinnbarren).

## 5. Rezepte (Werkbank)

Maschinenrahmen `IGI/GXG/IGI` (Eisen, Glas, Gold). Maschinen `" X "/"YCY"/"IPI"` mit C Rahmen, I Kupferbarren,
P Redstone-Empfangsspule:

| Maschine | X | Y |
|---|---|---|
| Elektrischer Ofen | Redstone | Ziegel |
| Pulverisierer | Kolben | Feuerstein |
| Sägewerk | Eisenaxt | Holzbretter |
| Induktionsschmelze | Eimer | Invarbarren |
| Magmatiegel | Eimer | Netherziegel |
| Flüssigkeitsumfüller | Eimer | Glas |
| Eisbereiter | Kolben | Schnee |
| Gesteinsextruder | Kolben | Glas (I Zinn, P Servo) |
| Wassersammler | Eimer | Glas (I Zinn, P Servo) |

Weitere: Crescent Hammer `I I/ S / I ` (Eisen, Silber); Schießpulver = Salpeter + Schwefel + Kohle/Holzkohle;
3 Holzschnitzel (`###`) → 3 Papier; Gepresstes Sägemehl `###/# #/###` → Ofen → Holzkohle;
Servo ` I /GRG/ I ` (Eisen, Glas, Redstone); Spulen `  R/ G /R  ` (Gold/Silber/Elektrum); Zahnräder ` X /XIX/ X `
(Metall um Eisen); Elektrumstaub = Gold- + Silberstaub → 2; Invarstaub = 2 Eisen- + 1 Nickelstaub → 3; Ofen:
Stäube → Barren, Schlacke → Steinwolle; Steinwolle färben (8 um Farbstoff); Tank ` G /GTG/ G ` (Gehärtetes Glas,
Zinn); Motoren `III/ C /GPG` (Dampf: Kupfer, Übertragungsspule, Kupferzahnrad, Kolben; Magma: Invar,
Invarzahnrad); Energiezellenrahmen `IGI/GXG/IGI` (Elektrum, Gehärtetes Glas, Diamant) → Umfüller mit 1000 mB
Geschmolzenem Redstone (1600 MJ) → Energiezelle ` X /YCY/IPI` (Elektrum, Blei, Leitspule); Leitungen `IGI`
(Elektrum bzw. Kupfer, Gehärtetes Glas) → 4, Energieleitung im Umfüller mit 50 mB Redstone füllen (80 MJ);
Tesserakt-Rahmen `IGI/GXG/IGI` (Zinn, Gehärtetes Glas, Diamant) + 1000 mB Ender (1600 MJ); Tesserakte
` X /YCY/IPI` (Energie: Elektrum/Silber/Blei/Leitspule; Flüssigkeit: Kupfer/Silber/Zinn/Servo; Item:
Zinn/Silber/Zinn/Servo); Multimeter `C C/LPL/ G ` (Kupfer, Blei, Leitspule, Zinnzahnrad).
IC2-Modul: Umfüller füllt leere Zelle mit 1000 mB Wasser/Lava; jeder registrierte Behälter wird gefüllt (Eimer 80,
sonst 160 MJ).

## 6. Energie und Transport

- **Motoren** (`energy/tileentity/TileEngine*.java`): Dampfmotor (Brennstoff/Wasser), Magmamotor (Lava), MJ-Ausgabe
  wie BC-Motoren; Werte §6.1.
- **Redstone-Energiezelle** (`TileEnergyCell`): MJ-Speicher mit einstellbarer Ein-/Ausgabe (für alle Seiten gemeinsam); Werte §6.1.

- **Redstone-Energieleitung** (`TileConduitEnergy`, `GridEnergy`, `PowerProviderGrid`): Netz mit 5 % Verlust
  (`transport.conduit.energy.loss = 5`).
- **Liquiduct** (`TileConduitLiquid`, `GridLiquid`): Flüssigkeitsnetz.
- **Tesserakte** (`TileTeleport*`, `TeleportRegistry`): Frequenz (Name) je Besitzer/öffentlich; Energie mit 25 %
  Verlust, max. 100 MJ/t Übertragung; Items und Flüssigkeiten sofort; Namen in `<Welt>/cofh/tesseracts.cfg`.

### 6.1 Motoren und Energiezelle (25.5)

**Motoren** (`TileEngineRoot`, `Engines.defaultConfigs`): Material Eisen, Härte 0,5, Licht 7 aktiv; beim Platzieren
nach oben ausgerichtet, dann zur ersten Nachbarseite mit MJ-Empfänger gedreht (Reihenfolge ab „oben“ + 1).

| Motor | min/max MJ/t | Puffer (MJ) | MJ je Hub | Brennstoff |
|---|---|---:|---:|---|
| Dampfmotor | 0,2 / 2 | 2000 | 200 | feste Brennstoffe + Wasser (Tank 4000 mB) |
| Magmamotor | 0,4 / 4 | 4000 | 400 | Lava (Tank 4000 mB): 50 mB → 900 MJ (= `core.lava.mj` · 90 / 2000) |

- Leistung je Tick: Puffer < 10 % → max, > 90 % → min, dazwischen `(Puffergröße − Inhalt) / Rampe` (0,1-Stufen,
  Rampe = 90 % · Puffer / max).
- Läuft nur mit MJ-Empfänger vor der Front und erfüllter Redstone-Bedingung (Vorgabe: ohne Signal); Hub 25 Ticks
  (+0,04 je Tick), bei halbem Hub bis zu „MJ je Hub“ an den Empfänger (höchstens dessen freier Platz). Untätig:
  −min MJ/t aus dem Puffer, alle 40 Ticks −10 MJ angebrannter Brennstoff.
- **Kaputt** (`shutdownEngine`), wenn der Puffer voll ist (Dampfmotor auch unter 20 mB Wasser): Brennstoff weg, Puffer
  −20 %, dann −min MJ/t; raucht; Schraubenschlüssel repariert. Sonst dreht der Schraubenschlüssel den Motor, Schleichen +
  Schraubenschlüssel baut ab, gefüllter Behälter füllt den Tank (nicht von der Front).
- Dampfmotor: Start mit Brennstoff und ≥ 20 mB Wasser; alle 40 Ticks `Leistung · 10` mB Wasser. Brennwerte
  (`getItemEnergyValue`, MJ): Kohle 4800, Holzkohle 3200, Holzblöcke (Material Holz) 450, Stock/Setzling 150, sonst
  Fuel-Handler der Mods · 3/2 – **nicht** die Vanilla-Brennwerte: IC2 (Gummisetzling 80, Zuckerrohr/Kaktus 50, Schrott
  350, Schrottbox 3150, Lavazelle 20 000), GT (Schild 600, Holzschliff 100, Holzplatte 1600); dazu Craft-Koks 1600 /
  Koksblock 16 000. Lavaeimer, Lohenrute, Holztür usw. brennen nicht. Ein verbranntes Item gibt keinen Behälter zurück.
- GUI: Brennstoff-Flamme (Rest des aktuellen Brennstoffs), Tankanzeige; Reiter Motor (Leistung, max. Leistung,
  gespeichert) und Redstone rechts, Information/Anleitung links. Magmamotor ohne nutzbaren Slot.

**Redstone-Energiezelle** (`TileEnergyCell`): Material Eisen, Härte 20, Widerstand 90, Stapel 1, selten; 600 000 MJ
ohne Verlust. Jeden Tick (Redstone-Bedingung) bekommt jeder angrenzende MJ-Empfänger außer anderen Zellen, der Energie
anfordert, bis `Senden` MJ (reihum ab einer wandernden Startseite). Anforderung an Rohre: bis `Empfangen` MJ; Motoren
füllen sie unabhängig davon. Grenzen 0–100 im GUI (Klick 5, Rechtsklick 1, mit Shift 100 bzw. 10), beim Platzieren
ohne Daten 50/100. Licht 0–10 mit der Ladung; Modell: Rahmen + Kern in 16 Ladestufen (animiertes Geschmolzenes
Redstone, `TextureRedstoneFX`). Schleichen + Schraubenschlüssel baut sie mit Ladung, Grenzen und Redstone-Modus ab
(Ladebalken am Item, Tooltip Ladung/Senden/Empfangen/Signal); normal abgebaut droppt sie leer. Kreativ-Tab: leer und
voll. Rezepte: Motoren `III/ C /GPG` (Dampf: Kupfer, Übertragungsspule, Kupferzahnrad, Kolben; Magma: Invar,
Übertragungsspule, Invarzahnrad, Kolben); Zelle ` X /YCY/IPI` (X/Y Elektrum, C voller Energiezellenrahmen, I Blei,
P Leitspule).

### 6.2 Redstone-Energieleitung, Liquiduct, Multimeter (25.6)

Gemeinsam (`BlockConduit`, `TileConduitRoot`): Glas, Härte 3, Widerstand 90, 8-px-Kern mit Arm je Verbindung. Jede Seite
ist keine Verbindung, eine Leitung desselben Netzes oder ein **Knoten**-Anschluss (MJ-Empfänger bzw. Tank). Leitungen
mit Anschluss sind Knoten, Modus **Ausgabe** (Vorgabe) oder **Eingabe** – der Schraubenschlüssel schaltet um (nur
Knoten), Schleichen + Schraubenschlüssel baut ab. Ausgabeknoten bedienen ihre Anschlüsse reihum (Startseite wandert).
Rezepte: Energieleitung (leer) `IGI` ×4 (Elektrum, Gehärtetes Glas) → Umfüller mit 50 mB Geschmolzenem Redstone
(80 MJ); Liquiduct `IGI` ×4 (Kupfer, Gehärtetes Glas). Modell aus `conduit_energy.png`/`conduit_liquid.png` (Arm,
Stummel + Platte Ausgabe/Eingabe), Flüssigkeit in sechs Stufen (Energieleitung immer voll Geschmolzenes Redstone).

**Redstone-Energieleitung** (`TileConduitEnergy`, `GridEnergy`, `PowerProviderGrid`): Licht 7, selten. Ein Netz hat
einen Puffer von 500 MJ je Knoten; alles, was hineinkommt (Rohre, Zellen, Motoren), verliert 5 %. BuildCraft-Rohre
machen keinen Knoten. Eingabeknoten fordern bis 500 MJ an (`powerRequest`), Ausgabeknoten fordern nichts. Je Tick gibt
jeder Ausgabeknoten bis zum Knotenanteil (`Puffer / Knoten`, erst ab 1 MJ) an seine Empfänger, so viel diese anfordern.
Motoren speisen auch einen Ausgabeknoten: mindestens `25 · minPower` je Hub.

**Liquiduct** (`TileConduitLiquid`, `GridLiquid`): ein Tank fürs Netz, `5 · Viskosität` mB je Leitung (Lava 80 →
400 mB, sonst 100 → 500 mB). Leitungen mit verschiedenen Flüssigkeiten verbinden sich nicht; der Schraubenschlüssel
auf eine Seite verbindet zwei Netze, wenn die Flüssigkeiten passen (eins leer). Von außen befüllbar außer über
Eingabeknoten, überall leerbar. Ausgabe je Tick: `min(Menge / Ausgabeknoten, Durchsatz)` je Knoten; Durchsatz =
Viskosität ab 75 % Füllung, halbe Viskosität bis 25 %, linear dazwischen (bei weniger als 1 mB je Knoten der Rest an
zufällige Knoten). Eingabeknoten saugen **nur mit Redstone-Signal** bis zur Viskosität je Seite aus ihren Tanks. Licht
wie die Flüssigkeit (Lava 15).

**Multimeter** (`ItemMultimeter`, Stapel 1, ` C C/LPL/ G `: Kupfer, Blei, Leitspule, Zinnzahnrad): Liquiduct →
Flüssigkeit, Menge/Kapazität, Durchsatz; Energieleitung → Netzauslastung, an einem Ausgabeknoten stattdessen 1. Klick
„Messe 20 Ticks“, 2. Klick mittlere Abgabe; anderer MJ-Empfänger → angeforderte Energie. Energiezellenrahmen
`IGI/GXG/IGI` (Elektrum, Gehärtetes Glas, Diamant).

### 6.3 Tesserakte (25.7)

**Block** (`BlockTeleport`, Nachtrag 25.7): Material Eisen, Härte 15, Widerstand 200 (`setResistance` · 3/5 = 120),
Metallklang, kein voller Würfel; Modell (`RenderBlockTeleport`): auf jeder Seite eine 3-px-Platte aus
`tesseract_<typ>.png` (128 × 64, wie die Energiezelle) und ein Kern 0,15–0,85 aus animiertem Geschmolzenem Ender,
auch im Inventar. `ItemBlockTeleport`: Stapel 64, selten (uncommon), Name „Energy/Liquid/Item Tesseract“. Platzieren setzt den Besitzer. Abbauen entfernt ihn aus der Registry; der
Item-Puffer geht dabei verloren (kein `TileInventory`). Schleichen + Schraubenschlüssel baut ihn ab (nur Besitzer oder
OP), Rechtsklick mit Multimeter schreibt „Device Disabled - Owner: …“ bzw. „Frequency (Private): …/Frequency: … -
Owner: …“ und „Mode: Send - Outputs: n“ / „Mode: Receive - Inputs: n“ / „Mode: Send/Receive - Inputs: n - Outputs: n“
(gültige Partner), sonst öffnet Rechtsklick das GUI.

`TileTeleportRoot`: Frequenz (−1 = inaktiv, Licht 7 wenn aktiv), Besitzer, Modus 0 Senden · 1 Empfangen · 2 beides
(Vorgabe), Zugriff 0 öffentlich (Vorgabe) · 1 Freunde · 2 privat (nur der Besitzer ändert ihn; Änderung setzt die
Frequenz auf −1). Fremde privat → Meldung „This Tesseract is private and belongs to …“. Registry global (auch über
Dimensionen) je Typ: Schlüssel (Besitzer bzw. `_public_`, Frequenz) → Sender-/Empfängerlisten; beim Entladen raus.
Partner = gleiche Liste ohne sich selbst, nur mit erfüllter Redstone-Bedingung (beide Seiten). Namen der Frequenzen
je (Besitzer/`_public_`).Typ in `<Welt>/cofh/tesseracts.cfg`; GUI: Frequenzfeld (3 Ziffern), Name (20 Zeichen),
Setzen/Deaktivieren/Speichern/Löschen, Namensliste (10 Zeilen, Auswahl mit Ender-Hintergrund), Reiter Redstone +
Konfiguration (Zugriff, Modus).

- **Energie** (`TileTeleportEnergy`, `PowerProviderTeleport`, verlustbehaftet wie die Leitungen – Motoren nutzen den
  Sonderzweig): Puffer und Annahme 100 MJ, Eingang × 0,75 (`transport.teleport.energy.loss = 25`). `powerRequest` nur
  mit Frequenz, Redstone, Senden und mindestens einem Empfänger. Je Tick gibt ein Sender seinen Puffer reihum an die
  Empfänger; jeder Empfänger höchstens 100 MJ/Tick (`tickEnergyLimit`, beim Empfangen-Modus je Tick neu), verteilt an
  angrenzende MJ-Empfänger (keine Tesserakte) nach deren Anforderung.
- **Flüssigkeit** (`TileTeleportLiquid`): kein Tank; `fill` (nur Sender mit Frequenz und Redstone) reicht direkt reihum
  an die angrenzenden Tanks der Empfänger weiter (auch beim Simulieren werden die Zähler weitergedreht); nicht leerbar.
- **Item** (`TileTeleportItem`): ein Slot, nur befüllbar mit Frequenz, Senden, Redstone und leerem Puffer; ein
  eingefügter Stapel geht sofort an die Empfänger (angrenzendes Rohr nimmt den ganzen Stapel, sonst Inventar; Öfen nur
  oben/unten), der Rest bleibt im Puffer und wird alle 40 Ticks erneut versucht.
- Rezepte: Tesserakt-Rahmen `IGI/GXG/IGI` (Zinn, Gehärtetes Glas, Diamant) → Umfüller + 1000 mB Geschmolzenes Ender
  (vorhanden); Tesserakte siehe §5 (C = Unabgestimmter Tesserakt).

**Details (Nachtrag 25.7, aus `TileTeleport*`, `GuiTeleport`, `TabTeleportConfig`):**

- Registry-Schlüssel ist `getOwner()`: öffentlich `_public_`, sonst der Besitzer (auch bei „Freunde“ – eine
  Freundesliste gibt es in 2.2.2.2 nicht). Ein Tesserakt mit Frequenz −1 ist nicht eingetragen. Senden-Modus trägt
  in die Sender-, Empfangen-Modus in die Empfängerliste ein, „beides“ in beide. Partner eines Senders = Empfängerliste
  derselben Frequenz ohne sich selbst, nur wenn beide ihre Redstone-Bedingung erfüllen. Beim Verlassen der Welt wird
  die Registry geleert.
- Das GUI öffnen dürfen bei nicht öffentlichem Zugriff nur Besitzer und OPs (sonst die Meldung „This Tesseract is
  private and belongs to …! You could have lost your hand!“).
- Senden reihum: ein Sender beginnt bei seinem Fernzähler (`outputTrackerRemote`, danach +1, Umlauf über die
  Partnerzahl); ein Empfänger verteilt reihum ab seinem Seitenzähler über die sechs Nachbarn und springt danach zur
  nächsten Seite mit passendem Nachbarn. Nachbarn, die selbst Tesserakte desselben Typs sind, bekommen nichts.
- Energie: Puffer 100 MJ, Annahme 100 MJ je Lieferung, alles Ankommende × 0,75. Sender geben jeden Tick (Frequenz egal,
  Redstone erfüllt, Puffer > 0) den ganzen Puffer reihum an die Partner. Ein Empfänger hat je Tick 100 MJ Kontingent
  (nur im Empfangen-/Beides-Modus neu gesetzt) und gibt an Nachbarn mit MJ-Bedarf: `min(Bedarf, freier Platz, Rest)`,
  ohne Verlust außer dem des Empfängers (Leitungen/Tesserakte rechnen ihren eigenen Verlust). Anforderung an Rohre und
  Leitungen: `ceil(min(100, frei))`, nur mit Frequenz, Redstone, Senden-Modus und mindestens einem Partner. Motoren
  speisen ihn wie eine Leitung (`IPowerProviderLoss`: mindestens `25 · minPower` je Hub).
- Flüssigkeit: `fill` von jeder Seite nur bei Sendern mit Frequenz und Redstone; die Menge geht reihum an die
  Tanks neben den Partnern; nichts zu entleeren, kein eigener Tank.
- Item: ein Puffer-Slot, von außen nur einfügbar bei Frequenz, Senden-Modus, Redstone und leerem Puffer; das Einfügen
  schickt den Stapel sofort weiter: je Partner reihum über dessen Nachbarseiten – ein Item-Rohr nimmt den ganzen
  Stapel, sonst das Inventar (seitenbezogen). Rest in den Puffer; alle 40 Ticks (Weltzeit) neuer Versuch.
- GUI (`tesseract.png`, 176 × 166, ohne Spielerinventar): Titel, „Device Inactive“ bzw. „Frequency: n“ (8, 28);
  Frequenzfeld (106, 28; 3 Ziffern), Knöpfe Setzen (131, 18) und Deaktivieren (151, 18; nur wenn aktiv), Namensfeld
  (12, 44; 20 Zeichen, 128 breit), Speichern (139, 40; Name nicht leer und nicht schon für eine andere Frequenz
  vergeben) und Löschen (155, 40; Name gehört zu dieser Frequenz), Namensliste (8, 58; 128 breit, 10 Zeilen à 10 px,
  Mausrad, Pfeile 147/65 und 147/137); ausgewählt ist der Name der aktuellen Frequenz, hinterlegt mit Geschmolzenem
  Ender. Klick auf einen Namen setzt Name und Frequenzfeld; Eingabe einer bekannten Frequenz setzt den Namen; Enter im
  Frequenzfeld = Setzen, Enter im Namensfeld = Speichern. Tooltips „Set Frequency“, „Disable“, „Save Frequency“,
  „Delete Frequency“. Klickgeräusche 0,8/0,6/0,7/0,5.
- Namen: je Kategorie `(<Besitzer klein> | _public_).<energy|liquid|item>` eine nach Frequenz-Text sortierte Liste
  Frequenz → Name; Löschen entfernt die Frequenz; leere Namen werden nicht angezeigt.
- Reiter rechts: Redstone und Konfiguration (Farbe 0x089E4C, 100 × 92): Zugriff (Symbol 67 öffentlich, 68 Freunde,
  69 privat, mit Schloss 63, wenn man nicht Besitzer ist) – Linksklick +2, Rechtsklick −2 (mod 3; nur der Besitzer;
  setzt die Frequenz auf −1), Modus (70 Senden, 71 Empfangen, 72 beides) – Linksklick +1, Rechtsklick −1; links
  Information („Use these to quickly transport things across vast distances. Tune the Ender Frequency to determine
  links.“) und Anleitung (Redstone + „The Configuration tab determines the privacy settings and operation mode for
  this device.“).

## 7. Teilphasen

| Teilphase | Inhalt |
|---|---|
| 25.0 | diese Spezifikation |
| 25.1 | Grundlagen: Erze + Weltgen, Materialien/Teile, Crescent Hammer, Multimeter, Glas, Steinwolle, Flüssigkeiten, Werkbank-/Ofenrezepte, Kisten-Loot |
| 25.2 | Maschinenbasis (MJ, Seitenkonfiguration, Redstone-Steuerung, GUI-Reiter) + Elektrischer Ofen, Pulverisierer, Sägewerk |
| 25.3 | Induktionsschmelze, Magmatiegel, Flüssigkeitsumfüller |
| 25.4 | Eisbereiter, Gesteinsextruder, Wassersammler, Tragbarer Tank |
| 25.5 | Dampf- und Magmamotor, Redstone-Energiezelle |
| 25.6 | Redstone-Energieleitung, Liquiduct |
| 25.7 | Tesserakte |
| 25.8 | EMI, Herkunftstexte, Wiki, Client-Test, Version + Paket |
