# Architektur – Kurzüberblick für neue Sitzungen

Zweck: Bausteine wiederverwenden, ohne vorhandenen Code als Muster lesen zu müssen. Bei neuen Basisklassen hier
ergänzen. Basispaket `dev.craft`, Einstieg `Craft` → `Ic2.init`, `BuildCraft.init`, `Forestry.init`, `PowerConverters.init`.

## Registrierung (je Ursprungs-Mod ein Satz Klassen)

| Mod | Klassen |
|---|---|
| IC2 | `Ic2Blocks`, `Ic2Items`, `Ic2BlockEntityTypes`, `Ic2Menus`, `Ic2Recipes`, `Ic2DataComponents`, `Ic2Sounds`, `Ic2Machine` (Enum aller Maschinen) |
| BuildCraft | `BcBlocks`, `BcItems`, `BcBlockEntityTypes`, `BcMenus`, `fluid/BcFluids`, `world/BcFeatures`, `power/BcCapabilities` |
| Forestry | `FrBlocks`, `FrItems`, `FrBlockEntityTypes`, `FrMenus`, `FrFluids`, `FrRecipes`, `FrDataComponents` |
| Power Converters | `PcBlocks`, `PcItems`, `PcBlockEntityTypes`, `PcMenus`, `PowerSystem` (PCU je Einheit), `ConverterType` |

- Capabilities werden in der jeweiligen Init-Klasse (`registerCapabilities`) registriert.
- Creative-Tabs: `core/CraftCreativeTabs` (ein Tab je Mod).
- Client: je Mod eine `@EventBusSubscriber`-Klasse (`Ic2Client`, `BuildCraftClient`, `ForestryClient`, `PowerConvertersClient`) für Screens,
  BER-Layer, Item-Farben, Item-Renderer (BEWLR) und Fluid-Extensions.

## Energie

- **EU** (`core/energy`): `EnergySource`/`EnergySink`/`EnergyConductor` implementieren; Anmeldung im Netz über
  `EnergyNets` (in `onLoad`, Abmeldung in `setRemoved`/`onChunkUnloaded`). Elektrische Items: `ic2/item/ElectricItems`.
  Elektrische Items/Werkzeuge: `ElectricItem` implementieren, Ladebalken über `ElectricItems.barWidth/barColor`;
  Werkzeuge erben von `ic2/item/tool/ElectricToolItem` (abbaubare Blöcke als Block-Tag, `onBlockStartBreak`).
- **MJ** (`buildcraft/power`): Empfänger implementiert `PowerReceptor`, hält einen `PowerProvider`
  (`configure(minRecv, maxRecv, activation, maxStored)`), ruft jeden Server-Tick `provider.update(this, level)` und
  registriert die Capability `BcCapabilities.POWER_RECEPTOR`. Engines suchen Empfänger über diese Capability.
- Zeitbasis überall `level.getGameTime()` (siehe `abweichungen.md`).

## Rüstung

- `ic2/item/armor/Ic2ArmorItem` (Basis, implementiert Forge-`SpecialArmor`; `onArmorTick` nur getragen,
  `onFall` für Stiefel), elektrisch `ElectricArmorItem` (Rüstungsbalken dynamisch nach Ladung). Material je
  Rüstungstextur in `Ic2ArmorMaterials`. Schaden läuft für Spieler mit IC2-Rüstung über
  `ArmorProperties.applyArmor` (Hooks in `ArmorEvents`).
- Mob-Effekte: `ic2/Ic2MobEffects`; Damage-Types `core/CraftDamageTypes` (+ Tags in `CraftDamageTypeTagsProvider`).
- Access Transformer: `src/main/resources/META-INF/accesstransformer.cfg`.

## Maschinen-Basisklassen

| Klasse | Zweck |
|---|---|
| `ic2/machine/MachineBlockEntity`, `MachineBlock` | IC2-Maschinen: Facing, Schraubenschlüssel (`Wrenchable`), Menü, Drops (`dropContents`) |
| `ic2/machine/ElectricMachineBlockEntity` | EU-Verbraucher mit Upgrades; `RecipeMachineBlockEntity` für Macerator/Extractor/Compressor |
| `ic2/generator/BaseGeneratorBlockEntity` | EU-Erzeuger |
| `ic2/storage/ElectricStorageBlockEntity` | BatBox, MFE, MFSU (Werte in `Spec`) |
| `ic2/storage/TransformerBlockEntity` | LV-/MV-/HV-Trafo (Werte in `Spec`); `Ic2Machine.hasMenu() = false` → kein Menü, keine Item-Capability |
| `ic2/machine/MachineInventory` | `ItemStackHandler` mit Änderungs-Callback; auch von Forestry genutzt |
| `buildcraft/engine/EngineBlockEntity` | BC-Engines (Kolben, Übertragung, Hitze) |
| `forestry/machine/ForestryMachineBlockEntity`, `ForestryMachineBlock` | Forestry-`TileMachine`: MJ, Arbeitsschritt alle 5 Ticks, Ausrichtung zum Spieler, Drops |
| `forestry/engine/ElectricalEngineBlockEntity` | Forestry-Engine (EU-Senke + MJ-Kolben, Hitze, Platinen-Sockel); eigenständig, nicht von der BC-Engine abgeleitet (int-Werte, anderes `extractEnergy`) |
| `powerconverters/power/BridgeComponentBlockEntity` | Konverter-Bauteil: Nachbar-Cache, „verbunden“-Seiten als Blockstate (`PowerConverterBlock.property(side)`); Consumer/Producer-Basisklassen, `EnergyBridgeBlockEntity` |
| `core/multiblock/MultiblockPattern`, `MultiblockController` | gemeinsames Multiblock-System |
| `core/explosion/Ic2Explosion` | IC2-Explosion (`explodeMachineAt`) |

