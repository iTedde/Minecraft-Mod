package dev.craft.gravisuite.item;

import dev.craft.gravisuite.GsDataComponents;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.tool.DrillItem;
import dev.craft.ic2.item.tool.ElectricToolItem;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * GraviSuite {@code ItemAdvDDrill} (spec 28 §9.1): the diamond drill's block list, transfer 500, tier 2, efficiency 35
 * and 160 EU per block; hits are free, no sounds. Phase 41 (user, 2026-11-02): 1 000 000 EU instead of 15 000 and a
 * switchable 3×3×1 mode (mode key + right click): breaking a block also breaks the 8 blocks around it in the plane
 * across the hit face, each like a normal break by the player (drops, enchantments, protection) for 160 EU.
 */
public class AdvancedDrillItem extends ElectricToolItem {
    public static final int OPERATION_COST = 160;
    /** Phase 41: capacity (pack: 15 000 EU). */
    public static final int MAX_CHARGE = 1_000_000;
    /** Phase 41: area blocks may be as hard as the centre block, but at least this hard (ores). */
    public static final float MIN_AREA_HARDNESS = 3.0F;

    /** Set while the area blocks are broken, so their own break events do not start another area. */
    private static boolean breakingArea;

    public AdvancedDrillItem(Properties properties) {
        super(properties, MAX_CHARGE, 500, 2, OPERATION_COST, 35.0F, DrillItem.MINEABLE);
    }

    public static boolean isAreaMode(ItemStack stack) {
        return stack.getOrDefault(GsDataComponents.DRILL_AREA.get(), false);
    }

    public static void setAreaMode(ItemStack stack, boolean on) {
        stack.set(GsDataComponents.DRILL_AREA.get(), on);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && Ic2Keyboard.isModeSwitchKeyDown(player)) {
            boolean on = !isAreaMode(stack);
            setAreaMode(stack, on);
            player.sendSystemMessage(Component.translatable(on ? "message.craft.gravisuite.drill_area_on"
                    : "message.craft.gravisuite.drill_area_off"));
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    protected void onBlockStartBreak(ItemStack stack, Level level, BlockPos pos, BlockState state, Player player) {
        if (breakingArea || !isAreaMode(stack) || !(player instanceof ServerPlayer serverPlayer)
                || !isMineable(state)) {
            return;
        }
        float maxHardness = Math.max(state.getDestroySpeed(level, pos), MIN_AREA_HARDNESS);
        breakingArea = true;
        try {
            for (BlockPos target : areaAround(pos, hitFace(player, pos))) {
                if (!ElectricItems.canUse(stack, OPERATION_COST) || player.getMainHandItem() != stack) {
                    break;
                }
                BlockState other = level.getBlockState(target);
                float hardness = other.getDestroySpeed(level, target);
                if (!other.isAir() && isMineable(other) && hardness >= 0.0F && hardness <= maxHardness) {
                    serverPlayer.gameMode.destroyBlock(target);
                }
            }
        } finally {
            breakingArea = false;
        }
    }

    /** The face of {@code pos} the player is looking at; their facing if the ray misses the block. */
    static Direction hitFace(Player player, BlockPos pos) {
        HitResult hit = player.pick(player.blockInteractionRange(), 1.0F, false);
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK
                && blockHit.getBlockPos().equals(pos)) {
            return blockHit.getDirection();
        }
        return Direction.orderedByNearest(player)[0].getOpposite();
    }

    /** The 8 positions around {@code centre} in the plane across {@code face} (3×3×1 without the centre). */
    public static List<BlockPos> areaAround(BlockPos centre, Direction face) {
        List<BlockPos> list = new ArrayList<>(8);
        Direction.Axis axis = face.getAxis();
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0) {
                    continue;
                }
                list.add(switch (axis) {
                    case X -> centre.offset(0, a, b);
                    case Y -> centre.offset(a, 0, b);
                    case Z -> centre.offset(a, b, 0);
                });
            }
        }
        return list;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable(isAreaMode(stack) ? "tooltip.craft.gravisuite.drill_area_on"
                : "tooltip.craft.gravisuite.drill_area_off").withStyle(ChatFormatting.GRAY));
    }
}
