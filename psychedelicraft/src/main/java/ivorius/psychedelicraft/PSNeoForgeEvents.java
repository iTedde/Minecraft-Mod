package ivorius.psychedelicraft;

import java.util.ArrayList;
import java.util.List;

import ivorius.psychedelicraft.util.PSHooks;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

/**
 * Wires the registrations collected in {@link PSHooks} (upstream: Fabric API helper registries) to NeoForge events.
 */
public final class PSNeoForgeEvents {
    private PSNeoForgeEvents() {
    }

    public static void register(IEventBus modBus, IEventBus gameBus) {
        modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(PSHooks::applyFlammability));

        gameBus.addListener(BlockEvent.BlockToolModificationEvent.class, event -> {
            if (event.getItemAbility() != ItemAbilities.AXE_STRIP) {
                return;
            }
            BlockState state = event.getState();
            Block stripped = PSHooks.STRIPPABLES.get(state.getBlock());
            if (stripped != null) {
                BlockState result = stripped.defaultBlockState();
                if (state.hasProperty(RotatedPillarBlock.AXIS) && result.hasProperty(RotatedPillarBlock.AXIS)) {
                    result = result.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
                }
                event.setFinalState(result);
            }
        });

        gameBus.addListener(VillagerTradesEvent.class, event -> {
            var levels = PSHooks.VILLAGER_TRADES.get(event.getType());
            if (levels == null) {
                return;
            }
            levels.forEach((level, consumers) -> {
                List<VillagerTrades.ItemListing> listings = new ArrayList<>();
                consumers.forEach(c -> c.accept(listings));
                event.getTrades().computeIfAbsent(level, l -> new ArrayList<>()).addAll(listings);
            });
        });

        gameBus.addListener(WandererTradesEvent.class, event -> {
            PSHooks.WANDERER_TRADES.forEach((level, consumers) -> {
                List<VillagerTrades.ItemListing> listings = new ArrayList<>();
                consumers.forEach(c -> c.accept(listings));
                // Fabric: level 1 = generic offers, level 2 = rare offers
                (level <= 1 ? event.getGenericTrades() : event.getRareTrades()).addAll(listings);
            });
        });
    }
}
