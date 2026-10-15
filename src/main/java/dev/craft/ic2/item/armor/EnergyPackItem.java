package dev.craft.ic2.item.armor;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;

/** IC2 {@code ItemArmorBatpack}/{@code ItemArmorLappack}: chest storage that charges held tools, no protection. */
public class EnergyPackItem extends ElectricArmorItem {
    public EnergyPackItem(Holder<ArmorMaterial> material, Properties properties, int maxCharge, int transferLimit,
                          int tier) {
        super(material, Type.CHESTPLATE, properties, maxCharge, transferLimit, tier);
    }

    @Override
    public boolean canProvideEnergy() {
        return true;
    }

    @Override
    public double getDamageAbsorptionRatio() {
        return 0.0;
    }

    @Override
    public int getEnergyPerDamage() {
        return 0;
    }
}
