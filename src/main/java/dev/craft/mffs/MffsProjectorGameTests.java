package dev.craft.mffs;

import dev.craft.Craft;
import dev.craft.mffs.block.CapacitorBlockEntity;
import dev.craft.mffs.block.MffsMachineBlock;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import dev.craft.mffs.block.ProjectorBlock;
import dev.craft.mffs.block.ProjectorBlockEntity;
import dev.craft.mffs.field.ForceFieldMap;
import dev.craft.mffs.field.ProjectorModule;
import dev.craft.mffs.item.CardItem;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** MFFS projector and force field, phase 48.2 (spec 48 §9.1, §9.4). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class MffsProjectorGameTests {
    private static final String LARGE = "empty_16x8x16";
    private static final BlockPos CAPACITOR = new BlockPos(2, 1, 2);
    private static final BlockPos PROJECTOR = new BlockPos(4, 1, 4);
    /** Wall facing south, distance 1, focus up 2: the column x 4, z 6, y 1–3. */
    private static final List<BlockPos> WALL = List.of(new BlockPos(4, 1, 6), new BlockPos(4, 2, 6),
            new BlockPos(4, 3, 6));
    private static final int START = 1_000_000;

    private MffsProjectorGameTests() {
    }

    private static CapacitorBlockEntity capacitor(GameTestHelper helper, int power) {
        helper.setBlock(CAPACITOR, MffsBlocks.CAPACITOR.get());
        CapacitorBlockEntity capacitor = helper.getBlockEntity(CAPACITOR);
        helper.runAfterDelay(1, () -> capacitor.setForcePower(power));
        return capacitor;
    }

    private static ProjectorBlockEntity projector(GameTestHelper helper, BlockPos pos, Direction facing) {
        helper.setBlock(pos, MffsBlocks.PROJECTOR.get().defaultBlockState().setValue(MffsMachineBlock.FACING, facing));
        return helper.getBlockEntity(pos);
    }

    /** Power link to the capacitor, wall module, distance 1, focus up 2. */
    private static void equipWall(ProjectorBlockEntity projector, CapacitorBlockEntity capacitor) {
        ItemStack card = new ItemStack(MffsItems.CARD_POWER_LINK.get());
        CardItem.link(card, capacitor, capacitor.getPowerStorageId(), capacitor.getDeviceName());
        projector.setStack(ProjectorBlockEntity.SLOT_POWER_LINK, card);
        projector.setStack(ProjectorBlockEntity.SLOT_MODULE,
                new ItemStack(MffsItems.module(ProjectorModule.WALL).get()));
        projector.setStack(ProjectorBlockEntity.SLOT_DISTANCE,
                new ItemStack(MffsItems.FIELD_MODULATOR_DISTANCE.get()));
        projector.setStack(ProjectorBlockEntity.SLOT_FOCUS_UP,
                new ItemStack(MffsItems.PROJECTOR_FOCUS_MATRIX.get(), 2));
    }

    private static void switchOn(MffsMachineBlockEntity machine) {
        while (machine.getSwitchMode() != 2) {
            machine.toggleSwitchMode();
        }
        if (!machine.getSwitchValue()) {
            machine.toggleSwitchValue();
        }
    }

    private static boolean isField(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockState(pos).is(MffsBlocks.FORCE_FIELD.get());
    }

    /**
     * Switched on after 40 ticks: three field blocks, creation 3 × 10 × 2 FE (default type pays twice), upkeep
     * 3 × 2 FE per 20 ticks; switched off the field disappears.
     */
    @GameTest(template = LARGE, timeoutTicks = 300)
    public static void projectorBuildsAndRemovesField(GameTestHelper helper) {
        CapacitorBlockEntity capacitor = capacitor(helper, START);
        ProjectorBlockEntity projector = projector(helper, PROJECTOR, Direction.SOUTH);
        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    equipWall(projector, capacitor);
                    switchOn(projector);
                })
                .thenWaitUntil(() -> {
                    helper.assertTrue(projector.isActive(), "active");
                    WALL.forEach(pos -> helper.assertTrue(isField(helper, pos), "field at " + pos));
                })
                .thenExecute(() -> {
                    int used = START - capacitor.getStorageAvailablePower();
                    helper.assertTrue(used >= 60 && (used - 60) % 6 == 0, "creation + upkeep, used " + used);
                    helper.assertValueEqual(projector.getFieldPoints().size(), 3, "three points");
                    helper.assertValueEqual(helper.getBlockState(PROJECTOR).getValue(ProjectorBlock.MODULE), 1,
                            "wall texture");
                    helper.assertBlockNotPresent(MffsBlocks.FORCE_FIELD.get(), new BlockPos(4, 4, 6));
                    projector.toggleSwitchValue();
                })
                .thenWaitUntil(() -> {
                    helper.assertFalse(projector.isActive(), "off");
                    WALL.forEach(pos -> helper.assertBlockPresent(Blocks.AIR, pos));
                })
                .thenSucceed();
    }

    /** Too little force energy for the estimate need(5) = 3 blocks × 15 = 45: the projector stays off. */
    @GameTest(template = LARGE, timeoutTicks = 200)
    public static void projectorNeedsEnoughPower(GameTestHelper helper) {
        CapacitorBlockEntity capacitor = capacitor(helper, 45);
        ProjectorBlockEntity projector = projector(helper, PROJECTOR, Direction.SOUTH);
        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    equipWall(projector, capacitor);
                    switchOn(projector);
                })
                .thenExecuteAfter(60, () -> {
                    helper.assertFalse(projector.isActive(), "45 FE is not more than 45");
                    capacitor.setForcePower(46);
                })
                .thenWaitUntil(() -> helper.assertTrue(projector.isActive(), "on with 46 FE"))
                .thenSucceed();
    }

    /**
     * A blocked position stays empty; when the block goes, the neighbouring field refills it at once (10 FE for the
     * default type).
     */
    @GameTest(template = LARGE, timeoutTicks = 300)
    public static void fieldRefillsFreedPosition(GameTestHelper helper) {
        CapacitorBlockEntity capacitor = capacitor(helper, START);
        ProjectorBlockEntity projector = projector(helper, PROJECTOR, Direction.SOUTH);
        BlockPos middle = WALL.get(1);
        int[] before = new int[1];
        helper.setBlock(middle, Blocks.STONE);
        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    equipWall(projector, capacitor);
                    switchOn(projector);
                })
                .thenWaitUntil(() -> helper.assertTrue(isField(helper, WALL.get(0)), "field below"))
                .thenExecute(() -> {
                    helper.assertBlockPresent(Blocks.STONE, middle);
                    before[0] = capacitor.getStorageAvailablePower();
                    helper.setBlock(middle, Blocks.AIR);
                    helper.assertTrue(isField(helper, middle), "refilled at once");
                    int used = before[0] - capacitor.getStorageAvailablePower();
                    helper.assertValueEqual(used, 10, "refill cost");
                })
                .thenSucceed();
    }

    /** Two walls share a position: the first owns it; switched off, the second takes it over at its next pass. */
    @GameTest(template = LARGE, timeoutTicks = 400)
    public static void overlappingProjectorsHandOver(GameTestHelper helper) {
        CapacitorBlockEntity capacitor = capacitor(helper, START);
        ProjectorBlockEntity first = projector(helper, PROJECTOR, Direction.SOUTH);
        ProjectorBlockEntity second = projector(helper, new BlockPos(4, 1, 8), Direction.NORTH);
        BlockPos shared = WALL.get(0);
        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    equipWall(first, capacitor);
                    equipWall(second, capacitor);
                    // The second wall is only the shared block.
                    second.setStack(ProjectorBlockEntity.SLOT_FOCUS_UP, ItemStack.EMPTY);
                    switchOn(first);
                })
                .thenWaitUntil(() -> helper.assertTrue(isField(helper, shared), "first field"))
                .thenExecute(() -> switchOn(second))
                .thenWaitUntil(() -> helper.assertTrue(second.isActive(), "second on"))
                .thenExecute(() -> {
                    ForceFieldMap.Stack stack = ForceFieldMap.get(helper.getLevel(), helper.absolutePos(shared));
                    helper.assertValueEqual(stack.getProjectorId(), first.getDeviceId(), "first owns");
                    first.toggleSwitchValue();
                })
                .thenWaitUntil(() -> {
                    helper.assertFalse(first.isActive(), "first off");
                    helper.assertTrue(isField(helper, shared), "second placed it");
                    ForceFieldMap.Stack stack = ForceFieldMap.get(helper.getLevel(), helper.absolutePos(shared));
                    helper.assertValueEqual(stack.getProjectorId(), second.getDeviceId(), "second owns");
                    helper.assertBlockPresent(Blocks.AIR, WALL.get(1));
                    helper.assertBlockPresent(Blocks.AIR, WALL.get(2));
                })
                .thenSucceed();
    }

    /** Removing an active projector removes its field. */
    @GameTest(template = LARGE, timeoutTicks = 300)
    public static void removedProjectorTakesFieldAlong(GameTestHelper helper) {
        CapacitorBlockEntity capacitor = capacitor(helper, START);
        ProjectorBlockEntity projector = projector(helper, PROJECTOR, Direction.SOUTH);
        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    equipWall(projector, capacitor);
                    switchOn(projector);
                })
                .thenWaitUntil(() -> WALL.forEach(pos -> helper.assertTrue(isField(helper, pos), "field")))
                .thenExecute(() -> {
                    helper.setBlock(PROJECTOR, Blocks.AIR);
                    WALL.forEach(pos -> helper.assertBlockPresent(Blocks.AIR, pos));
                })
                .thenSucceed();
    }

    /** Without a module the upgrade, modulator and focus slots fall out (checkslots). */
    @GameTest(template = LARGE)
    public static void removingModuleDropsParts(GameTestHelper helper) {
        CapacitorBlockEntity capacitor = capacitor(helper, START);
        ProjectorBlockEntity projector = projector(helper, PROJECTOR, Direction.SOUTH);
        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    equipWall(projector, capacitor);
                    projector.setStack(ProjectorBlockEntity.SLOT_MODULE, ItemStack.EMPTY);
                    helper.assertTrue(projector.getStack(ProjectorBlockEntity.SLOT_FOCUS_UP).isEmpty(), "focus out");
                    helper.assertTrue(projector.getStack(ProjectorBlockEntity.SLOT_DISTANCE).isEmpty(),
                            "distance out");
                    helper.assertFalse(projector.getStack(ProjectorBlockEntity.SLOT_POWER_LINK).isEmpty(),
                            "link stays");
                    helper.assertItemEntityPresent(MffsItems.PROJECTOR_FOCUS_MATRIX.get(), PROJECTOR, 2.0);
                    helper.assertValueEqual(helper.getBlockState(PROJECTOR).getValue(ProjectorBlock.MODULE), 0,
                            "no module texture");
                })
                .thenSucceed();
    }
}
