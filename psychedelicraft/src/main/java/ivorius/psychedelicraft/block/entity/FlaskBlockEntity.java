/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */
package ivorius.psychedelicraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.datafixers.util.Either;

import ivorius.psychedelicraft.block.BlockWithFluid;
import ivorius.psychedelicraft.block.PipeInsertable;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.Impurities;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.NbtSerialisable;

/**
 * Created by lukas on 25.10.14.
 * Updated by Sollace on 2 Jan 2023
 */
public class FlaskBlockEntity extends SyncedBlockEntity implements BlockWithFluid.DirectionalFluidResovoir, Resovoir.ChangeListener, PipeInsertable {
    private static final int[] INPUT_SLOT_ID = {0};
    private static final int[] OUTPUT_SLOT_ID = {1};
    private static final int[] BOTH_SLOT_ID = {0, 1};

    private final Resovoir tank;
    private boolean pendingSync;

    public final IoSlot inputSlot = new IoSlot(0);
    public final IoSlot outputSlot = new IoSlot(1);

    public final IoInventory ioInventory = new IoInventory();

    public final ContainerData propertyDelegate = new SimpleContainerData(getTotalProperties());

    public FlaskBlockEntity(BlockPos pos, BlockState state) {
        this(PSBlockEntities.FLASK, pos, state, FluidVolumes.FLASK);
    }

    public FlaskBlockEntity(BlockEntityType<? extends FlaskBlockEntity> type, BlockPos pos, BlockState state, int capacity) {
        super(type, pos, state);
        this.tank = new Resovoir(capacity, this);
    }

    public void markForUpdate() {
        pendingSync = true;
    }

    protected int getTotalProperties() {
        return 4;
    }

    @Override
    public void onLevelChange(Resovoir resovoir, int change) {
        markForUpdate();
    }

    @Override
    public Resovoir getPrimaryTank() {
        return tank;
    }

    @Override
    public void clientTick(Level world) {

    }

    @Override
    public void tick(ServerLevel world) {
        ItemStack output = outputSlot.getStack();
        boolean playSound = false;

        if (FluidCapacity.get(output) > 0) {
            ItemFluids.Transaction t = ItemFluids.Transaction.begin(output);
            int amount = tank.withdraw(t, FluidVolumes.BOWL);
            if (amount > 0) {
                outputSlot.incrementLevelsTransferred(amount);
                playSound = true;
                outputSlot.setStack(t.toItemStack());
            }
        }

        ItemStack input = inputSlot.getStack();
        if (FluidCapacity.get(input) > 0) {
            ItemFluids.Transaction t = ItemFluids.Transaction.begin(input);

            int amount = tank.deposit(t, FluidVolumes.BOWL);
            if (amount > 0) {
                inputSlot.incrementLevelsTransferred(amount);
                inputSlot.setStack(t.toItemStack());
                playSound = true;
            }
        }

        if (playSound && world.getGameTime() % 9 == 0) {
            world.playSound(null, getBlockPos(), SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.025F, 0.5F);
        }

        if (pendingSync) {
            pendingSync = false;
            setChanged();
            world.blockUpdated(getBlockPos(), getBlockState().getBlock());
        }
    }

    @Override
    public boolean acceptsConnectionFrom(LevelAccessor world, BlockState state, BlockPos pos, BlockState neighborState, BlockPos neighborPos, Direction direction, boolean input) {
        return true;
    }

    @Override
    public Either<PipeFluids, Unit> tryInsert(ServerLevel world, BlockState state, BlockPos pos, Direction direction, PipeFluids fluids) {
        Resovoir tank = getTankOnSide(direction);
        fluids.fluids().getFluids().stream().filter(i -> tank.getContents().canCombine(i)).findFirst().ifPresent(f -> {
            int amountMoved = tank.deposit(f);
            if (amountMoved > 0) {
                fluids.fluids().remove(f.ofAmount(amountMoved));
            }
        });
        return PipeInsertable.reject(fluids);
    }

    @Override
    public Optional<PipeFluids> tryExtract(ServerLevel world, BlockState state, BlockPos pos, Direction direction) {
        Resovoir tank = getTankOnSide(direction);
        ItemFluids fluids = tank.drain((int)tank.getCapacity() / 10);
        return fluids.isEmpty() ? Optional.empty() : Optional.of(PipeFluids.of(fluids, Impurities.EMPTY, 0));
    }

