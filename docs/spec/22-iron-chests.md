# 22 – Iron Chests

Quelle: `ftb ultimate/mods/IronChest.zip` (cpw, Iron Chests für 1.4.7), Pack-Config `ftb ultimate/config/IronChest.cfg`
(`ocelotsSitOnChests=true`, `cacheRenderingInformation=true`). Dekompiliert (2026-10-20, nur dieses Jar, MCP-Namen):
`reference/decompiled/ironchest/cpw/mods/ironchest/` – `IronChestType` (Kisten, Größen, Rezepte), `ChestChangerType` +
`ItemChestChanger` (Upgrades), `BlockIronChest`, `TileEntityIronChest`, `ContainerIronChestBase`, `client/GUIChest`,
`client/TileEntityIronChestRenderer` (Kristallkiste), `OcelotsSitOnChestsHandler` + `IronChestAIOcelotSit`. Texturen
unter `…/sprites/` (Kistenmodelle, GUI-Hintergründe, `block_textures.png`, `item_textures.png`).

Umfang (Nutzer, 2026-10-20): **komplett wie im Pack** – alle 7 Kisten und alle 9 Upgrade-Items. Paket
`dev.craft.ironchest`.

## 1 Kisten

Im Original ein Block mit Metadaten (`IronChest`, ID 975); in 1.21 ein eigener Block je Kiste.

| Kiste | ID | Slots | Raster (Spalten × Reihen) | GUI (B × H) | GUI-Textur |
|---|---|---:|---|---|---|
| Kupferkiste (Copper Chest) | `craft:copper_chest` | 45 | 9 × 5 | 184 × 184 | `coppercontainer.png` |
| Eisenkiste (Iron Chest) | `craft:iron_chest` | 54 | 9 × 6 | 184 × 202 | `ironcontainer.png` |
| Silberkiste (Silver Chest) | `craft:silver_chest` | 72 | 9 × 8 | 184 × 238 | `silvercontainer.png` |
| Goldkiste (Gold Chest) | `craft:gold_chest` | 81 | 9 × 9 | 184 × 256 | `goldcontainer.png` |
| Diamantkiste (Diamond Chest) | `craft:diamond_chest` | 108 | 12 × 9 | 238 × 256 | `diamondcontainer.png` |
| Kristallkiste (Crystal Chest) | `craft:crystal_chest` | 108 | 12 × 9 | 238 × 256 | `diamondcontainer.png` |
| Obsidiankiste (Obsidian Chest) | `craft:obsidian_chest` | 108 | 12 × 9 | 238 × 256 | `diamondcontainer.png` |

**Block (`BlockIronChest`):**
- Material Eisen, Härte **3,0**, Explosionswiderstand 3 (1.4.7: `hardness·5 = 15`, intern `/5`). Nur die
  Obsidiankiste hat **10 000**.
- Abbau: Spitzhacke beliebiger Stufe nötig (Material Eisen = `setRequiresTool`, Spitzhacke wirkt auf Eisen), sonst kein
  Drop. Tags `minecraft:mineable/pickaxe`, `requiresCorrectToolForDrops`, keine Stufe.
- Hitbox wie die Vanilla-Kiste: 1/16 bis 15/16 waagerecht, Höhe 14/16. Nicht undurchsichtig, TESR-Darstellung
  (`getRenderType 22`).
- Beim Platzieren zeigt die Front zum Spieler. Kisten **verbinden sich nie** zu Doppelkisten.
- Öffnen per Rechtsklick. Wenn der Block darüber auf der Unterseite fest ist (`isBlockSolidOnSide(DOWN)`), passiert
  nichts (1.21: `isFaceSturdy(DOWN)`). Ein Ozelot bzw. eine Katze blockiert **nicht** (anders als bei Vanilla).
- Abbauen: der Inhalt fällt verstreut heraus (Vanilla-Streuung: Stapel 10–30, Bewegung 0,05 Gauß, Y +0,2). Die Kiste
  droppt sich selbst, **ohne** Inhalt.
- Kreativ: eigener Tab „Craft: Iron Chests“ (ein Tab je Ursprungs-Mod; Original: Kisten unter „Dekoration“, Upgrades
  unter „Verschiedenes“). Reihenfolge Eisen, Gold, Diamant, Kupfer, Silber, Kristall, Obsidian (Ordinalzahl), danach
  die Upgrades.

