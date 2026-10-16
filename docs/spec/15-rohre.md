# 15 – BuildCraft-Rohre (Phase 10)

Quelle: BuildCraft 3.4.3, `reference/decompiled/buildcraft/buildcraft/transport/` (`BuildCraftTransport`, `Pipe`,
`TileGenericPipe`, `BlockGenericPipe`, `ItemPipe`, `PipeTransportItems/Liquids/Power/Structure`, `pipes/*`).
Pack-Config `ftb ultimate/config/buildcraft/main.conf`. GregTech ändert keine Rohrrezepte.

**Umfang (Nutzerentscheidung 2026-10-16):** nur die Rohre – 21 Rohrtypen + Rohrdichtmittel. **Nicht** im Umfang:
Gates (normal/autark), Rohrdrähte, Fassaden, Laser, Assembly Table (BuildCraft Silicon), Stripes-Rohr (gibt es in
3.4.3 nur als Logikklasse, kein Item). Alles, was im Original nur über Gates/Drähte wirkt (Signale, Trigger, Aktionen,
`broadcastRedstone`, Redstone-Ausgabe, Gate-GUI, Strippen mit Shift-Rechtsklick), entfällt damit.

## 1. Pack-Konfiguration

| Schlüssel | Wert | Wirkung |
|---|---|---|
| `pipes.alwaysConnect` | `false` | Material-Verbindungsregeln (§3.2) aktiv |
| `pipes.durability` | `0.25` | Härte des Rohrblocks |
| `pipes.groupItemsTrigger` | `32` | ab > 32 Items in einem Rohr werden gleiche Items zusammengefasst |
| `pipes.maxItems` | `100` | ab > 100 Items (nach dem Zusammenfassen) Explosion |
| `power.usePipeLoss` | `true` | Leistungsrohre haben Verlust (`powerResistance`) |
| `woodenPipe.item/liquid.exclusion` | leer | keine Ausnahmen – Holzrohre dürfen aus allem ziehen |
| `itemLifespan` (BC-Core) | `1200` | Lebensdauer von Items, die aus Rohren fallen |

## 2. Rohrtypen, Rezepte, Texturen

Rezepte (`createPipe`): drei Zutaten → `A B C` in **einer Reihe**, Ergebnis **8** Rohre; zwei Zutaten → formlos,
Ergebnis **1** Rohr. „Glas“ = Vanilla-Glas, „Holz“ = Tag `minecraft:planks` (Ore-Dict `plankWood`).
Rohrdichtmittel (`pipeWaterproof`): formlos aus **1 Kaktusgrün** (Farbstoff Meta 2 = 1.21 `green_dye`) → 1.

Texturindizes beziehen sich auf `gfx/buildcraft/blocks/blocks.png` (16×16 Kacheln, Index = Zeile·16 + Spalte) im
Pack-Jar `ftb ultimate/mods/buildcraft.jar`. „Basis“ = alle Seiten, „Sonder“ = siehe Spalte.

| Rohr (Original-Name) | ID-Vorschlag | Deutsch | Rezept | Textur |
|---|---|---|---|---|
| Wooden Transport Pipe | `wooden_transport_pipe` | Holz-Transportrohr | Holz Glas Holz → 8 | 16, Quellseite 31 |
| Cobblestone Transport Pipe | `cobblestone_transport_pipe` | Bruchstein-Transportrohr | Bruchstein Glas Bruchstein → 8 | 17 |
| Stone Transport Pipe | `stone_transport_pipe` | Stein-Transportrohr | Stein Glas Stein → 8 | 29 |
| Sandstone Transport Pipe | `sandstone_transport_pipe` | Sandstein-Transportrohr | Sandstein Glas Sandstein → 8 | 143 |
| Iron Transport Pipe | `iron_transport_pipe` | Eisen-Transportrohr | Eisenbarren Glas Eisenbarren → 8 | Ausgabeseite 18, sonst 19 |
| Golden Transport Pipe | `golden_transport_pipe` | Gold-Transportrohr | Goldbarren Glas Goldbarren → 8 | 30 |
| Diamond Transport Pipe | `diamond_transport_pipe` | Diamant-Transportrohr | Diamant Glas Diamant → 8 | Item 21, Seiten 22–27 (unten, oben, N, S, W, O) |
| Obsidian Transport Pipe | `obsidian_transport_pipe` | Obsidian-Transportrohr | Obsidian Glas Obsidian → 8 | 28 |
| Emerald Transport Pipe | `emerald_transport_pipe` | Smaragd-Transportrohr | Smaragd Glas Smaragd → 8 | 109, Quellseite 110 |
| Void Transport Pipe | `void_transport_pipe` | Leere-Transportrohr | Tintenbeutel Glas Redstone → 8 | 142 |
| Wooden Waterproof Pipe | `wooden_fluid_pipe` | Wasserdichtes Holzrohr | Dichtmittel + Holz-Transportrohr → 1 | 112, Quellseite 31 |
| Cobblestone Waterproof Pipe | `cobblestone_fluid_pipe` | Wasserdichtes Bruchsteinrohr | Dichtmittel + Bruchstein-Transportrohr → 1 | 113 |
| Stone Waterproof Pipe | `stone_fluid_pipe` | Wasserdichtes Steinrohr | Dichtmittel + Stein-Transportrohr → 1 | 114 |
| Sandstone Waterproof Pipe | `sandstone_fluid_pipe` | Wasserdichtes Sandsteinrohr | Dichtmittel + Sandstein-Transportrohr → 1 | 159 |
| Iron Waterproof Pipe | `iron_fluid_pipe` | Wasserdichtes Eisenrohr | Dichtmittel + Eisen-Transportrohr → 1 | Ausgabeseite 115, sonst 19 |
| Golden Waterproof Pipe | `golden_fluid_pipe` | Wasserdichtes Goldrohr | Dichtmittel + Gold-Transportrohr → 1 | 116 |
| Emerald Waterproof Pipe | `emerald_fluid_pipe` | Wasserdichtes Smaragdrohr | Dichtmittel + Smaragd-Transportrohr → 1 | 111, Quellseite 110 |
| Void Waterproof Pipe | `void_fluid_pipe` | Wasserdichtes Leererohr | Dichtmittel + Leere-Transportrohr → 1 | 158 |
| Wooden Conductive Pipe | `wooden_power_pipe` | Leitendes Holzrohr | Redstone + Holz-Transportrohr → 1 | 118, Quellseite 31 |
| Stone Conductive Pipe | `stone_power_pipe` | Leitendes Steinrohr | Redstone + Stein-Transportrohr → 1 | 120 |
| Golden Conductive Pipe | `golden_power_pipe` | Leitendes Goldrohr | Redstone + Gold-Transportrohr → 1 | 122 |
| Cobblestone Structure Pipe | `cobblestone_structure_pipe` | Bruchstein-Strukturrohr | Kies + Bruchstein-Transportrohr → 1 | 125 |
| Pipe Waterproof (Item) | `pipe_waterproof` | Rohrdichtmittel | Kaktusgrün → 1 | `items.png` Index 33 |

