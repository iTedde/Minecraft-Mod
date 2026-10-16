package dev.craft.ic2.machine;

import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.menu.ElectricMachineMenu;
import dev.craft.ic2.recipe.MachineRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Macerator, extractor and compressor (IC2: 2 EU/t, 400 ticks, 32 EU/p): recipe list lookup. */
public class RecipeMachineBlockEntity extends ElectricMachineBlockEntity {
    public static final int ENERGY_PER_TICK = 2;
    public static final int OPERATION_LENGTH = 400;
    public static final int MAX_INPUT = 32;

    /** IC2 compressor pump search: below, +X, −X, +Z, −Z. */
    private static final Direction[] PUMP_SEARCH = {Direction.DOWN, Direction.EAST, Direction.WEST, Direction.SOUTH,
            Direction.NORTH};

    private final MachineRecipe.Kind kind;
    /** Compressor only: adjacent ready pump above a water source (IC2 cache, not saved). */
    @Nullable
    private PumpBlockEntity validPump;

    public RecipeMachineBlockEntity(Ic2Machine machine, MachineRecipe.Kind kind, BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(machine), pos, state, ENERGY_PER_TICK, OPERATION_LENGTH, MAX_INPUT);
        this.kind = kind;
    }

    public static RecipeMachineBlockEntity macerator(BlockPos pos, BlockState state) {
        return new RecipeMachineBlockEntity(Ic2Machine.MACERATOR, MachineRecipe.Kind.MACERATOR, pos, state);
    }

    public static RecipeMachineBlockEntity extractor(BlockPos pos, BlockState state) {
        return new RecipeMachineBlockEntity(Ic2Machine.EXTRACTOR, MachineRecipe.Kind.EXTRACTOR, pos, state);
    }

    public static RecipeMachineBlockEntity compressor(BlockPos pos, BlockState state) {
        return new RecipeMachineBlockEntity(Ic2Machine.COMPRESSOR, MachineRecipe.Kind.COMPRESSOR, pos, state);
    }

    public MachineRecipe.Kind getKind() {
        return kind;
    }

    @Nullable
    @Override
    public ItemStack getResultFor(ItemStack input, boolean adjustInput) {
        ItemStack result = MachineRecipe.getOutputFor(level, kind, input, adjustInput);
        return result.isEmpty() ? null : result;
    }

    // --- compressor + pump (IC2 TileEntityCompressor) ---

    @Override
    public boolean canOperate() {
        if (kind != MachineRecipe.Kind.COMPRESSOR || getValidPump() == null) {
            return super.canOperate();
        }
        ItemStack output = inventory.get(SLOT_OUTPUT);
        return output.isEmpty() || output.is(Items.SNOWBALL) && output.getCount() < output.getMaxStackSize();
    }

    @Override
    public void operate() {
        if (kind != MachineRecipe.Kind.COMPRESSOR) {
            super.operate();
            return;
        }
        if (!canOperate()) {
            return;
        }
        ItemStack input = inventory.get(SLOT_INPUT);
        ItemStack result = input.isEmpty() ? null : getResultFor(input, false);
        if (result != null) {
            super.operate();
            return;
        }
        PumpBlockEntity pump = getValidPump();
        if (pump == null) {
            return;
        }
        pump.resetCharge();
        level.setBlock(pump.getBlockPos().below(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        ItemStack output = inventory.get(SLOT_OUTPUT);
        if (output.isEmpty()) {
            inventory.set(SLOT_OUTPUT, new ItemStack(Items.SNOWBALL));
        } else {
            output.grow(1);
            inventory.set(SLOT_OUTPUT, output);
        }
    }

    @Nullable
    private PumpBlockEntity getValidPump() {
        if (validPump != null && !validPump.isRemoved() && validPump.isPumpReady() && validPump.isWaterBelow()) {
            return validPump;
        }
        for (Direction direction : PUMP_SEARCH) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof PumpBlockEntity pump
                    && pump.isPumpReady() && pump.isWaterBelow()) {
                return validPump = pump;
            }
        }
        return null;
    }

    @Override
    public SoundEvent getStartSound() {
        return switch (kind) {
            case MACERATOR -> Ic2Sounds.MACERATOR_OP.get();
            case EXTRACTOR -> Ic2Sounds.EXTRACTOR_OP.get();
            case COMPRESSOR -> Ic2Sounds.COMPRESSOR_OP.get();
        };
    }

    @Override
    public SoundEvent getInterruptSound() {
        return Ic2Sounds.INTERRUPT_ONE.get();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new ElectricMachineMenu(id, playerInventory, this);
    }
}
