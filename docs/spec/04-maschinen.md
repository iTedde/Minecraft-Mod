# 04 – Maschinen

Quelle: `ic2.core.block.machine.*`, `ic2.core.block.personal.*`, `ic2.core.block.wiring.TileEntityLuminator`.
Maschinenrezepte (Macerator/Extractor/Compressor) inkl. GregTech-Änderungen stehen in `08-rezepte.md`.

## Blöcke

`blockMachine` (Material Eisen, Härte 2,0, Metall-Sound):

| Meta | Maschine | Drop ohne Wrench | Wrench-Drop-Rate |
|---:|---|---|---:|
| 0 | Maschinenblock (Machine Casing, kein TE-Verhalten) | sich selbst | 1,0 |
| 1 | Eisenofen | sich selbst | 1,0 |
| 2 | Elektroofen | sich selbst | 1,0 |
| 3 | Macerator | Maschinenblock | 0,85 |
| 4 | Extractor | Maschinenblock | 0,85 |
| 5 | Compressor | Maschinenblock | 0,85 |
| 6 | Canning Machine | Maschinenblock | 0,85 |
| 7 | Miner | Maschinenblock | 1,0 |
| 8 | Pumpe | Maschinenblock | 1,0 |
| 9 | Magnetisierer | sich selbst | 1,0 |
| 10 | Elektrolyseur | Maschinenblock | 1,0 |
| 11 | Recycler | Maschinenblock | 0,85 |
| 12 | Fortgeschrittener Maschinenblock | sich selbst | 1,0 |
| 13 | Induktionsofen | fortg. Maschinenblock | 0,8 |
| 14 | Massefabrikator | fortg. Maschinenblock | 0,7 |
| 15 | Terraformer | fortg. Maschinenblock | 1,0 |

`blockMachine2` (Härte 2,0): 0 Teleporter (Drop: fortg. Maschinenblock), 1 Tesla-Spule (Drop: Maschinenblock),
2 Crop-Matron (Drop: Maschinenblock).

Allgemein:
- Platzierung: Front zum Spieler (nur horizontal). GUI per Rechtsklick (nicht beim Schleichen).
- Beim Abbau wird das Inventar gedroppt.
- „Wrench-Drop-Rate“: Wahrscheinlichkeit, dass der Schraubenschlüssel den Block selbst droppt; sonst kommt der
  normale Drop (siehe Tabelle). Elektrischer Schraubenschlüssel im Lossless-Modus: immer Original (200 EU-Äquivalent,
  siehe `05-items.md`).
- Rarity: Massefabrikator „rare“, Teleporter „rare“, Induktionsofen/Terraformer/fortg. Maschinenblock „uncommon“.
- Überspannung: Paket > `maxInput` → Explosion (`01-eu-system.md` §4), Ausnahmen sind angegeben.
- Machine Casings (Meta 0 und 12) haben kein Verhalten.

---

## Gemeinsame Logik „Standard-Elektromaschine“ (`TileEntityElectricMachine`)

Gilt für **Elektroofen, Macerator, Extractor, Compressor, Recycler**.

Konstruktor `(slots=3, e=Verbrauch EU/t, length=Dauer in Ticks, maxInput=32)`:
- Slots: 0 Eingabe, 1 Batterie (Energie-Slot), 2 Ausgabe, 3–6 **Upgrade-Slots** (4 Stück).
- `maxEnergy = e · length + maxInput − 1`, `defaultEnergyStorage = e · length`, Tier 1.
- Energieannahme: von allen Seiten.

Pro Tick:
1. Wenn `energy ≤ energyConsume · operationLength` und `canOperate()` → `provideEnergy()` aus Slot 1:
   - Redstone-Staub: `energy += defaultEnergyConsume · defaultOperationLength` (genau 1 Vorgang ohne Upgrades), 1 Staub
     verbraucht.
   - Elektrisches Item mit `canProvideEnergy`: `discharge(stack, maxEnergy − energy, tier, false, false)`.
   - SU-Battery: +1000.
2. Wenn `progress ≥ operationLength` → `operate()`, `progress = 0`, inaktiv.
3. Aktiv-Logik: Läuft bereits (`active && progress ≠ 0`) und `!canOperate || energy < energyConsume` → inaktiv,
   bei `!canOperate` zusätzlich `progress = 0`. Sonst wenn `canOperate && energy ≥ energyConsume` → aktiv.
   Kann nicht arbeiten → `progress = 0`.
4. Aktiv → `progress++`, `energy −= energyConsume`.

`canOperate`: Eingabe vorhanden, Rezept vorhanden, Ausgabe-Slot leer oder gleiches Item mit genug Platz.
`operate`: Eingabe um Rezeptmenge reduzieren (bei Items mit Container-Item wird das Item durch sein Container ersetzt
und die Menge nicht reduziert), Ergebnis in Slot 2.

Fortschritt bleibt erhalten, wenn nur Energie fehlt (Pause), wird aber zurückgesetzt, sobald nicht mehr gearbeitet
werden kann (Eingabe weg, Ausgabe voll).

Sided Inventory: oben → Slot 0, unten → Slot 1, Seiten → Slot 2.

### Upgrades (Slots 3–6, Item `upgradeModule` Meta 0/1/2)

Neuberechnung bei jeder Inventaränderung und beim Laden:

```
oc = min(Σ Overclocker, 32); tr = min(Σ Transformer, 10); es = Σ Energiespeicher
energyConsume   = (int)(defaultEnergyConsume · 1,6^oc)
operationLength = max(1, (int)round(defaultOperationLength · 0,7^oc))
maxInput        = defaultMaxInput · (int)4^tr
maxEnergy       = defaultEnergyStorage + es · 10 000 + maxInput − 1
tier            = tr + 1
progress        = round(alterFortschrittAnteil · operationLength)
```

Overclocker: schneller, aber mehr EU pro Vorgang (×1,6/0,7 ≈ 2,29 → +14 % EU je Vorgang pro Overclocker).
Transformer-Upgrade: erhöht die sichere Eingangsspannung ×4 und den Tier zum Entladen von Batterien.
Energiespeicher-Upgrade: +10 000 EU Puffer.

Upgrade-Stackgröße: **4** wie im Pack (GregTech `UpgradeStacksize=4`, `GT_Mod` setzt die Stackgröße des IC2-Upgrade-
Items, also für Overclocker, Transformer- und Energiespeicher-Upgrade; IC2 allein: 64). Umgesetzt in Phase 12.10.

### Werte der Standardmaschinen

| Maschine | EU/t | Ticks | EU/Vorgang | maxInput | maxEnergy | Rezepte |
|---|---:|---:|---:|---:|---:|---|
| Elektroofen | 3 | 130 | 390 | 32 | 421 | Vanilla-Ofenrezepte |
| Macerator | 2 | 400 | 800 | 32 | 831 | Macerator-Liste |
| Extractor | 2 | 400 | 800 | 32 | 831 | Extractor-Liste |
| Compressor | 2 | 400 | 800 | 32 | 831 | Compressor-Liste |
| Recycler | 1 | 45 | 45 | 32 | 76 | jedes Item |

Sounds: Elektroofen-Loop `Machines/Electro Furnace/ElectroFurnaceLoop.ogg`; Macerator `Machines/MaceratorOp.ogg`,
Extractor `ExtractorOp.ogg`, Compressor `CompressorOp.ogg`, Recycler `RecyclerOp.ogg`; Abbruch `Machines/InterruptOne.ogg`.

### Rezeptsuche (Macerator/Extractor/Compressor)

Liste von (Eingabe-Stack mit Menge → Ausgabe-Stack). Erster Treffer gewinnt; Treffer, wenn Item-ID und Meta exakt
gleich (`isItemEqual`) und Eingabemenge ≥ Rezeptmenge. Rezepte werden auch über OreDictionary-Events ergänzt
(z. B. jedes `ingotCopper` → Kupferstaub). Vollständige Listen: `08-rezepte.md`.

### Compressor + Pumpe

Steht eine **bereite** Pumpe (pumpCharge ≥ 200) über Wasser-Quellblock direkt unter oder seitlich (±X, ±Z) neben dem
Compressor, kann der Compressor ohne Eingabe arbeiten: Ergebnis 1 Schneeball, der Wasserblock unter der Pumpe wird
entfernt, `pumpCharge = 0`. Bedingung: Ausgabe leer oder Schneebälle < 16. Hat Slot 0 ein gültiges Rezept, hat das
Vorrang.

