package dev.craft.forestry.item;

import java.util.List;

import dev.craft.forestry.FrDataComponents;
import dev.craft.forestry.circuit.CircuitBoard;
import dev.craft.forestry.circuit.CircuitBoardType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

/** Forestry {@code ItemCircuitBoard} (one item per board type instead of the damage value). */
public class CircuitBoardItem extends Item {
    private final CircuitBoardType type;

    public CircuitBoardItem(CircuitBoardType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public CircuitBoardType getType() {
        return type;
    }

    /** {@code getCircuitboard}: the soldered circuits, or null for a blank board. */
    @Nullable
    public static CircuitBoard getCircuitBoard(ItemStack stack) {
        return stack.get(FrDataComponents.CIRCUIT_BOARD.get());
    }

    /** {@code createCircuitboard}. */
    public static ItemStack create(Item board, CircuitBoard circuits) {
        ItemStack stack = new ItemStack(board);
        stack.set(FrDataComponents.CIRCUIT_BOARD.get(), circuits);
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CircuitBoard board = getCircuitBoard(stack);
        if (board != null) {
            board.addTooltip(tooltip::add);
        }
    }
}
