package dev.craft.omnitools;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Forge Lexicon groups (spec 50 §1.3): the pack's {@code cofh/lexicon.whitelist} (ore, dust, small dust, ingot names)
 * mapped to the common {@code c:} item tags ({@code oreTin} → {@code c:ores/tin}, {@code dustSmallIron} →
 * {@code c:small_dusts/iron}); GT's "aluminium" is the convention's "aluminum". The IC2 storage names of the list
 * have no tag and are left out. A tag with fewer than two items converts nothing.
 */
public final class LexiconGroups {
    private static final String[] DUSTS = {"almandine", "aluminum", "andradite", "ash", "basalt", "bauxite", "brass",
            "bronze", "calcite", "charcoal", "chrome", "cinnabar", "clay", "coal", "copper", "dark_ash", "diamond",
            "electrum", "emerald", "ender_eye", "ender_pearl", "endstone", "flint", "garnet_red", "garnet_yellow",
            "glowstone", "gold", "green_sapphire", "grossular", "gunpowder", "invar", "iron", "lapis", "lazurite",
            "lead", "magnesium", "manganese", "marble", "netherrack", "nickel", "nikolite", "obsidian", "olivine",
            "phosphorus", "platinum", "plutonium", "pyrite", "pyrope", "red_garnet", "red_rock", "redstone", "ruby",
            "saltpeter", "sapphire", "silver", "sodalite", "spessartine", "sphalerite", "steel", "sulfur", "thorium",
            "tin", "titanium", "tungsten", "uranium", "uvarovite", "wood", "zinc"};
    /** The whitelist's small dusts: the dusts above without lapis and nikolite. */
    private static final List<String> NO_SMALL_DUST = List.of("lapis", "nikolite");
    private static final String[] INGOTS = {"aluminum", "brass", "bronze", "chrome", "copper", "electrum", "gold",
            "invar", "iridium", "iron", "lead", "nickel", "platinum", "red_garnet", "refined_iron", "silver", "steel",
            "tin", "titanium", "tungsten", "tungsten_steel", "uranium", "zinc"};
    private static final String[] ORES = {"apatite", "bauxite", "coal", "copper", "diamond", "emerald", "end_cooperite",
            "end_olivine", "end_sodalite", "end_tungstate", "gold", "green_sapphire", "iridium", "iron", "lapis", "lead",
            "natural_aluminum", "nether_cinnabar", "nether_pyrite", "nether_sphalerite", "nickel", "nikolite",
            "redstone", "ruby", "saltpeter", "sapphire", "silver", "sulfur", "tin", "tungsten", "uranium"};

    public static final List<TagKey<Item>> GROUPS = build();

    private LexiconGroups() {
    }

    private static List<TagKey<Item>> build() {
        List<TagKey<Item>> groups = new ArrayList<>();
        for (String dust : DUSTS) {
            groups.add(tag("dusts/" + dust));
            if (!NO_SMALL_DUST.contains(dust)) {
                groups.add(tag("small_dusts/" + dust));
            }
        }
        for (String ingot : INGOTS) {
            groups.add(tag("ingots/" + ingot));
        }
        for (String ore : ORES) {
            groups.add(tag("ores/" + ore));
        }
        return List.copyOf(groups);
    }

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    /** First whitelisted group containing {@code stack} that offers a choice. */
    public static Optional<TagKey<Item>> groupOf(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        for (TagKey<Item> group : GROUPS) {
            if (stack.is(group) && variants(group).size() > 1) {
                return Optional.of(group);
            }
        }
        return Optional.empty();
    }

    /** Items of {@code group} in tag order, without duplicates. */
    public static List<Item> variants(TagKey<Item> group) {
        List<Item> items = new ArrayList<>();
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(group)) {
            if (!items.contains(holder.value())) {
                items.add(holder.value());
            }
        }
        return items;
    }

    /** {@code LexiconManager.equivalentOres}. */
    public static boolean equivalent(ItemStack a, ItemStack b) {
        Optional<TagKey<Item>> group = groupOf(a);
        return group.isPresent() && b.is(group.get());
    }
}
