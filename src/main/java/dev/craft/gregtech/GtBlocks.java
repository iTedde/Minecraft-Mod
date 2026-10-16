package dev.craft.gregtech;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import dev.craft.Craft;
import dev.craft.gregtech.block.GtCasingBlock;
import dev.craft.gregtech.machine.GtMachineBlock;
import dev.craft.gregtech.world.GtOre;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GtBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    private static final Map<GtMachine, DeferredBlock<GtMachineBlock>> MACHINES = new EnumMap<>(GtMachine.class);
    private static final Map<GtCasingBlock.Type, DeferredBlock<GtCasingBlock>> CASINGS =
            new EnumMap<>(GtCasingBlock.Type.class);
    private static final Map<GtOre, DeferredBlock<DropExperienceBlock>> ORES = new EnumMap<>(GtOre.class);
    private static final Map<GtOre, DeferredBlock<DropExperienceBlock>> DEEPSLATE_ORES = new EnumMap<>(GtOre.class);

    static {
        for (GtMachine machine : GtMachine.values()) {
            MACHINES.put(machine, BLOCKS.register(machine.getName(), () -> new GtMachineBlock(machineProperties(),
                    machine)));
        }
        for (GtCasingBlock.Type type : GtCasingBlock.Type.values()) {
            CASINGS.put(type, BLOCKS.register(type.getName(), () -> new GtCasingBlock(casingProperties(type), type)));
        }
        for (GtOre ore : GtOre.values()) {
            ORES.put(ore, BLOCKS.register(ore.getBlockName(), () -> oreBlock(ore, false)));
            if (ore.hasDeepslateVariant()) {
                DEEPSLATE_ORES.put(ore, BLOCKS.register(ore.getDeepslateBlockName(), () -> oreBlock(ore, true)));
            }
        }
    }

    /**
     * {@code GT_BlockMetaID_Block} 10: hardness 10, explosion resistance 200; no machine casing, so it does not notify
     * the machines around it.
     */
    public static final DeferredBlock<Block> HIGHLY_ADVANCED_MACHINE_BLOCK =
            BLOCKS.registerBlock("highly_advanced_machine_block", Block::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(10.0F, 200.0F).sound(SoundType.METAL)
                    .requiresCorrectToolForDrops().isValidSpawn((state, level, pos, type) -> false));
    /** {@code GT_BlockMetaID_Block} 11 "Block of Steel": hardness 3, explosion resistance 100; no machine casing. */
    public static final DeferredBlock<Block> STEEL_BLOCK =
            BLOCKS.registerBlock("steel_block", Block::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(3.0F, 100.0F).sound(SoundType.METAL)
                    .requiresCorrectToolForDrops().isValidSpawn((state, level, pos, type) -> false));

    private GtBlocks() {
    }

    public static DeferredBlock<GtMachineBlock> machine(GtMachine machine) {
        return MACHINES.get(machine);
    }

    public static DeferredBlock<GtCasingBlock> casing(GtCasingBlock.Type type) {
        return CASINGS.get(type);
    }

    public static DeferredBlock<DropExperienceBlock> ore(GtOre ore) {
        return ORES.get(ore);
    }

    /** The deepslate variant, {@code null} if the ore has none. */
    public static DeferredBlock<DropExperienceBlock> deepslateOre(GtOre ore) {
        return DEEPSLATE_ORES.get(ore);
    }

    /** All GT ore blocks including the deepslate variants. */
    public static List<DeferredBlock<DropExperienceBlock>> allOres() {
        List<DeferredBlock<DropExperienceBlock>> ores = new ArrayList<>(ORES.values());
        ores.addAll(DEEPSLATE_ORES.values());
        return ores;
    }

    /**
     * {@code GT_BlockMetaID_Ore}: Material.rock, stone sound, resistance 6, pickaxe of the ore's level; experience only
     * when not mined with silk touch (vanilla {@code DropExperienceBlock}).
     */
    private static DropExperienceBlock oreBlock(GtOre ore, boolean deepslate) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(ore.getHardness()
                        + (deepslate ? GtOre.DEEPSLATE_EXTRA_HARDNESS : 0.0F), GtOre.RESISTANCE);
        if (deepslate) {
            properties.mapColor(MapColor.DEEPSLATE).sound(SoundType.DEEPSLATE);
        } else {
            properties.mapColor(switch (ore.getGround()) {
                case STONE -> MapColor.STONE;
                case NETHERRACK -> MapColor.NETHER;
                case END_STONE -> MapColor.SAND;
            });
        }
        return new DropExperienceBlock(ore.getExperience() != null ? ore.getExperience() : ConstantInt.of(0),
                properties);
    }

    /** {@code GT_BlockMetaID_Block}: Material.iron, hardness 3, iron pickaxe, no mob spawns. */
    private static BlockBehaviour.Properties casingProperties(GtCasingBlock.Type type) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, type.getResistance())
                .sound(SoundType.METAL).requiresCorrectToolForDrops().isValidSpawn((state, level, pos, t) -> false);
    }

    /** {@code GT_BlockMetaID_Machine}: Material.iron, hardness 3, resistance 10 (effective 6), no mob spawns. */
    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().isValidSpawn((state, level, pos, type) -> false);
    }
}
