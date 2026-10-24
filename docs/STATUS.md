# Stand Phase 1 (2026-10-14)

## Erledigt
- Jars nach `reference/jars/` kopiert (IC2 1.115.231, GregTech 2.90, BuildCraft 3.4.3, Forestry 2.0.0.11,
  PowerConverters, PowerCrystalsCore – Letzteres von der Pack-URL geladen, Hash geprüft). Configs in `reference/config/`.
- Mit MCP-1.4.7-Mappings remappt (`reference/remapped/`) und mit Vineflower dekompiliert (`reference/decompiled/`).
  Werkzeuge: `tools/` (JDK 21, Vineflower, SpecialSource, MCP-Mappings, `decompile.sh`).
- Compact Solars 4.0 (MC 1.4.7) als Quellcode von GitHub geladen (`reference/source/compactsolars/v4.0/`),
  `docs/spec/11-compact-solars.md` verifiziert (Arrays, Solar-Hüte).
- Spezifikation `docs/spec/01–12` + `README.md`, `docs/balancing.md` (Vorschlag), `docs/abweichungen.md`, `CLAUDE.md`.
- Nachträge nach Nutzerentscheidungen:
  - `09-buildcraft.md` §7: Pumpe, Tank, Raffinerie, Mining Well (Zutat der Pumpe).
  - `10-energieumwandlung.md` §A.6: Thermionic Fabricator, Carpenter, Robustes Gehäuse, Lötkolben – Röhren/Platinen
    1:1 wie im Original.
  - `12-gregtech-materialien.md`: benötigte GT-Materialien und Herstellungsweg im Pack; `08-rezepte.md` §16 auf
    „wie im Pack“ umgestellt.
  - `abweichungen.md`: Kupfer, GT-Rezepte, Compact Solars, Forestry-Maschinen als entschieden eingetragen.

## Entscheidungen des Nutzers
- Externe Quellen (GitHub, CurseForge …) sind freigegeben.
- **Kupfer:** Vanilla-Kupfer mit IC2-Häufigkeit (15 Adern/Chunk, Größe 10). **Zinn:** eigenes Erz mit IC2-Werten
  (25 Adern/Chunk, Größe 6).
- **GregTech:** Rezepte wie im Pack; GT-Materialien kommen in den Umfang.
- **Umfang erweitert:** BuildCraft Pumpe/Tank/Raffinerie (Phase 4), Forestry Thermionic Fabricator/Carpenter (Phase 5).

## Später zu besprechen
1. Herstellungsweg der GT-Materialien (GT-Maschinen nachbauen: Plattenbieger, Hochofen, Implosionskompressor,
   Assembler, Canning Machine, Elektrolyseur, Zentrifuge, Industrial Grinder – oder einfachere Wege) und GT-Erze.
2. Massefabrikator (im Pack entfernt, dort GT Matter Fabricator).
3. Balancing-Vorschlag (`docs/balancing.md` §5).
4. GT-Nicht-Rezept-Änderungen (Upgrade-Stack 4, Schrottbox-Drops, Recycler-Blacklist).
5. Iridiumerz-Generierung.

## Phase 1 – Abschluss
- Durchsicht erledigt: veraltete „entfällt/nicht im Umfang“-Stellen korrigiert, neue Markierung [GT+M] in `08-rezepte.md`.

Phase 2 gestartet (2026-10-14) – Zwischenstand siehe unten.

# Stand Phase 2 (2026-10-14, Zwischenstand)

## Erledigt
- Gradle-Projekt aus offiziellem MDK (`NeoForgeMDKs/MDK-1.21.1-ModDevGradle`): NeoForge **21.1.259**,
  ModDevGradle 2.0.148, Gradle 9.2.1, Parchment 1.21.1/2024.11.17. `gradle.properties` setzt
  `org.gradle.java.home` auf `tools/jdk-21.0.12.1+1`. Mod-ID `craft`, Basispaket `dev.craft`, Version 0.2.0.
