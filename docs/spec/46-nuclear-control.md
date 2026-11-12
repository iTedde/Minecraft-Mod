# 46 – IC2 Nuclear Control

Quelle: `ftb ultimate/mods/IC2NuclearControl.zip` (**Nuclear Control 1.4.5** von Shedar, `dependencies: IC2`), Pack-Config
`ftb ultimate/config/IC2NuclearControl.cfg` (`alarmRange=64`, `maxAlarmRange=128`, `SMPMaxAlarmRange=256`,
`allowedAlarms=default,sci-fi`, `infoPanelRefreshPeriod=20`, `rangeTriggerRefreshPeriod=20`,
`remoteThermalMonitorEnergyConsumption=1`), Namen aus `ftb ultimate/config/IC2NuclearControl.lang` (nur `en_US`).
Dekompiliert (2026-11-12, Vineflower ohne Remapping, Minecraft-Namen verschleiert):
`reference/decompiled/nuclearcontrol/shedar/mods/ic2/nuclearcontrol/` – `IC2NuclearControl` (Registrierung, Rezepte,
Config), `BlockNuclearControlMain` (ein Block mit 9 Metas), `tileentities/*`, `items/*`, `panel/ScreenManager`
(Bildschirm aus Erweiterungen), `StorageArrayRecipe`, `utils/*`, `crossmod/*`. Dazu die GregTech-Erweiterung
`gregtechmod/common/items/GT_SensorKit_Item`/`GT_SensorCard_Item` (`reference/decompiled/gregtech/`).
Assets im Zip: `img/texture_thermo.png` (16×16-Raster mit Block- und Item-Icons), `img/InfoPanelColorsOn/Off.png`
(Bildschirmfarben), `img/GUI*.png` (9 GUIs), Sounds `sound/alarm-default.ogg`, `sound/alarm-sci-fi.ogg`.

Obfuskierte Vanilla-Namen über `ftb ultimate/instmods/minecraft.jar` aufgelöst (statischer Initialisierer):
`up.o` Eisenbarren, `up.aC` Redstone, `up.aK` Papier, `up.aw` Eimer, `up.aS` Uhr, `up.aT` Glowstonestaub,
`amq.U` Notenblock, `amq.A` Holzbretter, `amq.P` Glas, `amq.bt` Glasscheibe. Farbstoffe als Ore-Dict (`dyeRed` …).

Nutzerwunsch (2026-11-12): „IC2 Nuclear Control“ aus dem Pack. Paket `dev.craft.nuclearcontrol`, alles im IC2-Tab.

## 1 Übersicht

| Block (Registry `craft:…`) | Meta | Name EN / DE | Form | GUI |
|---|---|---|---|---|
| `thermal_monitor` | 0 | Thermal Monitor / Thermomonitor | flach (14×7×14 px), braucht festen Block dahinter | ja |
| `industrial_alarm` | 1 | Industrial Alarm / Industriealarm | flach (12×7×12), braucht festen Block | ja |
| `howler_alarm` | 2 | Howler Alarm / Heulalarm | flach (12×7×12), braucht festen Block | ja |
| `remote_thermal_monitor` | 3 | Remote Thermal Monitor / Fern-Thermomonitor | voller Block | ja |
| `information_panel` | 4 | Industrial Information Panel / Industrie-Infotafel | voller Block | ja |
| `information_panel_extender` | 5 | Information Panel Extender / Infotafel-Erweiterung | voller Block | nein |
| `energy_counter` | 6 | Energy Counter / Energiezähler | voller Block | ja |
| `average_counter` | 7 | Average Counter / Durchschnittszähler | voller Block | ja |
| `range_trigger` | 8 | Range Trigger / Bereichsauslöser | voller Block | ja |

Alle Blöcke: Härte 0,5, Material Metall; Schraubenschlüssel dreht (Shift + IC2-Schraubenschlüssel/Elektroschlüssel
auf Blöcke mit `IRotation` = Infotafel, Fern-Thermomonitor) bzw. baut ab (Drop-Rate 1,0 = immer der Block); Inventar
fällt beim Abbau heraus. Flache Blöcke werden an die angeklickte Fläche gesetzt (alle 6 Seiten); fehlt der Halt, fallen
sie als Item ab (Nachbar-Update). Volle Blöcke drehen sich zum Spieler (horizontal, Infotafel auch nach oben/unten über
die angeklickte Fläche).

