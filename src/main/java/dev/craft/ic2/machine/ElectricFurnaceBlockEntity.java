package dev.craft.ic2.machine;

import java.util.Optional;

import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.menu.ElectricMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Electric furnace (IC2 {@code TileEntityElecFurnace}): furnace recipes, 3 EU/t, 130 ticks. */
public class ElectricFurnaceBlockEntity extends ElectricMachineBlockEntity {
    public static final int ENERGY_PER_TICK = 3;
    public static final int OPERATION_LENGTH = 130;
    public static final int MAX_INPUT = 32;

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.ELECTRIC_FURNACE), pos, state, ENERGY_PER_TICK, OPERATION_LENGTH,
                MAX_INPUT);
    }

    @Nullable
    @Override
    public ItemStack getResultFor(ItemStack input, boolean adjustInput) {
        ItemStack result = getSmeltingResult(level, input);
        if (result.isEmpty()) {
            return null;
        }
        if (adjustInput) {
            input.shrink(1);
        }
        return result;
    }

    /** Vanilla furnace result for {@code input}, or empty (IC2 {@code FurnaceRecipes.getSmeltingResult}). */
    public static ItemStack getSmeltingResult(Level level, ItemStack input) {
        Optional<RecipeHolder<SmeltingRecipe>> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level);
        return recipe.map(holder -> holder.value().getResultItem(level.registryAccess())).orElse(ItemStack.EMPTY);
    }

    @Override
    public SoundEvent getStartSound() {
        return Ic2Sounds.ELECTRO_FURNACE_LOOP.get();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new ElectricMachineMenu(id, playerInventory, this);
    }
}
