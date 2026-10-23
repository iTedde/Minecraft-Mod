package dev.craft.thermalexpansion;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Items;
import dev.craft.buildcraft.BcBlocks;
import dev.craft.buildcraft.transport.PipeType;
import net.minecraft.world.level.block.PipeBlock;
import dev.craft.gregtech.GtItems;
import dev.craft.buildcraft.power.BcCapabilities;
import dev.craft.thermalexpansion.block.PortableTankBlockEntity;
import dev.craft.thermalexpansion.energy.EnergyCellBlockEntity;
import dev.craft.thermalexpansion.energy.EnergyCellData;
import dev.craft.thermalexpansion.energy.MagmaticEngineBlockEntity;
import dev.craft.thermalexpansion.energy.SteamEngineBlockEntity;
import dev.craft.thermalexpansion.transport.EnergyConduitBlockEntity;
import dev.craft.thermalexpansion.transport.GridPowerProvider;
import dev.craft.thermalexpansion.transport.LiquiductBlockEntity;
import dev.craft.thermalexpansion.transport.EnergyTesseractBlockEntity;
import dev.craft.thermalexpansion.transport.ItemTesseractBlockEntity;
import dev.craft.thermalexpansion.transport.LiquidTesseractBlockEntity;
import dev.craft.thermalexpansion.transport.TesseractBlock;
import dev.craft.thermalexpansion.transport.TesseractBlockEntity;
import dev.craft.thermalexpansion.transport.TesseractPowerProvider;
import dev.craft.thermalexpansion.machine.CrucibleBlockEntity;
import dev.craft.thermalexpansion.machine.GlacialPrecipitatorBlockEntity;
import dev.craft.thermalexpansion.machine.IgneousExtruderBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import dev.craft.thermalexpansion.machine.TeMachineBlock;
import dev.craft.thermalexpansion.machine.TransposerBlockEntity;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
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
    /** Induction Smelter: silver + gold give two electrum, in either slot order. */
    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void smelterAlloysInEitherOrder(GameTestHelper helper) {
        TeMachineBlockEntity machine = machine(helper, TeBlocks.INDUCTION_SMELTER.get());
        machine.getInventory().set(0, new ItemStack(Items.GOLD_INGOT));
        machine.getInventory().set(1, new ItemStack(GtItems.SILVER_INGOT.get()));
        helper.onEachTick(() -> machine.getPowerProvider().setEnergyStored(4800.0F));
        helper.succeedWhen(() -> helper.assertTrue(machine.getInventory().get(2).is(GtItems.ELECTRUM_INGOT.get())
                && machine.getInventory().get(2).getCount() == 2, "two electrum"));
    }

    /** Induction Smelter: an alloy needs the full amounts (3 copper + 1 tin). */
    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void smelterNeedsRecipeAmounts(GameTestHelper helper) {
        TeMachineBlockEntity machine = machine(helper, TeBlocks.INDUCTION_SMELTER.get());
        machine.getInventory().set(0, new ItemStack(Items.COPPER_INGOT, 2));
        machine.getInventory().set(1, new ItemStack(Ic2Items.TIN_INGOT.get()));
        helper.onEachTick(() -> machine.getPowerProvider().setEnergyStored(4800.0F));
        helper.runAfterDelay(40, () -> {
            helper.assertFalse(machine.isActive(), "two copper are not enough");
            helper.succeed();
        });
    }

    /** Magma Crucible: ice melts into 1000 mB water and is pushed into an adjacent tank on an output side. */
    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void crucibleMeltsIntoAdjacentTank(GameTestHelper helper) {
        CrucibleBlockEntity machine = machine(helper, TeBlocks.MAGMA_CRUCIBLE.get());
        helper.setBlock(POS.west(), BcBlocks.TANK.get());
        machine.getInventory().set(0, new ItemStack(Items.ICE));
        helper.onEachTick(() -> machine.getPowerProvider().setEnergyStored(36000.0F));
        helper.succeedWhen(() -> {
            IFluidHandler tank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                    helper.absolutePos(POS.west()), Direction.EAST);
            helper.assertTrue(tank != null && tank.getFluidInTank(0).is(Fluids.WATER)
                    && tank.getFluidInTank(0).getAmount() == 1000, "1000 mB water in the tank");
        });
    }

    /** Liquid Transposer: fills a bucket from its tank, then in extraction mode empties a water bucket again. */
    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void transposerFillsAndExtracts(GameTestHelper helper) {
        TransposerBlockEntity machine = machine(helper, TeBlocks.LIQUID_TRANSPOSER.get());
        IFluidHandler input = machine.getFluidHandler(Direction.EAST);
        helper.assertTrue(input != null && input.fill(new FluidStack(Fluids.WATER, 1000),
                IFluidHandler.FluidAction.EXECUTE) == 1000, "input side takes water");
        machine.getInventory().set(0, new ItemStack(Items.BUCKET));
        helper.onEachTick(() -> machine.getPowerProvider().setEnergyStored(4800.0F));
        helper.runAfterDelay(60, () -> {
            helper.assertTrue(machine.getInventory().get(1).is(Items.WATER_BUCKET), "bucket filled");
            helper.assertTrue(machine.getTankFluid().isEmpty(), "tank empty");
            machine.getInventory().set(1, ItemStack.EMPTY);
            machine.setMode(true);
            machine.getInventory().set(0, new ItemStack(Items.WATER_BUCKET));
        });
        helper.runAfterDelay(140, () -> {
            helper.assertTrue(machine.getInventory().get(1).is(Items.BUCKET), "bucket emptied");
            helper.assertTrue(machine.getTankFluid().getAmount() == 1000, "water back in the tank");
            helper.succeed();
        });
    }

    /** Glacial Precipitator: 1000 mB water through an input side become one ice block (160 MJ). */
    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void precipitatorFreezesIce(GameTestHelper helper) {
        GlacialPrecipitatorBlockEntity machine = machine(helper, TeBlocks.GLACIAL_PRECIPITATOR.get());
        IFluidHandler input = machine.getFluidHandler(Direction.EAST);
        helper.assertTrue(input != null && input.fill(new FluidStack(Fluids.WATER, 1000),
                IFluidHandler.FluidAction.EXECUTE) == 1000, "input side takes water");
        helper.assertTrue(input.fill(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.SIMULATE) == 0,
                "no lava");
        machine.setSelection(2);
        helper.onEachTick(() -> machine.getPowerProvider().setEnergyStored(2400.0F));
        helper.succeedWhen(() -> {
            helper.assertTrue(machine.getInventory().get(0).is(Items.ICE), "ice made");
            helper.assertTrue(machine.getDisplayFluid(0).isEmpty(), "water used");
        });
    }

    /** Igneous Extruder: stone uses 2 mB lava; with 998 mB left (below 1000) it does not start again. */
    @GameTest(template = EMPTY, timeoutTicks = 300)
    public static void extruderNeedsFullTanksToStart(GameTestHelper helper) {
        IgneousExtruderBlockEntity machine = machine(helper, TeBlocks.IGNEOUS_EXTRUDER.get());
        helper.assertTrue(helper.getLevel().getCapability(BcCapabilities.POWER_RECEPTOR, helper.absolutePos(POS),
                Direction.EAST) == null, "no MJ input");
        IFluidHandler input = machine.getFluidHandler(Direction.EAST);
        input.fill(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.EXECUTE);
        input.fill(new FluidStack(Fluids.WATER, 3000), IFluidHandler.FluidAction.EXECUTE);
        machine.setSelection(1);
        helper.runAfterDelay(200, () -> {
            helper.assertTrue(machine.getInventory().get(0).is(Items.STONE)
                    && machine.getInventory().get(0).getCount() == 1, "one stone");
            helper.assertTrue(machine.getDisplayFluid(1).getAmount() == 998, "2 mB lava used");
            helper.assertTrue(machine.getDisplayFluid(0).getAmount() == 2000, "1000 mB water used");
            helper.assertFalse(machine.isActive(), "stopped below 1000 mB lava");
            helper.succeed();
        });
    }

    /** Aqueous Accumulator: two adjacent water blocks give 1000 mB per 40 ticks, pushed into a tank above. */
    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void accumulatorFillsTankAbove(GameTestHelper helper) {
        machine(helper, TeBlocks.AQUEOUS_ACCUMULATOR.get());
        helper.setBlock(POS.west(), Blocks.WATER);
        helper.setBlock(POS.east(), Blocks.WATER);
        helper.setBlock(POS.above(), BcBlocks.TANK.get());
        helper.succeedWhen(() -> {
            IFluidHandler tank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                    helper.absolutePos(POS.above()), Direction.DOWN);
            helper.assertTrue(tank != null && tank.getFluidInTank(0).getAmount() >= 1000, "1000 mB in the tank");
        });
    }

    /** Steam engine fuel values ({@code getItemEnergyValue}): only mod fuel handlers count besides wood and coal. */
    @GameTest(template = EMPTY)
    public static void steamEngineFuelValues(GameTestHelper helper) {
        helper.assertTrue(SteamEngineBlockEntity.getItemEnergyValue(new ItemStack(Items.COAL)) == 4800, "coal");
        helper.assertTrue(SteamEngineBlockEntity.getItemEnergyValue(new ItemStack(Items.CHARCOAL)) == 3200,
                "charcoal");
        helper.assertTrue(SteamEngineBlockEntity.getItemEnergyValue(new ItemStack(Items.OAK_PLANKS)) == 450,
                "planks");
        helper.assertTrue(SteamEngineBlockEntity.getItemEnergyValue(new ItemStack(Items.OAK_LOG)) == 450, "log");
        helper.assertTrue(SteamEngineBlockEntity.getItemEnergyValue(new ItemStack(Items.STICK)) == 150, "stick");
        helper.assertTrue(SteamEngineBlockEntity.getItemEnergyValue(new ItemStack(Items.OAK_SAPLING)) == 150,
                "sapling");
        helper.assertTrue(SteamEngineBlockEntity.getItemEnergyValue(new ItemStack(Items.OAK_SIGN)) == 900, "sign");
        helper.assertTrue(SteamEngineBlockEntity.getItemEnergyValue(new ItemStack(Items.OAK_DOOR)) == 0, "door");
        helper.assertTrue(SteamEngineBlockEntity.getItemEnergyValue(new ItemStack(Items.LAVA_BUCKET)) == 0,
                "lava bucket");
        helper.assertTrue(SteamEngineBlockEntity.getItemEnergyValue(new ItemStack(Ic2Items.SCRAP.get())) == 525,
                "scrap");
        helper.succeed();
    }

    /** Steam engine: coal and water power an energy cell above it. */
    @GameTest(template = EMPTY, timeoutTicks = 300)
    public static void steamEngineChargesCell(GameTestHelper helper) {
        helper.setBlock(POS, TeBlocks.STEAM_ENGINE.get());
        helper.setBlock(POS.above(), TeBlocks.ENERGY_CELL.get());
        SteamEngineBlockEntity engine = helper.getBlockEntity(POS);
        EnergyCellBlockEntity cell = helper.getBlockEntity(POS.above());
        engine.getFluidHandler(Direction.NORTH).fill(new FluidStack(Fluids.WATER, 1000),
                IFluidHandler.FluidAction.EXECUTE);
        engine.getInventory().set(0, new ItemStack(Items.COAL));
        helper.succeedWhen(() -> {
            helper.assertTrue(engine.isActive(), "engine runs");
            helper.assertTrue(engine.getInventory().get(0).isEmpty(), "coal burnt");
            helper.assertTrue(cell.getEnergy() >= 100.0F, "energy in the cell");
        });
    }

    /** Magmatic engine: with a full cell in front its buffer fills up and it breaks; the wrench repairs it. */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void magmaticEngineBreaksWhenFull(GameTestHelper helper) {
        helper.setBlock(POS, TeBlocks.MAGMATIC_ENGINE.get());
        helper.setBlock(POS.above(), TeBlocks.ENERGY_CELL.get());
        MagmaticEngineBlockEntity engine = helper.getBlockEntity(POS);
        EnergyCellBlockEntity cell = helper.getBlockEntity(POS.above());
        cell.setEnergy(EnergyCellBlockEntity.STORAGE);
        engine.getFluidHandler(null).fill(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.EXECUTE);
        engine.getPowerProvider().setEnergyStored(3999.9F);
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(engine.isShutdown(), "engine broke");
            helper.assertTrue(engine.getEnergy() < 3300.0F, "buffer −20 %");
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TeItems.CRESCENT_HAMMER.get()));
            helper.useBlock(POS, player);
            helper.assertFalse(engine.isShutdown(), "repaired");
            helper.succeed();
        });
    }

    /** Energy cell: sends up to its limit to a machine that asks (40 MJ/t), keeps its data when dismantled. */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void energyCellPowersMachine(GameTestHelper helper) {
        TeMachineBlockEntity machine = machine(helper, TeBlocks.PULVERIZER.get());
        helper.setBlock(POS.above(), TeBlocks.ENERGY_CELL.get());
        EnergyCellBlockEntity cell = helper.getBlockEntity(POS.above());
        cell.load(new EnergyCellData(10000.0F, 50, 100, false, false));
        float[] received = new float[1];
        helper.runAfterDelay(3, () -> {
            received[0] = machine.getEnergy();
            helper.assertTrue(received[0] >= 40.0F && received[0] % 40.0F < 0.01F, "40 MJ per tick");
            helper.assertTrue(Math.abs(cell.getEnergy() + received[0] - 10000.0F) < 0.01F, "taken from the cell");
            cell.setLimits(0, 100);
        });
        helper.runAfterDelay(8, () -> {
            helper.assertTrue(Math.abs(machine.getEnergy() - received[0]) < 0.01F, "send limit 0 stops it");
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setShiftKeyDown(true);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TeItems.CRESCENT_HAMMER.get()));
            helper.useBlock(POS.above(), player);
            helper.assertBlockNotPresent(TeBlocks.ENERGY_CELL.get(), POS.above());
            ItemEntity drop = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                    new AABB(helper.absolutePos(POS.above())).inflate(1.0)).stream().findFirst().orElseThrow();
            EnergyCellData data = drop.getItem().get(TeDataComponents.ENERGY_CELL.get());
            helper.assertTrue(data != null && data.send() == 0
                            && Math.abs(data.energy() + received[0] - 10000.0F) < 0.01F,
                    "dismantled with its charge and limits");
            helper.succeed();
        });
    }

    /** Energy conduits: cell → input node → output node → pulverizer, 5 % lost on the way in. */
    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void energyConduitCarriesEnergy(GameTestHelper helper) {
        BlockPos machinePos = POS.east(2);
        helper.setBlock(machinePos, TeBlocks.PULVERIZER.get().defaultBlockState()
                .setValue(TeMachineBlock.FACING, Direction.NORTH));
        TeMachineBlockEntity machine = helper.getBlockEntity(machinePos);
        helper.setBlock(POS.west(), TeBlocks.ENERGY_CELL.get());
        EnergyCellBlockEntity cell = helper.getBlockEntity(POS.west());
        cell.load(new EnergyCellData(100000.0F, 50, 100, false, false));
        helper.setBlock(POS, TeBlocks.ENERGY_CONDUIT.get());
        helper.setBlock(POS.east(), TeBlocks.ENERGY_CONDUIT.get());
        EnergyConduitBlockEntity input = helper.getBlockEntity(POS);
        helper.runAfterDelay(3, () -> {
            helper.assertTrue(input.isNode() && input.getGrid() != null && input.getGrid().getNodeCount() == 2,
                    "two nodes in one network");
            helper.assertTrue(input.getPowerProvider().getMaxEnergyStored() == 1000, "500 MJ per node");
            input.changeMode();
        });
        helper.runAfterDelay(60, () -> {
            float sent = 100000.0F - cell.getEnergy();
            float stored = input.getPowerProvider().getEnergyStored();
            helper.assertTrue(sent > 0.0F, "the cell feeds the input node");
            helper.assertTrue(machine.getEnergy() > 0.0F, "the output node feeds the pulverizer");
            helper.assertTrue(Math.abs(machine.getEnergy() + stored - sent * GridPowerProvider.CONDUIT_ENERGY_LOSS)
                    < 0.1F, "5 % loss");
            helper.succeed();
        });
    }

    /** Magmatic engine into an energy conduit that asks for nothing: still 25 · 0.4 MJ per stroke, minus 5 %. */
    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void engineFeedsConduitMinimum(GameTestHelper helper) {
        helper.setBlock(POS, TeBlocks.MAGMATIC_ENGINE.get());
        helper.setBlock(POS.above(), TeBlocks.ENERGY_CONDUIT.get());
        MagmaticEngineBlockEntity engine = helper.getBlockEntity(POS);
        EnergyConduitBlockEntity conduit = helper.getBlockEntity(POS.above());
        engine.getFluidHandler(null).fill(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.EXECUTE);
        helper.succeedWhen(() -> {
            float stored = conduit.getPowerProvider().getEnergyStored();
            helper.assertTrue(stored > 0.0F, "energy in the network");
            float strokes = stored / (10.0F * GridPowerProvider.CONDUIT_ENERGY_LOSS);
            helper.assertTrue(Math.abs(strokes - Math.round(strokes)) < 0.01F, "9.5 MJ per stroke");
        });
    }

    /** Liquiducts: an input node with redstone pulls from a tank, the output node fills the other tank. */
    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void liquiductMovesWater(GameTestHelper helper) {
        helper.setBlock(POS.west(), BcBlocks.TANK.get());
        helper.setBlock(POS.east(2), BcBlocks.TANK.get());
        IFluidHandler source = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                helper.absolutePos(POS.west()), Direction.EAST);
        source.fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.EXECUTE);
        helper.setBlock(POS, TeBlocks.LIQUIDUCT.get());
        helper.setBlock(POS.east(), TeBlocks.LIQUIDUCT.get());
        LiquiductBlockEntity input = helper.getBlockEntity(POS);
        helper.runAfterDelay(3, () -> {
            helper.assertTrue(input.getGrid() != null && input.getGrid().getCapacity() == 1000, "500 mB per conduit");
            input.changeMode();
            helper.setBlock(POS.below(), Blocks.REDSTONE_BLOCK);
        });
        helper.succeedWhen(() -> {
            IFluidHandler target = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                    helper.absolutePos(POS.east(2)), Direction.WEST);
            helper.assertTrue(target != null && target.getFluidInTank(0).getAmount() >= 1000, "water arrives");
        });
    }

    /** Liquiducts with different liquids do not join. */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void liquiductsKeepLiquidsApart(GameTestHelper helper) {
        helper.setBlock(POS.west(), TeBlocks.LIQUIDUCT.get());
        helper.setBlock(POS.east(), TeBlocks.LIQUIDUCT.get());
        LiquiductBlockEntity water = helper.getBlockEntity(POS.west());
        LiquiductBlockEntity lava = helper.getBlockEntity(POS.east());
        helper.runAfterDelay(2, () -> {
            water.getFluidHandler(null).fill(new FluidStack(Fluids.WATER, 200), IFluidHandler.FluidAction.EXECUTE);
            lava.getFluidHandler(null).fill(new FluidStack(Fluids.LAVA, 200), IFluidHandler.FluidAction.EXECUTE);
            helper.setBlock(POS, TeBlocks.LIQUIDUCT.get());
        });
        helper.runAfterDelay(6, () -> {
            LiquiductBlockEntity middle = helper.getBlockEntity(POS);
            helper.assertTrue(water.getGrid() != null && lava.getGrid() != null && water.getGrid() != lava.getGrid(),
                    "two networks");
            helper.assertTrue(middle.getGrid() == water.getGrid() || middle.getGrid() == lava.getGrid(),
                    "the new liquiduct joins one of them");
            helper.assertTrue(water.getGrid().getLiquidAmount() + lava.getGrid().getLiquidAmount() == 400,
                    "nothing lost");
            helper.succeed();
        });
    }

    /** Portable Tank: a fill overflows into the tank above, which (output mode) refills the one below. */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void portableTanksStack(GameTestHelper helper) {
        helper.setBlock(POS, TeBlocks.PORTABLE_TANK.get());
        helper.setBlock(POS.above(), TeBlocks.PORTABLE_TANK.get());
        PortableTankBlockEntity bottom = helper.getBlockEntity(POS);
        PortableTankBlockEntity top = helper.getBlockEntity(POS.above());
        bottom.placeBlock();
        top.placeBlock();
        helper.assertTrue(top.getMode() && !bottom.getMode(), "upper tank starts in output mode");
        helper.assertTrue(bottom.getFluidHandler(Direction.NORTH) == null, "no access from the sides");
        IFluidHandler handler = bottom.getFluidHandler(null);
        helper.assertTrue(handler.fill(new FluidStack(Fluids.WATER, 10000), IFluidHandler.FluidAction.EXECUTE) == 10000,
                "10000 mB taken");
        helper.assertTrue(bottom.getTank().getFluidAmount() == 8000 && top.getTank().getFluidAmount() == 2000,
                "overflow into the upper tank");
        handler.drain(4000, IFluidHandler.FluidAction.EXECUTE);
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(top.getTank().isEmpty() && bottom.getTank().getFluidAmount() == 6000,
                    "upper tank emptied downwards");
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setShiftKeyDown(true);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TeItems.CRESCENT_HAMMER.get()));
            helper.useBlock(POS, player);
            helper.assertBlockNotPresent(TeBlocks.PORTABLE_TANK.get(), POS);
            ItemEntity drop = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                    new AABB(helper.absolutePos(POS)).inflate(1.0)).stream().findFirst().orElseThrow();
            SimpleFluidContent content = drop.getItem().get(TeDataComponents.TANK_LIQUID.get());
            helper.assertTrue(content != null && content.getAmount() == 6000, "dismantled with its water");
            helper.succeed();
        });
    }

    /** A tesseract tuned to {@code frequency} in {@code mode}; every test uses its own public frequency. */
    private static <T extends TesseractBlockEntity> T tesseract(GameTestHelper helper, BlockPos pos,
                                                                TesseractBlock block, int frequency, byte mode) {
        helper.setBlock(pos, block);
        T tesseract = helper.getBlockEntity(pos);
        tesseract.setTileInfo(frequency, mode, TesseractBlockEntity.ACCESS_PUBLIC);
        return tesseract;
    }

    /** Energy tesseracts: cell → sender → receiver → pulverizer; 25 % lost on the way in. */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void energyTesseractCarriesEnergy(GameTestHelper helper) {
        EnergyTesseractBlockEntity receiver = tesseract(helper, POS.east(3), TeBlocks.ENERGY_TESSERACT.get(), 901,
                TesseractBlockEntity.MODE_RECEIVE);
        EnergyTesseractBlockEntity sender = tesseract(helper, POS, TeBlocks.ENERGY_TESSERACT.get(), 901,
                TesseractBlockEntity.MODE_SEND);
        helper.assertTrue(helper.getBlockState(POS).getValue(TesseractBlock.ACTIVE), "tuned tesseract is active");
        helper.setBlock(POS.east(4), TeBlocks.PULVERIZER.get().defaultBlockState()
                .setValue(TeMachineBlock.FACING, Direction.NORTH));
        TeMachineBlockEntity machine = helper.getBlockEntity(POS.east(4));
        helper.setBlock(POS.west(), TeBlocks.ENERGY_CELL.get());
        EnergyCellBlockEntity cell = helper.getBlockEntity(POS.west());
        cell.load(new EnergyCellData(100000.0F, 20, 100, false, false));
        helper.assertTrue(sender.getValidOutputs().contains(receiver) && receiver.getValidOutputs().isEmpty(),
                "the sender sees the receiver");
        helper.runAfterDelay(20, () -> {
            float sent = 100000.0F - cell.getEnergy();
            helper.assertTrue(sent > 0.0F, "the cell feeds the sender");
            helper.assertTrue(machine.getEnergy() > 0.0F, "the receiver feeds the pulverizer");
            helper.assertTrue(Math.abs(machine.getEnergy() + sender.getEnergy() + receiver.getEnergy()
                    - sent * TesseractPowerProvider.TELEPORT_ENERGY_LOSS) < 0.1F, "25 % loss");
            sender.setTileInfo(-1, TesseractBlockEntity.MODE_SEND, TesseractBlockEntity.ACCESS_PUBLIC);
            helper.assertTrue(sender.powerRequest() == 0, "a disabled tesseract asks for nothing");
            helper.assertFalse(helper.getBlockState(POS).getValue(TesseractBlock.ACTIVE), "disabled");
            helper.succeed();
        });
    }

    /** Liquid tesseracts: a fill goes straight into the tank next to the receiver; private ones are not linked. */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void liquidTesseractFillsRemoteTank(GameTestHelper helper) {
        tesseract(helper, POS.east(3), TeBlocks.LIQUID_TESSERACT.get(), 902, TesseractBlockEntity.MODE_BOTH);
        helper.setBlock(POS.east(4), BcBlocks.TANK.get());
        LiquidTesseractBlockEntity sender = tesseract(helper, POS, TeBlocks.LIQUID_TESSERACT.get(), 902,
                TesseractBlockEntity.MODE_SEND);
        IFluidHandler handler = sender.getFluidHandler(Direction.WEST);
        helper.assertTrue(handler.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE) == 1000,
                "1000 mB sent");
        IFluidHandler tank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                helper.absolutePos(POS.east(4)), Direction.WEST);
        helper.assertTrue(tank.getFluidInTank(0).getAmount() == 1000, "water in the remote tank");
        helper.assertTrue(handler.drain(100, IFluidHandler.FluidAction.EXECUTE).isEmpty(), "nothing to drain");
        sender.setTileInfo(902, TesseractBlockEntity.MODE_SEND, TesseractBlockEntity.ACCESS_PRIVATE);
        helper.assertTrue(handler.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE) == 0,
                "a private tesseract has its own channel");
        helper.succeed();
    }

    /** Item tesseracts: an inserted stack lands in the chest next to the receiver; without one it waits. */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void itemTesseractSendsToChest(GameTestHelper helper) {
        ItemTesseractBlockEntity receiver = tesseract(helper, POS.east(3), TeBlocks.ITEM_TESSERACT.get(), 903,
                TesseractBlockEntity.MODE_RECEIVE);
        helper.setBlock(POS.east(4), Blocks.CHEST);
        ItemTesseractBlockEntity sender = tesseract(helper, POS, TeBlocks.ITEM_TESSERACT.get(), 903,
                TesseractBlockEntity.MODE_SEND);
        var handler = sender.getItemHandler(Direction.WEST);
        helper.assertTrue(handler.insertItem(0, new ItemStack(Items.COBBLESTONE, 16), false).isEmpty(), "taken");
        ChestBlockEntity chest = helper.getBlockEntity(POS.east(4));
        helper.assertTrue(chest.countItem(Items.COBBLESTONE) == 16, "cobblestone in the remote chest");
        receiver.setTileInfo(-1, TesseractBlockEntity.MODE_RECEIVE, TesseractBlockEntity.ACCESS_PUBLIC);
        helper.assertTrue(handler.insertItem(0, new ItemStack(Items.DIRT, 8), false).isEmpty(), "buffered");
        helper.assertTrue(sender.getBuffer().getCount() == 8, "no receiver: the stack waits in the buffer");
        helper.assertFalse(handler.insertItem(0, new ItemStack(Items.DIRT, 1), false).isEmpty(),
                "full buffer takes nothing");
        receiver.setTileInfo(903, TesseractBlockEntity.MODE_RECEIVE, TesseractBlockEntity.ACCESS_PUBLIC);
        helper.succeedWhen(() -> {
            helper.assertTrue(chest.countItem(Items.DIRT) == 8, "the buffer is retried every 40 ticks");
            helper.assertTrue(sender.getBuffer().isEmpty(), "buffer empty");
        });
    }

    /** Item pipes connect to every side of a machine, also to closed ones ({@code IInventory} with slots). */
    @GameTest(template = EMPTY)
    public static void itemPipesConnectToClosedSides(GameTestHelper helper) {
        machine(helper, TeBlocks.POWERED_FURNACE.get());
        helper.setBlock(POS.north(), BcBlocks.pipe(PipeType.COBBLESTONE_TRANSPORT).get());
        helper.setBlock(POS.above(), BcBlocks.pipe(PipeType.COBBLESTONE_TRANSPORT).get());
        helper.runAfterDelay(3, () -> {
            helper.assertBlockProperty(POS.north(), PipeBlock.SOUTH, true);
            helper.assertBlockProperty(POS.above(), PipeBlock.DOWN, true);
            helper.succeed();
        });
    }

    /** Changing a side in the configuration tab updates the connection of an adjacent fluid pipe. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void fluidPipeFollowsSideChange(GameTestHelper helper) {
        CrucibleBlockEntity crucible = machine(helper, TeBlocks.MAGMA_CRUCIBLE.get());
        crucible.clearSide(Direction.UP);
        helper.setBlock(POS.above(), BcBlocks.pipe(PipeType.COBBLESTONE_FLUID).get());
        helper.runAfterDelay(3, () -> {
            helper.assertBlockProperty(POS.above(), PipeBlock.DOWN, false);
            crucible.changeSide(Direction.UP, true);
            crucible.changeSide(Direction.UP, true);
            helper.runAfterDelay(3, () -> {
                helper.assertBlockProperty(POS.above(), PipeBlock.DOWN, true);
                helper.succeed();
            });
        });
    }
}