| Item (Registry `craft:…`) | Name EN / DE | Stapel |
|---|---|---|
| `thermometer` | Thermometer / Thermometer | 1, Haltbarkeit 102 |
| `digital_thermometer` | Digital Thermometer / Digitalthermometer | 1, elektrisch |
| `remote_sensor_kit` | Remote Sensor Kit / Reaktor-Sensorkit | 1 |
| `energy_sensor_kit` | Energy Sensor Kit / Energie-Sensorkit | 1 |
| `counter_sensor_kit` | Counter Sensor Kit / Zähler-Sensorkit | 1 |
| `liquid_sensor_kit` | Liquid Sensor Kit / Flüssigkeits-Sensorkit | 1 |
| `reactor_sensor_location_card` | Reactor Sensor Location Card / Reaktor-Sensorkarte | 1 |
| `energy_sensor_location_card` | Energy Sensor Location Card / Energie-Sensorkarte | 1 |
| `energy_array_location_card` | Energy Array Location Card / Energiefeld-Karte | 1 |
| `counter_sensor_location_card` | Counter Sensor Location Card / Zähler-Sensorkarte | 1 |
| `liquid_sensor_location_card` | Liquid Sensor Location Card / Flüssigkeits-Sensorkarte | 1 |
| `text_card` | Text Card / Textkarte | 1 |
| `time_card` | Time Card / Zeitkarte | 1 |
| `range_upgrade` | Range Upgrade / Reichweiten-Upgrade | 64 |
| `color_upgrade` | Color Upgrade / Farb-Upgrade | 64 |
| `gt_sensor_kit` | GregTech Sensor Kit / GregTech-Sensorkit (GT-Zusatz, §8) | 1 |
| `gt_sensor_card` | GregTech Sensor Location Card / GregTech-Sensorkarte (§8) | 1 |

Im Pack sind Zähler-/Flüssigkeitskit, Zähler-/Flüssigkeitskarte und Reichweiten-/Farb-Upgrade je ein Item mit Meta 0/1;
in 1.21 je ein eigenes Item (keine Metas).

## 2 Rezepte (`IC2NuclearControl.addRecipes`, alle geformt außer wo vermerkt)

| Ergebnis | Muster | Zutaten |
|---|---|---|
| Thermomonitor | `GGG/GCG/GRG` | G Verstärktes Glas, C Erweiterte Schaltung, R Redstone |
| Heulalarm | `NNN/ICI/IRI` | N Notenblock, I Eisenbarren, C Elektronische Schaltung, R Redstone |
| Industriealarm | `GOG/GHG/GRG` | G Verstärktes Glas, O oranger Farbstoff, H Heulalarm, R Redstone |
| Fern-Thermomonitor | `F/M/T` | F Frequenzsender, M Maschinengehäuse, T Thermomonitor |
| Industrie-Infotafel | `PPP/LCL/IRI` | P Glasscheibe, L hellgrüner Farbstoff, C Elektronische Schaltung, I schwarzer Farbstoff, R Redstone |
| Infotafel-Erweiterung | `PPP/WLW/WWW` | P Glasscheibe, L hellgrüner Farbstoff, W Holzbretter |
| Energiezähler | ` A /FTF` | A Erweiterte Schaltung, F Glasfaserkabel, T MV-Transformator |
| Durchschnittszähler | `FTF/ A ` | wie Energiezähler (Schaltung unten) |
| Bereichsauslöser | `EFE/AMA/ R ` | E Detektorkabel, F Frequenzsender, A Erweiterte Schaltung, M Maschinengehäuse, R Redstone |
| Thermometer | `IG /GWG/ GG` | I Eisenbarren, G Glas, W Wasserzelle |
| Digitalthermometer | `I  /IC / GI` | I Raffiniertes Eisen, C Elektronische Schaltung, G Glowstonestaub |
| Reaktor-Sensorkit | `  F/ D /P  ` | F Frequenzsender, D Digitalthermometer (beliebige Ladung), P Papier |
| Energie-Sensorkit | `  F/ D /P  ` | F Frequenzsender, D EU-Messgerät, P Papier |
| Zähler-Sensorkit | `  F/ C /P  ` | F Frequenzsender, C Elektronische Schaltung, P Papier |
| Flüssigkeits-Sensorkit | `  F/ C /P  ` | F Frequenzsender, C Eimer, P Papier |
| Reichweiten-Upgrade | `CFC` | C Isoliertes Kupferkabel, F Frequenzsender |
| Farb-Upgrade | `RYG/WCM/IAB` | Farbstoffe Rot, Gelb, Grün, Weiß, Magenta, Schwarz, Cyan, Blau; C Isoliertes Kupferkabel |
| Textkarte | ` C /PFP/ C ` | C Elektronische Schaltung, P Papier, F Isoliertes Kupferkabel |
| Zeitkarte | formlos | Elektronische Schaltung + Uhr |
| 2 Elektronische Schaltungen | formlos | Reaktor-Sensorkarte |
| 2 Elektronische Schaltungen | formlos | Energie-Sensorkarte |
| 2 Elektronische Schaltungen | formlos | Zähler-Sensorkarte |
| 1 Elektronische Schaltung | formlos | Flüssigkeits-Sensorkarte |
| Energiefeld-Karte | Sonderrezept (§5.3) | 2–6 Energie-Sensorkarten |