### Recycler

- Jedes Item ist gültige Eingabe (`canOperate`: Eingabe vorhanden und Ausgabe leer oder Schrott < 64).
- Pro Vorgang 1 Item verbraucht; mit Wahrscheinlichkeit **1/8** entsteht 1 Schrott – außer das Item steht auf der
  Blacklist (dann nie).
- Blacklist: Glasscheibe, Stock, Schneeball, Holzgerüst (fest) + Config `recyclerBlacklist` (Pack:
  `102, 280, 332, 4081` → Glasscheibe, Stock, Schneeball, ID 4081 = unbelegt) + GregTech, unbedingt (`GT_Mod`
  Z. 2080–2083: `up.aD`, `amq.aW`, `amq.aV`, `amq.aX` → Schneeball, Eis, Schneeschicht, Schneeblock). Die
  bedingten GT-Einträge (`DisableEasyMobGrindersInRecycler`, `DisableStoneInRecycler`) sind im Pack aus; GT-Kleinstäube
  nicht im Umfang. Tag `craft:recycler_blacklist`.

---

## Eisenofen (nicht elektrisch)

- Slots: 0 Eingabe, 1 Brennstoff, 2 Ausgabe. Vanilla-Ofenrezepte.
- Dauer **160 Ticks** pro Item (Vanilla-Ofen: 200).
- Brennstoff wie Vanilla (Brennwert = Ofen-Ticks), mit Sonderwerten aus `getFuelValueFor`: Lava-Behälter (auch
  Lavaeimer, Lavazelle) 2000 Ticks, Brennstoffkanister `value·2`.
- Neuer Brennstoff wird nur genommen, wenn `fuel ≤ 0` **und** gearbeitet werden kann (kein Verbrennen im Leerlauf).
- Fortschritt wird zurückgesetzt, wenn nicht gebrannt oder nicht gearbeitet werden kann.
- Erfahrungspunkte: keine (IC2 1.115 vergibt im Eisenofen keine XP).
- Sided: oben → 0, unten → 1, Seiten → 2. Partikel wie Vanilla-Ofen.

---

## Induktionsofen

| Wert | |
|---|---|
| Slots | 0, 1 Eingaben; 2 Batterie; 3, 4 Ausgaben |
| maxEnergy | 10 000 |
| maxInput | 128 (Tier 2) |
| Hitze | 0–10 000 (`heat`) |

Pro Tick:
1. `provideEnergy()` aus Slot 2 (Bedingung `energy ≤ maxEnergy` ist praktisch immer wahr):
   **Redstone gibt +maxEnergy (10 000) ohne Obergrenze**, SU-Battery +1000, elektrische Items über `discharge`.
   (Originalverhalten: Redstone wird jeden Tick nachgelegt, solange vorhanden; Energie kann über 10 000 steigen.)
2. Wenn `progress ≥ 4000` → beide Slotpaare (0→3, 1→4) je einmal schmelzen, `progress = 0`.
3. **Heizen**: Wenn `energy > 0` und (kann arbeiten **oder** Redstone-Signal): `energy −= 1`, `heat += 1` (max 10 000).
   Sonst `heat −= min(heat, 4)`.
4. Aktiv-Logik wie Standardmaschine, aber mit Schwelle `energy ≥ 15`.
5. Aktiv und arbeitsfähig: `progress += heat / 30`, `energy −= 15`.

→ Bei voller Hitze: +333 Fortschritt/Tick → ein Doppelvorgang alle 12 Ticks bei 16 EU/t. Kalter Start dauert lang.
Hitze-Anzeige in %: `heat·100/10000`. Wrench-Drop-Rate 0,8.
Sided: oben → 0 (2 Slots: 0,1), unten → 2 (1 Slot), Seiten → 3 (2 Slots: 3,4).
Ergänzt (6b.3a): Energiepakete > 128 → Explosion; Energieannahme kappt bei 10 000 (Rest zurück).
Beim Start ist `heat == 0` → inaktiv. Keine Upgrades, keine Sounds. Redstone-Signal = `isBlockIndirectlyGettingPowered`.
GUI (`GUIInduction.png`, 176 × 166): Titel zentriert y 6, „Heat:“ bei (10, 36), Wert „n%“ bei (10, 44), Inventar y 72.
Slots: Eingaben (47, 17), (63, 17), Batterie (56, 53, `SlotDischarge` Tier 2), Ausgaben (113, 35), (131, 35) mit
Ofen-XP. Energiebalken bei (56, 36 + 14 − h) ← (176, 14 − h), h = 14 · energy / 10 000 (ungekappt); Fortschritt bei
(79, 34) ← (176, 14), Breite `24 · progress / 4000 + 1`, Höhe 16. Shift-Klick bevorzugt Slot 0, wenn Slot 0 leer ist,
sonst Slot 1 (IC2 `getInput`). Textur `block_machine.png` Spalte 13.

---

## Canning Machine

| Wert | |
|---|---|
| Slots | 0 Eingabe, 1 Batterie, 2 Ausgabe, 3 Behälter (Dose/Kanister/Jetpack/CF-Pack) |
| maxEnergy | 631 |
| maxInput | 32 |
| Verbrauch | 1 EU/t |

Modus nach Slot 3: Zinndose → 1, leerer Brennstoffkanister oder Jetpack → 2, CF-Pack → 3, sonst 0 (nichts).

**Modus 1 – Lebensmittel in Dosen**
- Nahrungswert `food = ceil(healAmount / 2)`, Kuchen (Item/Block) = 6, gefüllte Dose = 0 (ungültig).
- Dauer `food · 50` Ticks. Verbraucht 1 Nahrung und `food` Zinndosen, erzeugt `food` gefüllte Dosen.
- Meta der gefüllten Dose: verdorbenes Fleisch 1, Spinnenauge 2, giftige Kartoffel 2, rohes Hühnchen 3,
  goldener Apfel 4, verzauberter goldener Apfel 5, sonst 0. Ausgabe muss leer sein oder gleiche Meta haben.
- Pilzsuppe hinterlässt eine Schüssel.

**Modus 2 – Brennstoff**
- Brennstoffwert pro Item: Kohle-Brennstoffzelle (coalfuelCell) 2548, Bio-Brennstoffzelle (biofuelCell) 868;
  Redstone `+20 %`, Glowstone-Staub `+30 %`, Schwarzpulver `+40 %` des bisher gesammelten Werts (nur wenn > 0).
- Alle 100 Ticks wird 1 Eingabe-Item verbraucht und addiert (bei 100…500). Bei 600 Ticks letztes Item und
  Abschluss: leerer Kanister → gefüllter Kanister mit NBT `value = fuelQuality`; Jetpack → Jetpack mit
  `damage = max(1, damage − fuelQuality)`. Ausgabe muss leer sein.
- Fehlt zwischendurch Eingabe, bleibt der Fortschritt (nur in Modus 2) erhalten.

**Modus 3 – CF-Pack füllen**
- Alle 50 Ticks: 1 Schaumpellet verbraucht, CF-Pack-Damage −2. Fertig (Pack → Ausgabe), wenn keine Pellets mehr oder
  Damage ≤ 1. Benötigt Damage > 2 zum Start.

Wrench-Drop-Rate 0,85. Sided: links (relativ zur Front) → Slot 1, unten → 3, oben → 0, sonst → 2.

