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
- Kabel-Texturen sind aus dem Original-IC2-Jar geschnitten (privates Projekt) – endgültige Texturen in Phase 10.
- Elektrische Items (Ladung, §5 der EU-Spec) kommen mit Phase 3 (Batterien).

## Entscheidungen nach Phase 2 (2026-10-14)
- Phase 2 abgenommen.
- Explosions-Versatz bei negativen Koordinaten (IC2 `(int)`-Abschneiden) bleibt wie im Original.
- Original-IC2-Texturen dürfen im (privaten) Repo liegen.

# Stand Phase 3 (2026-10-14, Zwischenstand – **baut noch nicht**)

## Entscheidungen (Phase 3)
- GT-Materialien über **1:1 nachgebaute GT-Maschinen** (neue Phase 6b). Solarpanel, Wasser- und Windmühle bis dahin
  **ohne Rezept** (Blöcke funktionieren, Creative). Weg im Pack: `docs/spec/12-gregtech-materialien.md` §4.
- Rohmetall (Eisen/Gold/Kupfer) → Macerator → 2 Staub; Erzblock ebenso. (Tiefenschiefer-)Kupfererz droppt **1**
  Rohkupfer (Loot-Tabelle überschreiben). Zinnerz droppt sich selbst. → `abweichungen.md`.
- Phase 3 enthält alle Vorprodukte (Materialien, Eisenofen, Gummibaum, Zinnerz + Weltgen, Baumzapfer,
  Schraubenschlüssel, Zellen, Energie-/Lapotronkristall, Upgrades inkl. 10k-Kühlzelle als Item).
  Transformer-Upgrade-Rezept erst mit MV-Trafo (Phase 6).

## Geschrieben (unkompiliert, ungetestet)
- `ic2/Ic2DataComponents` (`craft:charge`), `ic2/Ic2Machine` (Enum: Sheet/Spalte, Härte, Facing-Modus,
  Wrench-Rate, Drop), `ic2/Ic2Recipes` (Typen/Serializer macerator/extractor/compressor, `ic2_shaped`,
  `ic2_shapeless`).
- `ic2/item/`: `ElectricItem`, `ElectricItems` (charge/discharge/use/chargeFromArmor), `BatteryItem` (RE-Batterie
  = **ein** Item, leer stapelbar 16 über `MAX_STACK_SIZE`-Component – noch in abweichungen.md eintragen),
  `SingleUseBatteryItem`, `UpgradeModuleItem`, `EmptyCellItem`, `WrenchItem`, `TreetapItem`.
- `ic2/machine/`: `MachineBlock` (FACING 6 Richtungen + ACTIVE im Blockstate), `MachineBlockEntity`,
  `MachineInventory`, `Wrenchable`, `FacingMode`, `ElectricMachineBlockEntity` (Standardmaschine inkl. Upgrades,
  Block-Events 0/1/2 für Sounds), `RecipeMachineBlockEntity` (Macerator/Extractor/Compressor),
  `ElectricFurnaceBlockEntity`, `RecyclerBlockEntity` (Blacklist-Tag `craft:recycler_blacklist`),
  `IronFurnaceBlockEntity`, `MachineSoundHooks`.
- `ic2/generator/`: `BaseGeneratorBlockEntity`, `Generator-`, `GeothermalGenerator-` (Lava-FluidHandler),
  `SolarPanel-`, `WaterMill-`, `WindMillBlockEntity`, `Wind` (globale Windstärke), `FuelValues`.
- `ic2/storage/`: `ElectricStorageBlockEntity` (BatBox/MFE/MFSU-Logik), `ElectricStorageBlock` (Redstone-Signal).
- `ic2/menu/`: `MachineMenu` (IC2-Shift-Klick), `SyncedInts` (int über 2×16 Bit), `FilteredSlot`, `SlotFilters`.
- `ic2/recipe/`: `MachineRecipe` (+Kind-Enum), `Ic2ShapedRecipe` (nicht spiegelbar), `Ic2ShapelessRecipe`,
  `ChargeTransfer`.
