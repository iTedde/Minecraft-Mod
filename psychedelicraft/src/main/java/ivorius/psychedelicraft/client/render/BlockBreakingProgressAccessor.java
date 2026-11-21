package ivorius.psychedelicraft.client.render;

import java.util.SortedSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

public interface BlockBreakingProgressAccessor {
    Long2ObjectMap<SortedSet<BlockDestructionProgress>> getBlockBreakingProgressions();

    static int getStage(BlockPos pos) {
        if (Minecraft.getInstance().levelRenderer == null) {
            return -1;
        }
        SortedSet<BlockDestructionProgress> info = ((BlockBreakingProgressAccessor)Minecraft.getInstance().levelRenderer).getBlockBreakingProgressions().get(pos.asLong());
        return info == null || info.isEmpty() ? 0 : info.last().getProgress();
    }
}
