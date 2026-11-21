package dev.craft.buildcraft.menu;

import javax.annotation.Nullable;

import dev.craft.buildcraft.BcMenus;
import dev.craft.buildcraft.transport.ItemPipeBlockEntity;
import dev.craft.buildcraft.transport.PipeDistribution;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

/**
 * Weights of the distribution transport pipe (spec 31 §4), no slots. Data 0–5 = weight per side, 6 = sides connected
 * for items. Button {@code side·4 + k} changes a weight by −10/−1/+1/+10; {@code +24}: shift-click, sets 0 or 100.
 */
public class DistributionPipeMenu extends AbstractContainerMenu {
    public static final int[] STEPS = {-10, -1, 1, 10};
    public static final int SHIFT = 24;

    @Nullable
    private final ItemPipeBlockEntity pipe;
    private final ContainerData data;

    public DistributionPipeMenu(int id, ItemPipeBlockEntity pipe) {
        super(BcMenus.DISTRIBUTION_PIPE.get(), id);
        this.pipe = pipe;
        PipeDistribution distribution = pipe.getDistribution();
        data = new ContainerData() {
            @Override
            public int get(int index) {
                return index < 6 ? distribution.getWeight(Direction.from3DDataValue(index)) : pipe.getItemSidesMask();
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 7;
            }
        };
        addDataSlots(data);
    }

    public DistributionPipeMenu(int id, Inventory inventory) {
        super(BcMenus.DISTRIBUTION_PIPE.get(), id);
        pipe = null;
        data = new SimpleContainerData(7);
        addDataSlots(data);
    }

    public int getWeight(Direction side) {
        return data.get(side.get3DDataValue());
    }

    public boolean isConnected(Direction side) {
        return (data.get(6) & 1 << side.get3DDataValue()) != 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (pipe == null || id < 0 || id >= 2 * SHIFT) {
            return false;
        }
        boolean shift = id >= SHIFT;
        int button = id % SHIFT;
        Direction side = Direction.from3DDataValue(button / 4);
        int step = STEPS[button % 4];
        PipeDistribution distribution = pipe.getDistribution();
        int weight = shift ? (step < 0 ? 0 : PipeDistribution.MAX_WEIGHT) : distribution.getWeight(side) + step;
        distribution.setWeight(side, weight);
        pipe.setChanged();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return pipe != null && !pipe.isRemoved()
                && player.distanceToSqr(pipe.getBlockPos().getCenter()) <= 64.0;
    }
}
