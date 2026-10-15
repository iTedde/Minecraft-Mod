package dev.craft.forestry.menu;

import java.util.ArrayList;
import java.util.List;

import dev.craft.forestry.FrMenus;
import dev.craft.forestry.circuit.Circuit;
import dev.craft.forestry.circuit.CircuitBoard;
import dev.craft.forestry.circuit.CircuitBoardType;
import dev.craft.forestry.circuit.CircuitLayout;
import dev.craft.forestry.item.CircuitBoardItem;
import dev.craft.forestry.item.ElectronTubeItem;
import dev.craft.forestry.item.SolderingIronItem;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Forestry {@code ContainerSolderingIron} with its {@code SolderingInventory}: blank board, finished board and four
 * tube slots. Every change tries to solder; the inventory is not saved, everything left in it is dropped when the
 * GUI closes. Menu buttons 0/1 select the previous/next layout.
 */
public class SolderingIronMenu extends AbstractContainerMenu {
    public static final int SLOT_BLANK = 0;
    public static final int SLOT_FINISHED = 1;
    public static final int SLOT_INGREDIENT_1 = 2;
    public static final int SIZE = 6;

    private final Player player;
    private final Container inventory = new SimpleContainer(SIZE) {
        @Override
        public void setChanged() {
            super.setChanged();
            slotsChanged(this);
        }
    };
    private final DataSlot layout = DataSlot.standalone();

    public SolderingIronMenu(int id, Inventory playerInventory) {
        super(FrMenus.SOLDERING_IRON.get(), id);
        this.player = playerInventory.player;
        layout.set(CircuitLayout.getDefault().ordinal());
        addDataSlot(layout);
        addSlot(new BoardSlot(SLOT_BLANK, 152, 12));
        addSlot(new BoardSlot(SLOT_FINISHED, 152, 92));
        for (int i = 0; i < 4; i++) {
            addSlot(new Slot(inventory, SLOT_INGREDIENT_1 + i, 12, 32 + i * 20));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSecuredSlot(playerInventory, column + row * 9 + 9, 8 + column * 18, 123 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            addSecuredSlot(playerInventory, column, 8 + column * 18, 181);
        }
    }

    /** {@code addSecuredSlot}: the slot holding the soldering iron is locked. */
    private void addSecuredSlot(Inventory playerInventory, int index, int x, int y) {
        if (playerInventory.getItem(index).getItem() instanceof SolderingIronItem) {
            addSlot(new Slot(playerInventory, index, x, y) {
                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        } else {
            addSlot(new Slot(playerInventory, index, x, y));
        }
    }

    public CircuitLayout getLayout() {
        return CircuitLayout.values()[Math.floorMod(layout.get(), CircuitLayout.values().length)];
    }

    public Container getSolderingInventory() {
        return inventory;
    }

    /** The circuit the tube in {@code slot} would solder with the selected layout, or null. */
    @Nullable
    public Circuit getCircuit(ItemStack stack) {
        return stack.getItem() instanceof ElectronTubeItem tube ? tube.getTube().getCircuit(getLayout()) : null;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            layout.set(getLayout().previous().ordinal());
        } else if (id == 1) {
            layout.set(getLayout().next().ordinal());
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (!player.level().isClientSide()) {
            trySolder();
        }
    }

    /** {@code getCircuits}: valid tubes in the first {@code sockets} slots, at most {@code limit} per circuit. */
    private List<Circuit> getCircuits(CircuitBoardType type, boolean consume) {
        List<Circuit> circuits = new ArrayList<>();
        for (int i = 0; i < type.getSockets(); i++) {
            ItemStack ingredient = inventory.getItem(SLOT_INGREDIENT_1 + i);
            Circuit circuit = getCircuit(ingredient);
            if (circuit != null && circuits.stream().filter(c -> c == circuit).count() < circuit.getLimit()) {
                if (consume) {
                    ingredient.shrink(1);
                }
                circuits.add(circuit);
            }
        }
        return circuits;
    }

    /** {@code trySolder}: a single blank board, an empty result slot and exactly {@code sockets} valid tubes. */
    private void trySolder() {
        ItemStack blank = inventory.getItem(SLOT_BLANK);
        if (blank.isEmpty() || blank.getCount() > 1 || !inventory.getItem(SLOT_FINISHED).isEmpty()
                || !(blank.getItem() instanceof CircuitBoardItem board)) {
            return;
        }
        List<Circuit> circuits = getCircuits(board.getType(), false);
        if (circuits.isEmpty() || circuits.size() != board.getType().getSockets()) {
            return;
        }
        circuits = getCircuits(board.getType(), true);
        inventory.setItem(SLOT_BLANK, ItemStack.EMPTY);
        inventory.setItem(SLOT_FINISHED, CircuitBoardItem.create(board, new CircuitBoard(getLayout(), circuits)));
    }

    /** {@code purgeBag}: nothing is accepted, everything is dropped in front of the player. */
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            for (int i = 0; i < SIZE; i++) {
                ItemStack stack = inventory.removeItemNoUpdate(i);
                if (!stack.isEmpty()) {
                    player.drop(stack, false);
                }
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < SIZE) {
            if (!moveItemStackTo(stack, SIZE, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, SIZE, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return stack.getCount() == original.getCount() ? ItemStack.EMPTY : original;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    /** {@code SlotCustom} with {@code ItemCircuitBoard}: circuit boards only. */
    private final class BoardSlot extends Slot {
        BoardSlot(int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof CircuitBoardItem;
        }
    }
}
