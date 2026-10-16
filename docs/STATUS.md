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

## Nächste Schritte
1. Teilpaket 5b: Electrical Engine (`EngineTin`, §A.1–A.4): EU-Senke im EU-Netz, Kolben/Übertragung wie BC-Engine,
   Hitze/Zwangsabkühlung, Batterie-Slot, Platinen-Sockel mit Schaltungen (Data Component), Zinn-Zahnrad + Rezept,
   GUI `electricalengine.png`, Darstellung `engine_tin_*`.
2. Danach 5c: Power Converters.
3. Manuelle Client-Tests (gesammelt am Ende) zusätzlich: Fabricator (Glas/Sand schmelzen mit Engine, Hitzebalken,
   Röhren herstellen per Klick/Shift-Klick, Erstarren ohne Energie), Carpenter (Wassereimer, Muster per Phantom-Klick,
   Fortschrittsbalken, Tankstufen-Textur), Lötkolben (Röhren auf Platine löten, Layout-Knöpfe, Drop beim Schließen),
   Darstellung/Ausrichtung beider Maschinen in Welt und Inventar.

## Erledigt (Teilpaket „Phase 5b: Electrical Engine“, 2026-10-15)
- Lücken aus dem Original einmalig in `10-energieumwandlung.md` §A.8 nachgetragen (eigenes `extractEnergy` ohne
  `ceiling < min`, genaue Tick-Reihenfolge, MJ-Eingang 10/200/100 000, Sockel-Klicks inkl. Platinenverlust beim
  Ersetzen/Abbau, Block 3,5, Ausrichtung, Biodust, Renderer-Geometrie/Texturen, GUI-Maße).
- Paket `dev.craft.forestry.engine`: `ElectricalEngineBlockEntity` (EU-Senke ohne Überspannung, MJ-Empfänger mit
  95 %-Ketten, Kolben/Übertragung, Hitze und Zwangsabkühlung, Batterie, Platinen-Sockel), `ElectricalEngineBlock`
  (Schraubenschlüssel dreht, Partikel), `TemperatureState`; Menü `ElectricalEngineMenu` (Batterie-Slot, Sockel),
  `ElectricalEngineScreen` (EU-Anzeige mit Füllstufen), `ElectricalEngineRenderer` (+ Item im BEWLR).
- Zinn-Zahnrad (`tin_gear`) + Rezept, Engine-Rezept, Texturen/GUI aus dem Forestry-Jar, Lang en/de
  (Forestry-de_DE), Loot, Spitzhacken-Tag, Creative-Tab.
- 7 neue GameTests (`ElectricalEngineGameTests`, insgesamt 63 grün), `gradlew build` und `runClient` (Hauptmenü,
  keine Mod-Fehler/-Warnungen) erfolgreich. Abweichungen in `abweichungen.md`.
- Repo-Fix: `.gitignore` schloss mit `tools/` auch `assets/craft/sounds/tools/` aus (Baumzapfer-/Schraubenschlüssel-
  Sound fehlten im Klon, Datagen schlug fehl) → Regeln auf Repo-Wurzel verankert (`/tools/`, `/reference/`,
  `/ftb ultimate/`), Sounds aus `IC2.jar` wiederhergestellt.
- Umzug auf neuen Rechner (`X:/Craft/Minecraft-Mod`): JDK-Pfad in `gradle.properties` zeigt noch auf
  `C:/bitbucket/...`; lokal überschrieben in `~/.gradle/gradle.properties` (System-JDK 21). `reference/decompiled`
  vom Nutzer ohne MCP-Remap neu erzeugt (Vanilla-Namen verschleiert); `reference/jars|config`, `tools/` fehlen.

## Nächste Schritte
1. Teilpaket 5c: Power Converters (§B).
2. Manuelle Client-Tests zusätzlich: Electrical Engine (Platzieren neben Carpenter/Fabricator → Ausrichtung,
   Redstone + Kabel/BatBox → Kolben, Partikel, Stammfarbe nach Hitze, Zwangsabkühlung; GUI: EU-Anzeige,
   Batterie-Slot, Platine per Klick einsetzen/mit Lötkolben entnehmen, Tooltip leerer Sockel; Item-Darstellung).

## Erledigt (Teilpaket „Phase 5c: Power Converters“, 2026-10-15)
- Lücken aus dem Original einmalig in `10-energieumwandlung.md` §B.8 nachgetragen (Blöcke/Texturindizes,
  Bauteil-Cache, Consumer/Producer-Abläufe inkl. Eigenheiten, Bridge-Tick und „Input limited“, GUI-Maße,
  Charger-Ablauf). **Spec-Korrektur §B.2:** der IC2 Consumer explodiert **nicht** bei Überspannung (IC2 prüft
  `getMaxSafeInput` nicht im Netz).
- Paket `dev.craft.powerconverters`: `PowerConverters` (Init, MJ-Capability), `PcBlocks`, `PcItems`,
  `PcBlockEntityTypes`, `PcMenus`, `PowerSystem` (BC 4375 / IC2 1800 PCU, Spannungen), `ConverterType`,
  Creative-Tab „Power Converters“.
- `power/`: `BridgeComponentBlockEntity` (Nachbar-Cache, Seiten-Blockstate), `EnergyConsumer/ProducerBlockEntity`,
  `EnergyBridgeBlockEntity` (16 000 000 PCU), `Ic2Consumer/ProducerBlockEntity` (LV–EV, nur volle Pakete),
  `BcConsumer/ProducerBlockEntity`, `ChargerBlockEntity` (Spieler darauf bzw. angrenzende Inventare).
- `block/`: `PowerConverterBlock` (6 Seiten-Eigenschaften, GUI der ersten Bridge), `ChargerBlock`
  (Kollision 14 px), `EnergyBridgeBlock`; `menu/EnergyBridgeMenu`, `client/EnergyBridgeScreen`.
- Texturen aus `terrain_0.png` geschnitten (`textures/block/power_converter/`), GUI `energy_bridge.png`.
- Datagen: Multipart-Blockstates (Einzelflächen-Modelle je Seite), Item-Modelle, Loot, Lang en/de (nur englische
  Namen wie im Pack), Rezepte Energy Bridge, Charger, BC Consumer, formlose Umwandlungen.
  **IC2-Consumer-Rezepte erst mit Trafos/MFSU (Phase 6).**
- 6 neue GameTests (`PowerConvertersGameTests`, insgesamt 69 grün), `gradlew build` und `runClient` (Hauptmenü,
  keine Mod-Fehler) erfolgreich. Abweichungen in `abweichungen.md`.
- **Damit ist Phase 5 inhaltlich vollständig** – wartet auf Abnahme durch den Nutzer.

## Nächste Schritte
1. Abnahme Phase 5 durch den Nutzer.
2. Manuelle Client-Tests zusätzlich: Power Converters (Bridge + IC2 Consumer an Kabel/Generator → GUI zeigt
   „IN“ und EU/t, Ladebalken; IC2 Producer an Maschine/BatBox → nur volle Pakete; BC Consumer mit Engine,
   BC Producer an Quarry/Pumpe; verbundene Seiten-Texturen; Charger: draufstellen lädt Batterien im Inventar,
   Truhe daneben; Rechtsklick auf Bauteil öffnet Bridge-GUI; formlose Umwandlungsrezepte).
3. Danach Phase 6 (IC2 Mid/Late Game) – erstes Teilpaket festlegen; IC2-Consumer-Rezepte dort nachziehen.

## Abnahme Phase 5
Phase 5 am 2026-10-15 vom Nutzer abgenommen (manuelle Tests gesammelt am Ende). Aufteilung Phase 6 festgelegt:
6.1 MFE, MFSU, LV-/MV-/HV-Trafo (+ Rezepte Transformer-Upgrade und IC2 Consumer) · 6.2 Elektrowerkzeuge +
Tastensystem (Modus/Boost/Alt) · 6.3 Nano-, Quanten- und Utility-Rüstung · 6.4 Massefabrikator + Solar-Arrays als
Multiblock (vorher offene Entscheidungen 2 und 3 klären).

# Stand Phase 6 (2026-10-15)

## Erledigt (Teilpaket „Phase 6.1: Speicher und Trafos“)
- `Ic2Machine`: MFE, MFSU, LV-/MV-/HV-Trafo (Härte 1,5, Facing auch oben/unten, Wrench-Rate 1,0; Drops: LV-Trafo
  sich selbst, sonst Maschinenblock; Rarity „uncommon“ für MFSU/HV-Trafo). Neu: `getRarity()`, `hasMenu()`
  (Trafos ohne Menü, Inventar und Item-Capability).
- `ElectricStorageBlockEntity.Spec` (BatBox 1/32/40 000, MFE 2/128/600 000, MFSU 3/512/10 000 000) ersetzt die
  BatBox-Konstanten; Menü/Screen der BatBox gelten für alle drei.
- `TransformerBlockEntity` 1:1 nach `TileEntityTransformer`: abwärts (Front rein, bis 4 Pakete `lowOutput`),
  aufwärts mit Redstone (`hasNeighborSignal`, 1 Paket `highOutput` aus der Front), Umschalten meldet im Netz ab/an,
  Überspannung explodiert (HV-Trafo abwärts unbegrenzt), Puffer bis `maxStorage + highOutput − 1`.
- Texturen aus `block_electric.png` (Spalten 1–5) geschnitten; Lang en/de.
- Rezepte: MFE, LV-/MV-/HV-Trafo, Transformer-Upgrade, IC2 Consumer LV/MV/HV/EV. **MFSU ohne Rezept bis Phase 6b**
  (fortg. Maschinenblock → fortg. Legierung → Mischmetall aus GT-Platten).
- Spec: LV-Trafo-Rezept vervollständigt (`08-rezepte.md`), Trafo-Redstone nicht gespeichert (`03-speicher-trafos.md`).
- 5 neue GameTests (`StorageGameTests`, insgesamt 74 grün), `gradlew build` und `runClient` erfolgreich.

