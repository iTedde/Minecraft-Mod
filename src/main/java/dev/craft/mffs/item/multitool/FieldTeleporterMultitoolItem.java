package dev.craft.mffs.item.multitool;

import dev.craft.mffs.MffsDevices;
import dev.craft.mffs.block.CapacitorBlockEntity;
import dev.craft.mffs.block.ProjectorBlockEntity;
import dev.craft.mffs.field.ForceFieldMap;
import dev.craft.mffs.item.PowerLinkCardItem;
import dev.craft.mffs.security.MffsSecurity;
import dev.craft.mffs.security.SecurityRight;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@code ItemFieldtransporter} (spec 48 §8): right click on a force field of an active projector powered by a
 * capacitor. Access by access type (0 admin/debugger, 1 everyone, 2/3 right FFB at the capacitor's / projector's
 * station). The look direction (down, up or one of four horizontal ones) picks the crossing; the field may be at most
 * 5 blocks thick and its last block must belong to the same capacitor; target and the block next to it must be air
 * or liquid, not into the void; 10 000 FE.
 */
public class FieldTeleporterMultitoolItem extends MultitoolItem {
    public static final int COST = 10000;
    private static final int MAX_THICKNESS = 5;

    public FieldTeleporterMultitoolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level.isClientSide() || player == null) {
            return InteractionResult.PASS;
        }
        BlockPos clicked = context.getClickedPos();
        ForceFieldMap.Stack first = ForceFieldMap.get(level, clicked);
        if (first == null) {
            return InteractionResult.PASS;
        }
        int capacitorId = first.getCapacitorId();
        CapacitorBlockEntity capacitor = MffsDevices.get(level, MffsDevices.Kind.CAPACITOR, capacitorId)
                instanceof CapacitorBlockEntity found ? found : null;
        ProjectorBlockEntity projector = MffsDevices.get(level, MffsDevices.Kind.PROJECTOR, first.getProjectorId())
                instanceof ProjectorBlockEntity found ? found : null;
        if (projector == null || capacitor == null) {
            if (projector != null && !projector.getStack(projector.getPowerLinkSlot()).isEmpty()
                    && !(projector.getStack(projector.getPowerLinkSlot()).getItem() instanceof PowerLinkCardItem)) {
                message(player, "unsupported_source");
            }
            return InteractionResult.SUCCESS;
        }
        if (!projector.isActive()) {
            return InteractionResult.SUCCESS;
        }
        boolean pass = switch (projector.getAccessType()) {
            case 0 -> MffsSecurity.isAdmin(player);
            case 1 -> true;
            case 2 -> MffsSecurity.isAccessGranted(capacitor, player, SecurityRight.FFB);
            case 3 -> MffsSecurity.isAccessGranted(projector, player, SecurityRight.FFB);
            default -> false;
        };
        if (!pass) {
            player.sendSystemMessage(Component.translatable("message.craft.mffs.access_denied"));
            return InteractionResult.SUCCESS;
        }
        int direction = direction(player);
        int counter = 0;
        while (capacitorAt(level, clicked, counter, direction) != 0) {
            counter++;
        }
        if (capacitorId != capacitorAt(level, clicked, counter - 1, direction)) {
            player.sendSystemMessage(Component.translatable("message.craft.mffs.access_denied"));
            return InteractionResult.SUCCESS;
        }
        BlockPos target = offset(clicked, counter, direction);
        // Looking down the player lands one block lower, looking up one block higher (original ymodi).
        int yOffset = direction == 0 ? -1 : direction == 1 ? 1 : 0;
        message(player, "granted");
        if (counter > MAX_THICKNESS) {
            message(player, "too_strong");
            return InteractionResult.SUCCESS;
        }
        BlockPos feet = target.below(yOffset);
        if (!passable(level, target) || !passable(level, feet)) {
            message(player, "obstacle");
            return InteractionResult.SUCCESS;
        }
        if (feet.getY() <= level.getMinBuildHeight()) {
            message(player, "void");
        } else if (consumePower(stack, COST, true)) {
            consumePower(stack, COST, false);
            player.teleportTo(target.getX() + 0.5, feet.getY(), target.getZ() + 0.5);
            message(player, "complete");
        } else {
            message(player, "not_enough");
        }
        return InteractionResult.SUCCESS;
    }

    /** Original {@code typ}: 1 looking down (pitch ≥ 65), 0 up (≤ −65), else 2/5/3/4 for south/west/north/east. */
    static int direction(Player player) {
        int pitch = Math.round(player.getXRot());
        if (pitch >= 65) {
            return 1;
        }
        if (pitch <= -65) {
            return 0;
        }
        return switch (Mth.floor(player.getYRot() * 4.0F / 360.0F + 0.5) & 3) {
            case 0 -> 2;
            case 1 -> 5;
            case 2 -> 3;
            default -> 4;
        };
    }

    /** {@code isExistForceFieldStackMap}: the owning capacitor ID {@code counter} steps along the crossing, 0 if none. */
    private static int capacitorAt(Level level, BlockPos clicked, int counter, int direction) {
        ForceFieldMap.Stack stack = ForceFieldMap.get(level, offset(clicked, counter, direction));
        return stack == null || stack.isEmpty() ? 0 : stack.getCapacitorId();
    }

    private static BlockPos offset(BlockPos pos, int counter, int direction) {
        return switch (direction) {
            case 0 -> pos.above(counter);
            case 1 -> pos.below(counter);
            case 2 -> pos.south(counter);
            case 3 -> pos.north(counter);
            case 4 -> pos.east(counter);
            default -> pos.west(counter);
        };
    }

    private static boolean passable(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.getBlock() instanceof LiquidBlock;
    }

    private static void message(Player player, String key) {
        player.sendSystemMessage(Component.translatable("message.craft.mffs.teleporter." + key));
    }
}
