package dev.craft.omnitools;

import java.util.function.Predicate;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * {@code ContainerLexicon} (spec 50 §1.3): input (44, 53), output (116, 53, take only), preview (80, 23, locked) and
 * the player inventory from y 104. Buttons: 0 previous, 1 next, 2 convert. The portable lexicon gives its contents
 * back when closed.
 */
public class LexiconMenu extends AbstractContainerMenu {
    public static final int BUTTON_PREVIOUS = 0;
    public static final int BUTTON_NEXT = 1;
    public static final int BUTTON_CONVERT = 2;

    private final LexiconInventory lexicon;
    private final SimpleContainer display = new SimpleContainer(1);
    private final Predicate<Player> valid;
    private final boolean portable;

    /** Client side. */
    public LexiconMenu(int id, Inventory inventory) {
        this(id, inventory, new LexiconInventory(), player -> true, false);
    }

    public LexiconMenu(int id, Inventory inventory, LexiconInventory lexicon, Predicate<Player> valid,
            boolean portable) {
        super(OmniTools.LEXICON_MENU.get(), id);
        this.lexicon = lexicon;
        this.valid = valid;
        this.portable = portable;
        addSlot(new Slot(lexicon, LexiconInventory.INPUT, 44, 53));
        addSlot(new Slot(lexicon, LexiconInventory.OUTPUT, 116, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        addSlot(new Slot(display, 0, 80, 23) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 104 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 162));
        }
    }

    public LexiconInventory lexicon() {
        return lexicon;
    }

    public ItemStack preview() {
        return display.getItem(0);
    }

    /** Client side, from the synced slots. */
    public boolean canConvert() {
        ItemStack input = getSlot(0).getItem();
        ItemStack output = getSlot(1).getItem();
        ItemStack target = preview();
        return !input.isEmpty() && !target.isEmpty() && !input.is(target.getItem())
                && (output.isEmpty() || output.is(target.getItem()) && output.getCount() < output.getMaxStackSize());
    }

    @Override
    public void broadcastChanges() {
        display.setItem(0, lexicon.preview());
        super.broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        switch (id) {
            case BUTTON_PREVIOUS -> lexicon.cycle(-1);
            case BUTTON_NEXT -> lexicon.cycle(1);
            case BUTTON_CONVERT -> lexicon.convert();
            default -> {
                return false;
            }
        }
        broadcastChanges();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem() || index == 2) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < 3) {
            if (!moveItemStackTo(stack, 3, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return valid.test(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (portable) {
            clearContainer(player, lexicon);
        }
    }
}
