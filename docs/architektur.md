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
| GraviSuite | `GsItems` (`simpleItems` = Bauteile, `armorItems`, `toolItems`; Texturen `item/gravisuite/`, Rüstungsmaterial je Textur; GraviTool mit Modellen je Modus über `GraviSuiteClient.TOOL_MODE_PROPERTY`), Einträge im IC2-Tab über `GraviSuite.addToCreativeTab`, HUD oben rechts `gravisuite/client/GraviSuiteHud` (`lines(chest)` je Brustteil), Zustand je Spieler als nicht gespeicherte Datenanhänge (`GraviChestplateItem`, `UltimateSolarHelmetItem`), eigene Sounds `GsSounds`, EMI-Info `compat/emi/GraviSuiteEmiRecipes` |
| Modular Powersuits | `mps/MpsItems` (Bauteile `components()`, `armor()`, Werkzeug, Tisch), `MpsBlocks` (Tinker Table + Block-Entity), `MpsDataComponents`, Module in `mps/module` (`PowerModule` = Definition mit Grund-/Reglerwerten, `MpsModules` = alle 35 mit Pack-Werten, `ModuleData` = eingebautes Modul), Rechnung/Ein-/Ausbau `ModularItems` (`computeProperty`, `hasActiveModule`), Energie in J `MpsEnergy` (Spielervorrat, IC2 50 J/EU), Tisch-Aktionen `TinkerActions` + `MpsPayloads`; Bewegung `MpsMovement` (Spieler-Tick Pre beidseitig, Sprung-/Fall-Event, Energie nur Server) mit Eingabe `MpsInput` (Client `mps/client/MpsInputHandler` + Taste Runter, Payload `MpsPayloads.Input`), Luftbeschleunigung per `mixin/PlayerFlyingSpeedMixin`; Umwelt/Spezial `MpsEnvironment` (Effekte vor, Feeder/Solar/Kinetik nach der Bewegung aus `MpsMovement.tick`, Elektrolyseur im Spieler-Tick, Umschalten `toggleForPlayer` + Payload `MpsPayloads.Toggle`), Färbung `ModularItems.getTintColour` (Item-Farbe + `getArmorLayerTintColor` in `MpsClient`), Rüstungstextur `PowerArmorItem.getArmorTexture`; Tastenbelegungen `mps/client/MpsKeybinds` (Datei, Tick) + `MpsKeybindScreen` (Taste K); Power Tool `MpsTool` (Werkzeugtyp, Harvest-/BreakSpeed-Event, Kosten, Nahkampf, Rechtsklick-Module, Strahl `rayTrace`, Moduswechsel) + `item/PowerToolItem`, Plasmageschoss `mps/entity/PlasmaBoltEntity` (`MpsEntityTypes`) mit `client/PlasmaBoltRenderer`, Mausrad/Modus-HUD `client/MpsToolClient`; EMI-Info-Seiten `compat/emi/MpsEmiRecipes`; Client `mps/client` (`TinkerScreen` ohne Menü, `TinkerTableRenderer` + BEWLR, Tooltip in `MpsClient`) |
| Leuchtschild (neu) | `lightshield/LightShield` (Init, Spieler-Tick, `LivingShieldBlockEvent`), `LsItems`/`LsBlocks`/`LsDataComponents` (`craft:light_on`), `ElectricLightShieldItem` (`ShieldItem` + `ElectricItem`; Blocken nur mit Ladung über `canPerformAction`, `chargeForBlock`, `repairWithXp` für Reparatur über `mixin/ExperienceOrbMixin`), `ShieldLights` (wer leuchtet, Verbrauch 1 EU/4 Ticks); LambDynamicLights-Anbindung `lightshield/client/compat/LightShieldDynamicLights` (Yumi-Entrypoint `lambdynlights:initializer` in `neoforge.mods.toml`, API `compileOnly` von maven.gegy.dev; Spieler-Handler über `DynamicLightHandlers` mit Licht 14 wie eine Fackel, seit 30.6 statt Kegel); Altlast-Block `ShieldLightBlock` (entfernt sich beim nächsten geplanten oder zufälligen Tick); Client `lightshield/client/LightShieldClient` (BEWLR mit Vanilla-`ShieldModel`, eigene Textur `textures/entity/electric_light_shield.png` aus `tools/scripts/light_shield_texture.py`, Eigenschaft `blocking`); EMI-Info-Seite `compat/emi/LightShieldEmiRecipes`; Wiki-Icon aus der Entity-Textur (`ENTITY_ICONS` in `wiki/build_wiki.py`) |
| BuildCraft-Schraubenschlüssel | `buildcraft/item/ToolWrench` (`IToolWrench`): BC-Blöcke `canWrenchWith`/`wrenchUsedWith`, Forestry/TE `isWrench`; umgesetzt von `BcWrenchItem` (inkl. Halbmondhammer) und `GraviToolItem` |
| Iron Chests | `IronChestBlocks`/`IronChestItems` (je `IronChestType` bzw. `ChestUpgradeType`), `IronChestBlockEntityTypes` (ein Typ für alle Kisten), `IronChestMenus` (Kistenart als VarInt im Buffer), `IronChestBlock`/`IronChestBlockEntity` (Deckel, Nutzerzählung, Kristall-Top-Stapel per Update-Tag), `ChestUpgradeItem` (`onItemUseFirst`), `IronChestCatSitGoal`; Client `ironchest/client` (`IronChestRenderer` = 1.4.7-`ModelChest` gespiegelt wie 1.4.7, auch als BEWLR) |

