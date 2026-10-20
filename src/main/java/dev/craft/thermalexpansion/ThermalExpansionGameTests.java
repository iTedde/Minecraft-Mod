package dev.craft.thermalexpansion;

import dev.craft.Craft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for Thermal Expansion (spec 26). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class ThermalExpansionGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(2, 1, 2);

    private ThermalExpansionGameTests() {
    }

    /** The crescent hammer turns a vanilla furnace; sneaking turns it back. */
    @GameTest(template = EMPTY)
    public static void crescentHammerRotatesVanillaBlocks(GameTestHelper helper) {
        helper.setBlock(POS, Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, Direction.NORTH));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack hammer = new ItemStack(TeItems.CRESCENT_HAMMER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, hammer);
        BlockPos abs = helper.absolutePos(POS);
        hammer.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false)));
        helper.assertBlockProperty(POS, FurnaceBlock.FACING, Direction.EAST);
        helper.succeed();
    }

    /** Hardened glass drops nothing when broken; a sneaking wrench click gives it back. */
    @GameTest(template = EMPTY)
    public static void hardenedGlassOnlyDismantles(GameTestHelper helper) {
        helper.setBlock(POS, TeBlocks.HARDENED_GLASS.get());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TeItems.CRESCENT_HAMMER.get()));
        helper.useBlock(POS, player);
        helper.assertBlockNotPresent(TeBlocks.HARDENED_GLASS.get(), POS);
        helper.assertTrue(player.getInventory().contains(new ItemStack(TeItems.HARDENED_GLASS.get())), "glass returned");
        helper.succeed();
    }
}
