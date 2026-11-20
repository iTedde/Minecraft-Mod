package ivorius.psychedelicraft.test;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.entity.PSBoats;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Smoke tests for the NeoForge port (Phase 52.1): registries, data, capabilities.
 */
@GameTestHolder(Psychedelicraft.DEFAULT_NAMESPACE)
@PrefixGameTestTemplate(false)
public final class PortGameTests {
    private static final String EMPTY = "empty_16x8x16";

    private PortGameTests() {
    }

    private static long countIn(net.minecraft.core.Registry<?> registry) {
        return registry.keySet().stream().filter(id -> id.getNamespace().equals(Psychedelicraft.DEFAULT_NAMESPACE)).count();
    }

    @GameTest(template = EMPTY)
    public static void registriesArePopulated(GameTestHelper helper) {
        long blocks = countIn(BuiltInRegistries.BLOCK);
        long items = countIn(BuiltInRegistries.ITEM);
        long drugs = DrugType.REGISTRY.size();
        long fluids = SimpleFluid.REGISTRY.size();
        helper.assertTrue(blocks >= 90, "expected >= 90 blocks, got " + blocks);
        helper.assertTrue(items >= 115, "expected >= 115 items, got " + items);
        helper.assertTrue(drugs >= 20, "expected >= 20 drug types, got " + drugs);
        helper.assertTrue(fluids >= 36, "expected >= 36 fluids, got " + fluids);
        helper.assertTrue(PSBoats.juniper().getName().equals("psychedelicraft:juniper"), "juniper boat type missing");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void recipesAreLoaded(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager().getRecipes();
        long own = recipes.stream().map(RecipeHolder::id).filter(id -> id.getNamespace().equals(Psychedelicraft.DEFAULT_NAMESPACE)).count();
        helper.assertTrue(own >= 120, "expected >= 120 Psychedelicraft recipes, got " + own);
        var placed = helper.getLevel().registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        helper.assertTrue(placed.containsKey(Psychedelicraft.id("cannabis_tilled_patch_checked")), "placed feature missing");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void blockEntitiesExist(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, PSBlocks.DRYING_TABLE);
        helper.assertTrue(helper.getBlockEntity(pos) != null, "drying table has no block entity");
        helper.setBlock(pos.east(2), PSBlocks.MASH_TUB);
        helper.assertTrue(helper.getBlockEntity(pos.east(2)) != null, "mash tub has no block entity");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void containerItemsExposeFluidCapability(GameTestHelper helper) {
        ItemStack mug = ItemFluids.set(PSItems.WOODEN_MUG.getDefaultInstance(), PSFluids.COFFEE.getDefaultStack(FluidVolumes.MUG));
        var handler = mug.getCapability(Capabilities.FluidHandler.ITEM);
        helper.assertTrue(handler != null, "wooden mug has no fluid capability");
        helper.assertTrue(handler.getFluidInTank(0).getAmount() == FluidVolumes.MUG / 81, "unexpected amount " + handler.getFluidInTank(0).getAmount());
        var drained = handler.drain(100, FluidAction.EXECUTE);
        helper.assertTrue(drained.getAmount() == 100, "drained " + drained.getAmount());
        helper.assertTrue(ItemFluids.of(handler.getContainer()).amount() == FluidVolumes.MUG - 100 * 81, "mug contents not reduced");
        helper.succeed();
    }
}
