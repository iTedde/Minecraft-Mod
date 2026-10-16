| `ic2/machine/PumpBlockEntity`, `StackUtil` | IC2-Pumpe (`isPumpReady`/`isWaterBelow`/`resetCharge` für Compressor und später Miner); `StackUtil.distributeDrop(level, pos, stacks)` = IC2-Ausgabe an Nachbarinventare (≥ 6 Slots) bzw. als Item |
# Architektur – Kurzüberblick für neue Sitzungen

Zweck: Bausteine wiederverwenden, ohne vorhandenen Code als Muster lesen zu müssen. Bei neuen Basisklassen hier
ergänzen. Basispaket `dev.craft`, Einstieg `Craft` → `Ic2.init`, `BuildCraft.init`, `Forestry.init`, `PowerConverters.init`, `CompactSolars.init`, `GregTech.init`.

## Registrierung (je Ursprungs-Mod ein Satz Klassen)

| Mod | Klassen |
|---|---|
| IC2 | `Ic2Blocks`, `Ic2Items`, `Ic2BlockEntityTypes`, `Ic2Menus`, `Ic2Recipes`, `Ic2DataComponents`, `Ic2Sounds`, `Ic2Machine` (Enum aller Maschinen) |
| BuildCraft | `BcBlocks`, `BcItems`, `BcBlockEntityTypes`, `BcMenus`, `fluid/BcFluids`, `world/BcFeatures`, `power/BcCapabilities` |
| Forestry | `FrBlocks`, `FrItems`, `FrBlockEntityTypes`, `FrMenus`, `FrFluids`, `FrRecipes`, `FrDataComponents` |
| Power Converters | `PcBlocks`, `PcItems`, `PcBlockEntityTypes`, `PcMenus`, `PowerSystem` (PCU je Einheit), `ConverterType` |
| Compact Solars | `CsBlocks`, `CsItems`, `CsBlockEntityTypes`, `CsMenus`, `SolarArrayType` |
| GregTech | `GtBlocks`, `GtItems` (`simpleItems` = Platten/Komponenten mit Textur unter `item/gt/`), `GtBlockEntityTypes`, `GtMenus`, `GtRecipes`, `GtMachine` (Enum aller GT-Maschinen: Name, GUI, Vorderseiten-Textur, Beschreibung, Fabrik) |

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
| `core/multiblock/MultiblockPattern`, `MultiblockController` | gemeinsames Multiblock-System; `Listener.onChecked` nach jeder Prüfung; `MultiblockLease` = Zugehörigkeit eines Teils (nicht gespeichert) |
| `compactsolars/SolarArrayBlockEntity`, `SolarArrayPatterns` | Solar-Array-Controller (erbt `MachineBlockEntity`, ohne Facing/`Ic2Machine`), rekursive Layouts |
| `core/explosion/Ic2Explosion` | IC2-Explosion (`explodeMachineAt`) |
| `ic2/block/Ic2ExplosiveEntity`, `IndustrialTntBlock`, `NukeBlock` | IC2-Sprengstoffe: gezündete Entity (Fuse synchronisiert, Werte über abstrakte Getter, Schadensart über `getDamageSource`, Renderer `Ic2ExplosiveRenderer`); Block zündet bei Abbau/Feuerzeug/Redstone/Explosion (`canExplodeByHand = false` → Abbau droppt, Hook `onIgnite`) |
| `gregtech/machine/GtMachineBlockEntity` | GT-`BaseMetaTileEntity` (Spec 13 §1.1–1.6): Startverzögerung, EU-Netz, Upgrades als Zähler (Flags `is*Upgradable`), Entladen (`dechargerSlot`), Explosionen; Unterklasse implementiert `onPostTick` und die Wert-Hooks (`maxEUInput` …). Nicht-Basismaschinen (`GtMachine.isBasicMachine() = false`, z. B. Elektrolyseur): kein Facing, Texturen unten 32/oben `getTopTexture` (29)/Seiten eigene |
| `gregtech/machine/GtTankMachineBlockEntity`, `menu/GtTankMachineMenu`, `client/GtTankMachineScreen` | Elektrolyseur/Zentrifuge: 2 Eingaben, 4 Ausgaben, Ein-Fluid-Tank mit Holo-Slot 6 als zweite Rezeptquelle; Unterklasse liefert nur Rezeptart, Fluid, Holo-Item, NBT-Schlüssel; Slot-Lagen und GUI je `GtMachine` |
| `gregtech/machine/GtBasicMachineBlockEntity`, `GtMachineBlock` | GT-BasicMachine (Spec 13 §1): EU-Senke/-Quelle, Upgrades als Zähler (Rechtsklick), Energie-Explosionen, Slots 1–2 Eingabe, 3–4 Ausgabe, 5 Batterie; Unterklasse implementiert `checkRecipe()` (Muster: `BenderBlockEntity`). Menü `GtBasicMachineMenu` + `GtBasicMachineScreen` für alle Basismaschinen (GUI-Textur aus `GtMachine`) |
| `gregtech/machine/BlastFurnaceBlockEntity`, `gregtech/block/GtCasingBlock`, `MachineBlockUpdates` | GT-Multiblock-Muster: Struktur über `MultiblockPattern` (statisches `PATTERN`), geprüft nur nach `onMachineBlockUpdate` (Zähler `mUpdate`); Gehäuse lösen beim Setzen/Abbauen `MachineBlockUpdates.cause` aus. Layout je Maschine über `GtMachine.Layout` (BASIC/SIDES/FRONT); weitere GT-Hooks: `onRightclick` (Item ohne Upgrade), `saveItemData` (Item-NBT beim Schraubenschlüssel) |
| `ic2/machine/InductionFurnaceBlockEntity`, `CanningMachineBlockEntity` | IC2-`TileEntityElecMachine` ohne Upgrades (eigene Energie, `provideEnergy` aus dem Batterie-Slot, Seiten über `getSlotForSide`) – Muster für weitere solche IC2-Maschinen |
| `gregtech/machine/ImplosionCompressorBlockEntity` | GT-Multiblock ohne Facing (`GtMachine.Layout.TOP`, Texturen auf absoluten Seiten), Würfel unter dem Controller per `MultiblockPattern` mit fester Richtung |
| `gregtech/machine/CannerBlockEntity` | GT-Zwei-Eingaben-Maschine mit zwei Ausgaben (wie Assembler, `outputItem2`) |
| `gregtech/machine/VacuumFreezerBlockEntity` | GT-Multiblock wie der Implosionskompressor (`Layout.TOP`, Würfel unter dem Controller), Seitentexturen über `GtMachine.getTopLayoutSideTexture`; Overclocker und Energiemangel-Fehlercode wie der Grinder |
| `gregtech/machine/GrinderBlockEntity` | GT-Multiblock mit Facing und eigenem Wassertank (Fluid-Capability in `GregTech.registerCapabilities`), vier Ausgaben, Rezept mit virtueller Eingabe 2 aus dem Tank |
| `ic2/reactor/NuclearReactorBlockEntity`, `Reactor`, `ReactorComponent` | IC2-Reaktor: Matrix über `getItemAt`/`setItemAt`, Komponenten implementieren `ReactorComponent` am Item (`ic2/item/reactor/*`, Basis `GradualItem`/`GradualIntItem` für Abnutzung, `HeatStorageItem` für Hitze, `PassiveComponentItem` für Teile ohne Puls/Hitze); `runCycle()` für Tests; Kammern `ReactorChamberBlockEntity` |
| `gregtech/machine/AssemblerBlockEntity`, GT-Upgrades | Assembler = Muster für shapeless Zwei-Eingaben-Maschinen. GT-Basis ist `PowerReceptor`; MJ-Capability liefert nur mit MJ-Konverter einen Empfänger (`getPowerProvider() != null`) |

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
- GT-Maschinenrezepte: `gregtech/recipe/GtRecipe` (1–2 `SizedIngredient`, 1–4 Ausgaben, Dauer, EU/t, `special`), eine Liste je `GtRecipe.Kind` (Typ `craft:gt_<name>`), Suche mit `GtRecipe.find`; Datagen-Helfer `plate(...)` in `CraftRecipeProvider`.

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
- Befehle (Bash, `export JAVA_HOME="/c/Program Files/Java/jdk-21"`): `./gradlew compileJava -q`, `runData -q`,
  `runGameTestServer -q | grep "tests passed\|failed"`, `build -q`, `runClient` im Hintergrund (Log auf
  „Sound engine started“ prüfen, danach Prozess beenden).
- Texturen aus Original-Jars schneiden (`ftb ultimate/mods/*.jar|zip`): `tools/` fehlt seit dem Umzug – Python 3.12 + Pillow (`%LOCALAPPDATA%/Programs/Python/Python312/python.exe`, nicht im PATH) oder kleines Java-Skript mit ImageIO (Index = Zeile · 16 + Spalte); früher `SliceSprites.java`, `BcLiquidTextures.java`,
  `ForestryTextures.java` (mit `tools/jdk…/bin/java <Skript>.java` ausführen).
  GT-Sprites: `ftb ultimate/mods/GregTech.zip` (`terrain.png` für Maschinen, `metaitems/*.png` für Items; Index = Zeile · 16 + Spalte).

## Tokensparend arbeiten

- Spezifikation vor der Umsetzung vollständig machen (eigene kurze Sitzung), dann Originalcode nicht öffnen.
- Ausgaben von Gradle/Git immer filtern (`-q`, `| tail`, `| grep`).
- Diese Datei statt vorhandenem Code als Muster lesen; nur gezielt nachgreppen.
