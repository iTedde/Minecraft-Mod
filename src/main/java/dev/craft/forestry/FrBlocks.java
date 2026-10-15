package dev.craft.forestry;

import dev.craft.Craft;
import dev.craft.forestry.engine.ElectricalEngineBlock;
import dev.craft.forestry.machine.CarpenterBlock;
import dev.craft.forestry.machine.FabricatorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FrBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    /** Forestry {@code BlockMachine}: Material.rock (needs a pickaxe for drops), hardness 1.5. */
    public static final DeferredBlock<FabricatorBlock> THERMIONIC_FABRICATOR = BLOCKS.register("thermionic_fabricator",
            () -> new FabricatorBlock(machineProperties()));
    public static final DeferredBlock<CarpenterBlock> CARPENTER = BLOCKS.register("carpenter",
            () -> new CarpenterBlock(machineProperties()));
    /** Forestry {@code BlockEngine}: material like {@code BlockMachine}, hardness 3.5. */
    public static final DeferredBlock<ElectricalEngineBlock> ELECTRICAL_ENGINE = BLOCKS.register("electrical_engine",
            () -> new ElectricalEngineBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.5F)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE).noOcclusion()));

    private FrBlocks() {
    }

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F).requiresCorrectToolForDrops()
                .sound(SoundType.STONE).noOcclusion();
    }
}
