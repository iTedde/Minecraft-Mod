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

## Nächste Schritte
1. Manuelle Prüfung im Client (Nutzer): GUIs, Sounds, Texturen/Facing, Weltgen (Gummibäume, Erze), Rezepte.
2. Noch fehlend in Phase 3: Harz-Rechtsklick (Harzschicht `blockHarz`, Klebekolben), Schrottbox-Nutzung.
   Wasser-/Windmühle/Solar bleiben ohne Rezept (Phase 6b).
   Entschieden 2026-10-15: Bronze-Werkzeuge/-Rüstung entfallen ganz; GT-Recycling der Vanilla-Eisenteile umgesetzt
   (Eisentür → 2 statt 6).
3. Danach Phase-3-Abnahme.

## Offen / später
- Alt-Taste beim Schraubenschlüssel (zyklisches Drehen) kommt mit dem Tastensystem (Modus/Boost/Alt).
- Compressor+Pumpe. Schrottbox-Drops: IC2-Liste + GT-Erweiterung (entschieden 2026-10-15); GT-Items der Liste kommen mit Phase 6b dazu.
- Upgrade-Stapelgröße 64 (IC2) – GT-Wert 4 ist offene Entscheidung 4.
- Spec-Korrektur `01-eu-system.md` §4: bei `explodeMachineAt` wird das Inventar **gedroppt** (breakBlock), nicht
  vernichtet.