## Nächste Schritte
1. Teilpaket 6.2: Elektrowerkzeuge (`05-items.md` §3) + Tastensystem (Modus/Boost/Alt, auch Alt beim
   Schraubenschlüssel); Diamantbohrer → Quarry-Rezept nachziehen. Vorher Spec-Lücken prüfen.
2. Manuelle Client-Tests zusätzlich: MFE/MFSU (GUI, Laden/Entladen, Redstone-Modi, Ausgabe vorne, Drehen per
   Schraubenschlüssel inkl. oben/unten), Trafos (Abwärts: HV-Trafo zwischen MFSU-Ausgang und MFE; Aufwärts mit
   Hebel → aktive Textur, Ausgabe vorne; Überspannung mit falsch gedrehtem LV-Trafo explodiert; Drops ohne
   Schraubenschlüssel).

## Erledigt (Teilpaket „Phase 6.2: Elektrowerkzeuge + Tastensystem“, 2026-10-15)
- Spec-Lücken aus dem Original einmalig in `05-items.md` §3.1 (Details aller Elektrowerkzeuge, Laserprojektil,
  Texturindizes) und §3a (Tastensystem) nachgetragen. **Spec-Korrektur:** Nanosäbel-Abbaugeschwindigkeit ist
  wirkungslos überschrieben (Spinnennetz 6,0 mit ≥ 10 EU, an und aus); Bohrer-/Säbel-Sounds existieren im Pack nicht
  → lautlos.
- **Tastensystem** `ic2/keyboard/`: `Ic2Keyboard` (Bitmaske Alt/Boost/Vorwärts/Modus/Springen/Seiteninventar als
  Spieler-Attachment, Abfragen `isAltKeyDown` …), `KeyStatePayload`; Client `Ic2KeyMappings` (L-Alt, L-Strg, M, C,
  Senden bei Änderung). Schraubenschlüssel: Alt-Taste dreht zyklisch (Schleichen rückwärts), `canTakeDamage` vor
  Drehen/Abbau.
- **Werkzeuge** `ic2/item/tool/`: `ElectricToolItem` (Basis: Abbaugeschwindigkeit/-recht per Block-Tag, Kosten je
  Block, Ladebalken, `onBlockStartBreak` über `BlockEvent.BreakEvent`), `DrillItem` (Bergbau-/Diamantbohrer),
  `ChainsawItem` (Extraschaden 9, Scheren von Blöcken/Entities), `ElectricHoeItem`, `ElectricTreetapItem`,
  `MiningLaserItem` (7 Modi) + `MiningLaserEntity` (Projektil 1:1, nicht gespeichert), `NanoSaberItem`
  (Data Component `craft:active`), `ScannerItem` (OD/OV) + `OreValues`; `ic2/item/ElectricWrenchItem` (Lossless).
- Client: `MiningLaserRenderer` (Pfeil-Geometrie, `laser.png`), `ChainsawSoundHandler` (Leerlauf-Loop/Stopp),
  Item-Property `craft:active` (Säbel flackert animiert).
- Texturen aus `item_0.png` geschnitten, Laser-Textur und Sounds (Kettensäge, Laser ×5, OD-Scanner) aus `IC2.jar`
  (`ftb ultimate/mods/IC2.jar`). Data Components `craft:laser_mode`, `craft:lossless`, `craft:active`.
- Datagen: Item-Modelle (Werkzeuge handheld), Block-Tags `craft:mineable/{mining_drill,chainsaw,electric_hoe}`,
  `craft:chainsaw_shearable`, Lang en/de (Namen, Tasten, Meldungen), Sounds, Rezepte OD-/OV-Scanner (IC2) und
  **Quarry** (GT-Rezept mit Diamantbohrer). Übrige Werkzeug-Rezepte brauchen GT-Platten → Phase 6b.
- 8 neue GameTests (`ToolGameTests`, insgesamt 82 grün), `gradlew build` und `runClient` (Hauptmenü, keine
  Mod-Fehler) erfolgreich. Abweichungen in `abweichungen.md`.

## Nächste Schritte (überholt, siehe 6.3)
1. Teilpaket 6.3: Nano-, Quanten- und Utility-Rüstung (`05-items.md` §5.2/5.3) inkl. Nanosäbel-Rüstungsentzug,
   Jetpack (Vorwärts/Springen/Modus), Quantenrüstung (Boost/Springen) über `Ic2Keyboard`. Vorher Spec-Lücken prüfen.
2. Manuelle Client-Tests zusätzlich: Tasten im Steuerungsmenü (Kategorie IndustrialCraft 2); Bohrer/Diamantbohrer
   (Geschwindigkeit, leer = langsam, Ladebalken), Kettensäge (Holz, Blätter scheren, Schaf scheren, Extraschaden,
   Leerlauf-/Stopp-Sound), el. Hacke (Erde → Ackerboden), el. Baumzapfer, el. Schraubenschlüssel (M + Rechtsklick →
   Lossless-Meldung, Abbau einer Maschine), Schraubenschlüssel mit Alt; Bergbaulaser (alle 7 Modi, M wechselt,
   Horizontal nur auf Block, Super-Heat: Sand → Glas, Explosiv, Scatter; Projektil-Darstellung, Sounds), Nanosäbel
   (an/aus, Flackern, Schaden, Leerlaufverbrauch), OD-/OV-Scanner (Meldung, Sound).

## Erledigt (Teilpaket „Phase 6.3: Rüstung“, 2026-10-15)
- Spec-Lücken aus dem Original einmalig in `05-items.md` §5.4 nachgetragen (Forge-`ApplyArmor`/`StandardizeList`
  wortgetreu aus dem Forge-Jar des Packs, Elektro-/Utility-Rüstung, Hazmat, Jetpack, Quantum-Tick, Nanosäbel-Entzug,
  Texturindizes, Rezeptzutaten). **Originalfehler:** Jetpack-Treibstoff sinkt je Aufruf um `n + 4`; el. Jetpack-
  Leistungsabfall erst unter 1,25 EU; Nanosäbel entlädt nur Nano, nie Quantum.
- Paket `ic2/item/armor/`: `ArmorProperties` (Forge-Rechnung 1:1), `SpecialArmor`, `Ic2ArmorItem` (Basis, Tick
  über `inventoryTick`, Fall-Hook), `ElectricArmorItem`, `NanoArmorItem`, `QuantumArmorItem` (Luft, Tränke, Löschen,
  Sprint, Supersprung), `EnergyPackItem` (BatPack/LapPack), `HazmatArmorItem` (Hazmat + Gummistiefel),
  `JetpackItem`/`ElectricJetpackItem` (Schwebemodus, Flug-Kick-Reset per Access Transformer), `SolarHelmetItem`,
  `StaticBootsItem`, `NightvisionGogglesItem`, `Ic2ArmorMaterials`, `ArmorEvents` (DamageContainer-Modifikator,
  `ArmorHurtEvent`, `LivingFallEvent`).
- Strahlungseffekt `craft:radiation` (`Ic2MobEffects`) + Damage-Type (Tag `bypasses_armor`, neuer
  `CraftDamageTypeTagsProvider`). Data Components `hover_mode`, `toggle_timer`, `last_position`.
- Items Kohlefaser/-netz/-platte (+ Compressor), leerer Kanister (+ GT-Recycling Ofen/Macerator). Rezepte: Nano,
  Hazmat, Gummistiefel, Jetpack (leer), el. Jetpack, BatPack, LapPack, Solarhelm, Static Boots.
  **Ohne Rezept:** Quantum (Phase 6b), CF-Pack (kommt mit dem Bauschaumsprüher, Item noch nicht angelegt),
  Nachtsichtbrille (kein Rezept im Original).
- Nanosäbel: Rüstungsentzug umgesetzt. Client: `JetpackSoundHandler` (Loop). Texturen aus `item_0.png` und
  `armor/ic2/*`, Sprache en/de.
- 9 neue GameTests (`ArmorGameTests`, insgesamt 91 grün), `gradlew build` und `runClient` (Hauptmenü, keine
  Mod-Fehler) erfolgreich. Abweichungen in `abweichungen.md`.
- **Offen:** Quantumhelm isst Zinndosen (mit Canning Machine), Jetpack betanken (Canning Machine), CF-Pack.

## Nächste Schritte
1. Teilpaket 6.4: Massefabrikator + Solar-Arrays als Multiblock – vorher offene Entscheidungen 2 (Massefabrikator)
   und 3 (Balancing-Freigabe) mit dem Nutzer klären.
2. Manuelle Client-Tests zusätzlich: Rüstung anlegen (Modelle/Texturen aller Teile, Rüstungsbalken je Ladung),
   Nano/Quantum gegen Mobs und Fallschaden, Quantum-Sprint (Strg) und Supersprung (Strg+Leertaste), Luft unter
   Wasser, Hazmat in Lava/Feuer, Gummistiefel-Fall, Jetpack (Leertaste, M+Leertaste Schwebemodus, Vorwärts-Schub,
   Loop-Sound, kein Flug-Kick), BatPack lädt gehaltenes Werkzeug, Solarhelm/Static Boots laden das Brust-Item,
   Nachtsichtbrille Tag/Nacht, Nanosäbel gegen Spieler mit Nanorüstung.

## Erledigt (Teilpaket „Phase 6.4a: Massefabrikator“, 2026-10-15)
- Entscheidungen: Massefabrikator 1:1 mit IC2-Rezept (kein GT Matter Fabricator); Solar-Variante „Fläche der
  Panels“ freigegeben (Spec in `11-compact-solars.md` §4). Spec-Details zu `TileEntityMatter` in `04-maschinen.md`.
- `MassFabricatorBlock`/`-BlockEntity` (1 000 000 EU je UU-Materie, Puffer 1 100 000, max. 512 EU/t, Überspannung
  → Explosion 15; Verstärker Schrott 5000, Schrottbox 45 000, GT: Redstone 5000, Glowstonestaub 25 000; ×6 Energie
  bis aufgebraucht; Redstone stoppt), Menü/GUI (Fortschritt, Verstärker), Loop- und Schrott-Sound, Blockzustand
  `scrap`. Fortg. Maschinenblock (Block, Drop des Fabrikators, Rezept Phase 6b).