- Minimale Hauptklasse `src/main/java/dev/craft/Craft.java`; `gradlew build` erfolgreich (erster Lauf ca. 3,5 min).
- git initialisiert (Branch `main`), Remote `origin` = https://github.com/iTedde/Minecraft-Mod.git (privat, leer).
  `.gitignore` schließt `reference/`, `tools/`, `ftb ultimate/` aus (Fremdcode/Binärdateien). Ein lokaler Commit,
  **noch nicht gepusht** (Push erst nach Rückfrage). Kein globaler git-User: Commits mit `-c user.name/-c user.email`.

## Erledigt (Fortsetzung, 2026-10-14)
- **EU-Netz** `dev.craft.core.energy`: Schnittstellen `EnergyTile/Emitter/Acceptor/Source/Sink/Conductor`,
  `EnergyNet` 1:1 nach IC2 1.115 (Pfadsuche, Cache-Invalidierung, Paketverteilung, Stromschlag, Kabelschmelze,
  `getTotalEnergyEmitted/Sunken`), `EnergyNets` (ein Netz je ServerLevel, Anmeldung in `onLoad`, Abmeldung in
  `setRemoved`/`onChunkUnloaded`, Stromschlagschaden am Tick-Anfang, Damage-Type `craft:electricity`).
- **Kabel** `dev.craft.ic2.cable`: 13 Blöcke (`CableType`), BlockEntity mit Farbe/Schaumfarbe/Aktiv-Zustand,
  Verbindungen + Schaum im Blockstate, IC2-Kollisionsbox, Färben (`changeColor`), Isolieren/Abisolieren
  (`CableBlock.changeType`), Bauschaum (nass/hart, Aushärten, Sand), Detektor- und Splitterkabel, Schmelzen mit
  Zisch-Sound/Rauch. Dynamisches Modell `craft:cable`, Texturen aus dem IC2-Spritesheet.
- **Explosion** `dev.craft.core.explosion.Ic2Explosion` 1:1 (inkl. Originaleigenheiten), `explodeMachineAt`.
- **Multiblock-System** `dev.craft.core.multiblock`: `MultiblockPattern` (Zeichen-Layout, Y-Rotation, Matcher),
  `BlockMatcher`, `MultiblockController` (Validierung im Intervall/auf Anforderung, Listener).
- Datagen: Blockstates, Modelle, Item-Modelle, Lang (en_us aus `IC2.lang`, de_de), Loot, Damage-Type, Block-Tag.
- Tests: JUnit (`gradlew test`, 15 Tests: EU-Netz, Multiblock-Muster) und GameTests
  (`gradlew runGameTestServer`, 10 Tests: Kabel, Energiefluss, Schmelzen, Detektor, Explosion).
- Spec-Korrekturen: Kupferkabel-Beispiel (10 Blöcke → 9 EU wegen `double`-Summe), Kabelfarben 0–15 (schwarzer
  Farbroller = 0), effektive IC2-Explosionsresistenzen (36/18).
- `gradlew build` und `runClient` (Hauptmenü, keine Fehler) erfolgreich.

## Offen / Hinweise
- Werkzeuge für Kabel (Isolierungsschneider, Farbroller, Bauschaum-Sprüher) kommen mit den Items (später);
  die Kabel-API dafür steht.
- Kabel-Texturen sind aus dem Original-IC2-Jar geschnitten (privates Projekt) – endgültige Texturen in Phase 11 (damals Phase 10).
- Elektrische Items (Ladung, §5 der EU-Spec) kommen mit Phase 3 (Batterien).

## Entscheidungen nach Phase 2 (2026-10-14)
- Phase 2 abgenommen.
- Explosions-Versatz bei negativen Koordinaten (IC2 `(int)`-Abschneiden) bleibt wie im Original.
- Original-IC2-Texturen dürfen im (privaten) Repo liegen.

# Stand Phase 3 (2026-10-15, Zwischenstand – baut, Tests grün)

## Entscheidungen (Phase 3)
- GT-Materialien über **1:1 nachgebaute GT-Maschinen** (neue Phase 6b). Solarpanel, Wasser- und Windmühle bis dahin
  **ohne Rezept** (Blöcke funktionieren, Creative). Weg im Pack: `docs/spec/12-gregtech-materialien.md` §4.