- Werkzeug `tools/scripts/SliceSprites.java` (16×16-Kacheln aus IC2-Spritesheets; Sheets/Sounds entpackt nach
  Scratchpad, ggf. neu aus `reference/jars/IC2.jar` holen: `ic2/sprites/*`, `ic2/sounds/*`).

## Nächste Schritte
1. Fehlende Klassen, auf die schon verwiesen wird: `Ic2Items` (alle Items, u. a. `SU_BATTERY`, `LAVA_CELL`,
   `WATER_CELL`, `SCRAP`, `RESIN`), `Ic2Blocks.machine(Ic2Machine)`, `Ic2BlockEntityTypes.get(Ic2Machine)`,
   `Ic2Sounds` (`WRENCH`, `TREETAP`, Maschinen-/Generator-Sounds), `block/RubberWoodBlock` (Property `RESIN`
   NONE/NATURAL/WET/DRY mit `hasHole()`, `FACING`, `TICK_RATE = 100`, Regeneration 1/200), Gummiblätter
   (1/35 Setzling, Birkenfarbe), Gummisetzling (Licht ≥ 9, 1/30; Knochenmehl immer), Menüs `GeneratorMenu`,
   `GeothermalGeneratorMenu`, `SolarPanelMenu`, `WaterMillMenu`, `WindMillMenu`, `ElectricMachineMenu`,
   `IronFurnaceMenu`, `ElectricStorageMenu` (+ Redstone-Modus-Knopf per Payload), Menütypen, Screens
   (IC2-GUI-PNGs, Koordinaten aus `Gui*.java`), Client-Sounds (`MachineSoundHooks` setzen).
2. Registrierung in `Ic2.init`: Data Components, Rezepte, Menüs, Sounds, Capabilities (ItemHandler je Seite,
   FluidHandler Geothermie), Creative-Tab.
3. Datagen: Blockstates/Modelle (Spritesheet-Zeilen 0 unten, 1 oben, 2 hinten, 3 vorne, 4/5 Seiten, +6 aktiv),
   Item-Modelle (Batterie-Ladestufen per Predicate), Lang, Loot (Drop Generator/Maschinenblock, Kupfererz 1 Rohkupfer),
   Tags (Recycler-Blacklist, Logs, Erze, `c:`-Tags), Brennwerte-Datamap (Gummisetzling 80, Zuckerrohr/Kaktus 50,
   Schrott 350, Schrottbox 3150, Lavazelle 20000), alle Rezepte aus `08-rezepte.md` (§1–5, 7, 9, 10).
4. Weltgen: Zinnerz (25/Chunk Gauss, Größe 6, y 0–39), Kupfer ersetzen (15/Chunk, Größe 10, y 10–68), Gummibäume
   (`WorldGenRubTree` 1:1, Biom-Tags taiga/forest/jungle/swamp).
5. Texturen schneiden (Indizes: Items `item_0.png` z. B. Batterien 16/21/26/31, Upgrades 176+; Blöcke
   `block_0.png` Gummiholz 44–47, Blätter 52, Setzling 38, Zinnerz 33, Metallblöcke 93/94/78).
6. GameTests (Generator, Maschinen, BatBox, Upgrades, Überspannung), `gradlew build`, `runClient`, Doku.

## Offen / später
- Alt-Taste beim Schraubenschlüssel (zyklisches Drehen) kommt mit dem Tastensystem (Modus/Boost/Alt).
- Compressor+Pumpe, Schrottbox-Nutzung (IC2-Liste; GT-Erweiterung offene Entscheidung 4).
- Spec-Korrektur `01-eu-system.md` §4: bei `explodeMachineAt` wird das Inventar **gedroppt** (breakBlock), nicht
  vernichtet.
