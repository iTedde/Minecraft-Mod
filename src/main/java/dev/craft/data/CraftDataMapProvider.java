package dev.craft.data;

import java.util.concurrent.CompletableFuture;

import dev.craft.ic2.Ic2Items;
import dev.craft.ironchest.IronChestBlocks;
import dev.craft.powerplants.PpItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.Oxidizable;
import net.neoforged.neoforge.registries.datamaps.builtin.Waxable;

/** IC2 fuel handler ({@code IC2.getBurnTime}): burn times in vanilla furnaces, also used by IC2 machines. */
public final class CraftDataMapProvider extends DataMapProvider {
    public CraftDataMapProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        builder(NeoForgeDataMaps.FURNACE_FUELS)
                .add(Ic2Items.RUBBER_SAPLING, new FurnaceFuel(80), false)
                .add(Items.SUGAR_CANE.builtInRegistryHolder(), new FurnaceFuel(50), false)
                .add(Items.CACTUS.builtInRegistryHolder(), new FurnaceFuel(50), false)
                .add(Ic2Items.SCRAP, new FurnaceFuel(350), false)
                .add(Ic2Items.SCRAP_BOX, new FurnaceFuel(3150), false)
                .add(Ic2Items.LAVA_CELL, new FurnaceFuel(20000), false)
                // Coke burns like coal outside the coal power plant (spec 14 §1).
                .add(PpItems.COKE, new FurnaceFuel(1600), false)
                .add(PpItems.COKE_BLOCK, new FurnaceFuel(16000), false);
        // Copper chest stages (spec 35 §1): oxidation, waxing; scraping and lightning use the inverse maps.
        DataMapProvider.Builder<Oxidizable, Block> oxidizables = builder(NeoForgeDataMaps.OXIDIZABLES);
        DataMapProvider.Builder<Waxable, Block> waxables = builder(NeoForgeDataMaps.WAXABLES);
        WeatherState[] stages = WeatherState.values();
        for (WeatherState stage : stages) {
            if (stage.ordinal() + 1 < stages.length) {
                oxidizables.add(IronChestBlocks.copperChest(stage, false),
                        new Oxidizable(IronChestBlocks.copperChest(stages[stage.ordinal() + 1], false).get()), false);
            }
            waxables.add(IronChestBlocks.copperChest(stage, false),
                    new Waxable(IronChestBlocks.copperChest(stage, true).get()), false);
        }
    }
}
