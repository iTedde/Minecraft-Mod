# 13 – GregTech-Maschinen (Phase 6b)

Quelle: `reference/decompiled/gregtech/gregtechmod/` (GregTech 2.90, Vanilla-Namen verschleiert). Pack-Config:
`ftb ultimate/config/GregTech/GregTech.cfg` – Abschnitt `machines`: **alle** Schalter an (`explosions_on_nonwrenching`,
`fire_causes_explosions`, `lightning_causes_explosions`, `machines_flammable`, `rain_causes_explosions`,
`wirefire_on_explosion`, `constant_need_of_energy`); `addonrecipes.AutomaticBasicMachines=true`.
Rezeptdauern überschreibbar in `RecipesAdvancedConfig.cfg` (im Pack für den Plattenbieger keine Abweichung).

## 1. Gemeinsame Basis (`BaseMetaTileEntity` + `GT_MetaTileEntity_BasicMachine`)

GT-Maschinen sind Metadaten eines Blocks (`GT_BlockMetaID_Machine`, ID-Nummer = Maschine). Umsetzung: je Maschine ein
eigener Block mit gemeinsamer Basisklasse.

### 1.1 Block

- Härte 3,0, `setResistance(10)` → gespeichert 30, wirksam 30/5 = **6,0** (1.21-Wert). Keine Mob-Spawns, normaler Würfel, Redstone-Verbindung immer möglich.
- **Brennbar** (`machines_flammable`): Entflammbarkeit 0, Feuer-Ausbreitung 100, ist Feuerquelle (Feuer darauf erlischt
  nicht).
- **Platzieren:** Facing zum Spieler (Blick ≥ 65° nach oben → 1 = oben, ≤ −65° → 0 = unten, sonst horizontal wie
  IC2), nur wenn `isFacingValid`. Item mit NBT (Upgrades) stellt die Upgrades wieder her.
- **Rechtsklick** (nicht schleichend; Items mit `IIgnoreRightclickOnMachine` ausgenommen): Zugriff nur, wenn
  `mTickTimer > 40` und Spieler < 8 Blöcke entfernt. Erst Upgrade-Einbau (§1.5), sonst GUI.
- **Abbau ohne Schraubenschlüssel:** Drop ist **1 Maschinenteile** (`GT_MetaItem_Component` 22, „Machine Parts“)
  + Inventar (alle Slots ≥ 1, Slot 0 nie). Abbau durch Nicht-Creative-Spieler löst vorher `doEnergyExplosion` aus.
  Wird der Block von einer Explosion zerstört (Drop-Chance < 1), gibt es **keinen** Drop, sondern `doEnergyExplosion`.
- **Schraubenschlüssel:** jede Seite einstellbar, sofern `isFacingValid` (BasicMachine: nach dem ersten Tick alle
  6 Seiten); Drop-Rate 0,8 (`isSimpleMachine = false`). Wrench-Drop = die Maschine selbst mit NBT nur der vorhandenen
  Upgrades (`mMJConverter`, `mTransformers`, `mOverclockers`, `mLiBatteries`, `mRSEnergyCells`, `mBatteries`);
  Energie und Inventar gehen nicht mit.
- Besitzer (`mOwnerName`) wird beim ersten Zugriff gesetzt; für die Basismaschinen ohne Wirkung (`ownerControl = false`).

### 1.2 Tick (Server; Reihenfolge wie `BaseMetaTileEntity.g()`)

1. `mTickTimer++`. Erst ab Tick 21 läuft die Logik. Bei Tick 20: Facing −1 → 0.
2. Ab `mTickTimer > 30`: ins EU-Netz eintragen (Eingang **und** Ausgang). Facing- oder Ausgangsänderung → austragen,
   im nächsten Tick neu eintragen.
3. **Feuer** (`fire_causes_explosions`): wenn im Netz, mit Chance 1/1000 je Tick eine zufällige der 6 Seiten prüfen;
   ist dort Feuer → `doEnergyExplosion`.
4. **EU-Ausgabe** (nur wenn `bOutput`): wenn gespeichert ≥ max(Ausgabe, `getMinimumStoredEU` = 1000) und Ausgabe > 0,
   `getMaxOutputPackets` Pakete der Größe `getOutput` emittieren (Rest zurück in den Speicher).
5. **Entladen:** `getInputTier`-mal je Tick den Batterie-Slot 5 entladen (`ElectricItem.discharge(stack, Kapazität −
   gespeichert, inputTier, ignoreTransferLimit = false, simulate = false)`), nur solange `demandsEnergy > 0` und das Item
   `canProvideEnergy`.
6. Maschinenlogik `onPostTick` (§1.3).

Tiers: `getTier(v)` = ≤ 32 → 1, ≤ 128 → 2, ≤ 512 → 3, ≤ 2048 → 4, ≤ 8192 → 5, sonst 6; `inputTier = getTier(maxSafeInput)`.

### 1.3 BasicMachine-Ablauf (`onPostTick`)

Felder: `bOutput` (EU durchreichen, Std. aus), `bItemTransfer` (Auto-Ausgabe, Std. **an**), `bSeperatedInputs`
(Std. `hasTwoSeperateInputs()`, beim Plattenbieger aus), `mMainFacing` (Vorderseite, −1 bis zum ersten Tick mit
horizontalem Facing, dann fest), `mProgresstime`, `mMaxProgresstime`, `mEUt`, `mOutputItem1/2`.

1. Wenn `mMainFacing == −1` und Facing > 1: `mMainFacing = Facing` (fest, auch wenn später gedreht wird).
2. Wenn `mMaxProgresstime > 0` und (`mProgresstime < 0` oder `decreaseStoredEnergy(mEUt · 4^OC)` gelingt):
   `mProgresstime += 2^OC`; erreicht/überschreitet es `mMaxProgresstime` → Produkte in Slot 3/4 (auf Stapelgröße
   gekappt, überzählige Items verschwinden), Fortschritt/Max/Ausgaben = 0. Fehlt Energie, bleibt der Fortschritt stehen
   (kein Rückgang).
3. Wenn `mMaxProgresstime ≤ 0` und gespeicherte Energie **> 900**: `checkRecipe()` (maschinenspezifisch); leere
   Stapel in Slot 1/2 entfernen.
4. **Auto-Ausgabe**, wenn `bItemTransfer` und (aktiv und gerade fertig – d. h. `mActive && mMaxProgresstime ≤ 0` – oder
   `mTickTimer % 1200 == 0` oder (`bOutputBlocked` und `mTickTimer % 20 == 0`)) und die Rückseite (gegenüber Facing)
   ≠ `mMainFacing` und gespeichert ≥ 500 und Slot 3 oder 4 belegt: Inventar auf der Rückseite (Block bei
   Position − Facing-Richtung) suchen; bis zu **zwei** Mal einen Stapel aus Slot 3/4 hineinschieben (Einfügen über die
   Seite `Facing`); Kosten je Verschiebung = Anzahl bewegter Items in EU (`decreaseStoredEnergy(n, true)`).
   Hinweis: `bOutputBlocked` wird nur gesetzt, nie zurückgesetzt (Originalverhalten).
5. `mActive = mMaxProgresstime > 0`.

`decreaseStoredEnergy(n, ignore)`: erst MJ-Speicher (ohne Unterlauf), dann EU (mit `ignore` darf EU auf 0 gekappt
werden – Rückgabe dann false), sonst MJ mit Unterlauf auf 0. `getStoredEnergy` = max(MJ · 5 / 2, EU).

### 1.4 Energiewerte BasicMachine

| Wert | Formel |
|---|---|
| `maxEUInput` | 32 |
| Max. sichere Eingabe | 32 · 4^Trafos – darüber `doExplosion(Paketgröße)` |
| Kapazität | 2000 + 10 000 · Energiespeicher-Upgrades + 100 000 · Lithium-Upgrades |
| MJ-Kapazität | 2000 + 100 000 · RS-Energiezellen (nur mit MJ-Konverter wirksam) |
| Ausgabe (`bOutput`) | 32 · 4^(Trafos−1) (ohne Trafo 32), Pakete: 1, mit ≥ 1 Trafo 4 |
| `demandsEnergy` | Kapazität − gespeichert |
| Energieannahme | von allen Seiten außer Facing und Ausgangsseite (Rückseite, nur wenn `bOutput`) |

