package dev.craft.gravisuite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import dev.craft.Craft;
import dev.craft.gravisuite.item.AdvancedChainsawItem;
import dev.craft.gravisuite.item.AdvancedDrillItem;
import dev.craft.gravisuite.item.AdvancedJetpackItem;
import dev.craft.gravisuite.item.AdvancedNanoChestplateItem;
import dev.craft.gravisuite.item.GraviChestplateItem;
import dev.craft.gravisuite.item.VajraItem;
import dev.craft.ic2.item.armor.EnergyPackItem;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** GraviSuite items (spec 28 §1); textures under {@code item/gravisuite/}, armor under {@code models/armor/}. */
public final class GsItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);
    public static final DeferredRegister<ArmorMaterial> MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, Craft.MODID);

    private static final List<DeferredItem<Item>> SIMPLE = new ArrayList<>();
    private static final List<DeferredItem<? extends Item>> ARMOR = new ArrayList<>();
    private static final List<DeferredItem<? extends Item>> TOOLS = new ArrayList<>();

    public static final Holder<ArmorMaterial> ADVANCED_LAPPACK_MATERIAL = material("advanced_lappack");
    public static final Holder<ArmorMaterial> ULTIMATE_LAPPACK_MATERIAL = material("ultimate_lappack");
    public static final Holder<ArmorMaterial> ADVANCED_JETPACK_MATERIAL = material("advanced_jetpack");
    public static final Holder<ArmorMaterial> ADVANCED_NANO_CHESTPLATE_MATERIAL = material("advanced_nano_chestplate");
    public static final Holder<ArmorMaterial> GRAVI_CHESTPLATE_MATERIAL = material("gravi_chestplate");

    /** {@code ItemSimpleItems}, stack size 64. */
    public static final DeferredItem<Item> SUPERCONDUCTOR_COVER = simple("superconductor_cover");
    public static final DeferredItem<Item> SUPERCONDUCTOR = simple("superconductor");
    public static final DeferredItem<Item> COOLING_CORE = simple("cooling_core");
    public static final DeferredItem<Item> GRAVITATION_ENGINE = simple("gravitation_engine");
    public static final DeferredItem<Item> MAGNETRON = simple("magnetron");
    public static final DeferredItem<Item> VAJRA_CORE = simple("vajra_core");
    public static final DeferredItem<Item> ENGINE_BOOSTER = simple("engine_booster");

    /** {@code ItemAdvancedLappack}: 1 000 000 EU, transfer 1 000, tier 2, no protection. */
    public static final DeferredItem<EnergyPackItem> ADVANCED_LAPPACK = armor("advanced_lappack",
            properties -> new EnergyPackItem(ADVANCED_LAPPACK_MATERIAL, properties.rarity(Rarity.UNCOMMON), 1000000,
                    1000, 2));
    /** {@code ItemUltimateLappack}: 10 000 000 EU, transfer 20 000, tier 2, no protection. */
    public static final DeferredItem<EnergyPackItem> ULTIMATE_LAPPACK = armor("ultimate_lappack",
            properties -> new EnergyPackItem(ULTIMATE_LAPPACK_MATERIAL, properties.rarity(Rarity.RARE), 10000000,
                    20000, 2));
    public static final DeferredItem<AdvancedJetpackItem> ADVANCED_JETPACK = armor("advanced_jetpack",
            properties -> new AdvancedJetpackItem(ADVANCED_JETPACK_MATERIAL, properties.rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<AdvancedNanoChestplateItem> ADVANCED_NANO_CHESTPLATE = armor(
            "advanced_nano_chestplate", properties -> new AdvancedNanoChestplateItem(ADVANCED_NANO_CHESTPLATE_MATERIAL,
                    properties.rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<GraviChestplateItem> GRAVI_CHESTPLATE = armor("gravi_chestplate",
            properties -> new GraviChestplateItem(GRAVI_CHESTPLATE_MATERIAL, properties.rarity(Rarity.EPIC)));

    public static final DeferredItem<AdvancedDrillItem> ADVANCED_DIAMOND_DRILL = tool("advanced_diamond_drill",
            properties -> new AdvancedDrillItem(properties.rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<AdvancedChainsawItem> ADVANCED_CHAINSAW = tool("advanced_chainsaw",
            properties -> new AdvancedChainsawItem(properties.rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<VajraItem> VAJRA = tool("vajra",
            properties -> new VajraItem(properties.rarity(Rarity.EPIC)));

    private GsItems() {
    }

    public static List<DeferredItem<Item>> simpleItems() {
        return Collections.unmodifiableList(SIMPLE);
    }

    public static List<DeferredItem<? extends Item>> armorItems() {
        return Collections.unmodifiableList(ARMOR);
    }

    public static List<DeferredItem<? extends Item>> toolItems() {
        return Collections.unmodifiableList(TOOLS);
    }

    private static <T extends Item> DeferredItem<T> tool(String name,
                                                         java.util.function.Function<Item.Properties, T> factory) {
        DeferredItem<T> item = ITEMS.registerItem(name, factory);
        TOOLS.add(item);
        return item;
    }

    private static DeferredItem<Item> simple(String name) {
        DeferredItem<Item> item = ITEMS.registerSimpleItem(name);
        SIMPLE.add(item);
        return item;
    }

    private static <T extends Item> DeferredItem<T> armor(String name,
                                                          java.util.function.Function<Item.Properties, T> factory) {
        DeferredItem<T> item = ITEMS.registerItem(name, factory);
        ARMOR.add(item);
        return item;
    }

    /** IC2-style armor material without defense (the electric armor computes its own), texture {@code <name>_layer_1}. */
    private static Holder<ArmorMaterial> material(String name) {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            defense.put(type, 0);
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Craft.MODID, name);
        return MATERIALS.register(name, () -> new ArmorMaterial(defense, 0, SoundEvents.ARMOR_EQUIP_GENERIC,
                () -> Ingredient.EMPTY, List.of(new ArmorMaterial.Layer(id)), 0.0F, 0.0F));
    }
}
