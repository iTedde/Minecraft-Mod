package dev.craft.ic2.block;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2EntityTypes;
import dev.craft.ic2.Ic2Items;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The IC2 nuke (phase 7c). Nothing here lets a nuke go off: a 35-power explosion would wreck neighbouring tests. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class NukeGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(5, 1, 5);

    private NukeGameTests() {
    }

    private static List<NukeEntity> takePrimed(GameTestHelper helper) {
        List<NukeEntity> primed = helper.getEntities(Ic2EntityTypes.NUKE.get());
        primed.forEach(NukeEntity::discard);
        return primed;
    }

    /** Redstone primes it with the full fuse of 300 ticks. */
    @GameTest(template = EMPTY)
    public static void primesWithRedstone(GameTestHelper helper) {
        helper.setBlock(POS.east(), Blocks.REDSTONE_BLOCK);
        helper.setBlock(POS, Ic2Blocks.NUKE.get());
        helper.assertBlockPresent(Blocks.AIR, POS);
        List<NukeEntity> primed = takePrimed(helper);
        helper.assertValueEqual(primed.size(), 1, "primed nukes");
        helper.assertValueEqual(primed.getFirst().getFuse(), 300, "fuse");
        helper.succeed();
    }

    /** A signal source placed next to it later primes it as well (IC2 looks at the new neighbour block). */
    @GameTest(template = EMPTY)
    public static void primesWhenRedstoneIsPlacedNextToIt(GameTestHelper helper) {
        helper.setBlock(POS, Ic2Blocks.NUKE.get());
        helper.setBlock(POS.east(), Blocks.REDSTONE_BLOCK);
        helper.assertBlockPresent(Blocks.AIR, POS);
        helper.assertValueEqual(takePrimed(helper).size(), 1, "primed nukes");
        helper.succeed();
    }

    /** Unlike industrial TNT, breaking it by hand drops the nuke instead of priming it. */
    @GameTest(template = EMPTY)
    public static void breakingDropsIt(GameTestHelper helper) {
        helper.setBlock(POS, Ic2Blocks.NUKE.get());
        BlockPos abs = helper.absolutePos(POS);
        BlockState state = helper.getLevel().getBlockState(abs);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        state.onDestroyedByPlayer(helper.getLevel(), abs, player, true, Fluids.EMPTY.defaultFluidState());
        helper.assertBlockPresent(Blocks.AIR, POS);
        helper.assertValueEqual(takePrimed(helper).size(), 0, "primed nukes");
        helper.assertItemEntityPresent(Ic2Items.NUKE.get());
        helper.succeed();
    }

    /** A wrench defuses a primed nuke and loses one durability point. */
    @GameTest(template = EMPTY)
    public static void wrenchDefuses(GameTestHelper helper) {
        BlockPos abs = helper.absolutePos(POS);
        NukeEntity nuke = new NukeEntity(helper.getLevel(), abs.getX() + 0.5, abs.getY() + 0.5, abs.getZ() + 0.5);
        helper.getLevel().addFreshEntity(nuke);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Ic2Items.WRENCH.get()));
        nuke.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(nuke.isRemoved(), "defused");
        helper.assertValueEqual(player.getMainHandItem().getDamageValue(), 1, "wrench wear");
        helper.succeed();
    }
}
