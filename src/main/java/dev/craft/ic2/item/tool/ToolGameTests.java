package dev.craft.ic2.item.tool;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import dev.craft.ic2.machine.MachineBlockEntity;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the electric tools and the IC2 keys (phase 6.2). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class ToolGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private ToolGameTests() {
    }

    private static ItemStack charged(ItemStack stack, int charge) {
        ElectricItems.charge(stack, charge, Integer.MAX_VALUE, true, false);
        return stack;
    }

    private static UseOnContext click(GameTestHelper helper, Player player, BlockPos relative, Direction side) {
        BlockPos pos = helper.absolutePos(relative);
        return new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), side, pos, false));
    }

    @GameTest(template = EMPTY)
    public static void drillSpeedAndCost(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack drill = charged(Ic2Items.MINING_DRILL.toStack(), 100);
        helper.assertValueEqual(drill.getDestroySpeed(Blocks.STONE.defaultBlockState()), 8.0F, "speed on stone");
        helper.assertValueEqual(drill.getDestroySpeed(Blocks.DIRT.defaultBlockState()), 8.0F, "speed on dirt");
        helper.assertValueEqual(drill.getDestroySpeed(Blocks.OAK_LOG.defaultBlockState()), 1.0F, "speed on wood");
        helper.assertTrue(drill.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()), "obsidian harvest");
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.STONE);
        drill.mineBlock(helper.getLevel(), Blocks.STONE.defaultBlockState(), helper.absolutePos(pos), player);
        helper.assertValueEqual(ElectricItems.getCharge(drill), 50, "charge after one block");
        drill.mineBlock(helper.getLevel(), Blocks.STONE.defaultBlockState(), helper.absolutePos(pos), player);
        helper.assertValueEqual(drill.getDestroySpeed(Blocks.STONE.defaultBlockState()), 1.0F, "speed when empty");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void electricHoeUsesEnergyBeforeTilling(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack hoe = charged(Ic2Items.ELECTRIC_HOE.toStack(), 1000);
        player.setItemInHand(InteractionHand.MAIN_HAND, hoe);
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.DIRT);
        helper.setBlock(new BlockPos(2, 1, 1), Blocks.STONE);
        hoe.useOn(click(helper, player, new BlockPos(1, 1, 1), Direction.UP));
        helper.assertBlockPresent(Blocks.FARMLAND, new BlockPos(1, 1, 1));
        hoe.useOn(click(helper, player, new BlockPos(2, 1, 1), Direction.UP));
        helper.assertBlockPresent(Blocks.STONE, new BlockPos(2, 1, 1));
        helper.assertValueEqual(ElectricItems.getCharge(hoe), 900, "charge after till + stone");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void wrenchAltKeyCyclesFacing(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wrench = Ic2Items.WRENCH.toStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, Ic2Blocks.machine(Ic2Machine.BATBOX).get());
        MachineBlockEntity batbox = helper.getBlockEntity(pos);
        batbox.setFacing(Direction.NORTH);
        Ic2Keyboard.processKeyUpdate(player, Ic2Keyboard.ALT);
        // (north = 2 + 1) % 6 = south, whatever side is clicked.
        wrench.onItemUseFirst(click(helper, player, pos, Direction.UP));
        helper.assertValueEqual(batbox.getFacing(), Direction.SOUTH, "facing after ALT");
        helper.assertValueEqual(wrench.getDamageValue(), 1, "wrench damage");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void electricWrenchModeKeyTogglesLossless(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wrench = charged(Ic2Items.ELECTRIC_WRENCH.toStack(), 9999);
        player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
        var item = Ic2Items.ELECTRIC_WRENCH.get();
        helper.assertTrue(item.canTakeDamage(wrench, 10), "500 EU available");
        helper.assertFalse(item.canTakeDamage(wrench, 200), "10 000 EU not available");
        Ic2Keyboard.processKeyUpdate(player, Ic2Keyboard.MODE_SWITCH);
        item.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(wrench.getOrDefault(dev.craft.ic2.Ic2DataComponents.LOSSLESS.get(), false), "lossless");
        Ic2Keyboard.processKeyUpdate(player, 0);
        helper.assertFalse(Ic2Keyboard.isModeSwitchKeyDown(player), "key released");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void miningLaserModeSwitchAndCost(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack laser = charged(Ic2Items.MINING_LASER.toStack(), 200000);
        player.setItemInHand(InteractionHand.MAIN_HAND, laser);
        Ic2Keyboard.processKeyUpdate(player, Ic2Keyboard.MODE_SWITCH);
        Ic2Items.MINING_LASER.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(MiningLaserItem.getMode(laser), MiningLaserItem.Mode.LOW_FOCUS, "mode");
        helper.assertValueEqual(ElectricItems.getCharge(laser), 200000, "switching is free");
        Ic2Keyboard.processKeyUpdate(player, 0);
        Ic2Items.MINING_LASER.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(ElectricItems.getCharge(laser), 200000 - 100, "low-focus shot");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void miningLaserBreaksStoneUntilPowerIsUsed(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 start = helper.absoluteVec(new Vec3(3.5, 1.0, 0.5));
        player.moveTo(start.x, start.y, start.z, 0.0F, 0.0F);
        for (int z = 2; z <= 7; z++) {
            helper.setBlock(new BlockPos(3, 2, z), Blocks.STONE);
        }
        // Power 2: -0.5 for the tick in the air, -0.63 per stone (resistance 6): two blocks are mined, the third stops it.
        helper.getLevel().addFreshEntity(new MiningLaserEntity(helper.getLevel(), player, Float.POSITIVE_INFINITY,
                2.0F, Integer.MAX_VALUE, false, false, 0.0, 0.0, MiningLaserEntity.startY(player)));
        helper.runAfterDelay(20, () -> {
            helper.assertBlockPresent(Blocks.AIR, new BlockPos(3, 2, 2));
            helper.assertBlockPresent(Blocks.AIR, new BlockPos(3, 2, 3));
            helper.assertBlockPresent(Blocks.STONE, new BlockPos(3, 2, 4));
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void nanoSaberSwitchesOffWithoutEnergy(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack saber = charged(Ic2Items.NANO_SABER.toStack(), 100);
        player.setItemInHand(InteractionHand.MAIN_HAND, saber);
        Ic2Items.NANO_SABER.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(NanoSaberItem.isActive(saber), "switched on");
        NanoSaberItem.drain(saber, 10, player);
        helper.assertValueEqual(ElectricItems.getCharge(saber), 20, "80 EU drained");
        NanoSaberItem.drain(saber, 5, player);
        helper.assertFalse(NanoSaberItem.isActive(saber), "switched off");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void oreValues(GameTestHelper helper) {
        helper.assertValueEqual(OreValues.valueOf(Blocks.DIAMOND_ORE.defaultBlockState()), 5, "diamond");
        helper.assertValueEqual(OreValues.valueOf(Blocks.DEEPSLATE_IRON_ORE.defaultBlockState()), 4, "iron");
        helper.assertValueEqual(OreValues.valueOf(Ic2Blocks.TIN_ORE.get().defaultBlockState()), 2, "tin");
        helper.assertValueEqual(OreValues.valueOf(Blocks.NETHER_QUARTZ_ORE.defaultBlockState()), 1, "other ore");
        helper.assertValueEqual(OreValues.valueOf(Blocks.STONE.defaultBlockState()), 0, "stone");
        helper.succeed();
    }
}
