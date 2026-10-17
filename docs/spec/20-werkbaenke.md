# 20 – Projekttisch und Automatische Werkbank (Phase 18)

Nutzerwunsch 2026-10-17: „Es fehlen noch sowas wie Project Table und Automatic Crafting Table.“ Entscheidungen
(2026-10-17): Project Table = **RedPower 2** (`redpowercore.zip`, Paket `base`), Automatic Crafting Table =
**BuildCraft-Autocrafting-Table** (`TileAutoWorkbench`); erst Spezifikation, dann Umsetzung; weitere fehlende
Werkbank-Blöcke als Liste vorlegen (§5).

Quellen: `reference/decompiled/redpower/com/eloraam/redpower/` (`RedPowerBase`, `base/TileAdvBench`,
`base/ContainerAdvBench`, `base/SlotCraftRefill`, `base/InventorySubCraft`, `base/GuiAdvBench`, `base/ItemPlan`,
`base/BlockAppliance`, `base/TileAppliance`, `core/CoreLib`; nachdekompiliert 2026-10-17, ohne Remapping) und
`reference/decompiled/buildcraft/buildcraft/factory/` (`TileAutoWorkbench`, `BlockAutoWorkbench`,
`gui/ContainerAutoWorkbench`, `gui/GuiAutoCrafting`), `core/inventory/TransactorRoundRobin`.

## 1. Projekttisch (RedPower `TileAdvBench`)

### 1.1 Block

| Eigenschaft | 1.4.7 | Umsetzung 1.21 |
|---|---|---|
| Name | „Project Table“ (`rpabench`, `blockAppliance` Meta 3) | `craft:project_table`, „Project Table“ / „Projekttisch“ |
| Material / Härte | Stein (`agi.e`), 2,0 | Stein, 2,0; `mineable/pickaxe` + `requiresCorrectToolForDrops` (Stein ohne Spitzhacke droppt in 1.4.7 nichts) |
| Ausrichtung | `Rotation` = Blickrichtung des Spielers beim Setzen (4 Richtungen) | `HORIZONTAL_FACING` (Vorderseite zum Spieler) |
| Texturen | `eloraam/base/base1.png`: unten 35, oben 32, Vorderseite 34, übrige Seiten 33 | Original-Kacheln (Zeile 2, Spalten 0–3) |
| Kein Tick | `canUpdate() = false` | kein Ticker |
| Rechtsklick | schleichend → nichts (Block setzen); sonst GUI | gleich |
| Tab | `tabMachine` | Tab „Craft: RedPower“ |

Rezept (`RedPowerBase` 354, Ore-Rezept, spiegelbar): `SSS/WTW/WCW`, S = Stein (`amq.w`, nur Stein, kein
Bruchstein), W = `plankWood` (`#minecraft:planks`), T = Werkbank, C = Truhe.

### 1.2 Inventar (28 Slots)

| Slots | Inhalt |
|---|---|
| 0–8 | Werkbankraster 3×3 (bleibt beim Schließen der GUI im Tisch) |
| 9 | Bauplan-Slot (nur leerer oder beschriebener Bauplan, Stapelgröße 1) |
| 10–27 | Vorrat, 18 Slots (2 Reihen à 9) |

- Automatisierung (`ISidedInventory`): **jede Seite** greift nur auf den Vorrat 10–27 zu (`getStartInventorySide 10`,
  `getSizeInventorySide 18`). 1.21: Item-Capability auf allen Seiten = Slots 10–27.
- Abbau (`onBlockRemoval`): droppt die Slots **0–26** (`i < 27`). Slot 27 (letzter Vorratsslot) wird **nicht**
  gedroppt – Originalfehler, Inhalt geht verloren. **Umsetzung: alle 28 Slots droppen** (§4, F2).
- NBT `Items` (Slot-Byte) + `rot`.

### 1.3 Handwerk mit Vorrat (`ContainerAdvBench`, `SlotCraftRefill`)

