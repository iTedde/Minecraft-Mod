package ivorius.psychedelicraft.block.entity.contents;

import java.util.List;
import com.mojang.datafixers.util.Either;

import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PipeInsertable;
import ivorius.psychedelicraft.block.ShapeUtil;
import ivorius.psychedelicraft.block.entity.BurnerBlockEntity;
import ivorius.psychedelicraft.block.entity.BurnerBlockEntity.Contents;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.Impurities;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.item.component.ItemFluidsMixture;
import ivorius.psychedelicraft.recipe.BunsenBurnerRecipe;
import ivorius.psychedelicraft.recipe.FluidMound;
import ivorius.psychedelicraft.recipe.ItemMound;
import ivorius.psychedelicraft.recipe.PSRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LargeContents extends SmallContents {
    public static final ResourceLocation ID = Psychedelicraft.id("large");
    static final int MAX_INGREDIENTS = 4;
    static final int[] CONTAINER_SLOT_ID = {0};
    static final int[] INGREDIENT_SLOT_ID = {1, 2, 3, 4};
    static final VoxelShape SHAPE = ShapeUtil.createCenteredShape(5, 18, 5);

    private ItemMound ingredients = new ItemMound();

    public LargeContents(BurnerBlockEntity entity, int capacity, ItemStack stack) {
        super(entity, capacity, stack);
    }

    @Override
    public VoxelShape getOutlineShape() {
        return SHAPE;
    }

    @Override
    protected void loadContents(ItemStack stack) {
        ItemFluids.allOf(stack).forEach(this::deposit);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public ItemMound getIngredients() {
        return ingredients;
    }

    @Override
    public InteractionResultHolder<Contents> interact(ItemStack stack, Player player, InteractionHand hand, Direction side) {
        InteractionResultHolder<Contents> result = super.interact(stack, player, hand, side);
        if (result.getResult().consumesAction()) {
            return result;
        }

        if (ingredients.size() < MAX_INGREDIENTS
                && ingredients.getCount(stack.getItem()) < 5) {
            if (isValidIngredient(stack)) {
                if (!player.level().isClientSide) {
                    ingredients.addStack(stack.consumeAndReturn(1, player));
                    player.setItemInHand(hand, stack);
                    entity.playSound(null, PSSounds.BLOCK_BUNSEN_BURNER_FILL);
                }
                return InteractionResultHolder.success(this);
            }
        }

        return InteractionResultHolder.fail(this);
    }

    @Override
    protected InteractionResultHolder<Contents> interactWithFluidVessel(ItemStack stack, Player player, InteractionHand hand, Direction side) {
        if (!ItemFluids.of(stack).isEmpty()) {
            ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack);
            if (deposit(t)) {
                entity.playSound(player, SoundEvents.BOTTLE_EMPTY);
                if (!player.level().isClientSide) {
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, t.toItemStack()));
                }
                return InteractionResultHolder.success(this);
            }
            return InteractionResultHolder.fail(this);
        }

        ItemFluidsMixture mixture = ItemFluidsMixture.of(stack);
        if (!mixture.isEmpty()) {
            if (!player.level().isClientSide) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, ItemFluidsMixture.set(stack.copyWithCount(1), mixture.fluids().stream().map(this::deposit).toList())));
            }
            entity.playSound(player, SoundEvents.BOTTLE_EMPTY);
            return InteractionResultHolder.success(this);
        }

        Resovoir tank = player.isShiftKeyDown() ? getLastTank() : getPrimaryTank();
        if (!tank.getContents().isEmpty()) {
            ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack.copyWithCount(1));
            if (tank.withdraw(t, FluidCapacity.get(stack)) > 0) {
                if (!player.level().isClientSide) {
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, t.toItemStack()));
                }
                entity.playSound(player, SoundEvents.BOTTLE_FILL);
                return InteractionResultHolder.success(this);
            }
        }

        return InteractionResultHolder.fail(this);
    }

    protected boolean deposit(ItemFluids.Transaction t) {
        synchronized (auxiliaryTanks) {
            int maxToInsert = Math.max(0, capacity - getTotalFluidVolume());
            int transferred = 0;
            for (Resovoir auxTank : auxiliaryTanks) {
                transferred = auxTank.deposit(t, maxToInsert);
                if (transferred > 0) {
                    return true;
                }
            }
            if (auxiliaryTanks.size() < 4) {
                Resovoir auxTank = createTank();
                transferred = auxTank.deposit(t, maxToInsert);
                if (transferred > 0) {
                    auxiliaryTanks.add(auxTank);
                    entity.setChanged();
                    return true;
                }
            }
            return false;
        }
    }

    protected ItemFluids deposit(ItemFluids stack) {
        synchronized (auxiliaryTanks) {
            if (stack.isEmpty()) {
                return stack;
            }
            int maxToInsert = Math.min(stack.amount(), Math.max(0, capacity - getTotalFluidVolume()));
            ItemFluids toInsert = stack.ofAmount(maxToInsert);
            int transferred = 0;
            for (Resovoir auxTank : auxiliaryTanks) {
                transferred = auxTank.deposit(toInsert);
                if (transferred > 0) {
                    return stack.ofAmount(stack.amount() - transferred);
                }
            }
            if (auxiliaryTanks.size() < 4) {
                Resovoir auxTank = createTank();
                transferred = auxTank.deposit(toInsert);
                if (transferred > 0) {
                    auxiliaryTanks.add(auxTank);
                    entity.setChanged();
                    return stack.ofAmount(stack.amount() - transferred);
                }
            }
            return stack;
        }
    }

    @Override
    public Either<PipeFluids, Unit> tryInsert(ServerLevel world, BlockState state, BlockPos pos, Direction direction, PipeFluids fluids) {
        if (direction != Direction.DOWN) {
            return PipeInsertable.reject(fluids);
        }

        FluidMound mound = FluidMound.of(fluids.fluids());

        synchronized (auxiliaryTanks) {
            fluids.fluids().getFluids().forEach(fluid -> {
                for (Resovoir auxTank : auxiliaryTanks) {
                    int transferred = auxTank.deposit(fluid);
                    if (transferred > 0) {
                        mound.remove(fluid.ofAmount(transferred));
                        return;
                    }
                }
                if (auxiliaryTanks.size() < 4) {
                    Resovoir auxTank = createTank();
                    int transferred = auxTank.deposit(fluid);
                    if (transferred > 0) {
                        auxiliaryTanks.add(auxTank);
                        mound.remove(fluid.ofAmount(transferred));
                        entity.setChanged();
                    }
                }
            });
        }

        return PipeInsertable.reject(PipeFluids.of(mound, fluids.impurities(), fluids.temperature()));
    }

    private boolean isValidIngredient(ItemStack stack) {
        return FluidCapacity.get(stack) == 0 && entity.getLevel().getRecipeManager()
                .getAllRecipesFor(PSRecipes.CHEMISTRY)
                .stream()
                .anyMatch(recipe -> recipe.value().getIngredients().stream().anyMatch(i -> i.test(stack)));
    }

    @Override
    public ItemMound getCraftingIngredients() {
        return new ItemMound(ingredients);
    }

    @Override
    public void onCraft(BunsenBurnerRecipe.Input input) {
        ingredients = input.input();
    }

    @Override
    public void produceProducts(ServerLevel world, BlockPos pipePos, BunsenBurnerRecipe.Product product) {
        if (!product.items().isEmpty()) {
            ingredients.addStack(product.items().removeFirst());
        }
        ItemFluids fluid = product.fluids().split(400);
        getPrimaryTank().drain(fluid.amount());
        if (!PipeInsertable.tryInsert(world, pipePos, Direction.UP, PipeFluids.of(fluid, new Impurities(product.impurities()), 15)).equals(STATUS_ACCEPT_ALL)) {
            onFluidWasted(world);
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        ingredients.clear();
    }

    @Override
    public void toNbt(CompoundTag compound, Provider lookup) {
        super.toNbt(compound, lookup);
        compound.put("ingredients", ingredients.toNbt(lookup));
    }

    @Override
    public void fromNbt(CompoundTag compound, Provider lookup) {
        super.fromNbt(compound, lookup);
        ingredients = new ItemMound(compound.getCompound("ingredients"), lookup);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return INGREDIENT_SLOT_ID;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot >= 0 && slot < MAX_INGREDIENTS;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot >= 0 && slot < MAX_INGREDIENTS;
    }

    @Override
    public int getContainerSize() {
        return MAX_INGREDIENTS + 1;
    }

    @Override
    public boolean isEmpty() {
        return ingredients.isEmpty() && super.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot >= ingredients.size()) {
            return ItemStack.EMPTY;
        }
        return ingredients.getStack(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ingredients.removeStack(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ingredients.removeStack(slot, Integer.MAX_VALUE);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (ingredients.size() < MAX_INGREDIENTS) {
            ingredients.addStack(stack);
        }
    }

    @Override
    public List<ItemStack> getDroppedStacks(ItemStack container) {
        NonNullList<ItemStack> stacks = ingredients.convertToItemStacks();
        if (!container.isEmpty()) {
            stacks.add(container);
        }
        return stacks;
    }
}
