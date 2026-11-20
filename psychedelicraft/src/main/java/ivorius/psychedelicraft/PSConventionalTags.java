package ivorius.psychedelicraft;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public interface PSConventionalTags {

    interface Items {
        TagKey<Item> APPLES = of("foods/apple");
        TagKey<Item> PINEAPPLES = of("foods/pineapple");
        TagKey<Item> POTATO = of("foods/potato");
        TagKey<Item> BANANAS = of("foods/banana");
        TagKey<Item> CORN = of("foods/corn");
        TagKey<Item> RICE = of("foods/rice");
        TagKey<Item> GRAPES = of("foods/grapes");
        TagKey<Item> HONEY = of("foods/honey");
        TagKey<Item> TOMATOES = of("foods/tomatoes");

        static TagKey<Item> of(String name) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", name));
        }
    }
}
