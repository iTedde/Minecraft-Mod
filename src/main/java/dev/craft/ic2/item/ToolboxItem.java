package dev.craft.ic2.item;

import java.util.ArrayList;
import java.util.List;

import dev.craft.Craft;
import dev.craft.ic2.machine.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

/**
 * Toolbox (IC2 {@code ItemToolbox}): right click packs the boxable hotbar items, the next right click unpacks them.
 * The contents live in the vanilla container component (IC2 NBT {@code box0}–{@code box7}).
 */
public class ToolboxItem extends Item {
    public static final int CAPACITY = 8;
    /** IC2 {@code IBoxable} returning true for items that would not qualify by stack size. */
    public static final TagKey<Item> BOXABLE =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "toolbox/boxable"));
    /** IC2 {@code IBoxable} returning false. */
    public static final TagKey<Item> NOT_BOXABLE =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "toolbox/not_boxable"));

    public ToolboxItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static List<ItemStack> getContents(ItemStack toolbox) {
        List<ItemStack> contents = new ArrayList<>();
        toolbox.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItemsCopy()
                .forEach(contents::add);
        return contents;
    }

    public static boolean isFull(ItemStack toolbox) {
        return !getContents(toolbox).isEmpty();
    }

    /** IC2: {@code IBoxable} decides, otherwise anything with a maximum stack size of 1. */
    public static boolean isBoxable(ItemStack stack) {
        if (stack.is(NOT_BOXABLE)) {
            return false;
        }
        return stack.is(BOXABLE) || stack.getMaxStackSize() <= 1;
    }

    @Override
    public Component getName(ItemStack stack) {
        List<ItemStack> contents = getContents(stack);
        return contents.isEmpty() ? super.getName(stack) : contents.getFirst().getItem().getName(contents.getFirst());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack toolbox = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            if (isFull(toolbox)) {
                unpack(toolbox, player);
            } else {
                pack(toolbox, player);
            }
            player.inventoryMenu.broadcastChanges();
        }
        return InteractionResultHolder.sidedSuccess(toolbox, level.isClientSide());
    }

    /** Moves the boxable hotbar items into the box (at most {@link #CAPACITY}, like the eight NBT slots). */
    public static void pack(ItemStack toolbox, Player player) {
        NonNullList<ItemStack> items = player.getInventory().items;
        List<ItemStack> packed = new ArrayList<>();
        for (int slot = 0; slot < Inventory.getSelectionSize() && packed.size() < CAPACITY; slot++) {
            ItemStack stack = items.get(slot);
            if (!stack.isEmpty() && stack != toolbox && isBoxable(stack)) {
                packed.add(stack);
                items.set(slot, ItemStack.EMPTY);
            }
        }
        if (!packed.isEmpty()) {
            toolbox.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(packed));
        }
    }

    /** Fills free hotbar slots 0–7 (original quirk: never slot 8) and drops the rest at the player. */
    public static void unpack(ItemStack toolbox, Player player) {
        NonNullList<ItemStack> items = player.getInventory().items;
        List<ItemStack> contents = getContents(toolbox);
        int next = 0;
        for (int slot = 0; slot < CAPACITY && next < contents.size(); slot++) {
            if (items.get(slot).isEmpty()) {
                items.set(slot, contents.get(next++));
            }
        }
        BlockPos pos = BlockPos.containing(player.getX(), player.getY(), player.getZ());
        while (next < contents.size()) {
            StackUtil.dropAsEntity(player.level(), pos, contents.get(next++));
        }
        toolbox.remove(DataComponents.CONTAINER);
    }
}