- Rohmetall (Eisen/Gold/Kupfer) → Macerator → 2 Staub; Erzblock ebenso. (Tiefenschiefer-)Kupfererz droppt **1**
  Rohkupfer (Loot-Tabelle überschreiben). Zinnerz droppt sich selbst. → `abweichungen.md`.
- Phase 3 enthält alle Vorprodukte (Materialien, Eisenofen, Gummibaum, Zinnerz + Weltgen, Baumzapfer,
  Schraubenschlüssel, Zellen, Energie-/Lapotronkristall, Upgrades inkl. 10k-Kühlzelle als Item).
  Transformer-Upgrade-Rezept erst mit MV-Trafo (Phase 6).

## Erledigt (2026-10-15)
- Code aus dem 14.10. vervollständigt; `gradlew build`, `runGameTestServer` (26 Tests grün) und `runClient`
  (Hauptmenü, keine Mod-Fehler) erfolgreich.
- Registrierung: `Ic2Blocks` (12 Maschinen inkl. BatBox, Maschinenblock, Zinnerz + Tiefenschiefer-Zinnerz, Zinn-/
  Bronzeblock, Gummiholz/-blätter/-setzling), `Ic2Items` (Materialien, Zellen, 10k-Kühlzelle, Upgrades, Batterien,
  Baumzapfer, Schraubenschlüssel), `Ic2BlockEntityTypes.get(machine)`, `Ic2Menus` (Menütyp je Maschine), `Ic2Sounds`,
  `Ic2Features` (Gummibaum), Capabilities (ItemHandler je Seite, FluidHandler Geothermie), Creative-Tab.
- `block/`: `RubberWoodBlock` (RESIN none/natural/wet/dry + FACING), `RubberLeavesBlock`, `RubberSaplingBlock`,
  `RubberTree` (`WorldGenRubTree.grow` 1:1); `world/RubberTreeFeature`.
- Menüs `GeneratorMenu` (alle 5 Generatoren), `ElectricMachineMenu`, `IronFurnaceMenu`, `ElectricStorageMenu`
  (Redstone-Knopf über Vanilla-Button-Paket), Slots `MachineSlot`/`FilteredSlot`/`OutputSlot` (Ofen-XP wie 1.4.7).
- Client: Screens mit IC2-GUI-PNGs und Koordinaten aus `Gui*.java`, `MachineSoundHandler` (Block-Events, Loops),
  Birkenfarbe der Blätter, Batterie-Ladestufen-Property `craft:charge`.
- Datagen: Blockstates/Modelle (je Facing/Aktiv eigenes Modell nach `sideAndFacingToSpriteOffset`), Item-Modelle,
  Lang en/de, Loot (Generator-/Maschinenblock-Drops, Gummiholz 1/6 Harz, Blätter 1/35 Setzling, Vanilla-Kupfererz
  1 Rohkupfer), Block-/Item-/Biom-Tags, Rezepte (§1–5, 7, 9, 10 soweit Items vorhanden), Brennwerte (Data Map),
  Sound-Definitionen, Weltgen (Zinn, Kupfer mit IC2-Häufigkeit, Gummibäume).
- Texturen aus den IC2-Spritesheets geschnitten (Maschinen 12 je Maschine, Items, Gummibaum, Erze, Blöcke),
  Sounds kopiert. Abweichungen in `docs/abweichungen.md` eingetragen.

## Erledigt (2026-10-15, Teilpaket „Phase 3: Abschluss“)
- Harz (`ResinItem`): Rechtsklick auf die Vorderseite eines eingefahrenen Kolbens → Klebekolben; Rechtsklick auf die
  Oberseite eines undurchsichtigen Blocks → Harzschicht (`ResinSheetBlock`: 2 px, keine Kollision, bremst Entities,
  80 % Harz-Drop, bricht ohne Unterlage).
