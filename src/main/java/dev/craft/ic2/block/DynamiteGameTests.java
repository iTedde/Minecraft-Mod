package dev.craft.ic2.block;

import dev.craft.Craft;
import dev.craft.core.explosion.PointExplosion;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2EntityTypes;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.item.DynamiteRemoteItem;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Dynamite, sticky dynamite, dynamite sticks and the Dynamite-O-Mote (phase 12.9). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class DynamiteGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(4, 2, 4);

    private DynamiteGameTests() {
    }

    private static List<DynamiteEntity> primed(GameTestHelper helper) {
        return helper.getEntities(Ic2EntityTypes.DYNAMITE.get());
    }

    private static DynamiteEntity takeSinglePrimed(GameTestHelper helper) {
        List<DynamiteEntity> primed = primed(helper);
        helper.assertValueEqual(primed.size(), 1, "primed dynamite");
        primed.forEach(DynamiteEntity::discard);
        return primed.getFirst();
    }

    /** Places a stick standing on stone at {@link #POS}. */
    private static void placeStick(GameTestHelper helper, DynamiteBlock block) {
        helper.setBlock(POS.below(), Blocks.STONE);
        helper.setBlock(POS, block);
    }

    private static UseOnContext useOn(Player player, GameTestHelper helper, BlockPos pos, Direction face) {
        BlockPos abs = helper.absolutePos(pos);
        return new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(abs), face, abs, false));
    }

    /** Resistance ≤ 10 breaks with full drops (stone, dirt); obsidian (IC2 36) stays; a stick in range goes off. */
    @GameTest(template = EMPTY)
    public static void pointExplosionBreaksWeakBlocks(GameTestHelper helper) {
        helper.setBlock(POS.east(), Blocks.STONE);
        helper.setBlock(POS.west(), Blocks.OBSIDIAN);
        helper.setBlock(POS.north().below(), Blocks.STONE);
        helper.setBlock(POS.north(), Ic2Blocks.DYNAMITE_STICK.get());
        helper.setBlock(POS.south(), Blocks.DIRT);
        helper.setBlock(POS.east(2), Blocks.STONE);
        BlockPos abs = helper.absolutePos(POS);
        new PointExplosion(helper.getLevel(), abs.getX() + 0.5, abs.getY() + 0.5, abs.getZ() + 0.5, 1.0F, 1.0F, 0.8F)
                .explode(1);
        helper.assertBlockPresent(Blocks.AIR, POS.east());
        helper.assertBlockPresent(Blocks.AIR, POS.south());
        helper.assertBlockPresent(Blocks.OBSIDIAN, POS.west());
        helper.assertBlockPresent(Blocks.STONE, POS.east(2));
        helper.assertItemEntityPresent(Items.COBBLESTONE);
        helper.assertItemEntityPresent(Items.DIRT);
        helper.assertValueEqual(takeSinglePrimed(helper).getFuse(), DynamiteBlock.EXPLODED_FUSE, "chained fuse");
        helper.succeed();
    }

    /** Thrown dynamite flies with fuse 100 and takes one from the stack. */
    @GameTest(template = EMPTY)
    public static void throwingSpawnsDynamite(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(Vec3.atBottomCenterOf(POS)));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Ic2Items.STICKY_DYNAMITE.get(), 2));
        player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(player.getMainHandItem().getCount(), 1, "stack");
        List<DynamiteEntity> thrown = helper.getEntities(Ic2EntityTypes.STICKY_DYNAMITE.get());
        helper.assertValueEqual(thrown.size(), 1, "thrown sticky dynamite");
        DynamiteEntity dynamite = thrown.getFirst();
        helper.assertTrue(dynamite.isSticky(), "sticky");
        helper.assertValueEqual(dynamite.getFuse(), DynamiteEntity.FUSE, "fuse");
        helper.assertTrue(dynamite.getDeltaMovement().length() > 0.9, "speed 1");
        dynamite.discard();
        helper.succeed();
    }

    /** Plain dynamite used on a block places a stick, attached to the clicked wall; sticky dynamite does not. */
    @GameTest(template = EMPTY)
    public static void dynamitePlacesStick(GameTestHelper helper) {
        helper.setBlock(POS.east(), Blocks.STONE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Ic2Items.STICKY_DYNAMITE.get(), 2));
        player.getMainHandItem().useOn(useOn(player, helper, POS.east(), Direction.WEST));
        helper.assertBlockPresent(Blocks.AIR, POS);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Ic2Items.DYNAMITE.get(), 2));
        player.getMainHandItem().useOn(useOn(player, helper, POS.east(), Direction.WEST));
        helper.assertBlockPresent(Ic2Blocks.DYNAMITE_STICK.get(), POS);
        helper.assertBlockProperty(POS, DynamiteBlock.SUPPORT, Direction.EAST);
        helper.assertValueEqual(player.getMainHandItem().getCount(), 1, "stack");
        helper.succeed();
    }

    /** A redstone signal primes the stick with fuse 40. */
    @GameTest(template = EMPTY)
    public static void redstonePrimesStick(GameTestHelper helper) {
        placeStick(helper, Ic2Blocks.DYNAMITE_STICK.get());
        helper.assertBlockProperty(POS, DynamiteBlock.SUPPORT, Direction.DOWN);
        helper.setBlock(POS.west(), Blocks.REDSTONE_BLOCK);
        helper.assertBlockPresent(Blocks.AIR, POS);
        helper.assertValueEqual(takeSinglePrimed(helper).getFuse(), DynamiteBlock.IGNITE_FUSE, "fuse");
        helper.succeed();
    }

    /** Without any solid neighbour the stick goes off almost at once (fuse 5). */
    @GameTest(template = EMPTY)
    public static void lostSupportSetsOffStick(GameTestHelper helper) {
        placeStick(helper, Ic2Blocks.DYNAMITE_STICK.get());
        helper.setBlock(POS.below(), Blocks.AIR);
        helper.assertBlockPresent(Blocks.AIR, POS);
        helper.assertValueEqual(takeSinglePrimed(helper).getFuse(), DynamiteBlock.EXPLODED_FUSE, "fuse");
        helper.succeed();
    }

    /**
     * Linking turns the stick into a linked one and back; another remote cannot unlink it; firing sets off all linked
     * sticks with fuse 5 and clears the list.
     */
    @GameTest(template = EMPTY)
    public static void remoteLinksAndFires(GameTestHelper helper) {
        placeStick(helper, Ic2Blocks.DYNAMITE_STICK.get());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Ic2Items.DYNAMITE_O_MOTE.get()));
        ItemStack remote = player.getMainHandItem();
        remote.useOn(useOn(player, helper, POS, Direction.UP));
        helper.assertBlockPresent(Ic2Blocks.DYNAMITE_STICK_WITH_REMOTE.get(), POS);
        helper.assertValueEqual(DynamiteRemoteItem.getLinks(remote).size(), 1, "links");

        ItemStack other = new ItemStack(Ic2Items.DYNAMITE_O_MOTE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, other);
        other.useOn(useOn(player, helper, POS, Direction.UP));
        helper.assertBlockPresent(Ic2Blocks.DYNAMITE_STICK_WITH_REMOTE.get(), POS);

        player.setItemInHand(InteractionHand.MAIN_HAND, remote);
        remote.useOn(useOn(player, helper, POS, Direction.UP));
        helper.assertBlockPresent(Ic2Blocks.DYNAMITE_STICK.get(), POS);
        helper.assertValueEqual(DynamiteRemoteItem.getLinks(remote).size(), 0, "links after unlinking");

        remote.useOn(useOn(player, helper, POS, Direction.UP));
        remote.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertBlockPresent(Blocks.AIR, POS);
        helper.assertValueEqual(DynamiteRemoteItem.getLinks(remote).size(), 0, "links after firing");
        helper.assertValueEqual(takeSinglePrimed(helper).getFuse(), DynamiteBlock.EXPLODED_FUSE, "fuse");
        helper.succeed();
    }

    /** Water adds 2000 ticks to the fuse every tick. */
    @GameTest(template = EMPTY)
    public static void waterKeepsDynamiteFromExploding(GameTestHelper helper) {
        helper.setBlock(POS.below(), Blocks.STONE);
        helper.setBlock(POS, Blocks.WATER);
        BlockPos abs = helper.absolutePos(POS);
        DynamiteEntity dynamite = new DynamiteEntity(helper.getLevel(), abs.getX() + 0.5, abs.getY() + 0.2,
                abs.getZ() + 0.5, false);
        helper.getLevel().addFreshEntity(dynamite);
        helper.runAfterDelay(3, () -> {
            helper.assertTrue(dynamite.getFuse() > DynamiteEntity.FUSE + 2000, "fuse " + dynamite.getFuse());
            dynamite.discard();
            helper.succeed();
        });
    }

    /** Sticky dynamite stops on the block it hits and burns 4 ticks per tick there. */
    @GameTest(template = EMPTY)
    public static void stickyDynamiteSticks(GameTestHelper helper) {
        helper.setBlock(POS.below(), Blocks.STONE);
        BlockPos abs = helper.absolutePos(POS);
        DynamiteEntity dynamite = new DynamiteEntity(helper.getLevel(), abs.getX() + 0.5, abs.getY() + 0.2,
                abs.getZ() + 0.5, true);
        dynamite.setDeltaMovement(0.0, -0.5, 0.0);
        helper.getLevel().addFreshEntity(dynamite);
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(dynamite.isInGround(), "in ground");
            helper.assertTrue(dynamite.getFuse() < DynamiteEntity.FUSE - 20, "fuse " + dynamite.getFuse());
            helper.assertValueEqual(dynamite.getDeltaMovement(), Vec3.ZERO, "motion");
            dynamite.discard();
            helper.succeed();
        });
    }
}