- Capabilities werden in der jeweiligen Init-Klasse (`registerCapabilities`) registriert.
- Creative-Tabs: `core/CraftCreativeTabs` (ein Tab je Mod).
- Client: je Mod eine `@EventBusSubscriber`-Klasse (`Ic2Client`, `BuildCraftClient`, `ForestryClient`, `PowerConvertersClient`) für Screens,
  BER-Layer, Item-Farben, Item-Renderer (BEWLR) und Fluid-Extensions.

## Energie

- **EU** (`core/energy`): `EnergySource`/`EnergySink`/`EnergyConductor` implementieren; Anmeldung im Netz über
  `EnergyNets` (in `onLoad`, Abmeldung in `setRemoved`/`onChunkUnloaded`). Elektrische Items: `ic2/item/ElectricItems`.
  Elektrische Items/Werkzeuge: `ElectricItem` implementieren, Ladebalken über `ElectricItems.barWidth/barColor`;
  Werkzeuge erben von `ic2/item/tool/ElectricToolItem` (abbaubare Blöcke als Block-Tag, `onBlockStartBreak`).
  Eigene Ladelogik (IC2 `ICustomElectricItem`): `ic2/item/CustomElectricItem` – `ElectricItems` reicht alles weiter;
  Anzeigekapazität je Stack über `ElectricItem.getMaxCharge(stack)` (MPS).
- **MJ** (`buildcraft/power`): Empfänger implementiert `PowerReceptor`, hält einen `PowerProvider`
  (`configure(minRecv, maxRecv, activation, maxStored)`), ruft jeden Server-Tick `provider.update(this, level)` und
  registriert die Capability `BcCapabilities.POWER_RECEPTOR`. Engines suchen Empfänger über diese Capability.
- Zeitbasis überall `level.getGameTime()` (siehe `abweichungen.md`).
- Erzeugungsanzeige: Erzeuger implementieren `core/energy/OutputReadout` und zählen ihre Erzeugung je Tick mit
  `OutputMeter.record` (Sekundenmittel); Teile von Multiblocks reichen an den Controller weiter. Der Client fragt
  den Wert des angeschauten Blocks alle 10 Ticks über `OutputPayloads` ab, `core/client/OutputOverlay` zeichnet ihn.