Gespeicherte EU können über die Kapazität steigen (`injectEnergy` kappt nicht; Pakete bis 32 werden angenommen,
solange `demandsEnergy > 0` – Netzlogik wie IC2).

### 1.5 Upgrades (Rechtsklick mit dem Item, max. 16 insgesamt, Creative verbraucht nichts)

Reihenfolge der Prüfung, jeweils nur ein Upgrade je Klick:

1. **MJ-Konverter** (GT „Pneumatic Generator Upgrade“, Komponente 25) – einmalig, wenn MJ-Kapazität > 0. Macht die
   Maschine zum BC-Empfänger: `PowerProvider.configure(latency 100, min 1, max 100, activation 1, maxStored 10 000)`;
   jeden Tick `useEnergy(1, MJ-Kapazität − MJ, true)` in den MJ-Speicher. *(Item erst mit dem Assembler, 6b.4.)*
2. **Overclocker** (IC2), max. 4: Energie je Tick ×4, Fortschritt ×2 je Upgrade.
3. **Transformer** (IC2), solange max. sichere Eingabe < 512 und Ausgabe < 128; danach nur noch
   **HV-Transformer-Upgrade** (GT, Komponente 27) bis Eingabe < 8192 und Ausgabe < 2048. *(GT-Item später.)*
4. **Energiespeicher-Upgrade** (IC2): +10 000 EU.
5. **Lithium-Batterie-Upgrade** (GT, Komponente 26): +100 000 EU. *(GT-Item später.)*
6. **RS-Energiezellen-Upgrade** (GT, Komponente 28, nur mit MJ-Konverter): +100 000 MJ. *(GT-Item später.)*

Upgrades sind **keine** Slots: sie werden als Zähler gespeichert und können nur per Schraubenschlüssel-Abbau (im
Item-NBT) mitgenommen werden; ohne Schraubenschlüssel sind sie verloren.

### 1.6 Explosionen

- `doEnergyExplosion`: nur wenn gespeichert ≥ Kapazität/5; Stärke über `doExplosion(Ausgabe · f)`, f = 4 (voll),
  2 (≥ halb), sonst 1. Ohne `bOutput` ist die Ausgabe 0 → `doExplosion(0)` → Stärke 1.
- `doExplosion(n)`: wenn im Netz und `wirefire_on_explosion`: kurz als Quelle mit unbegrenzter Ausgabe ins Netz und je
  ein Paket 32, 128, 512, 2048, 8192 emittieren (lässt Kabel schmelzen). Dann Block → Luft (kein Drop) und
  Vanilla-Explosion mit Stärke n < 10 → 1, < 32 → 2, < 128 → 3, < 512 → 4, < 2048 → 5, < 4096 → 6, < 8192 → 7,
  sonst 8 (zerstört Blöcke).
- Regen/Gewitter (`rain_/lightning_causes_explosions`) betreffen in GT 2.90 nur die alten Maschinen
  (`GT_TileEntityMetaID_Machine`), **nicht** die MetaTileEntity-Maschinen wie Plattenbieger/Elektrolyseur/Assembler.

### 1.7 Inventar und Seiten

Slots: 0 = intern/ungenutzt (nie gedroppt, nicht zugänglich), 1/2 = Eingabe, 3/4 = Ausgabe, 5 = Batterie.

| Seite | Zugriff (`ISidedInventory`) |
|---|---|
| Vorderseite `mMainFacing` | keiner |
| Rückseite (gegenüber Facing) | Slots 3–4 (Ausgabe) |
| oben/unten bei `bSeperatedInputs` | Slot 2 |
| sonst | Slots 1–2 (getrennt: nur Slot 1) |

### 1.8 GUI (`GT_Container_BasicMachine`, Textur je Maschine, 176 × 166)

- Titel bei (8, 4), Farbe 0x404040.
- Slots: Eingabe 1 (35, 25), Eingabe 2 (53, 25), Batterie 5 (80, 63), Ausgabe 3 (107, 25), Ausgabe 4 (125, 25).
  Spielerinventar ab (8, 84), Hotbar y = 142.
- Knöpfe (Holo-Slots, Klick schaltet um): (8, 63) `bOutput`, (26, 63) `bItemTransfer`, (44, 63) `bSeperatedInputs`.
  Aktiv-Overlay aus der GUI-Textur bei (7/25/43, 62) ← (176, 18/36/54), 18 × 18.
- Fortschrittspfeil bei (78, 24) ← (176, 0), Breite 0–20 (= `(p > 0 ? 1 : 0) + p · 20 / max(1, max)`, gekappt), Höhe 18.
- Shift-Klick: aus Maschinen-Slots ins Spielerinventar (von hinten); aus dem Spielerinventar nur in die beiden
  Eingabe-Slots (erst passende Stapel auffüllen, dann leere Slots), nie in Ausgabe/Holo.
- Sync zusätzlich: Energie, Kapazität, Ausgabe, Eingabe, MJ (nur Werte, keine Anzeige in der BasicMachine-GUI).

### 1.9 Texturen (`terrain.png` aus `GregTech.zip`, Index = Zeile · 16 + Spalte)

| Seite | Index |
|---|---|
| Vorderseite `mMainFacing` | maschinenspezifisch (aktiv/inaktiv) |
| Ausgangsseite (gegenüber Facing): unten / oben / Seite | 38 / 79 / 36 |
| unten | 32 |
| oben | 29 (Standard, aktiv/inaktiv) |
| Seiten | 40 (Standard) |

Solange `mMainFacing < 2` (vor dem ersten Tick): Facing-Seite = Vorderseite, sonst wie oben ohne Ausgangsseite.

### 1.10 Tooltip des Maschinen-Items

Zeilen (nur wenn nicht leer): Beschreibung, „Max EU/p IN: 32“, „Max EU/p OUT: …“ (nur > 0, bei BasicMachine nicht),
„EU Storage: …“ (nur > 10 000), „Possible Upgrades: O T B M“; aus NBT: „has MJ-Converter“, „n Overclocker Upgrades“,
„n Transformer Upgrades“, „n Battery Upgrades“, „n Lithium Battery Upgrades“, „n Energy Cell Upgrades“.

## 2. Plattenbieger (`GT_MetaTileEntity_Bender`, ID 59, „Plate Bending Machine“)

- BasicMachine (§1), GUI `gui/Bender.png`, Vorderseite inaktiv/aktiv = `terrain.png` 238/239, Beschreibung
  „Boo, he's bad! We want BENDER!!!“.
- `checkRecipe`: erst Slot 1 → Slot 2 zusammenschieben (bis 64), Slot 3 → Slot 4 zusammenschieben; dann Rezept zu
  Slot 2 suchen (Item + Meta, Menge ≥ Rezeptmenge) und nur starten, wenn Slot 3 die Ausgabe aufnehmen kann (sonst
  `bOutputBlocked = true`). Eingabe wird beim Start abgezogen, Ausgabe erst am Ende eingetragen.
- **Rezepte:** jeder Barren mit Platten-Gegenstück: 1 Barren → 1 Platte, **50 Ticks, 20 EU/t** (`GT_OreDictHandler`
  für alle `ingot*`). Im Umfang: Eisen, Gold, raff. Eisen, Kupfer, Zinn, Bronze; weitere (Aluminium, Stahl, Titan,
  Platin, Silber …) mit ihren Barren. Railcraft-Schienen entfallen (Mod nicht im Umfang).
- **Rezept des Plattenbiegers:** `PCP/RQR/PCP`, P = Kolben, C = el. Schaltkreis (`craftingCircuitTier02`),
  R = Compressor, Q = Förderband-Modul.

## 3. GT-Komponenten und Platten (Teilpaket 6b.1)

