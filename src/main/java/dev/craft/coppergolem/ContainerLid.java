package dev.craft.coppergolem;

import dev.craft.ironchest.IronChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Opens and closes a container while a copper golem looks into it (spec 35 §2.3), without touching the players'
 * opener counts: Iron Chests count golems separately, vanilla chests get the lid event with one more opener, barrels
 * their {@code open} state.
 */
final class ContainerLid {
    private ContainerLid() {
    }

    static void set(ServerLevel level, BlockPos pos, boolean open) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        BlockState state = level.getBlockState(pos);
        if (blockEntity instanceof IronChestBlockEntity chest) {
            chest.setGolemOpen(open);
        } else if (blockEntity instanceof ChestBlockEntity) {
            int players = ChestBlockEntity.getOpenCount(level, pos);
            level.blockEvent(pos, state.getBlock(), 1, players + (open ? 1 : 0));
            if (players == 0) {
                level.playSound(null, pos, open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE,
                        SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
            }
        } else if (blockEntity instanceof BarrelBlockEntity barrel && state.hasProperty(BarrelBlock.OPEN)) {
            if (open || !watchedByPlayer(level, pos, barrel)) {
                if (state.getValue(BarrelBlock.OPEN) != open) {
                    level.playSound(null, pos, open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE,
                            SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
                }
                level.setBlock(pos, state.setValue(BarrelBlock.OPEN, open), 3);
            }
        }
    }

    private static boolean watchedByPlayer(ServerLevel level, BlockPos pos, BarrelBlockEntity barrel) {
        for (Player player : level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(5.0))) {
            if (player.containerMenu instanceof ChestMenu menu && menu.getContainer() == barrel) {
                return true;
            }
        }
        return false;
    }
}
