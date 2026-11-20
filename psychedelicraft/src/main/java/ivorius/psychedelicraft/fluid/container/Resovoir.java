package ivorius.psychedelicraft.fluid.container;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * @author Sollace
 * @since 3 Jan 2023
 */
public class Resovoir implements NbtSerialisable, IFluidHandler {
    public static final Resovoir EMPTY = new Resovoir(0, (r, l) -> {}) {
        @Override
        public void fromNbt(CompoundTag compound, Provider lookup) {

        }
    };
    private ItemFluids fluids;
    private final int capacity;

    private final ChangeListener changeCallback;

    public Resovoir(int capacity, ChangeListener changeCallback) {
        this(capacity, changeCallback, ItemFluids.EMPTY);
    }

    public Resovoir(int capacity, ChangeListener changeCallback, ItemFluids fluids) {
        this.capacity = capacity;
        this.changeCallback = changeCallback;
        this.fluids = fluids;
    }

    public ItemFluids getContents() {
        return fluids;
    }

    public void setContents(ItemFluids fluids) {
        setContents(fluids, true);
    }

    public void setContents(ItemFluids fluids, boolean notify) {
        int amount = this.fluids.amount();
        this.fluids = fluids.ofAmount(Math.min(fluids.amount(), capacity));
        if (notify) {
            changeCallback.onLevelChange(this, this.fluids.amount() - amount);
        }
    }

    public long getAmount() {
        return fluids.amount();
    }

    public long getCapacity() {
        return capacity;
    }

    public ItemStack deposit(ItemStack stack) {
        ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack);
        deposit(t, t.capacity());
        return t.toItemStack();
    }

    public int deposit(ItemFluids stack) {
        if (fluids.amount() >= capacity || stack.isEmpty() || !fluids.canCombine(stack)) {
            return 0;
        }

        int transferred = Mth.clamp(Math.max(0, capacity - fluids.amount()), 0, stack.amount());
        if (transferred > 0) {
            fluids = stack.ofAmount(fluids.amount() + transferred);
            changeCallback.onLevelChange(this, transferred);
        }

        return transferred;
    }

    public ItemFluids drain(int maxAmount) {
        ItemFluids extracted = fluids.ofAmount(Math.min(maxAmount, fluids.amount()));
        if (extracted.amount() > 0) {
            fluids = fluids.ofAmount(fluids.amount() - extracted.amount());
            changeCallback.onLevelChange(this, -extracted.amount());
        }

        return extracted;
    }

    public int deposit(ItemFluids.Transaction from, int maxAmount) {
        if (fluids.amount() >= capacity || from.fluids().isEmpty() || !fluids.canCombine(from.fluids())) {
            return 0;
        }

        ItemFluids transferred = from.withdraw(Math.min(Math.max(0, capacity - fluids.amount()), maxAmount));
        fluids = transferred.ofAmount(fluids.amount() + transferred.amount());
        if (transferred.amount() > 0) {
            changeCallback.onLevelChange(this, transferred.amount());
        }

        return transferred.amount();
    }

    public int withdraw(ItemFluids.Transaction output, int maxAmount) {
        if (fluids.isEmpty() || output.fluids().amount() >= output.capacity() || !fluids.canCombine(output.fluids())) {
            return 0;
        }
        int initialAmount = fluids.amount();
        fluids = output.deposit(fluids, maxAmount);
        int transferred = initialAmount - fluids.amount();
        if (transferred > 0) {
            changeCallback.onLevelChange(this, -transferred);
        }
        return transferred;
    }

    // NeoForge fluid handler (upstream: Fabric SingleSlotStorage). Amounts outside are millibuckets.

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return fluids.isEmpty() ? FluidStack.EMPTY : fluids.toVariant().toStack(fluids.amount());
    }

    @Override
    public int getTankCapacity(int tank) {
        return FluidVariant.toMillibuckets(capacity);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return !stack.isEmpty() && ItemFluids.of(FluidVariant.of(stack), 1).canCombine(fluids);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }
        ItemFluids incoming = ItemFluids.of(FluidVariant.of(resource), (int)FluidVariant.toDroplets(resource.getAmount()));
        if (!incoming.canCombine(this.fluids)) {
            return 0;
        }
        int accepted = (int)Math.min(Math.max(0, capacity - fluids.amount()), incoming.amount());
        accepted -= accepted % FluidVariant.DROPLETS_PER_MB;
        if (accepted > 0 && action.execute()) {
            this.fluids = incoming.ofAmount(this.fluids.amount() + accepted);
            changeCallback.onLevelChange(this, accepted);
        }
        return FluidVariant.toMillibuckets(accepted);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || fluids.isEmpty() || !ItemFluids.of(FluidVariant.of(resource), 1).canCombine(fluids)) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0 || fluids.isEmpty()) {
            return FluidStack.EMPTY;
        }
        int provided = (int)Math.min(FluidVariant.toDroplets(maxDrain), fluids.amount());
        provided -= provided % FluidVariant.DROPLETS_PER_MB;
        if (provided <= 0) {
            return FluidStack.EMPTY;
        }
        FluidStack result = fluids.toVariant().toStack(provided);
        if (action.execute()) {
            this.fluids = this.fluids.ofAmount(this.fluids.amount() - provided);
            changeCallback.onLevelChange(this, -provided);
        }
        return result;
    }

    public void clear() {
        int amount = fluids.amount();
        fluids = ItemFluids.EMPTY;
        if (amount > 0) {
            changeCallback.onLevelChange(this, -amount);
        }
    }

    @Override
    public void toNbt(CompoundTag compound, Provider lookup) {
        compound.put("fluid", fluids.encode());
    }

    @Override
    public void fromNbt(CompoundTag compound, Provider lookup) {
        if (compound.contains("stack", Tag.TAG_COMPOUND)) {
            fluids = ItemFluids.of(ItemStack.parseOptional(lookup, compound.getCompound("stack")));
        } else {
            fluids = ItemFluids.decode(compound.get("fluid"));
        }
    }

    public interface ChangeListener {
        void onLevelChange(Resovoir resovoir, int change);
    }

}