| Item | GT | Rezept / Herkunft |
|---|---|---|
| Förderband-Modul („Conveyor Module“, Tooltip „Moves Items around“) | Komponente 5 | `GGG/RRR/CBC`, G = Glas, R = raff. Eisenbarren **oder** -platte **oder** Aluminiumbarren/-platte, C = el. Schaltkreis, B = RE-Batterie (`crafting10kEUStore`, ungeladen) |
| Maschinenteile („Machine Parts“, „Random Machine Parts“) | Komponente 22 | Drop beim Abbau ohne Schraubenschlüssel; Schrottbox 0,8 |
| Platten Eisen, Gold, raff. Eisen, Zinn, Kupfer, Bronze | Material 64–68, 70 | Plattenbieger; Tooltip = Formel (Fe, Au, Fe, Sn, Cu, SnCu3) |

Platten-Nebenrezepte (`GT_OreDictHandler`, `plate*`): Ofen Platte → Barren; Macerator Platte → Staub (nur wenn es den
Staub gibt – raff. Eisen hat keinen); Compressor **8 Kupferplatten → dichte Kupferplatte** (ersetzt 8 Kupferbarren).
Alle Werkbank-Rezepte anderer Mods, die Platten herstellen, werden entfernt.

Mischmetallbarren (`GT_Mod` Z. 2255 ff., IC2-Rezept `III/BBB/TTT` entfernt), senkrecht `X/Y/Z` (Spalte frei wählbar):

| X \ Y+Z | Bronze + Zinn | Bronze/Messing + Zink | + Aluminium |
|---|---|---|---|
| raff. Eisen | 1 | 1 | 1 |
| Nickel | 1 | 1 | 1 |
| Invar | 2 | 2 | 3 |
| Stahl | 2 | 2 | 3 |
| Titan | 3 | 3 | 4 |
| Wolfram | 3 | 3 | 4 |
| Wolframstahl | 5 | 5 | 6 |

(Y = Bronze- oder Messingplatte, gleiche Menge.) Im Umfang ab 6b.1: raff. Eisen + Bronze + Zinn → 1; die übrigen
Varianten, sobald die Platten existieren (Aluminium 6b.3, Stahl 6b.3, Titan …).

Danach IC2: Compressor Mischmetall → fortg. Legierung; fortg. Maschinenblock (`08-rezepte.md` §1) → MFSU,
Massefabrikator, Kompositweste.

## 4. Industrieller Elektrolyseur (`GT_MetaTileEntity_Electrolyzer`, ID 25, „Industrial Electrolyzer“)

Eigene MetaTileEntity (**keine** BasicMachine), nutzt aber die Basis aus §1.1/§1.2/§1.5/§1.6.

| Wert | |
|---|---|
| `maxEUInput` | **128** → sichere Eingabe 128 · 4^Trafos (ein IC2-Trafo-Upgrade → 512, dann nur HV-Upgrade) |
| `maxEUStore` | 10 000 (+ Upgrades wie §1.4), MJ-Kapazität 10 000 (nur mit MJ-Konverter) |
| Ausgabe | keine (`maxEUOutput = 0`), `getMinimumStoredEU = 0` |
| Energieannahme | **alle** Seiten (`isInputFacing` immer wahr) |
| Facing | `isFacingValid` immer falsch → nicht drehbar, keine Vorderseite |
| Upgrades | Overclocker, Transformer, Energiespeicher/Lithium, MJ-Konverter („Possible Upgrades: O T B M“) |
| Batterie-Slot | keiner (kein Entladen) |
| Texturen | unten 32, oben 29, Seiten 64 (aktiv 65) |
| Beschreibung | „Give your Water a shocking Experience with this Device“; Tooltip „Max EU/p IN: 128“ |

**Slots:** 0 = Eingabe (Stoff), 1 = Eingabe (Zellen), 2–5 = Ausgabe 1–4, 6 = interner Wasser-Holo-Slot (nicht
greifbar, nicht gedroppt; `isValidSlot` < 6 → Drop beim Abbau: Slots 0–5). Seiten: oben → Slot 0, unten → Slot 1,
Seiten → Slots 2–5 (je ein Bereich, `ISidedInventory`).

**Wassertank:** Flüssigkeitseingang von allen Seiten, nur Wasser, Kapazität 10 000 mB (`mStoredWater`), kein
Ablassen. Jeden Tick: `mStoredWater ≥ 1000` und Slot 6 < 64 → 1000 mB → +1 „Wasser“ in Slot 6.

**Tick (`onPostTick`, Server):**
1. Wasser umwandeln (s. o.).
2. Läuft ein Rezept (`mMaxProgresstime > 0`): aktiv; wenn `mProgresstime ≥ 0` und `decreaseStoredEnergy(mEUt · 4^OC)`
   scheitert → inaktiv und (`constant_need_of_energy` = an) **`mProgresstime = −10`**, Fehlercode 1 („Insufficient
   Energy Line!“ statt „Inventory“ in der GUI). Sonst `mProgresstime += 2^OC`; bei ≥ Max → Ausgaben 1–4 in Slot 2–5
   (auf Stapelgröße gekappt), alles auf 0, Fehlercode 0. Bei `mProgresstime < 0` läuft der Fortschritt **ohne**
   Energie weiter (−10 → … → 0 → dann wieder Verbrauch).
3. Sonst inaktiv; wenn gespeicherte Energie **> 100**: `checkRecipe`.

**`checkRecipe`:** erst Rezept für (Slot 0, Slot 1), sonst (Slot 6 = Wasser, Slot 1); Reihenfolge fest (nicht
shapeless). Start nur, wenn alle vier Ausgaben passen (`spaceForOutput`, kein `bOutputBlocked`); Eingaben werden beim
Start abgezogen, Ausgaben erst am Ende eingetragen. Rezeptsuche wie §2: Eingabe 1 Item + Menge ≥ Rezept, Eingabe 2
(leere Zellen) nur wenn das Rezept eine hat.

**GUI** (`gui/Electrolyzer.png`, `GT_Container_Electrolyzer`): Titel „Industrial Electrolyzer“ (8, 4); Slots 0 (80, 46),
1 (50, 46), Ausgaben 2–5 (50/70/90/110, 16), Holo-Slot 6 (110, 46, nur Anzeige). Fortschrittsbalken: Höhe
`min(10, (p > 0 ? 1 : 0) + p · 10 / max(1, max))`, gezeichnet bei (73, 44 − h) ← (183, 44 − h), Breite 30, wächst nach
oben. Inventar-Beschriftung (8, 72) bzw. Fehlertext. Shift-Klick aus dem Spielerinventar nur in Slot 0/1.

**Rezept des Elektrolyseurs:** `RDR/CEC/RMR`, E = IC2-Elektrolyseur, M = IC2-Magnetisierer, D = Extractor,
C = fortg. Schaltkreis (`craftingCircuitTier04`), R = raff. Eisenplatte **oder** Aluminiumplatte (zwei Rezepte).

**Rezepte** (`addElectrolyzerRecipe`, Eingabe 2 = n leere Zellen; Dauer laut `RecipesAdvancedConfig.cfg` unverändert):

| Eingabe 1 | Zellen | Ausgaben | Ticks | EU/t | Umfang |
|---|---:|---|---:|---:|---|
| 6 Wasser (Tank) | 5 | 4 Wasserstoffzellen, 1 Luftzelle | 775 | 120 | 6b.2 |
| 6 elektrol. Wasserzellen | – | 4 Wasserstoffzellen, 1 Luftzelle, –, 1 leere Zelle | 100 | 30 | 6b.2 |
| 1 Wasserzelle | – | 1 elektrol. Wasserzelle | 128 | 128 | 6b.2 |
| 1 Wassereimer | 1 | 1 elektrol. Wasserzelle, 1 Eimer | 128 | 128 | 6b.2 |
| 3 Knochenmehl | 1 | 1 Calciumzelle | 24 | 106 | 6b.2 |
| 32 Zucker | 7 | 2 Kohlenstoffzellen, 5 Wasserzellen | 210 | 32 | 6b.2 |
| 4 Lohenstaub | – | 1 dunkle Asche, 1 Schwefelstaub | 300 | 25 | 6b.2 |
| 16 Sand | 2 | 1 Siliziumzelle, 1 Luftzelle | 1000 | 25 | 6b.2 |
| 8 Tonstaub | 5 | 1 Lithiumzelle, 2 Siliziumzellen, 2 Aluminiumstaub, 2 Natriumzellen | 200 | 50 | 6b.2 |
| 1 Kohlestaub | 2 | 2 Kohlenstoffzellen | 40 | 50 | 6b.2 |
| 8 Feuersteinstaub | 2 | 1 Siliziumzelle, 1 Luftzelle | 1000 | 5 | später (GT-Staub) |
| 1 Holzkohlestaub / 2 Asche | 1 | 1 Kohlenstoffzelle | 20 / 25 | 50 | später (GT-Staub) |
| 4 Obsidianstaub | 3 | 2 kl. Magnesium-, 2 kl. Eisenstaub, 1 Siliziumzelle, 2 Luftzellen | 500 | 5 | später (Kleinstaub) |
| 10 Salpeterstaub | 7 | 2 Kalium-, 2 Stickstoffzellen, –, 3 Luftzellen | 50 | 110 | später |
| Erze/Edelsteine (Bauxit, Rubin, Saphir, Granate, Smaragd, Enderperle, Lazurit, Pyrit, Calcit, Sodalith, Galena, Sphalerit, Zinnober …), Schwefelsäure-, Methan-, Holzgaszellen | | siehe `GT_OreDictHandler` | | | mit den GT-Erzen/-Stäuben |

