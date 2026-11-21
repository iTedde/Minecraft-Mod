package ivorius.psychedelicraft.fluid.container;

import java.util.Optional;

import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/**
 * Access to fluid containers of other mods through the NeoForge item fluid capability (upstream: Fabric Transfer API).
 * All amounts are in droplets ({@link FluidVariant#BUCKET} per bucket).
 */
public interface FluidTransferUtils {
    private static Optional<IFluidHandlerItem> handler(ItemStack stack) {
        return stack.isEmpty() ? Optional.empty() : Optional.ofNullable(stack.getCapability(Capabilities.FluidHandler.ITEM));
    }

    static Tuple<ItemStack, Optional<Tuple<Long, FluidVariant>>> extract(ItemStack stack, long amount) {
        ItemStack copy = stack.copyWithCount(1);
        return handler(copy).map(handler -> {
            FluidStack drained = handler.drain(FluidVariant.toMillibuckets(amount), FluidAction.EXECUTE);
            if (drained.isEmpty()) {
                return new Tuple<ItemStack, Optional<Tuple<Long, FluidVariant>>>(stack, Optional.empty());
            }
            return new Tuple<ItemStack, Optional<Tuple<Long, FluidVariant>>>(handler.getContainer(),
                    Optional.of(new Tuple<>(FluidVariant.toDroplets(drained.getAmount()), FluidVariant.of(drained))));
        }).orElseGet(() -> new Tuple<>(stack, Optional.empty()));
    }

    /**
     * @return the resulting stack and the amount that could <em>not</em> be inserted
     */
    static Tuple<ItemStack, Long> deposit(ItemStack stack, FluidVariant variant, long amount) {
        ItemStack copy = stack.copyWithCount(1);
        return handler(copy).map(handler -> {
            int filled = handler.fill(variant.toStack(amount), FluidAction.EXECUTE);
            if (filled <= 0) {
                return new Tuple<>(stack, amount);
            }
            return new Tuple<>(handler.getContainer(), amount - FluidVariant.toDroplets(filled));
        }).orElseGet(() -> new Tuple<>(stack, amount));
    }

    static Optional<Tuple<Long, FluidVariant>> getContents(ItemStack stack) {
        return handler(stack.copyWithCount(1))
                .filter(h -> h.getTanks() > 0 && !h.getFluidInTank(0).isEmpty())
                .map(h -> {
                    FluidStack fluid = h.getFluidInTank(0);
                    return new Tuple<>(FluidVariant.toDroplets(fluid.getAmount()), FluidVariant.of(fluid));
                });
    }

    static long getCapacity(ItemStack stack) {
        return handler(stack.copyWithCount(1))
                .filter(h -> h.getTanks() > 0)
                .map(h -> FluidVariant.toDroplets(h.getTankCapacity(0)))
                .orElse(0L);
    }
}
