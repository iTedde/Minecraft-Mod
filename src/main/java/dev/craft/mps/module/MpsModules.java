package dev.craft.mps.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.mps.MpsItems;
import dev.craft.mps.module.PowerModule.Kind;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/**
 * All modules in MPS registration order ({@code Config.loadPowerModules}) with the values of the pack config
 * {@code mmmPowersuits.cfg} where it overrides the code (spec 29 §3.2). Aurameter, Multimeter and Radiation Shielding
 * need mods Craft does not have and are left out (spec 29 §15).
 */
public final class MpsModules {
    public static final String ARMOR_PHYSICAL = "Armor (Physical)";
    public static final String ARMOR_ENERGY = "Armor (Energy)";
    public static final String ENERGY_PER_DAMAGE = "Energy Per Damage";
    public static final String WEIGHT = "Weight";
    public static final String MAXIMUM_ENERGY = "Maximum Energy";

    public static final String NIGHT_VISION = "Night Vision";
    public static final String ACTIVE_CAMOUFLAGE = "Active Camouflage";
    public static final String FLIGHT_CONTROL = "Flight Control";
    public static final String PLASMA_CANNON = "Plasma Cannon";
    public static final String MELEE_ASSIST = "Melee Assist";
    public static final String RAILGUN = "Railgun";
    public static final String IN_PLACE_ASSEMBLER = "In-Place Assembler";
    public static final String BLINK_DRIVE = "Blink Drive";
    public static final String KINETIC_GENERATOR = "Kinetic Generator";
    public static final String SOLAR_GENERATOR = "Solar Generator";
    public static final String AUTO_FEEDER = "Auto-Feeder";
    public static final String IRON_PLATING = "Iron Plating";
    public static final String DIAMOND_PLATING = "Diamond Plating";
    public static final String ENERGY_SHIELD = "Energy Shield";
    public static final String SHOVEL = "Shovel";
    public static final String PICKAXE = "Pickaxe";
    public static final String AXE = "Axe";
    public static final String DIAMOND_DRILL_UPGRADE = "Diamond Drill Upgrade";
    public static final String AQUA_AFFINITY = "Aqua Affinity";
    public static final String BASIC_BATTERY = "Basic Battery";
    public static final String ADVANCED_BATTERY = "Advanced Battery";
    public static final String ELITE_BATTERY = "Elite Battery";
    public static final String SPRINT_ASSIST = "Sprint Assist";
    public static final String JUMP_ASSIST = "Jump Assist";
    public static final String SHOCK_ABSORBER = "Shock Absorber";
    public static final String GLIDER = "Glider";
    public static final String PARACHUTE = "Parachute";
    public static final String JETPACK = "Jetpack";
    public static final String JET_BOOTS = "Jet Boots";
    public static final String WATER_ELECTROLYZER = "Water Electrolyzer";
    public static final String SWIM_BOOST = "Swim Boost";
    public static final String UPHILL_STEP_ASSIST = "Uphill Step Assist";
    public static final String CUSTOM_COLOUR = "Custom Colour Module";
    public static final String TRANSPARENT_ARMOR = "Transparent Armor";
    public static final String CITIZEN_JOE_STYLE = "Citizen Joe Style";

    private static final Map<String, PowerModule> BY_NAME = new LinkedHashMap<>();
    private static final List<PowerModule> ALL = new ArrayList<>();
    /** Units of the property values ({@code PowerModuleBase.units}). */
    private static final Map<String, String> UNITS = new LinkedHashMap<>();

    private static final Set<ModuleTarget> HEAD = EnumSet.of(ModuleTarget.HEAD);
    private static final Set<ModuleTarget> TORSO = EnumSet.of(ModuleTarget.TORSO);
    private static final Set<ModuleTarget> LEGS = EnumSet.of(ModuleTarget.LEGS);
    private static final Set<ModuleTarget> FEET = EnumSet.of(ModuleTarget.FEET);
    private static final Set<ModuleTarget> TOOL = EnumSet.of(ModuleTarget.TOOL);