- Schrottbox (`ScrapBoxItem`) per Rechtsklick und Werfer; Drops als Loot-Tabelle `craft:gameplay/scrap_box`
  (`CraftScrapBoxLoot`).
- **Korrektur/Entscheidung:** GT ersetzt die IC2-Schrottbox-Liste (leert sie, Schrott 200 + GT-Liste). Nutzer hat
  „wie im Pack“ gewählt. Liste vollständig in `05-items.md` §1.1; GT-Einträge kommen mit Phase 6b.
- `gradlew build`, 30 GameTests grün, `runClient` bis Hauptmenü ohne Mod-Fehler.

## Abnahme Phase 3
Phase 3 am 2026-10-15 vom Nutzer abgenommen (manuelle Tests im Client macht der Nutzer gesammelt am Ende).
Wasser-/Windmühle/Solar bleiben ohne Rezept (Phase 6b).

## Offen / später
- Alt-Taste beim Schraubenschlüssel (zyklisches Drehen) kommt mit dem Tastensystem (Modus/Boost/Alt).
- Compressor+Pumpe. Schrottbox: GT-Items der Liste (`05-items.md` §1.1) mit Phase 6b, Zinndosen mit der Canning Machine.
- Upgrade-Stapelgröße 64 (IC2) – GT-Wert 4 ist offene Entscheidung 4.
- Spec-Korrektur `01-eu-system.md` §4: bei `explodeMachineAt` wird das Inventar **gedroppt** (breakBlock), nicht
  vernichtet.

# Stand Phase 4 (2026-10-15)

## Erledigt (Teilpaket „Phase 4: MJ-System + Engines“)
- Paket `dev.craft.buildcraft`: `BuildCraft` (Init), `BcBlocks`, `BcItems`, `BcBlockEntityTypes`, `BcMenus`,
  Creative-Tab „BuildCraft“.
- **MJ-System** `power/`: `PowerProvider` 1:1 (Pneumatic-Framework → Latenz 0, −1 MJ alle 100 Ticks,
  `useEnergy`/`receiveEnergy`/Stromquellen-Marker), `PowerReceptor`, `SafeTimeTracker`, Block-Capability
  `BcCapabilities.POWER_RECEPTOR` (`craft:power_receptor`). Spätere Empfänger (Quarry, Pumpe …) registrieren diese
  Capability und rufen `provider.update(this, level)` jeden Tick.
- **Engines** `engine/`: `EngineBlockEntity` (TileEngine/Engine 1:1: Kolbenhub, Übergabe bei 0,5, Energiestufen,
  Explosion mit Feuer, Verlust ohne Redstone, Ketten mit 95 %), `RedstoneEngineBlockEntity`,
  `StirlingEngineBlockEntity` (Ofen-Brennwerte), `CombustionEngineBlockEntity` (Tanks 10 000 mB, Hitze, Kühlung,
  Abkühlstrafe 600, Eis), `CombustionFuels` (Lava 1 MJ/t × 20 000; Wasser als Kühlmittel; **Öl/Treibstoff kommen mit
  den Flüssigkeiten**), `EngineBlock` (Ausrichtung als `FACING`, BC-Schraubenschlüssel dreht, Redstone-Partikel).
- Items: Holz-/Stein-/Eisen-/Gold-/Diamant-Zahnrad, BC-Schraubenschlüssel (`craft:bc_wrench`); Rezepte §12
  (Vanilla-Shaped, spiegelbar). Capabilities: Item-Slot (Stirling/Combustion), Fluid (Combustion, nur füllen).
- Client: `EngineRenderer` (BER) + `EngineItemRenderer` mit Original-Geometrie/-Texturen, `EngineScreen`
  (Stirling-/Combustion-GUI inkl. Flüssigkeitsanzeige und aufklappbarem Engine-Ledger).
- 7 neue GameTests (insgesamt 37 grün), `gradlew build` und `runClient` (Hauptmenü, keine Mod-Fehler) erfolgreich.
- Doku: Eigenheiten in `09-buildcraft.md` §1.2, Abweichungen (Zeitbasis `getGameTime`, Capability, Darstellung).

