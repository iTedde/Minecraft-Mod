# 27 – EnderStorage

Quelle: `ftb ultimate/mods/EnderStorage.jar` (ChickenBones, EnderStorage **1.4.1.5** für 1.4.7, braucht CodeChickenCore),
Pack-Config `ftb ultimate/config/EnderStorage.cfg`. Dekompiliert (2026-10-23, Vineflower ohne Remapping, Minecraft-
Namen verschleiert): `reference/decompiled/enderstorage/codechicken/enderstorage/` – `common/BlockEnderStorage`,
`common/TileFrequencyOwner`, `common/ItemEnderStorage`, `common/EnderStorageRecipe`, `api/EnderStorageManager`,
`storage/item/*` (Truhe, Beutel, GUI), `storage/liquid/*` (Tank). Texturen/Modelle im Jar unter
`codechicken/enderstorage/gfx/` (`enderchest.png`, `buttons.png`, `enderpouch.png`, `endertank.obj`,
`endertanktex.png`, `hedronmap.png`).

Nutzerwunsch (2026-10-23): „Enderchest müssen noch mit verschiedenen Farben markiert werden können, so dass man mehrere
verschiedene nutzen kann.“ Entscheidungen (Nutzer, 2026-10-23): **Umfang komplett wie im Pack** (Ender Chest, Ender
Pouch, Ender Tank); **die Vanilla-Endertruhe bleibt nutzbar** (Abweichung von `disable-vanilla=true`). Paket
`dev.craft.enderstorage`.

## 1 Frequenz und Besitzer

- Jede Truhe, jeder Tank und jeder Beutel hat eine **Frequenz** aus drei Farben (Wollfarben 0 = weiß … 15 = schwarz):
  `freq = c1 << 8 | c2 << 4 | c3` (0–4095, `EnderStorageManager.getFreqFromColours`). Farbe 1 = links, 2 = Mitte,
  3 = rechts.
- **Besitzer** `"global"` (Standard) oder ein Spielername (persönlich). Ein Speicher ist eindeutig durch
  `(freq, owner, typ)` mit Typ `item` oder `liquid`; Truhen/Beutel und Tanks gleicher Farbe teilen also **nicht**
  denselben Speicher, persönliche und globale gleicher Farbe ebenfalls nicht.
- Farbstoff → Farbe: `farbe = ~farbstoff & 15` (Farbstoff-Metadaten 0 = schwarz … 15 = weiß, also wie die Wolle).
  1.21: Farbstoff-Item → `DyeColor.getId()` ist bereits die Wollnummer.
- Speicher werden weltweit (alle Dimensionen gemeinsam) gespeichert, im Original unter `<Welt>/EnderStorage/`
  (`data1.dat`/`data2.dat` im Wechsel, `lock.dat`), Schlüssel `"<freq>|<owner>|<typ>"`. 1.21: `SavedData` der
  Oberwelt (Abweichung, s. §7).

## 2 Block (`BlockEnderStorage`, `ItemEnderStorage`)

Im Original **ein** Block (ID 1200) mit zwei Block-Entities nach Metadaten: 0 = Ender Chest, 1 = Ender Tank. Das
Item trägt `damage = freq | typ << 12` (Truhe 0–4095, Tank 4096–8191) und NBT `owner`, wenn nicht global. 1.21: zwei
Blöcke `craft:ender_storage_chest` und `craft:ender_storage_tank`; Frequenz und Besitzer als Data Component am Item.

- Material Stein, Härte **20**, Explosionswiderstand 100 (1.4.7, intern ×3/5 → 1.21-Wert **60**), Steingeräusche,
  kein normales Licht-/Sichtblockieren (`c()`/`b()` false, eigener Renderer), Kollisionsbox voller Block.
- Drop: immer **sich selbst mit Frequenz und Besitzer** (nicht im Kreativmodus), auch ohne Werkzeug
  (`getBlockDropped`), Inhalt bleibt im Ender-Speicher. Pick-Block ebenso.