Hinweise: Es gibt **kein** Smaragd- oder Diamant-Leistungsrohr, kein Diamant-/Obsidian-Flüssigkeitsrohr. Die
Rezepte sind Original-Eigenheiten (z. B. Leere-Rohr mit Tintenbeutel + Redstone) und bleiben so. Stapelgröße 64.
Creative-Tab: BuildCraft-Tab.

## 3. Gemeinsamer Rohrblock

### 3.1 Block (`BlockGenericPipe`, `ItemPipe`, `TileGenericPipe`)

- Material Glas, Härte `0,25` (jedes Werkzeug, kein Werkzeugzwang), kein Vollblock, lässt Licht durch, Blätter
  ersetzen ihn nicht. Kein Redstone-Ausgang (nur über Gates).
- **Kollision und Auswahl:** Kern `0,25–0,75` in allen Achsen; für jede **verbundene** Seite (§3.2) reicht die Box
  bis zum Blockrand (Kollision: Kern + je verbundener Seite ein Arm; Auswahl: eine umschließende Box).
- **Setzen** (`ItemPipe.onItemUse`): Schnee-Schicht → ersetzt; Ranken, hohes Gras, toter Busch oder ersetzbarer
  Block → an dieser Stelle; sonst angrenzend an die geklickte Seite. Platzier-Sound = Block-Sound (Stein),
  Lautstärke `(v+1)/2`, Tonhöhe `·0,8`. Danach `onBlockPlaced` der Logik (Eisen: Ausgabe suchen, §4.5).
- **Abbau:** droppt das Rohr-Item (1×) und den Inhalt (`dropContents`): Item-Rohre fassen erst zusammen
  (`groupEntities`, §4.8) und werfen dann jeden Stapel aus; Flüssigkeit und Leistung gehen verloren. Filter von
  Diamant-/Smaragdrohr sind Phantom-Einträge und droppen **nicht**.
- **Rechtsklick** (`BlockGenericPipe.onBlockActivated`, ohne Gate-/Draht-Zweige):
  Hält der Spieler ein Rohr-Item → kein Handeln (Platzieren hat Vorrang). Schild → nichts.
  BuildCraft-Schraubenschlüssel → `logic.blockActivated` (Holz/Smaragd: Quelle wechseln, Eisen: Ausgabe wechseln).
  Sonst → `logic.blockActivated` (Diamant: GUI; Smaragd: erst Schlüssel-Prüfung, dann GUI).
  Danach immer Nachbar-Update (`notifyBlocksOfNeighborChange`).
- **Nachbaränderung:** setzt `blockNeighborChange`; im nächsten Tick: Verbindungen neu berechnen
  (`computeConnections`), `pipe.onNeighborBlockChange` (Logik + Transport), Render-Status erneuern.
- **Tick-Reihenfolge (Server und Client):** ggf. initialisieren → Nachbaränderung verarbeiten → `PowerProvider.update`
  (nur Holz-/Smaragd-/Obsidianrohre, §5) → `transport.updateEntity` → `logic.updateEntity`.
- Rohre verbinden sich **nur mit Block-Entities**, nie mit einfachen Blöcken.

### 3.2 Verbindungsregeln

