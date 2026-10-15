package dev.craft.ic2.generator;

import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.menu.GeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/** Solid fuel generator, 1:1 with IC2 {@code TileEntityGenerator}: 10 EU/t, 4000 EU buffer. */
public class GeneratorBlockEntity extends BaseGeneratorBlockEntity {
    public static final int PRODUCTION = 10;
    public static final int MAX_STORAGE = 4000;

    private int itemFuelTime;

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.GENERATOR), pos, state, 2, PRODUCTION, MAX_STORAGE);
    }

    /** IC2 {@code gaugeFuelScaled}. */
    public int gaugeFuelScaled(int scale) {
        if (fuel <= 0) {
            return 0;
        }
        if (itemFuelTime <= 0) {
            itemFuelTime = fuel;
        }
        return Math.min(fuel * scale / itemFuelTime, scale);
    }

    public int getItemFuelTime() {
        return itemFuelTime;
    }

    @Override
    public boolean gainFuel() {
        ItemStack stack = inventory.get(1);
        if (stack.isEmpty() || stack.is(Items.LAVA_BUCKET)) {
            return false;
        }
        int value = FuelValues.getFuelValueFor(stack) / 4;
        if (value <= 0) {
            return false;
        }
        fuel += value;
        itemFuelTime = value;
        inventory.set(1, consumeOne(stack));
        return true;
    }

    /** The generator keeps burning with a full buffer; the excess is lost. */
    @Override
    public boolean isConverting() {
        return fuel > 0;
    }

    @Override
    public SoundEvent getOperationSound() {
        return Ic2Sounds.GENERATOR_LOOP.get();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new GeneratorMenu(id, playerInventory, this);
    }
}
