package dev.craft.mffs;

import dev.craft.Craft;
import dev.craft.mffs.block.CapacitorBlockEntity;
import dev.craft.mffs.block.ControlSystemBlockEntity;
import dev.craft.mffs.block.ConverterBlockEntity;
import dev.craft.mffs.block.DefenseStationBlockEntity;
import dev.craft.mffs.block.ExtractorBlockEntity;
import dev.craft.mffs.block.ForceFieldBlockEntity;
import dev.craft.mffs.block.ProjectorBlockEntity;
import dev.craft.mffs.block.SecurityStationBlockEntity;
import dev.craft.mffs.block.SecurityStorageBlockEntity;
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

    public static final Supplier<BlockEntityType<ProjectorBlockEntity>> PROJECTOR = BLOCK_ENTITY_TYPES.register(
            "mffs_projector", () -> BlockEntityType.Builder.of(ProjectorBlockEntity::new,
                    MffsBlocks.PROJECTOR.get()).build(null));
    public static final Supplier<BlockEntityType<ForceFieldBlockEntity>> FORCE_FIELD = BLOCK_ENTITY_TYPES.register(
            "force_field", () -> BlockEntityType.Builder.of(ForceFieldBlockEntity::new,
                    MffsBlocks.FORCE_FIELD.get()).build(null));
    public static final Supplier<BlockEntityType<DefenseStationBlockEntity>> DEFENSE_STATION =
            BLOCK_ENTITY_TYPES.register("mffs_defense_station", () -> BlockEntityType.Builder.of(
                    DefenseStationBlockEntity::new, MffsBlocks.DEFENSE_STATION.get()).build(null));
    public static final Supplier<BlockEntityType<SecurityStationBlockEntity>> SECURITY_STATION =
            BLOCK_ENTITY_TYPES.register("mffs_security_station", () -> BlockEntityType.Builder.of(
                    SecurityStationBlockEntity::new, MffsBlocks.SECURITY_STATION.get()).build(null));
    public static final Supplier<BlockEntityType<SecurityStorageBlockEntity>> SECURITY_STORAGE =
            BLOCK_ENTITY_TYPES.register("mffs_security_storage", () -> BlockEntityType.Builder.of(
                    SecurityStorageBlockEntity::new, MffsBlocks.SECURITY_STORAGE.get()).build(null));
    public static final Supplier<BlockEntityType<ControlSystemBlockEntity>> CONTROL_SYSTEM =
            BLOCK_ENTITY_TYPES.register("mffs_control_system", () -> BlockEntityType.Builder.of(
                    ControlSystemBlockEntity::new, MffsBlocks.CONTROL_SYSTEM.get()).build(null));

    private MffsBlockEntityTypes() {
    }
}