Zwei Positionen sind verbunden (`Utils.checkPipesConnections`), wenn mindestens eine Seite ein Rohr ist und jedes
beteiligte Rohr der Verbindung zustimmt. Ein Rohr stimmt einer Nachbar-Block-Entity zu, wenn **alle** gelten:

1. **Transportart** (nur wenn der Nachbar ein Rohr ist): gleiche Transportklasse – Items ↔ Items,
   Flüssigkeit ↔ Flüssigkeit, Leistung ↔ Leistung, Struktur ↔ Struktur.
2. **Nachbarrohr stimmt zu** (rekursiv dieselben Regeln aus seiner Sicht).
3. **Material** (`PipeLogic*.isPipeConnected`, nur gegenüber Rohren):
   - Holz ↔ Holz **nein**; Smaragd zählt als Holz (Smaragd ↔ Holz, Smaragd ↔ Smaragd nein).
   - Bruchstein ↔ Stein **nein** (in beide Richtungen; gilt für Item-, Flüssigkeits- und Leistungsrohre,
     Bruchstein-Strukturrohr hat Bruchstein-Logik).
   - Obsidian ↔ Obsidian **nein**.
   - Sandstein: verbindet sich **nur mit Rohren** (keine Inventare/Tanks/Maschinen).
   - Gold, Eisen, Diamant, Leere: keine Einschränkung.
4. **Transport gegenüber Nicht-Rohren:**
   - Items: Item-Inventar mit ≥ 1 Slot auf dieser Seite (1.21: `Capabilities.ItemHandler.BLOCK` der Seite), Quarry,
     Mining Well (`IMachine.manageSolids`).
   - Flüssigkeit: Fluid-Handler auf dieser Seite mit ≥ 1 Tank (1.21: `Capabilities.FluidHandler.BLOCK`), Pumpe,
     Raffinerie (`IMachine.manageLiquids`).
   - Leistung: jede BC-Maschine (`IMachine`: Quarry, Mining Well, Pumpe, Raffinerie) und jeder MJ-Empfänger
     (`IPowerReceptor` → 1.21 `BcCapabilities.POWER_RECEPTOR`, auch ohne aktuellen Provider).
   - Struktur: **jede** Block-Entity.

Die Verbindungen werden je Seite gepuffert (`pipeConnectionsBuffer`) und beim Neuberechnen auch in das Nachbarrohr
gespiegelt. Sichtbar sind sie als Arme des Rohrmodells.

### 3.3 Darstellung

- Rohr = Kern + Arme zu verbundenen Seiten, je Seite die Textur aus §2 (Quell-/Ausgabeseite eigene Textur).
- Item im Inventar/der Hand: Basistextur (`getTextureIndex(UNKNOWN)`).
- **Items im Rohr:** höchstens die ersten 10 Items je Rohr, als schwebendes Item-Modell mit Maßstab 0,7, Position
  = interne Item-Position + 0,25 nach oben (Itemmitte auf Rohrmitte).
- **Flüssigkeit:** je Abschnitt (6 Seiten + Mitte) ein Quader mit Füllhöhe in 40 Stufen
  (`Menge / Kapazität · 39`), waagerecht von unten gefüllt, senkrecht als Säule; Anzeige-Menge geglättet
  `(alt·4 + neu)/5` (bei 0 und Inhalt > 0 sofort der neue Wert), Sync alle `updateFactor` Ticks an Spieler im
  Umkreis 24, alle `longUpdateFactor` Updates ein Vollpaket.
- **Leistung:** je Seite ein Strahl von der Blockkante zur Mitte mit Querschnitt nach `displayPower` (0–100 Stufen,
  Kantenlänge `0,005 + s·(0,5−0,005)/100`), Textur 4 normal, Textur 6 bei Überlast (`overload`); Sync alle
  `2·updateFactor` Ticks.

## 4. Item-Rohre (`PipeTransportItems`)

### 4.1 Grundbewegung

- Jedes Item im Rohr: Stack, Position (double), Geschwindigkeit, `input` (Bewegungsrichtung beim Eintritt),
  `output` (Zielseite, `UNKNOWN` = keine), `toCenter`, Sperrliste (`blacklist`, Seiten).
- Normalgeschwindigkeit `pipeNormalSpeed = 0,01` Blöcke/Tick; Itemboden `getPipeFloorOf = 0,25` (Position y = Rohr-y
  + 0,25).
- **Eintritt** (`entityEntering`): Geschwindigkeit anpassen (`readjustSpeed`, §4.3); neues Item registrieren (ein
  Item, das noch in einem anderen Rohr steckt, wird dort zur Entfernung vorgemerkt); bei waagerechter Bewegung
  y auf Rohr-y + 0,25 setzen; Ziel bestimmen (§4.2, nur Server); Hook `entityEntered` (Gold); Client-Sync. Sind
  danach > 32 Items im Rohr → zusammenfassen (§4.8); sind es dann noch > 100 → **Explosion Stärke 1 ohne
  Blockschaden** an der Rohrposition (Rohr und Items bleiben; passiert bei jedem weiteren Eintritt erneut).
