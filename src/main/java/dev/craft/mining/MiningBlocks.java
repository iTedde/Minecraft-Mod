package dev.craft.mining;

import com.google.common.collect.ImmutableSet;
import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MiningBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Craft.MODID);
    /** Garden grid of the farming dimension (spec 53 §3.3). */
    public static final DeferredHolder<Feature<?>, FarmingGardens> FARMING_GARDENS =
            FEATURES.register("farming_gardens", FarmingGardens::new);
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Craft.MODID);

    public static final DeferredBlock<CraftPortalBlock> MINING_PORTAL = BLOCKS.register("mining_portal",
            () -> new CraftPortalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL).noLootTable(),
                    MiningBlocks.MINING_LINK));
    /** Portal lookup at the exit, like {@code PoiTypes.NETHER_PORTAL}. */
    public static final DeferredHolder<PoiType, PoiType> MINING_PORTAL_POI = POI_TYPES.register("mining_portal",
            () -> new PoiType(ImmutableSet.copyOf(MINING_PORTAL.get().getStateDefinition().getPossibleStates()), 0, 1));

    /** Farming portal (spec 53 §4): green nether portal look, light 11 like the nether portal. */
    public static final DeferredBlock<CraftPortalBlock> FARMING_PORTAL = BLOCKS.register("farming_portal",
            () -> new CraftPortalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL).noLootTable(),
                    MiningBlocks.FARMING_LINK));
    public static final DeferredHolder<PoiType, PoiType> FARMING_PORTAL_POI = POI_TYPES.register("farming_portal",
            () -> new PoiType(ImmutableSet.copyOf(FARMING_PORTAL.get().getStateDefinition().getPossibleStates()), 0, 1));

    /** Smooth stone frame, overworld ↔ mining dimension (spec 18 §4). */
    public static final PortalLink MINING_LINK = new PortalLink(Blocks.SMOOTH_STONE, MINING_PORTAL,
            MINING_PORTAL_POI.getKey(), MiningDimension.LEVEL,
            PortalLink.ExitPlacement.FREE_SPOT);
    /** Mossy cobblestone frame, overworld ↔ farming dimension (spec 53 §4). */
    public static final PortalLink FARMING_LINK = new PortalLink(Blocks.MOSSY_COBBLESTONE, FARMING_PORTAL,
            FARMING_PORTAL_POI.getKey(), FarmingDimension.LEVEL,
            (level, pos, axis) -> level.dimension() == FarmingDimension.LEVEL
                    ? FarmingGardens.exitCorner(pos, axis) : null);

    private MiningBlocks() {
    }
}
