package dev.craft.redpower.table;

import static dev.craft.redpower.table.ProjectTableBlockEntity.GRID_SIZE;
import static dev.craft.redpower.table.ProjectTableBlockEntity.PLAN_SLOT;
import static dev.craft.redpower.table.ProjectTableBlockEntity.STORAGE_SIZE;
import static dev.craft.redpower.table.ProjectTableBlockEntity.STORAGE_START;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import dev.craft.redpower.RpDataComponents;
import dev.craft.redpower.RpItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

/**
 * Crafting rules of the RedPower project table (spec 20 §1.3): the grid refills itself from the storage, a written plan
 * takes every ingredient from the storage. Works on the table's 28-slot inventory.
 */
public final class ProjectTableCrafting {
    /** All nine grid slots satisfied. */
    public static final int ALL = (1 << GRID_SIZE) - 1;

    /** Material tags standing in for RedPower's ore dictionary classes (spec 20 §4 F1). */
    private static final Pattern MATERIAL_TAG = Pattern.compile("(ingots|dusts|gems|plates|nuggets|ores|dyes)/[^/]+");
    private static final List<ResourceLocation> VANILLA_MATERIAL_TAGS = List.of(
            ResourceLocation.withDefaultNamespace("planks"), ResourceLocation.withDefaultNamespace("logs"),
            ResourceLocation.withDefaultNamespace("wool"));

    private ProjectTableCrafting() {
    }

    /**
     * {@code CoreLib.matchItemStackOre}: same item (data components and damage ignored) or the same material tag.
     */
    public static boolean matches(ItemStack a, ItemStack b) {
        if (a.is(b.getItem())) {
            return true;
        }
        return a.getTags().anyMatch(tag -> isMaterialTag(tag) && b.is(tag));
    }

    private static boolean isMaterialTag(TagKey<Item> tag) {
        ResourceLocation id = tag.location();
        return VANILLA_MATERIAL_TAGS.contains(id)
                || id.getNamespace().equals("c") && MATERIAL_TAG.matcher(id.getPath()).matches();
    }

    /** {@code getPlanItems}: the required items of a written plan in the plan slot, or {@code null}. */
    @Nullable
    public static ItemStack[] planItems(IItemHandlerModifiable inventory) {
        Plan plan = inventory.getStackInSlot(PLAN_SLOT).get(RpDataComponents.PLAN);
        return plan == null ? null : plan.shadowItems();
    }

    /** {@code findMatch}: the first storage slot whose stack matches, or −1. */
    public static int findMatch(IItemHandlerModifiable inventory, ItemStack wanted) {
        for (int i = 0; i < STORAGE_SIZE; i++) {
            ItemStack test = inventory.getStackInSlot(STORAGE_START + i);
            if (!test.isEmpty() && matches(wanted, test)) {
                return STORAGE_START + i;
            }
        }
        return -1;
    }

    /**
     * {@code getSatisfyMask}: one bit per grid slot that is filled, not needed by the plan, or covered by the storage
     * (each storage stack covers as many open plan slots as it has items).
     */
    public static int satisfyMask(IItemHandlerModifiable inventory) {
        ItemStack[] plan = planItems(inventory);
        int bits = 0;
        for (int i = 0; i < GRID_SIZE; i++) {
            if (!inventory.getStackInSlot(i).isEmpty() || plan == null || plan[i].isEmpty()) {
                bits |= 1 << i;
            }
        }
        if (bits == ALL) {
            return ALL;
        }
        for (int i = 0; i < STORAGE_SIZE; i++) {
            ItemStack test = inventory.getStackInSlot(STORAGE_START + i);
            if (test.isEmpty()) {
                continue;
            }
            int left = test.getCount();
            for (int j = 0; j < GRID_SIZE; j++) {
                if ((bits & 1 << j) == 0 && !plan[j].isEmpty() && matches(plan[j], test)) {
                    bits |= 1 << j;
                    if (--left == 0) {
                        break;
                    }
                }
            }
        }
        return bits;
    }

