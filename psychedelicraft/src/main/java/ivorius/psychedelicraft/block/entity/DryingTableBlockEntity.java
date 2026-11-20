/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import java.util.Optional;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.recipe.DryingRecipe;
import ivorius.psychedelicraft.recipe.PSRecipes;
import ivorius.psychedelicraft.screen.DryingTableScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DryingTableBlockEntity extends BlockEntityWithInventory {
    public static final int OUTPUT_SLOT_INDEX = 0;
    private static final int[] INPUT_SLOTS = new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    private static final int[] OUTPUT_SLOTS = new int[]{ OUTPUT_SLOT_INDEX };

    public static long getCookingTime(float recipeDifficulty, boolean ironTable) {
        return (long)(recipeDifficulty * (ironTable
                ? Psychedelicraft.getConfig().ironDryingTableTickDuration.get()
                : Psychedelicraft.getConfig().dryingTableTickDuration.get()));
    }

    private float heat;
    private float dryingProgress;

    private long cookingTime;
    private Optional<ResourceLocation> currentRecipe = Optional.empty();

    public final ContainerData propertyDelegate = new ContainerData(){
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 ->  (int)(heat * 1000);
                case 1 -> (int)dryingProgress;
                case 2 -> (int)cookingTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0: {
                    heat = value / 1000F;
                    break;
                }
                case 1: {
                    dryingProgress = value;
                    break;
                }
                case 2: {
                    cookingTime = value;
                    break;
                }
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public DryingTableBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.DRYING_TABLE, pos, state, 10);
    }

    public static void serverTick(ServerLevel world, BlockPos pos, BlockState state, DryingTableBlockEntity entity) {
        entity.tick(world);
    }

    public float getHeatRatio() {
        return heat;
    }

    public float getDryingProgress() {
        return cookingTime == 0 ? 0 : dryingProgress / cookingTime;
    }

    private float calculateSunStrength() {
        float l = level.getMaxLocalRawBrightness(worldPosition) / 15F;
        float h = !level.isEmptyBlock(worldPosition) ? level.getBiome(worldPosition).value().getBaseTemperature() * 0.75F + 0.25F : 0;
        float sunStrength = Mth.clamp((l * l * h) * (l * l * h), 0, 1);

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (level.isEmptyBlock(worldPosition.relative(dir))) {
                BlockState neighbor = level.getBlockState(worldPosition.relative(dir, 2));
                if (neighbor.getBlock() instanceof AbstractFurnaceBlock
                        && neighbor.getValue(AbstractFurnaceBlock.LIT)
                        && neighbor.getValue(AbstractFurnaceBlock.FACING) == dir.getOpposite()) {
                    sunStrength += neighbor.is(Blocks.BLAST_FURNACE) ? 0.15F : neighbor.is(Blocks.SMOKER) ? 0.125F : 0.1F;
                    if (level instanceof ServerLevel sw && level.getRandom().nextInt(10) == 0) {
                        sw.sendParticles(ParticleTypes.FLAME,
                                worldPosition.getX() + dir.getStepX() + level.getRandom().triangle(0.5F, 0.25F),
                                worldPosition.getY() + level.getRandom().triangle(0.5F, 0.5F),
                                worldPosition.getZ() + dir.getStepZ() + level.getRandom().triangle(0.5F, 0.25F),
                                1, 0, 0, 0, 0.01F);
                    }
                }
            }
        }

        return sunStrength;
    }

    public void tick(ServerLevel world) {
        float oldProgress = dryingProgress;
        float oldHeat = heat;

        heat = calculateSunStrength();

        if (!(world.getRainLevel(1) > 0 && world.canSeeSky(worldPosition))) {
            if (currentRecipe.isPresent() && cookingTime > 0) {
                dryingProgress += heat;

                int delta = (int)((1 - Mth.clamp(dryingProgress / cookingTime, 0, 1)) * 100);

                if (delta == 0 || world.getGameTime() % 30 == 0) {
                    for (int i = 0; i < 5; i++) {
                        world.sendParticles(ParticleTypes.SMOKE,
                            worldPosition.getX() + world.getRandom().triangle(0.5F, 0.5F),
                            worldPosition.getY() + 0.6F,
                            worldPosition.getZ() + world.getRandom().triangle(0.5F, 0.5F),
                            2, 0, 0, 0, 0);
                    }
                }

                if (dryingProgress >= cookingTime) {
                    DryingRecipe.Input input = new DryingRecipe.Input(getItem(OUTPUT_SLOT_INDEX), getStacks().skip(1).toList());
                    world.getRecipeManager()
                        .getRecipeFor(PSRecipes.DRYING_TYPE, input, world, currentRecipe.get())
                        .ifPresent(recipe -> craft(world, recipe.value(), input));
                    currentRecipe = Optional.empty();
                    dryingProgress = 0;
                    cookingTime = 0;

                    world.getChunkSource().blockChanged(worldPosition);
                }
            } else {
                dryingProgress = 0;
            }
        }

        if (!Mth.equal(oldProgress, dryingProgress) || !Mth.equal(oldHeat, heat)) {
            setChanged();
        }
    }

    @Override
    public void setChanged() {
        if (level instanceof ServerLevel sw) {
            sw
                    .getRecipeManager()
                    .getRecipeFor(PSRecipes.DRYING_TYPE, new DryingRecipe.Input(getItem(OUTPUT_SLOT_INDEX), getStacks().skip(1).toList()), sw)
                    .ifPresentOrElse(recipe -> {
                        var recipeId = Optional.of(recipe.id());
                        if (!recipeId.equals(currentRecipe)) {
                            dryingProgress = 0;
                        }
                        currentRecipe = recipeId;
                        cookingTime = getCookingTime(recipe.value().cookTime(), getBlockState().is(PSBlocks.IRON_DRYING_TABLE));
                    }, () -> {
                        currentRecipe = Optional.empty();
                        cookingTime = 0;
                        dryingProgress = 0;
                    });
            heat = calculateSunStrength();
        }
        super.setChanged();
    }

    private void craft(ServerLevel world, DryingRecipe recipe, DryingRecipe.Input input) {
        ItemStack result = recipe.assemble(input, world.registryAccess());
        clearContent();
        NonNullList<ItemStack> remainder = recipe.getRemainingItems(input);
        for (int i = 0; i < remainder.size(); i++) {
            setItem(i + 1, remainder.get(i));
        }

        if (!input.result().isEmpty()) {
            result.grow(input.result().getCount());
        }

        setItem(OUTPUT_SLOT_INDEX, result);
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return super.canPlaceItemThroughFace(slot, stack, direction)
                && slot != OUTPUT_SLOT_INDEX
                && getItem(slot).isEmpty();
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == OUTPUT_SLOT_INDEX;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return direction == Direction.DOWN ? OUTPUT_SLOTS : INPUT_SLOTS;
    }

    @Override
    protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
        return new DryingTableScreenHandler(syncId, playerInventory, propertyDelegate, this);
    }

    @Override
    public void saveAdditional(CompoundTag compound, Provider lookup) {
        super.saveAdditional(compound, lookup);
        currentRecipe.ifPresent(r -> {
            compound.putString("currentRecipe", r.toString());
        });
        compound.putFloat("heatRatio", heat);
        compound.putLong("cookingTime", cookingTime);
        compound.putFloat("dryingProgress", dryingProgress);
    }

    @Override
    public void loadAdditional(CompoundTag compound, Provider lookup) {
        super.loadAdditional(compound, lookup);
        currentRecipe = ResourceLocation.read(compound.getString("currentRecipe")).result();
        heat = compound.getFloat("heatRatio");
        cookingTime = compound.getLong("cookingTime");
        dryingProgress = compound.getFloat("dryingProgress");
    }
}