- Items UU-Materie, Iridiumerz, Silberstaub (Tag `c:dusts/silver`). Rezepte: Massefabrikator, MFSU (beide warten auf
  den fortg. Maschinenblock), **alle UU-Rezepte** in Pack-Fassung (GT-Mengen/-Ersetzungen) mit fester Rasterposition
  (`FixedPosition` + Mixin `CraftingInputMixin`/`GridPosition`). Wasser/Lava aus UU entfallen.
- 4 neue GameTests (`MassFabricatorGameTests`, Überspannungstest in eigenem Batch; insgesamt 95 grün),
  `gradlew build` und `runClient` (Hauptmenü, keine Mod-Fehler) erfolgreich. Abweichungen in `abweichungen.md`.

## Nächste Schritte
1. Teilpaket 6.4b: Solar-Arrays als Multiblock (LV/MV/HV-Controller) nach `11-compact-solars.md` §4.
2. Manuelle Client-Tests zusätzlich: Massefabrikator (GUI-Texte, Fortschritt, Verstärker-Anzeige, Sounds Loop/
   Schrott, Redstone-Stopp, Abbau → fortg. Maschinenblock), UU-Rezepte in der Werkbank (nur an der richtigen Stelle).

## Erledigt (Teilpaket „Phase 6.4b: Solar-Arrays als Multiblock“, 2026-10-15)
- Paket `dev.craft.compactsolars`: `CompactSolars` (Init, Item-Capability), `CsBlocks`, `CsItems`,
  `CsBlockEntityTypes`, `CsMenus`, `SolarArrayType` (LV/MV/HV: 8/64/512, Tier 1–3), `SolarArrayPatterns`
  (rekursive Layouts 3×3/9×9/27×27), `SolarArrayBlock`, `SolarArrayBlockEntity`, `SolarArrayMenu`;
  Client `SolarArrayScreen` (IC2-Solarpanel-GUI, Sonnensymbol solange geliefert wird), Creative-Tab „Compact Solars“.
- Core: `MultiblockController.Listener.onChecked` (nach jeder Prüfung), `MultiblockLease`. Solarpanel trägt einen
  Array-Lease und erzeugt verbaut nichts.
- Leistung = min(Deckel, Panels mit Sonne), gezählt alle 20 Ticks; erst Lade-Slot, Rest ein Paket, kein Puffer;
  verbaute Controller liefern nichts; Prüfung nur bei geladenem Bereich.
- Texturen aus `block_textures.png` (Compact Solars v4.0, GitHub), Datagen: Blockstates/Modelle (Seite/oben/unten),
  Loot (droppt sich selbst), Spitzhacken-Tag, Lang (englische Namen), Rezepte Trafo → Controller (formlos).
- JUnit `SolarArrayPatternsTest` (Layouts), 5 neue GameTests (`CompactSolarsGameTests`, insgesamt 100 grün),
  `gradlew build` und `runClient` (Hauptmenü, keine Mod-Fehler) erfolgreich. Doku: `abweichungen.md`, Spec 11 §4.
- **Damit ist Phase 6 inhaltlich vollständig** – wartet auf Abnahme durch den Nutzer.

## Nächste Schritte
1. Abnahme Phase 6 durch den Nutzer.
2. Manuelle Client-Tests zusätzlich: LV-Array (8 Panels + Controller) aufbauen → GUI-Sonnensymbol am Tag, BatBox
   daneben lädt mit 8 EU/t; Panel abdecken → weniger; MV-Aufbau 9×9 (verbaute LV-Controller liefern nichts, 64 EU/t),
   HV 27×27; Lade-Slot; Abbau droppt den Controller; Texturen.
3. Danach Phase 6b (GT-Maschinen und -Materialien) – erstes Teilpaket festlegen.

## Abnahme Phase 6
Phase 6 am 2026-10-15 vom Nutzer abgenommen (manuelle Tests gesammelt am Ende). Aufteilung Phase 6b festgelegt:
6b.1 Plattenbieger + Platten/Stäube → Mischmetall, fortg. Maschinenblock (MFSU, Massefabrikator) · 6b.2 Elektrolyseur
+ Zellen → Silizium, Aluminiumstaub · 6b.3 Hochofen-Multiblock + Heizspulen → Stahl, Aluminium, Siliziumplatte
(Solarpanel, Stahlwerkzeuge) · 6b.4 Assembler → Wasser-/Windmühle · 6b.5 Implosionskompressor, Zentrifuge, Canning
Machine (Iridiumplatte, Helium, Beryllium, Zellen). GT-Erze (offene Entscheidung 2 in Spec 12 §4): **zum Ende von 6b
entscheiden**.

# Stand Phase 6b (2026-10-15)

## Erledigt (Teilpaket „Phase 6b.1: Plattenbieger“)
- Neue Spec `docs/spec/13-gregtech-maschinen.md`: GT-Basis (`BaseMetaTileEntity` + `BasicMachine`: Tick, Energie,
  Upgrades, Explosionen, Seiten, GUI, Texturen, Tooltip), Plattenbieger, Platten, Förderband-Modul, Mischmetall-Tabelle.
  Pack-Config: alle `machines`-Schalter an.
- Paket `dev.craft.gregtech`: `GregTech` (Init, Item-Capability), `GtBlocks`, `GtItems`, `GtBlockEntityTypes`,
  `GtMenus`, `GtRecipes`, `GtMachine`; Creative-Tab „GregTech“.
- `machine/GtMachineBlock` (Blockstate `facing`/`front`/`active`, Upgrade per Rechtsklick sonst GUI, Abbau ohne
  Schraubenschlüssel → Maschinenteile + Energie-Explosion, Explosionstreffer, brennbar), `GtBasicMachineBlockEntity`
  (EU-Senke und -Quelle mit `bOutput`, Entladen Slot 5, Overclocker/Transformer/Energiespeicher als Zähler,
  `doEnergyExplosion`/`doExplosion` mit Kabelbrand, Feuer-Explosion, Auto-Ausgabe nach hinten, Wrench-Drop mit
  Upgrades), `BenderBlockEntity`.
- `recipe/GtRecipe` (generischer GT-Rezepttyp, Liste `craft:gt_bender`), `menu/GtBasicMachineMenu`,
  `client/GtBasicMachineScreen` (drei Schalter, Fortschrittspfeil), `item/GtMachineItem` (GT-Tooltip, Upgrades aus
  den Block-Entity-Daten), `item/GtMaterialItem` (Formel bzw. Beschreibung).
- Items: Platten Eisen/Gold/raff. Eisen/Zinn/Kupfer/Bronze (Tags `c:plates/*`), Förderband-Modul, Maschinenteile;
  IC2: Mischmetallbarren, fortg. Legierung, dichte Kupferplatte.
- Rezepte: Plattenbieger, Förderband-Modul (Barren oder Platten), Bender Barren → Platte (50 Ticks, 20 EU/t), Ofen
  Platte → Barren, Macerator Platte → Staub, Compressor 8 Kupferplatten → dichte Kupferplatte (+ Recycling),
  Mischmetall `X/Y/Z` (raff. Eisen/Bronze/Zinn → 1), Compressor Mischmetall → fortg. Legierung, **fortg.
  Maschinenblock** (beide IC2-Rezepte) → damit sind **MFSU und Massefabrikator** herstellbar.
- Texturen aus `GregTech.zip` (`terrain.png` 29/32/36/38/40/79/238/239, `materials.png`, `components.png`, GUI
  `Bender.png`) und `IC2.jar` (`item_0.png` 11/100/226). Lang en/de (GT nur englisch).
- 6 neue GameTests (`GregTechGameTests`, insgesamt 106 grün), `gradlew build` und `runClient` (Hauptmenü, keine
  Mod-Fehler) erfolgreich. Abweichungen in `abweichungen.md`.

## Offen / später
- GT-Upgrades MJ-Konverter, Lithium-Batterie, HV-Transformer, RS-Energiezelle: Logik vorbereitet, Items + MJ-Empfang
  mit dem Assembler (6b.4) bzw. wenn ihre Zutaten existieren.
- Weitere Mischmetall-Varianten und Bender-Rezepte, sobald Aluminium/Stahl/Titan … existieren (6b.3).
- **Klassische Rüstung fehlt:** Bronzerüstung und Kompositweste (`05-items.md` §5.1) sind noch nicht umgesetzt; die
  Kompositweste wäre jetzt herstellbar (fortg. Legierung). Zuordnung klären.

## Nächste Schritte
1. Teilpaket 6b.2: Elektrolyseur (+ Zellen) → Silizium, Aluminiumstaub – vorher Spec-Lücken prüfen und in
   `13-gregtech-maschinen.md` nachtragen.
2. Manuelle Client-Tests zusätzlich: Plattenbieger (Platzieren → Vorderseite zum Spieler, GUI mit Titel links,
   Schalter Energieausgabe/Auto-Ausgabe/getrennte Eingänge, Fortschrittspfeil, Batterie-Slot entlädt), Barren →
   Platte mit Generator/BatBox, Auto-Ausgabe in Truhe hinten, Drehen per Schraubenschlüssel (Ausgangsseite-Textur),
   Upgrades per Rechtsklick + Tooltip nach Wrench-Abbau, Abbau ohne Schraubenschlüssel → Maschinenteile (mit Energie:
   Explosion), Überspannung (MV-Trafo) explodiert, Feuer daneben; Rezepte Förderband-Modul, Mischmetall, fortg.
   Maschinenblock, MFSU, Massefabrikator.

## Erledigt (Teilpaket „Phase 6b.2a: IC2-Vorprodukte des Elektrolyseurs“, 2026-10-15)
- Projekt liegt jetzt unter `X:/Craft`; `tools/` (JDK, Skripte) fehlt → `gradle.properties` auf
  `C:/Program Files/Java/jdk-21` umgestellt.
