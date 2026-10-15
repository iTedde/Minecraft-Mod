package dev.craft.gregtech;

import java.util.EnumMap;
import java.util.Map;

import dev.craft.Craft;
import dev.craft.gregtech.block.GtCasingBlock;
import dev.craft.gregtech.machine.GtMachineBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GtBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    private static final Map<GtMachine, DeferredBlock<GtMachineBlock>> MACHINES = new EnumMap<>(GtMachine.class);
    private static final Map<GtCasingBlock.Type, DeferredBlock<GtCasingBlock>> CASINGS =
            new EnumMap<>(GtCasingBlock.Type.class);

    static {
        for (GtMachine machine : GtMachine.values()) {
            MACHINES.put(machine, BLOCKS.register(machine.getName(), () -> new GtMachineBlock(machineProperties(),
                    machine)));
        }
        for (GtCasingBlock.Type type : GtCasingBlock.Type.values()) {
            CASINGS.put(type, BLOCKS.register(type.getName(), () -> new GtCasingBlock(casingProperties(type), type)));
        }
    }

    private GtBlocks() {
    }

    public static DeferredBlock<GtMachineBlock> machine(GtMachine machine) {
        return MACHINES.get(machine);
    }

    public static DeferredBlock<GtCasingBlock> casing(GtCasingBlock.Type type) {
        return CASINGS.get(type);
    }

    /** {@code GT_BlockMetaID_Block}: Material.iron, hardness 3, iron pickaxe, no mob spawns. */
    private static BlockBehaviour.Properties casingProperties(GtCasingBlock.Type type) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, type.getResistance())
                .sound(SoundType.METAL).requiresCorrectToolForDrops().isValidSpawn((state, level, pos, t) -> false);
    }

    /** {@code GT_BlockMetaID_Machine}: Material.iron, hardness 3, resistance 10 (effective 6), no mob spawns. */
    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().isValidSpawn((state, level, pos, type) -> false);
    }
}
