package dev.craft.mps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.craft.mps.item.ModularItem;
import dev.craft.mps.module.ModuleData;
import dev.craft.mps.module.MpsModules;
import dev.craft.mps.module.PowerModule;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** MPS {@code MuseItemUtils}/{@code ModuleManager}: modules of a modular item and the property sums (spec 29 §3). */
public final class ModularItems {
    /** {@code MuseItemUtils.itemsEquipped}: boots, legs, chest, helmet, held item. */
    private static final EquipmentSlot[] EQUIPPED = {
            EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD, EquipmentSlot.MAINHAND
    };

    private ModularItems() {
    }

    public static boolean isModular(ItemStack stack) {
        return stack.getItem() instanceof ModularItem;
    }

    public static Map<String, ModuleData> getModules(ItemStack stack) {
        return stack.getOrDefault(MpsDataComponents.MODULES.get(), Map.of());
    }

    public static boolean hasModule(ItemStack stack, String name) {
        return getModules(stack).containsKey(name);
    }

    /** Valid modules of the item in registration order ({@code getValidModulesForItem}). */
    public static List<PowerModule> getValidModules(ItemStack stack) {
        List<PowerModule> modules = new ArrayList<>();
        if (stack.getItem() instanceof ModularItem item) {
            for (PowerModule module : MpsModules.all()) {
                if (module.isValidFor(item.getTarget())) {
                    modules.add(module);
                }
            }
        }
        return modules;
    }

    /** Installed modules in registration order ({@code getItemInstalledModules}). */
    public static List<PowerModule> getInstalledModules(ItemStack stack) {
        List<PowerModule> modules = new ArrayList<>();
        for (PowerModule module : getValidModules(stack)) {
            if (hasModule(stack, module.getName())) {
                modules.add(module);
            }
        }
        return modules;
    }

    /** {@code itemAddModule}: a new module, active, all sliders 0. */
    public static void addModule(ItemStack stack, String name) {
        Map<String, ModuleData> modules = new HashMap<>(getModules(stack));
        modules.put(name, ModuleData.NEW);
        stack.set(MpsDataComponents.MODULES.get(), modules);
    }

    public static boolean removeModule(ItemStack stack, String name) {
        Map<String, ModuleData> modules = getModules(stack);
        if (!modules.containsKey(name)) {
            return false;
        }
        Map<String, ModuleData> copy = new HashMap<>(modules);
        copy.remove(name);
        if (copy.isEmpty()) {
            stack.remove(MpsDataComponents.MODULES.get());
        } else {
            stack.set(MpsDataComponents.MODULES.get(), copy);
        }
        return true;
    }

    /** {@code toggleModule}: only installed modules change. */
    public static void setActive(ItemStack stack, String name, boolean active) {
        ModuleData data = getModules(stack).get(name);
        if (data != null) {
            put(stack, name, data.withActive(active));
        }
    }

    /** {@code MusePacketTweakRequest}: clamped to 0…1, only for installed modules. */
    public static void setTweak(ItemStack stack, String name, String tweak, double value) {
        ModuleData data = getModules(stack).get(name);
        if (data != null) {
            put(stack, name, data.withTweak(tweak, Math.min(1.0, Math.max(0.0, value))));
        }
    }

    private static void put(ItemStack stack, String name, ModuleData data) {
        Map<String, ModuleData> modules = new HashMap<>(getModules(stack));
        modules.put(name, data);
        stack.set(MpsDataComponents.MODULES.get(), modules);
    }

    public static String getMode(ItemStack stack) {
        return stack.getOrDefault(MpsDataComponents.MODE.get(), "");
    }

    /**
     * {@code itemHasActiveModule}: right-click modules only as the selected mode, the others when installed and
     * switched on.
     */
    public static boolean hasActiveModule(ItemStack stack, String name) {
        PowerModule module = MpsModules.get(name);
        if (module == null) {
            return false;
        }
        if (module.getKind() == PowerModule.Kind.RIGHT_CLICK) {
            return name.equals(getMode(stack));
        }
        ModuleData data = getModules(stack).get(name);
        return data != null && data.active();
    }

    /** {@code ModuleManager.computeModularProperty}: sum over the active modules. */
    public static double computeProperty(ItemStack stack, String property) {
        Map<String, ModuleData> modules = getModules(stack);
        double value = 0.0;
        if (modules.isEmpty()) {
            return value;
        }
        for (PowerModule module : MpsModules.all()) {
            ModuleData data = modules.get(module.getName());
            if (data != null && hasActiveModule(stack, module.getName())) {
                value = module.apply(data, property, value);
            }
        }
        return value;
    }

    /** The worn and held items in {@code itemsEquipped} order, empty slots skipped. */
    public static List<ItemStack> equipped(Player player) {
        List<ItemStack> stacks = new ArrayList<>(EQUIPPED.length);
        for (EquipmentSlot slot : EQUIPPED) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    /** {@code getPlayerWeight}: total weight of all equipped modular items. */
    public static double getPlayerWeight(Player player) {
        double weight = 0.0;
        for (ItemStack stack : equipped(player)) {
            if (isModular(stack)) {
                weight += computeProperty(stack, MpsModules.WEIGHT);
            }
        }
        return weight;
    }

    /** Whether the player wears a modular armor piece or holds the power tool (spec 29 §6.8, decision §15). */
    public static boolean usesPowersuit(Player player) {
        for (ItemStack stack : equipped(player)) {
            if (isModular(stack)) {
                return true;
            }
        }
        return false;
    }
}