Ergebnis: Vanilla-Werkbankrezept (`CraftingManager`) über ein **Schattenraster**: für jeden Rasterslot der
Raster-Inhalt; ist der Slot leer und der Bauplan im Slot 9 sieht dort eine Zutat vor, steht im Schattenraster der
erste passende Vorratsstapel (`findMatch`, Slots 10–27 in Reihenfolge). Ein Ergebnis gibt es nur, wenn die
**Erfüllungsmaske** 511 (alle 9 Bits) ist:

- Bit gesetzt, wenn der Rasterslot belegt ist oder der Bauplan dort nichts verlangt.
- Für die übrigen (leer, vom Plan verlangt) werden die Vorratsstapel der Reihe nach verteilt: jeder Stapel deckt bis zu
  `Anzahl` noch offene Plan-Slots, deren Plan-Zutat zu ihm passt.

Passen (`CoreLib.matchItemStackOre`): beide Items haben dieselbe Ore-Dictionary-Klasse **oder**
`compareItemStack == 0` (gleiche ID; Schaden gleich, bei Items ohne Untertypen beliebig; **NBT wird nicht
verglichen**). → §4, F3.

Ergebnis nehmen (`SlotCraftRefill.onPickupFromSlot`), in dieser Reihenfolge:

1. Merkt sich den Rasterinhalt `cur` und `last = isLastUse()`.
2. Für jeden **leeren** Rasterslot mit Plan-Zutat: 1 Item vom ersten passenden Vorratsstapel abziehen; hat es ein
   Container-Item (Eimer), wird dieser Vorratsslot durch das Container-Item **ersetzt**.
3. Vanilla-Abnahme (1 je belegtem Rasterslot, Container-Items bleiben im Raster).
4. Ist es **nicht** die letzte Nutzung: für jeden vorher belegten Rasterslot **ohne** Plan-Zutat:
   - jetzt leer → 1 Item vom ersten passenden Vorratsstapel nachlegen;
   - jetzt ein anderes Item, das das Container-Item der alten Zutat ist (Wassereimer → Eimer) → mit dem ersten
     passenden Vorratsstapel **tauschen** (voller Eimer ins Raster, leerer in den Vorrat).
5. Ergebnis neu berechnen.

`isLastUse`: Bit gesetzt je Rasterslot, der leer, nicht stapelbar oder mit > 1 Item belegt ist; die übrigen
(genau 1 stapelbares Item) müssen über Vorratsstapel (gleiche Verteilung wie oben, Abgleich mit dem Rasterinhalt)
gedeckt sein, sonst „letzte Nutzung“.

Wirkung: der Spieler legt das Rezept mit **je einem Item** ins Raster, die Nachschubware in den Vorrat – das Raster
füllt sich nach jedem Handwerk selbst nach. Mit Bauplan bleibt das Raster leer und alles kommt aus dem Vorrat.

### 1.4 Shift-Klick (`transferStackInSlot`; Container-Indizes: 0–8 Raster, 9 Plan, 10 Ergebnis, 11–28 Vorrat, 29–55 Spielerinventar, 56–64 Hotbar)

| Quelle | Ziel |
|---|---|
| Ergebnis | `mergeCrafting`: wiederholt herstellen und ins **Spielerinventar** (29–64) legen, solange es passt, höchstens bis zur Stapelgröße des Ergebnisses (Stapelgröße 1 → **16 Stück**), Abbruch bei letzter Nutzung oder anderem Ergebnis |
| Raster 0–8 | Vorrat |
| Plan, Vorrat | Spielerinventar (von hinten, Hotbar zuerst) |
| Spielerinventar | Vorrat |

### 1.5 Baupläne (`ItemPlan`, `itemPlanBlank`)

| Item | ID | Name | Eigenschaften |
|---|---|---|---|
| Leerer Bauplan | `craft:blank_plan` | „Blank Plan“ / „Leerer Bauplan“ | Stapel 64, Icon `items1.png` 81, Tab Materialien |
| Bauplan | `craft:plan` | „<Ergebnis> Plan“ / „Bauplan: <Ergebnis>“ | Stapel 1, Icon 82, Seltenheit **selten** (Name cyan); ohne Daten „Plan“ |

