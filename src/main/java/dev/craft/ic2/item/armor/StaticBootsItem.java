package dev.craft.ic2.item.armor;

import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * IC2 {@code ItemArmorStaticBoots}: every 5 blocks walked (straight line, truncated coordinates) up to 3 EU into the
 * electric chest item. The last position is IC2's NBT {@code x}/{@code z}.
 */
public class StaticBootsItem extends Ic2ArmorItem {
    public StaticBootsItem(Properties properties) {
        super(Ic2ArmorMaterials.RUBBER, Type.BOOTS, properties.stacksTo(1));
    }

    @Override
    protected void onArmorTick(Level level, Player player, ItemStack stack) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (level.isClientSide() || !ElectricItems.isElectric(chest)) {
            return;
        }
        int x = (int) player.getX();
        int z = (int) player.getZ();
        boolean isNotWalking = player.getVehicle() != null || player.isInWater();
        Ic2DataComponents.Position last = stack.get(Ic2DataComponents.LAST_POSITION.get());
        if (last == null || isNotWalking) {
            last = new Ic2DataComponents.Position(x, z);
            stack.set(Ic2DataComponents.LAST_POSITION.get(), last);
        }
        double distance = Math.sqrt((last.x() - x) * (last.x() - x) + (last.z() - z) * (last.z() - z));
        if (distance >= 5.0) {
            stack.set(Ic2DataComponents.LAST_POSITION.get(), new Ic2DataComponents.Position(x, z));
            ElectricItems.charge(chest, Math.min(3, (int) distance / 5), Integer.MAX_VALUE, true, false);
        }
    }
}
