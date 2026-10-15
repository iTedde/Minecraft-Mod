package dev.craft.ic2.machine;

import net.minecraft.sounds.SoundEvent;

/**
 * Bridge from common machine code to the client sound handling (IC2 {@code AudioManager}), which installs its
 * handler on the client only.
 */
public final class MachineSoundHooks {
    private static Handler handler = new Handler() {
    };

    private MachineSoundHooks() {
    }

    public static void setHandler(Handler newHandler) {
        handler = newHandler;
    }

    /** Machine block event (start/interrupt/stop) on the client. */
    static void onMachineEvent(ElectricMachineBlockEntity machine, int event) {
        handler.machineEvent(machine, event);
    }

    /** Called every client tick: keeps the loop sound of {@code machine} playing while {@code active}. */
    public static void updateLoop(MachineBlockEntity machine, SoundEvent sound, boolean active) {
        handler.updateLoop(machine, sound, active);
    }

    public interface Handler {
        default void machineEvent(ElectricMachineBlockEntity machine, int event) {
        }

        default void updateLoop(MachineBlockEntity machine, SoundEvent sound, boolean active) {
        }
    }
}
