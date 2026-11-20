package ivorius.psychedelicraft.block.entity.contents;

import java.util.ArrayList;
import java.util.List;
import com.mojang.datafixers.util.Either;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.BlockWithFluid;
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
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SmallContents implements BurnerBlockEntity.CraftableContents, BlockWithFluid.DirectionalFluidResovoir, Resovoir.ChangeListener {
    public static final ResourceLocation ID = Psychedelicraft.id("small");
    protected List<Resovoir> auxiliaryTanks = new ArrayList<>();
    private static final VoxelShape SHAPE = ShapeUtil.createCenteredShape(1.5F, 8, 1.5F);

    protected int capacity;
    protected final BurnerBlockEntity entity;

    public SmallContents(BurnerBlockEntity entity, int capacity, ItemStack stack) {
        this.entity = entity;
        this.capacity = capacity;
        auxiliaryTanks.add(createTank());
        loadContents(stack);
    }

    @Override
    public int getTotalFluidVolume() {
        return BurnerBlockEntity.CraftableContents.super.getTotalFluidVolume();
    }

    @Override
    public VoxelShape getOutlineShape() {
        return SHAPE;
    }

    protected void loadContents(ItemStack stack) {
        getPrimaryTank().deposit(ItemFluids.of(stack));
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    protected Resovoir createTank() {
        return new Resovoir(capacity, this);
    }

    @Override
    public void onLevelChange(Resovoir resovoir, int change) {
        setChanged();
        if (change > 0) {
            entity.setTemperature(entity.getTemperature() / 2);
        }
        if (resovoir.getContents().isEmpty()) {
            synchronized (auxiliaryTanks) {
                auxiliaryTanks.removeIf(r -> r.getContents().isEmpty());
                if (auxiliaryTanks.isEmpty()) {
                    auxiliaryTanks.add(createTank());
                }
            }
        }
        if (change > 0) {
            entity.getLevel().playSound(null, entity.getBlockPos(), SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS);
        }
    }

    @Override
    public InteractionResultHolder<Contents> interact(ItemStack stack, Player player, InteractionHand hand, Direction side) {
        if (stack.isEmpty()) {
            if (!player.level().isClientSide) {
                player.setItemInHand(hand, ItemFluidsMixture.set(entity.getContainer(), getAuxiliaryTanks().stream().map(Resovoir::getContents).toList()));
                entity.setContainer(ItemStack.EMPTY);
                for (ItemStack ingredient : getCraftingIngredients().convertToItemStacks()) {
                    if (!player.addItem(ingredient)) {
                        Block.popResource(player.level(), entity.getBlockPos(), ingredient);
                    }
                }
                clearContent();
            }
            entity.playSound(player, SoundEvents.ITEM_PICKUP);

            return InteractionResultHolder.success(new EmptyContents(entity));
        }

        if (FluidCapacity.get(stack) > 0) {
            return interactWithFluidVessel(stack, player, hand, side);
        }

        return InteractionResultHolder.fail(this);
    }

    protected InteractionResultHolder<Contents> interactWithFluidVessel(ItemStack stack, Player player, InteractionHand hand, Direction side) {
        ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack);
        if (!t.fluids().isEmpty() && getPrimaryTank().deposit(t, t.fluids().amount()) > 0) {
            entity.playSound(player, SoundEvents.BOTTLE_EMPTY);
            if (!player.level().isClientSide) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, t.toItemStack()));
            }
            return InteractionResultHolder.success(this);
        }

        return InteractionResultHolder.fail(this);
    }

    @Override
    public Either<PipeFluids, Unit> tryInsert(ServerLevel world, BlockState state, BlockPos pos, Direction direction, PipeFluids fluids) {
        if (direction != Direction.DOWN) {
            return PipeInsertable.reject(fluids);
        }

        FluidMound remainder = FluidMound.of(fluids.fluids());

        fluids.fluids().getFluids().forEach(fluid -> {
            int transferred = getPrimaryTank().deposit(fluid);
            if (transferred > 0) {
                remainder.remove(fluid.ofAmount(transferred));
            }
        });

        return PipeInsertable.reject(fluids.withFluids(remainder));
    }

    @Override
    public void clientTick(Level world) {

    }

    @Override
    public void tick(ServerLevel world) {

    }

    @Override
    public ItemMound getCraftingIngredients() {
        return new ItemMound();
    }

    @Override
    public void onCraft(BunsenBurnerRecipe.Input input) {

    }

    @Override
    public void produceProducts(ServerLevel world, BlockPos pipePos, BunsenBurnerRecipe.Product product) {
        if (!product.items().isEmpty()) {
            Block.popResource(world, entity.getBlockPos(), product.items().removeFirst());
        }
        ItemFluids fluid = product.fluids().split(100);
        getPrimaryTank().drain(fluid.amount());
        if (!PipeInsertable.tryInsert(world, pipePos, Direction.UP, PipeFluids.of(fluid, new Impurities(product.impurities()), 15)).equals(STATUS_ACCEPT_ALL)) {
            onFluidWasted(world);
        }
    }

    @Override
    public void setChanged() {
        entity.setChanged();
    }

    protected void onFluidWasted(ServerLevel world) {
        world.sendParticles(ParticleTypes.DUST_PLUME,
                entity.getBlockPos().getX() + world.getRandom().triangle(0.5F, 0.1F),
                entity.getBlockPos().getY() + 0.6F,
                entity.getBlockPos().getZ() + world.getRandom().triangle(0.5F, 0.1F),
                2, 0, 0, 0, 0);
    }

    @Override
    public Resovoir getPrimaryTank() {
        synchronized (auxiliaryTanks) {
            if (auxiliaryTanks.isEmpty()) {
                auxiliaryTanks.add(createTank());
            }
            return auxiliaryTanks.get(0);
        }
    }

    public Resovoir getLastTank() {
        synchronized (auxiliaryTanks) {
            return auxiliaryTanks.isEmpty() ? getPrimaryTank() : auxiliaryTanks.get(auxiliaryTanks.size() - 1);
        }
    }

    @Override
    public Resovoir getTankOnSide(Direction direction) {
        return getPrimaryTank();
    }

    @Override
    public List<Resovoir> getAuxiliaryTanks() {
        synchronized (auxiliaryTanks) {
            return new ArrayList<>(auxiliaryTanks);
        }
    }

    @Override
    public void clearContent() {
        synchronized (auxiliaryTanks) {
            auxiliaryTanks.clear();
        }
        setChanged();
    }

    @Override
    public void toNbt(CompoundTag compound, Provider lookup) {
        compound.putInt("capacity", capacity);
        compound.put("fluids", NbtSerialisable.fromList(getAuxiliaryTanks(), lookup));
    }

    @Override
    public void fromNbt(CompoundTag compound, Provider lookup) {
        capacity = compound.getInt("capacity");
        auxiliaryTanks = NbtSerialisable.toList(new ArrayList<>(), compound.getList("fluids", Tag.TAG_COMPOUND), lookup, this::createTank);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return getTotalFluidVolume() == 0;
    }

    @Override
    public ItemStack getItem(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {

    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public List<ItemStack> getDroppedStacks(ItemStack container) {
        if (!isEmpty()) {
            synchronized (auxiliaryTanks) {
                if (auxiliaryTanks.size() == 1) {
                    return List.of(ItemFluids.set(container.copy(), getPrimaryTank().getContents()));
                }
                return List.of(ItemFluidsMixture.set(container.copy(), auxiliaryTanks.stream().map(Resovoir::getContents).toList()));
            }
        }
        return List.of(container);
    }

    @Override
    public ItemStack getFilled(ItemStack container, boolean dryRun, float drainPercentage) {
        if (!isEmpty()) {
            synchronized (auxiliaryTanks) {
                if (auxiliaryTanks.size() == 1) {
                    return ItemFluids.set(container.copy(), copyOrWithdraw(getPrimaryTank(), dryRun, drainPercentage));
                }

                return ItemFluidsMixture.set(container.copy(), auxiliaryTanks.stream()
                        .map(tank -> copyOrWithdraw(tank, dryRun, drainPercentage))
                        .toList());
            }
        }
        return container;
    }

    private ItemFluids copyOrWithdraw(Resovoir tank, boolean dryRun, float drainPercentage) {
        int amount = Mth.ceil(tank.getContents().amount() * drainPercentage);
        return dryRun ? tank.getContents().ofAmount(Math.min(amount, tank.getContents().amount())) : tank.drain(amount);
    }
}
