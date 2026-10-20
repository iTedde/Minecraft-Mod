package dev.craft.thermalexpansion;

import java.util.EnumMap;
import java.util.Map;

import dev.craft.Craft;
import dev.craft.thermalexpansion.block.HardenedGlassBlock;
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
