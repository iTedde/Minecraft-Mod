package dev.craft.ic2.menu;

import java.util.function.IntSupplier;

import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Menus;
import dev.craft.ic2.generator.BaseGeneratorBlockEntity;
import dev.craft.ic2.generator.GeneratorBlockEntity;
import dev.craft.ic2.generator.GeothermalGeneratorBlockEntity;
import dev.craft.ic2.generator.SolarPanelBlockEntity;
import dev.craft.ic2.generator.WaterMillBlockEntity;
import dev.craft.ic2.generator.WindMillBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Generator, geothermal generator, water mill, solar panel and wind mill (IC2 {@code ContainerBaseGenerator},
 * {@code ContainerWaterGenerator}, {@code ContainerSolarGenerator}, {@code ContainerWindGenerator}).
 */
public class GeneratorMenu extends MachineMenu<BaseGeneratorBlockEntity> {
    private static final int STORAGE = 0;
    private static final int MAX_STORAGE = 1;
    private static final int FUEL = 2;
    private static final int FUEL_GAUGE = 3;
    private static final int EXTRA = 4;

    private final Ic2Machine kind;

    public GeneratorMenu(int id, Inventory inventory, BaseGeneratorBlockEntity generator) {
        this(generator.getMachine(), id, inventory, generator, generator.getBlockPos(), new SyncedInts(
                generator::getStorage, generator::getMaxStorage, generator::getFuel,
                fuelGauge(generator), extra(generator)));
    }

    public GeneratorMenu(Ic2Machine kind, int id, Inventory inventory, BlockPos pos) {
        this(kind, id, inventory, null, pos, new SyncedInts(5));
    }

    private GeneratorMenu(Ic2Machine kind, int id, Inventory inventory, @Nullable BaseGeneratorBlockEntity generator,
                          BlockPos pos, SyncedInts data) {
        super(Ic2Menus.get(kind), id, generator, pos, data);
        this.kind = kind;
        IItemHandler items = items(generator, hasFuelSlot(kind) ? 2 : 1);
        switch (kind) {
            case GENERATOR, GEOTHERMAL_GENERATOR -> {
                addMachineSlot(new FilteredSlot(items, 0, 65, 17, SlotFilters.charge(BaseGeneratorBlockEntity.TIER)));
                addMachineSlot(new MachineSlot(items, 1, 65, 53));
            }
            case WATER_MILL -> {
                addMachineSlot(new FilteredSlot(items, 0, 80, 17, SlotFilters.charge(BaseGeneratorBlockEntity.TIER)));
                addMachineSlot(new MachineSlot(items, 1, 80, 53));
            }
            default -> addMachineSlot(new FilteredSlot(items, 0, 80, 26,
                    SlotFilters.charge(BaseGeneratorBlockEntity.TIER)));
        }
        addPlayerInventory(inventory);
    }

    private static boolean hasFuelSlot(Ic2Machine kind) {
        return kind != Ic2Machine.SOLAR_PANEL && kind != Ic2Machine.WIND_MILL;
    }

    /** The fuel gauge in the scale the machine's GUI draws it (generator 12 px, others 14 px). */
    private static IntSupplier fuelGauge(BaseGeneratorBlockEntity generator) {
        if (generator instanceof GeneratorBlockEntity be) {
            return () -> be.gaugeFuelScaled(12);
        }
        if (generator instanceof GeothermalGeneratorBlockEntity be) {
            return () -> be.gaugeFuelScaled(14);
        }
        if (generator instanceof WaterMillBlockEntity be) {
            return () -> be.gaugeFuelScaled(14);
        }
        if (generator instanceof WindMillBlockEntity be) {
            return () -> be.gaugeFuelScaled(14);
        }
        return () -> 0;
    }

    /** Wind mill: overheat gauge (14 px); solar panel: 1 while the sun is visible. */
    private static IntSupplier extra(BaseGeneratorBlockEntity generator) {
        if (generator instanceof WindMillBlockEntity be) {
            return () -> WindMillBlockEntity.getOverheatScaled(be.getSubproduction(), 14);
        }
        if (generator instanceof SolarPanelBlockEntity be) {
            return () -> be.isSunVisible() ? 1 : 0;
        }
        return () -> 0;
    }

    public Ic2Machine getKind() {
        return kind;
    }

    public int getStorage() {
        return data.getInt(STORAGE);
    }

    /** IC2 {@code gaugeStorageScaled}. */
    public int gaugeStorageScaled(int scale) {
        int max = data.getInt(MAX_STORAGE);
        return max <= 0 ? 0 : getStorage() * scale / max;
    }

    public int getFuel() {
        return data.getInt(FUEL);
    }

    public int getFuelGauge() {
        return data.getInt(FUEL_GAUGE);
    }

    public int getOverheatGauge() {
        return data.getInt(EXTRA);
    }

    public boolean isSunVisible() {
        return data.getInt(EXTRA) != 0;
    }

    @Override
    protected int getInputSlot() {
        return hasFuelSlot(kind) ? 1 : 0;
    }
}