## Erledigt (Teilpaket „Phase 4: Öl + Flüssigkeiten“, 2026-10-15)
- `buildcraft/fluid/BcFluids`: FluidTypes + Fluids `craft:oil`/`flowing_oil` (fließt wie 1.4.7-Wasser: 5 Ticks,
  7 Blöcke, Gefälle 4, **keine** neuen Quellen; Wasser-Eigenschaften für Entities) und `craft:fuel`/`flowing_fuel`
  (ohne Block). Lava neben Öl erstarrt (Quelle → Obsidian, Stufe ≤ 4 → Bruchstein) über `FluidInteractionRegistry`.
- Block `craft:oil` (LiquidBlock, Härte 100), Öleimer (BucketItem, Stack 1), Treibstoffeimer (`FuelBucketItem`,
  nicht ausleerbar, FluidBucketWrapper-Capability). Combustion Engine: Öl 3 MJ/t × 20 000, Treibstoff 6 MJ/t × 100 000.
- Weltgen `buildcraft/world/OilFeature` 1:1 nach `OilPopulate` (Wüstenpfützen, mittlere/große Vorkommen, Steigrohr,
  Oberflächenseen, Fontänen). **Entscheidungen:** Kugel y 20–29 absolut, Suche ab Geländeoberkante statt y 128;
  Blöcke außerhalb der 3×3 Nachbarchunks werden übersprungen. Biome-Modifier `craft:add_oil`, Schritt
  `top_layer_modification`.
- Texturen: Öl/Treibstoff mit den Original-FX-Algorithmen als Animationsstreifen gerendert
  (`tools/scripts/BcLiquidTextures.java`), Eimer-Icons aus `items.png` (1, 3). Client: Fluid-Extensions, Wassernebel
  unter Öl. Lang en/de.
- 6 neue GameTests (`OilGameTests`, insgesamt 43 grün), `gradlew build` und `runClient` (Hauptmenü, keine
  Mod-Fehler) erfolgreich. Doku: `abweichungen.md` (Öl-Weltgen, Öl als „Wasser“, Texturen, Treibstoff), Spec 07 §5,
  09 §2.

## Erledigt (Teilpaket „Phase 4: Landmarks + Quarry“, 2026-10-15)
- Neue Regeln in `CLAUDE.md` (8–11: nicht neu dekompilieren, gezielt greppen, `docs/spec/` ist Quelle der Wahrheit,
  Compact-Solars-Werte). Fehlende Details zu Landmark/Quarry in `09-buildcraft.md` §3.1 und §4.4 nachgetragen.
- `buildcraft/area/` (`AreaProvider`, `Box`), `buildcraft/util/BlockUtil` (`canChangeBlock`, `isSoftBlock`).
- **Landmark** `builders/MarkerBlock` + `MarkerBlockEntity`: Halt-Suche wie `onBlockAdded`, Verknüpfen per
  Rechtsklick (geteiltes `Origin`), Box als `AreaProvider`, Signallaser bei Redstone, Auflösen beim Abbau,
  Speichern/Neu-Verknüpfen über den Ursprung, `removeFromWorld` droppt alle.
- **Quarry** `factory/QuarryBlock` + `QuarryBlockEntity`: Bereich (Landmarks oder 11×5×11 hinter der Quarry,
  Größenprüfung 25), Chunkloading (`QuarryChunkLoading`, TicketController), Rahmenbau über `FrameBlueprint`
  (Räum-/Bauliste) und `BuilderRobot` (Warteschlange 20, Wartezeit, ewiges Hängen an Lava/Öl/Bedrock), Abbau
  (Serpentine bis Weltboden, 60 MJ + Kopfbewegung, Drops ohne Fortune, Item-Sog, Ausgabe zufälliges Nachbarinventar
  → Item-Entity mit Lebensdauer 1200), BC-Schraubenschlüssel = `reinitalize`, Rahmen zerfallen nach Abbau.