    @Deprecated
    @Override
    public List<ItemStack> getDroppedStacks(ItemStack container) {
        List<ItemStack> stacks = new ArrayList<>();
        int maxCapacity = Math.min(FluidCapacity.get(container), tank.getContents().amount());
        if (maxCapacity > 0) {
            stacks.add(ItemFluids.set(container, tank.getContents().ofAmount(maxCapacity)));
        }
        return stacks;
    }

    @Override
    public void saveAdditional(CompoundTag compound, Provider lookup) {
        super.saveAdditional(compound, lookup);
        compound.put("tank", tank.toNbt(lookup));
        ContainerHelper.saveAllItems(compound, ioInventory.getItems(), lookup);
        compound.put("inputSlot", inputSlot.toNbt(lookup));
        compound.put("outputSlot", outputSlot.toNbt(lookup));
    }

    @Override
    public void loadAdditional(CompoundTag compound, Provider lookup) {
        super.loadAdditional(compound, lookup);
        tank.fromNbt(compound.getCompound("tank"), lookup);
        ContainerHelper.loadAllItems(compound, ioInventory.getItems(), lookup);
        inputSlot.fromNbt(compound.getCompound("inputSlot"), lookup);
        outputSlot.fromNbt(compound.getCompound("outputSlot"), lookup);
    }

    @Override
    public int getContainerSize() {
        return ioInventory.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return ioInventory.isEmpty() && tank.getContents().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return ioInventory.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ioInventory.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ioInventory.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ioInventory.setItem(slot, stack);
        onContentsExternallyChanged(slot);
    }

    public void onContentsExternallyChanged(int slot) {
        if (slot == 0) {
            inputSlot.onChange();
        }
        if (slot == 1) {
            outputSlot.onChange();
        }
    }

    @Override
    public boolean stillValid(Player var1) {
        return true;
    }

    @Override
    public void clearContent() {
        ioInventory.clearContent();
        tank.clear();
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return switch (direction.getAxis()) {
            case X /* EW */ -> INPUT_SLOT_ID;
            case Z /* NS */ -> OUTPUT_SLOT_ID;
            case Y /* UD */ -> BOTH_SLOT_ID;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return ioInventory.canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        if (slot == OUTPUT_SLOT_ID[0] && outputSlot.getFillPercentage() >= 1) {
            return true;
        }

        if (slot == INPUT_SLOT_ID[0] && inputSlot.getFillPercentage() <= 0) {
            return true;
        }

        return false;
    }

    class IoInventory extends SimpleContainer {
        public IoInventory() {
            super(2);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return slot < getContainerSize() && FluidCapacity.get(stack) > 0
                    && (slot == 0 ? FluidCapacity.getPercentage(stack) > 0 : FluidCapacity.getPercentage(stack) < 1);
        }
    }

    public class IoSlot implements NbtSerialisable {
        public final int index;

        private final int levelsTransferredIndex;
        private final int inputtedLevelsIndex;

        public IoSlot(int index) {
            this.index = index;
            levelsTransferredIndex = 0 + (index * 2);
            inputtedLevelsIndex = 1 + (index * 2);
        }

        public float getProgress() {
            return propertyDelegate.get(inputtedLevelsIndex) == 0 ? 0 : (float)propertyDelegate.get(levelsTransferredIndex) / propertyDelegate.get(inputtedLevelsIndex);
        }

        public void incrementLevelsTransferred(int amount) {
            propertyDelegate.set(levelsTransferredIndex, propertyDelegate.get(levelsTransferredIndex) + amount);
        }

        public void onChange() {
            ItemStack stack = ioInventory.getItem(index);
            var fluids = ItemFluids.of(stack);
            int levels = fluids.amount();
            if (index == 1) {
                levels = FluidCapacity.get(stack) - levels;
            }
            propertyDelegate.set(inputtedLevelsIndex, levels);
            propertyDelegate.set(levelsTransferredIndex, 0);
        }

        public ItemStack getStack() {
            return ioInventory.getItem(index);
        }

        public void setStack(ItemStack stack) {
            ioInventory.setItem(index, stack);
        }

        public float getFillPercentage() {
            return FluidCapacity.getPercentage(getStack());
        }

        @Override
        public void toNbt(CompoundTag compound, Provider lookup) {
            compound.putInt("inputtedLevels", propertyDelegate.get(inputtedLevelsIndex));
            compound.putInt("levelsTransferred", propertyDelegate.get(levelsTransferredIndex));
        }

        @Override
        public void fromNbt(CompoundTag compound, Provider lookup) {
            propertyDelegate.set(inputtedLevelsIndex, compound.getInt("inputtedLevels"));
            propertyDelegate.set(levelsTransferredIndex, compound.getInt("levelsTransferred"));
        }
    }
}