- Platzieren: übernimmt Frequenz und Besitzer vom Item; Drehung = Blickrichtung des Spielers
  (`floor(yaw·4/360 + 2.5) & 3`); mit Schraubenschlüssel (`IReconfigurableFacing.rotateBlock`) weiterdrehen.
- Tooltip des Items: Besitzername, wenn persönlich.
- Kreativtab: Truhe und Tank je mit Frequenz 0 (weiß/weiß/weiß), global.
- Abbaugeschwindigkeit: nicht abbaubar, wenn `invincible()` – bei beiden Typen `false`.
- Redstone-Verbindung: nur der Tank (`redstoneInteraction`).

### 2.1 Klicks auf den Block (Ray-Trace auf Teilflächen)

Ein Rechtsklick wird auf Teil-Hitboxen ausgewertet (`addTraceableCuboids`, `subHit`):

| subHit | Truhe | Tank |
|---|---|---|
| 0 | Korpus → GUI öffnen | Korpus → Behälter füllen/leeren |
| 1–3 | Farbknopf 1–3 auf dem Deckel (nur bei geschlossenem Deckel) | Farbknopf 1–3 oben |
| 4 | Schloss vorne (nur bei geschlossenem Deckel) | Druckventil-Knauf vorne |

- **Farbknopf + Farbstoff in der Hand:** diese Farbe ändern; ist sie schon gleich, passiert nichts (`return false`).
  Verbraucht 1 Farbstoff (nicht im Kreativmodus). Frequenzwechsel lädt den Speicher neu.
- **Schloss/Knauf (subHit 4):**
  - Schleichen + persönlich: wird **global**; der Spieler bekommt einen **Diamanten** ins Inventar (nicht im
    Kreativmodus; passt er nicht hinein, passiert nichts). **Originalverhalten: es wird nicht geprüft, wer
    schleicht** – jeder kann eine persönliche Truhe so entsperren (1:1 übernommen, Grundregel 1).
  - **Diamant** (Pack-Config `personalItemID=264`) in der Hand + global: wird persönlich für diesen Spieler,
    Diamant wird verbraucht (nicht im Kreativmodus).
  - Beim Tank ist subHit 4 der Druckknauf (§5); das Persönlich-Machen läuft dort über dieselbe Abfrage **vor**
    `activate`, also: Schleichen/Diamant auf dem Knauf wie bei der Truhe, sonst Knauf umschalten.
- Sonst `tile.activate(player, subHit)`.

## 3 Ender Chest (`TileEnderChest`, `EnderItemStorage`)

- Speicher-Typ `item`, Größe nach Config `storage-size=1` → **27 Slots** (3 × 9; 0 = 9, 2 = 54 möglich, im Pack 27).
  Stapelgröße 64; jeder Spieler darf benutzen (`isUseableByPlayer` true).
- Rechtsklick auf den Korpus öffnet die GUI „Ender Chest“. GUI wie die Vanilla-Truhe 3 × 9
  (`/gui/container.png`, oberer Teil 71 px + unterer Teil ab y 126), Slots (8 + 18·x, 18 + 18·y), Spielerinventar
  ab y 85 (Hotbar +58). Titel links „Ender Chest“, bei persönlichen Truhen rechts der Besitzername
  (`170 - Breite`, y 6). Shift-Klick: Truhe ↔ Spielerinventar wie Vanilla.
- **Deckel:** öffnet sich, solange mindestens ein Spieler **irgendeine** Truhe/einen Beutel dieses Speichers offen
  hat (`openCount` am Speicher, nicht am Block) – alle Truhen derselben Frequenz öffnen sich gemeinsam. Server
  schickt die Zahl alle 20 Ticks bzw. bei Änderung per Block-Event. Deckelwinkel nähert sich linear um 0,1/Tick;
  Geräusche `random.chestopen`/`random.chestclosed` (0,5; Tonhöhe 0,9–1,0) beim Öffnen/Schließen.
- Automatisierung: `IInventory` mit allen 27 Slots, jede Seite → Item-Capability (Trichter, Rohre).
- Modell: eigene Truhe (`ModelEnderChest`, Textur `enderchest.png`), drei Farbknöpfe auf dem Deckel in den
  Wollfarben (`buttons.png`), Schloss vorne; persönlich = Diamant-Schloss. Lichtwert 0.