- **Tick** (`moveSolids`): geladene Items (nach Laden 2 Ticks Verzögerung) einfügen, vorgemerkte entfernen; jedes
  Item um `speed` in Richtung `input` (solange `toCenter`) bzw. `output` bewegen.
  - **Mitte erreicht** (`toCenter` und Abstand zur Mitte (x+0,5, y+0,25, z+0,5) in jeder Achse < `speed·1,01`) oder
    außerhalb `[-1, +2)` um das Rohr: `toCenter = false`, Position exakt auf die Mitte. Ist `output = UNKNOWN`:
    Hook `drop` (Leere), dann als Item-Entity in Bewegungsrichtung `input` auswerfen (§4.7). Sonst Hook
    `centerReached` (Leere: Item verschwindet).
  - **Rand erreicht** (nicht `toCenter`, Position außerhalb `[0, 1]` des Blocks): Hook `endReached`, Item entfernen und
    an die Zielseite übergeben (§4.4).

### 4.2 Zielwahl (`resolveDestination`, `getPossibleMovements`)

1. Herkunftsseite (`input.getOpposite()`) zur Sperrliste hinzufügen (die Liste bleibt für dieses Item in diesem Rohr
   erhalten, auch über Rückläufer hinweg).
2. Kandidaten: alle Seiten, die nicht gesperrt sind, für die `outputOpen` gilt (Eisen: nur Ausgabeseite) und die
   Items annehmen können: verbunden **und** (Rohr mit Item-Transport **oder** Quarry/Mining Well-artige Rohr-Eingänge
   **oder** Inventar, das den Stack auf der Gegenseite simuliert wenigstens teilweise aufnimmt).
3. Keine Kandidaten und `allowBouncing` (nur Eisen-Item-Rohr) → Herkunftsseite, sofern sie Items annimmt.
4. Hook `filterPossibleMovements` (Diamant, §4.5).
5. Zufällige Wahl gleichverteilt aus der Liste (Duplikate zählen mehrfach); leer → `UNKNOWN`.

### 4.3 Geschwindigkeit

- Standard (`defaultReajustSpeed`, alle Rohre ohne eigenen Hook sowie Diamant): `speed > 0,01` → `speed − 0,01`;
  danach mindestens 0,01.
- Stein: `speed > 0,01` → `speed − 0,005`; mindestens 0,01.
- Gold: `readjustSpeed` **und** `entityEntered` setzen beide `speed = min(max(0,01, speed) · 2, 0,3)` – beim
  Eintritt also zweimal (Faktor 4 je Goldrohr, Deckel 0,3). Original-Eigenheit, 1:1.

### 4.4 Übergabe am Rand (`handleTileReached`)

- Zielblock ist ein Item-Rohr / Rohr-Eingang → dort `entityEntering` mit derselben Bewegungsrichtung (gleiches
  Item-Objekt, Geschwindigkeit bleibt bis zur Anpassung).
- Inventar → Stack auf der Gegenseite einfügen; Rest > 0 → **Rückläufer** (`reverseItem`): `toCenter = true`,
  `input = output.getOpposite()`, Geschwindigkeit anpassen, neues Ziel (Sperrliste wächst um die abweisende Seite),
  Hook `entityEntered`, Sync.
- Sonst (Ziel verschwunden) → Hook `drop`, Item-Entity in Richtung `output` (§4.7).

### 4.5 Rohrtypen mit eigener Logik

**Holz (Item-Transport)** – `PipeItemsWood`, `PipeLogicWood`
- Quellseite = Blockzustand 0–5, 6 = keine. Quelle ist eine Nachbarseite mit Inventar oder Tank, **kein Rohr**, und
  verbunden. Neue Quelle suchen (`switchSource`): ab der aktuellen Seite + 1 reihum (Reihenfolge unten, oben, N, S,
  W, O), erste gültige, sonst 6. Automatisch bei Initialisierung und Nachbaränderung, wenn keine/ungültige Quelle;
  Schraubenschlüssel erzwingt den Wechsel.
- MJ: `configure(min 1, max 64, Aktivierung 1, Speicher 64)`, Verlust **64 je Tick** (`configurePowerPerdition(64, 1)`)
  – nicht im selben Tick genutzte Energie verfällt praktisch sofort.
- `doWork` (jeden Tick mit ≥ 1 MJ): Quelle 0–5 und dort ein Inventar → `checkExtract`: erster nicht leerer Slot im
  Bereich der Seite, entnimmt `useEnergy(1, Stapelgröße)` Items (also bis zur gespeicherten MJ-Menge, höchstens der
  Slot). Das Item startet 0,6 Blöcke vor der Inventarmitte in Richtung Rohr (Mitte + 0,5 in allen Achsen) und tritt
  mit Bewegungsrichtung weg vom Inventar ein. Leerer Stack → 1 MJ wird verbraucht.
- Sonderfälle für ungeseitete Inventare mit 2 bzw. 3 Slots (oben/unten vs. Seiten) siehe §8.1.

