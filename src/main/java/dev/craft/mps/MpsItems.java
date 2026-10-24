package dev.craft.mps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import dev.craft.Craft;
import dev.craft.mps.item.PowerArmorItem;
import dev.craft.mps.item.PowerToolItem;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Modular Powersuits items (spec 29 §1); textures under {@code item/mps/}. */
public final class MpsItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);
    public static final DeferredRegister<ArmorMaterial> MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, Craft.MODID);

    private static final List<DeferredItem<Item>> COMPONENTS = new ArrayList<>();

    /** {@code sebkarmor.png} as layer 1, {@code sebkarmorpants.png} as layer 2; protection comes from the modules. */
    public static final Holder<ArmorMaterial> POWER_ARMOR_MATERIAL = MATERIALS.register("power_armor", () -> {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            defense.put(type, 0);
        }
        return new ArmorMaterial(defense, 0, SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.EMPTY,
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "power_armor"))),
                0.0F, 0.0F);
    });

    /** {@code ItemComponent} meta 0–17 in order. */
    public static final DeferredItem<Item> WIRING = component("wiring");
    public static final DeferredItem<Item> SOLENOID = component("solenoid");
    public static final DeferredItem<Item> SERVO = component("servo");
    public static final DeferredItem<Item> GLIDER_WING = component("glider_wing");
    public static final DeferredItem<Item> ION_THRUSTER = component("ion_thruster");
    public static final DeferredItem<Item> LV_CAPACITOR = component("lv_capacitor");
    public static final DeferredItem<Item> MV_CAPACITOR = component("mv_capacitor");
    public static final DeferredItem<Item> HV_CAPACITOR = component("hv_capacitor");
    public static final DeferredItem<Item> PARACHUTE = component("parachute");
    public static final DeferredItem<Item> PLATING_BASIC = component("plating_basic");
    public static final DeferredItem<Item> PLATING_ADVANCED = component("plating_advanced");
    public static final DeferredItem<Item> FIELD_EMITTER = component("field_emitter");
    public static final DeferredItem<Item> LASER_EMITTER = component("laser_emitter");
    /** No recipe and no use in the pack. */
    public static final DeferredItem<Item> CARBON_MYOFIBER = component("carbon_myofiber");
    public static final DeferredItem<Item> CONTROL_CIRCUIT = component("control_circuit");
    /** No recipe and no use in the pack. */
    public static final DeferredItem<Item> MYOFIBER_GEL = component("myofiber_gel");
    /** No recipe and no use in the pack. */
    public static final DeferredItem<Item> ARTIFICIAL_MUSCLE = component("artificial_muscle");
    public static final DeferredItem<Item> SOLAR_PANEL = component("solar_panel");

    public static final DeferredItem<PowerArmorItem> POWER_ARMOR_HEAD = armor("power_armor_head",
            ArmorItem.Type.HELMET);
    public static final DeferredItem<PowerArmorItem> POWER_ARMOR_TORSO = armor("power_armor_torso",
            ArmorItem.Type.CHESTPLATE);
    public static final DeferredItem<PowerArmorItem> POWER_ARMOR_LEGS = armor("power_armor_legs",
            ArmorItem.Type.LEGGINGS);
    public static final DeferredItem<PowerArmorItem> POWER_ARMOR_FEET = armor("power_armor_feet",
            ArmorItem.Type.BOOTS);
    public static final DeferredItem<PowerToolItem> POWER_TOOL = ITEMS.registerItem("power_tool", PowerToolItem::new);

    public static final DeferredItem<BlockItem> TINKER_TABLE = ITEMS.registerSimpleBlockItem(MpsBlocks.TINKER_TABLE);

    private MpsItems() {
    }

    public static List<DeferredItem<Item>> components() {
        return Collections.unmodifiableList(COMPONENTS);
    }

    public static List<DeferredItem<PowerArmorItem>> armor() {
        return List.of(POWER_ARMOR_HEAD, POWER_ARMOR_TORSO, POWER_ARMOR_LEGS, POWER_ARMOR_FEET);
    }

    private static DeferredItem<Item> component(String name) {
        DeferredItem<Item> item = ITEMS.registerSimpleItem("component_" + name);
        COMPONENTS.add(item);
        return item;
    }

    private static DeferredItem<PowerArmorItem> armor(String name, ArmorItem.Type type) {
        return ITEMS.registerItem(name, properties -> new PowerArmorItem(POWER_ARMOR_MATERIAL, type, properties));
    }
}
