package dev.craft.core.energy;

import dev.craft.core.energy.OutputReadout.OutputReading;
import org.jetbrains.annotations.Nullable;

/**
 * A block entity whose current energy consumption is shown in the look-at overlay ({@code core/client/OutputOverlay}),
 * below the output of an {@link OutputReadout} if it is both. Counted where the energy is actually used up (work,
 * charging tools, handing it on to a bridge), not where it arrives, so a machine running on its battery slot or
 * internal buffer still reads its consumption.
 */
public interface ConsumptionReadout {
    /** Server side: the current consumption, or {@code null} if the block shows none right now. */
    @Nullable
    OutputReading getConsumptionReading();
}
