package dev.craft.core.multiblock;

import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Condition for one cell of a {@link MultiblockPattern}. */
@FunctionalInterface
public interface BlockMatcher {
    BlockMatcher ANY = (level, pos, state) -> true;
    BlockMatcher AIR = (level, pos, state) -> state.isAir();

    boolean matches(BlockGetter level, BlockPos pos, BlockState state);

    static BlockMatcher blocks(Block... blocks) {
        Set<Block> set = Set.of(blocks);
        return (level, pos, state) -> set.contains(state.getBlock());
    }

    static BlockMatcher tag(TagKey<Block> tag) {
        return (level, pos, state) -> state.is(tag);
    }

    static BlockMatcher state(Predicate<BlockState> predicate) {
        return (level, pos, state) -> predicate.test(state);
    }

    default BlockMatcher or(BlockMatcher other) {
        return (level, pos, state) -> matches(level, pos, state) || other.matches(level, pos, state);
    }

    default BlockMatcher and(BlockMatcher other) {
        return (level, pos, state) -> matches(level, pos, state) && other.matches(level, pos, state);
    }
}
