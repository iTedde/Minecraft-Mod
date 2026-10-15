package dev.craft.forestry;

import dev.craft.Craft;
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

    private FrBlocks() {
    }

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F).requiresCorrectToolForDrops()
                .sound(SoundType.STONE).noOcclusion();
    }
}
