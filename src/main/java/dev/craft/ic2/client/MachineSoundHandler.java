package dev.craft.ic2.client;

import java.util.Map;
import java.util.WeakHashMap;

import dev.craft.ic2.machine.ElectricMachineBlockEntity;
import dev.craft.ic2.machine.MachineBlockEntity;
import dev.craft.ic2.machine.MachineSoundHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * Client side of IC2's machine audio: operation sounds started/stopped by block events (electric machines) and loop
 * sounds that play while a generator is active.
 */
public final class MachineSoundHandler implements MachineSoundHooks.Handler {
    private final Map<MachineBlockEntity, MachineSound> operationSounds = new WeakHashMap<>();
    private final Map<MachineBlockEntity, MachineSound> loopSounds = new WeakHashMap<>();

    @Override
    public void machineEvent(ElectricMachineBlockEntity machine, int event) {
        SoundEvent start = machine.getStartSound();
        MachineSound current = operationSounds.remove(machine);
        if (current != null) {
            current.finish();
        }
        switch (event) {
            case ElectricMachineBlockEntity.EVENT_START -> {
                if (start != null) {
                    MachineSound sound = new MachineSound(start, machine, false);
                    operationSounds.put(machine, sound);
                    Minecraft.getInstance().getSoundManager().play(sound);
                }
            }
            case ElectricMachineBlockEntity.EVENT_INTERRUPT -> {
                SoundEvent interrupt = machine.getInterruptSound();
                if (current != null && interrupt != null) {
                    Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(interrupt,
                            SoundSource.BLOCKS, 1.0F, 1.0F, SoundInstance.createUnseededRandom(),
                            machine.getBlockPos()));
                }
            }
            default -> {
            }
        }
    }

    @Override
    public void updateLoop(MachineBlockEntity machine, SoundEvent sound, boolean active) {
        MachineSound current = loopSounds.get(machine);
        if (active) {
            if (current == null || current.isStopped()) {
                MachineSound loop = new MachineSound(sound, machine, true);
                loopSounds.put(machine, loop);
                Minecraft.getInstance().getSoundManager().play(loop);
            }
        } else if (current != null) {
            current.finish();
            loopSounds.remove(machine);
        }
    }

    /** A sound at the centre of a machine that ends when the machine is removed. */
    private static final class MachineSound extends AbstractTickableSoundInstance {
        private final MachineBlockEntity machine;

        MachineSound(SoundEvent sound, MachineBlockEntity machine, boolean looping) {
            super(sound, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
            this.machine = machine;
            this.looping = looping;
            this.delay = 0;
            this.x = machine.getBlockPos().getX() + 0.5;
            this.y = machine.getBlockPos().getY() + 0.5;
            this.z = machine.getBlockPos().getZ() + 0.5;
        }

        void finish() {
            stop();
        }

        @Override
        public void tick() {
            if (machine.isRemoved()) {
                stop();
            }
        }
    }
}
