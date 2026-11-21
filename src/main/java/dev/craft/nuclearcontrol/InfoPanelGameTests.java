package dev.craft.nuclearcontrol;

import java.util.List;

import dev.craft.Craft;
import dev.craft.buildcraft.BcBlocks;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.reactor.NuclearReactorBlockEntity;
import dev.craft.ic2.storage.ElectricStorageBlockEntity;
import dev.craft.nuclearcontrol.block.InfoPanelBlock;
import dev.craft.nuclearcontrol.block.InfoPanelBlockEntity;
import dev.craft.nuclearcontrol.card.CardData;
import dev.craft.nuclearcontrol.card.CardState;
import dev.craft.nuclearcontrol.card.PanelLine;
import dev.craft.nuclearcontrol.item.EnergyArrayCardItem;
import dev.craft.nuclearcontrol.item.ReactorSensorCardItem;
import dev.craft.nuclearcontrol.item.TextCardItem;
import dev.craft.nuclearcontrol.item.TimeCardItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** IC2 Nuclear Control, phase 46.2: information panel, extenders, cards (spec 46 §5–6). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class InfoPanelGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private InfoPanelGameTests() {
    }

    private static InfoPanelBlockEntity panel(GameTestHelper helper, BlockPos pos, Direction facing) {
        helper.setBlock(pos, NcBlocks.INFORMATION_PANEL.get().defaultBlockState()
                .setValue(InfoPanelBlock.FACING, facing));
        return helper.getBlockEntity(pos);
    }

    private static void extender(GameTestHelper helper, BlockPos pos, Direction facing) {
        helper.setBlock(pos, NcBlocks.INFORMATION_PANEL_EXTENDER.get().defaultBlockState()
                .setValue(InfoPanelBlock.FACING, facing));
    }

    private static boolean lit(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockState(pos).getValue(InfoPanelBlock.LIT);
    }

    private static ItemStack useOn(GameTestHelper helper, Player player, ItemStack stack, BlockPos pos) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos absolute = helper.absolutePos(pos);
        stack.onItemUseFirst(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false)));
        return player.getMainHandItem();
    }

    /**
     * Screen: the extenders grow the panel row by row (x−, x+, then y+); with redstone the panel and its extenders give
     * light 7. Removing a corner extender drops the whole upper row, which becomes free again.
     */
    @GameTest(template = EMPTY)
    public static void screenGrowsAndShrinks(GameTestHelper helper) {
        InfoPanelBlockEntity panel = panel(helper, new BlockPos(3, 1, 3), Direction.NORTH);
        for (BlockPos pos : List.of(new BlockPos(2, 1, 3), new BlockPos(4, 1, 3), new BlockPos(2, 2, 3),
                new BlockPos(3, 2, 3), new BlockPos(4, 2, 3))) {
            extender(helper, pos, Direction.NORTH);
        }
        extender(helper, new BlockPos(1, 1, 3), Direction.SOUTH);
        helper.startSequence()
                .thenWaitUntil(() -> {
                    helper.assertValueEqual(panel.getScreenMin(), helper.absolutePos(new BlockPos(2, 1, 3)), "min");
                    helper.assertValueEqual(panel.getScreenMax(), helper.absolutePos(new BlockPos(4, 2, 3)), "max");
                })
                .thenExecute(() -> helper.setBlock(new BlockPos(3, 1, 4), Blocks.REDSTONE_BLOCK))
                .thenWaitUntil(() -> {
                    helper.assertTrue(panel.isPowered(), "powered");
                    helper.assertTrue(lit(helper, new BlockPos(3, 1, 3)), "panel lit");
                    helper.assertTrue(lit(helper, new BlockPos(4, 2, 3)), "extender lit");
                    helper.assertFalse(lit(helper, new BlockPos(1, 1, 3)), "other facing stays out");
                })
                .thenExecute(() -> helper.setBlock(new BlockPos(4, 2, 3), Blocks.AIR))
                .thenWaitUntil(() -> {
                    helper.assertValueEqual(panel.getScreenMax(), helper.absolutePos(new BlockPos(4, 1, 3)), "max");
                    helper.assertFalse(lit(helper, new BlockPos(2, 2, 3)), "freed extender dark");
                    helper.assertTrue(lit(helper, new BlockPos(4, 1, 3)), "row kept");
                })
                .thenSucceed();
    }

    /**
     * Reactor card: heat, max heat and output from the reactor; a target further than 8 blocks is out of range until
     * range upgrades (8 · 2^n) reach it; a card without target is invalid.
     */
    @GameTest(template = EMPTY)
    public static void reactorCardAndRange(GameTestHelper helper) {
        BlockPos reactorPos = new BlockPos(5, 1, 1);
        helper.setBlock(reactorPos, Ic2Blocks.machine(Ic2Machine.NUCLEAR_REACTOR).get());
        NuclearReactorBlockEntity reactor = helper.getBlockEntity(reactorPos);
        reactor.setHeat(777);
        InfoPanelBlockEntity panel = panel(helper, new BlockPos(1, 1, 1), Direction.NORTH);
        ItemStack card = new ItemStack(NcItems.REACTOR_SENSOR_LOCATION_CARD.get());
        card.set(NcDataComponents.SENSOR_TARGET.get(), helper.absolutePos(reactorPos));
        panel.getInventory().setStackInSlot(InfoPanelBlockEntity.SLOT_CARD, card);
        CardData data = CardData.of(panel.getCard());
        helper.assertValueEqual(data.getState(), CardState.OK, "state");
        helper.assertValueEqual(data.getInt("heat"), 777, "heat");
        helper.assertValueEqual(data.getInt("maxHeat"), reactor.getMaxHeat(), "max heat");
        List<PanelLine> lines = NcItems.REACTOR_SENSOR_LOCATION_CARD.get().lines(
                ReactorSensorCardItem.DISPLAY_HEAT, data, false, helper.getLevel());
        helper.assertValueEqual(lines.getFirst().left(), "777", "heat line without label");

        ItemStack far = new ItemStack(NcItems.REACTOR_SENSOR_LOCATION_CARD.get());
        far.set(NcDataComponents.SENSOR_TARGET.get(), helper.absolutePos(new BlockPos(21, 1, 1)));
        panel.getInventory().setStackInSlot(InfoPanelBlockEntity.SLOT_CARD, far);
        helper.assertValueEqual(CardData.of(panel.getCard()).getState(), CardState.OUT_OF_RANGE, "20 > 8");
        panel.getInventory().setStackInSlot(InfoPanelBlockEntity.SLOT_RANGE,
                new ItemStack(NcItems.RANGE_UPGRADE.get(), 2));
        helper.assertValueEqual(CardData.of(panel.getCard()).getState(), CardState.NO_TARGET, "20 <= 32");

        panel.getInventory().setStackInSlot(InfoPanelBlockEntity.SLOT_CARD,
                new ItemStack(NcItems.REACTOR_SENSOR_LOCATION_CARD.get()));
        helper.assertValueEqual(CardData.of(panel.getCard()).getState(), CardState.INVALID_CARD, "no target");
        helper.succeed();
    }

    /** Energy kit on a BatBox → EU card; the panel reads stored energy and capacity (40 000). */
    @GameTest(template = EMPTY)
    public static void energyKitAndCard(GameTestHelper helper) {
        BlockPos batboxPos = new BlockPos(1, 1, 1);
        helper.setBlock(batboxPos, Ic2Blocks.machine(Ic2Machine.BATBOX).get());
        ElectricStorageBlockEntity batbox = helper.getBlockEntity(batboxPos);
        for (int i = 0; i < 10; i++) {
            batbox.injectEnergy(Direction.UP, 32);
        }
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack card = useOn(helper, player, new ItemStack(NcItems.ENERGY_SENSOR_KIT.get()), batboxPos);
        helper.assertTrue(card.is(NcItems.ENERGY_SENSOR_LOCATION_CARD.get()), "card");
        helper.assertValueEqual(CardData.of(card).getInt("targetType"), 0, "EU");
        InfoPanelBlockEntity panel = panel(helper, new BlockPos(3, 1, 1), Direction.NORTH);
        panel.getInventory().setStackInSlot(InfoPanelBlockEntity.SLOT_CARD, card);
        CardData data = CardData.of(panel.getCard());
        helper.assertValueEqual(data.getLong("energyL"), 320L, "stored");
        helper.assertValueEqual(data.getLong("maxStorageL"), 40000L, "capacity");
        List<PanelLine> lines = NcItems.ENERGY_SENSOR_LOCATION_CARD.get().lines(Integer.MAX_VALUE, data, false,
                helper.getLevel());
        helper.assertValueEqual(lines.get(1).left(), "39 680", "free, grouped with a space");
        helper.succeed();
    }

    /** Liquid kit on a BuildCraft tank → card with fluid, amount and capacity in mB. */
    @GameTest(template = EMPTY)
    public static void liquidKitAndCard(GameTestHelper helper) {
        BlockPos tankPos = new BlockPos(1, 1, 1);
        helper.setBlock(tankPos, BcBlocks.TANK.get());
        IFluidHandler tank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                helper.absolutePos(tankPos), Direction.UP);
        helper.assertTrue(tank != null, "tank");
        tank.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack card = useOn(helper, player, new ItemStack(NcItems.LIQUID_SENSOR_KIT.get()), tankPos);
        helper.assertTrue(card.is(NcItems.LIQUID_SENSOR_LOCATION_CARD.get()), "card");
        InfoPanelBlockEntity panel = panel(helper, new BlockPos(3, 1, 1), Direction.NORTH);
        panel.getInventory().setStackInSlot(InfoPanelBlockEntity.SLOT_CARD, card);
        CardData data = CardData.of(panel.getCard());
        helper.assertValueEqual(data.getInt("amount"), 1000, "amount");
        helper.assertValueEqual(data.getInt("capacity"), tank.getTankCapacity(0), "capacity");
        helper.assertValueEqual(data.getString("liquid"), "minecraft:water", "fluid");
        helper.succeed();
    }

    /** {@code StorageArrayRecipe}: 2–6 cards → array; array + cards up to 6; array alone → 2 circuits per target. */
    @GameTest(template = EMPTY)
    public static void energyArrayRecipe(GameTestHelper helper) {
        ItemStack card = new ItemStack(NcItems.ENERGY_SENSOR_LOCATION_CARD.get());
        card.set(NcDataComponents.SENSOR_TARGET.get(), new BlockPos(1, 2, 3));
        ItemStack array = EnergyArrayRecipe.assemble(CraftingInput.of(2, 1, List.of(card, card.copy())));
        helper.assertTrue(array.is(NcItems.ENERGY_ARRAY_LOCATION_CARD.get()), "array");
        helper.assertValueEqual(EnergyArrayCardItem.getCardCount(CardData.of(array)), 2, "two targets");
        helper.assertValueEqual(CardData.of(array).getInt("_1y"), 2, "target stored");
        helper.assertTrue(EnergyArrayRecipe.assemble(CraftingInput.of(1, 1, List.of(card))).isEmpty(), "one card");
        ItemStack bigger = EnergyArrayRecipe.assemble(CraftingInput.of(3, 2,
                List.of(array, card, card, card, card, ItemStack.EMPTY)));
        helper.assertValueEqual(EnergyArrayCardItem.getCardCount(CardData.of(bigger)), 6, "six targets");
        helper.assertTrue(EnergyArrayRecipe.assemble(CraftingInput.of(2, 1, List.of(bigger, card))).isEmpty(),
                "more than six");
        ItemStack circuits = EnergyArrayRecipe.assemble(CraftingInput.of(1, 1, List.of(array)));
        helper.assertTrue(circuits.is(Ic2Items.ELECTRONIC_CIRCUIT.get()), "circuits");
        helper.assertValueEqual(circuits.getCount(), 4, "2 per target");
        helper.succeed();
    }

    /** Time card: 0:00 at day time 18 000 (midnight); text card drops empty lines at the end only. */
    @GameTest(template = EMPTY)
    public static void timeAndTextCards(GameTestHelper helper) {
        helper.assertValueEqual(TimeCardItem.clock(18000L), "00:00", "midnight");
        helper.assertValueEqual(TimeCardItem.clock(24000L + 6000L), "12:00", "noon");
        helper.assertValueEqual(TimeCardItem.clock(24000L + 12500L), "18:30", "evening");
        ItemStack text = new ItemStack(NcItems.TEXT_CARD.get());
        CardData data = CardData.of(text);
        data.setString("line_1", "b");
        data.setString("line_3", "d");
        List<PanelLine> lines = ((TextCardItem) NcItems.TEXT_CARD.get()).lines(0, data, true, helper.getLevel());
        helper.assertValueEqual(lines.size(), 4, "lines 0–3");
        helper.assertValueEqual(lines.get(0).left(), "", "empty first line kept");
        helper.succeed();
    }
}