GregTech ändert keine Nuclear-Control-Rezepte (geprüft: `GT_Mod`, Pack-Configs). Alle Zutaten gibt es in Craft
(`reinforced_glass`, `advanced_circuit`, `electronic_circuit`, `machine_casing`, `frequency_transmitter`, `water_cell`,
`eu_meter`, `insulated_copper_cable`, `glass_fibre_cable`, `mv_transformer`, `detector_cable`, `refined_iron_ingot`).
Farbstoffe über die Tags `c:dyes/<farbe>`.

## 3 Reaktor-Überwachung

### 3.1 Thermomonitor (`TileEntityIC2Thermo`)

- Sucht in den **6 Nachbarfeldern** zuerst eine Reaktorkammer (→ deren Reaktor), sonst einen Reaktor.
- Prüft alle `reactor.tickRate / 2` Ticks (IC2-Reaktor: 20 → Takt 10; durch `updateTicker-- > 0` effektiv alle
  11 Ticks); ohne Reaktor jeden Tick.
- Schwelle `heatLevel` (Standard **500**, GUI: Eingabefeld + Knöpfe ±1/±10/±100/±1000/±10000, Bereich 0…1 000 000).
- Zustand: −1 kein Reaktor, 0 Hitze < Schwelle, 1 Hitze ≥ Schwelle. Textur der Front je Zustand (grün/rot/grau).
- **Redstone**: gibt schwaches Signal 15 auf allen Seiten, wenn `Zustand > 0` XOR „invertiert“ (Knopf in der GUI) –
  **nicht** in Richtung Reaktor/Kammer. Also bei „kein Reaktor“ (−1) nur invertiert ein Signal.
- Renderer zeigt die Schwelle als Zahl auf der Oberseite.

### 3.2 Fern-Thermomonitor (`TileEntityRemoteThermo`)

- Slots: 0 Batterie (Einweg-Batterie oder entladbares IC2-Item bis zur eigenen Stufe), 1 Reaktor-Sensorkarte,
  2–4 Upgrades (Transformator-, Energiespeicher-Upgrade von IC2, Reichweiten-Upgrade).
- **EU-Senke** von allen Seiten: Speicher `600 + 10 000 × Energiespeicher-Upgrades`, Paketgrenze
  `32 × 4^Transformator-Upgrades` (max. 4 → 8192), Stufe `1 + Upgrades`. Größeres Paket → Block wird entfernt und
  **Explosion Stärke 0,8** (kein Drop).
- Verbrauch **1 EU pro Tick** (Config). Batterie-Slot: entlädt Items bis zum freien Platz; Einweg-Batterie: −1 Stück,
  +1000 EU, wenn 1000 frei oder Speicher leer.
