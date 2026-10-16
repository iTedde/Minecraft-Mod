package dev.craft.forestry.menu;

import dev.craft.forestry.FrMenus;
import dev.craft.forestry.engine.ElectricalEngineBlockEntity;
import dev.craft.forestry.item.CircuitBoardItem;
import dev.craft.forestry.item.SolderingIronItem;
import dev.craft.forestry.machine.ErrorState;
import dev.craft.ic2.menu.SyncedInts;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Forestry {@code ContainerEngineTin} ({@code ContainerSocketed}): the battery slot and the circuit board socket.
 * The socket is no real slot: a click with a board puts the whole held stack in (replacing, and losing, a board that
 * was there), a click with the soldering iron takes it out. Synced: EU stored, EU storage and for the
 * ledgers output, stored MJ, heat and error state.
 */
public class ElectricalEngineMenu extends ForestryMenu {
    public static final int SOCKET_X = 30;
    public static final int SOCKET_Y = 40;

    @Nullable
    private final ElectricalEngineBlockEntity engine;

    public ElectricalEngineMenu(int id, Inventory inventory, ElectricalEngineBlockEntity engine) {
        this(id, inventory, engine, engine.getBlockPos(), engine.getInventory(), engine.getSocket(),
                new SyncedInts(engine::getEuStored, engine::getEuStorage, engine::getCurrentOutput,
                        engine::getStoredEnergy, engine::getHeat, () -> engine.getErrorState().ordinal()));
    }

    public ElectricalEngineMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new ItemStackHandler(1), new ItemStackHandler(1), new SyncedInts(6));
    }

    private ElectricalEngineMenu(int id, Inventory inventory, @Nullable ElectricalEngineBlockEntity engine,
                                 BlockPos pos, IItemHandler items, IItemHandler socket, SyncedInts data) {
        super(FrMenus.ELECTRICAL_ENGINE.get(), id, pos, data);
        this.engine = engine;
        addSlot(new SlotItemHandler(items, 0, 84, 53));
        setShiftSlots(1);
        addSlot(new SocketSlot(socket));
        addPlayerInventory(inventory, 84, 142);
    }

    public int getEuStored() {
        return data.getInt(0);
    }

    public int getEuStorage() {
        return data.getInt(1);
    }

    public int getCurrentOutput() {
        return data.getInt(2);
    }

    public int getStoredEnergy() {
        return data.getInt(3);
    }

    public int getHeat() {
        return data.getInt(4);
    }

    public ErrorState getErrorState() {
        return ErrorState.byId(data.getInt(5));
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < slots.size() && slots.get(slotId) instanceof SocketSlot) {
            if (clickType == ClickType.PICKUP && engine != null) {
                clickSocket(player);
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    /** {@code handleChipsetClick} / {@code handleSolderingIronClick}. */
    private void clickSocket(Player player) {
        ItemStack carried = getCarried();
        if (carried.getItem() instanceof CircuitBoardItem) {
            engine.setSocket(carried);
            setCarried(ItemStack.EMPTY);
        } else if (carried.getItem() instanceof SolderingIronItem) {
            ItemStack board = engine.getSocket().getStackInSlot(0).copy();
            if (board.isEmpty()) {
                return;
            }
            player.getInventory().add(board);
            if (board.isEmpty()) {
                engine.setSocket(ItemStack.EMPTY);
                if (player instanceof ServerPlayer serverPlayer) {
                    carried.hurtAndBreak(1, (ServerLevel) serverPlayer.level(), serverPlayer, item -> {
                    });
                    setCarried(carried);
                }
            } else {
                engine.setSocket(board);
            }
        }
    }

    /** {@code SocketSlot}: shows the board in the socket; never picked up or filled by vanilla clicks. */
    public static final class SocketSlot extends SlotItemHandler {
        SocketSlot(IItemHandler socket) {
            super(socket, 0, SOCKET_X, SOCKET_Y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}