**Smaragd (Item-Transport)** – `PipeItemsEmerald`, erbt Holz
- 9 Phantom-Filter in einer Reihe (GUI §4.6). Filter = Item + Schaden (`isItemEqual`, ohne NBT).
- Entnahme reihum: aktueller Filter (`currentFilter`, leere Filter werden übersprungen). Slots im Seitenbereich der
  Reihe nach; erster nicht leerer Slot, der zum aktuellen Filter passt → Entnahme wie Holz, danach **nächster Filter**.
  Kein Filter gesetzt → keine Entnahme. Ist das Item des aktuellen Filters nicht im Inventar, wird **nichts**
  entnommen und der Filter wechselt nicht (Original-Eigenheit, 1:1).
- Rechtsklick: Rohr-Item in der Hand → nichts; Schraubenschlüssel → Quelle wechseln; sonst GUI.
- Filter werden mit dem Rohr gespeichert und an Clients synchronisiert.

**Eisen** – `PipeItemsIron` / `PipeLiquidsIron`, `PipeLogicIron`
- Ausgabeseite = Blockzustand 0–5; `outputOpen` nur dort. Items nehmen Eingang von **allen** Seiten an.
- Wechsel (`switchPosition`): ab aktueller Seite + 1 reihum die erste Seite mit Rohr-Eingang, Inventar, Tank oder
  irgendeinem Rohr – **außer** Holz-/Smaragdrohren (beliebiger Transport) und Strukturrohren. Ob die Seite wirklich
  verbunden ist, wird nicht geprüft (Original). Nichts gefunden → bleibt.
- Beim Setzen: Zustand 1, dann einmal wechseln. Schraubenschlüssel → wechseln. Redstone: bei **jeder Änderung** des
  Signals (an **und** aus) einmal wechseln. Redstone-Staub verbindet sich optisch mit Eisenrohren.
- Nur das Eisen-**Item**-Rohr lässt Items zurückprallen (`allowBouncing`, §4.2).

**Diamant** – `PipeItemsDiamond`, `PipeLogicDiamond`
- 54 Phantom-Filter: 6 Reihen à 9, Reihe = Seite (unten, oben, N, S, W, O). GUI `filter.png` (175×225), Titel
  „Filters“, Spielerinventar bei y 140/198. Klick auf ein Filterfeld setzt eine Kopie des Cursor-Stacks mit Menge 1
  (leerer Cursor → löschen); Shift-Klick/Ziehen haben keine Wirkung auf die Filter.
- `filterPossibleMovements`: für jede mögliche Seite: hat die Reihe einen passenden Filter (gleiches Item; bei
  beschädigbaren Items Schaden egal, sonst gleicher Schaden) → Seite **je passendem Filterfeld einmal** in die
  Trefferliste (Gewichtung!). Seiten mit leerer Reihe → Standardliste. Treffer vorhanden → Trefferliste, sonst
  Standardliste. Seiten mit Filtern, aber ohne Treffer, sind damit gesperrt.

**Obsidian** – `PipeItemsObsidian`
- Nur wirksam mit **genau einer** verbundenen Seite; offene Seite = Gegenseite (`getOpenOrientation`).
- MJ: `configure(min 1, max 64, Aktivierung 1, Speicher 256)`, Verlust 1 je Tick.
- `doWork`: für Abstand d = 1…4 die Saugbox prüfen; erster Erfolg beendet den Tick. Kein Erfolg → 1 MJ verbrauchen.
  Saugbox in Richtung der offenen Seite: Scheibe der Dicke 1 im Abstand d, Querschnitt (2d+1)×(2d+1) um die
  Rohrachse (z. B. Osten: x ∈ [x+d, x+d+1], y ∈ [y−d, y+d+1], z ∈ [z−d, z+d+1]).
- Ansaugbar: lebende Item-Entities mit Menge > 0, die nicht unter den letzten 32 von diesem Rohr ausgeworfenen sind,
  und Pfeile, jeweils wenn `useEnergy(1, d)` (simuliert) ≥ d. Bei d = 1 zusätzlich Truhen-Loren: aus dem ersten nicht
  leeren Slot 1 Item, kostet 1 MJ, erscheint als Item 0,3 über der Lore und wird sofort angesaugt.
- Ansaugen (`pullItemIntoPipe`): Sound `random.pop` (Lautstärke 0,2). Pfeil → kostet d MJ, wird zu 1 Pfeil-Item.
  Item-Entity → kostet `d·Menge` MJ, reicht die Energie nicht, wird nur `Energie/d` Items abgespalten (Rest bleibt
  liegen), sonst die ganze Entity. Startgeschwindigkeit `|v|/2 − 0,05`, mindestens 0,01. Das Item startet in der
  Rohrmitte mit Bewegungsrichtung zur verbundenen Seite.
- **Berührung**: Item-Entities/Pfeile, die den Rohrblock berühren, werden mit d = 0 angesaugt – **kostenlos**
  (Original-Eigenheit).
- Abholanimation (`obsidianPipePickup`): Item fliegt sichtbar ins Rohr (Client-Effekt).

**Leere (Item)** – `PipeItemsVoid`: Items verschwinden beim Erreichen der Mitte; würden sie ausgeworfen (kein Ziel),
wird die Menge vorher auf 0 gesetzt (kein Drop).

