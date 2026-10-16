package dev.craft.forestry.machine;

import java.util.ArrayList;
import java.util.List;

import dev.craft.buildcraft.fluid.LiquidContainers;
import dev.craft.forestry.FrBlockEntityTypes;
import dev.craft.forestry.FrRecipes;
import dev.craft.forestry.menu.CarpenterMenu;
import dev.craft.forestry.recipe.CarpenterRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Forestry {@code MachineCarpenter}: crafts the recipe shown by the pattern grid from the stock, using liquid from the
 * 10 000 mB tank; one product per {@code packagingTime} work steps.
 */
public class CarpenterBlockEntity extends ForestryMachineBlockEntity {
    public static final int TANK_CAPACITY = 10000;
    public static final int ACTIVATION_ENERGY = 25;
    public static final int SLOT_CRAFTING_1 = 0;
    public static final int SLOT_BOX = 9;
    public static final int SLOT_PRODUCT = 10;
    public static final int SLOT_CAN_INPUT = 11;
    public static final int SLOT_INVENTORY_1 = 12;
    public static final int SLOT_INVENTORY_COUNT = 18;
    public static final int SLOT_COUNT = 30;

    private final FluidTank resourceTank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            updateTankLevel();
        }
    };
    private final IFluidHandler fluidHandler = new FluidHandler();
    private final IItemHandler automation = new Automation();
    @Nullable
    private CarpenterRecipe currentRecipe;
    private boolean recipeLookupPending;
    private int packageTime;
    private int totalTime;
    private ItemStack currentProduct = ItemStack.EMPTY;
    private ItemStack pendingProduct = ItemStack.EMPTY;
    /** Open GUIs; with one open the recipe is looked up again after every product ({@code activeContainer}). */
    private int openMenus;
    /** Tank level shown by the renderer ({@code EnumTankLevel} ordinal), synced to the client. */
    private int tankLevel;

    public CarpenterBlockEntity(BlockPos pos, BlockState state) {
        super(FrBlockEntityTypes.CARPENTER.get(), pos, state, SLOT_COUNT, 1, 110, ACTIVATION_ENERGY, 400);
    }

    public FluidTank getResourceTank() {
        return resourceTank;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public IItemHandler getItemHandler() {
        return automation;
    }

    public int getPackageTime() {
        return packageTime;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public int getTankLevel() {
        return tankLevel;
    }

    @Nullable
    public CarpenterRecipe getCurrentRecipe() {
        return currentRecipe;
    }

    public void menuOpened() {
        openMenus++;
    }

    public void menuClosed() {
        openMenus = Math.max(0, openMenus - 1);
    }

    // --- machine ---

    @Override
    public void serverTick() {
        if (recipeLookupPending) {
            recipeLookupPending = false;
            currentRecipe = findRecipe();
        }
        super.serverTick();
    }

    @Override
    protected void updateServerSide() {
        long time = level.getGameTime();
        if (time % 20L != 0L) {
            return;
        }
        ItemStack can = inventory.getStackInSlot(SLOT_CAN_INPUT);
        if (!can.isEmpty()) {
            FluidStack liquid = LiquidContainers.getLiquidForFilledItem(can);
            if (!liquid.isEmpty() && isResourceLiquid(liquid)
                    && resourceTank.fill(liquid.copy(), IFluidHandler.FluidAction.SIMULATE) >= liquid.getAmount()) {
                resourceTank.fill(liquid, IFluidHandler.FluidAction.EXECUTE);
                inventory.setStackInSlot(SLOT_CAN_INPUT, LiquidContainers.consumeItem(can));
            }
        }
        if (time % 40L == 0L && currentRecipe == null) {
            currentRecipe = findRecipe();
        }
    }

    @Override
    protected boolean machineWork() {
        if (packageTime > 0) {
            packageTime--;
            if (currentRecipe == null || !ItemStack.isSameItemSameComponents(currentProduct, currentRecipe.getResult())
                    || !validateResources()) {
                currentProduct = ItemStack.EMPTY;
                packageTime = totalTime = 0;
                return false;
            }
            if (packageTime <= 0) {
                pendingProduct = currentProduct;
                currentProduct = ItemStack.EMPTY;
                totalTime = 0;
                removeResources(currentRecipe);
                if (openMenus > 0) {
                    updateRecipe();
                }
                return tryAddPending();
            }
            return true;
        }
        if (!pendingProduct.isEmpty()) {
            return tryAddPending();
        }
        if (currentRecipe != null) {
            if (!validateResources()) {
                return false;
            }
            packageTime = totalTime = currentRecipe.getPackagingTime();
            currentProduct = currentRecipe.getResult().copy();
            return true;
        }
        return false;
    }

    /** {@code isWorking}: shows the progress bar. */
    @Override
    public boolean isWorking() {
        return packageTime > 0 || !pendingProduct.isEmpty() || currentRecipe != null && validateResources();
    }

    private List<ItemStack> slots(int from, int count) {
        List<ItemStack> stacks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            stacks.add(inventory.getStackInSlot(from + i));
        }
        return stacks;
    }

    /** {@code validateResources}: enough liquid (the kind is not checked here), the box, one set in the stock. */
    private boolean validateResources() {
        FluidStack liquid = currentRecipe.getLiquid();
        if (!liquid.isEmpty() && resourceTank.getFluidAmount() < liquid.getAmount()) {
            return false;
        }
        if (currentRecipe.getBox().isPresent() && inventory.getStackInSlot(SLOT_BOX).isEmpty()) {
            return false;
        }
        return ItemSets.containsSets(slots(SLOT_CRAFTING_1, 9), slots(SLOT_INVENTORY_1, SLOT_INVENTORY_COUNT)) > 0;
    }

    private void removeResources(CarpenterRecipe recipe) {
        if (!recipe.getLiquid().isEmpty()) {
            resourceTank.drain(recipe.getLiquid().getAmount(), IFluidHandler.FluidAction.EXECUTE);
        }
        if (recipe.getBox().isPresent()) {
            inventory.extractItem(SLOT_BOX, 1, false);
        }
        ItemSets.removeSet(inventory, SLOT_INVENTORY_1, SLOT_INVENTORY_1 + SLOT_INVENTORY_COUNT,
                slots(SLOT_CRAFTING_1, 9), true);
    }

    private boolean tryAddPending() {
        ItemStack product = inventory.getStackInSlot(SLOT_PRODUCT);
        if (product.isEmpty()) {
            inventory.setStackInSlot(SLOT_PRODUCT, pendingProduct.copy());
            pendingProduct = ItemStack.EMPTY;
            return true;
        }
        if (ItemStack.isSameItemSameComponents(product, pendingProduct)
                && product.getCount() <= product.getMaxStackSize() - pendingProduct.getCount()) {
            inventory.setStackInSlot(SLOT_PRODUCT, product.copyWithCount(product.getCount() + pendingProduct.getCount()));
            pendingProduct = ItemStack.EMPTY;
            return true;
        }
        return false;
    }

    // --- recipes ---

    @Nullable
    private CarpenterRecipe findRecipe() {
        if (level == null) {
            return null;
        }
        CarpenterRecipe.Input input = new CarpenterRecipe.Input(resourceTank.getFluid(),
                inventory.getStackInSlot(SLOT_BOX), CraftingInput.of(3, 3, slots(SLOT_CRAFTING_1, 9)));
        return level.getRecipeManager().getRecipeFor(FrRecipes.CARPENTER.get(), input, level)
                .map(RecipeHolder::value).orElse(null);
    }

    /** {@code resetProductDisplay}: the GUI changed the pattern or the box. */
    public void updateRecipe() {
        currentRecipe = findRecipe();
    }

    private List<CarpenterRecipe> recipes() {
        return level.getRecipeManager().getAllRecipesFor(FrRecipes.CARPENTER.get()).stream()
                .map(RecipeHolder::value).toList();
    }

    /** {@code RecipeManager.isResourceLiquid}. */
    private boolean isResourceLiquid(FluidStack liquid) {
        return recipes().stream().anyMatch(recipe -> recipe.hasLiquid(liquid));
    }

    /** {@code RecipeManager.isBox}. */
    private boolean isBox(ItemStack stack) {
        return recipes().stream().anyMatch(recipe -> recipe.hasBox(stack));
    }

    // --- tank level for the renderer ---

    /** {@code Utils.rateTankLevel(getResourceScaled(100))}. */
    public static int rateTankLevel(int amount) {
        int scaled = amount * 100 / TANK_CAPACITY;
        if (scaled < 5) {
            return 0;
        } else if (scaled < 30) {
            return 1;
        } else if (scaled < 60) {
            return 2;
        }
        return scaled < 90 ? 3 : 4;
    }

    private void updateTankLevel() {
        int rated = rateTankLevel(resourceTank.getFluidAmount());
        if (rated != tankLevel) {
            tankLevel = rated;
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            }
        }
    }

    // --- menu, drops, persistence ---

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new CarpenterMenu(id, playerInventory, this);
    }

    /** The {@code IInventory} of the carpenter starts behind the pattern. */
    @Override
    protected int[] droppedSlots() {
        int[] slots = new int[SLOT_COUNT - SLOT_BOX];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = SLOT_BOX + i;
        }
        return slots;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("PackageTime", packageTime);
        tag.putInt("PackageTotalTime", totalTime);
        if (!resourceTank.isEmpty()) {
            tag.put("ResourceTank", resourceTank.getFluid().save(registries));
        }
        if (!pendingProduct.isEmpty()) {
            tag.put("PendingProduct", pendingProduct.save(registries));
        }
        if (!currentProduct.isEmpty()) {
            tag.put("CurrentProduct", currentProduct.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        packageTime = tag.getInt("PackageTime");
        totalTime = tag.getInt("PackageTotalTime");
        resourceTank.setFluid(FluidStack.parseOptional(registries, tag.getCompound("ResourceTank")));
        pendingProduct = ItemStack.parseOptional(registries, tag.getCompound("PendingProduct"));
        currentProduct = ItemStack.parseOptional(registries, tag.getCompound("CurrentProduct"));
        tankLevel = rateTankLevel(resourceTank.getFluidAmount());
        // readFromNBT looks the recipe up right away; the level is only available on the first tick here.
        recipeLookupPending = true;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("TankLevel", tankLevel);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        tankLevel = tag.getInt("TankLevel");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        tankLevel = packet.getTag().getInt("TankLevel");
    }

    /** {@code fill} accepts any liquid (no check in the original), nothing can be drained. */
    private final class FluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return resourceTank.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return TANK_CAPACITY;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return resourceTank.fill(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }

    /**
     * BuildCraft {@code ISpecialInventory} access: {@code addItem} puts recipe-liquid containers into the container
     * slot, ingredients of the current recipe into the stock and anything else the recipes accept as box into the box
     * slot; {@code extractItem} takes single products.
     */
    private final class Automation implements IItemHandler {
        private static final int PRODUCT = 0;
        private static final int CAN = 1;
        private static final int BOX = 2;
        private static final int STOCK_1 = 3;

        @Override
        public int getSlots() {
            return STOCK_1 + SLOT_INVENTORY_COUNT;
        }

        private int inventorySlot(int slot) {
            return switch (slot) {
                case PRODUCT -> SLOT_PRODUCT;
                case CAN -> SLOT_CAN_INPUT;
                case BOX -> SLOT_BOX;
                default -> SLOT_INVENTORY_1 + slot - STOCK_1;
            };
        }

        /** Which slot kind {@code addItem} chooses for {@code stack}, or -1. */
        private int target(ItemStack stack) {
            FluidStack liquid = LiquidContainers.getLiquidForFilledItem(stack);
            if (!liquid.isEmpty() && isResourceLiquid(liquid)) {
                return CAN;
            }
            if (currentRecipe != null && currentRecipe.isIngredient(stack)) {
                return STOCK_1;
            }
            return isBox(stack) ? BOX : -1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(inventorySlot(slot));
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? inventory.insertItem(inventorySlot(slot), stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == PRODUCT && amount > 0 ? inventory.extractItem(SLOT_PRODUCT, 1, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == PRODUCT || level == null) {
                return false;
            }
            int target = target(stack);
            return slot >= STOCK_1 ? target == STOCK_1 : target == slot;
        }
    }
}