- Spec ergänzt: `04-maschinen.md` (Magnetisierer-Ticker, Elektrolyseur-GUI), `05-items.md` (Eisenzaun),
  `13-gregtech-maschinen.md` §4 (GT-Elektrolyseur komplett inkl. Rezeptliste und Umfang).
- **Eisenzaun** (`ic2/block/IronFenceBlock`, Blockstate `magnetization`, Item-Tag `craft:metal_boots`, Rezept 12 aus
  raff. Eisen), **Magnetisierer** (`MagnetizerBlockEntity`, inkl. Gratis-Aufladungs-Originalfehler, kein GUI),
  **IC2-Elektrolyseur** (`ElectrolyzerBlockEntity`, `ElectrolyzerMenu`/`ElectrolyzerScreen`, arbeitet mit
  angrenzender BatBox/MFE/MFSU), **elektrolysierte Wasserzelle**; Rezepte Magnetisierer/Elektrolyseur.
- Texturen aus `IC2.jar` (`block_machine.png` Spalten 9/10, `block_0.png` 1, `item_0.png` 43, `GUIElectrolyzer.png`).
- 5 neue GameTests (`ElectrolysisGameTests`, insgesamt 111 grün), `build` und `runClient` erfolgreich.

## Erledigt (Teilpaket „Phase 6b.2b: Industrieller Elektrolyseur“, 2026-10-15)
- GT-Basis aufgeteilt: `GtMachineBlockEntity` (BaseMetaTileEntity) und `GtBasicMachineBlockEntity` (BasicMachine);
  `GtMachine` kennt Basismaschine ja/nein und die Eingangsspannung (Tooltip).
- `ElectrolyzerBlockEntity` (Spec 13 §4): 128 EU/t Eingang, 10 000 EU, Energie von allen Seiten, kein Facing,
  Wassertank 10 000 mB → Holo-Slot, `constant_need_of_energy` (Fortschritt −10, „Insufficient Energy Line!“),
  4 Ausgänge; Menü `GtElectrolyzerMenu`, Screen `GtElectrolyzerScreen`.
- Items: Wasserstoff-, Lithium-, Silizium-, Kohlenstoff-, Calcium-, Natriumzelle; Schwefel-, Aluminiumstaub, dunkle
  Asche; internes Item `craft:water`. Rezepte: Maschine (raff. Eisenplatten) + 10 Elektrolyse-Rezepte
  (Wasser, el. Wasserzellen, Wasserzelle, Wassereimer, Knochenmehl, Zucker, Lohenstaub, Sand, Tonstaub, Kohlestaub)
  → **Siliziumzellen und Aluminiumstaub** sind herstellbar. `GtRecipe` erlaubt leere Ausgaben.
- Texturen aus `GregTech.zip` (`terrain.png` 64/65, `cells.png`, `dusts.png`, GUI `Electrolyzer.png`).
- 4 neue GameTests (insgesamt 115 grün), `build` und `runClient` erfolgreich.

## Nächste Schritte
1. Teilpaket 6b.3: Hochofen-Multiblock + Heizspulen → Stahl, Aluminium, Siliziumplatte (Spec-Lücken vorher prüfen).
2. Manuelle Client-Tests 6b.2: Eisenzaun-Pfahl mit Eisenstiefeln am Magnetisierer, IC2-Elektrolyseur an BatBox
   (≥ 70 % lädt, ≤ 30 % gibt zurück, Balken); GT-Elektrolyseur: GUI (Slots, Holo-Wasser, Balken wächst nach oben,
   Fehlertext ohne Energie), Wasser per BC-Pumpe/Tank, Sand → Siliziumzelle, Tonstaub → Aluminiumstaub, Upgrades
   per Rechtsklick (nur ein IC2-Trafo), keine Drehung per Schraubenschlüssel.

## Erledigt (Teilpaket „Phase 6b.3a: Induktionsofen“, 2026-10-15)
- Vorprodukt des GT-Hochofens (Rezept `ACA/CMC/FCF` mit F = Induktionsofen). Spec `04-maschinen.md` ergänzt (GUI,
  Slots, Energiekappung, Shift-Klick).
- **Induktionsofen** (`InductionFurnaceBlockEntity`, `InductionFurnaceMenu`, `InductionFurnaceScreen`): 10 000 EU,
  128 EU/t Eingang, Hitze 0–10 000 (steigt bei Arbeit oder Redstone-Signal, sinkt sonst um 4/Tick), Fortschritt
  `heat/30` je Tick bis 4000 → beide Slotpaare schmelzen; Redstone im Batterie-Slot +10 000 EU (Originalverhalten).
  Fortg. Maschinenblock als Drop, Wrench-Rate 0,8. Rezept `CCC/CFC/CMC` (Kupfer, Elektroofen, fortg. Maschinenblock).
- Texturen aus `IC2.jar` (`block_machine.png` Spalte 13, `GUIInduction.png`).
- 4 neue GameTests (`InductionFurnaceGameTests`, insgesamt 119 grün).

## Nächste Schritte
1. Teilpaket 6b.3b: GT-Hochofen (Multiblock 3 × 3 × 4 aus GT-Maschinengehäusen, Heizspulen per Rechtsklick,
   Hitzekapazität), Gehäuse Standard/Verstärkt, Kupfernickel-Heizspule (+ Nickel), Stahl, Aluminium, Siliziumplatte.
2. Manuelle Client-Tests 6b.3a: Induktionsofen GUI (Hitze-%, Energie-/Fortschrittsbalken), zwei Stapel parallel,
   Redstone-Signal heizt vor, Seitenzugriff per Trichter (oben Eingaben, unten Batterie, Seiten Ausgaben).

## Erledigt (Teilpaket „Phase 6b.3b: Industrieller Hochofen“, 2026-10-15)
- Nutzerentscheidungen: Heizspulen an der **Werkbank** (statt Railcraft-Walzmaschine); verbundene Gehäusetexturen
  erst in **Phase 11** (damals Phase 10; beides in `abweichungen.md`).
- Spec `13-gregtech-maschinen.md` §5 (Hochofen: Struktur, Hitze, Prüfzeitpunkt, Heizspulen, GUI, Rezepte), §5.1
  Gehäuse, §5.2 Spulen/Materialien; `12-gregtech-materialien.md` Stahl-Weg korrigiert (raff. Eisenbarren).
- `GtMachine.Layout` (BASIC/SIDES/FRONT), **Hochofen** (`BlastFurnaceBlockEntity`: Struktur über `MultiblockPattern`,
  `mUpdate`-Zähler, Heizspulen per Rechtsklick, Hitze-Prüfung, shapeless Rezepte; `GtBlastFurnaceMenu`/`Screen`),
  `MachineBlockUpdates` (`causeMachineUpdate`), **GT-Maschinengehäuse** Standard/Verstärkt/Fortgeschritten
  (`GtCasingBlock`, fortgeschritten = `gt_advanced_machine_casing`, noch ohne Rezept). GT-Hooks `onRightclick`,
  `saveItemData`, `onMachineBlockUpdate`.
- Items: Nickel-/Stahl-/Aluminiumbarren und -platten, Nickel-/Stahlstaub, Siliziumplatte, Kupfernickel-/Kanthal-/
  Nichrom-Heizspule (Kanthal/Nichrom ohne Rezept bis Chrom). Texturen aus `GregTech.zip` (`materials.png`,
  `dusts.png`, `components.png`, `terrain.png` 68/69/71/72, `blocks.png` 13–15, GUI `Blast.png`).
- Rezepte: Hochofen, Standard-/verstärktes Gehäuse, Kupfernickel-Spule, Hochofen (Stahl aus raff. Eisen + 2 Kohlestaub
  oder Stahlstaub, Aluminium, Siliziumplatte), Bender/Macerator/Ofen der neuen Metalle, Mischmetall-Varianten,
  Förderband-/Elektrolyseur-/Generator-Varianten mit Aluminium, **Solarpanel**, **Stahlwerkzeuge** (Bergbaubohrer,
  Kettensäge, el. Hacke, el. Baumzapfer, el. Schraubenschlüssel), UU-Materie Nickel-/Aluminiumstaub. Schrottbox:
  Siliziumzelle, Maschinenteile, Aluminium-/Nickel-/Stahlstaub (fehlten zum Teil seit 6b.1/6b.2).
- 7 neue GameTests (`BlastFurnaceGameTests`, insgesamt 126 grün), `build` und `runClient` erfolgreich.

## Nächste Schritte
1. Teilpaket 6b.4: Assembler → Wasser-/Windmühle (Spec-Lücken vorher prüfen).
2. Manuelle Client-Tests 6b.3: Hochofen hinter 3 × 3 × 4 Gehäusen bauen (Controller vor der Mitte der unteren Lage,
   Rückseite zum Gehäuse), GUI „Heat Capacity“ bzw. „Incomplete Machine Casing!“, Lava im Kern (+500 K, danach ein
   Gehäuse neu setzen, damit geprüft wird), Kanthal-Spulen per Rechtsklick, Stahl/Siliziumplatte/Aluminium, Drehen
   per Schraubenschlüssel, Texturen; Induktionsofen (siehe 6b.3a).

## Erledigt (Teilpaket „Phase 6b.4: Assembler“, 2026-10-16)
- Nutzerentscheidung Umfang: „Basis + Lithium“ – alle Assembler-Rezepte, deren Zutaten es gibt, plus Lithium-Batterie,
  Lithium-BatPack und Lithium-Batterie-Upgrade.
- Spec `13-gregtech-maschinen.md` §6 (Assembler, Rezeptsuche, Rezeptliste mit Config-Dauern je Ausgabe, GT-Upgrades
  inkl. MJ-Empfang, Lithium-Items, Maschinenhüllen und Ore-Namen-Tags).
- **Assembler** (`AssemblerBlockEntity`, `GtMachine.ASSEMBLING_MACHINE`): BasicMachine mit getrennten Eingängen
  (Standard an), shapeless Rezepte (`GtRecipe.Kind.ASSEMBLER`), eigene Oberseite 237 (`GtMachine.getTopTexture`).
  Rezept `CPC/RQR/CRC` mit raff. Eisen- oder Aluminiumplatten.