- Verbrauchsanzeige: Verbraucher implementieren `core/energy/ConsumptionReadout` und zählen mit einem eigenen
  `OutputMeter` dort, wo Energie tatsächlich verbraucht wird (nicht bei `injectEnergy`); die Antwort von
  `OutputPayloads` enthält beide Werte, das Overlay zeigt den Verbrauch als zweite Zeile.

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
| `compactsolars/SolarArrayBlockEntity` | stillgelegtes Solar Array (seit 2026-10-24): nur Lade-Slot/GUI, keine Leistung; Feldbonus ×1,2 im 9×9 sitzt in `ic2/generator/SolarPanelBlockEntity.isInField` |
| `core/explosion/Ic2Explosion` | IC2-Explosion (`explodeMachineAt`) |
| `core/explosion/PointExplosion` | IC2-Würfelexplosion (Dynamit): `new PointExplosion(level, x, y, z, Stärke, Drop, Schaden).explode(radius)`; IC2-Resistenzwerte aus `Ic2Explosion.explosionResistance` |
| `powerplants/block/PlantControllerBlock(Entity)`, `PlantPartBlock(Entity)`, `SlotAccess` | Kraftwerks-Multiblocks: Controller prüft Struktur (bei Teil-Änderung, sonst alle 100 Ticks), least die Teile, `tickFormed`; Teile (ein gemeinsamer BE-Typ `plant_part`) reichen Item-/Fluid-Capability an den Controller weiter (`getPartItemHandler`/`getPartFluidHandler`); Turbinengehäuse sind `ConductingPartBlockEntity` (Fabrik `PlantPartBlock.createPart`): verlustfreier EU-Leiter nur für die eigene geformte Struktur, Controller ruft beim Formen/Zerfallen `EnergyNets.rejoin` |
| `powerplants/boiler/BoilerTier`, `turbine/SteamTurbineTier`, `waterturbine/WaterTurbineTier`, `block/PlantShapes` | Stufen klein/groß (Phase 21): Größe, Tanks, Temperatur, Dampf-/EU-Raten, Deckel und Blöcke je Stufe; die Blockentities (`BoilerBlockEntity` usw.) lesen nur ihre Stufe (`getTier()`), die großen Unterklassen (`HighPressureBoilerBlockEntity`, `LargeSteamTurbineBlockEntity`, `LargeWaterTurbineBlockEntity`) setzen nur die Stufe; Muster der Strukturen in `PlantShapes`; Menüs übertragen die Grenzen der Stufe an den Screen |
| `powerplants/windturbine/LargeWindTurbineBlockEntity`, `WindRotorItem`, `client/WindRotorRenderer` | Große Windturbine: Gondel + 8 Masten (Masten leiten wie Turbinengehäuse), Rotor als Item im Slot 0 (Faktor am Item), Leistung nach Windmühlenformel × 20 × Faktor, Renderer zeichnet das 5×5-Rad mit der Textur `textures/entity/wind_rotor/<Rotor>.png` |
| `ic2/block/Ic2ExplosiveEntity`, `IndustrialTntBlock`, `NukeBlock` | IC2-Sprengstoffe: gezündete Entity (Fuse synchronisiert, Werte über abstrakte Getter, Schadensart über `getDamageSource`, Renderer `Ic2ExplosiveRenderer`); Block zündet bei Abbau/Feuerzeug/Redstone/Explosion (`canExplodeByHand = false` → Abbau droppt, Hook `onIgnite`) |
| `ic2/block/DynamiteEntity`, `DynamiteBlock`, `ic2/item/DynamiteItem`, `DynamiteRemoteItem` | Dynamit: Wurf-Entity (`Projectile`, eigene Flugbahn, `ItemSupplier` für `ThrownItemRenderer`, Werfer über `ProjectileItem`); Fackel-artiger Block (`SUPPORT`), `ignite`/`explode` statisch für Fernzünder |
| `gregtech/machine/GtMachineBlockEntity` | GT-`BaseMetaTileEntity` (Spec 13 §1.1–1.6): Startverzögerung, EU-Netz, Upgrades als Zähler (Flags `is*Upgradable`), Entladen (`dechargerSlot`), Explosionen; Unterklasse implementiert `onPostTick` und die Wert-Hooks (`maxEUInput` …). Nicht-Basismaschinen (`GtMachine.isBasicMachine() = false`, z. B. Elektrolyseur): kein Facing, Texturen unten 32/oben `getTopTexture` (29)/Seiten eigene |
| `gregtech/machine/GtTankMachineBlockEntity`, `menu/GtTankMachineMenu`, `client/GtTankMachineScreen` | Elektrolyseur/Zentrifuge: 2 Eingaben, 4 Ausgaben, Ein-Fluid-Tank mit Holo-Slot 6 als zweite Rezeptquelle; Unterklasse liefert nur Rezeptart, Fluid, Holo-Item, NBT-Schlüssel; Slot-Lagen und GUI je `GtMachine` |
| `gregtech/machine/GtBasicMachineBlockEntity`, `GtMachineBlock` | GT-BasicMachine (Spec 13 §1): EU-Senke/-Quelle, Upgrades als Zähler (Rechtsklick), Energie-Explosionen, Slots 1–2 Eingabe, 3–4 Ausgabe, 5 Batterie; Unterklasse implementiert `checkRecipe()` (Muster: `BenderBlockEntity`). Menü `GtBasicMachineMenu` + `GtBasicMachineScreen` für alle Basismaschinen (GUI-Textur aus `GtMachine`) |
| `gregtech/machine/BlastFurnaceBlockEntity`, `gregtech/block/GtCasingBlock`, `MachineBlockUpdates` | GT-Multiblock-Muster: Struktur über `MultiblockPattern` (statisches `PATTERN`), geprüft nur nach `onMachineBlockUpdate` (Zähler `mUpdate`); Gehäuse lösen beim Setzen/Abbauen `MachineBlockUpdates.cause` aus. Layout je Maschine über `GtMachine.Layout` (BASIC/SIDES/FRONT); weitere GT-Hooks: `onRightclick` (Item ohne Upgrade), `saveItemData` (Item-NBT beim Schraubenschlüssel) |
| `thermalexpansion/machine/TeMachineBlockEntity`, `TeMachineType`, `TeMachineBlock`, `TeMachineMenu` | TE-Maschinen (spec 26 §4): MJ-Puffer mit Leistungsrampe, Prozessschleife (`canStart`/`canFinish`/`processStart`/`processFinish`/`transferProducts`), Seitengruppen (`TeMachineType`: Slots je Gruppe, Texturspalte), Redstone-Modus, `ejectItem` (Rohr oder Inventar); ein Menütyp für alle (Typ im Öffnungspaket, Knöpfe über `clickMenuButton`); Rezepte `recipe/TeRecipe` (Typ `craft:te_<kind>`, Suche + Rückfälle in `TeRecipes.find`, Legierungen `findSmelter`) und `recipe/TeFluidRecipe` (Tiegel, Umfüller Füllen/Entleeren); Tank-Maschinen erben `TankMachineBlockEntity` (Tank 10 000 mB, `transferLiquid`, Fluid-Capability je Seite); Eisbereiter/Extruder erben `SelectionMachineBlockEntity` (Produktwahl, Menüknöpfe 41–43), Extruder/Wassersammler sind unbetrieben (`TeMachineType.isPowered`, eigener `serverTick`); Motoren/Energiezelle in `energy/` (`TeEngineBlockEntity` + Dampf/Magma, `EnergyCellBlockEntity`, Datenkomponente `ENERGY_CELL`; Renderer `TeEngineRenderer`/`EnergyCellRenderer`); alle TE-Screens erben `client/TeTabScreen` (Reiter, Redstone-Reiter über `TeRedstoneControl`, Flüssigkeitsanzeige); Leitungen in `transport/` (`ConduitBlockEntity`, Netze `EnergyGrid`/`LiquidGrid` per Flutfüllung, aufgelöst in Anteile bei jeder Änderung; `GridPowerProvider` = verlustbehafteter MJ-Puffer, Marker `LossPowerProvider` von Motoren erkannt); Tesserakte in `transport/` (`TesseractBlockEntity` + Energie/Flüssigkeit/Item, globale `TesseractRegistry` je Typ/Besitzer/Frequenz, Namen als Weltdaten `TesseractNames`, Pakete `TesseractPayloads` für Frequenz/Modus/Zugriff und Namensliste, Screen `TesseractScreen` ohne Spielerinventar über den Hook `TeTabScreen.renderTitles`, Renderer `TesseractRenderer` mit dem Layer der Energiezelle); Tragbarer Tank `block/PortableTankBlock(Entity)` mit `PortableTankRenderer` (Modell-Layer) und Datenkomponente `TeDataComponents.TANK_LIQUID`; Client: `TeMachineModel` (Loader `craft:te_machine`), `TeMachineScreen` + `TeTab` (CoFH-Reiter) |
| `advancedmachines/AdvancedMachineBlockEntity`, `AdvancedMachineType` | Advanced Machines: `Ic2Machine`-Einträge mit Blatt `ADVANCED_MACHINES` (Block `AdvancedMachineBlock`, Menü/Screen im Paket), Hochlauf-Logik; Rezepte über `MachineRecipe.Kind` |
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
- Herkunftstexte der Rohstoffe (spec 24): Text in `data/SourceTexts` (Eintrag = Item-IDs + DE + EN), ausgegeben als
  `source.<namespace>.<path>`; jedes Item mit diesem Schlüssel bekommt automatisch die EMI-Info-Seite
  (`compat/emi/SourceEmiInfo`), den Shift-Tooltip (`core/client/SourceTooltip`) und im Wiki den Abschnitt „Herkunft“.
  Abdeckung prüft `SourceTextsTest` (JUnit; Liste `REQUIRED` je Teilphase erweitern).