- Rezept leerer Bauplan (formlos): Papier + Indigofarbstoff (`itemDyeIndigo`, RedPower World). RedPower registriert
  Indigo als `dyeBlue` (vgl. eigenes Lumar-Rezept mit `"dyeBlue"`) → **Papier + `#c:dyes/blue`** (→ §4, F4).
- Beschreiben: Leerer Bauplan in Slot 9 **und** ein Ergebnis vorhanden → in der GUI leuchtet die Schaltfläche bei
  (18, 55), 14×14 (Overlay aus `advbench.png` bei 176, 0). Klick → Server: Slot 9 wird ein Bauplan mit
  - `result` = aktuelles Ergebnis (ganzer Stapel inkl. Anzahl),
  - `requires` = für jeden belegten Rasterslot eine Kopie mit Anzahl 1 + Slotnummer.
  1.21: Data Component `craft:plan` (Ergebnis-Stack + Liste Slot→Item), Payload für den Klick.
- Tooltip: je verschiedener Zutat „`<n>` x `<Name>`“ (gezählt nach Item + Schaden).
- In der GUI werden die Plan-Zutaten leerer Rasterslots als Geister-Items gezeichnet: weiß 50 % deckend, wenn das Bit
  der Erfüllungsmaske gesetzt ist, sonst **rot** (1,0/0,1/0,1, 60 %).
- Ein Bauplan wird nie verbraucht; ein beschriebener Bauplan lässt sich nicht löschen/überschreiben (kein Rezept, kein
  Knopf – nur leere Pläne werden beschrieben).

### 1.6 GUI

`eloraam/base/advbench.png` (176 × 222): Titel „Project Table“ (60, 6), „Inventory“ (8, Höhe − 94). Raster bei
(48 + 18·x, 18 + 18·y), Plan (17, 36), Ergebnis (143, 36), Vorrat (8 + 18·x, 90 / 108), Spielerinventar ab y = 140,
Hotbar y = 198. Reichweite 8 Blöcke.

## 2. Automatische Werkbank (BuildCraft `TileAutoWorkbench`)

### 2.1 Block

| Eigenschaft | 1.4.7 | Umsetzung 1.21 |
|---|---|---|
| Name | „Automatic Crafting Table“ (`autoWorkbenchBlock`; Lang-Datei „Autocrafting Table“, `addName` überschreibt) | `craft:auto_workbench`, „Automatic Crafting Table“ / „Automatische Werkbank“ |
| Material / Härte | Holz (`agi.d`), 1,0 | Holz, 1,0, `mineable/axe` |
| Texturen | BC `blocks.png`: oben/unten 43, Seiten 44 | Original-Kacheln |
| Rechtsklick | schleichend → nichts; mit Rohr in der Hand → nichts (Rohr setzen); sonst GUI | gleich |
| Abbau | `Utils.preDestroyBlock` droppt das Raster | gleich |
| Tab | BuildCraft | Tab „Craft: BuildCraft“ |

Rezept (`BuildCraftFactory` 178, spiegelbar): ` g /gwg/ g `, w = Werkbank, g = Holzzahnrad.

### 2.2 Inventar und Herstellung

- 9 Slots = Werkbankraster (Stapelgröße 64), NBT `stackList`. Das Raster ist zugleich Rezept und Puffer.
- **Kein Strom, kein Tick, keine eigene Ausgabe.** Hergestellt wird nur, wenn etwas das Ergebnis herauszieht:
  - **Holzrohr/Smaragdrohr** (`ISpecialInventory.extractItem`, `removeRecipe = false`): je Slot wird **1 Item als
    Muster liegen gelassen**. Slot mit > 1 Item → 1 Item daraus. Slot mit genau 1 Item → 1 passendes Item aus einem
    **benachbarten Inventar** (Reihenfolge West, Ost, unten, oben, Nord, Süd; erster passender Slot; Nachbarn, die selbst
    `ISpecialInventory` sind – Automatische Werkbänke, BC-Maschinen –, werden übersprungen; Doppeltruhen ganz).
    Passend = gleiche ID und gleicher Schaden, bei beschädigbaren Items beliebiger Schaden; NBT egal. Fehlt eine
    Zutat oder gibt es kein Rezept, wird alles zurückgelegt und nichts entnommen.
  - Ergebnis = **ein Handwerksvorgang** (z. B. 4 Bretter) unabhängig von der MJ-Menge; das Holzrohr verbraucht danach
    MJ in Höhe der Stückzahl (`useEnergy(n, n)`), auch wenn weniger gespeichert war.
  - Container-Items (Eimer): der Slot, aus dem die Zutat kam (Raster oder Nachbarinventar), erhält das Container-Item;
    geht ein beschädigbares Container-Item dabei kaputt (Schaden ≥ Haltbarkeit), verschwindet es mit Bruchgeräusch.