    static {
        add(NIGHT_VISION, HEAD, "Special", Kind.TOGGLEABLE)
                .cost(MpsItems.LASER_EMITTER, 1).cost(MpsItems.CONTROL_CIRCUIT, 1);
        add(ACTIVE_CAMOUFLAGE, TORSO, "Special", Kind.TOGGLEABLE)
                .cost(MpsItems.LASER_EMITTER, 4).cost(MpsItems.FIELD_EMITTER, 2).cost(MpsItems.CONTROL_CIRCUIT, 2);
        add(FLIGHT_CONTROL, HEAD, "Special", Kind.TOGGLEABLE).cost(MpsItems.CONTROL_CIRCUIT, 1);
        add(PLASMA_CANNON, TOOL, "Weapon", Kind.RIGHT_CLICK)
                .base("Plasma Energy Per Tick", 10.0).base("Plasma Damage At Full Charge", 2.0)
                .tradeoff("Amperage", "Plasma Energy Per Tick", 150.0)
                .tradeoff("Amperage", "Plasma Damage At Full Charge", 18.0)
                .tradeoff("Voltage", "Plasma Energy Per Tick", 50.0)
                .tradeoff("Voltage", "Plasma Explosiveness", 0.5)
                .cost(MpsItems.FIELD_EMITTER, 2).cost(MpsItems.HV_CAPACITOR, 2);
        add(MELEE_ASSIST, TOOL, "Weapon", Kind.TOGGLEABLE)
                .base("Punch Energy Consumption", 10.0).base("Melee Damage", 2.0)
                .tradeoff("Impact", "Punch Energy Consumption", 100.0).tradeoff("Impact", "Melee Damage", 8.0)
                .tradeoff("Carry-through", "Punch Energy Consumption", 20.0)
                .tradeoff("Carry-through", "Melee Knockback", 1.0)
                .cost(MpsItems.SERVO, 2).cost(MpsItems.LV_CAPACITOR, 1);
        add(RAILGUN, TOOL, "Weapon", Kind.RIGHT_CLICK)
                .base("Railgun Total Impulse", 500.0).base("Railgun Energy Cost", 500.0)
                .tradeoff("Voltage", "Railgun Total Impulse", 2500.0)
                .tradeoff("Voltage", "Railgun Energy Cost", 2500.0)
                .cost(MpsItems.SOLENOID, 6).cost(MpsItems.HV_CAPACITOR, 1);
        add(IN_PLACE_ASSEMBLER, TOOL, "Special", Kind.RIGHT_CLICK,
                ResourceLocation.withDefaultNamespace("textures/block/crafting_table_front.png"))
                .cost(MpsItems.CONTROL_CIRCUIT, 1).cost(() -> Items.CRAFTING_TABLE, 1);
        add(BLINK_DRIVE, TOOL, "Special", Kind.RIGHT_CLICK)
                .base("Blink Drive Energy Consuption", 1000.0).base("Blink Drive Range", 5.0)
                .tradeoff("Range", "Blink Drive Energy Consuption", 3000.0)
                .tradeoff("Range", "Blink Drive Range", 59.0)
                .cost(MpsItems.ION_THRUSTER, 1).cost(MpsItems.FIELD_EMITTER, 2);
        add(KINETIC_GENERATOR, LEGS, "Energy", Kind.PASSIVE)
                .base(WEIGHT, 1000.0).base("Energy Generation Per 5 Blocks", 250.0)
                .tradeoff("Energy Generated", "Energy Generation Per 5 Blocks", 750.0)
                .tradeoff("Energy Generated", WEIGHT, 3000.0)
                .cost(MpsItems.SERVO, 2).cost(MpsItems.CONTROL_CIRCUIT, 1);
        // Pack config: 100/10 J instead of the code's 2 000/200 J.
        add(SOLAR_GENERATOR, HEAD, "Energy", Kind.PASSIVE)
                .base("Daytime Solar Energy Generation", 100.0).base("Nighttime Solar Energy Generation", 10.0)
                .cost(MpsItems.SOLAR_PANEL, 1).cost(MpsItems.CONTROL_CIRCUIT, 2);
        add(AUTO_FEEDER, HEAD, "Environment", Kind.TOGGLEABLE)
                .base("Eating Energy Consumption", 100.0).base("Auto-Feeder Efficiency", 50.0)
                .tradeoff("Efficiency", "Eating Energy Consumption", 100.0)
                .tradeoff("Efficiency", "Auto-Feeder Efficiency", 50.0)
                .cost(MpsItems.SERVO, 2).cost(MpsItems.CONTROL_CIRCUIT, 1);
        add(IRON_PLATING, ModuleTarget.ARMOR, "Armor", Kind.PASSIVE)
                .cost(MpsItems.PLATING_BASIC, 1)
                .tradeoff("Plating Thickness", ARMOR_PHYSICAL, 5.0).tradeoff("Plating Thickness", WEIGHT, 10000.0);
        add(DIAMOND_PLATING, ModuleTarget.ARMOR, "Armor", Kind.PASSIVE)
                .cost(MpsItems.PLATING_ADVANCED, 1)
                .tradeoff("Plating Thickness", ARMOR_PHYSICAL, 6.0).tradeoff("Plating Thickness", WEIGHT, 6000.0);
        add(ENERGY_SHIELD, ModuleTarget.ARMOR, "Armor", Kind.PASSIVE)
                .cost(MpsItems.FIELD_EMITTER, 2)
                .tradeoff("Field Strength", ARMOR_ENERGY, 6.0).tradeoff("Field Strength", ENERGY_PER_DAMAGE, 500.0);
        // Pack config: 100 + 900 J and 8 + 17× instead of the code's 50 + 950 J and 8 + 22×.
        tool(SHOVEL);
        tool(PICKAXE);
        tool(AXE);
        add(DIAMOND_DRILL_UPGRADE, TOOL, "Special", Kind.PASSIVE)
                .cost(MpsItems.SOLENOID, 1).cost(() -> Items.DIAMOND, 3);
        add(AQUA_AFFINITY, TOOL, "Special", Kind.TOGGLEABLE)
                .cost(MpsItems.SERVO, 1)
                .base("Underwater Energy Consumption", 0.0).base("Underwater Harvest Speed", 0.2)
                .tradeoff("Power", "Underwater Energy Consumption", 100.0)
                .tradeoff("Power", "Underwater Harvest Speed", 0.8);
        battery(BASIC_BATTERY, MpsItems.LV_CAPACITOR, 20000.0, 80000.0);
        battery(ADVANCED_BATTERY, MpsItems.MV_CAPACITOR, 100000.0, 400000.0);
        battery(ELITE_BATTERY, MpsItems.HV_CAPACITOR, 750000.0, 4250000.0);
        add(SPRINT_ASSIST, LEGS, "Movement", Kind.TOGGLEABLE)
                .cost(MpsItems.SERVO, 4)
                .base("Sprint Energy Consumption", 0.0).tradeoff("Power", "Sprint Energy Consumption", 10.0)
                .base("Sprint Speed Multiplier", 1.0).tradeoff("Power", "Sprint Speed Multiplier", 2.0)
                .base("Sprint Energy Consumption", 0.0).tradeoff("Compensation", "Sprint Energy Consumption", 2.0)
                .base("Sprint Exhaustion Compensation", 0.0)
                .tradeoff("Compensation", "Sprint Exhaustion Compensation", 1.0)
                .base("Walking Energy Consumption", 0.0)
                .tradeoff("Walking Assist", "Walking Energy Consumption", 10.0)
                .base("Walking Speed Multiplier", 1.0).tradeoff("Walking Assist", "Walking Speed Multiplier", 1.0);
        add(JUMP_ASSIST, LEGS, "Movement", Kind.TOGGLEABLE)
                .base("Jump Energy Consumption", 0.0).tradeoff("Power", "Jump Energy Consumption", 25.0)
                .base("Jump Boost", 1.0).tradeoff("Power", "Jump Boost", 4.0)
                .base("Jump Energy Consumption", 0.0).tradeoff("Compensation", "Jump Energy Consumption", 5.0)
                .base("Jump Exhaustion Compensation", 0.0)
                .tradeoff("Compensation", "Jump Exhaustion Compensation", 1.0)
                .cost(MpsItems.SERVO, 4);
        add(SHOCK_ABSORBER, FEET, "Movement", Kind.TOGGLEABLE)
                .base("Impact Energy consumption", 0.0).tradeoff("Power", "Impact Energy consumption", 10.0)
                .base("Distance Reduction", 0.0).tradeoff("Power", "Distance Reduction", 1.0)
                .cost(MpsItems.SERVO, 2).cost(() -> Items.WHITE_WOOL, 2);
        add(GLIDER, TORSO, "Movement", Kind.TOGGLEABLE).cost(MpsItems.GLIDER_WING, 2);
        add(PARACHUTE, TORSO, "Movement", Kind.PASSIVE).cost(MpsItems.PARACHUTE, 2);
        add(JETPACK, TORSO, "Movement", Kind.TOGGLEABLE)
                .cost(MpsItems.ION_THRUSTER, 4)
                .base("Jet Energy Consumption", 0.0).base("Jet Thrust", 0.0)
                .tradeoff("Thrust", "Jet Energy Consumption", 150.0).tradeoff("Thrust", "Jet Thrust", 0.16);
        add(JET_BOOTS, FEET, "Movement", Kind.TOGGLEABLE)
                .cost(MpsItems.ION_THRUSTER, 2)
                .base("Jet Energy Consumption", 0.0).base("Jet Thrust", 0.0)
                .tradeoff("Thrust", "Jet Energy Consumption", 75.0).tradeoff("Thrust", "Jet Thrust", 0.08);
        add(WATER_ELECTROLYZER, HEAD, "Environment", Kind.TOGGLEABLE)
                .cost(MpsItems.LV_CAPACITOR, 1).base("Jolt Energy", 1000.0);
        add(SWIM_BOOST, LEGS, "Movement", Kind.TOGGLEABLE)
                .cost(MpsItems.ION_THRUSTER, 1).cost(MpsItems.SOLENOID, 2)
                .tradeoff("Thrust", "Swim Boost Energy Consumption", 100.0)
                .tradeoff("Thrust", "Underwater Movement Boost", 1.0);
        add(UPHILL_STEP_ASSIST, LEGS, "Movement", Kind.TOGGLEABLE).cost(MpsItems.SERVO, 2);
        add(CUSTOM_COLOUR, ModuleTarget.ALL, "Cosmetic", Kind.TOGGLEABLE)
                .cost(MpsItems.LASER_EMITTER, 1)
                .tradeoff("Red Intensity", "Red Tint", 1.0)
                .tradeoff("Green Intensity", "Green Tint", 1.0)
                .tradeoff("Blue Intensity", "Blue Tint", 1.0);
        add(TRANSPARENT_ARMOR, ModuleTarget.ARMOR, "Cosmetic", Kind.TOGGLEABLE)
                .cost(MpsItems.LASER_EMITTER, 1);
        add(CITIZEN_JOE_STYLE, ModuleTarget.ARMOR, "Cosmetic", Kind.PASSIVE);

        unit("J", "Punch Energy Consumption", "Plasma Energy Per Tick", "Railgun Energy Cost",
                "Blink Drive Energy Consuption", ENERGY_PER_DAMAGE, "Shovel Energy Consumption",
                "Pickaxe Energy Consumption", "Axe Energy Consumption", "Underwater Energy Consumption",
                MAXIMUM_ENERGY, "Sprint Energy Consumption", "Walking Energy Consumption", "Jump Energy Consumption",
                "Swim Boost Energy Consumption", "Jolt Energy");
        unit("pt", "Melee Damage", "Plasma Damage At Full Charge");
        unit("P", "Melee Knockback");
        unit("Creeper", "Plasma Explosiveness");
        unit("Ns", "Railgun Total Impulse");
        unit("m", "Blink Drive Range");
        unit(" Joules", "Energy Generation Per 5 Blocks");
        unit("g", WEIGHT);
        unit(" Points", ARMOR_PHYSICAL, ARMOR_ENERGY);
        unit("x", "Shovel Harvest Speed", "Pickaxe Harvest Speed", "Axe Harvest Speed");
        unit("%", "Underwater Harvest Speed", "Sprint Speed Multiplier", "Sprint Exhaustion Compensation",
                "Walking Speed Multiplier", "Jump Boost", "Jump Exhaustion Compensation", "Distance Reduction",
                "Red Tint", "Green Tint", "Blue Tint");
        unit("J/m", "Impact Energy consumption");
        unit("J/t", "Jet Energy Consumption");
        unit("N", "Jet Thrust");
        unit("m/s", "Underwater Movement Boost");
    }

