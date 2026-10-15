package dev.craft.gregtech.block;

import dev.craft.gregtech.machine.MachineBlockUpdates;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * GT machine casing ({@code GT_BlockMetaID_Block} 13–15): building block of the multiblock machines. Placing or
 * removing one notifies the machines around it ({@code causeMachineUpdate}, spec 13 §5.1).
 */
public class GtCasingBlock extends Block {
    private final Type type;

    public GtCasingBlock(Properties properties, Type type) {
        super(properties);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!oldState.is(state.getBlock())) {
            MachineBlockUpdates.cause(level, pos);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock())) {
            MachineBlockUpdates.cause(level, pos);
        }
    }

    /** Casing kinds with their blast furnace heat and effective explosion resistance. */
    public enum Type {
        STANDARD("standard_machine_casing", 13, 30, 30.0F),
        REINFORCED("reinforced_machine_casing", 14, 50, 60.0F),
        ADVANCED("gt_advanced_machine_casing", 15, 70, 30.0F);

        private final String name;
        private final int texture;
        private final int heat;
        private final float resistance;

        Type(String name, int texture, int heat, float resistance) {
            this.name = name;
            this.texture = texture;
            this.heat = heat;
            this.resistance = resistance;
        }

        public String getName() {
            return name;
        }

        /** Index on GregTech's {@code blocks.png} (= metadata). */
        public int getTexture() {
            return texture;
        }

        /** Heat each casing adds to the industrial blast furnace (K). */
        public int getHeat() {
            return heat;
        }

        public float getResistance() {
            return resistance;
        }
    }
}
