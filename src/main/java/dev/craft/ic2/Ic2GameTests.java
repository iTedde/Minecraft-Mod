package dev.craft.ic2;

import java.util.List;
import java.util.Optional;

import dev.craft.Craft;
import dev.craft.ic2.block.RubberSaplingBlock;
import dev.craft.ic2.block.RubberWoodBlock;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.generator.GeneratorBlockEntity;
import dev.craft.ic2.generator.GeothermalGeneratorBlockEntity;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.ScrapBoxItem;
import dev.craft.ic2.item.TreetapItem;
import dev.craft.ic2.machine.ElectricMachineBlockEntity;
import dev.craft.ic2.machine.IronFurnaceBlockEntity;
import dev.craft.ic2.machine.MachineBlock;
import dev.craft.ic2.machine.MachineBlockEntity;
import dev.craft.ic2.machine.RecipeMachineBlockEntity;
import dev.craft.ic2.machine.RecyclerBlockEntity;
import dev.craft.ic2.storage.ElectricStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the phase 3 content ({@code gradlew runGameTestServer}). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class Ic2GameTests {
    private static final String EMPTY = "empty_8x4x8";

    private Ic2GameTests() {
    }

    private static <T extends MachineBlockEntity> T place(GameTestHelper helper, BlockPos pos, Ic2Machine machine) {
        helper.setBlock(pos, Ic2Blocks.machine(machine).get());
        return helper.getBlockEntity(pos);
    }

    @GameTest(template = EMPTY)
    public static void generatorBurnsCoal(GameTestHelper helper) {
        GeneratorBlockEntity generator = place(helper, new BlockPos(1, 1, 1), Ic2Machine.GENERATOR);
        generator.getInventory().set(1, new ItemStack(Items.COAL));
        helper.succeedWhen(() -> {
            helper.assertTrue(generator.getInventory().isEmpty(1), "coal consumed");
            // Coal: 1600 ticks / 4 = 400 generator ticks of 10 EU.
            helper.assertValueEqual(generator.getItemFuelTime(), 400, "item fuel time");
            helper.assertTrue(generator.getStorage() >= 100, "storage filled");
            helper.assertValueEqual(generator.getStorage() + generator.getFuel() * GeneratorBlockEntity.PRODUCTION,
                    4000, "fuel converts to 10 EU per tick");
            helper.assertTrue(generator.isActive(), "active");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void generatorChargesBatBoxThroughCable(GameTestHelper helper) {
        GeneratorBlockEntity generator = place(helper, new BlockPos(1, 1, 1), Ic2Machine.GENERATOR);
        helper.setBlock(new BlockPos(2, 1, 1), Ic2Blocks.cable(CableType.INSULATED_COPPER).get());
        ElectricStorageBlockEntity batBox = place(helper, new BlockPos(3, 1, 1), Ic2Machine.BATBOX);
        generator.getInventory().set(1, new ItemStack(Items.COAL));
        helper.succeedWhen(() -> helper.assertTrue(batBox.getEnergy() >= 100, "BatBox charged"));
    }

    @GameTest(template = EMPTY, timeoutTicks = 500)
    public static void maceratorGrindsIronOre(GameTestHelper helper) {
        RecipeMachineBlockEntity macerator = place(helper, new BlockPos(1, 1, 1), Ic2Machine.MACERATOR);
        macerator.getInventory().set(ElectricMachineBlockEntity.SLOT_INPUT, new ItemStack(Items.IRON_ORE));
        // One redstone = exactly one operation (2 EU/t × 400 t).
        macerator.getInventory().set(ElectricMachineBlockEntity.SLOT_BATTERY, new ItemStack(Items.REDSTONE));
        helper.succeedWhen(() -> {
            ItemStack output = macerator.getInventory().get(ElectricMachineBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(output.is(Ic2Items.IRON_DUST.get()) && output.getCount() == 2, "2 iron dust");
            helper.assertTrue(macerator.getInventory().isEmpty(ElectricMachineBlockEntity.SLOT_INPUT), "ore used");
        });
    }

    @GameTest(template = EMPTY)
    public static void upgradesChangeMachineValues(GameTestHelper helper) {
        RecipeMachineBlockEntity macerator = place(helper, new BlockPos(1, 1, 1), Ic2Machine.MACERATOR);
        helper.runAfterDelay(1, () -> {
            macerator.getInventory().setStackInSlot(ElectricMachineBlockEntity.FIRST_UPGRADE_SLOT,
                    new ItemStack(Ic2Items.OVERCLOCKER_UPGRADE.get(), 2));
            macerator.getInventory().setStackInSlot(ElectricMachineBlockEntity.FIRST_UPGRADE_SLOT + 1,
                    new ItemStack(Ic2Items.TRANSFORMER_UPGRADE.get()));
            macerator.getInventory().setStackInSlot(ElectricMachineBlockEntity.FIRST_UPGRADE_SLOT + 2,
                    new ItemStack(Ic2Items.ENERGY_STORAGE_UPGRADE.get()));
            // 400 × 0.7² = 196, 2 × 1.6² = 5.12 → 5, 32 × 4 = 128, 800 + 10 000 + 128 − 1.
            helper.assertValueEqual(macerator.getOperationLength(), 196, "operation length");
            helper.assertValueEqual(macerator.getEnergyConsume(), 5, "energy per tick");
            helper.assertValueEqual(macerator.getMaxSafeInput(), 128, "max input");
            helper.assertValueEqual(macerator.getTier(), 2, "tier");
            helper.assertValueEqual(macerator.getMaxEnergy(), 10927, "max energy");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, batch = "ic2_overvoltage")
    public static void overvoltageExplodesMachine(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        RecipeMachineBlockEntity macerator = place(helper, pos, Ic2Machine.MACERATOR);
        helper.runAfterDelay(1, () -> {
            helper.assertValueEqual(macerator.injectEnergy(Direction.NORTH, 32), 0, "32 EU accepted");
            macerator.injectEnergy(Direction.NORTH, 33);
        });
        helper.runAfterDelay(2, () -> {
            helper.assertBlockNotPresent(Ic2Blocks.machine(Ic2Machine.MACERATOR).get(), pos);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void electricFurnaceRefinesIron(GameTestHelper helper) {
        ElectricMachineBlockEntity furnace = place(helper, new BlockPos(1, 1, 1), Ic2Machine.ELECTRIC_FURNACE);
        furnace.getInventory().set(ElectricMachineBlockEntity.SLOT_INPUT, new ItemStack(Items.IRON_INGOT));
        furnace.getInventory().set(ElectricMachineBlockEntity.SLOT_BATTERY, new ItemStack(Ic2Items.SU_BATTERY.get()));
        helper.succeedWhen(() -> helper.assertTrue(furnace.getInventory().get(ElectricMachineBlockEntity.SLOT_OUTPUT)
                .is(Ic2Items.REFINED_IRON_INGOT.get()), "refined iron"));
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void ironFurnaceSmeltsWithCoal(GameTestHelper helper) {
        IronFurnaceBlockEntity furnace = place(helper, new BlockPos(1, 1, 1), Ic2Machine.IRON_FURNACE);
        furnace.getInventory().set(0, new ItemStack(Items.RAW_IRON));
        furnace.getInventory().set(1, new ItemStack(Items.COAL));
        helper.succeedWhen(() -> {
            helper.assertTrue(furnace.getInventory().get(2).is(Items.IRON_INGOT), "iron ingot");
            helper.assertTrue(helper.getTick() >= IronFurnaceBlockEntity.OPERATION_LENGTH, "160 ticks per item");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void recyclerNeverScrapsBlacklistedItems(GameTestHelper helper) {
        ElectricMachineBlockEntity recycler = place(helper, new BlockPos(1, 1, 1), Ic2Machine.RECYCLER);
        recycler.getInventory().set(ElectricMachineBlockEntity.SLOT_INPUT, new ItemStack(Items.STICK));
        recycler.getInventory().set(ElectricMachineBlockEntity.SLOT_BATTERY, new ItemStack(Items.REDSTONE));
        helper.succeedWhen(() -> {
            helper.assertTrue(recycler.getInventory().isEmpty(ElectricMachineBlockEntity.SLOT_INPUT), "stick used");
            helper.assertTrue(recycler.getInventory().isEmpty(ElectricMachineBlockEntity.SLOT_OUTPUT), "no scrap");
        });
    }

    /** GregTech adds snowball, snow layer, ice and snow block to the recycler blacklist (phase 12.10). */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void recyclerNeverScrapsGregTechBlacklist(GameTestHelper helper) {
        ElectricMachineBlockEntity recycler = place(helper, new BlockPos(1, 1, 1), Ic2Machine.RECYCLER);
        recycler.getInventory().set(ElectricMachineBlockEntity.SLOT_INPUT, new ItemStack(Items.ICE));
        recycler.getInventory().set(ElectricMachineBlockEntity.SLOT_BATTERY, new ItemStack(Items.REDSTONE));
        helper.assertTrue(new ItemStack(Items.SNOW).is(RecyclerBlockEntity.BLACKLIST), "snow layer blacklisted");
        helper.assertTrue(new ItemStack(Items.SNOW_BLOCK).is(RecyclerBlockEntity.BLACKLIST), "snow block blacklisted");
        helper.succeedWhen(() -> {
            helper.assertTrue(recycler.getInventory().isEmpty(ElectricMachineBlockEntity.SLOT_INPUT), "ice used");
            helper.assertTrue(recycler.getInventory().isEmpty(ElectricMachineBlockEntity.SLOT_OUTPUT), "no scrap");
        });
    }

    /** GregTech {@code UpgradeStacksize=4}: all three upgrade modules stack to 4. */
    @GameTest(template = EMPTY)
    public static void upgradeModulesStackToFour(GameTestHelper helper) {
        helper.assertValueEqual(new ItemStack(Ic2Items.OVERCLOCKER_UPGRADE.get()).getMaxStackSize(), 4, "overclocker");
        helper.assertValueEqual(new ItemStack(Ic2Items.TRANSFORMER_UPGRADE.get()).getMaxStackSize(), 4, "transformer");
        helper.assertValueEqual(new ItemStack(Ic2Items.ENERGY_STORAGE_UPGRADE.get()).getMaxStackSize(), 4,
                "energy storage");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void batBoxChargesBatteryFromSingleUseBattery(GameTestHelper helper) {
        ElectricStorageBlockEntity batBox = place(helper, new BlockPos(1, 1, 1), Ic2Machine.BATBOX);
        batBox.getInventory().set(1, new ItemStack(Ic2Items.SU_BATTERY.get()));
        batBox.getInventory().set(0, new ItemStack(Ic2Items.RE_BATTERY.get()));
        helper.succeedWhen(() -> {
            ItemStack battery = batBox.getInventory().get(0);
            helper.assertValueEqual(ElectricItems.getCharge(battery), 1000, "battery charge");
            helper.assertValueEqual(battery.getMaxStackSize(), 1, "charged RE battery does not stack");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void batBoxRedstoneModeEmitsWhenEmpty(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        ElectricStorageBlockEntity batBox = place(helper, pos, Ic2Machine.BATBOX);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (int i = 0; i < 3; i++) {
            batBox.cycleRedstoneMode(player);
        }
        helper.succeedWhen(() -> {
            helper.assertTrue(batBox.isEmittingRedstone(), "emits if empty");
            helper.assertBlockProperty(pos, MachineBlock.ACTIVE, true);
            helper.assertValueEqual(helper.getLevel().getSignal(helper.absolutePos(pos), Direction.EAST), 15,
                    "redstone signal");
        });
    }

    @GameTest(template = EMPTY)
    public static void geothermalGeneratorTakesLavaBucket(GameTestHelper helper) {
        GeothermalGeneratorBlockEntity generator = place(helper, new BlockPos(1, 1, 1),
                Ic2Machine.GEOTHERMAL_GENERATOR);
        generator.getInventory().set(1, new ItemStack(Items.LAVA_BUCKET));
        helper.succeedWhen(() -> {
            helper.assertTrue(generator.getInventory().get(1).is(Items.BUCKET), "empty bucket returned");
            helper.assertTrue(generator.getFuel() > 0 && generator.getFuel() <= 1000, "1000 mB lava");
        });
    }

    @GameTest(template = EMPTY)
    public static void boneMealGrowsRubberTree(GameTestHelper helper) {
        BlockPos ground = new BlockPos(3, 0, 3);
        BlockPos sapling = ground.above();
        helper.setBlock(ground, Blocks.DIRT);
        helper.setBlock(sapling, Ic2Blocks.RUBBER_SAPLING.get());
        helper.runAfterDelay(1, () -> {
            BlockState state = helper.getBlockState(sapling);
            ((RubberSaplingBlock) state.getBlock()).performBonemeal(helper.getLevel(), helper.getLevel().random,
                    helper.absolutePos(sapling), state);
            helper.assertBlockPresent(Ic2Blocks.RUBBER_WOOD.get(), sapling);
            helper.assertBlockPresent(Ic2Blocks.RUBBER_WOOD.get(), sapling.above());
            RubberWoodBlock.Resin resin = helper.getBlockState(sapling).getValue(RubberWoodBlock.RESIN);
            helper.assertTrue(resin == RubberWoodBlock.Resin.NATURAL || resin == RubberWoodBlock.Resin.WET,
                    "natural rubber wood");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void treetapExtractsResinFromWetHole(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, Ic2Blocks.RUBBER_WOOD.get().defaultBlockState()
                .setValue(RubberWoodBlock.RESIN, RubberWoodBlock.Resin.WET)
                .setValue(RubberWoodBlock.FACING, Direction.NORTH));
        helper.runAfterDelay(1, () -> {
            helper.assertFalse(TreetapItem.attemptExtract(null, helper.getLevel(), helper.absolutePos(pos),
                    Direction.EAST), "wrong side");
            helper.assertTrue(TreetapItem.attemptExtract(null, helper.getLevel(), helper.absolutePos(pos),
                    Direction.NORTH), "resin extracted");
            helper.assertBlockProperty(pos, RubberWoodBlock.RESIN, RubberWoodBlock.Resin.DRY);
            helper.assertItemEntityPresent(Ic2Items.RESIN.get(), pos, 2.0);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void copperOreDropsOneRawCopper(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, Blocks.COPPER_ORE);
        helper.runAfterDelay(1, () -> {
            helper.getLevel().destroyBlock(helper.absolutePos(pos), true);
            helper.assertItemEntityCountIs(Items.RAW_COPPER, pos, 2.0, 1);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void lapotronCrystalKeepsEnergyCrystalCharge(GameTestHelper helper) {
        ItemStack crystal = ElectricItems.charged(new ItemStack(Ic2Items.ENERGY_CRYSTAL.get()));
        ItemStack lapis = new ItemStack(Items.LAPIS_LAZULI);
        ItemStack circuit = new ItemStack(Ic2Items.ELECTRONIC_CIRCUIT.get());
        CraftingInput input = CraftingInput.of(3, 3, List.of(lapis, circuit, lapis, lapis, crystal, lapis, lapis,
                circuit, lapis));
        Optional<RecipeHolder<CraftingRecipe>> recipe = helper.getLevel().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        helper.assertTrue(recipe.isPresent(), "lapotron recipe");
        ItemStack result = recipe.get().value().assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(result.is(Ic2Items.LAPOTRON_CRYSTAL.get()), "lapotron crystal");
        helper.assertValueEqual(ElectricItems.getCharge(result), 100000, "transferred charge");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void ic2RecipesAreNotMirrored(GameTestHelper helper) {
        ItemStack planks = new ItemStack(Items.OAK_PLANKS);
        ItemStack none = ItemStack.EMPTY;
        CraftingInput treetap = CraftingInput.of(3, 3, List.of(none, planks, none, planks, planks, planks, planks,
                none, none));
        CraftingInput mirrored = CraftingInput.of(3, 3, List.of(none, planks, none, planks, planks, planks, none,
                none, planks));
        helper.assertTrue(helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, treetap,
                helper.getLevel()).map(r -> r.value().assemble(treetap, helper.getLevel().registryAccess())
                .is(Ic2Items.TREETAP.get())).orElse(false), "treetap recipe");
        helper.assertFalse(helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, mirrored,
                helper.getLevel()).map(r -> r.value().assemble(mirrored, helper.getLevel().registryAccess())
                .is(Ic2Items.TREETAP.get())).orElse(false), "mirrored treetap recipe");
        helper.succeed();
    }

    private static InteractionResult useResin(GameTestHelper helper, BlockPos pos, Direction side) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Ic2Items.RESIN.get(), 2));
        BlockPos absolute = helper.absolutePos(pos);
        InteractionResult result = player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), side, absolute, false)));
        helper.assertValueEqual(player.getMainHandItem().getCount(), result.consumesAction() ? 1 : 2, "resin used");
        return result;
    }

    @GameTest(template = EMPTY)
    public static void resinMakesPistonSticky(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.NORTH));
        helper.assertFalse(useResin(helper, pos, Direction.SOUTH).consumesAction(), "back of the piston");
        helper.assertTrue(useResin(helper, pos, Direction.NORTH).consumesAction(), "front of the piston");
        helper.assertBlockPresent(Blocks.STICKY_PISTON, pos);
        helper.assertBlockProperty(pos, PistonBaseBlock.FACING, Direction.NORTH);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void resinSheetNeedsOpaqueBlockBelow(GameTestHelper helper) {
        BlockPos stone = new BlockPos(2, 1, 2);
        BlockPos glass = new BlockPos(4, 1, 2);
        helper.setBlock(stone, Blocks.STONE);
        helper.setBlock(glass, Blocks.GLASS);
        helper.assertFalse(useResin(helper, stone, Direction.EAST).consumesAction(), "only the top side");
        helper.assertFalse(useResin(helper, glass, Direction.UP).consumesAction(), "glass is not opaque");
        helper.assertTrue(useResin(helper, stone, Direction.UP).consumesAction(), "placed on stone");
        helper.assertBlockPresent(Ic2Blocks.RESIN_SHEET.get(), stone.above());
        helper.setBlock(stone, Blocks.AIR);
        helper.assertBlockNotPresent(Ic2Blocks.RESIN_SHEET.get(), stone.above());
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void scrapBoxDropsMostlyScrap(GameTestHelper helper) {
        // Pack weights: scrap 20000 of 30455 for the items that exist so far (≈ 66 %).
        int scrap = 0;
        for (int i = 0; i < 1000; i++) {
            List<ItemStack> drop = ScrapBoxItem.getDrop(helper.getLevel());
            helper.assertValueEqual(drop.size(), 1, "one drop");
            helper.assertValueEqual(drop.getFirst().getCount(), 1, "single item");
            if (drop.getFirst().is(Ic2Items.SCRAP.get())) {
                scrap++;
            }
        }
        helper.assertTrue(scrap > 600 && scrap < 720, "scrap share " + scrap + "/1000");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void dispenserOpensScrapBox(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.UP));
        DispenserBlockEntity dispenser = helper.getBlockEntity(pos);
        dispenser.setItem(0, new ItemStack(Ic2Items.SCRAP_BOX.get(), 2));
        helper.setBlock(pos.east(), Blocks.REDSTONE_BLOCK);
        helper.succeedWhen(() -> {
            helper.assertValueEqual(dispenser.getItem(0).getCount(), 1, "one box opened");
            helper.assertFalse(helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                    new AABB(helper.absolutePos(pos)).inflate(4.0)).isEmpty(), "drop dispensed");
        });
    }
}
