package dev.craft.ic2.machine;

import dev.craft.ic2.Ic2Machine;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Mass fabricator block; {@link #SCRAP} mirrors IC2's networked "running with scrap" state for the client sounds
 * (it does not change the model).
 */
public class MassFabricatorBlock extends MachineBlock {
    public static final BooleanProperty SCRAP = BooleanProperty.create("scrap");

    public MassFabricatorBlock(Properties properties) {
        super(properties, Ic2Machine.MASS_FABRICATOR, MassFabricatorBlockEntity::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SCRAP);
    }
}
