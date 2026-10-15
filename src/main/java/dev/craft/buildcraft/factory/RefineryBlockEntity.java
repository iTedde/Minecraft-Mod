package dev.craft.buildcraft.factory;

import dev.craft.buildcraft.BcBlockEntityTypes;
import dev.craft.buildcraft.menu.RefineryMenu;
import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.power.PowerReceptor;
import dev.craft.buildcraft.power.SafeTimeTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

/**
 * BuildCraft {@code TileRefinery}: two ingredient tanks with optional filters and a result tank (4000 mB each); one
 * recipe operation per {@code delay} ticks while enough MJ are stored.
 */
public class RefineryBlockEntity extends BlockEntity implements PowerReceptor, MenuProvider {
    public static final int LIQUID_PER_SLOT = 4000;

    private final PowerProvider provider = new PowerProvider();
    private final FluidTank ingredient1 = tank();
    private final FluidTank ingredient2 = tank();
    private final FluidTank result = tank();
    private final IFluidHandler fluidHandler = new Handler();
    /** Filter liquids; {@link Fluids#EMPTY} (registry id 0, like liquid id 0 in 1.4.7) means none. */
    private final Fluid[] filters = {Fluids.EMPTY, Fluids.EMPTY};
    private final ContainerData filterData = new ContainerData() {
        @Override
        public int get(int index) {
            return BuiltInRegistries.FLUID.getId(filters[index]);
        }

        @Override
        public void set(int index, int value) {
            filters[index] = BuiltInRegistries.FLUID.byId(value);
        }

        @Override
        public int getCount() {
            return 2;
        }
    };
    private final SafeTimeTracker time = new SafeTimeTracker();
    private final SafeTimeTracker updateNetworkTime = new SafeTimeTracker();
    private float animationSpeed = 1.0F;
    private int animationStage;

    public RefineryBlockEntity(BlockPos pos, BlockState state) {
        super(BcBlockEntityTypes.REFINERY.get(), pos, state);
        provider.configure(25, 100, 25, 1000);
    }

    private FluidTank tank() {
        return new FluidTank(LIQUID_PER_SLOT) {
            @Override
            protected void onContentsChanged() {
                setChanged();
            }
        };
    }

    @Override
    public PowerProvider getPowerProvider() {
        return provider;
    }

