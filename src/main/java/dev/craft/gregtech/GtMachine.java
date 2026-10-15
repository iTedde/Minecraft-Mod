package dev.craft.gregtech;

import java.util.function.BiFunction;

import dev.craft.gregtech.machine.BenderBlockEntity;
import dev.craft.gregtech.machine.ElectrolyzerBlockEntity;
import dev.craft.gregtech.machine.GtMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** GregTech machines (meta tile entities of {@code GT_BlockMetaID_Machine}), spec 13. */
public enum GtMachine {
    PLATE_BENDING_MACHINE("plate_bending_machine", "bender", true, 32, 238, 239,
            "Boo, he's bad! We want BENDER!!!", BenderBlockEntity::new),
    INDUSTRIAL_ELECTROLYZER("industrial_electrolyzer", "electrolyzer", false, 128, 64, 65,
            "Give your Water a shocking Experience with this Device", ElectrolyzerBlockEntity::new);

    private final String name;
    private final String gui;
    private final boolean basicMachine;
    private final int maxInput;
    private final int frontInactive;
    private final int frontActive;
    private final String description;
    private final BiFunction<BlockPos, BlockState, ? extends GtMachineBlockEntity> factory;

    GtMachine(String name, String gui, boolean basicMachine, int maxInput, int frontInactive, int frontActive,
              String description, BiFunction<BlockPos, BlockState, ? extends GtMachineBlockEntity> factory) {
        this.name = name;
        this.gui = gui;
        this.basicMachine = basicMachine;
        this.maxInput = maxInput;
        this.frontInactive = frontInactive;
        this.frontActive = frontActive;
        this.description = description;
        this.factory = factory;
    }

    public String getName() {
        return name;
    }

    /** GUI texture under {@code textures/gui/gt/}. */
    public String getGui() {
        return gui;
    }

    /**
     * {@code GT_MetaTileEntity_BasicMachine}: front and output side textures. Other machines have no facing and use
     * {@link #getFrontTexture} on all four sides.
     */
    public boolean isBasicMachine() {
        return basicMachine;
    }

    /** {@code maxEUInput} without transformer upgrades (tooltip "Max EU/p IN"). */
    public int getMaxInput() {
        return maxInput;
    }

    /** Front (basic machine) or side texture index on GregTech's {@code terrain.png}. */
    public int getFrontTexture(boolean active) {
        return active ? frontActive : frontInactive;
    }

    /** GT tooltip description ({@code getDescription}). */
    public String getDescription() {
        return description;
    }

    public BiFunction<BlockPos, BlockState, ? extends GtMachineBlockEntity> getFactory() {
        return factory;
    }
}
