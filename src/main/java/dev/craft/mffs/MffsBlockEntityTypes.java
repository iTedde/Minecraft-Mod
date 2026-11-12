package dev.craft.mffs;

import dev.craft.Craft;
import dev.craft.mffs.block.CapacitorBlockEntity;
import dev.craft.mffs.block.ConverterBlockEntity;
import dev.craft.mffs.block.ExtractorBlockEntity;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MffsBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craft.MODID);

    public static final Supplier<BlockEntityType<ExtractorBlockEntity>> EXTRACTOR = BLOCK_ENTITY_TYPES.register(
            "mffs_extractor", () -> BlockEntityType.Builder.of(ExtractorBlockEntity::new,
                    MffsBlocks.EXTRACTOR.get()).build(null));
    public static final Supplier<BlockEntityType<CapacitorBlockEntity>> CAPACITOR = BLOCK_ENTITY_TYPES.register(
            "mffs_capacitor", () -> BlockEntityType.Builder.of(CapacitorBlockEntity::new,
                    MffsBlocks.CAPACITOR.get()).build(null));
    public static final Supplier<BlockEntityType<ConverterBlockEntity>> CONVERTER = BLOCK_ENTITY_TYPES.register(
            "mffs_converter", () -> BlockEntityType.Builder.of(ConverterBlockEntity::new,
                    MffsBlocks.CONVERTER.get()).build(null));

    private MffsBlockEntityTypes() {
    }
}
