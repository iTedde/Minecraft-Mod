package dev.craft.mps;

import java.util.List;

import dev.craft.mps.item.ModularItem;
import dev.craft.mps.module.MpsModules;
import dev.craft.mps.module.PowerModule;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Server side of the Tinker Table requests (spec 29 §4, MPS {@code MusePacket*Request}). */
public final class TinkerActions {
    /** {@code Salvage Ratio} default, not set in the pack config. */
    public static final double SALVAGE_CHANCE = 0.9;

    private TinkerActions() {
    }

    /** The modular item in inventory slot {@code slot}, or empty. */
    public static ItemStack modularItemAt(Player player, int slot) {
        Inventory inventory = player.getInventory();
        if (slot < 0 || slot >= inventory.getContainerSize()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = inventory.getItem(slot);
        return stack.getItem() instanceof ModularItem ? stack : ItemStack.EMPTY;
    }

    /**
     * {@code MusePacketInstallModuleRequest}: when the module is missing and the cost is in the inventory, or always in
     * creative mode (also re-installing, which resets the sliders). Capacitors in the cost charge the item.
     */
    public static boolean install(Player player, int slot, String moduleName) {
        ItemStack stack = modularItemAt(player, slot);
        PowerModule module = MpsModules.get(moduleName);
        if (stack.isEmpty() || module == null
                || !module.isValidFor(((ModularItem) stack.getItem()).getTarget())) {
            player.sendSystemMessage(Component.translatable("message.craft.mps.module_disallowed"));
            return false;
        }
        List<ItemStack> cost = module.getInstallCost();
        boolean creative = player.getAbilities().instabuild;
        if (!(!ModularItems.hasModule(stack, moduleName) && hasInInventory(cost, player.getInventory())) && !creative) {
            return false;
        }
        ModularItems.addModule(stack, moduleName);
        for (ItemStack item : cost) {
            double bonus = capacitorCharge(item);
            if (bonus > 0.0) {
                MpsEnergy.setJoules(stack, MpsEnergy.getJoules(stack) + bonus);
            }
        }
        if (!creative) {
            deleteFromInventory(cost, player.getInventory());
        }
        return true;
    }

    /** Joules a capacitor in the install cost adds: LV 20 000, MV 100 000, HV 750 000. */
    private static double capacitorCharge(ItemStack stack) {
        if (stack.is(MpsItems.LV_CAPACITOR.get())) {
            return 20000.0;
        }
        if (stack.is(MpsItems.MV_CAPACITOR.get())) {
            return 100000.0;
        }
        return stack.is(MpsItems.HV_CAPACITOR.get()) ? 750000.0 : 0.0;
    }

    /** {@code MusePacketSalvageModuleRequest}: every cost item back with 90 %, capacitors never. */
    public static boolean salvage(Player player, int slot, String moduleName, RandomSource random) {
        ItemStack stack = modularItemAt(player, slot);
        PowerModule module = MpsModules.get(moduleName);
        if (stack.isEmpty() || module == null || !ModularItems.removeModule(stack, moduleName)) {
            return false;
        }
        for (ItemStack refund : module.getInstallCost()) {
            giveWithChance(player, refund, SALVAGE_CHANCE, random);
        }
        return true;
    }

    public static void tweak(Player player, int slot, String moduleName, String tweakName, double value) {
        ItemStack stack = modularItemAt(player, slot);
        if (!stack.isEmpty()) {
            ModularItems.setTweak(stack, moduleName, tweakName, value);
        }
    }

    /** {@code hasInInventory}: every cost stack counted over the whole inventory. */
    public static boolean hasInInventory(List<ItemStack> cost, Inventory inventory) {
        for (ItemStack needed : cost) {
            int found = 0;
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                if (ItemStack.isSameItem(stack, needed)) {
                    found += stack.getCount();
                }
            }
            if (found < needed.getCount()) {
                return false;
            }
        }
        return true;
    }

    private static void deleteFromInventory(List<ItemStack> cost, Inventory inventory) {
        for (ItemStack needed : cost) {
            int remaining = needed.getCount();
            for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
                ItemStack stack = inventory.getItem(i);
                if (ItemStack.isSameItem(stack, needed)) {
                    int take = Math.min(stack.getCount(), remaining);
                    stack.shrink(take);
                    remaining -= take;
                    if (stack.isEmpty()) {
                        inventory.setItem(i, ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    /**
     * {@code giveOrDropItemWithChance}: each item gets through with {@code chance}, first onto matching stacks, then
     * into empty slots of the main inventory (the original also filled empty armor slots, spec 29 §14), the rest is
     * dropped. Capacitors are destroyed.
     */
    static void giveWithChance(Player player, ItemStack items, double chance, RandomSource random) {
        if (capacitorCharge(items) > 0.0) {
            return;
        }
        int count = items.getCount();
        List<ItemStack> main = player.getInventory().items;
        for (int i = 0; i < main.size() && count > 0; i++) {
            ItemStack stack = main.get(i);
            if (ItemStack.isSameItemSameComponents(stack, items) && stack.isStackable()
                    && stack.getCount() < stack.getMaxStackSize()) {
                while (count > 0 && stack.getCount() < stack.getMaxStackSize()) {
                    count--;
                    if (random.nextDouble() < chance) {
                        stack.grow(1);
                    }
                }
            }
        }
        for (int i = 0; i < main.size() && count > 0; i++) {
            if (main.get(i).isEmpty()) {
                int max = items.getMaxStackSize();
                int given = 0;
                while (count > 0 && given < max) {
                    count--;
                    if (random.nextDouble() < chance) {
                        given++;
                    }
                }
                if (given > 0) {
                    main.set(i, items.copyWithCount(given));
                }
            }
        }
        for (int i = 0; i < count; i++) {
            if (random.nextDouble() < chance) {
                player.drop(items.copyWithCount(1), false);
            }
        }
    }
}
