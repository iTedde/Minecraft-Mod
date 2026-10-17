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
| Kraftwerke (neu) | `PpBlocks`, `PpItems`, `PpBlockEntityTypes`, `PpMenus`, `PpFluids` (Dampf) |
| RedPower | `RpItems` (`tool(gem, Tool)`, `tools()`, Enum `Tool` mit GT-Staubmenge; Baupläne, Projekttisch-Item), `RpGem` (Material/Tier, Edelstein-Tag, Staub), `SickleItem`, `RpBlocks`, `RpBlockEntityTypes`, `RpMenus`, `RpDataComponents` (`craft:plan`); Projekttisch in `redpower/table` (`ProjectTableCrafting` = Regeln aus spec 20 §1.3 auf dem 28-Slot-Inventar, Ergebnis-Slot `ProjectTableResultSlot`), Screen `redpower/client/ProjectTableScreen` |

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
- Erzeugungsanzeige: Erzeuger implementieren `core/energy/OutputReadout` und zählen ihre Erzeugung je Tick mit
  `OutputMeter.record` (Sekundenmittel); Teile von Multiblocks reichen an den Controller weiter. Der Client fragt
  den Wert des angeschauten Blocks alle 10 Ticks über `OutputPayloads` ab, `core/client/OutputOverlay` zeichnet ihn.

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
| `core/explosion/PointExplosion` | IC2-Würfelexplosion (Dynamit): `new PointExplosion(level, x, y, z, Stärke, Drop, Schaden).explode(radius)`; IC2-Resistenzwerte aus `Ic2Explosion.explosionResistance` |
| `powerplants/block/PlantControllerBlock(Entity)`, `PlantPartBlock(Entity)`, `SlotAccess` | Kraftwerks-Multiblocks: Controller prüft Struktur (bei Teil-Änderung, sonst alle 100 Ticks), least die Teile, `tickFormed`; Teile (ein gemeinsamer BE-Typ `plant_part`) reichen Item-/Fluid-Capability an den Controller weiter (`getPartItemHandler`/`getPartFluidHandler`); Turbinengehäuse sind `ConductingPartBlockEntity` (Fabrik `PlantPartBlock.createPart`): verlustfreier EU-Leiter nur für die eigene geformte Struktur, Controller ruft beim Formen/Zerfallen `EnergyNets.rejoin` |
| `ic2/block/Ic2ExplosiveEntity`, `IndustrialTntBlock`, `NukeBlock` | IC2-Sprengstoffe: gezündete Entity (Fuse synchronisiert, Werte über abstrakte Getter, Schadensart über `getDamageSource`, Renderer `Ic2ExplosiveRenderer`); Block zündet bei Abbau/Feuerzeug/Redstone/Explosion (`canExplodeByHand = false` → Abbau droppt, Hook `onIgnite`) |
| `ic2/block/DynamiteEntity`, `DynamiteBlock`, `ic2/item/DynamiteItem`, `DynamiteRemoteItem` | Dynamit: Wurf-Entity (`Projectile`, eigene Flugbahn, `ItemSupplier` für `ThrownItemRenderer`, Werfer über `ProjectileItem`); Fackel-artiger Block (`SUPPORT`), `ignite`/`explode` statisch für Fernzünder |
| `gregtech/machine/GtMachineBlockEntity` | GT-`BaseMetaTileEntity` (Spec 13 §1.1–1.6): Startverzögerung, EU-Netz, Upgrades als Zähler (Flags `is*Upgradable`), Entladen (`dechargerSlot`), Explosionen; Unterklasse implementiert `onPostTick` und die Wert-Hooks (`maxEUInput` …). Nicht-Basismaschinen (`GtMachine.isBasicMachine() = false`, z. B. Elektrolyseur): kein Facing, Texturen unten 32/oben `getTopTexture` (29)/Seiten eigene |
| `gregtech/machine/GtTankMachineBlockEntity`, `menu/GtTankMachineMenu`, `client/GtTankMachineScreen` | Elektrolyseur/Zentrifuge: 2 Eingaben, 4 Ausgaben, Ein-Fluid-Tank mit Holo-Slot 6 als zweite Rezeptquelle; Unterklasse liefert nur Rezeptart, Fluid, Holo-Item, NBT-Schlüssel; Slot-Lagen und GUI je `GtMachine` |
| `gregtech/machine/GtBasicMachineBlockEntity`, `GtMachineBlock` | GT-BasicMachine (Spec 13 §1): EU-Senke/-Quelle, Upgrades als Zähler (Rechtsklick), Energie-Explosionen, Slots 1–2 Eingabe, 3–4 Ausgabe, 5 Batterie; Unterklasse implementiert `checkRecipe()` (Muster: `BenderBlockEntity`). Menü `GtBasicMachineMenu` + `GtBasicMachineScreen` für alle Basismaschinen (GUI-Textur aus `GtMachine`) |
| `gregtech/machine/BlastFurnaceBlockEntity`, `gregtech/block/GtCasingBlock`, `MachineBlockUpdates` | GT-Multiblock-Muster: Struktur über `MultiblockPattern` (statisches `PATTERN`), geprüft nur nach `onMachineBlockUpdate` (Zähler `mUpdate`); Gehäuse lösen beim Setzen/Abbauen `MachineBlockUpdates.cause` aus. Layout je Maschine über `GtMachine.Layout` (BASIC/SIDES/FRONT); weitere GT-Hooks: `onRightclick` (Item ohne Upgrade), `saveItemData` (Item-NBT beim Schraubenschlüssel) |
| `ic2/machine/InductionFurnaceBlockEntity`, `CanningMachineBlockEntity` | IC2-`TileEntityElecMachine` ohne Upgrades (eigene Energie, `provideEnergy` aus dem Batterie-Slot, Seiten über `getSlotForSide`) – Muster für weitere solche IC2-Maschinen |
| `ic2/machine/MinerBlockEntity` | IC2-Miner: Rohrstrang (`MINING_PIPE` + `MINING_TIP`), Ziel per Scanner-Layer-Scan (`ScannerItem.startLayerScan`, `OreValues`), Flüssigkeiten über `PumpBlockEntity.canHarvest`/`pumpThis` |
| `ic2/machine/TeleporterBlockEntity`, `TeslaCoilBlockEntity` | `blockMachine2` (`Ic2Machine.Sheet.MACHINE2`, ohne Menü): Teleporter zieht EU aus angrenzenden `ElectricStorageBlockEntity` (`getWeightOf`, `setTarget`), Ziel im Item als Data Component `craft:teleporter_target` (`FrequencyTransmitterItem`) |
| `ic2/machine/TerraformerBlockEntity`, `ic2/terraformer/*` | Terraformer: Blaupausen implementieren `TerraformerBlueprint` (`getConsume`, `getRange`, `terraform`), Item `TerraformerBlueprintItem`; Hilfen und 1.4.7-Blockgruppen in `Terraforming` (`getFirstBlockFrom`, `switchGround`, `setBiomeAt`) |
| `ic2/personal/*` | Personal-Blöcke: `PersonalBlockEntity` (Besitzer als UUID, `canAccess`, Menü „offen“/„geschlossen“ über das Öffnungspaket), `PersonalBlock` (unzerstörbar), Safe mit `PersonalSafeRenderer` (BER + Item-Renderer), `PersonalInventories` (IC2-`StackUtil` für die O-Mats), Anzeige-Slot `ic2/menu/DisplaySlot` |
| `gregtech/machine/ImplosionCompressorBlockEntity` | GT-Multiblock ohne Facing (`GtMachine.Layout.TOP`, Texturen auf absoluten Seiten), Würfel unter dem Controller per `MultiblockPattern` mit fester Richtung |
| `gregtech/machine/CannerBlockEntity` | GT-Zwei-Eingaben-Maschine mit zwei Ausgaben (wie Assembler, `outputItem2`) |
| `gregtech/machine/VacuumFreezerBlockEntity` | GT-Multiblock wie der Implosionskompressor (`Layout.TOP`, Würfel unter dem Controller), Seitentexturen über `GtMachine.getTopLayoutSideTexture`; Overclocker und Energiemangel-Fehlercode wie der Grinder |
| `gregtech/machine/GrinderBlockEntity` | GT-Multiblock mit Facing und eigenem Wassertank (Fluid-Capability in `GregTech.registerCapabilities`), vier Ausgaben, Rezept mit virtueller Eingabe 2 aus dem Tank |
| `ic2/reactor/NuclearReactorBlockEntity`, `Reactor`, `ReactorComponent` | IC2-Reaktor: Matrix über `getItemAt`/`setItemAt`, Komponenten implementieren `ReactorComponent` am Item (`ic2/item/reactor/*`, Basis `GradualItem`/`GradualIntItem` für Abnutzung, `HeatStorageItem` für Hitze, `PassiveComponentItem` für Teile ohne Puls/Hitze); `runCycle()` für Tests; Kammern `ReactorChamberBlockEntity` |
| `buildcraft/transport/GenericPipeBlock`, `PipeBlockEntity`, `ItemPipeBlockEntity` | BC-Rohre: ein Block je `PipeType`, Verbindungen als Blockstate (`PipeBlock.PROPERTY_BY_DIRECTION`, Regeln in `PipeConnections.canConnect` + `PipeMaterial`), Quell-/Ausgabeseite als `source`/`output`; Transport in Unterklassen von `PipeBlockEntity` (`acceptsNonPipe`, `tickTransport`, `dropContents`); Items als `TravelingItem`, Sync per `PipeItemPayload`; Maschinen geben über `ItemOutput` in Rohre ab; Phantom-Filter (Diamant/Smaragd) in `ItemPipeBlockEntity.getFilters()`, GUI `menu/PipeFilterMenu` + `client/PipeFilterScreen` (ein Menütyp, Filterzahl im Öffnungspaket); Flüssigkeitsrohre in `FluidPipeBlockEntity` (7 `Section`s, Fluid-Capability je Seite nur Füllen, Sync `PipeFluidPayload`, Renderer `client/PipeFluidRenderer`); Leistungsrohre in `PowerPipeBlockEntity` (Doppelpuffer, `receiveEnergy`/`requestEnergy`, Holz = MJ-Empfänger mit Überhitzung, Sync `PipePowerPayload`, Renderer `client/PipePowerRenderer`); Strukturrohr `StructurePipeBlockEntity`. MJ-Empfänger melden Bedarf über `PowerReceptor.powerRequest()` (Standard 0; BC-Maschinen `PowerProvider.missingEnergyRequest()`) |
| `buildcraft/factory/AutoWorkbenchBlockEntity`, `SpecialInventory` | Automatische Werkbank; `SpecialInventory` = BC-`ISpecialInventory` (Holz-/Smaragdrohr fragen es vor der Item-Capability ab, `ItemPipeBlockEntity.doWork`) |
| `gregtech/machine/AssemblerBlockEntity`, GT-Upgrades | Assembler = Muster für shapeless Zwei-Eingaben-Maschinen. GT-Basis ist `PowerReceptor`; MJ-Capability liefert nur mit MJ-Konverter einen Empfänger (`getPowerProvider() != null`) |