**Sandstein, Bruchstein, Gold, Stein**: nur Verbindungsregeln (§3.2) bzw. Geschwindigkeit (§4.3).

### 4.6 GUIs

- Diamant: `gfx/buildcraft/gui/filter.png`, siehe oben. Smaragd: `gfx/buildcraft/gui/filter_2.png` (175×132),
  9 Filterfelder bei (8 + i·18, 18), Spielerinventar bei y 50/108. Beide: Phantom-Felder (Filter sind keine echten Slots), nutzbar solange das Rohr
  existiert (`isUseableByPlayer`).

### 4.7 Auswerfen (`EntityPassiveItem.toEntityItem`)

Item-Entity an der aktuellen Position, Lebensdauer 1200, Aufheben nach 10 Ticks, Bewegung `0,1 + speed·2` in die
angegebene Richtung plus Gauß-Rauschen `·(rand·0,04 − 0,02)` je Achse. Ausgeworfene Items merkt sich das
Obsidianrohr (letzte 32).

### 4.8 Zusammenfassen (`groupEntities`)

Je Paar gleicher Items (Item + Schaden) im Rohr: Summe < Maximalstapel → das zweite wird in das erste addiert und
entfernt. Gilt auch beim Abbau vor dem Drop.

### 4.9 Rohr-Eingang für Maschinen (`Utils.addToRandomPipeEntry`)

Quarry und Mining Well geben (nach dem zufälligen Nachbarinventar) an ein zufälliges angrenzendes Item-Rohr ab, das
Items annimmt und zur Maschine verbunden ist; das Item startet an der Blockkante (Maschinenmitte + 0,5 Richtung Rohr,
y + 0,25) mit Bewegungsrichtung ins Rohr. Erst danach Item-Fontäne (`ItemOutput`, schon umgesetzt).
Das Rohr hat **keine** Item-Capability: Trichter, Spender, Truhen usw. können nicht hineinschieben (§8.2).

## 5. Flüssigkeitsrohre (`PipeTransportLiquids`)

### 5.1 Werte

| Rohr | `flowRate` (mB je Takt) | `travelDelay` (Ticks) |
|---|---|---|
| Standard (Holz, Bruchstein, Stein, Sandstein, Eisen, Leere) | 10 | 12 |
| Gold, Smaragd | 40 | 4 |

7 Abschnitte (6 Seiten + Mitte), je **250 mB**. Jeder Abschnitt hat einen Ringpuffer `incoming[travelDelay]`
(Länge immer 12, da vor dem Setzen der Gold-Werte angelegt; genutzt werden die Plätze `Weltzeit % travelDelay`).
`INPUT_TTL = 60`, `OUTPUT_TTL = 80`, `OUTPUT_COOLDOWN = 30`. Je Seite Zustand `None / Input / Output`.

### 5.2 Abschnitt (`PipeSection`)

- `fill`: höchstens `flowRate − incoming[jetzt]` und die freie Kapazität; Gefülltes wird in `incoming[jetzt]` addiert.
- `available` = Inhalt − Summe aller `incoming` (frisch eingeflossene Menge ist erst nach `travelDelay` Ticks
  verfügbar).
- `drain`: höchstens `min(maxDrain, flowRate, available)`.
- Nur eine Flüssigkeitsart je Abschnitt (Forge-`LiquidTank`).

### 5.3 Tick (nur Server)

1. **Zustände** (`computeCurrentConnectionStatesAndTickFlows`): für alle Abschnitte Zeitplatz setzen und
   `incoming[jetzt] = 0`. Je Seite: `Input` → TTL −1, bei ≤ 0 → `None`; sonst nicht `outputOpen` → `None`; sonst
   Abklingzeit > 0 → −1; sonst Ausgabe-TTL ≤ 0 → `None`, Abklingzeit 30, TTL 80; sonst Nachbar kann annehmen
   (verbunden; Nachbarrohr mit `inputOpen` zur Gegenseite; Rohr-Eingang oder Tank) → `Output`, Zähler +1.
2. **Aus dem Rohr** (nur wenn Ausgänge > 0): je `Output`-Seite mit Tank: `drain(flowRate)` simuliert, in den Nachbarn
   füllen, Gefülltes abziehen; nichts gefüllt → Ausgabe-TTL −1.
3. **Aus der Mitte**: verfügbar ≥ 1 → je `Output`-Seite `menge = (int)(frei_Seite / flowRate / Ausgänge ·
   min(flowRate, verfügbar_Mitte))`, mindestens 1; aus der Mitte in den Seitenabschnitt.
4. **In die Mitte**: je Nicht-`Output`-Seite mit Inhalt passender Art: `inputPerTick = drain(flowRate)` simuliert.
   Dann je Seite `menge = (int)(input / flowRate / Anzahl · min(flowRate, frei_Mitte))`, mindestens 1, von der Seite in
   die Mitte.

- **Füllen von außen** (`fill(Seite)`): Tankindex = Seite; Gefülltes > 0 → Seite wird `Input`, TTL 60.
  Sandstein: nur aus Rohren (Nachbar auf dieser Seite muss ein Rohr sein). Leere: nimmt alles an und vernichtet es.
