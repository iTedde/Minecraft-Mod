package dev.craft.obsidiplates;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;

/** ObsidiPlates {@code BlockPressurePlate} with {@code EnumMobType.players} (spec 50 §2): only players press it. */
public class ObsidianPressurePlateBlock extends PressurePlateBlock {
    public ObsidianPressurePlateBlock(Properties properties) {
        super(BlockSetType.STONE, properties);
    }

    @Override
    protected int getSignalStrength(Level level, BlockPos pos) {
        return getEntityCount(level, TOUCH_AABB.move(pos), Player.class) > 0 ? 15 : 0;
    }
}
