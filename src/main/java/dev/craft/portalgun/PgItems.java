package dev.craft.portalgun;

import java.util.EnumMap;
import java.util.List;

import dev.craft.Craft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Portal Gun long fall boots (spec 21). */
public final class PgItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);
    public static final DeferredRegister<ArmorMaterial> MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, Craft.MODID);

    /**
     * {@code EnumArmorMaterial.DIAMOND} with the 1.4.7 values (defense 3/8/6/3, enchantability 10, no toughness),
     * textured with {@code skin_lfb.png}.
     */
    public static final Holder<ArmorMaterial> LONG_FALL = MATERIALS.register("long_fall_boots", () -> {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.HELMET, 3);
        defense.put(ArmorItem.Type.CHESTPLATE, 8);
        defense.put(ArmorItem.Type.LEGGINGS, 6);
        defense.put(ArmorItem.Type.BOOTS, 3);
        defense.put(ArmorItem.Type.BODY, 8);
        return new ArmorMaterial(defense, 10, SoundEvents.ARMOR_EQUIP_DIAMOND, () -> Ingredient.of(Items.DIAMOND),
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "long_fall_boots"))),
                0.0F, 0.0F);
    });

    /** {@code ItemPlaceholder} "Long Fall Boot": stack size 2, two of them make the boots. */
    public static final DeferredItem<Item> LONG_FALL_BOOT = ITEMS.registerItem("long_fall_boot",
            properties -> new Item(properties.stacksTo(2)));
    /** {@code ItemArmour}: diamond boots with {@code setMaxDamage(0)}, i.e. they never wear out. */
    public static final DeferredItem<ArmorItem> LONG_FALL_BOOTS = ITEMS.registerItem("long_fall_boots",
            properties -> new ArmorItem(LONG_FALL, ArmorItem.Type.BOOTS, properties.stacksTo(1)));

    private PgItems() {
    }
}
