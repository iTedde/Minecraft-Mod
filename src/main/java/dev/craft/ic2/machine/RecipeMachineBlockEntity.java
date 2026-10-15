package dev.craft.ic2.machine;

import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.menu.ElectricMachineMenu;
import dev.craft.ic2.recipe.MachineRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Macerator, extractor and compressor (IC2: 2 EU/t, 400 ticks, 32 EU/p): recipe list lookup. */
public class RecipeMachineBlockEntity extends ElectricMachineBlockEntity {
    public static final int ENERGY_PER_TICK = 2;
    public static final int OPERATION_LENGTH = 400;
    public static final int MAX_INPUT = 32;

    private final MachineRecipe.Kind kind;

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
