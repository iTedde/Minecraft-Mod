package dev.craft.redpower;

import dev.craft.Craft;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** RedPower gem tools (spec 19): sickle areas and durability, material values, GregTech recovery. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class RedPowerGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos CENTER = new BlockPos(3, 2, 3);

    private RedPowerGameTests() {
    }

    private static ItemStack sickle() {
        return RpItems.tool(RpGem.RUBY, RpItems.Tool.SICKLE).toStack();
    }

    /** Mines {@code CENTER} (already removed) as the player would and returns the sickle. */
    private static ItemStack mine(GameTestHelper helper, BlockState mined) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack stack = sickle();
        BlockPos pos = helper.absolutePos(CENTER);
        helper.setBlock(CENTER, Blocks.AIR);
        stack.getItem().mineBlock(stack, helper.getLevel(), mined, pos, player);
        return stack;
    }

    @GameTest(template = EMPTY)
    public static void sickleCutsGrassInFiveByFive(GameTestHelper helper) {
        for (int x = 0; x < 7; x++) {
            for (int z = 0; z < 7; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.GRASS_BLOCK);
                helper.setBlock(new BlockPos(x, 2, z), Blocks.SHORT_GRASS);
            }
        }
        BlockPos poppy = CENTER.offset(1, 0, 1);
        BlockPos fern = CENTER.offset(-2, 0, 2);
        helper.setBlock(poppy, Blocks.POPPY);
        helper.setBlock(fern, Blocks.FERN);
        ItemStack stack = mine(helper, Blocks.STONE.defaultBlockState());
        helper.assertBlockPresent(Blocks.AIR, CENTER.offset(2, 0, -2));
        helper.assertBlockPresent(Blocks.AIR, fern);
        helper.assertBlockPresent(Blocks.POPPY, poppy);
        helper.assertBlockPresent(Blocks.SHORT_GRASS, CENTER.offset(3, 0, 0));
        helper.assertTrue(stack.getDamageValue() == 1, "one point of durability per use: " + stack.getDamageValue());
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void sickleCutsLeavesInCube(GameTestHelper helper) {
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int x = 1; x <= 6; x++) {
            for (int y = 1; y <= 3; y++) {
                for (int z = 2; z <= 4; z++) {
                    helper.setBlock(new BlockPos(x, y, z), leaves);
                }
            }
        }
        ItemStack stack = mine(helper, leaves);
        helper.assertBlockPresent(Blocks.AIR, CENTER.offset(1, 1, 1));
        helper.assertBlockPresent(Blocks.AIR, CENTER.offset(-1, -1, -1));
        helper.assertBlockPresent(Blocks.OAK_LEAVES, CENTER.offset(2, 0, 0));
        helper.assertTrue(stack.getDamageValue() == 1, "one point of durability per use: " + stack.getDamageValue());
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void sickleWithoutTargetsTakesNoDamage(GameTestHelper helper) {
        ItemStack stack = mine(helper, Blocks.STONE.defaultBlockState());
        helper.assertTrue(stack.getDamageValue() == 0, "nothing cut, no damage");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void gemToolValues(GameTestHelper helper) {
        for (RpGem gem : RpGem.values()) {
            ItemStack pickaxe = RpItems.tool(gem, RpItems.Tool.PICKAXE).toStack();
            helper.assertTrue(pickaxe.getMaxDamage() == 500, "durability 500");
            helper.assertTrue(pickaxe.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()), "iron level");
            helper.assertTrue(!pickaxe.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()), "not diamond level");
            helper.assertTrue(pickaxe.getDestroySpeed(Blocks.STONE.defaultBlockState()) == 8.0F, "speed 8");
            helper.assertTrue(pickaxe.getEnchantmentValue() == 12, "enchantability 12");
            helper.assertTrue(RpItems.tool(gem, RpItems.Tool.SICKLE).toStack().getEnchantmentValue() == 20,
                    "sickle enchantability 20");
            helper.assertTrue(RpItems.tool(gem, RpItems.Tool.HANDSAW).toStack().getMaxDamage() == 640,
                    "handsaw durability 640");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void gemToolsAreRecoveredInMacerator(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        for (RpGem gem : RpGem.values()) {
            for (RpItems.Tool tool : RpItems.Tool.values()) {
                String name = gem.getName() + "_" + tool.getName();
                boolean present = recipes.byKey(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "macerator/" + name))
                        .isPresent();
                helper.assertTrue(present == (tool.getRecoveredDust() > 0), "macerator recipe for " + name);
                helper.assertTrue(recipes.byKey(ResourceLocation.fromNamespaceAndPath(Craft.MODID, name)).isPresent(),
                        "crafting recipe for " + name);
            }
        }
        helper.succeed();
    }
}