**Tick (`TileEntityElecMachine` ohne Upgrades, Ergänzung 6b.5a):** `canOperate` = Slot 0 belegt und je Modus: (1)
`food > 0`, `food ≤` Dosen in Slot 3, Ausgabe leer oder gefüllte Dose gleicher Meta mit Platz für `food`; (2)
Brennstoffwert des Eingabe-Items > 0 und Ausgabe leer; (3) Pack-Damage > 2, Eingabe = Schaumpellet, Ausgabe leer.
Ablauf je Tick: Energie ≤ 600 und `canOperate` → Batterie-Slot entladen (`provideEnergy`: elektrische Items Tier 1,
Redstone +631 ohne Deckel, Single-Use-Batterie +1000). Fertig-Bedingung (Modus 1: `progress ≥ food·50`; Modus 2:
`progress > 0 && progress % 100 == 0`; Modus 3: `progress ≥ 50`) → `operate` (Modus 2 unter 600: inkrementell, sonst
abschließend + `fuelQuality = 0`, `progress = 0`, inaktiv). Danach: aktiv mit Fortschritt, aber nicht mehr
`canOperate` oder Energie < 1 → inaktiv (ohne `canOperate` und nicht Modus 2: Fortschritt/Qualität = 0); sonst bei
`canOperate` und Energie ≥ 1 aktiv; ohne `canOperate` und nicht Modus 2 Reset. Aktiv → `progress++`, Energie −1.
Brennstoffwert im Modus 2: Kohle-/Bio-Brennstoffzelle 2548/868; Redstone/Glowstone-Staub/Schwarzpulver
`(int)(fuelQuality · 0,2/0,3/0,4)`, nur wenn `fuelQuality > 0`. Modus 1 Pilzsuppe: letzte Suppe → Schüssel bleibt in
Slot 0. NBT `fuelQuality`. Keine Sounds. Energiespeicher 631, Eingang 32 (darüber Explosion), Tier 1.

**GUI** (`GUICanner.png`, 176 × 166): Slots Eingabe 0 (69, 17), Batterie 1 (30, 45), Ausgabe 2 (119, 35), Behälter 3
(69, 53). Titel zentriert y 6. Energie-Flamme bei (31, 27) ← (176, 0), 14 × 14 von unten, Höhe
`min(14, energy·14/600)` (nur bei Energie > 0). Fortschritt bei (74, 36) ← (176, 15), Breite `progress·34/l`, Höhe 13;
`l` = 50·food (Modus 1, Eingabe belegt, food > 0), 50 (Modus 3), sonst 600. Sync: Fortschritt, Energie.
Shift-Klick: bevorzugt der erste leere Slot von 0–2 (`firstEmptyFrom(0, 2)`, Ende inklusive; die Ausgabe nimmt nichts an), sonst IC2-Standard.
Textur: `block_machine.png` Spalte 6 (Zeilen 0–5 unten/oben/hinten/vorne/links/rechts, +6 aktiv).

---

## Miner

| Wert | |
|---|---|
| Slots | 0 Batterie, 1 Scanner, 2 Bergbaurohre, 3 Bohrer |
| maxEnergy | 1000 |
| maxInput | 32 |
| Tier | 1 (Config `enableMinerLapotron=false`; sonst 3) |

- Betrieb (`isOperating`): `energy > 100`, Slot 2 = Bergbaurohre, Slot 3 = Bergbaubohrer oder Diamantbohrer,
  nicht „festgefahren“.
- Während Betrieb pro Tick: `energy −= 1`; Scanner in Slot 1 wird mit Tier 2 aus dem Miner-Puffer geladen, Bohrer in
  Slot 3 mit Tier 1.
- `provideEnergy` jeden Tick (Redstone gibt +1000).
- **Abbau-Zyklus**: Ziel suchen (`aquireTarget`): Rohrspitze ermitteln; mit Scanner wird pro Ebene ein Layer-Scan
  gemacht (Kosten/Reichweite siehe Scanner in `05-items.md`): im Quadrat ±Reichweite wird das erste „wertvolle“ und
  abbaubare Erz (Config `valuableOres`, Wert > 0) gewählt – bei angeschlossener Pumpe auch Lava-Quellblöcke.
  Sonst Ziel = Block direkt unter der Rohrspitze.
- Erreichbarkeit: Weg vom Ziel zum Rohr (in X/Z, zuerst entlang der größeren Differenz) muss passierbar sein
  (Luft, Wasser, Lava, Miner, Rohr, Rohrspitze); sonst wird das Ziel schrittweise Richtung Rohr verschoben.
- Abbaubar (`canMine`): Luft; nicht Rohr/Rohrspitze/Truhe; Flüssigkeit nur mit Pumpe (die abernten kann); Härte ≥ 0;
  Material ohne Werkzeugzwang oder Spinnennetz; sonst muss der Bohrer den Block abbauen können (Harvest-Level).
  Sonst „festgefahren“ (`miningTicker = −1`) – Flüssigkeit ohne Pumpe ist auch festgefahren,
  außer überhaupt eine Pumpe ist angrenzend.
- Pro Tick am Ziel: Bergbaubohrer `miningTicker += 1`, `energy −= 1`; Diamantbohrer `+4` und `energy −= 15`.
  Bei `miningTicker ≥ 200` → Block abbauen:
  - Bohrer-Ladung: Bergbaubohrer 50 EU, Diamantbohrer 80 EU (`ElectricItem.use`).
  - Drops (mit Fortune-Level des Bohrers) in angrenzende Inventare/Miner verteilt (`StackUtil.distributeDrop`).
    Flüssigkeit (nur Quellblock) → Pumpe füllt Behälter (`usePump`).
  - `energy −= 2 · (minerY − zielY)`.
  - Ist das Ziel direkt unter dem Miner: Rohr setzen, `energy −= 10`; Rohrspitze aktualisieren.
- **Rohre einziehen** (kein Bohrer in Slot 3): alle 20 Ticks (2 EU/Tick) wird die unterste Rohrspitze entfernt
  (Drop ins Inventar); liegt in Slot 2 ein Block (kein Rohr), wird er an der Stelle platziert (Loch füllen).
- Sided: links/vorne → Slot 3, rechts/hinten → Slot 1, unten → 0, oben → 2.
- Sound `Machines/MinerOp.ogg`.

Bergbaurohr/-spitze: Härte 6,0, Resistenz 10.

### Details aus `TileEntityMiner` (nachgetragen für Phase 12.3)

- Basis `TileEntityElecMachine(4 Slots, Batterie-Slot 0, maxEnergy 1000, maxInput 32, Tier 1)`, Block-Meta 7,
  Wrench-Drop-Rate 1,0, Drop Maschinenblock. NBT `targetX/Y/Z`, `miningTicker` (short).
- Tick: (1) `wasOperating = isOperating()`; dann `energy −1`, Scanner (Slot 1) mit Tier 2 und Bohrer (Slot 3) mit
  Tier 1 aus dem Puffer laden. (2) `energy ≤ maxEnergy` → `provideEnergy` (Redstone +1000, Einwegbatterie +1000,
  elektrische Items Tier 1). (3) `wasOperating` → `mine()`; sonst **ohne Bohrer**: `energy ≥ 2` und Rohr/Spitze
  direkt unter dem Miner → Ziel verwerfen, `miningTicker +1`, `energy −2`, bei 20 → `withdrawPipe()`; sonst, wenn
  festgefahren, `miningTicker = 0` (Bohrer herausnehmen löst das Festfahren). (4) `active = isOperating()`.
- `mine()`: kein Ziel → `aquireTarget()` (eigener Tick). Ziel nicht erreichbar → Ziel einen Schritt Richtung Rohr
  (größere Differenz zuerst), eigener Tick. Abbaubar → Ticker/Energie wie oben; Diamantbohrer: `+1 +3`, `−1 −14`.
  Nicht abbaubar: Wasser/Lava (still oder fließend) und irgendeine Pumpe angrenzend → warten; sonst festgefahren
  (`miningTicker = −1`, `stuckOn` = Blockname – **wird in der GUI nicht angezeigt**).
- `aquireTarget()`: `y` = Höhe der untersten Rohrspitze (bzw. Miner-Höhe ohne Rohr). Mit Rohr und Scanner:
  `startLayerScan` (kostet die Scan-Energie, sonst Radius 0) und Suche in der **Ebene der Spitze** (x, dann z
  aufsteigend): erster Block mit Erzwert > 0, der abbaubar ist, **oder** (irgendeine Pumpe angrenzend) Lava-Quellblock.
  Sonst Ziel = Block unter der Spitze.
- `mineBlock()`: Bohrer-Ladung −50/−80 (`use` ohne Spieler). Luft → nichts. Fließende Flüssigkeit → nichts. Sonst:
  fester Block → Drops mit dem Bohrer als Werkzeug (Fortune) an `distributeDrop`; Wasser/Lava-Quelle → erste
  angrenzende erntefähige Pumpe (oben, unten, +X, −X, +Z, −Z) `pumpThis`; Block entfernen, `energy −= 2·(minerY − y)`.
  Liegt das Ziel unter dem Miner: Bergbaurohr dorthin, `energy −10`. Dann `updateMineTip(zielY)`: zwischen Miner und
  Ziel fehlende Rohre aus Slot 2 ergänzen (je 1 Item), Spitze auf `zielY` setzen.
  **Originaleigenheit:** das Rohr am Ziel und die Spitze kosten kein Item; ein Strang der Tiefe n verbraucht n − 1
  Rohre, beim Einziehen kommen n zurück (die Spitze droppt ein Rohr).