Zellen (`GT_MetaItem_Cell`, Tooltip = Formel): 0 Wasserstoff H, 5 Lithium Li, 7 Silizium Si, 8 Kohlenstoff C,
11 Calcium Ca, 12 Natrium Na. Stäube (`GT_MetaItem_Dust`): 8 Schwefel S, 18 Aluminium Al, 63 dunkle Asche C.
Aluminiumstaub → Ofen nicht möglich (GT: nur Hochofen, 6b.3).

## 5. Industrieller Hochofen (`GT_MetaTileEntity_BlastFurnace`, ID 29, „Industrial Blast Furnace“) – Teilpaket 6b.3b

Eigene MetaTileEntity (keine BasicMachine), Basis §1.1/§1.2/§1.5/§1.6.

| Wert | |
|---|---|
| `maxEUInput` | 128 (sichere Eingabe 128 · 4^Trafos), Energie von **allen** Seiten |
| `maxEUStore` | 10 000 (+ Upgrades §1.4), MJ-Kapazität 10 000 (nur mit MJ-Konverter) |
| Ausgabe | keine; kein Batterie-Slot |
| Facing | nur horizontal (`isFacingValid = aFacing > 1`); Platzieren: zum Spieler; Schraubenschlüssel dreht horizontal |
| Upgrades | Overclocker, Transformer, Energiespeicher/Lithium, MJ-Konverter („O T B M“) |
| Texturen (`terrain.png`) | Vorderseite 68 (aktiv 69), Rückseite 71, alle übrigen Seiten (auch oben/unten) 72 |
| Beschreibung | „You'll have a Blast!“; Tooltip „Max EU/p IN: 128“ |

**Slots:** 0 = Eingabe 1, 1 = Eingabe 2, 2/3 = Ausgabe 1/2 (alle droppen). Seiten: oben → Slot 0, unten → Slot 1,
Seiten → Slots 2–3.

**Struktur** (`checkMachine`): Mittelpunkt = Controller − 2 · Facing-Richtung (also hinter dem Controller). Geprüft
wird der Quader 3 × 3 (horizontal um den Mittelpunkt) × 4 hoch, **y = Controller-Höhe bis +3**; der Controller selbst
liegt außerhalb, direkt vor der Mitte der unteren Lage. Die beiden Mittelblöcke der Lagen 2 und 3 (y + 1, y + 2)
müssen **Luft oder Lava** (fließend oder still) sein, alle übrigen 34 Positionen GT-Maschinengehäuse:

| Block | Hitze je Block |
|---|---:|
| Standard-Maschinengehäuse (`BlockMetaID_Block` 13) | 30 K |
| Verstärktes Maschinengehäuse (14) | 50 K |
| Fortgeschrittenes Maschinengehäuse (15) | 70 K |
| Lava in der Mitte | 250 K |

`mHeatCapacity` = 500 · Heizspulen-Stufe + Summe. Unvollständig → `mMachine = false`, Hitze 0. Beispiele: 34 Standard
= 1020 K (Stahl), + 2 Lava = 1520 K (Siliziumplatte), 34 verstärkt = 1700 K (Aluminium).

**Prüfzeitpunkt:** Zähler `mUpdate` startet bei 5 (neu und nach dem Laden) und wird jeden Tick (ab Tick 21, §1.2)
heruntergezählt; geprüft wird nur beim Übergang 0 → −1. `onMachineBlockUpdate` setzt `mUpdate = 5`, ebenso der
Einbau einer Heizspule. Auslöser (`GT_Mod.causeMachineUpdate`): Platzieren/Abbauen eines Maschinengehäuses
(Meta 13–15 sind als „Machine Block“ registriert) und Abbauen einer GT-Maschine. Ausbreitung (`stepToUpdateMachine`,
Tiefensuche in Reihenfolge +X, −X, +Y, −Y, +Z, −Z, besuchte Positionen gemerkt): jede besuchte Position mit
`IMachineBlockUpdateable` (alle GT-Maschinen) bekommt `onMachineBlockUpdate`; weitergegangen wird, solange weniger als
5 Positionen besucht sind **oder** die Position eine GT-Maschine oder ein Maschinengehäuse ist. Lava/Luft-Änderungen
in der Mitte lösen **keine** Prüfung aus (Originalverhalten).

**Heizspulen** (Rechtsklick, nach der Upgrade-Prüfung §1.5): mit ≥ 4 Spulen (Creative: beliebig, nichts verbraucht)
– Stufe 0 + `craftingHeatingCoilTier01` (Kanthal) → 1; Stufe 1 + `…Tier02` (Nichrom) → 2; Stufe 2 + `…Tier03` → 3
(gibt es in GT 2.90 nicht). Je Einbau −4 Spulen, `mUpdate = 5`. Mit weniger als 4 passenden Spulen passiert nichts
(auch keine GUI). Kupfernickel ist `…Tier00` und **nicht** einbaubar. Stufe bleibt beim Schraubenschlüssel-Abbau im
Item-NBT (`mHeatingCoilTier`, nur > 0).

**Tick (`onPostTick`, Server):**
1. Wenn `mUpdate-- == 0`: `mMachine = checkMachine()`, sonst Hitze 0.
2. Aktiv = `mMachine` (also auch ohne Rezept aktiv, sobald die Struktur steht).
3. Wenn `mMachine` und Rezept läuft: wie Elektrolyseur §4 (Energie `mEUt · 4^OC`, sonst `mProgresstime = −10` und
   Fehlercode 1; Fortschritt `+2^OC`; fertig → Ausgaben in Slot 2/3, auf Stapelgröße gekappt, nur bei gleichem Item;
   Fehlercode 0).
4. Sonst, wenn gespeicherte Energie > 100: `checkRecipe` – nur mit `mMachine`; Rezept zu (Slot 0, Slot 1) suchen,
   **Hitze ≥ Rezept-Hitze** (`mStartEU`), Platz für beide Ausgaben, Eingaben beim Start abziehen.
   Läuft ein Rezept und die Struktur bricht, ruft Schritt 4 `checkRecipe` auf, das ohne `mMachine` nichts tut – das
   Rezept bleibt stehen (kein Fortschritt, keine Energie).

**GUI** (`gui/Blast.png`, `GT_Container_BlastFurnace`): Titel „Industrial Blast Furnace“ (8, 4); Zeile (8, 63):
„Heat Capacity: n K“ bzw. „Incomplete Machine Casing!“; Zeile (8, 72): „Insufficient Energy Line!“ bzw. „Inventory“.
Slots 0 (34, 16), 1 (34, 34), Ausgaben 2 (86, 25), 3 (104, 25). Fortschrittspfeil bei (58, 28) ← (176, 0), Breite
`max(0, min(20, (p > 0 ? 1 : 0) + p · 20 / max(1, max)))`, Höhe 11. Shift-Klick aus dem Spielerinventar nur in Slot 0/1.

