package dev.craft.ic2.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the mass fabricator and the fixed-position UU matter recipes (phase 6.4). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class MassFabricatorGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private MassFabricatorGameTests() {
    }

    private static MassFabricatorBlockEntity place(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, Ic2Blocks.machine(Ic2Machine.MASS_FABRICATOR).get());
        return helper.getBlockEntity(pos);
    }

    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void scrapMultipliesEnergy(GameTestHelper helper) {
        MassFabricatorBlockEntity fab = place(helper, new BlockPos(2, 1, 2));
        fab.getInventory().set(MassFabricatorBlockEntity.SLOT_AMPLIFIER, new ItemStack(Ic2Items.SCRAP.get(), 2));
        // Without energy the amplifier stays in its slot.
        helper.runAfterDelay(2, () -> {
            helper.assertValueEqual(fab.getScrap(), 0, "scrap without energy");
            fab.injectEnergy(Direction.EAST, 100);
            helper.assertValueEqual(fab.getEnergy(), 100, "energy without scrap");
        });
        // One item per tick, and only while scrap < 1000.
        helper.runAfterDelay(6, () -> {
            helper.assertValueEqual(fab.getScrap(), 5000, "scrap value");
            helper.assertValueEqual(fab.getInventory().get(MassFabricatorBlockEntity.SLOT_AMPLIFIER).getCount(), 1,
                    "amplifiers left");
            fab.injectEnergy(Direction.EAST, 512);
            helper.assertValueEqual(fab.getEnergy(), 100 + 6 * 512, "energy with scrap");
            helper.assertValueEqual(fab.getScrap(), 5000 - 512, "scrap used");
            helper.assertBlockProperty(new BlockPos(2, 1, 2), MassFabricatorBlock.SCRAP, true);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void producesUuMatter(GameTestHelper helper) {
        MassFabricatorBlockEntity fab = place(helper, new BlockPos(2, 1, 2));
        fab.setEnergy(MassFabricatorBlockEntity.ENERGY_PER_MATTER + 5);
        helper.runAfterDelay(3, () -> {
            ItemStack output = fab.getInventory().get(MassFabricatorBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(output.is(Ic2Items.UU_MATTER.get()) && output.getCount() == 1, "one UU matter");
            helper.assertValueEqual(fab.getEnergy(), 5, "energy left");
            helper.assertValueEqual(fab.demandsEnergy(), MassFabricatorBlockEntity.MAX_ENERGY - 5, "demand");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, batch = "mass_fabricator_overvoltage")
    public static void explodesOnOvervoltage(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        MassFabricatorBlockEntity fab = place(helper, pos);
        helper.runAfterDelay(1, () -> {
            helper.assertValueEqual(fab.injectEnergy(Direction.EAST, 2048), 0, "returned energy");
            helper.assertBlockNotPresent(Ic2Blocks.machine(Ic2Machine.MASS_FABRICATOR).get(), pos);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void uuRecipesNeedTheirPlace(GameTestHelper helper) {
        // Single UU matter: centre → stone, top left → spruce log, bottom centre → nothing.
        helper.assertTrue(result(helper, 4).filter(stack -> stack.is(Items.STONE) && stack.getCount() == 16)
                .isPresent(), "centre gives 16 stone");
        helper.assertTrue(result(helper, 0).filter(stack -> stack.is(Items.SPRUCE_LOG)).isPresent(),
                "top left gives spruce log");
        helper.assertTrue(result(helper, 7).isEmpty(), "bottom centre gives nothing");
        helper.succeed();
    }

    private static Optional<ItemStack> result(GameTestHelper helper, int slot) {
        List<ItemStack> grid = new ArrayList<>(Collections.nCopies(9, ItemStack.EMPTY));
        grid.set(slot, new ItemStack(Ic2Items.UU_MATTER.get()));
        CraftingInput input = CraftingInput.ofPositioned(3, 3, grid).input();
        return helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel())
                .map(RecipeHolder::value)
                .map((CraftingRecipe recipe) -> recipe.assemble(input, helper.getLevel().registryAccess()));
    }
}
