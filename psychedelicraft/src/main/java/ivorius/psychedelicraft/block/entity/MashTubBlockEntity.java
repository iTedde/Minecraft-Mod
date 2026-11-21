/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import java.util.*;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Either;

import ivorius.psychedelicraft.ParticleHelper;
import ivorius.psychedelicraft.block.MashTubBlock;
import ivorius.psychedelicraft.block.PipeInsertable;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.particle.DrugDustParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import ivorius.psychedelicraft.recipe.ItemMound;
import ivorius.psychedelicraft.recipe.MashingRecipe;
import ivorius.psychedelicraft.recipe.PSRecipes;
import ivorius.psychedelicraft.util.MathUtils;
import ivorius.psychedelicraft.util.NbtSerialisable;
import ivorius.psychedelicraft.fluid.container.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Created by lukas on 27.10.14.
 */
public class MashTubBlockEntity extends FluidProcessingBlockEntity {
    public ItemStack solidContents = ItemStack.EMPTY;
    private ItemFluids auxiliaryFluids = ItemFluids.EMPTY;

    private Optional<Stew> currentStew = Optional.empty();

    private final ItemMound suppliedIngredients = new ItemMound();

    public MashTubBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.MASH_TUB, pos, state, FluidVolumes.VAT);
    }

    public ItemMound getSuppliedIngredients() {
        return suppliedIngredients;
    }

    public ItemFluids getAuxiliaryFluids() {
        return auxiliaryFluids;
    }

    @Override
    public void accept(ItemStack stack) {
        if (level == null) {
            return;
        }
        if (!solidContents.isEmpty()) {
            if (ItemStack.isSameItemSameComponents(solidContents, stack)) {
                int maxToMove = Math.min(stack.getCount(), solidContents.getMaxStackSize() - solidContents.getCount());
                if (maxToMove > 0) {
                    stack.shrink(maxToMove);
                    solidContents.grow(maxToMove);
                    level.playSound(null, worldPosition, SoundEvents.COMPOSTER_READY, SoundSource.BLOCKS);
                    return;
                }
            }

            Block.popResource(level, worldPosition, solidContents);
            level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS);
        }
        solidContents = stack;
        level.playSound(null, worldPosition, SoundEvents.COMPOSTER_READY, SoundSource.BLOCKS);
    }

    @Override
    protected void onFluidRejected(ServerLevel world, ItemFluids fluids) {
        if (auxiliaryFluids.isEmpty()) {
            auxiliaryFluids = fluids;
        } else {
            if (auxiliaryFluids.canCombine(fluids)) {
                int spaceInTank = (int)(getPrimaryTank().getCapacity() - getPrimaryTank().getAmount());
                int maxInserted = Math.min(fluids.amount(), spaceInTank - auxiliaryFluids.amount());
                if (maxInserted > 0) {
                    auxiliaryFluids = auxiliaryFluids.ofAmount(auxiliaryFluids.amount() + maxInserted);
                    if (maxInserted >= fluids.amount()) {
                        return;
                    }
                    fluids = fluids.ofAmount(fluids.amount() - maxInserted);
                }
            }

            super.onFluidRejected(world, fluids);
        }
    }

    @Override
    public Processable.ProcessType getProcessType() {
        return Processable.ProcessType.FERMENT;
    }

    @Override
    public void tick(ServerLevel world) {
        if (isAcceptingIngredients()) {
            Vec3 center = getBlockPos().getCenter();
            AABB box = AABB.ofSize(center, 1.5, 0.5, 1.5);
            for (ItemEntity item : world.getEntitiesOfClass(ItemEntity.class, box, EntitySelector.ENTITY_STILL_ALIVE)) {
                ItemStack stack = item.getItem();
                if (isValidIngredient(world, stack)) {
                    suppliedIngredients.addStack(stack);
                    beginStewing(world);
                    markForUpdate();
                    spawnBubbles(20, 0, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP);
                    getLevel().playSound(null, getBlockPos(), SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1, 1);
                    item.discard();
                }
            }
        }

        if (currentStew.isEmpty()) {
            super.tick(world);
        }
        currentStew = currentStew.filter(Stew::tick);
    }

    @Override
    public void onLevelChange(Resovoir resovoir, int difference) {
        if (level == null) {
            return;
        }
        if (resovoir.getContents().isEmpty() && !auxiliaryFluids.isEmpty()) {
            resovoir.setContents(auxiliaryFluids, false);
            auxiliaryFluids = ItemFluids.EMPTY;
        }
        super.onLevelChange(resovoir, difference);

        int luminance = resovoir.getContents().fluid().getPhysical().getDefaultState().createLegacyBlock().getLightEmission();

        int currentLuminance = getBlockState().getValue(MashTubBlock.LIGHT);
        if (luminance != currentLuminance) {
            level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(MashTubBlock.LIGHT, luminance));
        }

        if (difference > 0) {
            setTimeProcessed(0);
            setRepeatCount(0);
        }

        if (resovoir.getContents().isEmpty() && !suppliedIngredients.isEmpty()) {
            for (ItemStack stack : suppliedIngredients.convertToItemStacks()) {
                Block.popResource(getLevel(), getBlockPos(), stack);
            }
            level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS);
            suppliedIngredients.clear();
        }
    }

    @Override
    public void clientTick(Level world) {
        super.clientTick(world);
        if (!suppliedIngredients.isEmpty() && world.getRandom().nextFloat() < 0.33F && world.getGameTime() % 3 == 0) {
            spawnBubbles(1 + (int)(suppliedIngredients.size() * 1.5), 0, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP);
        }
    }

    public Either<ItemStack, ItemInteractionResult> interactWithItem(ItemStack stack, Player player) {

        if (!currentStew.isEmpty()) {
            return Either.right(ItemInteractionResult.FAIL);
        }

        if (getLevel().isClientSide) {
            return Either.right(ItemInteractionResult.SUCCESS);
        }

        if (FluidCapacity.get(stack) > 0) {
            ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack.copy());
            Resovoir tank = getPrimaryTank();
            FluidVariant variant = t.fluids().toVariant();
            if (!t.fluids().isEmpty()) {
                if (tank.deposit(t, t.capacity()) > 0) {
                    getLevel().playSound(null, getBlockPos(), variant.getEmptySound(), SoundSource.BLOCKS, 1, 1);
                    return Either.left(t.toItemStack());
                }
            } else {
                if (tank.withdraw(t, t.capacity()) > 0) {
                    getLevel().playSound(null, getBlockPos(), variant.getFillSound(), SoundSource.BLOCKS, 1, 1);
                    return Either.left(t.toItemStack());
                }
            }

            return Either.right(ItemInteractionResult.FAIL);
        }

        if (isAcceptingIngredients() && acceptsItem(stack)) {
            suppliedIngredients.addStack(stack.consumeAndReturn(1, player));
            if (getLevel() instanceof ServerLevel sw) {
                beginStewing(sw);
            }
            markForUpdate();
            spawnBubbles(20, 0, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP);
            getLevel().playSound(null, getBlockPos(), SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1, 1);
            return Either.right(ItemInteractionResult.SUCCESS);
        }

        return Either.right(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
    }

    public boolean acceptsItem(ItemStack stack) {
        return FluidCapacity.get(stack) == 0;
    }

    public boolean isAcceptingIngredients() {
        return currentStew.isEmpty() && !getPrimaryTank().getContents().isEmpty();
    }

    public boolean isValidIngredient(ServerLevel world, ItemStack stack) {
        return acceptsItem(stack)
            && (world.getRecipeManager()
                .getAllRecipesFor(PSRecipes.MASHING_TYPE).stream()
                .filter(recipe -> recipe.value().baseFluid().test(getPrimaryTank().getContents()))
                .flatMap(recipe -> recipe.value().getIngredients().stream())
                .anyMatch(i -> i.test(stack)));
    }

    public void beginStewing(ServerLevel world) {
        if (suppliedIngredients.isEmpty() || getLevel().isClientSide()) {
            return;
        }

        currentStew = world.getRecipeManager().getRecipeFor(
                PSRecipes.MASHING_TYPE,
                new MashingRecipe.Input(getPrimaryTank().getContents(), solidContents, suppliedIngredients),
                world
        ).map(Stew::new);

        if (currentStew.isEmpty() && suppliedIngredients.countMatches(stack -> !isValidIngredient(world, stack)) >= 8) {
            suppliedIngredients.clear();
            getPrimaryTank().setContents(PSFluids.SLURRY.getDefaultStack(getPrimaryTank().getContents().amount()));
            setChanged();
        }
    }

    public Stream<MashingRecipe> getPotentialMatches() {
        var input = new MashingRecipe.Input(getPrimaryTank().getContents(), solidContents, suppliedIngredients);
        return level.getRecipeManager().getAllRecipesFor(PSRecipes.MASHING_TYPE).stream().map(RecipeHolder::value).filter(recipe -> {
            return recipe.matchesPartially(input, level);
        });
    }

    private void spawnBubbles(int count, float spread, SoundEvent sound) {
        RandomSource random = getLevel().getRandom();
        Vec3 center = ParticleHelper.apply(getBlockPos().getCenter(), x -> random.triangle(x, 0.25));

        Resovoir tank = getPrimaryTank();
        ParticleHelper.spawnParticles(getLevel(),
                new DrugDustParticleEffect(PSParticles.BUBBLE, MathUtils.unpackRgb(tank.getContents().fluid().getColor(tank.getContents())), 1F),
                () -> ParticleHelper.apply(center, x -> random.triangle(x, 0.5 + spread)).add(0, 0.5, 0),
                Suppliers.ofInstance(new Vec3(
                        random.triangle(0, 0.125),
                        random.triangle(0.1, 0.125),
                        random.triangle(0, 0.125)
                )),
                count
        );

        if (getLevel() instanceof ServerLevel) {
            getLevel().playSound(null, getBlockPos(), sound, SoundSource.BLOCKS,
                    0.5F + getLevel().getRandom().nextFloat(),
                    0.3F + getLevel().getRandom().nextFloat()
            );
        } else {
            getLevel().playLocalSound(getBlockPos(), sound, SoundSource.BLOCKS,
                    0.5F + getLevel().getRandom().nextFloat(),
                    0.3F + getLevel().getRandom().nextFloat(), true);
        }
    }

    @Deprecated
    @Override
    public List<ItemStack> getDroppedStacks(ItemStack container) {
        List<ItemStack> ingredients = new ArrayList<>(this.suppliedIngredients.convertToItemStacks());
        if (!solidContents.isEmpty()) {
            ingredients.add(solidContents);
        }
        return ingredients;
    }

    @Override
    public Either<PipeFluids, Unit> tryInsert(ServerLevel world, BlockState state, BlockPos pos, Direction direction, PipeFluids fluids) {
        if (!currentStew.isEmpty()) {
            return PipeInsertable.reject(fluids);
        }
        return super.tryInsert(world, state, pos, direction, fluids);
    }

    @Override
    public Optional<PipeFluids> tryExtract(ServerLevel world, BlockState state, BlockPos pos, Direction direction) {
        if (!currentStew.isEmpty()) {
            return Optional.empty();
        }
        return super.tryExtract(world, state, pos, direction);
    }

    @Override
    public void saveAdditional(CompoundTag compound, Provider lookup) {
        super.saveAdditional(compound, lookup);
        if (!solidContents.isEmpty()) {
            compound.put("solidContents", solidContents.saveOptional(lookup));
        }
        compound.put("suppliedIngredients", suppliedIngredients.toNbt(lookup));
        compound.put("auxiliaryFluids", auxiliaryFluids.encode());
    }

    @Override
    public void loadAdditional(CompoundTag compound, Provider lookup) {
        super.loadAdditional(compound, lookup);
        solidContents = compound.contains("solidContents", Tag.TAG_COMPOUND)
                ? ItemStack.parseOptional(lookup, compound.getCompound("solidContents"))
                : ItemStack.EMPTY;
        auxiliaryFluids = compound.contains("auxiliaryFluids", Tag.TAG_COMPOUND)
                ? ItemFluids.decode(compound.get("auxiliaryFluids"))
                : ItemFluids.EMPTY;
        suppliedIngredients.fromNbt(compound.getCompound("suppliedIngredients"), lookup);
    }

    class Stew implements NbtSerialisable {
        @Nullable
        private ResourceLocation recipe;
        private int stewTime;

        public Stew(RecipeHolder<MashingRecipe> recipe) {
            this.recipe = recipe.id();
            this.stewTime = (2 + level.getRandom().nextInt(4)) + recipe.value().stewTime();
        }

        public boolean tick() {
            if (recipe == null) {
                setChanged();
                return false;
            }

            if (level.getGameTime() % 30 == 0) {
                spawnBubbles(9, 0.5F, SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE);
                setChanged();
                if (--stewTime <= 0) {
                    if (level.getRecipeManager().byKey(recipe).map(RecipeHolder::value).orElse(null) instanceof MashingRecipe recipe) {
                        var input = new MashingRecipe.Input(getPrimaryTank().getContents(), solidContents, suppliedIngredients);
                        getPrimaryTank().setContents(recipe.result().ofAmount(getPrimaryTank().getContents().amount()));

                        recipe.getRemainingItems(input).forEach(stack -> {
                           Block.popResource(level, getBlockPos(), stack);
                        });
                    }
                    suppliedIngredients.clear();

                    return false;
                }
            }

            return true;
        }

        @Override
        public void toNbt(CompoundTag compound, Provider lookup) {
            compound.putInt("stewTime", stewTime);
            compound.putString("recipe", recipe.toString());
        }

        @Override
        public void fromNbt(CompoundTag compound, Provider lookup) {
            stewTime = compound.getInt("stewTime");
            recipe = ResourceLocation.read(compound.getString("recipe")).result().orElse(null);
        }
    }
}
