package dev.craft.chargepads;

import dev.craft.Craft;
import dev.craft.chargepads.block.ChargePadBlock;
import dev.craft.chargepads.block.ChargePadBlockEntity;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Charge pads, phase 47.1 (spec 47 §2–§6). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class ChargePadGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos PAD = new BlockPos(2, 1, 2);

    private ChargePadGameTests() {
    }

    private static ChargePadBlockEntity pad(GameTestHelper helper, PadTier tier) {
        helper.setBlock(PAD, CpBlocks.pad(tier).get());
        return helper.getBlockEntity(PAD);
    }

    /** Discharge damage table of §6: static empty 2, full 10; crystalizor full 12; lapotronic full 20. */
    @GameTest(template = EMPTY)
    public static void dischargeDamageFormula(GameTestHelper helper) {
        helper.assertValueEqual(PadTier.STATIC.getDischargeDamage(0), 2, "static empty");
        helper.assertValueEqual(PadTier.STATIC.getDischargeDamage(40_000), 10, "static full");
        helper.assertValueEqual(PadTier.CRYSTALIZOR.getDischargeDamage(600_000), 12, "crystalizor full");
        helper.assertValueEqual(PadTier.LAPOTRONIC.getDischargeDamage(10_000_000), 20, "lapotronic full");
        helper.assertValueEqual(PadTier.STATIC.getZapRadius(2), 3, "minimum radius");
        helper.succeed();
    }

    /**
     * Energy only from below, IC2 overflow quirk ({@code packet − max(1, free)}), overvoltage explodes the pad;
     * storage upgrades add tier × 10 000, transformers raise the input ×4 each.
     */
    @GameTest(template = EMPTY)
    public static void energyFromBelowOnly(GameTestHelper helper) {
        ChargePadBlockEntity pad = pad(helper, PadTier.STATIC);
        helper.assertTrue(pad.acceptsEnergyFrom(null, Direction.DOWN), "accepts from below");
        helper.assertFalse(pad.acceptsEnergyFrom(null, Direction.NORTH), "not from the side");
        helper.assertValueEqual(pad.injectEnergy(Direction.NORTH, 32), 32, "side packet bounces");
        helper.assertValueEqual(pad.injectEnergy(Direction.DOWN, 32), 0, "bottom packet stored");
        pad.setStorage(39_990);
        helper.assertValueEqual(pad.injectEnergy(Direction.DOWN, 32), 22, "quirk: 32 − 10");
        helper.assertValueEqual(pad.getStorage(), 40_000, "full");
        pad.getInventory().setStackInSlot(1, new ItemStack(Ic2Items.ENERGY_STORAGE_UPGRADE.get(), 2));
        pad.injectEnergy(Direction.DOWN, 0);
        helper.assertValueEqual(pad.getCapacity(), 60_000, "two storage upgrades");
        pad.getInventory().setStackInSlot(1, new ItemStack(Ic2Items.TRANSFORMER_UPGRADE.get(), 2));
        pad.injectEnergy(Direction.DOWN, 0);
        helper.assertValueEqual(pad.getMaxInput(), 512, "32 × 4²");
        helper.assertValueEqual(pad.getCapacity(), 40_000, "storage upgrades gone");
        pad.injectEnergy(Direction.DOWN, 513);
        helper.assertBlockNotPresent(CpBlocks.STATIC_CHARGE_PAD.get(), PAD);
        helper.succeed();
    }

    /** Battery slot: redstone 500 EU, single-use battery 1 000 EU, only when the full amount fits. */
    @GameTest(template = EMPTY)
    public static void batterySlotFillsStorage(GameTestHelper helper) {
        ChargePadBlockEntity pad = pad(helper, PadTier.STATIC);
        helper.assertFalse(pad.getInventory().isItemValid(0, Ic2Items.ENERGY_CRYSTAL.toStack()),
                "tier 3 item too high for the static pad");
        pad.getInventory().setStackInSlot(0, new ItemStack(Items.REDSTONE, 2));
        helper.startSequence()
                .thenExecuteAfter(3, () -> {
                    helper.assertValueEqual(pad.getStorage(), 1000, "two redstone");
                    helper.assertTrue(pad.getInventory().getStackInSlot(0).isEmpty(), "used up");
                    pad.setStorage(39_200);
                    pad.getInventory().setStackInSlot(0, new ItemStack(Ic2Items.SU_BATTERY.get()));
                })
                .thenExecuteAfter(2, () -> {
                    helper.assertValueEqual(pad.getStorage(), 39_200, "1 000 EU do not fit");
                    helper.assertValueEqual(pad.getInventory().getStackInSlot(0).getCount(), 1, "battery kept");
                })
                .thenSucceed();
    }

    /**
     * A player on the pad switches it on (redstone 15 down and to the sides, not up); the held RE battery is charged
     * at 32 EU/t; the pad drains its tier as idle consumption. Without the player it switches off.
     */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void playerIsCharged(GameTestHelper helper) {
        ChargePadBlockEntity pad = pad(helper, PadTier.STATIC);
        pad.setStorage(10_000);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getInventory().selected = 0;
        player.getInventory().setItem(0, new ItemStack(Ic2Items.RE_BATTERY.get()));
        BlockPos absolute = helper.absolutePos(PAD);
        player.moveTo(absolute.getX() + 0.5, absolute.getY() + 0.25, absolute.getZ() + 0.5);
        BlockState state = helper.getBlockState(PAD);
        state.entityInside(helper.getLevel(), absolute, player);
        helper.startSequence()
                .thenExecute(() -> {
                    helper.assertBlockProperty(PAD, ChargePadBlock.ACTIVE, true);
                    BlockState active = helper.getBlockState(PAD);
                    helper.assertValueEqual(active.getSignal(helper.getLevel(), absolute, Direction.NORTH), 15,
                            "signal to the side");
                    helper.assertValueEqual(active.getSignal(helper.getLevel(), absolute, Direction.DOWN), 0,
                            "no signal upwards");
                })
                .thenExecuteAfter(10, () -> {
                    int charge = ElectricItems.getCharge(player.getInventory().getItem(0));
                    helper.assertTrue(charge >= 9 * 32 && charge % 32 == 0, "32 EU per tick, got " + charge);
                    helper.assertValueEqual(pad.getStorage(), 10_000 - charge - charge / 32,
                            "charge plus 1 EU idle per charging tick");
                    player.discard();
                })
                .thenExecuteAfter(25, () -> helper.assertBlockProperty(PAD, ChargePadBlock.ACTIVE, false))
                .thenSucceed();
    }

    /**
     * Breaking without a wrench: everyone near is zapped (D − ⌊distance to the corner⌋, electrocution ignores armor),
     * the breaker additionally gets 2 on a crystalizor pad; the pad itself never drops.
     */
    @GameTest(template = EMPTY)
    public static void breakingZaps(GameTestHelper helper) {
        ChargePadBlockEntity pad = pad(helper, PadTier.CRYSTALIZOR);
        pad.setStorage(600_000);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, PAD);
        Player breaker = helper.makeMockPlayer(GameType.SURVIVAL);
        pad.onBrokenByPlayer(breaker);
        helper.assertValueEqual(zombie.getMaxHealth() - zombie.getHealth(), 12.0F, "D = 12 at distance 0.7");
        helper.assertValueEqual(breaker.getMaxHealth() - breaker.getHealth(), 2.0F, "breaker extra 2");
        helper.setBlock(PAD, Blocks.AIR);
        helper.assertItemEntityNotPresent(CpItems.CRYSTALIZOR_CHARGE_PAD.get());
        helper.succeed();
    }

    /** Falling more than 3.5 blocks onto a static pad adds 3 electrocution damage. */
    @GameTest(template = EMPTY)
    public static void fallingZaps(GameTestHelper helper) {
        pad(helper, PadTier.STATIC);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, PAD.above());
        BlockPos absolute = helper.absolutePos(PAD);
        CpBlocks.STATIC_CHARGE_PAD.get().fallOn(helper.getLevel(), helper.getBlockState(PAD), absolute, zombie, 3.0F);
        helper.assertValueEqual(zombie.getHealth(), zombie.getMaxHealth(), "3 blocks: no zap, no fall damage");
        CpBlocks.STATIC_CHARGE_PAD.get().fallOn(helper.getLevel(), helper.getBlockState(PAD), absolute, zombie, 3.6F);
        helper.assertValueEqual(zombie.getMaxHealth() - zombie.getHealth(), 3.0F,
                "zap 3 (fall damage 1 absorbed by i-frames)");
        helper.succeed();
    }
}
