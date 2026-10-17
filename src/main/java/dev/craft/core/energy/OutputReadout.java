package dev.craft.core.energy;

import org.jetbrains.annotations.Nullable;

/**
 * A block entity whose current energy output is shown in the look-at overlay ({@code core/client/OutputOverlay}).
 * The client only checks the type; the value is asked from the server with {@link OutputPayloads}.
 */
public interface OutputReadout {
    /** Server side: the current output, or {@code null} if the block shows none right now. */
    @Nullable
    OutputReading getOutputReading();

    /** Output per tick, averaged over the last second. */
    record OutputReading(double rate, Unit unit) {
        public enum Unit {
            EU("EU/t"),
            MJ("MJ/t");

            private final String symbol;

            Unit(String symbol) {
                this.symbol = symbol;
            }

            public String symbol() {
                return symbol;
            }
        }
    }
}
