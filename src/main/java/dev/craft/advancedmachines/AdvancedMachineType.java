package dev.craft.advancedmachines;

import java.util.function.Supplier;

import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.recipe.MachineRecipe;
import net.minecraft.sounds.SoundEvent;

/**
 * The three Advanced Machines (spec 25 §4): recipe list, slot layout and speed display of
 * {@code TileEntityRotaryMacerator}, {@code TileEntitySingularityCompressor} and {@code TileEntityCentrifugeExtractor}.
 */
public enum AdvancedMachineType {
    ROTARY_MACERATOR(Ic2Machine.ROTARY_MACERATOR, MachineRecipe.Kind.MACERATOR, new int[]{2, 3}, 4, "%5d RPM", 1,
            Ic2Sounds.MACERATOR_OP, "rotary_macerator"),
    SINGULARITY_COMPRESSOR(Ic2Machine.SINGULARITY_COMPRESSOR, MachineRecipe.Kind.COMPRESSOR, new int[]{2}, 3,
            "%6d PSI", 10, Ic2Sounds.COMPRESSOR_OP, "singularity_compressor"),
    CENTRIFUGE_EXTRACTOR(Ic2Machine.CENTRIFUGE_EXTRACTOR, MachineRecipe.Kind.EXTRACTOR, new int[]{2, 3, 4}, 5,
            "%5d M/S", 1, Ic2Sounds.EXTRACTOR_OP, "centrifuge_extractor");

    public static final int SLOT_ENERGY = 0;
    public static final int SLOT_INPUT = 1;
    public static final int UPGRADE_SLOTS = 4;

    private final Ic2Machine machine;
    private final MachineRecipe.Kind kind;
    private final int[] outputs;
    private final int firstUpgradeSlot;
    private final String speedFormat;
    private final int speedScale;
    private final Supplier<SoundEvent> sound;
    private final String guiTexture;

    AdvancedMachineType(Ic2Machine machine, MachineRecipe.Kind kind, int[] outputs, int firstUpgradeSlot,
                        String speedFormat, int speedScale, Supplier<SoundEvent> sound, String guiTexture) {
        this.machine = machine;
        this.kind = kind;
        this.outputs = outputs;
        this.firstUpgradeSlot = firstUpgradeSlot;
        this.speedFormat = speedFormat;
        this.speedScale = speedScale;
        this.sound = sound;
        this.guiTexture = guiTexture;
    }

    public static AdvancedMachineType of(Ic2Machine machine) {
        for (AdvancedMachineType type : values()) {
            if (type.machine == machine) {
                return type;
            }
        }
        throw new IllegalArgumentException(machine + " is no advanced machine");
    }

    public Ic2Machine getMachine() {
        return machine;
    }

    public MachineRecipe.Kind getKind() {
        return kind;
    }

    public int[] getOutputs() {
        return outputs;
    }

    public int getFirstUpgradeSlot() {
        return firstUpgradeSlot;
    }

    /** Slots: energy, input, outputs, four upgrades and the rotary macerator's supplement slot 8. */
    public int getSlotCount() {
        return firstUpgradeSlot + UPGRADE_SLOTS + (this == ROTARY_MACERATOR ? 1 : 0);
    }

    /** {@code printFormattedData}: the speed in the machine's unit. */
    public String formatSpeed(int speed) {
        return String.format(speedFormat, speed * speedScale);
    }

    public SoundEvent getSound() {
        return sound.get();
    }

    public String getGuiTexture() {
        return guiTexture;
    }
}
