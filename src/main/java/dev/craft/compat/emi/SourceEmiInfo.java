package dev.craft.compat.emi;

import java.util.List;

import dev.craft.core.client.SourceTooltip;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/** "Where to get it" info page for every item with a source text (spec 24 §2.2), shown under its recipes. */
final class SourceEmiInfo {
    private SourceEmiInfo() {
    }

    static void register(EmiRegistry registry) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (!SourceTooltip.hasSource(item)) {
                continue;
            }
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(item)), List.of(
                    Component.translatable("emi.craft.source.title").withStyle(ChatFormatting.BOLD),
                    Component.translatable(SourceTooltip.key(item))),
                    Ic2EmiRecipes.id("source/" + id.getNamespace() + "/" + id.getPath())));
        }
    }
}