**Rezept des Hochofens:** `ACA/CMC/FCF`, M = fortg. Maschinenblock (`craftingRawMachineTier02`), A = el. Schaltkreis
(`craftingCircuitTier02`), C = **Kupfernickel-Heizspule** (`craftingHeatingCoilTier00`), F = IC2-Induktionsofen.

**Hochofen-Rezepte** (`addBlastRecipe`, Dauern laut `RecipesAdvancedConfig.cfg` unverändert, alle 128 EU/t):

| Eingabe 1 | Eingabe 2 | Ausgabe 1 | Ausgabe 2 | Ticks | Hitze | Umfang |
|---|---|---|---|---:|---:|---|
| raff. Eisenbarren | 2 Kohlestaub | Stahlbarren | 2 dunkle Asche | 500 | 1000 | 6b.3b |
| Stahlstaub | – | Stahlbarren | – | 100 | 1000 | 6b.3b |
| Aluminiumstaub | – | Aluminiumbarren | – | 200 | 1700 | 6b.3b |
| 2 Siliziumzellen | – | Siliziumplatte | 2 leere Zellen | 1000 | 1500 | 6b.3b |
| Titanstaub | – | Titanbarren | – | 1000 | 1500 | später |
| Chromstaub | – | Chrombarren | – | 800 | 1700 | später |
| Wolframstaub | – | Wolframbarren | – | 2000 | 2500 | später |
| Wolframbarren + Stahlbarren | | 2 Wolframstahlbarren | 4 dunkle Asche | 2000 | 3000 | später |
| 2 Galenastaub | – | Silberbarren | Bleibarren | 20 | 1500 | später |
| Eisen-/Pyriterz (+ Calciumcarbonatzelle) | | raff. Eisen | leere Zelle | 100 | 1000/1500 | mit GT-Erzen |

Pack-Config `basicmetalblastfurnacerequirement`: alles `true` → Aluminium-, Stahl-, Titan-, Chrom-, Wolframstaub
lassen sich **nicht** im Ofen schmelzen. Rezeptsuche wie §2 (Eingabe 1 Item + Menge ≥ Rezept, Eingabe 2 nur wenn das
Rezept eine hat). **Shapeless:** zuerst wird die vertauschte Belegung (Slot 1, Slot 0) geprüft.

### 5.1 GT-Maschinengehäuse (`GT_BlockMetaID_Block` 13–15)

| | Standard („Standard Machine Casing“) | Verstärkt („Reinforced …“) | Fortgeschritten („Advanced …“) |
|---|---|---|---|
| Härte | 3,0 | 3,0 | 3,0 |
| Explosionsresistenz (wirksam) | 30 | 60 | 30 |
| Rezept (→ 4 Stück) | `AAA/CMC/AAA`, A = raff. Eisen- **oder** Aluminiumplatte, C = el. Schaltkreis, M = Maschinenblock | `SSS/CMC/SSS`, S = Stahlplatte, C = fortg. Schaltkreis, M = fortg. Maschinenblock | `TTT/CMC/TTT`, T = Chromplatte, C = `craftingCircuitTier06`, M = `craftingRawMachineTier04` – **später** |

Material Eisen, Spitzhacke Stufe 2 (Eisen), droppt sich selbst, keine Mob-Spawns, kein Beacon-Sockel. Platzieren und
Abbauen lösen `causeMachineUpdate` aus. Textur: `blocks.png` Index 13/14/15 (alle Seiten gleich); im Original
verbundene Texturen (Startindex 96/112/224, 12 Varianten je Seite) – **Abweichung: erst Phase 10** (Nutzerentscheidung).

### 5.2 Heizspulen und Materialien (6b.3b)

| Item | GT | Tooltip | Rezept / Herkunft |
|---|---|---|---|
| Kupfernickel-Heizspule („Cupronickel Heating Coil“) | Komponente 21 | „Cheap and simple Heating Coil“ | → 3: `BAB/A A/BAB`, A = Kupferbarren, B = Nickelbarren |
| Kanthal-Heizspule („Kanthal Heating Coil“) | Komponente 19 | „Standard Heating Coil“ | → 3: `AAA/BCC/BBC`, A = raff. Eisen, B = Chrom-, C = Aluminiumbarren – **ohne Rezept bis Chrom** |
| Nichrom-Heizspule („Nichrome Heating Coil“) | Komponente 20 | „Advanced Heating Coil“ | → 1: ` B /BAB/ B `, A = Chrom-, B = Nickelbarren – **ohne Rezept bis Chrom** |
| Nickelstaub / -barren / -platte | Dust 28 / Material 28 / 72 | Ni | Staub: Schrottbox 0,5, UU-Materie `U  /U U` (3 UU) → 10; Ofen Staub → Barren; Macerator Barren/Platte → Staub; Bender |
| Stahlstaub / -barren / -platte | Dust 26 / Material 26 / 78 | Fe | Hochofen; Macerator Barren/Platte → Staub; Bender; Schrottbox 0,5 (Staub) |
| Aluminiumbarren / -platte | Material 18 / 75 | Al | Hochofen; Macerator Barren/Platte → Staub; Bender; Schrottbox 0,5 (Staub) |
| Siliziumplatte | Material 36 | Si2 | Hochofen |

Die Spulen-Rezepte sind im Pack **Walzmaschinen-Rezepte** (Railcraft, `MMIngotOnlyRollingMachine=true`); Railcraft
ist nicht im Umfang → Werkbank (GTs eigener Fallback ohne Railcraft; Nutzerentscheidung, `abweichungen.md`).
Platten → Ofen → Barren gilt auch für Aluminium/Stahl (GT entfernt nur die Barren-Ofenrezepte anderer Mods vor den
Platten-Rezepten). Ofen-Rezept Staub → Barren nur für Nickel.

**Damit herstellbar (Rezepte laut `08-rezepte.md`/§3):** Standard- und verstärktes Gehäuse, Hochofen, Solarpanel
(`GGG/SPS/CHC`, Siliziumplatte + Kohleplatte), Stahlwerkzeuge (Bergbaubohrer, Kettensäge, el. Hacke, el. Baumzapfer,
el. Schraubenschlüssel), Elektrolyseur-Variante mit Aluminiumplatten, Förderband-Modul mit Aluminium, Mischmetall-
Varianten mit Nickel/Stahl/Aluminium, Generator-Varianten mit Aluminium.

## 6. Assembler (`GT_MetaTileEntity_Assembler`, ID 60, „Assembling Machine“) – Teilpaket 6b.4

- BasicMachine (§1), GUI `gui/Assembler.png` (Layout wie §1.8, keine Besonderheiten), Beschreibung
  „Avengers, Assemble!“. Vorderseite inaktiv/aktiv = `terrain.png` **224/225**, **Oberseite 237** (aktiv und inaktiv;
  ersetzt die Standard-Oberseite 29, auch solange `mMainFacing < 2`). `hasTwoSeperateInputs() = true` → getrennte
  Eingänge standardmäßig **an** (oben/unten nur Slot 2, sonst nur Slot 1; Knopf schaltet um, §1.7).
- `checkRecipe`: Slot 3 → Slot 4 zusammenschieben (kein Zusammenschieben der Eingänge). Wenn Slot 1 oder 2 belegt:
  Rezept suchen (**shapeless**, §6.1), nur starten, wenn Slot 3 die Ausgabe aufnehmen kann (sonst
  `bOutputBlocked`); Eingaben werden beim Start abgezogen, Ausgabe am Ende eingetragen.
- **Rezept:** `CPC/RQR/CRC`, C = el. Schaltkreis (`craftingCircuitTier02`), P = Kolben, R = raff. Eisenplatte **oder**
  Aluminiumplatte, Q = Förderband-Modul.

### 6.1 Rezeptsuche (`GT_Recipe.findEqualRecipeIndex`, shapeless)

Erst mit vertauschten Slots (Slot 2 als Eingabe 1), dann normal. Je Reihenfolge: Hash-Treffer (Eingabe 1 + Eingabe 2,
sonst Eingabe 1 allein) bevorzugt, sonst erstes passendes Rezept der Liste. Ein Rezept passt, wenn Eingabe 1 (Item,
Meta oder Wildcard, Menge ≥) passt und – falls das Rezept eine zweite Eingabe hat – Eingabe 2 ebenso; hat das Rezept
keine zweite Eingabe, ist der andere Slot egal und wird nicht verbraucht. Im aktuellen Rezeptbestand gibt es keine
Mehrdeutigkeit, daher genügt die Suche über die Liste (`GtRecipe.find`).