    private MpsModules() {
    }

    public static List<PowerModule> all() {
        return Collections.unmodifiableList(ALL);
    }

    public static PowerModule get(String name) {
        return BY_NAME.get(name);
    }

    public static String unit(String property) {
        return UNITS.getOrDefault(property, "");
    }

    /** Icon {@code textures/gui/mps/module/<key>.png}, cut by {@code tools/scripts/mps_textures.py}. */
    private static PowerModule add(String name, Set<ModuleTarget> targets, String category, Kind kind) {
        String path = PowerModule.key(name);
        return add(name, targets, category, kind,
                ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/mps/module/" + path + ".png"));
    }

    private static PowerModule add(String name, Set<ModuleTarget> targets, String category, Kind kind,
                                   ResourceLocation icon) {
        PowerModule module = new PowerModule(name, targets, category, kind, icon);
        BY_NAME.put(name, module);
        ALL.add(module);
        return module;
    }

    private static void tool(String name) {
        add(name, TOOL, "Tool", Kind.TOGGLEABLE)
                .cost(() -> Items.IRON_INGOT, 3).cost(MpsItems.SOLENOID, 1)
                .base(name + " Energy Consumption", 100.0).base(name + " Harvest Speed", 8.0)
                .tradeoff("Overclock", name + " Energy Consumption", 900.0)
                .tradeoff("Overclock", name + " Harvest Speed", 17.0);
    }

    private static void battery(String name, Supplier<? extends ItemLike> capacitor, double energy,
                                double energyPerSize) {
        add(name, ModuleTarget.ALL, "Energy", Kind.PASSIVE)
                .cost(capacitor, 1)
                .base(MAXIMUM_ENERGY, energy).base(WEIGHT, 2000.0)
                .tradeoff("Battery Size", MAXIMUM_ENERGY, energyPerSize).tradeoff("Battery Size", WEIGHT, 8000.0);
    }

    private static void unit(String unit, String... properties) {
        for (String property : properties) {
            UNITS.put(property, unit);
        }
    }
}
