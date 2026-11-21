package ivorius.psychedelicraft.block.entity;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;

public interface PSBlockEntities {
    BlockEntityType<DryingTableBlockEntity> DRYING_TABLE = create("drying_table", BlockEntityType.Builder.of(DryingTableBlockEntity::new, PSBlocks.DRYING_TABLE, PSBlocks.IRON_DRYING_TABLE));
    BlockEntityType<MashTubBlockEntity> MASH_TUB = create("wooden_vat", BlockEntityType.Builder.of(MashTubBlockEntity::new, PSBlocks.MASH_TUB));
    BlockEntityType<MashTubWallBlock.MasterPosition> MASH_TUB_EDGE = create("wooden_vat_edge", BlockEntityType.Builder.of(MashTubWallBlock.MasterPosition::new, PSBlocks.MASH_TUB_EDGE));
    BlockEntityType<RiftJarBlockEntity> RIFT_JAR = create("rift_jar", BlockEntityType.Builder.of(RiftJarBlockEntity::new, PSBlocks.RIFT_JAR));
    BlockEntityType<PeyoteBlockEntity> PEYOTE = create("peyote", BlockEntityType.Builder.of(PeyoteBlockEntity::new, PSBlocks.PEYOTE));
    BlockEntityType<DistilleryBlockEntity> DISTILLERY = create("distillery", BlockEntityType.Builder.of(DistilleryBlockEntity::new, PSBlocks.DISTILLERY));
    BlockEntityType<BottleRackBlockEntity> BOTTLE_RACK = create("bottle_rack", BlockEntityType.Builder.of(BottleRackBlockEntity::new, PSBlocks.BOTTLE_RACK, PSBlocks.WALL_BOTTLE_RACK));
    BlockEntityType<FlaskBlockEntity> FLASK = create("flask", BlockEntityType.Builder.of(FlaskBlockEntity::new, PSBlocks.FLASK));
    BlockEntityType<BarrelBlockEntity> BARREL = create("barrel", BlockEntityType.Builder.of(BarrelBlockEntity::new, PSBlocks.ALL_BARRELS.toArray(Block[]::new)));
    BlockEntityType<BurnerBlockEntity> BUNSEN_BURNER = create("bunsen_burner", BlockEntityType.Builder.of(BurnerBlockEntity::new, PSBlocks.BUNSEN_BURNER));
    BlockEntityType<TrayBlockEntity> TRAY = create("tray", BlockEntityType.Builder.of(TrayBlockEntity::new, PSBlocks.TRAY));
    BlockEntityType<PlacedDrinksBlock.Data> PLACED_DRINK = create("placed_drink", BlockEntityType.Builder.of(PlacedDrinksBlock.Data::new, PSBlocks.PLACED_DRINK));
    BlockEntityType<GlassTubeBlock.Data> GLASS_TUBE = create("glass_tube", BlockEntityType.Builder.of(GlassTubeBlock.Data::new, PSBlocks.GLASS_TUBE, PSBlocks.GLASS_VALVE));
    BlockEntityType<FluidCauldronBlock.Data> CAULDRON = create("cauldron", BlockEntityType.Builder.of(FluidCauldronBlock.Data::new, PSBlocks.CAULDRON));

    static <T extends BlockEntity> BlockEntityType<T> create(String id, Builder<T> builder) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Psychedelicraft.id(id), builder.build(null));
    }

    static void bootstrap() { }
}