### 6.2 Rezepte (im Umfang 6b.4)

Dauer aus `RecipesAdvancedConfig.cfg` (Abschnitt `assembling`, Schlüssel = **Ausgabe**; alle Rezepte mit gleicher
Ausgabe teilen den Wert – im Pack gleich den Code-Standardwerten, außer wo unten vermerkt).

| Eingabe 1 | Eingabe 2 | Ausgabe | Ticks | EU/t | Quelle |
|---|---|---|---|---|---|
| Generator | 4 Aluminiumplatten | **2 Wassermühlen** | 6400 | 8 | `plateAluminium` |
| Generator | 4 Kohleplatten (IC2) | **Windmühle** | 6400 | 8 | `GT_Mod` |
| 2 Wassermühlen | – | Generator | 6400 | 8 | `GT_Mod` |
| BC-Eisenzahnrad | Generator | MJ-Konverter („Pneumatic Generator Upgrade“, Komp. 25) | 3200 | 4 | `GT_Mod` |
| HV-Trafo | Transformer-Upgrade | HV-Transformer-Upgrade (Komp. 27) | 3200 | 4 | `GT_Mod` |
| Lithium-Batterie (jede Ladung) | 4 Aluminiumplatten | Lithium-Batterie-Upgrade (Komp. 26) | 3200 | 4 | `craftingLiBattery`/`plateAluminium` |
| 8 raff. Eisenbarren | – | Maschinenblock | 400 | 8 | `ingotRefinedIron` |
| 2 raff. Eisenplatten | el. Schaltkreis | 4 Maschinenteile | 800 | 16 | `plateRefinedIron` |
| 2 Aluminiumplatten | el. Schaltkreis | 3 Maschinenteile | 800 | 16 | `plateAluminium` |
| 8 Aluminiumplatten | Maschinenteile | Aluminium-Maschinenhülle (Komp. 32) | 400 | 8 | `plateAluminium` |
| 8 Bronzeplatten | Maschinenteile | Bronze-Maschinenhülle (Komp. 33) | 400 | 8 | `plateBronze` |
| 8 Stahlplatten | Maschinenteile | Stahl-Maschinenhülle (Komp. 35) | 400 | 8 | `plateSteel` |
| BatPack (jede Ladung) | – | 6 RE-Batterien (leer) | 1600 | 2 | `GT_Mod` |
| Lithium-BatPack (jede Ladung) | – | 6 Lithium-Batterien (leer) | 3200 | 4 | `GT_Mod` |
| Kupferkabel | Gummi | isol. Kupferkabel | 100 | 2 | `itemRubber` |
| Goldkabel | 2 Gummi | 2fach isol. Goldkabel | 200 | 2 | `itemRubber` |
| isol. Goldkabel | Gummi | 2fach isol. Goldkabel | **200** (Config; Code 100) | 2 | `itemRubber` |
| HV-Kabel | 3 Gummi | 4fach isol. HV-Kabel | 300 | 2 | `itemRubber` |
| isol. HV-Kabel | 2 Gummi | 4fach isol. HV-Kabel | **300** (Config; Code 200) | 2 | `itemRubber` |
| 2fach isol. HV-Kabel | Gummi | 4fach isol. HV-Kabel | **300** (Config; Code 100) | 2 | `itemRubber` |
| BC-Steinzahnrad | 4 Eisenbarren | BC-Eisenzahnrad | 1600 | 2 | `ingot*` (Werkbank-Muster mit Steinzahnrad) |
| BC-Steinzahnrad | 4 Zinnbarren | Forestry-Zinnzahnrad | 1600 | 2 | `ingot*` |
| BC-Eisenzahnrad | 4 Goldbarren | BC-Goldzahnrad | 800 | 1 | `ingotGold` |
| BC-Goldzahnrad | 4 Diamanten | BC-Diamantzahnrad | 1600 | 2 | `itemDiamond` |
| Stock | Bruchstein | Hebel | 400 | 1 | `stickWood` |
| Stock | Kohle/Holzkohle | 4 Fackeln | 400 | 1 | `stickWood` |
| Stock | Harz | 4 Fackeln | 400 | 1 | `stickWood` |
| Stock | Redstone | Redstone-Fackel | 400 | 1 | `stickWood` |
| 4 Eisenbarren | Redstone | Kompass | 400 | 4 | `ingotIron` |
| 4 Goldbarren | Redstone | Uhr | 400 | 4 | `ingotGold` |

**Später** (Zutaten fehlen): Schaltkreise/Platinen (Elektrum, Platin, Lazurit, Olivin/Smaragd → Datenspeicher …),
Daten-Orb, Sonictron, Scanner (Cropnalyzer), Sensor-Kit, Messing-/Titan-Hülle, RS-Energiezellen-Upgrade (Thermal
Expansion), Railcraft-Teile (nicht im Umfang), Zahnräder aus Bronze/Kupfer/Invar.

### 6.3 GT-Upgrades (Items, Komponenten 25–27)

| Item | Tooltip | Wirkung (§1.5) |
|---|---|---|
| „Pneumatic Generator Upgrade“ (MJ-Konverter) | „Lets Machines accept MJ“ | einmalig, nur wenn MJ-Kapazität > 0 |
| „Lithium Battery Upgrade“ | „Adds 100000 EU to the Energy Capacity“ | +100 000 EU, wenn `isBatteryUpgradable` |
| „HV-Transformer Upgrade“ | „Higher Tier of Transformer Upgrade“ | Trafo-Zähler +1, nur wenn `isTransformerUpgradable`, (sichere Eingabe ≥ 512 **oder** Ausgabe ≥ 128) und Eingabe < 8192 und Ausgabe < 2048 – vorher wird nur das IC2-Trafo-Upgrade angenommen |

Einbaureihenfolge beim Rechtsklick (je ein Upgrade, max. 16): MJ-Konverter → Overclocker → Trafo/HV-Trafo →
Energiespeicher → Lithium → RS-Zelle. Passt das gehaltene Item nirgends, öffnet sich die GUI.

**MJ-Empfang** (`BaseMetaTileEntityMJ.updateStatus`, jeden Tick nach dem Entladen des Batterie-Slots, vor
`onPostTick`): nur mit MJ-Konverter. Im ersten Tick danach wird der `PowerProvider` angelegt
(`configure(latency 100, min 1, max 100, activation 1, maxStored 10 000)`), ab dem nächsten Tick
`useEnergy(1, MJ-Kapazität − MJ, true)` in den MJ-Speicher, solange MJ < MJ-Kapazität. Ohne Provider ist die Maschine
kein MJ-Empfänger (`getPowerProvider() == null`). GT ruft `PowerProvider.update` nie auf (kein `doWork`, kein
Verlust); der Provider-Puffer wird nicht gespeichert. Verbrauch: MJ zuerst (§1.3 `decreaseStoredEnergy`).

### 6.4 Lithium-Batterie und Lithium-BatPack (GT-Items 56–58)

| | Lithium-Batterie („Lithium-Battery“) | Lithium-BatPack („Lithium-Batpack“) |
|---|---|---|
| Kapazität / Transfer / Tier | 100 000 / 128 / 1 | 600 000 / 128 / 1 |
| Energiequelle (`canProvideEnergy`) | ja | ja |
| Stapel | leer 16 (eigenes Item 56), geladen 1 (Item 57) | 1, Brustplatte, kein Schutz, keine Funktion außer Speicher (Specials 0) |
| Icon | `items.png` 56 (leer) / 57 (geladen) | `items.png` 58, Rüstungstextur `armor/lithiumbatpack_1.png` |
| Tooltip | „Tier: 1“ | „Tier: 1“ |
| Rezept | ` C /ALA/ALA`, C = 2fach isol. Goldkabel, L = Lithiumzelle, A = Aluminiumplatte | `LCL/LAL/L L`, L = Lithium-Batterie, C = fortg. Schaltkreis, A = Aluminiumplatte |