- Reichweite der Karte: `8 × 2^Reichweiten-Upgrades` (max. 7 → 1024) je Achse; außerhalb → Ziel ungültig.
- Zustand: −2 keine Energie, −1 kein Reaktor, sonst aktuelle Reaktorhitze. Redstone: `Zustand ≥ Schwelle` XOR
  invertiert (Schwelle/GUI wie 3.1). Renderer zeigt Hitze und Schwelle auf der Front.

### 3.3 Thermometer (`ItemToolThermometer`, `ItemToolDigitalThermometer`)

- Rechtsklick auf Reaktor oder Kammer (nur Server): Chatmeldung „Hull heat: <Hitze>“; Thermometer nimmt **10 Schaden**
  (102 Haltbarkeit → 11 Messungen, bei 0 zerbricht es).
- Digitalthermometer: elektrisch, **12 000 EU**, Stufe 1, Transfer 250, liefert keine Energie; **50 EU je Messung**,
  ohne Ladung keine Messung. Meldung „Hull heat: H (Water evaporate: 50 % / melting: 85 % der Maximalhitze)“.
  Im Kreativ-Tab geladen und leer.

## 4 Alarme (`TileEntityHowlerAlarm`, `TileEntityIndustrialAlarm`)

- Aktiv bei Redstone am Block **oder** angrenzendem geladenem Redstone-Draht auf gleicher Höhe (4 Seiten;
  `RedstoneHelper.checkPowered`), geprüft bei Nachbar-Updates.
- Ton (nur Client): Schleife `alarm-<Sound>`, Auswahl in der GUI aus `allowedAlarms` (`default`, `sci-fi`);
  Reichweite per Schieber 0…`maxAlarmRange` (128) in 8er-Schritten, Standard `alarmRange` 64; auf dem Server wird
  höchstens `SMPMaxAlarmRange` (256) genutzt. Lautstärke = Reichweite/16, hörbar nur innerhalb der Reichweite; nach
  Ablauf wird der Ton alle 3 Ticks neu gestartet, solange aktiv.
- Industriealarm: zusätzlich **Licht**, Stufe wechselt alle 3 Ticks; durch einen Klammerfehler im Original ergibt sich
  die Folge **7, 7, 15, 0** (statt 0/7/15/7/0) – wird übernommen. Textur der Lampe je Lichtstufe. Aus → Licht 0.

## 5 Infotafel und Karten

### 5.1 Industrie-Infotafel (`TileEntityInfoPanel`) und Erweiterung

- Slots: 0 Karte, 1 Reichweiten-Upgrade (Stapel zählt, max. 7), 2 Farb-Upgrade.
- **Zeigt nur mit Redstone-Signal** (wie 4.); dann Licht 7 (auch an allen Erweiterungen des Bildschirms).
- Aktualisiert die Karte alle `infoPanelRefreshPeriod` (20 → effektiv 21) Ticks auf dem Server.
- Reichweite `8 × 2^n` je Achse (n ≤ 7 → 1024); Fernkarten außerhalb → „Out of range“, ohne Ziel → „Invalid card“,
  Ziel fehlt → „No target“.
- GUI: je Kartentyp Häkchen der Anzeigezeilen (gespeichert je Kartentyp), „Beschriftung zeigen“, Kartentitel (Textfeld, steht im Karten-Tooltip);
  mit Farb-Upgrade Farbwahl Hintergrund/Text (15 Farben, `COLORS_HEX`; Standard Hintergrund 2 = grün, Text 0).
- **Bildschirm**: Erweiterungen mit gleicher Ausrichtung, die an die Tafel bzw. an das Rechteck grenzen, erweitern die
  Fläche als Rechteck in der Ebene der Front, je Richtung bis zu 20 Schritte (`ScreenManager`); die Tafel ist der Kern,
  der Text wird über die ganze Fläche gezeichnet. Verbundene Ränder ohne Rahmen (Textur je Kante).

### 5.2 Kits → Karten (`ItemSensorKitBase`)

Rechtsklick mit Kit auf ein passendes Ziel (nur Server) ersetzt das Kit in der Hand durch die Karte mit den
Koordinaten; Chat „Remote Sensor mounted, Sensor Location Card received“.

