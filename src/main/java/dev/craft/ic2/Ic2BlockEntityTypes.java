package dev.craft.ic2;

import dev.craft.ic2.reactor.ReactorChamberBlockEntity;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.ic2.block.LuminatorBlockEntity;
import dev.craft.ic2.cable.CableBlockEntity;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.machine.MachineBlock;
import dev.craft.ic2.machine.MachineBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Ic2BlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craft.MODID);

    public static final Supplier<BlockEntityType<CableBlockEntity>> CABLE = BLOCK_ENTITY_TYPES.register("cable",
            () -> BlockEntityType.Builder.of(CableBlockEntity::new, Arrays.stream(CableType.values())
                    .map(type -> Ic2Blocks.cable(type).get()).toArray(Block[]::new)).build(null));

    public static final Supplier<BlockEntityType<ReactorChamberBlockEntity>> REACTOR_CHAMBER =
            BLOCK_ENTITY_TYPES.register("reactor_chamber", () -> BlockEntityType.Builder.of(
                    ReactorChamberBlockEntity::new, Ic2Blocks.REACTOR_CHAMBER.get()).build(null));

    public static final Supplier<BlockEntityType<LuminatorBlockEntity>> LUMINATOR =
            BLOCK_ENTITY_TYPES.register("luminator", () -> BlockEntityType.Builder.of(
                    LuminatorBlockEntity::new, Ic2Blocks.LUMINATOR.get()).build(null));

    private static final Map<Ic2Machine, Supplier<BlockEntityType<MachineBlockEntity>>> MACHINES =
            new EnumMap<>(Ic2Machine.class);

    static {
        for (Ic2Machine machine : Ic2Machine.values()) {
            MACHINES.put(machine, BLOCK_ENTITY_TYPES.register(machine.getName(), () -> {
                MachineBlock block = Ic2Blocks.machine(machine).get();
                return BlockEntityType.Builder.<MachineBlockEntity>of(
                        (pos, state) -> (MachineBlockEntity) block.newBlockEntity(pos, state), block).build(null);
            }));
        }
    }

    private Ic2BlockEntityTypes() {
    }

    public static BlockEntityType<MachineBlockEntity> get(Ic2Machine machine) {
        return MACHINES.get(machine).get();
    }
}