Beide Rezepte sind IC2-`AdvRecipe` (Ladung der Zutaten geht auf das Ergebnis über). Ore-Namen: Lithium-Batterie =
`craftingLiBattery`, `crafting100kEUStore` (zusammen mit dem Energiekristall); Lithium-BatPack = `crafting600kEUPack`.
Das BatPack ist Metallrüstung (`IMetalArmor`) und lädt – anders als das IC2-BatPack – nichts aktiv; elektrische
Werkzeuge ziehen aber wie bei allen energieliefernden Rüstungsteilen daraus (`ElectricItems.use`). Rechtsklick mit der
Lithium-Batterie bewirkt nichts (kein Entladen in die Hotbar wie bei der RE-Batterie).

### 6.5 Maschinenhüllen (Komponenten 32, 33, 35)

| Item | Tooltip | Ore-Name |
|---|---|---|
| „Aluminium Machine Hull“ | „Machine Block“ | `craftingRawMachineTier01` |
| „Bronze Machine Hull“ | „Machine Block“ | `craftingRawMachineTier01` |
| „Steel Machine Hull“ | „Advanced Machine Block“ | `craftingRawMachineTier02` |

`craftingRawMachineTier01` = IC2-Maschinenblock + Aluminium-/Bronze-Hülle; `craftingRawMachineTier02` = fortg.
Maschinenblock + Stahl-Hülle (später Titan-Hülle). Umsetzung als Tags `craft:raw_machine_tier_01/02`, verwendet in den
GT-Rezepten, die im Pack den Ore-Namen nutzen: Standard-Gehäuse (Tier 01), verstärktes Gehäuse (Tier 02), Generator
`B/M/F`, MFE `GCG/CMC/GCG`, Macerator `FDF/DMD/FAF` (alle Tier 01). Ebenso Tag `craft:crafting_100k_eu_store`
(Energiekristall + Lithium-Batterie) im MFE-Rezept.

**Damit herstellbar:** Wassermühle, Windmühle, GT-Upgrades MJ-Konverter/HV-Trafo/Lithium, Lithium-Batterie und -BatPack.

## 7. Automatische Canning Machine (`GT_MetaTileEntity_Canner`, ID 57, „Automatic Canning Machine“) – Teilpaket 6b.5a

- BasicMachine (§1), GUI `gui/Canner.png` (Layout wie §1.8), Beschreibung „Unmobile Food Canning Machine GTA4“.
  Vorderseite inaktiv/aktiv = `terrain.png` **254/255**, übrige Seiten Standard (§1.9). `hasTwoSeperateInputs() = true`.
- `checkRecipe` wie der Assembler (§6), aber **zwei Ausgaben** (Slot 3/4, `spaceForOutput(out1, out2)`), Liste
  `sCannerRecipes`, Suche **shapeless** (§6.1).
- **Rezept:** formlos Förderband-Modul (`craftingConveyor`) + IC2 Canning Machine.
- Dauer aus `RecipesAdvancedConfig.cfg`, Abschnitt `canning`, Schlüssel = **Eingabe 1** – im Pack überall gleich den
  Code-Standardwerten. Doppelte Rezepte (gleiche Eingaben) werden von GT nicht aufgenommen; das erste gewinnt.

| Eingabe 1 | Eingabe 2 | Ausgabe 1 | Ausgabe 2 | Ticks | EU/t |
|---|---|---|---|---|---|
| Verrottetes Fleisch | 2 Zinndosen | 2 gefüllte Zinndosen (Meta 1) | – | 200 | 1 |
| Spinnenauge | 1 Zinndose | 1 gefüllte Zinndose (Meta 1) | – | 100 | 1 |
| Giftige Kartoffel | 1 Zinndose | 1 gefüllte Zinndose (Meta 1) | – | 100 | 1 |
| Kuchen (Item und Block) | 6 Zinndosen | 6 gefüllte Zinndosen | – | 600 | 1 |
| Pilzsuppe | 3 Zinndosen | 3 gefüllte Zinndosen | Schüssel | 300 | 1 |
| jedes andere Nahrungsmittel (außer gefüllter Zinndose), `n = ceil(heal/2)` > 0 | n Zinndosen | n gefüllte Zinndosen (Meta 0) | Behälter-Item, falls vorhanden | 100 · n | 1 |
| Komprimierter Pflanzenball | leere Zelle | Biozelle | – | 100 | 1 |
| Hydr. Kohleklumpen | leere Zelle | hydr. Kohlezelle | – | 100 | 1 |
| Lavaeimer | leere Zelle | Lavazelle | Eimer | 100 | 1 |
| Wassereimer | leere Zelle | Wasserzelle | Eimer | 100 | 1 |
| 6 Bio-Brennstoffzellen | leerer Brennstoffkanister | gefüllter Kanister (`value` 5208) | 6 leere Zellen | 600 | 1 |
| 6 Kohle-Brennstoffzellen | leerer Brennstoffkanister | gefüllter Kanister (`value` 15288) | 6 leere Zellen | 600 | 1 |

Spätere Rezepte (Zutaten fehlen noch): Uranbarren + Zelle → Uranzelle (100 Ticks, 2 EU/t, Phase 7), Plutonium-,
Thorium-, Schwefel-, Calcit-, Wolfram-, Quecksilberzellen (GT-Zellen, je 100/1), Railcraft-Schwellen (Kreosot, nicht im
Umfang).

Umsetzung der Nahrungsrezepte: Datagen über alle Items mit `DataComponents.FOOD` (Vanilla und Mod) mit
`ceil(nutrition / 2)`; die 1.4.7-Werte stimmen für alle Vanilla-Nahrungsmittel von damals mit den 1.21-Werten überein
(Ergebnis `n`). Behälter-Item = `FoodProperties.usingConvertsTo` (Schüssel, Glasflasche), siehe `abweichungen.md`.

## 8. Implosionskompressor (`GT_MetaTileEntity_ImplosionCompressor`, ID 31, „Implosion Compressor“) – Teilpaket 6b.5b

Eigene MetaTileEntity wie der Hochofen (§5), Basis §1.1/§1.2/§1.5/§1.6. Unterschiede zum Hochofen:

| Wert | |
|---|---|
| `maxEUInput` | **32** (sichere Eingabe 32 · 4^Trafos), Energie von **allen** Seiten |
| `maxEUStore` | 10 000 (+ Upgrades §1.4), MJ-Kapazität 10 000 (nur mit MJ-Konverter) |
| Facing | nur **unten** (`isFacingValid = aFacing == 0`) – Ausrichtung fest, Schraubenschlüssel dreht nicht |
| Upgrades | Transformer, Energiespeicher/Lithium, MJ-Konverter – **kein Overclocker** (`isOverclockerUpgradable = false`) |
| Texturen (`terrain.png`, absolute Seiten) | oben 68 (aktiv 69), unten 71, Ost/West 73, Nord/Süd 74 |
| Beschreibung | „Over 9000 Gibbl!“; Tooltip „Max EU/p IN: 32“ |
| Rechtsklick | öffnet immer die GUI (keine Spulen) |

**Slots:** 0 = Eingabe 1, 1 = Eingabe 2, 2/3 = Ausgabe 1/2 (alle droppen). Seiten (absolut): oben → Slot 0,
unten/Nord/Süd → Slot 1, Ost/West → Slots 2–3.

**Struktur** (`checkMachine`, unabhängig vom Facing): Würfel 3 × 3 × 3 **unter** dem Controller, Mittelpunkt =
Controller − 2 nach unten (der Controller sitzt mittig auf der Oberseite). Mitte (y − 2) muss **Luft** sein (nur
Block-ID 0, keine Flüssigkeit); die **8 Ecken** (alle drei Koordinaten ≠ 0) **Standard**-Maschinengehäuse (Meta 13),
die übrigen **18** Positionen **verstärktes** Gehäuse (Meta 14). Andere Gehäuse (auch fortgeschritten) → unvollständig.
Prüfzeitpunkt wie §5 (`mUpdate`, `onMachineBlockUpdate`, `causeMachineUpdate`); ohne Heizspulen.

