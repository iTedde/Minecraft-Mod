# 29 – Modular Powersuits

Quelle: `ftb ultimate/mods/ModularPowersuits.jar` (MachineMuse's Modular Powersuits **0.3.2-199** für 1.4.7, Mod-ID
`mmmPowersuits`), Pack-Config `ftb ultimate/config/mmmPowersuits.cfg` (maßgeblich für alle Modulwerte, siehe §3).
Dekompiliert (2026-10-24, Vineflower ohne Remapping, Vanilla-Namen über `ftb ultimate/instmods/minecraft.jar`):
`reference/decompiled/modularpowersuits/net/machinemuse/` – `powersuits/common/Config` (Module), `…/RecipeManager`,
`…/ModCompatability` (Energieumrechnung), `powersuits/item/*` (Rüstung, Werkzeug, Bauteile),
`powersuits/powermodule/**` (Modulklassen), `powersuits/tick/PlayerTickHandler` (Bewegung, Umwelt, Energie),
`powersuits/event/*` (Abbau, Sprung, Fall, Atemluft), `powersuits/network/packets/*` (Einbauen, Ausbauen, Regler,
Modus, Umschalten), `api/*` (Modulrechnung, Energie, NBT). Mitgelieferte API-Pakete (UE, IC2, TE, ICBM, Atomic
Science) wurden entfernt. Texturen/Sounds im Jar unter `resources/machinemuse/` (Rüstung `sebkarmor.png`/
`sebkarmorpants.png` 64×32, Citizen-Joe-Stil `joearmor*.png`, `blankarmor.png`, Icons `sebkicons.png`/
`watericons.png`/`museicons.png` (256er-Raster 16×16), Werkzeug `tool.png` (eigenes Modell), Tisch
`tinkertable_tx.png` (eigenes Modell); Sounds `Glider`, `GUIInstall`, `GUISelect`, `JetBoots`, `Jetpack`,
`JumpAssist`, `SwimAssist`, `WaterElectrolyzer` (`.ogg`).

Vanilla-Namen: `up.o` Eisenbarren, `up.n` Diamant, `up.p` Goldbarren, `up.bq` Goldnugget, `up.aC` Redstone,
`up.K` Faden, `up.aK` Papier, `up.aT` Glowstonestaub, `up.bH` Smaragd, `up.aW` Farbstoff (1 Rosenrot, 2 Kaktusgrün,
4 Lapislazuli), `up.bn` Enderperle, `amq.ae` Wolle, `amq.P` Glas, `amq.bt` Glasscheibe, `amq.aB` Werkbank,
`amq.R` Lapisblock; Materialien `agi.d` Holz, `agi.e` Stein, `agi.f` Eisen, `agi.g` Amboss, `agi.h` Wasser,
`agi.k` Pflanzen, `agi.l` Ranken, `agi.w` Schnee(-decke); Tränke `ll.r` Nachtsicht, `ll.p` Unsichtbarkeit.

Nutzerwunsch (2026-10-24): „Tech-Rüstung“ → Modular Powersuits aus dem Pack, Phase 28. Paketvorschlag
`dev.craft.mps`. Entscheidungen (Nutzer, 2026-10-24): §15.

## 1 Übersicht

Fünf modulare Items (Helm, Brustteil, Hose, Stiefel, Power Tool), ein Block (Tinker Table) und 18 Bauteile. Die Items
können **nichts**, bis am Tinker Table Module eingebaut werden; jedes Modul kostet Bauteile, hat feste Grundwerte und
bis zu drei Regler (0–100 %), die Werte gegeneinander abwägen (z. B. Schub gegen Verbrauch). Energie liegt in
eingebauten Batteriemodulen; alle getragenen Teile + das Werkzeug in der Hand bilden **einen gemeinsamen Vorrat**.

| Item (Registry `craft:…`) | Name EN / DE | Slot | Stapel |
|---|---|---|---|
| `power_armor_head` | Power Armor Head / Power-Rüstungshelm | Kopf | 1 |
| `power_armor_torso` | Power Armor Torso / Power-Rüstungsbrustteil | Brust | 1 |
| `power_armor_legs` | Power Armor Legs / Power-Rüstungshose | Beine | 1 |
| `power_armor_feet` | Power Armor Feet / Power-Rüstungsstiefel | Füße | 1 |
| `power_tool` | Power Tool / Power-Werkzeug | Hand | 1 |
| `tinker_table` (Block) | Power Armor Tinker Table / Power-Rüstungs-Werkbank | – | 64 |

Eigener Kreativ-Tab „Modular Powersuits“ (Items leer, ohne Module). Rüstung: Material Leder (`sv.a`), keine
Haltbarkeit (Schaden kostet Energie, §5), Verzauberbarkeit 0, nicht reparierbar; Werkzeug: `ItemTool` Material
Diamant, Grundschaden 0, Angriffsschaden gegen Entities 1, Verzauberbarkeit 0, nicht reparierbar, Benutzungsdauer
72 000 Ticks, Animation Bogen (für die Plasmakanone). Helm/Hose/Stiefel/Brust entfernen jeden Tick eine eventuelle
Verzauberung (`ench`).

### 1.1 Bauteile (`ItemComponent`, Meta 0–17)

| Meta | Registry `craft:…` | Name EN / DE | Ore-Name (Pack) |
|---|---|---|---|
| 0 | `component_wiring` | Wiring / Verdrahtung | `componentWiring` |
| 1 | `component_solenoid` | Solenoid / Magnetspule | `componentSolenoid` |
| 2 | `component_servo` | Servo Motor / Servomotor | `componentServo` |
| 3 | `component_glider_wing` | Glider Wing / Gleitflügel | `componentGliderWing` |
| 4 | `component_ion_thruster` | Ion Thruster / Ionentriebwerk | `componentIonThruster` |
| 5 | `component_lv_capacitor` | LV Capacitor / NS-Kondensator | `componentLVCapacitor` |
| 6 | `component_mv_capacitor` | MV Capacitor / MS-Kondensator | `componentMVCapacitor` |
| 7 | `component_hv_capacitor` | HV Capacitor / HS-Kondensator | `componentHVCapacitor` |
| 8 | `component_parachute` | Parachute / Fallschirm | `componentParachute` |
| 9 | `component_plating_basic` | Basic Plating / Einfache Panzerung | `componentPlatingBasic` |
| 10 | `component_plating_advanced` | Advanced Plating / Fortgeschrittene Panzerung | `componentPlatingAdvanced` |
| 11 | `component_field_emitter` | Force Field Emitter / Kraftfeldemitter | `componentFieldEmitter` |
| 12 | `component_laser_emitter` | Hologram Emitter / Hologrammemitter | `componentLaserEmitter` |
| 13 | `component_carbon_myofiber` | Carbon Myofiber / Kohlenstoff-Myofaser | `componentCarbonMyofiber` |
| 14 | `component_control_circuit` | Control Circuit / Steuerschaltung | `componentControlCircuit` |
| 15 | `component_myofiber_gel` | Myofiber Gel / Myofaser-Gel | `componentMyofiberGel` |
| 16 | `component_artificial_muscle` | Artificial Muscle / Künstlicher Muskel | `componentArtificialMuscle` |
| 17 | `component_solar_panel` | Solar Panel / Solarzelle | `componentSolarPanel` |

Kohlenstoff-Myofaser, Myofaser-Gel und Künstlicher Muskel haben im Pack **kein Rezept und keine Verwendung** (nur
Kreativ-Tab) – wie im Pack übernehmen. Tooltip (Shift): „For use in Tinker Table.“ + Beschreibung aus `populate()`.
Die Ore-Namen werden in 1.21 nicht gebraucht (keine Fremdmods) und entfallen.

## 2 Energie

- Einheit intern **Joule (J)**, `double`. Pack-Config: **50 J = 1 EU** (`Joules per IC2 EU = 50`), **1 J = 1 MJ**
  (`Joules per MJ = 1`).
- Gespeichert je Item als „Current Energy“ (0 → Eintrag entfernt); Kapazität = Modulwert „Maximum Energy“ (nur
  Batteriemodule, §3.6). Beim Lesen wird auf die Kapazität gekappt (Ausbau einer Batterie vernichtet den Überschuss).
- **Spielervorrat** (`ElectricItemUtils`): Summe aller elektrischen Teile in den 4 Rüstungsslots und der Haupthand.
  **Abbuchen** nacheinander in der Reihenfolge Füße, Beine, Brust, Kopf, Hand: reicht ein Teil, zahlt es allein,
  sonst wird es geleert und der Rest geht ans nächste. **Gutschrift** (Solar, Kinetik) in derselben Reihenfolge bis
  zur jeweiligen Kapazität; Überschuss verfällt.
- **IC2-Laden/Entladen** (`ICustomElectricItem`): Tier 1, „liefert Energie“ ja (kann z. B. andere Items laden),
  keine Ladeanzeige-Tooltips von IC2. `charge(amount EU)`: J = 50 × amount; **Rüstung** begrenzt je Aufruf auf
  Kapazität/200 J (außer `ignoreTransferLimit`), **Werkzeug** ohne Grenze; Rückgabe ganzzahlig abgerundet in EU.
  `discharge` entsprechend. `canUse(amount)`: Ladung > 50 × amount J.
- **MJ/TE-Laden** (`IChargeableItem`): nur das Werkzeug, 1 J je MJ (Rüstung meldet 0). In Craft gibt es keine
  Ladestation für MJ-Items → entfällt (§14).
- UE-Schnittstelle (Spannung 120 V, EMP leert) und ICBM entfallen (Mods nicht vorhanden).
- **Ladebonus beim Einbau** (`MusePacketInstallModuleRequest`): enthält die Einbau-Kosten eines Moduls einen
  NS-/MS-/HS-Kondensator, erhält das Item sofort +20 000 / +100 000 / +750 000 J (je Kosten-Eintrag einmal, nicht je
  Stück), gekappt auf die Kapazität beim nächsten Lesen. Betrifft Basis-/Fortg./Elite-Batterie, Nahkampfhilfe,
  Wasser-Elektrolyseur, Railgun, Plasmakanone. Originalverhalten, übernehmen.
- Tooltip: „Energy: <J kurz>/<max kurz>“ grau kursiv; Werkzeug zusätzlich „Mode:“ + aktiver Modus rot; mit Shift
  „Installed Modules:“ + Liste bzw. „No installed modules! This item is useless until you add some modules at a Tinker
  Table.“, ohne Shift „Press SHIFT for more information.“.

## 3 Modulsystem

### 3.1 Rechnung

Ein Modulwert (z. B. „Jet Thrust“) eines Items = Summe über alle **aktiven** eingebauten Module von
`Grundwert + Σ Multiplikator × Regler` (Regler 0…1, im Item je Modul gespeichert, fehlend = 0). Aktiv heißt:
- Rechtsklick-Module (Plasmakanone, Railgun, Blink Drive, In-Place Assembler): nur, wenn sie der gewählte **Modus**
  des Werkzeugs sind;
- alle anderen: eingebaut und nicht ausgeschaltet („Active“, Standard an). Nur schaltbare Module (`Toggleable`)
  lassen sich über Tastenbelegungen (§9) ausschalten; Batterien, Panzerungen, Kinetik, Solar, Fallschirm, Diamant-
  bohrer, Citizen Joe sind immer an.
- Module, die in der Config verboten sind, zählen nie (im Pack alle erlaubt).

**Config schlägt Code:** Die Pack-Config enthält für fast alle Grundwerte/Multiplikatoren Einträge; wo sie vom Code
abweichen, gilt die Config (Spalte „Pack“). Fehlende Einträge = Codewert.

### 3.2 Modulliste (Pack-Werte)

Kurzform der Regler: „Regler → Wert ×Multiplikator“. Gewicht in g, Energie in J.

| Modul | Teile | Kategorie | schaltbar | Werte (Pack) | Einbaukosten |
|---|---|---|---|---|---|
| Iron Plating | Rüstung | Armor | nein | Plating Thickness → Armor (Physical) ×5, Weight ×10 000 | 1 Einf. Panzerung |
| Diamond Plating | Rüstung | Armor | nein | Plating Thickness → Armor (Physical) ×6, Weight ×6 000 | 1 Fortg. Panzerung |
| Energy Shield | Rüstung | Armor | nein | Field Strength → Armor (Energy) ×6, Energy Per Damage ×500 | 2 Kraftfeldemitter |
| Basic Battery | alle 5 | Energy | nein | Maximum Energy 20 000 + Battery Size ×80 000; Weight 2 000 + ×8 000 | 1 NS-Kondensator |
| Advanced Battery | alle 5 | Energy | nein | Maximum Energy 100 000 + ×400 000; Weight 2 000 + ×8 000 | 1 MS-Kondensator |
| Elite Battery | alle 5 | Energy | nein | Maximum Energy 750 000 + ×4 250 000; Weight 2 000 + ×8 000 | 1 HS-Kondensator |
| Kinetic Generator | Hose | Energy | nein | Energy Generation Per 5 Blocks 250 + Energy Generated ×750; Weight 1 000 + ×3 000 | 2 Servomotor, 1 Steuerschaltung |
| Solar Generator | Helm | Energy | nein | Daytime 100 (Code 2 000), Nighttime 10 (Code 200), keine Regler | 1 Solarzelle, 2 Steuerschaltung |
| Night Vision | Helm | Special | ja | – | 1 Hologrammemitter, 1 Steuerschaltung |
| Flight Control | Helm | Special | ja | – | 1 Steuerschaltung |
| Auto-Feeder | Helm | Environment | ja | Eating Energy Consumption 100 + Efficiency ×100; Auto-Feeder Efficiency 50 + Efficiency ×50 | 2 Servomotor, 1 Steuerschaltung |
| Water Electrolyzer | Helm | Environment | ja | Jolt Energy 1 000 | 1 NS-Kondensator |
| Active Camouflage | Brust | Special | ja | – | 4 Hologrammemitter, 2 Kraftfeldemitter, 2 Steuerschaltung |
| Jetpack | Brust | Movement | ja | Thrust → Jet Energy Consumption ×150, Jet Thrust ×0,16 | 4 Ionentriebwerk |
| Glider | Brust | Movement | ja | – | 2 Gleitflügel |
| Parachute | Brust | Movement | nein | – | 2 Fallschirm |
| Sprint Assist | Hose | Movement | ja | Power → Sprint Energy Consumption ×10, Sprint Speed Multiplier 1 + ×2; Compensation → Sprint Energy Consumption ×2, Sprint Exhaustion Compensation ×1; Walking Assist → Walking Energy Consumption ×10, Walking Speed Multiplier 1 + ×1 | 4 Servomotor |
| Jump Assist | Hose | Movement | ja | Power → Jump Energy Consumption ×25, Jump Boost 1 + ×4; Compensation → Jump Energy Consumption ×5, Jump Exhaustion Compensation ×1 | 4 Servomotor |
| Swim Boost | Hose | Movement | ja | Thrust → Swim Boost Energy Consumption ×100, Underwater Movement Boost ×1 | 1 Ionentriebwerk, 2 Magnetspule |
| Uphill Step Assist | Hose | Movement | ja | – | 2 Servomotor |
| Jet Boots | Füße | Movement | ja | Thrust → Jet Energy Consumption ×75, Jet Thrust ×0,08 | 2 Ionentriebwerk |
| Shock Absorber | Füße | Movement | ja | Power → Impact Energy consumption ×10 (J/m), Distance Reduction ×1 | 2 Servomotor, 2 weiße Wolle |
| Pickaxe | Werkzeug | Tool | ja | Energy 100 + Overclock ×900; Harvest Speed 8 + ×17 (Code 50/950/8/22) | 3 Eisenbarren, 1 Magnetspule |
| Shovel | Werkzeug | Tool | ja | wie Pickaxe | 3 Eisenbarren, 1 Magnetspule |
| Axe | Werkzeug | Tool | ja | wie Pickaxe | 3 Eisenbarren, 1 Magnetspule |
| Diamond Drill Upgrade | Werkzeug | Special | nein | – (nutzt Pickaxe-Werte) | 1 Magnetspule, 3 Diamant |
| Aqua Affinity | Werkzeug | Special | ja | Power → Underwater Energy Consumption ×100, Underwater Harvest Speed 0,2 + ×0,8 | 1 Servomotor |
| Melee Assist | Werkzeug | Weapon | ja | Punch Energy Consumption 10 + Impact ×100 + Carry-through ×20; Melee Damage 2 + Impact ×8; Melee Knockback Carry-through ×1 | 2 Servomotor, 1 NS-Kondensator |
| Plasma Cannon | Werkzeug | Weapon | Modus | Plasma Energy Per Tick 10 + Amperage ×150 + Voltage ×50; Damage At Full Charge 2 + Amperage ×18 (Code ×38); Explosiveness Voltage ×0,5 | 2 Kraftfeldemitter, 2 HS-Kondensator |
| Railgun | Werkzeug | Weapon | Modus | Total Impulse 500 + Voltage ×2 500; Energy Cost 500 + Voltage ×2 500 | 6 Magnetspule, 1 HS-Kondensator |
| Blink Drive | Werkzeug | Special | Modus | Energy 1 000 + Range ×3 000; Range 5 + ×59 m | 1 Ionentriebwerk, 2 Kraftfeldemitter |
| In-Place Assembler | Werkzeug | Special | Modus | – | 1 Steuerschaltung, 1 Werkbank |
| Custom Colour Module | alle 5 | Cosmetic | ja | Red/Green/Blue Intensity → Red/Green/Blue Tint ×1 | 1 Hologrammemitter |
| Transparent Armor | Rüstung | Cosmetic | ja | – | 1 Hologrammemitter |
| Citizen Joe Style | Rüstung | Cosmetic | nein | – | keine (kostenlos) |

Nicht übernommen (brauchen fehlende Mods): **Aurameter** (Thaumcraft), **Multimeter** (UE Basic Components),
**Radiation Shielding** (Atomic Science). „Antigravity Drive“/„Static Generator“ sind nur Konstanten ohne Modul.

### 3.3 Daten im Item (Vorschlag 1.21)

Data Component `craft:mps_modules`: Map Modulname → {aktiv, Regler-Map}; dazu `craft:mps_energy` (double J),
Werkzeug `craft:mps_mode` (String), Helm `craft:mps_food` (Puffer Nahrung/Sättigung, double), Hose
`craft:mps_kinetic` (letzte x/z, int). Modulnamen bleiben die englischen Pack-Namen (stabile Schlüssel).

## 4 Tinker Table

- Block: Material Eisen, Härte 1,5, Explosionswiderstand 1 000 (1.4.7, intern ×3/5 → 1.21-Wert **600**), Klang Metall,
  Lichtwert 0,4 (= 6), nicht undurchsichtig, Spitzhacke Stufe 0, Block-Entity ohne Inventar. Eigenes Modell
  (`TinkerTableModel`) – in 1.21 als Block-Entity-Renderer mit denselben Quadern (§14).
- Rechtsklick (nicht schleichend) öffnet die Tinker-GUI; schleichend normales Platzieren.
- GUI-Rahmen: **Itemauswahl** (alle modularen Items im Spielerinventar), **Iteminfo** (Rüstung, Energie, Gewicht),
  **Modulauswahl** nach Kategorie (alle für das Item gültigen Module, installierte markiert), **Einbauen/Ausbauen**
  (Kostenliste mit vorhandenen Mengen, Knopf „Install“ bzw. „Salvage“), **Regler** (je Regler 0–100 %).
- **Einbauen** (Server): Modul erlaubt, noch nicht eingebaut und alle Kosten im Spielerinventar (Kreativ: kostenlos)
  → Modul mit Standardwerten (aktiv, Regler 0), Ladebonus (§2), Kosten abziehen. Sound `GUIInstall`.
- **Ausbauen**: Modul entfernen, **jedes Kostenteil einzeln mit 90 % Chance** zurück (`Salvage Ratio` 0,9, erst auf
  passende Stapel, dann freie Slots, Rest gedroppt); **NS-/MS-/HS-Kondensatoren gehen immer verloren**. Regler gehen
  verloren.
- **Regler**: Server klemmt auf 0…1 und speichert nur bei eingebautem Modul.
- Sound `GUISelect` beim Anklicken.

## 5 Rüstung

- **Schutzwert je Teil** = min(6, Armor (Physical) + (Spielervorrat > Energy Per Damage ? Armor (Energy) : 0)).
  Nicht-Spieler-Träger: 2.
- Forge-`ISpecialArmor`: Priorität 1, Absorption **0,04 × Schutzwert**, AbsorbMax **(int)Schutzwert × 75**;
  Rüstungsanzeige (int)Schutzwert. Volle Rüstung mit 4 × 6 = 96 % Absorption.
- `damageArmor`: Energy Per Damage × Schaden J vom Spielervorrat (Nicht-Spieler: vom Teil selbst). Keine Haltbarkeit.
- In Craft über die vorhandene Rüstungsrechnung (`abweichungen.md` „Rüstungsrechnung“) wie IC2-/GraviSuite-Teile.
- Textur: Helm/Brust/Stiefel `sebkarmor.png`, Hose `sebkarmorpants.png`; mit **Citizen Joe Style** `joearmor*.png`;
  mit aktivem **Transparent Armor** unsichtbar (`blankarmor.png`). **Färbung**: Farbe = (clamp(1+R−G−B),
  clamp(1+G−R−B), clamp(1+B−R−G)) aus den Tint-Werten, gilt für Rüstungs- und Werkzeug-Icon und das getragene
  Modell. (Der Pack-Code färbt nur, wenn ein Modul „Red Tint“ … heißt – das gibt es nicht; das Färben über
  `getColor` greift dennoch, weil `b(ur)` die Farbe immer liefert. In 1.21: gefärbt, sobald das Farbmodul eingebaut
  und aktiv ist.)
- Nicht verzauberbar; Giftschutz (Atomic Science) entfällt.

## 6 Bewegung (`PlayerTickHandler`, jeden Spieler-Tick auf Client und Server)

Eingaben (Vor/Seit, Springen, Schleichen, „Runter“ = Taste **Z** nur bei offenem Spielfenster) schickt der Client
jeden Tick an den Server (`PlayerUpdate`). Alle Kosten eines Ticks werden gesammelt (`totalEnergyDrain`) und am Ende
einmal abgebucht; ein Modul läuft nur, wenn Kosten + bisherige Sammelkosten < Vorrat.

**Gewicht**: Summe „Weight“ aller getragenen modularen Teile + Werkzeug; Tragkraft **25 000 g**.
Gewichtsfaktor = 1, wenn Gewicht < 25 000, sonst 25 000/Gewicht. Über der Tragkraft wird jede Tick die horizontale
Geschwindigkeit mit 25 000/Gewicht multipliziert.

### 6.1 Sprint Assist (Hose)
- Beim Sprinten, wenn Sprint Energy Consumption < Vorrat: Kosten = Verbrauch × horizontale Geschwindigkeit × 5 J;
  Bodenbeschleunigung (`landMovementFactor`) × Sprint Speed Multiplier; Erschöpfung −0,01 × round(v·100)·0,01 ×
  Kompensation; Luftbeschleunigung = Boden × 0,5.
- Gehen (nicht sprinten): Kosten Walking Energy Consumption × v × 5, Faktor × Walking Speed Multiplier, Luft =
  Boden × 0,5.
- Wird sofort abgebucht (nicht gesammelt).

### 6.2 Jump Assist (Hose)
- Sprung-Event: Kosten Jump Energy Consumption (einmal je Sprung) < Vorrat → abbuchen, Sprungzähler =
  Jump Boost × 2; Erschöpfung −0,8 × Kompensation (sprintend) bzw. −0,2 × Kompensation.
- Je Tick, solange Springen gehalten und nicht im Wasser: Zähler > 0 → motionY += 0,15 × min(Zähler, 1) ×
  Gewichtsfaktor, Zähler −1; Luftbeschleunigung = Boden × 0,7. Springen losgelassen → Zähler 0.
- Sound `JumpAssist`.

### 6.3 Uphill Step Assist (Hose)
Aktiv: Stufenhöhe 1,001; inaktiv und Stufenhöhe = 1,001 → 0,5001 (1.4.7-Standard 0,5). In 1.21 über das
Attribut `step_height` (Modifikator +0,401 auf 0,6, vgl. §14).

### 6.4 Jetpack (Brust) und Jet Boots (Füße)
- Schub/Verbrauch beider Module addiert; Bedingung Verbrauch + Sammelkosten < Vorrat; Schub × Gewichtsfaktor.
- **Ohne Flight Control**: Springen gehalten und motionY < 0,5 → Kosten = voller Verbrauch; ohne Vorwärts motionY +=
  Schub, mit Vor/Zurück motionY += Schub/2 und horizontal ±Schub/2 in Blickrichtung.
- **Mit Flight Control (Helm)** und Schub > 0, unabhängig von der Sprungtaste: Wunschrichtung = Blick × sign(vor) +
  Seitwärts × sign(seit) + (Springen ? +1 : 0) − (Z ? 1 : 0) vertikal, normiert. Zuerst Fallen bremsen (motionY < 0
  und Wunsch nicht nach unten), Fall unter −1 kappen, horizontale Drift abbauen, wenn keine Wunschrichtung (Länge 0),
  dann Restschub in Wunschrichtung; Kosten = Verbrauch × jeweils verbrauchter Geschwindigkeitsanteil (Bremsen) bzw.
  × (vx²+vy²+vz²) (Schub). Damit Schweben ohne Taste.
- Sounds `Jetpack`/`JetBoots` (Schleife solange Schub).

### 6.5 Glider und Parachute (Brust)
- **Glider** (Schleichen, motionY < −0,1, ohne Fallschirm oder mit Vorwärts): Δ = min(0,08, −0,1 − motionY);
  motionY += Δ, horizontal += Δ in Blickrichtung, Luftbeschleunigung +0,03. Sound `Glider`. Kostet keine Energie.
- **Parachute** (Schleichen, motionY < −0,1, ohne Gleiter oder ohne Vorwärts): Gesamtgeschwindigkeit ×
  Gewichtsfaktor = g; jede Achse × 0,1/g. Kostenlos.

### 6.6 Swim Boost (Hose)
Im Wasser bei Bewegungstaste: Rate = Underwater Movement Boost × 0,05; Kosten Swim Boost Energy Consumption je
Tick; Geschwindigkeit += Blick × Rate × vor / moveRatio (moveRatio = vor² + seit² + 0,04 bei Springen/Schleichen);
Springen +Rate × 0,2/moveRatio, Schleichen −…. Sound `SwimAssist`. (Seitwärts allein: Division durch moveRatio
ohne Vorwärtsanteil → kein Schub, nur Kosten – Originalverhalten.)

### 6.7 Shock Absorber (Füße)
Fall-Event, Fallhöhe > 3: Absorb = Höhe × Distance Reduction; Kosten = Absorb × Impact Energy consumption J; wenn
< Vorrat: abbuchen, Fallhöhe −Absorb.

### 6.8 Fallhöhe (Entscheidung §15: nur mit MPS-Teil)
Am Ende **jedes** Spieler-Ticks, für **jeden** Spieler (auch ohne Rüstung), setzt MPS die Fallhöhe aus der
aktuellen Fallgeschwindigkeit neu: Fallhöhe = 0,0392 × (min(motionY, 0)/−0,0784)². Das ersetzt die Vanilla-
Aufsummierung global (z. B. wird Fallschaden nach Bremsen durch Jetpack/Gleiter/Fallschirm entsprechend kleiner).
**Craft:** nur für Spieler, die mindestens ein Power-Rüstungsteil tragen oder das Power Tool in der Haupthand halten
(Entscheidung Nutzer); sonst Vanilla.

## 7 Umwelt und Spezial (Helm, Brust)

- **Night Vision** (Helm): fehlt der eigene Effekt oder hat er < 210 Ticks Rest → Nachtsicht 500 Ticks (Kennung
  Verstärkung −337), 5 J. Aus/zu wenig Energie → eigener Effekt wird entfernt. (Schleifenfehler: Suche bricht beim
  ersten eigenen Effekt ab – bei Nachtsicht + Tarnung wird die Tarnung jede Tick neu gesetzt bzw. nicht entfernt;
  übernehmen.)
- **Active Camouflage** (Brust): wie oben, Unsichtbarkeit 500 Ticks (Kennung 81), 50 J.
- **Auto-Feeder** (Helm): Hunger 20 − Nahrung = Bedarf. Solange Bedarf > Puffer: Nahrungsitems aus dem Inventar ab
  Slot 0 (Hotbar zuerst) **verzehren** → Puffer += Nährwert × Effizienz/100, Sättigungspuffer += Sättigung ×
  Effizienz/100; Chat „Feeder module: Ate a <Name>“. Dann gegessen = (int) min(Bedarf, Puffer, Bedarf × Eating Energy
  Consumption × Restvorrat) (Originalformel – multipliziert statt dividiert, praktisch min(Bedarf, Puffer) sobald
  Energie > 0); Nahrung + gegessen, Sättigungsmodifikator 1,0 wenn Sättigungspuffer ≥ 1 (dann −1), sonst 0;
  Kosten = Bedarf × Eating Energy Consumption × gegessen J (gesammelt).
- **Water Electrolyzer** (Helm, LivingUpdate): Luft < 10 und Vorrat > Jolt Energy → 1 000 J, Luft = 300. Sound
  `WaterElectrolyzer`.
- **Solar Generator** (Helm, Server, nicht in Welten ohne Himmel): alle 80 Welt-Ticks; Himmel über Kopf+1 sichtbar
  und kein Regen (Regenprüfung nur alle 20 Ticks, sonst „kann regnen“) → Tag 100 J, Nacht 10 J Gutschrift.
- **Kinetic Generator** (Hose): Merkt x/z (ganzzahlig); bei Reiten/Wasser jedes Tick neu gesetzt; ab ≥ 5 Blöcken
  horizontaler Entfernung neu setzen und Energy Generation Per 5 Blocks gutschreiben. Nur wenn das verschleierte
  Entity-Feld `am` falsch ist (vermutlich `isAirBorne`; in 28.3 gegen `minecraft.jar` prüfen).

## 8 Power Tool

### 8.1 Abbau
- Werkzeugtyp je Block: **Pickaxe** wenn eine Eisenspitzhacke wirksam wäre oder (keine Diamantspitzhacke nötig und
  Material Stein/Eisen/Amboss); **Shovel** wenn Eisenschaufel wirksam oder Material Schnee; **Axe** wenn Eisenaxt
  wirksam oder Material Holz/Pflanzen/Ranken; **Diamond Drill** wenn Diamantspitzhacke wirksam oder Material
  Stein/Eisen/Amboss (also auch Obsidian). Reihenfolge Pickaxe → Shovel → Axe → Diamond.
- **Abbaubarkeit** (HarvestCheck): passender Typ und Vorrat > Verbrauch des Typs.
- **Tempo** (BreakSpeed): Vanilla-Tempo (Werkzeug selbst 1,0) × Harvest Speed des Typs (Diamond nutzt Pickaxe-
  Werte → ohne Pickaxe-Modul 0, Diamond-Upgrade also nur mit Pickaxe sinnvoll); kein Typ → 1,0. Unter Wasser mit
  Aqua Affinity, Tempo > 1 und Vorrat > Underwater Energy Consumption: × 5 × Underwater Harvest Speed.
- **Kosten je abgebautem Block**: Verbrauch des Typs (Diamond: Pickaxe-Verbrauch), unter Wasser + Aqua Affinity
  zusätzlich Underwater Energy Consumption; kein Typ → 0. Auch bei Härte 0.

### 8.2 Nahkampf (Melee Assist)
Treffer: Vorrat > Punch Energy Consumption → abbuchen, **zusätzlicher** Schaden Melee Damage (int) als Spieler-
Schaden und bei Erfolg Rückstoß Blick × Knockback (y: |Blick.y + 0,2| × Knockback). Durch die Unverwundbarkeitszeit
wirkt im Original nur der Anteil über dem ersten Treffer (Grundschaden 1) – übernehmen.

### 8.3 Modi (Rechtsklick-Module)
- Modus = Name eines eingebauten Rechtsklick-Moduls; wechseln mit **Schleichen + Mausrad** bei gehaltenem Werkzeug
  (zyklisch; HUD-Anzeige des Wechsels). Ohne gespeicherten Modus ist keiner aktiv; der erste Mausrad-Schritt geht vom
  ersten Modul aus weiter.
- Rechtsklick führt das aktive Rechtsklick-Modul aus:
  - **Blink Drive**: Vorrat > Kosten → abbuchen, Sound `mob.endermen.portal`, Strahl bis Reichweite (Entities und
    Blöcke); Teleport auf den Treffpunkt (Block: unten −2, Seiten ±0,5 nach außen, oben unverändert). Kein Treffer:
    nur Kosten.
  - **Railgun**: Vorrat > Kosten → abbuchen; Strahl 64 m; Schaden = Impulse/100 (5–30, int), Rückstoß = Schaden/20
    auf Ziel, gleicher Rückstoß entgegen auf den Spieler; Partikelspur (`townaura`), Sound `random.bow` zweimal.
  - **Plasma Cannon**: Vorrat > 500 J → Aufladen (Gebrauch wie Bogen). Loslassen: Ladeticks = max(gehalten, 10);
    Kosten = Energy Per Tick × Ladeticks; reicht der Vorrat nicht, passiert nichts. Geschoss (Wurfgeschoss ohne
    Schwerkraft, Geschwindigkeit 1 Block/Tick, max. 200 Ticks, erlischt im Wasser): Größe = min(Ladeticks, 50);
    Treffer Entity: Schaden (int)(Größe/50 × Damage At Full Charge); immer Explosion Stärke Größe/50 × 3 ×
    Explosiveness mit Blockschaden; Flammenpartikel.
  - **In-Place Assembler**: öffnet eine normale 3×3-Werkbank („A larger crafting grid“ – tatsächlich 3×3).

## 9 Tasten

- **Mausrad + Schleichen**: Werkzeugmodus (§8.3).
- **Z** („Runter“ für Flight Control) fest.
- **K** öffnet die **Tastenbelegungs-GUI**: Spieler legen beliebige Tasten an und ziehen schaltbare Module darauf;
  Tastendruck schaltet alle gebundenen Module auf **allen getragenen Teilen** abwechselnd an/aus (erster Druck
  „an“), Chat „Toggled <Modul> on/off“. Belegungen clientseitig in einer Datei gespeichert.
- 1.21: K als `KeyMapping` (Kategorie „Craft“); die freien Belegungen über eigene Tasten-Erfassung in der GUI,
  gespeichert in `config/craft-mps-keybinds.txt`; Umschalten per Payload.

## 10 Rezepte

Pack-Config: `Vanilla Recipes = true`, `Gregtech Recipes = true`, IC2/UE/TE = false. **Beide Sätze werden
registriert.** Die GT-Rezepte nutzen aber Ore-Namen ohne `crafting`-Präfix (`circuitTier04`, `monitorTier02`,
`rawMachineTier02`, `100kEUStore`, `itemMachineParts` …), die GT 1.4.7 nie registriert (dort `craftingCircuitTier04`
usw.), sowie `plateIridium` (GT hat keine Iridium-Materialplatte; die IC2-Iridiumplatte heißt `plateIridiumAlloy`).
Diese Rezepte sind im Pack **unbaubar**. Zudem verdecken die zuerst registrierten Vanilla-Rückgewinnungen
(Panzerung → 5 Eisen/5 Diamant) die GT-Varianten (→ 5 Stahl/5 Titan) mit identischem Muster.

### 10.1 Im Pack baubar – Vanilla-Satz
| Ergebnis | Muster | Zutaten |
|---|---|---|
| 8 Verdrahtung | `GRG` | G = Goldnugget, R = Redstone |
| Magnetspule | `WIW/WIW/WIW` | W = Verdrahtung, I = Eisenbarren |
| Servomotor | ` W /EIE` | W = Verdrahtung, E = Magnetspule, I = Eisenbarren |
| Gleitflügel | ` II/II /I  ` | I = Eisenbarren |
| Ionentriebwerk | ` FE/IG /IFE` | F = Kraftfeldemitter, E = Magnetspule, I = Eisenbarren, G = Glowstonestaub |
| NS-Kondensator | `WPI/W W` | W = Verdrahtung, P = Papier, I = Eisenbarren |
| MS-Kondensator | `GPL/W W` | G = Goldnugget, P = Papier, L = Lapislazuli, W = Verdrahtung |
| HS-Kondensator | `EPG/W W` | E = Enderperle, P = Glas, G = Glowstonestaub, W = Verdrahtung |
| Fallschirm | `WWW/S S` | W = weiße Wolle (Meta 0), S = Faden |
| Einfache Panzerung | `II/CI/II` | I = Eisenbarren, C = Verdrahtung |
| Fortg. Panzerung | `II/CI/II` | I = Diamant, C = Magnetspule |
| 5 Eisenbarren | `P` | P = Einfache Panzerung (Rückgewinnung) |
| 5 Diamanten | `P` | P = Fortg. Panzerung (Rückgewinnung) |
| Kraftfeldemitter | `SES/ESE/SES` (gespiegelt erlaubt) | S = Magnetspule, E = Enderperle |
| Hologrammemitter | `YTG/TWT/BTR` | Y = Glowstonestaub, T = Glas, G = Kaktusgrün, W = Verdrahtung, B = Lapislazuli, R = Rosenrot |
| Steuerschaltung | `WCI/RGC/IRW` (gespiegelt) | W = Verdrahtung, C = Kaktusgrün, I = Goldbarren, R = Redstone, G = Glowstonestaub |
| Solarzelle | `PPP/PLP/PPP` | P = Glasscheibe, L = Lapislazuli |
| Tinker Table | `ILI/LEL/ILI` | I = Eisenbarren, L = Lapislazuli, E = Smaragd |
| Helm | `III/C C` | I = Eisenbarren, C = Verdrahtung |
| Brustteil | `I I/CIC/III` | wie oben |
| Hose | `III/C C/I I` | wie oben |
| Stiefel | `C C/I I` | wie oben |
| Power Tool | ` C /CI / IC` | wie oben |

(„Kaktusgrün“ = 1.21 grüner Farbstoff, „Rosenrot“ = roter Farbstoff.)

### 10.2 Im Pack baubar – GT-Satz (zusätzlich)
| Ergebnis | Muster | Zutaten |
|---|---|---|
| 2 Verdrahtung | `CCC/SSS/CCC` | C = unisoliertes Kupferkabel (IC2), S = Silberbarren |
| Fallschirm | `WWW/S S/CNC` | W = Wolle, S = Faden, C = Kohleplatte, N = Stickstoffzelle |
| Hologrammemitter | `LLL/RGB/LLL` | L = Luminator, R = Rubin, G = Grüner Saphir, B = Saphir |
| Magnetspule | `WSW/WSW/WSW` | W = Verdrahtung, S = Stahlbarren |

### 10.3 Im Pack unbaubar (GT, nur zur Dokumentation)
Steuerschaltung (`WCI/RGC/IRW`, fortg./Datenspeicher-/Energiefluss-Schaltung, Elektrum), Tinker Table
(`CVC/IEI/IMI`, Monitor, fortg. Maschinenblock, raff. Eisen), Helm `ACA/MVM`, Brust `AMA/ACA/AAA`, Hose `MCM/A A/A A`,
Stiefel `M M/ACA`, Werkzeug `A A/AMA/ C ` (Aluminium, Datenspeicher-Schaltung, Maschinenteile, Monitor),
Kondensatoren (`IWI/IBI/IBI`, raff. Eisen/Titan/Chrom + Energiekristall/Lapotron/Lapotronic Orb), Einf. Panzerung
(Stahl + el. Schaltkreis), Fortg. Panzerung (Titan + fortg. Schaltkreis), Gleitflügel (Magnalium, Kohleplatte,
Magnetspule, fortg. Schaltkreis), Servomotor (Stahl, Messing, fortg. Schaltkreis, Magnetspule), Kraftfeldemitter
(Wolframstahl, Magnetspule, Energiefluss-Schaltung, Supraleiter), Ionentriebwerk (`ISI/FCF/N N`: Iridiumplatte,
Supraleiter, Iridium-Neutronenreflektor, HS-Kondensator, Kraftfeldemitter), Solarzelle (verstärktes Glas,
Iridiumplatte, Energiefluss-Schaltung); Rückgewinnung Panzerung → 5 Stahl / 5 Titan (verdeckt).

**Entschieden (Nutzer, 2026-10-24):** wie im Pack effektiv – §10.1 + §10.2; §10.3 entfällt.

## 11 Darstellung und Sounds

- Rüstung: Standard-Biped-Texturen (64×32) aus dem Jar, eingefärbt (§5).
- Item-Icons aus `sebkicons.png` (Rüstung), `watericons.png` (Werkzeug-Icon, Solarzelle Index 229), Bauteile und
  Modul-Icons (`MuseIcon`) aus `museicons.png`/`watericons.png`; Raster 16×16 auf 256×256.
- Werkzeug in der Hand: eigenes 3D-Modell (`ToolModel`, Textur `tool.png`) mit Färbung; in 1.21 flaches Item-Icon
  (Vorschlag, §14). Tinker Table: `TinkerTableModel` → JSON-Modell aus Quadern mit `tinkertable_tx.png`.
- Plasma-Geschoss: leuchtende Kugel nach Größe (`RenderPlasmaBolt`); Blink-Drive-Geschoss-Klasse ist ungenutzt.
- Sounds aus dem Jar unter `craft:mps/<name>`.

## 12 Netzwerk (1.21-Payloads)

`MpsInstall`, `MpsSalvage`, `MpsTweak` (Slot, Modul, Regler, Wert), `MpsModeChange` (Slot, Modus), `MpsToggle`
(Modul, an/aus), `MpsInput` (vor, seit, springen, schleichen, runter je Tick), Plasma-Geschoss über normales Entity-
Spawn-Paket. Bewegung läuft wie im Pack auf beiden Seiten; Energie wird vom Server abgebucht und synchronisiert.

## 13 Tests (GameTest)

- Modulrechnung: Grundwert + Regler, nur aktive Module, Rechtsklick-Module nur im Modus, Config-Werte (Solar 100/10,
  Pickaxe 100/900/8/17, Plasma ×18).
- Energie: gemeinsamer Vorrat, Abbuchreihenfolge Füße→Hand, Gutschrift bis Kapazität, Kappung nach Batterie-Ausbau,
  IC2-Laden 50 J/EU mit Rüstungsgrenze Kapazität/200, Werkzeug ohne Grenze.
- Tinker Table: Einbau zieht Kosten ab, Kreativ kostenlos, doppelter Einbau abgelehnt, Ladebonus (NS +20 000),
  Ausbau 90 %-Rückgabe (mit festem Zufall), Kondensatoren verloren, Regler 0…1 geklemmt.
- Rüstung: Schutzwert min 6, Energieschild nur mit Vorrat > Energy Per Damage, Absorption/AbsorbMax, Schaden kostet
  Energy Per Damage × Schaden.
- Bewegung: Jetpack-Schub und Kosten, Flight-Control-Schweben, Gleiter/Fallschirm-Geschwindigkeit, Sprungzähler,
  Stoßdämpfer-Fallhöhe, Gewichtsbremse, Stufenhöhe, Fallhöhenformel.
- Umwelt: Nachtsicht/Tarnung-Effekte und Kosten, Auto-Feeder-Puffer, Atemluft 300, Solar alle 80 Ticks, Kinetik je 5
  Blöcke.
- Werkzeug: Abbaubarkeit/Tempo je Typ, Obsidian nur mit Diamant-Upgrade, Kosten je Block, Aqua Affinity, Nahkampf,
  Moduswechsel, Railgun-Schaden, Blink-Teleport, Plasma-Kosten/Explosion, Assembler öffnet Werkbank.
- Alle Rezepte aus §10.1/§10.2.

## 14 Abweichungen 1.21 (Vorschlag; → `abweichungen.md` bei der Umsetzung)

1. Fehlende Fremdmods: Aurameter, Multimeter, Radiation Shielding, UE/ICBM/TE-Schnittstellen entfallen; MJ-Laden
   des Werkzeugs entfällt (keine MJ-Ladestation für Items in Craft).
2. Energie als Data Component (double J) statt NBT; Module als Data Component.
3. Bewegungsfaktoren (`landMovementFactor`/`jumpMovementFactor`) → transiente Attribut-Modifikatoren
   (`movement_speed`) bzw. direkte Geschwindigkeitsänderung; Stufenhöhe über `step_height` (1.21-Standard 0,6
   statt 0,5: aktiv 1,001, inaktiv Modifikator weg).
4. Werkzeug ohne 3D-Modell (flaches Icon, eingefärbt); Tinker Table als Block-Entity-Renderer (1:1-Quader).
5. Tastenbelegungs-GUI: Datei in `config/` statt `.minecraft`-Wurzel.
6. Meldungen übersetzbar (DE/EN); „Toggled …“ und „Feeder module: Ate a …“ als Systemnachricht.
7. Unsichtbarkeits-/Nachtsichteffekt ohne negative Verstärkung (1.21 erlaubt 0–255): Kennung über eigene
   Effekt-Instanz-Prüfung (Dauer ≤ 500, ohne Partikel), Verhalten gleich.
8. Fallhöhen-Neuberechnung (§6.8) nur mit getragenem MPS-Teil bzw. Power Tool in der Hand statt für alle Spieler
   (Entscheidung Nutzer).
9. Rüstungsanzeige zählt den Energieschild nur mit der eigenen Ladung des Teils (Schaden wie im Pack mit dem
   ganzen Vorrat); Ausbau-Rückgabe nur ins Hauptinventar (Original füllte auch leere Rüstungsslots); das
   Entfernen von Verzauberungen entfällt (Teile nicht verzauberbar).

## 15 Entscheidungen (Nutzer, 2026-10-24)

1. **Umfang**: alle Module außer den drei mod-abhängigen (Aurameter, Multimeter, Radiation Shielding; §3.2).
2. **Rezepte**: wie im Pack tatsächlich baubar – Vanilla-Satz + vier GT-Rezepte (§10.1/10.2); die unbaubaren
   GT-Rezepte (§10.3) entfallen.
3. **Fallhöhe** (§6.8): Neuberechnung nur mit getragenem MPS-Teil bzw. Power Tool in der Hand (Abweichung).

## 16 Teilpakete (Phase 28)

| Teil | Inhalt |
|---|---|
| 28.1 | Paket `…mps`, Energie (J, IC2-Laden), Bauteile, Rüstung + Werkzeug als Items, Modulsystem (Data Components, Rechnung, Config-Werte), Batterien, Panzerungen/Energieschild in der Rüstungsrechnung, Tinker Table (Block, GUI: Auswahl, Info, Einbau/Ausbau/Regler, Payloads), Rezepte, Texturen, Lang |
| 28.2 | Bewegung: Eingabe-Payload, Gewicht, Sprint/Gehen, Sprung, Stufe, Jetpack/Jet Boots/Flight Control, Gleiter, Fallschirm, Schwimmen, Stoßdämpfer, Fallhöhe, Sounds |
| 28.3 | Umwelt/Spezial/Kosmetik: Nachtsicht, Tarnung, Auto-Feeder, Elektrolyseur, Solar, Kinetik; Tastenbelegungs-GUI (K) + Umschalten; Färbung, Transparent, Citizen Joe |
| 28.4 | Power Tool: Abbau (Spitzhacke/Schaufel/Axt/Diamant/Aqua Affinity), Nahkampf, Modi (Mausrad), Railgun, Blink Drive, Plasmakanone (Entity, Renderer), In-Place Assembler |
| 28.5 | EMI (Info-Seiten), Herkunft, Wiki, Testplan, Version/Paket |