- **Entnehmen von außen**: nie (`drain` → nichts). Andere Blöcke können Rohren also nichts abzapfen.
- Nachbaränderung: Seiten ohne Verbindung werden geleert (Inhalt und `incoming`) und auf `None` gesetzt.
- NBT: Abschnitte (Kapazität, Flüssigkeit) und Zustände. `incoming` wird im Original beim Speichern **nicht**
  geschrieben (Fehler: die Schleife liest statt zu schreiben) → nach dem Laden 0. 1:1.

### 5.4 Holz-/Smaragd-Flüssigkeitsrohr (`PipeLiquidsWood`, `PipeLiquidsEmerald`)

- Quelle wie Holz-Item-Rohr (§4.5; Tank zählt als Quelle). `outputOpen` ist an der Quellseite **falsch**.
- MJ: `configure(min 1, max 100, Aktivierung 1, Speicher 250)`, Verlust 1 je Tick.
- `doWork`: Quelle mit Tank und `liquidToExtract ≤ 1000` → `liquidToExtract += useEnergy(1, 1) · 1000`
  (1 MJ = bis zu 1000 mB Entnahme-Budget).
- Tick: `liquidToExtract > 0` und Quelle 0–5 mit Tank → `drain(min(liquidToExtract, flowRate))` simuliert aus dem
  Tank, ins Rohr (Abschnitt der Quellseite) füllen, tatsächlich Gefülltes aus dem Tank abziehen und vom Budget.
- Smaragd-Flüssigkeitsrohr: nur andere Texturen und Gold-Werte (40/4), **kein Filter**.

## 6. Leistungsrohre (`PipeTransportPower`)

### 6.1 Werte

`powerResistance` 0,05 (Holz, Stein), 0,005 (Gold). `MAX_POWER_INTERNAL = 10 000`, `OVERLOAD_LIMIT = 7500`.

### 6.2 Algorithmus

Je Seite `internalPower`/`internalNextPower` (double) und `powerQuery`/`nextPowerQuery` (int); Doppelpuffer, der
einmal je Weltzeit-Tick umgeschaltet wird (`step`, auch aus `receiveEnergy`/`requestEnergy` heraus):
`powerQuery ← nextPowerQuery`, `nextPowerQuery ← 0`, `internalPower ↔ internalNextPower` (das neue `internalNextPower`
ist der alte `internalPower`-Rest); Seiten ohne Anfrage verlieren dabei 1 vom Rest (`internalNextPower[i]−−`, nur
wenn > 0).

Tick (nur Server): `step`; beim ersten Tick Nachbarn erfassen (sonst nur bei Nachbaränderung: verbundene Block-
Entities je Seite).
1. **Verteilen:** je Seite i mit `internalPower[i] > 0`: `div` = Summe `powerQuery[j]` der anderen Seiten j mit
   Anfrage > 0, die Rohr oder MJ-Empfänger sind. Je solcher Seite `watts = internalPower[i] / div · powerQuery[j]`:
   Rohr → dessen `receiveEnergy(Gegenseite, watts)`; MJ-Empfänger mit Provider → `receiveEnergy(watts)`; jeweils
   `internalPower[i] −= watts`, Anzeige beider Seiten `+= ceil(watts·0,1)`.
2. **Anzeige:** `displayPower[i] = min(100, max(Anzeige, ceil(internalPower[i]·0,1)))`;
   `overload = max(internalPower) > 7500`.
3. **Anfragen:** je Nachbar-Empfänger (kein Rohr) mit `powerRequest() > 0` → eigene Anfrage auf dieser Seite.
   Danach je Seite i: Summe der Anfragen aller anderen Seiten an das Nachbarrohr weiterreichen
   (`requestEnergy(Gegenseite, Summe)`).
- `receiveEnergy(Seite, w)`: `internalNextPower[Seite] += w · (1 − powerResistance)`; erreicht der Wert **≥ 10 000** →
  Explosion Stärke 3 ohne Blockschaden und das Rohr wird ohne Drop entfernt.
- NBT: alle vier Felder je Seite.

### 6.3 Leitendes Holzrohr (`PipePowerWood`)

- MJ-Empfänger: `configure(min 2, max 1000, Aktivierung 1, Speicher 1000)`, Verlust 1 je 100 Ticks; `doWork` leer.
- Quelle/Seiten wie Holz (§4.5; die Quelle ist nur für die Textur relevant).
- **Überhitzung:** voller Speicher (= 1000) → `overheatTicks + 1` (bis 100), sonst −1 (bis 0). Bei
  `overheatTicks > 0` liefert `getPowerProvider()` **nichts** → Engines liefern nicht mehr.
- Tick: je verbundenem **Rohr**-Nachbarn: `entnahme = gespeichert/40 + 4` (> 40), `gespeichert/10` (> 10), sonst 1;
  `useEnergy(1, entnahme)` → `receiveEnergy` des Nachbarrohrs. Nur an Rohre, nicht direkt an Maschinen.
- Holz ↔ Holz verbindet nicht (§3.2) – die Engine sitzt am Holzrohr, dahinter Stein/Gold.

### 6.4 `powerRequest` der Empfänger

