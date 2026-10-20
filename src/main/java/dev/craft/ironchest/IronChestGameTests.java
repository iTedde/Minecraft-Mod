package dev.craft.ironchest;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Items;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

/** Iron chests (spec 22 §7). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class IronChestGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(3, 1, 3);

    private IronChestGameTests() {
    }

    private static IronChestBlockEntity place(GameTestHelper helper, IronChestType type, BlockPos pos) {
        helper.setBlock(pos, IronChestBlocks.chest(type).get().defaultBlockState()
                .setValue(IronChestBlock.FACING, Direction.EAST));
        return (IronChestBlockEntity) helper.getBlockEntity(pos);
    }

    /** Applies {@code upgrade} like a right click on the chest at {@code pos}; returns the remaining stack. */
    private static ItemStack useUpgrade(GameTestHelper helper, ChestUpgradeType upgrade, BlockPos pos) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack stack = IronChestItems.upgrade(upgrade).toStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos absolute = helper.absolutePos(pos);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
        stack.getItem().onItemUseFirst(stack, context);
        return stack;
    }

    @GameTest(template = EMPTY)
    public static void chestsHaveTheirSizes(GameTestHelper helper) {
        int[] sizes = {54, 81, 108, 45, 72, 108, 108};
        for (IronChestType type : IronChestType.values()) {
            IronChestBlockEntity chest = place(helper, type, POS);
            helper.assertTrue(chest.getContainerSize() == sizes[type.ordinal()], type + " size");
            Inventory inventory = helper.makeMockPlayer(GameType.SURVIVAL).getInventory();
            IronChestMenu menu = new IronChestMenu(0, inventory, chest, type);
            helper.assertTrue(menu.slots.size() == sizes[type.ordinal()] + 36, type + " menu slots");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void vanillaChestUpgradesToIronKeepingContents(GameTestHelper helper) {
        BlockPos other = POS.east();
        BlockState left = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH);
        helper.setBlock(POS, left.setValue(ChestBlock.TYPE, ChestType.LEFT));
        helper.setBlock(other, left.setValue(ChestBlock.TYPE, ChestType.RIGHT));
        ChestBlockEntity vanilla = (ChestBlockEntity) helper.getBlockEntity(POS);
        for (int i = 0; i < 27; i++) {
            vanilla.setItem(i, new ItemStack(Items.COBBLESTONE, i + 1));
        }
        ((ChestBlockEntity) helper.getBlockEntity(other)).setItem(0, new ItemStack(Items.DIRT, 5));

        ItemStack rest = useUpgrade(helper, ChestUpgradeType.WOOD_IRON, POS);
        helper.assertTrue(rest.isEmpty(), "upgrade used up");
        helper.assertBlockPresent(IronChestBlocks.chest(IronChestType.IRON).get(), POS);
        helper.assertBlockProperty(POS, IronChestBlock.FACING, Direction.NORTH);
        IronChestBlockEntity chest = (IronChestBlockEntity) helper.getBlockEntity(POS);
        for (int i = 0; i < 27; i++) {
            helper.assertTrue(chest.getItem(i).getCount() == i + 1, "slot " + i + " kept");
        }
        helper.assertBlockProperty(other, ChestBlock.TYPE, ChestType.SINGLE);
        helper.assertTrue(((ChestBlockEntity) helper.getBlockEntity(other)).getItem(0).is(Items.DIRT),
                "other half keeps its contents");
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(POS))
                .inflate(3)).isEmpty(), "nothing dropped");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void upgradeKeepsContentsAndRefusesWrongChests(GameTestHelper helper) {
        IronChestBlockEntity chest = place(helper, IronChestType.IRON, POS);
        chest.setItem(53, new ItemStack(Items.DIAMOND, 7));

        ItemStack wrong = useUpgrade(helper, ChestUpgradeType.GOLD_DIAMOND, POS);
        helper.assertTrue(wrong.getCount() == 1, "wrong upgrade not used");
        helper.assertBlockPresent(IronChestBlocks.chest(IronChestType.IRON).get(), POS);

        chest.startOpen(helper.makeMockPlayer(GameType.SURVIVAL));
        helper.assertTrue(useUpgrade(helper, ChestUpgradeType.IRON_GOLD, POS).getCount() == 1, "open chest refused");
        chest.stopOpen(helper.makeMockPlayer(GameType.SURVIVAL));

        helper.assertTrue(useUpgrade(helper, ChestUpgradeType.IRON_GOLD, POS).isEmpty(), "upgrade used");
        helper.assertBlockPresent(IronChestBlocks.chest(IronChestType.GOLD).get(), POS);
        helper.assertBlockProperty(POS, IronChestBlock.FACING, Direction.EAST);
        IronChestBlockEntity gold = (IronChestBlockEntity) helper.getBlockEntity(POS);
        helper.assertTrue(gold.getContainerSize() == 81 && gold.getItem(53).getCount() == 7, "contents kept");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void onlyTheObsidianChestSurvivesExplosions(GameTestHelper helper) {
        BlockPos iron = new BlockPos(1, 1, 3);
        BlockPos obsidian = new BlockPos(5, 1, 3);
        place(helper, IronChestType.IRON, iron);
        place(helper, IronChestType.OBSIDIAN, obsidian);
        for (BlockPos pos : List.of(iron, obsidian)) {
            Vec3 center = Vec3.atCenterOf(helper.absolutePos(pos.north()));
            helper.getLevel().explode(null, center.x, center.y, center.z, 4.0F, Level.ExplosionInteraction.TNT);
        }
        helper.assertBlockNotPresent(IronChestBlocks.chest(IronChestType.IRON).get(), iron);
        helper.assertBlockPresent(IronChestBlocks.chest(IronChestType.OBSIDIAN).get(), obsidian);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void breakingDropsChestAndContents(GameTestHelper helper) {
        IronChestBlockEntity chest = place(helper, IronChestType.COPPER, POS);
        chest.setItem(10, new ItemStack(Items.REDSTONE, 40));
        helper.getLevel().destroyBlock(helper.absolutePos(POS), true);
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(helper.absolutePos(POS)).inflate(2));
        int redstone = drops.stream().filter(e -> e.getItem().is(Items.REDSTONE))
                .mapToInt(e -> e.getItem().getCount()).sum();
        helper.assertTrue(redstone == 40, "contents dropped: " + redstone);
        helper.assertTrue(drops.stream().anyMatch(e -> e.getItem().is(IronChestItems.chest(IronChestType.COPPER))),
                "chest dropped");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void crystalChestSortsTopStacks(GameTestHelper helper) {
        IronChestBlockEntity chest = place(helper, IronChestType.CRYSTAL, POS);
        Item[] items = {Items.STONE, Items.DIRT, Items.SAND, Items.GRAVEL, Items.CLAY_BALL, Items.STICK,
                Items.APPLE, Items.BONE, Items.STRING};
        for (int i = 0; i < items.length; i++) {
            chest.setItem(i, new ItemStack(items[i], i + 1));
        }
        chest.setItem(20, new ItemStack(Items.STONE, 64));
        chest.setItem(21, new ItemStack(Items.STONE, 64));
        List<ItemStack> top = chest.getTopStacks();
        helper.assertTrue(top.size() == IronChestType.TOP_STACKS, "eight top stacks: " + top.size());
        helper.assertTrue(top.get(0).is(Items.STONE) && top.get(0).getCount() == 129, "stone summed first");
        helper.assertTrue(top.get(1).is(Items.STRING), "then by count");
        helper.assertTrue(top.stream().noneMatch(stack -> stack.is(Items.DIRT)), "smallest left out");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void itemHandlerAndComparatorWork(GameTestHelper helper) {
        place(helper, IronChestType.SILVER, POS);
        IItemHandler handler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,
                helper.absolutePos(POS), Direction.DOWN);
        helper.assertTrue(handler != null && handler.getSlots() == 72, "72 slots from below");
        handler.insertItem(71, new ItemStack(Items.IRON_INGOT, 64), false);
        BlockState state = helper.getBlockState(POS);
        int signal = state.getAnalogOutputSignal(helper.getLevel(), helper.absolutePos(POS));
        helper.assertTrue(signal == 1, "comparator signal 1: " + signal);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void catsSitOnIronChests(GameTestHelper helper) {
        place(helper, IronChestType.IRON, POS);
        Cat cat = helper.spawn(EntityType.CAT, POS.above());
        helper.assertTrue(cat.goalSelector.getAvailableGoals().stream()
                .anyMatch(goal -> goal.getGoal() instanceof IronChestCatSitGoal), "goal replaced");
        // the cat must path onto the chest like onto a vanilla chest, not into it
        PathType chest = WalkNodeEvaluator.getPathTypeStatic(cat, helper.absolutePos(POS));
        PathType top = WalkNodeEvaluator.getPathTypeStatic(cat, helper.absolutePos(POS.above()));
        helper.assertTrue(chest == PathType.BLOCKED, "chest blocks the path: " + chest);
        helper.assertTrue(top == PathType.WALKABLE, "top of the chest is walkable: " + top);
        helper.succeed();
    }

    /**
     * Every chest and upgrade recipe, laid out with the first item of each ingredient, finds itself – no other recipe
     * takes the pattern. The iron chest also takes refined iron.
     */
    @GameTest(template = EMPTY)
    public static void recipesExistAndDoNotCollide(GameTestHelper helper) {
        Level level = helper.getLevel();
        List<Item> ours = IronChestItems.ITEMS.getEntries().stream().map(holder -> (Item) holder.get()).toList();
        int count = 0;
        for (RecipeHolder<CraftingRecipe> holder : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            if (!(holder.value() instanceof ShapedRecipe shaped)
                    || !ours.contains(shaped.getResultItem(level.registryAccess()).getItem())) {
                continue;
            }
            count++;
            List<ItemStack> grid = new ArrayList<>();
            for (Ingredient ingredient : shaped.getIngredients()) {
                grid.add(ingredient.isEmpty() ? ItemStack.EMPTY : ingredient.getItems()[0].copy());
            }
            CraftingInput input = CraftingInput.of(shaped.getWidth(), shaped.getHeight(), grid);
            Optional<RecipeHolder<CraftingRecipe>> found =
                    level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
            helper.assertTrue(found.isPresent() && found.get().id().equals(holder.id()),
                    holder.id() + " found as " + found.map(RecipeHolder::id).orElse(null));
        }
        helper.assertTrue(count == 20, "11 chest + 9 upgrade recipes: " + count);

        List<ItemStack> refined = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            refined.add(i == 4 ? new ItemStack(Items.CHEST) : Ic2Items.REFINED_IRON_INGOT.toStack());
        }
        Optional<RecipeHolder<CraftingRecipe>> iron = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,
                CraftingInput.of(3, 3, refined), level);
        helper.assertTrue(iron.isPresent() && iron.get().value().getResultItem(level.registryAccess())
                .is(IronChestItems.chest(IronChestType.IRON)), "iron chest from refined iron");
        helper.succeed();
    }
}
