package dev.craft.ic2.machine;

import java.util.function.BiConsumer;

/**
 * Bridge from common machine code to the client sound handling, which registers itself on the client only.
 */
public final class MachineSoundHooks {
    private static BiConsumer<ElectricMachineBlockEntity, Integer> machineEventHandler = (machine, event) -> {
    };

    private MachineSoundHooks() {
    }

    public static void setMachineEventHandler(BiConsumer<ElectricMachineBlockEntity, Integer> handler) {
        machineEventHandler = handler;
    }

    static void onMachineEvent(ElectricMachineBlockEntity machine, int event) {
        machineEventHandler.accept(machine, event);
    }
}
