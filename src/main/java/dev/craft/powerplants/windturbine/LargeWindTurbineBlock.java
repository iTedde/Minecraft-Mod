package dev.craft.powerplants.windturbine;

import dev.craft.powerplants.block.PlantControllerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Nacelle of the large wind turbine (spec 23 §4): right-clicking with a rotor puts it in (swapping an existing one),
 * sneaking with an empty hand takes it out, otherwise the state is reported in chat.
 */
public class LargeWindTurbineBlock extends PlantControllerBlock {
    public LargeWindTurbineBlock(Properties properties) {
        super(properties, LargeWindTurbineBlockEntity::new);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(stack.getItem() instanceof WindRotorItem)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof LargeWindTurbineBlockEntity turbine) {
            player.setItemInHand(hand, turbine.swapRotor(stack));
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!player.isShiftKeyDown()) {
            return super.useWithoutItem(state, level, pos, player, hit);
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof LargeWindTurbineBlockEntity turbine) {
            ItemStack rotor = turbine.swapRotor(ItemStack.EMPTY);
            if (rotor.isEmpty()) {
                return InteractionResult.PASS;
            }
            player.getInventory().placeItemBackInInventory(rotor);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
