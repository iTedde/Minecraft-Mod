/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.List;

import org.jetbrains.annotations.ApiStatus.Experimental;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.BlockEntityTypeSupportHelper;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.world.gen.PSSaplingGenerators;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public interface PSBlocks {
    Block MASH_TUB = register("mash_tub", new MashTubBlock(Properties.of()
            .sound(SoundType.WOOD)
            .destroyTime(2).noOcclusion().isSuffocating(BlockConstructionUtils::never).isViewBlocking(BlockConstructionUtils::never)
            .pushReaction(PushReaction.BLOCK)
    ));
    Block MASH_TUB_EDGE = register("mash_tub_edge", new MashTubWallBlock(Properties.ofFullCopy(MASH_TUB)));
    Block PLACED_DRINK = register("placed_drink", new PlacedDrinksBlock(Properties.of()
            .instabreak().noOcclusion().isSuffocating(BlockConstructionUtils::never).isViewBlocking(BlockConstructionUtils::never)
            .pushReaction(PushReaction.DESTROY)
    ));

    Block OAK_BARREL = register("oak_barrel", BlockConstructionUtils.barrel(MapColor.WOOD));
    @Experimental
    Block PALE_OAK_BARREL = register("pale_oak_barrel", BlockConstructionUtils.barrel(MapColor.COLOR_GRAY));
    Block SPRUCE_BARREL = register("spruce_barrel", BlockConstructionUtils.barrel(MapColor.PODZOL));
    Block BIRCH_BARREL = register("birch_barrel", BlockConstructionUtils.barrel(MapColor.SAND));
    Block JUNGLE_BARREL = register("jungle_barrel", BlockConstructionUtils.barrel(MapColor.DIRT));
    Block ACACIA_BARREL = register("acacia_barrel", BlockConstructionUtils.barrel(MapColor.COLOR_ORANGE));
    Block DARK_OAK_BARREL = register("dark_oak_barrel", BlockConstructionUtils.barrel(MapColor.COLOR_BROWN));
    Block MANGROVE_BARREL = register("mangrove_barrel", BlockConstructionUtils.barrel(MapColor.PLANT));
    Block CHERRY_BARREL = register("cherry_barrel", BlockConstructionUtils.barrel(MapColor.CRIMSON_STEM));
    Block BAMBOO_BARREL = register("bamboo_barrel", BlockConstructionUtils.barrel(MapColor.COLOR_YELLOW));
    Block WARPED_BARREL = register("warped_barrel", BlockConstructionUtils.barrel(MapColor.WARPED_WART_BLOCK));
    Block CRIMSON_BARREL = register("crimson_barrel", BlockConstructionUtils.barrel(MapColor.FIRE));
    Block JUNIPER_BARREL = register("juniper_barrel", BlockConstructionUtils.barrel(MapColor.CLAY));

    List<Block> ALL_BARRELS = List.of(
            OAK_BARREL, SPRUCE_BARREL, BIRCH_BARREL, JUNGLE_BARREL, ACACIA_BARREL, DARK_OAK_BARREL,
            MANGROVE_BARREL, CHERRY_BARREL, BAMBOO_BARREL, WARPED_BARREL, CRIMSON_BARREL, JUNIPER_BARREL, PALE_OAK_BARREL
    );

    Block FLASK = register("flask", new FlaskBlock(Properties.of().sound(SoundType.COPPER).destroyTime(1).pushReaction(PushReaction.BLOCK)));
    Block DISTILLERY = register("distillery", new DistilleryBlock(Properties.of().sound(SoundType.COPPER).destroyTime(1).pushReaction(PushReaction.BLOCK)));
    Block BOTTLE_RACK = register("bottle_rack", new BottleRackBlock(0, Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).destroyTime(0.5F).ignitedByLava()));
    Block WALL_BOTTLE_RACK = register("wall_bottle_rack", new BottleRackBlock(-3, Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).destroyTime(0.5F).ignitedByLava()));

    Block DRYING_TABLE = register("drying_table", new DryingTableBlock(Properties.of().mapColor(MapColor.WOOD).forceSolidOn().sound(SoundType.WOOD).destroyTime(2).ignitedByLava()));
    Block IRON_DRYING_TABLE = register("iron_drying_table", new DryingTableBlock(Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).destroyTime(5)));

    JuniperLeavesBlock JUNIPER_LEAVES = register("juniper_leaves", new JuniperLeavesBlock(BlockConstructionUtils.leaves(SoundType.GRASS)));
    JuniperLeavesBlock FRUITING_JUNIPER_LEAVES = register("fruiting_juniper_leaves", new JuniperLeavesBlock(BlockConstructionUtils.leaves(SoundType.GRASS)));
    Block JUNIPER_LOG = register("juniper_log", BlockConstructionUtils.log(MapColor.COLOR_CYAN, MapColor.CLAY));
    Block JUNIPER_WOOD = register("juniper_wood", BlockConstructionUtils.log(MapColor.COLOR_CYAN, MapColor.CLAY));
    Block STRIPPED_JUNIPER_LOG = register("stripped_juniper_log", BlockConstructionUtils.log(MapColor.COLOR_CYAN, MapColor.CLAY));
    Block STRIPPED_JUNIPER_WOOD = register("stripped_juniper_wood", BlockConstructionUtils.log(MapColor.COLOR_CYAN, MapColor.CLAY));
    Block JUNIPER_SAPLING = register("juniper_sapling", new SaplingBlock(PSSaplingGenerators.JUNIPER, BlockConstructionUtils.plant(SoundType.GRASS)));

    Block JUNIPER_PLANKS = register("juniper_planks", new Block(Properties.of().mapColor(MapColor.CLAY).instrument(NoteBlockInstrument.BASS).strength(2, 3).sound(SoundType.WOOD).ignitedByLava()));
    Block JUNIPER_STAIRS = register("juniper_stairs", new StairBlock(JUNIPER_PLANKS.defaultBlockState(), Properties.ofFullCopy(JUNIPER_PLANKS)));
    Block JUNIPER_SIGN = register("juniper_sign", new StandingSignBlock(PSWoodTypes.JUNIPER, Properties.of().mapColor(JUNIPER_PLANKS.defaultMapColor()).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1).ignitedByLava().sound(SoundType.WOOD)));
    Block JUNIPER_DOOR = register("juniper_door", new DoorBlock(PSWoodTypes.JUNIPER.setType(), Properties.of().mapColor(JUNIPER_PLANKS.defaultMapColor()).instrument(NoteBlockInstrument.BASS).strength(3.0f).noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY)));
    Block JUNIPER_WALL_SIGN = register("juniper_wall_sign", new WallSignBlock(PSWoodTypes.JUNIPER, Properties.of().mapColor(JUNIPER_PLANKS.defaultMapColor()).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1).dropsLike(JUNIPER_SIGN).ignitedByLava()));
    Block JUNIPER_HANGING_SIGN = register("juniper_hanging_sign", new CeilingHangingSignBlock(PSWoodTypes.JUNIPER, Properties.of().mapColor(JUNIPER_LOG.defaultMapColor()).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1).ignitedByLava()));
    Block JUNIPER_WALL_HANGING_SIGN = register("juniper_wall_hanging_sign", new WallHangingSignBlock(PSWoodTypes.JUNIPER, Properties.of().mapColor(JUNIPER_LOG.defaultMapColor()).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0f).ignitedByLava().dropsLike(JUNIPER_HANGING_SIGN)));
    Block JUNIPER_PRESSURE_PLATE = register("juniper_pressure_plate", new PressurePlateBlock(PSWoodTypes.JUNIPER.setType(), Properties.of().mapColor(JUNIPER_PLANKS.defaultMapColor()).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(PushReaction.DESTROY)));
    Block JUNIPER_FENCE = register("juniper_fence", new FenceBlock(Properties.of().mapColor(JUNIPER_PLANKS.defaultMapColor()).forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2, 3).sound(SoundType.WOOD).ignitedByLava()));
    Block JUNIPER_TRAPDOOR = register("juniper_trapdoor", new TrapDoorBlock(PSWoodTypes.JUNIPER.setType(), Properties.of().mapColor(JUNIPER_PLANKS.defaultMapColor()).instrument(NoteBlockInstrument.BASS).strength(3).noOcclusion().isValidSpawn(BlockConstructionUtils::never).ignitedByLava()));
    Block JUNIPER_FENCE_GATE = register("juniper_fence_gate", new FenceGateBlock(PSWoodTypes.JUNIPER, Properties.of().mapColor(JUNIPER_PLANKS.defaultMapColor()).forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2, 3).ignitedByLava()));
    Block JUNIPER_BUTTON = register("juniper_button", BlockConstructionUtils.woodenButton(PSWoodTypes.JUNIPER.setType()));
    Block JUNIPER_SLAB = register("juniper_slab", new SlabBlock(Properties.ofFullCopy(JUNIPER_PLANKS)));

    CannabisPlantBlock CANNABIS = register("cannabis", new CannabisPlantBlock(BlockConstructionUtils.plant(SoundType.GRASS)));
    HopPlantBlock HOP = register("hop", new HopPlantBlock(BlockConstructionUtils.plant(SoundType.GRASS)));
    TobaccoPlantBlock TOBACCO = register("tobacco", new TobaccoPlantBlock(BlockConstructionUtils.plant(SoundType.GRASS)));
    CocaPlantBlock COCA = register("coca", new CocaPlantBlock(BlockConstructionUtils.plant(SoundType.GRASS)));
    CoffeaPlantBlock COFFEA = register("coffea", new CoffeaPlantBlock(BlockConstructionUtils.plant(SoundType.GRASS)));
    PeyoteBlock PEYOTE = register("peyote", new PeyoteBlock(BlockConstructionUtils.plant(SoundType.GRASS)));
    AgavePlantBlock AGAVE_PLANT = register("agave_plant", new AgavePlantBlock(BlockConstructionUtils.plant(SoundType.GRASS)));
    NightshadeBlock JIMSONWEED = register("jimsonweed", new NightshadeBlock(
            () -> PSItems.JIMSONWEED_SEED_POD,
            () -> PSItems.JIMSONWEED_LEAF, BlockConstructionUtils.plant(SoundType.GRASS)));
    NightshadeBlock BELLADONNA = register("belladonna", new NightshadeBlock(
            () -> PSItems.BELLADONNA_BERRIES,
            () -> PSItems.BELLADONNA_LEAF, BlockConstructionUtils.plant(SoundType.GRASS)));
    NightshadeBlock TOMATOES = register("tomatoes", new NightshadeBlock(
            () -> PSItems.TOMATO,
            () -> PSItems.TOMATO_LEAF, BlockConstructionUtils.plant(SoundType.GRASS)));

    Block LATTICE = register("lattice", new LatticeBlock(Properties.of().mapColor(MapColor.WOOD)
            .sound(SoundType.WOOD).destroyTime(0.3F).noOcclusion().ignitedByLava()));
    Block WINE_GRAPE_LATTICE = register("wine_grape_lattice", new BurdenedLatticeBlock(true, null, 1, Properties.of().mapColor(MapColor.WOOD)
            .sound(SoundType.WOOD).destroyTime(0.3F).randomTicks().noOcclusion().ignitedByLava()
    ));
    Block MORNING_GLORY = register("morning_glory", new VineStemBlock(() -> PSBlocks.MORNING_GLORY_LATTICE, Block.Properties.of()
            .mapColor(MapColor.PLANT)
            .noCollission()
            .instabreak()
            .randomTicks()
            .sound(SoundType.GRASS)
            .offsetType(BlockBehaviour.OffsetType.XZ)
            .ignitedByLava()
    ));
    Block MORNING_GLORY_LATTICE = register("morning_glory_lattice", new BurdenedLatticeBlock(true, MORNING_GLORY, 2, Block.Properties.of()
            .mapColor(MapColor.WOOD)
            .sound(SoundType.WOOD)
            .destroyTime(0.3F)
            .randomTicks()
            .noOcclusion()
            .ignitedByLava()
    ));

    Block POTTED_MORNING_GLORY = register("potted_morning_glory", BlockConstructionUtils.pottedPlant(MORNING_GLORY));
    Block POTTED_JUNIPER_SAPLING = register("potted_juniper_sapling", BlockConstructionUtils.pottedPlant(JUNIPER_SAPLING));
    Block POTTED_CANNABIS = register("potted_cannabis", BlockConstructionUtils.pottedPlant(CANNABIS));
    Block POTTED_HOP = register("potted_hop", BlockConstructionUtils.pottedPlant(HOP));
    Block POTTED_TOBACCO = register("potted_tobacco", BlockConstructionUtils.pottedPlant(TOBACCO));
    Block POTTED_COCA = register("potted_coca", BlockConstructionUtils.pottedPlant(COCA));
    Block POTTED_COFFEA = register("potted_coffea", BlockConstructionUtils.pottedPlant(COFFEA));

    Block RIFT_JAR = register("rift_jar", new RiftJarBlock(Properties.of().destroyTime(0.5F).sound(SoundType.GLASS).noOcclusion().pushReaction(PushReaction.DESTROY)));
    Block GLITCH = register("glitch", new GlitchedBlock(Properties.of().mapColor(MapColor.COLOR_BLACK).instabreak().destroyTime(0)
            .emissiveRendering(BlockConstructionUtils::always)
            .air().noOcclusion().noTerrainParticles().noLootTable()
    ));

    Block FLAMMABLE_GAS = register("flammable_gas", new FlammableGasBlock(Properties.of().replaceable().noCollission().noLootTable().air()));

    Block TRAY = register("tray", new TrayBlock(Properties.of().mapColor(MapColor.METAL).destroyTime(0.7F).sound(SoundType.METAL).noOcclusion()));
    Block BUNSEN_BURNER = register("bunsen_burner", new BurnerBlock(Properties.of().mapColor(MapColor.METAL).destroyTime(0.7F).sound(SoundType.METAL).noOcclusion()));
    Block GLASS_TUBE = register("glass_tube", new GlassTubeBlock(Properties.of().mapColor(MapColor.QUARTZ).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
            .isValidSpawn((state, level, pos, type) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)));
    Block GLASS_VALVE = register("glass_valve", new ValveBlock(Properties.of().mapColor(MapColor.QUARTZ).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
            .isValidSpawn((state, level, pos, type) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)));
    Block PUMP = register("pump", new PumpBlock(Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F)));
    Block PUMP_HEAD = register("pump_head", new PumpHeadBlock(Properties.ofFullCopy(PUMP).noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK)));
    Block CAULDRON = register("cauldron", new FluidCauldronBlock(Properties.ofFullCopy(Blocks.CAULDRON)));

    static <T extends Block> T register(String name, T block) {
        return Registry.register(BuiltInRegistries.BLOCK, Psychedelicraft.id(name), block);
    }

    static void bootstrap() {
        PSBlockEntities.bootstrap();

        BlockEntityTypeSupportHelper.of(BlockEntityType.SIGN).addSupportedBlocks(JUNIPER_SIGN, JUNIPER_WALL_SIGN);
        BlockEntityTypeSupportHelper.of(BlockEntityType.HANGING_SIGN).addSupportedBlocks(JUNIPER_HANGING_SIGN, JUNIPER_WALL_HANGING_SIGN);

        ivorius.psychedelicraft.util.PSHooks.flammable(JUNIPER_LOG, 5, 5);
        ivorius.psychedelicraft.util.PSHooks.flammable(STRIPPED_JUNIPER_LOG, 5, 5);
        ivorius.psychedelicraft.util.PSHooks.flammable(JUNIPER_WOOD, 5, 5);
        ivorius.psychedelicraft.util.PSHooks.flammable(STRIPPED_JUNIPER_WOOD, 5, 5);
        ivorius.psychedelicraft.util.PSHooks.flammable(JUNIPER_LEAVES, 30, 60);
        ivorius.psychedelicraft.util.PSHooks.flammable(LATTICE, 5, 20);
        ivorius.psychedelicraft.util.PSHooks.flammable(WINE_GRAPE_LATTICE, 5, 20);
        ivorius.psychedelicraft.util.PSHooks.flammable(MORNING_GLORY_LATTICE, 5, 20);

        ivorius.psychedelicraft.util.PSHooks.strippable(JUNIPER_LOG, STRIPPED_JUNIPER_LOG);
        ivorius.psychedelicraft.util.PSHooks.strippable(JUNIPER_WOOD, STRIPPED_JUNIPER_WOOD);
    }
}