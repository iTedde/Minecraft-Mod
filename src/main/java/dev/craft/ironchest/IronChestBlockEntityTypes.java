package dev.craft.ironchest;

import dev.craft.Craft;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

/** One block entity type for all seven chests; the chest type comes from the block. */
public final class IronChestBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craft.MODID);

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<IronChestBlockEntity>> IRON_CHEST =
            BLOCK_ENTITY_TYPES.register("iron_chest", () -> BlockEntityType.Builder.of(IronChestBlockEntity::new,
                    IronChestBlocks.BLOCKS.getEntries().stream().map(holder -> (Block) holder.get())
                            .toArray(Block[]::new)).build(null));

    private IronChestBlockEntityTypes() {
    }
}
