package dev.craft.gravisuite;

import java.util.List;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Phase 27.4: advanced diamond drill, advanced chainsaw, vajra (spec 28 §9.1–9.3). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class GraviSuiteToolGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private GraviSuiteToolGameTests() {
    }

    private static ItemStack charged(ItemStack stack, int charge) {
        ElectricItems.charge(stack, charge, Integer.MAX_VALUE, true, false);
        return stack;
    }

    /** Efficiency 35 on the drill's blocks, 160 EU per block, 1.0 below 160 EU. */
    @GameTest(template = EMPTY)
    public static void advancedDrill(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack drill = charged(GsItems.ADVANCED_DIAMOND_DRILL.toStack(), 200);
        helper.assertValueEqual(drill.getDestroySpeed(Blocks.STONE.defaultBlockState()), 35.0F, "speed on stone");
        helper.assertValueEqual(drill.getDestroySpeed(Blocks.OAK_LOG.defaultBlockState()), 1.0F, "speed on wood");
        helper.assertTrue(drill.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()), "obsidian harvest");
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        drill.mineBlock(helper.getLevel(), Blocks.STONE.defaultBlockState(), pos, player);
        helper.assertValueEqual(ElectricItems.getCharge(drill), 40, "160 per block");
        helper.assertValueEqual(drill.getDestroySpeed(Blocks.STONE.defaultBlockState()), 1.0F, "speed when empty");
        helper.succeed();
    }

    /** Efficiency 35, wool/melon/cactus mineable, 100 EU per block; a hit costs 200 EU for 13 damage. */
    @GameTest(template = EMPTY)
    public static void advancedChainsaw(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack saw = charged(GsItems.ADVANCED_CHAINSAW.toStack(), 1000);
        for (var block : List.of(Blocks.OAK_LOG, Blocks.WHITE_WOOL, Blocks.MELON, Blocks.CACTUS, Blocks.SNOW)) {
            helper.assertValueEqual(saw.getDestroySpeed(block.defaultBlockState()), 35.0F, "speed on " + block);
        }
        helper.assertValueEqual(saw.getDestroySpeed(Blocks.STONE.defaultBlockState()), 1.0F, "speed on stone");
        saw.mineBlock(helper.getLevel(), Blocks.OAK_LOG.defaultBlockState(), helper.absolutePos(BlockPos.ZERO),
                player);
        helper.assertValueEqual(ElectricItems.getCharge(saw), 900, "100 per block");
        IronGolem golem = helper.spawn(EntityType.IRON_GOLEM, new BlockPos(3, 1, 3));
        saw.hurtEnemy(golem, player);
        helper.assertValueEqual(golem.getHealth(), golem.getMaxHealth() - 13.0F, "13 damage");
        helper.assertValueEqual(ElectricItems.getCharge(saw), 700, "hit costs 2 × 100");
        golem.discard();
        helper.succeed();
    }

    /** Efficiency 20 000 on every block (0.5 when empty), 3 333 EU also at hardness 0, hits 6 666 EU for 25. */
    @GameTest(template = EMPTY)
    public static void vajraMining(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack vajra = charged(GsItems.VAJRA.toStack(), 10000);
        helper.assertValueEqual(vajra.getDestroySpeed(Blocks.OBSIDIAN.defaultBlockState()), 20000.0F, "obsidian");
        helper.assertValueEqual(vajra.getDestroySpeed(Blocks.OAK_LOG.defaultBlockState()), 20000.0F, "wood");
        helper.assertTrue(vajra.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()), "harvests all");
        vajra.mineBlock(helper.getLevel(), Blocks.TORCH.defaultBlockState(), helper.absolutePos(BlockPos.ZERO),
                player);
        helper.assertValueEqual(ElectricItems.getCharge(vajra), 10000 - 3333, "hardness 0 costs too");
        IronGolem golem = helper.spawn(EntityType.IRON_GOLEM, new BlockPos(3, 1, 3));
        vajra.hurtEnemy(golem, player);
        helper.assertValueEqual(golem.getHealth(), golem.getMaxHealth() - 25.0F, "25 damage");
        helper.assertValueEqual(ElectricItems.getCharge(vajra), 1, "hit costs 6 666");
        helper.assertValueEqual(vajra.getDestroySpeed(Blocks.STONE.defaultBlockState()), 0.5F, "empty 0.5");
        golem.discard();
        helper.succeed();
    }

    /** Precision mode removes a block with its drops for 3 333 EU; bedrock and barrier stay, no charge no effect. */
    @GameTest(template = EMPTY)
    public static void vajraPrecisionMode(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack vajra = charged(GsItems.VAJRA.toStack(), 3333 + 100);
        player.setItemInHand(InteractionHand.MAIN_HAND, vajra);
        BlockPos stone = new BlockPos(1, 1, 1);
        BlockPos bedrock = new BlockPos(2, 1, 1);
        BlockPos barrier = new BlockPos(3, 1, 1);
        helper.setBlock(stone, Blocks.STONE);
        helper.setBlock(bedrock, Blocks.BEDROCK);
        helper.setBlock(barrier, Blocks.BARRIER);
        vajra.useOn(click(helper, player, bedrock));
        vajra.useOn(click(helper, player, barrier));
        helper.assertBlockPresent(Blocks.BEDROCK, bedrock);
        helper.assertBlockPresent(Blocks.BARRIER, barrier);
        vajra.useOn(click(helper, player, stone));
        helper.assertBlockPresent(Blocks.AIR, stone);
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(helper.absolutePos(stone)));
        helper.assertTrue(drops.size() == 1 && drops.get(0).getItem().is(Items.COBBLESTONE), "cobblestone dropped");
        helper.assertValueEqual(ElectricItems.getCharge(vajra), 100, "3 333 used");
        helper.setBlock(stone, Blocks.STONE);
        vajra.useOn(click(helper, player, stone));
        helper.assertBlockPresent(Blocks.STONE, stone);
        drops.forEach(ItemEntity::discard);
        helper.succeed();
    }

    /** Drill and chainsaw keep the charge of the IC2 tool; vajra from its parts. */
    @GameTest(template = EMPTY)
    public static void recipes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack oc = Ic2Items.OVERCLOCKER_UPGRADE.toStack();
        ItemStack circuit = Ic2Items.ADVANCED_CIRCUIT.toStack();
        ItemStack drill = craft(level, 3, 2, List.of(oc, charged(Ic2Items.DIAMOND_DRILL.toStack(), 5000), oc,
                circuit, oc, circuit));
        helper.assertTrue(drill.is(GsItems.ADVANCED_DIAMOND_DRILL.get()), "advanced drill");
        helper.assertValueEqual(ElectricItems.getCharge(drill), 5000, "drill charge kept");
        ItemStack saw = craft(level, 3, 3, List.of(ItemStack.EMPTY, new ItemStack(Items.DIAMOND), ItemStack.EMPTY,
                oc, charged(Ic2Items.CHAINSAW.toStack(), 3000), oc, circuit, oc, circuit));
        helper.assertTrue(saw.is(GsItems.ADVANCED_CHAINSAW.get()), "advanced chainsaw");
        helper.assertValueEqual(ElectricItems.getCharge(saw), 3000, "chainsaw charge kept");
        ItemStack iron = Ic2Items.REFINED_IRON_INGOT.toStack();
        ItemStack carbon = Ic2Items.CARBON_PLATE.toStack();
        ItemStack alloy = Ic2Items.ADVANCED_ALLOY.toStack();
        ItemStack vajra = craft(level, 3, 3, List.of(iron, Ic2Items.ENERGY_CRYSTAL.toStack(), iron, carbon,
                GsItems.VAJRA_CORE.toStack(), carbon, alloy, Ic2Items.LAPOTRON_CRYSTAL.toStack(), alloy));
        helper.assertTrue(vajra.is(GsItems.VAJRA.get()), "vajra");
        helper.succeed();
    }

    private static UseOnContext click(GameTestHelper helper, Player player, BlockPos relative) {
        BlockPos pos = helper.absolutePos(relative);
        return new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    private static ItemStack craft(ServerLevel level, int width, int height, List<ItemStack> grid) {
        CraftingInput input = CraftingInput.of(width, height, grid);
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
                .map(recipe -> recipe.value().assemble(input, level.registryAccess()))
                .orElse(ItemStack.EMPTY);
    }
}