    /**
     * {@code SlotCraftRefill.isLastUse}: whether the grid cannot be refilled after this craft – some slot with exactly
     * one stackable item has no matching storage stack left.
     */
    public static boolean isLastUse(IItemHandlerModifiable inventory) {
        int bits = 0;
        for (int i = 0; i < GRID_SIZE; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty() || !stack.isStackable() || stack.getCount() > 1) {
                bits |= 1 << i;
            }
        }
        if (bits == ALL) {
            return false;
        }
        for (int i = 0; i < STORAGE_SIZE; i++) {
            ItemStack test = inventory.getStackInSlot(STORAGE_START + i);
            if (test.isEmpty()) {
                continue;
            }
            int left = test.getCount();
            for (int j = 0; j < GRID_SIZE; j++) {
                if ((bits & 1 << j) == 0 && matches(inventory.getStackInSlot(j), test)) {
                    bits |= 1 << j;
                    if (--left == 0) {
                        break;
                    }
                }
            }
        }
        return bits != ALL;
    }

    /** The grid as crafted: empty slots the plan needs show the first matching storage stack. */
    public static List<ItemStack> shadowGrid(IItemHandlerModifiable inventory) {
        ItemStack[] plan = planItems(inventory);
        List<ItemStack> grid = new ArrayList<>(GRID_SIZE);
        for (int i = 0; i < GRID_SIZE; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty() && plan != null && !plan[i].isEmpty()) {
                int match = findMatch(inventory, plan[i]);
                if (match >= 0) {
                    stack = inventory.getStackInSlot(match);
                }
            }
            grid.add(stack);
        }
        return grid;
    }

    /** The recipe of the shadow grid, only when every grid slot is satisfied. */
    public static Optional<RecipeHolder<CraftingRecipe>> findRecipe(IItemHandlerModifiable inventory, Level level) {
        if (satisfyMask(inventory) != ALL) {
            return Optional.empty();
        }
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,
                CraftingInput.of(3, 3, shadowGrid(inventory)), level);
    }

    public static ItemStack assemble(RecipeHolder<CraftingRecipe> recipe, IItemHandlerModifiable inventory,
                                     Level level) {
        return recipe.value().assemble(CraftingInput.of(3, 3, shadowGrid(inventory)), level.registryAccess());
    }

    /**
     * {@code SlotCraftRefill.onPickupFromSlot} without the achievements: takes the plan ingredients from the storage,
     * uses up the grid, then refills the grid from the storage unless this was the last use.
     */
    public static void consumeIngredients(IItemHandlerModifiable inventory, Player player) {
        ItemStack[] plan = planItems(inventory);
        ItemStack[] before = new ItemStack[GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            before[i] = inventory.getStackInSlot(i).copy();
        }
        boolean last = isLastUse(inventory);
        if (plan != null) {
            for (int i = 0; i < GRID_SIZE; i++) {
                if (before[i].isEmpty() && !plan[i].isEmpty()) {
                    int match = findMatch(inventory, plan[i]);
                    if (match >= 0) {
                        useOne(inventory, match, player);
                    }
                }
            }
        }
        for (int i = 0; i < GRID_SIZE; i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                useOne(inventory, i, player);
            }
        }
        if (last) {
            return;
        }
        for (int i = 0; i < GRID_SIZE; i++) {
            if (before[i].isEmpty() || plan != null && !plan[i].isEmpty()) {
                continue;
            }
            ItemStack now = inventory.getStackInSlot(i);
            int match = findMatch(inventory, before[i]);
            if (match < 0) {
                continue;
            }
            if (now.isEmpty()) {
                ItemStack storage = inventory.getStackInSlot(match).copy();
                inventory.setStackInSlot(i, storage.split(1));
                inventory.setStackInSlot(match, storage);
            } else if (!matches(now, before[i]) && now.is(before[i].getCraftingRemainingItem().getItem())) {
                // the emptied container goes to the storage, a full one into the grid
                ItemStack storage = inventory.getStackInSlot(match);
                inventory.setStackInSlot(match, now);
                inventory.setStackInSlot(i, storage);
            }
        }
    }

    /**
     * Uses up one item of a slot; its crafting remainder (empty bucket …) takes the slot if it became empty, otherwise
     * goes to the player (the original overwrote the rest of a storage stack, spec 20 §4).
     */
    private static void useOne(IItemHandlerModifiable inventory, int slot, Player player) {
        ItemStack stack = inventory.getStackInSlot(slot).copy();
        ItemStack remainder = stack.getCraftingRemainingItem();
        stack.shrink(1);
        if (!remainder.isEmpty()) {
            if (stack.isEmpty()) {
                stack = remainder;
            } else if (ItemStack.isSameItemSameComponents(stack, remainder)) {
                stack.grow(remainder.getCount());
            } else if (!player.getInventory().add(remainder)) {
                player.drop(remainder, false);
            }
        }
        inventory.setStackInSlot(slot, stack);
    }

    /** Writes a blank plan in the plan slot: the result and one item per filled grid slot. */
    public static boolean writePlan(IItemHandlerModifiable inventory, ItemStack result) {
        if (!inventory.getStackInSlot(PLAN_SLOT).is(RpItems.BLANK_PLAN) || result.isEmpty()) {
            return false;
        }
        List<Plan.Requirement> requires = new ArrayList<>();
        for (int i = 0; i < GRID_SIZE; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                requires.add(new Plan.Requirement(i, stack));
            }
        }
        ItemStack plan = new ItemStack(RpItems.PLAN.get());
        plan.set(RpDataComponents.PLAN, new Plan(result, requires));
        inventory.setStackInSlot(PLAN_SLOT, plan);
        return true;
    }
}
