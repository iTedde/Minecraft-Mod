package dev.craft.ic2;

import dev.craft.advancedmachines.AdvancedMachineBlock;
import dev.craft.advancedmachines.AdvancedMachineBlockEntity;
import dev.craft.ic2.brewing.BarrelBlock;
import dev.craft.ic2.crop.CropBlock;
import dev.craft.ic2.crop.CropMatronBlockEntity;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;

import dev.craft.Craft;
import dev.craft.ic2.block.ConstructionFoamBlock;
import dev.craft.ic2.block.ConstructionFoamWallBlock;
import dev.craft.ic2.block.DynamiteBlock;
import dev.craft.ic2.block.IndustrialTntBlock;
import dev.craft.ic2.block.LuminatorBlock;
import dev.craft.ic2.block.ScaffoldBlock;
import dev.craft.ic2.item.tool.PainterItem;
import dev.craft.ic2.block.NukeBlock;
import dev.craft.ic2.reactor.NuclearReactorBlock;
import dev.craft.ic2.reactor.ReactorChamberBlock;
import dev.craft.ic2.block.IronFenceBlock;
import dev.craft.ic2.block.MiningPipeBlock;
import dev.craft.ic2.block.MiningTipBlock;
import dev.craft.ic2.block.ResinSheetBlock;
import dev.craft.ic2.block.RubberLeavesBlock;
import dev.craft.ic2.block.RubberSaplingBlock;
import dev.craft.ic2.block.RubberMatBlock;
import dev.craft.ic2.block.RubberWoodBlock;
import dev.craft.ic2.cable.CableBlock;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.generator.GeneratorBlockEntity;
import dev.craft.ic2.generator.GeothermalGeneratorBlockEntity;
import dev.craft.ic2.generator.SolarPanelBlockEntity;
import dev.craft.ic2.generator.WaterMillBlockEntity;
import dev.craft.ic2.generator.WindMillBlockEntity;
import dev.craft.ic2.machine.ElectricFurnaceBlockEntity;
import dev.craft.ic2.machine.ElectrolyzerBlockEntity;
import dev.craft.ic2.machine.CanningMachineBlockEntity;
import dev.craft.ic2.machine.MinerBlockEntity;
import dev.craft.ic2.machine.TeleporterBlockEntity;
import dev.craft.ic2.personal.EnergyOMatBlockEntity;
import dev.craft.ic2.personal.PersonalBlock;
import dev.craft.ic2.personal.PersonalSafeBlockEntity;
import dev.craft.ic2.personal.TradeOMatBlockEntity;
import dev.craft.ic2.machine.TerraformerBlockEntity;
import dev.craft.ic2.machine.TeslaCoilBlockEntity;
import dev.craft.ic2.machine.PumpBlockEntity;
import dev.craft.ic2.machine.InductionFurnaceBlockEntity;
import dev.craft.ic2.machine.IronFurnaceBlockEntity;
import dev.craft.ic2.machine.MachineBlock;
import dev.craft.ic2.machine.MachineBlockEntity;
import dev.craft.ic2.machine.MagnetizerBlockEntity;
import dev.craft.ic2.machine.MassFabricatorBlock;
import dev.craft.ic2.machine.RecipeMachineBlockEntity;
import dev.craft.ic2.machine.RecyclerBlockEntity;
import dev.craft.ic2.storage.ElectricStorageBlock;
import dev.craft.ic2.storage.ElectricStorageBlockEntity;
import dev.craft.ic2.storage.TransformerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Ic2Blocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    private static final Map<CableType, DeferredBlock<CableBlock>> CABLES = new EnumMap<>(CableType.class);
    private static final Map<Ic2Machine, DeferredBlock<MachineBlock>> MACHINES = new EnumMap<>(Ic2Machine.class);

    static {
        for (CableType type : CableType.values()) {
            CABLES.put(type, BLOCKS.register(type.getName(), () -> new CableBlock(type,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(CableBlock.HARDNESS, CableBlock.EXPLOSION_RESISTANCE)
                            .sound(SoundType.WOOL)
                            .noOcclusion()
                            .isRedstoneConductor((state, level, pos) ->
                                    state.getValue(CableBlock.FOAM) != CableBlock.Foam.NONE))));
        }
        for (Ic2Machine machine : Ic2Machine.values()) {
            MACHINES.put(machine, BLOCKS.register(machine.getName(), () -> createMachineBlock(machine)));
        }
    }

    public static final DeferredBlock<Block> MACHINE_CASING = BLOCKS.registerSimpleBlock("machine_casing",
            metal().strength(2.0F));
    /** Advanced machine block (IC2 {@code blockMachine} meta 12); recipe follows with the GT materials (phase 6b). */
    public static final DeferredBlock<Block> ADVANCED_MACHINE_CASING = BLOCKS.registerSimpleBlock(
            "advanced_machine_casing", metal().strength(2.0F));
    public static final DeferredBlock<Block> TIN_ORE = BLOCKS.registerSimpleBlock("tin_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(3.0F, 3.0F));
    public static final DeferredBlock<Block> DEEPSLATE_TIN_ORE = BLOCKS.registerSimpleBlock("deepslate_tin_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE));
    /** IC2 {@code blockOreUran}: hardness 4, {@code setResistance(6)} = 3.6 effective, iron pickaxe, 1–3 XP. */
    public static final DeferredBlock<DropExperienceBlock> URANIUM_ORE = BLOCKS.register("uranium_ore",
            () -> new DropExperienceBlock(UniformInt.of(1, 3), BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(4.0F, 3.6F)));
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_URANIUM_ORE =
            BLOCKS.register("deepslate_uranium_ore", () -> new DropExperienceBlock(UniformInt.of(1, 3),
                    BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM)
                            .requiresCorrectToolForDrops().strength(5.5F, 3.6F).sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> URANIUM_BLOCK = BLOCKS.registerSimpleBlock("uranium_block",
            metal().strength(4.0F));
    /** IC2 {@code BlockReactorChamber}: hardness 2, drops a machine casing without wrench. */
    public static final DeferredBlock<ReactorChamberBlock> REACTOR_CHAMBER = BLOCKS.register("reactor_chamber",
            () -> new ReactorChamberBlock(metal().strength(2.0F)));
    /** IC2 {@code BlockMiningPipe}: hardness 6, resistance 10 (1.4.7 scale, = 6 in 1.21). */
    public static final DeferredBlock<MiningPipeBlock> MINING_PIPE = BLOCKS.register("ic2_mining_pipe",
            () -> new MiningPipeBlock(metal().strength(6.0F, 6.0F).noOcclusion()));
    /** IC2 {@code BlockMiningTip}: hardness 6, resistance 10, drops a mining pipe; no item. */
    public static final DeferredBlock<MiningTipBlock> MINING_TIP = BLOCKS.register("ic2_mining_tip",
            () -> new MiningTipBlock(metal().strength(6.0F, 6.0F).noOcclusion()));
    public static final DeferredBlock<Block> TIN_BLOCK = BLOCKS.registerSimpleBlock("tin_block",
            metal().strength(4.0F));
    public static final DeferredBlock<Block> BRONZE_BLOCK = BLOCKS.registerSimpleBlock("bronze_block",
            metal().strength(4.0F));
    public static final DeferredBlock<RubberWoodBlock> RUBBER_WOOD = BLOCKS.register("rubber_wood",
            () -> new RubberWoodBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BASS).strength(1.0F).sound(SoundType.WOOD).ignitedByLava()
                    .pushReaction(PushReaction.BLOCK)));
    public static final DeferredBlock<RubberLeavesBlock> RUBBER_LEAVES = BLOCKS.register("rubber_leaves",
            () -> new RubberLeavesBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.2F)
                    .randomTicks().sound(SoundType.GRASS).noOcclusion().isValidSpawn((state, level, pos, type) -> false)
                    .isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)
                    .ignitedByLava().pushReaction(PushReaction.DESTROY)
                    .isRedstoneConductor((state, level, pos) -> false)));
    public static final DeferredBlock<RubberSaplingBlock> RUBBER_SAPLING = BLOCKS.register("rubber_sapling",
            () -> new RubberSaplingBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission()
                    .randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));
    /** IC2 {@code BlockPoleFence}: Material.iron, hardness 1.5, {@code setResistance(5)} = 3 effective. */
    public static final DeferredBlock<IronFenceBlock> IRON_FENCE = BLOCKS.register("iron_fence",
            () -> new IronFenceBlock(metal().strength(1.5F, 3.0F).forceSolidOn()));
    /** IC2 {@code BlockResin}: hardness 1.6, {@code setResistance(0.5)} = 0.3 effective, sand sound, no collision. */
    public static final DeferredBlock<ResinSheetBlock> RESIN_SHEET = BLOCKS.register("resin_sheet",
            () -> new ResinSheetBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).noCollission()
                    .noOcclusion().strength(1.6F, 0.3F).sound(SoundType.SAND).pushReaction(PushReaction.DESTROY)));

    /**
     * IC2 {@code BlockTexGlass} ({@code blockAlloyGlass}): glass material, hardness 5, {@code setResistance(150)} = 90
     * effective, drops itself.
     */
    public static final DeferredBlock<TransparentBlock> REINFORCED_GLASS = BLOCKS.register("reinforced_glass",
            () -> new TransparentBlock(BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT)
                    .strength(5.0F, 90.0F).sound(SoundType.GLASS).noOcclusion().isValidSpawn((s, l, p, t) -> false)
                    .isRedstoneConductor((s, l, p) -> false).isSuffocating((s, l, p) -> false)
                    .isViewBlocking((s, l, p) -> false)));
    /** IC2 {@code BlockTex} ({@code blockAlloy}): iron material, hardness 80, {@code setResistance(150)} = 90. */
    public static final DeferredBlock<Block> REINFORCED_STONE = BLOCKS.registerSimpleBlock("reinforced_stone",
            metal().strength(80.0F, 90.0F));
    /**
     * IC2 {@code BlockIC2Door} ({@code blockDoorAlloy}): iron material (redstone only), hardness 50,
     * {@code setResistance(150)} = 90.
     */
    public static final DeferredBlock<DoorBlock> REINFORCED_DOOR = BLOCKS.register("reinforced_door",
            () -> new DoorBlock(BlockSetType.IRON, metal().strength(50.0F, 90.0F).noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));
    /** IC2 {@code BlockRubberSheet} ({@code blockRubber}): cloth material, hardness 0.8, {@code setResistance(2)}. */
    public static final DeferredBlock<RubberMatBlock> RUBBER_MAT = BLOCKS.register("rubber_mat",
            () -> new RubberMatBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                    .strength(0.8F, 1.2F).sound(SoundType.WOOL).noOcclusion().ignitedByLava()));
    /** IC2 {@code BlockITNT} ({@code blockITNT}): TNT material, hardness 0, grass sound, never drops. */
    public static final DeferredBlock<IndustrialTntBlock> INDUSTRIAL_TNT = BLOCKS.register("industrial_tnt",
            () -> new IndustrialTntBlock(BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).instabreak()
                    .sound(SoundType.GRASS).isRedstoneConductor((s, l, p) -> false)));

    /** IC2 {@code BlockITNT} ({@code blockNuke}): like industrial TNT, but breaking it by hand drops it. */
    public static final DeferredBlock<NukeBlock> NUKE = BLOCKS.register("nuke",
            () -> new NukeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).instabreak()
                    .sound(SoundType.GRASS).isRedstoneConductor((s, l, p) -> false)));

    /**
     * IC2 {@code BlockFoam} ({@code blockFoam}): fire material (replaceable, no collision, breaks when pushed),
     * hardness 0.01, {@code setResistance(10)} = 6 effective, cloth sound, counts as a normal cube.
     */
    public static final DeferredBlock<ConstructionFoamBlock> CONSTRUCTION_FOAM = BLOCKS.register("construction_foam",
            () -> new ConstructionFoamBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).noCollission()
                    .noOcclusion().replaceable().randomTicks().strength(0.01F, 6.0F).sound(SoundType.WOOL)
                    .pushReaction(PushReaction.DESTROY).isRedstoneConductor((s, l, p) -> true)
                    .isValidSpawn((s, l, p, t) -> false)));
    /** IC2 {@code BlockWall} ({@code blockWall}): rock material, hardness 3, {@code setResistance(30)} = 18. */
    public static final DeferredBlock<ConstructionFoamWallBlock> CONSTRUCTION_FOAM_WALL = BLOCKS.register(
            "construction_foam_wall", () -> new ConstructionFoamWallBlock(BlockBehaviour.Properties.of()
                    .mapColor(state -> PainterItem.dyeColor(state.getValue(ConstructionFoamWallBlock.COLOR))
                            .getMapColor())
                    .strength(3.0F, 18.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    /** IC2 {@code BlockScaffold} (wood): hardness 0.5, {@code setResistance(0.2)} = 0.12, strength 2/5. */
    public static final DeferredBlock<ScaffoldBlock> WOODEN_SCAFFOLD = BLOCKS.register("wooden_scaffold",
            () -> new ScaffoldBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).noOcclusion()
                    .strength(0.5F, 0.12F).sound(SoundType.WOOD).ignitedByLava()
                    .isRedstoneConductor((s, l, p) -> false).isSuffocating((s, l, p) -> false)
                    .isViewBlocking((s, l, p) -> false), 2, 5, () -> Items.STICK, 2));
    /** IC2 {@code BlockScaffold} (iron): hardness 0.8, {@code setResistance(10)} = 6, strength 5/12. */
    public static final DeferredBlock<ScaffoldBlock> IRON_SCAFFOLD = BLOCKS.register("iron_scaffold",
            () -> new ScaffoldBlock(metal().noOcclusion().strength(0.8F, 6.0F)
                    .isRedstoneConductor((s, l, p) -> false).isSuffocating((s, l, p) -> false)
                    .isViewBlocking((s, l, p) -> false), 5, 12, () -> Ic2Items.IRON_FENCE.get(), 1));
    /**
     * IC2 {@code BlockLuminator} (dark {@code blockLuminatorD} and lit {@code blockLuminator}): redstone-lamp material,
     * hardness 0.3, {@code setResistance(0.5)} = 0.3, glass sound, lit light level 15.
     */
    public static final DeferredBlock<LuminatorBlock> LUMINATOR = BLOCKS.register("luminator",
            () -> new LuminatorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).noOcclusion()
                    .strength(0.3F, 0.3F).sound(SoundType.GLASS)
                    .lightLevel(state -> state.getValue(LuminatorBlock.LIT) ? 15 : 0)
                    .isRedstoneConductor((s, l, p) -> false)));
    /**
     * IC2 {@code BlockCrop} ({@code blockCrop}): vine material (replaceable, burns, destroyed by pistons), hardness 0.8,
     * {@code setResistance(0.2)} = 0.12.
     */
    public static final DeferredBlock<CropBlock> CROP = BLOCKS.register("crop",
            () -> new CropBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noOcclusion().replaceable()
                    .ignitedByLava().strength(0.8F, 0.12F).pushReaction(PushReaction.DESTROY)
                    .isRedstoneConductor((s, l, p) -> false).isSuffocating((s, l, p) -> false)
                    .isViewBlocking((s, l, p) -> false)));

    /** IC2 {@code BlockBarrel}: wood material, hardness 1 (resistance 5 = 1.0), wood sound; no block item. */
    public static final DeferredBlock<BarrelBlock> BARREL = BLOCKS.register("booze_barrel",
            () -> new BarrelBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BASS).strength(1.0F).sound(SoundType.WOOD).ignitedByLava()));

    /** IC2 {@code BlockDynamite} ({@code blockDynamite}): TNT material, hardness 0, grass sound, no collision. */
    public static final DeferredBlock<DynamiteBlock> DYNAMITE_STICK = BLOCKS.register("dynamite_stick",
            () -> new DynamiteBlock(dynamiteProperties()));
    /** IC2 {@code BlockDynamite} ({@code blockDynamiteRemote}): a stick linked to a Dynamite-O-Mote. */
    public static final DeferredBlock<DynamiteBlock> DYNAMITE_STICK_WITH_REMOTE =
            BLOCKS.register("dynamite_stick_with_remote", () -> new DynamiteBlock(dynamiteProperties()));

    private static BlockBehaviour.Properties dynamiteProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).instabreak().noCollission().noOcclusion()
                .sound(SoundType.GRASS);
    }

    private Ic2Blocks() {
    }

    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }

    private static MachineBlock createMachineBlock(Ic2Machine machine) {
        BlockBehaviour.Properties properties = metal().strength(machine.getHardness());
        return switch (machine) {
            case BATBOX, MFE, MFSU -> new ElectricStorageBlock(properties, machine,
                    (pos, state) -> new ElectricStorageBlockEntity(Ic2BlockEntityTypes.get(machine), pos, state,
                            ElectricStorageBlockEntity.Spec.of(machine)));
            case MASS_FABRICATOR -> new MassFabricatorBlock(properties);
            // IC2 BlockPersonal: unbreakable, setResistance(6 000 000) = 3 600 000 (bedrock).
            case PERSONAL_SAFE, TRADE_O_MAT, ENERGY_O_MAT -> new PersonalBlock(
                    metal().strength(-1.0F, 3600000.0F).noOcclusion(), machine, factory(machine));
            case NUCLEAR_REACTOR -> new NuclearReactorBlock(properties);
            case ROTARY_MACERATOR, SINGULARITY_COMPRESSOR, CENTRIFUGE_EXTRACTOR ->
                    new AdvancedMachineBlock(properties, machine, factory(machine));
            case LV_TRANSFORMER, MV_TRANSFORMER, HV_TRANSFORMER -> new MachineBlock(properties, machine,
                    (pos, state) -> new TransformerBlockEntity(Ic2BlockEntityTypes.get(machine), pos, state,
                            TransformerBlockEntity.Spec.of(machine)));
            default -> new MachineBlock(properties, machine, factory(machine));
        };
    }

    private static BiFunction<BlockPos, BlockState, ? extends MachineBlockEntity> factory(Ic2Machine machine) {
        return switch (machine) {
            case GENERATOR -> GeneratorBlockEntity::new;
            case GEOTHERMAL_GENERATOR -> GeothermalGeneratorBlockEntity::new;
            case WATER_MILL -> WaterMillBlockEntity::new;
            case SOLAR_PANEL -> SolarPanelBlockEntity::new;
            case WIND_MILL -> WindMillBlockEntity::new;
            case IRON_FURNACE -> IronFurnaceBlockEntity::new;
            case ELECTRIC_FURNACE -> ElectricFurnaceBlockEntity::new;
            case MACERATOR -> RecipeMachineBlockEntity::macerator;
            case EXTRACTOR -> RecipeMachineBlockEntity::extractor;
            case COMPRESSOR -> RecipeMachineBlockEntity::compressor;
            case RECYCLER -> RecyclerBlockEntity::new;
            case MAGNETIZER -> MagnetizerBlockEntity::new;
            case ELECTROLYZER -> ElectrolyzerBlockEntity::new;
            case INDUCTION_FURNACE -> InductionFurnaceBlockEntity::new;
            case CANNING_MACHINE -> CanningMachineBlockEntity::new;
            case PUMP -> PumpBlockEntity::new;
            case MINER -> MinerBlockEntity::new;
            case TELEPORTER -> TeleporterBlockEntity::new;
            case TESLA_COIL -> TeslaCoilBlockEntity::new;
            case CROP_MATRON -> CropMatronBlockEntity::new;
            case TERRAFORMER -> TerraformerBlockEntity::new;
            case PERSONAL_SAFE -> PersonalSafeBlockEntity::new;
            case TRADE_O_MAT -> TradeOMatBlockEntity::new;
            case ENERGY_O_MAT -> EnergyOMatBlockEntity::new;
            case ROTARY_MACERATOR -> AdvancedMachineBlockEntity::rotaryMacerator;
            case SINGULARITY_COMPRESSOR -> AdvancedMachineBlockEntity::singularityCompressor;
            case CENTRIFUGE_EXTRACTOR -> AdvancedMachineBlockEntity::centrifugeExtractor;
            case BATBOX, MFE, MFSU, LV_TRANSFORMER, MV_TRANSFORMER, HV_TRANSFORMER, MASS_FABRICATOR, NUCLEAR_REACTOR ->
                    throw new IllegalArgumentException(machine + " has its own block class");
        };
    }

    public static DeferredBlock<CableBlock> cable(CableType type) {
        return CABLES.get(type);
    }

    public static DeferredBlock<MachineBlock> machine(Ic2Machine machine) {
        return MACHINES.get(machine);
    }
}
