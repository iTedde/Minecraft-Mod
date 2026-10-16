package dev.craft.powerplants;

import dev.craft.Craft;
import dev.craft.ic2.machine.MachineBlock;
import dev.craft.powerplants.block.PlantControllerBlock;
import dev.craft.powerplants.block.PlantPartBlock;
import dev.craft.powerplants.boiler.BoilerBlockEntity;
import dev.craft.powerplants.cokeoven.CokeOvenBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PpBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    public static final DeferredBlock<PlantPartBlock> COKE_OVEN_BRICKS = BLOCKS.register("coke_oven_bricks",
            () -> new PlantPartBlock(bricks()));
    public static final DeferredBlock<PlantControllerBlock> COKE_OVEN = BLOCKS.register("coke_oven",
            () -> new PlantControllerBlock(bricks().lightLevel(PpBlocks::activeLight), CokeOvenBlockEntity::new));
    public static final DeferredBlock<PlantPartBlock> BOILER_CASING = BLOCKS.register("boiler_casing",
            () -> new PlantPartBlock(bricks()));
    public static final DeferredBlock<PlantControllerBlock> BOILER_FIREBOX = BLOCKS.register("boiler_firebox",
            () -> new PlantControllerBlock(bricks().lightLevel(PpBlocks::activeLight), BoilerBlockEntity::new));
    public static final DeferredBlock<Block> COKE_BLOCK = BLOCKS.registerSimpleBlock("coke_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(5.0F, 6.0F)
                    .requiresCorrectToolForDrops());

    private PpBlocks() {
    }

    private static int activeLight(BlockState state) {
        return state.getValue(MachineBlock.ACTIVE) ? 13 : 0;
    }

    /** Coke oven and boiler blocks: hardness 2.0, resistance 10, pickaxe (spec 14 §2, §3). */
    private static BlockBehaviour.Properties bricks() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(2.0F, 10.0F)
                .sound(SoundType.STONE).requiresCorrectToolForDrops();
    }
}