- Energie-Steckbrief (Wiki): `data/EnergyProfileProvider` liest beim `runData` per Interfaces/Reflection die Energiewerte
  aller Blockentities und elektrischen Items (Getter-Liste `GETTERS`, Konstanten `CONSTANTS`) nach
  `reports/craft/energy_profile.json`; `wiki/content_energie.py` ergänzt nur Erklärungen. Neue Geräte erscheinen
  automatisch, sofern sie die Interfaces bzw. üblichen Getter haben.
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
- Texturen aus Original-Jars schneiden (`ftb ultimate/mods/*.jar|zip`): `tools/` ist vorhanden (SpecialSource, Vineflower, MCP, JDK); Python 3.12 + Pillow (`%LOCALAPPDATA%/Programs/Python/Python312/python.exe`, nicht im PATH) oder kleines Java-Skript mit ImageIO (Index = Zeile · 16 + Spalte); früher `SliceSprites.java`, `BcLiquidTextures.java`,
  `ForestryTextures.java` (mit `tools/jdk…/bin/java <Skript>.java` ausführen; liegen in `tools/scripts/`).
  GT-Sprites: `ftb ultimate/mods/GregTech.zip` (`terrain.png` für Maschinen, `metaitems/*.png` für Items; Index = Zeile · 16 + Spalte).

## Tokensparend arbeiten

- Spezifikation vor der Umsetzung vollständig machen (eigene kurze Sitzung), dann Originalcode nicht öffnen.
- Ausgaben von Gradle/Git immer filtern (`-q`, `| tail`, `| grep`).
- Diese Datei statt vorhandenem Code als Muster lesen; nur gezielt nachgreppen.
