package dev.craft.redpower;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.redpower.table.ProjectTableBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RpBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craft.MODID);

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<ProjectTableBlockEntity>> PROJECT_TABLE =
            BLOCK_ENTITY_TYPES.register("project_table", () -> BlockEntityType.Builder
                    .of(ProjectTableBlockEntity::new, RpBlocks.PROJECT_TABLE.get()).build(null));

    private RpBlockEntityTypes() {
    }
}