- 30 Assembler-Rezepte: **Wassermühle** (Generator + 4 Aluminiumplatten → 2), **Windmühle** (Generator + 4
  Kohleplatten), 2 Wassermühlen → Generator, MJ-Konverter, HV-Trafo-Upgrade, Lithium-Batterie-Upgrade, Maschinenblock,
  Maschinenteile, Aluminium-/Bronze-/Stahl-Maschinenhülle, BatPack/Lithium-BatPack → Batterien, Kabelisolierung,
  Zahnräder (Eisen, Zinn, Gold, Diamant), Hebel, Fackeln, Redstone-Fackel, Kompass, Uhr.
- **GT-Upgrades** in `GtMachineBlockEntity.installUpgrade` in GT-Reihenfolge (MJ-Konverter → Overclocker →
  Trafo/HV-Trafo → Energiespeicher → Lithium); **MJ-Empfang** (`updateStatus`, `PowerReceptor`, Capability nur mit
  Konverter, Provider nicht gespeichert).
- **Lithium-Batterie** (`LithiumBatteryItem`, leer stapelbar bis 16, Icon leer/geladen) und **Lithium-BatPack**
  (`LithiumBatpackItem`, 600 000 EU, Brustplatte ohne Schutz); Tags `craft:raw_machine_tier_01/02` und
  `craft:crafting_100k_eu_store` in Gehäuse-, Generator-, MFE- und Macerator-Rezepten.
- Texturen aus `GregTech.zip` (`terrain.png` 224/225/237, `items.png` 56–58, `components.png` 25–27/32/33/35,
  GUI `Assembler.png`, Rüstung `lithiumbatpack_1.png`) – zugeschnitten mit Python/Pillow (neu installiert).
- 6 neue GameTests (`AssemblerGameTests`, insgesamt 132 grün), `build` und `runClient` erfolgreich.

## Nächste Schritte
1. Teilpaket 6b.5: Implosionskompressor/Zentrifuge/Canning Machine (Umfang und Spec-Lücken vorher klären).
2. Manuelle Client-Tests 6b.4: Assembler-GUI (Knopf „getrennte Eingänge“ an, Pfeil), Wassermühle aus Generator +
   Aluminiumplatten, MJ-Konverter per Rechtsklick und Engine daneben (MJ-Speicher füllt sich, Maschine läuft ohne EU),
   HV-Trafo-Upgrade erst nach zwei IC2-Trafos, Lithium-Batterie laden/entladen (Icon wechselt), Lithium-BatPack
   tragen (Bergbaubohrer zieht daraus), MFE mit Lithium-Batterien, Standard-Gehäuse mit Aluminiumhülle.

## Erledigt (Teilpaket „Phase 6b.5a: Canning“, 2026-10-16)
- Nutzerentscheidung Reihenfolge/Umfang 6b.5: **6b.5a Canning** (IC2 Canning Machine + GT Automatic Canning Machine) →
  6b.5b Implosionskompressor (Iridiumplatte) → 6b.5c Zentrifuge (**nur erreichbare Rezepte**, neue Ausgabe-Items nur
  bei Bedarf).
- Spec: `04-maschinen.md` Canning Machine (Tick, GUI, Seiten, Texturen), `05-items.md` §6 (gefüllte Zinndose,
  Kanister, Zellen, Quantenhelm), `13-gregtech-maschinen.md` §7 (GT-Canner, Rezeptliste).
- **IC2 Canning Machine** (`CanningMachineBlockEntity`, `CanningMachineMenu`/`Screen`): Modus 1 Lebensmittel in
  Dosen (Meta je Nahrung), Modus 2 Brennstoffkanister/Jetpack mit Kohle-/Bio-Brennstoffzellen + Redstone/Glowstone/
  Schwarzpulver-Booster. Modus 3 (CF-Pack) folgt mit dem Bauschaum.
- **GT Automatic Canning Machine** (`CannerBlockEntity`, `GtRecipe.Kind.CANNER`): 48 Rezepte – Nahrung (Datagen über
  alle Nahrungs-Items), Bio-/hydr. Kohlezelle, Wasser-/Lavazelle aus Eimern, Brennstoffkanister aus 6 Zellen.
- Items: Zinndose, gefüllte Zinndose (`craft:can_meta`, Effekte, „This looks bad...“), gefüllter Brennstoffkanister
  (`craft:fuel_value`, Brennwert `min(32767, 2·Wert)`, leerer Kanister bleibt), Bio-/hydr. Kohle-/Bio-Brennstoff-/
  Kohle-Brennstoffzelle. Rezepte: 4 Zinndosen, Canning Machine `TCT/TMT/TTT`, GT-Canner formlos, Extractor
  (Zellen → Brennstoffzellen, gefüllte Dose Meta 0/1 → leere Dose), Ofen Zinndose → Zinnbarren.
- **Quantenhelm** isst bei Hunger gefüllte Zinndosen (1000 EU). Schrottbox: gefüllte (100) und verdorbene Dose (200).
- Texturen aus `IC2.jar` (`item_0.png` 34/35/52/57/58, `block_machine.png` Spalte 6, `GUICanner.png`) und
  `GregTech.zip` (`terrain.png` 254/255, GUI `Canner.png`).
- 7 neue GameTests (`CanningGameTests`, insgesamt 139 grün), `build` und `runClient` erfolgreich.

## Nächste Schritte
1. Teilpaket 6b.5b: Implosionskompressor (Multiblock) → Iridium-Legierungsbarren, Iridiumplatte (Spec-Lücken vorher
   prüfen: Struktur, TNT/ITNT, GT-Iridiumbarren, Diamant-/Smaragdstaub).
2. Manuelle Client-Tests 6b.5a: Canning Machine GUI (Flamme, Pfeil je Modus), Kuchen → 6 Dosen, Pilzsuppe lässt
   Schüssel, Kanister mit 6 Kohle-Brennstoffzellen + Redstone als Booster, Jetpack betanken, Seitenzugriff (links von
   vorne = Batterie, unten = Behälter, oben = Eingabe), gefüllte Dose essen (leere Dose zurück, Effekte), Quantenhelm
   isst bei Hunger; GT-Canner: Apfel + Dosen in vertauschten Slots, Wassereimer + Zelle, Brennstoffkanister im
   Generator/Eisenofen.

## Erledigt (Teilpaket „Phase 6b.5b: Implosionskompressor“, 2026-10-16)
- Nutzerentscheidungen: **Industrie-TNT voll als Block** (Zünden, Entity, IC2-Explosion); Implosionsrezepte **nur
  erreichbare** (Iridiumplatte, Diamantstaub → Industriediamanten, Smaragdstaub → Smaragde).
- Spec: `13-gregtech-maschinen.md` §8 (Struktur, Tick inkl. doppeltem Knall, GUI, Rezepte), §8.1 (Iridium-Kette,
  Edelsteinstaub); `05-items.md` §7 (ITNT im Detail) und Blocktabelle (verstärktes Glas); `12-gregtech-materialien.md`
  (Industrie-TNT statt TNT, Verstärker).
- **Implosionskompressor** (`ImplosionCompressorBlockEntity`, `GtMachine.IMPLOSION_COMPRESSOR`, neues
  `GtMachine.Layout.TOP`): 3 × 3 × 3-Würfel unter dem Controller (Ecken Standard-, sonst verstärktes Gehäuse, Mitte
  Luft), 32 EU/t, kein Overclocker, Rezeptsuche nicht shapeless (`GtRecipe.Kind.IMPLOSION`), GUI
  `GtImplosionCompressorMenu`/`Screen`. Rezept `ABA/DCD/ABA` (fortg. Legierung, Tier-02-Maschinenblock, el.
  Schaltkreis, Compressor).
- **Industrie-TNT** (`IndustrialTntBlock`, `Ic2ExplosiveEntity`/`IndustrialTntEntity`, `Ic2ExplosiveRenderer`):
  Zünden durch Abbauen, Feuerzeug (ohne Abnutzung), Redstone, Explosion (7–21 Ticks); droppt nie. Rezepte 4 ITNT
  `FFF/TTT/FFF` und `FTF/FTF/FTF`.
- Items: Iridiumbarren (Compressor aus Iridiumerz), Iridium-Legierungsbarren (Werkbank `IAI/ADA/IAI`),
  Diamant-/Smaragdstaub (Macerator; Massefabrikator-Verstärker 125 000/50 000), **Iridiumplatte**; Block
  **verstärktes Glas** (7 aus Glas + fortg. Legierung). Damit **Quantum-Rüstung** mit IC2-Rezepten.
- Texturen aus `IC2.jar` (`block_0.png` 13/58–60, `item_0.png` 93) und `GregTech.zip` (`terrain.png` 73/74,
  `materials.png` 4/16, `dusts.png` 35/36, GUI `Implosion.png`).
- 9 neue GameTests (`ImplosionCompressorGameTests`, insgesamt 148 grün), `build` und `runClient` erfolgreich.

## Nächste Schritte
1. Teilpaket 6b.5c: Zentrifuge (nur erreichbare Rezepte; Spec-Lücken vorher prüfen).
2. Manuelle Client-Tests 6b.5b: Würfel bauen (Ecken Standard-, Rest verstärktes Gehäuse, Mitte frei), Controller
   mittig oben drauf → Oberseite mit grünem Bildschirm (aktiv), GUI ohne „Incomplete Machine Casing!“; Legierungsbarren
   + 8 ITNT → Iridiumplatte + 4 dunkle Asche (zwei Knalle); Seiten: oben Eingabe 1, Ost/West Ausgaben, sonst
   Eingabe 2. ITNT: abbauen/Feuerzeug/Redstone zündet, Kettenreaktion, Blitz-/Aufbläh-Animation, Sprengkraft;
   verstärktes Glas (durchsichtig, explosionsfest); Quantum-Rüstung craften (Ladung übernommen).
