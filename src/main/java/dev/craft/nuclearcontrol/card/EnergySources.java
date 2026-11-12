package dev.craft.nuclearcontrol.card;

import javax.annotation.Nullable;

import dev.craft.buildcraft.power.BcCapabilities;
import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.power.PowerReceptor;
import dev.craft.gregtech.machine.GtMachineBlockEntity;
import dev.craft.ic2.storage.ElectricStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * {@code EnergyStorageHelper} / {@code LiquidStorageHelper} (spec 46 §6). EU (type 0): IC2 {@code IEnergyStorage} =
 * BatBox, MFE, MFSU and every GregTech machine ({@code BaseMetaTileEntity}). MJ (type 1): BuildCraft power receptors.
 * Unloaded targets count as missing.
 */
public final class EnergySources {
    public static final int ANY = -1;
    public static final int EU = 0;
    public static final int MJ = 1;

    private EnergySources() {
    }

    public record Reading(long stored, long capacity, int type) {
    }

    @Nullable
    public static Reading at(Level level, BlockPos pos, int type) {
        if (!level.isLoaded(pos)) {
            return null;
        }
        return switch (type) {
            case ANY -> {
                Reading eu = eu(level, pos);
                yield eu != null ? eu : mj(level, pos);
            }
            case EU -> eu(level, pos);
            case MJ -> mj(level, pos);
            default -> null;
        };
    }

    @Nullable
    private static Reading eu(Level level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof ElectricStorageBlockEntity storage) {
            return new Reading(storage.getEnergy(), storage.getMaxStorage(), EU);
        }
        if (entity instanceof GtMachineBlockEntity machine) {
            return new Reading(machine.getStored(), machine.getCapacity(), EU);
        }
        return null;
    }

    /** {@code CrossBuildcraft}: {@code getPowerProvider()} of a receptor; the stored MJ are cut to whole numbers. */
    @Nullable
    private static Reading mj(Level level, BlockPos pos) {
        PowerReceptor receptor = level.getCapability(BcCapabilities.POWER_RECEPTOR, pos, null);
        PowerProvider provider = receptor == null ? null : receptor.getPowerProvider();
        if (provider == null) {
            return null;
        }
        return new Reading((int) provider.getEnergyStored(), provider.getMaxEnergyStored(), MJ);
    }

    /** First tank of the block, asked from above as in the original ({@code getTanks(UP)[0]}). */
    @Nullable
    public static IFluidHandler tanks(Level level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return null;
        }
        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.UP);
        if (handler == null) {
            handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
        }
        return handler != null && handler.getTanks() > 0 ? handler : null;
    }
}