## Kleine IC2-Werkzeuge (Phase 12.1)

- Färbbare Blöcke implementieren `ic2/block/PaintableBlock` (Kabel; Farbroller `PainterItem`, Farbe = IC2-Farbstoff-Meta,
  `PainterItem.dyeColor/dyeTag`). Isolierung: `InsulationCutterItem` + `CableBlock.attack`/`changeType`.
- Werkzeugkasten `ToolboxItem`: „boxable“ über die Item-Tags `craft:toolbox/boxable` (`IBoxable` = ja) und
  `craft:toolbox/not_boxable` (`IBoxable` = nein, z. B. alle `GradualItem`), sonst Stapelgröße ≤ 1 – neue Items
  (Dynamit, Dünger, Gerüst …) dort eintragen.
- Bauschaum (Phase 12.2): `ic2/block/ConstructionFoamBlock` (Sprühziel `canFoamReplace`, `harden`), Wand
  `ConstructionFoamWallBlock` (`COLOR`), Sprüher `FoamSprayerItem.sprayFoam` (auch für Kabel: `CableBlock.setFoam`),
  `ScaffoldBlock` (Stärke/Säule), `LuminatorBlock(Entity)` (`canCableConnectFrom` wird von Kabeln abgefragt).
- Crops (Phase 12.7a): Paket `ic2/crop`. Sorten erben von `CropCard` (IC2-API, Standardwerte wie im Original) und
  werden in `Crops` mit fester ID registriert (dazu Basissaaten `registerBaseSeed`, Biom-Boni). Tile
  `CropBlockEntity` (IC2 `TileEntityCrop`, `forcePlant` für Tests), Block `CropBlock` (Sprite als Blockstate
  `sprite`), Saatbeutel `CropSeedItem` (Data Component `craft:crop_seed`), Cropnalyzer `CropnalyzerItem`/`Menu`
  (Inhalt im Container-Component, Scan in `broadcastChanges`) + `client/CropnalyzerScreen`. Sorten mit gleicher
  Logik teilen sich eine parametrisierte Klasse (`ColorFlowerCrop`, `MetalCrop`, `WartCrop`, `SeedFoodCrop`, 12.7b);
  Items anderer Registries nur per `Supplier`/zur Laufzeit, `Crops` wird erst nach der Registrierung geladen.
  EMI-Ernteseiten: Drop-Liste je Sorte in `Ic2EmiRecipes.cropGains`.