- **GUI-Ergebnis** (`SlotAutoCrafting`): zeigt `findRecipe()` des Rasters. Abnehmen → `extractItem(true, true)`: dabei
  wird **alles** aus dem eigenen Raster genommen (kein Muster bleibt, keine Nachbarinventare). Shift-Klick legt ein
  Ergebnis ins Spielerinventar (kein Mehrfach-Handwerk). Erfolge/`onCrafting` wie an der Werkbank.
- **Einfügen** (Rohre, `TransactorRoundRobin`): ein Item nach dem anderen in den **belegten** Rasterslot mit gleicher
  ID/Schaden/NBT und der **kleinsten** Anzahl (Gleichstand: erster; ein Slot mit 1 Item wird sofort genommen); leere
  Slots werden nie befüllt, das Rezept bleibt so erhalten. Passt nichts, geht das Item weiter (Rohr: anderes Ziel/
  Rückprall).
- Original-Eigenheit: die Simulation (`doAdd = false`) meldet jedes Item als eingefügt, sobald irgendein Slot noch Platz
  für eins hat (Zählung ändert sich in der Schleife nicht) → §4, F5.

### 2.3 GUI

Vanilla-Werkbank-Hintergrund (`/gui/crafting.png`, 1.21 `textures/gui/container/crafting_table.png`), Titel zentriert
„Automatic Crafting Table“, „Inventory“. Raster (30 + 18·x, 17 + 18·y), Ergebnis (124, 35). Shift-Klick:
Ergebnis → Spielerinventar (von hinten); Inventar ↔ Hotbar (Raster ist kein Shift-Ziel; Raster → Spielerinventar).

## 3. Umsetzung 1.21 (Vorschlag)

| Teil | Umsetzung |
|---|---|
| Projekttisch | `redpower/ProjectTableBlock` + Block-Entity (`ItemStackHandler` 28), Menü mit eigenem `CraftingContainer`-Wrapper aufs Raster, Schattenraster als `CraftingInput` für `RecipeManager`; Plan-Payload `WritePlanPayload` |
| Baupläne | Data Component `craft:plan` (`ItemStack result`, `List<Slot+ItemStack> requires`), Name über `getName` |
| Automatische Werkbank | `buildcraft/factory/AutoWorkbenchBlock` + Block-Entity (9 Slots); Rohr-Entnahme über eine eigene Schnittstelle `CraftingExtractable` (wie `ISpecialInventory`), die Holz-/Smaragdrohr vor der Item-Capability abfragen |
| Item-Capability Werkbank | nur **Einfügen** (Round-Robin), Entnahme 0 – Trichter u. ä. ziehen nichts heraus (in 1.4.7 gab es keine Trichter) |
| Nachbarinventare | Item-Capability des Nachbarn von der Seite, die zur Werkbank zeigt |
| EMI | keine eigene Kategorie (Vanilla-Werkbank); EMI-„Füllen“ für beide Rasters (Recipe Handler) – optional |
| Tests | GameTests: Projekttisch-Nachschub aus dem Vorrat, Bauplan-Handwerk ohne Raster, Bauplan beschreiben; Werkbank: Holzrohr zieht 4 Bretter und lässt das Muster liegen, Zutat aus Nachbartruhe, Round-Robin-Einfügen |

## 4. Abweichungen / Punkte zur Freigabe