- `withdrawPipe()`: Block an der Spitze abbauen (Drops ohne Fortune, Spitze → 1 Rohr) und entfernen; liegt in Slot 2
  ein Block, der kein Rohr ist, wird er dort gesetzt (Slot −1, Loch füllen). Danach `updateMineTip(y + 1)` (das Rohr
  darüber wird zur Spitze; `updateMineTip` nimmt aus Slot 2, was immer dort liegt).
- `canMine`: Luft ja; Rohr, Spitze, Truhe nein; Wasser/Lava ja, wenn eine **erntefähige** Pumpe angrenzt; Härte < 0
  nein; Block ohne Werkzeugzwang (feste Blöcke) oder Spinnennetz ja; sonst Bohrer-Abbaurecht inkl. Harvest-Level
  (Bergbaubohrer 2, Diamantbohrer 3). Andere Flüssigkeiten (z. B. Öl) → festgefahren.
- `canPass` (Weg zum Rohr): Luft, Wasser, Lava, Miner, Rohr, Spitze.
- **GUI** (`GUIMiner.png`, 176 × 166): Titel zentriert y 6, „Inventory“ (8, 72). Slots: Batterie 0 (81, 59, Tier 1),
  Scanner 1 (117, 22, nur Scanner), Rohre 2 (81, 22, jeder Block), Bohrer 3 (45, 22, nur Bergbau-/Diamantbohrer).
  Energie: Höhe `min(14, energy·14/1000)` bei (81, 55 − h) ← (176, 14 − h), 14 breit. Sonst keine Anzeige. Sync:
  `miningTicker`, Energie. Shift-Klick: IC2-Standard (Eingabe-Slot 0).
- Seiten: „vorne“ ist im Original die der Front **gegenüberliegende** Seite (Originaleigenheit der Tabelle): links und
  hinten (von der Front aus) → Bohrer, rechts und Front → Scanner, unten → Batterie, oben → Rohre.
- Sound: `MinerOp.ogg` als Loop, solange aktiv.
- Rohrspitze `BlockMiningTip`: Härte 6, Resistenz 10, Textur `block_0.png` Index 36, droppt ein Bergbaurohr, nicht im
  Kreativinventar. Miner-Textur `block_machine.png` Spalte 7.
- **GT im Pack:** GregTech ergänzt Glowstone und Seelensand mit Wert 1 in `valuableOres` (gilt auch für die Scanner).
  `MinerListLavaAddition` steht in der Config, im GT-2.90-Code gibt es dazu keine weitere Logik – die Lava-Suche ist
  IC2-Verhalten (Pumpe angrenzend) und damit umgesetzt.

---

## Pumpe

| Wert | |
|---|---|
| Slots | 0 Behälter (Eimer/Zelle), 1 Batterie |
| maxEnergy | 200 |
| maxInput | 32 |

- Pro Tick: wenn `energy > 0` und nicht bereit → `energy −= 1`, `pumpCharge += 1`. Bereit bei `pumpCharge ≥ 200`
  (also 200 EU / 10 s pro Pumpvorgang).
- Bereit und erntefähig (Slot 0 hat leeren Eimer oder leere Zelle, oder angrenzende Truhe hat einen leeren Eimer):
  - Quellblock Wasser/Lava (Meta 0) direkt darunter → Block entfernen und abfüllen:
    - Lava: zuerst an angrenzende **Geothermie-Generatoren** liefern (3000 Einheiten Angebot, jeder nimmt
      bis zur Tankgrenze, erhält aber nur **die Hälfte** gutgeschrieben → max. 1500 mB). Erfolgreich, wenn insgesamt
      > 20 abgenommen wurden.
    - Sonst leerer Eimer in Slot 0 → Wasser-/Lavaeimer (ausgegeben in angrenzende Inventare/Drop); leere Zelle →
      Wasser-/Lavazelle; sonst leerer Eimer in angrenzender Truhe wird gefüllt.
  - Kein Quellblock: Forge-`FillBucketEvent` für Fremd-Flüssigkeiten mit leerem Eimer.
- Mit Miner (angrenzend) und Compressor (s. o.) kombinierbar.
- Sound `Machines/PumpOp.ogg`. Sided: unten → 1, sonst → 0.

### Details aus `TileEntityPump` (nachgetragen Phase 8d.1)

- Basis `TileEntityElecMachine(2 Slots, Batterie-Slot 1, maxEnergy 200, maxInput 32)`, Tier 1, Block-Meta 8
  (Härte 1,0, Drop Maschinenblock wie die anderen Maschinen). `pumpCharge` (short) als NBT `pumpCharge`.
- Tick-Reihenfolge: (1) `energy > 0` und nicht bereit → `energy −1`, `pumpCharge +1`; (2) wenn `energy ≤ maxEnergy`:
  `provideEnergy` (Batterie-Slot); (3) bereit → `pump()`; (4) wenn `active == bereit` und `energy > 0` → `active`
  umschalten. „Aktiv“ heißt also „lädt“ (Loop-Sound läuft beim Laden, nicht beim Pumpen); ohne Energie bleibt der
  Zustand stehen.
- `pump()`: nur wenn erntefähig (Slot 0 = leere Zelle oder leerer Eimer, **oder** ein leerer Eimer in einer der sechs
  angrenzenden Truhen). Kein Wasser-/Lava-Quellblock (Meta 0, still oder fließend) darunter: nur mit leerem Eimer in
  Slot 0 ein `FillBucketEvent` an y − 1 (andere Mods, z. B. BC-Öl); gefüllter Eimer → `distributeDrop`, Slot 0 −1,
  `pumpCharge = 0`. Sonst passiert nichts (Ladung bleibt voll).
- Quellblock darunter: Block **zuerst entfernen**, dann `pumpThis`:
  1. Lava → `deliverLavaToGeo`: 3000 mB nacheinander an Geothermie-Generatoren oben, unten, +X, −X, +Z, −Z
     (`distributeLava`, s. o.); Erfolg bei Rest < 2980 → `pumpCharge = 0`.
  2. Leerer Eimer in Slot 0: der **ganze Stapel** wird zu Wasser-/Lavaeimern (Item-ID ersetzt) und per
     `distributeDrop` ausgegeben; Slot 0 leer (Originaleigenheit: n Eimer für einen Block).
  3. Leere Zelle in Slot 0: −1, eine Wasser-/Lavazelle per `distributeDrop`.
  4. Sonst: `pumpCharge = 0`, erster leerer Eimer in der ersten angrenzenden Truhe (oben, unten, +X, −X, +Z, −Z;
     nur Vanilla-Truhen) wird umgewandelt – ebenfalls der **ganze Stapel** dieses Slots.
- `StackUtil.distributeDrop` (auch Miner): Nachbarn in der Reihenfolge −X, +X, −Y, +Y, −Z, +Z; nur Inventare mit
  ≥ 6 Slots (bei Seiten-Inventaren ≥ 6 Slots von dieser Seite); Truhen mit horizontaler Nachbartruhe als
  Doppeltruhe. Einfügen: erst auf gleiche Stapel auffüllen, dann leere Slots. Rest als Item-Entity im Pumpenblock
  (Zufallsversatz 0,15–0,85, Aufhebeverzögerung 10).
- GUI `GUIPump.png` (176 × 166): Titel „Pump“ zentriert (y 6), „Inventory“ (8, 72). Slot 0 (62, 17) nimmt **nur
  leere Zellen** an (`SlotCustom` mit `Ic2Items.cell` – Eimer nur per Automatisierung), Batterie-Slot 1 (62, 53).
  Energie: Höhe `min(14, energy · 14 / 200)` bei (62, 50 − h) ← (176, 14 − h), 14 breit. Ladung
  `i = min(41, pumpCharge · 41 / 200)`: Zeiger (99, 61 − i) ← (176, 55), 12 × 5; Füllung (99, 66 − i) ← (176, 14),
  12 × i; darüber Skala (98, 19) ← (188, 14), 13 × 47. Shift-Klick wie die anderen IC2-Maschinen.
