package dev.craft.ic2.personal;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Personal safe, Trade-O-Mat and Energy-O-Mat (phase 12.6). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class PersonalGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos BLOCK = new BlockPos(2, 1, 2);
    private static final BlockPos CHEST = BLOCK.east();

    private PersonalGameTests() {
    }

    private static int count(ChestBlockEntity chest, Item item) {
        int count = 0;
        for (int i = 0; i < chest.getContainerSize(); i++) {
            if (chest.getItem(i).is(item)) {
                count += chest.getItem(i).getCount();
            }
        }
        return count;
    }

    /** The first user owns the safe; a stranger is refused and a filled safe cannot be wrenched. */
    @GameTest(template = EMPTY, timeoutTicks = 5)
    public static void safeBelongsToFirstUser(GameTestHelper helper) {
        helper.setBlock(BLOCK, Ic2Blocks.machine(Ic2Machine.PERSONAL_SAFE).get());
        PersonalSafeBlockEntity safe = helper.getBlockEntity(BLOCK);
        ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        ServerPlayer stranger = helper.makeMockServerPlayerInLevel();
        helper.assertTrue(safe.canOpen(owner), "owner claims");
        helper.assertFalse(safe.canOpen(stranger), "stranger refused");
        safe.getInventory().set(0, new ItemStack(Items.DIAMOND));
        helper.assertFalse(safe.wrenchCanRemove(owner), "not empty");
        safe.getInventory().set(0, ItemStack.EMPTY);
        helper.assertTrue(safe.wrenchCanRemove(owner), "empty");
        helper.assertTrue(safe.getItemHandler(Direction.UP) == null, "no automation");
        helper.succeed();
    }

    /** 3 dirt pay for three trades of 1 diamond each from the chest beside it; the dirt goes into the chest. */
    @GameTest(template = EMPTY, timeoutTicks = 10)
    public static void tradeOMatTrades(GameTestHelper helper) {
        helper.setBlock(BLOCK, Ic2Blocks.machine(Ic2Machine.TRADE_O_MAT).get());
        helper.setBlock(CHEST, Blocks.CHEST);
        TradeOMatBlockEntity trader = helper.getBlockEntity(BLOCK);
        ChestBlockEntity chest = helper.getBlockEntity(CHEST);
        chest.setItem(0, new ItemStack(Items.DIAMOND, 10));
        trader.getInventory().set(TradeOMatBlockEntity.SLOT_WANT, new ItemStack(Items.DIRT));
        trader.getInventory().set(TradeOMatBlockEntity.SLOT_OFFER, new ItemStack(Items.DIAMOND));
        trader.getInventory().set(TradeOMatBlockEntity.SLOT_PAYMENT, new ItemStack(Items.DIRT, 3));
        helper.succeedWhen(() -> {
            helper.assertTrue(trader.getInventory().get(TradeOMatBlockEntity.SLOT_GOODS).is(Items.DIAMOND), "goods");
            helper.assertValueEqual(trader.getInventory().get(TradeOMatBlockEntity.SLOT_GOODS).getCount(), 3,
                    "diamonds bought");
            helper.assertValueEqual(count(chest, Items.DIAMOND), 7, "diamonds left");
            helper.assertValueEqual(count(chest, Items.DIRT), 3, "payment stored");
            helper.assertValueEqual(trader.getTotalTradeCount(), 3, "trade count");
            helper.assertValueEqual(trader.getStock(), 7, "stock");
        });
    }

    /** A safe of another owner is no stock for the Trade-O-Mat. */
    @GameTest(template = EMPTY, timeoutTicks = 10)
    public static void tradeOMatIgnoresForeignSafe(GameTestHelper helper) {
        helper.setBlock(BLOCK, Ic2Blocks.machine(Ic2Machine.TRADE_O_MAT).get());
        helper.setBlock(CHEST, Ic2Blocks.machine(Ic2Machine.PERSONAL_SAFE).get());
        TradeOMatBlockEntity trader = helper.getBlockEntity(BLOCK);
        PersonalSafeBlockEntity safe = helper.getBlockEntity(CHEST);
        trader.canAccess(helper.makeMockPlayer(GameType.SURVIVAL));
        safe.canAccess(helper.makeMockPlayer(GameType.SURVIVAL));
        safe.getInventory().set(0, new ItemStack(Items.DIAMOND, 10));
        trader.getInventory().set(TradeOMatBlockEntity.SLOT_WANT, new ItemStack(Items.DIRT));
        trader.getInventory().set(TradeOMatBlockEntity.SLOT_OFFER, new ItemStack(Items.DIAMOND));
        trader.getInventory().set(TradeOMatBlockEntity.SLOT_PAYMENT, new ItemStack(Items.DIRT, 3));
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(trader.getInventory().get(TradeOMatBlockEntity.SLOT_GOODS).isEmpty(), "no trade");
            helper.assertValueEqual(safe.getInventory().get(0).getCount(), 10, "safe untouched");
            helper.succeed();
        });
    }

    /** 5 dirt at 1000 EU per dirt unlock 5000 EU; the front accepts only what was paid for. */
    @GameTest(template = EMPTY, timeoutTicks = 10)
    public static void energyOMatSellsEnergy(GameTestHelper helper) {
        helper.setBlock(BLOCK, Ic2Blocks.machine(Ic2Machine.ENERGY_O_MAT).get());
        helper.setBlock(CHEST, Blocks.CHEST);
        EnergyOMatBlockEntity seller = helper.getBlockEntity(BLOCK);
        ChestBlockEntity chest = helper.getBlockEntity(CHEST);
        seller.getInventory().set(EnergyOMatBlockEntity.SLOT_PRICE, new ItemStack(Items.DIRT));
        seller.getInventory().set(EnergyOMatBlockEntity.SLOT_PAYMENT, new ItemStack(Items.DIRT, 5));
        helper.runAfterDelay(2, () -> {
            helper.assertValueEqual(seller.getPaidFor(), 5000, "paid for");
            helper.assertValueEqual(count(chest, Items.DIRT), 5, "payment stored");
            helper.assertValueEqual(seller.demandsEnergy(), 5000, "demand");
            helper.assertValueEqual(seller.injectEnergy(seller.getFacing(), 2048), 0, "takes any voltage");
            helper.assertValueEqual(seller.getPaidFor(), 5000 - 2048, "rest paid for");
            helper.succeed();
        });
    }
}
