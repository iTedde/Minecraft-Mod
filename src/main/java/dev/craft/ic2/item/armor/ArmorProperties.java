package dev.craft.ic2.item.armor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/**
 * Forge {@code ISpecialArmor.ArmorProperties} with its damage calculation {@link #applyArmor}, 1:1 with Forge
 * 6.6.2.534 (including its quirks). Damage is handled in units of 1/25.
 */
public final class ArmorProperties implements Comparable<ArmorProperties> {
    /** Forge armor inventory order: slot index 0 = boots … 3 = helmet. */
    public static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD
    };

    public static final ArmorProperties NONE = new ArmorProperties(0, 0.0, 0);

    public int priority;
    public int absorbMax;
    public double absorbRatio;
    public int slot;

    public ArmorProperties(int priority, double absorbRatio, int absorbMax) {
        this.priority = priority;
        this.absorbRatio = absorbRatio;
        this.absorbMax = absorbMax;
    }

    public ArmorProperties copy() {
        return new ArmorProperties(priority, absorbRatio, absorbMax);
    }

    public static boolean wearsSpecialArmor(Player player) {
        for (EquipmentSlot slot : SLOTS) {
            if (player.getItemBySlot(slot).getItem() instanceof SpecialArmor) {
                return true;
            }
        }
        return false;
    }

    /**
     * Applies the worn armor to {@code damage} and damages the armor pieces.
     *
     * @return the remaining damage; IC2's integer carry-over between hits is not needed with float health
     */
    public static float applyArmor(Player player, DamageSource source, float amount) {
        double damage = amount * 25.0;
        List<ArmorProperties> values = new ArrayList<>();
        for (int x = 0; x < SLOTS.length; x++) {
            ItemStack stack = player.getItemBySlot(SLOTS[x]);
            if (stack.isEmpty()) {
                continue;
            }
            ArmorProperties prop = null;
            if (stack.getItem() instanceof SpecialArmor armor) {
                prop = armor.getProperties(player, stack, source, damage / 25.0, x).copy();
            } else if (stack.getItem() instanceof ArmorItem armor && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
                prop = new ArmorProperties(0, armor.getDefense() / 25.0,
                        stack.getMaxDamage() + 1 - stack.getDamageValue());
            }
            if (prop != null) {
                prop.slot = x;
                values.add(prop);
            }
        }
        if (!values.isEmpty()) {
            ArmorProperties[] props = values.toArray(new ArmorProperties[0]);
            standardizeList(props, damage);
            int level = props[0].priority;
            double ratio = 0.0;
            for (ArmorProperties prop : props) {
                if (level != prop.priority) {
                    damage -= damage * ratio;
                    ratio = 0.0;
                    level = prop.priority;
                }
                ratio += prop.absorbRatio;
                double absorb = damage * prop.absorbRatio;
                if (absorb > 0.0) {
                    EquipmentSlot slot = SLOTS[prop.slot];
                    ItemStack stack = player.getItemBySlot(slot);
                    int itemDamage = (int) (absorb / 25.0 < 1.0 ? 1.0 : absorb / 25.0);
                    if (stack.getItem() instanceof SpecialArmor armor) {
                        armor.damageArmor(player, stack, source, itemDamage, prop.slot);
                    } else {
                        stack.hurtAndBreak(itemDamage, player, slot);
                    }
                }
            }
            damage -= damage * ratio;
        }
        return (float) (damage / 25.0);
    }

    private static void standardizeList(ArmorProperties[] armor, double damage) {
        Arrays.sort(armor);
        int start = 0;
        double total = 0.0;
        int priority = armor[0].priority;
        int pStart = 0;
        boolean pChange = false;
        boolean pFinished = false;
        for (int x = 0; x < armor.length; x++) {
            total += armor[x].absorbRatio;
            if (x != armor.length - 1 && armor[x].priority == priority) {
                continue;
            }
            if (armor[x].priority != priority) {
                total -= armor[x].absorbRatio;
                x--;
                pChange = true;
            }
            if (total > 1.0) {
                for (int y = start; y <= x; y++) {
                    double newRatio = armor[y].absorbRatio / total;
                    if (newRatio * damage > armor[y].absorbMax) {
                        armor[y].absorbRatio = armor[y].absorbMax / damage;
                        total = 0.0;
                        for (int z = pStart; z <= y; z++) {
                            total += armor[z].absorbRatio;
                        }
                        start = y + 1;
                        x = y;
                        break;
                    }
                    armor[y].absorbRatio = newRatio;
                    pFinished = true;
                }
                if (pChange && pFinished) {
                    damage -= damage * total;
                    total = 0.0;
                    start = x + 1;
                    priority = armor[start].priority;
                    pStart = start;
                    pChange = false;
                    pFinished = false;
                    if (damage <= 0.0) {
                        for (int y = x + 1; y < armor.length; y++) {
                            armor[y].absorbRatio = 0.0;
                        }
                        break;
                    }
                }
            } else {
                for (int y = start; y <= x; y++) {
                    total -= armor[y].absorbRatio;
                    if (damage * armor[y].absorbRatio > armor[y].absorbMax) {
                        armor[y].absorbRatio = armor[y].absorbMax / damage;
                    }
                    total += armor[y].absorbRatio;
                }
                damage -= damage * total;
                total = 0.0;
                if (x != armor.length - 1) {
                    start = x + 1;
                    priority = armor[start].priority;
                    pStart = start;
                    pChange = false;
                    if (damage <= 0.0) {
                        for (int y = x + 1; y < armor.length; y++) {
                            armor[y].absorbRatio = 0.0;
                        }
                        break;
                    }
                }
            }
        }
    }

    /** Higher priority first, then by {@code absorbMax · 100 / ratio} ascending (Forge truncates to int). */
    @Override
    public int compareTo(ArmorProperties o) {
        if (o.priority != priority) {
            return o.priority - priority;
        }
        double left = absorbRatio == 0.0 ? 0.0 : absorbMax * 100.0 / absorbRatio;
        double right = o.absorbRatio == 0.0 ? 0.0 : o.absorbMax * 100.0 / o.absorbRatio;
        return (int) (left - right);
    }
}
