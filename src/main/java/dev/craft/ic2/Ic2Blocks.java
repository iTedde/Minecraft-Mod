package dev.craft.ic2;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;

import dev.craft.Craft;
import dev.craft.ic2.block.IronFenceBlock;
import dev.craft.ic2.block.ResinSheetBlock;
import dev.craft.ic2.block.RubberLeavesBlock;
import dev.craft.ic2.block.RubberSaplingBlock;
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
import net.minecraft.world.level.block.SoundType;
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
            case BATBOX, MFE, MFSU, LV_TRANSFORMER, MV_TRANSFORMER, HV_TRANSFORMER, MASS_FABRICATOR ->
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