- `fountain()` existiert im Code, wird aber nie aufgerufen → entfällt.

### Bergbaurohr (`BlockMiningPipe`)

Block, Härte 6,0, Resistenz 10, Material Eisen, nicht voll (Kollisions-/Auswahlbox 0,375–0,625 in X/Z, volle Höhe),
eigenes Render-Modell (dünnes Rohr), Rarity normal, droppt sich selbst. Rezept → 8: `I I/I I/ITI`, I = raff. Eisen
(`ingotRefinedIron`), T = Baumzapfer. Die Rohrspitze (`BlockMiningTip`) kommt erst mit dem Miner.

---

## Magnetisierer

- Kein Inventar, nimmt Energie von allen Seiten, `maxEnergy = 100`, `maxInput = 32`, Annahme bis
  `maxEnergy + maxInput − 1`.
- Alle 10 Ticks (bzw. jeden Tick, solange sich etwas ändert): Eisenzäune (`ironFence`) in einer Säule ober- und
  unterhalb (je bis 20 Blöcke, ohne Unterbrechung) werden auf Magnetisierung 15 (Meta) aufgeladen; Kosten =
  fehlende Stufen. **Originalfehler:** Ist der Bedarf größer als die Energie, wird die Energie auf den Bedarf gesetzt
  (Gratis-Aufladung, solange `energy > 0`).
- Eisenzaun (Härte 1,5, Resistenz 5): Spieler mit Metallstiefeln (Vanilla Eisen, Gold, Kette; IC2: Bronze-, Nano-, Quantum-Stiefel, Static Boots –
  alle `IMetalArmor`) an einem magnetisierten Zaunpfahl: Meta −1 pro Tick,
  `motionY += 0,075`, bei Aufwärtsbewegung `×1,03` und `fallDistance = 0`; Max. 1,5 (schleichend 0,3).
  Ohne Magnetisierung: Schleichen bremst Fall (`×0,9` bei < −0,25, sonst `fallDistance = 0`).
- Ticker startet bei 0: erster Durchlauf sofort; nach einem Durchlauf mit Änderung läuft der nächste im folgenden Tick,
  ohne Änderung erst nach 11 Ticks (`ticker = 10`, Prüfung `ticker-- <= 0`). Säule nach unten bis y > 0 (1.21: über
  Weltuntergrenze), Abbruch auch bei `energy = 0`. Facing horizontal zum Spieler, Texturen Spalte 9 von
  `block_machine.png` (alle Seiten schlicht). Kein GUI. Überspannung: `explodeMachineAt`. Drop/Wrench: sich selbst, 1,0.

---

## Elektrolyseur

- Kein eigener Netzanschluss; arbeitet mit einem **angrenzenden Speicherblock** (BatBox/MFE/MFSU; gesucht alle
  16 Ticks in Reihenfolge unten, oben, −X, +X, −Z, +Z).
- Slots: 0 Wasserzelle, 1 elektrolysierte Wasserzelle. Interner Puffer `energy` (short).
- Rate nach Speicher-Tier: BatBox 2, MFE 8, MFSU 32 EU/Tick.
- **Laden** (Speicher ≥ 70 % voll, Wasserzelle in Slot 0, Platz in Slot 1): pro Tick Rate vom Speicher abziehen und in
  den Puffer; bei ≥ 20 000 → 1 Wasserzelle → 1 elektrolysierte Wasserzelle (Puffer −20 000).
- **Entladen** (Speicher ≤ 30 % voll): hat der Puffer Energie, pro Tick Rate in den Speicher. Ist der Puffer leer und
  Slot 1 hat eine elektrolysierte Zelle (und Slot 0 Platz) → Puffer `+= 12 000 + 2000 · tier` und die Zelle wird
  zur Wasserzelle (BatBox 14 000, MFE 16 000, MFSU 18 000 EU zurück).
- Sided: unten → 1, sonst → 0.
- Texturen Spalte 10, aktiv (Laden oder Entladen) Zeilen 6–11. Tick nur serverseitig; `ticker` startet zufällig 0–15.
  Laden und Entladen schließen sich aus (≥ 70 % / ≤ 30 %); der Speicher wird direkt verändert (kein Netz, keine
  Kappung). Zustandswechsel → aktiv/inaktiv.
- GUI (`GUIElectrolyzer.png`): Titel mittig y 6, Slot 0 (53, 35) nur Wasserzelle, Slot 1 (112, 35) nur elektrolysierte
  Wasserzelle; Energiebalken bei (79, 34) ← (176, 14), Breite `energy · 24 / 20000` (max. 24) + 1, Höhe 16, nur bei
  `energy > 0`.

---

## Massefabrikator

| Wert | |
|---|---|
| Slots | 0 Verstärker (Schrott), 1 Ausgabe (UU-Materie) |
| maxEnergy | 1 100 000 |
| maxInput | 512 (Überspannung: eigene Explosion, s. u.) |
| Kosten | 1 000 000 EU pro UU-Materie |

- Redstone-Signal → aus (nimmt keine Energie an, `demandsEnergy = 0`).
- Pro Tick (wenn nicht Redstone und `energy > 0`):
  - Verstärker: Liegt `scrap < 1000` und Slot 0 enthält einen Verstärker, wird 1 verbraucht und `scrap += Wert`
    (Schrott 5000, Schrottbox 45 000).
  - `energy ≥ 1 000 000` → 1 UU-Materie in Slot 1 (wenn Platz), `energy −= 1 000 000`.
- `injectEnergy(amount)`: `bonus = min(amount, scrap)`; `scrap −= bonus`; `energy += amount + 5 · bonus`.
  → Mit Schrott liefert jede EU 6 EU (bis der Schrottwert aufgebraucht ist).
- Überspannung (> 512): Block entfernt, `ExplosionIC2(power 15, dropRate 0,01, damage 1,5)`.
- GUI: Fortschritt `energy / 10 000` %. Sounds: `Generators/MassFabricator/MassFabLoop.ogg`, mit Schrott zusätzlich
  `MassFabScrapSolo.ogg`. Wrench-Drop-Rate 0,7. Sided: unten → 0, sonst → 1.
- Details (aus `TileEntityMatter`/`GuiMatter`/`ContainerMatter`):
  - Ablauf je Tick: Redstone **oder** `energy == 0` → Zustand „Leerlauf“, inaktiv. Sonst Zustand „läuft“ (bzw.
    „läuft mit Schrott“, wenn `scrap > 0`), aktiv; erst dann Verstärker nachlegen (nur ein Item pro Tick, Container-
    Items geben ihren Behälter zurück), danach ggf. 1 UU-Materie erzeugen. Der Verstärker wird also erst gezogen,
    wenn schon Energie im Puffer ist.
  - Sounds je Zustand: Leerlauf → beide aus; „läuft“ → Loop an, Schrott-Loop aus; „läuft mit Schrott“ → beide an.
  - `injectEnergy` prüft Überspannung zuerst (Block → Luft, dann Explosion), gibt Überschuss über `maxEnergy` zurück.
    Schrott-Bonus wirkt auch, wenn das Paket den Puffer überfüllt (Überschuss geht ans Netz zurück).
  - NBT: `scrap` (int), Energie wie Elektromaschine. Kein Upgrade-Slot, kein Batterie-Slot.
  - Slots: Verstärker (114, 54) – nur registrierte Verstärker; Ausgabe (114, 18). Spielerinventar ab y 84.
  - GUI `GUIMatter.png`: Titel zentriert y 6, „Fortschritt:“ (16, 20), Prozent (16, 28); bei `scrap > 0`
    „Verstärker:“ (16, 44) und Schrottwert (16, 56). Keine Balken.
  - Rezept siehe `08-rezepte.md` (IC2-Rezept, im Pack entfernt, hier trotzdem umgesetzt).

---

## Terraformer

