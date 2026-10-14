package dev.craft.ic2.item;

import dev.craft.ic2.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Empty cell (IC2 {@code ItemCell}): right click on a water or lava source within 5 blocks fills a cell. */
public class EmptyCellItem extends Item {
    public static final double REACH = 5.0;

    public EmptyCellItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(REACH));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE,
                ClipContext.Fluid.SOURCE_ONLY, player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }
        BlockPos pos = hit.getBlockPos();
        if (!level.mayInteract(player, pos)) {
            return InteractionResultHolder.pass(stack);
        }
        FluidState fluid = level.getFluidState(pos);
        boolean sourceBlock = fluid.isSource() && level.getBlockState(pos).getBlock() instanceof LiquidBlock;
        if (!sourceBlock) {
            return InteractionResultHolder.pass(stack);
        }
        ItemStack cell;
        if (fluid.is(Fluids.WATER)) {
            cell = new ItemStack(Ic2Items.WATER_CELL.get());
        } else if (fluid.is(Fluids.LAVA)) {
            cell = new ItemStack(Ic2Items.LAVA_CELL.get());
        } else {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        if (player.getInventory().add(cell)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            stack.shrink(1);
            player.containerMenu.broadcastChanges();
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }
}