3. Später: GT-Rückgewinnung von Werkzeugen/Rüstung im Macerator (`applyUsagesForMaterials`, alle Materialien),
   Smaragdstaub im Assembler/Elektrolyseur, ITNT-Variante mit Feuersteinstaub, Dynamit aus ITNT.

## Teilpaket „Phase 6b.5c: Zentrifuge“ (2026-10-16, **erledigt** – wartet auf Abnahme)
- Umgesetzt: Maschine (Spec 13 §9, gemeinsame Basis mit dem Elektrolyseur `GtTankMachineBlockEntity`), 53
  Zentrifugen-Rezepte (A + B1–B4), 23 neue Items + Lava-Holo-Item, Kleinstaub-Crafting, Werkbankrezept (2 Varianten).
  Abweichungen (Fisch/Kürbis/Riesenpilze, Schlacke, kl. Eisenstaub, Lava-Item) in `abweichungen.md`.
- Geprüft: `gradlew build`, alle 152 GameTests (neu: Asche, Lavatank, Rezeptwerte, Energiewerte), `runClient`
  startet ohne Modell-/Texturfehler.
- Zum Testen im Spiel: Zentrifuge craften, Lava per Pumpe/Rohr einfüllen (16 Lava → Metalle), Redstone/Glowstone/
  Nahrung zentrifugieren, GUI-Pfeile und Holo-Slot ansehen.
- Recherche-Notizen:
- Quelle: `GT_MetaTileEntity_Centrifuge` (ID-Name „Industrial Centrifuge“, GUI-ID 146, `gui/Centrifuge.png`,
  `GT_Container_Centrifuge`). Aufbau **fast identisch zum Elektrolyseur** (§4 in `13-gregtech-maschinen.md`) – Umsetzung
  kann dessen Code-Muster nutzen. Unterschiede:
  - `maxEUInput` **32** (statt 128), `maxEUStore`/MJ 10 000, Upgrades O T B (+ MJ), nicht drehbar, Energie von allen Seiten.
  - **Lavatank** statt Wasser: nur Lava, 10 000 mB, `getTankPressure −100`; je Tick ≥ 1000 mB → +1 Lava in Holo-Slot 6
    (max. 64). `checkRecipe`: erst (Slot 0, Slot 1), dann (Slot 6 = Lava, Slot 1).
  - Rezepte über `addCentrifugeRecipe(in1, nZellen, out1..4, Dauer)`: **immer 5 EU/t** (`GT_Recipe`-Konstruktor),
    Eingabe 2 = n leere Zellen (negativ = n leere Brennstoffkanister), Dauer per `RecipesAdvancedConfig.cfg` „centrifuge“.
  - Texturen `terrain.png`: unten 32, oben 41 aktiv / 43 inaktiv, Seiten 25 aktiv / 27 inaktiv.
  - Beschreibung „And round and round and round it goes“; Info „Progress:“ / „x secs“ / „/y secs“.
  - Seiten: oben → Slot 0, unten → Slot 1, Seiten → Slots 2–5. Tick/Energiemangel (−10) wie Elektrolyseur.
- Rezeptfundstellen: `GT_Mod.java` 1372/1384 (Lava-Dose, nur mit anderen Mods), 1731–1732 (Eiszelle → Eis + Zelle;
  Magmacreme → Lohenstaub + Schleimball), 1903–2001 (Uranzellen, Gras/Myzel/Seelensand → Pflanzenbälle/Sand,
  Methan aus Nahrung/Gold-Äpfeln, Harz → 14 Gummi, Seelensand?/Ölzelle, **Lavazelle ×16 / Lava ×16 → Elektrum,
  4 Kupfer, kl. Wolframstaub, Zinn**), 4819/4857 (Saatöl – im Pack mit Forestry → Squeezer, **nicht** Zentrifuge);
  `GT_OreDictHandler.java` 1500–3623 (Stäube: Redstone, Glowstone, Lapis, Bronze, Kupfer, Gold, Nickel, Eisen, Zinn,
  Netherrack, Endstein, Enderperle, Asche, Uran, Molekül-Zellen, Ölsand …; Auslöser je Zeile = letzter
  `equals("dust…")` davor).
- Namen geklärt: Zellen 3 Helium, 6 Helium-3, 7 Silizium, 9 Methan, 16 Quecksilber, 17 Öl, 23 Eis, 24 Saatöl;
  Kleinstaub 3/4/5 Pyrit/Calcit/Sodalith, 22 Wolfram, 24 Zink, 28 Nickel, 241–244 Eisen/Gold/Kupfer/Zinn.
- Vanilla-Feldnamen geklärt (2026-10-16, per `javap -c` auf `up`/`amq` aus `ftb ultimate/instmods/minecraft.jar`,
  statischer Initialisierer → `setUnlocalizedName`): `amq.x` Gras, `y` Erde, `G` Lava, `H` Sand, `aW` Eis,
  `ai`/`aj` brauner/roter Pilz, `bq`/`br` Riesenpilzblöcke, `bd` Kürbis, `bf` Seelensand, `bB` Myzel; `up.j` Apfel,
  `p` Goldbarren, `F` Pilzsuppe, `U` Brot, `aq`/`ar` Schwein roh/gebraten, `at` goldener Apfel (Meta 1 = verzaubert),
  `aC` Redstone, `aI` Tonklumpen, `aM` Schleimball, `aU`/`aV` Fisch roh/gebraten, `aZ` Kuchen, `bc` Keks, `bf` Melone,
  `bi`/`bj` Rind, `bk`/`bl` Huhn, `bm` verrottetes Fleisch, `bq` Goldnugget, `br` Netherwarze, `bu` Spinnenauge,
  `bw` Lohenstaub, `bx` Magmacreme, `bB` glitzernde Melone, `bK` Karotte, `bL` Kartoffel, `bM` Ofenkartoffel,
  `bN` giftige Kartoffel, `bP` goldene Karotte.
- Weitere Namen: GT-Staub 0 Enderperle, 1 Enderauge, 2 Lazurit, 3 Pyrit, 4 Calcit, 5 Sodalith, 6 Netherrack,
  7 Feuerstein, 8 Schwefel, 9 Salpeter, 13 Magnesium, 22 Wolfram, 23 Blei, 24 Zink, 28 Nickel, 32 Rubin, 37 Olivin,
  62 Asche, 63 dunkle Asche; Zellen 1 Deuterium, 2 Tritium. `getGregTechItem(1,…)` = Staub, `(2,…)` = Zelle.
  `ForestryBronzeNerf=true` (GregTech.cfg) → Bronzestaub-Rezept mit **1** Staub. Dauern in
  `RecipesAdvancedConfig.cfg` = Code-Standardwerte. Zinnzellen-Anzahl 8 → Lavazellen-Rezept 10 Zinn.

### Rezeptliste Zentrifuge (alle 5 EU/t; Zellen = leere Zellen als Eingabe 2)
**A – sofort umsetzbar (alles vorhanden):** Magmacreme → Lohenstaub + Schleimball (500) · 16 Erde → kompr.
Pflanzenball, Pflanzenball, Ton, 8 Sand (2500) · 16 Gras → 2 kompr. Pflanzenb., 2 Pflanzenb., Ton, 8 Sand (2500) ·
8 Myzel → 2 braune + 2 rote Pilze, Ton, 4 Sand (1650) · 4 Harz → 14 Gummi, kompr. Pflanzenb., Pflanzenb. (1300).

**B – Eingabe erreichbar, neue Ausgabe-Items nötig:**
- B1 Methan (neu: Methanzelle): Nahrung + 1 Zelle → Methanzelle (5000; Apfel 32, Pilzsuppe 16, Brot 64, Schwein
  12/16, Fisch 12/16, Rind 12/16, Huhn 12/16, Melone 64, Kürbis 16, verr. Fleisch 16, Spinnenauge 32, Karotte 16,
  Kartoffel 16, giftige Kart. 12, Ofenkart. 24, Keks 64, Kuchen 8, Riesenpilzblöcke je 12, Pilze je 32,
  Netherwarze 32) · goldene Karotte 1 / glitzernde Melone 8 + 1 Zelle → Methan + 6 Goldnuggets (10000) ·
  verz. goldener Apfel + 2 Zellen → 2 Methan + 64 Gold (10000) · 16 Gummiholz + 5 Zellen → 8 Harz, 6 Pflanzenb.,
  Methan, 4 Kohlenstoffzellen (5000).
- B2 Lava (neu: Elektrumbarren, kleiner Wolframstaub): 16 Lavazellen → Elektrum, 4 Kupfer, kl. Wolfram, 10 Zinn
  (15000) · 16 Lava (Tank) → dto. mit 2 Zinn (10000). Ohne B2 ist der Lavatank der Zentrifuge funktionslos.
- B3 Kleinstäube (neu: kl. Kupfer/Gold/Zinn/Nickel/Zink/Bleistaub; kl. Eisen vorhanden): 3 Kupferst. → kl. Gold +
  kl. Nickel (2400) · 3 Goldst. → kl. Kupfer + kl. Nickel (2400) · 3 Nickelst. → kl. Eisen/Gold/Kupfer (3450) ·
  2 Eisenst. → kl. Zinn + kl. Nickel (1500) · 2 Zinnst. → kl. Zink + kl. Eisen (2100) · 1 Bronzest. → 6 kl. Kupfer +
  2 kl. Zinn (1500) · 2 Silberst. → kl. Blei (2400).
- B4 Gase/Mineralien: 4 Wasserstoffz. → Deuterium + 3 Zellen (3000) · 4 Deuterium → Tritium + 3 Zellen (3000) ·
  16 Glowstonestaub + 1 Zelle → 8 Redstone, 8 Goldstaub, Heliumzelle (25000) · 16 Helium → Helium-3 + 15 Zellen
  (10000) · 10 Redstone + 4 Zellen → Siliziumzelle, 5 Pyrit, Rubin, 3 Quecksilberzellen (7000) · 4 Lapis → 3 Lazurit,
  kl. Pyrit, kl. Calcit, 2 kl. Sodalith (1500) · 16 Seelensand + 1 Zelle → Ölzelle, 4 Salpeter, Kohlestaub, 10 Sand
  (2500) · 2 dunkle Asche → 2 Asche (250; TE-Schlacke gibt es nicht → 2. Ausgabe Asche).

