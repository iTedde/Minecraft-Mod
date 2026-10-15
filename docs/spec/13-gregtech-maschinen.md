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
