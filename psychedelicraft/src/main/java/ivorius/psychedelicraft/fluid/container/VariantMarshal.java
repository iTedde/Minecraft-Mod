package ivorius.psychedelicraft.fluid.container;

import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.item.FilledBucketItem;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.item.component.PSComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/**
 * Bridges Psychedelicraft's own item fluids ({@link PSComponents#FLUIDS}) and the NeoForge item fluid capability
 * (upstream: Fabric Transfer API).
 */
public final class VariantMarshal {
    public static void bootstrap() {
        FilledBucketItem.registerDispenserBehaviour(Items.MILK_BUCKET);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item.components().has(PSComponents.FLUID_CAPACITY)) {
                event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new ItemFluidsHandler(stack), item);
            }
        }
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new MilkBucketHandler(stack), Items.MILK_BUCKET);
    }

    /**
     * Transaction on a foreign fluid container, through the NeoForge capability.
     */
    public static final class FabricTransaction implements ItemFluids.Transaction {
        private ItemStack stack;
        private ItemFluids fluids;

        public FabricTransaction(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int capacity() {
            return (int)FluidTransferUtils.getCapacity(stack);
        }

        @Override
        public ItemFluids fluids() {
            if (fluids == null) {
                fluids = FluidTransferUtils.getContents(stack).map(contents -> {
                    return ItemFluids.of(contents.getB(), contents.getA().intValue());
                }).orElse(ItemFluids.EMPTY);
            }
            return fluids;
        }

        @Override
        public ItemFluids withdraw(int amount) {
            var result = FluidTransferUtils.extract(stack, amount);
            stack = result.getA();
            fluids = null;
            return result.getB().map(fluid -> {
                return ItemFluids.of(fluid.getB(), fluid.getA().intValue());
            }).orElse(ItemFluids.EMPTY);
        }

        @Override
        public ItemFluids deposit(ItemFluids fluids, int maxAmount) {
            maxAmount = Math.min(maxAmount, fluids.amount());
            var result = FluidTransferUtils.deposit(stack, fluids.toVariant(), maxAmount);
            stack = result.getA();
            this.fluids = null;
            int inserted = maxAmount - result.getB().intValue();
            return fluids.ofAmount(fluids.amount() - inserted);
        }

        @Override
        public ItemStack toItemStack() {
            return stack;
        }
    }

    /**
     * Exposes the fluids of a Psychedelicraft container item to other mods.
     */
    public static final class ItemFluidsHandler implements IFluidHandlerItem {
        private ItemStack stack;

        public ItemFluidsHandler(ItemStack stack) {
            this.stack = stack;
        }

        private int capacityDroplets() {
            FluidCapacity capacity = stack.get(PSComponents.FLUID_CAPACITY);
            return capacity == null ? 0 : capacity.capacity();
        }

        @Override
        public ItemStack getContainer() {
            return stack;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            ItemFluids fluids = ItemFluids.direct(stack);
            return fluids.isEmpty() ? FluidStack.EMPTY : fluids.toVariant().toStack(fluids.amount());
        }

        @Override
        public int getTankCapacity(int tank) {
            return FluidVariant.toMillibuckets(capacityDroplets());
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return !stack.isEmpty();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || stack.getCount() != 1) {
                return 0;
            }
            ItemFluids input = ItemFluids.of(FluidVariant.of(resource), (int)FluidVariant.toDroplets(resource.getAmount()));
            ItemFluids.Transaction t = new ItemFluids.DirectTransaction(stack.copy(), capacityDroplets(), ItemFluids.direct(stack));
            ItemFluids remainder = t.deposit(input);
            int accepted = input.amount() - remainder.amount();
            accepted -= accepted % FluidVariant.DROPLETS_PER_MB;
            if (accepted <= 0) {
                return 0;
            }
            if (action.execute()) {
                ItemFluids.Transaction exact = new ItemFluids.DirectTransaction(stack, capacityDroplets(), ItemFluids.direct(stack));
                exact.deposit(input, accepted);
                stack = exact.toItemStack();
            }
            return FluidVariant.toMillibuckets(accepted);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            ItemFluids fluids = ItemFluids.direct(stack);
            if (resource.isEmpty() || fluids.isEmpty() || !fluids.canCombine(ItemFluids.of(FluidVariant.of(resource), 1))) {
                return FluidStack.EMPTY;
            }
            return drain(resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            ItemFluids fluids = ItemFluids.direct(stack);
            if (maxDrain <= 0 || fluids.isEmpty() || stack.getCount() != 1) {
                return FluidStack.EMPTY;
            }
            int amount = (int)Math.min(FluidVariant.toDroplets(maxDrain), fluids.amount());
            amount -= amount % FluidVariant.DROPLETS_PER_MB;
            if (amount <= 0) {
                return FluidStack.EMPTY;
            }
            FluidStack result = fluids.toVariant().toStack(amount);
            if (action.execute()) {
                ItemFluids.Transaction t = new ItemFluids.DirectTransaction(stack, capacityDroplets(), fluids);
                t.withdraw(amount);
                stack = t.toItemStack();
            }
            return result;
        }
    }

    /**
     * A milk bucket holds one bucket of Psychedelicraft milk.
     */
    static final class MilkBucketHandler implements IFluidHandlerItem {
        private ItemStack stack;

        MilkBucketHandler(ItemStack stack) {
            this.stack = stack;
        }

        private FluidStack milk() {
            return FluidVariant.of(PSFluids.MILK.getPhysical().getStandingFluid()).toStack(FluidVariant.BUCKET);
        }

        @Override
        public ItemStack getContainer() {
            return stack;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return stack.is(Items.MILK_BUCKET) ? milk() : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return 1000;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.isSameFluidSameComponents(resource, milk()) ? drain(resource.getAmount(), action) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain < 1000 || stack.getCount() != 1 || !stack.is(Items.MILK_BUCKET)) {
                return FluidStack.EMPTY;
            }
            FluidStack result = milk();
            if (action.execute()) {
                stack = new ItemStack(Items.BUCKET);
            }
            return result;
        }
    }
}
