package dev.craft.ic2;

import java.util.Arrays;
import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.ic2.cable.CableBlockEntity;
import dev.craft.ic2.cable.CableType;
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

    private Ic2BlockEntityTypes() {
    }
}
