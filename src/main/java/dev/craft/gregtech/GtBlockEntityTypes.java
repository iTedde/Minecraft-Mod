package dev.craft.gregtech;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.gregtech.machine.GtBasicMachineBlockEntity;
import dev.craft.gregtech.machine.GtMachineBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GtBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craft.MODID);

    private static final Map<GtMachine, Supplier<BlockEntityType<GtBasicMachineBlockEntity>>> MACHINES =
            new EnumMap<>(GtMachine.class);

    static {
        for (GtMachine machine : GtMachine.values()) {
            MACHINES.put(machine, BLOCK_ENTITY_TYPES.register(machine.getName(), () -> {
                GtMachineBlock block = GtBlocks.machine(machine).get();
                return BlockEntityType.Builder.<GtBasicMachineBlockEntity>of(
                        (pos, state) -> machine.getFactory().apply(pos, state), block).build(null);
            }));
        }
    }

    private GtBlockEntityTypes() {
    }

    public static BlockEntityType<GtBasicMachineBlockEntity> get(GtMachine machine) {
        return MACHINES.get(machine).get();
    }
}
