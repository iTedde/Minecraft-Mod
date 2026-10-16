package dev.craft.gregtech;

import java.util.function.BiFunction;

import dev.craft.gregtech.machine.AssemblerBlockEntity;
import dev.craft.gregtech.machine.BenderBlockEntity;
import dev.craft.gregtech.machine.BlastFurnaceBlockEntity;
import dev.craft.gregtech.machine.CannerBlockEntity;
import dev.craft.gregtech.machine.CentrifugeBlockEntity;
import dev.craft.gregtech.machine.ElectrolyzerBlockEntity;
import dev.craft.gregtech.machine.GtMachineBlockEntity;
import dev.craft.gregtech.machine.ImplosionCompressorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** GregTech machines (meta tile entities of {@code GT_BlockMetaID_Machine}), spec 13. */
public enum GtMachine {
    PLATE_BENDING_MACHINE("plate_bending_machine", "bender", Layout.BASIC, 32, 238, 239,
            "Boo, he's bad! We want BENDER!!!", BenderBlockEntity::new),
    INDUSTRIAL_ELECTROLYZER("industrial_electrolyzer", "electrolyzer", Layout.SIDES, 128, 64, 65,
            "Give your Water a shocking Experience with this Device", ElectrolyzerBlockEntity::new),
    INDUSTRIAL_BLAST_FURNACE("industrial_blast_furnace", "blast_furnace", Layout.FRONT, 128, 68, 69,
            "You'll have a Blast!", BlastFurnaceBlockEntity::new),
    ASSEMBLING_MACHINE("assembling_machine", "assembler", Layout.BASIC, 32, 224, 225,
            "Avengers, Assemble!", AssemblerBlockEntity::new) {
        @Override
        public int getTopTexture(boolean active) {
            return 237;
        }
    },
    AUTOMATIC_CANNING_MACHINE("automatic_canning_machine", "canner", Layout.BASIC, 32, 254, 255,
            "Unmobile Food Canning Machine GTA4", CannerBlockEntity::new),
    IMPLOSION_COMPRESSOR("implosion_compressor", "implosion", Layout.TOP, 32, 68, 69, "Over 9000 Gibbl!",
            ImplosionCompressorBlockEntity::new),
    INDUSTRIAL_CENTRIFUGE("industrial_centrifuge", "centrifuge", Layout.SIDES, 32, 27, 25,
            "And round and round and round it goes", CentrifugeBlockEntity::new) {
        @Override
        public int getTopTexture(boolean active) {
            return active ? 41 : 43;
        }
    };

    /** {@link Layout#FRONT}: back and remaining sides of the blast furnace ({@code terrain.png}). */
    public static final int FRONT_LAYOUT_BACK = 71;
    public static final int FRONT_LAYOUT_SIDE = 72;
    /** {@link Layout#TOP}: bottom, east/west and north/south textures of the implosion compressor. */
    public static final int TOP_LAYOUT_BOTTOM = 71;
    public static final int TOP_LAYOUT_EAST_WEST = 73;
    public static final int TOP_LAYOUT_NORTH_SOUTH = 74;

    private final String name;
    private final String gui;
    private final Layout layout;
    private final int maxInput;
    private final int frontInactive;
    private final int frontActive;
    private final String description;
    private final BiFunction<BlockPos, BlockState, ? extends GtMachineBlockEntity> factory;

    GtMachine(String name, String gui, Layout layout, int maxInput, int frontInactive, int frontActive,
              String description, BiFunction<BlockPos, BlockState, ? extends GtMachineBlockEntity> factory) {
        this.name = name;
        this.gui = gui;
        this.layout = layout;
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

    public Layout getLayout() {
        return layout;
    }

    /** {@code GT_MetaTileEntity_BasicMachine}: front and output side textures, main facing. */
    public boolean isBasicMachine() {
        return layout == Layout.BASIC;
    }

    /** {@code maxEUInput} without transformer upgrades (tooltip "Max EU/p IN"). */
    public int getMaxInput() {
        return maxInput;
    }

    /** Front (basic machine, front layout) or side texture index on GregTech's {@code terrain.png}. */
    public int getFrontTexture(boolean active) {
        return active ? frontActive : frontInactive;
    }

    /** Top texture of a basic or side-layout machine ({@code getTopFacingActive/Inactive}), 29 unless overridden. */
    public int getTopTexture(boolean active) {
        return 29;
    }

    /** GT tooltip description ({@code getDescription}). */
    public String getDescription() {
        return description;
    }

    public BiFunction<BlockPos, BlockState, ? extends GtMachineBlockEntity> getFactory() {
        return factory;
    }

    /** Facing and texture layout ({@code isFacingValid} / {@code getTextureIndex}). */
    public enum Layout {
        /** BasicMachine: all six facings, fixed main facing with the front texture, output side textures. */
        BASIC,
        /** No facing: bottom 32, top texture (29 unless overridden), own texture on the four sides. */
        SIDES,
        /** Horizontal facing only: front texture, back 71, everything else 72. */
        FRONT,
        /** Facing fixed down, textures on absolute sides: front texture on top, bottom 71, east/west 73, north/south 74. */
        TOP
    }
}