- Slot 0: Terraformer-Blaupause (TFBP). `maxEnergy = 100 000`, `maxInput = 512` (Annahme ohne Puffer-Überlauf).
- Pro Tick, wenn `energy ≥ consume` der Blaupause:
  - Hatte der letzte Versuch Erfolg: Ziel zufällig im Umkreis `range/10` um die letzte Position.
  - Sonst: Radius `range · (fehlversuche+1) / 5` (fehlversuche max. 4) um den Terraformer.
  - Zufallsoffset je Achse: `−random(r+1) + random(r+1)`.
  - Erfolg → `energy −= consume`, Fehlversuche = 0; Misserfolg → `energy −= consume/10`, Fehlversuche + 1.
- Blaupause ein-/auswerfen: Rechtsklick mit TFBP auf den Terraformer setzt sie ein (alte wird ausgeworfen);
  Schraubenschlüssel wirft sie aus.

| Blaupause | consume (EU) | range | Wirkung (Kurzfassung) |
|---|---:|---:|---|
| Cultivation | 4000 | 40 | Sand→Erde (von oben), Erde→Gras, auf Gras Pflanzen (Gras, Blumen, Setzlinge, Weizen auf Ackerboden, Pilze, Kürbis, Gummisetzling) |
| Irrigation | 3000 | 60 | 1/48000: Regen starten; Sand→Erde; Setzlinge wachsen; Baumstamm wächst +1 mit Blättern; Weizen reif; Feuer löschen; Gras ausbreiten |
| Chilling | 2000 | 50 | Wasser→Eis; Eis über Wasser verdicken; Schneeschicht setzen/zu Schneeblock |
| Desertification | 2500 | 40 | Erde/Gras/Ackerboden→Sand; Wasser, Schnee, Blätter, Pflanzen entfernen; Eis/Schneeblock→Wasser; Holz 1/15 anzünden |
| Flatification | 4000 | 40 | Terrain auf Höhe des Terraformers einebnen (Erde auffüllen bzw. Liste natürlicher Blöcke entfernen; Suche ab y+20) |
| Mushroom | 8000 | 25 | Myzel ausbreiten (setzt Biom Pilzinsel), Pilze und Riesenpilze wachsen |

Exakte Algorithmen: `reference/decompiled/ic2/ic2/core/item/tfbp/*.java` (für die Umsetzung 1:1 nachbauen).

### Details aus `TileEntityTerra` und `ItemTFBP*` (nachgetragen für Phase 12.5)

- `TileEntityElecMachine(1 Slot, maxEnergy 100 000, maxInput 512)`, Meta 15, kein GUI, kein Batterie-Slot.
  Annahme bis genau `maxEnergy` (Rest zurück), Paket > 512 → Explosion. NBT nur Energie und Inventar
  (`failedAttempts`, letzte Position gehen beim Neuladen verloren). Loop `Terraformers/TerraformerGenericloop.ogg`
  solange aktiv; aktiv, sobald ein Versuch läuft, inaktiv nach > 30 Ticks ohne Versuch.
- Tick: Blaupause in Slot 0 und `energy ≥ consume` → ein Versuch. Mit letztem Erfolg: `r = range/10` um (lastX,
  lastZ); sonst `fehl = min(fehl, 4)`, `r = range·(fehl+1)/5` um den Terraformer; `x ± (rand(r+1) − rand(r+1))`.
  `terraform(world, x, z, y_Terraformer)`: Erfolg → `−consume`, fehl = 0, Position merken; sonst `−consume/10`,
  fehl+1, Position vergessen.
- Einsetzen: Rechtsklick mit Blaupause auf den Terraformer (alte wird als Item ausgeworfen), Handstapel weg.
  Schraubenschlüssel: wirft aus (−1 Haltbarkeit, Sound), statt zu drehen/abzubauen. Beim Abbau droppt der Inhalt.
- Hilfsfunktionen: `getFirstSolidBlockFrom(y)` = erster Normalwürfel abwärts (> 0), `getFirstBlockFrom(y)` = erster
  Nicht-Luft-Block; −1 = nichts. `switchGround(from, to, aufwärts)`: aufwärts → unterster `from`-Block der Säule,
  die an `y` endet, wird `to`; abwärts → unter den `to`-Blöcken ab `y` wird der erste `from`-Block `to`.
- **Cultivation** (4000/40): `y = solid(y+10)`; Sand→Erde (aufwärts) **oder** Erde→Gras **oder** auf Gras: Platz
  darüber Luft oder (1/4) hohes Gras → Pflanze (Liste der Reihe nach je 2/5: hohes Gras, Rose, Löwenzahn, Setzling,
  Weizen (Erde darunter → Ackerboden), roter Pilz, brauner Pilz, Kürbis, Gummisetzling; sonst hohes Gras).
- **Irrigation** (3000/60): 1/48 000 Regen an (`setRaining`, Zeit bleibt) = Erfolg; `y = block(y+10)`; Sand→Erde
  (aufwärts, bei Erfolg zweimal); hohes Gras oben → Gras ausbreiten (+X, −X, +Z, −Z je 1/2: Säule ab gleicher Höhe:
  Erde→Gras, Gras → hohes Gras darüber); Setzling/Gummisetzling wächst sofort; Stamm: gleicher Stamm darüber,
  Blätter derselben Holzart an y+2 und vier Seiten auf y+1 (nur Luft); Weizen → reif; Feuer → Luft.
- **Chilling** (2000/50): `y = block(y+10)`; Wasser (auch fließend) → Eis; Eis über Wasser → darunter Eis; Schneeschicht
  mit Schnee/Schneeblock in allen vier Nachbarsäulen (je ab y+16, nicht tiefer als y) → Schneeblock; sonst Schneeschicht
  auf y+1, wenn dort möglich (oder auf Eis) – **kein** Erfolg. `isSnowHere` legt dabei auch Schnee auf die Nachbarn.
- **Desertification** (2500/40): `y = block(y+10)`; Erde/Gras/Ackerboden → Sand (abwärts, dann Erde nochmal) **oder**
  Wasser/Schneeschicht/Blätter/Gummiblätter/Pflanzen (Cultivation-Liste) → Luft **oder** Eis/Schneeblock → Wasser
  **oder** Bretter/Stamm/Gummiholz 1/15 → Feuer (ersetzt den Block).
- **Flatification** (4000/40): `y = block(y+20)`, Schneeschicht → y−1; `y == yT` → nichts; `y < yT` → Erde auf y+1;
  sonst Block aus der Liste (Schneeschicht, Eis, Gras, Stein, Kies, Sand, Erde, Blätter, Stamm, Pflanzen, Gummiholz/
  -blätter/-setzling) → Luft.
- **Mushroom** (8000/25): `y = solid(y+20)`, `grow(x, y, z, Riesenpilz, abhängig: Pilz)`. Die Nachbarschleifen laufen
  nur über x−1..x und z−1..z (Originaleigenheit), Höhe y+5 bis y−1. Abhängigkeit Myzel: Myzel/Pilzhut → Säule fertig,
  Luft → weiter, Erde/Gras → Myzel + Biom Pilzinsel an (x, z) = Erfolg. Abhängigkeit Pilz: Pilz → Säule fertig, sonst
  jeder feste Block → `grow(Pilz, abhängig: Myzel)` dort. Danach Ziel Pilz: Untergrund Myzel (Pilzhut wird zu Myzel),
  darüber Luft/hohes Gras → brauner oder roter Pilz (1/2). Ziel Riesenpilz: auf y+1 ein Pilz, der als Riesenpilz
  wächst → übrige Pilze im 2×2 auf y+1 entfernen.
- Blaupausen: leer `itemTFBP` (Stapel 64, Textur 144), Varianten Stapel 1 (145–149, Mushroom 161).

---

## Teleporter

- Kein eigener Speicher. Aktiv bei Redstone-Signal und gesetztem Ziel (per Frequenzsender verknüpft, siehe Items).
  Ziel muss ein Teleporter sein (Chunk wird bei Bedarf geladen), sonst wird das Ziel gelöscht.
- Jeden Tick (aktiv): nächste Entity (ohne Reittier-Mitfahrt) im Bereich x−1..x+2, y..y+3, z−1..z+2 wird
  teleportiert, wenn genug Energie in angrenzenden `IEnergyStorage`-Blöcken (BatBox/MFE/MFSU) vorhanden ist.