| Kit | Ziel | Karte |
|---|---|---|
| Reaktor-Sensorkit | Reaktor oder Kammer (Position des Reaktors) | Reaktor-Sensorkarte |
| Energie-Sensorkit | EU-Speicher, sonst MJ-Empfänger (§6) | Energie-Sensorkarte (+ Typ EU/MJ) |
| Zähler-Sensorkit | Energie- oder Durchschnittszähler | Zähler-Sensorkarte |
| Flüssigkeits-Sensorkit | Block mit Tank | Flüssigkeits-Sensorkarte |

Karten zeigen im Tooltip Titel und „x: …, y: …, z: …“.

### 5.3 Kartentypen und Anzeigezeilen

| Karte | Zeilen (Häkchen) | Werte |
|---|---|---|
| Reaktor | An/Aus, Hitze, Max. Hitze, Schmelzpunkt, Ausgabe, Restzeit | An = Redstone am Reaktor (grün „On“/rot „Off“, rechts in der ersten Zeile); Schmelzpunkt = 85 % Max.; Ausgabe = `output × EU je Einheit` EU/t (Dampf-Reaktoren gibt es in Craft nicht); Restzeit = längste Restlaufzeit einer Uran-, Thorium- oder Plutoniumzelle × Reaktortakt / 20 als `h:mm:ss` |
| Energie | Aktuell, Speicher, Frei, Füllstand % | aus §6 |
| Energiefeld | wie Energie + „je Speicher“ und „Summe“ | bis zu 6 Ziele; Zeilen „#n …“ je Speicher und/oder Summe |
| Zähler | Energie bzw. Ausgabe | Energiezähler: Zählerstand in EU; Durchschnittszähler: Durchschnitt EU/t |
| Flüssigkeit | Name, Menge, Frei, Kapazität, Füllstand % | erster Tank des Ziels (mB); leer = „None“ |
| Text | – | 10 Zeilen freier Text (bearbeitet über den Einstellungsknopf in der Tafel-GUI, Karte im Kartenplatz) |
| Zeit | – | Spielzeit `hh:mm` (Tageszeit, 0:00 = Mitternacht) |

**Energiefeld-Karte** (`StorageArrayRecipe`, formlos in der Werkbank): 2–6 Energie-Sensorkarten → neue Feldkarte;
Feldkarte + Energie-Sensorkarten → Feldkarte mit mehr Zielen (Summe ≤ 6); Feldkarte allein → `2 × Ziele`
Elektronische Schaltungen. Reichweite wird für jedes Ziel einzeln geprüft (Ziel außerhalb zählt nicht).

## 6 Energiequellen der Sensoren (`EnergyStorageHelper`, `crossmod`)

- **EU** (Typ 0): Blöcke mit IC2-`IEnergyStorage` = BatBox, MFE, MFSU (Craft: Speicherblöcke in `ic2/storage`;
  dazu alle anderen Craft-Blöcke, die einen EU-Speicher mit Kapazität anbieten – Liste in 46.1).
- **MJ** (Typ 1): BuildCraft-`IPowerReceptor` mit `PowerProvider` – gespeicherte/maximale MJ (Craft:
  `BcCapabilities.POWER_RECEPTOR`).
- Flüssigkeit: jeder Block mit Tank (Pack: BC-`ITankContainer`, erster Tank von oben; Craft: Fluid-Capability,
  erster Tank) – damit auch Tanks anderer Mods.

## 7 Zähler und Bereichsauslöser

### 7.1 Energiezähler (`TileEntityEnergyCounter`)

- Eingang nur an der **Front** (Facing, per Schraubenschlüssel), Ausgang an allen anderen Seiten.
- Paketgröße `32 × 4^Transformator-Upgrades` (1 Slot, max. 4); Puffer `2 × Paket`; größere Pakete → Explosion 0,8.
- Gibt jeden Tick **ein volles Paket** ab, wenn der Puffer reicht; zählt die tatsächlich abgegebenen EU
  (`counter`, long). GUI: Zählerstand, Knopf „Reset“.

### 7.2 Durchschnittszähler (`TileEntityAverageCounter`)

- Energiefluss wie 7.1. Ringpuffer mit 11 Messpunkten zu je 20 Ticks; Durchschnitt über die gewählte Zeitspanne
  **1/3/5/10 s** (Knöpfe, Standard 1): `Summe / (Zeitspanne × 20)` EU/t. GUI zeigt den Durchschnitt.

