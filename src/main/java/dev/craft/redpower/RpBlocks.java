package dev.craft.redpower;

import dev.craft.Craft;
import dev.craft.redpower.table.LargeProjectTableBlock;
import dev.craft.redpower.table.ProjectTableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** RedPower blocks (spec 20). */
public final class RpBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    /** {@code blockAppliance} meta 3: material rock, hardness 2.0 (resistance 10 / 5 = 2.0). */
    public static final DeferredBlock<ProjectTableBlock> PROJECT_TABLE = BLOCKS.register("project_table",
            () -> new ProjectTableBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F).requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));
    /** Large project table (spec 34): same block properties. */
    public static final DeferredBlock<LargeProjectTableBlock> LARGE_PROJECT_TABLE = BLOCKS.register(
            "large_project_table", () -> new LargeProjectTableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));

    private RpBlocks() {
    }
}
