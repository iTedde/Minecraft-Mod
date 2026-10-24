package dev.craft.mps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.armor.ArmorProperties;
import dev.craft.mps.module.MpsModules;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Phase 28.1: modules, energy, Tinker Table actions, armor values and recipes (spec 29 §2–§5, §10). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class MpsGameTests {
    private static final double EPSILON = 1.0E-6;

    private MpsGameTests() {
    }

    private static ItemStack withModules(ItemStack stack, String... modules) {
        for (String module : modules) {
            ModularItems.addModule(stack, module);
        }
        return stack;
    }

    private static void assertNear(GameTestHelper helper, double actual, double expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) < EPSILON, message + ": expected " + expected + ", got "
                + actual);
    }

    /** Base + multiplier · slider with the pack config values; right-click modules only as the selected mode. */
    @GameTest(template = "empty_8x4x8")
    public static void moduleProperties(GameTestHelper helper) {
        ItemStack tool = withModules(MpsItems.POWER_TOOL.toStack(), MpsModules.PICKAXE, MpsModules.RAILGUN);
        assertNear(helper, ModularItems.computeProperty(tool, "Pickaxe Energy Consumption"), 100.0, "pick energy");
        ModularItems.setTweak(tool, MpsModules.PICKAXE, "Overclock", 0.5);
        assertNear(helper, ModularItems.computeProperty(tool, "Pickaxe Energy Consumption"), 550.0, "overclocked");
        assertNear(helper, ModularItems.computeProperty(tool, "Pickaxe Harvest Speed"), 16.5, "speed 8 + 17/2");
        ModularItems.setTweak(tool, MpsModules.PICKAXE, "Overclock", 3.0);
        assertNear(helper, ModularItems.computeProperty(tool, "Pickaxe Harvest Speed"), 25.0, "slider clamped to 1");
        assertNear(helper, ModularItems.computeProperty(tool, "Railgun Energy Cost"), 0.0, "railgun not selected");
        tool.set(MpsDataComponents.MODE.get(), MpsModules.RAILGUN);
        assertNear(helper, ModularItems.computeProperty(tool, "Railgun Energy Cost"), 500.0, "railgun as mode");
        ModularItems.setActive(tool, MpsModules.PICKAXE, false);
        assertNear(helper, ModularItems.computeProperty(tool, "Pickaxe Harvest Speed"), 0.0, "switched off");
        ItemStack head = withModules(MpsItems.POWER_ARMOR_HEAD.toStack(), MpsModules.SOLAR_GENERATOR);
        assertNear(helper, ModularItems.computeProperty(head, "Daytime Solar Energy Generation"), 100.0, "solar day");
        assertNear(helper, ModularItems.computeProperty(head, "Nighttime Solar Energy Generation"), 10.0,
                "solar night");
        ItemStack legs = withModules(MpsItems.POWER_ARMOR_LEGS.toStack(), MpsModules.KINETIC_GENERATOR,
                MpsModules.ELITE_BATTERY);
        ModularItems.setTweak(legs, MpsModules.ELITE_BATTERY, "Battery Size", 1.0);
        assertNear(helper, MpsEnergy.getMaxJoules(legs), 5000000.0, "elite battery 750k + 4.25M");
        assertNear(helper, ModularItems.computeProperty(legs, MpsModules.WEIGHT), 1000.0 + 10000.0, "weight");
        helper.assertTrue(ModularItems.getValidModules(MpsItems.POWER_ARMOR_FEET.toStack()).stream()
                .noneMatch(module -> module.getName().equals(MpsModules.JETPACK)), "jetpack only on the torso");
        helper.assertValueEqual(MpsModules.all().size(), 35, "35 modules");
        helper.succeed();
    }

    /** Shared pool: drain boots first, fill in the same order, capacity cap written back after a battery is gone. */
    @GameTest(template = "empty_8x4x8")
    public static void energyPool(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack feet = withModules(MpsItems.POWER_ARMOR_FEET.toStack(), MpsModules.BASIC_BATTERY);
        ItemStack head = withModules(MpsItems.POWER_ARMOR_HEAD.toStack(), MpsModules.BASIC_BATTERY);
        MpsEnergy.setJoules(feet, 15000.0);
        MpsEnergy.setJoules(head, 20000.0);
        player.setItemSlot(EquipmentSlot.FEET, feet);
        player.setItemSlot(EquipmentSlot.HEAD, head);
        feet = player.getItemBySlot(EquipmentSlot.FEET);
        head = player.getItemBySlot(EquipmentSlot.HEAD);
        assertNear(helper, MpsEnergy.getPlayerEnergy(player), 35000.0, "pool");
        MpsEnergy.drainPlayerEnergy(player, 10000.0);
        assertNear(helper, MpsEnergy.getJoules(feet), 5000.0, "boots pay alone");
        MpsEnergy.drainPlayerEnergy(player, 10000.0);
        assertNear(helper, MpsEnergy.getJoules(feet), 0.0, "boots emptied");
        assertNear(helper, MpsEnergy.getJoules(head), 15000.0, "rest from the helmet");
        MpsEnergy.givePlayerEnergy(player, 30000.0);
        assertNear(helper, MpsEnergy.getJoules(feet), 20000.0, "boots filled first");
        assertNear(helper, MpsEnergy.getJoules(head), 20000.0, "helmet full, surplus lost");
        ModularItems.removeModule(head, MpsModules.BASIC_BATTERY);
        assertNear(helper, MpsEnergy.getJoules(head), 0.0, "no battery, no energy");
        helper.assertTrue(!head.has(MpsDataComponents.ENERGY.get()), "cap written back");
        helper.succeed();
    }

    /** IC2: 50 J per EU; armor at most capacity/200 per call, the tool without limit; any tier is accepted. */
    @GameTest(template = "empty_8x4x8")
    public static void ic2Charging(GameTestHelper helper) {
        ItemStack armor = withModules(MpsItems.POWER_ARMOR_TORSO.toStack(), MpsModules.BASIC_BATTERY);
        helper.assertValueEqual(ElectricItems.charge(armor, 1000, 1, false, false), 2, "100 J = 2 EU per call");
        assertNear(helper, MpsEnergy.getJoules(armor), 100.0, "armor charge");
        helper.assertValueEqual(ElectricItems.charge(armor, 1000, 4, true, false), 398, "ignoring the limit fills");
        helper.assertValueEqual(ElectricItems.getCharge(armor), 400, "20 000 J = 400 EU");
        ItemStack tool = withModules(MpsItems.POWER_TOOL.toStack(), MpsModules.BASIC_BATTERY);
        helper.assertValueEqual(ElectricItems.charge(tool, 100, 1, false, true), 100, "simulated");
        assertNear(helper, MpsEnergy.getJoules(tool), 0.0, "simulation keeps the charge");
        helper.assertValueEqual(ElectricItems.charge(tool, 100, 1, false, false), 100, "tool without limit");
        helper.assertValueEqual(ElectricItems.discharge(tool, 40, 1, false, false), 40, "discharge");
        assertNear(helper, MpsEnergy.getJoules(tool), 3000.0, "5 000 - 2 000 J");
        helper.assertTrue(ElectricItems.canUse(tool, 59) && !ElectricItems.canUse(tool, 60), "canUse strictly more");
        ItemStack empty = MpsItems.POWER_TOOL.toStack();
        helper.assertValueEqual(ElectricItems.charge(empty, 100, 1, false, false), 0, "no battery, no charge");
        helper.succeed();
    }

    /** Install takes the cost and adds the capacitor charge; a second install is refused; creative is free. */
    @GameTest(template = "empty_8x4x8")
    public static void installAndSalvage(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().setItem(0, MpsItems.POWER_ARMOR_HEAD.toStack());
        player.getInventory().setItem(1, new ItemStack(MpsItems.LV_CAPACITOR.get(), 2));
        helper.assertTrue(TinkerActions.install(player, 0, MpsModules.BASIC_BATTERY), "installed");
        ItemStack head = player.getInventory().getItem(0);
        helper.assertTrue(ModularItems.hasModule(head, MpsModules.BASIC_BATTERY), "module in the item");
        helper.assertValueEqual(player.getInventory().getItem(1).getCount(), 1, "one capacitor taken");
        assertNear(helper, MpsEnergy.getJoules(head), 20000.0, "LV capacitor charge");
        helper.assertTrue(!TinkerActions.install(player, 0, MpsModules.BASIC_BATTERY), "already installed");
        helper.assertTrue(!TinkerActions.install(player, 0, MpsModules.JETPACK), "not valid for the helmet");
        helper.assertTrue(!TinkerActions.install(player, 0, MpsModules.NIGHT_VISION), "cost missing");
        player.getAbilities().instabuild = true;
        helper.assertTrue(TinkerActions.install(player, 0, MpsModules.NIGHT_VISION), "creative installs free");
        player.getAbilities().instabuild = false;
        TinkerActions.tweak(player, 0, MpsModules.BASIC_BATTERY, "Battery Size", 7.0);
        assertNear(helper, ModularItems.getModules(head).get(MpsModules.BASIC_BATTERY).tweak("Battery Size"), 1.0,
                "slider clamped");
        helper.assertTrue(TinkerActions.salvage(player, 0, MpsModules.BASIC_BATTERY, RandomSource.create(1)),
                "salvaged");
        helper.assertValueEqual(player.getInventory().getItem(1).getCount(), 1, "capacitor not refunded");
        helper.assertTrue(!ModularItems.hasModule(head, MpsModules.BASIC_BATTERY), "module removed");
        assertNear(helper, MpsEnergy.getJoules(head), 0.0, "energy gone with the battery");
        helper.succeed();
    }

    /** Refunds: each item with the chance, first onto matching stacks, then free slots. */
    @GameTest(template = "empty_8x4x8")
    public static void salvageChance(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().setItem(5, new ItemStack(MpsItems.SERVO.get(), 63));
        TinkerActions.giveWithChance(player, new ItemStack(MpsItems.SERVO.get(), 4), 1.0, RandomSource.create(0));
        helper.assertValueEqual(player.getInventory().getItem(5).getCount(), 64, "stack topped up");
        helper.assertValueEqual(player.getInventory().getItem(0).getCount(), 3, "rest in the first free slot");
        TinkerActions.giveWithChance(player, new ItemStack(MpsItems.SOLENOID.get(), 4), 0.0, RandomSource.create(0));
        helper.assertTrue(player.getInventory().items.stream().noneMatch(stack -> stack.is(MpsItems.SOLENOID.get())),
                "chance 0 gives nothing");
        int servos = 0;
        RandomSource random = RandomSource.create(42);
        for (int i = 0; i < 1000; i++) {
            Player other = helper.makeMockPlayer(GameType.SURVIVAL);
            TinkerActions.giveWithChance(other, new ItemStack(MpsItems.SERVO.get(), 1), TinkerActions.SALVAGE_CHANCE,
                    random);
            servos += other.getInventory().countItem(MpsItems.SERVO.get());
        }
        helper.assertTrue(servos > 860 && servos < 940, "about 90 % of 1000: " + servos);
        helper.succeed();
    }

    /** Protection min(6, plating + shield while the pool pays), ratio 0.04 per point, absorb max (int)armor·75. */
    @GameTest(template = "empty_8x4x8")
    public static void armorValues(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack torso = withModules(MpsItems.POWER_ARMOR_TORSO.toStack(), MpsModules.IRON_PLATING,
                MpsModules.ENERGY_SHIELD, MpsModules.BASIC_BATTERY);
        ModularItems.setTweak(torso, MpsModules.IRON_PLATING, "Plating Thickness", 0.5);
        ModularItems.setTweak(torso, MpsModules.ENERGY_SHIELD, "Field Strength", 0.5);
        player.setItemSlot(EquipmentSlot.CHEST, torso);
        torso = player.getItemBySlot(EquipmentSlot.CHEST);
        DamageSource source = player.damageSources().generic();
        ArmorProperties empty = MpsItems.POWER_ARMOR_TORSO.get().getProperties(player, torso, source, 4.0, 2);
        assertNear(helper, empty.absorbRatio, 0.1, "2.5 points without energy");
        helper.assertValueEqual(empty.absorbMax, 150, "(int) 2.5 · 75");
        MpsEnergy.setJoules(torso, 251.0);
        ArmorProperties shielded = MpsItems.POWER_ARMOR_TORSO.get().getProperties(player, torso, source, 4.0, 2);
        assertNear(helper, shielded.absorbRatio, 0.22, "2.5 + 3 points with energy > 250 J");
        ModularItems.setTweak(torso, MpsModules.IRON_PLATING, "Plating Thickness", 1.0);
        ArmorProperties capped = MpsItems.POWER_ARMOR_TORSO.get().getProperties(player, torso, source, 4.0, 2);
        assertNear(helper, capped.absorbRatio, 0.24, "capped at 6");
        helper.assertValueEqual(capped.absorbMax, 450, "6 · 75");
        helper.assertValueEqual(MpsItems.POWER_ARMOR_TORSO.get().getArmorDisplay(torso), 6, "display");
        MpsEnergy.setJoules(torso, 20000.0);
        MpsItems.POWER_ARMOR_TORSO.get().damageArmor(player, torso, source, 3, 2);
        assertNear(helper, MpsEnergy.getJoules(torso), 20000.0 - 3 * 250.0, "250 J per damage");
        helper.succeed();
    }

    @GameTest(template = "empty_8x4x8")
    public static void recipes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack wiring = MpsItems.WIRING.toStack();
        ItemStack iron = new ItemStack(Items.IRON_INGOT);
        ItemStack result = craft(level, 3, List.of(ItemStack.EMPTY, wiring, ItemStack.EMPTY, wiring, iron,
                ItemStack.EMPTY, ItemStack.EMPTY, iron, wiring));
        helper.assertTrue(result.is(MpsItems.POWER_TOOL.get()), "power tool");
        ItemStack nugget = new ItemStack(Items.GOLD_NUGGET);
        ItemStack wires = craft(level, 3, List.of(nugget, new ItemStack(Items.REDSTONE), nugget));
        helper.assertTrue(wires.is(MpsItems.WIRING.get()) && wires.getCount() == 8, "8 wiring");
        ItemStack cable = Ic2Items.cable(CableType.COPPER).toStack();
        ItemStack silver = new ItemStack(dev.craft.gregtech.GtItems.SILVER_INGOT.get());
        ItemStack gtWires = craft(level, 3, List.of(cable, cable, cable, silver, silver, silver, cable, cable,
                cable));
        helper.assertTrue(gtWires.is(MpsItems.WIRING.get()) && gtWires.getCount() == 2, "2 wiring (GT)");
        ItemStack back = craft(level, 1, List.of(MpsItems.PLATING_BASIC.toStack()));
        helper.assertTrue(back.is(Items.IRON_INGOT) && back.getCount() == 5, "plating back to 5 iron");
        ItemStack lapis = new ItemStack(Items.LAPIS_LAZULI);
        ItemStack table = craft(level, 3, List.of(iron, lapis, iron, lapis, new ItemStack(Items.EMERALD), lapis,
                iron, lapis, iron));
        helper.assertTrue(table.is(MpsItems.TINKER_TABLE.get()), "tinker table");
        helper.succeed();
    }

    private static ItemStack craft(ServerLevel level, int width, List<ItemStack> items) {
        List<ItemStack> grid = new ArrayList<>(items);
        grid.addAll(Collections.nCopies((width - grid.size() % width) % width, ItemStack.EMPTY));
        CraftingInput input = CraftingInput.of(width, grid.size() / width, grid);
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
                .map(recipe -> recipe.value().assemble(input, level.registryAccess()))
                .orElse(ItemStack.EMPTY);
    }
}