## 4 Ender Pouch (`ItemEnderPouch`)

- Item `craft:ender_pouch`, Stapelgröße 1, Frequenz/Besitzer wie die Truhe. Bit 8192 im Damage = persönlich (nur für
  die Darstellung).
- Rechtsklick (nicht schleichend) in die Luft oder auf Blöcke: öffnet den **Item-Speicher** seiner Frequenz und
  seines Besitzers, GUI-Titel „Ender Pouch“ (zählt als geöffnet → Truhendeckel gehen auf).
- **Schleichen + Rechtsklick auf eine Ender Chest:** übernimmt deren Frequenz und Besitzer (nur von Truhen, nicht
  vom Tank).
- Kein Umfärbe-Rezept für den Beutel (das Rezept in §6.2 nimmt nur Truhe/Tank).
- Tooltip: Besitzername, wenn persönlich. Darstellung: 4 Ebenen – Grundbeutel (normal/persönlich) und drei
  Farbbänder in den Frequenzfarben (`enderpouch.png`, Zeile 1–3 je 16 Farben).

## 5 Ender Tank (`TileEnderTank`, `EnderLiquidStorage`)

- Speicher-Typ `liquid`: **ein Tank, 16 Eimer = 16 000 mB**, beliebige Flüssigkeit; Füllen/Entleeren von jeder Seite.
- Rechtsklick auf den Korpus: Flüssigkeitsbehälter (Eimer, Zellen …) in den Tank leeren, sonst leeren Behälter aus dem
  Tank füllen (`LiquidUtils.fillTankWithContainer` / `emptyTankIntoContainer`).
- **Druckventil:** Zustand `pressure = Redstone-Signal am Tank XOR invert`. Bei Druck gibt der Tank jeden Tick an
  **jede** der sechs Nachbarseiten mit Flüssigkeits-Container bis zu **100 mB** ab (Simulation, dann tatsächliche
  Menge). Klick auf den Knauf (subHit 4) schaltet `invert` um (NBT `ir`). Der Knauf dreht sich sichtbar
  (−90°/+90°).
- Lichtwert = Lichtwert des Flüssigkeitsblocks, wenn der Tank etwas enthält (z. B. Lava 15).
- NBT des Block-Entitys: `freq`, `owner`, `rot`, `ir`.
- Modell: `endertank.obj` mit `endertanktex.png` (Glaskörper, Flüssigkeit sichtbar, drei Farbknöpfe oben, Knauf
  vorne); `hedronmap.png` = Textur des schwebenden Ender-Kerns im Tank.
- `TankSynchroniser`: hält die Füllanzeige der Tank-Blöcke und der Tank-Items in Spielerhand beim Client aktuell
  (nur Darstellung).

## 6 Rezepte (`EnderStorageRecipe`)

### 6.1 Grundrezepte (je Wollfarbe i = 0–15, ergibt Frequenz i/i/i, global)

| Ergebnis | Muster | Zutaten |
|---|---|---|
| Ender Chest | `bWb` / `OCO` / `bpb` | b = Lohenrute, W = Wolle i, O = Obsidian, C = **Truhe**, p = Enderperle |
| Ender Tank | `bWb` / `OCO` / `bpb` | wie oben, C = **Kessel** |
| Ender Pouch | `blb` / `lpl` / `bWb` | b = Lohenstaub, l = Leder, p = Enderperle, W = Wolle i |

### 6.2 Umfärben (eigene Rezeptklasse, formlos innerhalb des 3×3-Rasters)

- Ender Chest oder Ender Tank in Spalte 2 der Zeile 2 **oder** 3; in der Zeile direkt darüber mindestens ein Farbstoff
  (Spalte 1, 2 oder 3). Jeder Farbstoff setzt die Farbe seiner Spalte (`~dye & 15`), leere Spalten behalten die alte
  Farbe. Ergebnis: 1 Stück, Typ, Besitzer und NBT bleiben (`copyStack`). Andere Felder werden nicht geprüft (Original:
  keine Prüfung auf leere Restfelder).
