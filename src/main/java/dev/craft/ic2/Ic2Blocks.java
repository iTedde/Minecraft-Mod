package dev.craft.ic2;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;

import dev.craft.Craft;
import dev.craft.ic2.block.IndustrialTntBlock;
import dev.craft.ic2.block.NukeBlock;
import dev.craft.ic2.reactor.NuclearReactorBlock;
import dev.craft.ic2.reactor.ReactorChamberBlock;
import dev.craft.ic2.block.IronFenceBlock;
import dev.craft.ic2.block.MiningPipeBlock;
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
            case NUCLEAR_REACTOR -> new NuclearReactorBlock(properties);
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
