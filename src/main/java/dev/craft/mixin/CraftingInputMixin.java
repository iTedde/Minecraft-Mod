package dev.craft.mixin;

import java.util.List;

import dev.craft.core.crafting.GridPosition;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps the grid position of trimmed crafting inputs, see {@link GridPosition}. */
@Mixin(CraftingInput.class)
public abstract class CraftingInputMixin implements GridPosition {
    @Unique
    @Nullable
    private GridPosition.Position craft$gridPosition;

    @Override
    @Nullable
    public GridPosition.Position craft$getGridPosition() {
        return craft$gridPosition;
    }

    @Override
    public void craft$setGridPosition(GridPosition.Position position) {
        craft$gridPosition = position;
    }

    @Inject(method = "ofPositioned", at = @At("RETURN"))
    private static void craft$rememberGridPosition(int width, int height, List<ItemStack> items,
                                                   CallbackInfoReturnable<CraftingInput.Positioned> cir) {
        CraftingInput.Positioned positioned = cir.getReturnValue();
        if (positioned.input() != CraftingInput.EMPTY) {
            ((GridPosition) (Object) positioned.input()).craft$setGridPosition(
                    new GridPosition.Position(width, height, positioned.left(), positioned.top()));
        }
    }
}
