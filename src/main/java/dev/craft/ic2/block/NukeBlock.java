package dev.craft.ic2.block;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

/**
 * IC2 {@code BlockITNT} as nuke ({@code isITNT = false}, spec 05 §7): cannot be set off by hand (breaking drops it),
 * flint and steel, redstone and explosions prime it; placing and priming are logged.
 */
public class NukeBlock extends IndustrialTntBlock {
    private static final Logger LOGGER = LogUtils.getLogger();

    public NukeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean canExplodeByHand() {
        return false;
    }

    @Override
    protected Ic2ExplosiveEntity createEntity(Level level, BlockPos pos) {
        return new NukeEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    @Override
    protected void onIgnite(Level level, BlockPos pos, @Nullable Player player) {
        LOGGER.info("Nuke at {}:({},{},{}) was ignited {}", level.dimension().location(), pos.getX(), pos.getY(),
                pos.getZ(), player == null ? "indirectly" : "by " + player.getGameProfile().getName());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && placer instanceof Player player) {
            LOGGER.info("Player {} placed a nuke at {}:({},{},{})", player.getGameProfile().getName(),
                    level.dimension().location(), pos.getX(), pos.getY(), pos.getZ());
        }
    }
}