**Entschieden (Nutzer, 2026-10-16):** A und alle Gruppen B1–B4 werden umgesetzt (inkl. der neuen Ausgabe-Items).

**C – Eingabe nicht erreichbar / später:** Eiszelle (Vakuumgefrierer fehlt), Uranzellen + Uranstaub (Phase 7),
Terra Wart (Crops), Lava-Dose/Refractory Lava (andere Mods), Ölsand, Netherrack-/Endstein-/Enderaugenstaub (GT-
Macerator-Rezepte fehlen noch), Marmor/Basalt/Granat/Invar/Messing/Elektrum/Platin/Iridium/Blei/Zink/
Quecksilber-Staub, Schwefel-/Calciumcarbonat-/Quecksilber-Molekülzellen, Red Rock.

## Abnahme Phase 6b
- Vom Nutzer am 2026-10-16 abgenommen („Weiter geht's“). Offen bleiben die GT-Teile, die GT-Erze brauchen (Titan,
  Platin, Natrium/Kalium …; offene Entscheidung 2 in `CLAUDE.md`).

# Stand Phase 7 (2026-10-16)

**Entschieden (Nutzer, 2026-10-16):** Phase 7 = IC2-Reaktor 1:1 **plus die ohne GT-Erze erreichbaren GT-Reaktorteile**
(Helium-Kühlzellen 60k/180k/360k, Iridium-Neutronenreflektor, Thorium-/Plutoniumzellen samt Uranstaub-Kette; keine
NaK-Kühlzellen, kein Vakuumgefrierer) **und die IC2-Nuke**. Teilpakete: 7a IC2-Reaktor · 7b GT-Reaktorteile · 7c Nuke.

## Erledigt (Teilpaket „Phase 7a: IC2-Reaktor“, 2026-10-16)
- Spec: `06-reaktor.md` ergänzt (Vent-Abbruch greift nie, Heizzelle/Platten stapelbar, §5a Block/GUI/Klang/Texturen,
  Uranzelle im GT-Canner mit 2 EU/t).
- **Uran:** Uranerz + Tiefenschiefer-Uranerz (Weltgen IC2: 20 Adern/Chunk, Größe 3, y 0–64; 1–3 XP; Behutsamkeit),
  Uranerz-Brocken, raffiniertes Uran, Uranblock (Compressor 9 Barren / Ofen → 9 Barren wie GT). Compressor: Brocken
  bzw. Erz → Barren. GT-Canner: Barren + Zelle → Uranzelle (100 Ticks, 2 EU/t).
- **Reaktor** (`ic2/reactor/NuclearReactorBlockEntity`, `NuclearReactorBlock`): 9 × 6-Matrix, Breite 3 + Kammern,
  Zyklus alle 20 Ticks (Zufallsversatz), Redstone-Pflicht, Energie `output · 5` je Tick (Rest über die Kammern),
  Hitzeeffekte ab 40/50/70/85 % (Feuer, Wasser verdampft, Strahlungsschaden, Lava), Kernschmelze mit IC2-Explosion
  (Stärke 10 + Komponenten, max. 45, Schadensart `craft:nuke` mit Strahlungseffekt ±100). GUI `GUI6by3–9`, Klang
  (Loop + Geigerzähler nach Leistung), Rauch/Flammen nach Hitze.
- **Reaktorkammer** (`ReactorChamberBlock`/`BlockEntity`): nur an genau einem Reaktor, fällt sonst ab, öffnet die
  Reaktor-GUI, Item-Capability des Reaktors, Energiequelle (Rest der Reaktorenergie).
- **Komponenten** (`ic2/item/reactor/*`): Uran-/Doppel-/Vierfachzelle, 10k/30k/60k-Kühlzelle, drei Platten, vier
  Wärmetauscher, fünf Wärmeableiter, Heizzelle, Neutronenreflektor, dicker Neutronenreflektor (noch ohne Rezept, GT-
  Berylliumzelle → 7b), RSH-/LZH-Kondensator (Reparatur per Redstone/Lapis, `craft:gradual_repair`), Isotopenzelle,
  angereicherte und fast erschöpfte Uranzelle. Alle IC2-Rezepte (abgenutzte Teile passen nicht).
- Texturen und Klänge aus `IC2.jar`. 15 neue GameTests (`ReactorGameTests`, insgesamt 167 grün), `build` und
  `runClient` erfolgreich.

## Erledigt (Teilpaket „Phase 7b: GT-Reaktorteile“, 2026-10-16)
- Spec: `06-reaktor.md` §8 (Helium-Kühlzellen, Iridium-Reflektor, Thorium-/Plutoniumzellen inkl. Pulsraten, Material-
  und Rezeptliste).
- Items (`gregtech/item/GtCoolantCellItem`, `GtNeutronReflectorItem`, `GtRadioactiveCellItem`): 60k/180k/360k-
  Heliumkühlzelle, Iridium-Neutronenreflektor, Thorium- und Plutoniumzelle (einfach/doppelt/vierfach), Thorium-/
  Plutoniumbarren, Uran-/Thorium-/Plutoniumstaub (leuchtend), kleiner Plutoniumstaub, Wolframstaub, Berylliumzelle.
- Rezepte: Macerator/Compressor (Uran-, Thorium-, Plutoniumstaub ↔ Barren), Zentrifuge (fast erschöpfte und
  angereicherte Uranzellen, Uranstaub), GT-Canner (Thorium-/Plutoniumzelle), Doppel-/Vierfachzellen, Heliumkühlzellen,
  Overclocker aus Heliumkühlzelle, dicker Neutronenreflektor mit Berylliumzelle, Iridium-Reflektor, Smaragdstaub im
  Elektrolyseur (→ Beryllium), GT-Wärmeableiter mit Aluminiumplatten. Massefabrikator-Verstärker Uran/Plutonium/Thorium.
- Nicht umgesetzt: NaK-Kühlzellen, Containment-Platte mit Bleiplatte (beide doch ohne GT-Erze erreichbar → Phase 8),
  Vakuumgefrierer (Zurückkühlen der Heliumzellen; Rezept braucht die IC2-Pumpe).
- 5 neue GameTests (`GtReactorGameTests`, insgesamt 172 grün), `build` und `runClient` erfolgreich.

## Erledigt (Teilpaket „Phase 7c: Nuke“, 2026-10-16)
- `NukeBlock`/`NukeEntity` auf Basis des Industrie-TNT (neue Hooks `canExplodeByHand`, `onIgnite`,
  `getDamageSource`): Zündschnur 300, Stärke 35, Drop-Rate 0,05, Schadensart `craft:nuke` (Hunger/Strahlung im Umkreis
  100, Hazmat schützt), Abbauen von Hand droppt die Nuke, Entschärfen mit Schraubenschlüssel, Platzieren/Zünden im Log.
  Rezept `RCR/UMU/RCR`. Texturen `block_0.png` 61–63. `NukeExplosion.ogg` wird im Original nirgends abgespielt und ist
  deshalb nicht übernommen.
- 3 neue GameTests (`NukeGameTests`, insgesamt 175 grün; eine echte Detonation wird bewusst nicht getestet),
  `build` und `runClient` erfolgreich.

## Abnahme Phase 7
- Wartet auf Abnahme durch den Nutzer.

## Nächste Schritte
1. Manuelle Client-Tests 7c (in einer Testwelt!): Nuke platzieren → Log-Eintrag, von Hand abbauen → Item, Feuerzeug/
   Redstone → 15 s Zündschnur, Schraubenschlüssel-Rechtsklick entschärft, Detonation (Krater, Strahlung/Hunger,
   Hazmat schützt).
2. Manuelle Client-Tests 7b: Thoriumzelle (Output nur jeden 5. Zyklus), Plutoniumzelle, Iridium-Reflektor (Stapel
   wirkt nicht), Heliumkühlzelle (Tooltip „Stored Heat“), Zentrifuge mit Uranstaub, Smaragdstaub-Elektrolyse.
3. Manuelle Client-Tests 7a: Uranerz finden (y 0–64), Reaktor + Kammern bauen (GUI-Breite 3–9, Kammer nur an einem
   Reaktor), Redstone an → Loop/Geigerzähler, Energie in eine MFE, Kühlzellen-Balken, Kernschmelze in einer Testwelt
   (Rauch/Flammen, Feuer/Lava ringsum, Explosion), Kondensator mit Redstone reparieren.

## Phasenplan geändert (Nutzer, 2026-10-16)
- Neue **Phase 8 „GT-Nachträge“**: NaK-Kühlzellen (Natrium: Lapis → Zentrifuge → kleiner Sodalithstaub → Sodalithstaub
  → Elektrolyseur; Kalium: Enderperle → Macerator → Enderperlenstaub → Elektrolyseur), Containment-Reaktorplatte mit
  Bleiplatte (Silberstaub per UU-Materie → Zentrifuge → Blei → Ofen → Barren → Plattenbieger) und **GT-Erze** samt
  dem, was sie freischalten (z. B. Titan, Platin, Rubin). Bisherige Phasen 8/9/10 → 9/10/11.
- Vakuumgefrierer bleibt offen, bis die IC2-Pumpe existiert (IC2-Teile ohne Phase).
- **Entschieden (Nutzer, 2026-10-16): alle GT-Erze wie im Pack** (komplette GT-2.90-Weltgen inkl. Nether/End).
  Teilpakete: 8a NaK-Kühlzellen + Bleiplatte · 8b Spezifikation GT-Weltgen · 8c GT-Erze umsetzen · danach Freischaltungen.

# Stand Phase 8 (2026-10-16)