**Blockentity (`TileEntityIronChest`):**
- Stapelgrenze 64. Benutzbar bis 8 Blöcke Abstand (`distSq ≤ 64`) und nur, solange die Blockentity noch dieselbe ist.
- Deckel: Vanilla-Animation (Schritt 0,1 pro Tick). Öffnungsgeräusch `random.chestopen` bei Deckel 0, Schließgeräusch
  `random.chestclosed` beim Unterschreiten von 0,5, jeweils Lautstärke 0,5 und Tonhöhe 0,9–1,0. Die Zahl der Nutzer
  wird alle 200 Ticks über Spieler im Umkreis von 5 Blöcken mit offenem Kistenmenü neu gezählt.
- Automatisierung: Das Original ist ein schlichtes `IInventory` (kein `ISidedInventory`). Alle Slots sind von allen
  Seiten erreichbar, Ein- und Ausgabe frei → 1.21: `Capabilities.ItemHandler.BLOCK` auf allen Seiten (Trichter,
  BC-Rohre).
- **Komparator-Signal** wie bei der Vanilla-Kiste (Füllstand). Das ist eine bewusste Erweiterung, weil es Komparatoren
  erst ab 1.5 gab (Nutzer, 2026-10-20: „machs rein, wenn's sinnvoll ist“).

**Menü (`ContainerIronChestBase`, `GUIChest`):**
- Kistenslots ab (12, 8), Raster 18 px. Spielerinventar mittig: `links = (B − 162)/2 + 1`, Reihen bei
  `H − (4 − r)·18 − 10`, Hotbar bei `H − 24`.
- Shift-Klick: aus der Kiste ins Inventar (von hinten aufgefüllt), aus dem Inventar in die Kiste.
- Hintergrund ist die Original-Textur, ohne Titeltext (das Original zeichnet keinen).

## 2 Kristallkiste

- Durchsichtiges Modell (`crystalchest.png`).
- Server: Nach jeder Inventaränderung werden gleiche Items (`isItemEqual`: ID + Schaden, ohne NBT) zusammengezählt, nach
  Menge absteigend sortiert, und die **8 häufigsten** werden als „Top-Stapel“ an die Clients übertragen (Item, Schaden,
  Menge; 1.21: Update-Tag).
- Darstellung: Ist der Spieler näher als 128 Blöcke, drehen sich die Top-Stapel als Items in der Kiste: 8 Positionen
  (2 Ebenen × 2 × 2: x/z 0,3 / 0,7, y 0,45 / 0,1) mit Skalierung 0,7. Gibt es nur **ein** Item, wird es mittig bei
  (0,5 / 0,32 / 0,5) mit Skalierung 0,85 dargestellt. Drehung um Y: eine Umdrehung pro 16,384 s
  (`System.currentTimeMillis() & 16383`). Beleuchtung beim Item-Rendern aus.

## 3 Upgrade-Items

Werden mit Rechtsklick (`onItemUseFirst`, nur Server) auf die passende Kiste angewendet. Stapelgröße 1.

| Item | ID | von → nach |
|---|---|---|
| Eisen→Gold-Upgrade (Iron to Gold Chest Upgrade) | `craft:iron_gold_upgrade` | Eisen → Gold |
| Gold→Diamant-Upgrade | `craft:gold_diamond_upgrade` | Gold → Diamant |
| Kupfer→Silber-Upgrade | `craft:copper_silver_upgrade` | Kupfer → Silber |
| Silber→Gold-Upgrade | `craft:silver_gold_upgrade` | Silber → Gold |
| Kupfer→Eisen-Upgrade | `craft:copper_iron_upgrade` | Kupfer → Eisen |
| Diamant→Kristall-Upgrade | `craft:diamond_crystal_upgrade` | Diamant → Kristall |
| Holz→Eisen-Upgrade (Normal chest to Iron Chest Upgrade) | `craft:wood_iron_upgrade` | Vanilla-Kiste → Eisen |
| Holz→Kupfer-Upgrade | `craft:wood_copper_upgrade` | Vanilla-Kiste → Kupfer |
| Diamant→Obsidian-Upgrade | `craft:diamond_obsidian_upgrade` | Diamant → Obsidian |

**Ablauf (`TileEntityIronChest.applyUpgradeItem`, `ItemChestChanger.onItemUseFirst`):**
- Abbruch ohne Wirkung, wenn die Kiste gerade geöffnet ist (Nutzer > 0) oder das Upgrade nicht zur Kistenart passt.
- Neue Kiste der Zielart, Inhalt Slot für Slot übernommen (`min(neu, alt)` Slots), was nicht passt, fällt 1 Block über
  der Kiste heraus. Das kommt praktisch nie vor, weil jedes Upgrade gleich groß oder größer ist. Blickrichtung bleibt,
  Kristall-Top-Stapel werden neu sortiert.
- **Vanilla-Kiste** (nur `minecraft:chest`; die Redstone-Truhe gab es in 1.4.7 noch nicht): nur Holz→Eisen und
  Holz→Kupfer. Die 27 Slots werden übernommen, die Vanilla-Kiste wird ersetzt, ohne ihren Inhalt zu droppen. War sie
  Teil einer Doppelkiste, wird die andere Hälfte wieder eine Einzelkiste mit ihrem eigenen Inhalt.
- Das Upgrade-Item wird verbraucht.

## 4 Rezepte

Alle geformt. Zeichen: `C` Vanilla-Kiste, `G` Glas (`minecraft:glass`, kein Tag – 1.4.7 kannte nur ein Glas),
`O` Obsidian, `P` Holzbretter (`#minecraft:planks`, Ore-Dict `plankWood`). Kistenbezeichnungen = die Kiste als Zutat.

**Material-Zuordnung** (Ore-Dict → 1.21):
- Eisen: `ingotIron` **und** `ingotRefinedIron`. Das Original erzeugt jedes Rezept einmal pro Material; hier eine
  Zutat aus Eisenbarren oder `#c:ingots/refined_iron`.
- Gold: Goldbarren. Diamant: Diamant.
- Kupfer: `#c:ingots/copper`. Silber: `#c:ingots/silver` (GT-Silberbarren).

**Kisten (`IronChestType.generateTieredRecipes`):**

| Ergebnis | Muster | Zutaten |
|---|---|---|
| Eisenkiste | `EEE/ECE/EEE` | E Eisen, C Vanilla-Kiste |
| Eisenkiste | `EGE/GKG/EGE` | E Eisen, K Kupferkiste |
| Goldkiste | `AAA/AEA/AAA` | A Gold, E Eisenkiste |
| Goldkiste | `AGA/GSG/AGA` | A Gold, S Silberkiste |
| Diamantkiste | `GGG/DAD/GGG` | D Diamant, A Goldkiste |
| Diamantkiste | `GGG/GSG/DDD` | D Diamant, S Silberkiste |
| Kupferkiste | `KKK/KCK/KKK` | K Kupfer, C Vanilla-Kiste |
| Silberkiste | `SSS/SKS/SSS` | S Silber, K Kupferkiste |
| Silberkiste | `SGS/GEG/SGS` | S Silber, E Eisenkiste |
| Kristallkiste | `GGG/GDG/GGG` | D Diamantkiste |
| Obsidiankiste | `OOO/ODO/OOO` | D Diamantkiste |

**Upgrades (`ChestChangerType`; `m` = Material der Zielkiste, `s` = Material der Ausgangskiste):**

| Ergebnis | Muster |
|---|---|
| Eisen→Gold | `mmm/msm/mmm` – Gold-Ring, Eisen in der Mitte |
| Gold→Diamant | `GGG/msm/GGG` – Diamant, Gold, Diamant in der Mitte |
| Kupfer→Silber | `mmm/msm/mmm` – Silber-Ring, Kupfer in der Mitte |
| Silber→Gold | `mGm/GsG/mGm` – Gold in den Ecken, Glas, Silber in der Mitte |
| Kupfer→Eisen | `mGm/GsG/mGm` – Eisen in den Ecken, Glas, Kupfer in der Mitte |
| Diamant→Kristall | `GGG/GOG/GGG` – Glas-Ring, Obsidian in der Mitte |
| Holz→Eisen | `mmm/msm/mmm` – Eisen-Ring, Holzbretter in der Mitte |
| Holz→Kupfer | `mmm/msm/mmm` – Kupfer-Ring, Holzbretter in der Mitte |
| Diamant→Obsidian | `mmm/mGm/mmm` – Obsidian-Ring, Glas in der Mitte |

„Eisen“ ist überall Eisenbarren oder Raffiniertes Eisen (s. o.). In 20.4 prüfen, ob ein Muster mit einem vorhandenen
Rezept kollidiert (z. B. Ringe aus 8 Barren). Falls ja: in `abweichungen.md` festhalten und nachfragen.

## 5 Ozelots/Katzen

Pack-Config `ocelotsSitOnChests=true`: Gezähmte Ozelots setzen sich auch auf Iron Chests. Das Original ersetzt in
neuen Ozelots (`ticksExisted < 5`) die Sitz-KI (Priorität 6) durch eine Variante, für die der Iron-Chest-Block als
Sitzplatz zählt. In 1.21 sitzen nur gezähmte **Katzen** (`CatSitOnBlockGoal`; Ozelots sind nicht mehr zähmbar).
Deshalb wird beim `EntityJoinLevelEvent` von Katzen deren `CatSitOnBlockGoal` durch eine Unterklasse ersetzt, die
zusätzlich alle sieben Kisten als gültiges Ziel akzeptiert. Wie im Original verhindert die sitzende Katze das Öffnen
**nicht** (§1).

## 6 Abweichungen 1.21 (→ `abweichungen.md`)

- Ein Block je Kiste statt Metadaten; ein gemeinsamer Blockentity-Typ, die Kistenart kommt vom Block.
- Ozelot → Katze (§5).
- Kistenmodelle: Das 1.4.7-`ModelChest` wird mit derselben Spiegelung wie 1.4.7 gezeichnet (`scale(1, −1, −1)`), so
  passen die Original-Texturen (`*chest.png`, 64 × 64) unverändert (`textures/entity/ironchest/<art>.png`). Die
  Kristallkiste ist durchscheinend (`entityTranslucent`). Item-Darstellung über die Blockentity (geschlossen, Front
  nach Süden).
- Item-Texturen der Upgrades aus `item_textures.png` (Icon = Ordinalzahl) geschnitten, Partikel aus
  `block_textures.png` (Zeile = Kistenart).
- Komparator-Signal ergänzt (§1).
- Rezepte für Eisen nicht doppelt, sondern eine Zutat aus Eisen oder Raffiniertem Eisen (gleiche Wirkung).
- Eigener Kreativ-Tab statt „Dekoration“/„Verschiedenes“.

## 7 Tests (GameTest)

- Größen: jede Kiste hat ihre Slotzahl. Das Menü hat Slotzahl + 36 Slots.
- Upgrade Vanilla-Kiste → Eisen übernimmt 27 Stapel und die Blickrichtung. Eine Doppelkiste bleibt als Einzelkiste mit
  ihrem Inhalt stehen.
- Upgrade Eisen → Gold übernimmt den Inhalt. Ein falsches Upgrade (Gold→Diamant auf Eisen) ändert nichts und wird nicht
  verbraucht.
- Upgrade auf eine geöffnete Kiste wirkt nicht.
- Obsidiankiste übersteht eine TNT-Explosion, die Eisenkiste nicht.
- Abbauen droppt Kiste + Inhalt.
- Kristallkiste: Top-Stapel sortiert nach Menge, höchstens 8, gleiche Items zusammengezählt.
- Item-Capability: alle Slots von jeder Seite; Komparator zeigt den Füllstand.
- Katzen bekommen das Sitzziel für Iron Chests.
- Rezepte: alle 11 Kisten- und 9 Upgrade-Rezepte vorhanden und ohne Kollision (jedes Muster findet sein eigenes
  Rezept), Eisenkiste auch mit Raffiniertem Eisen.

## 8 Teilpakete

- **20.4** Umsetzung komplett (Blöcke, Blockentities, Menü/Screen, Renderer inkl. Kristallkiste, Upgrades, Rezepte,
  Katzen, Datagen, Lang de/en, Texturen, EMI läuft über normale Werkbank-Rezepte, GameTests, Wiki, Testplan).
