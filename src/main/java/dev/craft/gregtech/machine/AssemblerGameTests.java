package dev.craft.gregtech.machine;

import dev.craft.Craft;
import dev.craft.buildcraft.BcItems;
import dev.craft.buildcraft.power.BcCapabilities;
import dev.craft.data.CraftItemTagsProvider;
import dev.craft.gregtech.GtBlocks;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.recipe.GtRecipe;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the assembler, the GT upgrades and the lithium items (phase 6b.4, spec 13 §6). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class AssemblerGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(2, 1, 2);

    private AssemblerGameTests() {
    }

    private static AssemblerBlockEntity assembler(GameTestHelper helper) {
        helper.setBlock(POS, GtBlocks.machine(GtMachine.ASSEMBLING_MACHINE).get());
        return helper.getBlockEntity(POS);
    }

    private static ItemStack generator() {
        return new ItemStack(Ic2Items.machine(Ic2Machine.GENERATOR).get());
    }

    @GameTest(template = EMPTY, timeoutTicks = 500)
    public static void assemblesLeverFromSwappedSlots(GameTestHelper helper) {
        AssemblerBlockEntity assembler = assembler(helper);
        helper.assertTrue(assembler.isSeparatedInputs(), "separated inputs by default");
        assembler.toggleItemTransfer();
        assembler.getInventory().set(GtBasicMachineBlockEntity.SLOT_INPUT_1, new ItemStack(Items.COBBLESTONE, 2));
        assembler.getInventory().set(GtBasicMachineBlockEntity.SLOT_INPUT_2, new ItemStack(Items.STICK));
        assembler.setStoredEnergyForTest(1500);
        // 400 ticks at 1 EU/t; the second cobblestone has no stick left. The next recipe check (still > 900 EU)
        // merges output slot 3 into slot 4.
        helper.succeedWhen(() -> {
            ItemStack lever = assembler.getInventory().get(GtBasicMachineBlockEntity.SLOT_OUTPUT_2);
            helper.assertTrue(lever.is(Items.LEVER) && lever.getCount() == 1, "one lever");
            helper.assertValueEqual(assembler.getStoredEU(), 1100, "energy left");
            helper.assertTrue(assembler.getInventory().get(GtBasicMachineBlockEntity.SLOT_INPUT_2).isEmpty(),
                    "stick used");
            helper.assertValueEqual(assembler.getInventory().get(GtBasicMachineBlockEntity.SLOT_INPUT_1).getCount(), 1,
                    "one cobblestone left");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void startsWaterMillRecipe(GameTestHelper helper) {
        AssemblerBlockEntity assembler = assembler(helper);
        assembler.getInventory().set(GtBasicMachineBlockEntity.SLOT_INPUT_1,
                new ItemStack(GtItems.ALUMINIUM_PLATE.get(), 5));
        assembler.getInventory().set(GtBasicMachineBlockEntity.SLOT_INPUT_2, generator());
        assembler.setStoredEnergyForTest(2000);
        helper.succeedWhen(() -> {
            helper.assertBlockProperty(POS, GtMachineBlock.ACTIVE, true);
            helper.assertTrue(assembler.getInventory().get(GtBasicMachineBlockEntity.SLOT_INPUT_2).isEmpty(),
                    "generator used");
            helper.assertValueEqual(assembler.getInventory().get(GtBasicMachineBlockEntity.SLOT_INPUT_1).getCount(), 1,
                    "four plates used");
        });
    }

    @GameTest(template = EMPTY)
    public static void assemblerRecipes(GameTestHelper helper) {
        GtRecipe waterMills = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.ASSEMBLER,
                new ItemStack(GtItems.ALUMINIUM_PLATE.get(), 4), generator(), true);
        helper.assertTrue(waterMills != null && waterMills.getOutput(0).is(Ic2Items.machine(Ic2Machine.WATER_MILL).get())
                && waterMills.getOutput(0).getCount() == 2 && waterMills.getDuration() == 6400
                && waterMills.getEuPerTick() == 8, "generator + 4 aluminium plates → 2 water mills");
        GtRecipe windMill = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.ASSEMBLER, generator(),
                new ItemStack(Ic2Items.CARBON_PLATE.get(), 4), true);
        helper.assertTrue(windMill != null && windMill.getOutput(0).is(Ic2Items.machine(Ic2Machine.WIND_MILL).get()),
                "wind mill");
        GtRecipe back = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.ASSEMBLER, new ItemStack(Items.DIRT),
                new ItemStack(Ic2Items.machine(Ic2Machine.WATER_MILL).get(), 2), true);
        helper.assertTrue(back != null && back.getOutput(0).is(Ic2Items.machine(Ic2Machine.GENERATOR).get()),
                "two water mills → generator, other slot ignored");
        GtRecipe cable = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.ASSEMBLER,
                new ItemStack(Ic2Items.cable(CableType.INSULATED_GOLD).get()), new ItemStack(Ic2Items.RUBBER.get()),
                true);
        helper.assertTrue(cable != null && cable.getDuration() == 200, "configured duration of the output");
        GtRecipe converter = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.ASSEMBLER,
                new ItemStack(BcItems.IRON_GEAR.get()), generator(), true);
        helper.assertTrue(converter != null && converter.getOutput(0).is(GtItems.PNEUMATIC_GENERATOR_UPGRADE.get()),
                "MJ converter");
        helper.assertTrue(GtRecipe.find(helper.getLevel(), GtRecipe.Kind.ASSEMBLER, generator(),
                new ItemStack(GtItems.ALUMINIUM_PLATE.get(), 3), true) == null, "not enough plates");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void gregTechUpgrades(GameTestHelper helper) {
        AssemblerBlockEntity assembler = assembler(helper);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack hv = new ItemStack(GtItems.HV_TRANSFORMER_UPGRADE.get(), 4);
        helper.assertFalse(assembler.installUpgrade(player, hv), "HV transformer only after two IC2 transformers");
        ItemStack transformers = new ItemStack(Ic2Items.TRANSFORMER_UPGRADE.get(), 2);
        assembler.installUpgrade(player, transformers);
        assembler.installUpgrade(player, transformers);
        helper.assertTrue(assembler.installUpgrade(player, hv), "first HV transformer");
        helper.assertValueEqual(assembler.getMaxSafeInput(), 2048, "input after three");
        helper.assertTrue(assembler.installUpgrade(player, hv), "second HV transformer");
        helper.assertFalse(assembler.installUpgrade(player, hv), "stops at 8192");
        helper.assertValueEqual(hv.getCount(), 2, "two used up");
        ItemStack lithium = new ItemStack(GtItems.LITHIUM_BATTERY_UPGRADE.get());
        helper.assertTrue(assembler.installUpgrade(player, lithium), "lithium battery upgrade");
        helper.assertValueEqual(assembler.getCapacity(), 102000, "capacity");
        ItemStack converter = new ItemStack(GtItems.PNEUMATIC_GENERATOR_UPGRADE.get(), 2);
        helper.assertTrue(assembler.installUpgrade(player, converter), "MJ converter");
        helper.assertFalse(assembler.installUpgrade(player, converter), "only one MJ converter");
        helper.assertTrue(assembler.hasMJConverter(), "converter installed");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void mjConverterReceivesMj(GameTestHelper helper) {
        AssemblerBlockEntity assembler = assembler(helper);
        BlockPos abs = helper.absolutePos(POS);
        helper.assertTrue(helper.getLevel().getCapability(BcCapabilities.POWER_RECEPTOR, abs, Direction.UP) == null,
                "no MJ receiver without converter");
        assembler.installUpgrade(helper.makeMockPlayer(GameType.CREATIVE),
                new ItemStack(GtItems.PNEUMATIC_GENERATOR_UPGRADE.get()));
        helper.onEachTick(() -> {
            if (assembler.getPowerProvider() != null && assembler.getStoredMJ() == 0) {
                assembler.getPowerProvider().setEnergyStored(150.0F);
            }
        });
        // The provider appears with the first logic tick; afterwards everything up to the capacity moves over.
        helper.succeedWhen(() -> {
            helper.assertTrue(helper.getLevel().getCapability(BcCapabilities.POWER_RECEPTOR, abs, Direction.UP)
                    == assembler, "MJ receiver with converter");
            helper.assertValueEqual(assembler.getStoredMJ(), 150, "MJ moved into the storage");
            helper.assertValueEqual(assembler.getStoredEnergy(), 375, "MJ counts 2.5 times");
        });
    }

    @GameTest(template = EMPTY)
    public static void lithiumItems(GameTestHelper helper) {
        ItemStack battery = new ItemStack(GtItems.LITHIUM_BATTERY.get());
        ElectricItem item = GtItems.LITHIUM_BATTERY.get();
        helper.assertValueEqual(battery.getMaxStackSize(), 16, "empty batteries stack");
        helper.assertTrue(item.getMaxCharge() == 100000 && item.getTransferLimit() == 128 && item.getTier() == 1
                && item.canProvideEnergy(), "battery values");
        ItemStack charged = ElectricItems.charged(battery.copy());
        helper.assertValueEqual(charged.getMaxStackSize(), 1, "charged battery does not stack");
        helper.assertTrue(charged.is(CraftItemTagsProvider.CRAFTING_100K_EU_STORE), "100k EU store");
        ElectricItem pack = GtItems.LITHIUM_BATPACK.get();
        helper.assertTrue(pack.getMaxCharge() == 600000 && pack.getTransferLimit() == 128 && pack.getTier() == 1
                && pack.canProvideEnergy(), "batpack values");
        helper.assertTrue(new ItemStack(GtItems.STEEL_MACHINE_HULL.get()).is(CraftItemTagsProvider.RAW_MACHINE_TIER_02)
                && new ItemStack(GtItems.BRONZE_MACHINE_HULL.get()).is(CraftItemTagsProvider.RAW_MACHINE_TIER_01),
                "machine hull tags");
        helper.succeed();
    }
}