Entscheidung Nutzer 2026-10-18: F2 – Fehler muss nicht bleiben (alle Slots droppen); übrige Punkte wie vorgeschlagen.
Zusätzlich in 18.1: Container-Items aus einem Vorratsstapel überschreiben den Rest des Stapels nicht mehr
(`abweichungen.md`).

| Nr. | Thema | Original | Vorschlag |
|---|---|---|---|
| F1 | Ore-Dictionary | `matchItemStackOre`: gleiche Ore-Klasse (genau ein Name je Item, z. B. `plankWood`, `ingotCopper`) | gleiche Item-ID **oder** beide im selben „Material-Tag“: `#minecraft:planks`, `#minecraft:logs`, `#minecraft:wool` sowie die `c:`-Untertags `ingots/…`, `dusts/…`, `gems/…`, `plates/…`, `nuggets/…`, `ores/…`, `dyes/…` (gleiches Unter-Tag, z. B. beide `c:ingots/copper`) |
| F2 | Projekttisch-Abbau | letzter Vorratsslot (27) wird nicht gedroppt (Fehler) | **1:1 übernehmen** (Grundregel 1) oder alle 28 Slots droppen – Nutzerentscheidung |
| F3 | NBT/Komponenten beim Abgleich | NBT egal (Projekttisch und Werkbank) | Data Components egal (Item + ggf. Schaden), wie im Original |
| F4 | Indigofarbstoff | RedPower-World-Indigo (Blume) | `#c:dyes/blue` (RedPower registriert Indigo als `dyeBlue`) |
| F5 | Simulation Round-Robin | Simulation meldet zu viel | 1.21-Capability simuliert genau (Rohre sehen sonst Items verschwinden) |
| F6 | Rohr-Entnahme Werkbank | `ISpecialInventory`, Ergebnisstack beliebiger Größe | eigene Schnittstelle (s. §3); die Item-Capability erlaubt keine Rückgabe größer als angefragt |
| F7 | Trichter | – (gab es nicht) | können einfügen, nicht entnehmen |

## 5. Weitere Werkbank-/Handwerksblöcke im Pack (Liste für den Nutzer)

Gefunden in `ftb ultimate/mods/` (Klassen mit Workbench/Crafter/Bench/Assembler). **Nicht** im Craft-Umfang:

| Block | Mod | Kurzbeschreibung | Bezug zum Craft-Umfang |
|---|---|---|---|
| Electric Auto Workbench | GregTech | elektrischer Autocrafter (EU) | GT ist teilweise drin |
| Assembly Table + Advanced Crafting Table | BuildCraft Silicon | Laser-betriebene Montage (Chipsets, Gates) bzw. Werkbank mit Laser-Energie | BC drin, aber ohne Laser/Gates (Phase 10) |
| Charging Bench | RedPower Machine / Thermal Expansion | lädt Werkzeuge (Bluetricity bzw. MJ), kein Handwerk | – |
| Auto Crafter | Thermal Expansion | MJ-Autocrafter mit Puffer | TE nicht im Umfang |
| Auto Crafting Table Mk II, Logic Crafter | Tubestuff (immibis) | Redstone-/Rohr-gesteuerte Autocrafter | nicht im Umfang |
| Molecular Assembler, Crafting Terminal | Applied Energistics | ME-Autocrafting | nicht im Umfang |
| Arcane/Infusion Workbench | Thaumcraft | Magie-Handwerk | nicht im Umfang |
| Uncrafting Table | Twilight Forest | zerlegt Items | nicht im Umfang |
| Cart Assembler | Steve's Carts | Loren bauen | nicht im Umfang |
| LiquiCrafter | MineFactory Reloaded | Autocrafter mit Flüssigkeiten | nicht im Umfang |
| Rolling Machine | Railcraft | Metallrezepte (GT-Fallback ist bereits Werkbank, `abweichungen.md`) | nicht im Umfang |
| Crafty Turtle | ComputerCraft | – | **schon vorhanden** über CC: Tweaked (Phase 15.3) |

## 6. Teilphasen

- 18.0 Spezifikation (dieses Dokument) und Liste §5
- 18.1 Projekttisch + Baupläne
- 18.2 Automatische Werkbank (inkl. Rohr-Schnittstelle)
