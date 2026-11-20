/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.mojang.datafixers.util.Either;
import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.block.BlockWithFluid;
import ivorius.psychedelicraft.block.BlockWithFluid.DirectionalFluidResovoir;
import ivorius.psychedelicraft.block.BurnerBlock;
import ivorius.psychedelicraft.block.PipeInsertable;
import ivorius.psychedelicraft.block.entity.contents.EmptyContents;
import ivorius.psychedelicraft.block.entity.contents.LargeContents;
import ivorius.psychedelicraft.block.entity.contents.SmallContents;
import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.fluid.Processable.ProcessType;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.PSComponents;
import ivorius.psychedelicraft.particle.DrugDustParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import ivorius.psychedelicraft.recipe.BunsenBurnerRecipe;
import ivorius.psychedelicraft.recipe.FluidMound;
import ivorius.psychedelicraft.recipe.ItemMound;
import ivorius.psychedelicraft.recipe.PSRecipes;
import ivorius.psychedelicraft.recipe.ReactingRecipe;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BurnerBlockEntity extends SyncedBlockEntity implements BlockWithFluid.DirectionalFluidResovoir, PipeInsertable {
    static final int[] CONTAINER_SLOT_ID = {0};

    private int temperature;
    private ItemStack container = ItemStack.EMPTY;

    private Contents contents = new EmptyContents(this);

    @Nullable
    private BunsenBurnerRecipe.Product product;

    private int processingTime;

    public BurnerBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.BUNSEN_BURNER, pos, state);
    }

    public void setContainer(ItemStack container) {
        this.container = container.copy();
        this.container.remove(PSComponents.FLUIDS);
        this.container.remove(PSComponents.FLUIDS_MIXTURE);
        this.container.remove(PSComponents.RIFT_FRACTION);
        this.processingTime = 0;
        setChanged();
    }

    public ItemStack getContainer() {
        return container;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
        setChanged();
    }

    public Contents getContents() {
        return contents;
    }

    public boolean interact(ItemStack stack, Player player, InteractionHand hand, Direction side) {

        if (hand != InteractionHand.MAIN_HAND) {
            return false;
        }

        var action = contents.interact(stack, player, hand, side);
        contents = action.getObject();
        setChanged();
        return action.getResult().consumesAction();
    }

    public void playSound(@Nullable Player player, SoundEvent sound) {
        if (level != null && worldPosition != null) {
            level.playSound(player, worldPosition.above(), sound, SoundSource.BLOCKS, 1, level.random.nextFloat() * 0.4F + 0.8F);
        }
    }

    @Override
    public void clientTick(Level world) {
        if (getTemperature() > 60 && getTotalFluidVolume() > 0) {
            BlockPos pos = getBlockPos();
            world.addParticle(new DrugDustParticleEffect(PSParticles.BUBBLE, new Vector3f(1, 1, 1), 0.6F),
                    world.getRandom().triangle(pos.getX() + 0.5, 0.2),
                    world.getRandom().triangle(pos.getY() + 1, 0.2),
                    world.getRandom().triangle(pos.getZ() + 0.5, 0.2), 0, 0, 0);
        }
    }

    @Override
    public void tick(ServerLevel world) {
        if (world.getGameTime() % 14 == 0) {
            if (getBlockState().getValue(BurnerBlock.LIT)) {
                if (temperature < (getPrimaryTank().getContents().isEmpty() ? 200 : 100)) {
                    temperature++;
                    setChanged();
                }
            } else {
                if (temperature > 0) {
                    temperature--;
                    setChanged();
                }
            }
        }

        contents.tick(world);

        if (contents instanceof CraftableContents c) {
            if (product != null && !product.fluids().isEmpty()) {
                if (world.getGameTime() % 5 == 0) {
                    world.playSound(null, getBlockPos(), PSSounds.BLOCK_BUNSEN_BURNER_WORK, SoundSource.BLOCKS, 1.25F, 0.02F);
                    world.gameEvent(null, GameEvent.BLOCK_CHANGE, worldPosition);
                    c.produceProducts(world, getBlockPos().above(), product);
                    if (c.getPrimaryTank().getContents().isEmpty()) {
                        product = null;
                    }
                    if (temperature > 90) {
                        setTemperature(temperature - 1);
                    }
                }
                return;
            }

            if (temperature <= 50 || world.getGameTime() % 5 != world.random.nextInt(3)) {
                return;
            }

            if (getTotalFluidVolume() == 0) {
                BlockPos pos = getBlockPos();
                world.playSound(null, pos, PSSounds.BLOCK_BUNSEN_BURNER_OVERHEAT, SoundSource.BLOCKS, 1.25F, 0.02F);
                world.sendParticles(ParticleTypes.SMOKE,
                        pos.getX() + world.getRandom().triangle(0.5F, 0.1F),
                        pos.getY() + 0.6F,
                        pos.getZ() + world.getRandom().triangle(0.5F, 0.1F),
                        2, 0, 0, 0, 0);

                if (temperature > 180 && world.random.nextInt(3) == 0) {
                    world.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.25F, 0.02F);
                    world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(Blocks.GLASS_PANE.defaultBlockState()));
                    world.gameEvent(null, GameEvent.BLOCK_DESTROY, pos);
                    Block.popResource(world, pos, new ItemStack(PSItems.BROKEN_GLASS, world.random.nextIntBetweenInclusive(1, 3)));
                    Vec3 center = pos.getCenter();
                    for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(center, 2, 2, 2), EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
                        entity.hurt(world.damageSources().explosion(null, null), 3);
                        entity.knockback(0.1, center.x - entity.getX(), center.z - entity.getZ());
                    }
                    clearContent();
                }
            } else {
                if (getTemperature() > 100) {
                    temperature -= 2;
                    setTemperature(temperature);
                }
                world.playSound(null, getBlockPos(), PSSounds.BLOCK_BUNSEN_BURNER_WORK, SoundSource.BLOCKS, 1.25F, 0.02F);
                world.gameEvent(null, GameEvent.BLOCK_CHANGE, worldPosition);
                product = craft(world, c);
            }
        } else {
            processingTime = 0;
            product = null;
        }
    }

    private BunsenBurnerRecipe.Product craft(ServerLevel world, CraftableContents contents) {
        var consumer = new BunsenBurnerRecipe.Product(FluidMound.of(), new ArrayList<>(), new HashSet<>());
        var input = new ReactingRecipe.Input(
                FluidMound.of(this),
                contents.getCraftingIngredients(),
                consumer
        );
        world.getRecipeManager().getRecipeFor(PSRecipes.CHEMISTRY, input, world).ifPresentOrElse(recipe -> {
            if (++processingTime >= recipe.value().stewTime()) {
                processingTime = 0;
                craftAndCollectResult(world, input, recipe);
                contents.onCraft(input);
            }
        }, () -> {
            getAuxiliaryTanks().forEach(tank -> {
                if (tank.getContents().fluid() instanceof Processable processable) {
                    ProcessType type = processable.modifyProcess(tank, ProcessType.PURIFY);
                    if (processable.getProcessingTime(tank, type) != Processable.UNCONVERTABLE) {
                        processable.process(contents, type, consumer);
                        return;
                    }
                }

                consumer.accept(tank.drain(Math.max(1, (int)tank.getAmount() / 10)));
            });
        });

        return consumer;
    }

    private void craftAndCollectResult(ServerLevel world, ReactingRecipe.Input input, RecipeHolder<BunsenBurnerRecipe> recipe) {
        ItemStack byProduct = recipe.value().assemble(input, world.registryAccess());
        if (!byProduct.isEmpty()) {
            input.consumer().accept(byProduct);
        }
        if (input.nextPhase() != null) {
            world.getRecipeManager().getRecipesFor(PSRecipes.CHEMISTRY, input.nextPhase(), world)
                .forEach(addition -> craftAndCollectResult(world, input.nextPhase(), addition));
        }
    }

    @Override
    public Resovoir getPrimaryTank() {
        return contents instanceof Processable.Context c ? c.getPrimaryTank() : Resovoir.EMPTY;
    }

    @Override
    public Resovoir getTankOnSide(Direction direction) {
        return contents instanceof Processable.Context c ? c.getTankOnSide(direction) : Resovoir.EMPTY;
    }

    @Override
    public List<Resovoir> getAuxiliaryTanks() {
        return contents instanceof Processable.Context c ? c.getAuxiliaryTanks() : List.of();
    }

    @Override
    public void clearContent() {
        if (contents instanceof CraftableContents c && getLevel() instanceof ServerLevel sw) {
            for (ItemStack stack : c.getCraftingIngredients().convertToItemStacks()) {
                Block.popResource(sw, getBlockPos(), stack);
            }
        }
        contents = new EmptyContents(this);
        container = ItemStack.EMPTY;
        processingTime = 0;
        setChanged();
    }

    @Override
    public boolean acceptsConnectionFrom(LevelAccessor world, BlockState state, BlockPos pos, BlockState neighborState, BlockPos neighborPos, Direction direction, boolean input) {
        return input && direction == Direction.UP;
    }

    @Override
    public Either<PipeFluids, Unit> tryInsert(ServerLevel world, BlockState state, BlockPos pos, Direction direction, PipeFluids fluids) {
        if (direction != Direction.DOWN) {
            return PipeInsertable.reject(fluids);
        }
        return getContents().tryInsert(world, state, pos, direction, fluids);
    }

    @Override
    public void saveAdditional(CompoundTag compound, Provider lookup) {
        super.saveAdditional(compound, lookup);
        compound.putInt("temperature", temperature);
        compound.putInt("processingTime", processingTime);
        NbtSerialisable.put(compound, "container", ItemStack.OPTIONAL_CODEC, container);
        NbtSerialisable.putNullable(compound, "product", BunsenBurnerRecipe.Product.CODEC, product);
        compound.putString("contentsType", contents.getId().toString());
        compound.put("contents", contents.toNbt(lookup));
    }

    @Override
    public void loadAdditional(CompoundTag compound, Provider lookup) {
        super.loadAdditional(compound, lookup);
        temperature = compound.getInt("temperature");
        processingTime = compound.getInt("processingTime");
        product = NbtSerialisable.get(compound, "product", BunsenBurnerRecipe.Product.CODEC).orElse(null);
        container = NbtSerialisable.get(compound, "container", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        ResourceLocation contentType = ResourceLocation.parse(compound.getString("contentsType"));
        if (contentType.equals(contents.getId())) {
            contents.fromNbt(compound.getCompound("contents"), lookup);
        } else {
            contents = Contents.TYPES
                    .getOrDefault(contentType, Contents.TYPES.get(EmptyContents.ID))
                    .create(this, compound.getCompound("contents"), lookup);
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side.getAxis() == Axis.Y ? CONTAINER_SLOT_ID : contents instanceof WorldlyContainer l ? l.getSlotsForFace(side) : new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
        if (slot == CONTAINER_SLOT_ID[0] && dir == Direction.UP) {
            return container.isEmpty() && FluidCapacity.get(stack) > 0;
        }
        return contents instanceof WorldlyContainer l && l.canPlaceItemThroughFace(slot - 1, stack, dir);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        if (slot == CONTAINER_SLOT_ID[0] && container.isEmpty()) {
            return false;
        }
        return contents instanceof WorldlyContainer l && l.canTakeItemThroughFace(slot - 1, stack, dir);
    }

    @Override
    public int getContainerSize() {
        return 1 + (contents instanceof Container i ? i.getContainerSize() : 0);
    }

    @Override
    public boolean isEmpty() {
        return container.isEmpty() && contents.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot == 0) {
            return contents.getFilled(container, true, 1);
        }
        return contents instanceof Container l ? l.getItem(slot - 1) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot == 0) {
            float drainPercentage = container.getCount() / (float)amount;
            return contents.getFilled(container.split(amount), false, drainPercentage);
        }
        return contents instanceof Container l ? l.removeItem(slot - 1, amount) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot == 0) {
            return contents.getFilled(container.copyAndClear(), false, 1);
        }
        return contents instanceof Container l ? l.removeItemNoUpdate(slot - 1) : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            container = stack.split(1);
            contents = new EmptyContents(this).getForStack(getLevel(), getBlockPos(), getBlockState(), stack);
        } else if (contents instanceof Container l) {
            l.setItem(slot - 1, stack);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Deprecated
    @Override
    public List<ItemStack> getDroppedStacks(ItemStack ignored) {
        if (contents instanceof DirectionalFluidResovoir l) {
            return l.getDroppedStacks(getContainer());
        }
        if (!getContainer().isEmpty()) {
            return List.of(removeItemNoUpdate(0));
        }
        return List.of();
    }

    public interface Contents extends NbtSerialisable, PipeInsertable {
        Map<ResourceLocation, Factory> TYPES = Util.make(new HashMap<>(), map -> {
            map.put(EmptyContents.ID, (entity, nbt, lookup) -> new EmptyContents(entity));
            map.put(SmallContents.ID, (entity, nbt, lookup) -> {
                SmallContents contents = new SmallContents(entity, 0, ItemStack.EMPTY);
                contents.fromNbt(nbt, lookup);
                return contents;
            });
            map.put(LargeContents.ID, (entity, nbt, lookup) -> {
                SmallContents contents = new LargeContents(entity, 0, ItemStack.EMPTY);
                contents.fromNbt(nbt, lookup);
                return contents;
            });
        });

        ResourceLocation getId();

        void tick(ServerLevel world);

        boolean isEmpty();

        ItemStack getFilled(ItemStack container, boolean dryRun, float drainPercentage);

        @Nullable
        InteractionResultHolder<Contents> interact(ItemStack stack, Player player, InteractionHand hand, Direction side);

        VoxelShape getOutlineShape();

        interface Factory {
            Contents create(BurnerBlockEntity entity, CompoundTag compound, Provider lookup);
        }
    }

    public interface CraftableContents extends Contents, Processable.Context {
        ItemMound getCraftingIngredients();

        void produceProducts(ServerLevel world, BlockPos pipePos, BunsenBurnerRecipe.Product product);

        void onCraft(BunsenBurnerRecipe.Input input);
    }
}
