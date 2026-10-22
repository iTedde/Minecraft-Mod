package dev.craft.thermalexpansion;

import java.util.EnumMap;
import java.util.Map;

import dev.craft.Craft;
import dev.craft.thermalexpansion.block.HardenedGlassBlock;
import dev.craft.thermalexpansion.block.PortableTankBlock;
import dev.craft.thermalexpansion.transport.ConduitBlock;
import dev.craft.thermalexpansion.transport.ConduitBlockEntity;
import dev.craft.thermalexpansion.transport.EnergyConduitBlockEntity;
import dev.craft.thermalexpansion.transport.LiquiductBlockEntity;
import dev.craft.thermalexpansion.transport.EnergyTesseractBlockEntity;
import dev.craft.thermalexpansion.transport.ItemTesseractBlockEntity;
import dev.craft.thermalexpansion.transport.LiquidTesseractBlockEntity;
import dev.craft.thermalexpansion.transport.TesseractBlock;
import dev.craft.thermalexpansion.transport.TesseractBlockEntity;
import dev.craft.thermalexpansion.transport.TesseractType;
import dev.craft.thermalexpansion.energy.EnergyCellBlock;
import dev.craft.thermalexpansion.energy.MagmaticEngineBlockEntity;
import dev.craft.thermalexpansion.energy.SteamEngineBlockEntity;
import dev.craft.thermalexpansion.energy.TeEngineBlock;
import dev.craft.thermalexpansion.energy.TeEngineBlockEntity;
import dev.craft.thermalexpansion.energy.TeEngineType;
import dev.craft.thermalexpansion.machine.AqueousAccumulatorBlockEntity;
import dev.craft.thermalexpansion.machine.GlacialPrecipitatorBlockEntity;
import dev.craft.thermalexpansion.machine.IgneousExtruderBlockEntity;
import dev.craft.thermalexpansion.machine.CrucibleBlockEntity;
import dev.craft.thermalexpansion.machine.PoweredFurnaceBlockEntity;
import dev.craft.thermalexpansion.machine.SmelterBlockEntity;
import dev.craft.thermalexpansion.machine.TransposerBlockEntity;
import dev.craft.thermalexpansion.machine.SecondaryOutputMachineBlockEntity;
import dev.craft.thermalexpansion.machine.TeMachineBlock;
import dev.craft.thermalexpansion.machine.TeMachineBlockEntity;
import dev.craft.thermalexpansion.machine.TeMachineType;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Thermal Expansion blocks (spec 26 §1); machines, engines and transport follow in their sub-phases. */
public final class TeBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    /** {@code BlockOre}: hardness 3, {@code setResistance(5)} = 3 effective, iron pickaxe (harvest level 2). */
    public static final DeferredBlock<Block> SILVER_ORE = ore("silver_ore");
    public static final DeferredBlock<Block> DEEPSLATE_SILVER_ORE = deepslateOre("deepslate_silver_ore");
    public static final DeferredBlock<Block> LEAD_ORE = ore("lead_ore");
    public static final DeferredBlock<Block> DEEPSLATE_LEAD_ORE = deepslateOre("deepslate_lead_ore");
    public static final DeferredBlock<Block> NICKEL_ORE = ore("nickel_ore");
    public static final DeferredBlock<Block> DEEPSLATE_NICKEL_ORE = deepslateOre("deepslate_nickel_ore");

    /** {@code BlockHardenedGlass}: hardness 3, {@code setResistance(155)} = 93, drops only when dismantled. */
    public static final DeferredBlock<HardenedGlassBlock> HARDENED_GLASS = BLOCKS.register("hardened_glass",
            () -> new HardenedGlassBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE)
                    .instrument(NoteBlockInstrument.HAT).strength(3.0F, 93.0F).sound(SoundType.GLASS).noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false).isRedstoneConductor((s, l, p) -> false)
                    .isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false)));

    public static final DeferredBlock<TeMachineBlock> POWERED_FURNACE = machine(TeMachineType.POWERED_FURNACE,
            PoweredFurnaceBlockEntity::new);
    public static final DeferredBlock<TeMachineBlock> PULVERIZER = machine(TeMachineType.PULVERIZER,
            SecondaryOutputMachineBlockEntity::pulverizer);
    public static final DeferredBlock<TeMachineBlock> SAWMILL = machine(TeMachineType.SAWMILL,
            SecondaryOutputMachineBlockEntity::sawmill);
    public static final DeferredBlock<TeMachineBlock> INDUCTION_SMELTER = machine(TeMachineType.INDUCTION_SMELTER,
            SmelterBlockEntity::new);
    public static final DeferredBlock<TeMachineBlock> MAGMA_CRUCIBLE = machine(TeMachineType.MAGMA_CRUCIBLE,
            CrucibleBlockEntity::new);
    public static final DeferredBlock<TeMachineBlock> LIQUID_TRANSPOSER = machine(TeMachineType.LIQUID_TRANSPOSER,
            TransposerBlockEntity::new);
    public static final DeferredBlock<TeMachineBlock> GLACIAL_PRECIPITATOR =
            machine(TeMachineType.GLACIAL_PRECIPITATOR, GlacialPrecipitatorBlockEntity::new);
    public static final DeferredBlock<TeMachineBlock> IGNEOUS_EXTRUDER = machine(TeMachineType.IGNEOUS_EXTRUDER,
            IgneousExtruderBlockEntity::new);
    public static final DeferredBlock<TeMachineBlock> AQUEOUS_ACCUMULATOR = machine(TeMachineType.AQUEOUS_ACCUMULATOR,
            AqueousAccumulatorBlockEntity::new);
    /** {@code BlockEngine}: Material.iron (pickaxe), hardness 0.5. */
    public static final DeferredBlock<TeEngineBlock> STEAM_ENGINE = engine(TeEngineType.STEAM,
            SteamEngineBlockEntity::new);
    public static final DeferredBlock<TeEngineBlock> MAGMATIC_ENGINE = engine(TeEngineType.MAGMATIC,
            MagmaticEngineBlockEntity::new);
    /** {@code BlockEnergyCell}: Material.iron (pickaxe), hardness 20, {@code setResistance(150)} = 90. */
    public static final DeferredBlock<EnergyCellBlock> ENERGY_CELL = BLOCKS.register("redstone_energy_cell",
            () -> new EnergyCellBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops().strength(20.0F, 90.0F).sound(SoundType.METAL).noOcclusion()));
    /** {@code BlockConduit}: Material.glass (no tool needed), hardness 3, {@code setResistance(150)} = 90. */
    public static final DeferredBlock<ConduitBlock> ENERGY_CONDUIT = conduit("redstone_energy_conduit", true,
            EnergyConduitBlockEntity::new);
    public static final DeferredBlock<ConduitBlock> LIQUIDUCT = conduit("liquiduct", false,
            LiquiductBlockEntity::new);
    /** {@code BlockTeleport}: Material.iron (pickaxe), hardness 15, {@code setResistance(200)} = 120. */
    public static final DeferredBlock<TesseractBlock> ENERGY_TESSERACT = tesseract(TesseractType.ENERGY,
            EnergyTesseractBlockEntity::new);
    public static final DeferredBlock<TesseractBlock> LIQUID_TESSERACT = tesseract(TesseractType.LIQUID,
            LiquidTesseractBlockEntity::new);
    public static final DeferredBlock<TesseractBlock> ITEM_TESSERACT = tesseract(TesseractType.ITEM,
            ItemTesseractBlockEntity::new);
    /** {@code BlockTank}: Material.glass (no tool needed), hardness 3, {@code setResistance(150)} = 90. */
    public static final DeferredBlock<PortableTankBlock> PORTABLE_TANK = BLOCKS.register("portable_tank",
            () -> new PortableTankBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE)
                    .instrument(NoteBlockInstrument.HAT).strength(3.0F, 90.0F).sound(SoundType.GLASS).noOcclusion()
                    .isRedstoneConductor((s, l, p) -> false).isSuffocating((s, l, p) -> false)
                    .isViewBlocking((s, l, p) -> false)));

    private static final Map<DyeColor, DeferredBlock<Block>> ROCKWOOL = new EnumMap<>(DyeColor.class);

    static {
        // BlockRockwool: CoFH clothFireproof (wool that does not burn), hardness 0.8, setResistance(10) = 6.
        for (DyeColor color : DyeColor.values()) {
            ROCKWOOL.put(color, BLOCKS.registerSimpleBlock(color.getSerializedName() + "_rockwool",
                    BlockBehaviour.Properties.of().mapColor(color.getMapColor()).instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.8F, 6.0F).sound(SoundType.WOOL)));
        }
    }

    private TeBlocks() {
    }

    public static DeferredBlock<Block> rockwool(DyeColor color) {
        return ROCKWOOL.get(color);
    }

    /** {@code BlockMachine}: Material.iron (any pickaxe), hardness 15, {@code setResistance(25)} = 15. */
    private static DeferredBlock<TeMachineBlock> machine(TeMachineType type,
                                                         BiFunction<BlockPos, BlockState, ? extends TeMachineBlockEntity> factory) {
        return BLOCKS.register(type.getName(), () -> new TeMachineBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(15.0F, 15.0F).sound(SoundType.METAL),
                type, factory));
    }

    private static DeferredBlock<TeEngineBlock> engine(TeEngineType type,
                                                       BiFunction<BlockPos, BlockState, ? extends TeEngineBlockEntity> factory) {
        return BLOCKS.register(type.getName(), () -> new TeEngineBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(0.5F).sound(SoundType.METAL)
                .noOcclusion(), type, factory));
    }

    private static DeferredBlock<ConduitBlock> conduit(String name, boolean energy,
                                                       BiFunction<BlockPos, BlockState, ? extends ConduitBlockEntity> factory) {
        return BLOCKS.register(name, () -> new ConduitBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE)
                .instrument(NoteBlockInstrument.HAT).strength(3.0F, 90.0F).sound(SoundType.GLASS).noOcclusion()
                .isRedstoneConductor((s, l, p) -> false).isSuffocating((s, l, p) -> false)
                .isViewBlocking((s, l, p) -> false), energy, factory));
    }

    private static DeferredBlock<TesseractBlock> tesseract(TesseractType type,
                                                           BiFunction<BlockPos, BlockState, ? extends TesseractBlockEntity> factory) {
        return BLOCKS.register(type.getBlockName(), () -> new TesseractBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(15.0F, 120.0F).sound(SoundType.METAL)
                .noOcclusion(), type, factory));
    }

    private static DeferredBlock<Block> ore(String name) {
        return BLOCKS.registerSimpleBlock(name, BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                .instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F));
    }

    /** Deepslate variant: +1.5 hardness like the vanilla deepslate ores. */
    private static DeferredBlock<Block> deepslateOre(String name) {
        return BLOCKS.registerSimpleBlock(name, BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE)
                .instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(4.5F, 3.0F)
                .sound(SoundType.DEEPSLATE));
    }
}
