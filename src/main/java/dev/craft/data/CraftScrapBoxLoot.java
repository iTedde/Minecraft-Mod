package dev.craft.data;

import java.util.Optional;
import java.util.function.BiConsumer;

import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.item.ScrapBoxDrops;
import dev.craft.ic2.item.ScrapBoxItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/** Scrap box loot table from {@link ScrapBoxDrops} (one roll, weighted). */
public record CraftScrapBoxLoot(HolderLookup.Provider registries) implements LootTableSubProvider {
    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F));
        for (ScrapBoxDrops.Drop drop : ScrapBoxDrops.all()) {
            LootPoolSingletonContainer.Builder<?> entry = LootItem.lootTableItem(drop.stack().getItem())
                    .setWeight(drop.weight());
            Optional<? extends Integer> meta = drop.stack().getComponentsPatch().get(Ic2DataComponents.CAN_META.get());
            if (meta != null && meta.isPresent()) {
                entry.apply(SetComponentsFunction.setComponent(Ic2DataComponents.CAN_META.get(), meta.get()));
            }
            pool.add(entry);
        }
        output.accept(ScrapBoxItem.DROPS, LootTable.lootTable().withPool(pool));
    }
}