Für Leistungsrohre nötig (bisher nicht umgesetzt, `abweichungen.md` „offen bis Phase 10“):

| Empfänger | `powerRequest()` |
|---|---|
| BC-`TileMachine` (Quarry, Mining Well, Pumpe, Raffinerie) | aktiv → `ceil(min(maxReceived, maxStored − stored))`, sonst 0 |
| GT-Maschinen mit MJ (`BaseMetaTileEntityMJ`) | `ceil(min(maxReceived, maxStored − stored))` |
| Forestry-Maschinen (Fabricator, Carpenter) | arbeitet → `min(maxReceived, Aktivierung/5 + 1)`, sonst 0 |
| Power-Converters-BC-Consumer | Gesamtbedarf / interne Energie je Eingabe-MJ |
| Engines (BC, Forestry), PC-BC-Producer | 0 |

`isActive` der BC-Maschinen siehe deren Klassen (`TileQuarry`, `TilePump`, `TileRefinery`, `TileMiningWell`).

## 7. Strukturrohr (`PipeStructureCobblestone`)

Kein Transport; verbindet sich mit Strukturrohren und jeder Block-Entity, Bruchstein-Logik (nicht mit Steinrohren –
gilt nur zwischen gleichen Transportarten, also praktisch ohne Wirkung). Eisenrohre zeigen nie auf Strukturrohre.

## 8. Abweichungen / Punkte zur Freigabe

### 8.1 Inventarseiten (zur Freigabe)

Original: Holz-/Smaragdrohr entnehmen bei Forge-`ISidedInventory` aus dem Slotbereich der Seite, bei ungeseiteten
Inventaren mit 2 Slots (oben/unten → Slot 0, Seiten → Slot 1) bzw. 3 Slots (oben → 0, unten → 1, Seiten → 2), sonst
aus allen Slots; Einfügen über den `Transactor` der Seite. Der Vanilla-Ofen ist in Forge 1.4.7 geseitet: oben
Eingabe, unten Brennstoff, Seiten **Ausgabe** – in beide Richtungen.

**Vorschlag:** in 1.21 ausschließlich die Item-Capability der jeweiligen Seite (Slots der Seite in Reihenfolge,
`extractItem`/`insertItem`); die 2-/3-Slot-Sonderfälle entfallen. Folge beim Vanilla-Ofen (1.21-Regeln): Einfügen
oben → Eingabe, Seiten → Brennstoff; Entnehmen nur unten (Ausgabe, leerer Eimer). Eigene Maschinen bilden ihre
Original-Seiten bereits in ihrer Capability ab.

### 8.2 Kein Hineinschieben in Rohre (zur Freigabe)

Original: Rohre sind kein Inventar; nur Rohr-Eingänge (Quarry, Mining Well, andere Rohre, Holz/Obsidian) bringen
Items hinein, Trichter gab es noch nicht. **Vorschlag 1:1:** Rohre erhalten keine Item-Capability – Trichter/Spender
können nicht in Rohre schieben (wohl aber Rohre in Trichter liefern). Flüssigkeitsrohre erhalten eine
Fluid-Capability nur zum **Füllen** (wie `fill`, kein Entnehmen).

### 8.3 Technische Abweichungen

| Thema | Original | Neu | Grund |
|---|---|---|---|
| Blockstruktur | ein Block `pipe.id` 514, Rohrtyp im Tile, 21 Items | ein Block + BlockItem je Rohrtyp, gemeinsame Block-Entity-Klassen je Transportart | Registry-Idiom, kein Tile-Typwechsel |
| Quell-/Ausgabeseite | Block-Metadaten 0–6 | Blockstate-Eigenschaft (Holz/Smaragd 0–6, Eisen 0–5) | keine Metadaten |
| Items im Rohr | `EntityPassiveItem` mit Entity-ID, Pakete je Item | interne Item-Datensätze mit eigener ID, Sync per Payload | keine Pseudo-Entities |
| Flüssigkeitsabschnitte | Forge `LiquidTank` (ID + Meta) | `FluidStack` je Abschnitt | Fluid-API |
| Renderer | Display-Listen (GL) | Block-Entity-Renderer bzw. dynamisches Modell für Rohrarme | 1.21-Rendering |
| Rohrdichtmittel | Kaktusgrün (Farbstoff Meta 2) | `minecraft:green_dye` | 1.21-Item |
| Schraubenschlüssel | jedes `IToolWrench` | BC-Schraubenschlüssel (`BcWrenchItem`) | einziges `IToolWrench` im Umfang |

## 9. Teilpakete (Vorschlag)

| Teil | Inhalt |
|---|---|
| 10.1 | Rohr-Grundgerüst (Block, BE, Modell mit Armen, Verbindungsregeln) + Item-Transport + Holz, Bruchstein, Stein, Sandstein, Eisen, Gold, Leere; Rohr-Eingang für Quarry/Mining Well |
| 10.2 | Diamant (GUI, Filter), Smaragd (GUI, Filter), Obsidian (Saugen) |
| 10.3 | Flüssigkeitsrohre (alle 8) + Rohrdichtmittel |
| 10.4 | Leistungsrohre (3) + `powerRequest` aller Empfänger + Strukturrohr |
