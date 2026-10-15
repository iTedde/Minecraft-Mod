package dev.craft.gregtech;

import java.util.EnumMap;
import java.util.Map;

import dev.craft.Craft;
import dev.craft.gregtech.machine.GtMachineBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GtBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    private static final Map<GtMachine, DeferredBlock<GtMachineBlock>> MACHINES = new EnumMap<>(GtMachine.class);

    static {
        for (GtMachine machine : GtMachine.values()) {
            MACHINES.put(machine, BLOCKS.register(machine.getName(), () -> new GtMachineBlock(machineProperties(),
                    machine)));
        }
    }

    private GtBlocks() {
    }

    public static DeferredBlock<GtMachineBlock> machine(GtMachine machine) {
        return MACHINES.get(machine);
    }

    /** {@code GT_BlockMetaID_Machine}: Material.iron, hardness 3, resistance 10 (effective 6), no mob spawns. */
    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().isValidSpawn((state, level, pos, type) -> false);
    }
}