    @Override
    public void doWork() {
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public FluidTank getIngredient1() {
        return ingredient1;
    }

    public FluidTank getIngredient2() {
        return ingredient2;
    }

    public FluidTank getResult() {
        return result;
    }

    public Fluid getFilter(int number) {
        return filters[number];
    }

    public void setFilter(int number, Fluid liquid) {
        filters[number] = liquid;
        setChanged();
    }

    public ContainerData getFilterData() {
        return filterData;
    }

    public float getAnimationSpeed() {
        return animationSpeed;
    }

    public int getAnimationStage() {
        return animationStage;
    }

    // --- ticking ---

    public void serverTick() {
        provider.update(this, level);
        if (updateNetworkTime.markTimeIfDelay(level, 2)) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
        RefineryRecipe currentRecipe = RefineryRecipe.find(ingredient1.getFluid(), ingredient2.getFluid());
        if (currentRecipe == null) {
            decreaseAnimation();
        } else if (!result.isEmpty()
                && !FluidStack.isSameFluidSameComponents(result.getFluid(), currentRecipe.result())) {
            decreaseAnimation();
        } else if (result.fill(currentRecipe.result().copy(), IFluidHandler.FluidAction.SIMULATE)
                != currentRecipe.result().getAmount()) {
            decreaseAnimation();
        } else if (containsInput(currentRecipe.ingredient1()) && containsInput(currentRecipe.ingredient2())) {
            if (provider.getEnergyStored() >= currentRecipe.energy()) {
                increaseAnimation();
            } else {
                decreaseAnimation();
            }
            if (time.markTimeIfDelay(level, currentRecipe.delay())) {
                float energyUsed = provider.useEnergy(currentRecipe.energy(), currentRecipe.energy(), true);
                if (energyUsed != 0.0F && consumeInput(currentRecipe.ingredient1())
                        && consumeInput(currentRecipe.ingredient2())) {
                    result.fill(currentRecipe.result().copy(), IFluidHandler.FluidAction.EXECUTE);
                }
            }
        } else {
            decreaseAnimation();
        }
        setChanged();
    }

    public void clientTick() {
        if (animationSpeed > 1.0F) {
            animationStage = (int) (animationStage + animationSpeed);
            if (animationStage > 300) {
                animationStage = 100;
            }
        } else if (animationStage > 0) {
            animationStage--;
        }
    }

    /** {@code LiquidStack.containsLiquid}: same liquid and at least the amount. */
    private static boolean contains(FluidStack stored, FluidStack liquid) {
        return !stored.isEmpty() && FluidStack.isSameFluidSameComponents(stored, liquid)
                && stored.getAmount() >= liquid.getAmount();
    }

    private boolean containsInput(FluidStack liquid) {
        return liquid.isEmpty() || contains(ingredient1.getFluid(), liquid) || contains(ingredient2.getFluid(), liquid);
    }

    private boolean consumeInput(FluidStack liquid) {
        if (liquid.isEmpty()) {
            return true;
        }
        if (contains(ingredient1.getFluid(), liquid)) {
            ingredient1.drain(liquid.getAmount(), IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        if (contains(ingredient2.getFluid(), liquid)) {
            ingredient2.drain(liquid.getAmount(), IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        return false;
    }

    private void increaseAnimation() {
        if (animationSpeed < 2.0F) {
            animationSpeed = 2.0F;
        } else if (animationSpeed <= 5.0F) {
            animationSpeed = (float) (animationSpeed + 0.1);
        }
        animationStage = (int) (animationStage + animationSpeed);
        if (animationStage > 300) {
            animationStage = 100;
        }
    }

    private void decreaseAnimation() {
        if (animationSpeed >= 1.0F) {
            animationSpeed = (float) (animationSpeed - 0.1);
            animationStage = (int) (animationStage + animationSpeed);
            if (animationStage > 300) {
                animationStage = 100;
            }
        } else if (animationStage > 0) {
            animationStage--;
        }
    }

    /** {@code fill}: without filters ingredient 1 first, then 2; with a filter only into the matching tanks. */
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }
        int used = 0;
        FluidStack resourceUsing = resource.copy();
        if (filters[0] == Fluids.EMPTY && filters[1] == Fluids.EMPTY) {
            used += ingredient1.fill(resourceUsing, action);
            resourceUsing.shrink(used);
            used += ingredient2.fill(resourceUsing, action);
        } else {
            if (resource.is(filters[0])) {
                used += ingredient1.fill(resourceUsing, action);
            }
            resourceUsing.shrink(used);
            if (resource.is(filters[1]) && !resourceUsing.isEmpty()) {
                used += ingredient2.fill(resourceUsing, action);
            }
        }
        if (action.execute() && used > 0) {
            updateNetworkTime.markTime(level);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
        return used;
    }

    // --- menu ---

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new RefineryMenu(id, inventory, this);
    }

    // --- persistence and sync ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveTanks(tag, registries);
        tag.putInt("animationStage", animationStage);
        tag.putFloat("animationSpeed", animationSpeed);
        tag.put("power", provider.save());
        for (int i = 0; i < 2; i++) {
            tag.putString("filters_" + i, BuiltInRegistries.FLUID.getKey(filters[i]).toString());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadTanks(tag, registries);
        animationStage = tag.getInt("animationStage");
        animationSpeed = tag.contains("animationSpeed") ? tag.getFloat("animationSpeed") : 1.0F;
        if (tag.contains("power")) {
            provider.load(tag.getCompound("power"));
        }
        for (int i = 0; i < 2; i++) {
            ResourceLocation key = ResourceLocation.tryParse(tag.getString("filters_" + i));
            filters[i] = key == null ? Fluids.EMPTY : BuiltInRegistries.FLUID.get(key);
        }
    }

    private void saveTanks(CompoundTag tag, HolderLookup.Provider registries) {
        if (!ingredient1.isEmpty()) {
            tag.put("ingredient1", ingredient1.getFluid().save(registries));
        }
        if (!ingredient2.isEmpty()) {
            tag.put("ingredient2", ingredient2.getFluid().save(registries));
        }
        if (!result.isEmpty()) {
            tag.put("result", result.getFluid().save(registries));
        }
    }

    private void loadTanks(CompoundTag tag, HolderLookup.Provider registries) {
        ingredient1.setFluid(FluidStack.parseOptional(registries, tag.getCompound("ingredient1")));
        ingredient2.setFluid(FluidStack.parseOptional(registries, tag.getCompound("ingredient2")));
        result.setFluid(FluidStack.parseOptional(registries, tag.getCompound("result")));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveTanks(tag, registries);
        tag.putFloat("animationSpeed", animationSpeed);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        readSync(tag, registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        readSync(packet.getTag(), registries);
    }

    private void readSync(CompoundTag tag, HolderLookup.Provider registries) {
        loadTanks(tag, registries);
        animationSpeed = tag.getFloat("animationSpeed");
    }

    private final class Handler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 3;
        }

        private FluidTank get(int index) {
            return index == 0 ? ingredient1 : index == 1 ? ingredient2 : result;
        }

        @Override
        public FluidStack getFluidInTank(int index) {
            return get(index).getFluid();
        }

        @Override
        public int getTankCapacity(int index) {
            return LIQUID_PER_SLOT;
        }

        @Override
        public boolean isFluidValid(int index, FluidStack stack) {
            return index < 2;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return RefineryBlockEntity.this.fill(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return result.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return result.drain(maxDrain, action);
        }
    }
}
