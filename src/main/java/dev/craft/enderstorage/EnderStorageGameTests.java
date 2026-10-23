package dev.craft.enderstorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import dev.craft.Craft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

/** EnderStorage ender chest (spec 27 §8). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class EnderStorageGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos A = new BlockPos(1, 1, 1);
    private static final BlockPos B = new BlockPos(5, 1, 5);

    private EnderStorageGameTests() {
    }

    /** A chest facing north (front towards -z) with {@code frequency}. */
    private static EnderChestBlockEntity chest(GameTestHelper helper, BlockPos pos, Frequency frequency) {
        helper.setBlock(pos, EsBlocks.ENDER_CHEST.get().defaultBlockState()
                .setValue(EnderChestBlock.FACING, Direction.NORTH));
        EnderChestBlockEntity chest = helper.getBlockEntity(pos);
        chest.setFrequency(frequency);
        return chest;
    }

    private static IItemHandler handler(GameTestHelper helper, BlockPos pos) {
        return helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(pos), Direction.UP);
    }

    /** Right click at a point of a north-facing chest, given in the chest's own frame (front = +z). */
    private static void click(GameTestHelper helper, BlockPos pos, Player player, double x, double y, double z) {
        BlockPos absolute = helper.absolutePos(pos);
        // North facing: two quarter turns, (x, z) → (1 - x, 1 - z).
        Vec3 location = new Vec3(absolute.getX() + 1 - x, absolute.getY() + y, absolute.getZ() + 1 - z);
        BlockHitResult hit = new BlockHitResult(location, Direction.UP, absolute, false);
        BlockState state = helper.getLevel().getBlockState(absolute);
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty() || !state.useItemOn(stack, helper.getLevel(), player, InteractionHand.MAIN_HAND, hit)
                .consumesAction() && !player.isSecondaryUseActive()) {
            state.useWithoutItem(helper.getLevel(), player, hit);
        }
    }

    @GameTest(template = EMPTY)
    public static void sameFrequencySharesStorage(GameTestHelper helper) {
        chest(helper, A, Frequency.of(14, 4, 11));
        chest(helper, B, Frequency.of(14, 4, 11));
        chest(helper, new BlockPos(5, 1, 1), Frequency.of(14, 4, 12));
        handler(helper, A).insertItem(0, new ItemStack(Items.DIAMOND, 5), false);
        helper.assertTrue(handler(helper, B).getStackInSlot(0).is(Items.DIAMOND), "same colours share the items");
        helper.assertTrue(handler(helper, new BlockPos(5, 1, 1)).getStackInSlot(0).isEmpty(), "other colours not");
        helper.assertValueEqual(handler(helper, A).getSlots(), EnderItemStorage.SIZE, "27 slots");
        handler(helper, A).extractItem(0, 5, false);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void personalStorageIsSeparate(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        chest(helper, A, Frequency.of(1, 2, 3));
        chest(helper, B, Frequency.of(1, 2, 3).withOwner(player));
        handler(helper, A).insertItem(0, new ItemStack(Items.STICK), false);
        helper.assertTrue(handler(helper, B).getStackInSlot(0).isEmpty(), "personal storage of the same colours");
        handler(helper, A).extractItem(0, 1, false);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void dyeOnButtonChangesItsColour(GameTestHelper helper) {
        EnderChestBlockEntity chest = chest(helper, A, Frequency.GLOBAL);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.RED_DYE, 2));
        // Middle button: x 0.4375–0.5625 on top of the lid.
        click(helper, A, player, 0.5, 0.9375, 0.5);
        helper.assertValueEqual(chest.getFrequency().colour(1), 14, "middle button red");
        helper.assertValueEqual(chest.getFrequency().colour(0), 0, "left button unchanged");
        helper.assertValueEqual(player.getMainHandItem().getCount(), 1, "one dye used");
        click(helper, A, player, 0.5, 0.9375, 0.5);
        helper.assertValueEqual(player.getMainHandItem().getCount(), 1, "same colour uses no dye");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BLUE_DYE));
        click(helper, A, player, 0.3125, 0.9375, 0.5);
        helper.assertValueEqual(chest.getFrequency().colour(0), 11, "left button blue");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void diamondOnLatchMakesChestPersonal(GameTestHelper helper) {
        EnderChestBlockEntity chest = chest(helper, A, Frequency.of(5, 5, 5));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND));
        click(helper, A, player, 0.5, 0.5625, 1.0);
        helper.assertTrue(chest.getFrequency().isPersonal(), "personal");
        helper.assertTrue(chest.getFrequency().owner().equals(Optional.of(player.getUUID())), "owner");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "diamond used");
        player.setShiftKeyDown(true);
        click(helper, A, player, 0.5, 0.5625, 1.0);
        helper.assertFalse(chest.getFrequency().isPersonal(), "sneaking on the latch makes it global");
        helper.assertTrue(player.getInventory().contains(new ItemStack(Items.DIAMOND)), "diamond back");
        helper.assertValueEqual(chest.getFrequency().colours(), Frequency.of(5, 5, 5).colours(), "colours kept");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void chestDropsWithFrequency(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Frequency frequency = Frequency.of(3, 7, 9).withOwner(player);
        EnderChestBlockEntity chest = chest(helper, A, frequency);
        ServerLevel level = helper.getLevel();
        BlockPos absolute = helper.absolutePos(A);
        List<ItemStack> drops = Block.getDrops(level.getBlockState(absolute), level, absolute, chest, player,
                new ItemStack(Items.WOODEN_PICKAXE));
        helper.assertValueEqual(drops.size(), 1, "one drop");
        helper.assertTrue(drops.get(0).is(EsItems.ENDER_CHEST.get())
                && frequency.equals(drops.get(0).get(EsDataComponents.FREQUENCY.get())), "frequency kept");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void recipesColourTheChest(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<ItemStack> grid = new ArrayList<>(List.of(
                new ItemStack(Items.BLAZE_ROD), new ItemStack(Items.RED_WOOL), new ItemStack(Items.BLAZE_ROD),
                new ItemStack(Items.OBSIDIAN), new ItemStack(Items.CHEST), new ItemStack(Items.OBSIDIAN),
                new ItemStack(Items.BLAZE_ROD), new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.BLAZE_ROD)));
        ItemStack red = craft(level, 3, 3, grid);
        helper.assertTrue(red.is(EsItems.ENDER_CHEST.get()), "base recipe");
        helper.assertValueEqual(red.get(EsDataComponents.FREQUENCY.get()).colours(), Frequency.of(14, 14, 14).colours(),
                "red/red/red");
        // Recolour: blue dye left and yellow dye right above the chest.
        ItemStack recoloured = craft(level, 3, 2, List.of(new ItemStack(Items.BLUE_DYE), ItemStack.EMPTY,
                new ItemStack(Items.YELLOW_DYE), ItemStack.EMPTY, red, ItemStack.EMPTY));
        helper.assertValueEqual(recoloured.get(EsDataComponents.FREQUENCY.get()).colours(),
                Frequency.of(11, 14, 4).colours(), "blue/red/yellow");
        ItemStack middle = craft(level, 1, 2, List.of(new ItemStack(Items.LIME_DYE), red));
        helper.assertValueEqual(middle.get(EsDataComponents.FREQUENCY.get()).colours(),
                Frequency.of(14, 5, 14).colours(), "dye straight above sets the middle colour");
        helper.assertTrue(craft(level, 1, 2, List.of(red, new ItemStack(Items.LIME_DYE))).isEmpty(), "dye below");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void pouchTakesChestFrequencyAndOpensItsStorage(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Frequency frequency = Frequency.of(9, 9, 2).withOwner(owner);
        chest(helper, A, frequency);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack pouch = new ItemStack(EsItems.ENDER_POUCH.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, pouch);
        player.setShiftKeyDown(true);
        BlockPos absolute = helper.absolutePos(A);
        pouch.onItemUseFirst(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false)));
        helper.assertTrue(frequency.equals(pouch.get(EsDataComponents.FREQUENCY.get())), "frequency and owner copied");
        player.setShiftKeyDown(false);
        EnderItemStorage storage = EnderStorageData.get(helper.getLevel().getServer()).item(frequency);
        // The menu itself (mock players cannot receive the open-screen payload): opening counts on the storage.
        EnderStorageMenu menu = new EnderStorageMenu(0, player.getInventory(), storage, frequency.ownerName());
        helper.assertValueEqual(storage.getOpenCount(), 1, "open storage counts for the chest lids");
        menu.removed(player);
        helper.assertValueEqual(storage.getOpenCount(), 0, "closed again");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void pouchRecipe(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack pouch = craft(level, 3, 3, List.of(
                new ItemStack(Items.BLAZE_POWDER), new ItemStack(Items.LEATHER), new ItemStack(Items.BLAZE_POWDER),
                new ItemStack(Items.LEATHER), new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.LEATHER),
                new ItemStack(Items.BLAZE_POWDER), new ItemStack(Items.GREEN_WOOL), new ItemStack(Items.BLAZE_POWDER)));
        helper.assertTrue(pouch.is(EsItems.ENDER_POUCH.get()), "pouch");
        helper.assertValueEqual(pouch.get(EsDataComponents.FREQUENCY.get()).colours(), Frequency.of(13, 13, 13).colours(),
                "green/green/green");
        helper.assertTrue(craft(level, 1, 2, List.of(new ItemStack(Items.RED_DYE), pouch)).isEmpty(),
                "no recolouring of pouches");
        helper.succeed();
    }

    private static ItemStack craft(ServerLevel level, int width, int height, List<ItemStack> items) {
        CraftingInput input = CraftingInput.of(width, height, items);
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
                .map(recipe -> recipe.value().assemble(input, level.registryAccess()))
                .orElse(ItemStack.EMPTY);
    }
}
