package dev.craft.ic2.item.armor;

import net.minecraft.world.item.ItemStack;

/**
 * CF backpack (IC2 {@code ItemArmorCFPack}): utility chest armor with the BatPack look whose damage counts used pellets
 * (260 = empty). The foam sprayer draws from it, the canning machine refills it. Repairable by combining two packs.
 */
public class CfPackItem extends Ic2ArmorItem {
    public static final int MAX_DAMAGE = 260;
    /** Damage of an empty pack (as crafted). */
    public static final int EMPTY_DAMAGE = 259;

    public CfPackItem(Properties properties) {
        super(Ic2ArmorMaterials.BATPACK, Type.CHESTPLATE, properties.durability(MAX_DAMAGE));
    }

    /** IC2 {@code getCFPellet}: uses one pellet unless the pack is empty. */
    public boolean getCfPellet(ItemStack pack) {
        if (pack.getDamageValue() < pack.getMaxDamage() - 1) {
            pack.setDamageValue(pack.getDamageValue() + 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return true;
    }
}
