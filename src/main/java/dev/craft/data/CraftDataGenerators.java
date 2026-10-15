package dev.craft.data;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import dev.craft.Craft;
import dev.craft.core.CraftDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@EventBusSubscriber(modid = Craft.MODID)
public final class CraftDataGenerators {
    private static final RegistrySetBuilder DATAPACK_ENTRIES = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, CraftDamageTypes::bootstrap)
            .add(Registries.CONFIGURED_FEATURE, CraftWorldGen::bootstrapConfigured)
            .add(Registries.PLACED_FEATURE, CraftWorldGen::bootstrapPlaced)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, CraftWorldGen::bootstrapBiomeModifiers);

    private CraftDataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper files = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        DatapackBuiltinEntriesProvider datapackEntries = generator.addProvider(event.includeServer(),
                new DatapackBuiltinEntriesProvider(output, lookup, DATAPACK_ENTRIES, Set.of(Craft.MODID)));
        CompletableFuture<HolderLookup.Provider> fullLookup = datapackEntries.getRegistryProvider();
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(CraftBlockLoot::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(CraftBlockLoot.Vanilla::new, LootContextParamSets.BLOCK)),
                lookup));
        CraftBlockTagsProvider blockTags = generator.addProvider(event.includeServer(),
                new CraftBlockTagsProvider(output, lookup, files));
        generator.addProvider(event.includeServer(),
                new CraftItemTagsProvider(output, lookup, blockTags.contentsGetter(), files));
        generator.addProvider(event.includeServer(), new CraftBiomeTagsProvider(output, fullLookup, files));
        generator.addProvider(event.includeServer(), new CraftRecipeProvider(output, lookup));
        generator.addProvider(event.includeServer(), new CraftDataMapProvider(output, lookup));

        generator.addProvider(event.includeClient(), new CraftBlockStateProvider(output, files));
        generator.addProvider(event.includeClient(), new CraftItemModelProvider(output, files));
        generator.addProvider(event.includeClient(), new CraftSoundDefinitionsProvider(output, files));
        generator.addProvider(event.includeClient(), new CraftLanguageProvider.English(output));
        generator.addProvider(event.includeClient(), new CraftLanguageProvider.German(output));
    }
}
