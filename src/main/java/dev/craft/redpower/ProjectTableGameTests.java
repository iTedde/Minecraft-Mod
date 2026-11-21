package dev.craft.redpower;

import dev.craft.Craft;
import dev.craft.redpower.table.ProjectTableBlockEntity;
import dev.craft.redpower.table.ProjectTableCrafting;
import dev.craft.redpower.table.ProjectTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Project table (spec 20 §1): grid refill from the storage, plans, last use, shift-click crafting, drops. Large
 * project table (spec 34): 27 storage slots in menu, crafting, automation and drops, recipe.
 */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class ProjectTableGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos TABLE = new BlockPos(3, 2, 3);

    private ProjectTableGameTests() {
    }

    private static ProjectTableBlockEntity table(GameTestHelper helper) {
        return table(helper, RpBlocks.PROJECT_TABLE.get());
    }

    private static ProjectTableBlockEntity table(GameTestHelper helper, Block block) {
        helper.setBlock(TABLE, block);
        return (ProjectTableBlockEntity) helper.getBlockEntity(TABLE);
    }

    /** Sticks: two planks on top of each other in grid slots 0 and 3. */
    private static void stickGrid(ItemStackHandler inventory) {
        inventory.setStackInSlot(0, new ItemStack(Items.OAK_PLANKS));
        inventory.setStackInSlot(3, new ItemStack(Items.OAK_PLANKS));
    }

    private static int count(ItemStackHandler inventory, int from, int to) {
        int count = 0;
        for (int i = from; i < to; i++) {
            count += inventory.getStackInSlot(i).getCount();
        }
        return count;
    }

    private static int storage(ItemStackHandler inventory) {
        return count(inventory, ProjectTableBlockEntity.STORAGE_START, inventory.getSlots());
    }

    @GameTest(template = EMPTY)
    public static void gridRefillsFromStorage(GameTestHelper helper) {
        ProjectTableBlockEntity table = table(helper);
        ItemStackHandler inventory = table.getInventory();
        stickGrid(inventory);
        inventory.setStackInSlot(ProjectTableBlockEntity.STORAGE_START + 4, new ItemStack(Items.SPRUCE_PLANKS, 10));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ProjectTableMenu menu = new ProjectTableMenu(0, player.getInventory(), table);
        helper.assertTrue(menu.getSlot(ProjectTableMenu.RESULT).getItem().is(Items.STICK), "sticks offered");
        menu.clicked(ProjectTableMenu.RESULT, 0, ClickType.PICKUP, player);
        helper.assertTrue(menu.getCarried().is(Items.STICK) && menu.getCarried().getCount() == 4, "4 sticks taken");
        helper.assertTrue(inventory.getStackInSlot(0).is(Items.SPRUCE_PLANKS)
                && inventory.getStackInSlot(3).is(Items.SPRUCE_PLANKS), "grid refilled with matching planks");
        helper.assertTrue(storage(inventory) == 8, "two planks from the storage: " + storage(inventory));
        helper.assertTrue(menu.getSlot(ProjectTableMenu.RESULT).getItem().is(Items.STICK), "next craft offered");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void lastUseEmptiesGrid(GameTestHelper helper) {
        ProjectTableBlockEntity table = table(helper);
        ItemStackHandler inventory = table.getInventory();
        stickGrid(inventory);
        helper.assertTrue(ProjectTableCrafting.isLastUse(inventory), "no storage: last use");
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ProjectTableMenu menu = new ProjectTableMenu(0, player.getInventory(), table);
        menu.clicked(ProjectTableMenu.RESULT, 0, ClickType.PICKUP, player);
        helper.assertTrue(count(inventory, 0, ProjectTableBlockEntity.GRID_SIZE) == 0, "grid used up");
        helper.assertTrue(menu.getSlot(ProjectTableMenu.RESULT).getItem().isEmpty(), "no further result");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void planCraftsFromStorageOnly(GameTestHelper helper) {
        ProjectTableBlockEntity table = table(helper);
        ItemStackHandler inventory = table.getInventory();
        stickGrid(inventory);
        inventory.setStackInSlot(ProjectTableBlockEntity.PLAN_SLOT, new ItemStack(RpItems.BLANK_PLAN.get()));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ProjectTableMenu menu = new ProjectTableMenu(0, player.getInventory(), table);
        helper.assertTrue(menu.clickMenuButton(player, ProjectTableMenu.BUTTON_WRITE_PLAN), "plan written");
        ItemStack plan = inventory.getStackInSlot(ProjectTableBlockEntity.PLAN_SLOT);
        helper.assertTrue(plan.is(RpItems.PLAN) && plan.get(RpDataComponents.PLAN) != null, "written plan");
        inventory.setStackInSlot(0, ItemStack.EMPTY);
        inventory.setStackInSlot(3, ItemStack.EMPTY);
        menu.broadcastChanges();
        helper.assertTrue(menu.getSlot(ProjectTableMenu.RESULT).getItem().isEmpty(), "no planks: no result");
        helper.assertTrue(menu.getSatisfyMask() == (ProjectTableCrafting.ALL & ~(1 | 1 << 3)), "slots 0, 3 red");
        inventory.setStackInSlot(ProjectTableBlockEntity.STORAGE_START, new ItemStack(Items.BIRCH_PLANKS, 3));
        menu.broadcastChanges();
        helper.assertTrue(menu.getSlot(ProjectTableMenu.RESULT).getItem().is(Items.STICK), "sticks from storage");
        menu.clicked(ProjectTableMenu.RESULT, 0, ClickType.PICKUP, player);
        helper.assertTrue(storage(inventory) == 1 && count(inventory, 0, ProjectTableBlockEntity.GRID_SIZE) == 0,
                "two planks from the storage, grid stays empty");
        helper.assertTrue(menu.getSlot(ProjectTableMenu.RESULT).getItem().isEmpty(), "one plank is not enough");
        helper.succeed();
    }

    /** Shift-click crafts up to a stack (64 sticks = 16 crafts) and stops before the last possible craft. */
    @GameTest(template = EMPTY)
    public static void shiftClickCraftsAStack(GameTestHelper helper) {
        ProjectTableBlockEntity table = table(helper);
        ItemStackHandler inventory = table.getInventory();
        stickGrid(inventory);
        inventory.setStackInSlot(ProjectTableBlockEntity.STORAGE_START, new ItemStack(Items.OAK_PLANKS, 64));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ProjectTableMenu menu = new ProjectTableMenu(0, player.getInventory(), table);
        menu.quickMoveStack(player, ProjectTableMenu.RESULT);
        helper.assertTrue(player.getInventory().countItem(Items.STICK) == 64,
                "a stack of sticks: " + player.getInventory().countItem(Items.STICK));
        helper.assertTrue(storage(inventory) == 32, "32 planks used: " + storage(inventory));
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void plankTagsMatch(GameTestHelper helper) {
        helper.assertTrue(ProjectTableCrafting.matches(new ItemStack(Items.OAK_PLANKS),
                new ItemStack(Items.JUNGLE_PLANKS)), "planks match");
        helper.assertFalse(ProjectTableCrafting.matches(new ItemStack(Items.OAK_PLANKS),
                new ItemStack(Items.OAK_LOG)), "planks and logs differ");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void breakingDropsAllSlots(GameTestHelper helper) {
        breakAndCountDrops(helper, table(helper), 28);
    }

    @GameTest(template = EMPTY)
    public static void largeTableBreakingDropsAllSlots(GameTestHelper helper) {
        breakAndCountDrops(helper, table(helper, RpBlocks.LARGE_PROJECT_TABLE.get()), 37);
    }

    private static void breakAndCountDrops(GameTestHelper helper, ProjectTableBlockEntity table, int slots) {
        ItemStackHandler inventory = table.getInventory();
        helper.assertTrue(inventory.getSlots() == slots, "inventory size " + inventory.getSlots());
        for (int i = 0; i < slots; i++) {
            inventory.setStackInSlot(i, new ItemStack(i == ProjectTableBlockEntity.PLAN_SLOT
                    ? RpItems.BLANK_PLAN.get() : Items.COBBLESTONE));
        }
        helper.setBlock(TABLE, Blocks.AIR);
        int dropped = helper.getEntities(EntityType.ITEM).stream()
                .mapToInt(entity -> ((ItemEntity) entity).getItem().getCount()).sum();
        helper.assertTrue(dropped == slots, "all " + slots + " slots dropped: " + dropped);
        helper.succeed();
    }

    /** Three storage rows in the menu; the last storage slot refills the grid. */
    @GameTest(template = EMPTY)
    public static void largeTableRefillsFromThirdRow(GameTestHelper helper) {
        ProjectTableBlockEntity table = table(helper, RpBlocks.LARGE_PROJECT_TABLE.get());
        ItemStackHandler inventory = table.getInventory();
        int last = ProjectTableBlockEntity.STORAGE_START + ProjectTableBlockEntity.LARGE_STORAGE_SIZE - 1;
        stickGrid(inventory);
        inventory.setStackInSlot(last, new ItemStack(Items.SPRUCE_PLANKS, 10));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ProjectTableMenu menu = new ProjectTableMenu(0, player.getInventory(), table);
        helper.assertTrue(menu.getType() == RpMenus.LARGE_PROJECT_TABLE.get(), "large menu type");
        helper.assertTrue(menu.storageRows() == 3 && menu.playerStart() == ProjectTableMenu.STORAGE + 27
                && menu.end() == menu.playerStart() + 36, "menu layout");
        helper.assertTrue(menu.getSlot(menu.playerStart() - 1).getItem().is(Items.SPRUCE_PLANKS),
                "last storage slot shown in the menu");
        menu.clicked(ProjectTableMenu.RESULT, 0, ClickType.PICKUP, player);
        helper.assertTrue(menu.getCarried().is(Items.STICK) && menu.getCarried().getCount() == 4, "4 sticks taken");
        helper.assertTrue(inventory.getStackInSlot(last).getCount() == 8, "two planks from the third row");
        helper.succeed();
    }

    /** Automation reaches all 27 storage slots, but neither the grid nor the plan slot. */
    @GameTest(template = EMPTY)
    public static void largeTableAutomationReachesStorage(GameTestHelper helper) {
        ProjectTableBlockEntity table = table(helper, RpBlocks.LARGE_PROJECT_TABLE.get());
        IItemHandler handler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,
                helper.absolutePos(TABLE), Direction.UP);
        helper.assertTrue(handler != null && handler.getSlots() == 27, "27 storage slots");
        handler.insertItem(26, new ItemStack(Items.COBBLESTONE, 5), false);
        helper.assertTrue(table.getInventory().getStackInSlot(36).getCount() == 5, "slot 26 is table slot 36");
        helper.assertTrue(count(table.getInventory(), 0, ProjectTableBlockEntity.STORAGE_START) == 0,
                "grid and plan untouched");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void largeTableRecipe(GameTestHelper helper) {
        helper.assertTrue(helper.getLevel().getRecipeManager()
                .byKey(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "large_project_table"))
                .filter(recipe -> recipe.value().getResultItem(helper.getLevel().registryAccess())
                        .is(RpItems.LARGE_PROJECT_TABLE.get()))
                .isPresent(), "recipe craft:large_project_table");
        helper.succeed();
    }
}