- Kosten: `(int)(gewicht · (distanz + 10)^0,7 · 5)` EU, gleichmäßig von allen angrenzenden Speichern abgezogen.
- Gewicht (für Entity und alle Reiter darauf):
  - Item-Entity: `100 · Anzahl / MaxStack`; Tier/Minecart/Boot: 100; Spieler: 1000 (+ Inventar `100·n/max` je Slot,
    Config `enableTeleporterInventory=true`); Ghast 2500; Enderdrache 10 000; sonstige Kreaturen 500.
  - Lebewesen zusätzlich: getragene Rüstung/Item je `100·n/max` (Spieler ohne Hand-Item).
- Zielposition: Ziel-x+0,5, y+1,5 (+YOffset), z+0,5. Sound `Machines/Teleporter/TeleUse.ogg`, Partikel.

### Details aus `TileEntityTeleporter`/`ItemFrequencyTransmitter` (nachgetragen für Phase 12.4)

- `TileEntityBlock` ohne Energie und Inventar, `blockMachine2` Meta 0 (Härte 2, Textur `block_machine2.png` Spalte 0,
  Front zum Spieler, Rarity „rare“, Drop fortg. Maschinenblock, Wrench-Rate 1,0). NBT `targetSet`, `targetX/Y/Z`
  (an den Client synchronisiert).
- Tick (Server): Redstone (`isBlockIndirectlyGettingPowered`) **und** Ziel gesetzt → Ziel-Chunk laden; ist dort ein
  Teleporter → aktiv, Entities in der Box (x−1, y, z−1)–(x+2, y+3, z+2), die **auf nichts reiten**, die nächste zu
  (x, y+1, z) (ohne +0,5) wird teleportiert. Kein Teleporter am Ziel → Ziel löschen, inaktiv. Ohne Redstone/Ziel →
  inaktiv. Aktiv = Loop `TeleChargedLoop.ogg` + je Tick 2×2 blaue „reddust“-Partikel über dem Block.
- `teleport`: Distanz = euklidisch zwischen den Blockpositionen; Gewicht 0 → nichts; Kosten
  `(int)(gewicht · (distanz + 10)^0,7 · 5)` ≤ Summe der angrenzenden Speicher → abziehen, Spieler per
  `setPositionAndUpdate`, sonst `setLocationAndAngles` (Blickrichtung bleibt); Ereignis → `TeleUse.ogg` und je 20×2
  Partikel an beiden Teleportern. Erfolg „teleportFarAway“ ab 1000 Blöcken (Erfolge nicht im Umfang).
- Gewicht: Kette Entity → Reiter → dessen Reiter …; Item-Entity `100·n/max`; Tier (`EntityAnimal`), Lore, Boot 100;
  Spieler 1000 + Hauptinventar (36 Slots) je `100·n/max`; Ghast 2500; Enderdrache 10 000; sonstige `EntityCreature`
  500; andere Entities 0 (z. B. Pfeile, XP, Fledermaus, Schleim). Lebewesen zusätzlich Hand-Item (nicht beim
  Spieler) und die vier Rüstungsteile je `100·n/max`.
- Energie: alle sechs Nachbarn, die `IEnergyStorage` sind (BatBox/MFE/MFSU, alle `isTeleporterCompatible`).
  Abzug: Speicher mit Energie > 0 sammeln, je Runde `ceil(rest / anzahl)` von jedem, leere fallen heraus.
- **Frequenzsender** (`itemFreq`, Stapel 1, Textur `item_0.png` Index 95): Rechtsklick auf einen Teleporter: ist das
  gespeicherte Ziel kein Teleporter mehr → vergessen. Kein Ziel → diesen Teleporter speichern („Frequency Transmitter
  linked to Teleporter.“). Gleicher Teleporter → „Can't link Teleporter to itself.“. Teleporter zeigt schon auf das
  Ziel → „Teleportation link unchanged.“. Sonst Teleporter → Ziel verknüpfen, und das Ziel zurück, **falls es noch
  kein Ziel hat**; „Teleportation link established.“. Das Ziel bleibt im Sender gespeichert. Rechtsklick in die Luft
  mit Ziel → vergessen, „Frequency Transmitter unlinked“.

---

## Tesla-Spule

- `maxEnergy = 10 000`, `maxInput = 128`, Energie von allen Seiten.
- Mit Redstone-Signal und `energy ≥ 400`: pro Tick `energy −= 1`; alle 32 Ticks Schock: alle Lebewesen im Würfel
  x−4..x+5 (usw.) **ohne vollständige Hazmat-Rüstung** erhalten `energy / 400` Schaden (DamageSource electricity);
  wurde jemand getroffen → `energy = 0`.

### Details aus `TileEntityTesla` (nachgetragen für Phase 12.4)

- `blockMachine2` Meta 1 (Textur Spalte 1, Drop Maschinenblock, Rarity normal), kein Inventar/GUI. NBT `energy`
  (short). Annahme bis `maxEnergy + maxInput − 1`, Paket > 128 → Explosion.
- Tick (Server), nur mit Redstone und `energy ≥ 400`: `damage = energy / 400` (vor dem Abzug), `energy −1`, dann
  `ticker++ % 32 == 0` → Schock (also gleich beim ersten Tick mit Signal). Box (x−4, y−4, z−4)–(x+5, y+5, z+5);
  geschützt ist nur ein **Spieler** mit vollständigem Hazmat-Anzug (inkl. Gummistiefel). Schadensart `electricity`.
  Die „reddust“-Partikel entstehen im Original auf der Serverwelt und sind daher unsichtbar → entfallen.
- Kein Sound.

---

## Crop-Matron

- 9 Slots: 0–2 Dünger, 3–5 Hydrierzellen, 6–8 Weed-EX. `maxEnergy = 1000`, `maxInput = 32`.
- Bei `energy ≥ 31`: pro Tick 1 Position in 9×3×9 (x/z −4..4, y −1..1) prüfen, `energy −= 1`. Bei Crop:
  Slots nachrücken (2→1→0 usw.), Dünger anwenden (−10 EU, 1 verbraucht), Hydrierzelle anwenden (−10 EU, Zelle wird
  beschädigt), Weed-EX anwenden (−10 EU, 1 Haltbarkeit).
- Sided: unten → 6–8, oben → 3–5, Seiten → 0–2.

**Details aus `TileEntityCropmatron` (nachgetragen für Phase 12.7c):** `blockMachine2` Meta 2 (Sprites Spalte 2: unten,
oben, Seiten gleich; aktiv = inaktiv), Drop Maschinenblock, Wrench-Rate 1,0, NBT nur `energy` (short); Scan-Position
wird nicht gespeichert. Scan: erst `x++` (Start −4, erste Prüfung also x −3), bei x > 4 → x −4 und `z++`, bei z > 4 →
z −4 und `y++` (y −1..1); 243 Ticks je Durchgang. Energie: `demandsEnergy = 1000 − energy`, Annahme bis 1000 (Rest
zurück), Paket > 32 → Explosion; kein Batterie-Slot. Pro Crop zuerst `updateSlots` (je Spalte 1→0, dann 2→1 – ein
Stapel rückt also pro Crop-Besuch nur einen Slot vor), dann je Spalte nur Slot 0/3/6 und nur, wenn die Pflege
greift (Rückgabe `true`): Dünger −1, Hydrierzelle nutzt sich um die zugegebene Wassermenge ab (bis 180), Weed-EX +1
Schaden und weg bei Schaden ≥ 64. Mindestens 31 EU, damit der Tick (bis 1 + 3 × 10 EU) nicht ins Negative geht.
GUI `GUICropmatron.png`: Slots x 62/98/134, y 20/38/56 (nur passende Items), Energiebalken (26, 38) 14 × 14 aus
(176, 0), Titel zentriert; Shift-Klick bevorzugt Slot 0. Automatisierung kann beliebige Items in die Seiten-Slots
legen (sie bleiben liegen).

---

## Luminator

- Zwei Blöcke: `blockLuminatorDark` (aus) und `blockLuminator` (an, Lichtlevel 15). Härte 0,3, Resistenz 0,5.
  Meta = Montagerichtung (an welcher Fläche er klebt).
- Energie nur von **Kabeln** und nur von der Montageseite. `maxEnergy = 10 000`, `maxInput = 32`.
- Verbrauch: 1 EU alle 4 Ticks. Bei Energie > 0 → aktiver Block; bei 0 → dunkler Block.
- Überspannung (> 32): Block entfernt, `ExplosionIC2(power 0,5, drop 0,85, damage 2,0)`.

