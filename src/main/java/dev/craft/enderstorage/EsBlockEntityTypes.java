package dev.craft.enderstorage;

import java.util.function.Supplier;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class EsBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craft.MODID);

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<EnderChestBlockEntity>> ENDER_CHEST =
            BLOCK_ENTITY_TYPES.register("ender_storage_chest", () -> BlockEntityType.Builder.of(
                    EnderChestBlockEntity::new, EsBlocks.ENDER_CHEST.get()).build(null));

    private EsBlockEntityTypes() {
    }
}