## Tasten und Netzwerk

- IC2-Tasten (Alt/Boost/Modus/Seiteninventar + Vorwärts/Springen): serverseitig `ic2/keyboard/Ic2Keyboard.isModeSwitchKeyDown(player)` usw.
- Eigene Payloads: in `Ic2Keyboard.registerPayloads` (Registrar Version „1“) ergänzen bzw. analog registrieren.
- Entities: `Ic2EntityTypes`; Renderer in `Ic2Client.registerEntityRenderers`.

## Menüs und GUIs

- Sync von Zahlen: `ic2/menu/SyncedInts` (Server: Lieferanten, Client: Anzahl) als `ContainerData`.
- IC2: `ic2/menu/MachineMenu` (Shift-Klick wie IC2), Slots `MachineSlot`/`FilteredSlot`/`OutputSlot`.
- Forestry: `forestry/menu/ForestryMenu` (Phantom-Slots über `PhantomSlot`, Shift-Klick wie Forestry),
  `forestry/client/ForestryScreen` (Titel, Flüssigkeitsanzeige, Tank-Tooltip).
- Menütypen mit Blockposition über `IMenuTypeExtension.create(... buf.readBlockPos())`.

## Flüssigkeiten

- Behälter wie in 1.4.7: `buildcraft/fluid/LiquidContainers` (`getLiquidForFilledItem`, `fillLiquidContainer`,
  `consumeItem`).
- Neue Flüssigkeit: FluidType + Source/Flowing in der Fluids-Klasse des Mods, Textur über Fluid-Extension im Client.

## Rezepte

- Werkbank IC2: `Ic2ShapedRecipe`/`Ic2ShapelessRecipe` (kein Spiegeln, Ladungsübertragung); BC/Forestry: Vanilla
  `ShapedRecipeBuilder` (Spiegeln erlaubt). Positionsgebundene IC2-Rezepte (UU-Materie): `.fixed()` im
  `ShapedBuilder` → `FixedPosition`; Rasterlage über `core/crafting/GridPosition` + `mixin/CraftingInputMixin`.
- Maschinenrezepte als Datapack-Rezepttypen: `ic2/recipe/MachineRecipe` (Macerator/Extractor/Compressor),
  `forestry/recipe/FabricatorRecipe`, `CarpenterRecipe`. Code-Tabellen nur für Kleinkram (`RefineryRecipe`,
  `FabricatorSmelting`).

## Datagen (`dev.craft.data`) – wo was ergänzt wird

| Inhalt | Klasse |
|---|---|
| Blockstates/Blockmodelle | `CraftBlockStateProvider` (BER-Blöcke: leeres Modell mit Partikeltextur, `horizontalBlock`) |
| Item-Modelle | `CraftItemModelProvider` (`engine(...)` = `builtin/entity` mit Block-Transforms, `twoLayers` für getönte Items) |
| Loot | `CraftBlockLoot` (`getKnownBlocks` umfasst alle Block-Register) |
| Tags | `CraftBlockTagsProvider`, `CraftItemTagsProvider` (Konstanten `INGOTS_TIN`, `INGOTS_BRONZE` …) |
| Sprache | `CraftLanguageProvider` (en_us/de_de; Forestry über `forestry(...)`) |
| Rezepte | `CraftRecipeProvider` (Abschnitte je Mod) |
| Weltgen, Data Maps, Sounds | `CraftWorldGen`, `CraftDataMapProvider`, `CraftSoundDefinitionsProvider` |

## Tests und Werkzeuge

- GameTests: `@GameTestHolder(Craft.MODID)` + `@PrefixGameTestTemplate(false)`, Strukturen `empty_8x4x8`,
  `empty_16x8x16`. MJ in Tests: `getPowerProvider().setEnergyStored(...)` per `helper.onEachTick`.
- Befehle (Bash, `export JAVA_HOME="$PWD/tools/jdk-21.0.12.1+1"`): `./gradlew compileJava -q`, `runData -q`,
  `runGameTestServer -q | grep "tests passed\|failed"`, `build -q`, `runClient` im Hintergrund (Log auf
  „Sound engine started“ prüfen, danach Prozess beenden).
- Texturen aus Original-Jars schneiden: `tools/scripts/SliceSprites.java`, Flüssigkeits-FX: `BcLiquidTextures.java`,
  `ForestryTextures.java` (mit `tools/jdk…/bin/java <Skript>.java` ausführen).

## Tokensparend arbeiten

- Spezifikation vor der Umsetzung vollständig machen (eigene kurze Sitzung), dann Originalcode nicht öffnen.
- Ausgaben von Gradle/Git immer filtern (`-q`, `| tail`, `| grep`).
- Diese Datei statt vorhandenem Code als Muster lesen; nur gezielt nachgreppen.
