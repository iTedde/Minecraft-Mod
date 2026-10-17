package dev.craft.redpower;

import java.util.function.Supplier;

import dev.craft.gregtech.GtItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.SimpleTier;

/**
 * RedPower World tool materials (spec 19 §1): {@code addToolMaterial(name, 2, 500, 8.0F, 3, 12)}, equal for all three
 * gems; repaired with the own gem. The gems themselves are the GregTech ones.
 */
public enum RpGem {
    RUBY("ruby", GtItems.RUBY_DUST),
    GREEN_SAPPHIRE("green_sapphire", GtItems.GREEN_SAPPHIRE_DUST),
    SAPPHIRE("sapphire", GtItems.SAPPHIRE_DUST);

    public static final int DURABILITY = 500;

    private final String name;
    private final TagKey<Item> gemTag;
    private final Supplier<? extends ItemLike> dust;
    private final Tier tier;

    RpGem(String name, Supplier<? extends ItemLike> dust) {
        this.name = name;
        this.gemTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems/" + name));
        this.dust = dust;
        this.tier = new SimpleTier(BlockTags.INCORRECT_FOR_IRON_TOOL, DURABILITY, 8.0F, 3.0F, 12,
                () -> Ingredient.of(gemTag));
    }

    public String getName() {
        return name;
    }

    public TagKey<Item> getGemTag() {
        return gemTag;
    }

    /** GregTech dust of the gem, the macerator output of the tools (spec 19 §5). */
    public ItemLike getDust() {
        return dust.get();
    }

    public Tier getTier() {
        return tier;
    }
}
