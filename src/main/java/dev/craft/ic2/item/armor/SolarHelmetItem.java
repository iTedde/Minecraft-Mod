package dev.craft.ic2.item.armor;

import dev.craft.ic2.generator.SolarPanelBlockEntity;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** IC2 {@code ItemArmorSolarHelmet}: 1 EU per tick into the electric chest item while the sun is visible. */
public class SolarHelmetItem extends Ic2ArmorItem {
    public SolarHelmetItem(Properties properties) {
        super(Ic2ArmorMaterials.SOLAR, Type.HELMET, properties.stacksTo(1));
    }

    @Override
    protected void onArmorTick(Level level, Player player, ItemStack stack) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!level.isClientSide() && ElectricItems.isElectric(chest) && SolarPanelBlockEntity.isSunVisible(level,
                new BlockPos((int) player.getX(), (int) player.getY() + 1, (int) player.getZ()))) {
            ElectricItems.charge(chest, 1, Integer.MAX_VALUE, true, false);
        }
    }
}
