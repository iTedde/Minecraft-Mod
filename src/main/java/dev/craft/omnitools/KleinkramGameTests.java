package dev.craft.omnitools;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.machine.Wrenchable;
import dev.craft.obsidiplates.ObsidiPlates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Phase 50.1 (spec 50 §1.1, §2): OmniWrench and Obsidian Pressure Plate. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class KleinkramGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(3, 1, 3);

    private KleinkramGameTests() {
    }

    private static InteractionResult wrench(GameTestHelper helper, Player player, Direction face) {
        ItemStack wrench = new ItemStack(OmniTools.OMNIWRENCH.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
        BlockPos absolute = helper.absolutePos(POS);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), face, absolute, false));
        return wrench.getItem().onItemUseFirst(wrench, context);
    }

    /** Clicking the front removes an IC2 machine and always drops the machine itself (macerator: 85 % with IC2). */
    @GameTest(template = EMPTY)
    public static void omniwrenchRemovesMachineLossless(GameTestHelper helper) {
        helper.setBlock(POS, Ic2Blocks.machine(Ic2Machine.MACERATOR).get());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Wrenchable machine = (Wrenchable) helper.getBlockEntity(POS);
        Direction front = machine.getFacing();
        helper.assertTrue(wrench(helper, player, front).consumesAction(), "used on the front");
        helper.assertBlockPresent(Blocks.AIR, POS);
        boolean dropped = helper.getEntities(EntityType.ITEM).stream().map(ItemEntity::getItem)
                .anyMatch(stack -> stack.is(Ic2Blocks.machine(Ic2Machine.MACERATOR).get().asItem()));
        helper.assertTrue(dropped, "macerator dropped");
        helper.assertTrue(new ItemStack(OmniTools.OMNIWRENCH.get()).getMaxDamage() == 0, "unbreakable");
        helper.succeed();
    }

    /** Vanilla blocks rotate (crescent hammer behaviour); the OmniWrench is a BuildCraft/TE wrench. */
    @GameTest(template = EMPTY)
    public static void omniwrenchRotatesVanillaBlocks(GameTestHelper helper) {
        helper.setBlock(POS, Blocks.FURNACE.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wrench = new ItemStack(OmniTools.OMNIWRENCH.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
        BlockPos absolute = helper.absolutePos(POS);
        wrench.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false)));
        helper.assertTrue(helper.getBlockState(POS).getValue(BlockStateProperties.HORIZONTAL_FACING) != Direction.NORTH,
                "furnace rotated");
        helper.assertTrue(dev.craft.buildcraft.item.ToolWrench.isWrench(wrench), "BuildCraft/TE wrench");
        helper.succeed();
    }

    /** Only players press the plate: a pig standing on it leaves it off. */
    @GameTest(template = EMPTY)
    public static void obsidianPlateIgnoresMobs(GameTestHelper helper) {
        helper.setBlock(POS.below(), Blocks.STONE);
        helper.setBlock(POS, ObsidiPlates.OBSIDIAN_PRESSURE_PLATE.get());
        helper.spawn(EntityType.PIG, POS);
        helper.runAfterDelay(20, () -> {
            helper.assertBlockProperty(POS, PressurePlateBlock.POWERED, false);
            helper.killAllEntities();
            helper.succeed();
        });
    }
}
