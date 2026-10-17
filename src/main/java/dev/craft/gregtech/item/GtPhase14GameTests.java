package dev.craft.gregtech.item;

import java.util.List;
import java.util.Optional;

import dev.craft.Craft;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.recipe.GtRecipe;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.item.ScrapBoxDrops;
import dev.craft.ic2.recipe.MachineRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Phase 14: GT circuit boards, brass, mortars, sapling/leaves compressor, undamaged iron armor in IC2 recipes. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class GtPhase14GameTests {
    private static final String EMPTY = "empty_8x4x8";

    private GtPhase14GameTests() {
    }

    /** Electrum plate, circuit boards and their assembler chain; scrap box weights. */
    @GameTest(template = EMPTY)
    public static void circuitBoards(GameTestHelper helper) {
        GtRecipe plate = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.BENDER,
                new ItemStack(GtItems.ELECTRUM_INGOT.get()), ItemStack.EMPTY, false);
        helper.assertTrue(plate != null && plate.getOutput(0).is(GtItems.ELECTRUM_PLATE.get()), "electrum plate");
        assertAssembles(helper, new ItemStack(GtItems.ELECTRUM_PLATE.get(), 2),
                new ItemStack(GtItems.REFINED_IRON_PLATE.get()), GtItems.BASIC_CIRCUIT_BOARD.get(), 2, 800);
        assertAssembles(helper, new ItemStack(GtItems.SILICON_PLATE.get()),
                new ItemStack(GtItems.ELECTRUM_PLATE.get(), 4), GtItems.ADVANCED_CIRCUIT_BOARD.get(), 2, 1600);
        assertAssembles(helper, new ItemStack(Items.LAPIS_LAZULI), new ItemStack(Items.GLOWSTONE_DUST),
                GtItems.ADVANCED_CIRCUIT_PARTS.get(), 2, 800);
        assertAssembles(helper, new ItemStack(GtItems.ADVANCED_CIRCUIT_BOARD.get()),
                new ItemStack(GtItems.ADVANCED_CIRCUIT_PARTS.get(), 2), Ic2Items.ADVANCED_CIRCUIT.get(), 1, 1600);
        ItemStack cable = new ItemStack(Ic2Items.cable(CableType.INSULATED_COPPER).get(), 3);
        assertAssembles(helper, new ItemStack(GtItems.BASIC_CIRCUIT_BOARD.get()), cable,
                Ic2Items.ELECTRONIC_CIRCUIT.get(), 1, 800);
        assertAssembles(helper, new ItemStack(Ic2Items.ELECTRONIC_CIRCUIT.get()), cable.copyWithCount(1),
                Ic2Items.FREQUENCY_TRANSMITTER.get(), 1, 800);
        assertWeight(helper, GtItems.ADVANCED_CIRCUIT_PARTS.get(), 120);
        assertWeight(helper, GtItems.BASIC_CIRCUIT_BOARD.get(), 180);
        assertWeight(helper, GtItems.ADVANCED_CIRCUIT_BOARD.get(), 40);
        helper.succeed();
    }

    /** Brass dust from copper + zinc, ingot, plate, mixed metal with brass, centrifuge, scrap box. */
    @GameTest(template = EMPTY)
    public static void brass(GameTestHelper helper) {
        ItemStack copper = new ItemStack(Ic2Items.COPPER_DUST.get());
        ItemStack dust = craft(helper, 2, List.of(copper, copper, copper, new ItemStack(GtItems.ZINC_DUST.get())))
                .orElse(ItemStack.EMPTY);
        helper.assertTrue(dust.is(GtItems.BRASS_DUST.get()) && dust.getCount() == 4, "4 brass dust");
        var smelted = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING,
                new SingleRecipeInput(new ItemStack(GtItems.BRASS_DUST.get())), helper.getLevel());
        helper.assertTrue(smelted.isPresent() && smelted.get().value()
                .getResultItem(helper.getLevel().registryAccess()).is(GtItems.BRASS_INGOT.get()), "brass ingot");
        GtRecipe plate = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.BENDER,
                new ItemStack(GtItems.BRASS_INGOT.get()), ItemStack.EMPTY, false);
        helper.assertTrue(plate != null && plate.getOutput(0).is(GtItems.BRASS_PLATE.get()), "brass plate");
        ItemStack mixed = craft(helper, 1, List.of(new ItemStack(GtItems.REFINED_IRON_PLATE.get()),
                new ItemStack(GtItems.BRASS_PLATE.get()), new ItemStack(GtItems.TIN_PLATE.get())))
                .orElse(ItemStack.EMPTY);
        helper.assertTrue(mixed.is(Ic2Items.MIXED_METAL_INGOT.get()) && mixed.getCount() == 1, "mixed metal");
        GtRecipe centrifuge = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.CENTRIFUGE,
                new ItemStack(GtItems.BRASS_DUST.get()), ItemStack.EMPTY, false);
        helper.assertTrue(centrifuge != null && centrifuge.getOutput(0).getCount() == 3
                && centrifuge.getOutput(1).is(GtItems.SMALL_ZINC_DUST.get()) && centrifuge.getDuration() == 1500,
                "brass centrifuge");
        assertWeight(helper, GtItems.BRASS_DUST.get(), 50);
        helper.succeed();
    }

    /** Iron mortar: 64 uses, then iron dust; flint mortar is used up and grinds fewer ingots; flour. */
    @GameTest(template = EMPTY)
    public static void mortars(GameTestHelper helper) {
        ItemStack mortar = new ItemStack(GtItems.IRON_MORTAR.get());
        List<ItemStack> grid = List.of(mortar, new ItemStack(Items.IRON_INGOT));
        helper.assertTrue(craft(helper, 2, grid).orElse(ItemStack.EMPTY).is(Ic2Items.IRON_DUST.get()), "iron dust");
        ItemStack used = remaining(helper, grid).get(0);
        helper.assertTrue(used.is(GtItems.IRON_MORTAR.get()) && used.getDamageValue() == 1, "mortar damage +1");
        ItemStack worn = mortar.copy();
        worn.setDamageValue(63);
        helper.assertTrue(remaining(helper, List.of(worn, new ItemStack(Items.GRAVEL))).get(0)
                .is(Ic2Items.IRON_DUST.get()), "64th use breaks into iron dust");
        helper.assertTrue(craft(helper, 2, List.of(worn, new ItemStack(Items.GRAVEL))).orElse(ItemStack.EMPTY)
                .is(Items.FLINT), "gravel → flint");
        ItemStack flint = new ItemStack(GtItems.FLINT_MORTAR.get());
        helper.assertTrue(craft(helper, 2, List.of(flint, new ItemStack(Items.IRON_INGOT))).isEmpty(),
                "flint mortar cannot grind iron");
        List<ItemStack> wheat = List.of(flint, new ItemStack(Items.WHEAT));
        helper.assertTrue(craft(helper, 2, wheat).orElse(ItemStack.EMPTY).is(GtItems.FLOUR.get()), "flour");
        helper.assertTrue(remaining(helper, wheat).get(0).isEmpty(), "flint mortar used up");
        helper.assertTrue(MachineRecipe.getOutputFor(helper.getLevel(), MachineRecipe.Kind.MACERATOR,
                new ItemStack(Items.WHEAT), false).is(GtItems.FLOUR.get()), "macerator wheat");
        var bread = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING,
                new SingleRecipeInput(new ItemStack(GtItems.FLOUR.get())), helper.getLevel());
        helper.assertTrue(bread.isPresent() && bread.get().value()
                .getResultItem(helper.getLevel().registryAccess()).is(Items.BREAD), "flour → bread");
        helper.succeed();
    }

    /** GT compressor: 4 saplings or 8 leaves → compressed plant ball, rubber leaves excluded. */
    @GameTest(template = EMPTY)
    public static void plantBallCompression(GameTestHelper helper) {
        assertCompresses(helper, new ItemStack(Items.BIRCH_SAPLING, 4), true);
        assertCompresses(helper, new ItemStack(Items.BIRCH_SAPLING, 3), false);
        assertCompresses(helper, new ItemStack(Items.OAK_LEAVES, 8), true);
        assertCompresses(helper, new ItemStack(Ic2Items.RUBBER_LEAVES.get(), 8), false);
        helper.succeed();
    }

    /** Solar helmet and static boots only accept undamaged iron armor. */
    @GameTest(template = EMPTY)
    public static void undamagedIronArmor(GameTestHelper helper) {
        ItemStack cable = new ItemStack(Ic2Items.cable(CableType.INSULATED_COPPER).get());
        ItemStack solar = new ItemStack(Ic2Items.machine(Ic2Machine.SOLAR_PANEL).get());
        ItemStack helmet = new ItemStack(Items.IRON_HELMET);
        helper.assertTrue(craft(helper, 3, List.of(ItemStack.EMPTY, helmet, ItemStack.EMPTY, ItemStack.EMPTY, solar,
                ItemStack.EMPTY, cable, cable, cable)).orElse(ItemStack.EMPTY).is(Ic2Items.SOLAR_HELMET.get()),
                "new helmet");
        ItemStack damaged = helmet.copy();
        damaged.setDamageValue(1);
        helper.assertTrue(craft(helper, 3, List.of(ItemStack.EMPTY, damaged, ItemStack.EMPTY, ItemStack.EMPTY, solar,
                ItemStack.EMPTY, cable, cable, cable)).isEmpty(), "damaged helmet rejected");
        ItemStack boots = new ItemStack(Items.IRON_BOOTS);
        boots.setDamageValue(1);
        helper.assertTrue(craft(helper, 3, List.of(ItemStack.EMPTY, boots, ItemStack.EMPTY, ItemStack.EMPTY,
                new ItemStack(Items.WHITE_WOOL), ItemStack.EMPTY, cable, cable, cable)).isEmpty(),
                "damaged boots rejected");
        helper.succeed();
    }

    private static void assertCompresses(GameTestHelper helper, ItemStack input, boolean expected) {
        ItemStack result = MachineRecipe.getOutputFor(helper.getLevel(), MachineRecipe.Kind.COMPRESSOR, input, false);
        helper.assertTrue(result.is(Ic2Items.COMPRESSED_PLANT_BALL.get()) == expected,
                "compressor " + input + " → " + result);
    }

    private static void assertAssembles(GameTestHelper helper, ItemStack input1, ItemStack input2, Item output,
                                        int count, int duration) {
        GtRecipe recipe = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.ASSEMBLER, input1, input2, true);
        helper.assertTrue(recipe != null && recipe.getOutput(0).is(output) && recipe.getOutput(0).getCount() == count
                && recipe.getDuration() == duration, "assembler " + input1 + " + " + input2 + " → " + output);
    }

    private static void assertWeight(GameTestHelper helper, Item item, int weight) {
        int found = ScrapBoxDrops.all().stream().filter(drop -> drop.stack().is(item))
                .mapToInt(ScrapBoxDrops.Drop::weight).sum();
        helper.assertValueEqual(found, weight, "scrap box weight of " + item);
    }

    private static Optional<RecipeHolder<CraftingRecipe>> recipe(GameTestHelper helper, CraftingInput input) {
        return helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
    }

    private static Optional<ItemStack> craft(GameTestHelper helper, int width, List<ItemStack> items) {
        CraftingInput input = CraftingInput.of(width, items.size() / width, items);
        return recipe(helper, input).map(holder -> holder.value().assemble(input, helper.getLevel().registryAccess()));
    }

    private static NonNullList<ItemStack> remaining(GameTestHelper helper, List<ItemStack> items) {
        CraftingInput input = CraftingInput.of(items.size(), 1, items);
        return recipe(helper, input).map(holder -> holder.value().getRemainingItems(input))
                .orElseThrow(() -> new IllegalStateException("no recipe for " + items));
    }
}