- Farbstoffe: Vanilla-Farbstoffe (`dyes`) plus Ore-Dictionary `dye<Farbe>`. 1.21: Tag `c:dyes/<farbe>`.

## 7 Abweichungen 1.21 (→ `abweichungen.md`)

| Punkt | Original | 1.21 |
|---|---|---|
| Vanilla-Endertruhe | `disable-vanilla=true`: nicht platzierbar | **bleibt** nutzbar (Nutzer, 2026-10-23); EnderStorage-Truhe ist ein eigener Block |
| Block/IDs | ein Block mit Metadaten, Frequenz im Item-Damage | zwei Blöcke + Data Component `craft:frequency` (freq, owner) |
| Besitzer | Spielername (String) | Spieler-**UUID** als Schlüssel, Name nur zur Anzeige (Namensänderungen verlieren sonst den Speicher) |
| Speicherort | eigene Dateien `<Welt>/EnderStorage/` | `SavedData` der Oberwelt |
| Ray-Trace-Teilflächen | CodeChickenCore `RayTracer` | eigene Teil-Hitboxen in `useItemOn`/`useWithoutItem` (Treffpunkt im Block gegen die Knopf-/Schloss-Quader) |
| Farbstoff-Ore-Dict | `dyeRed` … | Tags `c:dyes/*` |
| Flüssigkeit | Forge `LiquidStack` | `FluidStack`, Fluid-Capability (jede Seite) |
| Tank-Modell | eigenes OBJ-Renderer | NeoForge-OBJ-Modell bzw. Block-Entity-Renderer mit derselben Datei |
| Namen | „Ender Chest“ wie Vanilla | deutsch „Ender-Truhe (farbig)“, Farb-Tooltip (§9) |

## 8 Tests (GameTest)

- Zwei Truhen gleicher Frequenz teilen den Inhalt; andere Frequenz/Typ/Besitzer nicht.
- Farbstoff auf Knopf 1–3 ändert genau die Farbe, verbraucht den Farbstoff; gleiche Farbe → nichts.
- Diamant auf Schloss → persönlich, Schleichen auf Schloss → global + Diamant zurück.
- Abbau droppt die Truhe mit Frequenz/Besitzer; Inhalt bleibt im Speicher.
- Umfärbe-Rezept (einzelne Spalte, alle drei Spalten, Zeile 2 und 3), Grundrezepte.
- Beutel: Schleichklick übernimmt Frequenz, Öffnen zeigt denselben Inhalt, Deckel der Truhen geht auf.
- Tank: 16 000 mB, Füllen/Leeren per Eimer, Druck mit Redstone gibt 100 mB/Tick an Nachbartanks ab, Knauf invertiert.
- Trichter/Rohr an Truhe und Tank.

## 9 Entscheidungen (Nutzer, 2026-10-23)

1. **Namen:** englisch „Ender Chest“, deutsch „Ender-Truhe (farbig)“; der Tooltip zeigt die drei Farben
   („Farben: Weiß / Rot / Blau“) und bei persönlichen den Besitzer.
2. **Persönlich entsperren ohne Besitzerprüfung** (§2.1): 1:1 wie im Original.

## 10 Teilpakete (Phase 26)

| Teil | Inhalt |
|---|---|
| 26.0 | diese Spezifikation |
| 26.1 | Frequenz-Grundgerüst (Data Component, `SavedData`), Ender Chest: Block, Block-Entity, Speicher, GUI, Farbknöpfe/Schloss, Deckel, Renderer, Grund- und Umfärberezepte, Capability, GameTests |
| 26.2 | Ender Pouch (Item, Darstellung, Frequenz übernehmen), GameTests |
| 26.3 | Ender Tank (Block, Speicher, Druckventil, Behälter, OBJ-Modell/Renderer, Lichtwert), GameTests |
| 26.4 | EMI (Umfärbe-Rezept), Herkunft, Wiki, Client-Test, Paket |
