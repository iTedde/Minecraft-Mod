package dev.craft.ic2.item.armor;

import java.util.EnumMap;
import java.util.List;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Items;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Armor materials of the IC2 armor, one per armor texture ({@code armor/ic2/<name>_1|2.png}). IC2 armor is not
 * enchantable and not repairable with material; its armor points come from {@link SpecialArmor#getArmorDisplay}
 * (the material defense only carries the constant display values).
 */
public final class Ic2ArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, Craft.MODID);

    public static final Holder<ArmorMaterial> NANO = register("nano", 0);
    public static final Holder<ArmorMaterial> QUANTUM = register("quantum", 0);
    /** Hazmat suit and rubber boots, 1 armor point each. */
    public static final Holder<ArmorMaterial> HAZMAT = register("hazmat", 1);
    public static final Holder<ArmorMaterial> JETPACK = register("jetpack", 0);
    /** BatPack and CF pack. */
    public static final Holder<ArmorMaterial> BATPACK = register("batpack", 0);
    public static final Holder<ArmorMaterial> LAPPACK = register("lappack", 0);
    public static final Holder<ArmorMaterial> SOLAR = register("solar", 0);
    /** Static boots. */
    public static final Holder<ArmorMaterial> RUBBER = register("rubber", 0);
    public static final Holder<ArmorMaterial> NIGHTVISION = register("nightvision", 0);
    /** GregTech lithium batpack (texture from {@code armor/lithiumbatpack_1.png}). */
    public static final Holder<ArmorMaterial> LITHIUM_BATPACK = register("lithium_batpack", 0);
    /**
     * IC2 {@code IC2_ALLOY} (composite vest): a plain vanilla material, defense 4/9/7/4, enchantability 12, repaired
     * with advanced alloy; durability factor 50 is applied by the item.
     */
    public static final Holder<ArmorMaterial> ALLOY = MATERIALS.register("alloy", () -> {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.HELMET, 4);
        defense.put(ArmorItem.Type.CHESTPLATE, 9);
        defense.put(ArmorItem.Type.LEGGINGS, 7);
        defense.put(ArmorItem.Type.BOOTS, 4);
        defense.put(ArmorItem.Type.BODY, 9);
        return new ArmorMaterial(defense, 12, SoundEvents.ARMOR_EQUIP_GENERIC,
                () -> Ingredient.of(Ic2Items.ADVANCED_ALLOY), List.of(new ArmorMaterial.Layer(
                        ResourceLocation.fromNamespaceAndPath(Craft.MODID, "alloy"))), 0.0F, 0.0F);
    });
    /** IC2 {@code IC2_ALLOY} durability factor (chest 16 · 50 = 800). */
    public static final int ALLOY_DURABILITY = 50;

    private Ic2ArmorMaterials() {
    }

    private static Holder<ArmorMaterial> register(String name, int defense) {
        EnumMap<ArmorItem.Type, Integer> defenseMap = new EnumMap<>(ArmorItem.Type.class);
        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            defenseMap.put(type, defense);
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Craft.MODID, name);
        return MATERIALS.register(name, () -> new ArmorMaterial(defenseMap, 0, SoundEvents.ARMOR_EQUIP_GENERIC,
                () -> Ingredient.EMPTY, List.of(new ArmorMaterial.Layer(id)), 0.0F, 0.0F));
    }
}
