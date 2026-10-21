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
  wie BC-Motoren; Werte in 25.5.
- **Redstone-Energiezelle** (`TileEnergyCell`): MJ-Speicher mit einstellbarer Ein-/Ausgabe je Seite; Werte in 25.5.
- **Redstone-Energieleitung** (`TileConduitEnergy`, `GridEnergy`, `PowerProviderGrid`): Netz mit 5 % Verlust
  (`transport.conduit.energy.loss = 5`).
- **Liquiduct** (`TileConduitLiquid`, `GridLiquid`): Flüssigkeitsnetz.
- **Tesserakte** (`TileTeleport*`, `TeleportRegistry`): Frequenz (Name) je Besitzer/öffentlich; Energie mit 25 %
  Verlust, max. 100 MJ/t Übertragung; Items und Flüssigkeiten sofort; Namen in `<Welt>/cofh/tesseracts.cfg`.

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