- **Rahmen** `factory/FrameBlock` (Rohrform, kein Drop, Zerfall 4/10 per Random-Tick).
- Client: `BlockBoxRenderer` (EntityBlock-Kacheln), `MarkerRenderer` (blau/rot), `QuarryRenderer` (Streifen,
  Roboter + Energielaser 1–4 mit Leistungsmittel, Bohrarm 1/32-gerastert). Texturen aus `blocks.png`/`entities/`.
- Datagen: Blockstates/Modelle (Landmark gedreht, Quarry orientierbar, Rahmen Multipart), Item-Modelle, Loot, Tag,
  Lang en/de inkl. Chatmeldungen, Rezept Landmark. **Quarry-Rezept erst mit dem Diamantbohrer** (Phase 6/6b).
- Neue GameTest-Struktur `empty_16x8x16`; 3 neue GameTests (`QuarryGameTests`, insgesamt 46 grün), `gradlew build`.
- Doku: `abweichungen.md` (Entities → BER, Chunkloading, Tiefe bis −64, Ausgabe, weiche Blöcke, Helligkeit,
  Landmark-Modell, Zerfallsrate, Rezept).

## Erledigt (Teilpaket „Phase 4: Tank/Pumpe/Raffinerie/Mining Well“, 2026-10-15)
- Fehlende Details aus dem Original einmalig in `09-buildcraft.md` §7.5 nachgetragen (Tank, Pumpe, Raffinerie inkl.
  GUI/Animation, Mining Well, Minenrohr).
- `fluid/LiquidContainers` (Ersatz für `LiquidContainerRegistry` + `Utils.consumeItem`, jetzt auch von der
  Combustion Engine genutzt), `util/ItemOutput` (Quarry-Ausgabe, gemeinsam mit dem Mining Well).
- **Tank** `factory/TankBlock` + `TankBlockEntity`: 16 000 mB, Stapel-Verbund (Füllen von unten, Entnahme unten,
  Abgabe nach unten pro Tick), Eimer/Zellen per Rechtsklick, Blockstate `joined` (Seitentextur 97).
- **Pumpe** `PumpBlock` + `PumpBlockEntity`: Rohr senkt sich 0,01/Tick, Flood-Fill (BlockIndex-Reihenfolge, oberste
  Schicht, `getLast`/`pop`-Eigenheit), 10 MJ je 1000 mB, Abgabe an Nachbar-Fluid-Handler.
- **Raffinerie** `RefineryBlock` + `RefineryBlockEntity` + `RefineryRecipe` (Öl → Treibstoff, 12 MJ/mB): drei Tanks,
  Filter, Animation, BC-Schraubenschlüssel dreht; `RefineryMenu` + `RefineryScreen` (Filter per Klick mit Behälter).
- **Mining Well** `MiningWellBlock` + `MiningWellBlockEntity` (25 MJ/Block, bohrt bis Bedrock/Lava/Öl, Abbau entfernt
  den Rohrstrang), **Minenrohr** `MiningPipeBlock` (ohne Item).
- Client: `TankRenderer`, `PumpRenderer` (Rohr), `RefineryRenderer` + `RefineryItemRenderer` (Original-Modell),
  `FluidBoxRenderer`. Texturen aus `blocks.png` (32, 35–38, 96–102), `refinery.png`, `refinery_filter.png`.
- Datagen: Blockstates/Modelle, Item-Modelle, Loot, Tags (Spitzhacke/Schaufel), Lang en/de (BC-Namen), Rezepte
  Tank, Mining Well (nur unbeschädigte Eisenspitzhacke wie 1.4.7), Pumpe, Raffinerie.
- 5 neue GameTests (`FactoryGameTests`, insgesamt 51 grün), `gradlew build` und `runClient` (Hauptmenü, keine
  Mod-Fehler) erfolgreich. Abweichungen in `abweichungen.md` eingetragen.
- **Damit ist Phase 4 inhaltlich vollständig** – wartet auf Abnahme durch den Nutzer.

