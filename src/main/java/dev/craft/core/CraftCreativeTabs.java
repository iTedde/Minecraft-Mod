package dev.craft.core;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.advancedmachines.AdvancedMachinesItems;
import dev.craft.buildcraft.BcItems;
import dev.craft.buildcraft.transport.PipeType;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.forestry.FrItems;
import dev.craft.forestry.circuit.CircuitBoardType;
import dev.craft.forestry.circuit.ElectronTube;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.block.GtCasingBlock;
import dev.craft.gregtech.world.GtOre;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.item.BatteryItem;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.armor.Ic2ArmorItem;
import dev.craft.ic2.item.armor.JetpackItem;
import dev.craft.powerconverters.ConverterType;
import dev.craft.powerconverters.PcItems;
import dev.craft.powerplants.PpItems;
import dev.craft.redpower.RpGem;
import dev.craft.redpower.RpItems;
import dev.craft.thermalexpansion.TeDataComponents;
import dev.craft.thermalexpansion.TeItems;
import dev.craft.thermalexpansion.energy.EnergyCellData;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import dev.craft.enderstorage.EsItems;
import dev.craft.mps.MpsItems;
import dev.craft.ironchest.IronChestItems;
import dev.craft.ironchest.IronChestType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CraftCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Craft.MODID);

    public static final Supplier<CreativeModeTab> IC2 = TABS.register("ic2", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.craft.ic2"))
            .icon(() -> Ic2Items.cable(CableType.INSULATED_COPPER).toStack())
            .displayItems((parameters, output) -> {
                for (Ic2Machine machine : Ic2Machine.values()) {
                    if (!machine.isAdvancedMachine()) {
                        output.accept(Ic2Items.machine(machine));
                    }
                }
                output.accept(Ic2Items.MACHINE_CASING);
                for (CableType type : CableType.values()) {
                    output.accept(Ic2Items.cable(type));
                }
                output.accept(Ic2Items.TIN_ORE);
                output.accept(Ic2Items.DEEPSLATE_TIN_ORE);
                output.accept(Ic2Items.TIN_BLOCK);
                output.accept(Ic2Items.BRONZE_BLOCK);
                output.accept(Ic2Items.RUBBER_WOOD);
                output.accept(Ic2Items.RUBBER_LEAVES);
                output.accept(Ic2Items.RUBBER_SAPLING);
                output.accept(Ic2Items.IRON_FENCE);
                output.accept(Ic2Items.REINFORCED_GLASS);
                output.accept(Ic2Items.REINFORCED_STONE);
                output.accept(Ic2Items.RUBBER_MAT);
                output.accept(Ic2Items.CONSTRUCTION_FOAM);
                output.accept(Ic2Items.WOODEN_SCAFFOLD);
                output.accept(Ic2Items.IRON_SCAFFOLD);
                output.accept(Ic2Items.LUMINATOR);
                output.accept(Ic2Items.CROP);
                output.accept(Ic2Items.INDUSTRIAL_TNT);
                output.accept(Ic2Items.NUKE);
                output.accept(Ic2Items.TOOLBOX);
                for (DeferredHolder<Item, ? extends Item> item : Ic2Items.ITEMS.getEntries()) {
                    Item value = item.get();
                    if (Ic2Items.simpleItems().contains(item) && value != Ic2Items.INDUSTRIAL_DIAMOND.get()
                            && value != Ic2Items.COIN.get() && value != Ic2Items.CROP_SEED_BAG.get()) {
                        output.accept(value);
                    } else if (value instanceof BatteryItem) {
                        output.accept(new ItemStack(value));
                        output.accept(ElectricItems.charged(new ItemStack(value)));
                    } else if (value instanceof ElectricItem) {
                        output.accept(ElectricItems.charged(new ItemStack(value)));
                        output.accept(new ItemStack(value));
                    } else if (value instanceof JetpackItem jetpack) {
                        output.accept(jetpack.createFull());
                    } else if (value instanceof Ic2ArmorItem) {
                        output.accept(value);
                    }
                }
            })
            .build());

    public static final Supplier<CreativeModeTab> BUILDCRAFT = TABS.register("buildcraft", () -> CreativeModeTab
            .builder()
            .title(Component.translatable("itemGroup.craft.buildcraft"))
            .icon(() -> BcItems.engine(EngineType.REDSTONE).toStack())
            .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "ic2"))
            .displayItems((parameters, output) -> {
                for (EngineType type : EngineType.values()) {
                    output.accept(BcItems.engine(type));
                }
                output.accept(BcItems.LANDMARK);
                output.accept(BcItems.QUARRY);
                output.accept(BcItems.MINING_WELL);
                output.accept(BcItems.PUMP);
                output.accept(BcItems.TANK);
                output.accept(BcItems.REFINERY);
                output.accept(BcItems.AUTO_WORKBENCH);
                for (PipeType type : PipeType.values()) {
                    output.accept(BcItems.pipe(type));
                }
                for (DeferredHolder<Item, ? extends Item> item : BcItems.simpleItems()) {
                    output.accept(item.get());
                }
            })
            .build());

    public static final Supplier<CreativeModeTab> FORESTRY = TABS.register("forestry", () -> CreativeModeTab
            .builder()
            .title(Component.translatable("itemGroup.craft.forestry"))
            .icon(() -> FrItems.STURDY_CASING.toStack())
            .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "buildcraft"))
            .displayItems((parameters, output) -> {
                output.accept(FrItems.THERMIONIC_FABRICATOR);
                output.accept(FrItems.CARPENTER);
                output.accept(FrItems.ELECTRICAL_ENGINE);
                output.accept(FrItems.STURDY_CASING);
                output.accept(FrItems.TIN_GEAR);
                output.accept(FrItems.SOLDERING_IRON);
                for (ElectronTube tube : ElectronTube.values()) {
                    output.accept(FrItems.tube(tube));
                }
                for (CircuitBoardType type : CircuitBoardType.values()) {
                    output.accept(FrItems.circuitBoard(type));
                }
            })
            .build());

    public static final Supplier<CreativeModeTab> POWER_CONVERTERS = TABS.register("power_converters", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.craft.power_converters"))
                    .icon(() -> PcItems.ENERGY_BRIDGE.toStack())
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "forestry"))
                    .displayItems((parameters, output) -> {
                        output.accept(PcItems.ENERGY_BRIDGE);
                        output.accept(PcItems.UNIVERSAL_CHARGER);
                        for (ConverterType type : ConverterType.values()) {
                            output.accept(PcItems.converter(type));
                        }
                    })
                    .build());

    public static final Supplier<CreativeModeTab> POWER_PLANTS = TABS.register("power_plants", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.craft.power_plants"))
                    .icon(() -> PpItems.COKE_OVEN.toStack())
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "gregtech"))
                    .displayItems((parameters, output) -> PpItems.ITEMS.getEntries()
                            .forEach(item -> output.accept(item.get())))
                    .build());

    public static final Supplier<CreativeModeTab> GREGTECH = TABS.register("gregtech", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.craft.gregtech"))
                    .icon(() -> GtItems.machine(GtMachine.PLATE_BENDING_MACHINE).toStack())
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "power_converters"))
                    .displayItems((parameters, output) -> {
                        for (GtMachine machine : GtMachine.values()) {
                            output.accept(GtItems.machine(machine));
                        }
                        for (GtCasingBlock.Type type : GtCasingBlock.Type.values()) {
                            output.accept(GtItems.casing(type));
                        }
                        output.accept(GtItems.HIGHLY_ADVANCED_MACHINE_BLOCK);
                        output.accept(GtItems.STEEL_BLOCK);
                        for (GtOre ore : GtOre.values()) {
                            output.accept(GtItems.ore(ore));
                            if (ore.hasDeepslateVariant()) {
                                output.accept(GtItems.deepslateOre(ore));
                            }
                        }
                        GtItems.simpleItems().forEach(output::accept);
                        output.accept(GtItems.LITHIUM_BATTERY);
                        output.accept(ElectricItems.charged(GtItems.LITHIUM_BATTERY.toStack()));
                        output.accept(ElectricItems.charged(GtItems.LITHIUM_BATPACK.toStack()));
                        output.accept(GtItems.LITHIUM_BATPACK);
                    })
                    .build());

    public static final Supplier<CreativeModeTab> REDPOWER = TABS.register("redpower", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.craft.redpower"))
                    .icon(() -> RpItems.tool(RpGem.RUBY, RpItems.Tool.PICKAXE).toStack())
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "power_plants"))
                    .displayItems((parameters, output) -> RpItems.ITEMS.getEntries().stream()
                            .filter(item -> item != RpItems.PLAN)
                            .forEach(item -> output.accept(item.get())))
                    .build());

    public static final Supplier<CreativeModeTab> IRON_CHESTS = TABS.register("iron_chests", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.craft.iron_chests"))
                    .icon(() -> IronChestItems.chest(IronChestType.IRON).toStack())
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "redpower"))
                    .displayItems((parameters, output) -> IronChestItems.ITEMS.getEntries()
                            .forEach(item -> output.accept(item.get())))
                    .build());

    public static final Supplier<CreativeModeTab> ADVANCED_MACHINES = TABS.register("advanced_machines", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.craft.advanced_machines"))
                    .icon(() -> Ic2Items.machine(Ic2Machine.ROTARY_MACERATOR).toStack())
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "iron_chests"))
                    .displayItems((parameters, output) -> {
                        for (Ic2Machine machine : Ic2Machine.values()) {
                            if (machine.isAdvancedMachine()) {
                                output.accept(Ic2Items.machine(machine));
                            }
                        }
                        output.accept(AdvancedMachinesItems.REFINED_IRON_DUST);
                    })
                    .build());

    public static final Supplier<CreativeModeTab> THERMAL_EXPANSION = TABS.register("thermal_expansion", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.craft.thermal_expansion"))
                    .icon(() -> TeItems.MACHINE_FRAME.toStack())
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "advanced_machines"))
                    .displayItems((parameters, output) -> TeItems.ITEMS.getEntries().forEach(item -> {
                        output.accept(item.get());
                        // BlockEnergyCell.getSubBlocks: an empty and a full cell.
                        if (item == TeItems.ENERGY_CELL) {
                            ItemStack full = new ItemStack(TeItems.ENERGY_CELL.get());
                            full.set(TeDataComponents.ENERGY_CELL.get(), EnergyCellData.FULL);
                            output.accept(full);
                        }
                    }))
                    .build());

    /** {@code BlockEnderStorage.getSubBlocks}: the chest with frequency 0, global. */
    public static final Supplier<CreativeModeTab> ENDER_STORAGE = TABS.register("ender_storage", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.craft.ender_storage"))
                    .icon(() -> EsItems.ENDER_CHEST.toStack())
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "thermal_expansion"))
                    .displayItems((parameters, output) -> EsItems.ITEMS.getEntries()
                            .forEach(item -> output.accept(item.get())))
                    .build());

    /** In 1.4.7 item-ID order: table, the four armor pieces, tool, components; all without modules. */
    public static final Supplier<CreativeModeTab> MODULAR_POWERSUITS = TABS.register("modular_powersuits", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.craft.modular_powersuits"))
                    .icon(() -> MpsItems.POWER_ARMOR_HEAD.toStack())
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "ender_storage"))
                    .displayItems((parameters, output) -> {
                        output.accept(MpsItems.TINKER_TABLE.get());
                        MpsItems.armor().forEach(item -> output.accept(item.get()));
                        output.accept(MpsItems.POWER_TOOL.get());
                        MpsItems.components().forEach(item -> output.accept(item.get()));
                    })
                    .build());

    private CraftCreativeTabs() {
    }
}
