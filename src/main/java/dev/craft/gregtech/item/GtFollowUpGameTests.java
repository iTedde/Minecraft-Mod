package dev.craft.gregtech.item;

import dev.craft.Craft;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.recipe.GtRecipe;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.item.reactor.HeatStorageItem;
import dev.craft.ic2.machine.MassFabricatorBlockEntity;
import dev.craft.ic2.reactor.NuclearReactorBlockEntity;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** GregTech follow-ups (phase 8a): NaK coolant cells and the lead plate for the containment reactor plating. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class GtFollowUpGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(3, 1, 3);

    private GtFollowUpGameTests() {
    }

    /** NaK coolant cells behave like the helium cells: same capacities, heat from a neighbouring uranium cell. */
    @GameTest(template = EMPTY)
    public static void nakCoolantCell(GameTestHelper helper) {
        helper.setBlock(POS.below(), Blocks.REDSTONE_BLOCK);
        helper.setBlock(POS, Ic2Blocks.machine(Ic2Machine.NUCLEAR_REACTOR).get());
        NuclearReactorBlockEntity reactor = helper.getBlockEntity(POS);
        reactor.setItemAt(0, 0, new ItemStack(Ic2Items.URANIUM_CELL.get()));
        reactor.setItemAt(1, 0, new ItemStack(GtItems.NAK_COOLANT_CELL_360K.get()));
        reactor.runCycle();
        ItemStack coolant = reactor.getItemAt(1, 0);
        helper.assertValueEqual(HeatStorageItem.getHeatOfStack(coolant), 4, "coolant heat");
        helper.assertValueEqual(GtItems.NAK_COOLANT_CELL_360K.get().getMaxHeat(reactor, coolant, 1, 0), 360000, "capacity");
        helper.assertValueEqual(GtItems.NAK_COOLANT_CELL_60K.get().getMaxHeat(reactor, coolant, 1, 0), 60000, "capacity 60k");
        helper.assertValueEqual(reactor.getHeat(), 0, "hull heat");
        helper.succeed();
    }

    /** {@code TNT/KCK/TNT} with an unused 10k coolant cell; a worn cell does not match. */
    @GameTest(template = EMPTY)
    public static void nakCoolantCellRecipe(GameTestHelper helper) {
        ItemStack tin = new ItemStack(Ic2Items.TIN_INGOT.get());
        ItemStack sodium = new ItemStack(GtItems.SODIUM_CELL.get());
        ItemStack potassium = new ItemStack(GtItems.POTASSIUM_CELL.get());
        ItemStack fresh = new ItemStack(Ic2Items.COOLANT_CELL_10K.get());
        Optional<ItemStack> result = craft(helper, 3, List.of(tin, sodium, tin, potassium, fresh, potassium, tin,
                sodium, tin));
        helper.assertTrue(result.isPresent() && result.get().is(GtItems.NAK_COOLANT_CELL_60K.get()), "60k NaK cell");
        Optional<ItemStack> rotated = craft(helper, 3, List.of(tin, potassium, tin, sodium, fresh, sodium, tin,
                potassium, tin));
        helper.assertTrue(rotated.isPresent() && rotated.get().is(GtItems.NAK_COOLANT_CELL_60K.get()), "rotated");
        ItemStack worn = fresh.copy();
        worn.setDamageValue(1);
        helper.assertTrue(craft(helper, 3, List.of(tin, sodium, tin, potassium, worn, potassium, tin, sodium, tin))
                .isEmpty(), "worn coolant cell does not match");
        helper.succeed();
    }

    /** GT: reactor plating + lead plate → containment reactor plating (the IC2 recipe stays). */
    @GameTest(template = EMPTY)
    public static void containmentPlatingFromLead(GameTestHelper helper) {
        Optional<ItemStack> result = craft(helper, 2, List.of(new ItemStack(Ic2Items.REACTOR_PLATING.get()),
                new ItemStack(GtItems.LEAD_PLATE.get())));
        helper.assertTrue(result.isPresent() && result.get().is(Ic2Items.CONTAINMENT_REACTOR_PLATING.get()),
                "containment reactor plating");
        helper.succeed();
    }

    /** Sodium from sodalite dust, potassium from ender pearl dust (GT electrolyzer, not shapeless). */
    @GameTest(template = EMPTY)
    public static void sodiumAndPotassiumElectrolysis(GameTestHelper helper) {
        GtRecipe sodalite = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.ELECTROLYZER,
                new ItemStack(GtItems.SODALITE_DUST.get(), 23), new ItemStack(Ic2Items.EMPTY_CELL.get(), 8), false);
        helper.assertTrue(sodalite != null && sodalite.getOutput(0).is(GtItems.SODIUM_CELL.get())
                && sodalite.getOutput(0).getCount() == 4 && sodalite.getOutput(3).is(GtItems.CHLORITE_CELL.get())
                && sodalite.getDuration() == 1350 && sodalite.getEuPerTick() == 90, "sodalite electrolysis");
        GtRecipe ender = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.ELECTROLYZER,
                new ItemStack(GtItems.ENDER_PEARL_DUST.get(), 16), new ItemStack(Ic2Items.EMPTY_CELL.get(), 16), false);
        helper.assertTrue(ender != null && ender.getOutput(2).is(GtItems.POTASSIUM_CELL.get())
                && ender.getOutput(2).getCount() == 4 && ender.getOutput(0).getCount() == 5
                && ender.getDuration() == 1300 && ender.getEuPerTick() == 50, "ender pearl dust electrolysis");
        helper.assertTrue(GtRecipe.find(helper.getLevel(), GtRecipe.Kind.ELECTROLYZER,
                new ItemStack(GtItems.SODALITE_DUST.get(), 22), new ItemStack(Ic2Items.EMPTY_CELL.get(), 8),
                false) == null, "not enough sodalite");
        helper.assertValueEqual(MassFabricatorBlockEntity.getAmplifierValue(
                new ItemStack(GtItems.ENDER_PEARL_DUST.get())), 50000, "ender pearl dust amplifier");
        helper.succeed();
    }

    /** Lead chain: 2 silver dust → small lead dust (centrifuge); lead ingot → lead plate (bender). */
    @GameTest(template = EMPTY)
    public static void leadChain(GameTestHelper helper) {
        GtRecipe silver = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.CENTRIFUGE,
                new ItemStack(Ic2Items.SILVER_DUST.get(), 2), ItemStack.EMPTY, false);
        helper.assertTrue(silver != null && silver.getOutput(0).is(GtItems.SMALL_LEAD_DUST.get()), "silver → lead");
        GtRecipe lead = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.CENTRIFUGE,
                new ItemStack(GtItems.LEAD_DUST.get(), 2), ItemStack.EMPTY, false);
        helper.assertTrue(lead != null && lead.getOutput(0).is(GtItems.SMALL_SILVER_DUST.get()), "lead → silver");
        GtRecipe plate = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.BENDER,
                new ItemStack(GtItems.LEAD_INGOT.get()), ItemStack.EMPTY, false);
        helper.assertTrue(plate != null && plate.getOutput(0).is(GtItems.LEAD_PLATE.get()), "lead plate");
        ItemStack small = new ItemStack(GtItems.SMALL_LEAD_DUST.get());
        Optional<ItemStack> dust = craft(helper, 2, List.of(small, small, small, small));
        helper.assertTrue(dust.isPresent() && dust.get().is(GtItems.LEAD_DUST.get()), "4 small → lead dust");
        helper.succeed();
    }

    private static Optional<ItemStack> craft(GameTestHelper helper, int width, List<ItemStack> items) {
        CraftingInput input = CraftingInput.of(width, items.size() / width, items);
        Optional<RecipeHolder<CraftingRecipe>> recipe = helper.getLevel().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        return recipe.map(holder -> holder.value().assemble(input, helper.getLevel().registryAccess()));
    }
}