### 7.3 Bereichsauslöser (`TileEntityRangeTrigger`)

- Slots: 0 Energie- oder Energiefeld-Karte, 1 Reichweiten-Upgrade. Prüfung alle `rangeTriggerRefreshPeriod`
  (20 → 21) Ticks.
- Zwei Grenzen (Standard Start 10 000 000, Ende 9 000 000; GUI mit Ziffernknöpfen); `min/max` daraus.
  Energie ≥ max → an (1); < min → aus (0); dazwischen bleibt der letzte Zustand (Hysterese; aus −1 startet „aus“).
  Fehler/kein Ziel → −1. Redstone wie 3.1: `Zustand > 0` XOR invertiert. Textur je Zustand.

## 8 GregTech-Zusatz (`GT_SensorKit_Item`, `GT_SensorCard_Item`)

GregTech legt im Pack ein eigenes Kit + Karte an, sobald Nuclear Control da ist:

- **GregTech-Sensorkit**: Assembler, Elektronische Schaltung + Frequenzsender, **1600 Ticks × 2 EU/t**.
  Karte zurück: Assembler, Karte → 2 Elektronische Schaltungen, 1600 × 2.
- Rechtsklick auf eine GT-Maschine, die Informationen liefert, → Karte. Anzeige drei Zeilen: „Progress:“,
  „<Fortschritt> secs“, „/<Gesamt> secs“.
- In Craft vorhandene GT-Maschinen mit Information: Elektrolyseur, Industrial Blast Furnace, Zentrifuge, Industrial
  Grinder, Implosionskompressor, Vakuumgefrierer (die übrigen GT-Geräte mit Information gibt es in Craft nicht).

## 9 Umsetzung (Vorschlag Teilphasen)

- Paket `dev.craft.nuclearcontrol` (`NcBlocks`, `NcItems`, `NcBlockEntityTypes`, `NcMenus`, `NcSounds`), je Meta ein
  eigener Block; Texturen und Sounds aus dem Pack-Zip geschnitten (`tools/scripts/nuclear_control_textures.py`,
  wie GraviSuite).
- Reaktorzugriff über `ic2/reactor/NuclearReactorBlockEntity` / `ReactorChamberBlockEntity`; Redstone über
  `getSignal` mit Richtungsausnahme; Alarmton als Client-`SoundInstance` mit linearer Abschwächung über die Reichweite.
- Texte auf der Tafel und den Monitoren über Block-Entity-Renderer; Kartendaten als Data Components, Sync per
  Block-Entity-Update.
- Teilphasen: **46.1** Thermometer, Thermomonitor, Fern-Thermomonitor, Alarme · **46.2** Infotafel + Erweiterung,
  Kits/Karten (Reaktor, Energie, Feld, Text, Zeit, Flüssigkeit), Upgrades · **46.3** Energie-/Durchschnittszähler,
  Bereichsauslöser, Zählerkarte, GT-Sensorkit · **46.4** EMI, Herkunft, Wiki, GameTests, Paket.

## 10 Stand

- **46.1** (2026-11-12, umgesetzt): Thermometer, Digitalthermometer, Thermomonitor, Fern-Thermomonitor (mit
  Reaktor-Sensorkit, Reaktor-Sensorkarte, Reichweiten-Upgrade), Heul- und Industriealarm; GUIs aus den Pack-Texturen,
  Texturen/Sounds per `tools/scripts/nuclear_control_textures.py`. IC2-Schraubenschlüssel reicht Shift-Klicks an den
  Block weiter (`shouldPassSneakingClickToBlock`), damit Shift + Schraubenschlüssel das Display dreht.

## 11 Offene Fragen (Nutzer)

1. Umfang **komplett** wie oben, einschließlich GregTech-Sensorkit (§8)? Vorschlag: ja.
2. Energie-Sensor wie im Pack nur **EU + MJ**, oder zusätzlich **FE** (Speicher anderer Mods wie Mekanism-Würfel,
   AE2-Energiezellen)? Vorschlag: wie im Pack (EU + MJ); Flüssigkeiten gehen ohnehin mit allen Mods.
