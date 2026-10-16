package dev.craft.ic2.machine;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.storage.ElectricStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
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

/** In-world tests for the teleporter, the frequency transmitter and the tesla coil (phase 12.4). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class TeleporterGameTests {
    private static final String EMPTY = "empty_16x8x16";
    private static final BlockPos FROM = new BlockPos(2, 1, 2);
    private static final BlockPos TO = new BlockPos(8, 1, 2);

    private TeleporterGameTests() {
    }

    private static TeleporterBlockEntity teleporter(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, Ic2Blocks.machine(Ic2Machine.TELEPORTER).get());
        return helper.getBlockEntity(pos);
    }

    /**
     * A full stack weighs 100; 6 blocks away the jump costs (int)(100 · 16^0.7 · 5) = 3482 EU from the BatBox. The
     * items land on top of the target teleporter.
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void teleportsItemsForEnergy(GameTestHelper helper) {
        TeleporterBlockEntity from = teleporter(helper, FROM);
        teleporter(helper, TO);
        from.setTarget(helper.absolutePos(TO));
        helper.setBlock(FROM.west(), Ic2Blocks.machine(Ic2Machine.BATBOX).get());
        ElectricStorageBlockEntity batbox = helper.getBlockEntity(FROM.west());
        batbox.setEnergy(10000);
        ItemEntity item = helper.spawnItem(Items.COBBLESTONE, FROM.getX() + 0.5F, FROM.getY() + 1.2F,
                FROM.getZ() + 0.5F);
        item.setItem(new ItemStack(Items.COBBLESTONE, 64));
        helper.setBlock(FROM.north(), Blocks.REDSTONE_BLOCK);
        helper.succeedWhen(() -> {
            Vec3 target = Vec3.atBottomCenterOf(helper.absolutePos(TO)).add(0.0, 1.5, 0.0);
            helper.assertTrue(item.position().distanceTo(target) < 1.0, "item at target");
            helper.assertValueEqual(batbox.getEnergy(), 10000 - 3482, "energy cost");
        });
    }

    /** Without enough stored energy nothing moves. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void notEnoughEnergyKeepsEntity(GameTestHelper helper) {
        TeleporterBlockEntity from = teleporter(helper, FROM);
        teleporter(helper, TO);
        from.setTarget(helper.absolutePos(TO));
        helper.setBlock(FROM.west(), Ic2Blocks.machine(Ic2Machine.BATBOX).get());
        ElectricStorageBlockEntity batbox = helper.getBlockEntity(FROM.west());
        batbox.setEnergy(3000);
        ItemEntity item = helper.spawnItem(Items.COBBLESTONE, FROM.getX() + 0.5F, FROM.getY() + 1.2F,
                FROM.getZ() + 0.5F);
        item.setItem(new ItemStack(Items.COBBLESTONE, 64));
        helper.setBlock(FROM.north(), Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(item.position().distanceTo(Vec3.atCenterOf(helper.absolutePos(FROM))) < 2.0,
                    "item stays");
            helper.assertValueEqual(batbox.getEnergy(), 3000, "energy kept");
            helper.assertTrue(from.isActive(), "active with signal and target");
            helper.succeed();
        });
    }

    /** IC2 weights: animal 100, other creature 500, item stack share. */
    @GameTest(template = EMPTY, timeoutTicks = 5)
    public static void weights(GameTestHelper helper) {
        Pig pig = helper.spawn(EntityType.PIG, new BlockPos(4, 2, 4));
        helper.assertValueEqual(TeleporterBlockEntity.getWeightOf(pig), 100, "pig");
        helper.assertValueEqual(TeleporterBlockEntity.getWeightOf(helper.spawn(EntityType.ZOMBIE,
                new BlockPos(5, 2, 4))), 500, "zombie");
        ItemEntity item = helper.spawnItem(Items.ENDER_PEARL, 6, 2, 4);
        item.setItem(new ItemStack(Items.ENDER_PEARL, 8));
        helper.assertValueEqual(TeleporterBlockEntity.getWeightOf(item), 50, "8 of 16 pearls");
        helper.succeed();
    }

    /** First click stores the teleporter, the second links both directions. */
    @GameTest(template = EMPTY, timeoutTicks = 5)
    public static void transmitterLinksBothWays(GameTestHelper helper) {
        TeleporterBlockEntity from = teleporter(helper, FROM);
        TeleporterBlockEntity to = teleporter(helper, TO);
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack transmitter = new ItemStack(Ic2Items.FREQUENCY_TRANSMITTER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, transmitter);
        click(helper, player, transmitter, FROM);
        helper.assertValueEqual(transmitter.get(Ic2DataComponents.TELEPORTER_TARGET.get()), helper.absolutePos(FROM),
                "stored");
        click(helper, player, transmitter, TO);
        helper.assertValueEqual(to.getTarget(), helper.absolutePos(FROM), "to → from");
        helper.assertValueEqual(from.getTarget(), helper.absolutePos(TO), "from → to (was unset)");
        helper.succeed();
    }

    private static void click(GameTestHelper helper, Player player, ItemStack stack, BlockPos pos) {
        BlockPos absolute = helper.absolutePos(pos);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
        stack.getItem().onItemUseFirst(stack, new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND,
                stack, hit));
    }

    /** 800 EU: the first shock deals 2 damage to the pig and empties the coil. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void teslaCoilShocks(GameTestHelper helper) {
        BlockPos coil = new BlockPos(4, 1, 4);
        helper.setBlock(coil, Ic2Blocks.machine(Ic2Machine.TESLA_COIL).get());
        TeslaCoilBlockEntity tesla = helper.getBlockEntity(coil);
        tesla.setEnergyForTest(800);
        Pig pig = helper.spawn(EntityType.PIG, coil.east(2).above());
        helper.setBlock(coil.west(), Blocks.REDSTONE_BLOCK);
        helper.succeedWhen(() -> {
            helper.assertValueEqual(tesla.getEnergy(), 0, "energy used up");
            helper.assertValueEqual(pig.getHealth(), pig.getMaxHealth() - 2.0F, "pig health");
        });
    }

    /** Below 400 EU the coil does nothing. */
    @GameTest(template = EMPTY, timeoutTicks = 50)
    public static void teslaCoilNeedsEnergy(GameTestHelper helper) {
        BlockPos coil = new BlockPos(4, 1, 4);
        helper.setBlock(coil, Ic2Blocks.machine(Ic2Machine.TESLA_COIL).get());
        TeslaCoilBlockEntity tesla = helper.getBlockEntity(coil);
        tesla.setEnergyForTest(399);
        Pig pig = helper.spawn(EntityType.PIG, coil.east(2).above());
        helper.setBlock(coil.west(), Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(40, () -> {
            helper.assertValueEqual(tesla.getEnergy(), 399, "energy kept");
            helper.assertValueEqual(pig.getHealth(), pig.getMaxHealth(), "pig unhurt");
            helper.succeed();
        });
    }
}