**Details aus dem Original (nachgetragen für Phase 12.2):** Sprites `block_0.png` 30 (an) / 31 (aus), Material
Redstone-Lampe, Klang Glas, **droppt nichts** (auch beim Abbau). Meta = geklickte Seite; Halt am Block dahinter:
Normalwürfel oder „Sonder-Träger“ (Zaun, Eisenzaun, Kabel, verstärktes Glas, Glas). Fehlt der Halt nach einer
Nachbaränderung, verschwindet er ohne Drop. Form: am Sonder-Träger eine 1-px-Platte über die ganze Fläche, sonst
eine Leuchte (Decke 8×3×8 px, Wand 4×10×2 px; Boden immer volle 1-px-Platte); Kollision = Auswahl = Form.
Energie: nimmt nur von Kabeln an, und ein Kabel verbindet sich/liefert nur, wenn es der Halteblock ist
(`canCableConnectFrom`). `injectEnergy`: dunkel → zuerst hell (frisches Tile mit 0 EU), Puffer bis 10 031 (`max +
maxInput − 1`), Rest zurück. Tick: alle 4 Ticks −1 EU (erster Abzug im ersten Tick), bei ≤ 0 dunkel (frisches Tile);
das dunkle Tile zählt weiter ins Negative (erhöht nur den Bedarf). Rechtsklick mit elektrischem Item: entlädt bis zur
Lücke (Tier 2, ohne Transferlimit), macht hell (auch bei 0 EU). Hell: berührende Monster brennen 20 s (Untote) bzw.
10 s. Rezept 8 Stück `ICI/GTG/GGG`.

---

## Personal-Blöcke (`blockPersonal`, Resistenz 6 000 000)

Besitzer = erster Spieler, der zugreift. Nur der Besitzer kann abbauen (Wrench) und die „offene“ GUI nutzen.

- **Persönlicher Safe**: 54 Slots (wie große Truhe), nur für Besitzer zugänglich.
- **Trade-O-Mat**: Slots 0 Angebot-Muster (Bezahlung), 1 Ware-Muster, 2 Bezahl-Eingang, 3 Ware-Ausgang. Handelt so oft
  wie möglich (Bezahlung vorhanden, Ware in angrenzenden Inventaren mit ≥ 18 Slots bzw. Personal-Safe des Besitzers vorhanden, Platz).
  Bezahlung wird in angrenzende Inventare des Besitzers verschoben. Zählt `totalTradeCount`.
- **Energy-O-Mat**: Verkauft EU. `euOffer` (Standard 1000, per Knöpfen ±100/±1000) pro Bezahl-Muster (Slot 0).
  Eingezahlte Items (Slot 1) gleichen Typs schalten `euOffer / Musteranzahl · eingezahlt` frei (`paidFor`), werden in
  angrenzende Besitzer-Inventare verschoben. Eingang: Frontseite (nimmt nur bezahlte Menge an, unbegrenzte Spannung).
  Ausgabe: übrige Seiten, `min(maxOutputRate, euBuffer)` pro Tick. Puffer 10 000; Upgrade-Slot 2:
  Energiespeicher-Upgrade → `10 000 · (n+1)`, Transformer-Upgrade → Ausgabe `32 · 4^min(4,n)`.

### Details aus `ic2.core.block.personal.*` (nachgetragen für Phase 12.6)

- Block: unzerstörbar (Härte −1), Resistenz 6 000 000, nicht vom Enderdrachen zerstörbar, droppt sich selbst (Meta),
  Front zum Spieler. Texturen `block_personal.png` Spalten 1/2 (Trade-/Energy-O-Mat, Maschinenschema ohne Aktiv);
  der Safe wird nur vom TESR gezeichnet (`newsafe.png`, 64 × 64, fünf Wände + Tür 12 × 14 × 1 an (2, 1, 2), die Tür
  dreht um Y mit `(1 − (1 − l)³) · π/2`; `mirror` wird erst nach `addBox` gesetzt → wirkungslos). Rarity: Safe
  „uncommon“, sonst normal.
- Besitzer: Name des ersten Spielers, der `canAccess` auslöst (Rechtsklick, Schraubenschlüssel); Vergleich ohne
  Groß-/Kleinschreibung. Schraubenschlüssel entfernt nur für den Besitzer; Drehen nur beim Energy-O-Mat (Besitzer,
  andere Seite).
- **Safe** (`TileEntityPersonalChest`, 54 Slots): Rechtsklick nur für Besitzer, Server-Besitzer (Einzelspieler) oder
  Operatoren, sonst Meldung „This safe is owned by …“ und keine GUI. Abbau mit Schraubenschlüssel nur leer („Can't
  wrench non-empty safe“). Keine Automatisierung (Seitengröße 0). Tür: Nutzerzahl per Block-Ereignis (alle 20 Ticks
  und bei Öffnen/Schließen), Winkel ±0,1 je Tick, Truhengeräusche (Öffnen bei 0, Schließen beim Unterschreiten von
  0,5). GUI `GUIPersonalChest.png` 176 × 222: 6 × 9 Slots ab (8, 18), Inventar ab y 140, Hotbar y 198, Titel (8, 6).
- **Trade-O-Mat** (4 Slots): jeden Tick, wenn Muster 0 (Preis) und 1 (Ware) und Bezahlung (Slot 2, gleiches Item,
  Menge ≥ Preis) vorhanden und Ausgabe 3 leer oder gleiche Ware mit Platz für **eine** Ware: Nachbarn in IC2-Reihenfolge
  (−X, +X, −Y, +Y, −Z, +Z), jedes Inventar mit ≥ 18 Slots (Personal-Safe nur bei gleichem Besitzer): Platz für die
  Bezahlung (leere Slots zählen mit der Stapelgröße des Preises), vorhandene Ware → `n = min(Bezahlung/Preis,
  Platz/Preis, Ausgabeplatz/Ware, Vorrat/Ware)`; erster Nachbar mit `n > 0` handelt n-mal, Sound `o-mat.ogg`,
  `totalTradeCount += n`, Vorrat neu zählen. Vergleich `isStackEqual` = gleiches Item (Meta nur bei nicht
  beschädigbaren Items). Seiten: unten → 3, sonst → 2. GUI Besitzer `GUITradeOMatOpen.png`: Slots 0 (24, 17),
  1 (24, 53), 2 (80, 17), 3 (80, 53), Text „Performed“/„Trades:“/Zahl bei (112, 20/28/36). GUI Kunde
  `GUITradeOMatClosed.png`: Muster nur angezeigt (50, 19)/(50, 38), Bezahlung (143, 17), Ware (143, 53), „Want:“
  (12, 23), „Offer:“ (12, 42), „Stock:“ (12, 60) mit Vorrat bei (50, 60), rot bei 0. Shift-Klick Eingabe Slot 2.
- **Energy-O-Mat** (3 Slots: 0 Preis, 1 Bezahlung, 2 Upgrade): je Tick Puffer 10 000 / Ausgabe 32; Upgrade-Slot
  Energiespeicher → `10 000·(n+1)`, sonst Transformer → `32·4^min(4,n)`. Bezahlung gleiches Item wie der Preis →
  `paidFor += euOffer / Preis-Menge · Menge` (Ganzzahl), dann in das erste Nachbarinventar, das **alles** aufnimmt;
  der Slot wird immer geleert (Rest geht verloren). Puffer kappen, `min(Ausgabe, Puffer)` ins Netz (an alle Seiten außer
  der Front). Eingang nur über die Front, `demandsEnergy = min(paidFor, frei)`, keine Überspannung. Knöpfe (Besitzer)
  −1000/−100/+1000/+100 (Angebot bleibt > 0). Seiten → Slot 1. GUI Besitzer `GUIEnergyOMatOpen.png`: Slots 0 (24, 17),
  2 (24, 53), 1 (80, 17), „Offer:“ + Angebot bei (112, 20/28), Knöpfe 28 × 12 bei (112/140, 43/55). Kunde
  `GUIEnergyOMatClosed.png`: Preis angezeigt (50, 19), Bezahlung (143, 17), „Want:“, „Offer:“ + Angebot (50, 42),
  „Paid For: n EU“ (12, 60). Shift-Klick: Containerslot 1 (beim Besitzer der Upgrade-Slot – Originaleigenheit).

Rezepte aller Maschinen: `08-rezepte.md`.
