package dev.craft.gregtech;

import java.util.function.BiFunction;

import dev.craft.gregtech.machine.BenderBlockEntity;
import dev.craft.gregtech.machine.GtBasicMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** GregTech machines (meta tile entities of {@code GT_BlockMetaID_Machine}), spec 13. */
public enum GtMachine {
    PLATE_BENDING_MACHINE("plate_bending_machine", "bender", 238, 239, "Boo, he's bad! We want BENDER!!!",
            BenderBlockEntity::new);

    private final String name;
    private final String gui;
    private final int frontInactive;
    private final int frontActive;
    private final String description;
    private final BiFunction<BlockPos, BlockState, ? extends GtBasicMachineBlockEntity> factory;

    GtMachine(String name, String gui, int frontInactive, int frontActive, String description,
              BiFunction<BlockPos, BlockState, ? extends GtBasicMachineBlockEntity> factory) {
        this.name = name;
        this.gui = gui;
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

    /** Front texture index on GregTech's {@code terrain.png}. */
    public int getFrontTexture(boolean active) {
        return active ? frontActive : frontInactive;
    }

    /** GT tooltip description ({@code getDescription}). */
    public String getDescription() {
        return description;
    }

    public BiFunction<BlockPos, BlockState, ? extends GtBasicMachineBlockEntity> getFactory() {
        return factory;
    }
}
