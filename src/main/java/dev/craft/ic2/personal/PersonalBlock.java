package dev.craft.ic2.personal;

import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.machine.MachineBlock;
import dev.craft.ic2.machine.MachineBlockEntity;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * IC2 {@code BlockPersonal}: unbreakable (only the owner's wrench removes it), drops itself. The safe refuses
 * strangers; the O-Mats open the owner's or the customer's GUI.
 */
public class PersonalBlock extends MachineBlock {
    public PersonalBlock(Properties properties, Ic2Machine machine,
                         BiFunction<BlockPos, BlockState, ? extends MachineBlockEntity> factory) {
        super(properties, machine, factory);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof PersonalBlockEntity be) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        boolean open = be.canAccess(player);
        if (be instanceof PersonalSafeBlockEntity safe && !safe.canOpen(serverPlayer)) {
            return InteractionResult.PASS;
        }
        serverPlayer.openMenu(be, buf -> {
            buf.writeBlockPos(pos);
            buf.writeBoolean(open);
        });
        return InteractionResult.CONSUME;
    }

    /** The safe is drawn by its block entity renderer. */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return getMachine() == Ic2Machine.PERSONAL_SAFE ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.MODEL;
    }
}