- Kaffee/Fass (Phase 12.8): Paket `ic2/brewing`. Getränke erben von `DrinkItem` (32 Ticks, leerer Steinkrug zurück,
  `drink` serverseitig); gepackter IC2-Wert in `Booze` (Data Component `craft:booze`); `BarrelBlock` (Zapfer-Seite als
  Blockstate `tapped`/`facing`, Linksklick über `attack`) + `BarrelBlockEntity`; Baumzapfer-Zweig in `TreetapItem`.

## Rezeptanzeige (EMI)

- Plugin `compat/emi/CraftEmiPlugin`; EMI-Klassen nur in `compat/emi` (EMI ist optional). Je Mod eine
  Registrierungsklasse (`Ic2EmiRecipes` …) mit Kategorien, Workstations und Rezepten; Anzeige über
  `CraftEmiRecipe` (Eingaben, Pfeil mit Dauer, Ausgaben, Textzeilen), Zahlen über `EmiText`.
  Synthetische Rezept-IDs `craft:/<kategorie>/…`; Kategorienamen `emi.category.craft.<id>` in
  `CraftLanguageProvider.recipeViewer`.
- GUI-Klickbereiche: Screen liefert `List<RecipeArea>` (IC2: `MachineScreen.getRecipeAreas()` überschreiben),
  IDs aus `core/client/RecipeCategories`; das Plugin bildet die IDs auf EMI-Kategorien ab.
- Bereiche außerhalb des GUI-Rechtecks (Ledger): Screen implementiert `core/client/ExtraAreas`; das EMI-Plugin hält
  sie über eine generische Ausschlussfläche frei.
- Forestry-Ledger: `forestry/client/ledger` (`LedgerManager` in `ForestryScreen.ledgers`, Ledger im Konstruktor des
  Screens hinzufügen; Werte kommen über die `SyncedInts` des Menüs). Fehlerzustand `forestry/machine/ErrorState`
  (in `ForestryMachineBlockEntity.errorState` bzw. der Electrical Engine).

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
- Werkbank-Werkzeug, das im Raster bleibt (Schaden +1, am Ende ein anderes Item): `gregtech/item/GtMortarItem` (`getCraftingRemainingItem`); Zutaten „nur unbeschädigt“: `DataComponentIngredient.of(false, DataComponents.DAMAGE, 0, item)`.

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
