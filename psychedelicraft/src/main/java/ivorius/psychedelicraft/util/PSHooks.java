package ivorius.psychedelicraft.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;

/**
 * Collects the registrations upstream made through Fabric API helper registries (flammability, stripping, trades)
 * and hands them to the matching NeoForge events in {@code PSNeoForgeEvents}.
 */
public final class PSHooks {
    public record Flammability(Block block, int burn, int spread) {}

    public static final List<Flammability> FLAMMABLES = new ArrayList<>();
    public static final Map<Block, Block> STRIPPABLES = new HashMap<>();
    public static final Map<VillagerProfession, Map<Integer, List<Consumer<List<VillagerTrades.ItemListing>>>>> VILLAGER_TRADES = new HashMap<>();
    public static final Map<Integer, List<Consumer<List<VillagerTrades.ItemListing>>>> WANDERER_TRADES = new HashMap<>();

    private PSHooks() {
    }

    public static void flammable(Block block, int burn, int spread) {
        FLAMMABLES.add(new Flammability(block, burn, spread));
    }

    public static void strippable(Block log, Block stripped) {
        STRIPPABLES.put(log, stripped);
    }

    public static void villagerOffers(VillagerProfession profession, int level, Consumer<List<VillagerTrades.ItemListing>> factories) {
        VILLAGER_TRADES.computeIfAbsent(profession, p -> new HashMap<>()).computeIfAbsent(level, l -> new ArrayList<>()).add(factories);
    }

    public static void wanderingTraderOffers(int level, Consumer<List<VillagerTrades.ItemListing>> factories) {
        WANDERER_TRADES.computeIfAbsent(level, l -> new ArrayList<>()).add(factories);
    }

    public static void applyFlammability() {
        FireBlock fire = (FireBlock)Blocks.FIRE;
        FLAMMABLES.forEach(f -> fire.setFlammable(f.block(), f.burn(), f.spread()));
    }
}