## Nächste Schritte (Stand Ende Phase 4 – überholt, siehe Phase 5)
Arbeitsweise: eine Sitzung pro Teilpaket, Übergabe über diese Datei; am Ende jedes Teilpakets committen und pushen.
1. Manuelle Client-Tests macht der Nutzer gesammelt, wenn alles fertig ist (Engines: Animation/Farben, Drehen per
   BC-Schraubenschlüssel, GUIs + Ledger, Explosion, Kette Engine → Engine; **Öl:** Ölquellen in neuer Welt finden
   (Wüste/Fontänen, keine Fehler „far chunk“ im Log), Öl fließen lassen/aufnehmen, Schwimmen/Nebel im Öl,
   Öl-/Treibstoffeimer in der Combustion Engine; **Landmarks/Quarry:** Landmarks setzen (Wand/Boden/Decke),
   verknüpfen, Signallaser mit Redstone, rote Box; Quarry daneben (Creative) → Streifenlaser, Roboter mit
   Energielaser, Rahmenbau, Bohrarm, Abbau in Truhe/als Item-Fontäne; Quarry ohne Landmarks (Bereich hinter der
   Front); Neuladen der Welt mitten im Abbau; Quarry abbauen → Rahmen zerfallen; **Tank/Pumpe/Raffinerie/Mining
   Well:** Tanks stapeln, mit Eimern füllen/leeren, Flüssigkeitsstand; Pumpe über Wasser/Lava/Öl mit Engine → Rohr
   senkt sich, Quellen verschwinden, Tank darüber füllt sich; Raffinerie mit Öl + Engine → Magnete laufen,
   Treibstoff entnehmen (Eimer geht nicht – nur über Behälter-Handler/Pumpe), Filter in der GUI setzen, Drehen mit
   BC-Schraubenschlüssel; Mining Well bohrt mit Engine bis Bedrock, Drops in Truhe, Abbau entfernt Rohre).
2. Abnahme Phase 4 durch den Nutzer.
3. Danach Phase 5 (Electrical Engine inkl. Thermionic Fabricator, Carpenter, Platinen, Röhren, Power Converters) –
   erstes Teilpaket festlegen.

## Abnahme Phase 4
Phase 4 am 2026-10-15 vom Nutzer abgenommen (manuelle Tests gesammelt am Ende). Aufteilung Phase 5 festgelegt:
5a Fabricator/Carpenter/Röhren/Platinen/Lötkolben, 5b Electrical Engine inkl. Upgrades, 5c Power Converters.

# Stand Phase 5 (2026-10-15)

## Erledigt (Teilpaket „Phase 5a: Forestry-Werkstatt“)
- Fehlende Details aus dem Original einmalig in `10-energieumwandlung.md` §A.7 nachgetragen (Arbeitstakt, Block,
  TESR-Geometrie, Slots/GUIs, Fabricator-/Carpenter-Ablauf, Platinen, Schaltungen, Röhren, Lötkolben).
- Paket `dev.craft.forestry`: `Forestry` (Init, Capabilities), `FrBlocks`, `FrItems`, `FrBlockEntityTypes`,
  `FrMenus`, `FrFluids` (Flüssigglas, ohne Block/Eimer), `FrDataComponents` (`craft:circuit_board`), `FrRecipes`,
  Creative-Tab „Forestry“.
- `circuit/`: `CircuitBoardType` (Sockel, Farben), `Circuit` (Choke/Boost I/Boost II/Efficiency mit EU-/MJ-Änderung
  und Limit – **für 5b**: Engine liest `CircuitBoard` aus dem Platinen-Item), `CircuitLayout` (`engine_tin`),
  `CircuitBoard` (Record), `ElectronTube` (Röhre → Schaltung).
- `machine/`: `ForestryMachineBlockEntity` (TileMachine: PowerProvider, Arbeitsschritt alle 5 Ticks = 25 MJ),
  `ForestryMachineBlock` (Ausrichtung zum Spieler, Drops), `FabricatorBlockEntity` (Hitze, Schmelzen, Erstarren,
  Herstellen aus Vorrat/Raster, Automatisierung), `CarpenterBlockEntity` (Behälter alle 20 Ticks, Rezeptsuche 40,
  Auftrag/ausstehendes Produkt, Tank nimmt jede Flüssigkeit, Automatisierung inkl. Kisten-Eigenheit), `ItemSets`.