## Erledigt (Teilpaket „Phase 8a: NaK-Kühlzellen + Bleiplatte“, 2026-10-16)
- Spec: `06-reaktor.md` §8 „Phase 8a“ (Rezepte, Mengen, Dauern).
- Items (`GtItems`): Enderperlen-, Sodalith-, Bleistaub, kl. Silberstaub, Bleibarren, Bleiplatte, Chlorit-/Kalium-/
  Stickstoffzelle, 60k/180k/360k-NaK-Kühlzelle (`GtCoolantCellItem` wie Helium). Tags `c:ingots|dusts|plates/lead`.
- Rezepte: Sodalith 4 ↔ 1, Sodalith- und Enderperlenstaub-Elektrolyse, Enderperle → Macerator, NaK-Zellen (zwei
  60k-Varianten), Overclocker mit NaK-Zelle, Blei-Kette (Silber-Zentrifuge, kl. Blei/Silber 4 ↔ 1, Ofen, Macerator,
  Plattenbieger, UU-Materie 14 Bleistaub), Blei-Zentrifuge → kl. Silberstaub, Containment-Platte mit Bleiplatte.
- Massefabrikator: Enderperlenstaub als Verstärker (50 000). Schrottbox: alle inzwischen vorhandenen GT-Stäube ergänzt
  (Silber, Blei, Schwefel, Salpeter, Lazurit, Pyrit, Sodalith, Wolfram).
- Texturen aus `GregTech.zip` (`items.png` 60–62, `metaitems/*`). 5 neue GameTests (`GtFollowUpGameTests`, insgesamt
  180 grün), `build` und `runClient` erfolgreich.
- Nicht umgesetzt: Vakuumgefrierer (Rückkühlen), GT-Batterie-Rezepte mit Bleistaub (Schwefelsäurezelle fehlt),
  Bleierz/Galena (→ 8c).

## Nächste Schritte
1. Manuelle Client-Tests 8a: Lapis in der Zentrifuge → kl. Sodalith → Sodalithstaub → Elektrolyseur (Natrium);
   Enderperle → Macerator → Elektrolyseur (Kalium); NaK-60k craften (nur mit unbenutzter 10k-Zelle), im Reaktor
   (Tooltip „Stored Heat“); Silberstaub (UU) → Zentrifuge → kl. Blei → Bleistaub → Ofen → Plattenbieger →
   Containment-Platte.
2. ~~Phase 8b: Spezifikation der GT-Weltgen~~ → erledigt, siehe unten.

## Erledigt (Teilpaket „Phase 8b: Spezifikation GT-Weltgen“, 2026-10-16)
- `07-weltgenerierung.md` §6 vollständig: Pack-Config, Erzblock (Härte, Werkzeugstufe, Drops mit Glück-Formeln, XP,
  Erzwerte, Texturschema), Generierung Oberwelt (Galena, Iridium, Rubin/Saphir/Bauxit mit Biomlisten), Nether (Pyrit,
  Zinnober, Sphalerit), End (Grundgenerierung + Asteroiden), Verarbeitung im Pack (Macerator, Ofen, Hochofen,
  Industrial Grinder) und Umsetzungsvorschlag für 1.21. Originalfehler dokumentiert (Dimensionsprüfung mit
  Chunkkoordinaten, Saphir-`case`-Durchfall, ungenutzter `Silverore`-Schlüssel).
- `abweichungen.md`: GT-Erz-Blöcke und -Weltgen (geplant). `12-gregtech-materialien.md` §4.2 als entschieden markiert.

## Entscheidungen (Nutzer, 2026-10-16)
1. GT-Biomlisten mit 1.21-Entsprechungen (Liste in `07-weltgenerierung.md` §6.5).
2. Industrial Grinder wird 1:1 nachgebaut – eigenes Teilpaket **8d** nach den Erzen.

## Nächste Schritte (überholt)
1. ~~Phase 8c~~ → erledigt, siehe unten.

## Erledigt (Teilpaket „Phase 8c: GT-Erze“, 2026-10-16)
- `GtOre` (12 Erze), Blöcke `craft:<erz>_ore` (Iridium: `iridium_ore_block`), Tiefenschiefer-Varianten für Galena,
  Iridium, Rubin, Saphir (+1,5 Härte). Werkzeugstufe, `dragon_immune`, `c:ores/*`, Erzwerte für Miner/Scanner.
- Loot wie `GT_BlockMetaID_Ore` (eigene Loot-Funktion `GtOreCountFunction` und Bedingung `GtOreChanceCondition` für
  die Glück-Formeln), XP über `DropExperienceBlock`, Behutsamkeit → Erzblock.
- Weltgen: Oberwelt (Galena, Iridium, Rubin/Saphir/Bauxit mit `ChunkBiomeFilter` und den entschiedenen Biom-Tags),
  Nether (`GtNetherOresFeature`), End inkl. Asteroiden (`GtEndOresFeature`).
- Texturen: sechs Flächenbilder je Erz aus `blocks.png` (Zeilen 8–13), sechs Modellvarianten je Erz.
- Neue Items: Rubin, Saphir, grüner Saphir, Olivin, roter/gelber Granat, Platinbarren, Stäube Galena/Zinnober/
  Sphalerit/Bauxit/Zink/Platin/Saphir/grüner Saphir/Olivin/Granate/Calcit, kl. Schwefelstaub, Calciumcarbonatzelle.
- Rezepte: `07-weltgenerierung.md` §6.4 (Macerator, Ofen, Pyrit-Hochofen) und die erreichbaren Zeilen aus §6.6
  (neu nachgetragen: Folgerezepte aus `GT_OreDictHandler`); Implosion der Edelsteinstäube; Verstärker; Schrottbox um
  Bauxit, Zink, Calcit, Platin, Granate, Olivin, Rubin, Saphire ergänzt.
- 5 neue GameTests (`GtOreGameTests`, insgesamt 185 grün), `build` und `runClient` erfolgreich.
- Nicht umgesetzt (fehlende Items, §6.6 „später“): Galena → Silber-/Bleibarren (Hochofen), Bauxit-/Rubin-/Olivin-
  Elektrolyse (Titan, Chrom, Magnesium), Platin-/Granat-Zentrifuge, Zinkbarren, Datenspeicherchips.

## Nächste Schritte
1. Manuelle Client-Tests 8c: neue Welt → Galena/Iridium unter y 32 bzw. 99, Rubin in Wüste/Badlands/Savanne,
   Saphir an Ozean/Strand, Bauxit in Ebenen/Wäldern (y 32–79); Nether Pyrit/Zinnober/Sphalerit; End Erze und
   Asteroiden. Abbau mit/ohne Glück und Behutsamkeit, Macerator/Ofen, Pyriterz + Calciumcarbonatzelle im Hochofen.
2. Phase 8d: Industrial Grinder inkl. aller Erz-Rezepte (`07-weltgenerierung.md` §6.4).
3. Danach Freischaltungen aus §6.6 „später“ (Titan aus Bauxit, Silberbarren, Chrom, Magnesium …) – Umfang mit dem
   Nutzer klären.

## Spezifikation 8d (2026-10-16)
- `13-gregtech-maschinen.md` §10 Industrial Grinder (Struktur, Wassertank, Tick, GUI, Rezept, Schleifköpfe, alle
  erreichbaren Grinder-Rezepte inkl. Netherrack und Vanilla-/IC2-Erze, neue Items).
- `04-maschinen.md` Pumpe: Details aus `TileEntityPump` (Tick, Eigenheiten, `distributeDrop`, GUI) und Bergbaurohr.
- **Entschieden (Nutzer, 2026-10-16):** Das Grinder-Rezept braucht die IC2-Pumpe → sie wird zuerst gebaut.
  Teilpakete: **8d.1 IC2-Pumpe + Bergbaurohr**, **8d.2 Industrial Grinder**.
- Natriumpersulfatzelle hat in GT 2.90 keine Herstellung → ihre Grinder-Rezepte entfallen. Wolfram-Schleifkopf später.

## Erledigt (Teilpaket „Phase 8d.1: IC2-Pumpe“, 2026-10-16)
- `Ic2Machine.PUMP` (`craft:ic2_pump`, Härte 2, Drop Maschinenblock, Wrench-Rate 1,0), `PumpBlockEntity` 1:1 nach
  `TileEntityPump` (200 EU Ladung, Wasser/Lava-Quelle → Zelle/Eimer/Truhen-Eimer, Lava zuerst an Geothermie,
  andere Flüssigkeiten wie BC-Öl in den Eimer, Eimerstapel-Eigenheit), `StackUtil.distributeDrop`.
- `PumpMenu`/`PumpScreen` (`GUIPump.png`, Slot 0 nur leere Zellen, Shift-Klick bevorzugt Slot 1 wie im Original),
  Loop-Sound `machines/pump_op` beim Laden.
- Compressor + Pumpe: Schneeball aus dem Wasser unter einer bereiten Pumpe (`RecipeMachineBlockEntity`).
- IC2-Bergbaurohr `craft:ic2_mining_pipe` (6–10 px, Härte 6) + Rezept (8 aus raff. Eisen + Baumzapfer);
  Pumpen-Rezept `cCc/cMc/PTP`. Texturen aus `block_machine.png` (Spalte 8) und `block_0.png` (35).
- 5 neue GameTests (`PumpGameTests`, insgesamt 190 grün), `build` und `runClient` (Hauptmenü, keine Mod-Fehler)
  erfolgreich. Abweichungen in `abweichungen.md`.

## Nächste Schritte
1. Teilpaket 8d.2: Industrial Grinder (`13-gregtech-maschinen.md` §10).
2. Manuelle Client-Tests 8d.1: Pumpe über Wasser/Lava mit Generator/BatBox → GUI (Energie, Ladebalken), leere Zellen
   in Slot 0 → Wasserzellen in Truhe daneben, Truhe mit leeren Eimern, Geothermie-Generator daneben mit Lava,
   Öl-Quelle + Eimer per Trichter, Loop-Sound beim Laden; Compressor neben der Pumpe → Schneebälle; Bergbaurohr
   platzieren/abbauen.
