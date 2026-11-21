package dev.craft.core;

import dev.craft.Craft;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Hoppers ignore redstone (user 2026-10-26). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class HopperGameTests {
    private static final BlockPos HOPPER = new BlockPos(2, 2, 2);

    private HopperGameTests() {
    }

    @GameTest(template = "empty_8x4x8")
    public static void poweredHopperStaysEnabled(GameTestHelper helper) {
        helper.setBlock(HOPPER, Blocks.HOPPER);
        helper.setBlock(HOPPER.east(), Blocks.REDSTONE_BLOCK);
        helper.<HopperBlockEntity>getBlockEntity(HOPPER).setItem(0, new ItemStack(Items.COBBLESTONE));
        helper.setBlock(HOPPER.below(), Blocks.CHEST);
        helper.succeedWhen(() -> {
            helper.assertBlockProperty(HOPPER, HopperBlock.ENABLED, true);
            helper.assertContainerContains(HOPPER.below(), Items.COBBLESTONE);
        });
    }

    @GameTest(template = "empty_8x4x8")
    public static void disabledHopperFromOldWorldStillMoves(GameTestHelper helper) {
        helper.setBlock(HOPPER.below(), Blocks.CHEST);
        helper.setBlock(HOPPER, Blocks.HOPPER);
        // like a hopper saved switched off: no neighbour update re-enables it
        helper.getLevel().setBlock(helper.absolutePos(HOPPER),
                helper.getBlockState(HOPPER).setValue(HopperBlock.ENABLED, false), 2);
        helper.<HopperBlockEntity>getBlockEntity(HOPPER).setItem(0, new ItemStack(Items.COBBLESTONE));
        helper.succeedWhen(() -> helper.assertContainerContains(HOPPER.below(), Items.COBBLESTONE));
    }
}
