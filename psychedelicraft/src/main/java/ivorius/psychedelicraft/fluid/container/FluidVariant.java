package ivorius.psychedelicraft.fluid.container;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * A fluid together with its components. Stands in for the Fabric Transfer API type used upstream; amounts stay in
 * Fabric droplets internally ({@link #BUCKET} per bucket) and are converted to millibuckets at the NeoForge boundary.
 */
public record FluidVariant(Fluid fluid, DataComponentPatch components) {
    public static final long BUCKET = 81000;
    public static final long BOTTLE = BUCKET / 3;
    public static final long DROPLETS_PER_MB = BUCKET / 1000;

    private static final FluidVariant BLANK = new FluidVariant(Fluids.EMPTY, DataComponentPatch.EMPTY);

    public static FluidVariant blank() {
        return BLANK;
    }

    public static FluidVariant of(Fluid fluid) {
        return of(fluid, DataComponentPatch.EMPTY);
    }

    public static FluidVariant of(Fluid fluid, DataComponentPatch components) {
        return fluid == Fluids.EMPTY ? BLANK : new FluidVariant(fluid, components);
    }

    public static FluidVariant of(FluidStack stack) {
        return stack.isEmpty() ? BLANK : of(stack.getFluid(), stack.getComponentsPatch());
    }

    public Fluid getFluid() {
        return fluid;
    }

    public DataComponentPatch getComponents() {
        return components;
    }

    public boolean isBlank() {
        return fluid == Fluids.EMPTY;
    }

    public FluidStack toStack(long droplets) {
        int mb = (int)(droplets / DROPLETS_PER_MB);
        return isBlank() || mb <= 0 ? FluidStack.EMPTY : new FluidStack(fluid.builtInRegistryHolder(), mb, components);
    }

    public static long toDroplets(int millibuckets) {
        return millibuckets * DROPLETS_PER_MB;
    }

    public static int toMillibuckets(long droplets) {
        return (int)(droplets / DROPLETS_PER_MB);
    }

    public SoundEvent getEmptySound() {
        SoundEvent sound = fluid.getFluidType().getSound(SoundActions.BUCKET_EMPTY);
        return sound == null ? SoundEvents.BUCKET_EMPTY : sound;
    }

    public SoundEvent getFillSound() {
        SoundEvent sound = fluid.getFluidType().getSound(SoundActions.BUCKET_FILL);
        return sound == null ? SoundEvents.BUCKET_FILL : sound;
    }
}