- `recipe/`: `FabricatorRecipe`, `CarpenterRecipe` (Datapack-Rezepttypen, Spiegeln erlaubt), `FabricatorSmelting`.
- `menu/`: `ForestryMenu` (Phantom-Klicks, Shift-Klick), `PhantomSlot`, `FabricatorMenu` (Ergebnis-Slot),
  `CarpenterMenu` (Rezeptanzeige), `SolderingIronMenu` (Löten, Layoutwahl, Drop beim Schließen).
- Items: Robustes Gehäuse, Lötkolben, 4 Röhren, 4 Platinen (zweifarbig über Item-Tints).
- Client: `ForestryMachineRenderer` (+ Item-Renderer), Screens für Fabricator/Carpenter/Lötkolben, Flüssigglas-Textur
  (FX gerendert, `tools/scripts/ForestryTextures.java`), Texturen/GUIs aus dem Forestry-Jar.
- Datagen: Blockstates, Item-Modelle, Loot, Spitzhacken-Tag, Lang en/de (Forestry-de_DE, fehlende Einträge englisch),
  Rezepte (Gehäuse, Fabricator, Carpenter; 4 Röhren im Fabricator; 4 Platinen + Lötkolben im Carpenter).
- 5 neue GameTests (`ForestryGameTests`, insgesamt 56 grün), `gradlew build` und `runClient` (Hauptmenü, keine
  Mod-Fehler) erfolgreich. Abweichungen in `abweichungen.md` eingetragen.


## Erledigt (27.1 GraviSuite: Bauteile, LapPacks, Rezepte, 2026-10-24)
- [x] Paket `dev.craft.gravisuite`: `GraviSuite` (Init, Einträge im IC2-Kreativtab, elektrische Teile geladen + leer),
  `GsItems` (7 Bauteile, Rüstungsmaterialien je Rüstungstextur), Fortg. LapPack (1 000 000 EU, Transfer 1 000) und
  Ultimatives LapPack (10 000 000 EU, Transfer 20 000) als `EnergyPackItem` (Tier 2, liefert Energie, kein Schutz).
- [x] Rezepte der vorhandenen Items (spec 28 §10): Supraleiter-Hülle/Supraleiter (Vanilla), Magnetron (Tags Raff.
  Eisen/Kupfer), sonst IC2-Rezepte mit Ladungsübertrag; Kühlzelle/Wärmetauscher/Wärmeableiter nur unbenutzt.
  Rezepte für Jetpack, Brustplatten, Werkzeuge folgen mit ihren Items (27.2–27.5).
- [x] Texturen aus `gravisuite.zip` (alle 20 Item-Sprites nach `item/gravisuite/`, 6 Rüstungstexturen; Skript
  `tools/scripts/gravisuite_textures.py`), Lang DE/EN.
- [x] HUD-Grundgerüst `gravisuite/client/GraviSuiteHud`: oben rechts „Energy level: n%“ beim Tragen eines LapPacks
  (nur „n%“ eingefärbt wie im Pack), Statuszeilen folgen in 27.2/27.3.
- [x] GameTests `GraviSuiteGameTests` (5: Werte, Ladungsübertrag mit Deckel 1 000 000, Summe 6 300 000 aus
  Kristallen + LapPack, Bauteil-Rezepte inkl. benutzter Kühlzelle, Laden von Werkzeugen aus beiden LapPacks).
  Alle 460 grün, `gradlew runData build` grün.
- Ore-Dict `itemSuperconductor` nicht als Tag nachgebaut (kein Rezept im Mod nutzt es).
- **Client-Test offen.**

## Nächste Schritte
- 27.2 Fortg. Jetpack + Fortg. Nano-Brustplatte (Triebwerk, Schweben, Boost, Flug-Taste, Payloads, Sound, HUD-Status).
