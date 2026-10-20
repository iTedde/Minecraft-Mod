package dev.craft.thermalexpansion;

import java.util.Set;

import dev.craft.gregtech.GtItems;
import dev.craft.thermalexpansion.world.TeWorldGen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;

/** Entry point of Thermal Expansion 2.2.2.2 (spec 26). */
public final class ThermalExpansion {
    /** {@code ChestGenHooks} "dungeonChest", "pyramidDesertyChest", "pyramidJungleChest". */
    private static final Set<ResourceLocation> PLATINUM_CHESTS = Set.of(
            ResourceLocation.withDefaultNamespace("chests/simple_dungeon"),
            ResourceLocation.withDefaultNamespace("chests/desert_pyramid"),
            ResourceLocation.withDefaultNamespace("chests/jungle_temple"));

    private ThermalExpansion() {
    }

    public static void init(IEventBus modBus) {
        TeBlocks.BLOCKS.register(modBus);
        TeItems.ITEMS.register(modBus);
        TeFluids.FLUID_TYPES.register(modBus);
        TeFluids.FLUIDS.register(modBus);
        TeWorldGen.FEATURES.register(modBus);
        NeoForge.EVENT_BUS.addListener(ThermalExpansion::onLootTableLoad);
    }

    /**
     * Shiny (platinum) ingots 1–3, weight 5 in the 1.4.7 chest lists; 1.21 chests have no shared list, so an extra pool
     * with the same expectation per chest (about 8 rolls at 5 of ~100 weight) is added.
     */
    private static void onLootTableLoad(LootTableLoadEvent event) {
        if (!PLATINUM_CHESTS.contains(event.getName())) {
            return;
        }
        event.getTable().addPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(GtItems.PLATINUM_INGOT.get()).setWeight(35)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                .add(EmptyLootItem.emptyItem().setWeight(65))
                .build());
    }
}
