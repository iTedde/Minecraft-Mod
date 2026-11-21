package dev.craft.gravisuite;

import java.util.List;

import dev.craft.Craft;
import dev.craft.buildcraft.BcBlocks;
import dev.craft.buildcraft.factory.RefineryBlock;
import dev.craft.gravisuite.item.GraviToolItem;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.block.RubberWoodBlock;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import dev.craft.ic2.machine.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Phase 27.5: GraviTool (spec 28 §9.4). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class GraviSuiteGraviToolGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private GraviSuiteGraviToolGameTests() {
    }

    private static ItemStack tool(int charge, int mode) {
        ItemStack stack = GsItems.GRAVITOOL.toStack();
        ElectricItems.charge(stack, charge, Integer.MAX_VALUE, true, false);
        if (mode != 0) {
            GraviToolItem.setMode(stack, mode);
        }
        return stack;
    }

    private static Player player(GameTestHelper helper, ItemStack stack) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return player;
    }

    private static UseOnContext click(GameTestHelper helper, Player player, BlockPos relative, Direction side) {
        BlockPos pos = helper.absolutePos(relative);
        return new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), side, pos, false));
    }

    /** Mode key + right click cycles 1 → 4 → 1 for free; the name follows only once a mode is stored. */
    @GameTest(template = EMPTY)
    public static void modeSwitch(GameTestHelper helper) {
        ItemStack stack = tool(1000, 0);
        Player player = player(helper, stack);
        helper.assertValueEqual(GraviToolItem.getMode(stack), GraviToolItem.HOE, "default hoe");
        helper.assertValueEqual(stack.getHoverName().getString(), "GraviTool", "plain name");
        GraviToolItem item = GsItems.GRAVITOOL.get();
        item.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(GraviToolItem.getMode(stack), GraviToolItem.HOE, "no mode key");
        Ic2Keyboard.processKeyUpdate(player, Ic2Keyboard.MODE_SWITCH);
        for (int expected : new int[]{2, 3, 4, 1}) {
            item.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            helper.assertValueEqual(GraviToolItem.getMode(stack), expected, "mode");
        }
        helper.assertTrue(stack.getHoverName().getString().startsWith("GraviTool ("), "mode name");
        stack.set(GsDataComponents.TOOL_MODE.get(), 7);
        helper.assertValueEqual(GraviToolItem.getMode(stack), GraviToolItem.HOE, "invalid → hoe");
        helper.assertValueEqual(ElectricItems.getCharge(stack), 1000, "switching is free");
        helper.succeed();
    }

    /** Dirt is tilled even from below and under a block (original quirk), grass not from below; 50 EU each. */
    @GameTest(template = EMPTY)
    public static void hoe(GameTestHelper helper) {
        ItemStack stack = tool(120, GraviToolItem.HOE);
        Player player = player(helper, stack);
        BlockPos dirt = new BlockPos(1, 1, 1);
        BlockPos grass = new BlockPos(3, 1, 1);
        helper.setBlock(dirt, Blocks.DIRT);
        helper.setBlock(dirt.above(), Blocks.STONE);
        helper.setBlock(grass, Blocks.GRASS_BLOCK);
        stack.useOn(click(helper, player, grass, Direction.DOWN));
        helper.assertBlockPresent(Blocks.GRASS_BLOCK, grass);
        stack.useOn(click(helper, player, dirt, Direction.DOWN));
        helper.assertBlockPresent(Blocks.FARMLAND, dirt);
        stack.useOn(click(helper, player, grass, Direction.UP));
        helper.assertBlockPresent(Blocks.FARMLAND, grass);
        helper.assertValueEqual(ElectricItems.getCharge(stack), 20, "2 × 50");
        helper.setBlock(dirt, Blocks.DIRT);
        stack.useOn(click(helper, player, dirt, Direction.UP));
        helper.assertBlockPresent(Blocks.DIRT, dirt);
        helper.succeed();
    }

    /** A wet hole gives 1–3 resin for 50 EU and dries; without 50 EU nothing happens. */
    @GameTest(template = EMPTY)
    public static void treetap(GameTestHelper helper) {
        ItemStack stack = tool(60, GraviToolItem.TREETAP);
        Player player = player(helper, stack);
        BlockPos pos = new BlockPos(2, 1, 2);
        BlockState wet = Ic2Blocks.RUBBER_WOOD.get().defaultBlockState()
                .setValue(RubberWoodBlock.RESIN, RubberWoodBlock.Resin.WET)
                .setValue(RubberWoodBlock.FACING, Direction.NORTH);
        helper.setBlock(pos, wet);
        stack.useOn(click(helper, player, pos, Direction.SOUTH));
        helper.assertBlockProperty(pos, RubberWoodBlock.RESIN, RubberWoodBlock.Resin.WET);
        stack.useOn(click(helper, player, pos, Direction.NORTH));
        helper.assertBlockProperty(pos, RubberWoodBlock.RESIN, RubberWoodBlock.Resin.DRY);
        helper.assertValueEqual(ElectricItems.getCharge(stack), 10, "50 per extraction");
        List<ItemEntity> resin = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(helper.absolutePos(pos)).inflate(1.0));
        int count = resin.stream().mapToInt(entity -> entity.getItem().getCount()).sum();
        helper.assertTrue(count >= 1 && count <= 3 && resin.stream().allMatch(e -> e.getItem().is(Ic2Items.RESIN.get())),
                "1–3 resin, got " + count);
        resin.forEach(ItemEntity::discard);
        helper.setBlock(pos, wet);
        stack.useOn(click(helper, player, pos, Direction.NORTH));
        helper.assertBlockProperty(pos, RubberWoodBlock.RESIN, RubberWoodBlock.Resin.WET);
        helper.succeed();
    }

    /**
     * Turning 50 EU, removal 500 EU (MFE, drop rate 1); drop rate 0.85 (macerator) is always lossless for 10 000 EU,
     * without 10 000 EU the machine stays.
     */
    @GameTest(template = EMPTY)
    public static void wrench(GameTestHelper helper) {
        ItemStack stack = tool(10550, GraviToolItem.WRENCH);
        Player player = player(helper, stack);
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, Ic2Blocks.machine(Ic2Machine.MFE).get());
        MachineBlockEntity mfe = helper.getBlockEntity(pos);
        mfe.setFacing(Direction.NORTH);
        stack.onItemUseFirst(click(helper, player, pos, Direction.EAST));
        helper.assertValueEqual(mfe.getFacing(), Direction.EAST, "turned to the clicked side");
        helper.assertValueEqual(ElectricItems.getCharge(stack), 10500, "turning costs 50");
        stack.onItemUseFirst(click(helper, player, pos, Direction.EAST));
        helper.assertBlockPresent(Blocks.AIR, pos);
        helper.assertValueEqual(ElectricItems.getCharge(stack), 10000, "removal costs 500");
        discardDrops(helper, pos);

        helper.setBlock(pos, Ic2Blocks.machine(Ic2Machine.MACERATOR).get());
        stack.onItemUseFirst(click(helper, player, pos, Direction.UP));
        helper.assertBlockPresent(Blocks.AIR, pos);
        helper.assertValueEqual(ElectricItems.getCharge(stack), 0, "lossless costs 10 000");
        helper.assertTrue(discardDrops(helper, pos).stream().anyMatch(e -> e.getItem().is(
                Ic2Blocks.machine(Ic2Machine.MACERATOR).get().asItem())), "macerator dropped");

        ElectricItems.charge(stack, 9999, Integer.MAX_VALUE, true, false);
        helper.setBlock(pos, Ic2Blocks.machine(Ic2Machine.MACERATOR).get());
        stack.onItemUseFirst(click(helper, player, pos, Direction.UP));
        helper.assertBlockPresent(Ic2Blocks.machine(Ic2Machine.MACERATOR).get(), pos);
        helper.assertValueEqual(ElectricItems.getCharge(stack), 9999, "nothing used");
        helper.succeed();
    }

    private static List<ItemEntity> discardDrops(GameTestHelper helper, BlockPos pos) {
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(helper.absolutePos(pos)));
        drops.forEach(ItemEntity::discard);
        return drops;
    }

    /** BuildCraft wrench only in wrench mode with 500 EU; the refinery turns for 500 EU. */
    @GameTest(template = EMPTY)
    public static void buildCraftWrench(GameTestHelper helper) {
        ItemStack stack = tool(999, GraviToolItem.HOE);
        Player player = player(helper, stack);
        GraviToolItem item = GsItems.GRAVITOOL.get();
        BlockPos pos = new BlockPos(2, 1, 2);
        BlockPos abs = helper.absolutePos(pos);
        helper.assertFalse(item.canWrench(stack, player, abs), "not in hoe mode");
        GraviToolItem.setMode(stack, GraviToolItem.WRENCH);
        helper.assertTrue(item.canWrench(stack, player, abs), "wrench mode");
        helper.setBlock(pos, BcBlocks.REFINERY.get());
        BlockState state = helper.getBlockState(pos);
        Direction before = state.getValue(RefineryBlock.FACING);
        state.useItemOn(stack, helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
        helper.assertTrue(helper.getBlockState(pos).getValue(RefineryBlock.FACING) != before, "refinery turned");
        helper.assertValueEqual(ElectricItems.getCharge(stack), 499, "500 per use");
        helper.assertFalse(item.canWrench(stack, player, abs), "below 500 EU");
        helper.succeed();
    }

    /** Screwdriver, 500 EU each: repeater clockwise, dispenser N → W → S → E, piston +1, extended piston untouched. */
    @GameTest(template = EMPTY)
    public static void screwdriver(GameTestHelper helper) {
        ItemStack stack = tool(5000, GraviToolItem.SCREWDRIVER);
        Player player = player(helper, stack);
        BlockPos repeater = new BlockPos(1, 1, 1);
        BlockPos dispenser = new BlockPos(3, 1, 1);
        BlockPos piston = new BlockPos(5, 1, 1);
        helper.setBlock(repeater, Blocks.REPEATER.defaultBlockState().setValue(RepeaterBlock.FACING, Direction.SOUTH)
                .setValue(RepeaterBlock.DELAY, 3));
        stack.onItemUseFirst(click(helper, player, repeater, Direction.UP));
        helper.assertBlockProperty(repeater, RepeaterBlock.FACING, Direction.WEST);
        helper.assertBlockProperty(repeater, RepeaterBlock.DELAY, 3);
        helper.setBlock(dispenser, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.NORTH));
        for (Direction expected : new Direction[]{Direction.WEST, Direction.SOUTH, Direction.EAST, Direction.NORTH}) {
            stack.onItemUseFirst(click(helper, player, dispenser, Direction.UP));
            helper.assertBlockProperty(dispenser, DispenserBlock.FACING, expected);
        }
        helper.setBlock(piston, Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.DOWN));
        stack.onItemUseFirst(click(helper, player, piston, Direction.UP));
        helper.assertBlockProperty(piston, PistonBaseBlock.FACING, Direction.UP);
        helper.assertValueEqual(ElectricItems.getCharge(stack), 5000 - 6 * 500, "500 per turn");
        helper.setBlock(piston, Blocks.STICKY_PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.UP)
                .setValue(PistonBaseBlock.EXTENDED, true));
        helper.assertValueEqual(stack.onItemUseFirst(click(helper, player, piston, Direction.UP)),
                InteractionResult.PASS, "extended piston");
        helper.assertBlockProperty(piston, PistonBaseBlock.FACING, Direction.UP);
        ElectricItems.discharge(stack, Integer.MAX_VALUE, Integer.MAX_VALUE, true, false);
        ElectricItems.charge(stack, 499, Integer.MAX_VALUE, true, false);
        stack.onItemUseFirst(click(helper, player, repeater, Direction.UP));
        helper.assertBlockProperty(repeater, RepeaterBlock.FACING, Direction.WEST);
        helper.succeed();
    }

    /** {@code ABA/CDC/EFG}; carbon plate, electric hoe/wrench/treetap, alloy, energy crystal, advanced circuit. */
    @GameTest(template = EMPTY)
    public static void recipe(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack carbon = Ic2Items.CARBON_PLATE.toStack();
        ItemStack alloy = Ic2Items.ADVANCED_ALLOY.toStack();
        CraftingInput input = CraftingInput.of(3, 3, List.of(carbon, Ic2Items.ELECTRIC_HOE.toStack(), carbon, alloy,
                Ic2Items.ENERGY_CRYSTAL.toStack(), alloy, Ic2Items.ELECTRIC_WRENCH.toStack(),
                Ic2Items.ADVANCED_CIRCUIT.toStack(), Ic2Items.ELECTRIC_TREETAP.toStack()));
        ItemStack result = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
                .map(recipe -> recipe.value().assemble(input, level.registryAccess())).orElse(ItemStack.EMPTY);
        helper.assertTrue(result.is(GsItems.GRAVITOOL.get()), "gravitool");
        helper.succeed();
    }
}
