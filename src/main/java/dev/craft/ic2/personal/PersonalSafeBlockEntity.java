package dev.craft.ic2.personal;

import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Machine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * IC2 {@code TileEntityPersonalChest}: 54 slots for the owner (and operators) only, no automation, a door that
 * opens while someone looks inside.
 */
public class PersonalSafeBlockEntity extends PersonalBlockEntity {
    public static final int SLOTS = 54;
    private static final int EVENT_USERS = 1;

    private int ticksSinceSync;
    private int numUsingPlayers;
    private float lidAngle;
    private float prevLidAngle;

    public PersonalSafeBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.PERSONAL_SAFE), pos, state, SLOTS);
    }

    /** IC2 {@code canAccess} of the safe: owner, server owner (single player) or operator. */
    public boolean canOpen(ServerPlayer player) {
        if (canAccess(player)) {
            return true;
        }
        MinecraftServer server = player.getServer();
        if (server != null && (server.isSingleplayerOwner(player.getGameProfile())
                || server.getPlayerList().isOp(player.getGameProfile()))) {
            return true;
        }
        player.sendSystemMessage(Component.translatable("message.craft.personal_safe.owned", getOwnerName()));
        return false;
    }

    @Override
    public boolean wrenchCanRemove(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || !canOpen(serverPlayer)) {
            return false;
        }
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.get(i).isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.craft.personal_safe.not_empty"));
                return false;
            }
        }
        return true;
    }

    /** No sided access in IC2 ({@code getSizeInventorySide} = 0). */
    @Nullable
    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        return null;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory, boolean open) {
        return new PersonalSafeMenu(id, inventory, this);
    }

    // --- door ---

    public void startOpen() {
        numUsingPlayers++;
        syncUsers();
    }

    public void stopOpen() {
        numUsingPlayers--;
        syncUsers();
    }

    private void syncUsers() {
        if (level != null) {
            level.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_USERS, numUsingPlayers);
        }
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == EVENT_USERS) {
            numUsingPlayers = param;
            return true;
        }
        return super.triggerEvent(id, param);
    }

    @Override
    public void serverTick() {
        // IC2 writes "% 20 * 4 == 0", i.e. every 20 ticks.
        if (++ticksSinceSync % 20 == 0) {
            syncUsers();
        }
    }

    /** IC2 {@code updateEntity}: the door swings by 0.1 per tick, with the chest sounds. */
    @Override
    public void clientTick() {
        prevLidAngle = lidAngle;
        if (numUsingPlayers > 0 && lidAngle == 0.0F) {
            playSound(true);
        }
        if (numUsingPlayers == 0 && lidAngle > 0.0F || numUsingPlayers > 0 && lidAngle < 1.0F) {
            float previous = lidAngle;
            lidAngle += numUsingPlayers > 0 ? 0.1F : -0.1F;
            if (lidAngle > 1.0F) {
                lidAngle = 1.0F;
            }
            if (lidAngle < 0.5F && previous >= 0.5F) {
                playSound(false);
            }
            if (lidAngle < 0.0F) {
                lidAngle = 0.0F;
            }
        }
    }

    private void playSound(boolean open) {
        level.playLocalSound(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F,
                level.random.nextFloat() * 0.1F + 0.9F, false);
    }

    public float getLidAngle(float partialTick) {
        return prevLidAngle + (lidAngle - prevLidAngle) * partialTick;
    }
}
