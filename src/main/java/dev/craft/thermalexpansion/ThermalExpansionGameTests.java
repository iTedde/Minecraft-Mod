package dev.craft.thermalexpansion;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Items;
import dev.craft.thermalexpansion.machine.TeMachineBlock;
import dev.craft.thermalexpansion.machine.TeMachineBlockEntity;
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
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
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
    private static <T extends TeMachineBlockEntity> T machine(GameTestHelper helper, TeMachineBlock block) {
        helper.setBlock(POS, block.defaultBlockState().setValue(TeMachineBlock.FACING, Direction.NORTH));
        @SuppressWarnings("unchecked")
        T machine = (T) helper.getBlockEntity(POS);
        machine.initFacing(Direction.NORTH);
        return machine;
    }

    /** Pulverizer: iron ore gives two iron dust at 400 MJ; the buffer is kept full (4 MJ/t). */
    @GameTest(template = EMPTY, timeoutTicks = 300)
    public static void pulverizerGrindsOre(GameTestHelper helper) {
        TeMachineBlockEntity machine = machine(helper, TeBlocks.PULVERIZER.get());
        machine.getInventory().set(0, new ItemStack(Items.IRON_ORE));
        helper.onEachTick(() -> machine.getPowerProvider().setEnergyStored(4800.0F));
        helper.succeedWhen(() -> {
            helper.assertTrue(machine.getInventory().get(1).is(Ic2Items.IRON_DUST.get())
                    && machine.getInventory().get(1).getCount() == 2, "two iron dust");
            helper.assertTrue(machine.getInventory().get(0).isEmpty(), "ore used");
        });
    }

    /** Sawmill: an oak log gives six planks and always sawdust. */
    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void sawmillCutsLogs(GameTestHelper helper) {
        TeMachineBlockEntity machine = machine(helper, TeBlocks.SAWMILL.get());
        machine.getInventory().set(0, new ItemStack(Items.OAK_LOG));
        helper.onEachTick(() -> machine.getPowerProvider().setEnergyStored(2400.0F));
        helper.succeedWhen(() -> {
            helper.assertTrue(machine.getInventory().get(1).is(Items.OAK_PLANKS)
                    && machine.getInventory().get(1).getCount() == 6, "six planks");
            helper.assertTrue(machine.getInventory().get(3).is(TeItems.SAWDUST.get()), "sawdust");
        });
    }

    /** Powered Furnace: without an own recipe it smelts like a furnace at 160 MJ. */
    @GameTest(template = EMPTY, timeoutTicks = 300)
    public static void poweredFurnaceUsesFurnaceRecipes(GameTestHelper helper) {
        TeMachineBlockEntity machine = machine(helper, TeBlocks.POWERED_FURNACE.get());
        machine.getInventory().set(0, new ItemStack(Items.SAND, 2));
        helper.onEachTick(() -> machine.getPowerProvider().setEnergyStored(2400.0F));
        helper.succeedWhen(() -> helper.assertTrue(machine.getInventory().get(1).is(Items.GLASS)
                && machine.getInventory().get(1).getCount() == 2, "two glass"));
    }

    /** Placement: front group 0, left and back input; the front cannot be changed, other sides cycle. */
    @GameTest(template = EMPTY)
    public static void machineSidesFollowFacing(GameTestHelper helper) {
        TeMachineBlockEntity machine = machine(helper, TeBlocks.POWERED_FURNACE.get());
        helper.assertTrue(machine.getSide(Direction.NORTH) == 0, "front none");
        helper.assertTrue(machine.getSide(Direction.EAST) == 1 && machine.getSide(Direction.SOUTH) == 1, "input");
        helper.assertTrue(machine.getSide(Direction.WEST) == 2, "output default");
        helper.assertTrue(machine.getItemHandler(Direction.NORTH).getSlots() == 0, "front closed");
        ItemStack rest = machine.getItemHandler(Direction.EAST).insertItem(0, new ItemStack(Items.SAND), false);
        helper.assertTrue(rest.isEmpty() && machine.getInventory().get(0).is(Items.SAND), "input side fills slot 0");
        helper.assertFalse(machine.changeSide(Direction.NORTH, true), "front fixed");
        machine.changeSide(Direction.WEST, true);
        helper.assertTrue(machine.getSide(Direction.WEST) == 3, "cycled to open");
        machine.changeSide(Direction.WEST, true);
        helper.assertTrue(machine.getSide(Direction.WEST) == 0, "wraps to none");
        helper.succeed();
    }

    /** Output sides push into an adjacent chest while idle (every 40 ticks, 4 items). */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void machineEjectsToOutputSide(GameTestHelper helper) {
        TeMachineBlockEntity machine = machine(helper, TeBlocks.POWERED_FURNACE.get());
        helper.setBlock(POS.west(), Blocks.CHEST);
        machine.getInventory().set(1, new ItemStack(Items.GLASS, 10));
        helper.succeedWhen(() -> {
            ChestBlockEntity chest = (ChestBlockEntity) helper.getBlockEntity(POS.west());
            helper.assertTrue(chest.countItem(Items.GLASS) == 4, "four items in the chest");
            helper.assertTrue(machine.getInventory().get(1).getCount() == 6, "four items per push");
        });
    }

    /** Default redstone mode needs no signal; set to ignored, the machine runs despite the signal. */
    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void machineRedstoneControl(GameTestHelper helper) {
        TeMachineBlockEntity machine = machine(helper, TeBlocks.SAWMILL.get());
        helper.setBlock(POS.above(), Blocks.REDSTONE_BLOCK);
        machine.getInventory().set(0, new ItemStack(Items.OAK_LOG));
        helper.onEachTick(() -> machine.getPowerProvider().setEnergyStored(2400.0F));
        helper.runAfterDelay(20, () -> {
            helper.assertFalse(machine.isActive(), "blocked by signal");
            machine.setRedstone(true, false);
        });
        helper.succeedWhen(() -> helper.assertTrue(machine.getInventory().get(1).is(Items.OAK_PLANKS), "ran"));
    }
}
