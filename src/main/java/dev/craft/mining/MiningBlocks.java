package dev.craft.mining;

import com.google.common.collect.ImmutableSet;
import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MiningBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Craft.MODID);

    public static final DeferredBlock<MiningPortalBlock> MINING_PORTAL = BLOCKS.register("mining_portal",
            () -> new MiningPortalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL).noLootTable()));
    /** Portal lookup at the exit, like {@code PoiTypes.NETHER_PORTAL}. */
    public static final DeferredHolder<PoiType, PoiType> MINING_PORTAL_POI = POI_TYPES.register("mining_portal",
            () -> new PoiType(ImmutableSet.copyOf(MINING_PORTAL.get().getStateDefinition().getPossibleStates()), 0, 1));

    private MiningBlocks() {
    }
}