**Tick (`onPostTick`, Server):**
1. Wenn `mUpdate-- == 0`: `mMachine = checkMachine()`.
2. Aktiv = `mMachine`.
3. Wenn **nicht** `mMachine` oder kein Rezept läuft (`mMaxProgresstime ≤ 0`): bei gespeicherter Energie > 100
   `checkRecipe` (nur mit `mMachine`; Rezept zu (Slot 0, Slot 1) **nicht shapeless**, Platz für beide Ausgaben,
   Eingaben beim Start abziehen). Bricht die Struktur während eines Rezepts, steht es (kein Fortschritt, keine Energie).
4. Sonst: wenn `decreaseStoredEnergy(mEUt)` gelingt (kein Overclock, **kein** `constant_need_of_energy`-Rückfall,
   kein Fehlercode): `++mProgresstime > mMaxProgresstime` → Ausgaben wie §5 in Slot 2/3, Fortschritt 0. Danach, wenn
   `mProgresstime == mMaxProgresstime / 2`: **Explosionssound** („random.explode“, Lautstärke 4, Tonhöhe
   `(1 + (r1 − r2) · 0,2) · 0,7`) an der Maschine. Fehlt Energie, passiert in diesem Tick nichts.
   Ein Rezept dauert also `mDuration + 1` Ticks mit Energie. Die Sound-Bedingung gilt auch direkt nach dem Abschluss
   (`0 == 0 / 2`) – der Knall ertönt je Rezept zweimal.

**GUI** (`gui/Implosion.png`, `GT_Container_ImplosionCompressor`): Titel „Implosion Compressor“ (8, 4); Zeile (8, 63)
„Incomplete Machine Casing!“ nur bei unvollständiger Struktur; „Inventory“ (8, 72). Slots 0 (34, 16), 1 (34, 34),
Ausgaben 2 (86, 25), 3 (104, 25). Fortschrittspfeil (58, 28) ← (176, 0), Breite wie §5, Höhe 11. Shift-Klick aus dem
Spielerinventar nur in Slot 0/1. Info-Anzeige (GT-Scanner): „Progress:“, „n secs“, „/m secs“.

**Rezept des Kompressors:** `ABA/DCD/ABA`, A = fortg. Legierung, B = fortg. Maschinenblock
(`craftingRawMachineTier02`), D = el. Schaltkreis (`craftingCircuitTier02`), C = IC2-Compressor.

**Implosionsrezepte** (`addImplosionRecipe`, immer 20 Ticks, 32 EU/t): Eingabe 2 ist **Industrie-TNT** (bei fehlendem
IC2 TNT), Anzahl aus `RecipesAdvancedConfig.cfg` Abschnitt `implosion` (im Pack = Code-Werte):

| Eingabe 1 | ITNT | Ausgabe 1 | Ausgabe 2 | Umfang |
|---|---:|---|---|---|
| Iridium-Legierungsbarren | 8 | IC2-Iridiumplatte | 4 dunkle Asche | 6b.5b |
| 4 Diamantstaub | 32 | 3 Industriediamanten | 16 dunkle Asche | 6b.5b |
| 4 Smaragdstaub | 24 | 3 Smaragde | 12 dunkle Asche | 6b.5b |
| 4 Rubin-/Saphir-/grüner Saphir-/Olivinstaub | 24 | 3 Edelsteine | 12 dunkle Asche | mit GT-Erzen |
| 4 roter/gelber Granatstaub | 16 | 3 Granat | 8 dunkle Asche | mit GT-Erzen |

### 8.1 Iridium-Kette und Edelsteinstaub (6b.5b)

| Item | GT | Tooltip | Rezept / Herkunft |
|---|---|---|---|
| Iridiumbarren („Iridium Ingot“) | Material 16 | Ir | Compressor: IC2-Iridiumerz → 1 Iridiumbarren. Kein Macerator-/Ofen-Rückweg |
| Iridium-Legierungsbarren („Iridium Alloy Ingot“) | Material 4 | – | → 1: `IAI/ADA/IAI`, I = Iridiumbarren, A = fortg. Legierung, D = Diamantstaub (`DifficultIridiumPlate=true`) – im Pack Walzmaschinen-Rezept, ohne Railcraft **Werkbank** (wie die Heizspulen) |
| Diamantstaub („Diamond Dust“) | Dust 36 | C128 | Macerator: Diamant **und** Industriediamant (`gemDiamond`) → 1 Staub. Massefabrikator-Verstärker **125 000** |
| Smaragdstaub („Emerald Dust“) | Dust 35 | Be3Al2Si6O18 | Macerator: Smaragd → 1 Staub. Massefabrikator-Verstärker **50 000** |

Die IC2-Rezepte der Iridiumplatte (`IAI/ADA/IAI` mit Iridiumerz und Diamant bzw. Industriediamant) entfernt GT.
Später (Zutaten fehlen): Smaragdstaub im Assembler (8 + fortg. Schaltkreis) und Elektrolyseur (Berylliumzellen),
GT-Rückgewinnung von Werkzeugen/Rüstung im Macerator (`applyUsagesForMaterials`, für alle Materialien offen).

## 9. Industrielle Zentrifuge (`GT_MetaTileEntity_Centrifuge`, ID 62, „Industrial Centrifuge“) – Teilpaket 6b.5c

Aufbau **wie der Elektrolyseur (§4)**, Code gemeinsam in `GtTankMachineBlockEntity`. Unterschiede:

| Wert | |
|---|---|
| `maxEUInput` | **32** (ein IC2-Trafo-Upgrade → 128) |
| Tank | nur **Lava**, 10 000 mB, von allen Seiten befüllbar, nie entleert (`getTankPressure −100`); je Tick ≥ 1000 mB → +1 Lava im Holo-Slot 6 (max. 64), NBT `mStoredLava` |
| Rezeptsuche | erst (Slot 0, Slot 1), dann (Slot 6 = Lava, Slot 1); nicht shapeless |
| Rezepte | `addCentrifugeRecipe(in, nZellen, out1..4, Dauer)`: **immer 5 EU/t**, Eingabe 2 = n leere Zellen |
| Texturen | unten 32, oben 43 (aktiv 41), Seiten 27 (aktiv 25) |
| GUI | `gui/Centrifuge.png`: Eingabe 0 (80,35), Zellen 1 (50,5), Ausgaben 2–5 (80,5)/(110,35)/(80,65)/(50,35), Holo 6 (110,65); vier Pfeile wachsen nach außen (Fortschritt 0–10); Titel zweizeilig „Industrial“/„Centrifuge“ bei (110,4)/(110,12) |
| Beschreibung | „And round and round and round it goes“ |
| Rezept | `RCR/AEA/RCR`, R = raff. Eisen- **oder** Aluminiumplatte, C = fortg. Schaltkreis, A = fortg. Maschinenblock (`craftingRawMachineTier02`), E = Extraktor |

**Rezepte** (umgesetzt: alle mit erreichbarer Eingabe, Liste mit Dauern in `docs/STATUS.md` „Rezeptliste Zentrifuge“):
Magmacreme, Erde, Gras, Myzel, Harz, Nahrung → Methan (+ Goldäpfel/-karotte/glitzernde Melone), Gummiholz, Seelensand,
Lavazellen und Lava, Lapis, Redstone, Glowstonestaub, Bronze-/Kupfer-/Gold-/Nickel-/Eisen-/Zinn-/Silberstaub,
dunkle Asche, Wasserstoff → Deuterium → Tritium, Helium → Helium-3.

**Neue Materialien:** Zellen Deuterium (1, H-2), Tritium (2, H-3), Helium (3, He), Helium-3 (6), Methan (9, CH4),
Quecksilber (16, Hg), Öl (17); Stäube Lazurit (2), Pyrit (3), Salpeter (9), Rubin (32), Asche (62, „Ashes“);
kleine Stäube („Tiny Pile of …“, gleicher Index) Pyrit, Calcit, Sodalith, Wolfram, Blei, Zink, Nickel, Gold, Kupfer,
Zinn; Elektrumbarren (Material 21, AgAu). GT `dustSmall*`: 4 kleine → 1 Staub und 1 Staub → 4 kleine, umgesetzt für
Pyrit, Nickel, Gold, Kupfer, Zinn (die anderen Stäube gibt es nicht).
